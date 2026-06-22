package com.example.backend.common; // 声明基础公共类包

/**
 * 统一响应状态码枚举
 * 定义了 API 接口返回的标准状态码和对应的提示信息
 */
public enum ResultCode { // 枚举类型，固定数量的常量集合

    SUCCESS(200, "操作成功"),
    ERROR(500, "服务器错误"),
    UNAUTHORIZED(401, "未登录"),
    FORBIDDEN(403, "权限不足"),
    PARAM_ERROR(400, "参数错误"),
    BUSINESS_ERROR(4000, "业务错误"); // 自定义业务异常码，避免与 HTTP 标准码冲突

    private final Integer code;  // 状态码
    private final String message; // 状态码对应的默认描述信息

    // 枚举构造方法（默认 private），初始化 code 和 message
    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    // 获取状态码
    public Integer getCode() {
        return code;
    }

    // 获取描述信息
    public String getMessage() {
        return message;
    }
}
