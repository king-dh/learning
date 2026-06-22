/*
 * ================================================================
 * 文件位置:  src/main/java/com/example/backend/mapper/SysUserMapper.java
 * 对应 Entity: SysUser.java
 * 对应数据库表: sys_user
 * 在架构中的位置:
 *
 *   Controller → Service → Mapper(当前文件) → 数据库 sys_user 表
 *
 * Mapper（数据访问接口）= sys_user 表的数据库操作接口
 *
 * 为什么这个 Mapper 不需要任何自定义方法？
 *   因为 sys_user 表的操作都是标准 CRUD：
 *     - 注册新用户 → insert(SysUser)
 *     - 根据用户名查用户 → selectOne(Wrapper)  【用条件构造器实现】
 *     - 修改密码 → updateById(SysUser)
 *     - 禁用用户 → updateById(SysUser)
 *   这些操作 BaseMapper 已经全部提供，不需要自己写 SQL。
 *
 *   条件构造器（Wrapper）是什么？
 *     MyBatis-Plus 的查询条件构造工具，让你用 Java 代码构建 WHERE 条件，
 *     而不是手写 SQL。例如根据用户名查询用户：
 *       LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
 *       wrapper.eq(SysUser::getUsername, "zhangsan");  // WHERE username = 'zhangsan'
 *       SysUser user = sysUserMapper.selectOne(wrapper);
 *
 *   JS 类比：
 *     const SysUserMapper = {
 *       insert(user) { ... },         // 注册新用户
 *       selectById(id) { ... },       // 按 ID 查用户
 *       selectOne(where) { ... },     // 按条件查用户（如按 username）
 *       updateById(user) { ... },     // 更新用户信息
 *       deleteById(id) { ... },       // 删除用户
 *     }
 *     // 以上方法都是继承 BaseMapper 自动获得的，不用写代码。
 * ================================================================
 */

// ==================== 1. 包声明 ====================
package com.example.backend.mapper; // mapper 子包（数据访问层）

// ==================== 2. 导入其他类 ====================

/*
 * com.baomidou.mybatisplus.core.mapper.BaseMapper
 *   MyBatis-Plus 的核心接口，提供了所有通用 CRUD 方法。
 *   泛型参数 <SysUser> 告诉它"我要操作的是 SysUser 实体对应的 sys_user 表"。
 *
 *   自动继承的方法包括（不需要自己写）：
 *     insert(SysUser entity)         → INSERT INTO sys_user (...)
 *     deleteById(Long id)            → DELETE FROM sys_user WHERE id = ?
 *     updateById(SysUser entity)     → UPDATE sys_user SET ... WHERE id = ?
 *     selectById(Long id)            → SELECT * FROM sys_user WHERE id = ?
 *     selectOne(Wrapper<SysUser>)    → SELECT * FROM sys_user WHERE ...（条件查询）
 *     selectList(Wrapper<SysUser>)   → SELECT * FROM sys_user（列表查询）
 *     selectPage(Page, Wrapper)      → SELECT ... LIMIT ...（分页查询）
 *     selectCount(Wrapper<SysUser>)  → SELECT COUNT(*) FROM sys_user（计数）
 */
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/*
 * com.example.backend.entity.SysUser
 *   SysUser 实体类，定义了 sys_user 表的字段结构。
 *   导入它是为了指定 BaseMapper 的泛型类型。
 */
import com.example.backend.entity.SysUser;

/*
 * org.apache.ibatis.annotations.Mapper
 *   标记当前接口为 MyBatis Mapper，框架在启动时扫描并创建代理实现。
 *
 *   @Mapper 的作用：
 *     1. 告诉 Spring："这个接口需要被管理"
 *     2. MyBatis 使用 JDK 动态代理技术自动生成实现类
 *     3. 代理类在运行时拦截方法调用，自动执行对应的 SQL
 *
 *   和 @Repository 的区别：
 *     @Repository 是 Spring 的数据访问层注解（和 @Service/@Controller 同级）
 *     @Mapper 是 MyBatis 的特定注解，Spring 需要额外配置才能识别
 *     在 Spring Boot 中通常用 @Mapper 或启动类上的 @MapperScan 来扫描
 */
import org.apache.ibatis.annotations.Mapper;

// ==================== 3. 接口的声明和注解 ====================

/*
 * @Mapper
 *   告诉 MyBatis 框架：SysUserMapper 是一个数据访问接口。
 *
 *   运行原理（理解即可）：
 *     1. Spring Boot 启动 → 扫描 @Mapper 注解
 *     2. MyBatis 用 JDK 动态代理创建一个"代理对象"
 *     3. 代理对象被注入到需要 SysUserMapper 的 Service 中
 *     4. 当 Service 调用 mapper.selectById(1) 时：
 *        代理对象拦截调用 → 解析方法名 → 生成 SQL → 执行 → 返回结果
 *
 *   这就是为什么你只需要定义接口（interface），
 *   不需要写实现类（Impl），框架帮你做了所有脏活累活。
 */
@Mapper

/*
 * public interface SysUserMapper extends BaseMapper<SysUser>
 *
 *   逐词解释：
 *     public              —— 公开接口
 *     interface           —— 接口关键字（定义方法签名，不实现方法体）
 *     SysUserMapper       —— 接口名，命名规范：实体名 + Mapper
 *     extends             —— 继承关键字（Java 接口可以多继承，但这里只继承一个）
 *     BaseMapper<SysUser> —— 泛型父接口，尖括号里的 <SysUser> 是类型参数
 *
 *   <SysUser> 泛型参数的含义：
 *     "这个 Mapper 操作的是 SysUser 实体，对应的表是 sys_user"。
 *     BaseMapper 里所有方法的参数/返回值类型都将使用 SysUser：
 *       insert(SysUser entity)     → 参数是 SysUser 类型
 *       selectById(Long id)        → 返回 SysUser 类型
 *       selectList(...)            → 返回 List<SysUser> 类型
 *
 *   类比 TypeScript：
 *     interface SysUserMapper extends BaseMapper<SysUser> {}
 *     // 相当于 TS 的泛型继承：
 *     // interface SysUserMapper extends BaseMapper<SysUser> {}
 *
 *   没有自定义方法的原因：
 *     这张表的所有操作（增删改查、按条件查询）都可以通过 BaseMapper 的通用方法完成，
 *     不需要自己写任何 SQL。例如：
 *       - 登录验证 → Service 中构造 LambdaQueryWrapper，调 mapper.selectOne(wrapper)
 *       - 注册用户 → Service 中调 mapper.insert(sysUser)
 *       - 修改角色 → Service 中调 mapper.updateById(sysUser)
 *
 *   这在 MyBatis-Plus 中被称为"无 SQL 开发"——常见操作全自动生成。
 */
public interface SysUserMapper extends BaseMapper<SysUser> {
    /*
     * 这里没有声明任何方法，因为所有需要的数据库操作
     * 都已经由 BaseMapper<SysUser> 提供了。
     *
     * 继承 BaseMapper 后，Service 层就可以直接调用：
     *
     *   sysUserMapper.insert(sysUser);        // 新增用户
     *   sysUserMapper.selectById(1L);         // 按ID查用户
     *   sysUserMapper.selectOne(wrapper);     // 按条件查用户（如按username）
     *   sysUserMapper.selectList(wrapper);    // 查用户列表
     *   sysUserMapper.updateById(sysUser);    // 更新用户
     *   sysUserMapper.deleteById(1L);         // 删除用户
     *   sysUserMapper.selectPage(page, wrapper); // 分页查询
     *   sysUserMapper.selectCount(wrapper);   // 统计数量
     *
     * 如果未来需要复杂查询（如多表联查、统计报表），
     * 可以在这里添加自定义方法，用 @Select 注解写 SQL。
     *
     * ================================================================
     * 总结：SysUserMapper = sys_user 表的数据库操作接口
     *   - 继承 BaseMapper<SysUser> 获得所有标准 CRUD 方法
     *   - 不需要写任何额外代码（无自定义 SQL 需求）
     *   - Service 层通过构造 Wrapper 条件来实现各种查询逻辑
     * ================================================================
     */
}
