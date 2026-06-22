package com.example.backend.vo; // 声明 VO 包

import lombok.Data; // Lombok 注解

import java.time.LocalDateTime; // Java 8 时间 API

/**
 * 教师视图对象
 * 与教师实体字段一一对应，前端展示用
 */
@Data // 自动生成 Getter/Setter
public class TeacherVO {

    private Long id; // 教师 ID

    private String teacherNo; // 教师工号

    private String name; // 教师姓名

    private String gender; // 性别

    private String title; // 职称

    private String department; // 院系

    private String phone; // 联系电话

    private LocalDateTime createTime; // 入职时间
}
