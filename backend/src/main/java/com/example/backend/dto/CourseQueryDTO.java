/*
 * ================================================================
 * 文件位置:  src/main/java/com/example/backend/dto/CourseQueryDTO.java
 * 对应前端:  frontend/src/views/course/CourseList.vue（课程列表页面）
 *           frontend/src/api/course.js（前端 API 调用）
 *
 * 数据流向（以分页搜索课程为例）:
 *   用户打开课程列表页面
 *     ↓ 在搜索框输入"数学"，选教师"张三"，选学期"2024-2025-1"
 *   axios.get('/api/courses/page?name=数学&teacherId=5&semester=2024-2025-1&pageNum=1&pageSize=10')
 *     ↓ URL 参数自动绑定
 *   Spring 把 URL 参数绑定到 CourseQueryDTO:
 *     name="数学", teacherId=5, semester="2024-2025-1", pageNum=1, pageSize=10
 *     ↓ CourseController → CourseService → MyBatis 拼接查询条件
 *   SQL: SELECT ... FROM course WHERE name LIKE '%数学%' AND teacher_id = 5 AND semester = '2024-2025-1' LIMIT 10
 *     ↓ 返回分页结果
 *   前端收到: { code: 200, data: { records: [课程列表], total: 8 } }
 *
 * 搜索字段说明:
 *   - name:       模糊搜索（LIKE），用户输入"数学"能搜到"高等数学"、"离散数学"
 *   - teacherId:  精确搜索（=），下拉框选中的教师 ID，匹配唯一教师
 *   - semester:   精确搜索（=），下拉框选中的学期
 *
 * JS 类比:
 *   // 前端搜索表单数据
 *   const searchParams: CourseSearchParams = {
 *     name: '数学',
 *     teacherId: 5,
 *     semester: '2024-2025-1',
 *     pageNum: 1,
 *     pageSize: 10
 *   }
 * ================================================================
 */

// ==================== 1. 包声明 ====================

/*
 * package com.example.backend.dto;
 *   所有 DTO 类统一放在 dto 包下，便于 import 和维护。
 */
package com.example.backend.dto; // 声明 DTO 包

// ==================== 2. 导入其他类（import） ====================

/*
 * import lombok.Data;
 *   引入 Lombok 的 @Data 注解。
 *   import 的路径 lodash.Data 是 lombok 库的包结构。
 *   Lombok 不在 JDK 中，需要在 pom.xml 中引入依赖。
 */
import lombok.Data; // Lombok 注解：自动生成 getter/setter/toString/equals/hashCode

// ==================== 3. 类的声明和注解 ====================

/*
 * @Data
 *   自动生成 4 个字段（name、teacherId、semester、pageNum、pageSize）的 getter/setter 方法。
 *   因为 QueryDTO 也是要接收前端参数的，Spring 需要通过 setter 方法赋值。
 */
@Data // 自动生成 Getter/Setter/toString/equals/hashCode

/*
 * public class CourseQueryDTO { ... }
 *
 *   Course     ← 业务领域：课程
 *   Query      ← 用途：查询
 *   DTO        ← 类型：数据传输对象
 */
public class CourseQueryDTO {

    // ==================== 4. 字段声明 ====================

    /*
     * private String name;
     *
     *   用途:       按课程名称模糊搜索（前后端约定：模糊匹配）
     *   数据来源:   前端课程列表页顶部的搜索输入框
     *   后端处理:   MyBatis-Plus 拼 SQL: WHERE name LIKE CONCAT('%', #{name}, '%')
     *              即搜索名称中包含该关键字的课程
     *   是否必填:   否（留空表示不按名称筛选）
     *   示例:       用户输入 "数据" → 搜到 "数据结构"、"数据库原理"、"大数据分析"
     *
     *   为什么用模糊搜索而不是精确搜索？
     *     - 用户可能只记得部分课程名（如只记得"结构"，不记得是"数据结构"还是"软件结构"）
     *     - 模糊搜索更友好，用户体验更好
     *     - 性能方面：如果数据量大（几十万条），LIKE 前模糊（LIKE '%xxx'）不走索引，
     *       但教学管理系统课程数量通常不多（几千门），性能没问题
     */
    private String name; // 按课程名称模糊搜索（用户输入关键字即可）

    // ================================================================

    /*
     * private Long teacherId;
     *
     *   用途:       按授课教师 ID 精确搜索
     *   数据来源:   前端课程列表页的"授课教师"下拉筛选框
     *              下拉框显示教师姓名，提交的是教师 ID
     *   后端处理:   MyBatis-Plus 拼 SQL: WHERE teacher_id = #{teacherId}
     *              注意：这里是精确匹配（=），不是模糊匹配（LIKE）
     *   是否必填:   否（留空表示不按教师筛选）
     *   示例值:     5L（筛选 id=5 的教师教的所有课程）
     *
     *   为什么 teacherId 用精确搜索而 name 用模糊搜索？
     *     - ID 是精确值（数字），没有"部分匹配"的语义
     *     - 姓名是文本，"张"应该能搜到"张三"和"张伟"
     *     - 这里存的是 teacherId 而不是 teacherName，所以天然是精确搜索
     *
     *   JS 类比:
     *     // 前端下拉框（Element Plus）
     *     <el-select v-model="searchForm.teacherId" clearable>
     *       <el-option v-for="t in teachers" :key="t.id" :label="t.name" :value="t.id" />
     *     </el-select>
     *     // 选中"张三" → searchForm.teacherId = 5
     */
    private Long teacherId; // 按授课教师 ID 精确搜索（下拉框选中的教师 ID）

    // ================================================================

    /*
     * private String semester;
     *
     *   用途:       按学期精确搜索
     *   数据来源:   前端课程列表页的"学期"下拉筛选框
     *   后端处理:   MyBatis-Plus 拼 SQL: WHERE semester = #{semester}
     *   是否必填:   否（留空表示不按学期筛选，查所有学期的课程）
     *   示例值:     "2024-2025-1"
     *
     *   为什么学期用精确搜索？
     *     - 学期格式固定（如 "2024-2025-1"），不需要模糊匹配
     *     - 前端用下拉框提供学期选项，用户不会手输
     *     - 精确搜索效率更高，而且设置学期过滤通常是想看某个特定学期的课
     */
    private String semester; // 按学期精确搜索（通常通过下拉框选择）

    // ================================================================

    /*
     * private Integer pageNum = 1;
     *
     *   用途:       分页查询的当前页码
     *   数据来源:   前端分页组件或 URL 参数
     *   默认值:     1（用户不传参时默认第 1 页）
     *   后端处理:   传给 MyBatis-Plus 分页插件，计算 SQL 的 LIMIT 偏移量
     *
     *   分页逻辑:  LIMIT (pageNum - 1) * pageSize, pageSize
     *     pageNum=1, pageSize=10 → LIMIT 0, 10  （第 0 ~ 9 条记录）
     *     pageNum=2, pageSize=10 → LIMIT 10, 10 （第 10 ~ 19 条记录）
     *     pageNum=3, pageSize=10 → LIMIT 20, 10 （第 20 ~ 29 条记录）
     */
    private Integer pageNum = 1; // 当前页码，默认第 1 页

    // ================================================================

    /*
     * private Integer pageSize = 10;
     *
     *   用途:       分页查询的每页记录数
     *   数据来源:   前端分页组件的 page-size 属性
     *   默认值:     10
     *   后端处理:   传给 MyBatis-Plus 分页插件，作为 SQL LIMIT 的数量
     *
     *   常见可选项:  [10, 20, 50, 100]
     *     10 条 — 适合移动端，屏幕小
     *     20 条 — 适合 PC 端列表展示
     *     50 条 — 看大数据量列表场景
     *     100 条 — 一般不建议，加载慢
     */
    private Integer pageSize = 10; // 每页显示条数，默认 10 条
}
