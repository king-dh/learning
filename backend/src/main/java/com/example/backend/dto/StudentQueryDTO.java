package com.example.backend.dto; // 声明 DTO 包

import lombok.Data; // Lombok 注解

/**
 * 学生查询参数 DTO
 * 支持按姓名和学号模糊搜索 + 分页
 */
@Data // 自动生成 Getter/Setter
public class StudentQueryDTO {

    private String name; // 搜索关键字（按姓名模糊匹配）

    private String studentNo; // 搜索关键字（按学号模糊匹配）

    private Integer pageNum = 1; // 当前页码，默认第 1 页

    private Integer pageSize = 10; // 每页显示条数，默认 10 条
}
