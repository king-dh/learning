package com.example.backend.controller; // 声明 Controller 包

import com.example.backend.common.Result; // 统一响应结果
import com.example.backend.dto.ClassDTO; // 班级请求 DTO
import com.example.backend.dto.ClassQueryDTO; // 班级查询 DTO
import com.example.backend.service.ClassService; // 班级服务
import com.example.backend.vo.ClassVO; // 班级视图对象
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
 * 班级管理控制器
 * 处理班级的增删改查和班级学生列表
 */
@Tag(name = "班级管理", description = "班级信息增删改查") // Knife4j 标签
@RestController // REST 控制器
@RequestMapping("/api/classes") // 路径前缀
@RequiredArgsConstructor // 构造器注入
public class ClassController {

    private final ClassService classService; // 注入班级服务

    /**
     * 分页查询班级列表
     */
    @Operation(summary = "分页查询班级")
    @GetMapping("/page")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')") // 管理员和教师可访问
    public Result<?> page(ClassQueryDTO dto) { // 参数绑定
        return Result.ok(classService.pageQuery(dto)); // 返回分页结果
    }

    /**
     * 根据 ID 查询班级详情
     */
    @Operation(summary = "根据ID查询班级")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')") // 管理员和教师可访问
    public Result<ClassVO> getById(@PathVariable Long id) {
        return Result.ok(classService.getById(id)); // 返回班级详情
    }

    /**
     * 新增班级
     */
    @Operation(summary = "新增班级")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')") // 仅管理员可新增
    public Result<Void> save(@RequestBody ClassDTO dto) {
        classService.save(dto); // 调用服务保存
        return Result.ok(null); // 返回成功
    }

    /**
     * 修改班级信息
     */
    @Operation(summary = "修改班级信息")
    @PutMapping
    @PreAuthorize("hasRole('ADMIN')") // 仅管理员可修改
    public Result<Void> update(@RequestBody ClassDTO dto) {
        classService.update(dto); // 调用服务更新
        return Result.ok(null); // 返回成功
    }

    /**
     * 删除班级
     */
    @Operation(summary = "删除班级")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')") // 仅管理员可删除
    public Result<Void> delete(@PathVariable Long id) {
        classService.delete(id); // 调用服务删除
        return Result.ok(null); // 返回成功
    }

    /**
     * 查询班级下的所有学生列表
     * GET /api/classes/{id}/students
     */
    @Operation(summary = "查询班级下的学生列表")
    @GetMapping("/{id}/students")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')") // 管理员和教师可访问
    public Result<?> getStudents(@PathVariable Long id) {
        return Result.ok(classService.getStudentsByClassId(id)); // 返回班级学生列表
    }
}
