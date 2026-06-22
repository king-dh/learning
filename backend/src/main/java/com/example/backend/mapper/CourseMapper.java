package com.example.backend.mapper; // 声明 Mapper 接口包

import com.baomidou.mybatisplus.core.mapper.BaseMapper; // MyBatis-Plus 基础 Mapper
import com.example.backend.entity.Course; // Course 实体类
import org.apache.ibatis.annotations.Mapper; // MyBatis Mapper 标识注解
import org.apache.ibatis.annotations.Param; // 参数绑定注解
import org.apache.ibatis.annotations.Select; // 自定义查询注解

import java.util.List; // Java 集合框架

/**
 * 课程 Mapper 接口
 * 包含基础 CRUD 和按教师查询的方法
 */
@Mapper // 标记为 MyBatis Mapper
public interface CourseMapper extends BaseMapper<Course> {

    /**
     * 根据授课教师 ID 查询该教师的所有课程
     * @param teacherId 教师 ID
     * @return 课程列表
     */
    @Select("SELECT * FROM course WHERE teacher_id = #{teacherId}") // 按教师ID查询课程
    List<Course> selectByTeacherId(@Param("teacherId") Long teacherId); // 参数绑定
}
