/*
 * ================================================================
 * 文件位置:  src/main/java/com/example/backend/vo/LoginVO.java
 * 对应前端:  frontend/src/views/login/Login.vue（登录页面）
 *           frontend/src/stores/user.js（用户状态管理 Pinia Store）
 *           frontend/src/api/auth.js（前端 API 调用）
 *
 * 数据流向（登录成功后）:
 *   用户提交 LoginDTO → 后端验证用户名密码 → 验证通过
 *     ↓ AuthService 生成 JWT Token
 *     ↓ 构建 LoginVO 对象:
 *       LoginVO {
 *         token: "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiIsInJvbGUiOiJBRE1JTiJ9...",
 *         username: "admin",
 *         realName: "管理员",
 *         role: "ADMIN"
 *       }
 *     ↓ Spring 用 Jackson 序列化为 JSON → 返回给前端
 *   前端收到: { code: 200, data: { token: 'xxx...', username: 'admin', realName: '管理员', role: 'ADMIN' } }
 *     ↓ 前端存 token 到 localStorage
 *     ↓ 前端存用户信息到 Pinia Store
 *     ↓ 跳转到首页（Dashboard）
 *     ↓ 后续所有 API 请求都在 Authorization 头里携带 token
 *
 * Token 是什么？
 *   Token（令牌）就像一张"通行证"。
 *   JWT（JSON Web Token）是一种具体的 token 格式，由三部分组成：
 *     Header（头部）      — 算法信息（如 HS256）
 *     Payload（载荷）    — 存放数据（用户名、角色、过期时间）
 *     Signature（签名）  — 防篡改的签名
 *   三部分用 . 号连接：xxxxx.yyyyy.zzzzz
 *
 *   前端拿到 token 后：
 *     1. 存在 localStorage 中
 *     2. 每次请求时放在 HTTP 头里: Authorization: Bearer <token>
 *     3. 后端 JwtAuthenticationFilter 拦截请求，从 Authorization 头提取 token
 *     4. 验证签名 → 提取用户信息 → 放到 SecurityContext 中
 *     5. 校验权限（如 @PreAuthorize 注解）
 *
 *   Token 和 Session 的区别:
 *     Session: 服务器存储用户状态（有状态），需要 Redis 等共享
 *     Token:   客户端存储信息（无状态），服务器不需要存储，分布式友好
 *     RESTful API 通常用 Token，因为无状态的设计更易扩展。
 *
 * LoginVO vs LoginDTO:
 *   LoginDTO — 前端 → 后端：只包含 username + password
 *   LoginVO  — 后端 → 前端：包含 token + username + realName + role
 *   LoginVO 包含的信息更多，因为它是登录成功后返回的"用户身份信息包"。
 *
 * JS 类比:
 *   // Pinia Store 中
 *   export const useUserStore = defineStore('user', () => {
 *     const token = ref('')
 *     const username = ref('')
 *     const realName = ref('')
 *     const role = ref('')
 *
 *     const login = async (loginForm) => {
 *       const res = await axios.post('/api/auth/login', loginForm)
 *       const data = res.data.data    // ← 这里是 LoginVO 的 JSON 表示
 *       token.value = data.token
 *       username.value = data.username
 *       realName.value = data.realName
 *       role.value = data.role
 *       localStorage.setItem('token', data.token)
 *     }
 *   })
 *
 *   // TypeScript 类型定义
 *   interface LoginResponse {
 *     token: string;      // JWT 令牌
 *     username: string;   // 用户名
 *     realName: string;   // 真实姓名
 *     role: string;       // 角色
 *   }
 * ================================================================
 */

// ==================== 1. 包声明 ====================

/*
 * package com.example.backend.vo;
 *   声明当前文件属于 vo 包。
 *   LoginVO 是全系统最重要的 VO 之一 — 登录成功后用户身份的凭证都在这。
 */
package com.example.backend.vo; // 声明 VO 包

// ==================== 2. 导入其他类（import） ====================

/*
 * import lombok.Data;
 *   Lombok 注解：自动生成所有 getter/setter 方法。
 *   LoginVO 的 4 个字段返回给前端时，需要 getter 方法让 Jackson 序列化。
 */
import lombok.Data; // Lombok 注解：自动生成 getter/setter/toString/equals/hashCode

// ==================== 3. 类的声明和注解 ====================

/*
 * @Data
 *   自动生成 token/username/realName/role 四个字段的 getter/setter 等。
 */
@Data // 自动生成 Getter/Setter/toString/equals/hashCode

/*
 * public class LoginVO { ... }
 *
 *   Login ← 业务场景：登录
 *   VO    ← 类型：视图对象（返回给前端）
 */
public class LoginVO {

    // ==================== 4. 字段声明 ====================

    /*
     * private String token;
     *
     *   分解解释:
     *     private —— 封装
     *     String  —— 字符串类型
     *     token   —— JWT 令牌
     *
     *   用途:       JWT（JSON Web Token）登录凭证
     *   生成方式:   后端 AuthService 使用 JJWT 库生成：
     *              - 用密钥签名（确保 token 没被篡改过）
     *              - Payload 中包含 username、role、过期时间
     *              - 过期后用户需要重新登录
     *   长度:       通常 200-400 字符（取决于 Payload 内容）
     *   格式:       xxx.yyy.zzz（三个 Base64 编码段用 . 连接）
     *   传给前端:   前端收到后存入 localStorage，后续请求带上 Bearer token
     *   示例值:     "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiIsInJvbGUiOiJBRE1JTiIsImV4cCI6MTcwMDAwMDAwMH0.abc123..."
     *
     *   安全性注意事项:
     *     1. Token 不能存在 Cookie 中（会被 CSRF 攻击利用），存在 localStorage
     *     2. Token 有过期时间（如 24 小时），过期后需重新登录
     *     3. Token 一旦签发，在过期前无法主动失效（除非加黑名单机制）
     *     4. 退出登录 = 前端删除 localStorage 中的 token
     *     5. 生产环境必须用 HTTPS，防止 token 被中间人截获
     *
     *   JS 类比:
     *     // 登录成功后
     *     const login = async () => {
     *       const { data } = await axios.post('/api/auth/login', loginForm)
     *       // data.data.token ← 就是这里的 token 字段
     *       localStorage.setItem('token', data.data.token)
     *
     *       // 设置 axios 全局拦截器，每次请求自动带 token
     *       axios.interceptors.request.use(config => {
     *         config.headers.Authorization = `Bearer ${localStorage.getItem('token')}`
     *         return config
     *       })
     *     }
     *
     *     // 退出登录
     *     const logout = () => {
     *       localStorage.removeItem('token')
     *       router.push('/login')
     *     }
     */
    private String token; // JWT Token 字符串，前端后续请求需携带此 Token（放在 Authorization 头中）

    // ================================================================

    /*
     * private String username;
     *
     *   用途:       登录用户名
     *   数据来源:   登录验证成功后，从数据库中查询到的用户记录的 username 字段
     *   前端使用:   在页面右上角显示用户名，或用于后续操作的用户标识
     *   示例值:     "admin"
     *
     *   为什么会返回 username？
     *     虽然 LoginDTO 中前端传了 username，后端"知道"用户名。
     *     但 RESTful API 的最佳实践是：后端应该返回确认后的用户名，
     *     让前端知道"确实是以这个用户身份登录的"。
     *     同时前端可能需要在不同页面显示用户名，直接存在 Store 中比每次解析 Token 方便。
     */
    private String username; // 用户名（前端用于显示和后续请求）

    // ================================================================

    /*
     * private String realName;
     *
     *   用途:       用户真实姓名（显示用）
     *   数据来源:   从 user 表的 real_name 列查询得到
     *   前端使用:   登录后在页面右上角显示"欢迎，张三"
     *              菜单栏显示用户真实姓名
     *   示例值:     "管理员"、"张三"
     *
     *   为什么返回 realName 而不只是 username？
     *     - username 是登录用的技术标识（如 "admin"、"zhangsan"）
     *     - realName 是给人看的中文名（如 "管理员"、"张三"）
     *     - 前端界面上显示"张三"比显示"zhangsan"更友好
     *     - 一次登录请求把所需信息全部返回，避免前端再发请求获取
     *
     *   JS 类比:
     *     // 登录后显示用户名
     *     <span>欢迎，{{ userStore.realName }}</span>
     *     // 显示 "欢迎，张三" 而不是 "欢迎，zhangsan"
     */
    private String realName; // 用户真实姓名（用于界面展示，比 username 更友好）

    // ================================================================

    /*
     * private String role;
     *
     *   用途:       用户角色（权限标识）
     *   数据来源:   从 user 表的 role 列查询得到
     *   前端使用:   前端通过角色判断显示哪些菜单、哪些按钮
     *              - ADMIN:   显示所有管理菜单
     *              - TEACHER: 显示教师相关功能
     *              - STUDENT: 显示学生选课、查看成绩等功能
     *   可选值:     "ADMIN"、"TEACHER"、"STUDENT"
     *   示例值:     "ADMIN"
     *
     *   前端路由守卫使用 scene:
     *     // Vue Router 中
     *     router.beforeEach((to, from, next) => {
     *       const role = userStore.role
     *       if (to.meta.roles && !to.meta.roles.includes(role)) {
     *         next('/403') // 角色不匹配，跳转到 403 页面
     *       } else {
     *         next()
     *       }
     *     })
     *
     *   注意：前端角色校验只是 UX（用户体验）层面的，
     *   真正的安全控制在后端（@PreAuthorize 注解）。
     *   攻击者可以修改前端 JS 绕过前端检查，但无法绕过后端的权限拦截。
     *   这就是"前端校验防君子，后端校验防小人"。
     */
    private String role; // 用户角色：ADMIN（管理员）/ TEACHER（教师）/ STUDENT（学生）
}
