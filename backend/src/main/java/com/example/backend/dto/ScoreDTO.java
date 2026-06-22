/*
 * ================================================================
 * 文件位置:  src/main/java/com/example/backend/dto/ScoreDTO.java
 * 对应前端:  frontend/src/views/score/ScoreForm.vue（成绩录入/编辑表单页）
 *           frontend/src/api/score.js（前端 API 调用）
 *
 * 数据流向（以教师录入成绩为例）:
 *   教师在成绩录入页面选择学生、课程，输入分数和学期
 *     ↓ 点击"保存"按钮
 *   axios.post('/api/scores', {
 *     studentId: 12,
 *     courseId: 8,
 *     score: 85.5,
 *     semester: '2024-2025-1'
 *   })
 *     ↓ JSON 请求体 → Spring Boot
 *   @Min/@Max 校验：确保 score 在 0~100 之间
 *     ↓ 如果 score < 0 或 > 100，返回 400 "分数不能小于 0" 或 "分数不能大于 100"
 *     ↓ ScoreController.save() → ScoreService → INSERT INTO score ...
 *
 * 校验链:
 *   @Min(0)  → score 不能小于 0
 *   @Max(100) → score 不能大于 100
 *   两者结合：score 必须在 0~100 之间
 *   如果前端传 score: 105 → Spring 校验失败 → 返回 400
 *
 * JS 类比:
 *   interface SubmitScoreRequest {
 *     studentId: number;
 *     courseId: number;
 *     score: number;        // 0-100 之间
 *     semester: string;
 *   }
 *
 *   // 前端表单校验规则（Element Plus）
 *   const scoreRules = {
 *     score: [
 *       { required: true, message: '请输入分数' },
 *       { type: 'number', min: 0, max: 100, message: '分数必须在 0~100 之间' }
 *     ]
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
 * import jakarta.validation.constraints.Max;
 *
 *   @Max 注解：校验数值字段的最大值。
 *   value 属性指定最大值（如 value = 100 表示不能超过 100）。
 *   message 属性指定校验失败时返回给前端的错误信息。
 *
 *   适用类型: 整数（int/Integer/long/Long）和浮点数（double/Double/float/Float）
 *   校验规则: fieldValue <= value → 通过; fieldValue > value → 失败
 *   注意: 如果字段是 null，@Max 不会校验（null 需要通过 @NotNull 单独校验）
 *
 *   JS 类比:
 *     // Element Plus 表单校验
 *     { max: 100, message: '分数不能大于 100' }
 */
import jakarta.validation.constraints.Max; // 校验注解：字段值不能大于指定值

/*
 * import jakarta.validation.constraints.Min;
 *
 *   @Min 注解：校验数值字段的最小值。
 *   value 属性指定最小值（如 value = 0 表示不能小于 0）。
 *   message 属性指定校验失败时的错误信息。
 *
 *   适用类型: 和 @Max 一样，支持整数和浮点数。
 *   校验规则: fieldValue >= value → 通过; fieldValue < value → 失败
 *   注意: 如果字段是 null，@Min 也不会校验。
 *
 *   @Min + @Max 组合使用:
 *     可以实现"数值必须在某个范围内"的校验。
 *     例如 @Min(0) + @Max(100) 表示 score 必须在 [0, 100] 区间内。
 *
 *   JS 类比:
 *     // Element Plus 表单校验
 *     { min: 0, message: '分数不能小于 0' }
 */
import jakarta.validation.constraints.Min; // 校验注解：字段值不能小于指定值

/*
 * import lombok.Data;
 *   Lombok 注解：自动生成 getter/setter 方法。
 */
import lombok.Data; // Lombok 注解：自动生成 getter/setter/toString/equals/hashCode

// ==================== 3. 类的声明和注解 ====================

/*
 * @Data
 *   自动生成所有字段的 getter/setter/toString/equals/hashCode。
 *   此 DTO 有校验注解（@Min/@Max），但这是校验框架的注解，不影响 @Data 的工作。
 */
@Data // 自动生成 Getter/Setter/toString/equals/hashCode

/*
 * public class ScoreDTO { ... }
 *
 *   Score  ← 业务领域：成绩
 *   DTO    ← 类型：数据传输对象
 *
 *   命名对应的数据库表：score（成绩表）
 */
public class ScoreDTO {

    // ==================== 4. 字段声明 ====================

    /*
     * private Long studentId;
     *
     *   分解解释:
     *     private    —— 封装
     *     Long       —— 64 位整数包装类型
     *     studentId  —— 学生 ID
     *                  数据库对应列: student_id（外键指向 student 表）
     *
     *   用途:       标识这条成绩属于哪位学生
     *   数据来源:   前端成绩录入表单，从"学生"下拉选择框中获取
     *              下拉框显示学生姓名，提交的是学生 ID
     *   示例值:     12L（学生张三的成绩）
     *
     *   业务含义:
     *     一条成绩记录必须同时指定 studentId 和 courseId，才能唯一标识"谁"在"哪门课"得了多少分。
     *     同一学生、同一课程、同一学期通常只有一条成绩记录（唯一约束）。
     */
    private Long studentId; // 学生 ID，标识这条成绩属于哪位学生

    // ================================================================

    /*
     * private Long courseId;
     *
     *   分解解释:
     *     private  —— 封装
     *     Long     —— 64 位整数包装类型
     *     courseId —— 课程 ID
     *                数据库对应列: course_id（外键指向 course 表）
     *
     *   用途:       标识这条成绩是哪门课程的
     *   数据来源:   前端成绩录入表单，从"课程"下拉选择框中获取
     *              下拉框显示课程名称，提交的是课程 ID
     *   示例值:     8L（课程"数据结构"的成绩）
     *
     *   业务含义:
     *     studentId(12) + courseId(8) = 学生 12 在课程 8 的成绩
     *     后续查询成绩列表时，通过 LEFT JOIN 取出学生姓名和课程名称用于显示。
     */
    private Long courseId; // 课程 ID，标识这条成绩属于哪门课程

    // ================================================================

    /*
     * @Min(value = 0, message = "分数不能小于0")
     * @Max(value = 100, message = "分数不能大于100")
     * private Double score;
     *
     *   逐行分解解释:
     *
     *   @Min(value = 0, message = "分数不能小于0")
     *     @Min         —— 校验注解：最小值限制
     *     value = 0    —— 最小值是 0（成绩不能为负数）
     *     message      —— 校验失败时返回给前端的错误提示
     *
     *   @Max(value = 100, message = "分数不能大于100")
     *     @Max         —— 校验注解：最大值限制
     *     value = 100  —— 最大值是 100（满分 100 分）
     *     message      —— 校验失败时的错误提示
     *
     *   两个注解共同组成"分数区间校验"：
     *     如果 score = -5    → @Min 校验失败，返回 "分数不能小于0"
     *     如果 score = 105   → @Max 校验失败，返回 "分数不能大于100"
     *     如果 score = 85.5  → 两个注解都通过，校验成功
     *     如果 score = null  → 两个注解都跳过（null 值不触发 @Min/@Max）
     *
     *   为什么用 @Min/@Max 而不是 @Range？
     *     @Range 来自 Hibernate Validator（第三方扩展），@Min/@Max 是 Jakarta 标准。
     *     用标准注解兼容性更好，不依赖特定实现。
     *
     *   private Double score;
     *     private —— 封装
     *     Double  —— 64 位浮点数包装类型
     *               为什么用 Double 而不是 Integer？
     *                 成绩可能有小数（如 85.5 分），所以用浮点数。
     *                 用 Double 包装类型（可以 null），因为新增时 score 可能暂时为空。
     *               JS 类比: number
     *               MySQL 对应: DECIMAL 或 DOUBLE
     *
     *   用途:       考试/考核分数
     *   数据来源:   前端成绩录入表单的"分数"输入框
     *   校验规则:   0 ≤ score ≤ 100
     *   示例值:     85.5、92.0、60.0
     *
     *   实际业务中的成绩可能更复杂:
     *     - 有些学校用百分制（0-100）
     *     - 有些学校用五级制（优秀/良好/中等/及格/不及格）
     *     - 有些学校用 GPA（0-4.0）
     *   本项目简化为百分制，用 0-100 的数值表示。
     */
    @Min(value = 0, message = "分数不能小于0")   // 校验：分数最小值 0（不能为负数）
    @Max(value = 100, message = "分数不能大于100") // 校验：分数最大值 100（满分）
    private Double score; // 考试分数，范围 0-100（支持小数如 85.5）

    // ================================================================

    /*
     * private String semester;
     *
     *   分解解释:
     *     private   —— 封装
     *     String    —— 字符串类型
     *     semester  —— 学期
     *
     *   用途:       成绩所属的学期
     *   数据来源:   前端成绩录入表单的"学期"选择框
     *   后端存储:   score 表的 semester 列
     *   示例值:     "2024-2025-1"
     *
     *   为什么成绩也记录学期？
     *     同一学生可能在不同学期修同一门课（如补考、重修）。
     *     加上学期字段可以区分不同学期的成绩。
     *     实际上，学期信息也可以在 course 表中查，但冗余存储能简化成绩查询。
     *
     *   学期格式:
     *     "2024-2025-1" = 2024-2025 学年第一学期
     *     "2024-2025-2" = 2024-2025 学年第二学期
     */
    private String semester; // 学期，如 "2024-2025-1"（区分不同学期的成绩）
}
