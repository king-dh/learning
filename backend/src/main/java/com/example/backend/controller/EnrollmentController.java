package com.example.backend.controller; // 声明 Controller 包

import com.example.backend.common.Result; // 统一响应结果
import com.example.backend.dto.EnrollmentDTO; // 选课请求 DTO
import com.example.backend.service.EnrollmentService; // 选课服务
import com.example.backend.vo.EnrollmentVO; // 选课视图对象
import io.swagger.v3.oas.annotations.Operation; // Knife4j 接口说明
import io.swagger.v3.oas.annotations.tags.Tag; // Knife4j 分组标签
import lombok.RequiredArgsConstructor; // 构造器注入
import org.springframework.security.access.prepost.PreAuthorize; // 方法级权限控制
import org.springframework.security.core.Authentication; // 认证信息
import org.springframework.security.core.context.SecurityContextHolder; // 安全上下文
import org.springframework.web.bind.annotation.DeleteMapping; // DELETE 请求
import org.springframework.web.bind.annotation.GetMapping; // GET 请求
import org.springframework.web.bind.annotation.PathVariable; // 路径参数
import org.springframework.web.bind.annotation.PostMapping; // POST 请求
import org.springframework.web.bind.annotation.RequestBody; // 请求体
import org.springframework.web.bind.annotation.RequestMapping; // 路径前缀
import org.springframework.web.bind.annotation.RestController; // REST 控制器

import java.util.List; // 列表

/**
 * 选课管理控制器
 * 处理选课和退课请求
 */
@Tag(name = "选课管理", description = "选课和退课接口") // Knife4j 标签
@RestController // REST 控制器
@RequestMapping("/api/enrollments") // 路径前缀
@RequiredArgsConstructor // 构造器注入
public class EnrollmentController {

    private final EnrollmentService enrollmentService; // 注入选课服务

    /**
     * 查询当前登录学生的已选课程列表
     * GET /api/enrollments/my
     * 从 SecurityContext 中提取当前用户的用户名，通过 SysUser 关联 Student 查询
     */
    @Operation(summary = "查询我的已选课程")
    @GetMapping("/my")
    @PreAuthorize("hasAnyRole('ADMIN', 'STUDENT')") // 管理员和学生可访问
    public Result<List<EnrollmentVO>> getMyEnrollments() {
        // 从 SecurityContext 获取当前认证用户的用户名
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName(); // 获取当前登录用户名
        return Result.ok(enrollmentService.getByUsername(username)); // 返回我的选课
    }

    /**
     * 查询某个学生的已选课程列表
     * GET /api/enrollments/student/{studentId}
     */
    @Operation(summary = "查询学生已选课程")
    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')") // 所有角色可访问
    public Result<List<EnrollmentVO>> getByStudentId(@PathVariable Long studentId) {
        return Result.ok(enrollmentService.getByStudentId(studentId)); // 返回学生已选课程
    }

    /**
     * 选课
     * POST /api/enrollments
     * 如果未传 studentId，从当前登录用户自动获取
     */
    @Operation(summary = "学生选课")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'STUDENT')") // 管理员和学生可操作
    public Result<Void> enroll(@RequestBody EnrollmentDTO dto) {
        // 如果未传 studentId，从当前用户获取
        if (dto.getStudentId() == null) {
            String username = SecurityContextHolder.getContext().getAuthentication().getName();
            dto.setStudentId(enrollmentService.getStudentIdByUsername(username)); // 自动获取学生ID
        }
        enrollmentService.enroll(dto); // 调用选课服务
        return Result.ok(null); // 返回成功
    }

    /**
     * 退课
     * DELETE /api/enrollments/{id}
     */
    @Operation(summary = "学生退课")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'STUDENT')") // 管理员和学生可操作
    public Result<Void> unenroll(@PathVariable Long id) {
        enrollmentService.unenroll(id); // 调用退课服务
        return Result.ok(null); // 返回成功
    }
}
