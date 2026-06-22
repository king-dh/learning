package com.example.backend.dto; // 声明 DTO 包

import lombok.Data; // Lombok 注解

/**
 * 学生信息 DTO（新增/修改学生时使用）
 */
@Data // 自动生成 Getter/Setter
public class StudentDTO {

    private Long id; // 学生ID（修改时使用，新增时可为空）

    private String studentNo; // 学号

    private String name; // 学生姓名

    private String gender; // 性别：男 / 女

    private Integer age; // 年龄

    private String phone; // 手机号码

    private String email; // 电子邮箱

    private Long classId; // 所属班级 ID
}
