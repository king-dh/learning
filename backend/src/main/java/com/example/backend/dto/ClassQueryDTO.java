/*
 * ================================================================
 * 文件位置:  src/main/java/com/example/backend/dto/ClassQueryDTO.java
 * 对应前端:  frontend/src/views/class/ClassList.vue（班级列表页面）
 *           frontend/src/api/class.js（前端 API 调用）
 *
 * 数据流向（以分页查询班级为例）:
 *   用户打开班级列表页面
 *     ↓ 搜索框输入"软件工程"，年级选"2024级"
 *   axios.get('/api/classes/page?className=软件工程&grade=2024级&pageNum=1&pageSize=10')
 *     ↓ URL 参数自动绑定
 *   Spring 把 URL 参数绑定到 ClassQueryDTO 对象:
 *     className="软件工程", grade="2024级", pageNum=1, pageSize=10
 *     ↓ ClassController → ClassService → 拼接 SQL WHERE 条件
 *   MyBatis 执行: SELECT ... FROM class_info WHERE class_name LIKE '%软件工程%' AND grade LIKE '%2024%' LIMIT 10
 *     ↓ 返回 JSON
 *   前端收到: { code: 200, data: { records: [...], total: 15, current: 1, size: 10 } }
 *
 * QueryDTO 和普通 DTO 的区别:
 *   - 普通 DTO（如 ClassDTO）:  用于"增删改"操作，字段对应表单输入
 *   - QueryDTO（如 ClassQueryDTO）: 用于"查询"操作，字段对应搜索条件 + 分页参数
 *   - QueryDTO 的字段通常都是可选的（用户不一定填全部搜索条件）
 *   - QueryDTO 通常有分页默认值（pageNum=1, pageSize=10）
 *
 * JS 类比:
 *   // 前端发起请求时的查询参数
 *   const params = {
 *     className: '软件工程',  // 搜索条件
 *     grade: '2024级',        // 搜索条件
 *     pageNum: 1,             // 分页：第几页
 *     pageSize: 10            // 分页：每页几条
 *   }
 *   axios.get('/api/classes/page', { params })
 *   // Spring 自动把 params 映射到 ClassQueryDTO 的字段上
 * ================================================================
 */

// ==================== 1. 包声明 ====================

/*
 * package com.example.backend.dto;
 *   声明当前文件属于 dto 包。
 *   所有 DTO 类（ClassDTO、ClassQueryDTO、StudentDTO...）都放在这个包下，
 *   便于统一管理和 import。
 */
package com.example.backend.dto; // 声明 DTO 包

// ==================== 2. 导入其他类（import） ====================

/*
 * import lombok.Data;
 *   Lombok 的 @Data 注解所在位置。
 *   lombok 包是第三方依赖（pom.xml 里引入的），不是 JDK 自带的。
 *
 *   编译时处理流程:
 *     源码 @Data → Lombok 注解处理器（编译期插件）→ 插入 getter/setter/toString 代码
 *     → 编译器继续编译 → 生成 .class 文件（已包含所有方法）
 */
import lombok.Data; // Lombok 注解：自动生成 getter/setter/toString/equals/hashCode

// ==================== 3. 类的声明和注解 ====================

/*
 * @Data
 *   上一文件 ClassDTO.java 已详细解释。
 *   Lombok 编译时自动生成:
 *     getClassName() / setClassName()
 *     getGrade() / setGrade()
 *     getPageNum() / setPageNum()
 *     getPageSize() / setPageSize()
 *     toString() / equals() / hashCode()
 */
@Data // 自动生成 Getter/Setter/toString/equals/hashCode

/*
 * public class ClassQueryDTO { ... }
 *
 *   类名命名规范：
 *     Class        ← 业务领域（班级）
 *     Query        ← 用途（查询）
 *     DTO          ← 类型后缀（数据传输对象）
 *   合起来：ClassQueryDTO = 班级查询专用的数据传输对象
 */
public class ClassQueryDTO {

    // ==================== 4. 字段声明 ====================

    /*
     * private String className;
     *
     *   用途:       班级名称搜索关键字（模糊匹配，不是精确搜索）
     *   数据来源:   前端列表页顶部的搜索框输入
     *   后端处理:   MyBatis-Plus 中拼成 WHERE class_name LIKE '%${className}%'
     *   是否必填:   否（留空表示不按名称筛选，查全部）
     *   示例值:     "软件工程"（会匹配到"软件工程1班"、"软件工程2班"等）
     *
     *   JS 类比:
     *     // 前端搜索框
     *     <input v-model="searchForm.className" placeholder="输入班级名称" />
     *     // v-model 双向绑定 → searchForm.className = '软件工程'
     *     // 发起请求 → axios.get('/api/classes/page', { params: searchForm })
     */
    private String className; // 按班级名称模糊搜索（用户输入部分名称即可）

    // ================================================================

    /*
     * private String grade;
     *
     *   用途:       年级搜索关键字（模糊匹配）
     *   数据来源:   前端列表页的年级筛选下拉框
     *   后端处理:   WHERE grade LIKE '%${grade}%'
     *   是否必填:   否
     *   示例值:     "2024"（会匹配到"2024级"所有班级）
     *
     *   注: 如果前端用下拉框（而非输入框），则 grade 实际上是精确值。
     *       但后端仍用模糊匹配（LIKE '%xxx%'），兼容两种前端实现方式。
     */
    private String grade; // 按年级模糊搜索

    // ================================================================

    /*
     * private Integer pageNum = 1;
     *
     *   分解解释:
     *     private       —— 封装
     *     Integer       —— Java 的 32 位整数包装类型（int 的包装类）
     *                     为什么用 Integer 而不是 int？
     *                       Integer 可以 null，int 不能 null
     *                       虽然这里给了默认值 1，但 URL 参数可以不传，Spring 需要支持 null
     *                     JS 类比: number
     *                     MySQL 对应: INT
     *     pageNum = 1   —— 字段初始值（默认值），用户不传参数时默认查第 1 页
     *                     这是 Java 字段级别的默认值设定，不是 MyBatis 的默认值
     *
     *   用途:       分页查询中的"第几页"
     *   数据来源:   前端 URL 参数 ?pageNum=1
     *   后端处理:   MyBatis-Plus 分页插件用 pageNum 和 pageSize 计算 LIMIT 偏移量
     *               LIMIT offset, size → LIMIT (pageNum-1)*pageSize, pageSize
     *               例如 pageNum=1, pageSize=10 → LIMIT 0, 10（第 1 页，从第 0 条开始取 10 条）
     *   默认值:     1（用户不传参数时默认第 1 页）
     *
     *   为什么需要分页？
     *     数据库可能有几万条班级记录，一次全查出来内存不够、前端渲染也慢。
     *     分页就是"每次只查 10 条"，和 Google 搜索结果只显示 10 条一个道理。
     *
     *   JS 类比:
     *     // 前端 Element Plus 分页组件
     *     <el-pagination :current-page="1" :page-size="10" />
     *     // current-page → pageNum
     *     // page-size    → pageSize
     */
    private Integer pageNum = 1; // 当前页码，默认第 1 页（第一页）

    // ================================================================

    /*
     * private Integer pageSize = 10;
     *
     *   分解解释:
     *     private       —— 封装
     *     Integer       —— 32 位整数包装类型
     *     pageSize = 10 —— 默认每页显示 10 条记录
     *
     *   用途:       分页查询中的"每页显示几条"
     *   数据来源:   前端 URL 参数 ?pageSize=10
     *   后端处理:   传给 MyBatis-Plus 的分页对象，作为 SQL LIMIT 的值
     *   默认值:     10 条/页
     *
     *   为什么默认是 10？
     *     - 太少（如 2）要翻太多页
     *     - 太多（如 100）加载慢，客户端卡
     *     - 10 是常见约定，兼顾性能和用户体验
     *
     *   JS 类比:
     *     // 前端 Element Plus 分页组件
     *     <el-pagination :page-size="10" :page-sizes="[10, 20, 50, 100]" />
     *     // page-size 默认 10，用户可选 20/50/100
     */
    private Integer pageSize = 10; // 每页显示条数，默认 10 条
}
