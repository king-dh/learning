package com.example.backend.entity; // 声明实体类包

import com.baomidou.mybatisplus.annotation.FieldFill; // MyBatis-Plus 字段填充策略
import com.baomidou.mybatisplus.annotation.IdType; // MyBatis-Plus 主键类型
import com.baomidou.mybatisplus.annotation.TableField; // MyBatis-Plus 字段映射注解
import com.baomidou.mybatisplus.annotation.TableId; // MyBatis-Plus 主键注解
import com.baomidou.mybatisplus.annotation.TableName; // MyBatis-Plus 表名注解
import lombok.Data; // Lombok 注解

import java.time.LocalDateTime; // Java 8 时间 API

/**
 * 班级实体类
 * 对应数据库表 class_info，存储班级信息
 * 注：表名用 class_info 而不是 class，因为 class 是 MySQL 保留关键字
 */
@Data // 自动生成 Getter/Setter/toString 等方法
@TableName("class_info") // 映射到数据库表 class_info
public class ClassInfo {

    @TableId(type = IdType.AUTO) // 主键，自增策略
    private Long id; // 班级唯一标识

    private String className; // 班级名称，如 "软件工程1班"

    private String grade; // 所属年级，如 "2024级"

    private Long headTeacherId; // 班主任教师 ID，外键关联 teacher 表

    @TableField(fill = FieldFill.INSERT) // 插入数据时自动填充当前时间
    private LocalDateTime createTime; // 创建时间
}
