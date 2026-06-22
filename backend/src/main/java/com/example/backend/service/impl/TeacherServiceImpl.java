package com.example.backend.service.impl; // 声明服务实现包

import cn.hutool.core.bean.BeanUtil; // Hutool 属性复制
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper; // Lambda 条件构造器
import com.baomidou.mybatisplus.core.metadata.IPage; // 分页接口
import com.baomidou.mybatisplus.extension.plugins.pagination.Page; // 分页对象
import com.example.backend.common.BusinessException; // 业务异常
import com.example.backend.common.ResultCode; // 状态码
import com.example.backend.dto.TeacherDTO; // 教师 DTO
import com.example.backend.dto.TeacherQueryDTO; // 教师查询 DTO
import com.example.backend.entity.Teacher; // 教师实体
import com.example.backend.mapper.TeacherMapper; // 教师 Mapper
import com.example.backend.service.TeacherService; // 教师服务接口
import com.example.backend.vo.TeacherVO; // 教师视图对象
import lombok.RequiredArgsConstructor; // 构造器注入
import lombok.extern.slf4j.Slf4j; // 日志
import org.springframework.stereotype.Service; // Service 注解
import org.springframework.transaction.annotation.Transactional; // 事务

/**
 * 教师服务实现类
 * 负责教师信息的增删改查和分页查询
 */
@Slf4j // 日志
@Service // Service Bean
@RequiredArgsConstructor // 构造器注入
public class TeacherServiceImpl implements TeacherService { // 实现接口

    private final TeacherMapper teacherMapper; // 教师 Mapper

    /**
     * 分页条件查询教师列表
     */
    @Override
    public IPage<TeacherVO> pageQuery(TeacherQueryDTO dto) {
        Page<Teacher> page = new Page<>(dto.getPageNum(), dto.getPageSize()); // 创建分页对象
        LambdaQueryWrapper<Teacher> queryWrapper = new LambdaQueryWrapper<>(); // 条件构造器
        // 按姓名模糊搜索
        if (dto.getName() != null && !dto.getName().isEmpty()) { // name 有值
            queryWrapper.like(Teacher::getName, dto.getName()); // 模糊查询
        }
        // 按院系模糊搜索
        if (dto.getDepartment() != null && !dto.getDepartment().isEmpty()) { // department 有值
            queryWrapper.like(Teacher::getDepartment, dto.getDepartment()); // 模糊查询
        }
        queryWrapper.orderByDesc(Teacher::getCreateTime); // 按创建时间倒序
        IPage<Teacher> teacherPage = teacherMapper.selectPage(page, queryWrapper); // 执行分页
        return teacherPage.convert(t -> BeanUtil.copyProperties(t, TeacherVO.class)); // 转换为 VO
    }

    /**
     * 根据 ID 查询教师详情
     */
    @Override
    public TeacherVO getById(Long id) {
        Teacher teacher = teacherMapper.selectById(id); // 按主键查询
        if (teacher == null) { // 不存在
            throw new BusinessException(ResultCode.BUSINESS_ERROR.getCode(), "教师不存在"); // 抛异常
        }
        return BeanUtil.copyProperties(teacher, TeacherVO.class); // 转 VO 返回
    }

    /**
     * 新增教师
     */
    @Override
    @Transactional(rollbackFor = Exception.class) // 事务
    public void save(TeacherDTO dto) {
        Teacher teacher = BeanUtil.copyProperties(dto, Teacher.class); // 属性复制
        teacherMapper.insert(teacher); // 插入
        log.info("新增教师成功，工号：{}，姓名：{}", dto.getTeacherNo(), dto.getName()); // 记录日志
    }

    /**
     * 修改教师信息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(TeacherDTO dto) {
        Teacher teacher = BeanUtil.copyProperties(dto, Teacher.class); // 属性复制
        teacherMapper.updateById(teacher); // 按主键更新
        log.info("修改教师信息成功"); // 记录日志
    }

    /**
     * 删除教师
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        teacherMapper.deleteById(id); // 按主键删除
        log.info("删除教师成功，ID：{}", id); // 记录日志
    }
}
