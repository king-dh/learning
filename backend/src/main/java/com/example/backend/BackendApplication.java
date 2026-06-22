/*
 * ================================================================
 * 文件位置:  src/main/java/com/example/backend/BackendApplication.java
 * 所在层:    项目根包（启动入口）
 *
 * 职责说明:
 *   Spring Boot 应用程序的"总开关"。
 *   这个类只有一个 main 方法，但它启动了整个 Spring 生态系统。
 *
 * 类比 JavaScript Node.js 项目：
 *   // package.json 的 "main": "app.js"
 *   // app.js
 *   const express = require('express')
 *   const app = express()
 *   // ... 各种中间件和路由
 *   app.listen(8088, () => console.log('Server started'))
 *
 *   SpringApplication.run() 相当于上面的所有初始化代码 + app.listen()
 *
 * 启动过程（SpringApplication.run 做了哪些事）：
 *   1. 创建 Spring IoC 容器（ApplicationContext）— "大仓库"
 *   2. 扫描所有 @Component/@Service/@Controller/@Configuration 注解的类
 *   3. 创建所有 Bean 的实例 → 完成依赖注入（构造函数注入、字段注入）
 *   4. 执行 @PostConstruct 初始化方法（如 JwtUtil.init()）
 *   5. 执行 CommandLineRunner（如 DataInitializer.run()）
 *   6. 启动内嵌 Tomcat 服务器 → 监听 8088 端口
 *   7. 打印启动日志：Started BackendApplication in X.XXX seconds
 *
 * ================================================================
 */

package com.example.backend;

// --- SpringDoc / OpenAPI ---
/*
 * @OpenAPIDefinition：
 *   定义 OpenAPI 3 文档的全局信息。
 *   和 Knife4jConfig 中的配置类似，但这里是注解方式。
 *   实际上 Knife4jConfig 中定义的会覆盖这里的部分配置。
 *   两者都可以定义，但通常会保留一个。
 */
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;

// --- MyBatis ---
/*
 * @MapperScan("com.example.backend.mapper")：
 *   告诉 MyBatis-Plus："去 com.example.backend.mapper 包下扫描所有 Mapper 接口"。
 *   如果没有这个注解，所有 Mapper 接口都不会被注册，数据库操作全部失败！
 *
 *   原理：
 *     MyBatis-Plus 会扫描指定包下的接口 → 用 JDK 动态代理生成实现类 → 注册为 Spring Bean。
 *     所以你才能在其他地方 @Autowired 注入 Mapper 接口。
 *
 *   类比 JavaScript：
 *     // 告诉 ORM 去哪里找 Model 定义
 *     sequelize.import('./models') // ← @MapperScan
 */
import org.mybatis.spring.annotation.MapperScan;

// --- Spring Boot ---
/*
 * SpringApplication：
 *   Spring Boot 的核心启动类。
 *   它的 run() 方法做了上文描述的所有初始化工作。
 *
 * @SpringBootApplication：
 *   这是一个"组合注解"，等于同时加了：
 *     @SpringBootConfiguration   — 声明这是 Spring 配置类
 *     @EnableAutoConfiguration   — 启用"自动配置"（Spring Boot 最大卖点）
 *     @ComponentScan             — 扫描当前包及子包的所有组件
 *
 *   其中最重要的 @EnableAutoConfiguration：
 *     它会根据 pom.xml 中引入的依赖，自动配置对应的功能。
 *     比如：
 *       - 引入了 spring-boot-starter-web → 自动启动内嵌 Tomcat
 *       - 引入了 mybatis-plus-boot-starter → 自动配置 MyBatis
 *       - 引入了 spring-boot-starter-security → 自动配置 Spring Security
 *     不需要手写 XML 配置，开箱即用！
 *
 *   类比 JavaScript：
 *     create-react-app 自动配置 webpack、babel 等 → 你只管写组件
 *     Vite 自动配置开发服务器、HMR → 你只管写代码
 *     Spring Boot 也类似：引入依赖 = 自动配置，无需手写样板代码
 */
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// ==================== 类声明 ====================

/*
 * @OpenAPIDefinition：
 *   定义 API 文档的全局元数据。
 *   这些信息会显示在 Swagger UI 页面顶部。
 *   和 Knife4jConfig 中定义的基本相同，加在这里也是为了文档标准化。
 */
@OpenAPIDefinition(
        info = @Info( // 文档信息对象（@Info 是注解中的注解）
                title = "学生教育管理系统 API",          // API 文档标题
                version = "1.0.0",                       // API 版本
                description = "基于 Spring Boot + Vue3 的学生教育管理系统，包含学生、教师、课程、成绩、选课等管理功能",
                contact = @Contact(                       // 联系人
                        name = "学习项目",
                        email = "admin@example.com"),
                license = @License(                       // 许可证
                        name = "Apache 2.0",
                        url = "https://www.apache.org/licenses/LICENSE-2.0")
        )
)

/*
 * @SpringBootApplication：
 *   核心注解，声明这是 Spring Boot 应用的入口类。
 *   必须放在基础包（com.example.backend）下，这样 @ComponentScan 才能扫描到所有子包。
 *   如果放在子包中（如 controller 包下），其他包中的组件就不会被扫描到。
 */
@SpringBootApplication

/*
 * @MapperScan("com.example.backend.mapper")：
 *   扫描 Mapper 接口包。
 *   路径要和项目结构一致：src/main/java/com/example/backend/mapper/
 *
 *   MyBatis-Plus 会为每个 Mapper 接口生成 JDK 动态代理实现：
 *     StudentMapper 接口 → $Proxy102（动态代理类） → Spring Bean
 *
 *   如果这里路径写错，所有 Mapper 都不会注册，项目启动时不会有任何提示，
 *   但运行时调用 Mapper 方法会抛 NullPointerException。
 */
@MapperScan("com.example.backend.mapper")
public class BackendApplication {

    /**
     * Java 应用程序的主入口方法
     *
     * public static void main(String[] args)：
     *   这是 JVM（Java 虚拟机）启动时调用的第一个方法。
     *   没有这个方法，程序无法运行。
     *
     *   类比 JavaScript：
     *     // Node.js 中不需要显式声明 main 函数
     *     // 文件被加载时自动执行
     *     // 但 Java 必须有一个 public static void main(String[] args) 方法
     *
     * main 方法签名解释：
     *   public   — 公开（JVM 需要调用它）
     *   static   — 静态（不需要创建对象就能调用，JVM 不创建对象）
     *   void     — 无返回值
     *   main     — 固定的方法名（JVM 约定）
     *   String[] args — 命令行参数（运行 java 命令时传入的参数）
     *
     * @param args 命令行参数（通常为空，但可以通过 IDE 或命令行传入）
     */
    public static void main(String[] args) {
        /*
         * SpringApplication.run(BackendApplication.class, args);
         *
         *   参数解释：
         *     BackendApplication.class — 告诉 Spring："从这个类的包开始扫描组件"
         *     args                     — 命令行参数（传给 Spring 处理）
         *
         *   这行代码做的事：
         *     1. 创建 Spring IoC 容器
         *     2. 扫描所有组件（@Component/@Service/@Controller/@Configuration）
         *     3. 创建所有 Bean 实例，完成依赖注入
         *     4. 执行初始化回调（@PostConstruct, CommandLineRunner）
         *     5. 启动内嵌 Tomcat 服务器（监听 8088 端口）
         *     6. 程序进入"就绪"状态，等待 HTTP 请求
         *
         *   类比 JavaScript：
         *     const app = express()
         *     // ... 配置各种中间件和路由
         *     app.listen(8088, () => console.log('Server started'))
         *     // SpringApplication.run() ≈ 上面所有代码的总和
         *
         *   注意：run() 方法会阻塞当前线程，直到程序被关闭（Ctrl+C）。
         *   所以 main 方法执行到这里就不会再往下走了。
         */
        SpringApplication.run(BackendApplication.class, args);
    }

    /*
     * ================================================================
     * 启动后的验证：
     *
     *   1. 启动成功 → 控制台输出 "Started BackendApplication in X.XXX seconds"
     *   2. 访问 http://localhost:8088/swagger-ui.html → 看到 API 文档
     *   3. 访问 http://localhost:8088/doc.html → 看到 Knife4j 增强文档
     *   4. 数据库自动初始化测试数据（DataInitializer）
     *   5. 用 admin/admin123 登录 → 获取 Token → 测试其他接口
     *
     * 如果启动失败，常见原因：
     *   - 端口 8088 被占用 → 修改 application.yml 中的 server.port
     *   - 数据库连接失败 → 检查 MySQL 是否启动，密码是否正确
     *   - Mapper 接口未注册 → 检查 @MapperScan 路径是否正确
     * ================================================================
     */
}
