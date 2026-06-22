package com.example.backend.controller; // 声明 Controller 包

import com.example.backend.common.Result; // 统一响应结果
import com.example.backend.dto.ScoreDTO; // 成绩请求 DTO
import com.example.backend.dto.ScoreQueryDTO; // 成绩查询 DTO
import com.example.backend.service.ScoreService; // 成绩服务
import io.swagger.v3.oas.annotations.Operation; // Knife4j 接口说明
import io.swagger.v3.oas.annotations.tags.Tag; // Knife4j 分组标签
import jakarta.validation.Valid; // 参数校验
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
 * 成绩管理控制器
 * 处理成绩的增删改查和多维度查询
 */
@Tag(name = "成绩管理", description = "成绩信息增删改查") // Knife4j 标签
@RestController // REST 控制器
@RequestMapping("/api/scores") // 路径前缀
@RequiredArgsConstructor // 构造器注入
public class ScoreController {

    private final ScoreService scoreService; // 注入成绩服务

    /**
     * 分页查询成绩列表
     */
    @Operation(summary = "分页查询成绩")
    @GetMapping("/page")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')") // 管理员和教师可访问
    public Result<?> page(ScoreQueryDTO dto) { // 参数绑定
        return Result.ok(scoreService.pageQuery(dto)); // 返回分页结果
    }

    /**
     * 新增成绩（含分数范围校验）
     */
    @Operation(summary = "新增成绩")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')") // 管理员和教师可录入
    public Result<Void> save(@Valid @RequestBody ScoreDTO dto) { // @Valid 校验分数范围
        scoreService.save(dto); // 调用服务保存
        return Result.ok(null); // 返回成功
    }

    /**
     * 修改成绩
     */
    @Operation(summary = "修改成绩")
    @PutMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')") // 管理员和教师可修改
    public Result<Void> update(@Valid @RequestBody ScoreDTO dto) {
        scoreService.update(dto); // 调用服务更新
        return Result.ok(null); // 返回成功
    }

    /**
     * 删除成绩
     */
    @Operation(summary = "删除成绩")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')") // 管理员和教师可删除
    public Result<Void> delete(@PathVariable Long id) {
        scoreService.delete(id); // 调用服务删除
        return Result.ok(null); // 返回成功
    }

    /**
     * 根据学生 ID 查询该学生的所有成绩（含课程名称）
     * GET /api/scores/student/{studentId}
     */
    @Operation(summary = "根据学生ID查询成绩")
    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')") // 所有角色可访问
    public Result<?> getByStudentId(@PathVariable Long studentId) {
        return Result.ok(scoreService.getByStudentId(studentId)); // 返回学生成绩列表
    }
}
