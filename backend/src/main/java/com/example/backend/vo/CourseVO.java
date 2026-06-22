package com.example.backend.vo; // 声明 VO 包

import lombok.Data; // Lombok 注解

import java.time.LocalDateTime; // Java 8 时间 API

/**
 * 课程视图对象
 * 在课程实体的基础上增加了授课教师姓名，便于前端展示
 */
@Data // 自动生成 Getter/Setter
public class CourseVO {

    private Long id; // 课程 ID

    private String courseNo; // 课程编号

    private String name; // 课程名称

    private Double credit; // 学分

    private Long teacherId; // 授课教师 ID

    private String teacherName; // 授课教师姓名（LEFT JOIN teacher 表获取）

    private String semester; // 开课学期

    private String description; // 课程简介

    private String classroom; // 上课教室

    private Integer maxStudents; // 最大容纳学生数

    private LocalDateTime createTime; // 创建时间
}
