package com.example.backend.service; // 声明服务接口包

import com.example.backend.dto.LoginDTO; // 登录请求 DTO
import com.example.backend.dto.RegisterDTO; // 注册请求 DTO
import com.example.backend.vo.LoginVO; // 登录响应 VO

/**
 * 认证服务接口
 * 负责用户登录和注册的业务逻辑
 */
public interface AuthService { // 服务接口，定义业务方法

    /**
     * 用户登录
     * 验证用户名和密码，生成 JWT Token 返回
     * @param loginDTO 登录请求参数（用户名、密码）
     * @return 登录结果，包含 Token、用户名、角色等
     */
    LoginVO login(LoginDTO loginDTO);

    /**
     * 用户注册
     * 检查用户名唯一性，加密密码后保存用户
     * @param registerDTO 注册请求参数（用户名、密码、姓名、角色）
     */
    void register(RegisterDTO registerDTO);
}
