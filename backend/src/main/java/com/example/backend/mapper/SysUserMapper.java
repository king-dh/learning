package com.example.backend.mapper; // 声明 Mapper 接口包

import com.baomidou.mybatisplus.core.mapper.BaseMapper; // MyBatis-Plus 基础 Mapper，提供通用 CRUD 方法
import com.example.backend.entity.SysUser; // SysUser 实体类
import org.apache.ibatis.annotations.Mapper; // MyBatis Mapper 标识注解

/**
 * 系统用户 Mapper 接口
 * 继承 BaseMapper<实体类型> 后自动获得 insert/update/delete/selectById/selectList 等通用方法
 */
@Mapper // 标记为 MyBatis Mapper，会被 Spring 扫描并创建代理实现
public interface SysUserMapper extends BaseMapper<SysUser> { // BaseMapper<实体类型> 泛型参数指定操作的实体
    // 继承 BaseMapper 后，不需要写任何方法就能使用通用 CRUD
    // 如有复杂查询需求，可在此添加自定义方法
}
