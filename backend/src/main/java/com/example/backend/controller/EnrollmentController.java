/*
 * ================================================================
 * 文件位置:  src/main/java/com/example/backend/controller/EnrollmentController.java
 * 对应前端:  frontend/src/views/enrollment/EnrollmentList.vue（选课管理页面）
 *           frontend/src/api/enrollment.js（前端 API 调用）
 *
 * 调用链路（以"查询我的已选课程"为例）:
 * 学生登录后点"我的课程" → axios 发 GET /api/enrollments/my
 *                       → Header 携带 Authorization: Bearer <token>
 *                       → JwtAuthenticationFilter 从 Token 解析出用户名
 *                       → SecurityContextHolder 存入认证信息
 *                       → 本 Controller 的 getMyEnrollments() 从 SecurityContext 取用户名
 *                       → EnrollmentServiceImpl.getByUsername(username)
 *                       → 返回该学生已选课程列表
 *
 * 选课模块的特殊性：
 *   - 涉及 student 和 course 两张表的关联（多对多）
 *   - 需要从 JWT Token 中自动获取当前学生身份
 *   - 学生只能选/退自己的课
 * ================================================================
 */

// ==================== 1. 包声明 ====================
package com.example.backend.controller;

// ==================== 2. 导入 ====================

/*
 * Common 层：统一响应结果包装类
 *   和 JavaScript 的「统一返回格式中间件」一样：所有 API 返回 { code, message, data }
 */
import com.example.backend.common.Result;

/*
 * DTO 层：EnrollmentDTO 接收前端选课请求
 *   包含 studentId（学生ID）和 courseId（课程ID）
 */
import com.example.backend.dto.EnrollmentDTO;

/*
 * Service 层：EnrollmentService 处理选课/退课的业务逻辑
 *   Controller 不写 SQL，全靠 Service
 */
import com.example.backend.service.EnrollmentService;

/*
 * VO 层：EnrollmentVO 返回给前端的选课记录
 *   包含课程名称、学生姓名等关联信息
 */
import com.example.backend.vo.EnrollmentVO;

/*
 * SpringDoc：API 文档注解
 */
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/*
 * Lombok：自动生成构造函数
 */
import lombok.RequiredArgsConstructor;

/*
 * Spring Security：权限控制
 */
import org.springframework.security.access.prepost.PreAuthorize;

/*
 * Spring Security 核心类：用于获取当前登录用户信息
 *
 * Authentication：
 *   代表"当前登录用户的身份凭证"，包含用户名（principal）和权限列表（authorities）。
 *   在 JWT 认证流程中，JwtAuthenticationFilter 创建了一个 Authentication 对象并存入 SecurityContext。
 *
 *   类比 JavaScript：这就是 req.user，存储当前已登录用户的信息。
 *     const user = req.user // Express + Passport.js 中的认证中间件效果
 *     req.user.username      // ← authentication.getName()
 *     req.user.role           // ← authentication.getAuthorities()
 */
import org.springframework.security.core.Authentication;

/*
 * SecurityContextHolder：
 *   Spring Security 的"全局上下文持有者"，类似于一个 ThreadLocal 变量。
 *   在任何地方调用 SecurityContextHolder.getContext().getAuthentication()
 *   都能拿到当前请求的认证信息，不需要层层传递参数。
 *
 *   类比 JavaScript：
 *     在 Express 中间件中，req.user 可以在所有路由处理器中访问。
 *     SecurityContextHolder 就是 Java 版的 req.user，但是通过 ThreadLocal 实现。
 */
import org.springframework.security.core.context.SecurityContextHolder;

/*
 * Spring Web：REST 接口定义
 */
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// ==================== 3. 类声明和注解 ====================

@Tag(name = "选课管理", description = "选课和退课接口")
@RestController
@RequestMapping("/api/enrollments")
@RequiredArgsConstructor
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    // ================================================================
    // 接口 1：查询当前登录学生的已选课程
    // GET /api/enrollments/my
    //
    // 关键：从 SecurityContextHolder 中获取当前用户名
    // 学生不需要传参，系统自动识别"你是谁"
    // ================================================================
    @Operation(summary = "查询我的已选课程")
    @GetMapping("/my")
    @PreAuthorize("hasAnyRole('ADMIN', 'STUDENT')")
    public Result<List<EnrollmentVO>> getMyEnrollments() {

        /*
         * SecurityContextHolder.getContext().getAuthentication()
         *
         *   工作流程（结合 JWT 认证链路）：
         *     1. 前端请求携带 Authorization: Bearer <JWT>
         *     2. JwtAuthenticationFilter 拦截请求 → 解析 JWT → 获取用户名和角色
         *     3. 创建 Authentication 对象 → 存入 SecurityContextHolder
         *     4. 本 Controller 从 SecurityContextHolder 取出 Authentication
         *     5. getName() 获取用户名（即 JWT 的 subject 字段）
         *
         *   类比 JavaScript：
         *     router.get('/my', (req, res) => {
         *       const username = req.user.username // ← SecurityContextHolder
         *       const courses = enrollmentService.getByUsername(username)
         *       res.json({ code: 200, data: courses })
         *     })
         */
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        return Result.ok(enrollmentService.getByUsername(username));
    }

    // ================================================================
    // 接口 2：查询指定学生的已选课程（管理员/教师视角）
    // GET /api/enrollments/student/{studentId}
    // ================================================================
    @Operation(summary = "查询学生已选课程")
    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    public Result<List<EnrollmentVO>> getByStudentId(@PathVariable Long studentId) {
        return Result.ok(enrollmentService.getByStudentId(studentId));
    }

    // ================================================================
    // 接口 3：选课
    // POST /api/enrollments
    //
    // 学生选课时只需要传 courseId，系统自动从 JWT 中获取 studentId
    // 管理员选课时需要手动传 studentId 和 courseId
    // ================================================================
    @Operation(summary = "学生选课")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STUDENT')")
    public Result<Void> enroll(@RequestBody EnrollmentDTO dto) {

        /*
         * 如果前端没传 studentId（学生自己选课的场景），
         * 就从 JWT Token 中自动获取当前登录用户的学生 ID。
         *
         * 这样前端不需要显式传 studentId，系统自动绑定当前用户。
         *
         * 类比 JavaScript：
         *   const studentId = dto.studentId || req.user.studentId
         */
        if (dto.getStudentId() == null) {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            dto.setStudentId(enrollmentService.getStudentIdByUsername(username));
        }

        enrollmentService.enroll(dto);
        return Result.ok(null);
    }

    // ================================================================
    // 接口 4：退课
    // DELETE /api/enrollments/{id}
    // id 是选课记录的主键，不是学生 ID 或课程 ID
    // ================================================================
    @Operation(summary = "学生退课")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STUDENT')")
    public Result<Void> unenroll(@PathVariable Long id) {
        enrollmentService.unenroll(id);
        return Result.ok(null);
    }
}
