package com.example.backend.dto; // 声明 DTO 包

import lombok.Data; // Lombok 注解

/**
 * 班级查询参数 DTO
 * 支持按名称和年级搜索 + 分页
 */
@Data // 自动生成 Getter/Setter
public class ClassQueryDTO {

    private String className; // 按班级名称模糊搜索

    private String grade; // 按年级模糊搜索

    private Integer pageNum = 1; // 当前页码，默认第 1 页

    private Integer pageSize = 10; // 每页显示条数，默认 10 条
}
