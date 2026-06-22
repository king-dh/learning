package com.example.backend.entity; // 声明实体类包

import com.baomidou.mybatisplus.annotation.FieldFill; // MyBatis-Plus 字段填充策略
import com.baomidou.mybatisplus.annotation.IdType; // MyBatis-Plus 主键类型
import com.baomidou.mybatisplus.annotation.TableField; // MyBatis-Plus 字段映射注解
import com.baomidou.mybatisplus.annotation.TableId; // MyBatis-Plus 主键注解
import com.baomidou.mybatisplus.annotation.TableName; // MyBatis-Plus 表名注解
import lombok.Data; // Lombok 注解

import java.time.LocalDateTime; // Java 8 时间 API

/**
 * 选课实体类
 * 对应数据库表 course_enrollment，记录学生选课关系
 * 通过 studentId + courseId 唯一确定一条记录（同一学生不能重复选同一课程）
 */
@Data // 自动生成 Getter/Setter/toString 等方法
@TableName("course_enrollment") // 映射到数据库表 course_enrollment
public class CourseEnrollment {

    @TableId(type = IdType.AUTO) // 主键，自增策略
    private Long id; // 选课记录唯一标识

    private Long studentId; // 学生 ID，外键关联 student 表

    private Long courseId; // 课程 ID，外键关联 course 表

    @TableField(fill = FieldFill.INSERT) // 插入数据时自动填充当前时间
    private LocalDateTime createTime; // 选课时间
}
