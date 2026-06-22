package com.example.backend.mapper; // 声明 Mapper 接口包

import com.baomidou.mybatisplus.core.mapper.BaseMapper; // MyBatis-Plus 基础 Mapper
import com.example.backend.entity.Student; // Student 实体类
import org.apache.ibatis.annotations.Mapper; // MyBatis Mapper 标识注解
import org.apache.ibatis.annotations.Param; // 参数绑定注解，用于 SQL 中的参数名映射
import org.apache.ibatis.annotations.Select; // 自定义查询注解，直接写 SQL

import java.util.List; // Java 集合框架

/**
 * 学生 Mapper 接口
 * 包含基础 CRUD 和自定义搜索方法
 */
@Mapper // 标记为 MyBatis Mapper
public interface StudentMapper extends BaseMapper<Student> {

    /**
     * 根据关键字搜索学生（按姓名或学号模糊匹配）
     * @param keyword 搜索关键字，可为 null
     * @return 匹配的学生列表
     */
    @Select("SELECT * FROM student WHERE name LIKE CONCAT('%', #{keyword}, '%') OR student_no LIKE CONCAT('%', #{keyword}, '%')") // 自定义 SQL 查询
    List<Student> searchByKeyword(@Param("keyword") String keyword); // @Param 将方法参数映射到 SQL 中的 #{keyword}
}
