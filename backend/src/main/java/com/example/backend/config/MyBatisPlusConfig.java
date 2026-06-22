/*
 * ================================================================
 * 文件位置:  src/main/java/com/example/backend/config/MyBatisPlusConfig.java
 * 所在层:    Config 层（配置层）
 *
 * 职责说明:
 *   配置 MyBatis-Plus 的分页插件。如果不配置这个类，所有分页查询功能将失效！
 *
 * 什么是 MyBatis-Plus？
 *   MyBatis 是 Java 最流行的 ORM 框架之一（ORM = Object Relational Mapping，对象关系映射）。
 *   它把 Java 对象和数据库表对应起来，让你用 Java 代码操作数据库，不用写 SQL。
 *
 *   类比 JavaScript 的 ORM：
 *     MyBatis-Plus  ≈ Prisma / Sequelize / TypeORM
 *     Mapper 接口    ≈ model.findMany() / repository.findAll()
 *
 * 为什么需要这个配置类？
 *   MyBatis-Plus 的分页功能不是默认开启的，需要注册一个分页插件（拦截器）。
 *   这个拦截器会"拦截"所有查询 SQL，在需要分页时自动加上 LIMIT/OFFSET。
 *
 * 类比 JavaScript：
 *   // Prisma 中分页是内置的，不需要额外配置
 *   await prisma.student.findMany({ skip: 0, take: 10 })
 *   // 但 MyBatis-Plus 需要先注册分页拦截器才能用分页
 * ================================================================
 */

package com.example.backend.config;

// --- MyBatis-Plus 类 ---
import com.baomidou.mybatisplus.annotation.DbType;                                     // 数据库类型枚举（MySQL, Oracle, PostgreSQL...）
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;              // MyBatis-Plus 拦截器链（主拦截器）
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;     // 分页内部拦截器（真正的分页实现）

// --- Spring 注解 ---
import org.springframework.context.annotation.Bean;          // @Bean：注册 Bean
import org.springframework.context.annotation.Configuration; // @Configuration：配置类

/*
 * ==================== 拦截器模式 ====================
 *
 * "拦截器"是 AOP（面向切面编程）的一种应用。
 * 它会在目标方法执行前后插入额外的逻辑。
 *
 * MyBatis-Plus 的分页拦截器工作原理：
 *   1. Service 调用 mapper.selectPage(page, queryWrapper)
 *   2. MyBatis-Plus 拦截器拦截到这个调用
 *   3. 拦截器自动修改 SQL：
 *      原始:  SELECT * FROM student WHERE name LIKE '%张%'
 *      修改后: SELECT * FROM student WHERE name LIKE '%张%' LIMIT 0, 10
 *   4. 同时执行 COUNT 查询获取总记录数
 *   5. 返回分页结果（records + total + current + size）
 *
 * 这个修改对开发者透明：你写的还是 selectPage()，底层自动处理分页。
 *
 * 类比 JavaScript Express 中间件：
 *   app.use((req, res, next) => {
 *     // 拦截请求，添加额外逻辑
 *     req.startTime = Date.now()
 *     next() // 放行
 *   })
 */
@Configuration // Spring 配置类
public class MyBatisPlusConfig {

    /**
     * 配置 MyBatis-Plus 拦截器链
     * 注册分页插件，指定数据库类型为 MySQL
     *
     * @return MybatisPlusInterceptor 实例（会被注入到 MyBatis-Plus 框架中）
     */
    /*
     * public MybatisPlusInterceptor mybatisPlusInterceptor() { ... }
     *
     *   这个方法注册了一个 Bean，Spring 启动时会调用它。
     *   MyBatis-Plus 框架会自动发现并使用这个拦截器。
     *
     *   如果有多个拦截器，可以都 add 到同一个 MybatisPlusInterceptor 中。
     */
    @Bean // Spring 管理此对象的生命周期
    public MybatisPlusInterceptor mybatisPlusInterceptor() {

        /*
         * new MybatisPlusInterceptor()
         *
         *   创建一个拦截器链。可以往里面添加多个内部拦截器。
         *   这里只添加了分页拦截器，如果需要乐观锁、防止全表更新等，
         *   可以继续 add 对应的拦截器。
         */
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor(); // 创建拦截器链

        /*
         * new PaginationInnerInterceptor(DbType.MYSQL)
         *
         *   DbType.MYSQL：
         *     指定数据库类型为 MySQL。
         *     不同数据库的分页语法不同：
         *       MySQL:        LIMIT offset, size
         *       PostgreSQL:   OFFSET offset LIMIT size
         *       SQL Server:   OFFSET offset ROWS FETCH NEXT size ROWS ONLY
         *     MyBatis-Plus 根据 DbType 自动生成正确的分页 SQL。
         *
         *   setOverflow(true)：
         *     当请求的页码超过总页数时（如总共 3 页但请求第 10 页），
         *     自动回到第一页，避免返回空数据。
         */
        PaginationInnerInterceptor paginationInterceptor = new PaginationInnerInterceptor(DbType.MYSQL); // MySQL 分页拦截器
        paginationInterceptor.setOverflow(true); // 页码溢出保护：超过最大页数自动回第一页

        /*
         * interceptor.addInnerInterceptor(paginationInterceptor);
         *
         *   将分页拦截器加入拦截器链。
         *   如果有多个拦截器，按添加顺序依次执行。
         */
        interceptor.addInnerInterceptor(paginationInterceptor); // 添加分页拦截器

        return interceptor; // 返回配置好的拦截器链
    }

    /*
     * ================================================================
     * 这个配置类虽然代码不多，但非常关键！
     *
     * 如果不配这个类，在代码中调用：
     *   Page<Student> page = new Page<>(1, 10);
     *   studentMapper.selectPage(page, queryWrapper);
     *
     * 结果将是：返回全部数据（没有分页），因为 MyBatis-Plus 不知道要生成 LIMIT 子句。
     *
     * 配置后，同样的代码就能正确分页了。
     * ================================================================
     */
}
