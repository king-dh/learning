/*
 * ================================================================
 * 文件位置:  src/main/java/com/example/backend/mapper/ScoreMapper.java
 * 对应 Entity: Score.java
 * 对应数据库表: score
 * 在架构中的位置:
 *
 *   Controller → Service → Mapper(当前文件) → 数据库 score 表
 *
 * Mapper（数据访问接口）= score（成绩）表的数据库操作接口
 *
 * JS 类比：
 *   const ScoreMapper = {
 *     // 以下是继承 BaseMapper 自动获得的（不用写）：
 *     insert(score) { ... },            // 录入成绩
 *     selectById(id) { ... },           // 按ID查成绩
 *     updateById(score) { ... },        // 修改成绩
 *     deleteById(id) { ... },           // 删除成绩
 *
 *     // 以下是自己定义的方法：
 *     selectByStudentId(studentId) { ... }  // 按学生ID查所有成绩（联表）
 *   }
 *
 * 联表查询是什么？
 *   selectByStudentId 方法不返回 Score 实体，而是返回 ScoreVO（视图对象）。
 *   因为成绩表只存了 studentId 和 courseId（数字），
 *   但前端需要显示学生姓名和课程名称（文字），
 *   所以需要在 SQL 中用 JOIN 把 student 和 course 表的数据也查出来。
 *
 *   这就是 VO（View Object / 视图对象）的作用：
 *     Entity（Score）: 只包含数据库列本身的数据
 *     VO（ScoreVO）  : 包含联表查询后的展示数据，比 Entity 多了学生名和课程名
 *
 *   类比：SQL JOIN 就像 JS 中的：
 *     const scoresWithNames = scores.map(s => ({
 *       ...s,
 *       studentName: students.find(st => st.id === s.studentId).name,
 *       courseName: courses.find(c => c.id === s.courseId).name
 *     }))
 * ================================================================
 */

// ==================== 1. 包声明 ====================
package com.example.backend.mapper; // mapper 子包（数据访问层）

// ==================== 2. 导入其他类 ====================

import com.baomidou.mybatisplus.core.mapper.BaseMapper; // MyBatis-Plus 基础 Mapper（通用 CRUD）
import com.example.backend.entity.Score;                  // Score 实体类（成绩表映射）
import com.example.backend.vo.ScoreVO;                   // ScoreVO 视图对象（联表查询结果，多列）
import org.apache.ibatis.annotations.Mapper;              // MyBatis Mapper 标识注解
import org.apache.ibatis.annotations.Param;               // 参数绑定注解
import org.apache.ibatis.annotations.Select;              // 自定义查询注解
import java.util.List;                                    // Java 列表接口

// ==================== 3. 接口的声明和注解 ====================

/*
 * @Mapper
 *   标记为 MyBatis Mapper 接口，由框架自动创建代理实现。
 *   没有这个注解，Spring 不会管理这个接口，调用时会报错。
 */
@Mapper

/*
 * public interface ScoreMapper extends BaseMapper<Score>
 *
 *   继承 BaseMapper<Score> 获得所有标准 CRUD 方法：
 *     insert(Score)、deleteById(Long)、updateById(Score)、
 *     selectById(Long)、selectList(Wrapper)、selectPage(...) 等。
 *
 *   同时声明了一个自定义方法 selectByStudentId，用于联表查询。
 */
public interface ScoreMapper extends BaseMapper<Score> {

    // ==================== 4. 自定义查询方法 ====================

    /*
     * @Select 注解内容拆解（逐行解释）：
     *
     *   SELECT s.*, st.name AS student_name, c.name AS course_name
     *     ↑ 查询三部分数据：
     *       s.*           → score 表的所有列（id, student_id, course_id, score, semester, create_time）
     *       st.name AS student_name → student 表的 name 列，用 AS 起别名为 student_name
     *       c.name AS course_name  → course 表的 name 列，用 AS 起别名为 course_name
     *     ↑ "AS xxx" 别名的作用：
     *       查询结果的列名就叫 student_name 和 course_name，
     *       MyBatis-Plus 会按这个列名自动映射到 ScoreVO 对象的对应字段。
     *
     *   FROM score s
     *     ↑ 主表是 score（缩写为 s，方便后面引用）
     *     ↑ "s" 是表的别名（alias），相当于给它起了个短名字
     *
     *   LEFT JOIN student st ON s.student_id = st.id
     *     ↑ 左连接 student 表（q 缩写为 st）
     *     ↑ ON s.student_id = st.id → 连接条件：成绩的学生ID = 学生表的主键ID
     *     ↑ LEFT JOIN 的特点：
     *       即使 student 表中找不到对应学生（数据异常），成绩记录也会保留。
     *       如果是 INNER JOIN，学生被删了，成绩也会跟着消失，所以用 LEFT JOIN 更安全。
     *
     *   LEFT JOIN course c ON s.course_id = c.id
     *     ↑ 左连接 course 表（缩写为 c）
     *     ↑ ON s.course_id = c.id → 连接条件：成绩的课程ID = 课程表的主键ID
     *     ↑ 同样用 LEFT JOIN，保证课程的删除不影响已有成绩记录。
     *
     *   WHERE s.student_id = #{studentId}
     *     ↑ 条件过滤：只查指定学生ID的成绩
     *     ↑ #{studentId} 是 MyBatis 的参数占位符，会被替换为实际的方法参数值
     *     ↑ 这是预编译参数（PreparedStatement），安全防 SQL 注入
     *
     *   完整执行效果（假设 studentId = 10）：
     *     SELECT s.*, st.name AS student_name, c.name AS course_name
     *     FROM score s
     *     LEFT JOIN student st ON s.student_id = st.id
     *     LEFT JOIN course c ON s.course_id = c.id
     *     WHERE s.student_id = 10
     *
     *   返回的数据行示例：
     *     | id | student_id | course_id | score | semester    | ... | student_name | course_name |
     *     | 1  | 10         | 5         | 85.5  | 2024-2025-1 | ... | 张三         | 高等数学     |
     *     | 2  | 10         | 8         | 92.0  | 2024-2025-1 | ... | 张三         | 大学英语     |
     *
     *   MyBatis 会自动将每行数据映射为 ScoreVO 对象：
     *     ScoreVO { id:1, score:85.5, studentName:"张三", courseName:"高等数学", ... }
     *
     * ---------------------------------------------------------------------------
     *
     * List<ScoreVO> selectByStudentId(@Param("studentId") Long studentId);
     *
     *   逐词解释：
     *     List<ScoreVO>  —— 返回 ScoreVO 列表（不是 Score！因为联表了，多了字段）
     *     selectByStudentId —— 方法名：根据学生ID查询成绩（命名规范：select + By + 条件）
     *     @Param("studentId") —— 把参数绑定到 SQL 的 #{studentId}
     *     Long studentId —— 学生的主键 ID
     *
     *   为什么返回 ScoreVO 而不是 Score？
     *     Score 实体只有 6 个字段（对应 score 表的列）。
     *     ScoreVO 有更多字段（除了 score 表的列，还有 studentName 和 courseName）。
     *     如果返回 Score，联表查出来的 studentName 和 courseName 就没地方放了。
     *
     *   JS 类比：
     *     function selectByStudentId(studentId) {
     *       return db.query(`
     *         SELECT s.*, st.name AS student_name, c.name AS course_name
     *         FROM score s
     *         LEFT JOIN student st ON s.student_id = st.id
     *         LEFT JOIN course c ON s.course_id = c.id
     *         WHERE s.student_id = ?
     *       `, [studentId])
     *     }
     */
    @Select("SELECT s.*, st.name AS student_name, c.name AS course_name " +
            "FROM score s " +
            "LEFT JOIN student st ON s.student_id = st.id " +
            "LEFT JOIN course c ON s.course_id = c.id " +
            "WHERE s.student_id = #{studentId}")
    List<ScoreVO> selectByStudentId(@Param("studentId") Long studentId);

    /*
     * ================================================================
     * 总结：ScoreMapper = score 表的数据库操作接口
     *
     * 自动获得的方法（来自 BaseMapper<Score>）：
     *   insert(Score)、deleteById(Long)、updateById(Score)、
     *   selectById(Long)、selectList(Wrapper)、selectPage(...)
     *
     * 自定义的方法：
     *   selectByStudentId(Long studentId)
     *     → 按学生ID联表查询成绩（LEFT JOIN student + LEFT JOIN course）
     *     → 返回 ScoreVO 列表（包含学生名和课程名）
     * ================================================================
     */
}
