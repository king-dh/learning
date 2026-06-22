package com.example.backend.security; // 声明安全包

import io.jsonwebtoken.Claims; // JWT Claims（声明/载荷），存储 Token 中的数据
import io.jsonwebtoken.ExpiredJwtException; // JWT 过期异常
import io.jsonwebtoken.Jwts; // JWT 核心工具类
import io.jsonwebtoken.security.Keys; // JWT 密钥工具类
import jakarta.annotation.PostConstruct; // Bean 初始化后执行注解
import lombok.extern.slf4j.Slf4j; // Lombok 日志注解
import org.springframework.beans.factory.annotation.Value; // 读取配置文件属性
import org.springframework.stereotype.Component; // Spring 组件注解

import javax.crypto.SecretKey; // 加密密钥类（JDK 自带）
import java.security.SecureRandom; // 安全随机数生成器
import java.util.Base64; // Base64 编解码
import java.util.Date; // Java 日期类

/**
 * JWT（JSON Web Token）工具类
 * 负责 JWT Token 的生成、解析、验证等操作
 * JWT 结构：Header（头）.Payload（载荷）.Signature（签名）
 * 通过 @Value 从 application.yml 读取配置
 */
@Slf4j // 自动生成 log 对象，用于打印日志
@Component // 声明为 Spring Bean，由 Spring 容器管理
public class JwtUtil {

    @Value("${jwt.secret}") // 从配置文件读取 JWT 签名密钥
    private String secret; // 密钥字符串，从配置文件读取

    @Value("${jwt.expiration}") // 从配置文件读取过期时间（毫秒）
    private Long expiration; // Token 有效期，默认 24 小时

    private SecretKey secretKey; // 解析后的密钥对象，用于签名和验证

    /**
     * 初始化方法，在 Bean 构造完成后自动执行
     * 检查密钥安全性，如果不够安全则自动生成
     */
    @PostConstruct // Spring 注解：在依赖注入完成后立即执行此方法
    public void init() {
        try { // 尝试从配置文件读取密钥
            // 解码 Base64 密钥，获取密钥字节数组
            byte[] keyBytes = Base64.getDecoder().decode(secret);
            // 检查密钥长度是否达到 256 位（32 字节）的安全标准
            if (keyBytes.length < 32) { // 如果密钥长度不足 32 字节
                log.warn("JWT 密钥长度不足 256 位（当前 {} 位），自动生成安全密钥...", keyBytes.length * 8);
                generateNewKey(); // 自动生成新密钥
            } else { // 密钥长度满足要求
                // 直接用配置的密钥创建密钥对象
                secretKey = Keys.hmacShaKeyFor(keyBytes);
                log.info("JWT 密钥加载成功，长度 {} 位", keyBytes.length * 8);
            }
        } catch (IllegalArgumentException e) { // 如果配置的密钥不是有效的 Base64 格式
            log.warn("JWT 密钥格式无效，自动生成安全密钥...");
            generateNewKey(); // 自动生成新密钥
        }
    }

    /**
     * 生成一个新的安全密钥（256 位，HMAC-SHA256 兼容）
     */
    private void generateNewKey() {
        // 创建 256 位（32 字节）的字节数组
        byte[] newKey = new byte[32];
        // 使用 SecureRandom 生成密码学安全的随机数填充
        new SecureRandom().nextBytes(newKey);
        // 转换为 HMAC-SHA 算法可用的密钥对象
        secretKey = Keys.hmacShaKeyFor(newKey);
        // 打印新生成的 Base64 密钥，方便复制到配置文件
        log.warn("新生成密钥（Base64）：{}，请复制到 application.yml 的 jwt.secret", Base64.getEncoder().encodeToString(newKey));
    }

    /**
     * 生成 JWT Token
     * Token 中包含用户名和角色信息，用于后续的身份认证和授权
     * @param username 用户名，存入 subject 字段
     * @param role     用户角色，存入自定义 claim
     * @return JWT 签名字符串
     */
    public String generateToken(String username, String role) {
        Date now = new Date(); // 获取当前时间
        Date expiryDate = new Date(now.getTime() + expiration); // 计算过期时间 = 当前时间 + 配置的有效期

        return Jwts.builder() // 使用 JWT Builder 模式构建 Token
                .subject(username) // 设置主题（sub 声明），通常存用户名或用户ID
                .claim("role", role) // 设置自定义声明，存储用户角色（后续鉴权使用）
                .issuedAt(now) // 设置签发时间（iat 声明）
                .expiration(expiryDate) // 设置过期时间（exp 声明）
                .signWith(secretKey) // 使用密钥进行 HMAC 签名，防止篡改
                .compact(); // 构建并压缩为最终的 JWT 字符串
    }

    /**
     * 解析 JWT Token，提取 Claims（载荷数据）
     * @param token JWT 签名字符串
     * @return Claims 对象，可从中获取用户名、角色、过期时间等
     */
    public Claims parseToken(String token) {
        return Jwts.parser() // 创建 JWT 解析器
                .verifyWith(secretKey) // 设置验证密钥（必须与签名密钥一致）
                .build() // 构建解析器
                .parseSignedClaims(token) // 解析并验证签名
                .getPayload(); // 获取载荷（Claims）部分
    }

    /**
     * 验证 JWT Token 是否有效
     * 如果 Token 已过期或签名不匹配，返回 false
     * @param token JWT 签名字符串
     * @return true=有效, false=无效
     */
    public boolean validateToken(String token) {
        try { // 尝试解析 Token
            parseToken(token);
            return true; // 解析成功，Token 有效
        } catch (ExpiredJwtException e) { // 捕获过期异常
            log.warn("JWT Token 已过期：{}", e.getMessage());
            return false; // Token 已过期
        } catch (Exception e) { // 捕获其他异常（签名错误、格式错误等）
            log.warn("JWT Token 无效：{}", e.getMessage());
            return false; // Token 无效
        }
    }

    /**
     * 从 Token 中获取用户名（subject 声明）
     * @param token JWT 签名字符串
     * @return 用户名
     */
    public String getUsernameFromToken(String token) {
        return parseToken(token).getSubject(); // 获取 JWT 的 subject 字段
    }

    /**
     * 从 Token 中获取用户角色（自定义声明）
     * @param token JWT 签名字符串
     * @return 角色字符串
     */
    public String getRoleFromToken(String token) {
        return parseToken(token).get("role", String.class); // 获取自定义的 "role" 声明
    }
}
