/*
 * ================================================================
 * 文件位置:  src/main/java/com/example/backend/controller/ScoreController.java
 * 对应前端:  frontend/src/views/score/ScoreList.vue（成绩管理页面）
 *           frontend/src/api/score.js（前端 API 调用）
 *
 * 调用链路（以"分页查询成绩"为例）:
 * 教师点"成绩管理"页面 → axios 发 GET /api/scores/page?pageNum=1
 *                     → Vite 代理转发到 localhost:8088
 *                     → 本 Controller 的 page() 方法
 *                     → ScoreServiceImpl.pageQuery()
 *                     → MyBatis-Plus 分页查询（JOIN student, course）
 *                     → 返回 JSON（含学生姓名、课程名称）
 *
 * 成绩模块的特殊性：
 *   - 涉及 student 和 course 两张表的关联
 *   - 有分数范围校验（@Valid + @Min/@Max 注解）
 *   - 学生只能看自己的成绩，教师和管理员可以录入和修改
 * ================================================================
 */

// ==================== 1. 包声明 ====================
package com.example.backend.controller;

// ==================== 2. 导入 ====================
import com.example.backend.common.Result;        // 统一响应结果
import com.example.backend.dto.ScoreDTO;         // 成绩新增/修改时的请求数据（学生ID、课程ID、分数）
import com.example.backend.dto.ScoreQueryDTO;    // 成绩查询条件（学生名、课程名、页码...）
import com.example.backend.service.ScoreService;  // 成绩服务接口
import io.swagger.v3.oas.annotations.Operation;  // Swagger 接口说明
import io.swagger.v3.oas.annotations.tags.Tag;    // Swagger 分组标签
import jakarta.validation.Valid;                 // 参数校验：触发 ScoreDTO 中的 @Min/@Max 分数范围校验
import lombok.RequiredArgsConstructor;           // 构造器注入
import org.springframework.security.access.prepost.PreAuthorize; // 方法级权限控制
import org.springframework.web.bind.annotation.DeleteMapping;  // DELETE
import org.springframework.web.bind.annotation.GetMapping;     // GET
import org.springframework.web.bind.annotation.PathVariable;   // 路径参数
import org.springframework.web.bind.annotation.PostMapping;    // POST
import org.springframework.web.bind.annotation.PutMapping;     // PUT
import org.springframework.web.bind.annotation.RequestBody;    // 请求体
import org.springframework.web.bind.annotation.RequestMapping; // 路径前缀
import org.springframework.web.bind.annotation.RestController; // REST 控制器

// ==================== 3. 类声明 ====================
@Tag(name = "成绩管理", description = "成绩信息增删改查")
@RestController
@RequestMapping("/api/scores")
@RequiredArgsConstructor
public class ScoreController {

    private final ScoreService scoreService;

    // ================================================================
    // 接口 1：分页查询成绩列表
    // GET /api/scores/page?pageNum=1&pageSize=10&studentName=赵
    // 返回：学生姓名 + 课程名称 + 分数（多表关联查询）
    // ================================================================
    @Operation(summary = "分页查询成绩")
    @GetMapping("/page")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public Result<?> page(ScoreQueryDTO dto) {
        return Result.ok(scoreService.pageQuery(dto));
    }

    // ================================================================
    // 接口 2：新增成绩（含分数范围校验）
    // POST /api/scores
    //
    // @Valid 注解会触发 ScoreDTO 中的校验规则：
    //   - @NotNull(message = "分数不能为空")
    //   - @Min(value = 0) / @Max(value = 100) → 分数必须在 0~100 之间
    //
    // 如果校验失败（如分数为 150），Spring 会抛出 MethodArgumentNotValidException，
    // 由 GlobalExceptionHandler 统一处理，返回 { code: 400, message: "score: 最大不能超过100" }
    // ================================================================
    @Operation(summary = "新增成绩")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public Result<Void> save(@Valid @RequestBody ScoreDTO dto) {
        scoreService.save(dto);
        return Result.ok(null);
    }

    // ================================================================
    // 接口 3：修改成绩
    // PUT /api/scores
    // 同样使用 @Valid 做分数校验
    // ================================================================
    @Operation(summary = "修改成绩")
    @PutMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public Result<Void> update(@Valid @RequestBody ScoreDTO dto) {
        scoreService.update(dto);
        return Result.ok(null);
    }

    // ================================================================
    // 接口 4：删除成绩
    // DELETE /api/scores/{id}
    // ================================================================
    @Operation(summary = "删除成绩")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public Result<Void> delete(@PathVariable Long id) {
        scoreService.delete(id);
        return Result.ok(null);
    }

    // ================================================================
    // 接口 5：根据学生 ID 查询成绩
    // GET /api/scores/student/{studentId}
    //
    // 所有角色都可以访问：学生查自己的成绩，教师查学生的成绩，管理员全看
    // ================================================================
    @Operation(summary = "根据学生ID查询成绩")
    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    public Result<?> getByStudentId(@PathVariable Long studentId) {
        return Result.ok(scoreService.getByStudentId(studentId));
    }
}
