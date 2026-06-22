package com.example.backend.config; // 声明配置类包

import io.swagger.v3.oas.models.OpenAPI; // OpenAPI 3 规范对象
import io.swagger.v3.oas.models.info.Contact; // 联系人信息
import io.swagger.v3.oas.models.info.Info; // API 基本信息
import io.swagger.v3.oas.models.info.License; // 许可证信息
import org.springframework.context.annotation.Bean; // Spring Bean 注解
import org.springframework.context.annotation.Configuration; // Spring 配置注解

/**
 * Knife4j（Swagger）API 文档配置类
 * 配置生成的 OpenAPI 文档的基本信息
 * 文档访问地址：http://localhost:8088/doc.html
 */
@Configuration // 声明这是一个 Spring 配置类
public class Knife4jConfig {

    /**
     * 配置 OpenAPI 文档信息
     * 标题、描述、版本、作者等基本信息会显示在 Knife4j 文档页面顶部
     * @return OpenAPI 实例
     */
    @Bean // 注册为 Spring Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI() // 创建 OpenAPI 3 对象
                .info(new Info() // 设置文档基本信息
                        .title("学生教育管理系统API") // 文档标题
                        .description("学生教育管理系统后端接口文档，包含学生、教师、课程、成绩、选课等管理功能") // 文档描述
                        .version("1.0.0") // 版本号
                        .contact(new Contact() // 作者联系方式
                                .name("开发团队") // 团队名称
                                .email("admin@example.com")) // 邮箱
                        .license(new License() // 许可证信息
                                .name("Apache 2.0") // 许可证名称
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html"))); // 许可证链接
    }
}
