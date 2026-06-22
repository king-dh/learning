/*
 * ================================================================
 * 文件位置:  src/main/java/com/example/backend/security/JwtUtil.java
 * 所在层:    Security 层（安全层）
 *
 * 职责说明:
 *   JWT（JSON Web Token）工具类，负责 Token 的生成、解析和验证。
 *
 * 什么是 JWT？
 *   JWT = JSON Web Token，是一个开放标准（RFC 7519）。
 *   它是一种紧凑、自包含的方式，用于在各方之间安全地传输信息。
 *
 *   JWT 结构（三段式，用 . 分隔）：
 *     Header.Payload.Signature
 *     eyJhbGci.mF1dGgiOns.4HBHjU4Zz
 *
 *     Header（头）：{ "alg": "HS256", "typ": "JWT" }
 *       说明签名算法和 Token 类型
 *
 *     Payload（载荷）：{ "sub": "admin", "role": "ADMIN", "iat": 123456, "exp": 789012 }
 *       存储实际数据（用户名、角色、签发时间、过期时间）
 *       注意：Payload 只是 Base64 编码，不是加密！不要放敏感信息（如密码）
 *
 *     Signature（签名）：HMACSHA256(Header.Payload, secretKey)
 *       使用密钥对前两部分签名，确保 Token 没被篡改
 *
 *   类比 JavaScript 的 jsonwebtoken 库：
 *     const jwt = require('jsonwebtoken')
 *     const token = jwt.sign({ role: 'ADMIN' }, secret, { subject: 'admin', expiresIn: '24h' })
 *     const decoded = jwt.verify(token, secret)
 *
 * JWT 认证的完整流程：
 *   1. 用户登录 → 服务端验证密码 → 调用 generateToken() 生成 JWT → 返回给前端
 *   2. 前端存 Token 到 localStorage
 *   3. 后续请求在 Header 中携带：Authorization: Bearer <token>
 *   4. JwtAuthenticationFilter 拦截请求 → 调用 validateToken() 验证
 *   5. 调用 parseToken() 解析出用户名和角色 → 存入 SecurityContext
 * ================================================================
 */

package com.example.backend.security;

// --- JJWT 库（Java JWT 实现库）---
import io.jsonwebtoken.Claims;           // JWT 的"声明"（即 Payload 载荷部分，存储数据）
import io.jsonwebtoken.ExpiredJwtException; // Token 过期异常
import io.jsonwebtoken.Jwts;             // JJWT 核心工具类（Builder 模式构建/解析 Token）
import io.jsonwebtoken.security.Keys;    // JJWT 密钥工具类（生成/转换密钥对象）

// --- Spring Framework ---
/*
 * @PostConstruct：
 *   标记在方法上，表示在 Bean 初始化（构造函数 + 依赖注入）完成后自动执行。
 *   用于执行一些初始化工作（这里是检查并生成 JWT 密钥）。
 *
 * @Value("${jwt.secret}")：
 *   从 application.yml / application.properties 配置文件中读取属性值。
 *   例如：jwt.secret: d2hhdC10aGUtZnVjay1pcy10aGlzLXNlY3JldC1rZXk=
 */
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

// --- Java 标准库 ---
import javax.crypto.SecretKey;      // JDK 加密包：密钥接口
import java.security.SecureRandom;  // 密码学安全的随机数生成器
import java.util.Base64;            // Base64 编解码（编码二进制为文本，解码文本为二进制）
import java.util.Date;              // Java 日期类

// ==================== 类声明 ====================

/*
 * @Component：
 *   声明为 Spring Bean，由 Spring 容器管理。
 *   这样 JwtAuthenticationFilter 和 AuthServiceImpl 才能注入 JwtUtil。
 */
@Slf4j       // 自动生成 log 日志对象
@Component   // Spring Bean
public class JwtUtil {

    /*
     * @Value("${jwt.secret}")
     *   从 application.yml 读取 jwt.secret 的值。
     *
     *   application.yml 中配置示例：
     *     jwt:
     *       secret: d2hhdC10aGUtZnVjay1pcy10aGlzLXNlY3JldC1rZXk=   ← Base64 编码的密钥
     *       expiration: 86400000  ← 24 小时（毫秒）
     *
     *   这个机制叫做"外部化配置"，避免把密钥硬编码在代码中。
     *   如果有问题，改配置文件就行，不用重新编译。
     *
     *   类比 JavaScript：
     *     const secret = process.env.JWT_SECRET  // 从环境变量读取
     */
    @Value("${jwt.secret}")    // 从配置文件读取密钥字符串
    private String secret;

    @Value("${jwt.expiration}") // 从配置文件读取过期时间
    private Long expiration;    // Token 有效期（毫秒），如 86400000 = 24 小时

    private SecretKey secretKey; // 密钥对象（JJWT 签名和验证时需要）

    // ==================== 初始化方法 ====================

    /**
     * 初始化方法：在依赖注入完成后自动执行
     * 检查密钥安全性，不够安全则自动生成
     */
    @PostConstruct // 依赖注入完成后执行
    public void init() {
        try {
            /*
             * Base64.getDecoder().decode(secret)
             *
             *   Base64 是一种编码方式：把二进制数据编码为可打印的 ASCII 字符。
             *   这里把配置文件中的 Base64 字符串解码为字节数组。
             *
             *   例如：
             *     "YWJj" → decode → [97, 98, 99] (即 "abc" 的字节)
             */
            byte[] keyBytes = Base64.getDecoder().decode(secret);

            /*
             * 检查密钥长度是否 >= 256 位（32 字节）
             *
             *   HMAC-SHA256 算法推荐使用至少 256 位的密钥。
             *   如果密钥太短（如只有 "mySecret"），安全性不足，容易破解。
             */
            if (keyBytes.length < 32) {
                log.warn("JWT 密钥长度不足 256 位（当前 {} 位），自动生成安全密钥...", keyBytes.length * 8);
                generateNewKey(); // 自动生成
            } else {
                // 密钥长度合格 → 用配置的密钥
                secretKey = Keys.hmacShaKeyFor(keyBytes); // 转换为 JJWT 可用的密钥对象
                log.info("JWT 密钥加载成功，长度 {} 位", keyBytes.length * 8);
            }
        } catch (IllegalArgumentException e) {
            // 如果配置的值不是合法 Base64 格式，也会自动生成
            log.warn("JWT 密钥格式无效，自动生成安全密钥...");
            generateNewKey();
        }
    }

    /**
     * 生成一个新的安全密钥（256 位，HMAC-SHA256 兼容）
     *
     * SecureRandom：
     *   密码学安全的随机数生成器。
     *   和 Java 的 Random 不同，SecureRandom 生成的随机数不可预测，
     *   适用于加密密钥生成。
     */
    private void generateNewKey() {
        byte[] newKey = new byte[32];          // 32 字节 = 256 位
        new SecureRandom().nextBytes(newKey);  // 用安全随机数填充
        secretKey = Keys.hmacShaKeyFor(newKey);// 转成 JJWT 密钥对象
        // 打印新密钥的 Base64，方便复制到配置文件（生产环境应该手动配置）
        log.warn("新生成密钥（Base64）：{}，请复制到 application.yml 的 jwt.secret",
                Base64.getEncoder().encodeToString(newKey));
    }

    // ==================== Token 生成 ====================

    /**
     * 生成 JWT Token
     *
     * @param username 用户名 → 存入 subject（sub）字段
     * @param role     角色 → 存入自定义 claim
     * @return JWT 签名字符串（三段式）
     */
    /*
     * public String generateToken(String username, String role) { ... }
     *
     *   生成的 Token 包含以下信息：
     *     sub: username       ← 主题（通常存用户名或用户ID）
     *     role: role          ← 自定义声明（本项目用来存角色）
     *     iat: 当前时间        ← 签发时间
     *     exp: 当前时间+有效期  ← 过期时间
     *
     *   使用 Builder 模式构建 Token：
     *     .subject(username)      设置 sub 字段
     *     .claim("role", role)    设置自定义字段（key = "role", value = role）
     *     .issuedAt(now)          设置 iat 字段
     *     .expiration(expiryDate) 设置 exp 字段
     *     .signWith(secretKey)    用密钥签名（防篡改）
     *     .compact()              生成最终的三段式字符串
     */
    public String generateToken(String username, String role) {
        Date now = new Date();                                  // 当前时间
        Date expiryDate = new Date(now.getTime() + expiration); // 过期时间 = 现在 + 有效期

        return Jwts.builder()           // 创建 JWT Builder
                .subject(username)       // sub：主题（用户名）
                .claim("role", role)     // 自定义声明：角色
                .issuedAt(now)           // iat：签发时间
                .expiration(expiryDate)  // exp：过期时间
                .signWith(secretKey)     // 签名：用密钥做 HMAC 签名
                .compact();              // 构建最终 Token 字符串
    }

    // ==================== Token 解析 ====================

    /**
     * 解析 JWT Token，提取 Claims（载荷数据）
     *
     * @param token JWT 签名字符串
     * @return Claims 对象（可取出 sub/role/iat/exp 等所有字段）
     */
    /*
     * 解析流程：
     *   1. Jwts.parser() 创建解析器
     *   2. .verifyWith(secretKey) 设置验证密钥
     *   3. .build() 构建解析器
     *   4. .parseSignedClaims(token) 解析并验证签名
     *   5. .getPayload() 返回 Claims（Payload 部分的数据）
     *
     * 如果 Token 被篡改或签名不匹配，parseSignedClaims 会抛异常。
     */
    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)     // 设置验证密钥
                .build()                   // 构建解析器
                .parseSignedClaims(token)  // 解析 + 验证签名
                .getPayload();             // 获取载荷数据
    }

    // ==================== Token 验证 ====================

    /**
     * 验证 JWT Token 是否有效
     *
     * @param token JWT 签名字符串
     * @return true = 有效, false = 无效
     */
    /*
     * 验证逻辑：
     *   尝试解析 Token → 如果成功，说明签名正确且未过期 → true
     *                   如果 ExpiredJwtException，Token 过期 → false
     *                   如果其他异常（签名错误/格式错误）→ false
     */
    public boolean validateToken(String token) {
        try {
            parseToken(token);   // 解析 Token，如果成功则有效
            return true;
        } catch (ExpiredJwtException e) { // Token 已过期
            log.warn("JWT Token 已过期：{}", e.getMessage());
            return false;
        } catch (Exception e) {           // 其他异常（签名错误、格式错误）
            log.warn("JWT Token 无效：{}", e.getMessage());
            return false;
        }
    }

    // ==================== 从 Token 提取信息 ====================

    /**
     * 从 Token 中获取用户名
     *
     * @param token JWT 字符串
     * @return 用户名（即 JWT 的 subject 字段）
     */
    public String getUsernameFromToken(String token) {
        return parseToken(token).getSubject(); // getSubject() = 获取 sub 字段
    }

    /**
     * 从 Token 中获取用户角色
     *
     * @param token JWT 字符串
     * @return 角色字符串（如 "ADMIN", "TEACHER", "STUDENT"）
     */
    /*
     * parseToken(token).get("role", String.class)
     *
     *   get("role"): 获取自定义声明中 key 为 "role" 的值
     *   String.class: 指定返回类型为 String（JJWT 需要类型信息来做类型转换）
     *
     *   类比 JavaScript：
     *     const decoded = jwt.verify(token, secret)
     *     const role = decoded.role
     */
    public String getRoleFromToken(String token) {
        return parseToken(token).get("role", String.class);
    }
}
