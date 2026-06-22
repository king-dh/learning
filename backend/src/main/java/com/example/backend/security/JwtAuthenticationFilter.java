/*
 * ================================================================
 * 文件位置:  src/main/java/com/example/backend/security/JwtAuthenticationFilter.java
 * 所在层:    Security 层（安全层）
 *
 * 职责说明:
 *   JWT 认证过滤器，是 JWT 认证流程的"守门员"。
 *   它拦截每一个 HTTP 请求，从请求头中提取 JWT Token，验证并解析出用户信息，
 *   然后存入 SecurityContextHolder，使后续的 Controller 能获取当前用户。
 *
 * 类比 JavaScript Express 的 JWT 中间件：
 *   app.use((req, res, next) => {
 *     const token = req.headers.authorization?.split(' ')[1]
 *     if (token) {
 *       const decoded = jwt.verify(token, secret)
 *       req.user = { username: decoded.sub, role: decoded.role }
 *     }
 *     next() // 无论有没有 Token，都放行
 *   })
 *
 * 这个过滤器在 SecurityConfig 中被注册为：
 *   .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
 *   即在用户密码认证之前执行。
 *
 * OncePerRequestFilter 父类：
 *   确保在一个请求中只执行一次 doFilterInternal 方法。
 *   防止因请求转发/包含导致重复执行过滤逻辑。
 * ================================================================
 */

package com.example.backend.security;

// --- Servlet API ---
import jakarta.servlet.FilterChain;          // 过滤器链：决定是否放行请求给下一个过滤器
import jakarta.servlet.ServletException;     // Servlet 异常
import jakarta.servlet.http.HttpServletRequest;  // HTTP 请求对象（包含 Headers, URL, Body 等）
import jakarta.servlet.http.HttpServletResponse; // HTTP 响应对象（用于设置状态码、写入数据）

// --- Lombok ---
import lombok.RequiredArgsConstructor;      // 构造器注入
import lombok.extern.slf4j.Slf4j;          // 日志

// --- Spring Security ---
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken; // 认证令牌对象
import org.springframework.security.core.authority.SimpleGrantedAuthority;              // 权限对象
import org.springframework.security.core.context.SecurityContextHolder;                  // 安全上下文持有者

// --- Spring Framework ---
import org.springframework.stereotype.Component;         // Spring 组件注解
import org.springframework.util.StringUtils;              // 字符串工具（判断是否为空字符串）
import org.springframework.web.filter.OncePerRequestFilter; // 每次请求只执行一次

import java.io.IOException; // IO 异常
import java.util.List;      // List 接口

// ==================== 类声明 ====================

/*
 * @Component：
 *   声明为 Spring Bean，由 Spring 容器管理。
 *   有了这个注解，SecurityConfig 中才能通过构造函数注入。
 *
 * @RequiredArgsConstructor：
 *   为 final 字段 JwtUtil 生成构造函数并注入。
 */
@Slf4j        // 自动生成 log 对象
@Component    // Spring Bean
@RequiredArgsConstructor // 构造器注入
public class JwtAuthenticationFilter extends OncePerRequestFilter { // 继承：每个请求只执行一次

    private final JwtUtil jwtUtil; // 注入 JWT 工具类（Token 的生成/解析/验证都在那）

    // ==================== 核心过滤方法 ====================

    /**
     * 过滤器的核心方法
     * 对每个 HTTP 请求执行 JWT 认证逻辑
     *
     * @param request     HTTP 请求对象
     * @param response    HTTP 响应对象
     * @param filterChain 过滤器链（调用 doFilter 放行请求）
     */
    @Override
    protected void doFilterInternal(
            HttpServletRequest request,   // 请求对象
            HttpServletResponse response,  // 响应对象
            FilterChain filterChain)       // 过滤器链（继续执行的通道）
            throws ServletException, IOException {

        /*
         * 放行 OPTIONS 预检请求
         *
         *   浏览器在跨域请求前会发一个 OPTIONS 请求（CORS 预检），
         *   这个请求不带 Authorization Header，如果不过滤会被 401 拦截。
         *
         *   类比 JavaScript：
         *     if (req.method === 'OPTIONS') return next()
         */
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            filterChain.doFilter(request, response); // 放行
            return;
        }

        /*
         * 从请求头提取 JWT Token
         *
         *   格式：Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
         *                        ^^^^^^              ^^^^^^^^^^^
         *                        前缀(7字符)         实际 Token
         *
         *   extractToken 方法会去掉 "Bearer " 前缀，只返回 Token 字符串。
         */
        String token = extractToken(request);

        /*
         * 验证 Token 并设置认证信息
         *
         *   条件：Token 不为空 且 Token 有效（未过期、签名正确）
         *
         *   如果 Token 存在且有效：
         *     1. 从 Token 中解析出用户名和角色
         *     2. 创建一个 Authentication 对象
         *     3. 存入 SecurityContextHolder
         *     4. 之后 Controller 就可以通过 SecurityContextHolder.getContext().getAuthentication()
         *        拿到当前用户信息
         *
         *   如果 Token 不存在或无效：
         *     不设置 Authentication，SecurityContext 为空
         *     后续的 SecurityConfig 规则会拦截未认证的请求
         */
        if (token != null && jwtUtil.validateToken(token)) {

            // 从 Token 中提取用户信息
            String username = jwtUtil.getUsernameFromToken(token);
            String role = jwtUtil.getRoleFromToken(token);

            /*
             * 创建 UsernamePasswordAuthenticationToken
             *
             *   构造函数参数：
             *     principal（主身份）：这里是 username
             *     credentials（凭证）：JWT 认证不需要密码，设为 null
             *     authorities（权限列表）：包装角色为 SimpleGrantedAuthority
             *
             *   注意：角色名要加 "ROLE_" 前缀！
             *   Spring Security 约定：Authority 的名字以 ROLE_ 开头才被识别为"角色"。
             *   所以 "ADMIN" → "ROLE_ADMIN"，"TEACHER" → "ROLE_TEACHER"。
             *
             *   如果不加 ROLE_ 前缀：
             *     @PreAuthorize("hasRole('ADMIN')") 会找不到权限，返回 403！
             */
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            username,                                           // principal：用户名
                            null,                                               // credentials：无需密码
                            List.of(new SimpleGrantedAuthority("ROLE_" + role)) // 权限列表
                    );

            /*
             * 存入 SecurityContextHolder
             *
             *   SecurityContextHolder 是 Spring Security 的全局上下文。
             *   它使用 ThreadLocal 实现，每个请求线程有自己的副本，线程安全。
             *
             *   存入后，后续代码可以通过以下方式获取认证信息：
             *     Authentication auth = SecurityContextHolder.getContext().getAuthentication();
             *     String username = auth.getName();     // 用户名
             *     auth.getAuthorities();                // 权限列表
             *
             *   类比 JavaScript：
             *     req.user = { username, role }  // 存入请求上下文
             *     // 后续中间件通过 req.user 获取
             */
            SecurityContextHolder.getContext().setAuthentication(authentication);

            log.debug("JWT 认证成功，用户名：{}，角色：{}", username, role);
        }

        /*
         * 放行请求到下一个过滤器
         *
         *   无论 Token 验证是否成功，都调用 filterChain.doFilter() 放行。
         *
         *   为什么失败也放行？
         *     如果在这里不放行，请求就会卡住。
         *     应该放行后由 SecurityConfig 的授权规则处理未认证的请求。
         *     如果请求需要认证但 Token 无效，SecurityConfig 会返回 401。
         *
         *   类比 Express：
         *     next() // 传给下一个中间件 / 路由处理器
         */
        filterChain.doFilter(request, response);
    }

    // ==================== 提取 Token 的辅助方法 ====================

    /**
     * 从 HTTP 请求头中提取 JWT Token
     *
     * 请求头格式：Authorization: Bearer <token>
     *
     * @param request HTTP 请求对象
     * @return 纯 Token 字符串（不含 "Bearer " 前缀），或 null
     */
    private String extractToken(HttpServletRequest request) {

        /*
         * request.getHeader("Authorization")
         *
         *   HTTP 请求头通常长这样：
         *     GET /api/students HTTP/1.1
         *     Host: localhost:8088
         *     Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.xxx.yyy   ← 取这一行
         *     Content-Type: application/json
         *
         *   getHeader("Authorization") 返回：Bearer eyJhbGciOiJIUzI1NiJ9.xxx.yyy
         */
        String bearerToken = request.getHeader("Authorization");

        /*
         * StringUtils.hasText(bearerToken)：
         *   判断字符串是否不为 null 且不为空字符串（不为 ""）。
         *   和 JavaScript 的 if (bearerToken) 类似。
         *
         * bearerToken.startsWith("Bearer ")：
         *   判断是否以 "Bearer " 开头。
         *   注意后面有一个空格！
         *
         * bearerToken.substring(7)：
         *   "Bearer " 正好 7 个字符（B-e-a-r-e-r-空格），
         *   从第 7 位开始截取，获取纯 Token 部分。
         *
         *   例如：
         *     "Bearer eyJhbGciOiJIUzI1NiJ9.xxx.yyy"
         *     .substring(7) → "eyJhbGciOiJIUzI1NiJ9.xxx.yyy"
         */
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7); // 截取 "Bearer " 之后的内容
        }

        return null; // 没有合法 Token
    }
}
