/*
 * ================================================================
 * 文件位置:  src/main/java/com/example/backend/entity/ClassInfo.java
 * 对应数据库表: class_info（班级信息表）
 * 在架构中的位置:
 *
 *   前端 Vue → Controller → Service → Mapper → Entity(当前文件) → 数据库 class_info 表
 *
 * Entity（实体类）= 数据库班级表的 Java 对象映射
 *
 * JS 类比：
 *   const classInfo = {
 *     id: 1,
 *     className: "软件工程1班",
 *     grade: "2024级",
 *     headTeacherId: 3,
 *     createTime: "2024-09-01T00:00:00"
 *   }
 *
 * 为什么表名叫 class_info 而不是 class？
 *   class 是 MySQL 的保留关键字（用于定义类/存储过程），
 *   如果用 class 做表名，SQL 里每次都要用反引号 `class` 包起来，容易出错。
 *   所以用 class_info 避开关键字冲突。
 * ================================================================
 */

// ==================== 1. 包声明 ====================
package com.example.backend.entity; // entity 子包（实体层，数据模型）

// ==================== 2. 导入其他类 ====================

// --- MyBatis-Plus 注解（对象-数据库映射）---
import com.baomidou.mybatisplus.annotation.FieldFill;  // 字段自动填充策略
import com.baomidou.mybatisplus.annotation.IdType;     // 主键生成策略
import com.baomidou.mybatisplus.annotation.TableField;  // 字段-列映射配置
import com.baomidou.mybatisplus.annotation.TableId;     // 主键标识
import com.baomidou.mybatisplus.annotation.TableName;   // 表名映射

// --- Lombok 注解（自动生成 getter/setter/toString 等方法）---
import lombok.Data; // @Data：编译时自动生成所有字段的 getter、setter、toString、equals、hashCode

// --- Java 8 时间 API ---
import java.time.LocalDateTime; // 不带时区的日期时间类

// ==================== 3. 类的声明和注解 ====================

/*
 * @Data
 *   编译时 Lombok 自动生成：
 *     getId()、setId()、getClassName()、setClassName()、...、toString()
 *   不用手写这些方法，代码保持简洁。
 *
 *   JS 类比：Java 的 getter/setter 类似于 JS 对象的属性访问器。
 *   没有 @Data 的话，每个字段要写 10 行 getter/setter 代码，非常啰嗦。
 */
@Data

/*
 * @TableName("class_info")
 *   显式指定数据库表名，因为类名 ClassInfo 和表名 class_info 不是简单的下划线转换关系。
 *   如果不写 @TableName，MyBatis-Plus 会把 ClassInfo → class_info（驼峰转下划线），
 *   刚好和表名一致，这里显式写出是为了代码意图更清晰。
 *
 *   类比 TypeORM：
 *     @Entity("class_info")  // 和 @TableName("class_info") 一样
 */
@TableName("class_info")

/*
 * public class ClassInfo { ... }
 *
 *   public    —— 公开类，任何地方都可以使用
 *   class     —— 定义类的关键字
 *   ClassInfo —— 类名，必须和文件名 ClassInfo.java 一致
 *   { ... }   —— 类的成员：字段（数据库列）+ 方法（由 @Data 自动生成）
 *
 *   每个 ClassInfo 实例 = class_info 表中的一条班级记录
 */
public class ClassInfo {

    // ==================== 4. 字段声明 ====================

    /*
     * @TableId(type = IdType.AUTO)
     *   标记 id 是主键，类型是数据库自增（AUTO_INCREMENT）。
     *   插入新班级时不传 id，MySQL 自动生成 1, 2, 3...
     *
     *   JS 类比：
     *     db.insert({ className: "软件1班" })
     *     // 返回 { id: 1, className: "软件1班" }  ← id 由数据库自动生成
     */
    @TableId(type = IdType.AUTO)
    private Long id; // 班级唯一标识（主键）

    /*
     * 以下字段没有加 @TableField 注解，因为 MyBatis-Plus 默认把 Java 驼峰命名转成数据库下划线命名：
     *   className     → class_name    （驼峰 → 下划线）
     *   headTeacherId → head_teacher_id
     *   createTime    → create_time
     *
     * 只有列名和转换规则不一致时才需要用 @TableField 显式指定。
     */
    private String className; // 班级名称，如 "软件工程1班"、"计算机科学2班"

    private String grade; // 所属年级，如 "2024级"、"2023级"

    /*
     * private Long headTeacherId;
     *
     *   外键字段：指向 teacher 表的主键 id，表示这个班的班主任是谁。
     *   需要联表查询时：SELECT c.*, t.name AS headTeacherName
     *                  FROM class_info c LEFT JOIN teacher t ON c.head_teacher_id = t.id
     *
     *   注意：这里只存了一个数字 ID，
     *   班主任的名字、职称等信息在 teacher 表里，通过 JOIN 获取。
     */
    private Long headTeacherId; // 班主任教师 ID（外键关联 teacher 表）

    /*
     * @TableField(fill = FieldFill.INSERT)
     *   插入数据时（INSERT），框架自动把当前时间填到 createTime 字段。
     *   实际的填充值在项目的 MetaObjectHandler 配置类中定义。
     *
     *   JS 类比：
     *     const record = { ...data, createTime: new Date() }  // 框架自动加
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime; // 记录创建时间（插入时自动填入）

    /*
     * ================================================================
     * 总结：ClassInfo 实体类 = class_info 表的 Java 对象表示
     *
     * 数据库表 → Java 类映射：
     *   id              BIGINT PK AUTO_INCREMENT  →  Long id + @TableId(AUTO)
     *   class_name      VARCHAR                   →  String className
     *   grade           VARCHAR                   →  String grade
     *   head_teacher_id BIGINT                    →  Long headTeacherId
     *   create_time     DATETIME                  →  LocalDateTime createTime + @TableField(fill=INSERT)
     *
     * 关联关系：
     *   id             ←  Student.classId    （多个学生属于一个班级，一对多）
     *   headTeacherId  →  Teacher.id         （一个班主任管理一个班级，一对一）
     * ================================================================
     */
}
