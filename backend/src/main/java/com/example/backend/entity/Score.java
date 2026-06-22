package com.example.backend.entity; // 声明实体类包

import com.baomidou.mybatisplus.annotation.FieldFill; // MyBatis-Plus 字段填充策略
import com.baomidou.mybatisplus.annotation.IdType; // MyBatis-Plus 主键类型
import com.baomidou.mybatisplus.annotation.TableField; // MyBatis-Plus 字段映射注解
import com.baomidou.mybatisplus.annotation.TableId; // MyBatis-Plus 主键注解
import com.baomidou.mybatisplus.annotation.TableName; // MyBatis-Plus 表名注解
import lombok.Data; // Lombok 注解

import java.time.LocalDateTime; // Java 8 时间 API

/**
 * 成绩实体类
 * 对应数据库表 score，存储学生的课程成绩
 * 通过 studentId + courseId + semester 唯一确定一条记录（同一学生同一课程同一学期只有一条记录）
 */
@Data // 自动生成 Getter/Setter/toString 等方法
@TableName("score") // 映射到数据库表 score
public class Score {

    @TableId(type = IdType.AUTO) // 主键，自增策略
    private Long id; // 成绩记录唯一标识

    private Long studentId; // 学生 ID，外键关联 student 表

    private Long courseId; // 课程 ID，外键关联 course 表

    private Double score; // 考试分数，范围 0-100

    private String semester; // 学期，如 "2024-2025-1"

    @TableField(fill = FieldFill.INSERT) // 插入数据时自动填充当前时间
    private LocalDateTime createTime; // 成绩录入时间
}
