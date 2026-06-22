package com.example.backend.service.impl; // 声明服务实现包

import cn.hutool.core.bean.BeanUtil; // Hutool 属性复制
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper; // Lambda 条件构造器
import com.example.backend.common.BusinessException; // 业务异常
import com.example.backend.common.ResultCode; // 状态码
import com.example.backend.dto.EnrollmentDTO; // 选课 DTO
import com.example.backend.entity.Course; // 课程实体
import com.example.backend.entity.CourseEnrollment; // 选课实体
import com.example.backend.entity.Student; // 学生实体
import com.example.backend.entity.SysUser; // 系统用户实体
import com.example.backend.mapper.CourseEnrollmentMapper; // 选课 Mapper
import com.example.backend.mapper.CourseMapper; // 课程 Mapper
import com.example.backend.mapper.StudentMapper; // 学生 Mapper
import com.example.backend.mapper.SysUserMapper; // 用户 Mapper
import com.example.backend.service.EnrollmentService; // 选课服务接口
import com.example.backend.vo.EnrollmentVO; // 选课视图对象
import lombok.RequiredArgsConstructor; // 构造器注入
import lombok.extern.slf4j.Slf4j; // 日志
import org.springframework.stereotype.Service; // Service 注解
import org.springframework.transaction.annotation.Transactional; // 事务

import java.util.List; // 列表

/**
 * 选课服务实现类
 * 负责选课和退课的业务逻辑
 */
@Slf4j // 日志
@Service // Service Bean
@RequiredArgsConstructor // 构造器注入
public class EnrollmentServiceImpl implements EnrollmentService { // 实现接口

    private final CourseEnrollmentMapper courseEnrollmentMapper; // 选课 Mapper
    private final CourseMapper courseMapper;                     // 课程 Mapper
    private final StudentMapper studentMapper;                   // 学生 Mapper
    private final SysUserMapper sysUserMapper;                   // 用户 Mapper

    /**
     * 根据用户名查询其已选课程列表
     * 通过 sys_user.real_name 匹配 student.name 来查找学生
     */
    @Override
    public List<EnrollmentVO> getByUsername(String username) {
        // 先查 sys_user 获取 real_name
        LambdaQueryWrapper<SysUser> userWrapper = new LambdaQueryWrapper<>();
        userWrapper.eq(SysUser::getUsername, username);
        SysUser user = sysUserMapper.selectOne(userWrapper);
        if (user == null || user.getRealName() == null) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR.getCode(), "用户信息不完整");
        }
        // 通过 real_name 匹配 student.name
        LambdaQueryWrapper<Student> studentWrapper = new LambdaQueryWrapper<>();
        studentWrapper.eq(Student::getName, user.getRealName());
        Student student = studentMapper.selectOne(studentWrapper);
        if (student == null) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR.getCode(), "未找到关联的学生信息");
        }
        return getByStudentId(student.getId());
    }

    /**
     * 根据用户名获取对应的学生ID
     */
    @Override
    public Long getStudentIdByUsername(String username) {
        LambdaQueryWrapper<SysUser> userWrapper = new LambdaQueryWrapper<>();
        userWrapper.eq(SysUser::getUsername, username);
        SysUser user = sysUserMapper.selectOne(userWrapper);
        if (user == null || user.getRealName() == null) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR.getCode(), "用户信息不完整");
        }
        LambdaQueryWrapper<Student> studentWrapper = new LambdaQueryWrapper<>();
        studentWrapper.eq(Student::getName, user.getRealName());
        Student student = studentMapper.selectOne(studentWrapper);
        if (student == null) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR.getCode(), "未找到关联的学生信息");
        }
        return student.getId();
    }

    /**
     * 查询某个学生的已选课程列表（联表查询含课程名、教师名、学分）
     */
    @Override
    public List<EnrollmentVO> getByStudentId(Long studentId) {
        // 调用 Mapper 中自定义的联表查询
        return courseEnrollmentMapper.selectByStudentId(studentId); // 返回含完整信息的列表
    }

    /**
     * 选课（学生选择一门课程）
     * 步骤：1.验证学生和课程是否存在 2.检查是否重复选课 3.保存选课记录
     */
    @Override
    @Transactional(rollbackFor = Exception.class) // 事务
    public void enroll(EnrollmentDTO dto) {
        // 第1步：验证学生是否存在
        Student student = studentMapper.selectById(dto.getStudentId()); // 查询学生
        if (student == null) { // 学生不存在
            throw new BusinessException(ResultCode.BUSINESS_ERROR.getCode(), "学生不存在"); // 抛异常
        }

        // 第2步：验证课程是否存在
        Course course = courseMapper.selectById(dto.getCourseId()); // 查询课程
        if (course == null) { // 课程不存在
            throw new BusinessException(ResultCode.BUSINESS_ERROR.getCode(), "课程不存在"); // 抛异常
        }

        // 第3步：检查是否已选过该课程（同一学生不能重复选同一门课）
        LambdaQueryWrapper<CourseEnrollment> queryWrapper = new LambdaQueryWrapper<>(); // 条件构造器
        queryWrapper.eq(CourseEnrollment::getStudentId, dto.getStudentId()); // 条件1：学生ID
        queryWrapper.eq(CourseEnrollment::getCourseId, dto.getCourseId());   // 条件2：课程ID
        if (courseEnrollmentMapper.selectCount(queryWrapper) > 0) { // 存在重复记录
            throw new BusinessException(ResultCode.BUSINESS_ERROR.getCode(), "已选过该课程，不能重复选择"); // 抛异常
        }

        // 第4步：保存选课记录
        CourseEnrollment enrollment = new CourseEnrollment(); // 创建选课实体
        enrollment.setStudentId(dto.getStudentId()); // 设置学生ID
        enrollment.setCourseId(dto.getCourseId());   // 设置课程ID
        courseEnrollmentMapper.insert(enrollment);   // 插入数据库

        log.info("选课成功，学生ID：{}，课程ID：{}，课程名：{}", dto.getStudentId(), dto.getCourseId(), course.getName());
    }

    /**
     * 退课（取消选课记录）
     */
    @Override
    @Transactional(rollbackFor = Exception.class) // 事务
    public void unenroll(Long id) {
        // 检查选课记录是否存在
        CourseEnrollment enrollment = courseEnrollmentMapper.selectById(id); // 按主键查询
        if (enrollment == null) { // 记录不存在
            throw new BusinessException(ResultCode.BUSINESS_ERROR.getCode(), "选课记录不存在"); // 抛异常
        }
        // 删除选课记录
        courseEnrollmentMapper.deleteById(id); // 按主键删除
        log.info("退课成功，选课记录ID：{}", id); // 记录日志
    }
}
