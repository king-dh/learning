/*
 * ================================================================
 * 文件位置:  src/main/java/com/example/backend/controller/StudentController.java
 * 对应前端:  frontend/src/views/student/StudentList.vue（学生管理页面）
 *           frontend/src/api/student.js（前端 API 调用）
 *
 * 调用链路（以"分页查询学生"为例）:
 * 前端点"学生管理"页面 → axios 发 GET /api/students/page?pageNum=1
 *                     → Vite 代理转发到 localhost:8088
 *                     → 本 Controller 的 page() 方法
 *                     → StudentServiceImpl.pageQuery()
 *                     → MyBatis 查数据库
 *                     → 返回 JSON 给前端
 * ================================================================
 */

// ==================== 1. 包声明 ====================

/*
 * package 关键字：声明当前文件属于哪个"包"（相当于 JavaScript 的文件夹/命名空间）。
 *
 * 这个名字不是随便写的，它必须和文件在硬盘上的路径完全对应：
 *   com.example.backend.controller
 *   └── com/example/backend/controller/   ← 文件夹路径（. 换成 /）
 *
 * "com.example.backend" 这个基础包名来自哪里？
 *   → 在创建项目时，pom.xml 里配置了:
 *       <groupId>com.example</groupId>   ← 组织名
 *       <artifactId>backend</artifactId> ← 项目名
 *   → Spring Initializr 自动把 ${groupId}.${artifactId} 拼成基础包名
 *   → 启动类 BackendApplication.java 放在这个基础包下，作为项目根目录
 *   → 所有子包（controller/service/mapper/dto/vo/common）都在基础包之下
 */
package com.example.backend.controller; // 声明当前文件属于 controller 子包

// ==================== 2. 导入其他类（import） ====================

/*
 * import 关键字：告诉编译器"我要用别的文件里的类"。
 * 和 JavaScript 的 import { xxx } from './xxx' 完全一样的概念。
 * 区别：Java 的 import 用的是完整包路径，不是相对路径。
 *
 * 例如下面这行：
 *   import com.example.backend.common.Result;
 * 意思是：
 *   我要用 src/main/java/com/example/backend/common/Result.java 这个文件里定义的 Result 类。
 */

// --- 2.1 本项目内部的类 ---
import com.example.backend.common.Result;      // 统一响应结果包装类（{ code: 200, message: "成功", data: {...} }）
import com.example.backend.dto.StudentDTO;     // 学生新增/修改时，前端传过来的 JSON 对应的接收类
import com.example.backend.dto.StudentQueryDTO; // 学生分页查询时，前端传的查询条件（姓名、学号、页码...）
import com.example.backend.service.StudentService; // 学生服务接口（本 Controller 不直接操作数据库，而是调 Service）
import com.example.backend.vo.StudentVO;       // 学生视图对象（返回给前端的数据结构，比数据库多一个 className 字段）

// --- 2.2 SpringDoc 的注解（用来生成 Swagger 接口文档）---
import io.swagger.v3.oas.annotations.Operation; // @Operation：给每个接口写说明文字（出现在 http://localhost:8088/swagger-ui.html）
import io.swagger.v3.oas.annotations.tags.Tag;   // @Tag：给整个 Controller 分组命名

// --- 2.3 Lombok 注解（减少样板代码）---
import lombok.RequiredArgsConstructor; // @RequiredArgsConstructor：自动生成带 final 字段的构造函数

// --- 2.4 Spring Security 注解（权限控制）---
import org.springframework.security.access.prepost.PreAuthorize; // @PreAuthorize：方法级别的权限控制，等价于前端的路由守卫

// --- 2.5 Spring Web 注解（定义 REST 接口）---
// 这些注解来自 Spring Boot 内置的 spring-web 依赖（自动引入，无需手动安装）
import org.springframework.web.bind.annotation.DeleteMapping;  // @DeleteMapping：对应 HTTP DELETE 请求
import org.springframework.web.bind.annotation.GetMapping;     // @GetMapping：对应 HTTP GET 请求
import org.springframework.web.bind.annotation.PathVariable;   // @PathVariable：提取 URL 路径中的变量，如 /api/students/{id} 里的 id
import org.springframework.web.bind.annotation.PostMapping;    // @PostMapping：对应 HTTP POST 请求
import org.springframework.web.bind.annotation.PutMapping;     // @PutMapping：对应 HTTP PUT 请求
import org.springframework.web.bind.annotation.RequestBody;    // @RequestBody：把请求体中的 JSON 自动转换成 Java 对象
import org.springframework.web.bind.annotation.RequestMapping; // @RequestMapping：给整个 Controller 所有接口统一添加路径前缀
import org.springframework.web.bind.annotation.RestController; // @RestController：标识这是一个 REST 控制器（返回 JSON，不是 HTML 页面）

// ==================== 3. 类的声明和注解 ====================

/*
 * @RestController 注解：
 *   告诉 Spring："这个类的每个方法返回值都要转成 JSON 返回给前端，而不是跳转 HTML 页面"。
 *   等价于同时加了 @Controller + @ResponseBody 两个注解。
 *
 *   类比 JavaScript：
 *     app.get('/api/students', (req, res) => { res.json({ data: ... }) })
 *                                  ↑ 返回 JSON，不返回 HTML  ↑
 */
@RestController

/*
 * @RequestMapping("/api/students")：
 *   给这个 Controller 里所有接口的 URL 统一加上 /api/students 前缀。
 *
 *   例如：
 *     page() 方法上有 @GetMapping("/page")
 *     最终访问路径 = @RequestMapping 前缀 + @GetMapping 路径 = /api/students/page
 *
 *   类比 JavaScript Express：
 *     const router = express.Router()
 *     app.use('/api/students', router)         ← @RequestMapping("/api/students")
 *     router.get('/page', (req, res) => {...}) ← @GetMapping("/page")
 */
@RequestMapping("/api/students")

/*
 * @RequiredArgsConstructor：
 *   Lombok 注解，编译时会自动生成一个构造函数，参数是所有带 final 修饰的字段。
 *
 *   本类中只有 studentService 是 final 的，所以等价于手写：
 *     public StudentController(StudentService studentService) {
 *         this.studentService = studentService;
 *     }
 *
 *   为什么要用构造函数？
 *     Spring 创建这个 Controller 实例时，发现构造函数需要 StudentService 参数，
 *     就会自动从容器里找到 StudentService 的实现类，注入进来。
 *     这就是"依赖注入"（Dependency Injection），简称 DI。
 *
 *   类比 JavaScript 中：
 *     // 不用 DI（自己 new，紧耦合）
 *     const service = new StudentService()
 *     // 用 DI（构造函数注入，松耦合）
 *     function StudentController(service) { this.service = service }
 *     const controller = new StudentController(injectedService) // 由框架自动注入
 */
@RequiredArgsConstructor

/*
 * @Tag(name = "学生管理", description = "学生信息增删改查")：
 *   SpringDoc 注解，在 Swagger UI 页面上给这组接口起个分组名。
 *   打开 http://localhost:8088/swagger-ui.html 就能看到"学生管理"这个分组。
 */
@Tag(name = "学生管理", description = "学生信息增删改查")

/*
 * public class StudentController { ... }
 *
 *   public：这个类可以被任何其他类使用（Java 的访问修饰符）
 *            类比 JS 的 "export class"
 *   class：定义类的关键字
 *   StudentController：类名，必须和文件名完全一致（Java 的硬性规定）
 *
 *   { ... } 里面的内容：
 *     字段（成员变量）+ 方法（成员函数）= 类的成员
 */
public class StudentController {

    // ==================== 4. 字段声明 ====================

    /*
     * private final StudentService studentService;
     *
     *   分解解释：
     *     private       —— 这个字段只能在本类内部访问，外部拿不到（封装）
     *     final         —— 这个字段一旦赋值就不能再改了（类似 JS 的 const）
     *                      final 字段必须在构造函数里赋值，Spring 通过 @RequiredArgsConstructor 自动完成
     *     StudentService—— 字段类型是 StudentService 接口（面向接口编程，不是具体实现类）
     *     studentService—— 字段名，驼峰命名（首字母小写）
     *
     *   Spring 的依赖注入过程：
     *     1. Spring 启动时扫描到 @RequiredArgsConstructor 注解
     *     2. 发现 studentService 是 final 的，需要在构造函数里赋值
     *     3. 自动生成构造函数 StudentController(StudentService studentService)
     *     4. Spring 从容器中找到 StudentService 接口的唯一实现类 StudentServiceImpl
     *     5. 把 StudentServiceImpl 的实例通过构造函数传进来
     *     6. this.studentService = studentService（Spring 自动完成）
     *
     *   类比 JavaScript：
     *     class StudentController {
     *       #studentService; // private field
     *       constructor(service) { this.#studentService = service } // final = 只在这里赋值
     *     }
     */
    private final StudentService studentService; // 学生服务（Spring 自动注入，Controller 靠它干活）

    // ==================== 5. 接口方法 ====================

    /*
     * ================================================================
     * 接口 1：分页查询学生列表
     *
     * 请求示例：GET /api/students/page?pageNum=1&pageSize=10&name=张
     * 前端调用：studentApi.getPage({ pageNum: 1, pageSize: 10, name: '张' })
     *
     * 数据流转：
     *   前端 URL 参数 ?pageNum=1&pageSize=10&name=张
     *     ↓ Spring 自动把 URL 参数绑定到 StudentQueryDTO 对象的对应字段
     *     ↓ (请求参数名和 DTO 字段名相同时自动映射，无需手动取值)
     *   本方法收到 StudentQueryDTO dto = { pageNum: 1, pageSize: 10, name: "张" }
     *     ↓ 调用 service 层
     *   studentService.pageQuery(dto)
     *     ↓ service 内部调 MyBatis 查数据库
     *     ↓ 返回 IPage<StudentVO>（分页结果，包含记录列表 + 总条数 + 当前页）
     *   Result.ok(...) 包装成统一格式
     *     ↓
     *   前端收到：{ code: 200, message: "操作成功", data: { records: [...], total: 20, page: 1, ... } }
     * ================================================================
     */

    /*
     * @Operation(summary = "分页查询学生")
     *   SpringDoc 注解：在 Swagger 文档页面上显示"分页查询学生"这个说明。
     */
    @Operation(summary = "分页查询学生")

    /*
     * @GetMapping("/page")
     *   - 告诉 Spring：这个方法是用来处理 HTTP GET 请求的
     *   - "/page" 是子路径，和类上的 @RequestMapping("/api/students") 拼在一起
     *     最终完整 URL = /api/students/page
     *   - 等同 JavaScript 的：router.get('/page', handler)
     */
    @GetMapping("/page")

    /*
     * @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
     *   Spring Security 方法级权限控制（等价于前端路由守卫）：
     *   - 只有登录用户拥有 ADMIN 或 TEACHER 角色时，才能访问这个接口
     *   - 如果是 STUDENT 角色或未登录，返回 403 Forbidden
     *   - hasAnyRole 里的字符串会自动匹配 ROLE_ 前缀，即匹配 ROLE_ADMIN / ROLE_TEACHER
     *   - 这是 AOP（面向切面编程）的典型应用：在执行方法前自动检查权限
     *
     *   类比 Vue Router：
     *     router.beforeEach((to, from, next) => {
     *       if (userStore.hasRole('ADMIN') || userStore.hasRole('TEACHER')) next()
     *       else next('/403')
     *     })
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")

    /*
     * public Result<?> page(StudentQueryDTO dto) { ... }
     *
     *   public       —— 公开方法（其他类可以调用）
     *   Result<?>    —— 返回值类型，? 是通配符，意思是"不知道具体类型"（这里实际返回的是分页结果）
     *   page         —— 方法名
     *   StudentQueryDTO dto —— 方法参数
     *                      Spring 会自动把 URL 中的 ?pageNum=1&pageSize=10&name=张
     *                      绑定到 StudentQueryDTO 对象的对应字段上（字段名必须和参数名一致）
     *
     *   注意：Spring 不需要 @RequestParam 注解来逐个接收参数，
     *   只要 DTO 对象里的字段名和 URL 参数名一致，就会自动绑定。
     */
    public Result<?> page(StudentQueryDTO dto) { // 参数自动绑定（URL 参数名 → DTO 字段名）

        /*
         * return Result.ok(studentService.pageQuery(dto));
         *
         *   拆解执行顺序（从内到外）：
         *     1. studentService.pageQuery(dto)
         *        调用 Service 层的分页查询方法，参数是 dto（包含 pageNum/pageSize/name）
         *        返回一个 IPage<StudentVO> 对象（MyBatis-Plus 的分页结果）
         *
         *     2. Result.ok(...)
         *        调用 Result 类的静态方法 ok()，把分页结果包一层统一格式
         *        内部逻辑：new Result() → setCode(200) → setMessage("操作成功") → setData(分页数据)
         *        返回 Result 对象 => { code: 200, message: "操作成功", data: { records: [...], total: 20 } }
         *
         *     3. return
         *        把这个 Result 对象返回给 Spring 框架
         *        Spring 的 @RestController 会把它序列化成 JSON 字符串，放进 HTTP 响应体
         *        最终前端 axios 收到的就是 { code: 200, message: "操作成功", data: {...} }
         *
         *   类比 JavaScript：
         *     async function page(req, res) {
         *       const result = await studentService.pageQuery(req.query)
         *       res.json({ code: 200, message: '成功', data: result })
         *     }
         */
        return Result.ok(studentService.pageQuery(dto)); // 查数据库 → 包装格式 → 返回 JSON
    }

    // ================================================================
    // 接口 2：根据 ID 查询单个学生
    // ================================================================

    /*
     * 请求示例：GET /api/students/5
     * 前端调用：studentApi.getById(5)
     * 返回：单个学生信息和所属班级名称
     */
    @Operation(summary = "根据ID查询学生")
    @GetMapping("/{id}") // {id} 是路径占位符，实际请求 /api/students/5 时，5 就是 id 的值
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")

    /*
     * public Result<StudentVO> getById(@PathVariable Long id)
     *
     *   Result<StudentVO>  —— 返回值泛型指定为 StudentVO，不再是通配符 ?
     *   getById            —— 方法名
     *
     *   @PathVariable Long id：
     *     @PathVariable   —— 告诉 Spring："这个参数的值从 URL 路径中取"
     *     Long            —— Java 的 64 位整数类型（对应于数据库的 BIGINT）
     *     id              —— 参数名，必须和 @GetMapping("/{id}") 里的 {id} 名字一致
     *
     *   例如请求 GET /api/students/5：
     *     Spring 自动提取 URL 中的 5，转成 Long 类型，赋值给参数 id
     *
     *   类比 JavaScript Express：
     *     router.get('/:id', (req, res) => {
     *       const id = req.params.id // ← @PathVariable Long id
     *     })
     */
    public Result<StudentVO> getById(@PathVariable Long id) {
        /*
         * studentService.getById(id)
         *   查数据库：SELECT s.*, c.name as className FROM student s LEFT JOIN class_info c ...
         *   返回 StudentVO 对象（包含学生信息 + 班级名称）
         */
        return Result.ok(studentService.getById(id));
    }

    // ================================================================
    // 接口 3：新增学生
    // ================================================================

    /*
     * 请求示例：POST /api/students
     * 请求体 JSON：{ "name": "新同学", "studentNo": "S2024001", "gender": "男", "age": 20, "classId": 1 }
     * 前端调用：studentApi.create({ name: '新同学', ... })
     */
    @Operation(summary = "新增学生")
    @PostMapping // 没有写路径，说明路径就是类上的 /api/students
    @PreAuthorize("hasRole('ADMIN')") // hasRole 匹配单个角色，区别于上面的 hasAnyRole 匹配多个

    /*
     * public Result<Void> save(@RequestBody StudentDTO dto)
     *
     *   Result<Void>  —— 新增成功无需返回数据，用 Void（Java 的 void 大写，表示没有返回值）
     *   save          —— 方法名
     *
     *   @RequestBody StudentDTO dto：
     *     @RequestBody   —— 告诉 Spring："这个参数的值从 HTTP 请求体中取，并且是 JSON 格式"
     *     StudentDTO     —— Spring 用 Jackson 库把请求体 JSON 自动转成 StudentDTO 对象
     *                       JSON: { "name": "张三", "age": 20 }
     *                         ↓ Jackson 自动转换
     *                       Java: StudentDTO { name="张三", age=20 }
     *     dto            —— 参数名（DTO = Data Transfer Object，专门用来接收前端数据的对象）
     *
     *   类比 JavaScript Express：
     *     router.post('/', (req, res) => {
     *       const dto = req.body // ← @RequestBody StudentDTO dto
     *       // JS 不需要类型声明，Java 必须明确告诉框架"把这个 JSON 转成 StudentDTO 类型"
     *     })
     */
    public Result<Void> save(@RequestBody StudentDTO dto) {
        /*
         * studentService.save(dto);
         *   调用 Service 层保存学生数据到数据库
         *   Void 表示"我不关心返回值"，只关心是否发生异常
         *   如果过程中抛异常，会被全局异常处理器 GlobalExceptionHandler 捕获并返回错误 JSON
         */
        studentService.save(dto); // INSERT INTO student ...
        return Result.ok(null); // 返回成功（无数据，所以 data 传 null）
    }

    // ================================================================
    // 接口 4：修改学生信息
    // ================================================================

    /*
     * 请求示例：PUT /api/students
     * 请求体 JSON：{ "id": 5, "name": "改名同学", "studentNo": "S2024001", ... }
     * 前端调用：studentApi.update({ id: 5, name: '改名同学', ... })
     *
     * 注：RESTful 规范中，修改用 PUT，新增用 POST，虽然我们这个项目两个都发 JSON 到请求体
     */
    @Operation(summary = "修改学生信息")
    @PutMapping // PUT 请求，对应 HTTP PUT 方法
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> update(@RequestBody StudentDTO dto) {
        studentService.update(dto); // UPDATE student SET ...
        return Result.ok(null);
    }

    // ================================================================
    // 接口 5：删除学生
    // ================================================================

    /*
     * 请求示例：DELETE /api/students/5
     * 前端调用：studentApi.remove(5)
     * 不需要请求体，id 直接写在 URL 路径里
     */
    @Operation(summary = "删除学生")
    @DeleteMapping("/{id}") // DELETE /api/students/{id}，{id} 会被 @PathVariable 提取
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> delete(@PathVariable Long id) { // 从 URL 中取 id，如 /api/students/5 → id=5
        studentService.delete(id); // DELETE FROM student WHERE id = ?
        return Result.ok(null);
    }

    /*
     * ================================================================
     * 总结：这个 Controller 类就是一个"接线员"
     *
     * 职责：接收 HTTP 请求 → 校验权限 → 调 Service → 包装返回值 → 返回 JSON
     *
     * 它不写任何业务逻辑（业务逻辑在 Service 层），不写任何 SQL（SQL 在 Mapper 层），
     * 只负责"接请求、叫 Service 干活、把结果包好送回去"。
     *
     * 文件对照表：
     *   Controller  →  StudentController.java    ← 当前文件（接线员）
     *   Service     →  StudentServiceImpl.java    （干活的人）
     *   Mapper      →  StudentMapper.java         （数据库操作）
     *   DTO         →  StudentDTO.java            （接收前端 JSON）
     *   VO          →  StudentVO.java             （返回给前端的 JSON）
     *   Entity      →  Student.java               （数据库表映射）
     * ================================================================
     */
}
