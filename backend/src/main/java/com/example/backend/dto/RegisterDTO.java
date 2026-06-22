package com.example.backend.dto; // 声明 DTO 包

import jakarta.validation.constraints.NotBlank; // 校验注解
import lombok.Data; // Lombok 注解

/**
 * 注册请求 DTO
 * 前端提交注册表单时传递的数据结构
 */
@Data // 自动生成 Getter/Setter
public class RegisterDTO {

    @NotBlank(message = "用户名不能为空")
    private String username; // 登录用户名，需要唯一

    @NotBlank(message = "密码不能为空")
    private String password; // 登录密码，后端会 BCrypt 加密后存储

    @NotBlank(message = "真实姓名不能为空")
    private String realName; // 用户真实姓名

    @NotBlank(message = "角色不能为空")
    private String role; // 用户角色：ADMIN / TEACHER / STUDENT
}
