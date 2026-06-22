/*
 * ================================================================
 * 文件位置:  src/main/java/com/example/backend/entity/Teacher.java
 * 对应数据库表: teacher（教师信息表）
 * 在架构中的位置:
 *
 *   前端 Vue → Controller → Service → Mapper → Entity(当前文件) → 数据库 teacher 表
 *
 * Entity（实体类）= 数据库教师表的 Java 对象映射
 *
 * JS 类比：
 *   const teacher = {
 *     id: 1,
 *     teacherNo: "T2024001",                       // 工号
 *     name: "李教授",
 *     gender: "男",
 *     title: "教授",                               // 职称
 *     department: "计算机科学与技术学院",          // 院系
 *     phone: "13800138001",
 *     createTime: "2020-09-01T00:00:00"
 *   }
 *
 * 注意：teacher 表和 sys_user 表是分开的。
 *   - sys_user 存登录信息（username, password, role）
 *   - teacher 存教师基本信息（姓名, 工号, 职称等）
 *   它们之间通常通过 username 或 realName 做逻辑关联，
 *   但数据库层面没有直接的外键约束。
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
 * @TableName("teacher")
 *   Teacher → teacher（类名转小写），和表名一致。显式声明提高可读性。
 */
@TableName("teacher")

/*
 * public class Teacher { ... }
 *
 *   每个 Teacher 实例 = teacher 表中的一条教师记录。
 */
public class Teacher {

    // ==================== 4. 字段声明 ====================

    /*
     * @TableId(type = IdType.AUTO)
     *   主键，数据库自增。每次新增教师，id 自动 +1。
     */
    @TableId(type = IdType.AUTO)
    private Long id; // 教师唯一标识（主键）

    /*
     * private String teacherNo;
     *
     *   驼峰命名：teacherNo → teacher_no（自动映射到数据库列）
     *   教师工号，如 "T2024001"，在系统中工号通常是唯一的。
     */
    private String teacherNo; // 教师工号，如 "T2024001"

    private String name; // 教师姓名

    private String gender; // 性别：男 / 女

    /*
     * private String title;
     *
     *   职称，表示教师的专业技术职务等级。
     *   中常见取值：教授、副教授、讲师、助教。
     */
    private String title; // 职称：教授 / 副教授 / 讲师 / 助教

    private String department; // 所属院系，如 "计算机科学与技术学院"

    private String phone; // 联系电话

    /*
     * @TableField(fill = FieldFill.INSERT)
     *   新增教师时，自动填入当前时间（如入职时间）。
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime; // 创建时间（插入时自动填入）

    /*
     * ================================================================
     * 总结：Teacher 实体类 = teacher 表的 Java 对象映射
     *
     * 数据库表 → Java 类映射：
     *   id          BIGINT PK AUTO_INCREMENT  →  Long id + @TableId(AUTO)
     *   teacher_no  VARCHAR                   →  String teacherNo
     *   name        VARCHAR                   →  String name
     *   gender      VARCHAR                   →  String gender
     *   title       VARCHAR                   →  String title
     *   department  VARCHAR                   →  String department
     *   phone       VARCHAR                   →  String phone
     *   create_time DATETIME                  →  LocalDateTime createTime + @TableField(fill=INSERT)
     *
     * 关联关系：
     *   id  ←  Course.teacherId      （一个教师教多门课程，一对多）
     *   id  ←  ClassInfo.headTeacherId（一个教师担任多个班班主任）
     * ================================================================
     */
}
