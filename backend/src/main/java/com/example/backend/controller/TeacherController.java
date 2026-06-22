/*
 * ================================================================
 * 文件位置:  src/main/java/com/example/backend/controller/TeacherController.java
 * 对应前端:  frontend/src/views/teacher/TeacherList.vue（教师管理页面）
 *           frontend/src/api/teacher.js（前端 API 调用）
 *
 * 调用链路（以"分页查询教师"为例）:
 * 前端点"教师管理"页面 → axios 发 GET /api/teachers/page?pageNum=1
 *                     → Vite 代理转发到 localhost:8088
 *                     → 本 Controller 的 page() 方法
 *                     → TeacherServiceImpl.pageQuery()
 *                     → MyBatis-Plus 分页查询 teacher 表
 *                     → 返回 JSON 给前端
 * ================================================================
 */

// ==================== 1. 包声明 ====================
package com.example.backend.controller; // 声明 Controller 包

// ==================== 2. 导入其他类（import） ====================
import com.example.backend.common.Result;        // 统一响应结果
import com.example.backend.dto.TeacherDTO;       // 教师新增/修改 DTO
import com.example.backend.dto.TeacherQueryDTO;  // 教师查询 DTO
import com.example.backend.service.TeacherService; // 教师服务接口
import com.example.backend.vo.TeacherVO;         // 教师视图对象
import io.swagger.v3.oas.annotations.Operation;  // Swagger 接口说明
import io.swagger.v3.oas.annotations.tags.Tag;    // Swagger 分组标签
import lombok.RequiredArgsConstructor;           // 构造器注入
import org.springframework.security.access.prepost.PreAuthorize; // 方法级权限
import org.springframework.web.bind.annotation.DeleteMapping;  // DELETE
import org.springframework.web.bind.annotation.GetMapping;     // GET
import org.springframework.web.bind.annotation.PathVariable;   // 路径参数
import org.springframework.web.bind.annotation.PostMapping;    // POST
import org.springframework.web.bind.annotation.PutMapping;     // PUT
import org.springframework.web.bind.annotation.RequestBody;    // 请求体
import org.springframework.web.bind.annotation.RequestMapping; // 路径前缀
import org.springframework.web.bind.annotation.RestController; // REST 控制器

// ==================== 3. 类声明 ====================
@Tag(name = "教师管理", description = "教师信息增删改查")
@RestController
@RequestMapping("/api/teachers")
@RequiredArgsConstructor
public class TeacherController {

    private final TeacherService teacherService;

    // ================================================================
    // 接口 1：分页查询教师列表
    // GET /api/teachers/page?pageNum=1&pageSize=10&name=张
    // 权限：ADMIN 和 TEACHER 可访问
    // ================================================================
    @Operation(summary = "分页查询教师")
    @GetMapping("/page")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public Result<?> page(TeacherQueryDTO dto) {
        return Result.ok(teacherService.pageQuery(dto));
    }

    // ================================================================
    // 接口 2：根据 ID 查询教师详情
    // GET /api/teachers/{id}
    // ================================================================
    @Operation(summary = "根据ID查询教师")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public Result<TeacherVO> getById(@PathVariable Long id) {
        return Result.ok(teacherService.getById(id));
    }

    // ================================================================
    // 接口 3：新增教师
    // POST /api/teachers
    // 仅管理员可新增
    // ================================================================
    @Operation(summary = "新增教师")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> save(@RequestBody TeacherDTO dto) {
        teacherService.save(dto);
        return Result.ok(null);
    }

    // ================================================================
    // 接口 4：修改教师
    // PUT /api/teachers
    // 仅管理员可修改
    // ================================================================
    @Operation(summary = "修改教师信息")
    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> update(@RequestBody TeacherDTO dto) {
        teacherService.update(dto);
        return Result.ok(null);
    }

    // ================================================================
    // 接口 5：删除教师
    // DELETE /api/teachers/{id}
    // 仅管理员可删除
    // ================================================================
    @Operation(summary = "删除教师")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> delete(@PathVariable Long id) {
        teacherService.delete(id);
        return Result.ok(null);
    }
}
