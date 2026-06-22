/*
 * ================================================================
 * 文件位置:  src/main/java/com/example/backend/security/UserDetailsServiceImpl.java
 * 所在层:    Security 层（安全层）
 *
 * 职责说明:
 *   实现 Spring Security 的 UserDetailsService 接口。
 *   核心方法 loadUserByUsername() 在用户登录时被 Spring Security 自动调用。
 *
 * 它解决什么问题？
 *   Spring Security 不知道你的用户数据存在哪里（MySQL / MongoDB / LDAP / 内存...）。
 *   通过实现 UserDetailsService 接口，告诉 Spring Security "去数据库查"。
 *
 * 类比 JavaScript + Passport.js：
 *   passport.use(new LocalStrategy(
 *     async (username, password, done) => {
 *       const user = await db.findUserByUsername(username)  // ← loadUserByUsername 做的事
 *       if (!user) return done(null, false)
 *       if (!bcrypt.compare(password, user.password)) return done(null, false)
 *       return done(null, user)
 *     }
 *   ))
 *
 * 登录认证流程（完整链路）：
 *   1. 用户在 AuthController.login() 输入用户名密码
 *   2. AuthServiceImpl 创建 UsernamePasswordAuthenticationToken
 *   3. 调用 AuthenticationManager.authenticate(token)
 *   4. Spring Security 内部自动调用本类的 loadUserByUsername(username)
 *      → 查询数据库 → 返回 LoginUser（实现了 UserDetails）
 *   5. Spring Security 用 BCryptPasswordEncoder 比对密码
 *   6. 匹配成功 → 返回认证成功
 *   7. AuthServiceImpl 拿到认证结果 → 生成 JWT → 返回前端
 * ================================================================
 */

package com.example.backend.security;

// --- MyBatis-Plus ---
/*
 * LambdaQueryWrapper：
 *   MyBatis-Plus 提供的"类型安全"查询条件构造器。
 *   使用 Lambda 表达式引用实体字段，避免手写字符串（如 "username"），
 *   重构字段名时会自动更新引用，减少 Bug。
 *
 * 示例：
 *   传统：queryWrapper.eq("username", "admin")  // 字符串，重构时不会自动更新
 *   Lambda：queryWrapper.eq(SysUser::getUsername, "admin")  // Lambda 引用，重构安全
 *
 * 类比 JavaScript：
 *   // 传统
 *   db.find({ username: 'admin' })
 *   // 类型安全
 *   db.findByUsername('admin')  // 有 IDE 智能提示
 */
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

// --- 本项目实体类 ---
import com.example.backend.entity.SysUser;     // 用户表实体（映射 sys_user 表）
import com.example.backend.mapper.SysUserMapper; // 用户 Mapper（操作数据库）

// --- Lombok ---
import lombok.RequiredArgsConstructor; // 构造器注入

// --- Spring Security ---
/*
 * UserDetails：
 *   Spring Security 定义的"用户详情"接口。
 *   需要实现的方法：getUsername(), getPassword(), getAuthorities(),
 *   isEnabled(), isAccountNonExpired(), isAccountNonLocked(), isCredentialsNonExpired()
 *
 * UserDetailsService：
 *   Spring Security 定义的"加载用户详情"服务接口。
 *   只有一个方法：loadUserByUsername(String username)
 *   实现这个接口，告诉 Spring Security 如何从你的数据源加载用户。
 *
 * UsernameNotFoundException：
 *   当用户名不存在时抛出的标准异常。
 *   Spring Security 会捕获这个异常，返回"用户名或密码错误"。
 */
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

// --- Spring Framework ---
import org.springframework.stereotype.Service; // Spring Service 注解

// ==================== 类声明 ====================

/*
 * @Service：
 *   声明为 Spring Service Bean。
 *   @Service 和 @Component 功能相同，但语义上表示"业务逻辑 Bean"。
 *   Spring Security 会自动发现 UserDetailsService 的实现类并使用。
 */
@Service
@RequiredArgsConstructor // 为 sysUserMapper 生成构造函数
public class UserDetailsServiceImpl implements UserDetailsService { // 必须实现这个接口

    /*
     * private final SysUserMapper sysUserMapper;
     *
     *   SysUserMapper 是 MyBatis-Plus 的 Mapper 接口，
     *   继承自 BaseMapper<SysUser>，拥有内置的 CRUD 方法。
     *
     *   MyBatis-Plus 会在运行时自动生成 Mapper 接口的实现类（动态代理），
     *   我们不需要手写实现，直接注入使用即可。
     */
    private final SysUserMapper sysUserMapper;

    // ==================== 核心方法：按用户名加载用户 ====================

    /**
     * 根据用户名从数据库加载用户信息
     * 这是 Spring Security 认证流程的核心方法
     *
     * @param username 登录时输入的用户名
     * @return UserDetails（实际是 LoginUser），包含密码、权限等信息
     * @throws UsernameNotFoundException 用户名不存在时抛出
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        /*
         * 构建查询条件：SELECT * FROM sys_user WHERE username = ?
         *
         *   LambdaQueryWrapper<SysUser>：
         *     创建一个针对 SysUser 表的查询条件构造器。
         *
         *   .eq(SysUser::getUsername, username)：
         *     .eq = "等于" 条件
         *     SysUser::getUsername = 方法引用（Method Reference），等价于 Lambda 表达式 s -> s.getUsername()
         *     告诉 MyBatis-Plus："以 username 字段为判断条件，值等于方法参数"
         *
         *   最终生成的 SQL：
         *     SELECT * FROM sys_user WHERE username = 'admin'
         *
         *   类比 JavaScript ORM：
         *     await db.sysUser.findFirst({ where: { username: username } })
         */
        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysUser::getUsername, username);

        /*
         * sysUserMapper.selectOne(queryWrapper)
         *
         *   selectOne 查询单条记录。
         *   如果有多条匹配，会抛出异常（因为 WHERE username = ? 应该唯一）。
         *   查询不到返回 null。
         */
        SysUser sysUser = sysUserMapper.selectOne(queryWrapper);

        /*
         * 如果用户名不存在，抛出标准异常
         *
         *   为什么使用 UsernameNotFoundException？
         *     这是 Spring Security 定义的标准异常。
         *     Spring Security 捕获后会自动返回"用户名或密码错误"。
         *
         *   安全考量：不要区分"用户名不存在"和"密码错误"！
         *     攻击者可以通过不同的提示信息枚举系统中的用户名。
         *     统一返回"用户名或密码错误"更安全。
         */
        if (sysUser == null) {
            throw new UsernameNotFoundException("用户名不存在：" + username);
        }

        /*
         * 将 SysUser 封装为 LoginUser
         *
         *   LoginUser 实现了 UserDetails 接口（适配器模式）。
         *   SysUser 是数据库实体，UserDetails 是 Spring Security 需要的格式。
         *   LoginUser 作为"适配器"，把 SysUser 包装成 Spring Security 能识别的 UserDetails。
         *
         *   类比 JavaScript 适配器模式：
         *     function toUserDetails(sysUser) {
         *       return {
         *         username: sysUser.username,
         *         password: sysUser.password,
         *         authorities: [new SimpleGrantedAuthority('ROLE_' + sysUser.role)],
         *         isEnabled: () => sysUser.status === 1,
         *         // ...
         *       }
         *     }
         */
        return new LoginUser(sysUser);
    }

    /*
     * ================================================================
     * 总结：
     *
     *   这个类做的事很简单：根据用户名查数据库，返回用户信息。
     *
     *   但它是整个认证系统的"数据源"——Spring Security 不关心数据从哪来，
     *   只关心 loadUserByUsername() 返回了正确的 UserDetails。
     *
     *   如果想切换数据源（如换到 Redis / LDAP），只需要改这个类，
     *   其他地方（AuthService、JwtUtil、SecurityConfig）完全不用改。
     *   这就是"面向接口编程"的好处。
     * ================================================================
     */
}
