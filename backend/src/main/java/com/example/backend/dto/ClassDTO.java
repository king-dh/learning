package com.example.backend.dto; // 声明 DTO 包

import lombok.Data; // Lombok 注解

/**
 * 班级信息 DTO（新增/修改班级时使用）
 */
@Data // 自动生成 Getter/Setter
public class ClassDTO {

    private String className; // 班级名称，如 "软件工程1班"

    private String grade; // 所属年级，如 "2024级"

    private Long headTeacherId; // 班主任教师 ID，可为 null
}
