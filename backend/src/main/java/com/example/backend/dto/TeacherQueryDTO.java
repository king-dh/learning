/*
 * ================================================================
 * 文件位置:  src/main/java/com/example/backend/dto/TeacherQueryDTO.java
 * 对应前端:  frontend/src/views/teacher/TeacherList.vue（教师列表页面）
 *           frontend/src/api/teacher.js（前端 API 调用）
 *
 * 数据流向（以分页搜索教师为例）:
 *   管理员打开教师管理页面
 *     ↓ 搜索框输入"张"，院系选"计算机学院"
 *   axios.get('/api/teachers/page?name=张&department=计算机学院&pageNum=1&pageSize=10')
 *     ↓ URL 参数自动绑定
 *   Spring 绑定到 TeacherQueryDTO:
 *     name="张", department="计算机学院", pageNum=1, pageSize=10
 *     ↓ TeacherController → TeacherService → MyBatis 拼接 SQL
 *   SQL: SELECT ... FROM teacher WHERE name LIKE '%张%' AND department LIKE '%计算机学院%' LIMIT 10
 *     ↓ 返回分页结果
 *
 * 搜索字段说明:
 *   - name:        模糊搜索（LIKE），按教师姓名搜索
 *   - department:  模糊搜索（LIKE），按院系搜索
 *   两个条件可以独立使用或组合使用（AND 关系）
 *
 * JS 类比:
 *   interface TeacherSearchParams {
 *     name?: string;        // 教师姓名
 *     department?: string;  // 院系
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
 *   Lombok 注解：自动生成 getter/setter 方法。
 */
import lombok.Data; // Lombok 注解：自动生成 getter/setter/toString/equals/hashCode

// ==================== 3. 类的声明和注解 ====================

/*
 * @Data
 *   自动生成 4 个字段的 getter/setter/toString/equals/hashCode 方法。
 */
@Data // 自动生成 Getter/Setter/toString/equals/hashCode

/*
 * public class TeacherQueryDTO { ... }
 *
 *   Teacher  ← 业务领域：教师
 *   Query    ← 用途：查询
 *   DTO      ← 类型：数据传输对象
 */
public class TeacherQueryDTO {

    // ==================== 4. 字段声明 ====================

    /*
     * private String name;
     *
     *   用途:       按教师姓名模糊搜索
     *   数据来源:   前端教师列表页的搜索输入框
     *   后端处理:   MyBatis-Plus 拼 SQL: WHERE name LIKE CONCAT('%', #{name}, '%')
     *   是否必填:   否（留空表示不按姓名筛选）
     *   示例:       输入 "张" → 匹配 "张三"、"张伟" 等教师
     *
     *   JS 类比:
     *     // 前端搜索框
     *     <el-input v-model="searchForm.name" placeholder="搜索教师姓名" clearable />
     */
    private String name; // 按教师姓名模糊搜索（用户输入部分姓名即可）

    // ================================================================

    /*
     * private String department;
     *
     *   用途:       按院系模糊搜索
     *   数据来源:   前端教师列表页的院系筛选框（下拉或输入框）
     *   后端处理:   MyBatis-Plus 拼 SQL: WHERE department LIKE CONCAT('%', #{department}, '%')
     *   是否必填:   否
     *   示例:       输入 "计算机" → 匹配 "计算机学院"、"计算机科学与技术系"
     *
     *   为什么院系也用模糊搜索？
     *     - 院系名称可能不统一（"计算机学院" vs "计算机科学与技术学院"）
     *     - 模糊搜索能匹配到用户输入的部分名称
     *     - 如果前端用下拉框，则应改为精确搜索（=）
     *     - 两种方式都支持，模糊搜索更通用
     *
     *   JS 类比:
     *     // 前端搜索表单（Element Plus）
     *     <el-select v-model="searchForm.department" clearable placeholder="选择院系">
     *       <el-option label="计算机学院" value="计算机学院" />
     *       <el-option label="数学学院" value="数学学院" />
     *       <el-option label="物理学院" value="物理学院" />
     *     </el-select>
     *     // 如果前端用下拉框，提交的值已经是精确的院系名
     *     // 但后端仍用 LIKE 做模糊匹配，兼容两种前端实现
     */
    private String department; // 按院系模糊搜索（输入院系的部分名称即可）

    // ================================================================

    /*
     * private Integer pageNum = 1;
     *
     *   用途:       分页查询的当前页码
     *   默认值:     1
     *   分页逻辑:   LIMIT (pageNum - 1) * pageSize, pageSize
     *              pageNum=1 → 取第 0~9 条
     *              pageNum=2 → 取第 10~19 条
     */
    private Integer pageNum = 1; // 当前页码，默认第 1 页

    // ================================================================

    /*
     * private Integer pageSize = 10;
     *
     *   用途:       分页查询的每页记录数
     *   默认值:     10
     *
     *   教师数量通常不多（一个学校几百到几千教师），
     *   但分页依然是必要的，保证用户体验和性能。
     */
    private Integer pageSize = 10; // 每页显示条数，默认 10 条
}
