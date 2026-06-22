/*
 * ================================================================
 * 文件位置:  src/main/java/com/example/backend/common/Result.java
 * 所在层:    Common 层（基础公共层）
 *
 * 职责说明:
 *   统一 API 响应格式的包装类。所有 Controller 的返回值都通过它包裹，
 *   确保前端收到的 JSON 格式始终一致：{ code, message, data }。
 *
 *   类比 JavaScript：
 *     // 前端 axios 响应拦截器中常见的统一格式
 *     function buildResponse(code, message, data) {
 *       return { code, message, data }
 *     }
 *     // 本项目用 Java 的泛型类来做到同样的效果
 *
 * 为什么需要这个类？
 *   如果没有统一格式，各接口可能返回不同的 JSON 结构：
 *     有的返回 { success: true, result: [...] }
 *     有的返回 { status: 200, msg: "ok", payload: {...} }
 *   前端需要针对每种格式写不同的处理逻辑，非常混乱。
 *   统一之后，前端只需要了解 { code, message, data } 这一种格式。
 * ================================================================
 */

package com.example.backend.common;

/*
 * Lombok 的 @Data 注解：
 *   自动生成所有字段的 getter 和 setter 方法（等价于手写 getCode/setCode/getMessage/setMessage...）
 *   还自动生成 toString()、equals()、hashCode() 方法。
 *
 *   类比 JavaScript：
 *     相当于自动给你写了：
 *       get code() { return this.#code }
 *       set code(v) { this.#code = v }
 *     而不需要手写。
 */
import lombok.Data;

/*
 * ==================== 什么是 Java 泛型 (Generics)？ ====================
 *
 * public class Result<T> 中的 <T> 就是泛型参数。
 * T 是一个"类型占位符"，在创建对象时才确定具体类型。
 *
 * 例如：
 *   Result<StudentVO>         → data 字段的类型是 StudentVO
 *   Result<List<CourseVO>>    → data 字段的类型是 List<CourseVO>
 *   Result<Void>              → data 字段的类型是 Void（表示无数据）
 *
 * 泛型的好处：
 *   1. 类型安全：编译器知道 data 的类型，不会写错
 *   2. 不用强制转换：Result<StudentVO>.getData() 直接返回 StudentVO，不用 (StudentVO) 强转
 *
 * 类比 TypeScript：
 *   interface Result<T> {
 *     code: number
 *     message: string
 *     data: T  // ← 泛型
 *   }
 *   const result: Result<StudentVO> = { code: 200, data: student }
 */
@Data // Lombok：自动生成 getter/setter/toString/equals/hashCode
public class Result<T> { // <T> = 类型参数，可以是任意 Java 类型

    // ==================== 字段定义 ====================

    /*
     * private Integer code;
     *
     *   状态码字段。
     *     - 200："操作成功"
     *     - 400："参数错误"
     *     - 401："未登录"
     *     - 403："权限不足"
     *     - 500："服务器错误"
     *     - 4000：自定义业务错误
     *
     *   用 Integer 而不是 int：因为 Integer 可以是 null（虽然这里不会为 null，但习惯用包装类型）
     */
    private Integer code;

    /*
     * private String message;
     *
     *   提示信息字段。
     *   成功时："操作成功"
     *   失败时：具体的错误描述，如"用户名已存在"、"分数不能超过100"
     *
     *   @Data 会自动生成 getMessage() 和 setMessage() 方法
     */
    private String message;

    /*
     * private T data;
     *
     *   泛型字段：存储实际的业务数据。
     *   T 在创建 Result 对象时具体化：
     *     new Result<StudentVO>()  → data 是 StudentVO 类型
     *     new Result<List<CourseVO>>() → data 是 List<CourseVO> 类型
     *     new Result<Void>()  → data 是 Void 类型（空，表示没有业务数据）
     *
     *   类比 JavaScript：data 可以是任何值（对象/数组/null）
     */
    private T data;

    // ==================== 静态工厂方法 ====================

    /*
     * ========== ok() - 成功响应 ==========
     *
     * Result.ok(data) 是项目中最常用的方法：
     *   1. 创建一个新的 Result 对象
     *   2. 设置 code = 200（来自 ResultCode.SUCCESS）
     *   3. 设置 message = "操作成功"
     *   4. 设置 data = 传入的业务数据
     *   5. 返回完整的 Result 对象
     */

    /**
     * 返回成功结果（包含数据）
     *
     * @param data 返回的业务数据，可以是对象、列表、null
     * @param <T>  数据类型（自动推断）
     * @return Result 对象 { code: 200, message: "操作成功", data: ... }
     */
    /*
     * public static <T> Result<T> ok(T data) { ... }
     *
     *   方法签名逐词解释：
     *     public        —— 公开方法
     *     static        —— 静态方法：不依赖实例，直接通过类名调用 Result.ok(data)
     *     <T>           —— 声明方法级别的泛型参数 T（和类名后面的 <T> 不冲突，这里的方法泛型）
     *     Result<T>     —— 返回值类型：一个包含泛型 T 的 Result 对象
     *     ok            —— 方法名
     *     (T data)      —— 参数：类型为 T 的业务数据
     *
     *   泛型推断机制：
     *     调用 Result.ok(studentVO) 时，编译器自动推断 T = StudentVO
     *     返回值就是 Result<StudentVO> 类型
     *
     *   类比 TypeScript：
     *     function ok<T>(data: T): Result<T> {
     *       return { code: 200, message: '操作成功', data }
     *     }
     */
    public static <T> Result<T> ok(T data) {
        Result<T> result = new Result<>();               // 创建空的 Result 实例
        result.setCode(ResultCode.SUCCESS.getCode());     // 从枚举取状态码 200
        result.setMessage(ResultCode.SUCCESS.getMessage()); // 从枚举取消息 "操作成功"
        result.setData(data);                            // 设置业务数据（可以是 null）
        return result;                                   // 返回包装好的 Result
    }

    /*
     * ========== fail(ResultCode) - 失败响应（通过枚举） ==========
     *
     * 当你有一个标准的错误类型时使用此方法：
     *   Result.fail(ResultCode.UNAUTHORIZED)   → 返回 { code: 401, message: "未登录" }
     *   Result.fail(ResultCode.FORBIDDEN)       → 返回 { code: 403, message: "权限不足" }
     *
     * 通常在 GlobalExceptionHandler 中使用：根据异常类型选择对应的 ResultCode。
     */
    public static <T> Result<T> fail(ResultCode code) {
        Result<T> result = new Result<>();
        result.setCode(code.getCode());       // 从枚举取值
        result.setMessage(code.getMessage()); // 从枚举取消息
        result.setData(null);                 // 失败不返回业务数据
        return result;
    }

    /*
     * ========== fail(Integer, String) - 失败响应（自定义码和消息） ==========
     *
     * 当你需要自定义错误码和消息时使用：
     *   Result.fail(4001, "用户名已存在")
     *   Result.fail(4002, "密码不能少于6位")
     *
     * 通常在 BusinessException 被捕获后使用，因为 BusinessException 自带 code 和 message。
     */
    public static <T> Result<T> fail(Integer code, String message) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        result.setData(null);
        return result;
    }

    /*
     * ================================================================
     * 为什么用静态工厂方法而不是直接 new？
     *
     *   如果直接 new，每次要写四行代码：
     *     Result<StudentVO> r = new Result<>();
     *     r.setCode(200);
     *     r.setMessage("操作成功");
     *     r.setData(data);
     *
     *   用工厂方法，一行搞定：
     *     Result.ok(data)
     *
     *   类比 JavaScript：
     *     // 不好：每次手动构造
     *     { code: 200, message: '成功', data: data }
     *     // 好：用工厂函数
     *     function ok(data) { return { code: 200, message: '成功', data } }
     *     ok(data)  // 一行搞定
     * ================================================================
     */
}
