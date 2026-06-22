/*
 * ================================================================
 * 文件位置:  src/main/java/com/example/backend/security/SecurityConfig.java
 * 所在层:    Security 层（安全配置层）
 *
 * 职责说明:
 *   这是整个认证授权系统的"总控制台"。
 *   在这里配置：哪些 URL 需要登录？哪些 URL 公开？密码怎么加密？JWT 过滤器在哪插入？
 *
 * Spring Security 过滤器链（请求处理流程）：
 *
 *   浏览器发送请求
 *     ↓
 *   [1] SecurityContextPersistenceFilter — 从 Session 恢复安全上下文（本项目无状态，跳过）
 *     ↓
 *   [2] JwtAuthenticationFilter — 从 Header 取 JWT Token，解析并存入 SecurityContext
 *     ↓                             ↑↑ 本项目自定义的过滤器（在下面 addFilterBefore 注册）
 *     ↓
 *   [3] UsernamePasswordAuthenticationFilter — 传统表单登录认证（本项目不需要，但占位）
 *     ↓
 *   [4] ExceptionTranslationFilter — 将认证/授权异常转为 HTTP 响应（401/403）
 *     ↓
 *   [5] FilterSecurityInterceptor — 根据配置的规则决定是否放行（authorizeHttpRequests 在这里生效）
 *     ↓
 *   Controller 方法 — 业务处理
 *
 * 类比 JavaScript Express 的中间件链：
 *   app.use(jwtMiddleware)        // [2] JWT 认证中间件
 *   app.use(authMiddleware)       // [3][5] 授权中间件
 *   app.get('/api/students', ...) // Controller
 *
 * 本配置类的三个关键概念：
 *   1. 认证（Authentication）：你是谁？ → JWT Token 验证身份
 *   2. 授权（Authorization）：你能做什么？ → @PreAuthorize("hasRole('ADMIN')")
 *   3. 无状态（Stateless）：服务端不保存 Session，每次请求都通过 JWT 验证
 * ================================================================
 */

package com.example.backend.security;

// --- 本项目内部类 ---
import com.example.backend.common.Result;       // 统一响应结果
import com.example.backend.common.ResultCode;    // 状态码枚举
import com.fasterxml.jackson.databind.ObjectMapper; // Jackson JSON 序列化（手动转 JSON 字符串）

// --- Servlet API ---
import jakarta.servlet.http.HttpServletResponse; // HTTP 响应对象

// --- Lombok ---
import lombok.RequiredArgsConstructor;           // 构造器注入

// --- Spring Framework ---
import org.springframework.context.annotation.Bean;            // @Bean：注册 Bean
import org.springframework.context.annotation.Configuration;   // @Configuration：配置类

// --- Spring Security ---
/*
 * AuthenticationManager：
 *   Spring Security 认证的核心接口。
 *   负责执行认证流程：接收用户名密码 → 查数据库验证 → 返回认证结果。
 *   在登录时由 AuthServiceImpl 调用。
 */
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;

/*
 * @EnableMethodSecurity：
 *   启用方法级别的安全注解，使 @PreAuthorize 等注解生效。
 *   如果没有这个注解，所有 Controller 上的 @PreAuthorize 都会被忽略！
 *
 * @EnableWebSecurity：
 *   启用 Spring Security 的 Web 安全支持。
 *   Spring Boot 3.x + Spring Security 6.x 中必须显式声明。
 */
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;          // HTTP 安全配置构建器
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;// 启用 Web 安全
import org.springframework.security.config.http.SessionCreationPolicy;                    // Session 创建策略枚举

/*
 * BCryptPasswordEncoder：
 *   BCrypt 是一种不可逆的密码哈希算法。
 *   特点：
 *     - 每次加密结果不同（随机加盐），但验证时能正确比对
 *     - 计算速度适中，暴力破解成本高
 *     - 自动内置盐值，不需要手动管理
 *
 *   BCrypt 加密示例：
 *     明文：admin123
 *     密文：$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy
 *          ^^^^  ^^^^^^^^^^^^^^^^^^^^^^^^
 *         算法   随机盐值 + 加密结果
 *
 *   类比 JavaScript：
 *     const bcrypt = require('bcrypt')
 *     const hash = await bcrypt.hash('admin123', 10)  // 10 = 加密强度
 *     const match = await bcrypt.compare('admin123', hash) // 验证
 */
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/*
 * SecurityFilterChain：
 *   Spring Security 6.x 的核心概念，替代了旧版的 WebSecurityConfigurerAdapter。
 *   它是一个 Bean，定义了整个安全过滤器的配置。
 */
import org.springframework.security.web.SecurityFilterChain;

/*
 * UsernamePasswordAuthenticationFilter：
 *   Spring Security 自带的用户名密码认证过滤器。
 *   本项目使用 JWT 认证，不需要它，但可以用作过滤器排序的"锚点"。
 *   我们的 JwtAuthenticationFilter 会插入在它之前执行。
 */
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.PrintWriter; // 用于向 HTTP 响应写入内容

// ==================== 类声明 ====================

/*
 * @Configuration：
 *   声明这是一个 Spring 配置类。
 *
 * @EnableWebSecurity：
 *   启用 Spring Security 的 Web 安全支持。
 *   这告诉 Spring："启动安全过滤器链，拦截所有 HTTP 请求"。
 *
 * @EnableMethodSecurity：
 *   启用方法级安全注解。
 *   有了它，@PreAuthorize("hasRole('ADMIN')") 才会生效。
 *   如果忘了加，所有 @PreAuthorize 都会被忽略（安全漏洞！）。
 *
 * @RequiredArgsConstructor：
 *   为 final 字段生成构造函数并注入。
 *   这里 JwtAuthenticationFilter 需要被注入。
 */
@Configuration
@EnableWebSecurity         // 启用 Web 安全
@EnableMethodSecurity      // 启用方法级权限控制
@RequiredArgsConstructor   // 构造器注入
public class SecurityConfig {

    // 注入 JWT 认证过滤器（在下一个文件中定义）
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    // ==================== 核心配置：安全过滤器链 ====================

    /**
     * 配置安全过滤器链
     * 这是 Spring Security 6.x 的核心配置方法
     *
     * @param http HttpSecurity 配置构建器（Spring 自动传入）
     * @return SecurityFilterChain 过滤器链（Spring 自动管理）
     */
    /*
     * public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception
     *
     *   这个方法是整个认证系统的"指挥中心"。
     *   通过 http 参数，可以配置：
     *     - 哪些路径需要登录（authorizeHttpRequests）
     *     - 关闭 CSRF（因为用 JWT 不需要）
     *     - Session 设为无状态（因为用 JWT 不需要 Session）
     *     - 自定义 401/403 错误响应格式
     *     - 插入自定义 JWT 过滤器
     *
     *   throws Exception：
     *     HttpSecurity 的方法可能抛出异常，需要声明抛出。
     *     但实际上运行时不会抛，只是 API 签名要求。
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                /*
                 * .csrf(csrf -> csrf.disable())
                 *
                 *   CSRF = Cross-Site Request Forgery（跨站请求伪造）
                 *   这是 Web 安全问题：攻击者诱导用户点击链接，利用用户已登录的身份发送恶意请求。
                 *
                 *   传统方案：服务端生成一个 CSRF Token，前端每次请求携带。
                 *
                 *   为什么本项目关闭 CSRF？
                 *     CSRF 攻击依赖浏览器自动发送 Cookie。
                 *     本项目使用 JWT，Token 存储在 localStorage，不通过 Cookie 传输。
                 *     攻击者无法读取 localStorage 中的 Token，所以不存在 CSRF 风险。
                 *
                 *   类比 JavaScript：
                 *     // Cookie-based auth → 需要 CSRF 保护
                 *     // Token-based auth (JWT) → 不需要 CSRF 保护
                 */
                .csrf(csrf -> csrf.disable())

                /*
                 * .sessionManagement(session -> session
                 *     .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                 *
                 *   SessionCreationPolicy.STATELESS（无状态）：
                 *     告诉 Spring Security："不要创建 HTTP Session，也不要从 Session 恢复认证信息"。
                 *
                 *   为什么是无状态？
                 *     JWT 本身就携带了用户信息（用户名、角色），服务端不需要 Session 来记忆用户状态。
                 *     每次请求通过 Token 独立验证，服务端不存储任何会话信息。
                 *
                 *   无状态的好处：
                 *     1. 服务端扩展性好：增加服务器不需要共享 Session
                 *     2. 不需要 Redis 等 Session 存储（省资源）
                 *     3. 天然支持分布式部署
                 *
                 *   对比有状态（传统 Session 模式）：
                 *     用户登录 → 服务端创建 Session（存 Redis/内存）→ 返回 SessionID Cookie
                 *     每次请求 → 服务端根据 SessionID 查 Session 数据
                 *     服务重启 → Session 全丢（除非用 Redis）
                 */
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                /*
                 * .authorizeHttpRequests(auth -> auth
                 *     .requestMatchers("/api/auth/login").permitAll()
                 *     .requestMatchers("/api/auth/register").permitAll()
                 *     // ... 更多放行路径
                 *     .anyRequest().authenticated())
                 *
                 *   这是 URL 级别的授权规则配置。
                 *
                 *   permitAll()：
                 *     允许匿名访问（不需要登录），用于登录/注册/Swagger 文档等公开页面。
                 *
                 *   authenticated()：
                 *     需要登录才能访问，任何有效 Token 都可以。
                 *     如果未登录，返回 401。
                 *
                 *   匹配顺序：
                 *     Spring Security 按声明顺序匹配，先匹配先生效。
                 *     所以 permitAll() 的路径要写在 anyRequest().authenticated() 前面。
                 *
                 *   类比 Express 中间件：
                 *     const publicPaths = ['/api/auth/login', '/api/auth/register']
                 *     app.use((req, res, next) => {
                 *       if (publicPaths.includes(req.path)) return next() // permitAll
                 *       if (!req.headers.authorization) return res.status(401) // authenticated
                 *       next()
                 *     })
                 */
                .authorizeHttpRequests(auth -> auth
                        // 以下路径公开访问（不需要 Token）
                        .requestMatchers("/api/auth/login").permitAll()          // 登录接口
                        .requestMatchers("/api/auth/register").permitAll()       // 注册接口
                        .requestMatchers("/swagger-ui.html", "/swagger-ui/**").permitAll() // Knife4j UI
                        .requestMatchers("/v3/api-docs/**").permitAll()          // OpenAPI JSON 数据
                        // 除上述之外的所有请求都需要认证
                        .anyRequest().authenticated())

                /*
                 * .exceptionHandling(ex -> ex
                 *     .authenticationEntryPoint(...)    // 未认证 → 401
                 *     .accessDeniedHandler(...))        // 无权限 → 403
                 *
                 *   自定义错误响应格式。
                 *
                 *   默认情况下，Spring Security 未认证返回 302 重定向到登录页，
                 *   前后端分离项目需要返回 JSON 格式的错误信息。
                 *
                 *   401 (Unauthorized)：
                 *     触发条件：未登录（没有 Token 或 Token 过期/无效）
                 *     响应：{ code: 401, message: "未登录或Token已过期，请重新登录" }
                 *
                 *   403 (Forbidden)：
                 *     触发条件：已登录但角色不够（如学生访问管理员接口）
                 *     响应：{ code: 403, message: "权限不足" }
                 */
                .exceptionHandling(ex -> ex
                        // 未认证处理（没有 Token / Token 无效）
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setContentType("application/json;charset=UTF-8"); // JSON + UTF-8
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);   // HTTP 401
                            PrintWriter writer = response.getWriter();                 // 获取输出流
                            // 手动将 Result 对象序列化为 JSON 字符串写入响应
                            writer.write(new ObjectMapper().writeValueAsString(
                                    Result.fail(401, "未登录或Token已过期，请重新登录")));
                            writer.flush(); // 刷新输出
                        })
                        // 无权限处理（角色不够）
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setContentType("application/json;charset=UTF-8"); // JSON + UTF-8
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);      // HTTP 403
                            PrintWriter writer = response.getWriter();
                            writer.write(new ObjectMapper().writeValueAsString(
                                    Result.fail(ResultCode.FORBIDDEN)));
                            writer.flush();
                        }))

                /*
                 * .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                 *
                 *   将自定义的 JWT 过滤器插入到 Spring Security 过滤器链中。
                 *
                 *   addFilterBefore：在指定的过滤器"之前"插入。
                 *   UsernamePasswordAuthenticationFilter.class：以这个标准过滤器为锚点。
                 *
                 *   为什么要在它之前？
                 *     UsernamePasswordAuthenticationFilter 是处理表单登录的。
                 *     JWT 认证需要在表单认证之前执行（有 Token 就不走表单登录）。
                 *
                 *   执行顺序：
                 *     请求 → JwtAuthenticationFilter（解析 Token）→ UsernamePasswordAuthenticationFilter（本项目跳过）
                 *
                 *   类比 Express：
                 *     app.use(jwtMiddleware)  // 放在路由之前
                 *     app.use('/api', router)
                 */
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build(); // 构建并返回 SecurityFilterChain
    }

    // ==================== 密码编码器 ====================

    /**
     * 密码编码器 Bean
     *
     * BCryptPasswordEncoder：使用 BCrypt 算法加密密码。
     *
     * 在哪里使用？
     *   1. 注册时：passwordEncoder.encode(rawPassword) → 存入数据库
     *   2. 登录时：Spring Security 自动调用 passwordEncoder.matches(rawPassword, encodedPassword)
     *
     * 为什么不用 MD5/SHA256？
     *   MD5 和 SHA256 是"哈希"而不是"密码哈希"，计算太快，容易被彩虹表暴力破解。
     *   BCrypt 内置随机盐 + 可调节计算速度，专门为密码存储设计。
     *
     * 类比 JavaScript：
     *   const bcrypt = require('bcrypt')
     *   const hash = await bcrypt.hash(password, 10)  // 加密
     *   await bcrypt.compare(password, hash)           // 验证
     */
    /*
     * @Bean：
     *   注册为 Spring Bean，这样在其他地方通过 @Autowired 或构造函数注入就能拿到。
     *   例如 DataInitializer 中就注入了 PasswordEncoder 用于初始化密码。
     *
     *   类比 JS：注册一个全局可用的 bcrypt 实例。
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(); // BCrypt 是单向加密（不可解密），只能比对
    }

    // ==================== 认证管理器 ====================

    /**
     * 认证管理器 Bean
     *
     * AuthenticationManager 是 Spring Security 认证流程的核心。
     * 登录时，AuthServiceImpl 调用它来验证用户名和密码。
     *
     * 认证流程：
     *   1. AuthService 创建 UsernamePasswordAuthenticationToken(用户名, 密码)
     *   2. 调用 authenticationManager.authenticate(token)
     *   3. Spring Security 内部调用 UserDetailsServiceImpl.loadUserByUsername()
     *   4. 查出用户 → BCrypt 比对密码
     *   5. 验证通过 → 返回认证成功的 Authentication
     *   6. 验证失败 → 抛出 BadCredentialsException
     *
     * @param authenticationConfiguration Spring Security 自动提供的配置
     * @return AuthenticationManager 实例
     * @throws Exception 配置异常（实际上不会抛出）
     */
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}
