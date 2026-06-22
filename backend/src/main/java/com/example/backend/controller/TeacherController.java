package com.example.backend.controller; // 声明 Controller 包

import com.example.backend.common.Result; // 统一响应结果
import com.example.backend.dto.TeacherDTO; // 教师请求 DTO
import com.example.backend.dto.TeacherQueryDTO; // 教师查询 DTO
import com.example.backend.service.TeacherService; // 教师服务
import com.example.backend.vo.TeacherVO; // 教师视图对象
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
 * 教师管理控制器
 * 处理教师信息的增删改查和分页搜索
 */
@Tag(name = "教师管理", description = "教师信息增删改查") // Knife4j 标签
@RestController // REST 控制器
@RequestMapping("/api/teachers") // 路径前缀
@RequiredArgsConstructor // 构造器注入
public class TeacherController {

    private final TeacherService teacherService; // 注入教师服务

    /**
     * 分页查询教师列表
     * GET /api/teachers/page?pageNum=1&pageSize=10&name=xxx
     */
    @Operation(summary = "分页查询教师")
    @GetMapping("/page")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')") // 管理员和教师可访问
    public Result<?> page(TeacherQueryDTO dto) { // 参数绑定到 DTO
        return Result.ok(teacherService.pageQuery(dto)); // 返回分页结果
    }

    /**
     * 根据 ID 查询教师详情
     * GET /api/teachers/{id}
     */
    @Operation(summary = "根据ID查询教师")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')") // 管理员和教师可访问
    public Result<TeacherVO> getById(@PathVariable Long id) { // 路径参数
        return Result.ok(teacherService.getById(id)); // 返回教师详情
    }

    /**
     * 新增教师
     * POST /api/teachers
     */
    @Operation(summary = "新增教师")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')") // 仅管理员可新增
    public Result<Void> save(@RequestBody TeacherDTO dto) {
        teacherService.save(dto); // 调用服务保存
        return Result.ok(null); // 返回成功
    }

    /**
     * 修改教师信息
     * PUT /api/teachers
     */
    @Operation(summary = "修改教师信息")
    @PutMapping
    @PreAuthorize("hasRole('ADMIN')") // 仅管理员可修改
    public Result<Void> update(@RequestBody TeacherDTO dto) {
        teacherService.update(dto); // 调用服务更新
        return Result.ok(null); // 返回成功
    }

    /**
     * 删除教师
     * DELETE /api/teachers/{id}
     */
    @Operation(summary = "删除教师")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')") // 仅管理员可删除
    public Result<Void> delete(@PathVariable Long id) {
        teacherService.delete(id); // 调用服务删除
        return Result.ok(null); // 返回成功
    }
}
