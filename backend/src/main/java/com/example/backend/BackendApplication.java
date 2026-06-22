package com.example.backend; // 声明包名，与目录结构对应

import io.swagger.v3.oas.annotations.OpenAPIDefinition; // OpenAPI 文档定义注解
import io.swagger.v3.oas.annotations.info.Contact; // 联系人信息注解
import io.swagger.v3.oas.annotations.info.Info; // 文档信息注解
import io.swagger.v3.oas.annotations.info.License; // 许可证信息注解
import org.mybatis.spring.annotation.MapperScan; // 导入 MyBatis Mapper 扫描注解
import org.springframework.boot.SpringApplication; // 导入 Spring Boot 启动类
import org.springframework.boot.autoconfigure.SpringBootApplication; // 导入 Spring Boot 自动配置注解

/**
 * Spring Boot 应用程序主入口类
 * @SpringBootApplication 是一个组合注解，包含以下三个注解的功能：
 * - @SpringBootConfiguration：标识这是一个配置类
 * - @EnableAutoConfiguration：启用 Spring Boot 自动配置机制
 * - @ComponentScan：自动扫描当前包及其子包中的组件
 */
@OpenAPIDefinition( // 定义 OpenAPI 文档的基本信息
        info = @Info( // 文档信息对象
                title = "学生教育管理系统 API", // 文档标题
                version = "1.0.0", // API 版本号
                description = "基于 Spring Boot + Vue3 的学生教育管理系统，包含学生、教师、课程、成绩、选课等管理功能", // 文档描述
                contact = @Contact(name = "学习项目", email = "admin@example.com"), // 联系人信息
                license = @License(name = "Apache 2.0", url = "https://www.apache.org/licenses/LICENSE-2.0") // 许可证
        )
)
@SpringBootApplication // 标识这是 Spring Boot 应用的主配置类
@MapperScan("com.example.backend.mapper") // 扫描 MyBatis Mapper 接口所在包，自动生成代理实现
public class BackendApplication {

    /**
     * Java 应用程序的主方法（入口点）
     * 这是 JVM 启动时第一个执行的方法
     */
    public static void main(String[] args) { // main 方法，程序入口，接收命令行参数
        SpringApplication.run(BackendApplication.class, args); // 启动 Spring Boot 应用，加载整个 Spring 容器
    }
}
