/*
 * ================================================================
 * 文件位置:  src/main/java/com/example/backend/controller/ClassController.java
 * 对应前端:  frontend/src/views/class/ClassList.vue（班级管理页面）
 *           frontend/src/api/class.js（前端 API 调用）
 *
 * 调用链路（以"分页查询班级"为例）:
 * 前端点"班级管理"页面 → axios 发 GET /api/classes/page?pageNum=1
 *                     → Vite 代理转发到 localhost:8088
 *                     → 本 Controller 的 page() 方法
 *                     → ClassServiceImpl.pageQuery()
 *                     → MyBatis-Plus 自动生成分页 SQL 查数据库
 *                     → 返回 JSON 给前端
 * ================================================================
 */

// ==================== 1. 包声明 ====================

/*
 * package 关键字：声明当前文件属于 controller 子包。
 * 完整路径：com.example.backend.controller。
 * 必须与硬盘目录 src/main/java/com/example/backend/controller/ 一一对应。
 */
package com.example.backend.controller; // 声明 Controller 包

// ==================== 2. 导入其他类（import） ====================

/*
 * import 关键字：告诉编译器"我要用这些类"。
 * 和 JavaScript 的 import { xxx } from './xxx' 概念完全相同。
 */

// --- 2.1 Common 层：统一响应 ---
import com.example.backend.common.Result;        // 统一响应结果包装类，所有接口都用它包装返回值

// --- 2.2 DTO 层：数据传输对象 ---
import com.example.backend.dto.ClassDTO;         // 班级新增/修改时，前端 JSON 对应的接收类
import com.example.backend.dto.ClassQueryDTO;     // 班级分页查询时，前端传的查询条件（班级名、年级、页码...）

// --- 2.3 Service 层：业务逻辑 ---
import com.example.backend.service.ClassService;  // 班级服务接口（Controller 不直接操作数据库，调 Service）

// --- 2.4 VO 层：视图对象 ---
import com.example.backend.vo.ClassVO;            // 班级视图对象（返回给前端的数据结构，含班主任姓名等关联信息）

// --- 2.5 SpringDoc 注解：API 文档 ---
import io.swagger.v3.oas.annotations.Operation;   // @Operation：给每个接口方法写说明文字
import io.swagger.v3.oas.annotations.tags.Tag;     // @Tag：给整个 Controller 分组命名

// --- 2.6 Lombok 注解：减少代码 ---
import lombok.RequiredArgsConstructor;            // @RequiredArgsConstructor：自动生成带 final 字段的构造函数

// --- 2.7 Spring Security 注解：权限控制 ---
import org.springframework.security.access.prepost.PreAuthorize; // @PreAuthorize：方法级权限控制（路由守卫）

// --- 2.8 Spring Web 注解：定义 REST 接口 ---
import org.springframework.web.bind.annotation.DeleteMapping;  // @DeleteMapping：对应 HTTP DELETE 请求
import org.springframework.web.bind.annotation.GetMapping;     // @GetMapping：对应 HTTP GET 请求
import org.springframework.web.bind.annotation.PathVariable;   // @PathVariable：提取 URL 路径中的变量
import org.springframework.web.bind.annotation.PostMapping;    // @PostMapping：对应 HTTP POST 请求
import org.springframework.web.bind.annotation.PutMapping;     // @PutMapping：对应 HTTP PUT 请求
import org.springframework.web.bind.annotation.RequestBody;    // @RequestBody：请求体 JSON → Java 对象
import org.springframework.web.bind.annotation.RequestMapping; // @RequestMapping：统一路径前缀
import org.springframework.web.bind.annotation.RestController; // @RestController：REST 控制器

// ==================== 3. 类的声明和注解 ====================

/*
 * @Tag(name = "班级管理", description = "班级信息增删改查")
 *   在 Swagger UI（http://localhost:8088/swagger-ui.html）中显示"班级管理"分组。
 */
@Tag(name = "班级管理", description = "班级信息增删改查") // Knife4j 标签

/*
 * @RestController：
 *   Spring 会将方法返回值自动序列化为 JSON，而不是去找 HTML 模板。
 *   等同于 @Controller + @ResponseBody。
 *
 *   类比 JavaScript Express：
 *     const router = express.Router()
 *     router.get('/', (req, res) => { res.json(data) })  ← 返回 JSON
 */
@RestController // REST 控制器

/*
 * @RequestMapping("/api/classes")：
 *   给这个 Controller 里所有接口的 URL 统一加上 /api/classes 前缀。
 *
 *   例如：page() 方法上有 @GetMapping("/page")，最终完整 URL = /api/classes/page
 *
 *   类比 JavaScript Express：
 *     app.use('/api/classes', router)
 */
@RequestMapping("/api/classes") // 路径前缀

/*
 * @RequiredArgsConstructor：
 *   Lombok 编译时自动生成构造函数，参数是所有 final 字段。
 *   本类中 classService 是 final，等价于：
 *     public ClassController(ClassService classService) { this.classService = classService; }
 *   Spring 创建 Controller 实例时，自动从容器中找到 ClassServiceImpl 并注入。
 */
@RequiredArgsConstructor // 构造器注入

/*
 * public class ClassController { ... }
 *
 *   public  —— 这个类可以被任何其他类访问（类似 JS 的 export）
 *   class   —— 声明类的关键字
 *   ClassController —— 类名，必须和文件名 ClassController.java 完全一致
 */
public class ClassController {

    // ==================== 4. 字段声明（成员变量） ====================

    /*
     * private final ClassService classService;
     *
     *   private       —— 封装：外部不能直接访问此字段
     *   final         —— 不可变：赋值后不能修改（类似 JS const）
     *   ClassService  —— 接口类型（面向接口编程，具体实现是 ClassServiceImpl）
     *   classService  —— 字段名（驼峰命名，首字母小写）
     *
     *   依赖注入过程：
     *     1. @RequiredArgsConstructor 发现 classService 是 final 字段
     *     2. 自动生成构造函数 ClassController(ClassService classService)
     *     3. Spring 从容器中找到 ClassService 的唯一实现类 ClassServiceImpl
     *     4. 将 ClassServiceImpl 实例注入进来
     */
    private final ClassService classService; // 注入班级服务（Spring 自动注入）

    // ==================== 5. 接口方法 ====================

    /*
     * ================================================================
     * 接口 1：分页查询班级列表
     *
     * 请求示例：GET /api/classes/page?pageNum=1&pageSize=10&className=软件
     * 前端调用：classApi.getPage({ pageNum: 1, pageSize: 10, className: '软件' })
     *
     * 数据流转：
     *   前端 URL 参数 ?pageNum=1&pageSize=10&className=软件
     *     ↓ Spring 自动把 URL 参数绑定到 ClassQueryDTO 对象的对应字段上
     *     ↓ 参数名必须和 DTO 字段名一致，否则自动绑定失败
     *   本方法收到 ClassQueryDTO dto = { pageNum: 1, pageSize: 10, className: "软件" }
     *     ↓ 调 Service 层
     *   classService.pageQuery(dto)
     *     ↓ MyBatis-Plus 分页插件拦截 SQL，自动加 LIMIT/OFFSET
     *     ↓ 返回 IPage<ClassVO>（包含 records/total/current/size 等分页信息）
     *   Result.ok(...)
     *     ↓ 包装成 { code: 200, message: "操作成功", data: { records: [...], total: 3 } }
     *   前端收到后渲染表格和分页组件
     * ================================================================
     */

    @Operation(summary = "分页查询班级") // Swagger 说明

    /*
     * @GetMapping("/page")
     *   HTTP GET 请求，完整 URL = /api/classes/page
     *   用 GET 是因为"查询"是幂等的、不产生副作用的操作
     */
    @GetMapping("/page") // GET /api/classes/page

    /*
     * @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
     *   Spring Security 方法级权限控制：
     *   - hasAnyRole 表示"拥有以下任意一个角色即可"
     *   - Spring Security 会自动在角色名前加 ROLE_ 前缀，即匹配 ROLE_ADMIN 或 ROLE_TEACHER
     *   - 不是这两个角色的用户访问会返回 403 Forbidden
     *
     *   类比 Vue Router 的路由守卫：
     *     router.beforeEach((to, from, next) => {
     *       const role = userStore.role
     *       if (role === 'ADMIN' || role === 'TEACHER') next()
     *       else next('/403')
     *     })
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')") // 管理员和教师可访问

    /*
     * public Result<?> page(ClassQueryDTO dto) { ... }
     *
     *   Result<?>      —— 返回值类型，? 是通配符（不确定 data 的具体类型）
     *   page           —— 方法名
     *   ClassQueryDTO dto —— 参数：Spring 自动将 URL 参数绑定到此对象
     *                        ClassQueryDTO 通常包含 pageNum、pageSize、className 等字段
     *
     *   注意：Spring 不需要 @RequestParam 逐个接收 URL 参数，
     *   只要 DTO 对象的字段名和 URL 参数名一致，就会自动绑定。
     *   这和 JavaScript 的 req.query 类似，但 Java 是强类型绑定。
     */
    public Result<?> page(ClassQueryDTO dto) { // 参数自动绑定（URL 参数名 → DTO 字段名）

        /*
         * return Result.ok(classService.pageQuery(dto));
         *
         *   执行顺序（从内到外）：
         *     1. classService.pageQuery(dto)
         *        → Service 调用 MyBatis-Plus 分页查询
         *        → 返回 IPage<ClassVO> 对象
         *     2. Result.ok(...)
         *        → 包装成统一格式：{ code: 200, message: "操作成功", data: 分页数据 }
         *     3. return
         *        → @RestController 将 Result 对象序列化为 JSON 字符串
         */
        return Result.ok(classService.pageQuery(dto)); // 查数据库 → 包装 → 返回 JSON
    }

    // ================================================================
    // 接口 2：根据 ID 查询班级详情
    // ================================================================

    /*
     * 请求示例：GET /api/classes/1
     * 前端调用：classApi.getById(1)
     * 响应：{ code: 200, data: { id: 1, className: "软件工程1班", headTeacherName: "张教授", ... } }
     */
    @Operation(summary = "根据ID查询班级") // Swagger 说明

    /*
     * @GetMapping("/{id}")
     *   {id} 是路径占位符，Spring 会从 URL 中提取实际值。
     *   例如请求 /api/classes/1 → id = 1
     *
     *   类比 JavaScript Express：
     *     router.get('/:id', (req, res) => { const id = req.params.id })
     */
    @GetMapping("/{id}") // GET /api/classes/{id}
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')") // 管理员和教师可访问

    /*
     * public Result<ClassVO> getById(@PathVariable Long id)
     *
     *   @PathVariable Long id：
     *     @PathVariable —— 告诉 Spring："这个参数的值从 URL 路径中取"
     *     Long          —— Java 的 64 位整数（对应数据库 BIGINT）
     *     id            —— 参数名，必须和 @GetMapping("/{id}") 里的 {id} 一致！
     */
    public Result<ClassVO> getById(@PathVariable Long id) {

        /*
         * classService.getById(id)
         *   → SELECT * FROM class_info WHERE id = ?（MyBatis-Plus 自动生成）
         *   → LEFT JOIN teacher ON class_info.head_teacher_id = teacher.id
         *   → 返回 ClassVO（含班级信息 + 班主任姓名）
         */
        return Result.ok(classService.getById(id)); // 查询并返回班级详情
    }

    // ================================================================
    // 接口 3：新增班级
    // ================================================================

    /*
     * 请求示例：POST /api/classes
     * 请求体：{ "className": "软件工程2班", "grade": "2024级", "headTeacherId": 1 }
     * 前端调用：classApi.create({ className: '软件工程2班', ... })
     */
    @Operation(summary = "新增班级") // Swagger 说明

    /*
     * @PostMapping
     *   没有写子路径，说明路径就是类上的 @RequestMapping("/api/classes")
     *   POST 请求通常用于"新增"操作，HTTP 方法语义与 CRUD 对应。
     */
    @PostMapping // POST /api/classes
    @PreAuthorize("hasRole('ADMIN')") // 仅管理员可新增

    /*
     * public Result<Void> save(@RequestBody ClassDTO dto)
     *
     *   Result<Void>          —— 新增成功无需返回数据，Void 表示空
     *   @RequestBody          —— 从 HTTP 请求体取 JSON → Jackson 转 ClassDTO 对象
     *   ClassDTO dto          —— 接收前端传来的班级信息
     *
     *   类比 JavaScript：
     *     router.post('/', (req, res) => {
     *       const dto = req.body // ← @RequestBody ClassDTO dto
     *       classService.save(dto)
     *       res.json({ code: 200 })
     *     })
     */
    public Result<Void> save(@RequestBody ClassDTO dto) {

        /*
         * classService.save(dto)
         *   → INSERT INTO class_info (class_name, grade, head_teacher_id) VALUES (?, ?, ?)
         *   → 如果字段校验不通过（如班级名为空），Service 会抛异常
         *   → GlobalExceptionHandler 统一捕获后返回错误 JSON
         */
        classService.save(dto); // 调用服务保存

        /*
         * return Result.ok(null)
         *   null 表示没有业务数据要返回，只有"操作成功"的状态信息。
         *   前端看到 { code: 200, message: "操作成功" } 就知道新增成功了，刷新列表即可。
         */
        return Result.ok(null); // 返回成功
    }

    // ================================================================
    // 接口 4：修改班级信息
    // ================================================================

    /*
     * 请求示例：PUT /api/classes
     * 请求体：{ "id": 1, "className": "软件工程1班(改)", "grade": "2024级", "headTeacherId": 2 }
     *
     * RESTful 规范中，修改用 PUT，新增用 POST。
     * 两者在本项目中都发 JSON 到请求体，区别在于 PUT 的 JSON 中包含 id 字段。
     */
    @Operation(summary = "修改班级信息") // Swagger 说明
    @PutMapping // PUT /api/classes
    @PreAuthorize("hasRole('ADMIN')") // 仅管理员可修改

    /*
     * public Result<Void> update(@RequestBody ClassDTO dto)
     *
     *   和新增用同一个 DTO（ClassDTO），区别是修改时 dto.getId() != null。
     *   Service 层会根据 id 是否存在决定是 INSERT 还是 UPDATE。
     */
    public Result<Void> update(@RequestBody ClassDTO dto) {

        /*
         * classService.update(dto)
         *   → UPDATE class_info SET class_name=?, grade=?, head_teacher_id=? WHERE id=?
         */
        classService.update(dto); // 调用服务更新
        return Result.ok(null);    // 返回成功
    }

    // ================================================================
    // 接口 5：删除班级
    // ================================================================

    /*
     * 请求示例：DELETE /api/classes/1
     * 前端调用：classApi.remove(1)
     *
     * 用 DELETE 方法，id 直接写在 URL 路径中，不需要请求体。
     */
    @Operation(summary = "删除班级") // Swagger 说明
    @DeleteMapping("/{id}") // DELETE /api/classes/{id}
    @PreAuthorize("hasRole('ADMIN')") // 仅管理员可删除

    /*
     * public Result<Void> delete(@PathVariable Long id)
     *
     *   @PathVariable：从 URL 路径中提取 id 的值。
     *   如果班级下有学生，Service 通常会在删除前检查并拒绝删除。
     */
    public Result<Void> delete(@PathVariable Long id) {

        /*
         * classService.delete(id)
         *   → DELETE FROM class_info WHERE id = ?
         *   → 如果班级下有学生，可能抛出 BusinessException（有外键约束或业务校验）
         */
        classService.delete(id); // 调用服务删除
        return Result.ok(null);  // 返回成功
    }

    // ================================================================
    // 接口 6：查询班级下的学生列表
    // ================================================================

    /*
     * 请求示例：GET /api/classes/1/students
     * 前端调用：classApi.getStudents(1)
     *
     * 这个接口做的是"班级-学生"关联查询：先查班级存在，再查该班级下的所有学生。
     */
    @Operation(summary = "查询班级下的学生列表") // Swagger 说明
    @GetMapping("/{id}/students") // GET /api/classes/{id}/students
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')") // 管理员和教师可访问

    /*
     * public Result<?> getStudents(@PathVariable Long id)
     *
     *   返回该班级下所有学生的列表。
     *   ? 通配符是因为学生列表的返回类型可能是 List<StudentVO> 或其他的泛型集合。
     */
    public Result<?> getStudents(@PathVariable Long id) {

        /*
         * classService.getStudentsByClassId(id)
         *   → SELECT * FROM student WHERE class_id = ?
         *   → 可以额外 JOIN 其他表获取关联信息
         */
        return Result.ok(classService.getStudentsByClassId(id)); // 返回班级学生列表
    }

    /*
     * ================================================================
     * 总结：ClassController 的职责
     *
     * 1. 接收 HTTP 请求（GET/POST/PUT/DELETE）
     * 2. 用 @PreAuthorize 做权限检查（ADMIN 全权限，TEACHER 可查看）
     * 3. 调 ClassService 处理实际业务
     * 4. 用 Result 包装返回值保持统一格式
     *
     * 和 StudentController 的结构非常相似：
     *   - 都有 CRUD（增删改查）五个基本接口
     *   - ClassController 多了一个"查班级下学生"的关联查询接口
     *   - 权限控制基本一致：ADMIN 全权限，TEACHER 可查看
     *
     * 文件对照表：
     *   Controller  → ClassController.java       ← 当前文件（接线员）
     *   Service     → ClassServiceImpl.java       （业务逻辑）
     *   Mapper      → ClassInfoMapper.java        （数据库操作）
     *   DTO         → ClassDTO.java               （接收前端 JSON）
     *   DTO         → ClassQueryDTO.java          （接收查询条件）
     *   VO          → ClassVO.java                （返回前端数据）
     *   Entity      → ClassInfo.java              （数据库表映射）
     * ================================================================
     */
}
