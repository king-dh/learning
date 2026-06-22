/*
 * ================================================================
 * 文件位置:  src/main/java/com/example/backend/service/EnrollmentService.java
 * 架构层级:  Service 接口（业务逻辑层的"合同/规范"）
 * 实现类:    src/main/java/com/example/backend/service/impl/EnrollmentServiceImpl.java
 * 被调用者:  src/main/java/com/example/backend/controller/EnrollmentController.java
 *
 * 调用链路（以"选课"为例）:
 * 前端选课页面点"选课"按钮 → axios 发 POST /api/enrollments/enroll
 *                        → EnrollmentController.enroll()
 *                        → EnrollmentService.enroll()（本接口）
 *                        → EnrollmentServiceImpl.enroll()（实现类）
 *                        → 检查学生/课程是否存在 → 检查是否重复选课
 *                        → INSERT INTO course_enrollment
 *                        → 返回成功
 *
 * 为什么选课需要独立的 Service？
 *   "选课"是一种"关系"操作：把学生和课程关联起来。
 *   它不是单纯的学生 CRUD，也不是课程 CRUD，而是一个跨实体的业务操作。
 *   这种"关联操作"在数据库里对应的是中间表（course_enrollment），
 *   在代码里对应独立的 Service。
 *
 *   JS 类比（MERN 栈）：
 *     // EnrollmentService = 处理 Student 和 Course 之间"多对多"关系的模块
 *     async function enroll(studentId, courseId) {
 *       // 检查学生存在、课程存在、不重复
 *       await Enrollment.create({ studentId, courseId })
 *     }
 * ================================================================
 */

// ==================== 1. 包声明 ====================
package com.example.backend.service; // 声明当前文件属于 service 子包

// ==================== 2. 导入其他类（import） ====================

// --- DTO（接收前端请求数据）---
import com.example.backend.dto.EnrollmentDTO; // 选课请求 DTO：{ studentId, courseId }

// --- VO（返回给前端的数据）---
import com.example.backend.vo.EnrollmentVO; // 选课视图对象（含课程名、教师名、学分等）

// Java 标准库
import java.util.List; // 列表接口

// ==================== 3. 接口声明 ====================

/*
 * public interface EnrollmentService { ... }
 *
 *   这个接口定义了 5 个方法，涵盖选课业务的所有操作：
 *     - getByUsername：学生登录后看自己的选课列表
 *     - getStudentIdByUsername：通过登录用户名找到对应的学生 ID
 *     - getByStudentId：查询某学生的所有选课记录
 *     - enroll：选课（核心业务）
 *     - unenroll：退课
 *
 *   注意：这里没有传统的 CRUD（增删改查分页），
 *   因为选课业务本身就是针对 course_enrollment 中间表的操作，
 *   业务语义更丰富，不是简单的增删改查。
 */
public interface EnrollmentService {

    /*
     * ================================================================
     * 方法 1：根据登录用户名查询该用户的选课列表
     *
     * List<EnrollmentVO> getByUsername(String username);
     *
     *   这个方法是为"我的选课"页面服务的。
     *   用户登录后，前端拿到 Token，Token 里有 username，
     *   然后调用这个接口获取该用户（作为学生）已选的所有课程。
     *
     *   数据流转：
     *     username = "student1"
     *          → 查 sys_user 表找到 realName
     *          → 用 realName 匹配 student 表的 name 字段找到 studentId
     *          → 用 studentId 查 course_enrollment 表（联查 course 和 teacher）
     *          → 返回 List<EnrollmentVO>
     *
     *   为什么要这么绕？
     *     因为登录用的是 sys_user 表（通用用户表），
     *     选课关联的是 student 表（具体的学生信息表），
     *     两者通过 realName / name 字段关联。这是一种"业务关联"而非外键关联。
     *
     *   JS 类比：
     *     async function getByUsername(username) {
     *       const user = await SysUser.findOne({ username })
     *       const student = await Student.findOne({ name: user.realName })
     *       return await Enrollment.find({ studentId: student.id }).populate('course')
     *     }
     */
    List<EnrollmentVO> getByUsername(String username);

    /*
     * ================================================================
     * 方法 2：根据登录用户名获取对应的学生 ID
     *
     * Long getStudentIdByUsername(String username);
     *
     *   拆解解释：
     *     Long      —— 返回值：学生主键 ID（可能是 null）
     *     username  —— 参数：登录用户名
     *
     *   这个方法是辅助方法，用于一些只需 studentId 的场景（比如前端需要 studentId 来发其他请求）。
     *   如果未找到对应的学生，返回 null 或抛异常（取决于实现）。
     */
    Long getStudentIdByUsername(String username);

    /*
     * ================================================================
     * 方法 3：根据学生 ID 查询该学生的所有选课记录
     *
     * List<EnrollmentVO> getByStudentId(Long studentId);
     *
     *   返回的每个 EnrollmentVO 包含：
     *     - 选课记录 ID
     *     - 学生姓名
     *     - 课程名称
     *     - 授课教师姓名
     *     - 课程学分
     *     - 学期等
     *
     *   这个方法是联表查询的结果（course_enrollment JOIN course JOIN teacher），
     *   不是简单查 course_enrollment 表。
     */
    List<EnrollmentVO> getByStudentId(Long studentId);

    /*
     * ================================================================
     * 方法 4：选课（核心业务）
     *
     * void enroll(EnrollmentDTO dto);
     *
     *   业务规则：
     *     1. 学生必须存在
     *     2. 课程必须存在
     *     3. 同一学生不能重复选同一门课（数据库层面也可以加唯一索引防重）
     *
     *   数据流转：
     *     前端传 → { studentId: 1, courseId: 5 }
     *           → 查 student 表确认学生存在
     *           → 查 course 表确认课程存在
     *           → 查 course_enrollment 表确认未重复选课
     *           → INSERT INTO course_enrollment ...
     *           → 返回成功
     *
     *   JS 类比：
     *     async function enroll(dto) {
     *       const student = await Student.findById(dto.studentId)
     *       if (!student) throw new Error('学生不存在')
     *       const course = await Course.findById(dto.courseId)
     *       if (!course) throw new Error('课程不存在')
     *       const exists = await Enrollment.findOne({ studentId, courseId })
     *       if (exists) throw new Error('已选过该课程')
     *       await Enrollment.create(dto)
     *     }
     */
    void enroll(EnrollmentDTO dto);

    /*
     * ================================================================
     * 方法 5：退课
     *
     * void unenroll(Long id);
     *
     *   void   —— 返回值：无
     *   unenroll —— 方法名：退课（取消选课）
     *   Long id —— 参数：选课记录的主键 ID（不是学生 ID 也不是课程 ID）
     *
     *   注意区分：
     *     - enroll 的参数是 EnrollmentDTO（包含 studentId + courseId）
     *     - unenroll 的参数是选课记录 ID（course_enrollment 表的主键）
     *     为什么不同？因为选课时还没有选课记录，用学生+课程来定位；
     *                 退课时已经有选课记录，直接用记录 ID 删除即可。
     *
     *   JS 类比：
     *     async function unenroll(id) {
     *       const record = await Enrollment.findById(id)
     *       if (!record) throw new Error('选课记录不存在')
     *       await Enrollment.deleteById(id)
     *     }
     */
    void unenroll(Long id);
}
