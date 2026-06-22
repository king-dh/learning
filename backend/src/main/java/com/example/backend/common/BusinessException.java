/*
 * ================================================================
 * 文件位置:  src/main/java/com/example/backend/common/BusinessException.java
 * 所在层:    Common 层（基础公共层）
 *
 * 职责说明:
 *   自定义业务异常类。当业务逻辑出问题时（用户名重复、数据不存在等），
 *   抛出此异常 → GlobalExceptionHandler 统一捕获 → 返回标准 JSON 错误。
 *
 *   类比 JavaScript：
 *     // 自定义错误类
 *     class BusinessError extends Error {
 *       constructor(code, message) {
 *         super(message)
 *         this.code = code
 *         this.message = message
 *       }
 *     }
 *     throw new BusinessError(4001, '用户名已存在')
 *
 * 完整的异常处理链路：
 *   Service 发现用户名已存在
 *     → throw new BusinessException(ResultCode.BUSINESS_ERROR)
 *     → 方法栈逐层向上传播（Controller 不处理，继续往上抛）
 *     → Spring 的 DispatcherServlet 捕获到异常
 *     → 转发给 GlobalExceptionHandler.handleBusinessException()
 *     → 返回 { code: 4000, message: "业务错误" } 给前端
 * ================================================================
 */

package com.example.backend.common;

/*
 * Lombok 的 @Getter 注解：
 *   只生成 getter 方法，不生成 setter。
 *
 *   为什么只生成 getter？
 *     异常对象的属性应该在构造时就确定，不需要也不应该被修改。
 *     code 和 message 都是 final 的，用 @Getter 自动生成 getCode() 和 getMessage()。
 *
 *   对比 @Data：
 *     @Data 会生成 getter + setter + toString + equals + hashCode
 *     @Getter 只生成 getter，更精确，避免"不该有 setter 的字段有了 setter"
 */
import lombok.Getter;

/**
 * 自定义业务异常类
 *
 * 继承 RuntimeException（非受检异常）：
 *   Java 的异常分两种：
 *     1. Checked Exception（受检异常）
 *        - 必须用 try/catch 或 throws 声明处理
 *        - 例如：IOException, SQLException
 *        - 强制调用者处理，代码冗余多
 *     2. Unchecked Exception（非受检异常）
 *        - 继承 RuntimeException
 *        - 不需要显式 throws 声明
 *        - 例如：NullPointerException, IllegalArgumentException
 *        - 本项目的 BusinessException 选这种，因为由全局异常处理器统一处理
 *
 *   类比 JavaScript：
 *     JavaScript 没有 checked exception 概念，所有异常都不强制处理。
 *     RuntimeException 就是 JavaScript 中 throw new Error() 的效果。
 */
@Getter // 自动生成 getCode() 和 getMessage() 方法
public class BusinessException extends RuntimeException { // 继承非受检异常

    /*
     * private final Integer code;
     *
     *   业务错误码。
     *   final：异常抛出后不可修改（不可变性原则）
     *   通常使用 ResultCode 枚举中的值，如 4000（业务错误）
     *   也可以自定义，如 4001（用户名重复）、4002（课程已选满）
     */
    private final Integer code;

    /*
     * private final String message;
     *
     *   错误描述信息。
     *   这个字段会覆盖父类 RuntimeException 的 message 字段。
     *   不过父类也有一个 message，在构造时通过 super(message) 同步。
     */
    private final String message;

    // ==================== 构造函数 1：从枚举创建 ====================

    /**
     * 使用 ResultCode 枚举构造异常（最常用方式）
     *
     * 调用示例：
     *   throw new BusinessException(ResultCode.BUSINESS_ERROR);
     *
     * @param code ResultCode 枚举值（如 BUSINESS_ERROR）
     */
    /*
     * public BusinessException(ResultCode code) { ... }
     *
     *   这个构造函数的意图：
     *     直接用枚举值创建异常，code 和 message 都从枚举中取。
     *
     *   执行步骤：
     *     1. super(code.getMessage())
     *        调用父类 RuntimeException 的构造函数，传入错误消息
     *        这行很重要：如果不用 super 传消息，父类的 getMessage() 会返回 null
     *     2. this.code = code.getCode()
     *        设置业务错误码
     *     3. this.message = code.getMessage()
     *        设置错误消息（和父类保持一致）
     */
    public BusinessException(ResultCode code) {
        super(code.getMessage());    // 必须调父类构造函数，传入消息（RuntimeException 要求）
        this.code = code.getCode();  // 从枚举取值
        this.message = code.getMessage(); // 从枚举取值
    }

    // ==================== 构造函数 2：自定义码和消息 ====================

    /**
     * 使用自定义状态码和消息构造异常（灵活场景）
     *
     * 调用示例：
     *   throw new BusinessException(4001, "用户名已存在");
     *
     * @param code    自定义状态码（建议 4xxx 格式）
     * @param message 自定义错误消息
     */
    /*
     * 这个构造函数在需要更精确的错误描述时使用。
     * 比如"用户名已存在"（4001）和"课程已选过"（4002）都是业务错误，
     * 但需要不同的 code 和 message 来区分。
     *
     * 类比 JavaScript：
     *   throw new BusinessError(4001, '用户名已存在')
     */
    public BusinessException(Integer code, String message) {
        super(message);    // 把自定义消息传给父类
        this.code = code;  // 自定义错误码
        this.message = message; // 自定义错误消息
    }

    /*
     * ================================================================
     * 总结：为什么要有 BusinessException？
     *
     * 如果没有自定义异常：
     *   if (用户名已存在) {
     *     return Result.fail(4001, "用户名已存在")  ← 需要在 Service 层直接返回 Result
     *   }
     *   问题：Service 层不应该关心"返回什么 JSON 给前端"，那是 Controller 的事。
     *
     * 有了 BusinessException：
     *   if (用户名已存在) {
     *     throw new BusinessException(4001, "用户名已存在")  ← 只抛异常，不管怎么响应
     *   }
     *   异常向上传播 → GlobalExceptionHandler 统一捕获 → 转成 Result JSON
     *
     *   好处：
     *     - Service 层只负责业务判断（抛异常）
     *     - Controller 层不用写 try/catch
     *     - 错误响应格式由 GlobalExceptionHandler 统一处理
     *     - 关注点分离（Separation of Concerns）
     * ================================================================
     */
}
