package com.example.backend.dto; // 声明 DTO 包

import lombok.Data; // Lombok 注解

/**
 * 教师查询参数 DTO
 * 支持按姓名和院系搜索 + 分页
 */
@Data // 自动生成 Getter/Setter
public class TeacherQueryDTO {

    private String name; // 按教师姓名模糊搜索

    private String department; // 按院系模糊搜索

    private Integer pageNum = 1; // 当前页码，默认第 1 页

    private Integer pageSize = 10; // 每页显示条数，默认 10 条
}
