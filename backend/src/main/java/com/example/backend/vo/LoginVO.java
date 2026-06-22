package com.example.backend.vo; // 声明 VO 包

import lombok.Data; // Lombok 注解

/**
 * 登录响应 VO（View Object，视图对象）
 * 登录成功后返回给前端的数据结构
 */
@Data // 自动生成 Getter/Setter
public class LoginVO {

    private String token;   // JWT Token 字符串，前端后续请求需携带此 Token
    private String username; // 用户名
    private String realName; // 用户真实姓名
    private String role;    // 用户角色
}
