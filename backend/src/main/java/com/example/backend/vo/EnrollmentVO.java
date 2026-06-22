package com.example.backend.vo; // 声明 VO 包

import lombok.Data; // Lombok 注解

import java.time.LocalDateTime; // Java 8 时间 API

/**
 * 选课视图对象
 * 在选课实体的基础上联表查询了学生名、课程名、教师名、学分等信息
 * 前端一次请求即可获得完整的选课展示数据
 */
@Data // 自动生成 Getter/Setter
public class EnrollmentVO {

    private Long id; // 选课记录 ID

    private Long studentId; // 学生 ID

    private String studentName; // 学生姓名

    private Long courseId; // 课程 ID

    private String courseNo; // 课程编号

    private String courseName; // 课程名称

    private String teacherName; // 授课教师姓名

    private Double credit; // 课程学分

    private String semester; // 学期

    private LocalDateTime createTime; // 选课时间
}
