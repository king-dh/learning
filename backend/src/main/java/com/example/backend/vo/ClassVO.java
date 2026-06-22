/*
 * ================================================================
 * 文件位置:  src/main/java/com/example/backend/vo/ClassVO.java
 * 对应前端:  frontend/src/views/class/ClassList.vue（班级列表页面）
 *           frontend/src/views/class/ClassDetail.vue（班级详情页面）
 *           frontend/src/api/class.js（前端 API 调用）
 *
 * 数据流向（以查询班级列表为例）:
 *   前端请求 GET /api/classes/page?pageNum=1&pageSize=10
 *     ↓ ClassController → ClassService
 *   ClassService 执行 SQL 查询:
 *     SELECT c.*, t.name AS head_teacher_name,
 *            (SELECT COUNT(*) FROM student s WHERE s.class_id = c.id) AS student_count
 *     FROM class_info c
 *     LEFT JOIN teacher t ON c.head_teacher_id = t.id
 *     ↓ MyBatis 查询数据库 → 返回 ClassVO 列表
 *     ↓ 包装成 Result JSON → 返回前端
 *   前端收到: {
 *     code: 200,
 *     data: {
 *       records: [
 *         { id: 1, className: '软件工程1班', grade: '2024级',
 *           headTeacherId: 3, headTeacherName: '张教授',
 *           studentCount: 35, createTime: '2024-09-01T00:00:00' }
 *       ],
 *       total: 10
 *     }
 *   }
 *   前端表格显示: 班级名称列、年级列、班主任姓名列、学生人数字、创建时间列
 *
 * VO 和 Entity 的区别:
 *   Entity（ClassInfo.java）—— 和数据库表完全对应，不加额外字段
 *     ClassInfo 只有: id, className, grade, headTeacherId, createTime
 *   VO（ClassVO.java）— 在 Entity 基础之上增加前端展示需要的字段
 *     ClassVO 额外有: headTeacherName（班主任姓名）、studentCount（学生人数）
 *   这两个额外字段是通过 SQL 的 LEFT JOIN 和子查询计算出来的，
 *   不是直接存在于 class_info 表中。
 *
 * VO 和 DTO 的区别（重要！）:
 *   DTO — 前端 → 后端（输入）：从前端接收 JSON 数据
 *   VO  — 后端 → 前端（输出）：把数据返回给前端
 *   一个方向是"进来"，一个方向是"出去"，要分清楚。
 *
 * DTO ≈ interface CreateClassRequest { ... }      // 前端传给后端的
 * VO  ≈ interface ClassResponse { ... }            // 后端返回给前端的
 *
 * JS 类比:
 *   // 前端表格列定义
 *   const columns = [
 *     { prop: 'className', label: '班级名称' },
 *     { prop: 'grade', label: '年级' },
 *     { prop: 'headTeacherName', label: '班主任' },
 *     { prop: 'studentCount', label: '学生人数' },
 *     { prop: 'createTime', label: '创建时间' }
 *   ]
 *
 *   // TypeScript 类型（ClassVO 的 JS 等价物）
 *   interface ClassVO {
 *     id: number;
 *     className: string;
 *     grade: string;
 *     headTeacherId: number | null;
 *     headTeacherName: string | null;  // LEFT JOIN 得到
 *     studentCount: number;            // 子查询 COUNT 得到
 *     createTime: string;              // ISO 格式的日期时间字符串
 *   }
 * ================================================================
 */

// ==================== 1. 包声明 ====================

/*
 * package com.example.backend.vo;
 *   声明当前文件属于 vo 包。
 *   所有 VO 类（ClassVO、StudentVO、CourseVO...）都放在 vo 包下。
 *
 *   项目结构约定:
 *     vo/  ← 放所有 View Object（视图对象，后端返回给前端）
 *     dto/ ← 放所有 Data Transfer Object（数据传输对象，前端传给后端）
 *   两个包分工明确：
 *     vo  负责"出"（后端 → 前端）
 *     dto 负责"入"（前端 → 后端）
 */
package com.example.backend.vo; // 声明 VO 包

// ==================== 2. 导入其他类（import） ====================

/*
 * import lombok.Data;
 *   Lombok 注解：自动生成 getter/setter 方法。
 *   VO 返回给前端时，Spring 用 Jackson 序列化，Jackson 通过 getter 方法获取字段值。
 *   所以 VO 也需要 getter 方法（@Data 自动生成）。
 */
import lombok.Data; // Lombok 注解：自动生成 getter/setter/toString/equals/hashCode

/*
 * import java.time.LocalDateTime;
 *
 *   java.time 包是 Java 8 引入的日期时间 API。
 *   LocalDateTime 表示日期+时间（不含时区），
 *   例如：2024-09-01T10:30:00（ISO 8601 格式）。
 *
 *   为什么用 LocalDateTime 而不是旧版的 Date？
 *     - java.util.Date 是 Java 1.0 时期的类，设计有很多缺陷
 *     - LocalDateTime 是不可变对象（线程安全）
 *     - LocalDateTime 的 API 更清晰、更易用
 *     - Jackson 自动把 LocalDateTime 序列化成 ISO 8601 字符串（如 "2024-09-01T10:30:00"）
 *
 *   Java 8 时间 API 家族:
 *     LocalDate       —— 只有日期（2024-09-01）
 *     LocalTime       —— 只有时间（10:30:00）
 *     LocalDateTime   —— 日期+时间（2024-09-01T10:30:00）
 *     ZonedDateTime   —— 日期+时间+时区（2024-09-01T10:30:00+08:00[Asia/Shanghai]）
 *     Instant         —— 时间戳（1970-01-01 以来的毫秒数）
 *
 *   本项目所有 createTime 字段都用 LocalDateTime，表示记录的创建时间。
 *
 *   JS 类比:
 *     // JavaScript 中
 *     const createTime: string = '2024-09-01T10:30:00' // 从后端收到的 ISO 字符串
 *     // 也可以用 new Date(createTime) 转成 JS Date 对象
 *     // new Date('2024-09-01T10:30:00').toLocaleString() → '2024/9/1 10:30:00'
 */
import java.time.LocalDateTime; // Java 8 时间 API（表示创建时间）

// ==================== 3. 类的声明和注解 ====================

/*
 * @Data
 *   自动生成 7 个字段的 getter/setter/toString/equals/hashCode 方法。
 *   VO 的字段都是只读的（后端设置值，前端读取值），
 *   但 @Data 仍然需要生成 setter，因为 Service 层设置值时需要 setter。
 *   前端收到的 JSON 是 Jackson 通过 getter 序列化的。
 */
@Data // 自动生成 Getter/Setter/toString/equals/hashCode

/*
 * public class ClassVO { ... }
 *
 *   Class  ← 业务领域：班级
 *   VO     ← 类型：视图对象（View Object）
 *
 *   注意命名：同一个业务概念（班级）有 3 个类：
 *     ClassInfo.java   → Entity（数据库表映射）
 *     ClassDTO.java    → DTO（前端提交表单）
 *     ClassVO.java     → VO（返回给前端展示）
 *   三个类通过包名区分，不会冲突。
 */
public class ClassVO {

    // ==================== 4. 字段声明 ====================

    /*
     * private Long id;
     *
     *   用途:       班级 ID（数据库主键）
     *   数据来源:   class_info 表的 id 列
     *   前端使用:   编辑/删除操作时获取行数据的 id
     *   示例值:     1L
     *
     *   注意：VO 中字段的顺序定义了 JSON 中字段的顺序（Jackson 默认按字段声明顺序输出）。
     *   所以 id 通常放在第一个（人类阅读 JSON 时的习惯）。
     */
    private Long id; // 班级 ID（数据库主键）

    // ================================================================

    /*
     * private String className;
     *
     *   用途:       班级名称
     *   数据来源:   class_info 表的 class_name 列
     *   前端显示:   班级列表的"班级名称"列
     *   示例值:     "软件工程1班"
     */
    private String className; // 班级名称

    // ================================================================

    /*
     * private String grade;
     *
     *   用途:       年级
     *   数据来源:   class_info 表的 grade 列
     *   前端显示:   班级列表的"年级"列
     *   示例值:     "2024级"
     */
    private String grade; // 年级

    // ================================================================

    /*
     * private Long headTeacherId;
     *
     *   用途:       班主任的教师 ID
     *   数据来源:   class_info 表的 head_teacher_id 列
     *   前端使用:   编辑班级时回填班主任下拉框的初始值
     *              也可以用于前端跳转到教师详情页（teacherId → 页面链接）
     *   可空:       是（班级可能未分配班主任）
     *   示例值:     3L
     *
     *   注意：这个字段和下面的 headTeacherName 是对应的：
     *     headTeacherId = 3         ← 来自 class_info 表直接查询
     *     headTeacherName = "张教授" ← 来自 LEFT JOIN teacher 表
     *   两者一起返回，前端用 headTeacherName 显示，用 headTeacherId 做后续操作。
     */
    private Long headTeacherId; // 班主任 ID（可为 null）

    // ================================================================

    /*
     * private String headTeacherName;
     *
     *   用途:       班主任姓名
     *   数据来源:   不是 class_info 表的直接字段！
     *              而是通过 SQL LEFT JOIN teacher 表查询出来的。
     *   SQL: SELECT c.*, t.name AS head_teacher_name
     *        FROM class_info c LEFT JOIN teacher t ON c.head_teacher_id = t.id
     *   前端显示:   班级列表的"班主任"列显示姓名（如"张教授"）
     *   可空:       是（如果 headTeacherId 为 null，则 headTeacherName 也为 null）
     *   示例值:     "张教授"
     *
     *   为什么这里存"姓名"而不是"ID"？
     *     这就是 VO 和 Entity 的区别：
     *       Entity（ClassInfo）只有 headTeacherId（数据库直接存的）。
     *       VO（ClassVO）多了 headTeacherName（为了前端方便显示，JOIN 查出来的）。
     *     前端表格需要显示班主任名字，VO 就把名字查好直接给前端，
     *     避免前端为了显示名字再发一次请求。
     */
    private String headTeacherName; // 班主任姓名（LEFT JOIN teacher 表获取）

    // ================================================================

    /*
     * private Integer studentCount;
     *
     *   分解解释:
     *     private       —— 封装
     *     Integer       —— 32 位整数包装类型（可以 null，虽然统计人数通常不会是 null）
     *     studentCount  —— 字段名：学生数量
     *
     *   用途:       该班级的学生总人数
     *   数据来源:   不是数据库直接字段，而是 SQL 子查询计算的：
     *              (SELECT COUNT(*) FROM student WHERE class_id = c.id) AS student_count
     *              也就是每条班级记录都去 student 表里数一下有多少学生属于这个班级。
     *   前端显示:   班级列表的"学生人数"列
     *   示例值:     35
     *
     *   为什么用子查询而不是存一个 studentCount 字段？
     *     - 学生人数是动态变化的（学生新增/转出），存冗余字段容易不一致
     *     - 每次查询班级列表时子查询实时计算，保证数据准确
     *     - 性能：如果数据量大，子查询可能慢，可以考虑缓存或统计表
     *     - 本项目数据量不大，子查询足够
     *
     *   JS 类比:
     *     // 前端收到后展示
     *     <el-table-column prop="studentCount" label="学生人数" width="100" />
     *     // 显示为：35
     */
    private Integer studentCount; // 班级学生人数（通过 SQL 子查询 COUNT 计算得到）

    // ================================================================

    /*
     * private LocalDateTime createTime;
     *
     *   分解解释:
     *     private     —— 封装
     *     LocalDateTime —— Java 8 的日期+时间类型
     *     createTime  —— 记录创建时间
     *
     *   用途:       班级记录的创建时间
     *   数据来源:   class_info 表的 create_time 列（DATETIME 类型）
     *   前端显示:   班级列表的"创建时间"列
     *   序列化格式: Jackson 自动转成 ISO 8601 字符串 "2024-09-01T10:30:00"
     *   前端处理:   new Date('2024-09-01T10:30:00').toLocaleString()
     *              → "2024/9/1 10:30:00"（浏览器本地化显示）
     *
     *   注意：createTime 一般变体是 createdAt（如 Ruby on Rails），
     *        这里用 createTime 是因为 MyBatis-Plus 自动填充的字段。
     */
    private LocalDateTime createTime; // 创建时间（Java 8 时间 API，Jackson 自动序列化为 ISO 格式字符串）
}
