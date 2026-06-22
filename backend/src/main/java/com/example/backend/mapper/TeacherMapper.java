package com.example.backend.mapper; // 声明 Mapper 接口包

import com.baomidou.mybatisplus.core.mapper.BaseMapper; // MyBatis-Plus 基础 Mapper
import com.example.backend.entity.Teacher; // Teacher 实体类
import org.apache.ibatis.annotations.Mapper; // MyBatis Mapper 标识注解

/**
 * 教师 Mapper 接口
 * 继承 BaseMapper 获得通用 CRUD 方法
 */
@Mapper // 标记为 MyBatis Mapper
public interface TeacherMapper extends BaseMapper<Teacher> {
    // 基础 CRUD 方法由 BaseMapper 自动提供
}
