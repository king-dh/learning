package com.example.backend.entity; // 声明实体类包

import com.baomidou.mybatisplus.annotation.FieldFill; // MyBatis-Plus 字段填充策略
import com.baomidou.mybatisplus.annotation.IdType; // MyBatis-Plus 主键类型
import com.baomidou.mybatisplus.annotation.TableField; // MyBatis-Plus 字段映射注解
import com.baomidou.mybatisplus.annotation.TableId; // MyBatis-Plus 主键注解
import com.baomidou.mybatisplus.annotation.TableName; // MyBatis-Plus 表名注解
import lombok.Data; // Lombok 注解

import java.time.LocalDateTime; // Java 8 时间 API

/**
 * 学生实体类
 * 对应数据库表 student，存储学生基本信息
 */
@Data // 自动生成 Getter/Setter/toString 等方法
@TableName("student") // 映射到数据库表 student
public class Student {

    @TableId(type = IdType.AUTO) // 主键，自增策略
    private Long id; // 学生唯一标识

    private String studentNo; // 学号，如 "20240001"，具有唯一性

    private String name; // 学生姓名

    private String gender; // 性别：男 / 女

    private Integer age; // 年龄

    private String phone; // 手机号码

    private String email; // 电子邮箱地址

    private Long classId; // 所属班级 ID，外键关联 class_info 表

    @TableField(fill = FieldFill.INSERT) // 插入数据时自动填充当前时间
    private LocalDateTime createTime; // 创建时间（入学时间）
}
