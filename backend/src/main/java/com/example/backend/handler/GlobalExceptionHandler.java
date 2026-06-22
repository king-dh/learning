/*
 * ================================================================
 * 文件位置:  src/main/java/com/example/backend/handler/GlobalExceptionHandler.java
 * 所在层:    Handler 层（全局异常处理层）
 *
 * 核心概念：AOP（面向切面编程）
 *   本类不修改任何 Controller 代码，但能"拦截"所有 Controller 抛出的异常。
 *   就像前端 axios 的响应拦截器：不管哪个接口出错了，统一到这里处理。
 *
 * 类比 JavaScript Express 的全局错误中间件：
 *   app.use((err, req, res, next) => {
 *     if (err instanceof BusinessError) {
 *       res.json({ code: err.code, message: err.message })
 *     } else {
 *       res.status(500).json({ code: 500, message: '服务器错误' })
 *     }
 *   })
 *
 * 异常处理的完整链路：
 *   Controller 调 Service → Service 抛异常 → 异常向上抛
 *     → Spring 的 DispatcherServlet 捕获
 *     → 查找匹配的 @ExceptionHandler 方法
 *     → 本类的对应方法执行 → 返回 Result JSON 给前端
 * ================================================================
 */

package com.example.backend.handler;

// --- 本项目内部类 ---
import com.example.backend.common.BusinessException; // 自定义业务异常
import com.example.backend.common.Result;             // 统一响应格式
import com.example.backend.common.ResultCode;          // 状态码枚举

// --- Lombok ---
import lombok.extern.slf4j.Slf4j; // @Slf4j：自动生成 log 对象，可直接用 log.info()/log.error()

// --- Spring 框架 ---
import org.springframework.http.HttpStatus; // HTTP 状态码常量（HttpStatus.FORBIDDEN = 403）

// --- Spring Security 异常类 ---
/*
 * AccessDeniedException vs AuthorizationDeniedException：
 *
 *   AccessDeniedException (旧版/URL级别)：
 *     当在 SecurityConfig 中配置的 URL 拦截规则触发时抛出。
 *     例如：SecurityConfig 中配置了 .anyRequest().authenticated()，
 *     未登录用户访问时抛出此异常。
 *
 *   AuthorizationDeniedException (新版/方法级别)：
 *     Spring Security 6.x 新增的异常类。
 *     当 @PreAuthorize 注解校验失败时抛出。
 *     例如：STUDENT 角色访问了 @PreAuthorize("hasRole('ADMIN')") 的方法。
 *
 *   两者都表示"权限不足"，但触发时机不同。
 *   本 Handler 同时处理两者，确保前端都能收到 403 响应。
 */
import org.springframework.security.access.AccessDeniedException;           // URL 级别权限异常
import org.springframework.security.authorization.AuthorizationDeniedException; // 方法级权限异常

// --- Spring Web / Validation ---
/*
 * MethodArgumentNotValidException：
 *   当 Controller 方法参数上使用了 @Valid 注解，且校验不通过时抛出。
 *   例如：@Valid @RequestBody ScoreDTO dto，ScoreDTO 里的 @Max(100) 不通过。
 *   异常对象中包含详细的字段校验失败信息。
 */
import org.springframework.web.bind.MethodArgumentNotValidException;

// --- Spring 异常处理注解 ---
/*
 * @RestControllerAdvice：
 *   = @ControllerAdvice + @ResponseBody
 *   跟 @RestController = @Controller + @ResponseBody 一样的概念。
 *   @ControllerAdvice 告诉 Spring："这个类的方法可以拦截所有 Controller 的异常"。
 *   @ResponseBody 确保返回值自动序列化为 JSON。
 */
import org.springframework.web.bind.annotation.ExceptionHandler; // @ExceptionHandler：标记方法是异常处理器
import org.springframework.web.bind.annotation.ResponseStatus;    // @ResponseStatus：设置 HTTP 响应状态码
import org.springframework.web.bind.annotation.RestControllerAdvice; // 全局异常处理声明

// ==================== 类声明 ====================

/*
 * @Slf4j：
 *   Lombok 注解，自动生成名为 "log" 的日志对象。
 *   之后可以直接用 log.info(...)、log.warn(...)、log.error(...) 打印日志。
 *
 *   类比 JavaScript：
 *     import logger from './logger'
 *     logger.warn('xxx')      // ← log.warn('xxx')
 *     logger.error('xxx', e)  // ← log.error('xxx', e)
 */
@Slf4j

/*
 * @RestControllerAdvice：
 *   核心注解，功能分三部分：
 *     1. @ControllerAdvice：增强所有 Controller，可拦截它们的异常
 *     2. @ResponseBody：返回值自动转 JSON（不用写 @ResponseBody 在每个方法上）
 *     3. 自动扫描：Spring Boot 默认扫描启动类所在包及其子包
 *
 *   类比 JavaScript Express：
 *     app.use((err, req, res, next) => { ... })  ← 全局错误中间件
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ==================== 1. 方法级权限异常（Spring Security 6.x） ====================

    /**
     * 处理 @PreAuthorize 注解校验失败
     *
     * 触发场景：
     *   STUDENT 用户访问了 @PreAuthorize("hasRole('ADMIN')") 的接口
     *   → Spring Security 拦截 → 抛出 AuthorizationDeniedException
     *   → 本方法捕获 → 返回 { code: 403, message: "权限不足" }
     */
    /*
     * @ResponseStatus(HttpStatus.FORBIDDEN)
     *   设置 HTTP 响应的状态码为 403。
     *   即使方法返回了 Result 对象，HTTP 状态码仍然由这个注解控制。
     *   前端可以通过 response.status 拿到 403。
     *
     * @ExceptionHandler(AuthorizationDeniedException.class)
     *   告诉 Spring："当发生 AuthorizationDeniedException 时，调这个方法来处理"。
     *   .class 是 Java 获取类对象的语法（类似 JS 的 typeof 但更强大）。
     */
    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(AuthorizationDeniedException.class)
    public Result<Void> handleAuthorizationDeniedException(AuthorizationDeniedException e) {
        log.warn("方法级权限不足：{}", e.getMessage()); // 记录警告日志
        return Result.fail(ResultCode.FORBIDDEN);       // 返回 403
    }

    // ==================== 2. URL 级权限异常 ====================

    /**
     * 处理 URL 拦截规则校验失败
     *
     * 触发场景：
     *   未登录用户访问了需要认证的接口（SecurityConfig 中配置的规则）
     *   → 抛出 AccessDeniedException
     */
    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(AccessDeniedException.class)
    public Result<Void> handleAccessDeniedException(AccessDeniedException e) {
        log.warn("URL级权限不足：{}", e.getMessage());
        return Result.fail(ResultCode.FORBIDDEN);
    }

    // ==================== 3. 自定义业务异常 ====================

    /**
     * 处理业务异常（最常用的异常类型）
     *
     * 触发场景：
     *   Service 层 throw new BusinessException(4001, "用户名已存在")
     *   → 本方法捕获 → 取出异常的 code 和 message → 返回给前端
     *
     * 注意：这里没有 @ResponseStatus 注解。
     *   业务异常的状态码由异常的 code 字段决定（如 4000/4001），
     *   不是固定的 HTTP 状态码，所以不硬编码。
     */
    /*
     * @ExceptionHandler(BusinessException.class)
     *   当任何 Controller 中抛出 BusinessException 时，Spring 调用此方法。
     *
     * public Result<Void> handleBusinessException(BusinessException e)
     *   参数 e 就是 Service 层抛出的那个异常对象。
     *   通过 e.getCode() 和 e.getMessage() 取到当初构造时传入的值。
     */
    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException e) {
        /*
         * log.warn("业务异常：code={}, message={}", e.getCode(), e.getMessage());
         *
         *   {} 是 SLF4J 的占位符，后续参数按顺序替换。
         *   和 console.log(`code=${code}, message=${msg}`) 的模板字符串一样。
         *
         *   这里用 warn 级别而不是 error：
         *     业务异常是由用户操作不当引起的（如输入已存在的用户名），
         *     不是系统 bug，不需要 error 级别。
         */
        log.warn("业务异常：code={}, message={}", e.getCode(), e.getMessage());

        /*
         * Result.fail(e.getCode(), e.getMessage())
         *
         *   用异常的 code 和 message 构造失败响应。
         *   例如：{ code: 4001, message: "用户名已存在", data: null }
         */
        return Result.fail(e.getCode(), e.getMessage());
    }

    // ==================== 4. 参数校验失败异常 ====================

    /**
     * 处理 @Valid 参数校验失败
     *
     * 触发场景：
     *   Controller 中有 @Valid @RequestBody ScoreDTO dto
     *   ScoreDTO 的 score 字段有 @Max(100) 注解
     *   前端传了 score=150 → Spring 校验失败 → 抛出 MethodArgumentNotValidException
     *
     * 异常对象中包含所有校验失败的字段和原因，需要遍历提取。
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleValidationException(MethodArgumentNotValidException e) {

        /*
         * StringBuilder errorMsg = new StringBuilder();
         *
         *   StringBuilder：Java 的字符串构建器（性能优化）。
         *   如果在循环中用 + 拼接字符串，每次会创建新的 String 对象（String 不可变）。
         *   用 StringBuilder 可以避免大量临时对象。
         *
         *   类比 JavaScript：
         *     let errorMsg = ''          // JS 中字符串拼接没有性能问题
         *     // Java 中用 StringBuilder 替代 String +=
         */
        StringBuilder errorMsg = new StringBuilder();

        /*
         * e.getBindingResult().getFieldErrors().forEach(error -> { ... })
         *
         *   拆解：
         *     getBindingResult()   → 获取校验结果对象
         *     getFieldErrors()     → 获取所有字段校验错误列表（List<FieldError>）
         *     forEach(error -> {})  → Lambda 表达式遍历列表
         *
         *   FieldError 对象包含：
         *     error.getField()        → 字段名（如 "score"）
         *     error.getDefaultMessage() → 校验失败消息（如 "最大不能超过100"）
         *
         *   最终拼接出："score: 最大不能超过100; name: 不能为空; "
         *
         *   类比 JavaScript：
         *     const errors = e.details.map(err => `${err.field}: ${err.message}`)
         *     const msg = errors.join('; ')
         */
        e.getBindingResult().getFieldErrors().forEach(error -> {
            errorMsg.append(error.getField())          // 字段名
                    .append(": ")                      // 冒号分隔
                    .append(error.getDefaultMessage()) // 校验消息
                    .append("; ");                     // 分号分隔
        });

        String msg = errorMsg.toString();
        log.warn("参数校验失败：{}", msg);
        return Result.fail(ResultCode.PARAM_ERROR.getCode(), msg); // code=400, 消息=具体校验错误
    }

    // ==================== 5. 兜底异常处理 ====================

    /**
     * 处理所有未被上面捕获的异常（通用兜底）
     *
     * 这是一个"安全网"：任何意料之外的异常都会到这里。
     * 防止系统内部错误信息（如 SQL 语句、堆栈信息）泄露到前端。
     *
     * 为什么需要兜底？
     *   如果发生 NullPointerException 或 SQLException：
     *     - 没有这个兜底 → 前端收到 Tomcat 默认的 HTML 错误页面 → 前端无法解析
     *     - 有这个兜底 → 前端收到 { code: 500, message: "服务器错误" } → 前端正常提示
     */
    /*
     * @ExceptionHandler(Exception.class)
     *
     *   Exception.class 是 Java 所有异常的父类（除了 Error）。
     *   所以这个方法可以捕获任何未在上面被处理的异常。
     *
     *   匹配顺序：
     *     Spring 先找最精确的 @ExceptionHandler（如 BusinessException.class），
     *     找不到才找父类的（如 Exception.class）。
     *     所以上面定义的更具体的处理器会优先匹配。
     */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {

        /*
         * 异常链检查：处理 AOP 代理包装的权限异常
         *
         *   有时候 Spring Security 的异常会被 AOP 代理包装成 UndeclaredThrowableException，
         *   直接 catch 到的是包装后的异常，原始异常在 getCause() 链中。
         *
         *   这里遍历异常链，找到真正的原因。
         *
         *   类比 JavaScript：
         *     let cause = e
         *     while (cause) {
         *       if (cause instanceof AccessDenied) return 403
         *       cause = cause.cause // 获取上层异常
         *     }
         */
        Throwable cause = e; // Throwable 是 Exception 的父类
        while (cause != null) {
            /*
             * if (cause instanceof AccessDeniedException) { ... }
             *
             *   instanceof 关键字：运行时类型检查。
             *   判断 cause 对象是否是 AccessDeniedException 的实例（或其子类的实例）。
             *
             *   类比 JavaScript：if (cause instanceof AccessDeniedError)
             */
            if (cause instanceof AccessDeniedException) {
                log.warn("通过异常链检测到权限不足：{}", cause.getMessage());
                return Result.fail(ResultCode.FORBIDDEN);
            }
            cause = cause.getCause(); // 获取原始异常（解开一层包装）
        }

        /*
         * log.error("服务器内部错误 [{}]: {}", e.getClass().getName(), e.getMessage(), e);
         *
         *   这里用 error 级别（不是 warn），因为这是代码 bug 或系统故障。
         *   e.getClass().getName() → 异常类名（如 "java.lang.NullPointerException"）
         *   e.getMessage()         → 异常信息
         *   最后一个 e 参数 → 把完整堆栈信息打印到日志中
         */
        log.error("服务器内部错误 [{}]: {}", e.getClass().getName(), e.getMessage(), e);

        return Result.fail(ResultCode.ERROR); // { code: 500, message: "服务器错误" }
    }
}
