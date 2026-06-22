package com.example.backend.handler; // 声明全局异常处理包

import com.example.backend.common.BusinessException; // 导入自定义业务异常
import com.example.backend.common.Result; // 导入统一响应结果
import com.example.backend.common.ResultCode; // 导入状态码枚举
import lombok.extern.slf4j.Slf4j; // Lombok 日志注解
import org.springframework.http.HttpStatus; // HTTP 状态码
import org.springframework.security.access.AccessDeniedException; // 权限不足异常（URL级别拦截）
import org.springframework.security.authorization.AuthorizationDeniedException; // 方法级权限异常（@PreAuthorize）
import org.springframework.web.bind.MethodArgumentNotValidException; // 参数校验失败异常
import org.springframework.web.bind.annotation.ExceptionHandler; // 异常处理注解
import org.springframework.web.bind.annotation.ResponseStatus; // 响应HTTP状态码注解
import org.springframework.web.bind.annotation.RestControllerAdvice; // 全局异常处理注解

/**
 * 全局异常处理器
 * 统一捕获整个应用中抛出的各类异常，返回标准 JSON 格式的错误信息
 * @RestControllerAdvice = @ControllerAdvice + @ResponseBody（返回值自动序列化为 JSON）
 */
@Slf4j // 自动生成 log 日志对象
@RestControllerAdvice // 声明这是一个全局异常处理类，对所有 Controller 生效
public class GlobalExceptionHandler {

    /**
     * 处理 Spring Security 方法级权限异常 AuthorizationDeniedException
     * 当 @PreAuthorize/@PostAuthorize 注解校验失败时抛出此异常（Spring Security 6.x）
     */
    @ResponseStatus(HttpStatus.FORBIDDEN) // 返回 HTTP 403 状态码
    @ExceptionHandler(AuthorizationDeniedException.class) // 捕获方法级权限异常
    public Result<Void> handleAuthorizationDeniedException(AuthorizationDeniedException e) {
        log.warn("方法级权限不足：{}", e.getMessage()); // 记录警告日志
        return Result.fail(ResultCode.FORBIDDEN); // 返回 403 权限不足
    }

    /**
     * 处理 URL 级别权限不足异常 AccessDeniedException
     * 当 URL 拦截规则校验失败时抛出此异常
     */
    @ResponseStatus(HttpStatus.FORBIDDEN) // 返回 HTTP 403 状态码
    @ExceptionHandler(AccessDeniedException.class) // 指定捕获权限不足异常
    public Result<Void> handleAccessDeniedException(AccessDeniedException e) { // 参数 e 是异常对象
        log.warn("URL级权限不足：{}", e.getMessage()); // 记录警告日志
        return Result.fail(ResultCode.FORBIDDEN); // 返回 403 权限不足
    }

    /**
     * 处理自定义业务异常 BusinessException
     * 如：用户名已存在、数据不存在等业务逻辑错误
     */
    @ExceptionHandler(BusinessException.class) // 指定捕获的异常类型
    public Result<Void> handleBusinessException(BusinessException e) { // 参数 e 是捕获到的异常对象
        log.warn("业务异常：code={}, message={}", e.getCode(), e.getMessage()); // 记录警告日志
        return Result.fail(e.getCode(), e.getMessage()); // 返回自定义的错误码和消息
    }

    /**
     * 处理参数校验失败异常 MethodArgumentNotValidException
     * 当 DTO 中使用 @NotBlank/@NotNull 等注解校验失败时抛出此异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class) // 指定捕获参数校验失败异常
    public Result<Void> handleValidationException(MethodArgumentNotValidException e) {
        // 从异常对象中获取字段校验错误，拼接成字符串
        StringBuilder errorMsg = new StringBuilder(); // StringBuilder 效率高于字符串拼接
        e.getBindingResult().getFieldErrors().forEach(error -> { // 遍历所有字段错误
            errorMsg.append(error.getField()) // 字段名，如 "username"
                    .append(": ")              // 分隔符
                    .append(error.getDefaultMessage()) // 校验失败消息，如 "不能为空"
                    .append("; ");             // 多个错误用分号分隔
        });
        String msg = errorMsg.toString(); // 转为字符串
        log.warn("参数校验失败：{}", msg); // 记录警告日志
        return Result.fail(ResultCode.PARAM_ERROR.getCode(), msg); // 返回 400 参数错误
    }

    /**
     * 处理所有未被上述处理器捕获的其他异常
     * 作为兜底异常处理，防止敏感信息泄露到前端
     * 同时检查异常链中是否包含权限相关异常，若包含则返回 403 而非 500
     */
    @ExceptionHandler(Exception.class) // 捕获所有 Exception 及其子类
    public Result<Void> handleException(Exception e) {
        // 检查异常链中是否包含 AccessDeniedException（处理 AOP 代理包装的情况）
        Throwable cause = e;
        while (cause != null) {
            if (cause instanceof AccessDeniedException) {
                log.warn("通过异常链检测到权限不足：{}", cause.getMessage());
                return Result.fail(ResultCode.FORBIDDEN); // 返回 403
            }
            cause = cause.getCause(); // 获取上层异常
        }
        log.error("服务器内部错误 [{}]: {}", e.getClass().getName(), e.getMessage(), e); // 记录错误日志
        return Result.fail(ResultCode.ERROR); // 返回 500 服务器错误
    }
}
