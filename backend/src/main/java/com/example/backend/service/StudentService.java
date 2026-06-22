/*
 * ================================================================
 * 文件位置:  src/main/java/com/example/backend/service/StudentService.java
 * 架构层级:  Service 接口（业务逻辑层的"合同/规范"）
 * 实现类:    src/main/java/com/example/backend/service/impl/StudentServiceImpl.java
 * 被调用者:  src/main/java/com/example/backend/controller/StudentController.java
 *
 * 调用链路（以"分页查询学生"为例）:
 * 前端点"学生管理"页面 → axios 发 GET /api/students/page?pageNum=1
 *                     → StudentController.page()
 *                     → StudentService.pageQuery()（本接口）
 *                     → StudentServiceImpl.pageQuery()（实现类）
 *                     → MyBatis-Plus 查 student 表 → 联查 class_info 表
 *                     → 返回分页数据给前端
 *
 * 面向接口编程的本质：
 *   StudentController 只认识 StudentService 接口，
 *   不认识 StudentServiceImpl 实现类。
 *   比如你把 MyBatis-Plus 换成 JPA，只需新建 StudentServiceJpaImpl，
 *   Controller 一行不改，因为 Controller 只依赖接口。
 *
 *   JS 类比：
 *     // 在 TS 项目中：
 *     interface IStudentService {
 *       pageQuery(dto: StudentQueryDTO): IPage<StudentVO>;
 *       getById(id: number): StudentVO;
 *       save(dto: StudentDTO): void;
 *       update(dto: StudentDTO): void;
 *       delete(id: number): void;
 *     }
 *     // 使用时只引入接口类型，不关心具体实现
 *     constructor(private studentService: IStudentService) {}
 * ================================================================
 */

// ==================== 1. 包声明 ====================
package com.example.backend.service; // 声明当前文件属于 service 子包（业务逻辑层接口）

// ==================== 2. 导入其他类（import） ====================

/*
 * MyBatis-Plus 的分页结果类型。
 * IPage<T> 是一个泛型接口，T 是具体的数据类型。
 * IPage<StudentVO> 表示"包含 StudentVO 列表的分页对象"。
 * 分页对象里通常有：
 *   - records: 当前页的数据列表
 *   - total: 总记录数
 *   - current: 当前页码
 *   - size: 每页大小
 * 类比 JS 中 axios 返回的 { data: { records: [...], total: 50, current: 1, size: 10 } }
 */
import com.baomidou.mybatisplus.core.metadata.IPage; // MyBatis-Plus 分页结果接口

// --- DTO（Data Transfer Object）---
// 前端传过来的请求参数对应的 Java 类
import com.example.backend.dto.StudentDTO;      // 新增/修改学生时前端传的 JSON 对应的类
import com.example.backend.dto.StudentQueryDTO; // 分页查询时前端传的查询条件（姓名、学号、页码等）

// --- VO（View Object）---
// 返回给前端的视图对象
import com.example.backend.vo.StudentVO; // 学生视图对象（比数据库实体多一个 className 字段）

// ==================== 3. 接口声明 ====================

/*
 * public interface StudentService { ... }
 *
 *   这是一个"合同"：任何实现 StudentService 的类，都必须提供以下 5 个方法的具体实现。
 *   Spring 的依赖注入会根据这个接口类型，自动找到它的实现类（StudentServiceImpl）注入给调用者。
 *
 *   类比 JS：
 *     // 在 TS 中：
 *     export interface IStudentService {
 *       pageQuery(dto: StudentQueryDTO): IPage<StudentVO>;  // 分页查询
 *       getById(id: number): StudentVO;                     // 按 ID 查
 *       save(dto: StudentDTO): void;                        // 新增
 *       update(dto: StudentDTO): void;                      // 修改
 *       delete(id: number): void;                           // 删除
 *     }
 */
public interface StudentService {

    // ==================== 4. 方法签名 ====================

    /*
     * ================================================================
     * 方法 1：分页条件查询学生列表
     *
     * IPage<StudentVO> pageQuery(StudentQueryDTO dto);
     *
     *   拆解解释：
     *     IPage<StudentVO>  —— 返回值：MyBatis-Plus 的分页对象
     *                          泛型 <StudentVO> 指定分页里装的是学生视图对象
     *                          IPage 是一个接口，实际返回的是 Page 类的实例
     *     pageQuery         —— 方法名：分页查询
     *     StudentQueryDTO   —— 参数类型：包含 pageNum、pageSize、name、studentNo
     *     dto               —— 参数名
     *
     *   这个方法的职责：
     *     根据前端传来的查询条件（姓名模糊搜索、学号模糊搜索），
     *     返回匹配的学生分页结果，同时附带上每个学生所属的班级名称。
     *
     *   数据流转：
     *     前端传 → { pageNum: 1, pageSize: 10, name: "张" }
     *           → StudentServiceImpl 构建 LambdaQueryWrapper 条件
     *           → MyBatis-Plus 生成 SQL: SELECT * FROM student WHERE name LIKE '%张%' ORDER BY create_time DESC LIMIT 0,10
     *           → 返回 Page<Student> 对象
     *           → 逐个转为 StudentVO（查询 class_info 表填上 className）
     *           → 返回 IPage<StudentVO>
     */
    IPage<StudentVO> pageQuery(StudentQueryDTO dto);

    /*
     * ================================================================
     * 方法 2：根据 ID 查询单个学生详情
     *
     * StudentVO getById(Long id);
     *
     *   拆解解释：
     *     StudentVO  —— 返回值：单个学生的视图对象（含班级名称）
     *     getById    —— 方法名：按主键 ID 查询
     *     Long       —— 参数类型：64 位整数（数据库中 BIGINT 的 Java 对应类型）
     *     id         —— 参数名：学生的唯一标识
     *
     *   这个方法的职责：
     *     根据主键 ID 查询一条学生记录，并附带上班级名称。
     *     如果 ID 不存在，实现类应抛出异常。
     *
     *   数据流转：
     *     Controller 从 URL 中提取 id（如 /api/students/5 → id=5）
     *           → 调用 studentService.getById(5)
     *           → StudentServiceImpl 查数据库
     *           → 返回 StudentVO { id: 5, name: "张三", className: "计算机1班", ... }
     */
    StudentVO getById(Long id);

    /*
     * ================================================================
     * 方法 3：新增学生
     *
     * void save(StudentDTO dto);
     *
     *   拆解解释：
     *     void      —— 返回值：新增成功没有数据要返回
     *                 （如果失败，靠抛异常来通知 Controller）
     *     save      —— 方法名：保存数据
     *     StudentDTO —— 参数类型：包含 name、studentNo、gender、age、classId 等
     *
     *   这个方法的职责：
     *     1. 检查学号是否重复
     *     2. 把 DTO 转成实体对象
     *     3. 插入数据库
     *
     *   数据流转：
     *     前端 POST JSON → Spring 转成 StudentDTO
     *           → StudentServiceImpl 检查学号 → 复制属性到 Student 实体
     *           → INSERT INTO student (name, student_no, ...) VALUES (...)
     *           → 返回 void（成功），或抛异常（失败）
     *
     *   JS 类比：
     *     async function save(dto) {
     *       const existing = await Student.findOne({ studentNo: dto.studentNo })
     *       if (existing) throw new Error('学号已存在')
     *       await Student.create(dto)
     *     }
     */
    void save(StudentDTO dto);

    /*
     * ================================================================
     * 方法 4：修改学生信息
     *
     * void update(StudentDTO dto);
     *
     *   拆解解释：
     *     void      —— 返回值：修改成功无需返回数据
     *     update    —— 方法名：更新数据
     *     StudentDTO —— 参数：包含 id 和要修改的字段值
     *
     *   这个方法的职责：
     *     1. 检查学号是否与其他学生冲突
     *     2. 把 DTO 属性复制到实体
     *     3. 按主键 id 更新数据库记录
     *
     *   JS 类比：
     *     async function update(dto) {
     *       await Student.update({ id: dto.id }, dto)
     *     }
     */
    void update(StudentDTO dto);

    /*
     * ================================================================
     * 方法 5：删除学生
     *
     * void delete(Long id);
     *
     *   拆解解释：
     *     void   —— 返回值：删除成功无需返回数据
     *     delete —— 方法名
     *     Long   —— 参数类型：要删除的学生 ID
     *     id     —— 参数名
     *
     *   这个方法的职责：
     *     1. 检查是否有该学生的成绩记录（有则不允许删除）
     *     2. 检查是否有该学生的选课记录（有则不允许删除）
     *     3. 按主键删除学生记录
     *
     *   为什么删除前要做检查？
     *     如果直接把学生删了，他关联的成绩和选课数据就成了"孤儿数据"，
     *     会导致数据不一致。所以要么先清理关联数据，要么阻止删除。
     *
     *   JS 类比：
     *     async function delete(id) {
     *       const scores = await Score.find({ studentId: id })
     *       if (scores.length > 0) throw new Error('有关联成绩，无法删除')
     *       await Student.deleteById(id)
     *     }
     */
    void delete(Long id);
}
