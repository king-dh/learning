package com.example.backend.controller; // 声明 Controller 包

import com.example.backend.common.Result; // 统一响应结果
import com.example.backend.dto.LoginDTO; // 登录请求 DTO
import com.example.backend.dto.RegisterDTO; // 注册请求 DTO
import com.example.backend.service.AuthService; // 认证服务
import com.example.backend.vo.LoginVO; // 登录响应 VO
import io.swagger.v3.oas.annotations.Operation; // Knife4j 接口描述注解
import io.swagger.v3.oas.annotations.tags.Tag; // Knife4j 分组标签
import jakarta.validation.Valid; // 开启 DTO 参数校验
import lombok.RequiredArgsConstructor; // 构造器注入
import org.springframework.web.bind.annotation.PostMapping; // POST 请求映射
import org.springframework.web.bind.annotation.RequestBody; // 请求体参数
import org.springframework.web.bind.annotation.RequestMapping; // 请求路径前缀
import org.springframework.web.bind.annotation.RestController; // REST 控制器

/**
 * 认证控制器
 * 处理登录和注册请求，这些接口不需要 JWT 认证
 */
@Tag(name = "认证管理", description = "用户登录和注册接口") // Knife4j 接口分组标签
@RestController // 标识为 REST 控制器，返回值自动序列化为 JSON
@RequestMapping("/api/auth") // 接口路径前缀
@RequiredArgsConstructor // 构造器注入
public class AuthController {

    private final AuthService authService; // 注入认证服务

    /**
     * 用户登录接口
     * POST /api/auth/login
     * 验证用户名和密码，返回 JWT Token
     */
    @Operation(summary = "用户登录") // Knife4j 接口说明
    @PostMapping("/login") // 处理 POST /api/auth/login 请求
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO loginDTO) { // @Valid 开启校验，@RequestBody 接收 JSON
        LoginVO loginVO = authService.login(loginDTO); // 调用登录服务
        return Result.ok(loginVO); // 返回成功结果（包含 Token）
    }

    /**
     * 用户注册接口
     * POST /api/auth/register
     * 注册新用户（用户名需唯一）
     */
    @Operation(summary = "用户注册")
    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody RegisterDTO registerDTO) { // @Valid 开启参数校验
        authService.register(registerDTO); // 调用注册服务
        return Result.ok(null); // 注册成功，无返回数据
    }
}
