package com.example.backend.dto; // 声明 DTO 包

import jakarta.validation.constraints.Max; // 校验注解：最大值
import jakarta.validation.constraints.Min; // 校验注解：最小值
import lombok.Data; // Lombok 注解

/**
 * 成绩信息 DTO（新增/修改成绩时使用）
 */
@Data // 自动生成 Getter/Setter
public class ScoreDTO {

    private Long studentId; // 学生 ID

    private Long courseId; // 课程 ID

    @Min(value = 0, message = "分数不能小于0")   // 校验：分数最小值 0
    @Max(value = 100, message = "分数不能大于100") // 校验：分数最大值 100
    private Double score; // 考试分数，范围 0-100

    private String semester; // 学期，如 "2024-2025-1"
}
