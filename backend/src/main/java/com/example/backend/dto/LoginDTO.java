package com.example.backend.dto; // 声明 DTO 包

import jakarta.validation.constraints.NotBlank; // 校验注解：不能为空白字符串
import lombok.Data; // Lombok 注解

/**
 * 登录请求 DTO（Data Transfer Object，数据传输对象）
 * 前端提交登录表单时传递的数据结构
 */
@Data // 自动生成 Getter/Setter
public class LoginDTO {

    @NotBlank(message = "用户名不能为空") // 校验：用户名不能为 null 或空字符串或纯空格
    private String username; // 登录用户名

    @NotBlank(message = "密码不能为空") // 校验：密码不能为空
    private String password; // 登录密码（明文，后端验证）
}
