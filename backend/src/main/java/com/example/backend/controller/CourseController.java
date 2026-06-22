/*
 * ================================================================
 * 文件位置:  src/main/java/com/example/backend/controller/CourseController.java
 * 对应前端:  frontend/src/views/course/CourseList.vue（课程管理页面）
 *           frontend/src/api/course.js（前端 API 调用）
 *
 * 调用链路（以"分页查询课程"为例）:
 * 前端点"课程管理"页面 → axios 发 GET /api/courses/page?pageNum=1
 *                     → Vite 代理转发到 localhost:8088
 *                     → 本 Controller 的 page() 方法
 *                     → CourseServiceImpl.pageQuery()
 *                     → MyBatis-Plus 分页查询
 *                     → 返回 JSON 给前端
 *
 * 课程管理的特殊性：
 *   - 课程与教师关联（每门课有一个授课教师）
 *   - 学生也能查看课程列表（方便选课）
 *   - 提供"按教师查询课程"的专属接口
 * ================================================================
 */

// ==================== 1. 包声明 ====================

/*
 * package 声明当前文件所在的包，必须和硬盘路径一致。
 * 这里在 controller 子包下，表示这是一个"控制器"层的类。
 *
 * 类比 JavaScript：就是声明这个文件在项目的哪个文件夹下。
 */
package com.example.backend.controller; // 声明 Controller 包

// ==================== 2. 导入其他类（import） ====================

/*
 * import 导入了本项目中和第三方库中的类。
 * 与 JavaScript 的 import { xxx } from './xxx' 完全等价。
 */

// --- Common 层 ---
import com.example.backend.common.Result;        // 统一响应结果：所有接口的返回值都用它包装

// --- DTO 层：接收前端数据 ---
import com.example.backend.dto.CourseDTO;         // 课程新增/修改时的请求数据（课程名、学分、教师ID...）
import com.example.backend.dto.CourseQueryDTO;     // 课程分页查询时的请求参数（课程名、页码...）

// --- Service 层：业务逻辑 ---
import com.example.backend.service.CourseService;  // 课程服务接口（Controller 不写业务逻辑，全靠它）

// --- VO 层：返回前端数据 ---
import com.example.backend.vo.CourseVO;            // 课程视图对象（含课程信息 + 授课教师姓名等关联数据）

// --- SpringDoc：Swagger 接口文档 ---
import io.swagger.v3.oas.annotations.Operation;   // @Operation：给每个接口添加文字描述
import io.swagger.v3.oas.annotations.tags.Tag;     // @Tag：Swagger 页面的分组标签

// --- Lombok：减少代码 ---
import lombok.RequiredArgsConstructor;            // 自动生成构造函数，实现依赖注入

// --- Spring Security：权限控制 ---
import org.springframework.security.access.prepost.PreAuthorize; // 方法级权限注解（前端路由守卫的等价物）

// --- Spring Web：定义 REST 接口 ---
import org.springframework.web.bind.annotation.DeleteMapping;  // 映射 HTTP DELETE 请求
import org.springframework.web.bind.annotation.GetMapping;     // 映射 HTTP GET 请求
import org.springframework.web.bind.annotation.PathVariable;   // 提取 URL 中的路径参数（如 /{id}）
import org.springframework.web.bind.annotation.PostMapping;    // 映射 HTTP POST 请求
import org.springframework.web.bind.annotation.PutMapping;     // 映射 HTTP PUT 请求
import org.springframework.web.bind.annotation.RequestBody;    // 把 HTTP 请求体的 JSON → Java 对象
import org.springframework.web.bind.annotation.RequestMapping; // 给整个 Controller 设置 URL 前缀
import org.springframework.web.bind.annotation.RestController; // 声明这个类是 REST API 控制器（返回 JSON）

// ==================== 3. 类的声明和注解 ====================

/*
 * @Tag(name = "课程管理", description = "课程信息增删改查")
 *   在 Knife4j API 文档页面（http://localhost:8088/swagger-ui.html）上，
 *   所有课程相关接口会被归类到"课程管理"分组下。
 */
@Tag(name = "课程管理", description = "课程信息增删改查") // Knife4j 标签

/*
 * @RestController：
 *   = @Controller + @ResponseBody
 *   方法返回值会被自动序列化为 JSON 格式，用于前后端分离架构。
 *
 *   类比 JavaScript Express：
 *     app.get('/api/courses', (req, res) => { res.json(data) }) // ← 也是返回 JSON
 */
@RestController // REST 控制器

/*
 * @RequestMapping("/api/courses")：
 *   本 Controller 所有接口的 URL 都以 /api/courses 开头。
 *
 *   例如：page() 有 @GetMapping("/page")，最终 URL = /api/courses/page
 *
 *   类比 JavaScript Express：
 *     const router = express.Router()
 *     app.use('/api/courses', router)           ← @RequestMapping("/api/courses")
 *     router.get('/page', (req, res) => {})     ← @GetMapping("/page")
 */
@RequestMapping("/api/courses") // 路径前缀

/*
 * @RequiredArgsConstructor：
 *   Lombok 会在编译时生成带 final 字段的构造函数。
 *   本类中 courseService 是 final 的，所以生成的构造函数是：
 *     public CourseController(CourseService courseService) {
 *         this.courseService = courseService;
 *     }
 *   Spring 自动从容器中获取 CourseServiceImpl 实例并注入。
 *
 *   类比 JavaScript：
 *     class CourseController {
 *       #courseService;
 *       constructor(service) { this.#courseService = service } // DI
 *     }
 */
@RequiredArgsConstructor // 构造器注入

/*
 * public class CourseController { ... }
 *
 *   public            —— 公开类，任何地方都可访问（类似 JS export）
 *   class             —— 声明这是一个类
 *   CourseController  —— 类名，Java 要求与文件名完全一致
 */
public class CourseController {

    // ==================== 4. 字段声明 ====================

    /*
     * private final CourseService courseService;
     *
     *   private       —— 封装：只有本类能访问（外部看不到）
     *   final         —— 不可变：初始化后不能重新赋值（类似 JS const）
     *   CourseService —— 接口类型：面向接口编程，不依赖具体实现类
     *   courseService —— 字段名：小驼峰命名
     *
     *   Spring 依赖注入：
     *     @RequiredArgsConstructor 生成构造函数 → Spring 发现构造函数需要 CourseService
     *     → Spring 从容器中找 CourseServiceImpl（CourseService 的唯一实现）→ 自动注入。
     */
    private final CourseService courseService; // 注入课程服务

    // ==================== 5. 接口方法 ====================

    /*
     * ================================================================
     * 接口 1：分页查询课程列表
     *
     * 请求示例：GET /api/courses/page?pageNum=1&pageSize=10&name=Java
     *
     * 数据流转：
     *   前端发起 GET 请求带查询参数
     *     ↓ Spring 自动将 URL 参数映射到 CourseQueryDTO 对象的字段上
     *     ↓ 例如 ?name=Java → dto.setName("Java")
     *   本方法拿到 CourseQueryDTO dto（含 pageNum, pageSize, name 等）
     *     ↓ 调 Service
     *   courseService.pageQuery(dto)
     *     ↓ MyBatis-Plus 分页插件生成 LIMIT/OFFSET SQL
     *     ↓ 返回 IPage<CourseVO> 分页数据
     *   Result.ok(...)
     *     ↓ 包装成统一格式 JSON
     *   前端收到 { code: 200, message: "操作成功", data: { records: [...], total: 5 } }
     * ================================================================
     */

    @Operation(summary = "分页查询课程") // Swagger 文档说明

    /*
     * @GetMapping("/page")：
     *   处理 HTTP GET 请求，URL = /api/courses/page。
     *   查询操作使用 GET，因为它是幂等的（多次调用不影响数据状态）。
     */
    @GetMapping("/page") // GET /api/courses/page

    /*
     * @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
     *
     *   与班级管理的区别：班级只能管理员和教师查看，但课程允许学生也查看。
     *   因为学生需要看到课程列表才能选课。
     *
     *   hasAnyRole 的内部逻辑：
     *     - Spring Security 自动在角色名前加 "ROLE_" 前缀
     *     - 实际匹配的是 ROLE_ADMIN、ROLE_TEACHER、ROLE_STUDENT
     *     - 只要当前用户拥有其中任意一个角色，就允许访问
     *
     *   类比 Vue Router：
     *     { path: '/courses', meta: { roles: ['ADMIN', 'TEACHER', 'STUDENT'] } }
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')") // 所有角色可访问

    /*
     * public Result<?> page(CourseQueryDTO dto) { ... }
     *
     *   Result<?>        —— 返回 Result 对象，? 表示 data 类型不确定（实际是分页数据）
     *   page             —— 方法名
     *   CourseQueryDTO dto —— Spring 自动从 URL 参数绑定到对象
     *                        参数名必须和 DTO 字段名一致（如 pageNum, pageSize, name）
     */
    public Result<?> page(CourseQueryDTO dto) { // 参数绑定
        return Result.ok(courseService.pageQuery(dto)); // 查数据 → 包装 → 返回
    }

    // ================================================================
    // 接口 2：根据 ID 查询课程详情
    // ================================================================

    /*
     * 请求示例：GET /api/courses/1
     * 响应包含：课程本身信息 + 授课教师姓名
     */
    @Operation(summary = "根据ID查询课程") // Swagger 说明
    @GetMapping("/{id}") // GET /api/courses/{id}
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')") // 所有角色可查看

    /*
     * public Result<CourseVO> getById(@PathVariable Long id)
     *
     *   @PathVariable Long id —— 从 URL 路径 /{id} 中提取主键值
     *   例如 GET /api/courses/5 → id = 5（Long 类型）
     *
     *   Result<CourseVO> —— 返回的 data 明确是 CourseVO 类型（不是通配符 ?）
     */
    public Result<CourseVO> getById(@PathVariable Long id) {
        return Result.ok(courseService.getById(id)); // 查询并返回
    }

    // ================================================================
    // 接口 3：新增课程
    // ================================================================

    /*
     * 请求示例：POST /api/courses
     * 请求体：{ "name": "Python 程序设计", "courseNo": "CS102", "credit": 3.0, "teacherId": 2, "semester": "2024-2025-1" }
     *
     * 注意：新增课程时需要指定 teacherId（授课教师），这是关联外键。
     */
    @Operation(summary = "新增课程") // Swagger 说明
    @PostMapping // POST /api/courses
    @PreAuthorize("hasRole('ADMIN')") // 仅管理员可新增

    /*
     * public Result<Void> save(@RequestBody CourseDTO dto)
     *
     *   @RequestBody —— 将 HTTP 请求体的 JSON 字符串自动转成 CourseDTO 对象
     *                  Jackson 库负责 JSON ↔ Java 对象的互转
     *
     *   Result<Void> —— Void 表示不返回业务数据，只返回操作状态
     */
    public Result<Void> save(@RequestBody CourseDTO dto) {
        courseService.save(dto); // INSERT INTO course ...
        return Result.ok(null);  // 成功，无数据返回
    }

    // ================================================================
    // 接口 4：修改课程信息
    // ================================================================

    /*
     * 请求示例：PUT /api/courses
     * 请求体：{ "id": 1, "name": "Java 高级程序设计", "credit": 4.0, ... }
     *
     * PUT 和 POST 的区别：
     *   POST /api/courses     → 新增（请求体不含 id）
     *   PUT /api/courses      → 修改（请求体含 id，标识要改哪条记录）
     */
    @Operation(summary = "修改课程信息") // Swagger 说明
    @PutMapping // PUT /api/courses
    @PreAuthorize("hasRole('ADMIN')") // 仅管理员可修改
    public Result<Void> update(@RequestBody CourseDTO dto) {
        courseService.update(dto); // UPDATE course SET ...
        return Result.ok(null);
    }

    // ================================================================
    // 接口 5：删除课程
    // ================================================================

    /*
     * 请求示例：DELETE /api/courses/1
     *
     * 用 DELETE 方法删除，id 在 URL 路径中。
     * 删除课程前，Service 通常要检查是否有学生选了这门课。
     */
    @Operation(summary = "删除课程") // Swagger 说明
    @DeleteMapping("/{id}") // DELETE /api/courses/{id}
    @PreAuthorize("hasRole('ADMIN')") // 仅管理员可删除
    public Result<Void> delete(@PathVariable Long id) {
        courseService.delete(id); // DELETE FROM course WHERE id = ?
        return Result.ok(null);
    }

    // ================================================================
    // 接口 6：根据教师 ID 查询该教师的课程
    // ================================================================

    /*
     * ================================================================
     * 接口 6：根据教师查询课程
     *
     * 请求示例：GET /api/courses/teacher/1   （查询教师 ID=1 的所有课程）
     *
     * 这个接口是课程管理特有的：
     *   - 教师登录后，想查看"我教的课有哪些"
     *   - 管理员管理课程时，按教师筛选课程
     *
     * 为什么路径是 /teacher/{teacherId} 而不是 ?teacherId=1？
     *   RESTful 风格推荐：资源标识放在路径中，过滤条件放在查询参数中。
     *   teacherId 标识了"某个教师的课程"，是资源路径的一部分。
     * ================================================================
     */
    @Operation(summary = "根据教师ID查询课程") // Swagger 说明
    @GetMapping("/teacher/{teacherId}") // GET /api/courses/teacher/{teacherId}
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')") // 管理员和教师可访问
    /*
     * 学生不需要按教师查询课程（学生看的是全部课程列表），所以只开放给 ADMIN 和 TEACHER。
     */
    public Result<?> getByTeacherId(@PathVariable Long teacherId) {

        /*
         * courseService.getByTeacherId(teacherId)
         *   → SELECT * FROM course WHERE teacher_id = ?
         *   → 返回该教师教授的所有课程列表
         */
        return Result.ok(courseService.getByTeacherId(teacherId)); // 返回教师课程列表
    }

    /*
     * ================================================================
     * 总结：CourseController 的职责
     *
     * 1. 提供课程的 CRUD 接口（增删改查）
     * 2. 提供按教师查询课程的特殊接口
     * 3. 权限分配：ADMIN 全权限，TEACHER 可查看+按教师查询，STUDENT 仅可查看
     *
     * 和 StudentController 的最大不同：
     *   - 多了一个"按教师查询课程"的关联查询接口
     *   - 学生角色也能访问课程列表（因为学生需要选课）
     *   - 课程表和教师表有关联（teacher_id 外键）
     *
     * 文件对照表：
     *   Controller → CourseController.java       ← 当前文件
     *   Service    → CourseServiceImpl.java       （业务逻辑）
     *   Mapper     → CourseMapper.java            （数据库操作）
     *   DTO        → CourseDTO.java               （接收前端 JSON）
     *   DTO        → CourseQueryDTO.java          （接收查询条件）
     *   VO         → CourseVO.java                （返回前端数据）
     *   Entity     → Course.java                  （数据库表映射）
     * ================================================================
     */
}
