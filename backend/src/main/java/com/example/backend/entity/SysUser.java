package com.example.backend.entity; // 声明实体类包

import com.baomidou.mybatisplus.annotation.FieldFill; // MyBatis-Plus 字段填充策略
import com.baomidou.mybatisplus.annotation.IdType; // MyBatis-Plus 主键类型
import com.baomidou.mybatisplus.annotation.TableField; // MyBatis-Plus 字段映射注解
import com.baomidou.mybatisplus.annotation.TableId; // MyBatis-Plus 主键注解
import com.baomidou.mybatisplus.annotation.TableName; // MyBatis-Plus 表名注解
import lombok.Data; // Lombok 注解

import java.time.LocalDateTime; // Java 8 时间 API

/**
 * 系统用户实体类
 * 对应数据库表 sys_user，存储所有系统用户的登录和角色信息
 */
@Data // 自动生成 Getter/Setter/toString 等方法
@TableName("sys_user") // 映射到数据库表 sys_user
public class SysUser {

    @TableId(type = IdType.AUTO) // 主键，自增策略（数据库自动生成）
    private Long id; // 用户唯一标识

    private String username; // 登录用户名，用于认证登录

    private String password; // 登录密码，存储 BCrypt 加密后的密文

    private String role; // 用户角色：ADMIN（管理员）/ TEACHER（教师）/ STUDENT（学生）

    private String realName; // 用户真实姓名

    private Integer status; // 用户状态：1 表示启用，0 表示禁用

    @TableField(fill = FieldFill.INSERT) // 插入数据时自动填充当前时间
    private LocalDateTime createTime; // 创建时间
}
