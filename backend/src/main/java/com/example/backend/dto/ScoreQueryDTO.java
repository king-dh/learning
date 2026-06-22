package com.example.backend.dto; // 声明 DTO 包

import lombok.Data; // Lombok 注解

/**
 * 成绩查询参数 DTO
 * 支持按学生ID、课程ID、学期搜索 + 分页
 */
@Data // 自动生成 Getter/Setter
public class ScoreQueryDTO {

    private Long studentId; // 按学生 ID 精确搜索

    private Long courseId; // 按课程 ID 精确搜索

    private String semester; // 按学期精确搜索

    private Integer pageNum = 1; // 当前页码，默认第 1 页

    private Integer pageSize = 10; // 每页显示条数，默认 10 条
}
