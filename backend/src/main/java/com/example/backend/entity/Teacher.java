package com.example.backend.entity; // 声明实体类包

import com.baomidou.mybatisplus.annotation.FieldFill; // MyBatis-Plus 字段填充策略
import com.baomidou.mybatisplus.annotation.IdType; // MyBatis-Plus 主键类型
import com.baomidou.mybatisplus.annotation.TableField; // MyBatis-Plus 字段映射注解
import com.baomidou.mybatisplus.annotation.TableId; // MyBatis-Plus 主键注解
import com.baomidou.mybatisplus.annotation.TableName; // MyBatis-Plus 表名注解
import lombok.Data; // Lombok 注解

import java.time.LocalDateTime; // Java 8 时间 API

/**
 * 教师实体类
 * 对应数据库表 teacher，存储教师基本信息
 */
@Data // 自动生成 Getter/Setter/toString 等方法
@TableName("teacher") // 映射到数据库表 teacher
public class Teacher {

    @TableId(type = IdType.AUTO) // 主键，自增策略
    private Long id; // 教师唯一标识

    private String teacherNo; // 教师工号，如 "T2024001"

    private String name; // 教师姓名

    private String gender; // 性别：男 / 女

    private String title; // 职称：教授 / 副教授 / 讲师 / 助教

    private String department; // 所属院系，如 "计算机科学与技术学院"

    private String phone; // 联系电话

    @TableField(fill = FieldFill.INSERT) // 插入数据时自动填充当前时间
    private LocalDateTime createTime; // 创建时间（入职时间）
}
