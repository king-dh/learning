/*
 * ================================================================
 * 文件位置:  src/main/java/com/example/backend/controller/AuthController.java
 * 对应前端:  frontend/src/views/login/Login.vue（登录页面）
 *           frontend/src/api/auth.js（前端 API 调用）
 *
 * 调用链路（以"用户登录"为例）:
 * 用户在登录页输入用户名密码 → axios 发 POST /api/auth/login
 *                           → Vite 代理转发到 localhost:8088
 *                           → 本 Controller 的 login() 方法
 *                           → AuthServiceImpl.login()
 *                           → Spring Security 的 AuthenticationManager 验证密码
 *                           → JwtUtil.generateToken() 生成 JWT
 *                           → 返回 JSON（含 Token）给前端
 *                           → 前端 localStorage.setItem('token', token)
 *
 * 注意：登录和注册接口不需要 JWT 认证（在 SecurityConfig 中已放行），
 * 是系统中唯一不需要携带 Token 的接口。
 * ================================================================
 */

// ==================== 1. 包声明 ====================

/*
 * package 关键字：声明当前文件属于 controller 子包。
 * 完整路径：com.example.backend.controller
 * 对应硬盘路径：com/example/backend/controller/
 *
 * 与 JavaScript 类比：
 *   JS  namespace/模块路径
 *   Java package 声明
 */
package com.example.backend.controller;

// ==================== 2. 导入其他类（import） ====================

/*
 * import 关键字：告诉编译器"我需要使用哪些类"。
 * 与 JavaScript 的 import { xxx } from './xxx' 概念相同，但 Java 使用完整包路径。
 */

// --- 2.1 本项目内部的类（Common 层）---
import com.example.backend.common.Result;       // 统一响应结果包装类（{ code: 200, message: "...", data: {...} }）

// --- 2.2 本项目内部的类（DTO 层 - 数据传输对象）---
import com.example.backend.dto.LoginDTO;        // 登录请求 DTO：接收前端传来的 { username, password }
import com.example.backend.dto.RegisterDTO;      // 注册请求 DTO：接收前端传来的 { username, password, role, realName }

// --- 2.3 本项目内部的类（Service 层）---
import com.example.backend.service.AuthService;  // 认证服务接口（处理登录注册业务逻辑）

// --- 2.4 本项目内部的类（VO 层 - 视图对象）---
import com.example.backend.vo.LoginVO;          // 登录响应 VO：返回给前端的 { token, username, role, realName }

// --- 2.5 SpringDoc 注解（用于生成 Swagger 接口文档）---
import io.swagger.v3.oas.annotations.Operation;  // @Operation：给每个接口方法添加说明文字
import io.swagger.v3.oas.annotations.tags.Tag;    // @Tag：给整个 Controller 分组命名

// --- 2.6 Bean Validation 注解（用于参数校验）---
// @Valid 告诉 Spring："在调用这个方法之前，先检查参数对象里的校验注解（如 @NotBlank）是否通过"
import jakarta.validation.Valid;                 // @Valid：开启 Java Bean 验证，触发 DTO 中的 @NotBlank 等校验

/*
 * 注意：这里引入的是 jakarta.validation.Valid，而不是 javax.validation.Valid。
 * 这是因为 Spring Boot 3.x 迁移到了 Jakarta EE 9+，包名从 javax.* 变为 jakarta.*。
 * 类比 JavaScript：就像从 require 迁移到 import，本质功能相同但写法不同。
 */

// --- 2.7 Lombok 注解（减少样板代码）---
import lombok.RequiredArgsConstructor;           // @RequiredArgsConstructor：自动生成带 final 字段的构造函数

// --- 2.8 Spring Web 注解（定义 REST 接口）---
import org.springframework.web.bind.annotation.PostMapping;    // @PostMapping：对应 HTTP POST 请求
import org.springframework.web.bind.annotation.RequestBody;    // @RequestBody：把请求体 JSON → Java 对象
import org.springframework.web.bind.annotation.RequestMapping; // @RequestMapping：统一路径前缀
import org.springframework.web.bind.annotation.RestController; // @RestController：REST 控制器（返回 JSON）

// ==================== 3. 类的声明和注解 ====================

/*
 * @Tag(name = "认证管理", description = "用户登录和注册接口")
 *   SpringDoc 注解：在 Swagger UI 页面上给这组接口起分组名。
 *   打开 http://localhost:8088/swagger-ui.html 就能看到"认证管理"这个分组。
 */
@Tag(name = "认证管理", description = "用户登录和注册接口") // Knife4j 接口分组标签

/*
 * @RestController 注解：
 *   告诉 Spring："这个类的每个方法返回值都要转成 JSON，而不是跳转 HTML 页面"。
 *   等价于同时加了 @Controller + @ResponseBody 两个注解。
 *
 *   类比 JavaScript Express：
 *     app.post('/api/auth/login', (req, res) => { res.json({ data: ... }) })
 *                                        ↑ 返回 JSON，不返回 HTML ↑
 */
@RestController

/*
 * @RequestMapping("/api/auth")：
 *   给这个 Controller 里所有接口统一加上 /api/auth 路径前缀。
 *
 *   例如：
 *     login() 方法上有 @PostMapping("/login")
 *     最终完整 URL = @RequestMapping 前缀 + @PostMapping 路径 = /api/auth/login
 *
 *   类比 JavaScript Express：
 *     const router = express.Router();
 *     app.use('/api/auth', router);             ← @RequestMapping("/api/auth")
 *     router.post('/login', (req, res) => {...}) ← @PostMapping("/login")
 */
@RequestMapping("/api/auth")

/*
 * @RequiredArgsConstructor：
 *   Lombok 注解：编译时自动生成一个构造函数，参数是所有带 final 修饰的字段。
 *
 *   本类中只有 authService 是 final 的，所以等价于手写：
 *     public AuthController(AuthService authService) {
 *         this.authService = authService;
 *     }
 *
 *   Spring 的依赖注入：
 *     Spring 发现构造函数需要 AuthService 参数 → 从容器中找到 AuthServiceImpl → 自动注入。
 *
 *   类比 JavaScript：
 *     class AuthController {
 *       #authService; // private field
 *       constructor(service) { this.#authService = service } // DI
 *     }
 */
@RequiredArgsConstructor

/*
 * public class AuthController { ... }
 *
 *   public  —— 这个类可以被任何其他类使用（类似 JS 的 export class）
 *   class   —— 定义类的关键字
 *   AuthController —— 类名，必须和文件名完全一致（Java 硬性规定）
 */
public class AuthController {

    // ==================== 4. 字段声明（成员变量） ====================

    /*
     * private final AuthService authService;
     *
     *   分解解释：
     *     private      —— 只能在本类内部访问，外部拿不到（封装性）
     *     final        —— 一旦赋值就不能再改（类似 JS 的 const）
     *                     final 字段必须在构造函数里赋值，由 @RequiredArgsConstructor 自动完成
     *     AuthService  —— 字段类型是 AuthService 接口（面向接口编程）
     *     authService  —— 字段名，驼峰命名（首字母小写）
     *
     *   认证控制器只负责"接线"，不写任何认证逻辑，认证逻辑在 AuthService 里。
     *   类比 JS：Controller 就是 Express 的路由处理器，Service 才是真正的业务代码。
     */
    private final AuthService authService; // 认证服务（Spring 自动注入，Controller 靠它处理登录注册）

    // ==================== 5. 接口方法 ====================

    /*
     * ================================================================
     * 接口 1：用户登录
     *
     * 请求示例：POST /api/auth/login
     * 请求体 JSON：{ "username": "admin", "password": "admin123" }
     * 响应 JSON：{ "code": 200, "message": "操作成功", "data": { "token": "eyJ...", "username": "admin", ... } }
     *
     * 数据流转（完整链路）：
     *   前端发送 JSON { username, password }
     *     ↓ 网络传输（HTTP 请求体）
     *     ↓ Spring 用 Jackson 把 JSON 自动转成 LoginDTO 对象
     *     ↓ @Valid 触发 LoginDTO 里的 @NotBlank 校验（用户名密码不能为空）
     *   本方法收到 LoginDTO dto = { username: "admin", password: "admin123" }
     *     ↓ 调 Service 层
     *   authService.login(dto)
     *     ↓ Service 内部：
     *     ↓   1. 用 AuthenticationManager 验证密码（BCrypt 比对）
     *     ↓   2. 调用 JwtUtil.generateToken() 生成 JWT 字符串
     *     ↓   3. 封装成 LoginVO { token, username, role, realName }
     *   Result.ok(loginVO)
     *     ↓ Result.ok() 包装成统一格式：{ code: 200, message: "操作成功", data: loginVO }
     *     ↓ @RestController 自动序列化为 JSON 字符串
     *   前端收到完整的 JSON 响应 → 将 token 保存到 localStorage
     *     → 后续请求在 header 中携带 Authorization: Bearer <token>
     * ================================================================
     */

    /*
     * @Operation(summary = "用户登录")
     *   SpringDoc 注解：在 Swagger 页面显示"用户登录"说明。
     */
    @Operation(summary = "用户登录") // Knife4j 接口说明

    /*
     * @PostMapping("/login")
     *   - 告诉 Spring：这个方法是用来处理 HTTP POST 请求的
     *   - "/login" 拼上类级别的 @RequestMapping("/api/auth") = /api/auth/login
     *   - 登录通常用 POST 而不是 GET，因为密码不应该暴露在 URL 中
     *
     *   类比 JavaScript Express：
     *     router.post('/login', (req, res) => { ... })
     */
    @PostMapping("/login") // 处理 POST /api/auth/login 请求

    /*
     * public Result<LoginVO> login(@Valid @RequestBody LoginDTO loginDTO) { ... }
     *
     *   Result<LoginVO>   —— 返回值类型：Result 里包裹 LoginVO（泛型指定具体类型）
     *   login             —— 方法名
     *
     *   @Valid LoginDTO loginDTO：
     *     @Valid       —— 告诉 Spring：在调用方法前，先对 loginDTO 做校验
     *                     校验规则写在 LoginDTO 类的字段注解上（如 @NotBlank）
     *                     校验失败会抛出 MethodArgumentNotValidException
     *     @RequestBody  —— 告诉 Spring：从 HTTP 请求体取 JSON，自动转成 LoginDTO 对象
     *     LoginDTO      —— 接收参数的类型（Data Transfer Object，数据传输对象）
     *     loginDTO      —— 参数变量名
     *
     *   类比 JavaScript Express：
     *     router.post('/login', (req, res) => {
     *       const { username, password } = req.body // ← @RequestBody LoginDTO loginDTO
     *       if (!username || !password) return res.status(400).json(...) // ← @Valid 自动完成
     *       const result = authService.login({ username, password })
     *       res.json({ code: 200, data: result })
     *     })
     */
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO loginDTO) { // @Valid 开启校验，@RequestBody 接收 JSON

        /*
         * authService.login(loginDTO)
         *
         *   调用 Service 层的登录方法：
         *     1. 通过 AuthenticationManager 验证用户名和密码
         *     2. 验证通过后生成 JWT Token
         *     3. 返回 LoginVO 对象（含 token, username, role, realName）
         *
         *   如果用户名或密码错误，Service 会抛出 BusinessException
         *   该异常会被 GlobalExceptionHandler 统一捕获，返回 { code: 4xxx, message: "用户名或密码错误" }
         */
        LoginVO loginVO = authService.login(loginDTO); // 调用登录服务：验证密码 → 生成 Token → 返回 VO

        /*
         * Result.ok(loginVO)
         *
         *   调用 Result 类的静态方法 ok()，把 LoginVO 包一层统一格式：
         *     { code: 200, message: "操作成功", data: { token: "eyJ...", username: "admin", ... } }
         *
         *   Spring 的 @RestController 会自动把这个 Result 对象序列化为 JSON 字符串返回给前端。
         */
        return Result.ok(loginVO); // 返回成功结果（包含 Token）
    }

    // ================================================================
    // 接口 2：用户注册
    // ================================================================

    /*
     * ================================================================
     * 接口 2：用户注册
     *
     * 请求示例：POST /api/auth/register
     * 请求体 JSON：{ "username": "newuser", "password": "123456", "role": "STUDENT", "realName": "新用户" }
     *
     * 数据流转：
     *   前端发送注册 JSON
     *     ↓ @RequestBody + Jackson 自动转成 RegisterDTO 对象
     *     ↓ @Valid 触发校验（用户名/密码/角色/真实姓名都不能为空）
     *   authService.register(registerDTO)
     *     ↓ Service 内部：
     *     ↓   1. 检查用户名是否已存在（存在则抛异常）
     *     ↓   2. 用 BCryptPasswordEncoder 加密密码
     *     ↓   3. INSERT INTO sys_user
     *   Result.ok(null)
     *     ↓ 注册成功，无需返回数据，所以 data 传 null
     *     ↓ 前端收到 { code: 200, message: "操作成功", data: null }
     *     ↓ 前端根据 code === 200 判断成功，跳转到登录页
     * ================================================================
     */

    @Operation(summary = "用户注册") // Knife4j 接口说明

    /*
     * @PostMapping("/register")
     *   - HTTP POST 请求，路径为 /api/auth/register
     *   - 和登录一样，注册也是一个"写操作"，语义上应该用 POST
     */
    @PostMapping("/register") // 处理 POST /api/auth/register 请求

    /*
     * public Result<Void> register(@Valid @RequestBody RegisterDTO registerDTO) { ... }
     *
     *   Result<Void>  —— 注册成功不需要返回数据，用 Void（Java 的空类型，注意是大写 V）
     *   register      —— 方法名
     *
     *   @Valid @RequestBody RegisterDTO registerDTO：
     *     和登录接口相同的参数接收方式。
     *     RegisterDTO 里有 @NotBlank 注解校验用户名、密码等必填字段。
     *
     *   为什么不需要 @PreAuthorize？
     *     注册接口应该是公开的，不需要登录就能访问。
     *     在 SecurityConfig 中已经放行 /api/auth/register，不需要额外的权限注解。
     */
    public Result<Void> register(@Valid @RequestBody RegisterDTO registerDTO) { // @Valid 开启参数校验

        /*
         * authService.register(registerDTO)
         *
         *   调用 Service 层的注册方法：
         *     1. 检查用户名是否已存在
         *     2. BCrypt 加密密码（明文密码从不入库）
         *     3. 插入 sys_user 表
         *
         *   如果用户名已存在，Service 会抛出 BusinessException
         *   被 GlobalExceptionHandler 捕获后返回 { code: 4000, message: "用户名已存在" }
         */
        authService.register(registerDTO); // 调用注册服务：校验唯一性 → 加密密码 → 入库

        /*
         * return Result.ok(null);
         *
         *   null 表示"没有业务数据需要返回"，但 Result 框架仍然会生成标准的成功响应：
         *     { code: 200, message: "操作成功", data: null }
         *   前端看到 code === 200 就知道注册成功了，然后跳转到登录页。
         */
        return Result.ok(null); // 注册成功，无返回数据
    }

    /*
     * ================================================================
     * 总结：AuthController 的职责
     *
     * 1. 接收登录和注册的 HTTP 请求
     * 2. 用 @Valid 做参数校验（保证用户名密码不为空）
     * 3. 调 AuthService 处理实际的认证逻辑
     * 4. 用 Result 包装返回值，保持统一格式
     *
     * 和 StudentController 最大的不同：
     *   - AuthController 不需要 @PreAuthorize（接口是公开的，不需要权限）
     *   - AuthController 没有 CRUD 操作，只有登录和注册两个接口
     *   - 登录接口返回的 LoginVO 中包含 JWT Token，前端需要保存
     *
     * 文件对照表：
     *   Controller → AuthController.java       ← 当前文件（接线员）
     *   Service    → AuthServiceImpl.java       （认证逻辑实现）
     *   DTO        → LoginDTO.java              （接收登录 JSON）
     *   DTO        → RegisterDTO.java           （接收注册 JSON）
     *   VO         → LoginVO.java               （返回登录结果，含 Token）
     *   Entity     → SysUser.java               （用户表映射）
     * ================================================================
     */
}
