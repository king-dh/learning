package com.example.backend.vo; // 声明 VO 包

import lombok.Data; // Lombok 注解

import java.time.LocalDateTime; // Java 8 时间 API

/**
 * 学生视图对象
 * 在学生实体的基础上增加了班级名称字段，便于前端展示
 */
@Data // 自动生成 Getter/Setter
public class StudentVO {

    private Long id; // 学生 ID

    private String studentNo; // 学号

    private String name; // 姓名

    private String gender; // 性别

    private Integer age; // 年龄

    private String phone; // 手机号

    private String email; // 邮箱

    private Long classId; // 班级 ID

    private String className; // 班级名称（通过 LEFT JOIN 查询得到）

    private LocalDateTime createTime; // 创建时间
}
