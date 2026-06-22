package com.example.backend.common; // 声明基础公共类包

import lombok.Data; // Lombok 注解：自动生成 Getter/Setter/toString/equals/hashCode

/**
 * 统一 API 响应结果包装类
 * 所有 Controller 接口的返回值都用此类包裹，保持前端接收格式一致
 *
 * @param <T> 泛型参数，表示 data 字段的实际数据类型
 */
@Data // Lombok 注解：自动生成所有字段的 getter 和 setter 方法
public class Result<T> { // 泛型类，T 可以是任意 Java 类型

    private Integer code;  // 状态码，对应 ResultCode 枚举中的值
    private String message; // 提示信息，描述本次请求的结果
    private T data;         // 响应数据，泛型类型，可以是对象、列表、null 等

    /**
     * 返回成功结果（包含数据）
     * @param data 返回的业务数据
     * @param <T>  数据类型
     * @return Result 对象
     */
    public static <T> Result<T> ok(T data) { // 静态泛型方法，<T> 声明方法级泛型
        Result<T> result = new Result<>();     // 创建 Result 实例
        result.setCode(ResultCode.SUCCESS.getCode());     // 设置状态码 200
        result.setMessage(ResultCode.SUCCESS.getMessage()); // 设置提示信息 "操作成功"
        result.setData(data);                 // 设置业务数据
        return result;                        // 返回封装好的结果
    }

    /**
     * 返回失败结果（根据 ResultCode 枚举）
     * @param code ResultCode 枚举值
     * @param <T>  数据类型
     * @return Result 对象（data 为 null）
     */
    public static <T> Result<T> fail(ResultCode code) {
        Result<T> result = new Result<>();
        result.setCode(code.getCode());       // 设置状态码
        result.setMessage(code.getMessage()); // 设置提示信息
        result.setData(null);                 // 失败时通常不返回数据
        return result;
    }

    /**
     * 返回失败结果（自定义状态码和消息）
     * @param code    自定义状态码
     * @param message 自定义错误消息
     * @param <T>     数据类型
     * @return Result 对象（data 为 null）
     */
    public static <T> Result<T> fail(Integer code, String message) {
        Result<T> result = new Result<>();
        result.setCode(code);      // 设置自定义状态码
        result.setMessage(message); // 设置自定义消息
        result.setData(null);
        return result;
    }
}
