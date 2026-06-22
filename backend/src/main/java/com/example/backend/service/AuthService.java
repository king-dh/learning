/*
 * ================================================================
 * 文件位置:  src/main/java/com/example/backend/service/AuthService.java
 * 架构层级:  Service 接口（业务逻辑层的"合同/规范"）
 * 实现类:    src/main/java/com/example/backend/service/impl/AuthServiceImpl.java
 * 被调用者:  src/main/java/com/example/backend/controller/AuthController.java
 *
 * 调用链路（以"用户登录"为例）:
 * 前端登录页提交表单 → axios 发 POST /api/auth/login
 *                   → AuthController.login()
 *                   → AuthService.login()（本接口）
 *                   → AuthServiceImpl.login()（实现类，真正的业务逻辑）
 *                   → MyBatis 查 sys_user 表
 *                   → 验证密码 → 生成 JWT → 返回 Token 给前端
 *
 * 为什么需要接口（Interface）？
 * ┌────────────────────────────────────────────────┐
 * │  面向接口编程（Program to Interface）          │
 * │                                                │
 * │  Controller 层只依赖 AuthService 接口          │
 * │  不依赖 AuthServiceImpl 具体实现               │
 * │                                                │
 * │  好处：                                        │
 * │  1. 哪天想换实现（比如从 BCrypt 换 SHA256），  │
 * │     只需新建 AuthServiceNewImpl 实现同一接口    │
 * │     Controller 代码一行不改                    │
 * │  2. 写单元测试时可以用 Mock 实现代替真实实现    │
 * │  3. Spring 自动找到接口唯一的实现类并注入       │
 * │                                                │
 * │  JS 类比：                                     │
 * │    // Java 接口                                │
 * │    interface AuthService { login(dto) }         │
 * │    class AuthServiceImpl implements AuthService │
 * │                                                │
 * │    // JS/TS 等价写法                           │
 * │    interface IAuthService { login(dto) }        │
 * │    class AuthServiceImpl implements IAuthService│
 * │    // 或者甚至更简单的：                        │
 * │    // 在 JS 中没有 interface 关键字时，         │
 * │    // 接口就是一种"约定"：约定 AuthService     │
 * │    // 必须有 login() 和 register() 两个方法     │
 * └────────────────────────────────────────────────┘
 *
 * interface vs class 的区别：
 * ┌──────────────────┬──────────────────────┬──────────────────────────┐
 * │    特性          │   interface（接口）   │   class（类）            │
 * ├──────────────────┼──────────────────────┼──────────────────────────┤
 * │ 能写方法体吗？   │ 不能（只有方法签名）  │ 能（有具体实现代码）     │
 * │ 能 new 实例吗？  │ 不能                  │ 能（除非 abstract）      │
 * │ 能定义字段吗？   │ 只能定义常量           │ 能定义各种字段           │
 * │ 被谁使用？       │ 被 implements          │ 被 extends 继承          │
 * │ 类比 JS         │ TS 的 interface        │ TS 的 class              │
 * │ 职责            │ "定义能做什么" 的规范   │ "具体怎么做" 的实现      │
 * └──────────────────┴──────────────────────┴──────────────────────────┘
 * ================================================================
 */

// ==================== 1. 包声明 ====================

/*
 * package 关键字：声明当前文件属于哪个"包"（相当于 JS 的命名空间/文件夹）。
 * 包名 com.example.backend.service 和文件夹路径
 *   com/example/backend/service/ 必须完全一致。
 * "service" 子包专门存放服务层的接口定义。
 */
package com.example.backend.service; // 声明当前文件属于 service 子包（服务层接口）

// ==================== 2. 导入其他类（import） ====================

/*
 * import 关键字：告诉编译器"我要用别的文件里定义的类"。
 * 和 JS 的 import { xxx } from './xxx' 是一样的概念。
 * 区别：Java import 必须写完整包路径，不是相对路径。
 */

// --- 2.1 本项目内部的 DTO（接收前端数据的对象）---
// DTO = Data Transfer Object，专门用来接收前端请求参数的"壳"
import com.example.backend.dto.LoginDTO;    // 登录请求 DTO：{ username, password }
import com.example.backend.dto.RegisterDTO; // 注册请求 DTO：{ username, password, realName, role }

// --- 2.2 本项目内部的 VO（返回给前端的数据对象）---
// VO = View Object，专门用来组装返回给前端的数据
import com.example.backend.vo.LoginVO;     // 登录响应 VO：{ token, username, realName, role }

// ==================== 3. 接口声明 ====================

/*
 * public interface AuthService { ... }
 *
 *   public          —— 公开的，任何地方的代码都能使用这个接口
 *   interface       —— 接口关键字（不是 class！），表示这里只定义方法签名，不写具体实现
 *   AuthService     —— 接口名，语义化的命名：Auth（认证）+ Service（服务）
 *   { ... }         —— 接口体，里面只包含方法的"签名"（名称+参数+返回值），没有方法体 { }
 *
 *   JS 类比：
 *     // TS 中：
 *     export interface IAuthService {
 *       login(loginDTO: LoginDTO): LoginVO;
 *       register(registerDTO: RegisterDTO): void;
 *     }
 */
public interface AuthService { // 认证服务接口：定义登录和注册两个业务的规范

    // ==================== 4. 方法签名 ====================

    /*
     * ================================================================
     * 方法 1：用户登录
     *
     * LoginVO login(LoginDTO loginDTO);
     *
     *   拆解解释：
     *     LoginVO   —— 返回值类型：登录成功后返回一个包含 Token 的 LoginVO 对象
     *                 类比 JS：function login(dto): Promise<LoginVO>
     *     login     —— 方法名：见名知意，就是执行登录操作
     *     LoginDTO  —— 参数类型：接收前端传来的登录请求数据
     *     loginDTO  —— 参数名：驼峰命名
     *
     *   注意：接口里只有方法签名，没有 { 方法体 }
     *   具体怎么查数据库、怎么验证密码，写在 AuthServiceImpl 里
     *
     *   数据流转：
     *     前端 → JSON { username: "admin", password: "123456" }
     *          → Spring 自动转成 LoginDTO 对象
     *          → 传给本方法
     *          → 实现类 AuthServiceImpl 处理：
     *              查数据库 → 验证密码 → 生成 JWT → 返回 LoginVO { token: "eyJ...", ... }
     *          → Controller 包一层 Result → 前端收到 JSON
     *
     *   JS 类比：
     *     async function login(loginDTO) {
     *       // 查用户、验证密码、生成 JWT...
     *       return { token, username, realName, role }
     *     }
     */
    LoginVO login(LoginDTO loginDTO);

    /*
     * ================================================================
     * 方法 2：用户注册
     *
     * void register(RegisterDTO registerDTO);
     *
     *   拆解解释：
     *     void     —— 返回值类型：void 就是"没有返回值"
     *                 Java 中 void 表示这个方法执行完后不返回任何数据
     *                 类比 JS 中不写 return 的函数
     *     register —— 方法名：执行注册操作
     *     RegisterDTO —— 参数类型：包含用户名、密码、真实姓名、角色等信息
     *
     *   数据流转：
     *     前端 → JSON { username: "new_user", password: "123", realName: "新用户", role: "STUDENT" }
     *          → Spring 转成 RegisterDTO
     *          → 实现类 AuthServiceImpl 处理：
     *              检查用户名唯一 → 加密密码 → 保存到 sys_user 表
     *          → 不返回数据（void），只靠是否异常判断成败
     *
     *   JS 类比：
     *     async function register(registerDTO) {
     *       // 检查用户名、加密密码、保存...
     *       // 不 return 任何值
     *     }
     */
    void register(RegisterDTO registerDTO);
}
