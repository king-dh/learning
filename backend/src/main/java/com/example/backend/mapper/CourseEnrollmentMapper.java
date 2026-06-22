package com.example.backend.mapper; // 声明 Mapper 接口包

import com.baomidou.mybatisplus.core.mapper.BaseMapper; // MyBatis-Plus 基础 Mapper
import com.example.backend.entity.CourseEnrollment; // CourseEnrollment 实体类
import com.example.backend.vo.EnrollmentVO; // EnrollmentVO 视图对象
import org.apache.ibatis.annotations.Mapper; // MyBatis Mapper 标识注解
import org.apache.ibatis.annotations.Param; // 参数绑定注解
import org.apache.ibatis.annotations.Select; // 自定义查询注解

import java.util.List; // Java 集合框架

/**
 * 选课 Mapper 接口
 * 包含基础 CRUD 和按学生ID联表查询的方法
 */
@Mapper // 标记为 MyBatis Mapper
public interface CourseEnrollmentMapper extends BaseMapper<CourseEnrollment> {

    /**
     * 根据学生 ID 查询该学生的所有选课记录（联表查询课程信息、教师信息）
     * 多表 LEFT JOIN 组合出完整的前端展示数据
     * @param studentId 学生 ID
     * @return 选课视图对象列表（包含课程名、教师名、学分等）
     */
    @Select("SELECT ce.*, st.name AS student_name, c.name AS course_name, " + // 查询选课信息 + 关联的名称
            "c.course_no AS courseNo, t.name AS teacher_name, c.credit AS credit, c.semester AS semester " +                  // 教师姓名和课程学分
            "FROM course_enrollment ce " +                                   // 主表：course_enrollment
            "LEFT JOIN student st ON ce.student_id = st.id " +              // 左连接 student 表
            "LEFT JOIN course c ON ce.course_id = c.id " +                  // 左连接 course 表
            "LEFT JOIN teacher t ON c.teacher_id = t.id " +                 // 左连接 teacher 表（通过 course 的 teacher_id）
            "WHERE ce.student_id = #{studentId}")                           // 按学生ID过滤
    List<EnrollmentVO> selectByStudentId(@Param("studentId") Long studentId); // 返回 EnrollmentVO 列表
}
