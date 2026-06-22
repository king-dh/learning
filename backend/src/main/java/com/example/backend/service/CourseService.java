/*
 * ================================================================
 * 文件位置:  src/main/java/com/example/backend/service/CourseService.java
 * 架构层级:  Service 接口（业务逻辑层的"合同/规范"）
 * 实现类:    src/main/java/com/example/backend/service/impl/CourseServiceImpl.java
 * 被调用者:  src/main/java/com/example/backend/controller/CourseController.java
 *
 * 调用链路（以"分页查询课程"为例）:
 * 前端课程管理页面 → axios 发 GET /api/courses/page?pageNum=1
 *                 → CourseController.page()
 *                 → CourseService.pageQuery()（本接口）
 *                 → CourseServiceImpl.pageQuery()（实现类）
 *                 → MyBatis-Plus 查 course 表 → 联查 teacher 表获取教师姓名
 *                 → 返回分页 JSON 给前端
 *
 * 面向接口编程的好处：
 *   假设未来要加入"课程缓存"功能，只需在 CourseServiceImpl 的方法里加缓存逻辑，
 *   接口方法签名完全不变，Controller 也不需要任何改动。
 *   这就是"对接口编程，不对实现编程"的真正价值。
 *
 *   JS 类比：
 *     // TS 接口定义
 *     interface ICourseService {
 *       pageQuery(dto: CourseQueryDTO): IPage<CourseVO>
 *       getById(id: number): CourseVO
 *       save(dto: CourseDTO): void
 *       update(dto: CourseDTO): void
 *       delete(id: number): void
 *       getByTeacherId(teacherId: number): CourseVO[]
 *     }
 *     // 可以自由切换实现（数据库 / 缓存 / Mock）
 *     class CourseDbService implements ICourseService { ... }
 *     class CourseCacheService implements ICourseService { ... }
 *     class CourseMockService implements ICourseService { ... } // 单元测试用
 * ================================================================
 */

// ==================== 1. 包声明 ====================
package com.example.backend.service; // 声明当前文件属于 service 子包

// ==================== 2. 导入其他类（import） ====================

// MyBatis-Plus 分页结果接口
import com.baomidou.mybatisplus.core.metadata.IPage; // MyBatis-Plus 分页结果

// --- DTO（接收前端请求数据）---
import com.example.backend.dto.CourseDTO;      // 新增/修改课程时的请求数据
import com.example.backend.dto.CourseQueryDTO; // 分页查询时的请求参数

// --- VO（返回给前端的数据）---
import com.example.backend.vo.CourseVO; // 课程视图对象（含教师名称）

// Java 标准库的 List 接口（类似于 JS 的 Array）
import java.util.List; // 列表接口

// ==================== 3. 接口声明 ====================

/*
 * public interface CourseService { ... }
 *
 *   这个接口定义了课程相关的 6 个业务操作。
 *   比增删改查多了一个 getByTeacherId 方法，用于查询某教师教授的所有课程。
 *
 *   注意 interface 里的方法默认都是 public abstract 的，
 *   所以不需要写 public abstract 修饰符（写了也没错，但通常省略）。
 *   这与 class 不同，class 的方法默认是包级可见的。
 */
public interface CourseService {

    // ==================== 4. 方法签名 ====================

    /*
     * ================================================================
     * 方法 1：分页条件查询课程列表
     *
     * IPage<CourseVO> pageQuery(CourseQueryDTO dto);
     *
     *   返回的分页结果中，每个 CourseVO 都包含：
     *     - 课程基本信息（名称、编号、学分、学期等）
     *     - teacherName（授课教师姓名，通过联查 teacher 表获得）
     *
     *   查询条件支持：
     *     - name：课程名称模糊搜索
     *     - teacherId：按教师 ID 精确筛选
     *     - semester：按学期精确筛选
     */
    IPage<CourseVO> pageQuery(CourseQueryDTO dto);

    /*
     * ================================================================
     * 方法 2：根据 ID 查询课程详情
     *
     * CourseVO getById(Long id);
     *
     *   返回单个课程的完整信息，包含授课教师姓名。
     *   如果课程不存在，实现类抛出 BusinessException。
     */
    CourseVO getById(Long id);

    /*
     * ================================================================
     * 方法 3：新增课程
     *
     * void save(CourseDTO dto);
     *
     *   将前端传来的课程数据保存到 course 表。
     *   dto 包含：courseNo（课程编号）、name（课程名称）、credit（学分）、
     *             teacherId（教师ID）、semester（学期）等
     */
    void save(CourseDTO dto);

    /*
     * ================================================================
     * 方法 4：修改课程信息
     *
     * void update(CourseDTO dto);
     *
     *   按课程主键 id 更新课程信息。
     *   dto 中必须包含 id 字段以定位要修改的记录。
     */
    void update(CourseDTO dto);

    /*
     * ================================================================
     * 方法 5：删除课程
     *
     * void delete(Long id);
     *
     *   按主键 id 删除课程记录。
     */
    void delete(Long id);

    /*
     * ================================================================
     * 方法 6：根据教师 ID 查询该教师的所有课程
     *
     * List<CourseVO> getByTeacherId(Long teacherId);
     *
     *   List<CourseVO>  —— 返回值：课程视图对象列表（不是分页，是全部）
     *   getByTeacherId  —— 方法名：按教师 ID 查询课程
     *   Long teacherId   —— 参数：教师的主键 ID
     *
     *   这个方法是 CRUD 之外的特殊查询需求：
     *   前端可能需要展示"某教师教授的所有课程"，比如教师个人主页。
     *   返回的每个 CourseVO 都附带 teacherName。
     *
     *   JS 类比：
     *     async function getByTeacherId(teacherId) {
     *       const courses = await Course.find({ teacherId })
     *       return courses.map(c => ({ ...c, teacherName: '...' }))
     *     }
     */
    List<CourseVO> getByTeacherId(Long teacherId);
}
