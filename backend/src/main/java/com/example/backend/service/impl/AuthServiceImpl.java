/*
 * ================================================================
 * 文件位置:  src/main/java/com/example/backend/service/impl/AuthServiceImpl.java
 * 架构层级:  Service 实现类（认证业务的实际执行者）
 * 对应接口:  src/main/java/com/example/backend/service/AuthService.java
 * 被调用者:  src/main/java/com/example/backend/controller/AuthController.java
 *
 * 调用链路（以"用户登录"为例）:
 * 前端登录页面 → axios POST /api/auth/login { username: "admin", password: "123456" }
 *              → AuthController.login()
 *              → AuthService.login()       ← 接口
 *              → AuthServiceImpl.login()   ← 本文件
 *              → 1.查 sys_user 表 2.BCrypt 验证密码 3.生成 JWT 4.返回 Token
 *
 * 认证服务是系统安全的第一道防线：
 *   - 登录：验证身份 → 签发 Token
 *   - 注册：创建新用户 → 加密密码
 *   - 密码永远不会以明文存储（使用 BCrypt 加密）
 *
 * JS 类比（Express + JWT）:
 *   const AuthService = {
 *     login: async (dto) => {
 *       const user = await User.findOne({ username: dto.username })
 *       if (!user || !bcrypt.compare(dto.password, user.password))
 *         throw new Error('用户名或密码错误')
 *       const token = jwt.sign({ username: user.username, role: user.role }, secret)
 *       return { token, username: user.username, realName: user.realName, role: user.role }
 *     },
 *     register: async (dto) => {
 *       const exists = await User.findOne({ username: dto.username })
 *       if (exists) throw new Error('用户名已存在')
 *       await User.create({ ...dto, password: bcrypt.hash(dto.password), status: 1 })
 *     }
 *   }
 * ================================================================
 */

// ==================== 1. 包声明 ====================
package com.example.backend.service.impl; // 声明在 service/impl 子包

// ==================== 2. 导入其他类（import） ====================

// Hutool Bean 属性复制工具：类似 JS 的 Object.assign / 展开运算符
import cn.hutool.core.bean.BeanUtil;

/*
 * LambdaQueryWrapper：SQL WHERE 子句条件构造器。
 * 通过方法引用（SysUser::getUsername）指定字段，编译时安全检查。
 *
 * 类比 JS：
 *   // MyBatis-Plus:
 *   queryWrapper.eq(SysUser::getUsername, username)
 *   // 等同于 Sequelize:
 *   { where: { username: username } }
 */
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;

// 业务异常：抛出后由 GlobalExceptionHandler 统一捕获 → 返回 JSON 错误
import com.example.backend.common.BusinessException;
// 状态码枚举：如 BUSINISS_ERROR → 5001
import com.example.backend.common.ResultCode;

// DTO：接收前端请求的数据对象
import com.example.backend.dto.LoginDTO;    // { username, password }
import com.example.backend.dto.RegisterDTO; // { username, password, realName, role }

// Entity：数据库表映射实体
import com.example.backend.entity.SysUser; // sys_user 表 → 用户实体

// Mapper：数据库操作接口
import com.example.backend.mapper.SysUserMapper; // 操作 sys_user 表

// JWT 工具类：生成和解析 JWT Token
import com.example.backend.security.JwtUtil;

import com.example.backend.service.AuthService; // 本类要实现的接口
import com.example.backend.vo.LoginVO;           // 登录返回数据：{ token, username, realName, role }

// Lombok：编译时代码生成
import lombok.RequiredArgsConstructor; // 为 final 字段自动生成构造函数（Spring 依赖注入用）
import lombok.extern.slf4j.Slf4j;     // 自动创建日志对象 log

// Spring Security 密码加密器接口（默认实现是 BCrypt）
import org.springframework.security.crypto.password.PasswordEncoder;

// Spring 框架注解
import org.springframework.stereotype.Service; // 标记为 Service Bean，纳入 IoC 容器管理

// ==================== 3. 类的声明和注解 ====================

/*
 * @Slf4j
 *   Lombok 自动生成日志对象：
 *     private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);
 *   之后可直接用 log.info()、log.error() 记录日志。类比 JS 的 console.log。
 */
@Slf4j

/*
 * @Service
 *   告诉 Spring："这个类是一个业务 Bean，请把它纳入 IoC 容器管理"。
 *   当 Controller 需要 AuthService 时，Spring 自动从容器中找到唯一的 AuthServiceImpl 实例注入。
 *
 *   与 @Component 的区别：
 *     @Service 是 @Component 的"语义化别名"——功能一样，但告诉开发者"这是 Service 层"。
 *     类似 @Repository（DAO 层）、@Controller（控制器层）。
 */
@Service

/*
 * @RequiredArgsConstructor
 *   Lombok 为所有 final 字段生成构造函数。
 *   本类有 3 个 final 字段：sysUserMapper、passwordEncoder、jwtUtil
 *   Lombok 自动生成：
 *     public AuthServiceImpl(SysUserMapper m, PasswordEncoder p, JwtUtil j) {
 *         this.sysUserMapper = m; this.passwordEncoder = p; this.jwtUtil = j;
 *     }
 *
 *   Spring 依赖注入过程：
 *     1. 扫描到 @Service，需要创建 AuthServiceImpl 实例
 *     2. 发现构造函数需要 3 个参数
 *     3. 在容器中查找：
 *        - SysUserMapper → MyBatis 自动生成的代理对象
 *        - PasswordEncoder → BCryptPasswordEncoder（Security 配置中注册的）
 *        - JwtUtil → JwtUtil 实例（被 @Component 标记的工具类）
 *     4. 传入构造函数，创建 AuthServiceImpl 实例
 *
 *   这种方式叫"构造器注入"（Constructor Injection），是 Spring 推荐的注入方式。
 *   优势：final 字段不可变、不需要 @Autowired、方便测试（new AuthServiceImpl(mock1, mock2, mock3)）
 *
 *   类比 JS：
 *     class AuthServiceImpl {
 *       #userMapper; #encoder; #jwtUtil;
 *       constructor(um, pe, ju) { this.#userMapper = um; ... }
 *     }
 */
@RequiredArgsConstructor

/*
 * public class AuthServiceImpl implements AuthService
 *
 *   实现 AuthService 接口，提供 login() 和 register() 的具体代码。
 *   如果漏写接口中的某个方法，编译器直接报错。
 */
public class AuthServiceImpl implements AuthService { // 认证服务实现类

    // ==================== 4. 字段声明（依赖注入的接收者） ====================

    /*
     * 三个 final 字段，由 Spring 通过构造器注入：
     *
     * sysUserMapper  → 操作 sys_user 表（MyBatis-Plus 的 BaseMapper）
     * passwordEncoder → BCrypt 密码加密器，encode() 加密，matches() 比对
     * jwtUtil         → JWT 工具类，generateToken() 生成签名 Token
     *
     * 为什么需要 passwordEncoder 作为独立依赖？
     *   加密算法是"可替换的策略"。现在用 BCrypt，将来可能换 Argon2 或 SHA-256。
     *   只要所有地方都依赖 PasswordEncoder 接口而非具体实现，换算法时只需改配置。
     *   这就是"依赖倒置原则"（DIP）：高层模块不依赖低层模块，两者都依赖抽象。
     */
    private final SysUserMapper sysUserMapper;   // 用户 Mapper
    private final PasswordEncoder passwordEncoder; // BCrypt 密码加密器
    private final JwtUtil jwtUtil;                 // JWT 工具

    // ==================== 5. 方法实现 ====================

    /*
     * ================================================================
     * 方法 1：用户登录
     *
     * 完整业务流程（6 步）：
     *   1. 根据用户名查询用户
     *   2. 判断用户是否存在
     *   3. 验证密码（明文 vs BCrypt 密文）
     *   4. 检查用户状态（是否被禁用）
     *   5. 生成 JWT Token
     *   6. 返回登录结果（Token + 用户信息）
     * ================================================================
     */
    @Override
    public LoginVO login(LoginDTO loginDTO) {

        /*
         * 第1步：根据用户名查询用户
         *
         * LambdaQueryWrapper<SysUser>：创建 SysUser 表的查询条件构造器
         * .eq(SysUser::getUsername, loginDTO.getUsername())：
         *   添加条件 username = ?，最终 SQL：WHERE username = 'admin'
         *
         * selectOne(queryWrapper)：查询一条记录，查不到返回 null，查到多条抛异常。
         */
        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>(); // 条件构造器
        queryWrapper.eq(SysUser::getUsername, loginDTO.getUsername()); // 条件：username = 前端传入的用户名
        SysUser sysUser = sysUserMapper.selectOne(queryWrapper); // 执行查询

        // 第2步：用户不存在 → 抛异常（不透露是"用户不存在"还是"密码错误"，安全考虑）
        if (sysUser == null) { // 查找失败
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "用户名或密码错误");
        }

        /*
         * 第3步：验证密码
         *
         * passwordEncoder.matches(明文, 密文)：
         *   把用户输入的明文密码和数据库中存储的 BCrypt 密文进行比对。
         *
         *   BCrypt 的特点：
         *     - 不可逆：无法从密文反推明文
         *     - 加盐：每次加密结果不同（包含随机盐值）
         *     - 慢哈希：故意消耗 CPU 时间，防止暴力破解
         *
         *   数据库存的是：$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy
         *   用户输入的是："123456"
         *   matches() 从密文中提取盐值，用同样的算法加密输入，比对结果。
         *
         *   类比 JS：
         *     const match = await bcrypt.compare(dto.password, user.password)
         */
        if (!passwordEncoder.matches(loginDTO.getPassword(), sysUser.getPassword())) {
            throw new BusinessException(ResultCode.PARAM_ERROR.getCode(), "用户名或密码错误");
        }

        // 第4步：检查用户状态（status == 1 表示启用，其他值表示禁用）
        if (sysUser.getStatus() == null || sysUser.getStatus() != 1) { // 状态不是"启用"
            throw new BusinessException(ResultCode.FORBIDDEN.getCode(), "账户已被禁用，请联系管理员");
        }

        /*
         * 第5步：生成 JWT Token
         *
         * jwtUtil.generateToken(username, role)：
         *   把用户名和角色编码进 JWT 的 payload 中，用服务器密钥签名。
         *
         *   JWT 结构（三段，Base64 编码，用 . 分隔）：
         *     Header.Payload.Signature
         *     例如：eyJhbGciOiJIUzI1NiJ9.eyJ1c2VybmFtZSI6ImFkbWluIn0.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c
         *
         *   类比 JS：
         *     const token = jwt.sign({ username, role }, secretKey, { expiresIn: '24h' })
         */
        String token = jwtUtil.generateToken(sysUser.getUsername(), sysUser.getRole()); // 生成 JWT

        /*
         * 第6步：组装 LoginVO 返回
         *
         * LoginVO 包含 4 个字段：
         *   - token：JWT 签名字符串（前端存入 localStorage，后续请求带在 Authorization header）
         *   - username：用户名
         *   - realName：真实姓名（显示在页面右上角）
         *   - role：角色（ADMIN/TEACHER/STUDENT，前端据此显示不同菜单）
         */
        LoginVO loginVO = new LoginVO(); // 创建 LoginVO 对象
        loginVO.setToken(token);                  // 设置 Token
        loginVO.setUsername(sysUser.getUsername()); // 设置用户名
        loginVO.setRealName(sysUser.getRealName()); // 设置真实姓名
        loginVO.setRole(sysUser.getRole());         // 设置角色

        log.info("用户 {} 登录成功，角色：{}", sysUser.getUsername(), sysUser.getRole()); // 记录日志
        return loginVO; // 返回登录结果
    }

    /*
     * ================================================================
     * 方法 2：用户注册
     *
     * 业务流程（3 步）：
     *   1. 检查用户名是否已存在
     *   2. 加密密码 + 构建用户实体
     *   3. 保存到数据库
     * ================================================================
     */
    @Override
    public void register(RegisterDTO registerDTO) {

        /*
         * 第1步：检查用户名唯一性
         *
         * selectCount(queryWrapper)：查符合条件的记录数。
         *
         * 逻辑：如果 count > 0 → 用户名已被占用 → 抛异常。
         *
         * 为什么不直接用 selectOne？
         *   selectOne 查不到时返回 null（正常），查到多条时抛异常。
         *   selectCount 只返回一个数字，开销更小，语义更清晰。
         *
         *   类比 JS：
         *     const count = await User.countDocuments({ username: dto.username })
         *     if (count > 0) throw new Error('用户名已存在')
         */
        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysUser::getUsername, registerDTO.getUsername()); // 条件：username = 注册的用户名
        if (sysUserMapper.selectCount(queryWrapper) > 0) { // 查询记录数 > 0 表示已存在
            throw new BusinessException(ResultCode.BUSINESS_ERROR.getCode(), "用户名已存在");
        }

        /*
         * 第2步：构建用户实体对象
         *
         * new SysUser()：创建空的用户实体。
         *
         * passwordEncoder.encode(registerDTO.getPassword())：
         *   用 BCrypt 加密密码。同样的明文每次 encode 结果不同（含随机盐值）。
         *
         *   加密后的密文示例：
         *   $2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy
         *   这个字符串包含了：算法版本(2a)、计算轮次(10=2^10次)、盐值、密文
         *
         *   status = 1 表示用户状态为"启用"。
         *
         *   类比 JS：
         *     const hashedPassword = await bcrypt.hash(dto.password, 10)
         */
        SysUser sysUser = new SysUser(); // 创建用户实体
        sysUser.setUsername(registerDTO.getUsername()); // 设置用户名
        sysUser.setPassword(passwordEncoder.encode(registerDTO.getPassword())); // 加密密码
        sysUser.setRealName(registerDTO.getRealName()); // 设置真实姓名
        sysUser.setRole(registerDTO.getRole()); // 设置角色（ADMIN/TEACHER/STUDENT）
        sysUser.setStatus(1); // 默认状态：启用

        /*
         * 第3步：保存到数据库
         *
         * sysUserMapper.insert(sysUser)：MyBatis-Plus 通用插入方法。
         *
         * 最终 SQL：
         *   INSERT INTO sys_user (username, password, real_name, role, status) VALUES (?, ?, ?, ?, ?)
         *
         * 类比 JS：
         *   await User.create({ username, password: hashedPwd, realName, role, status: 1 })
         */
        sysUserMapper.insert(sysUser); // INSERT INTO sys_user ...

        log.info("用户 {} 注册成功，角色：{}", sysUser.getUsername(), sysUser.getRole()); // 记录日志
    }
}
