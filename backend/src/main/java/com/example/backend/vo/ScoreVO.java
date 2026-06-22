package com.example.backend.vo; // 声明 VO 包

import lombok.Data; // Lombok 注解

import java.time.LocalDateTime; // Java 8 时间 API

/**
 * 成绩视图对象
 * 在成绩实体的基础上增加了学生姓名和课程名称，便于前端展示
 */
@Data // 自动生成 Getter/Setter
public class ScoreVO {

    private Long id; // 成绩记录 ID

    private Long studentId; // 学生 ID

    private String studentName; // 学生姓名（LEFT JOIN student 表获取）

    private Long courseId; // 课程 ID

    private String courseName; // 课程名称（LEFT JOIN course 表获取）

    private Double score; // 考试分数

    private String semester; // 学期

    private LocalDateTime createTime; // 录入时间
}
