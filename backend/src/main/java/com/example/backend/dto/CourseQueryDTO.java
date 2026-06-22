package com.example.backend.dto; // 声明 DTO 包

import lombok.Data; // Lombok 注解

/**
 * 课程查询参数 DTO
 * 支持按课程名称、教师ID、学期搜索 + 分页
 */
@Data // 自动生成 Getter/Setter
public class CourseQueryDTO {

    private String name; // 按课程名称模糊搜索

    private Long teacherId; // 按授课教师 ID 精确搜索

    private String semester; // 按学期精确搜索

    private Integer pageNum = 1; // 当前页码，默认第 1 页

    private Integer pageSize = 10; // 每页显示条数，默认 10 条
}
