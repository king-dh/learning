/*
 * ================================================================
 * 文件位置:  src/main/java/com/example/backend/entity/Student.java
 * 对应数据库表: student（学生信息表）
 * 在架构中的位置:
 *
 *   ┌─────────────┐
 *   │  前端 Vue    │  发送 JSON 请求
 *   └──────┬──────┘
 *          ↓
 *   ┌─────────────┐
 *   │ Controller   │  StudentController.java（接线员：接收 HTTP 请求）
 *   └──────┬──────┘
 *          ↓
 *   ┌─────────────┐
 *   │  Service     │  StudentServiceImpl.java（干活的人：处理业务逻辑）
 *   └──────┬──────┘
 *          ↓
 *   ┌─────────────┐
 *   │   Mapper     │  StudentMapper.java（数据库操作接口）
 *   └──────┬──────┘
 *          ↓
 *   ┌─────────────┐
 *   │   Entity     │  Student.java  ← 当前文件（数据库行的 Java 表示）
 *   └──────┬──────┘
 *          ↓
 *   ┌─────────────┐
 *   │   数据库     │  MySQL 的 student 表
 *   └──────────────┘
 *
 * Entity（实体类）是什么？
 *   = 数据库中一行数据的 Java 对象模型
 *   = 它的一个实例就代表数据库 student 表中的一条记录
 *
 *   JS 类比：
 *     // 在 JavaScript 中，你可能会这样表示一条学生数据：
 *     const student = {
 *       id: 1,
 *       studentNo: "20240001",
 *       name: "张三",
 *       gender: "男",
 *       age: 20,
 *       phone: "13800138000",
 *       email: "zhangsan@example.com",
 *       classId: 1,
 *       createTime: "2024-09-01T00:00:00"
 *     }
 *     // Java 的 Student 类就是这个对象的"类型定义"（Type Definition）
 *     // TypeScript 类比：interface Student { id: number; name: string; ... }
 *
 * 数据流转（以查询为例）:
 *   数据库 student 表的某一行数据
 *     → MyBatis-Plus 自动读取每一列的值
 *     → 按字段名匹配，填充到 Student 对象的对应字段
 *     → Student 对象被 Service 层使用
 *     → 最终通过 Controller 转成 JSON 返回给前端
 * ================================================================
 */

// ==================== 1. 包声明 ====================

/*
 * package 关键字：声明当前文件属于哪个"包"。
 * 和文件在硬盘上的路径完全对应：
 *   com.example.backend.entity
 *   └── com/example/backend/entity/   ← 文件夹路径
 *
 * entity 包专门存放"实体类"——Java 对象和数据库表的映射定义。
 * 这是整个项目最底层的数据模型，靠近数据库。
 */
package com.example.backend.entity; // 声明当前文件属于 entity 子包（实体层）

// ==================== 2. 导入其他类（import） ====================

/*
 * import 关键字：告诉编译器"我要用别的文件/库里的类"。
 * 和 JavaScript 的 import { xxx } from './xxx' 完全一样的概念。
 * 区别：Java 用的是完整包路径，不是相对路径。
 */

// --- 2.1 MyBatis-Plus 注解（对象-数据库映射的核心工具）---
import com.baomidou.mybatisplus.annotation.FieldFill;  // 字段自动填充策略：数据插入或更新时自动填入值（如当前时间）
import com.baomidou.mybatisplus.annotation.IdType;     // 主键生成策略：AUTO=数据库自增, INPUT=手动输入, ASSIGN_ID=雪花算法
import com.baomidou.mybatisplus.annotation.TableField;  // 声明 Java 字段和数据库列的映射关系
import com.baomidou.mybatisplus.annotation.TableId;     // 声明哪个字段是数据库主键
import com.baomidou.mybatisplus.annotation.TableName;   // 声明 Java 类对应数据库的哪张表

/*
 * MyBatis-Plus 是什么？
 *   一个对 MyBatis 的增强框架，让你不用手写 SQL 就能操作数据库。
 *   类比 JS 的 Prisma 或 TypeORM —— 用注解把 Java 类和数据库表绑定在一起。
 *   比如：Student 类 → student 表，Student 的 name 字段 → student 表的 name 列。
 */

// --- 2.2 Lombok 注解（减少样板代码）---
import lombok.Data; // @Data：自动生成 Getter、Setter、toString、equals、hashCode 等常用方法

/*
 * Lombok 是什么？
 *   一个编译期代码生成工具。你写 @Data，编译时 Lombok 自动帮你生成那些啰嗦的
 *   getXxx()、setXxx()、toString() 方法，你不用手写。
 *
 *   JS 类比：
 *     没有 Lombok 时，你得手写：
 *       public String getName() { return this.name; }          // getter
 *       public void setName(String name) { this.name = name; } // setter
 *       // ... 每个字段都要写一对，非常啰嗦
 *
 *     有了 Lombok 的 @Data，编译时自动生成，源代码保持简洁。
 *     就类似于 JS 中有了 getter/setter 语法糖，不需要手动 defineProperty。
 */

// --- 2.3 Java 8 时间 API ---
import java.time.LocalDateTime; // 日期时间类，不带时区信息（如 "2024-09-01T10:30:00"）

// ==================== 3. 类的声明和注解 ====================

/*
 * @Data
 *   Lombok 的核心注解，放在类上，编译时自动生成：
 *     - 所有字段的 getter 方法（如 getName()、getAge()）
 *     - 所有非 final 字段的 setter 方法（如 setName()、setAge()）
 *     - toString() 方法（打印对象时把所有字段拼成字符串）
 *     - equals() 和 hashCode() 方法（用于比较两个对象是否相等）
 *
 *   类比 JS：
 *     class Student {
 *       constructor(data) {
 *         this.id = data.id;
 *         this.name = data.name;
 *         // ...
 *       }
 *     }
 *     // @Data 就自动帮你写了所有 getter/setter，你不必手写
 *
 *   注意：@Data 不会生成构造函数（区别于 @Value 或 @AllArgsConstructor），
 *   但 MyBatis-Plus 创建实体对象时用的是无参构造函数 + setter 方式，
 *   所以 @Data 刚好够用。
 */
@Data

/*
 * @TableName("student")
 *   告诉 MyBatis-Plus："这个 Java 类和数据库的 student 表绑定在一起"。
 *   - 如果不写 @TableName，MyBatis-Plus 默认把类名转小写当表名（Student → student）
 *   - 但如果表名和类名转换规则不一致（如 SysUser → sys_user），就必须显式指定
 *   - "student" 是写在引号里的字符串，表示数据库中真实的表名
 *
 *   类比 JS Prisma：
 *     model Student {                ← Prisma schema
 *       @@map("student")             ← 类似 @TableName("student")
 *     }
 */
@TableName("student")

/*
 * public class Student { ... }
 *
 *   public  —— 这个类可以被任何其他类使用（Java 的访问修饰符，类似 JS 的 export）
 *   class  —— 定义类的关键字
 *   Student—— 类名，必须和文件名完全一致（Java 硬性规定）
 *
 *   { ... } 里面的内容：
 *     字段（成员变量）= 对应数据库表的列（Column）
 *     每个 Student 实例 = 数据库 student 表的一行数据（Row）
 */

public class Student {

    // ==================== 4. 字段声明（每一行 = 数据库的一列） ====================

    /*
     * @TableId(type = IdType.AUTO)
     *   声明主键字段及其生成策略。
     *
     *   @TableId      —— 告诉 MyBatis-Plus："这个字段是数据库表的主键（PRIMARY KEY）"
     *   type          —— 主键的生成方式
     *   IdType.AUTO   —— 数据库自增（MySQL 的 AUTO_INCREMENT）
     *                    插入数据时不传 id 值，数据库自动生成一个递增的数字
     *
     *   其他常见策略：
     *     IdType.ASSIGN_ID  —— 雪花算法生成 Long 型 ID（分布式系统常用）
     *     IdType.INPUT      —— 手动指定 ID，数据库不自增
     *     IdType.NONE       —— 无策略（全局配置为准）
     *
     *   JS 类比：
     *     // 不传 id，数据库自动生成
     *     db.students.insert({ name: "张三" })  // id 字段由数据库自增
     *
     *   private Long id;
     *     private —— 私有字段，外部不能直接访问（要通过 getter/setter）
     *     Long   —— Java 的 64 位整数类型（对应数据库的 BIGINT），用对象类型不用基础类型 long 是因为可以为 null
     *     id     —— 字段名，和数据库列名相同时可以省略 @TableField 注解
     */
    @TableId(type = IdType.AUTO)
    private Long id; // 学生唯一标识（主键，数据库自动生成）

    /*
     * private String studentNo;
     *
     *   没有加任何 MyBatis-Plus 注解，因为字段名 "studentNo" 自动映射到数据库列 "student_no"。
     *   MyBatis-Plus 默认的命名转换规则：驼峰命名 → 下划线分隔
     *     studentNo         → student_no    （Java 字段名 → 数据库列名）
     *     classId           → class_id
     *     createTime        → create_time
     *
     *   所以只要字段名转下划线后和数据库列名一致，就不用加 @TableField 注解。
     *   只有不一致时才需要显式指定，如 @TableField("real_column_name")。
     *
     *   String —— Java 的字符串类型（引用类型，可以为 null），对应数据库的 VARCHAR
     */
    private String studentNo; // 学号，如 "20240001"

    private String name; // 学生姓名

    private String gender; // 性别：男 / 女

    private Integer age; // 年龄（Integer 而不是 int，因为 int 不能为 null，Integer 可以为 null）

    private String phone; // 手机号码

    private String email; // 电子邮箱地址

    /*
     * private Long classId;
     *
     *   外键字段，指向 class_info 表的主键 id。
     *   通过它可以在查询时关联到学生所属的班级信息。
     *
     *   注意：这里存的只是班级的 id（一个数字），
     *   班级的名称等信息在 class_info 表中，需要通过 SQL JOIN 查询获取。
     */
    private Long classId; // 所属班级 ID，外键关联 class_info 表的 id

    /*
     * @TableField(fill = FieldFill.INSERT)
     *
     *   @TableField         —— 通常用于字段名和列名不一致时指定映射，或配置填充策略
     *   fill                —— 自动填充策略
     *   FieldFill.INSERT    —— 仅在插入新记录时自动填入值
     *                          其他可选：FieldFill.UPDATE（更新时填充）、
     *                          FieldFill.INSERT_UPDATE（插入和更新时都填充）
     *
     *   这里和 createTime 搭配使用：
     *     插入数据时，MyBatis-Plus 自动把"当前时间"填到这个字段，无需手动设置。
     *     实际的填充逻辑在 MetaObjectHandler 配置类中定义（自动填充处理器）。
     *
     *   类比 JS：
     *     // 在插入数据时，自动添加创建时间
     *     const student = { name: "张三", createTime: new Date() }
     *                                                 ↑ 框架自动填
     *
     *   LocalDateTime：
     *     Java 8 引入的日期时间类，不包含时区信息。
     *     格式如 "2024-09-01T10:30:00"，对应数据库的 DATETIME 类型。
     *     比老式的 Date 类更好用（线程安全、API 清晰）。
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime; // 记录创建时间（插入时由框架自动填入当前时间）

    /*
     * ================================================================
     * 总结：这个 Student 类就是一个"数据模型"（Data Model）
     *
     * 它的作用：
     *   1. 定义数据库中 student 表的 Java 表示
     *   2. 告诉 MyBatis-Plus "字段怎么映射到列"
     *   3. 作为数据在各层之间传递的载体
     *
     * 它不写任何业务逻辑（不验证、不计算、不查数据库）。
     * 它只负责"我就是一条学生数据的结构定义"。
     *
     * 数据库 student 表结构（CREATE TABLE） vs 这个 Java 类：
     *   id          BIGINT PRIMARY KEY AUTO_INCREMENT  →  Long id + @TableId(AUTO)
     *   student_no  VARCHAR                            →  String studentNo
     *   name        VARCHAR                            →  String name
     *   gender      VARCHAR                            →  String gender
     *   age         INT                                →  Integer age
     *   phone       VARCHAR                            →  String phone
     *   email       VARCHAR                            →  String email
     *   class_id    BIGINT                             →  Long classId
     *   create_time DATETIME                           →  LocalDateTime createTime + @TableField(fill=INSERT)
     *
     * 关联的其他 Entity：
     *   classId  →  ClassInfo.id   （通过 classId 找到学生所属的班级）
     *   id       ←  Score.studentId（通过 id 找到学生的成绩记录）
     * ================================================================
     */
}
