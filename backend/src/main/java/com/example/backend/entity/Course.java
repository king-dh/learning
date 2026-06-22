package com.example.backend.entity; // 声明实体类包

import com.baomidou.mybatisplus.annotation.FieldFill; // MyBatis-Plus 字段填充策略
import com.baomidou.mybatisplus.annotation.IdType; // MyBatis-Plus 主键类型
import com.baomidou.mybatisplus.annotation.TableField; // MyBatis-Plus 字段映射注解
import com.baomidou.mybatisplus.annotation.TableId; // MyBatis-Plus 主键注解
import com.baomidou.mybatisplus.annotation.TableName; // MyBatis-Plus 表名注解
import lombok.Data; // Lombok 注解

import java.time.LocalDateTime; // Java 8 时间 API

/**
 * 课程实体类
 * 对应数据库表 course，存储课程信息
 */
@Data // 自动生成 Getter/Setter/toString 等方法
@TableName("course") // 映射到数据库表 course
public class Course {

    @TableId(type = IdType.AUTO) // 主键，自增策略
    private Long id; // 课程唯一标识

    private String courseNo; // 课程编号，如 "CS101"，用于标识课程

    private String name; // 课程名称，如 "高等数学"

    private Double credit; // 学分数，如 3.0

    private Long teacherId; // 授课教师 ID，外键关联 teacher 表

    private String semester; // 开课学期，如 "2024-2025-1"（2024-2025学年第一学期）

    private String description; // 课程简介/描述

    private String classroom; // 上课教室，如 "B301"

    private Integer maxStudents; // 最大容纳学生数

    @TableField(fill = FieldFill.INSERT) // 插入数据时自动填充当前时间
    private LocalDateTime createTime; // 创建时间
}
