/*
 * ================================================================
 * 文件位置:  src/main/java/com/example/backend/vo/EnrollmentVO.java
 * 对应前端:  frontend/src/views/enrollment/EnrollmentList.vue（选课列表页面）
 *           frontend/src/views/student/MyCourses.vue（学生端"我的课程"页面）
 *           frontend/src/api/enrollment.js（前端 API 调用）
 *
 * 数据流向（以学生查看自己选课列表为例）:
 *   学生打开"我的课程"页面
 *     ↓ GET /api/enrollments/student/{studentId}
 *   EnrollmentService 执行 SQL 查询（多表联表）:
 *     SELECT e.id, e.student_id, s.name AS student_name,
 *            e.course_id, c.course_no, c.name AS course_name,
 *            t.name AS teacher_name, c.credit, e.semester, e.create_time
 *     FROM enrollment e
 *     LEFT JOIN student s ON e.student_id = s.id
 *     LEFT JOIN course c ON e.course_id = c.id
 *     LEFT JOIN teacher t ON c.teacher_id = t.id
 *     ↓ MyBatis 执行 → 返回 EnrollmentVO 列表
 *     ↓ 序列化为 JSON
 *   前端收到: {
 *     code: 200,
 *     data: {
 *       records: [{
 *         id: 25, studentId: 12, studentName: '张三',
 *         courseId: 8, courseNo: 'CS101', courseName: '数据结构',
 *         teacherName: '张教授', credit: 3.0,
 *         semester: '2024-2025-1', createTime: '2024-09-15T10:30:00'
 *       }]
 *     }
 *   }
 *   前端用这些数据渲染选课表格（课程名、教师名、学分等）
 *
 * EnrollmentVO 字段来源分析（这是最复杂的 VO，联表最多）:
 *   来自 enrollment 表:  id, studentId, courseId, createTime
 *   来自 student 表:     studentName（LEFT JOIN student ON student_id）
 *   来自 course 表:      courseId, courseNo, courseName, credit, semester
 *   来自 teacher 表:     teacherName（LEFT JOIN teacher ON course.teacher_id）
 *
 *   也就是说，前端一次请求就能拿到所有选课信息：
 *   谁选的课、选的什么课、谁教的课、多少学分、哪个学期。
 *   避免前端再发起 "查学生信息"、"查课程信息"、"查教师信息" 的多次请求。
 *   这就是 VO 的核心价值：一次查询，一次返回，前端直接渲染。
 *
 * EnrollmentVO 和 EnrollmentDTO 的对比:
 *   EnemymentDTO — 只有 studentId 和 courseId（前端只需要传这两个 ID）
 *   EnemymentVO  — 包含 10 个字段（后端把所有展示信息一起返回）
 *   这非常清楚地展示了 DTO（输入简化）和 VO（输出完整）的区别。
 *
 * JS 类比:
 *   // 前端表格渲染（"我的课程"页面）
 *   <el-table :data="myCourses">
 *     <el-table-column prop="courseNo" label="课程编号" />
 *     <el-table-column prop="courseName" label="课程名称" />
 *     <el-table-column prop="teacherName" label="授课教师" />
 *     <el-table-column prop="credit" label="学分" />
 *     <el-table-column prop="semester" label="学期" />
 *     <el-table-column prop="createTime" label="选课日期" />
 *   </el-table>
 *
 *   // TypeScript 类型
 *   interface EnrollmentVO {
 *     id: number;
 *     studentId: number;
 *     studentName: string;     // JOIN student 表
 *     courseId: number;
 *     courseNo: string;        // JOIN course 表
 *     courseName: string;      // JOIN course 表
 *     teacherName: string;     // JOIN teacher 表
 *     credit: number;          // JOIN course 表
 *     semester: string;        // JOIN course 表 或 enrollment 表
 *     createTime: string;      // enrollment 表
 *   }
 * ================================================================
 */

// ==================== 1. 包声明 ====================

/*
 * package com.example.backend.vo;
 *   声明当前文件属于 vo 包。
 */
package com.example.backend.vo; // 声明 VO 包

// ==================== 2. 导入其他类（import） ====================

/*
 * import lombok.Data;
 *   Lombok 注解：自动生成 getter/setter 方法。
 *   EnrollmentVO 有 10 个字段，手写会很繁琐。
 */
import lombok.Data; // Lombok 注解：自动生成 getter/setter/toString/equals/hashCode

/*
 * import java.time.LocalDateTime;
 *   用于 createTime（选课时间）字段。
 *   Jackson 序列化后前端收到 ISO 8601 格式的日期时间字符串。
 */
import java.time.LocalDateTime; // Java 8 时间 API

// ==================== 3. 类的声明和注解 ====================

/*
 * @Data
 *   自动生成 10 个字段的全部 getter/setter/toString/equals/hashCode 方法。
 */
@Data // 自动生成 Getter/Setter/toString/equals/hashCode

/*
 * public class EnrollmentVO { ... }
 *
 *   Enemyment   ← 业务领域：选课
 *   VO           ← 类型：视图对象
 */
public class EnrollmentVO {

    // ==================== 4. 字段声明 ====================

    /*
     * private Long id;
     *
     *   用途:       选课记录 ID（数据库主键）
     *   数据来源:   enrollment 表的 id 列（BIGINT AUTO_INCREMENT）
     *   前端使用:   退选（删除选课）时获取行 ID
     *   示例值:     25L
     */
    private Long id; // 选课记录 ID（数据库主键）

    // ================================================================

    /*
     * private Long studentId;
     *
     *   用途:       学生 ID
     *   数据来源:   enrollment 表的 student_id 列
     *   前端使用:   如果需要跳转到学生详情页，用这个 ID
     *   示例值:     12L
     *
     *   注意：虽然前端通常显示学生姓名（studentName），
     *   但保留 studentId 用于后续可能的交互操作（如点击学生名跳转）。
     */
    private Long studentId; // 学生 ID

    // ================================================================

    /*
     * private String studentName;
     *
     *   用途:       学生姓名
     *   数据来源:   通过 LEFT JOIN student 表获取：
     *              LEFT JOIN student s ON e.student_id = s.id → s.name
     *   前端显示:   选课列表的"学生姓名"列
     *   示例值:     "张三"
     *
     *   为什么需要 studentName？
     *     如果是管理员查看选课列表，需要显示"哪位学生选了哪些课"。
     *     如果是学生查看自己的课程，这个字段可能不会显示（所有记录都是同一个学生）。
     *     VO 包含足够的信息，前端按需渲染。
     */
    private String studentName; // 学生姓名（LEFT JOIN student 表）

    // ================================================================

    /*
     * private Long courseId;
     *
     *   用途:       课程 ID
     *   数据来源:   enrollment 表的 course_id 列
     *   前端使用:   跳转到课程详情页
     *   示例值:     8L
     */
    private Long courseId; // 课程 ID

    // ================================================================

    /*
     * private String courseNo;
     *
     *   用途:       课程编号（业务编号）
     *   数据来源:   通过 LEFT JOIN course 表获取：
     *               LEFT JOIN course c ON e.course_id = c.id → c.course_no
     *   前端显示:   选课列表的"课程编号"列
     *   示例值:     "CS101"
     *
     *   这个字段在 EnrollmentDTO 中没有，因为 DTO 不需要传课程编号。
     *   但 VO 中返回给前端，用于列表展示。
     */
    private String courseNo; // 课程编号（LEFT JOIN course 表）

    // ================================================================

    /*
     * private String courseName;
     *
     *   用途:       课程名称
     *   数据来源:   LEFT JOIN course 表 → c.name
     *   前端显示:   选课列表的"课程名称"列
     *   示例值:     "数据结构"
     */
    private String courseName; // 课程名称（LEFT JOIN course 表）

    // ================================================================

    /*
     * private String teacherName;
     *
     *   用途:       授课教师姓名
     *   数据来源:   这是一个间接 JOIN 的结果：
     *              enrollment → LEFT JOIN course → LEFT JOIN teacher
     *              e.course_id = c.id → c.teacher_id = t.id → t.name
     *              也就是说，enrollment 表本身没有 teacher_id，
     *              但可以通过 course 表间接获取授课教师的名字。
     *   前端显示:   选课列表的"授课教师"列
     *   示例值:     "张教授"
     *
     *   这种多级 JOIN 的场景在实际项目中很常见：
     *     选课表 → 课程表 → 教师表（三级关联）
     *   虽然 SQL 变复杂了，但前端一次请求拿到所有数据，用户体验更好。
     */
    private String teacherName; // 授课教师姓名（通过 course 表间接 JOIN teacher 表）

    // ================================================================

    /*
     * private Double credit;
     *
     *   用途:       课程学分
     *   数据来源:   LEFT JOIN course 表 → c.credit
     *   前端显示:   选课列表的"学分"列
     *   示例值:     3.0
     *
     *   为什么学分放在选课 VO 中？
     *     学生在查看自己的选课列表时，学分是很重要的信息。
     *     学生需要知道自己选了哪些课，以及这些课各有多少学分。
     *     后端一次返回所有信息，前端直接展示。
     */
    private Double credit; // 课程学分（LEFT JOIN course 表）

    // ================================================================

    /*
     * private String semester;
     *
     *   用途:       学期
     *   数据来源:   可能来自 enrollment 表或 course 表的 semester 列
     *              （具体看后端 SQL 怎么写的）
     *   前端显示:   选课列表的"学期"列
     *   示例值:     "2024-2025-1"
     *
     *   如果 enrollment 和 course 都有 semester 字段：
     *     通常取 enrollment 的 semester（选课时确定的学期），
     *     因为同一门课可能在不同学期开，选课时的学期更精确。
     */
    private String semester; // 学期（来自 enrollment 或 course 表）

    // ================================================================

    /*
     * private LocalDateTime createTime;
     *
     *   用途:       选课时间（选课记录创建时间）
     *   数据来源:   enrollment 表的 create_time 列
     *   前端显示:   选课列表的"选课日期"列
     *   序列化格式: "2024-09-15T10:30:00"
     *   示例值:     "2024-09-15T10:30:00"（2024年9月15日 10:30:00 选的课）
     *
     *   前端可以格式化显示：
     *     new Date(row.createTime).toLocaleDateString() → "2024/9/15"
     *     或者用 moment/dayjs 格式化 → "2024年9月15日"
     */
    private LocalDateTime createTime; // 选课时间（选课记录的创建时间）
}
