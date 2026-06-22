package com.example.backend.security; // 声明安全包

import com.example.backend.common.Result; // 统一响应结果
import com.example.backend.common.ResultCode; // 状态码枚举
import com.fasterxml.jackson.databind.ObjectMapper; // JSON 序列化
import jakarta.servlet.http.HttpServletResponse; // HTTP 响应
import lombok.RequiredArgsConstructor; // Lombok 构造器注入
import org.springframework.context.annotation.Bean; // Spring Bean 注解
import org.springframework.context.annotation.Configuration; // Spring 配置类注解
import org.springframework.security.authentication.AuthenticationManager; // 认证管理器
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration; // 认证配置
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity; // 启用方法级别安全注解
import org.springframework.security.config.annotation.web.builders.HttpSecurity; // HTTP 安全配置构建器
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity; // 启用 Web 安全
import org.springframework.security.config.http.SessionCreationPolicy; // Session 创建策略
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder; // BCrypt 密码加密器
import org.springframework.security.crypto.password.PasswordEncoder; // 密码加密器接口
import org.springframework.security.web.SecurityFilterChain; // 安全过滤器链
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter; // 用户名密码认证过滤器

import java.io.PrintWriter; // 输出流

/**
 * Spring Security 核心配置类
 * 配置认证规则、授权规则、密码加密方式、JWT 过滤器等
 * @EnableWebSecurity：启用 Spring Security 的 Web 安全支持
 * @EnableMethodSecurity：启用方法级别的权限控制（@PreAuthorize 等注解）
 */
@Configuration // 声明这是一个 Spring 配置类
@EnableWebSecurity // 启用 Spring Security Web 安全
@EnableMethodSecurity // 启用 @PreAuthorize/@PostAuthorize 等注解的方法级权限控制
@RequiredArgsConstructor // 为 final 字段生成构造器并注入
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter; // 注入 JWT 认证过滤器

    /**
     * 配置安全过滤器链（Spring Security 6.x 的核心配置方法）
     * 替代了旧版本的 WebSecurityConfigurerAdapter
     * @param http HttpSecurity 对象，用于配置安全规则
     * @return SecurityFilterChain 安全过滤器链
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 禁用 CSRF（跨站请求伪造）保护
                // JWT 认证模式下不存在 CSRF 风险，因为服务端不依赖 Cookie/Session
                .csrf(csrf -> csrf.disable())

                // 设置 Session 管理策略为 STATELESS（无状态）
                // JWT 认证模式下不需要服务端保存 Session，所有信息都在 Token 中
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 配置请求授权规则
                .authorizeHttpRequests(auth -> auth
                        // 以下路径允许匿名访问（无需登录）
                        .requestMatchers("/api/auth/login").permitAll()          // 登录接口
                        .requestMatchers("/api/auth/register").permitAll()       // 注册接口
                        .requestMatchers("/swagger-ui.html", "/swagger-ui/**").permitAll() // Swagger UI 页面
                        .requestMatchers("/v3/api-docs/**").permitAll() // OpenAPI JSON 数据
                        // 除了上述放行的路径，其余所有请求都需要认证
                        .anyRequest().authenticated())

                // 配置异常处理：自定义 401 和 403 的 JSON 响应
                .exceptionHandling(ex -> ex
                        // 未认证（无Token或Token无效）返回 401
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setContentType("application/json;charset=UTF-8"); // 设置响应类型
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // HTTP 401
                            PrintWriter writer = response.getWriter(); // 获取输出流
                            writer.write(new ObjectMapper().writeValueAsString(Result.fail(401, "未登录或Token已过期，请重新登录")));
                            writer.flush(); // 刷新输出
                        })
                        // 无权限（角色不足）返回 403
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setContentType("application/json;charset=UTF-8"); // 设置响应类型
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN); // HTTP 403
                            PrintWriter writer = response.getWriter(); // 获取输出流
                            writer.write(new ObjectMapper().writeValueAsString(Result.fail(ResultCode.FORBIDDEN)));
                            writer.flush(); // 刷新输出
                        }))

                // 将 JWT 过滤器添加到 Spring Security 过滤器链中
                // 在 UsernamePasswordAuthenticationFilter 之前执行
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build(); // 构建并返回 SecurityFilterChain
    }

    /**
     * 密码编码器 Bean
     * BCryptPasswordEncoder 使用 BCrypt 强哈希算法加密密码
     * 注册时加密存储，登录时比对密文
     * @return PasswordEncoder 实例
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // BCrypt 是一种不可逆的加密算法，安全性高
    }

    /**
     * 认证管理器 Bean
     * AuthenticationManager 是 Spring Security 认证的核心接口
     * 负责执行用户认证（验证用户名密码）
     * @param authenticationConfiguration Spring Security 认证配置
     * @return AuthenticationManager 实例
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
            throws Exception {
        return authenticationConfiguration.getAuthenticationManager(); // 从配置中获取 AuthenticationManager
    }
}
