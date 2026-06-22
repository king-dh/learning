/*
 * ================================================================
 * 文件位置:  src/main/java/com/example/backend/dto/ScoreQueryDTO.java
 * 对应前端:  frontend/src/views/score/ScoreList.vue（成绩列表页面）
 *           frontend/src/api/score.js（前端 API 调用）
 *
 * 数据流向（以教师查询成绩为例）:
 *   教师打开成绩列表页面
 *     ↓ 按学生 ID=12、课程 ID=8、学期"2024-2025-1"筛选
 *   axios.get('/api/scores/page?studentId=12&courseId=8&semester=2024-2025-1&pageNum=1&pageSize=10')
 *     ↓ URL 参数自动绑定
 *   Spring 绑定到 ScoreQueryDTO:
 *     studentId=12, courseId=8, semester="2024-2025-1", pageNum=1, pageSize=10
 *     ↓ ScoreController → ScoreService → MyBatis 拼接 SQL
 *   SQL: SELECT ... FROM score WHERE student_id = 12 AND course_id = 8 AND semester = '2024-2025-1' LIMIT 10
 *     ↓ 返回分页结果
 *   前端收到: { code: 200, data: { records: [{ id: 1, studentName: '张三', courseName: '数据结构', score: 85.5, ... }], total: 1 } }
 *
 * 搜索字段说明:
 *   - studentId:  精确搜索（=），如果学生成绩页面，可能自动携带当前学生 ID
 *   - courseId:   精确搜索（=），按课程筛选成绩
 *   - semester:   精确搜索（=），按学期筛选
 *   三者可以组合使用，也可以留空（查全部）
 *
 * JS 类比:
 *   interface ScoreSearchParams {
 *     studentId?: number;   // 学生ID
 *     courseId?: number;    // 课程ID
 *     semester?: string;    // 学期
 *     pageNum?: number;     // 页码
 *     pageSize?: number;    // 每页条数
 *   }
 * ================================================================
 */

// ==================== 1. 包声明 ====================

/*
 * package com.example.backend.dto;
 *   声明当前文件属于 dto 包。
 */
package com.example.backend.dto; // 声明 DTO 包

// ==================== 2. 导入其他类（import） ====================

/*
 * import lombok.Data;
 *   Lombok 注解：自动生成所有 getter/setter 方法。
 *   ScoreQueryDTO 的所有字段都需要接收前端 URL 参数，必须有 setter。
 */
import lombok.Data; // Lombok 注解：自动生成 getter/setter/toString/equals/hashCode

// ==================== 3. 类的声明和注解 ====================

/*
 * @Data
 *   自动生成 5 个字段（studentId/courseId/semester/pageNum/pageSize）的 getter/setter。
 */
@Data // 自动生成 Getter/Setter/toString/equals/hashCode

/*
 * public class ScoreQueryDTO { ... }
 *
 *   Score    ← 业务领域：成绩
 *   Query    ← 用途：查询
 *   DTO      ← 类型：数据传输对象
 */
public class ScoreQueryDTO {

    // ==================== 4. 字段声明 ====================

    /*
     * private Long studentId;
     *
     *   用途:       按学生 ID 精确搜索成绩
     *   数据来源:   前端成绩列表页的"学生"下拉筛选框
     *              如果是学生角色登录，系统会自动带上当前学生的 ID（只能看自己的成绩）
     *   后端处理:   MyBatis-Plus 拼 SQL: WHERE student_id = #{studentId}
     *              精确匹配（=），因为 ID 是精确数字
     *   是否必填:   否（留空表示查所有学生的成绩 — 通常只有管理员和教师需要）
     *   示例值:     12L（查 id=12 的学生的所有成绩）
     *
     *   权限控制说明（在 Controller 层）:
     *     - STUDENT 角色：只能查自己的成绩，Controller 自动从 JWT 中提取 studentId 并设置
     *     - TEACHER/ADMIN 角色：可以查任意学生的成绩，由前端筛选框自由选择
     *     所以这个字段在 DTO 中是可选的，权限逻辑在 Controller/Service 层处理。
     */
    private Long studentId; // 按学生 ID 精确搜索（查该学生的所有成绩）

    // ================================================================

    /*
     * private Long courseId;
     *
     *   用途:       按课程 ID 精确搜索成绩
     *   数据来源:   前端成绩列表页的"课程"下拉筛选框
     *   后端处理:   MyBatis-Plus 拼 SQL: WHERE course_id = #{courseId}
     *   是否必填:   否
     *   示例值:     8L（查课程"数据结构"的所有学生成绩）
     *
     *   典型使用场景:
     *     教师在成绩管理中，先选择一门课程，然后看到该课程所有学生的成绩列表。
     *     再配合 studentId 筛选，可以精确找到"某学生在某课程的成绩"。
     *
     *   JS 类比:
     *     // 教师端的筛选表单
     *     const searchForm = reactive({
     *       studentId: null,  // 可选：筛选特定学生
     *       courseId: 8,      // 当前选的课程
     *       semester: '2024-2025-1'
     *     })
     */
    private Long courseId; // 按课程 ID 精确搜索（查该课程所有学生的成绩）

    // ================================================================

    /*
     * private String semester;
     *
     *   用途:       按学期精确搜索成绩
     *   数据来源:   前端成绩列表页的"学期"下拉筛选框
     *   后端处理:   MyBatis-Plus 拼 SQL: WHERE semester = #{semester}
     *   是否必填:   否（留空表示查所有学期的成绩）
     *   示例值:     "2024-2025-1"
     *
     *   学期精确搜索 vs 模糊搜索:
     *     和 CourseQueryDTO 一样，学期格式固定，用精确匹配。
     *     用户通过下拉框选择学期，不会手动输入，所以不需要模糊搜索。
     */
    private String semester; // 按学期精确搜索（下拉框选择，如 "2024-2025-1"）

    // ================================================================

    /*
     * private Integer pageNum = 1;
     *
     *   用途:       分页查询的当前页码
     *   默认值:     1（用户不传参时默认第 1 页）
     *
     *   Integer vs int:
     *     Integer（大写）是包装类型，可以 null
     *     int（小写）是基本类型，不能 null，默认 0
     *     URL 参数可以不传，所以用 Integer 支持 null（null 时用默认值 1）
     *
     *   注意：虽然字段有默认值 1，但如果前端传了 pageNum=null，
     *         Spring 会把 null 值赋给 Integer 类型（因为 Integer 支持 null），
     *         导致默认值被覆盖。这种情况需要 Service 层做 null 判断兜底。
     */
    private Integer pageNum = 1; // 当前页码，默认第 1 页

    // ================================================================

    /*
     * private Integer pageSize = 10;
     *
     *   用途:       分页查询的每页记录数
     *   默认值:     10
     *
     *   和前几个 QueryDTO 一样的设计。
     *   所有列表查询都需要分页参数，所以每个 QueryDTO 都有这两个字段。
     *   这就是重复代码，但在简单项目中可以接受。
     *
     *   如果项目规模变大，可以抽取一个公共父类（如 BaseQueryDTO），
     *   把所有 QueryDTO 共有的 pageNum/pageSize 放在父类中，子类继承。
     *   但当前项目保持简单，每个 QueryDTO 独立定义。
     */
    private Integer pageSize = 10; // 每页显示条数，默认 10 条
}
