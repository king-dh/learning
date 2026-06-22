package com.example.backend.controller; // 声明 Controller 包

import com.example.backend.common.Result; // 统一响应结果
import com.example.backend.dto.StudentDTO; // 学生请求 DTO
import com.example.backend.dto.StudentQueryDTO; // 学生查询 DTO
import com.example.backend.service.StudentService; // 学生服务
import com.example.backend.vo.StudentVO; // 学生视图对象
import io.swagger.v3.oas.annotations.Operation; // Knife4j 接口说明
import io.swagger.v3.oas.annotations.tags.Tag; // Knife4j 分组标签
import lombok.RequiredArgsConstructor; // 构造器注入
import org.springframework.security.access.prepost.PreAuthorize; // 方法级权限控制
import org.springframework.web.bind.annotation.DeleteMapping; // DELETE 请求
import org.springframework.web.bind.annotation.GetMapping; // GET 请求
import org.springframework.web.bind.annotation.PathVariable; // 路径参数
import org.springframework.web.bind.annotation.PostMapping; // POST 请求
import org.springframework.web.bind.annotation.PutMapping; // PUT 请求
import org.springframework.web.bind.annotation.RequestBody; // 请求体
import org.springframework.web.bind.annotation.RequestMapping; // 路径前缀
import org.springframework.web.bind.annotation.RestController; // REST 控制器

/**
 * 学生管理控制器
 * 处理学生信息的增删改查和分页搜索
 */
@Tag(name = "学生管理", description = "学生信息增删改查") // Knife4j 标签
@RestController // REST 控制器
@RequestMapping("/api/students") // 路径前缀
@RequiredArgsConstructor // 构造器注入
public class StudentController {

    private final StudentService studentService; // 注入学生服务

    /**
     * 分页查询学生列表
     * GET /api/students/page?pageNum=1&pageSize=10&name=xxx
     */
    @Operation(summary = "分页查询学生") // Knife4j 说明
    @GetMapping("/page") // GET 请求
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')") // 管理员和教师可访问
    public Result<?> page(StudentQueryDTO dto) { // 参数自动绑定到 DTO
        return Result.ok(studentService.pageQuery(dto)); // 返回分页结果
    }

    /**
     * 根据学生 ID 查询详情
     * GET /api/students/{id}
     */
    @Operation(summary = "根据ID查询学生")
    @GetMapping("/{id}") // 路径参数 {id}
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')") // 管理员和教师可访问
    public Result<StudentVO> getById(@PathVariable Long id) { // @PathVariable 获取路径参数
        return Result.ok(studentService.getById(id)); // 返回学生详情
    }

    /**
     * 新增学生
     * POST /api/students
     */
    @Operation(summary = "新增学生")
    @PostMapping // POST 请求
    @PreAuthorize("hasRole('ADMIN')") // 仅管理员可新增
    public Result<Void> save(@RequestBody StudentDTO dto) { // @RequestBody 接收 JSON
        studentService.save(dto); // 调用服务保存
        return Result.ok(null); // 返回成功
    }

    /**
     * 修改学生信息
     * PUT /api/students
     */
    @Operation(summary = "修改学生信息")
    @PutMapping // PUT 请求
    @PreAuthorize("hasRole('ADMIN')") // 仅管理员可修改
    public Result<Void> update(@RequestBody StudentDTO dto) {
        studentService.update(dto); // 调用服务更新
        return Result.ok(null); // 返回成功
    }

    /**
     * 删除学生
     * DELETE /api/students/{id}
     */
    @Operation(summary = "删除学生")
    @DeleteMapping("/{id}") // DELETE 请求 + 路径参数
    @PreAuthorize("hasRole('ADMIN')") // 仅管理员可删除
    public Result<Void> delete(@PathVariable Long id) {
        studentService.delete(id); // 调用服务删除
        return Result.ok(null); // 返回成功
    }
}
