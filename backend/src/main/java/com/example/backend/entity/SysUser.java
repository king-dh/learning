/*
 * ================================================================
 * 文件位置:  src/main/java/com/example/backend/entity/SysUser.java
 * 对应数据库表: sys_user（系统用户表）
 * 在架构中的位置:
 *
 *   前端 Vue → Controller → Service → Mapper → Entity(当前文件) → 数据库 sys_user 表
 *
 * Entity（实体类）= 数据库系统用户表的 Java 对象映射
 *
 * 这张表是什么？
 *   sys_user 是系统认证与授权的核心表，存储所有能登录系统的人。
 *   三种角色的用户（管理员、教师、学生）共用这张表，通过 role 字段区分。
 *
 *   为什么叫 sys_user 而不是 user？
 *     user 是 MySQL 的保留关键字（用于用户管理），
 *     加 sys_ 前缀（system 的缩写）可以避免关键字冲突。
 *
 *   这张表和其他表的关系（重要！）：
 *     sys_user 表和 student/teacher 表是分开的，但逻辑上存在一对一关系：
 *       sys_user.username = "zhangsan"   → 登录用
 *       student.name = "张三"             → 基本信息
 *     用户先用 sys_user 表中的 username + password 登录，
 *     系统根据 role 字段知道他是学生/教师/管理员，
 *     然后用 realName 或关联 ID 去对应的 student/teacher 表查详细信息。
 *
 *   JS 类比：
 *     const user = {
 *       id: 1,
 *       username: "zhangsan",      // 登录名（唯一）
 *       password: "$2a$10$...",    // BCrypt 加密后的密码（不是明文！）
 *       role: "STUDENT",           // 角色：ADMIN / TEACHER / STUDENT
 *       realName: "张三",          // 真实姓名
 *       status: 1,                 // 1=启用, 0=禁用
 *       createTime: "2024-09-01T00:00:00"
 *     }
 *
 *   关于密码安全：
 *     password 字段存的是 BCrypt 加密后的密文，不是明文。
 *     比如用户输入密码 "123456"，数据库里存的是类似 "$2a$10$N9qo8uLOickgx2ZMRZoMye..." 的密文。
 *     这样即使数据库被泄露，攻击者也拿不到原始密码。
 *     BCrypt 是单向加密（只能加密，不能解密），验证时是对用户输入的密码再次加密，比对密文。
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
 * @TableName("sys_user")
 *   显式指定表名。SysUser 自动转换是 sys_user，和表名一致。
 *   显式写出让代码更清晰（看到这个就知道对应哪张表）。
 */
@TableName("sys_user")

/*
 * public class SysUser { ... }
 *
 *   每个 SysUser 实例 = sys_user 表中的一条用户记录。
 */
public class SysUser {

    // ==================== 4. 字段声明 ====================

    /*
     * @TableId(type = IdType.AUTO)
     *   主键，数据库自增。每注册一个用户，id 自动 +1。
     */
    @TableId(type = IdType.AUTO)
    private Long id; // 用户唯一标识（主键）

    /*
     * private String username;
     *
     *   登录用户名，用于身份认证。在整个系统中应该是唯一的。
     *   用户登录时提交 username + password，系统去 sys_user 表查匹配的记录。
     */
    private String username; // 登录用户名（用于认证）

    /*
     * private String password;
     *
     *   存储 BCrypt 加密后的密码密文，不是明文！
     *   BCrypt 是一种单向哈希算法，特点：
     *     - 同样的明文每次加密结果不同（因为包含随机盐值）
     *     - 只能加密不能解密
     *     - 验证时：把用户输入的密码用同样算法加密，比对密文
     *
     *   密文格式示例：$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy
     *                ├─$2a$──┤ ├──────22位盐值──────┤├────────31位密文───────────┤
     *                算法版本   随机生成的盐          加密后的密码
     */
    private String password; // 加密后的密码（BCrypt 密文）

    /*
     * private String role;
     *
     *   用户角色，控制访问权限。取值：
     *     "ADMIN"   —— 管理员（最高权限，可以管理所有数据）
     *     "TEACHER" —— 教师（可以管理自己的课程、录入成绩）
     *     "STUDENT" —— 学生（可以选课、查看自己的成绩）
     *
     *   Spring Security 使用这个字段判断用户能访问哪些接口。
     *   例如：@PreAuthorize("hasRole('ADMIN')")  → 只有 role=ADMIN 的用户才能访问。
     */
    private String role; // 用户角色：ADMIN（管理员）/ TEACHER（教师）/ STUDENT（学生）

    /*
     * private String realName;
     *
     *   用户的真实姓名，用于前端显示（如"欢迎你，张三"）。
     *   和 student 表、teacher 表中的 name 字段是同一个人的名字，
     *   但存储在不同表中。
     */
    private String realName; // 真实姓名（用于界面显示）

    /*
     * private Integer status;
     *
     *   用户状态，用于账号启用/禁用控制：
     *     1 —— 启用（正常登录）
     *     0 —— 禁用（无法登录）
     *
     *   Spring Security 在认证时会检查这个字段，
     *   如果 status = 0，即使密码正确也会拒绝登录。
     */
    private Integer status; // 用户状态：1=启用, 0=禁用

    /*
     * @TableField(fill = FieldFill.INSERT)
     *   用户注册时，自动填入当前时间。
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime; // 用户创建时间（插入时自动填入）

    /*
     * ================================================================
     * 总结：SysUser 实体类 = sys_user 表的 Java 对象映射
     *
     * 数据库表 → Java 类映射：
     *   id          BIGINT PK AUTO_INCREMENT  →  Long id + @TableId(AUTO)
     *   username    VARCHAR                   →  String username
     *   password    VARCHAR                   →  String password
     *   role        VARCHAR                   →  String role
     *   real_name   VARCHAR                   →  String realName
     *   status      INT                       →  Integer status
     *   create_time DATETIME                  →  LocalDateTime createTime + @TableField(fill=INSERT)
     *
     * 与 Spring Security 的配合：
     *   SysUser          →  存入数据库
     *   UserDetails      →  Spring Security 的认证对象（Security 框架定义的接口）
     *   登录时：SysUser 的信息被转换成 UserDetails，存入 SecurityContext
     *
     * 注意：这个 Entity 不直接关联 student 或 teacher 表，
     *   它只负责认证（登录）和授权（角色）相关数据，
     *   用户的基本信息在对应的 student/teacher 表中。
     * ================================================================
     */
}
