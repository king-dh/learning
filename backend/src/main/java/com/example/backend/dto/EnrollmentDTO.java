package com.example.backend.dto; // 声明 DTO 包

import lombok.Data; // Lombok 注解

/**
 * 选课请求 DTO
 * 前端提交选课时传递的数据结构
 */
@Data // 自动生成 Getter/Setter
public class EnrollmentDTO {

    private Long studentId; // 学生 ID，标识哪位学生选课

    private Long courseId; // 课程 ID，标识选择哪门课程
}
