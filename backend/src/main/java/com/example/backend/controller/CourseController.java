package com.example.backend.controller; // 声明 Controller 包

import com.example.backend.common.Result; // 统一响应结果
import com.example.backend.dto.CourseDTO; // 课程请求 DTO
import com.example.backend.dto.CourseQueryDTO; // 课程查询 DTO
import com.example.backend.service.CourseService; // 课程服务
import com.example.backend.vo.CourseVO; // 课程视图对象
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
 * 课程管理控制器
 * 处理课程信息的增删改查和按教师查询
 */
@Tag(name = "课程管理", description = "课程信息增删改查") // Knife4j 标签
@RestController // REST 控制器
@RequestMapping("/api/courses") // 路径前缀
@RequiredArgsConstructor // 构造器注入
public class CourseController {

    private final CourseService courseService; // 注入课程服务

    /**
     * 分页查询课程列表
     */
    @Operation(summary = "分页查询课程")
    @GetMapping("/page")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')") // 所有角色可访问
    public Result<?> page(CourseQueryDTO dto) { // 参数绑定
        return Result.ok(courseService.pageQuery(dto)); // 返回分页结果
    }

    /**
     * 根据 ID 查询课程详情
     */
    @Operation(summary = "根据ID查询课程")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')") // 所有角色可访问
    public Result<CourseVO> getById(@PathVariable Long id) {
        return Result.ok(courseService.getById(id)); // 返回课程详情
    }

    /**
     * 新增课程
     */
    @Operation(summary = "新增课程")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')") // 仅管理员可新增
    public Result<Void> save(@RequestBody CourseDTO dto) {
        courseService.save(dto); // 调用服务保存
        return Result.ok(null); // 返回成功
    }

    /**
     * 修改课程信息
     */
    @Operation(summary = "修改课程信息")
    @PutMapping
    @PreAuthorize("hasRole('ADMIN')") // 仅管理员可修改
    public Result<Void> update(@RequestBody CourseDTO dto) {
        courseService.update(dto); // 调用服务更新
        return Result.ok(null); // 返回成功
    }

    /**
     * 删除课程
     */
    @Operation(summary = "删除课程")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')") // 仅管理员可删除
    public Result<Void> delete(@PathVariable Long id) {
        courseService.delete(id); // 调用服务删除
        return Result.ok(null); // 返回成功
    }

    /**
     * 根据教师 ID 查询该教师的所有课程
     * GET /api/courses/teacher/{teacherId}
     */
    @Operation(summary = "根据教师ID查询课程")
    @GetMapping("/teacher/{teacherId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')") // 管理员和教师可访问
    public Result<?> getByTeacherId(@PathVariable Long teacherId) {
        return Result.ok(courseService.getByTeacherId(teacherId)); // 返回教师课程列表
    }
}
