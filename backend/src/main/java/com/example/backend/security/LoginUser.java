package com.example.backend.security; // 声明安全包

import com.example.backend.entity.SysUser; // SysUser 实体类
import lombok.AllArgsConstructor; // Lombok 注解：生成所有参数构造器
import lombok.Data; // Lombok 注解
import lombok.NoArgsConstructor; // Lombok 注解：生成无参构造器
import org.springframework.security.core.GrantedAuthority; // Spring Security 权限接口
import org.springframework.security.core.authority.SimpleGrantedAuthority; // 简单权限实现类
import org.springframework.security.core.userdetails.UserDetails; // Spring Security 用户详情接口

import java.util.Collection; // Java 集合顶层接口
import java.util.List; // Java 列表

/**
 * Spring Security 登录用户实体
 * 实现 UserDetails 接口，使 SysUser 能够被 Spring Security 框架识别
 * UserDetails 是 Spring Security 中用户的抽象，框架通过它获取用户信息进行认证
 */
@Data // 自动生成 Getter/Setter
@NoArgsConstructor // 自动生成无参构造器（Spring Security 需要）
@AllArgsConstructor // 自动生成全参构造器
public class LoginUser implements UserDetails { // 实现 UserDetails 接口，适配 Spring Security

    private SysUser sysUser; // 系统用户实体，存储用户完整信息

    /**
     * 获取用户的权限/角色集合
     * Spring Security 通过此方法获取用户拥有的权限列表
     * @return 权限集合，格式为 [ROLE_ADMIN] 或 [ROLE_TEACHER] 等
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // 将角色拼接 "ROLE_" 前缀（Spring Security 约定格式），包装为 SimpleGrantedAuthority
        return List.of(new SimpleGrantedAuthority("ROLE_" + sysUser.getRole()));
    }

    /**
     * 获取用户密码（BCrypt 加密后的密文）
     * Spring Security 用此密码与用户输入的密码进行比对
     * @return 加密后的密码字符串
     */
    @Override
    public String getPassword() {
        return sysUser.getPassword(); // 返回数据库中的加密密码
    }

    /**
     * 获取用户名（登录标识）
     * @return 用户名
     */
    @Override
    public String getUsername() {
        return sysUser.getUsername(); // 返回数据库中的用户名
    }

    // 以下四个方法用于判断账户状态，返回 true 表示正常

    @Override
    public boolean isAccountNonExpired() { // 账户是否未过期
        return true; // 所有账户永不过期（简化处理）
    }

    @Override
    public boolean isAccountNonLocked() { // 账户是否未被锁定
        return true; // 不启用锁定机制（简化处理）
    }

    @Override
    public boolean isCredentialsNonExpired() { // 凭证（密码）是否未过期
        return true; // 密码不过期（简化处理）
    }

    @Override
    public boolean isEnabled() { // 账户是否启用
        // 数据库 status 字段：1 表示启用，0 表示禁用
        return sysUser.getStatus() != null && sysUser.getStatus() == 1;
    }
}
