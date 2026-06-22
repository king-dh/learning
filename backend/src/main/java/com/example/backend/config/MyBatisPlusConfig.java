package com.example.backend.config; // 声明配置类包

import com.baomidou.mybatisplus.annotation.DbType; // 数据库类型枚举
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor; // MyBatis-Plus 拦截器
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor; // 分页拦截器
import org.springframework.context.annotation.Bean; // Spring Bean 注解
import org.springframework.context.annotation.Configuration; // Spring 配置注解

/**
 * MyBatis-Plus 配置类
 * 主要配置分页插件，使分页查询功能生效
 * MyBatis-Plus 的分页功能需要通过插件实现
 */
@Configuration // 声明这是一个 Spring 配置类
public class MyBatisPlusConfig {

    /**
     * 配置 MyBatis-Plus 拦截器
     * 添加分页插件，指定数据库类型为 MySQL
     * @return MybatisPlusInterceptor 实例
     */
    @Bean // 将方法返回值注册为 Spring Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor(); // 创建拦截器实例
        // 添加分页拦截器，指定数据库类型
        PaginationInnerInterceptor paginationInterceptor = new PaginationInnerInterceptor(DbType.MYSQL); // MySQL 分页
        paginationInterceptor.setOverflow(true); // 页码溢出时自动回到第一页
        interceptor.addInnerInterceptor(paginationInterceptor); // 将分页拦截器添加到拦截器链中
        return interceptor; // 返回配置好的拦截器
    }
}
