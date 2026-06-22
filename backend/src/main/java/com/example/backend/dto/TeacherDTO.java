/*
 * ================================================================
 * 文件位置:  src/main/java/com/example/backend/dto/TeacherDTO.java
 * 对应前端:  frontend/src/views/teacher/TeacherForm.vue（教师新增/编辑表单页）
 *           frontend/src/api/teacher.js（前端 API 调用）
 *
 * 数据流向（以新增教师为例）:
 *   管理员打开教师新增页面，填写工号、姓名、性别、职称、院系、电话
 *     ↓ 点击"保存"按钮
 *   axios.post('/api/teachers', {
 *     teacherNo: 'T2024001',
 *     name: '张教授',
 *     gender: '男',
 *     title: '教授',
 *     department: '计算机学院',
 *     phone: '13800138000'
 *   })
 *     ↓ JSON → Spring Jackson 转换 → TeacherDTO 对象
 *     ↓ TeacherController → TeacherService → INSERT INTO teacher
 *     ↓ 返回成功
 *
 * 和 StudentDTO 的对比:
 *   学生有 学号/姓名/性别/年龄/手机/邮箱/班级ID
 *   教师有 工号/姓名/性别/职称/院系/电话
 *   两个 DTO 字段不同是因为学生和教师的属性不同。
 *   但两者都代表"人"，在系统中分别对应 student 表和 teacher 表。
 *
 * JS 类比:
 *   interface CreateTeacherRequest {
 *     teacherNo: string;    // 工号
 *     name: string;         // 姓名
 *     gender: string;       // 性别
 *     title: string;        // 职称
 *     department: string;   // 院系
 *     phone: string;        // 电话
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
 *
 *   TeacherDTO 有 6 个字段，@Data 自动生成 12 个 getter/setter + toString 等，
 *   节省大量样板代码。
 */
import lombok.Data; // Lombok 注解：自动生成 getter/setter/toString/equals/hashCode

// ==================== 3. 类的声明和注解 ====================

/*
 * @Data
 *   自动生成 6 个字段的 getter/setter/toString/equals/hashCode 方法。
 */
@Data // 自动生成 Getter/Setter/toString/equals/hashCode

/*
 * public class TeacherDTO { ... }
 *
 *   Teacher  ← 业务领域：教师
 *   DTO      ← 类型：数据传输对象
 *
 *   注意：TeacherDTO 没有 id 字段（和 StudentDTO、CourseDTO 不同）。
 *   添加教师时用 POST，修改时用 PUT，但通常教师工号（teacherNo）作为唯一标识。
 *   这说明项目可能在 Service 层通过 teacherNo 而不是 id 来判断新增还是修改。
 */
public class TeacherDTO {

    // ==================== 4. 字段声明 ====================

    /*
     * private String teacherNo;
     *
     *   分解解释:
     *     private    —— 封装
     *     String     —— 字符串类型
     *     teacherNo  —— 教师工号
     *                  数据库对应列: teacher_no（teacher → no → teacher_no）
     *
     *   用途:       教师工号，学校内部的教师唯一编号
     *   数据来源:   前端教师表单的"工号"输入框
     *   数据库列:   teacher 表的 teacher_no 列
     *   示例值:     "T2024001"（T + 年份 + 序号）
     *
     *   和 id 的区别:
     *     id         —— 数据库自增主键，对人不友好
     *     teacherNo  —— 人为可读的业务编号，可能有编码规律
     *   作用类似 StudentDTO 中的 studentNo（学号）。
     *
     *   JS 类比:
     *     // 前端表单
     *     <el-input v-model="formData.teacherNo" placeholder="输入教师工号" />
     *     // 用户看到"T2024001"以为是工号，实际数据库主键是 id=5
     */
    private String teacherNo; // 教师工号（业务编号，如 "T2024001"）

    // ================================================================

    /*
     * private String name;
     *
     *   用途:       教师姓名
     *   数据来源:   前端教师表单的"姓名"输入框
     *   数据库列:   teacher 表的 name 列
     *   示例值:     "张教授"、"李讲师"
     *
     *   名字字段在大多数 DTO 中都用 `name` 而不是 `teacherName`，
     *   因为上下文已经说明这是"教师的姓名"（类名 TeacherDTO 提供了上下文）。
     */
    private String name; // 教师姓名

    // ================================================================

    /*
     * private String gender;
     *
     *   分解解释:
     *     private —— 封装
     *     String  —— 字符串类型（不是枚举/布尔值）
     *     gender  —— 性别
     *
     *   用途:       教师性别
     *   数据来源:   前端教师表单的"性别"选择框（单选框或下拉）
     *   数据库列:   teacher 表的 gender 列
     *   示例值:     "男"、"女"
     *
     *   为什么用 String 而不是 Boolean 或 Enum？
     *     Boolean 只能表示两种值（true/false），不适合性别（可能还有"不愿透露"等选项）
     *     Enum 可以在 Java 层限定取值（如 Gender.MALE, Gender.FEMALE），
     *     但本项目用 String 保持简单和数据库兼容性。
     *     如果业务需要精确控制，用 Enum 更好（编译期检查，防拼写错误）。
     *
     *   JS 类比:
     *     // 前端性别选择（Element Plus）
     *     <el-radio-group v-model="formData.gender">
     *       <el-radio label="男">男</el-radio>
     *       <el-radio label="女">女</el-radio>
     *     </el-radio-group>
     */
    private String gender; // 性别：男 / 女

    // ================================================================

    /*
     * private String title;
     *
     *   分解解释:
     *     private —— 封装
     *     String  —— 字符串类型
     *     title   —— 职称
     *
     *   用途:       教师职称（学术头衔）
     *   数据来源:   前端教师表单的"职称"下拉选择框
     *   数据库列:   teacher 表的 title 列
     *   可选值:     "教授"、"副教授"、"讲师"、"助教"
     *   示例值:     "教授"
     *
     *   中国高校职称体系（从高到低）:
     *     教授（正高级）→ 副教授（副高级）→ 讲师（中级）→ 助教（初级）
     *   这个字段帮助系统识别教师的学术级别，前端可以用不同颜色或标签展示。
     *
     *   和 StudentDTO 的区别:
     *     学生没有"职称"字段，这是教师特有的属性。
     *     学生有"年龄"、"邮箱"字段，教师没有。
     *     这体现了 DTO 的"按需定义"原则 — 每个 DTO 只包含该业务对象需要的字段。
     */
    private String title; // 职称：教授 / 副教授 / 讲师 / 助教

    // ================================================================

    /*
     * private String department;
     *
     *   分解解释:
     *     private    —— 封装
     *     String     —— 字符串类型
     *     department —— 院系（部门）
     *
     *   用途:       教师所属院系/部门
     *   数据来源:   前端教师表单的"院系"下拉选择框或输入框
     *   数据库列:   teacher 表的 department 列
     *   示例值:     "计算机学院"、"数学学院"、"物理学院"
     *
     *   这个字段在教师管理中非常重要：
     *     - 教师列表可以按院系筛选
     *     - 同一个院系的教师通常教同一个学院的课程
     *     - 院系之间的数据可以用于统计报表
     *
     *   JS 类比:
     *     // 前端下拉框
     *     <el-select v-model="formData.department" placeholder="选择院系">
     *       <el-option label="计算机学院" value="计算机学院" />
     *       <el-option label="数学学院" value="数学学院" />
     *     </el-select>
     */
    private String department; // 所属院系（如 "计算机学院"）

    // ================================================================

    /*
     * private String phone;
     *
     *   分解解释:
     *     private —— 封装
     *     String  —— 字符串类型（注意不是数字类型）
     *     phone   —— 联系电话
     *
     *   用途:       教师联系电话
     *   数据来源:   前端教师表单的"电话"输入框
     *   数据库列:   teacher 表的 phone 列（VARCHAR 类型）
     *   示例值:     "13800138000"
     *
     *   为什么 phone 用 String 而不是 Long？
     *     电话号码看似是数字，但有以下几个原因不适合用数字类型：
     *     1. 电话号码可能以 0 开头（如 "010-12345678"），Long 会丢掉前导 0
     *     2. 电话号码可能包含特殊字符（如 "-"、"+86"、"()"）
     *     3. 电话号码是"标识符"而不是"数值"，不需要做算术运算
     *     4. Long 可能超出范围（11 位手机号在 Long 范围内，但座机号可能更长）
     *     所以电话、身份证号、邮编等"看起来像数字"的字段，都应该用 String 存储。
     *
     *   和 StudentDTO 的 phone 字段对比:
     *     两者都用 String 类型存储电话，设计一致。
     *     但 StudentDTO 额外有 email 字段，教师没有 — 因为教师可能不需要邮箱联系。
     */
    private String phone; // 联系电话（String 类型，因为可能包含 "-" 或前导 0）
}
