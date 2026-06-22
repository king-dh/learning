/*
 * ================================================================
 * 文件位置:  src/main/java/com/example/backend/mapper/StudentMapper.java
 * 对应 Entity: Student.java
 * 对应数据库表: student
 * 在架构中的位置:
 *
 *   ┌─────────────┐
 *   │ Controller   │  StudentController.java（接线员）
 *   └──────┬──────┘
 *          ↓  调 Service
 *   ┌─────────────┐
 *   │  Service     │  StudentServiceImpl.java（处理业务逻辑）
 *   └──────┬──────┘
 *          ↓  调 Mapper
 *   ┌─────────────┐
 *   │   Mapper     │  StudentMapper.java  ← 当前文件（数据库操作接口）
 *   └──────┬──────┘
 *          ↓  发 SQL
 *   ┌─────────────┐
 *   │   数据库     │  MySQL 的 student 表
 *   └──────────────┘
 *
 * Mapper（数据访问接口）是什么？
 *   = 定义数据库操作的"接口"（Interface）
 *   = 它只声明"我要做什么"，不写"怎么做"
 *   = MyBatis-Plus / MyBatis 框架在运行时自动生成实现类去执行 SQL
 *
 *   JS 类比：
 *     // Mapper 就像是一个"数据库操作函数集合"的声明文件
 *     // 你定义了要调用哪些函数，框架负责实现它们
 *
 *     // 在 JavaScript 中，你可能会这样写：
 *     const StudentMapper = {
 *       selectById(id) { return db.query("SELECT * FROM student WHERE id = ?", [id]) },
 *       insert(student) { return db.query("INSERT INTO student ...") },
 *       updateById(student) { return db.query("UPDATE student SET ...") },
 *       deleteById(id) { return db.query("DELETE FROM student WHERE id = ?", [id]) },
 *       selectList() { return db.query("SELECT * FROM student") },
 *       searchByKeyword(keyword) { return db.query("SELECT * FROM student WHERE ...") }
 *     }
 *
 *     // 在 Java 中，上面的 selectById/insert/updateById/deleteById/selectList
 *     // 都是继承 BaseMapper 后自动获得的，你不需要写！
 *     // 只有 searchByKeyword 是自定义的，需要写 SQL。
 *
 * MyBatis-Plus vs 传统 MyBatis 的区别：
 *   传统 MyBatis：每个 SQL 都要手写（XML 文件或注解），非常繁琐。
 *   MyBatis-Plus：继承 BaseMapper 后，常用的增删改查自动生成，只需要写特殊的查询。
 * ================================================================
 */

// ==================== 1. 包声明 ====================
package com.example.backend.mapper; // mapper 子包（数据访问层，专门放数据库操作接口）

// ==================== 2. 导入其他类（import） ====================

import com.baomidou.mybatisplus.core.mapper.BaseMapper; // MyBatis-Plus 基础 Mapper，提供通用 CRUD
import com.example.backend.entity.Student;                 // Student 实体类（告诉 Mapper 操作哪张表/哪个实体）
import org.apache.ibatis.annotations.Mapper;               // MyBatis Mapper 标识注解
import org.apache.ibatis.annotations.Param;                // 参数绑定注解：把方法参数映射到 SQL 中的 #{xxx}
import org.apache.ibatis.annotations.Select;               // 自定义查询注解：在方法上直接写 SQL（替代 XML 文件）

import java.util.List; // Java 集合框架：List 接口（类似 JS 的 Array）

// ==================== 3. 接口的声明和注解 ====================

/*
 * @Mapper
 *   这是 MyBatis 的注解，告诉 MyBatis 框架："这是一个 Mapper 接口，请扫描并管理它"。
 *
 *   MyBatis 在项目启动时会扫描所有带 @Mapper 注解的接口，
 *   然后用"动态代理"技术为每个接口自动生成一个实现类。
 *
 *   什么是动态代理？
 *     你不用写 StudentMapperImpl.java 这个文件，
 *     MyBatis 在运行时自动创建一个"替身"对象（代理对象），
 *     当你调用 mapper.selectById(1) 时，代理对象自动执行对应的 SQL。
 *
 *   JS 类比：
 *     // 你写的是声明（接口），框架在运行时创建实现（代理）
 *     const proxy = new Proxy({}, {
 *       get(target, method) {
 *         return (...args) => db.query(sqlFor(method), args)
 *       }
 *     })
 *
 *   注意：Spring Boot 项目中也可以用 @MapperScan 在启动类上统一扫描，
 *   这样就不需要每个 Mapper 都写 @Mapper 了。但逐个写 @Mapper 更直观。
 */
@Mapper

/*
 * public interface StudentMapper extends BaseMapper<Student>
 *
 *   逐词解释：
 *     public         —— 公开接口
 *     interface      —— 接口关键字（不是 class！接口只有方法声明，没有实现）
 *     StudentMapper  —— 接口名，命名规范：实体名 + Mapper
 *     extends        —— 继承关键字
 *     BaseMapper<Student> —— 泛型继承：继承 BaseMapper，并指定操作的实体类型是 Student
 *
 *   extends BaseMapper<Student> 是什么意思？
 *     BaseMapper 是 MyBatis-Plus 提供的泛型接口，里面定义了大量通用数据库操作方法。
 *     当你写 extends BaseMapper<Student> 时：
 *       1. 告诉 BaseMapper："我要操作的是 Student 实体，也就是 student 表"
 *       2. StudentMapper 自动继承以下方法（不需要手动写！）：
 *
 *          ┌─────────────────────┬──────────────────────────────────────────────┐
 *          │       方法名         │                  功能说明                      │
 *          ├─────────────────────┼──────────────────────────────────────────────┤
 *          │ insert(entity)      │ 插入一条记录（= INSERT INTO student ...）       │
 *          │ deleteById(id)      │ 根据主键删除（= DELETE FROM student WHERE id=?）│
 *          │ updateById(entity)  │ 根据主键更新（= UPDATE student SET ...）        │
 *          │ selectById(id)      │ 根据主键查询一条（= SELECT * FROM student WHERE id=?）│
 *          │ selectOne(wrapper)  │ 根据条件查询一条                                │
 *          │ selectList(wrapper) │ 查询列表（= SELECT * FROM student）            │
 *          │ selectPage(page, w) │ 分页查询（= SELECT ... LIMIT ?, ?）            │
 *          │ selectCount(wrapper)│ 统计数量（= SELECT COUNT(*) FROM student）     │
 *          └─────────────────────┴──────────────────────────────────────────────┘
 *
 *     这就是 MyBatis-Plus 的强大之处：继承 BaseMapper 后，你不用写任何 CRUD 代码！
 *     相当于 Prisma 的 prisma.student.findMany() / create() / update() / delete()。
 *
 *   接口 vs 类（重要概念）：
 *     - 接口（interface）：只定义"有什么方法"，不实现"方法怎么执行"。
 *       类比 TypeScript 的 interface 或抽象类。
 *     - 类（class）：同时定义"有什么方法"和"方法怎么执行"。
 *
 *     Mapper 定义为接口而不是类：
 *       因为 SQL 执行逻辑由 MyBatis 框架动态生成，不需要你写实现。
 *       你只需声明方法签名，框架负责"翻译成 SQL → 执行 → 返回结果"。
 */
public interface StudentMapper extends BaseMapper<Student> {

    // ==================== 4. 自定义查询方法（继承 BaseMapper 之外的特殊查询） ====================

    /*
     * @Select("SELECT * FROM student WHERE name LIKE CONCAT('%', #{keyword}, '%') OR student_no LIKE CONCAT('%', #{keyword}, '%')")
     *
     *   @Select 注解：
     *     在方法上直接写 SQL 语句，MyBatis 会执行它并返回结果。
     *     这是"注解式 SQL"，替代了传统的 XML 映射文件方式。
     *
     *   拆解这个 SQL：
     *     SELECT * FROM student                              -- 查询 student 表的所有列
     *     WHERE name LIKE CONCAT('%', #{keyword}, '%')       -- 条件1：name 列包含关键字（模糊匹配）
     *        OR student_no LIKE CONCAT('%', #{keyword}, '%') -- 条件2：或 student_no 列包含关键字
     *
     *     CONCAT('%', #{keyword}, '%') 是什么？
     *       MySQL 的字符串拼接函数，效果：'%' + keyword + '%'
     *       例如：keyword = "张"
     *         CONCAT('%', '张', '%') = '%张%'
     *         name LIKE '%张%' → 匹配 "张三"、"张伟"、"小张" 等
     *
     *     #{keyword} 是什么？
     *       MyBatis 的参数占位符，会被替换为实际参数值。
     *       和 JDBC 的 ? 占位符类似，但功能更强（自动处理类型转换、防 SQL 注入）。
     *       这里的 #{keyword} 对应方法参数 @Param("keyword") String keyword。
     *
     *     为什么用 CONCAT 而不是直接用 '%${keyword}%'？
     *       #{...} 是预编译参数（安全的，防 SQL 注入）
     *       ${...} 是字符串拼接（不安全的，可能被 SQL 注入攻击）
     *       CONCAT + #{...} 的方式既实现了模糊查询，又保证了安全。
     *
     *   JS 类比：
     *     // 在 Node.js 中类似于：
     *     function searchByKeyword(keyword) {
     *       return db.query(
     *         "SELECT * FROM student WHERE name LIKE ? OR student_no LIKE ?",
     *         [`%${keyword}%`, `%${keyword}%`]
     *       )
     *     }
     *
     * ---------------------------------------------------------------------------
     *
     * List<Student> searchByKeyword(@Param("keyword") String keyword);
     *
     *   逐词解释：
     *     List<Student>  —— 返回值类型：Student 对象的列表（多条学生记录）
     *     searchByKeyword—— 方法名（自定义的搜索方法）
     *
     *     @Param("keyword")  —— MyBatis 参数注解：
     *       告诉 MyBatis "把方法参数 keyword 的值绑到 SQL 中的 #{keyword}"。
     *       如果方法只有一个参数且 SQL 中只有一处引用，@Param 可以省略。
     *       但写上更明确，不容易出错。
     *
     *     String keyword   —— 方法参数：要搜索的关键字（如 "张"）
     *
     *   数据流转：
     *     Service 调用 → mapper.searchByKeyword("张")
     *       → @Param 将 "张" 绑定到 SQL 中的 #{keyword}
     *       → MyBatis 执行：SELECT * FROM student WHERE name LIKE '%张%' OR student_no LIKE '%张%'
     *       → 数据库返回匹配的行
     *       → MyBatis 将每行数据映射为 Student 对象
     *       → 封装为 List<Student> 返回
     */
    @Select("SELECT * FROM student WHERE name LIKE CONCAT('%', #{keyword}, '%') OR student_no LIKE CONCAT('%', #{keyword}, '%')")
    List<Student> searchByKeyword(@Param("keyword") String keyword);

    /*
     * ================================================================
     * 总结：StudentMapper = 学生表的数据库操作接口
     *
     * 自动获得的方法（来自 BaseMapper<Student>）：
     *   insert(Student)、deleteById(Long)、updateById(Student)、
     *   selectById(Long)、selectList(Wrapper)、selectPage(...)
     *
     * 自定义的方法：
     *   searchByKeyword(String keyword) → 按姓名或学号模糊搜索学生
     *
     * JS 类比：
     *   // MyBatis-Plus 的 Mapper 等价于：
     *   const studentDB = {
     *     insert: (student) => db.student.create({ data: student }),
     *     deleteById: (id) => db.student.delete({ where: { id } }),
     *     updateById: (s) => db.student.update({ where: { id: s.id }, data: s }),
     *     selectById: (id) => db.student.findUnique({ where: { id } }),
     *     selectList: () => db.student.findMany(),
     *     searchByKeyword: (kw) => db.$queryRaw`SELECT * FROM student WHERE ...`
     *   }
     * ================================================================
     */
}
