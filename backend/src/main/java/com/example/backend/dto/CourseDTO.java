/*
 * ================================================================
 * 文件位置:  src/main/java/com/example/backend/dto/CourseDTO.java
 * 对应前端:  frontend/src/views/course/CourseForm.vue（课程新增/编辑表单页）
 *           frontend/src/api/course.js（前端 API 调用）
 *
 * 数据流向（以新增课程为例）:
 *   用户在 CourseForm.vue 填写课程编号、名称、学分、授课教师等
 *     ↓ 点击"保存"按钮
 *   axios.post('/api/courses', {
 *     courseNo: 'CS101',
 *     name: '数据结构',
 *     credit: 3.0,
 *     teacherId: 5,
 *     semester: '2024-2025-1',
 *     description: '计算机核心基础课...',
 *     classroom: 'B301',
 *     maxStudents: 60
 *   })
 *     ↓ Vite 代理转发到 Spring Boot
 *   Spring 用 Jackson 把请求体 JSON 自动转换成 CourseDTO 对象
 *     ↓ CourseController.save() → CourseServiceImpl → INSERT INTO course ...
 *     ↓ 返回成功
 *
 * JSON ↔ Java 类型映射:
 *   JSON:       "CS101"      "数据结构"      3.0          5            "2024-2025-1"
 *   Java:       String       String          Double       Long         String
 *   数据库:      VARCHAR      VARCHAR         DECIMAL      BIGINT       VARCHAR
 *   数据库列:    course_no    name            credit       teacher_id   semester
 *
 * JS/TS 类比:
 *   interface CreateCourseRequest {
 *     id?: number;          // 修改时传，新增时不传
 *     courseNo: string;     // 课程编号
 *     name: string;         // 课程名称
 *     credit: number;       // 学分
 *     teacherId: number;    // 教师ID
 *     semester: string;     // 学期
 *     description: string;  // 课程简介
 *     classroom: string;    // 教室
 *     maxStudents: number;  // 最大人数
 *   }
 * ================================================================
 */

// ==================== 1. 包声明 ====================

/*
 * package com.example.backend.dto;
 *   声明当前文件属于 dto 包。
 *   项目结构约定:
 *     dto/         ← 放所有 DTO（数据传输对象）
 *     vo/          ← 放所有 VO（视图对象，返回给前端）
 *     entity/      ← 放所有 Entity（数据库表映射）
 *     controller/  ← 放所有 Controller（HTTP 接口）
 *     service/     ← 放所有 Service（业务逻辑）
 *     mapper/      ← 放所有 Mapper（数据库操作）
 *     common/      ← 放公共类（如 Result.java 统一返回格式）
 */
package com.example.backend.dto; // 声明 DTO 包

// ==================== 2. 导入其他类（import） ====================

/*
 * import lombok.Data;
 *   Lombok 的 @Data 注解。
 *
 *   为什么 DTO 类几乎都用 @Data？
 *     Spring 在接收 JSON 时，实际上是这样做的：
 *       1. 用反射创建 CourseDTO 的空对象
 *       2. 遍历 JSON 里的每个字段
 *       3. 找到对应的 setter 方法（如 "name" → setName()）
 *       4. 调用 setter 方法把值设进去
 *     如果没有 setter 方法，Spring 就无法给字段赋值！
 *     所以 DTO 必须有 setter，而 @Data 自动生成了所有 setter。
 *
 *   数据接收链路:
 *     HTTP 请求体 JSON → Jackson 反序列化 → 调 setCourseNo()/setName()/... → Java 对象
 */
import lombok.Data; // Lombok 注解：自动生成 getter/setter/toString/equals/hashCode

// ==================== 3. 类的声明和注解 ====================

/*
 * @Data
 *   编译时自动生成所有字段的 getter/setter/toString/equals/hashCode 方法。
 *   此文件有 9 个字段，如果手写 getter/setter，大约需要 18 × 3 = 54 行代码。
 *   @Data 一行搞定。
 */
@Data // 自动生成 Getter/Setter/toString/equals/hashCode

/*
 * public class CourseDTO { ... }
 *
 *   为什么这个 DTO 同时用于"新增"和"修改"？
 *     - 新增时：前端不传 id（或传 null），后端 INSERT 一条新记录
 *     - 修改时：前端传 id，后端根据 id 找到已有记录，UPDATE 其他字段
 *     - 用同一个 DTO 减少代码重复，前端也只维护一份类型定义
 *     - 区分逻辑在后端 Service 层：if (dto.getId() == null) → 新增，else → 修改
 */
public class CourseDTO {

    // ==================== 4. 字段声明 ====================

    /*
     * private Long id;
     *
     *   用途:       课程的唯一标识符
     *   使用时机:   修改课程时传（告诉后端"我要改 id=5 的那门课"）
     *              新增课程时不传或传 null（后端 INSERT 后自动生成 ID）
     *   数据库来源:  course 表的主键 id 列（BIGINT AUTO_INCREMENT）
     *   前端来源:   课程列表页点"编辑"后，当前行数据的 id 会被填入表单隐藏字段
     *
     *   Long 类型的含义:
     *     Long 是 Java 的 64 位整数包装类型（来自 java.lang 包，自动导入）
     *     可以 null（这是用 Long 而不是 long 的原因）
     *     MySQL BIGINT 对应 Java Long
     *
     *   JS 类比:
     *     // 修改时
     *     const formData = { id: 5, name: '数据结构', ... }
     *     // 新增时
     *     const formData = { id: undefined, name: '新课程', ... }
     */
    private Long id; // 课程ID（修改时使用，新增时可为空/null）

    // ================================================================

    /*
     * private String courseNo;
     *
     *   用途:       课程编号，学校教务系统中的唯一标识
     *   数据库列:   course 表的 course_no 列
     *   前端显示:   课程列表的"课程编号"列
     *   示例值:     "CS101"、"MATH201"、"ENG105"
     *
     *   和 id 的区别:
     *     id        → 数据库自增主键，技术上唯一，对人无意义（如 5）
     *     courseNo  → 业务编号，有人类可读的意义（如 CS101 = 计算机科学第 101 号课）
     *   两者都需要，id 用于关联查询（效率高），courseNo 用于业务展示和搜索。
     */
    private String courseNo; // 课程编号，如 "CS101"（业务层面的唯一标识）

    // ================================================================

    /*
     * private String name;
     *
     *   用途:       课程名称
     *   数据库列:   course 表的 name 列
     *   前端显示:   课程列表的"课程名称"列
     *   示例值:     "数据结构"、"高等数学"、"大学英语"
     *
     *   前端字段映射说明:
     *     如果前端表单字段名叫 courseName，Spring 默认无法自动映射到 name 字段。
     *     解决方案通常是在 Service 层做手动转换，或前端提交时字段名对齐后端。
     *     所以这里注释说明"前端传 courseName 也会映射至此"是提示 Service 层有兼容处理。
     */
    private String name; // 课程名称，如 "高等数学"

    // ================================================================

    /*
     * private Double credit;
     *
     *   分解解释:
     *     private  —— 封装
     *     Double   —— Java 的 64 位浮点数包装类型（注意大写 D）
     *                double（小写）是基本类型，不能 null
     *                Double（大写）是包装类型，可以 null
     *                这里用 Double 因为学分可能暂时未填写
     *     credit   —— 学分
     *
     *   用途:       该课程的学分值
     *   数据库列:   course 表的 credit 列（DECIMAL 或 DOUBLE 类型）
     *   前端显示:   课程列表的"学分"列
     *   示例值:     3.0（3 个学分）、4.0、1.5
     *
     *   为什么用浮点数而不是整数？
     *     有些学校的学分有小数，如 1.5 学分、2.5 学分。
     *     但如果学分始终是整数，用 Integer 更合适（浮点计算有精度问题）。
     *
     *   JS 类比:
     *     // JavaScript 中不区分 float/double，统一叫 Number
     *     const credit: number = 3.0
     */
    private Double credit; // 学分数，如 3.0

    // ================================================================

    /*
     * private Long teacherId;
     *
     *   用途:       授课教师的 ID（外键，指向 teacher 表的主键 id）
     *   数据库列:   course 表的 teacher_id 列
     *   前端来源:   课程表单中的"授课教师"下拉选择框
     *              用户看到的是教师姓名，实际提交的是教师 ID
     *   示例值:     5L
     *
     *   为什么不存教师姓名？
     *     和 ClassDTO 中 headTeacherId 一样的原因：
     *     - 存 ID 建立外键关联，保证数据一致性
     *     - 查询时 LEFT JOIN teacher 表取姓名
     *     - 教师改名后不需要改 course 表
     */
    private Long teacherId; // 授课教师 ID（外键关联 teacher 表）

    // ================================================================

    /*
     * private String semester;
     *
     *   用途:       开课学期
     *   数据库列:   course 表的 semester 列
     *   前端显示:   课程列表的"学期"列
     *   示例值:     "2024-2025-1"（2024-2025 学年第 1 学期）
     *              "2024-2025-2"（2024-2025 学年第 2 学期）
     *
     *   格式说明:
     *     "学年-学年-学期"
     *     2024-2025-1 = 2024-2025 学年第一学期（秋季学期）
     *     2024-2025-2 = 2024-2025 学年第二学期（春季学期）
     *     虽然这个字段看起来是 String，但格式是固定的，前端可以用下拉框控制输入
     */
    private String semester; // 开课学期，如 "2024-2025-1"

    // ================================================================

    /*
     * private String description;
     *
     *   用途:       课程简介/描述信息
     *   数据库列:   course 表的 description 列（通常为 TEXT 类型，支持长文本）
     *   前端显示:   课程详情页的"课程介绍"区域
     *   示例值:     "数据结构是计算机专业的核心基础课程，主要介绍线性表、树、图等基本数据结构..."
     *
     *   和 VARCHAR 的区别:
     *     MySQL VARCHAR 最多存 65535 字节（实际更少），适合短文本
     *     MySQL TEXT 可以存 65535 字符（约 6 万汉字），适合长文本
     *     description 可能很长，所以数据库用 TEXT 类型
     *     Java 层不分 VARCHAR/TEXT，统一用 String
     */
    private String description; // 课程简介/描述（可以是较长的文本）

    // ================================================================

    /*
     * private String classroom;
     *
     *   用途:       上课教室位置
     *   数据库列:   course 表的 classroom 列
     *   前端显示:   课程列表/详情页的"教室"信息
     *   示例值:     "B301"、"理教 201"、"实验楼 A102"
     *
     *   注意:
     *     这里只是一个教室标识，不代表排课（同一教室不同时间可能有不同课）。
     *     真正的排课系统还需要时间维度的信息，本系统做了简化。
     */
    private String classroom; // 上课教室，如 "B301"

    // ================================================================

    /*
     * private Integer maxStudents;
     *
     *   分解解释:
     *     private     —— 封装
     *     Integer     —— 32 位整数包装类型
     *     maxStudents —— 字段名，驼峰命名
     *                   数据库对应列: max_students（max → students → max_students）
     *
     *   用途:       课程最大容纳学生数（选课人数上限）
     *   数据库列:   course 表的 max_students 列（INT 类型）
     *   前端显示:   课程列表/详情页的"容量"信息
     *   业务逻辑:   选课时检查当前选课人数 < maxStudents，否则提示"课程已满"
     *   示例值:     60、120、30
     *
     *   选课容量检查流程:
     *     学生选课 → EnrollmentController → 查看该课程当前已有多少人选
     *     → 如果 enrolledCount >= maxStudents → 返回错误"课程容量已满"
     *     → 如果 enrolledCount < maxStudents → 允许选课，插入选课记录
     *
     *   JS 类比:
     *     interface Course {
     *       maxStudents: number; // 课程容量
     *     }
     *     function canEnroll(course: Course, currentCount: number) {
     *       return currentCount < course.maxStudents;
     *     }
     */
    private Integer maxStudents; // 最大容纳学生数（选课容量上限）
}
