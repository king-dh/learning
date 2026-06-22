/*
 * ================================================================
 * 文件位置:  src/main/java/com/example/backend/common/ResultCode.java
 * 所在层:    Common 层（基础公共层）
 *
 * 职责说明:
 *   定义整个项目中所有 API 接口返回的标准状态码。
 *   类似 JavaScript 项目中的 constants/errorCodes.js 文件，所有错误码集中管理。
 *
 * 为什么要有这个枚举？
 *   如果每个 Controller 自己写 code: 200 / code: 500，数字散落各处不好维护。
 *   用一个枚举统一管理，改一处全项目生效。就像 CSS 变量统一管理颜色值。
 *
 * 类比 JavaScript：
 *   // errorCodes.js
 *   export const ERROR_CODES = {
 *     SUCCESS: { code: 200, message: '操作成功' },
 *     ERROR: { code: 500, message: '服务器错误' },
 *     UNAUTHORIZED: { code: 401, message: '未登录' },
 *     // ...
 *   }
 * ================================================================
 */

package com.example.backend.common;

/*
 * ==================== 什么是 Java 枚举 (enum)？ ====================
 *
 * enum 是 Java 的一种特殊类，用来定义一组固定的常量。
 *
 * 枚举和普通类的区别：
 *   - 普通 class：可以创建无数个实例（new Student(), new Student()...）
 *   - 枚举 enum：只有这里定义的几个实例，不能外部 new，全局唯一
 *
 * 枚举的好处：
 *   1. 类型安全：用 ResultCode.SUCCESS 比用数字 200 更不容易出错
 *   2. 自带 name() 方法：如 ResultCode.SUCCESS.name() 返回 "SUCCESS"
 *   3. 可以带属性：如下面的 code 和 message 字段
 *
 * 类比 JavaScript：
 *   enum 类似于 TypeScript 的 enum 关键字：
 *     enum ResultCode {
 *       SUCCESS = "SUCCESS",
 *       ERROR = "ERROR",
 *       // ...
 *     }
 *   Java 的 enum 比 TS 更强大：每个枚举值可以携带额外属性。
 */
public enum ResultCode { // 枚举类型：固定数量的常量集合

    // ==================== 枚举常量定义 ====================

    /*
     * SUCCESS(200, "操作成功")
     *
     *   语法解释：
     *     SUCCESS       —— 枚举常量名（习惯全大写，下划线分隔）
     *     (200, "操作成功") —— 调用了枚举的构造函数，传入 code 和 message
     *
     *   使用示例：
     *     ResultCode.SUCCESS.getCode()     → 200
     *     ResultCode.SUCCESS.getMessage()  → "操作成功"
     *
     *   为什么是 200？
     *     HTTP 协议规定 200 表示"OK"，所有成功的 API 响应都用这个码。
     */
    SUCCESS(200, "操作成功"),

    /*
     * ERROR(500, "服务器错误")
     *   HTTP 500 = Internal Server Error（服务器内部错误）
     *   一般是代码 bug 或数据库连接失败等"意料之外的错误"。
     *   前端收到 500 后通常提示"系统繁忙，请稍后再试"。
     */
    ERROR(500, "服务器错误"),

    /*
     * UNAUTHORIZED(401, "未登录")
     *   HTTP 401 = Unauthorized
     *   含义：需要登录但没有 Token / Token 过期 / Token 无效
     *   前端收到 401 后通常跳转到登录页。
     *
     *   注意：401 和 403 的区别
     *     401 = "我不知道你是谁，请先登录"
     *     403 = "我知道你是谁，但你没这个权限"
     */
    UNAUTHORIZED(401, "未登录"),

    /*
     * FORBIDDEN(403, "权限不足")
     *   HTTP 403 = Forbidden
     *   含义：已登录但角色不够（如学生想管理教师）
     *   前端收到 403 后通常提示"没有权限访问"。
     */
    FORBIDDEN(403, "权限不足"),

    /*
     * PARAM_ERROR(400, "参数错误")
     *   HTTP 400 = Bad Request
     *   含义：请求参数格式不对、校验不通过（如分数填了 -10）
     */
    PARAM_ERROR(400, "参数错误"),

    /*
     * BUSINESS_ERROR(4000, "业务错误")
     *
     *   自定义状态码，不是标准 HTTP 状态码。
     *   为什么不用 400？
     *     因为 400 已经被"参数校验失败"占用了。
     *     业务错误（如"用户名已存在"、"课程已选过"）需要一个单独的码。
     *
     *   4000 是自定义的，HTTP 标准中没有这个码。
     *   "4" 开头表示"客户端错误"，"000"是自定义子码。
     *   你可以自己细分：4001=用户名重复, 4002=密码错误, ...
     */
    BUSINESS_ERROR(4000, "业务错误");

    // ==================== 枚举的属性字段 ====================

    /*
     * private final Integer code;
     *
     *   每个枚举常量都携带一个"状态码"。
     *   private：外部不能直接访问（通过 getter 访问）
     *   final：不可修改，确保枚举常量的值一旦定义就不会变
     */
    private final Integer code;

    /*
     * private final String message;
     *
     *   每个枚举常量都携带一个"默认提示信息"。
     *   比如 SUCCESS 的 message 是 "操作成功"，用作 API 返回的默认描述。
     */
    private final String message;

    // ==================== 枚举的构造函数 ====================

    /*
     * ResultCode(Integer code, String message) { ... }
     *
     *   枚举的构造函数：
     *     - 默认就是 private 的（不能手动 new）
     *     - 每个常量定义时调用（如 SUCCESS(200, "操作成功") 就是调这个构造函数）
     *
     *   执行时机：
     *     JVM 加载这个类时，按顺序创建每个枚举常量。
     *     比如先创建 SUCCESS → 调用构造函数 → this.code=200, this.message="操作成功"。
     *
     *   注意：枚举构造函数每定义一个常量就调用一次，所以 SUCCESS/ERROR/UNAUTHORIZED 等
     *   各有一个独立的对象实例（类似单例模式，全局唯一）。
     */
    ResultCode(Integer code, String message) { // 枚举构造函数（默认 private）
        this.code = code;       // 把参数 code 赋给字段 this.code
        this.message = message; // 把参数 message 赋给字段 this.message
    }

    // ==================== Getter 方法 ====================

    /*
     * public Integer getCode() { return code; }
     *
     *   public：外部可以调用这个方法获取状态码
     *   Integer：返回类型
     *   getCode：方法名（getter 命名惯例：get + 属性名首字母大写）
     *
     *   使用场景：
     *     ResultCode.SUCCESS.getCode()          → 返回 200
     *     ResultCode.BUSINESS_ERROR.getCode()   → 返回 4000
     */
    public Integer getCode() {
        return code;
    }

    /*
     * public String getMessage() { return message; }
     *
     *   和上面类似，获取默认提示信息。
     *
     *   使用场景：
     *     ResultCode.SUCCESS.getMessage()  → "操作成功"
     *     ResultCode.ERROR.getMessage()    → "服务器错误"
     */
    public String getMessage() {
        return message;
    }

    /*
     * ================================================================
     * 为什么没有 setter？
     *
     *   因为 code 和 message 都是 final 的，不需要也不能修改。
     *   枚举常量一旦创建，其属性值就固定了。
     *   这就是"不可变对象"原则，确保全局状态码的一致性。
     *
     *   类比 JavaScript：
     *     Object.freeze({ code: 200, message: '操作成功' })
     *     // 冻结后不能修改任何属性
     * ================================================================
     */
}
