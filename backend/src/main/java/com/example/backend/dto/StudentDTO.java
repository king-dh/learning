/*
 * ================================================================
 * 文件位置:  src/main/java/com/example/backend/dto/StudentDTO.java
 * 对应前端:  frontend/src/views/student/StudentForm.vue（学生新增/编辑表单页）
 *           frontend/src/api/student.js（前端 API 调用）
 *
 * 数据流向（以新增学生为例）:
 *   管理员打开学生新增页面，填写学号、姓名、性别、年龄等
 *     ↓ 点击"保存"
 *   axios.post('/api/students', {
 *     studentNo: 'S2024001',
 *     name: '张三',
 *     gender: '男',
 *     age: 20,
 *     phone: '13800138000',
 *     email: 'zhangsan@example.com',
 *     classId: 3
 *   })
 *     ↓ JSON → Spring Jackson → StudentDTO 对象
 *     ↓ StudentController.save() → StudentService → INSERT INTO student
 *     ↓ 返回成功
 *
 * 和 TeacherDTO 的对比:
 *   两者都代表人，但字段不同：
 *     学生特有: studentNo/age/email/classId
 *     教师特有: teacherNo/title/department
 *   这说明虽然"学生"和"教师"都是"人"，
 *   但在系统中它们是不同的业务实体，有不同的属性。
 *
 * JS 类比:
 *   interface CreateStudentRequest {
 *     id?: number;         // 修改时传
 *     studentNo: string;   // 学号
 *     name: string;        // 姓名
 *     gender: string;      // 性别
 *     age: number;         // 年龄
 *     phone: string;       // 手机
 *     email: string;       // 邮箱
 *     classId: number;     // 班级ID
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
 *   Lombok 注解：自动生成所有 getter/setter/toString/equals/hashCode 方法。
 *
 *   StudentDTO 有 8 个字段，如果手写 getter/setter，需要 16 个方法。
 *   @Data 一行注解代替所有。
 */
import lombok.Data; // Lombok 注解：自动生成 getter/setter/toString/equals/hashCode

// ==================== 3. 类的声明和注解 ====================

/*
 * @Data
 *   自动生成 8 个字段的全部 getter/setter 方法。
 */
@Data // 自动生成 Getter/Setter/toString/equals/hashCode

/*
 * public class StudentDTO { ... }
 *
 *   Student  ← 业务领域：学生
 *   DTO      ← 类型：数据传输对象
 *
 *   和 CourseDTO 一样，StudentDTO 也同时用于新增和修改：
 *     新增：id 不传或为 null → INSERT
 *     修改：id 有值 → 根据 id UPDATE
 */
public class StudentDTO {

    // ==================== 4. 字段声明 ====================

    /*
     * private Long id;
     *
     *   用途:       学生的唯一标识符（数据库主键）
     *   使用时机:   修改学生信息时传（告诉后端"修改 id=5 的那个学生"）
     *              新增时不传或传 null（新增时 id 由数据库自增生成）
     *   数据库来源:  student 表的 id 主键列（BIGINT AUTO_INCREMENT）
     *   前端来源:   学生列表页点"编辑"后，当前行数据的 id 填入表单隐藏字段
     *   示例值:     5L
     *
     *   注意：并不是所有 DTO 都有 id 字段。
     *         TeacherDTO 就没有 id，因为教师可能通过 teacherNo（工号）标识。
     *         有 id 的 DTO 说明后端用主键 id 来做 UPDATE 操作。
     */
    private Long id; // 学生ID（修改时使用，新增时可为空/null）

    // ================================================================

    /*
     * private String studentNo;
     *
     *   分解解释:
     *     private    —— 封装
     *     String     —— 字符串类型
     *     studentNo  —— 学号
     *
     *   用途:       学生的学号（学校内部唯一标识）
     *   数据来源:   前端学生表单的"学号"输入框
     *   数据库列:   student 表的 student_no 列
     *   示例值:     "S2024001"（S + 年份 + 序号）
     *
     *   学号 vs id:
     *     id         —— 数据库自动生成（1, 2, 3...），对人不直观
     *     studentNo  —— 有编码规则的业务编号（S2024001 = 学生 + 2024 年入学 + 001 号）
     *   两者都用于唯一标识一个学生，但用途不同：
     *     id         用于表关联（速度快）
     *     studentNo  用于搜索和展示（有业务含义）
     */
    private String studentNo; // 学号（业务唯一标识，如 "S2024001"）

    // ================================================================

    /*
     * private String name;
     *
     *   用途:       学生姓名
     *   数据来源:   前端学生表单的"姓名"输入框
     *   数据库列:   student 表的 name 列
     *   示例值:     "张三"
     */
    private String name; // 学生姓名

    // ================================================================

    /*
     * private String gender;
     *
     *   用途:       学生性别
     *   数据来源:   前端学生表单的"性别"单选框/下拉框
     *   数据库列:   student 表的 gender 列
     *   可选值:     "男"、"女"
     *
     *   和 TeacherDTO 的 gender 字段设计一致，都用 String 类型。
     */
    private String gender; // 性别：男 / 女

    // ================================================================

    /*
     * private Integer age;
     *
     *   分解解释:
     *     private  —— 封装
     *     Integer  —— 32 位整数包装类型（可以 null）
     *     age      —— 年龄
     *
     *   用途:       学生年龄
     *   数据来源:   前端学生表单的"年龄"输入框
     *   数据库列:   student 表的 age 列（INT 类型）
     *   示例值:     20
     *
     *   Integer vs int:
     *     用 Integer 包装类型，因为年龄可能暂未填写（null）。
     *
     *   为什么存年龄而不是出生日期？
     *     - 年龄是在变化的（每年长一岁），存在数据库里过时很快
     *     - 更好的做法：存出生日期，年龄在前端或 SQL 中动态计算
     *     - 本项目为简化，直接存年龄（适合演示目的）
     *
     *   注意：StudentDTO 有 age 字段，但 TeacherDTO 没有。
     *        这是因为教师通常不关心年龄，或者年龄信息不对外展示。
     */
    private Integer age; // 年龄（示例：20）

    // ================================================================

    /*
     * private String phone;
     *
     *   用途:       学生手机号码
     *   数据来源:   前端学生表单的"手机"输入框
     *   数据库列:   student 表的 phone 列（VARCHAR 类型）
     *   示例值:     "13800138000"
     *
     *   为什么用 String 而不是 Long？
     *     和 TeacherDTO 的 phone 字段一样的设计理由：
     *     - 手机号可能以特殊格式输入（如 "+86-13800138000"）
     *     - 前导不会丢失（不会出现 Long 把 "013800138000" 变成 "13800138000"）
     *     - 电话号码不是用来计算的数值
     *
     *   和 TeacherDTO 的区别:
     *     教师没有 email 但有 phone，学生两者都有。
     *     说明学生（通常是年轻人）比教师（可能不使用邮件）更需要邮箱联系。
     */
    private String phone; // 手机号码（String 类型，避免前导 0 丢失）

    // ================================================================

    /*
     * private String email;
     *
     *   用途:       学生电子邮箱
     *   数据来源:   前端学生表单的"邮箱"输入框
     *   数据库列:   student 表的 email 列（VARCHAR 类型）
     *   示例值:     "zhangsan@example.com"
     *
     *   为什么 TeacherDTO 没有 email 但 StudentDTO 有？
     *     - 学生通常通过学校邮箱接收课程通知、作业提醒
     *     - 教师可能不通过邮箱联系（用办公电话或办公系统）
     *     - 这也说明了 DTO 的"按需定义"原则：只包含需要的字段
     */
    private String email; // 电子邮箱

    // ================================================================

    /*
     * private Long classId;
     *
     *   分解解释:
     *     private  —— 封装
     *     Long     —— 64 位整数包装类型（可以 null）
     *     classId  —— 班级 ID（外键）
     *                数据库对应列: class_id
     *
     *   用途:       学生所属班级的 ID（外键关联 class_info 表）
     *   数据来源:   前端学生表单的"班级"下拉选择框
     *              下拉显示班级名称，提交班级 ID
     *   数据库列:   student 表的 class_id 列（BIGINT 类型，外键）
     *   可空:       是（学生可能暂时未分配班级）
     *   示例值:     3L（id=3 的班级"软件工程1班"）
     *
     *   为什么存班级 ID 而不是班级名称？
     *     - 数据库设计规范：通过外键关联，存 ID
     *     - 查询时 LEFT JOIN class_info 表取名称
     *     - 如果班级改名，学生表不需要修改
     *
     *   和 TeacherDTO 的区别:
     *     TeacherDTO 有 department（院系），StudentDTO 有 classId（班级）。
     *     学生属于班级（小单位），教师属于院系（大单位），这反映了学校的组织架构。
     *
     *   JS 类比:
     *     // 前端班级下拉框
     *     <el-select v-model="formData.classId" placeholder="选择班级">
     *       <el-option v-for="c in classList" :key="c.id" :label="c.className" :value="c.id" />
     *     </el-select>
     *     // 用户选"软件工程1班" → 提交 classId = 3
     */
    private Long classId; // 所属班级 ID（外键关联 class_info 表）
}
