/*
 * ================================================================
 * 文件位置:  src/main/java/com/example/backend/security/LoginUser.java
 * 所在层:    Security 层（安全层）
 *
 * 职责说明:
 *   Spring Security 的"用户适配器"。
 *   把我们的 SysUser 实体包装成 Spring Security 能识别的 UserDetails 格式。
 *
 * 设计模式：适配器模式（Adapter Pattern）
 *   SysUser 是我们的数据库实体（和业务相关）
 *   UserDetails 是 Spring Security 的标准接口（和框架相关）
 *   LoginUser 就是"适配器"：让 SysUser 能适配 Spring Security 的接口要求。
 *
 * 类比 JavaScript：
 *   const toUserDetails = (sysUser) => ({
 *     getUsername: () => sysUser.username,
 *     getPassword: () => sysUser.password,
 *     getAuthorities: () => [{ authority: 'ROLE_' + sysUser.role }],
 *     isEnabled: () => sysUser.status === 1,
 *     isAccountNonExpired: () => true,
 *     isAccountNonLocked: () => true,
 *     isCredentialsNonExpired: () => true,
 *   })
 *
 * UserDetails 接口要求实现的方法：
 *   必须实现 getUsername()     → 返回用户名
 *   必须实现 getPassword()     → 返回密码（BCrypt 加密后的）
 *   必须实现 getAuthorities()  → 返回权限列表
 *   必须实现 isEnabled()       → 账户是否启用
 *   必须实现 isAccountNonExpired()  → 账户是否未过期
 *   必须实现 isAccountNonLocked()   → 账户是否未锁定
 *   必须实现 isCredentialsNonExpired() → 凭证（密码）是否未过期
 * ================================================================
 */

package com.example.backend.security;

import com.example.backend.entity.SysUser; // 用户实体类

// --- Lombok ---
/*
 * @Data：生成 getter/setter/toString/equals/hashCode
 * @NoArgsConstructor：生成无参构造器（Spring Security 某些场景需要）
 * @AllArgsConstructor：生成全参构造器（方便创建对象）
 */
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// --- Spring Security ---
/*
 * GrantedAuthority：权限接口。Spring Security 用它表示用户的权限/角色。
 *
 * SimpleGrantedAuthority：权限接口的简单实现。
 *   构造函数接收一个字符串，如 "ROLE_ADMIN"。
 *   注意：Spring Security 约定 Authority 以 "ROLE_" 开头时才是"角色"。
 *
 * UserDetails：Spring Security 的用户详情接口。
 *   框架通过这个接口获取用户信息进行认证和授权。
 */
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

// --- Java 标准库 ---
import java.util.Collection; // Java 集合框架顶层接口（List/Set 的父接口）
import java.util.List;       // 不可变列表（Java 9+ 的 List.of() 工厂方法）

// ==================== 类声明 ====================

@Data               // 生成 getter/setter
@NoArgsConstructor  // 无参构造器
@AllArgsConstructor // 全参构造器
public class LoginUser implements UserDetails { // 实现 UserDetails 接口

    /*
     * private SysUser sysUser;
     *
     *   LoginUser 包装了一个 SysUser 对象。
     *   所有 UserDetails 方法都是"转调用"：把请求转发给 sysUser 处理。
     *
     *   例如：
     *     getUsername() → return sysUser.getUsername()
     *     getPassword() → return sysUser.getPassword()
     *
     *   这就是"装饰器模式"的变体：LoginUser 给 SysUser 添加了 Spring Security 需要的接口。
     */
    private SysUser sysUser;

    // ==================== 权限方法 ====================

    /**
     * 获取用户的权限/角色集合
     *
     * @return 权限集合，如 [ROLE_ADMIN]、[ROLE_TEACHER]、[ROLE_STUDENT]
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        /*
         * List.of(new SimpleGrantedAuthority("ROLE_" + sysUser.getRole()))
         *
         *   List.of()：
         *     Java 9+ 的工厂方法，创建不可变列表。
         *     类比 JavaScript：Object.freeze(['ROLE_ADMIN'])
         *
         *   new SimpleGrantedAuthority("ROLE_" + sysUser.getRole())：
         *     SimpleGrantedAuthority 构造函数接收一个字符串权限名。
         *     "ROLE_" 前缀是 Spring Security 的约定：
         *       - 有 "ROLE_" 前缀 → 被识别为"角色"（可以用 hasRole() 匹配）
         *       - 没有前缀 → 被识别为普通"权限"（需要用 hasAuthority() 匹配）
         *
         *   例如：
         *     sysUser.getRole() = "ADMIN"
         *     → "ROLE_" + "ADMIN" = "ROLE_ADMIN"
         *     → @PreAuthorize("hasRole('ADMIN')") 可以匹配
         *
         *   注意：一个用户可以拥有多个权限，这里简化为只有一个角色。
         *   如果需要多角色，可以改为返回多个 SimpleGrantedAuthority。
         *
         *   类比 JavaScript：
         *     getAuthorities() {
         *       return ['ROLE_' + this.sysUser.role]
         *     }
         */
        return List.of(new SimpleGrantedAuthority("ROLE_" + sysUser.getRole()));
    }

    // ==================== 身份认证方法 ====================

    /**
     * 获取用户密码（BCrypt 加密后的密文）
     * Spring Security 用此密码与登录输入的密码进行比对
     *
     * @return BCrypt 加密后的密码字符串
     */
    @Override
    public String getPassword() {
        /*
         * 直接返回数据库中的密码。
         * 数据库中存储的是 BCrypt 加密后的密文，不是明文。
         *
         * 例如：$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy
         * Spring Security 拿到这个值后，用 BCrypt 算法比对登录输入的密码。
         */
        return sysUser.getPassword();
    }

    /**
     * 获取用户名（登录标识）
     *
     * @return 用户名（如 "admin", "teacher1", "student1"）
     */
    @Override
    public String getUsername() {
        return sysUser.getUsername();
    }

    // ==================== 账户状态方法（安全检查） ====================

    /*
     * 以下四个方法用于判断用户账户的状态。
     * 返回 true 表示"状态正常"。
     *
     * Spring Security 在认证时会检查这些方法：
     *   - 如果 isEnabled() 返回 false → 用户被禁用，不能登录
     *   - 如果 isAccountNonLocked() 返回 false → 用户被锁定，不能登录
     *   - 等等...
     *
     * 本项目简化处理：除了 isEnabled() 外，其他都返回 true。
     */

    /**
     * 账户是否未过期
     * @return true（本项目不启用账户过期机制）
     */
    @Override
    public boolean isAccountNonExpired() {
        return true; // 永不过期
    }

    /**
     * 账户是否未锁定
     * @return true（本项目不启用锁定机制）
     */
    @Override
    public boolean isAccountNonLocked() {
        return true; // 永不锁定
    }

    /**
     * 凭证（密码）是否未过期
     * @return true（本项目不启用密码过期机制）
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true; // 密码不过期
    }

    /**
     * 账户是否启用
     * 根据数据库 sys_user 表的 status 字段判断：
     *   status = 1 → 启用（可以登录）
     *   status = 0 → 禁用（不能登录）
     *
     * @return true = 启用，false = 禁用
     */
    @Override
    public boolean isEnabled() {
        /*
         * sysUser.getStatus() != null && sysUser.getStatus() == 1
         *
         *   先判断 status 不为 null（防御性编程），
         *   再判断 status == 1（1=启用，0=禁用）。
         *
         *   如果 sysUser 的 status 字段是 null（如数据不完整），返回 false。
         *   这是防御性编程：null 当作禁用处理更安全。
         */
        return sysUser.getStatus() != null && sysUser.getStatus() == 1;
    }
}
