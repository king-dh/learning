package com.example.backend.service.impl; // 声明服务实现包

import cn.hutool.core.bean.BeanUtil; // Hutool 属性复制
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper; // Lambda 条件构造器
import com.baomidou.mybatisplus.core.metadata.IPage; // 分页接口
import com.baomidou.mybatisplus.extension.plugins.pagination.Page; // 分页对象
import com.example.backend.common.BusinessException; // 业务异常
import com.example.backend.common.ResultCode; // 状态码
import com.example.backend.dto.ClassDTO; // 班级 DTO
import com.example.backend.dto.ClassQueryDTO; // 班级查询 DTO
import com.example.backend.entity.ClassInfo; // 班级实体
import com.example.backend.entity.Student; // 学生实体
import com.example.backend.entity.Teacher; // 教师实体
import com.example.backend.mapper.ClassInfoMapper; // 班级 Mapper
import com.example.backend.mapper.StudentMapper; // 学生 Mapper
import com.example.backend.mapper.TeacherMapper; // 教师 Mapper
import com.example.backend.service.ClassService; // 班级服务接口
import com.example.backend.vo.ClassVO; // 班级视图对象
import com.example.backend.vo.StudentVO; // 学生视图对象
import lombok.RequiredArgsConstructor; // 构造器注入
import lombok.extern.slf4j.Slf4j; // 日志
import org.springframework.stereotype.Service; // Service 注解
import org.springframework.transaction.annotation.Transactional; // 事务

import java.util.List; // 列表
import java.util.stream.Collectors; // 流收集器

/**
 * 班级服务实现类
 * 负责班级的增删改查、分页和班级学生列表
 */
@Slf4j // 日志
@Service // Service Bean
@RequiredArgsConstructor // 构造器注入
public class ClassServiceImpl implements ClassService { // 实现接口

    private final ClassInfoMapper classInfoMapper; // 班级 Mapper
    private final TeacherMapper teacherMapper;   // 教师 Mapper（查询班主任姓名用）
    private final StudentMapper studentMapper;   // 学生 Mapper（查询班级学生用）

    /**
     * 分页条件查询班级列表（含班主任姓名、学生人数）
     */
    @Override
    public IPage<ClassVO> pageQuery(ClassQueryDTO dto) {
        Page<ClassInfo> page = new Page<>(dto.getPageNum(), dto.getPageSize()); // 分页对象
        LambdaQueryWrapper<ClassInfo> queryWrapper = new LambdaQueryWrapper<>(); // 条件构造器
        // 按班级名称模糊搜索
        if (dto.getClassName() != null && !dto.getClassName().isEmpty()) { // className 有值
            queryWrapper.like(ClassInfo::getClassName, dto.getClassName()); // 模糊查询
        }
        // 按年级模糊搜索
        if (dto.getGrade() != null && !dto.getGrade().isEmpty()) { // grade 有值
            queryWrapper.like(ClassInfo::getGrade, dto.getGrade()); // 模糊查询
        }
        queryWrapper.orderByDesc(ClassInfo::getCreateTime); // 按创建时间倒序

        IPage<ClassInfo> classPage = classInfoMapper.selectPage(page, queryWrapper); // 执行分页
        return classPage.convert(classInfo -> { // 转换为 ClassVO
            ClassVO vo = BeanUtil.copyProperties(classInfo, ClassVO.class); // 复制基础属性
            // 查询班主任姓名
            if (classInfo.getHeadTeacherId() != null) { // 有班主任
                Teacher teacher = teacherMapper.selectById(classInfo.getHeadTeacherId()); // 查询教师
                if (teacher != null) { // 教师存在
                    vo.setHeadTeacherName(teacher.getName()); // 设置班主任姓名
                }
            }
            // 统计该班级的学生人数
            LambdaQueryWrapper<Student> countWrapper = new LambdaQueryWrapper<>();
            countWrapper.eq(Student::getClassId, classInfo.getId()); // 条件：class_id = 班级ID
            vo.setStudentCount(Math.toIntExact(studentMapper.selectCount(countWrapper))); // 统计并设置
            return vo; // 返回 VO
        });
    }

    /**
     * 根据 ID 查询班级详情
     */
    @Override
    public ClassVO getById(Long id) {
        ClassInfo classInfo = classInfoMapper.selectById(id); // 查询班级
        if (classInfo == null) { // 不存在
            throw new BusinessException(ResultCode.BUSINESS_ERROR.getCode(), "班级不存在"); // 抛异常
        }

        ClassVO vo = BeanUtil.copyProperties(classInfo, ClassVO.class); // 复制基础属性
        // 查询班主任姓名
        if (classInfo.getHeadTeacherId() != null) {
            Teacher teacher = teacherMapper.selectById(classInfo.getHeadTeacherId());
            if (teacher != null) {
                vo.setHeadTeacherName(teacher.getName()); // 设置班主任姓名
            }
        }
        // 统计学生人数
        LambdaQueryWrapper<Student> countWrapper = new LambdaQueryWrapper<>();
        countWrapper.eq(Student::getClassId, id);
        vo.setStudentCount(Math.toIntExact(studentMapper.selectCount(countWrapper))); // 设置学生人数
        return vo; // 返回
    }

    /**
     * 新增班级
     */
    @Override
    @Transactional(rollbackFor = Exception.class) // 事务
    public void save(ClassDTO dto) {
        ClassInfo classInfo = BeanUtil.copyProperties(dto, ClassInfo.class); // 属性复制
        classInfoMapper.insert(classInfo); // 插入数据库
        log.info("新增班级成功，名称：{}", dto.getClassName()); // 记录日志
    }

    /**
     * 修改班级信息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(ClassDTO dto) {
        ClassInfo classInfo = BeanUtil.copyProperties(dto, ClassInfo.class); // 属性复制
        classInfoMapper.updateById(classInfo); // 按主键更新
        log.info("修改班级信息成功"); // 记录日志
    }

    /**
     * 删除班级
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        classInfoMapper.deleteById(id); // 按主键删除
        log.info("删除班级成功，ID：{}", id); // 记录日志
    }

    /**
     * 查询班级下的所有学生列表
     */
    @Override
    public List<StudentVO> getStudentsByClassId(Long classId) {
        // 构建查询条件：WHERE class_id = classId
        LambdaQueryWrapper<Student> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Student::getClassId, classId); // 按班级 ID 过滤
        // 查询学生列表
        List<Student> students = studentMapper.selectList(queryWrapper); // 获取学生列表
        // 转换为 StudentVO 列表（这里不联表查班级名称，因为已知班级）
        return students.stream().map(s -> BeanUtil.copyProperties(s, StudentVO.class)) // 逐个转换
                .collect(Collectors.toList()); // 收集为列表
    }
}
