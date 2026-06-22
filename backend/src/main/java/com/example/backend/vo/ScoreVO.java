/*
 * ================================================================
 * 文件位置:  src/main/java/com/example/backend/vo/ScoreVO.java
 * 对应前端:  frontend/src/views/score/ScoreList.vue（成绩列表页面）
 *           frontend/src/views/student/MyScores.vue（学生端"我的成绩"页面）
 *           frontend/src/api/score.js（前端 API 调用）
 *
 * 数据流向（以教师查看某课程成绩为例）:
 *   教师打开成绩管理页面，选择课程"数据结构"
 *     ↓ GET /api/scores/page?courseId=8&semester=2024-2025-1
 *   ScoreService 执行 SQL 查询（多表联表）:
 *     SELECT s.id, s.student_id, st.name AS student_name,
 *            s.course_id, c.name AS course_name,
 *            s.score, s.semester, s.create_time
 *     FROM score s
 *     LEFT JOIN student st ON s.student_id = st.id
 *     LEFT JOIN course c ON s.course_id = c.id
 *     ↓ MyBatis 执行 → 返回 ScoreVO 列表
 *     ↓ 序列化为 JSON
 *   前端收到: {
 *     code: 200,
 *     data: {
 *       records: [{
 *         id: 1, studentId: 12, studentName: '张三',
 *         courseId: 8, courseName: '数据结构',
 *         score: 85.5, semester: '2024-2025-1',
 *         createTime: '2024-10-15T14:30:00'
 *       }, ...]
 *     }
 *   }
 *
 * ScoreVO 字段来源分析:
 *   来自 score 表:       id, studentId, courseId, score, semester, createTime
 *   来自 student 表:     studentName（LEFT JOIN student ON student_id）
 *   来自 course 表:      courseName（LEFT JOIN course ON course_id）
 *
 * ScoreVO 和 ScoreDTO 的对比:
 *   ScoreDTO —  input（前端传来）：studentId + courseId + score + semester
 *   ScoreVO  —  output（后端返回）：id + studentId + studentName + courseId + courseName + score + semester + createTime
 *   区别：VO 多了 id（主键）、studentName、courseName、createTime
 *   这些额外字段来自数据库联表查询和自动填充字段。
 *
 * JS 类比:
 *   // 前端表格渲染
 *   <el-table :data="scoreList">
 *     <el-table-column prop="studentName" label="学生姓名" />
 *     <el-table-column prop="courseName" label="课程名称" />
 *     <el-table-column prop="score" label="成绩" sortable>
 *       <template #default="{ row }">
 *         <el-tag :type="row.score >= 60 ? 'success' : 'danger'">
 *           {{ row.score }}
 *         </el-tag>
 *       </template>
 *     </el-table-column>
 *     <el-table-column prop="semester" label="学期" />
 *     <el-table-column prop="createTime" label="录入时间" />
 *   </el-table>
 *
 *   // TypeScript 类型
 *   interface ScoreVO {
 *     id: number;
 *     studentId: number;
 *     studentName: string;    // LEFT JOIN student 表
 *     courseId: number;
 *     courseName: string;     // LEFT JOIN course 表
 *     score: number;
 *     semester: string;
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
 *   Lombok 注解：自动生成 getter/setter/toString/equals/hashCode 方法。
 *   ScoreVO 有 8 个字段，@Data 自动生成所有方法。
 */
import lombok.Data; // Lombok 注解：自动生成 getter/setter/toString/equals/hashCode

/*
 * import java.time.LocalDateTime;
 *   Java 8 时间 API，用于 createTime（成绩录入时间）字段。
 *   Jackson 序列化为 ISO 8601 格式字符串。
 */
import java.time.LocalDateTime; // Java 8 时间 API

// ==================== 3. 类的声明和注解 ====================

/*
 * @Data
 *   自动生成 8 个字段的全部 getter/setter/toString/equals/hashCode 方法。
 */
@Data // 自动生成 Getter/Setter/toString/equals/hashCode

/*
 * public class ScoreVO { ... }
 *
 *   Score ← 业务领域：成绩
 *   VO    ← 类型：视图对象
 */
public class ScoreVO {

    // ==================== 4. 字段声明 ====================

    /*
     * private Long id;
     *
     *   用途:       成绩记录 ID（数据库主键）
     *   数据来源:   score 表的 id 列
     *   前端使用:   编辑/删除成绩时获取行 ID
     *   示例值:     1L
     */
    private Long id; // 成绩记录 ID（数据库主键）

    // ================================================================

    /*
     * private Long studentId;
     *
     *   用途:       学生 ID
     *   数据来源:   score 表的 student_id 列（外键）
     *   前端使用:   跳转到学生详情页
     *   示例值:     12L
     *
     *   保留 studentId 和 courseId 的目的是让前端可以做后续操作
     *   （如点击学生名跳转到学生详情页、点击课程名跳转到课程详情页）。
     *   前端展示用 studentName 和 courseName，交互操作用 id。
     */
    private Long studentId; // 学生 ID

    // ================================================================

    /*
     * private String studentName;
     *
     *   用途:       学生姓名
     *   数据来源:   LEFT JOIN student 表获取：
     *              LEFT JOIN student st ON s.student_id = st.id → st.name
     *   前端显示:   成绩列表的"学生姓名"列
     *   示例值:     "张三"
     *
     *   为什么需要 studentName？
     *     如果只返回 studentId，前端还要再发一次请求查学生名字。
     *     一次 JOIN 查询就把名字带回来，前端直接展示，节省网络请求。
     */
    private String studentName; // 学生姓名（LEFT JOIN student 表获取）

    // ================================================================

    /*
     * private Long courseId;
     *
     *   用途:       课程 ID
     *   数据来源:   score 表的 course_id 列（外键）
     *   前端使用:   跳转到课程详情页
     *   示例值:     8L
     */
    private Long courseId; // 课程 ID

    // ================================================================

    /*
     * private String courseName;
     *
     *   用途:       课程名称
     *   数据来源:   LEFT JOIN course 表获取：
     *              LEFT JOIN course c ON s.course_id = c.id → c.name
     *   前端显示:   成绩列表的"课程名称"列
     *   示例值:     "数据结构"
     *
     *   同样，一次 JOIN 避免了前端再发请求查课程名称。
     */
    private String courseName; // 课程名称（LEFT JOIN course 表获取）

    // ================================================================

    /*
     * private Double score;
     *
     *   分解解释:
     *     private —— 封装
     *     Double  —— 64 位浮点数包装类型
     *     score   —— 分数
     *
     *   用途:       考试/考核分数
     *   数据来源:   score 表的 score 列
     *   前端显示:   成绩列表的"分数"列
     *   范围:       0 ~ 100
     *   示例值:     85.5
     *
     *   前端可以做条件渲染:
     *     - score >= 90 → 绿色标签"优秀"
     *     - score >= 80 → 蓝色标签"良好"
     *     - score >= 70 → 黄色标签"中等"
     *     - score >= 60 → 橙色标签"及格"
     *     - score < 60  → 红色标签"不及格"
     *
     *   成绩排序:
     *     前端可以用 sortable 属性让用户按成绩排序。
     *     或者后端在 SQL 中加 ORDER BY score DESC（按成绩降序）。
     */
    private Double score; // 考试分数（0-100）

    // ================================================================

    /*
     * private String semester;
     *
     *   用途:       学期
     *   数据来源:   score 表的 semester 列
     *   前端显示:   成绩列表的"学期"列
     *   示例值:     "2024-2025-1"
     *
     *   这个字段在成绩查询中很重要：
     *     同一个学生在不同学期可能修过同一门课（如补考、重修），
     *     学期区分不同学期的成绩记录。
     */
    private String semester; // 学期

    // ================================================================

    /*
     * private LocalDateTime createTime;
     *
     *   用途:       成绩录入时间
     *   数据来源:   score 表的 create_time 列
     *   前端显示:   成绩列表的"录入时间"列
     *   序列化格式: "2024-10-15T14:30:00"
     *   示例值:     "2024-10-15T14:30:00"（2024年10月15日 14:30:00 录入的成绩）
     *
     *   前端可以格式化显示：
     *     new Date(row.createTime).toLocaleString() → "2024/10/15 14:30:00"
     *
     *   ScoreDTO 没有 createTime 字段：
     *     因为录入时间由后端自动填充（@TableField(fill = FieldFill.INSERT)），
     *     前端不需要也不应该传这个值。
     */
    private LocalDateTime createTime; // 录入时间（成绩记录创建时间）
}
