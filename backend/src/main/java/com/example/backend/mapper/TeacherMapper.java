/*
 * ================================================================
 * 文件位置:  src/main/java/com/example/backend/mapper/TeacherMapper.java
 * 对应 Entity: Teacher.java
 * 对应数据库表: teacher
 * 在架构中的位置:
 *
 *   Controller → Service → Mapper(当前文件) → 数据库 teacher 表
 *
 * Mapper（数据访问接口）= teacher（教师）表的数据库操作接口
 *
 * JS 类比：
 *   const TeacherMapper = {
 *     insert(teacher) { ... },       // 新增教师
 *     selectById(id) { ... },        // 按ID查教师
 *     updateById(teacher) { ... },   // 修改教师信息
 *     deleteById(id) { ... },        // 删除教师
 *     selectList(where) { ... },     // 查询教师列表（可加过滤条件）
 *     selectPage(page, where) { ... }, // 分页查询教师
 *     selectCount(where) { ... },    // 统计教师数量
 *   }
 *   // 以上全部从 BaseMapper 继承，无需自己写代码
 *
 * 为什么这个 Mapper 也没有自定义方法？
 *   teacher 表的查询都是标准操作，用 BaseMapper + 条件构造器（Wrapper）就能完成：
 *
 *   场景示例：
 *     - 查询所有教师 → mapper.selectList(null)
 *     - 按院系查教师 → mapper.selectList(wrapper.eq("department", "计算机学院"))
 *     - 按职称查教师 → mapper.selectList(wrapper.eq("title", "教授"))
 *     - 搜索教师姓名 → mapper.selectList(wrapper.like("name", "张"))
 *     - 分页查教师   → mapper.selectPage(page, wrapper)
 *
 *   这些用条件构造器一行代码就能搞定，不需要自己写 @Select SQL。
 *
 * 条件构造器 Wrapper 快速入门（在 Service 层使用）：
 *   LambdaQueryWrapper<Teacher> wrapper = new LambdaQueryWrapper<>();
 *   wrapper.eq(Teacher::getDepartment, "计算机学院")  // WHERE department = '计算机学院'
 *          .like(Teacher::getName, "张")               // AND name LIKE '%张%'
 *          .orderByDesc(Teacher::getCreateTime);       // ORDER BY create_time DESC
 *   List<Teacher> list = teacherMapper.selectList(wrapper);
 *
 *   强大之处：条件构造器自动处理驼峰转下划线、防止 SQL 注入、支持链式调用。
 * ================================================================
 */

// ==================== 1. 包声明 ====================
package com.example.backend.mapper; // mapper 子包（数据访问层，数据库操作接口）

// ==================== 2. 导入其他类 ====================

/*
 * com.baomidou.mybatisplus.core.mapper.BaseMapper
 *   MyBatis-Plus 的核心接口，泛型 <Teacher> 指定操作 teacher 表。
 *
 *   继承 BaseMapper 后，MyBatis-Plus 在运行时自动生成以下 SQL 对应的方法：
 *     insert(Teacher entity)              → INSERT INTO teacher (teacher_no, name, ...) VALUES (?, ?, ...)
 *     deleteById(Long id)                 → DELETE FROM teacher WHERE id = ?
 *     updateById(Teacher entity)          → UPDATE teacher SET teacher_no=?, name=?, ... WHERE id = ?
 *     selectById(Long id)                 → SELECT * FROM teacher WHERE id = ?
 *     selectList(Wrapper<Teacher> query)  → SELECT * FROM teacher WHERE ... (动态条件)
 *     selectPage(Page<Teacher> page, ...) → SELECT * FROM teacher WHERE ... LIMIT ?, ? (分页)
 *     selectCount(Wrapper<Teacher> query) → SELECT COUNT(*) FROM teacher WHERE ...
 *
 *   这些方法都是框架在运行时通过"动态代理"技术自动生成的，
 *   你既不需要写实现类，也不需要写 XML 映射文件。
 */
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/*
 * com.example.backend.entity.Teacher
 *   教师实体类（teacher 表的 Java 映射）。
 *   BaseMapper 的泛型参数，告诉框架操作哪个实体。
 *
 *   Teacher 实体包含字段：id, teacherNo, name, gender, title, department, phone, createTime
 *   BaseMapper 根据这些字段自动推断数据库列名（驼峰 → 下划线转换）。
 */
import com.example.backend.entity.Teacher;

/*
 * org.apache.ibatis.annotations.Mapper
 *   标记接口为 MyBatis Mapper 接口。
 *
 *   加了 @Mapper 后：
 *     1. MyBatis-Spring 启动时扫描到这个接口
 *     2. 使用 JDK Proxy（java.lang.reflect.Proxy）动态创建实现类
 *     3. 将实现类注册为 Spring Bean（默认 Bean 名 = 接口名首字母小写 → teacherMapper）
 *     4. Service 中可以直接 @Autowired 注入使用
 *
 *   如果不加 @Mapper，Spring 容器中不会有这个 Bean，启动时会报错：
 *     "Field teacherMapper in ... required a bean of type '...TeacherMapper' that could not be found."
 *
 *   解决方案：加 @Mapper，或者在启动类上加 @MapperScan("com.example.backend.mapper")
 */
import org.apache.ibatis.annotations.Mapper;

// ==================== 3. 接口的声明和注解 ====================

/*
 * @Mapper
 *   MyBatis Mapper 标识注解。框架扫描到后自动创建代理实现。
 *
 *   你应该知道的幕后原理（简单版）：
 *     MyBatis 使用 JDK 的动态代理（java.lang.reflect.Proxy）：
 *       - 运行时动态生成一个实现了 TeacherMapper 接口的类
 *       - 当调用 insert(teacher) 时，代理对象拦截调用
 *       - 解析方法签名 → 确定执行 INSERT SQL → 执行 → 返回结果
 *
 *   这个机制让你不需要手写任何实现代码，只用定义"我想做什么"（接口），
 *   框架负责"怎么做"（生成实现类 + 执行 SQL）。
 *   这就是"声明式编程"的核心思想。
 *
 *   类比 JS 的 Proxy：
 *     const handler = {
 *       get(target, method) {
 *         return (...args) => sqlExecutor.execute(method, args)  // 自动执行 SQL
 *       }
 *     }
 *     const teacherMapper = new Proxy({}, handler)
 *     // 调用 teacherMapper.selectById(1) → 自动执行 SQL
 */
@Mapper

/*
 * public interface TeacherMapper extends BaseMapper<Teacher>
 *
 *   这是一个极其简洁的 Mapper 接口，只继承了 BaseMapper，没有自定义方法。
 *   它的接口体（花括号里的内容）是空的，因为所有需要的方法都来自基接口。
 *
 *   这种"空接口"模式在 MyBatis-Plus 中非常常见：
 *     对于只做标准 CRUD 的表（增删改查、分页、统计），
 *     Mapper 接口只需 extend BaseMapper<Entity> 就够了。
 *     对于需要自定义 SQL 的表（联表查询、报表统计），
 *     需要在接口中声明额外的方法并用 @Select/@Update 等注解写 SQL。
 *
 *   Java 接口的继承（extends）：
 *     - 接口可以继承接口（TeacherMapper extends BaseMapper）
 *     - 一个接口可以继承多个接口（用逗号分隔）
 *     - 这称为"接口的多重继承"（Java 类不允许多继承，但接口可以）
 *
 *   类比 TypeScript：
 *     interface TeacherMapper extends BaseMapper<Teacher> {}  // 空接口体
 *
 *   类比 JavaScript：
 *     // @Mapper 相当于在运行时做：
 *     const TeacherMapper = Object.assign({}, BaseMapper.prototype)
 *     // 这样 TeacherMapper 就有了 BaseMapper 的所有方法
 */
public interface TeacherMapper extends BaseMapper<Teacher> {
    /*
     * 空接口体 —— 没有声明任何额外方法。
     *
     * 所有的数据库操作都由 BaseMapper<Teacher> 提供：
     *   teacherMapper.insert(teacher);       // 新增教师
     *   teacherMapper.selectById(1L);        // 查ID=1的教师
     *   teacherMapper.selectList(wrapper);   // 条件查询教师列表
     *   teacherMapper.updateById(teacher);   // 更新教师信息
     *   teacherMapper.deleteById(1L);        // 删除ID=1的教师
     *   teacherMapper.selectPage(page, ...); // 分页查询
     *
     * 这种极简风格是 MyBatis-Plus 的核心优势之一：
     *   不需要为每个表写 CRUD 方法，继承 BaseMapper 就全都有了。
     *
     * ================================================================
     * 总结：TeacherMapper = teacher 表的数据库操作接口
     *   - 继承 BaseMapper<Teacher> 获得全部标准 CRUD 方法
     *   - 无需自定义 SQL（所有查询用 Wrapper 实现）
     *   - 这是最简单但最典型的 Mapper 写法
     * ================================================================
     */
}
