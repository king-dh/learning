/*
 * ================================================================
 * 文件位置:  src/main/java/com/example/backend/dto/RegisterDTO.java
 * 对应前端:  frontend/src/views/login/Register.vue（注册页面）
 *           frontend/src/api/auth.js（前端认证 API 调用）
 *
 * 数据流向（用户注册流程）:
 *   用户打开注册页面，填写用户名、密码、真实姓名、选择角色
 *     ↓ 点击"注册"按钮
 *   axios.post('/api/auth/register', {
 *     username: 'newstudent',
 *     password: 'secure123',
 *     realName: '李四',
 *     role: 'STUDENT'
 *   })
 *     ↓ JSON 请求体 → Vite 代理 → Spring Boot
 *   Spring 用 Jackson 转成 RegisterDTO 对象:
 *     RegisterDTO { username="newstudent", password="secure123", realName="李四", role="STUDENT" }
 *     ↓ @Valid/@NotBlank 校验 → 拒绝空值
 *     ↓ AuthController.register() → AuthService
 *     ↓ 检查用户名是否已存在 → 不存在则继续
 *     ↓ 用 BCrypt 加密密码
 *     ↓ INSERT INTO user 表（创建新用户记录）
 *     ↓ 如果是 STUDENT 角色，可能还要在 student 表创建对应记录
 *     ↓ 返回成功
 *
 * RegisterDTO vs LoginDTO:
 *   LoginDTO    只有 username + password（登录只需要这两个）
 *   RegisterDTO 有 username + password + realName + role（注册需要更多信息）
 *   两者分工明确，不能混用（单一职责原则）
 *
 * JS 类比:
 *   // 前端注册表单
 *   const registerForm = reactive({
 *     username: '',
 *     password: '',
 *     confirmPassword: '',  // 前端确认密码，不需要传给后端
 *     realName: '',
 *     role: 'STUDENT'
 *   })
 *
 *   const handleRegister = async () => {
 *     // 前端校验两次密码一致（不需要后端校验这个）
 *     if (registerForm.password !== registerForm.confirmPassword) {
 *       return ElMessage.error('两次密码不一致')
 *     }
 *     // 发送注册请求
 *     const res = await axios.post('/api/auth/register', {
 *       username: registerForm.username,
 *       password: registerForm.password,
 *       realName: registerForm.realName,
 *       role: registerForm.role
 *     })
 *   }
 *
 *   // TypeScript 接口
 *   interface RegisterRequest {
 *     username: string;
 *     password: string;
 *     realName: string;
 *     role: 'ADMIN' | 'TEACHER' | 'STUDENT';
 *   }
 * ================================================================
 */

// ==================== 1. 包声明 ====================

/*
 * package com.example.backend.dto;
 *   声明当前文件属于 dto 包。
 *   RegisterDTO 是注册请求的数据载体。
 */
package com.example.backend.dto; // 声明 DTO 包

// ==================== 2. 导入其他类（import） ====================

/*
 * import jakarta.validation.constraints.NotBlank;
 *
 *   Jakarta Bean Validation 的校验注解。
 *   @NotBlank 确保字段不能是 null、空字符串或纯空格字符串。
 *
 *   为什么注册比登录多了 2 个 @NotBlank？
 *     登录只需要校验用户名和密码不为空。
 *     注册还需要校验真实姓名和角色不为空，因为这两个是创建用户记录的必需信息。
 *
 *   校验顺序:
 *     Spring 先转换 JSON 为 RegisterDTO → 再执行校验注解 → 通过后才进入 Controller 方法
 *     如果有任何一个 @NotBlank 校验失败，Controller 方法根本不会执行。
 */
import jakarta.validation.constraints.NotBlank; // 校验注解：字符串不能为空或纯空格

/*
 * import lombok.Data;
 *   Lombok 注解：自动生成所有 getter/setter 方法。
 */
import lombok.Data; // Lombok 注解：自动生成 getter/setter/toString/equals/hashCode

// ==================== 3. 类的声明和注解 ====================

/*
 * @Data
 *   自动生成以下方法:
 *     getUsername()/setUsername()、getPassword()/setPassword()
 *     getRealName()/setRealName()、getRole()/setRole()
 *     toString()、equals()、hashCode()
 */
@Data // 自动生成 Getter/Setter/toString/equals/hashCode

/*
 * public class RegisterDTO { ... }
 *
 *   Register  ← 业务场景：注册
 *   DTO       ← 类型：数据传输对象
 */
public class RegisterDTO {

    // ==================== 4. 字段声明 ====================

    /*
     * @NotBlank(message = "用户名不能为空")
     * private String username;
     *
     *   分解解释:
     *
     *   @NotBlank(message = "用户名不能为空")
     *     和 LoginDTO 一样，注册时用户名不能为空。
     *     实际业务中可能还需要更复杂的校验（如长度、字符类型），
     *     但本项目用 @NotBlank 做最基础的校验，其他前端处理。
     *
     *   private String username;
     *     用户名，注册后用作登录凭据。
     *     通常要求全系统唯一，这一点由 Service 层在数据库层面保证（UNIQUE 约束）。
     *
     *   用途:       注册时设置的用户名，后续用于登录
     *   数据来源:   前端注册表单的"用户名"输入框
     *   后端处理:   检查数据库中是否已存在该用户名 → 不存在则创建
     *   示例值:     "newstudent"、"teacher_zhang"
     *
     *   业务规则（由 Service 层实现，不在此 DTO 体现）:
     *     - 用户名必须全系统唯一
     *     - 不能和已有用户名重复
     *     - 如果重复，返回 400 "用户名已存在"
     */
    @NotBlank(message = "用户名不能为空") // 校验：用户名不能为空
    private String username; // 登录用户名，需要全系统唯一

    // ================================================================

    /*
     * @NotBlank(message = "密码不能为空")
     * private String password;
     *
     *   分解解释:
     *
     *   @NotBlank(message = "密码不能为空")
     *     注册密码不能为空。
     *     同样，实际项目中会加上 @Size(min = 6, max = 20) 等长度限制。
     *
     *   private String password;
     *     用户设置的密码。
     *     前端可能有一个"确认密码"字段做二次确认，但不需要发给后端（前后端约定）。
     *
     *   用途:       注册时设置的登录密码
     *   数据来源:   前端注册表单的"密码"输入框
     *   后端处理:   用 BCryptPasswordEncoder.encode() 加密后存入数据库
     *              前端传的是明文，后端存的是哈希（加密后的不可逆字符串）
     *              登录时用 BCryptPasswordEncoder.matches() 验证
     *
     *   密码处理时间线:
     *     注册:   前端明文"123456" → 传输（HTTPS加密） → 后端收到明文 → BCrypt 加密 → 存哈希到 DB
     *     登录:   前端明文"123456" → 传输（HTTPS加密） → 后端收到明文 → BCrypt 比对 DB 哈希 → 验证通过
     *
     *   BCrypt 加密后的样子（示例）:
     *     明文: "123456"
     *     哈希: "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy"
     *                     ↑                   ↑
     *                     Salt（盐值）         Hash（哈希值）
     *     同一密码每次加密结果不同（因为 Salt 随机），但验证时能正确匹配。
     */
    @NotBlank(message = "密码不能为空") // 校验：密码不能为空
    private String password; // 登录密码，后端会 BCrypt 加密后存储（永远不存明文密码）

    // ================================================================

    /*
     * @NotBlank(message = "真实姓名不能为空")
     * private String realName;
     *
     *   分解解释:
     *
     *   @NotBlank(message = "真实姓名不能为空")
     *     真实姓名不能为空。
     *     在注册时要求填写真实姓名，便于系统中显示用户身份。
     *
     *   private String realName;
     *     realName —— 真实姓名（区别于 username 登录名）
     *                username 是登录用的（如 "zhangsan"）
     *                realName 是显示用的（如 "张三"）
     *
     *   用途:       用户的真实姓名，在系统中展示用
     *   数据来源:   前端注册表单的"真实姓名"输入框
     *   前端使用:   登录成功后，在页面右上角显示"欢迎你，张三"
     *   后端存储:   user 表的 real_name 列
     *   示例值:     "张三"、"李四"
     *
     *   为什么分 username 和 realName？
     *     - username 用于登录认证（通常英文或数字），是系统内部的标识
     *     - realName 用于界面展示，是人类可读的中文名
     *     - 分两个字段使系统更灵活（比如可以改真实姓名而不影响登录）
     *
     *   JS 类比:
     *     interface UserInfo {
     *       username: string;   // 登录名：zhangsan
     *       realName: string;   // 真名：张三
     *       role: string;       // 角色：STUDENT
     *     }
     */
    @NotBlank(message = "真实姓名不能为空") // 校验：真实姓名不能为空
    private String realName; // 用户真实姓名（区别于用户名，用于界面展示）

    // ================================================================

    /*
     * @NotBlank(message = "角色不能为空")
     * private String role;
     *
     *   分解解释:
     *
     *   @NotBlank(message = "角色不能为空")
     *     角色不能为空。注册时必须指定用户角色。
     *
     *   private String role;
     *     role —— 用户角色，决定用户能做什么操作（权限控制的基础）
     *            Java 类型是 String，但在数据库中存储时，Spring Security 会自动加上 ROLE_ 前缀
     *            例如 role="ADMIN" → Spring Security 内部识别为 ROLE_ADMIN
     *
     *   用途:       用户角色，控制系统权限
     *   数据来源:   前端注册表单的"角色"选择框（下拉）
     *   可选值:     "ADMIN"（管理员）、"TEACHER"（教师）、"STUDENT"（学生）
     *   后端使用:   存入数据库 user 表的 role 列
     *              Spring Security 根据 role 值判断用户权限
     *              例如：@PreAuthorize("hasRole('ADMIN')") → 只有 role="ADMIN" 的用户才能访问
     *
     *   角色权限对照（本项目）:
     *     ADMIN   —— 管理员：  增删改查所有数据，管理用户
     *     TEACHER —— 教师：    查看学生、课程、成绩，录入成绩
     *     STUDENT —— 学生：    查看课程，选课，查看自己的成绩
     *
     *   为什么用 String 而不是 Enum（枚举）？
     *     用 String 更灵活，数据库也不依赖 Java 枚举定义顺序。
     *     但如果角色值固定，用 Enum 更安全（编译期检查，不会拼错）。
     *     两种方式各有利弊，本项目选择 String 以保持简洁。
     *
     *   JS 类比:
     *     // 前端角色选择
     *     <el-select v-model="registerForm.role">
     *       <el-option label="学生" value="STUDENT" />
     *       <el-option label="教师" value="TEACHER" />
     *     </el-select>
     *     // ADMIN 角色通常不开放给用户自己注册（由管理员手动指定）
     */
    @NotBlank(message = "角色不能为空") // 校验：角色不能为空
    private String role; // 用户角色：ADMIN（管理员）/ TEACHER（教师）/ STUDENT（学生）
}
