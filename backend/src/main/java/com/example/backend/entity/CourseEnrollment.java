/*
 * ================================================================
 * 文件位置:  src/main/java/com/example/backend/entity/CourseEnrollment.java
 * 对应数据库表: course_enrollment（选课记录表）
 * 在架构中的位置:
 *
 *   前端 Vue → Controller → Service → Mapper → Entity(当前文件) → 数据库 course_enrollment 表
 *
 * Entity（实体类）= 数据库选课记录表的 Java 对象映射
 *
 * 这张表是什么？
 *   course_enrollment 是一张"中间表"（关联表 / Junction Table），
 *   用来记录"哪个学生选了哪门课"。
 *
 *   它是学生（student）和课程（course）之间的"多对多"关系的桥梁：
 *     一个学生可以选多门课 → course_enrollment 里有这个学生的多条记录
 *     一门课可以有多个学生 → course_enrollment 里有这门课的多条记录
 *
 *   数据库关系图：
 *
 *     ┌─────────┐      ┌───────────────────┐      ┌─────────┐
 *     │ student │      │ course_enrollment │      │ course  │
 *     ├─────────┤      ├───────────────────┤      ├─────────┤
 *     │ id      │◄─────│ student_id (FK)   │      │ id      │
 *     │ name    │      │ course_id  (FK)   │─────►│ name    │
 *     │ ...     │      │ create_time       │      │ ...     │
 *     └─────────┘      └───────────────────┘      └─────────┘
 *
 * JS 类比：
 *   // 前端选课数据可以表示为：
 *   const enrollment = {
 *     id: 1,
 *     studentId: 10,   // 学生 ID = 10
 *     courseId: 5,     // 选了 ID = 5 的课
 *     createTime: "2024-09-15T10:00:00"  // 选课时间
 *   }
 *   // 含义：学生 10 在 2024年9月15日 选了课程 5
 *
 * 数据流转（以查询某学生的选课列表为例）:
 *   前端请求 GET /api/enrollments?studentId=10
 *     → Controller → Service → Mapper.selectByStudentId(10)
 *     → MyBatis 执行 SQL（联表查询 course_enrollment + student + course + teacher）
 *     → 每行结果映射为 EnrollmentVO 对象（比这个 Entity 多了学生名、课程名、教师名）
 *     → 序列化为 JSON 返回前端
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
 * @Data —— 编译时自动生成所有字段的 getter、setter、toString、equals、hashCode 方法。
 */
@Data

/*
 * @TableName("course_enrollment")
 *   如果类名转下划线后和表名一致可以省略，但这里显式写出让代码更清晰。
 *   类名 CourseEnrollment → 自动转换 course_enrollment → 和表名一致。
 */
@TableName("course_enrollment")

/*
 * public class CourseEnrollment { ... }
 *
 *   每个 CourseEnrollment 实例 = course_enrollment 表中的一条选课记录。
 *   即"某个学生在某个时间选了某门课"。
 */
public class CourseEnrollment {

    // ==================== 4. 字段声明 ====================

    /*
     * @TableId(type = IdType.AUTO)
     *   主键，数据库自增。每次新增选课记录，id 自动 +1。
     */
    @TableId(type = IdType.AUTO)
    private Long id; // 选课记录唯一标识（主键）

    /*
     * private Long studentId;
     *
     *   外键字段 1：指向 student 表，表示是哪个学生选了课。
     *   MyBatis-Plus 自动映射 studentId → student_id。
     *
     *   通过这个字段可以查到这个学生的信息：
     *     SELECT * FROM student WHERE id = 10  -- 查到学生 10 的姓名、学号等
     */
    private Long studentId; // 学生 ID（外键关联 student 表）

    /*
     * private Long courseId;
     *
     *   外键字段 2：指向 course 表，表示选了哪门课。
     *
     *   通过这个字段可以查到这个课程的信息：
     *     SELECT * FROM course WHERE id = 5  -- 查到课程 5 的名称、学分等
     *
     *   studentId + courseId 组合起来就是"哪个学生选了哪门课"的完整信息。
     */
    private Long courseId; // 课程 ID（外键关联 course 表）

    /*
     * @TableField(fill = FieldFill.INSERT)
     *   插入选课记录时，自动填入选课时间（当前时间）。
     *
     *   LocalDateTime —— Java 8 的日期时间类型，无时区。
     *   如 "2024-09-15T10:30:00" 表示 2024年9月15日 10点30分。
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime; // 选课时间（插入时自动填入当前时间）

    /*
     * ================================================================
     * 总结：CourseEnrollment 实体类 = course_enrollment 表的 Java 对象映射
     *
     * 数据库表 → Java 类映射：
     *   id          BIGINT PK AUTO_INCREMENT  →  Long id + @TableId(AUTO)
     *   student_id  BIGINT                    →  Long studentId
     *   course_id   BIGINT                    →  Long courseId
     *   create_time DATETIME                  →  LocalDateTime createTime + @TableField(fill=INSERT)
     *
     * 关联关系：
     *   studentId  →  Student.id  （选课记录属于一个学生）
     *   courseId   →  Course.id   （选课记录指向一门课程）
     * ================================================================
     */
}
