/*
 * ================================================================
 * 文件位置:  src/main/java/com/example/backend/dto/StudentQueryDTO.java
 * 对应前端:  frontend/src/views/student/StudentList.vue（学生列表页面）
 *           frontend/src/api/student.js（前端 API 调用）
 *
 * 数据流向（以管理员搜索学生为例）:
 *   管理员打开学生管理页面
 *     ↓ 在搜索框输入学生姓名"张"，学号输入"S2024"
 *   axios.get('/api/students/page?name=张&studentNo=S2024&pageNum=1&pageSize=10')
 *     ↓ URL 参数自动绑定
 *   Spring 绑定到 StudentQueryDTO:
 *     name="张", studentNo="S2024", pageNum=1, pageSize=10
 *     ↓ StudentController → StudentService → MyBatis 拼接 SQL
 *   SQL: SELECT ... FROM student WHERE name LIKE '%张%' AND student_no LIKE '%S2024%' LIMIT 10
 *     ↓ 返回分页结果
 *   前端收到: { code: 200, data: { records: [{ id: 1, name: '张三', studentNo: 'S2024001', ... }], total: 5 } }
 *
 * 搜索字段说明:
 *   - name:       模糊搜索（LIKE），输入"张"能搜到"张三"、"张伟"
 *   - studentNo:  模糊搜索（LIKE），输入学号部分内容即可
 *   两个条件可以同时使用（AND 关系），也可以只填一个
 *
 *   注意：和 CourseQueryDTO 不一样，name 和 studentNo 都是模糊搜索。
 *   这是因为学生对用户来说"叫什么"和"学号多少"都是部分信息，都需要模糊匹配。
 *
 * JS 类比:
 *   interface StudentSearchParams {
 *     name?: string;       // 姓名关键字
 *     studentNo?: string;  // 学号关键字
 *     pageNum?: number;    // 页码
 *     pageSize?: number;   // 每页条数
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
 *   Lombok 注解：自动生成 getter/setter 方法。
 */
import lombok.Data; // Lombok 注解：自动生成 getter/setter/toString/equals/hashCode

// ==================== 3. 类的声明和注解 ====================

/*
 * @Data
 *   自动生成 4 个字段（name/studentNo/pageNum/pageSize）的 getter/setter 方法。
 */
@Data // 自动生成 Getter/Setter/toString/equals/hashCode

/*
 * public class StudentQueryDTO { ... }
 *
 *   Student  ← 业务领域：学生
 *   Query    ← 用途：查询
 *   DTO      ← 类型：数据传输对象
 */
public class StudentQueryDTO {

    // ==================== 4. 字段声明 ====================

    /*
     * private String name;
     *
     *   分解解释:
     *     private —— 封装
     *     String  —— 字符串类型
     *     name    —— 学生姓名搜索关键字
     *
     *   用途:       按学生姓名模糊搜索
     *   数据来源:   前端学生列表页顶部的搜索输入框
     *   后端处理:   MyBatis-Plus 拼 SQL: WHERE name LIKE CONCAT('%', #{name}, '%')
     *              即搜索姓名字段中包含该关键字的学生
     *   是否必填:   否（留空表示不按姓名筛选，查全部）
     *   示例:       用户输入 "张" → 搜到 "张三"、"张伟"、"张学友"
     *
     *   和精确搜索的区别:
     *     模糊搜索:  LIKE '%张%' → 匹配包含"张"的行
     *     精确搜索:  = "张三"    → 只匹配完全等于"张三"的行
     *     这里需要模糊搜索，因为用户可能只记得部分姓名。
     *
     *   JS 类比:
     *     // Vue 中的搜索
     *     <el-input v-model="searchForm.name" placeholder="输入学生姓名搜索" clearable />
     *     // v-model 双向绑定 → searchForm.name = '张'
     */
    private String name; // 搜索关键字（按姓名模糊匹配，输入部分姓名即可）

    // ================================================================

    /*
     * private String studentNo;
     *
     *   分解解释:
     *     private    —— 封装
     *     String     —— 字符串类型
     *     studentNo  —— 学号搜索关键字
     *
     *   用途:       按学号模糊搜索
     *   数据来源:   前端学生列表页的学号搜索输入框
     *   后端处理:   MyBatis-Plus 拼 SQL: WHERE student_no LIKE CONCAT('%', #{studentNo}, '%')
     *   是否必填:   否
     *   示例:       用户输入 "S2024" → 搜到学号以 S2024 开头的所有学生
     *
     *   为什么学号也用模糊搜索？
     *     - 学生学号可能有规律（如 S 开头 + 年份 + 序号），
     *       管理员可能只想看某一年级的学生 → 输入 "S2024"（学号前缀）
     *     - 模糊搜索比精确搜索更灵活
     *     - 如果业务需要精确搜索，可以用下拉框替代输入框（前端控制）
     *
     *   JS 类比:
     *     // 前端搜索表单
     *     const searchForm = reactive({
     *       name: '',       // 姓名搜索框
     *       studentNo: ''   // 学号搜索框
     *     })
     *     // 两个搜索条件是 AND 关系：姓名 AND 学号同时满足才返回
     */
    private String studentNo; // 搜索关键字（按学号模糊匹配，输入学号部分内容即可）

    // ================================================================

    /*
     * private Integer pageNum = 1;
     *
     *   用途:       分页查询的当前页码
     *   默认值:     1
     *   后端处理:   传给 MyBatis-Plus 分页插件，计算 LIMIT 偏移量
     *
     *   和之前所有 QueryDTO 的分页设计完全一致。
     *   如果需要，可以提取公共父类避免重复，但当前项目保持简洁。
     */
    private Integer pageNum = 1; // 当前页码，默认第 1 页

    // ================================================================

    /*
     * private Integer pageSize = 10;
     *
     *   用途:       分页查询的每页记录数
     *   默认值:     10
     *
     *   分页参数是所有查询列表的基础功能。
     *   没有分页的话，学生数据一多，页面加载慢，甚至可能撑爆内存。
     */
    private Integer pageSize = 10; // 每页显示条数，默认 10 条
}
