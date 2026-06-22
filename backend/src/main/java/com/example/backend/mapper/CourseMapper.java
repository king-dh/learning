/*
 * ================================================================
 * 文件位置:  src/main/java/com/example/backend/mapper/CourseMapper.java
 * 对应 Entity: Course.java
 * 对应数据库表: course
 * 在架构中的位置:
 *
 *   Controller → Service → Mapper(当前文件) → 数据库 course 表
 *
 * Mapper（数据访问接口）= course（课程）表的数据库操作接口
 *
 * JS 类比：
 *   const CourseMapper = {
 *     // 以下来自 BaseMapper 自动获得：
 *     insert(course) { ... },           // 新增课程
 *     selectById(id) { ... },           // 按ID查课程
 *     updateById(course) { ... },       // 修改课程
 *     deleteById(id) { ... },           // 删除课程
 *     selectList(wrapper) { ... },      // 查课程列表（可加条件）
 *
 *     // 自定义方法：
 *     selectByTeacherId(teacherId) { ... }  // 按教师ID查询课程列表
 *   }
 * ================================================================
 */

// ==================== 1. 包声明 ====================
package com.example.backend.mapper; // mapper 子包（数据访问层）

// ==================== 2. 导入其他类 ====================

import com.baomidou.mybatisplus.core.mapper.BaseMapper; // MyBatis-Plus 基础 Mapper，提供通用 CRUD 方法
import com.example.backend.entity.Course;                 // Course 实体类（课程表映射）
import org.apache.ibatis.annotations.Mapper;              // MyBatis Mapper 标识注解
import org.apache.ibatis.annotations.Param;               // 参数绑定注解：绑定方法参数到 SQL 占位符
import org.apache.ibatis.annotations.Select;              // 自定义查询注解：方法上直接写 SQL

import java.util.List; // Java 列表接口（类似 JS 的 Array）

// ==================== 3. 接口的声明和注解 ====================

/*
 * @Mapper
 *   告诉 MyBatis：扫描并管理这个接口，为它生成代理实现。
 *
 *   BaseMapper 提供了课程表的所有基本操作，加上一个自定义的按教师查询方法。
 */
@Mapper

/*
 * public interface CourseMapper extends BaseMapper<Course>
 *
 *   继承 BaseMapper<Course> 自动获得所有标准 CRUD 方法。
 *   自定义了 selectByTeacherId 方法，用于查询某个教师教的所有课程。
 */
public interface CourseMapper extends BaseMapper<Course> {

    // ==================== 4. 自定义查询方法 ====================

    /*
     * @Select("SELECT * FROM course WHERE teacher_id = #{teacherId}")
     *
     *   这个 SQL 比较简单，没有 JOIN：
     *     SELECT * FROM course            -- 查询 course 表的所有列
     *     WHERE teacher_id = #{teacherId} -- 条件：只查指定教师的课程
     *
     *   #{teacherId} 是 MyBatis 的预编译参数占位符：
     *     - 会被替换为方法参数的实际值
     *     - 使用 PreparedStatement（数据库预编译），防止 SQL 注入
     *     - 和 JDBC 的 ? 占位符等价，但功能更强
     *
     *   SQL 注入科普（为什么不用 ${...} 拼接？）：
     *     ❌ 危险写法：@Select("SELECT * FROM course WHERE teacher_id = ${teacherId}")
     *       如果 teacherId = "1 OR 1=1"，SQL 变成：
     *         SELECT * FROM course WHERE teacher_id = 1 OR 1=1  → 查出所有课程！
     *     ✅ 安全写法：@Select("SELECT * FROM course WHERE teacher_id = #{teacherId}")
     *       使用预编译，参数值被当作数据处理，不会被当作 SQL 代码执行。
     *
     *   JS 类比：
     *     function selectByTeacherId(teacherId) {
     *       return db.query(
     *         "SELECT * FROM course WHERE teacher_id = ?",
     *         [teacherId]  // ← 参数化查询，防 SQL 注入
     *       )
     *     }
     *
     * ---------------------------------------------------------------------------
     *
     * List<Course> selectByTeacherId(@Param("teacherId") Long teacherId);
     *
     *   逐词解释：
     *     List<Course>      —— 返回课程列表（每个元素是 Course 对象）
     *     selectByTeacherId —— 方法名：按教师ID查询课程
     *     @Param("teacherId") —— 把 teacherId 参数绑定到 SQL 的 #{teacherId}
     *     Long teacherId    —— 教师的数据库主键 ID
     *
     *   使用场景：
     *     教师登录后查看"我的课程"列表，系统根据当前登录教师的 ID，
     *     调用 selectByTeacherId 查出该教师负责的所有课程。
     *
     *   和 BaseMapper 条件查询的区别：
     *     用 BaseMapper 也可以实现同样的功能：
     *       LambdaQueryWrapper<Course> wrapper = new LambdaQueryWrapper<>();
     *       wrapper.eq(Course::getTeacherId, teacherId);
     *       List<Course> courses = courseMapper.selectList(wrapper);
     *
     *     但用 @Select 直接写 SQL 更直观、更高效（减少了一层条件构造器的开销）。
     *     两种方式都可以，看个人偏好。简单条件用 @Select，复杂动态条件用 Wrapper。
     */
    @Select("SELECT * FROM course WHERE teacher_id = #{teacherId}")
    List<Course> selectByTeacherId(@Param("teacherId") Long teacherId);

    /*
     * ================================================================
     * 总结：CourseMapper = course 表的数据库操作接口
     *
     * 自动获得的方法（来自 BaseMapper<Course>）：
     *   insert(Course)、deleteById(Long)、updateById(Course)、
     *   selectById(Long)、selectList(Wrapper)、selectPage(...)
     *
     * 自定义的方法：
     *   selectByTeacherId(Long teacherId)
     *     → 按教师ID查询该教师的课程列表
     *     → 返回 List<Course>
     * ================================================================
     */
}
