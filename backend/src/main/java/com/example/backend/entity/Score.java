/*
 * ================================================================
 * 文件位置:  src/main/java/com/example/backend/entity/Score.java
 * 对应数据库表: score（成绩表）
 * 在架构中的位置:
 *
 *   前端 Vue → Controller → Service → Mapper → Entity(当前文件) → 数据库 score 表
 *
 * Entity（实体类）= 数据库成绩表的 Java 对象映射
 *
 * 这张表记录的是"某个学生在某个学期某门课的考试成绩"。
 * 它是学生和课程之间的另一个关联表，但与 course_enrollment（选课表）不同：
 *   - course_enrollment：记录选课关系（选了没选）
 *   - score：记录考试成绩（考了多少分）
 *
 * JS 类比：
 *   const score = {
 *     id: 1,
 *     studentId: 10,        // 学生 ID = 10
 *     courseId: 5,          // 课程 ID = 5
 *     score: 85.5,          // 考了 85.5 分
 *     semester: "2024-2025-1", // 2024-2025学年第一学期
 *     createTime: "2025-01-15T00:00:00"  // 成绩录入时间
 *   }
 *   // 含义：学生 10 在 2024-2025学年第一学期 的课程 5 中考了 85.5 分
 *
 * 数据流转（以查询某学生的成绩为例）:
 *   前端请求 GET /api/scores?studentId=10
 *     → Controller → Service → Mapper.selectByStudentId(10)
 *     → SQL：SELECT s.*, st.name AS student_name, c.name AS course_name
 *            FROM score s
 *            LEFT JOIN student st ON s.student_id = st.id
 *            LEFT JOIN course c ON s.course_id = c.id
 *            WHERE s.student_id = 10
 *     → 返回 ScoreVO 列表（包含学生姓名和课程名称，比 Entity 信息更丰富）
 * ================================================================
 */

// ==================== 1. 包声明 ====================
package com.example.backend.entity; // entity 子包（实体层，数据模型）

// ==================== 2. 导入其他类 ====================

import com.baomidou.mybatisplus.annotation.FieldFill;  // 字段自动填充策略
import com.baomidou.mybatisplus.annotation.IdType;     // 主键生成策略
import com.baomidou.mybatisplus.annotation.TableField;  // 字段-列映射
import com.baomidou.mybatisplus.annotation.TableId;     // 主键标识
import com.baomidou.mybatisplus.annotation.TableName;   // 表名映射
import lombok.Data;                                     // 自动生成 getter/setter/toString
import java.time.LocalDateTime;                         // 日期时间类

// ==================== 3. 类的声明和注解 ====================

/*
 * @Data —— 编译时自动生成 getter/setter/toString/equals/hashCode。
 */
@Data

/*
 * @TableName("score")
 *   指定对应的数据库表名。Score 转小写是 score，和表名一致。
 *   显式写出让代码意图更清晰。
 */
@TableName("score")

/*
 * public class Score { ... }
 *
 *   每个 Score 实例 = score 表中的一条成绩记录。
 */
public class Score {

    // ==================== 4. 字段声明 ====================

    /*
     * @TableId(type = IdType.AUTO)
     *   主键，数据库自增。每次录入成绩，id 自动 +1。
     */
    @TableId(type = IdType.AUTO)
    private Long id; // 成绩记录唯一标识（主键）

    /*
     * private Long studentId;
     *
     *   外键：指向 student 表，表示这个成绩属于哪个学生。
     *   MyBatis-Plus 自动映射 studentId → student_id。
     */
    private Long studentId; // 学生 ID（外键关联 student 表）

    /*
     * private Long courseId;
     *
     *   外键：指向 course 表，表示这个成绩是哪门课的。
     */
    private Long courseId; // 课程 ID（外键关联 course 表）

    /*
     * private Double score;
     *
     *   Double —— 浮点数类型（可以为 null），对应数据库的 DOUBLE 或 DECIMAL。
     *   成绩通常范围 0-100，如 85.5、92.0。
     *
     *   注意：这个字段也叫 score，和类名 / 表名重复了，这是合法的。
     *   Java 中通过上下文区分：
     *     Score 类名   → 指的是这个类
     *     score 字段名 → 指的是 double 类型的成员变量
     */
    private Double score; // 考试分数，范围 0-100

    /*
     * private String semester;
     *
     *   学期信息。和 course 表中的 semester 字段含义相同，但这里存的是成绩所属学期。
     *   比如同一门课可能在不同学期开设，这个字段可以区分。
     */
    private String semester; // 学期，如 "2024-2025-1"（2024-2025学年第一学期）

    /*
     * @TableField(fill = FieldFill.INSERT)
     *   录入成绩时，自动填入当前时间作为成绩录入时间。
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime; // 成绩录入时间（插入时自动填入）

    /*
     * ================================================================
     * 总结：Score 实体类 = score 表的 Java 对象映射
     *
     * 数据库表 → Java 类映射：
     *   id          BIGINT PK AUTO_INCREMENT  →  Long id + @TableId(AUTO)
     *   student_id  BIGINT                    →  Long studentId
     *   course_id   BIGINT                    →  Long courseId
     *   score       DOUBLE                    →  Double score
     *   semester    VARCHAR                   →  String semester
     *   create_time DATETIME                  →  LocalDateTime createTime + @TableField(fill=INSERT)
     *
     * 关联关系：
     *   studentId  →  Student.id  （成绩属于一个学生）
     *   courseId   →  Course.id   （成绩对应一门课程）
     * ================================================================
     */
}
