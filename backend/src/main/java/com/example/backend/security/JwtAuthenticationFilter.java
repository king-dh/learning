package com.example.backend.security; // 声明安全包

import jakarta.servlet.FilterChain; // 过滤器链
import jakarta.servlet.ServletException; // Servlet 异常
import jakarta.servlet.http.HttpServletRequest; // HTTP 请求
import jakarta.servlet.http.HttpServletResponse; // HTTP 响应
import lombok.RequiredArgsConstructor; // Lombok 构造器注入
import lombok.extern.slf4j.Slf4j; // 日志注解
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken; // 用户名密码认证令牌
import org.springframework.security.core.authority.SimpleGrantedAuthority; // 简单权限类
import org.springframework.security.core.context.SecurityContextHolder; // 安全上下文持有者
import org.springframework.stereotype.Component; // Spring 组件注解
import org.springframework.util.StringUtils; // Spring 字符串工具类
import org.springframework.web.filter.OncePerRequestFilter; // 每次请求只执行一次的过滤器基类

import java.io.IOException; // IO 异常
import java.util.List; // 列表

/**
 * JWT 认证过滤器
 * 继承 OncePerRequestFilter 确保每个请求只被过滤一次
 * 拦截所有请求，从 Header 中提取 JWT Token 并验证，验证通过则设置认证信息
 * 执行时机：在 Spring Security 过滤器链中，位于 UsernamePasswordAuthenticationFilter 之前
 */
@Slf4j // 自动生成日志对象
@Component // 声明为 Spring Bean
@RequiredArgsConstructor // 为 final 字段生成构造器并注入
public class JwtAuthenticationFilter extends OncePerRequestFilter { // 继承 OncePerRequestFilter

    private final JwtUtil jwtUtil; // 注入 JWT 工具类

    /**
     * 过滤器的核心方法
     * 对每个 HTTP 请求执行 JWT 认证逻辑
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,  // HTTP 请求对象
                                    HttpServletResponse response, // HTTP 响应对象
                                    FilterChain filterChain) // 过滤器链，用于放行请求
            throws ServletException, IOException {

        // OPTIONS 预检请求直接放行（浏览器跨域预检请求不需要认证）
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) { // 判断是否为 OPTIONS 方法
            filterChain.doFilter(request, response); // 放行，交给下一个过滤器
            return; // 直接返回，不执行后续逻辑
        }

        // 从请求头中提取 JWT Token（格式：Bearer xxxxxx）
        String token = extractToken(request);

        // 如果 Token 存在且验证有效
        if (token != null && jwtUtil.validateToken(token)) { // token 不为空且未过期
            // 从 Token 中获取用户名
            String username = jwtUtil.getUsernameFromToken(token);
            // 从 Token 中获取用户角色
            String role = jwtUtil.getRoleFromToken(token);

            // 构建 Spring Security 认证令牌
            // 参数1：用户名（principal），参数2：凭证（credentials，可设为 null），参数3：权限列表
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            username, // 主身份信息（用户名）
                            null, // 凭证信息（JWT 认证不需要密码）
                            List.of(new SimpleGrantedAuthority("ROLE_" + role)) // 权限列表（Spring Security 需要 ROLE_ 前缀）
                    );

            // 将认证信息存入 SecurityContextHolder（Spring Security 的全局上下文）
            // 存入之后，后续的 Controller 和 Service 就能通过 SecurityContextHolder 获取当前用户信息
            SecurityContextHolder.getContext().setAuthentication(authentication);

            log.debug("JWT 认证成功，用户名：{}，角色：{}", username, role); // 记录调试日志
        }

        // 无论是否携带 Token，都放行请求（未认证的请求会被 SecurityConfig 拦截）
        filterChain.doFilter(request, response);
    }

    /**
     * 从 HTTP 请求头中提取 JWT Token
     * 格式：Authorization: Bearer <token>
     * @param request HTTP 请求对象
     * @return Token 字符串（不含 "Bearer " 前缀），如果不存在则返回 null
     */
    private String extractToken(HttpServletRequest request) {
        // 从请求头获取 "Authorization" 的值
        String bearerToken = request.getHeader("Authorization"); // 获取 Authorization 头

        // 判断是否以 "Bearer " 开头（Spring Security 标准格式）
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) { // 有值且以 Bearer 开头
            return bearerToken.substring(7); // 截取 "Bearer " 之后的内容（7 个字符），获取纯 Token
        }

        return null; // 没有合法的 Token
    }
}
