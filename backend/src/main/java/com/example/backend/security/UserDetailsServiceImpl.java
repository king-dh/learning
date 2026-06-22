package com.example.backend.security; // 声明安全包

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper; // Lambda 方式构建查询条件（类型安全）
import com.example.backend.entity.SysUser; // SysUser 实体类
import com.example.backend.mapper.SysUserMapper; // 用户 Mapper 接口
import lombok.RequiredArgsConstructor; // Lombok 注解：为 final 字段生成构造器注入
import org.springframework.security.core.userdetails.UserDetails; // Spring Security 用户详情
import org.springframework.security.core.userdetails.UserDetailsService; // 用户详情加载服务
import org.springframework.security.core.userdetails.UsernameNotFoundException; // 用户名未找到异常
import org.springframework.stereotype.Service; // Spring Service 注解

/**
 * Spring Security 用户详情服务实现类
 * 实现了 UserDetailsService 接口，负责从数据库加载用户信息
 * 当用户登录时，Spring Security 会自动调用 loadUserByUsername 方法
 */
@Service // 声明为 Spring Service Bean
@RequiredArgsConstructor // 为 final 字段（sysUserMapper）生成构造器并自动注入
public class UserDetailsServiceImpl implements UserDetailsService { // 实现 Spring Security 的接口

    private final SysUserMapper sysUserMapper; // 注入用户 Mapper，用于查询数据库

    /**
     * 根据用户名从数据库加载用户信息
     * 这是 Spring Security 认证流程的核心方法
     * @param username 登录时输入的用户名
     * @return UserDetails 对象，包含用户的密码、权限等信息
     * @throws UsernameNotFoundException 如果用户名不存在，抛出此异常
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException { // 方法签名必须保持一致
        // 构建查询条件：SELECT * FROM sys_user WHERE username = ?
        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>(); // 创建 Lambda 条件构造器
        queryWrapper.eq(SysUser::getUsername, username); // 设置等于条件：username = 参数值

        // 执行查询，获取单个用户
        SysUser sysUser = sysUserMapper.selectOne(queryWrapper); // selectOne 查询单条记录

        // 如果查询结果为空，抛出异常，Spring Security 会处理为"用户名或密码错误"
        if (sysUser == null) { // 用户不存在
            throw new UsernameNotFoundException("用户名不存在：" + username); // 抛出 Spring Security 标准异常
        }

        // 将 SysUser 封装为 LoginUser（适配 Spring Security 的 UserDetails 接口）
        return new LoginUser(sysUser); // 返回 LoginUser，Spring Security 后续会用此对象验证密码
    }
}
