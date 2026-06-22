/*
 * ================================================================
 * 文件位置:  src/main/java/com/example/backend/mapper/CourseEnrollmentMapper.java
 * 对应 Entity: CourseEnrollment.java
 * 对应数据库表: course_enrollment
 * 在架构中的位置:
 *
 *   Controller → Service → Mapper(当前文件) → 数据库 course_enrollment 表
 *
 * Mapper（数据访问接口）= course_enrollment（选课记录）表的数据库操作接口
 *
 * JS 类比：
 *   const CourseEnrollmentMapper = {
 *     // 以下来自 BaseMapper 自动获得：
 *     insert(enrollment) { ... },       // 新增选课记录
 *     selectById(id) { ... },           // 按ID查选课
 *     deleteById(id) { ... },           // 删除选课（退课）
 *
 *     // 以下是自定义：
 *     selectByStudentId(studentId) { ... }  // 按学生ID查询选课列表（联表查课程和教师信息）
 *   }
 *
 * 联表查询说明：
 *   course_enrollment 表只存了 studentId 和 courseId（两个外键数字），
 *   但前端选课列表需要显示：
 *     - 学生姓名（来自 student 表）
 *     - 课程名称（来自 course 表）
 *     - 教师姓名（来自 teacher 表，需要 course 表中转）
 *     - 课程学分、学期等（来自 course 表）
 *
 *   这个 Mapper 的 selectByStudentId 方法通过多层 LEFT JOIN 把所有信息一次性查出，
 *   返回 EnrollmentVO 视图对象供前端使用。
 *
 *   数据查询链路（4 张表联动）：
 *     course_enrollment(主表)
 *       → LEFT JOIN student （拿学生姓名）
 *       → LEFT JOIN course  （拿课程名称、学分、学期）
 *       → LEFT JOIN teacher （拿教师姓名，通过 course.teacher_id 桥接）
 * ================================================================
 */

// ==================== 1. 包声明 ====================
package com.example.backend.mapper; // mapper 子包（数据访问层）

// ==================== 2. 导入其他类 ====================

import com.baomidou.mybatisplus.core.mapper.BaseMapper;       // MyBatis-Plus 基础 Mapper（通用 CRUD）
import com.example.backend.entity.CourseEnrollment;            // 选课记录实体类
import com.example.backend.vo.EnrollmentVO;                    // 选课视图对象（联表查询结果）
import org.apache.ibatis.annotations.Mapper;                    // MyBatis Mapper 标识
import org.apache.ibatis.annotations.Param;                     // 参数绑定
import org.apache.ibatis.annotations.Select;                    // 自定义 SQL 查询
import java.util.List;                                          // Java 列表接口

// ==================== 3. 接口的声明和注解 ====================

/*
 * @Mapper
 *   标记为 MyBatis Mapper，由框架创建代理实现。
 *
 *   调用示例（在 Service 中）：
 *     courseEnrollmentMapper.insert(enrollment);         // 学生选课
 *     courseEnrollmentMapper.deleteById(id);             // 学生退课
 *     courseEnrollmentMapper.selectByStudentId(10L);     // 查学生选课列表
 */
@Mapper

/*
 * public interface CourseEnrollmentMapper extends BaseMapper<CourseEnrollment>
 *
 *   继承 BaseMapper 获得标准 CRUD。
 *   泛型 <CourseEnrollment> 指定操作的是 course_enrollment 表。
 */
public interface CourseEnrollmentMapper extends BaseMapper<CourseEnrollment> {

    // ==================== 4. 自定义查询方法 ====================

    /*
     * @Select 注解中的 SQL（包含 4 张表的 LEFT JOIN）：
     *
     *   SELECT ce.*, st.name AS student_name, c.name AS course_name,
     *          c.course_no AS courseNo, t.name AS teacher_name,
     *          c.credit AS credit, c.semester AS semester
     *
     *     查询的列：
     *       ce.*                      → course_enrollment 表的所有列（id, student_id, course_id, create_time）
     *       st.name AS student_name   → student 表的姓名，别名映射到 EnrollmentVO.studentName
     *       c.name AS course_name     → course 表的课程名，别名映射到 EnrollmentVO.courseName
     *       c.course_no AS courseNo   → 课程编号（由于 course_no 中有下划线，需要显式起别名）
     *       t.name AS teacher_name    → teacher 表的教师名，别名映射到 EnrollmentVO.teacherName
     *       c.credit AS credit        → 学分
     *       c.semester AS semester    → 学期
     *
     *     注意：c.course_no AS courseNo 为什么要显式写？
     *       course_no（数据库下划线列名）→ 自动映射为 courseNo（Java 驼峰）
     *       但如果自动映射失败或为了明确，可以用 AS 显式指定别名。
     *
     *   FROM course_enrollment ce
     *     主表：course_enrollment（别名 ce）
     *
     *   LEFT JOIN student st ON ce.student_id = st.id
     *     连接 student 表获取学生姓名
     *
     *   LEFT JOIN course c ON ce.course_id = c.id
     *     连接 course 表获取课程信息
     *
     *   LEFT JOIN teacher t ON c.teacher_id = t.id
     *     连接 teacher 表获取教师姓名（通过 course 表的 teacher_id 桥接）
     *     注意：不是 course_enrollment 直接连 teacher，而是通过 course 中转。
     *
     *   WHERE ce.student_id = #{studentId}
     *     只查询指定学生的选课记录
     *
     *   完整执行示例（studentId = 10）：
     *     | ce.id | st.name | c.name   | c.course_no | t.name  | c.credit | c.semester   |
     *     | 1     | 张三    | 高等数学  | MATH101    | 李教授   | 3.0     | 2024-2025-1 |
     *     | 2     | 张三    | 大学英语  | ENG201     | 王老师   | 2.0     | 2024-2025-1 |
     *
     *     每行数据映射为 EnrollmentVO 对象。
     *
     * ---------------------------------------------------------------------------
     *
     * List<EnrollmentVO> selectByStudentId(@Param("studentId") Long studentId);
     *
     *   List<EnrollmentVO>   —— 返回 EnrollmentVO 列表（视图对象，不是 Entity）
     *   selectByStudentId    —— 方法名：根据学生ID查询选课记录
     *   @Param("studentId")  —— 绑定参数到 #{studentId}
     *   Long studentId       —— 学生主键 ID
     *
     *   EnrollmentVO vs CourseEnrollment Entity：
     *     Entity 只有 4 个字段（id, studentId, courseId, createTime）
     *     VO 多了 studentName, courseName, teacherName, credit, semester 等展示用字段
     *
     *   JS 类比：
     *     function selectByStudentId(studentId) {
     *       return db.query(`
     *         SELECT ce.*, st.name AS student_name, c.name AS course_name,
     *                c.course_no, t.name AS teacher_name, c.credit, c.semester
     *         FROM course_enrollment ce
     *         LEFT JOIN student st ON ce.student_id = st.id
     *         LEFT JOIN course c ON ce.course_id = c.id
     *         LEFT JOIN teacher t ON c.teacher_id = t.id
     *         WHERE ce.student_id = ?
     *       `, [studentId])
     *     }
     */
    @Select("SELECT ce.*, st.name AS student_name, c.name AS course_name, " +
            "c.course_no AS courseNo, t.name AS teacher_name, c.credit AS credit, c.semester AS semester " +
            "FROM course_enrollment ce " +
            "LEFT JOIN student st ON ce.student_id = st.id " +
            "LEFT JOIN course c ON ce.course_id = c.id " +
            "LEFT JOIN teacher t ON c.teacher_id = t.id " +
            "WHERE ce.student_id = #{studentId}")
    List<EnrollmentVO> selectByStudentId(@Param("studentId") Long studentId);

    /*
     * ================================================================
     * 总结：CourseEnrollmentMapper = course_enrollment 表的操作接口
     *
     * 自动获得的方法（来自 BaseMapper<CourseEnrollment>）：
     *   insert、deleteById、updateById、selectById、selectList 等
     *
     * 自定义的方法：
     *   selectByStudentId(Long studentId)
     *     → 多层 LEFT JOIN 查询学生的选课列表
     *     → 包含学生名、课程名、教师名、学分、学期等展示信息
     *     → 返回 EnrollmentVO（视图对象）
     * ================================================================
     */
}
