package com.example.backend.service.impl; // 声明服务实现包

import cn.hutool.core.bean.BeanUtil; // Hutool 属性复制
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper; // Lambda 条件构造器
import com.baomidou.mybatisplus.core.metadata.IPage; // 分页接口
import com.baomidou.mybatisplus.extension.plugins.pagination.Page; // 分页对象
import com.example.backend.common.BusinessException; // 业务异常
import com.example.backend.common.ResultCode; // 状态码
import com.example.backend.dto.CourseDTO; // 课程 DTO
import com.example.backend.dto.CourseQueryDTO; // 课程查询 DTO
import com.example.backend.entity.Course; // 课程实体
import com.example.backend.entity.Teacher; // 教师实体
import com.example.backend.mapper.CourseMapper; // 课程 Mapper
import com.example.backend.mapper.TeacherMapper; // 教师 Mapper
import com.example.backend.service.CourseService; // 课程服务接口
import com.example.backend.vo.CourseVO; // 课程视图对象
import lombok.RequiredArgsConstructor; // 构造器注入
import lombok.extern.slf4j.Slf4j; // 日志
import org.springframework.stereotype.Service; // Service 注解
import org.springframework.transaction.annotation.Transactional; // 事务

import java.util.List; // 列表
import java.util.stream.Collectors; // 流收集器

/**
 * 课程服务实现类
 * 负责课程信息的增删改查和分页查询
 */
@Slf4j // 日志
@Service // Service Bean
@RequiredArgsConstructor // 构造器注入
public class CourseServiceImpl implements CourseService { // 实现接口

    private final CourseMapper courseMapper;   // 课程 Mapper
    private final TeacherMapper teacherMapper; // 教师 Mapper（查询教师姓名用）

    /**
     * 分页条件查询课程列表（含教师姓名）
     */
    @Override
    public IPage<CourseVO> pageQuery(CourseQueryDTO dto) {
        Page<Course> page = new Page<>(dto.getPageNum(), dto.getPageSize()); // 创建分页对象
        LambdaQueryWrapper<Course> queryWrapper = new LambdaQueryWrapper<>(); // 条件构造器
        // 按课程名称模糊搜索
        if (dto.getName() != null && !dto.getName().isEmpty()) { // name 有值
            queryWrapper.like(Course::getName, dto.getName()); // 模糊查询
        }
        // 按教师 ID 精确搜索
        if (dto.getTeacherId() != null) { // teacherId 有值
            queryWrapper.eq(Course::getTeacherId, dto.getTeacherId()); // 精确查询
        }
        // 按学期精确搜索
        if (dto.getSemester() != null && !dto.getSemester().isEmpty()) { // semester 有值
            queryWrapper.eq(Course::getSemester, dto.getSemester()); // 精确查询
        }
        queryWrapper.orderByDesc(Course::getCreateTime); // 按创建时间倒序

        IPage<Course> coursePage = courseMapper.selectPage(page, queryWrapper); // 执行分页
        return coursePage.convert(course -> { // 转换为 CourseVO
            CourseVO vo = BeanUtil.copyProperties(course, CourseVO.class); // 复制基础属性
            // 查询授课教师姓名
            if (course.getTeacherId() != null) { // 有教师
                Teacher teacher = teacherMapper.selectById(course.getTeacherId()); // 查询教师
                if (teacher != null) { // 教师存在
                    vo.setTeacherName(teacher.getName()); // 设置教师姓名
                }
            }
            return vo; // 返回 VO
        });
    }

    /**
     * 根据 ID 查询课程详情
     */
    @Override
    public CourseVO getById(Long id) {
        Course course = courseMapper.selectById(id); // 按主键查询
        if (course == null) { // 不存在
            throw new BusinessException(ResultCode.BUSINESS_ERROR.getCode(), "课程不存在"); // 抛异常
        }
        CourseVO vo = BeanUtil.copyProperties(course, CourseVO.class); // 复制属性
        // 查询授课教师姓名
        if (course.getTeacherId() != null) {
            Teacher teacher = teacherMapper.selectById(course.getTeacherId());
            if (teacher != null) {
                vo.setTeacherName(teacher.getName()); // 设置教师姓名
            }
        }
        return vo; // 返回
    }

    /**
     * 新增课程
     */
    @Override
    @Transactional(rollbackFor = Exception.class) // 事务
    public void save(CourseDTO dto) {
        Course course = BeanUtil.copyProperties(dto, Course.class); // 属性复制
        courseMapper.insert(course); // 插入
        log.info("新增课程成功，编号：{}，名称：{}", dto.getCourseNo(), dto.getName()); // 记录日志
    }

    /**
     * 修改课程信息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(CourseDTO dto) {
        Course course = BeanUtil.copyProperties(dto, Course.class); // 属性复制
        courseMapper.updateById(course); // 按主键更新
        log.info("修改课程信息成功"); // 记录日志
    }

    /**
     * 删除课程
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        courseMapper.deleteById(id); // 按主键删除
        log.info("删除课程成功，ID：{}", id); // 记录日志
    }

    /**
     * 根据教师 ID 查询该教师的所有课程（含教师姓名）
     */
    @Override
    public List<CourseVO> getByTeacherId(Long teacherId) {
        // 调用 Mapper 中的自定义查询方法
        List<Course> courses = courseMapper.selectByTeacherId(teacherId); // 按教师ID查询课程
        // 转换为 CourseVO 列表
        return courses.stream().map(course -> { // 流转换
            CourseVO vo = BeanUtil.copyProperties(course, CourseVO.class); // 复制属性
            // 查询教师姓名
            Teacher teacher = teacherMapper.selectById(teacherId);
            if (teacher != null) {
                vo.setTeacherName(teacher.getName()); // 设置教师姓名
            }
            return vo; // 返回 VO
        }).collect(Collectors.toList()); // 收集为列表
    }
}
