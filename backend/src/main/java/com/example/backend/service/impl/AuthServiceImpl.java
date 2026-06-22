package com.example.backend.service.impl; // 声明服务实现包

import cn.hutool.core.bean.BeanUtil; // Hutool Bean 属性复制工具
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper; // Lambda 查询条件构造器
import com.example.backend.common.BusinessException; // 自定义业务异常
import com.example.backend.common.ResultCode; // 统一状态码
import com.example.backend.dto.LoginDTO; // 登录 DTO
import com.example.backend.dto.RegisterDTO; // 注册 DTO
import com.example.backend.entity.SysUser; // 用户实体
import com.example.backend.mapper.SysUserMapper; // 用户 Mapper
import com.example.backend.security.JwtUtil; // JWT 工具类
import com.example.backend.service.AuthService; // 认证服务接口
import com.example.backend.vo.LoginVO; // 登录响应 VO
import lombok.RequiredArgsConstructor; // Lombok 构造器注入
import lombok.extern.slf4j.Slf4j; // 日志注解
import org.springframework.security.crypto.password.PasswordEncoder; // 密码加密器
import org.springframework.stereotype.Service; // Spring Service 注解

/**
 * 认证服务实现类
 * 负责用户的登录验证和注册逻辑
 */
@Slf4j // 自动生成日志对象
@Service // 声明为 Spring Service Bean
@RequiredArgsConstructor // 为 final 字段生成构造器（构造器注入）
public class AuthServiceImpl implements AuthService { // 实现 AuthService 接口

    private final SysUserMapper sysUserMapper; // 注入用户 Mapper，用于数据库操作
    private final PasswordEncoder passwordEncoder; // 注入密码加密器（BCrypt）
    private final JwtUtil jwtUtil; // 注入 JWT 工具类，用于生成 Token

    /**
     * 用户登录
     * 步骤：1.查询用户 2.验证密码 3.生成 Token 4.返回结果
     */
    @Override
    public LoginVO login(LoginDTO loginDTO) {
        // 第1步：根据用户名查询用户
        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>(); // 创建查询条件构造器
        queryWrapper.eq(SysUser::getUsername, loginDTO.getUsername()); // 条件：username = 传入的用户名
        SysUser sysUser = sysUserMapper.selectOne(queryWrapper); // 执行查询，获取用户

        // 第2步：判断用户是否存在
        if (sysUser == null) { // 如果用户名不存在
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "用户名或密码错误"); // 抛出业务异常
        }

        // 第3步：验证密码
        // matches(明文, 密文)：将用户输入的明文密码与数据库中的 BCrypt 密文进行比对
        if (!passwordEncoder.matches(loginDTO.getPassword(), sysUser.getPassword())) { // 密码不匹配
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "用户名或密码错误"); // 密码错误
        }

        // 第4步：检查用户状态（是否被禁用）
        if (sysUser.getStatus() == null || sysUser.getStatus() != 1) { // 状态不是 1（启用）
            throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "账户已被禁用，请联系管理员"); // 被禁用
        }

        // 第5步：生成 JWT Token
        String token = jwtUtil.generateToken(sysUser.getUsername(), sysUser.getRole()); // 生成签名 Token

        // 第6步：组装返回数据
        LoginVO loginVO = new LoginVO(); // 创建 LoginVO 对象
        loginVO.setToken(token); // 设置 Token
        loginVO.setUsername(sysUser.getUsername()); // 设置用户名
        loginVO.setRealName(sysUser.getRealName()); // 设置真实姓名
        loginVO.setRole(sysUser.getRole()); // 设置角色

        log.info("用户 {} 登录成功，角色：{}", sysUser.getUsername(), sysUser.getRole()); // 记录日志
        return loginVO; // 返回登录结果
    }

    /**
     * 用户注册
     * 步骤：1.检查用户名唯一性 2.加密密码 3.保存用户
     */
    @Override
    public void register(RegisterDTO registerDTO) {
        // 第1步：检查用户名是否已存在
        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>(); // 创建查询条件构造器
        queryWrapper.eq(SysUser::getUsername, registerDTO.getUsername()); // 条件：username = 注册的用户名
        if (sysUserMapper.selectCount(queryWrapper) > 0) { // 查询该用户名的记录数，大于 0 表示已存在
            throw new BusinessException(ResultCode.BUSINESS_ERROR.getCode(), "用户名已存在"); // 用户名重复
        }

        // 第2步：构建用户实体并设置属性
        SysUser sysUser = new SysUser(); // 创建用户实体
        sysUser.setUsername(registerDTO.getUsername()); // 设置用户名
        sysUser.setPassword(passwordEncoder.encode(registerDTO.getPassword())); // 加密密码（BCrypt 不可逆加密）
        sysUser.setRealName(registerDTO.getRealName()); // 设置真实姓名
        sysUser.setRole(registerDTO.getRole()); // 设置角色
        sysUser.setStatus(1); // 默认状态：启用

        // 第3步：保存到数据库
        sysUserMapper.insert(sysUser); // MyBatis-Plus 通用插入方法

        log.info("用户 {} 注册成功，角色：{}", sysUser.getUsername(), sysUser.getRole()); // 记录日志
    }
}
