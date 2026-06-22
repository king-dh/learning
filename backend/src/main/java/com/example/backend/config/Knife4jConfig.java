/*
 * ================================================================
 * 文件位置:  src/main/java/com/example/backend/config/Knife4jConfig.java
 * 所在层:    Config 层（配置层）
 *
 * 职责说明:
 *   配置 Knife4j（增强版 Swagger）自动生成 API 接口文档。
 *   项目启动后访问 http://localhost:8088/doc.html 即可看到可视化接口文档。
 *
 * 什么是 Knife4j？
 *   Knife4j 是 Swagger/OpenAPI 的增强版，在 Spring Boot 中自动生成接口文档。
 *   它会扫描所有 Controller 中的注解（@Tag/@Operation），生成漂亮的 Web UI 页面。
 *
 * 类比 JavaScript 项目：
 *   npm install swagger-jsdoc swagger-ui-express
 *   const swaggerSpec = swaggerJsdoc({
 *     definition: { info: { title: 'API', version: '1.0' } },
 *     apis: ['./routes/*.js']
 *   })
 *   app.use('/api-docs', swaggerUi.serve, swaggerUi.setup(swaggerSpec))
 *
 * @Configuration 注解：
 *   告诉 Spring："这是一个配置类，启动时读取并执行"。
 *   相当于 JavaScript 中的"初始化配置文件"。
 * ================================================================
 */

package com.example.backend.config;

// --- OpenAPI 3 规范类（Swagger 使用的标准）---
import io.swagger.v3.oas.models.OpenAPI;          // OpenAPI 文档对象（根对象）
import io.swagger.v3.oas.models.info.Contact;      // 联系人信息（团队名称、邮箱）
import io.swagger.v3.oas.models.info.Info;         // API 基本信息（标题、版本、描述）
import io.swagger.v3.oas.models.info.License;      // 许可证信息（Apache 2.0 等）

// --- Spring 注解 ---
import org.springframework.context.annotation.Bean;          // @Bean：声明方法返回的对象由 Spring 管理
import org.springframework.context.annotation.Configuration; // @Configuration：声明此类是 Spring 配置类

/*
 * ==================== @Configuration 和 @Bean 的含义 ====================
 *
 * @Configuration：
 *   把这个类标记为"Spring 配置类"，类似于 XML 配置文件的 Java 替代版。
 *   Spring 在启动时会自动读取这类，执行其中的 @Bean 方法。
 *
 * @Bean：
 *   标记在方法上，表示"这个方法的返回值需要注册为 Spring Bean"。
 *   之后其他地方可以通过 @Autowired 或构造函数注入来使用这个 Bean。
 *
 *   类比 JavaScript：
 *     // 注册一个全局服务
 *     app.set('swaggerConfig', new OpenAPI())
 *     // 其他模块中使用
 *     const swaggerConfig = app.get('swaggerConfig')
 *
 * Spring 容器（IoC 容器）：
 *   你可以把 Spring 想象成一个"大仓库"，@Bean 方法就像把东西放入仓库，
 *   其他地方需要时直接从仓库取出（依赖注入）。
 */
@Configuration // Spring 配置类
public class Knife4jConfig {

    /**
     * 配置 OpenAPI 文档的基本信息
     *
     * @return OpenAPI 实例（会被注册为 Spring Bean）
     */
    /*
     * public OpenAPI customOpenAPI() { ... }
     *
     *   方法名 customOpenAPI 不重要（可以是任何名字），
     *   重要的是 @Bean 注解，告诉 Spring 把返回值注册为 Bean。
     *
     *   这里使用了"链式调用"（Builder/Fluent 模式）：
     *     new OpenAPI()
     *       .info(new Info()
     *         .title("xxx")
     *         .version("1.0"))
     *
     *   链式调用的特点：每个方法返回 this（自身对象），所以可以连续调用。
     *
     *   类比 JavaScript 的链式调用：
     *     new OpenAPI()
     *       .setInfo(new Info()
     *         .setTitle('API')
     *         .setVersion('1.0'))
     *   Java 中用 . 代替 set 前缀，更简洁。
     */
    @Bean // 注册为 Spring Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI() // 创建 OpenAPI 3 文档对象
                .info(new Info() // 设置文档信息部分
                        .title("学生教育管理系统API")       // 文档标题（显示在页面顶部）
                        .description("学生教育管理系统后端接口文档，包含学生、教师、课程、成绩、选课等管理功能") // 文档描述
                        .version("1.0.0")                  // API 版本号
                        .contact(new Contact()              // 联系信息
                                .name("开发团队")           // 团队名称
                                .email("admin@example.com")) // 联系邮箱
                        .license(new License()              // 许可证
                                .name("Apache 2.0")         // 许可证名称
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html"))); // 许可证 URL
    }

    /*
     * ================================================================
     * 如何使用生成的接口文档？
     *
     *   1. 启动项目（run BackendApplication）
     *   2. 打开浏览器访问 http://localhost:8088/doc.html
     *   3. 看到"学生教育管理系统API"标题
     *   4. 左侧有所有 Controller 的分组（学生管理、教师管理...）
     *   5. 点击接口可以看到请求参数和响应格式
     *   6. 可以在页面上直接测试接口（发送请求、查看响应）
     *
     * 文档数据来源：
     *   - @Tag(name = "学生管理")      → 左侧分组名
     *   - @Operation(summary = "分页查询") → 接口标题
     *   - DTO 和 VO 类的字段            → 请求/响应参数表
     *   - 由 Knife4j 自动扫描生成，无需手写文档
     * ================================================================
     */
}
