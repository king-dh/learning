/*
 * ================================================================
 * 文件位置:  src/main/java/com/example/backend/vo/CourseVO.java
 * 对应前端:  frontend/src/views/course/CourseList.vue（课程列表页面）
 *           frontend/src/views/course/CourseDetail.vue（课程详情页面）
 *           frontend/src/api/course.js（前端 API 调用）
 *
 * 数据流向（以查询课程列表为例）:
 *   前端请求 GET /api/courses/page
 *     ↓ CourseController → CourseService
 *   CourseService 执行 SQL:
 *     SELECT c.*, t.name AS teacher_name
 *     FROM course c
 *     LEFT JOIN teacher t ON c.teacher_id = t.id
 *     ↓ 返回 CourseVO 列表
 *     ↓ 序列化为 JSON
 *   前端收到: {
 *     code: 200,
 *     data: {
 *       records: [{
 *         id: 8, courseNo: 'CS101', name: '数据结构', credit: 3.0,
 *         teacherId: 5, teacherName: '张教授', semester: '2024-2025-1',
 *         description: '计算机核心基础课...', classroom: 'B301',
 *         maxStudents: 60, createTime: '2024-09-01T00:00:00'
 *       }],
 *       total: 15
 *     }
 *   }
 *
 * CourseVO 的字段来源分析:
 *   来自 course 表直接映射:   id, courseNo, name, credit, teacherId, semester, description, classroom, maxStudents, createTime
 *   来自 LEFT JOIN teacher 表: teacherName（增加了 Entity 中没有的字段）
 *
 *   相比之下，CourseDTO 也有类似字段，但有一些关键区别：
 *     CourseVO 有 createTime —— 数据库自动生成的，返回给前端展示
 *     CourseDTO 没有 createTime —— 前端不需要传这个，由数据库自动填充
 *     CourseVO 有 teacherName —— JOIN 查到的教师姓名
 *     CourseDTO 没有 teacherName —— 前端传 teacherId 就够了
 *
 * JS 类比:
 *   // 前端表格渲染
 *   <el-table :data="courseList">
 *     <el-table-column prop="courseNo" label="课程编号" />
 *     <el-table-column prop="name" label="课程名称" />
 *     <el-table-column prop="credit" label="学分" />
 *     <el-table-column prop="teacherName" label="授课教师" />
 *     <el-table-column prop="semester" label="学期" />
 *     <el-table-column prop="classroom" label="教室" />
 *     <el-table-column prop="maxStudents" label="容量" />
 *     <el-table-column prop="createTime" label="创建时间" />
 *   </el-table>
 *
 *   // TypeScript 类型
 *   interface CourseVO {
 *     id: number;
 *     courseNo: string;
 *     name: string;
 *     credit: number;
 *     teacherId: number;
 *     teacherName: string;   // 联表查询得到的额外字段
 *     semester: string;
 *     description: string;
 *     classroom: string;
 *     maxStudents: number;
 *     createTime: string;
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
 *   CourseVO 有 11 个字段，@Data 节省大量代码。
 */
import lombok.Data; // Lombok 注解：自动生成 getter/setter/toString/equals/hashCode

/*
 * import java.time.LocalDateTime;
 *   用于 createTime 字段，表示记录的创建时间。
 *   Jackson 自动序列化为 ISO 8601 字符串。
 */
import java.time.LocalDateTime; // Java 8 时间 API

// ==================== 3. 类的声明和注解 ====================

/*
 * @Data
 *   自动生成 11 个字段的全部 getter/setter/toString/equals/hashCode 方法。
 */
@Data // 自动生成 Getter/Setter/toString/equals/hashCode

/*
 * public class CourseVO { ... }
 *
 *   Course ← 业务领域：课程
 *   VO     ← 类型：视图对象（返回给前端展示用）
 */
public class CourseVO {

    // ==================== 4. 字段声明 ====================

    /*
     * private Long id;
     *
     *   用途:       课程 ID（主键）
     *   数据来源:   course 表的 id 列
     *   前端使用:   编辑课程时获取行数据 ID，选课时获取课程 ID
     *   示例值:     8L
     */
    private Long id; // 课程 ID（数据库主键）

    // ================================================================

    /*
     * private String courseNo;
     *
     *   用途:       课程编号（业务标识）
     *   数据来源:   course 表的 course_no 列
     *   前端显示:   课程列表页的"课程编号"列
     *   示例值:     "CS101"、"MATH201"
     */
    private String courseNo; // 课程编号（业务唯一标识）

    // ================================================================

    /*
     * private String name;
     *
     *   用途:       课程名称
     *   数据来源:   course 表的 name 列
     *   前端显示:   课程列表的"课程名称"列（通常加粗显示）
     *   示例值:     "数据结构"、"高等数学"
     *
     *   注意：VO 中的 name 和 DTO 中的 name 含义相同，
     *   但来源不同 — DTO 来自前端表单，VO 来自数据库。
     */
    private String name; // 课程名称

    // ================================================================

    /*
     * private Double credit;
     *
     *   用途:       课程学分
     *   数据来源:   course 表的 credit 列
     *   前端显示:   课程列表的"学分"列
     *   示例值:     3.0、4.0
     *
     *   和 CourseDTO 一样用 Double 类型，保持一致性。
     */
    private Double credit; // 学分（浮点数，支持小数如 1.5 学分）

    // ================================================================

    /*
     * private Long teacherId;
     *
     *   用途:       授课教师 ID
     *   数据来源:   course 表的 teacher_id 列
     *   前端使用:   如果需要跳转到教师详情页，用这个 ID 构建链接
     *   示例值:     5L
     *
     *   注意：虽然 VO 中也有 teacherId，但前端展示通常用 teacherName（下面的字段）。
     *         teacherId 保留是为了前端可能的后续操作（如点击教师名跳转详情页）。
     */
    private Long teacherId; // 授课教师 ID

    // ================================================================

    /*
     * private String teacherName;
     *
     *   用途:       授课教师姓名
     *   数据来源:   不是 course 表的字段！
     *              通过 SQL LEFT JOIN teacher 表查询得到：
     *              LEFT JOIN teacher t ON c.teacher_id = t.id → t.name AS teacher_name
     *   前端显示:   课程列表的"授课教师"列
     *   示例值:     "张教授"
     *
     *   为什么不在数据库 course 表中存教师姓名？
     *     原因同之前的解释：存 ID（外键），查姓名（JOIN）。
     *     这是关系型数据库的基本设计原则。
     *
     *   Field Name Mapping (VO 中的 teacherName 对应 SQL 的 teacher_name):
     *     Java VO:     teacherName（驼峰）
     *     SQL 别名:    teacher_name（下划线）
     *     数据库:       teacher 表的 name 列
     */
    private String teacherName; // 授课教师姓名（LEFT JOIN teacher 表获取）

    // ================================================================

    /*
     * private String semester;
     *
     *   用途:       开课学期
     *   数据来源:   course 表的 semester 列
     *   前端显示:   课程列表的"学期"列
     *   示例值:     "2024-2025-1"
     */
    private String semester; // 开课学期

    // ================================================================

    /*
     * private String description;
     *
     *   用途:       课程简介/描述
     *   数据来源:   course 表的 description 列（TEXT 类型，支持长文本）
     *   前端显示:   课程详情页的"课程介绍"部分
     *              （列表页一般只显示课程名，不显示描述，太长了）
     *   示例值:     "数据结构是计算机专业的核心基础课程..."
     *
     *   前端处理长文本:
     *     列表中可能截断显示（前 50 字 + "..."）
     *     详情页中完整展示
     */
    private String description; // 课程简介（长文本）

    // ================================================================

    /*
     * private String classroom;
     *
     *   用途:       上课教室
     *   数据来源:   course 表的 classroom 列
     *   前端显示:   课程列表的"教室"列
     *   示例值:     "B301"
     */
    private String classroom; // 上课教室

    // ================================================================

    /*
     * private Integer maxStudents;
     *
     *   用途:       课程最大容纳学生数
     *   数据来源:   course 表的 max_students 列
     *   前端显示:   课程列表的"容量"列，通常显示为"30/60"形式（已选/容量）
     *              （但 CourseVO 只有 maxStudents，已选人数需要额外的 enrolledCount 字段）
     *   示例值:     60
     *
     *   前端可以用这个值做视觉提示:
     *     - 已满（enrolledCount >= maxStudents）→ 显示红色"已满"标签
     *     - 即将满（enrolledCount >= maxStudents * 0.8）→ 显示黄色"即将满"标签
     *     - 充裕 → 显示绿色"可选"标签
     */
    private Integer maxStudents; // 最大容纳学生数（课程容量上限）

    // ================================================================

    /*
     * private LocalDateTime createTime;
     *
     *   用途:       课程记录创建时间
     *   数据来源:   course 表的 create_time 列
     *   前端显示:   课程列表的"创建时间"列
     *   序列化格式: "2024-09-01T00:00:00"（ISO 8601）
     *
     *   注意：这个字段 CourseDTO 里没有！
     *        DTO 是前端传来的，createTime 由后端自动填充，前端不需要也不应该传。
     *        VO 是后端返回的，包含完整的字段信息。
     */
    private LocalDateTime createTime; // 创建时间
}
