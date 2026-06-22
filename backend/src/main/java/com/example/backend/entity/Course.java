/*
 * ================================================================
 * 文件位置:  src/main/java/com/example/backend/entity/Course.java
 * 对应数据库表: course（课程信息表）
 * 在架构中的位置:
 *
 *   前端 Vue → Controller → Service → Mapper → Entity(当前文件) → 数据库 course 表
 *
 * Entity（实体类）= 数据库课程表的 Java 对象映射
 *
 * JS 类比：
 *   const course = {
 *     id: 1,
 *     courseNo: "CS101",
 *     name: "计算机科学导论",
 *     credit: 3.0,
 *     teacherId: 5,
 *     semester: "2024-2025-1",
 *     description: "介绍计算机科学基本概念和原理",
 *     classroom: "B301",
 *     maxStudents: 60,
 *     createTime: "2024-09-01T00:00:00"
 *   }
 *
 * 数据流转（以查询某教师的课程为例）:
 *   前端请求 GET /api/courses?teacherId=5
 *     → Controller 接收请求
 *     → Service 调用 CourseMapper.selectByTeacherId(5)
 *     → MyBatis-Plus 执行 SQL：SELECT * FROM course WHERE teacher_id = 5
 *     → 每一行结果映射为一个 Course 对象
 *     → 返回 List<Course> 给前端（序列化为 JSON）
 * ================================================================
 */

// ==================== 1. 包声明 ====================
package com.example.backend.entity; // entity 子包（实体层，数据模型）

// ==================== 2. 导入其他类 ====================

// --- MyBatis-Plus 注解（对象-数据库映射）---
import com.baomidou.mybatisplus.annotation.FieldFill;  // 字段自动填充策略
import com.baomidou.mybatisplus.annotation.IdType;     // 主键生成策略：AUTO=数据库自增, INPUT=手动输入
import com.baomidou.mybatisplus.annotation.TableField;  // 声明字段和列的映射关系
import com.baomidou.mybatisplus.annotation.TableId;     // 声明主键字段
import com.baomidou.mybatisplus.annotation.TableName;   // 声明对应的数据库表名

// --- Lombok 注解（自动生成 getter/setter/toString 等方法）---
import lombok.Data; // @Data：编译时自动生成 getter、setter、toString、equals、hashCode

// --- Java 8 时间 API ---
import java.time.LocalDateTime; // 日期时间类（不含时区），对应数据库 DATETIME

// ==================== 3. 类的声明和注解 ====================

/*
 * @Data
 *   编译时自动生成所有字段的 getter/setter/toString 等方法。
 *   比如你写 course.getName()，实际上是调用 Lombok 自动生成的 getName() 方法。
 *
 *   JS 类比：相当于自动给对象的每个属性加了 getter/setter 访问器。
 */
@Data

/*
 * @TableName("course")
 *   指定 Java 类对应的数据库表名。
 *   如果不写，MyBatis-Plus 默认将类名 Course 转小写 → course，刚好和表名一致。
 *   显式写出是为了代码可读性——一目了然这个类对应哪张表。
 *
 *   类比 TypeORM：
 *     @Entity("course")  // 相同含义
 */
@TableName("course")

/*
 * public class Course { ... }
 *
 *   public —— 公开类
 *   class —— 类定义关键字
 *   Course—— 类名，与文件名 Course.java 一致
 */
public class Course {

    // ==================== 4. 字段声明 ====================

    /*
     * @TableId(type = IdType.AUTO)
     *   主键，数据库自增。每次插入课程，id 自动 +1：
     *   第一条课程 id=1，第二条 id=2，以此类推。
     *
     *   JS 类比：
     *     // MySQL 中 id 列设为 AUTO_INCREMENT
     *     db.insert({ name: "高等数学" })  // id 自动生成，如 1
     */
    @TableId(type = IdType.AUTO)
    private Long id; // 课程唯一标识（主键）

    /*
     * private String courseNo;
     *
     *   驼峰命名 courseNo，MyBatis-Plus 自动映射到列 course_no。
     *   命名转换规则：大写字母前加下划线并转小写。courseNo → course_no。
     */
    private String courseNo; // 课程编号，如 "CS101"（用于标识和检索课程）

    private String name; // 课程名称，如 "高等数学"、"大学英语"

    /*
     * private Double credit;
     *
     *   Double —— Java 的 64 位浮点数类型（包装类，可以为 null）
     *   对应数据库的 DECIMAL 或 DOUBLE 类型
     *   学分如 3.0、2.5、4.0
     *
     *   为什么用 Double 而不是 double？
     *     Double 是对象类型，可以是 null（表示未设置学分）
     *     double 是基础类型，默认值是 0.0（无法区分"0 分"和"未设置"）
     */
    private Double credit; // 学分数，如 3.0

    /*
     * private Long teacherId;
     *
     *   外键字段：指向 teacher 表的主键 id，表示这门课由哪位老师授课。
     *   如果想在页面上同时显示课程名和教师名，需要在 Mapper 中写 SQL JOIN：
     *     SELECT c.*, t.name AS teacher_name
     *     FROM course c LEFT JOIN teacher t ON c.teacher_id = t.id
     */
    private Long teacherId; // 授课教师 ID（外键关联 teacher 表）

    /*
     * private String semester;
     *
     *   学期格式如 "2024-2025-1"：
     *     "2024-2025" → 学年
     *     "1"         → 第一学期
     *   有时也写作 "20241" 或 "2024-2025-1"，取决于学校规定。
     */
    private String semester; // 开课学期，如 "2024-2025-1"（2024-2025学年第一学期）

    private String description; // 课程简介描述，介绍课程内容和教学目标

    private String classroom; // 上课教室，如 "B301"、"致远楼101"

    /*
     * private Integer maxStudents;
     *
     *   Integer —— 包装类（可以为 null），对应数据库 INT 类型
     *   最大容纳学生数，用于选课时判断是否还有名额。
     *   例如：已选人数 >= maxStudents 时，提示"该课程已满"。
     */
    private Integer maxStudents; // 最大容纳学生数（用于选课容量控制）

    /*
     * @TableField(fill = FieldFill.INSERT)
     *   插入数据时自动填入当前时间，无需手动设置 createTime。
     *
     *   时间的填充逻辑在全局配置 MetaObjectHandler 中定义：
     *     this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
     *                                                          ↑ 填当前时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime; // 课程创建时间（插入时自动填入）

    /*
     * ================================================================
     * 总结：Course 实体类 = course 表的 Java 对象映射
     *
     * 数据库表 → Java 类映射：
     *   id           BIGINT PK AUTO_INCREMENT  →  Long id + @TableId(AUTO)
     *   course_no    VARCHAR                   →  String courseNo
     *   name         VARCHAR                   →  String name
     *   credit       DOUBLE                    →  Double credit
     *   teacher_id   BIGINT                    →  Long teacherId
     *   semester     VARCHAR                   →  String semester
     *   description  VARCHAR                   →  String description
     *   classroom    VARCHAR                   →  String classroom
     *   max_students INT                       →  Integer maxStudents
     *   create_time  DATETIME                  →  LocalDateTime createTime + @TableField(fill=INSERT)
     *
     * 关联关系：
     *   teacherId   →  Teacher.id              （课程属于一个教师）
     *   id          ←  CourseEnrollment.courseId（课程被多个学生选修）
     *   id          ←  Score.courseId           （课程有多个成绩记录）
     * ================================================================
     */
}
