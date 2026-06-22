/*
 * ================================================================
 * 文件位置:  src/main/java/com/example/backend/dto/LoginDTO.java
 * 对应前端:  frontend/src/views/login/Login.vue（登录页面）
 *           frontend/src/api/auth.js（前端认证 API 调用）
 *
 * 数据流向（用户登录流程）:
 *   用户打开登录页面，输入用户名和密码
 *     ↓ 点击"登录"按钮
 *   axios.post('/api/auth/login', { username: 'admin', password: '123456' })
 *     ↓ JSON 请求体
 *     ↓ Vite 代理转发 → Spring Boot
 *   Spring 用 Jackson 把 JSON 转成 LoginDTO 对象:
 *     LoginDTO { username="admin", password="123456" }
 *     ↓ 在这之前，@NotBlank 校验拦截：如果 username 或 password 为空，直接返回 400 错误
 *     ↓ AuthController.login() → AuthService → 查数据库验证用户名密码
 *     ↓ 验证通过 → 生成 JWT Token
 *     ↓ 返回 LoginVO { token: "xxx...", username: "admin", realName: "管理员", role: "ADMIN" }
 *
 * 为什么密码是明文传输？
 *   登录时的密码确实是明文从浏览器传到后端。
 *   但这是安全的，因为：
 *     - 生产环境使用 HTTPS 加密整个传输通道（TLS 加密）
 *     - 密码在 HTTP 层面传输时已被 TLS 加密，中间人看不到
 *     - 后端收到明文后立刻用 BCrypt 加密和数据库中的哈希做对比
 *     - 绝不能在前端对密码做哈希再传（那等于哈希值就是密码了）
 *
 * JS 类比:
 *   // 前端登录表单
 *   const loginForm = reactive({
 *     username: '',
 *     password: ''
 *   })
 *
 *   const handleLogin = async () => {
 *     const res = await axios.post('/api/auth/login', loginForm)
 *     // res.data = { token: 'xxx', username: 'admin', ... }
 *     localStorage.setItem('token', res.data.token)
 *     router.push('/dashboard')
 *   }
 *
 *   // TypeScript 接口
 *   interface LoginRequest {
 *     username: string;
 *     password: string;
 *   }
 * ================================================================
 */

// ==================== 1. 包声明 ====================

/*
 * package com.example.backend.dto;
 *   声明当前文件属于 dto 包。
 *   LoginDTO 是登录请求的数据传输对象，放在 dto 包统一管理。
 */
package com.example.backend.dto; // 声明 DTO 包

// ==================== 2. 导入其他类（import） ====================

/*
 * import jakarta.validation.constraints.NotBlank;
 *
 *   jakarta.validation 是 Java Bean Validation（校验框架）的包名。
 *   这个框架从 Java EE 时代叫 javax.validation，后来改名 jakarta.validation。
 *   它在 pom.xml 中通过 spring-boot-starter-validation 引入。
 *
 *   @NotBlank 是校验注解，属于"声明式校验"：
 *     - 你只需要在字段上加 @NotBlank 注解，不需要写 if (username == null) 这样的校验代码
 *     - Spring 在接收到请求后、调用 Controller 方法前，自动检查注解
 *     - 如果不通过，直接返回 400 Bad Request + message 里的错误信息
 *     - 整个过程不需要你写一行校验逻辑代码
 *
 *   @NotBlank vs @NotNull vs @NotEmpty:
 *     @NotNull    —— 不能是 null
 *     @NotEmpty   —— 不能是 null 且不能是空字符串 ""
 *     @NotBlank   —— 不能是 null、不能是空字符串 ""、不能是纯空格 "   "
 *                  （最严格，适合用户名/密码这种不允许空白字符的字段）
 *
 *   JS 类比:
 *     // 前端表单校验（Element Plus）
 *     <el-form :rules="rules">
 *       <el-form-item prop="username">
 *         <el-input v-model="loginForm.username" />
 *       </el-form-item>
 *     </el-form>
 *     const rules = {
 *       username: [{ required: true, message: '用户名不能为空', trigger: 'blur' }]
 *     }
 *     // 后端 @NotBlank 就是这些规则的后端版本
 */
import jakarta.validation.constraints.NotBlank; // 校验注解：字符串不能为 null / 空字符串 / 纯空格

/*
 * import lombok.Data;
 *   Lombok 注解，为 DTO 自动生成 getter/setter 方法。
 */
import lombok.Data; // Lombok 注解：自动生成 getter/setter/toString/equals/hashCode

// ==================== 3. 类的声明和注解 ====================

/*
 * @Data
 *   编译时自动生成 getUsername()/setUsername()/getPassword()/setPassword() 等全部方法。
 *   Spring 接收 JSON 时需要调用 setter 方法赋值。
 */
@Data // 自动生成 Getter/Setter/toString/equals/hashCode

/*
 * public class LoginDTO { ... }
 *
 *   Login  ← 业务场景：登录
 *   DTO    ← 类型：数据传输对象
 *
 *   这个类的结构非常简单：只有用户名和密码两个字段。
 *   但它的重要性很高 — 整个系统的入口验证都靠它。
 */
public class LoginDTO {

    // ==================== 4. 字段声明 ====================

    /*
     * @NotBlank(message = "用户名不能为空")
     * private String username;
     *
     *   逐行解释:
     *
     *   @NotBlank(message = "用户名不能为空")
     *     @NotBlank        —— Jakarta Validation 的校验注解，告诉 Spring:
     *                         "在调用 Controller 方法之前，先检查这个字段的值。"
     *     message          —— 注解的属性（参数），指定校验不通过时返回给前端的错误信息。
     *                        前端会收到类似 { code: 400, message: "用户名不能为空" } 的响应。
     *                        message 是可选属性，如果不写，会使用默认英文提示。
     *
     *   校验执行时机（什么时候检查？）:
     *     1. 前端发 POST /api/auth/login，请求体 JSON: { "username": "", "password": "123" }
     *     2. Spring 先尝试把 JSON 转成 LoginDTO
     *     3. 转换完成后，Spring 检查 DTO 上的校验注解
     *     4. 发现 @NotBlank 标注的 username 为空字符串 → 校验不通过
     *     5. 直接抛出 MethodArgumentNotValidException（方法参数校验异常）
     *     6. 全局异常处理器 GlobalExceptionHandler 捕获，返回 400 + 错误信息
     *     7. Controller 的 login() 方法根本不会被执行（被校验拦截了）
     *
     *   这样可以保证:
     *     - Controller 方法真正执行时，所有字段都已经通过校验
     *     - Controller 不需要写 if (username == null) 这样的冗余校验代码
     *     - 校验逻辑集中管理，和维护业务逻辑分开
     *
     *   private String username;
     *     private   —— 封装
     *     String    —— 字符串类型
     *     username  —— 用户名，用于登录认证
     *
     *   用途:       登录用户名
     *   数据来源:   前端登录表单的"用户名"输入框
     *   后端使用:   AuthService 用 username 去数据库查用户，对比密码
     *   示例值:     "admin"、"zhangsan"
     *
     *   JS 类比（前端校验）:
     *     // Element Plus 表单校验规则
     *     const loginRules = {
     *       username: [
     *         { required: true, message: '用户名不能为空', trigger: 'blur' },
     *         { whitespace: true, message: '用户名不能全是空格', trigger: 'blur' }
     *       ]
     *     }
     *     // 后端的 @NotBlank 就是这些规则的后端等价物
     */
    @NotBlank(message = "用户名不能为空") // 校验：用户名不能为 null 或空字符串或纯空格
    private String username; // 登录用户名（前端登录表单输入）

    // ================================================================

    /*
     * @NotBlank(message = "密码不能为空")
     * private String password;
     *
     *   分解解释:
     *
     *   @NotBlank(message = "密码不能为空")
     *     和 username 上的注解完全一样。
     *     校验密码字段不能是 null、空字符串或纯空格。
     *
     *     注意：这里只校验"是否为空"，不校验密码长度或复杂度。
     *     如果需要"密码至少 6 位"的校验，可以用 @Size(min = 6)。
     *     但本项目把密码长度校验放在了前端，后端只检查是否为空。
     *
     *   private String password;
     *     private   —— 封装
     *     String    —— 字符串类型
     *     password  —— 密码
     *
     *   用途:       登录密码（明文，由前端原样传输）
     *   数据来源:   前端登录表单的"密码"输入框（type="password"）
     *   后端使用:   AuthService 用 BCryptPasswordEncoder.matches() 比对
     *              明文密码 → BCrypt 哈希（单向不可逆）→ 和数据库中的哈希比较
     *   安全性:    通过 HTTPS 传输层加密保护，不会明文暴露在网络上
     *   示例值:     "123456"
     *
     *   密码处理流程（重要！）:
     *     1. 用户输入 "123456"（明文）
     *     2. HTTPS 加密传输到后端
     *     3. 后端收到 "123456"（明文，因为 TLS 在 HTTP 层以下就解密了）
     *     4. 后端调 BCryptPasswordEncoder.matches("123456", 数据库中存哈希)
     *     5. BCrypt 用同样的算法加盐，算出一个哈希值
     *     6. 比较两个哈希是否相等 → 相等 = 密码正确
     *
     *   BCrypt 为什么安全？
     *     - 单向函数：从哈希值无法反推出原始密码
     *     - 加盐（Salt）：相同密码每次生成的哈希不同，防彩虹表攻击
     *     - 慢哈希：故意放慢计算速度（约 0.1 秒），防暴力穷举
     *     - 数据库即使泄露，攻击者也拿不到明文密码
     *
     *   JS 类比:
     *     // 前端
     *     <el-input v-model="loginForm.password" type="password" show-password />
     *     // type="password" 使浏览器隐藏输入内容（显示 ●●●●●●）
     *     // show-password 添加切换明文/密文的眼睛图标
     */
    @NotBlank(message = "密码不能为空") // 校验：密码不能为 null 或空字符串或纯空格
    private String password; // 登录密码（明文，后端验证后不存储，用 BCrypt 比对）
}
