package com.example.backend.mapper; // 声明 Mapper 接口包

import com.baomidou.mybatisplus.core.mapper.BaseMapper; // MyBatis-Plus 基础 Mapper
import com.example.backend.entity.Score; // Score 实体类
import com.example.backend.vo.ScoreVO; // ScoreVO 视图对象
import org.apache.ibatis.annotations.Mapper; // MyBatis Mapper 标识注解
import org.apache.ibatis.annotations.Param; // 参数绑定注解
import org.apache.ibatis.annotations.Select; // 自定义查询注解

import java.util.List; // Java 集合框架

/**
 * 成绩 Mapper 接口
 * 包含基础 CRUD 和按学生ID联表查询的方法
 */
@Mapper // 标记为 MyBatis Mapper
public interface ScoreMapper extends BaseMapper<Score> {

    /**
     * 根据学生 ID 查询该学生的所有成绩（联表查询课程名称和学生姓名）
     * LEFT JOIN 确保即使课程或学生被删除，成绩记录也不会丢失
     * @param studentId 学生 ID
     * @return 成绩视图对象列表（包含学生名和课程名）
     */
    @Select("SELECT s.*, st.name AS student_name, c.name AS course_name " + // 联表查询，用 AS 起别名映射到 ScoreVO 字段
            "FROM score s " +                                                // 主表：score
            "LEFT JOIN student st ON s.student_id = st.id " +               // 左连接 student 表获取学生姓名
            "LEFT JOIN course c ON s.course_id = c.id " +                   // 左连接 course 表获取课程名称
            "WHERE s.student_id = #{studentId}")                            // 按学生ID过滤
    List<ScoreVO> selectByStudentId(@Param("studentId") Long studentId);    // 返回 ScoreVO 列表（包含联表字段）
}
