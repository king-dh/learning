package com.example.backend.vo; // 声明 VO 包

import lombok.Data; // Lombok 注解

import java.time.LocalDateTime; // Java 8 时间 API

/**
 * 班级视图对象
 * 在班级实体的基础上增加了班主任姓名和学生人数，便于前端展示
 */
@Data // 自动生成 Getter/Setter
public class ClassVO {

    private Long id; // 班级 ID

    private String className; // 班级名称

    private String grade; // 年级

    private Long headTeacherId; // 班主任 ID

    private String headTeacherName; // 班主任姓名

    private Integer studentCount; // 班级学生人数

    private LocalDateTime createTime; // 创建时间
}
