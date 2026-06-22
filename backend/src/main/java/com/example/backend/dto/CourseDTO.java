package com.example.backend.dto; // 声明 DTO 包

import lombok.Data; // Lombok 注解

/**
 * 课程信息 DTO（新增/修改课程时使用）
 */
@Data // 自动生成 Getter/Setter
public class CourseDTO {

    private Long id; // 课程ID（修改时使用，新增时可为空）

    private String courseNo; // 课程编号，如 "CS101"

    private String name; // 课程名称，如 "高等数学"（前端传 courseName 也会映射至此）

    private Double credit; // 学分数，如 3.0

    private Long teacherId; // 授课教师 ID

    private String semester; // 开课学期，如 "2024-2025-1"

    private String description; // 课程简介/描述

    private String classroom; // 上课教室，如 "B301"

    private Integer maxStudents; // 最大容纳学生数
}
