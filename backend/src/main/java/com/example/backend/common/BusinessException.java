package com.example.backend.common; // 声明基础公共类包

import lombok.Getter; // Lombok 注解：只生成 getter 方法

/**
 * 自定义业务异常类
 * 当业务逻辑出现错误时（如用户名重复、数据不存在等），抛出此异常
 * 会被 GlobalExceptionHandler 统一捕获处理
 */
@Getter // 自动生成 code 和 message 的 getter 方法（但不生成 setter）
public class BusinessException extends RuntimeException { // 继承 RuntimeException，属于非受检异常

    private final Integer code;     // 业务错误码，对应 ResultCode 枚举
    private final String message;   // 错误描述信息

    /**
     * 使用 ResultCode 枚举构造异常
     * @param code ResultCode 枚举值
     */
    public BusinessException(ResultCode code) {
        super(code.getMessage()); // 调用父类 RuntimeException 构造，设置异常消息
        this.code = code.getCode();
        this.message = code.getMessage();
    }

    /**
     * 使用自定义状态码和消息构造异常
     * @param code    自定义状态码
     * @param message 自定义错误消息
     */
    public BusinessException(Integer code, String message) {
        super(message); // 调用父类构造
        this.code = code;
        this.message = message;
    }
}
