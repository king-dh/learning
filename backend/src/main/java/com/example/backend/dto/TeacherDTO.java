package com.example.backend.dto; // 声明 DTO 包

import lombok.Data; // Lombok 注解

/**
 * 教师信息 DTO（新增/修改教师时使用）
 */
@Data // 自动生成 Getter/Setter
public class TeacherDTO {

    private String teacherNo; // 教师工号

    private String name; // 教师姓名

    private String gender; // 性别：男 / 女

    private String title; // 职称：教授 / 副教授 / 讲师 / 助教

    private String department; // 所属院系

    private String phone; // 联系电话
}
