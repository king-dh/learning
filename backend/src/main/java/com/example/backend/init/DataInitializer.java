/*
 * ================================================================
 * 文件位置:  src/main/java/com/example/backend/init/DataInitializer.java
 * 所在层:    Init 层（初始化层）
 *
 * 职责说明:
 *   在 Spring Boot 应用启动完成后，自动检查数据库并插入测试数据。
 *   方便开发和测试，不需要手动执行 SQL 脚本。
 *
 * CommandLineRunner 接口：
 *   Spring Boot 提供的回调接口，只有一个 run() 方法。
 *   实现在应用启动完成后（所有 Bean 初始化完、Tomcat 启动后）自动执行。
 *   如果有多个 CommandLineRunner，可以用 @Order 注解控制执行顺序。
 *
 * 类比 JavaScript：
 *   // Node.js 中的初始化脚本
 *   async function initDatabase() {
 *     const userCount = await db.sysUser.count()
 *     if (userCount > 0) return // 已有数据，跳过
 *     await db.sysUser.insertMany(testData)
 *   }
 *   initDatabase()  // 启动后自动执行
 *
 * 数据初始化策略：
 *   1. 检查数据库是否已有数据（查询 sys_user 表）
 *   2. 如果已有数据 → 跳过（避免重复插入）
 *   3. 如果没有数据 → 插入测试数据（用户、教师、学生、班级、课程）
 *   4. 密码使用 BCrypt 加密存储（不存明文）
 * ================================================================
 */

package com.example.backend.init;

// --- MyBatis-Plus ---
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper; // 查询条件构造

// --- 实体类（Entity）---
import com.example.backend.entity.ClassInfo;  // 班级实体
import com.example.backend.entity.Course;     // 课程实体
import com.example.backend.entity.Student;    // 学生实体
import com.example.backend.entity.SysUser;     // 用户实体
import com.example.backend.entity.Teacher;    // 教师实体

// --- Mapper 接口（数据库操作）---
import com.example.backend.mapper.ClassInfoMapper;  // 班级 Mapper
import com.example.backend.mapper.CourseMapper;     // 课程 Mapper
import com.example.backend.mapper.StudentMapper;    // 学生 Mapper
import com.example.backend.mapper.SysUserMapper;     // 用户 Mapper
import com.example.backend.mapper.TeacherMapper;    // 教师 Mapper

// --- Lombok ---
import lombok.RequiredArgsConstructor; // 构造器注入
import lombok.extern.slf4j.Slf4j;     // 日志

// --- Spring Boot ---
/*
 * CommandLineRunner：
 *   Spring Boot 的回调接口。
 *   方法 run(String... args) 在应用启动完成后自动执行。
 *   可以理解为 JavaScript 的 DOMContentLoaded 事件。
 *
 * @Component：
 *   声明为 Spring Bean。Spring 发现它实现了 CommandLineRunner 接口时，
 *   会在应用启动完成后自动调用它的 run() 方法。
 *
 * @Transactional：
 *   事务注解。rollbackFor = Exception.class 表示任何异常都回滚。
 *   保证初始化数据要么全部成功，要么全部不插（不会出现"插入一半"的情况）。
 *
 * PasswordEncoder：
 *   BCrypt 密码加密器。密码明文用 encode() 方法加密后存入数据库。
 *   数据库中永远不存明文密码！
 */
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

// ==================== 类声明 ====================

/*
 * @Slf4j：
 *   自动生成 log 日志对象，用于打印数据库初始化进度。
 *
 * @Component：
 *   声明为 Spring Bean（必须是 Bean，Spring 才能识别并调用 run() 方法）。
 *
 * @RequiredArgsConstructor：
 *   为所有 final 字段生成构造函数并注入依赖。
 */
@Slf4j        // 日志
@Component    // Spring Bean
@RequiredArgsConstructor // 构造器注入
public class DataInitializer implements CommandLineRunner { // 实现启动回调接口

    // ==================== 依赖注入 ====================

    private final SysUserMapper sysUserMapper;       // 用户 Mapper（操作 sys_user 表）
    private final StudentMapper studentMapper;       // 学生 Mapper（操作 student 表）
    private final TeacherMapper teacherMapper;       // 教师 Mapper（操作 teacher 表）
    private final ClassInfoMapper classInfoMapper;   // 班级 Mapper（操作 class_info 表）
    private final CourseMapper courseMapper;         // 课程 Mapper（操作 course 表）
    private final PasswordEncoder passwordEncoder;   // BCrypt 密码加密器（明文 → 密文）

    // ==================== 启动后执行 ====================

    /**
     * Spring Boot 应用启动完成后自动执行
     * 检查并初始化测试数据
     *
     * @param args 命令行参数（main 方法传入的）
     */
    @Override
    /*
     * @Transactional(rollbackFor = Exception.class)
     *
     *   事务注解：确保所有数据库操作在一个事务中。
     *   如果中途抛任何异常（Exception.class），所有已执行的操作都会回滚。
     *
     *   rollbackFor = Exception.class：
     *     Spring 默认只回滚 RuntimeException（非受检异常）。
     *     加上这个参数后，任何 Exception 都回滚，更安全。
     *
     *   类比 JavaScript 数据库事务：
     *     const txn = await db.transaction()
     *     try {
     *       await txn.user.insertMany(users)
     *       await txn.student.insertMany(students)
     *       await txn.commit()
     *     } catch (e) {
     *       await txn.rollback()
     *     }
     */
    @Transactional(rollbackFor = Exception.class)
    public void run(String... args) { // args = 命令行参数（通常为空）
        log.info("===== 开始检查并初始化测试数据 =====");

        /*
         * 检查数据库是否已有数据
         *
         * selectCount(new LambdaQueryWrapper<>())：
         *   相当于 SELECT COUNT(*) FROM sys_user
         *   如果 count > 0，说明以前初始化过，跳过
         *
         *   为什么用 LambdaQueryWrapper<>() 不传条件？
         *     空的 QueryWrapper 表示"无条件"，即统计全表行数。
         */
        if (sysUserMapper.selectCount(new LambdaQueryWrapper<>()) > 0) {
            log.info("数据库已存在数据，跳过初始化");
            return; // 已有数据，直接返回
        }

        log.info("数据库为空，开始插入测试数据...");

        // ===== 1. 创建系统用户（6 个） =====
        log.info("---- 初始化系统用户 ----");

        // 管理员（1 个）
        SysUser admin = new SysUser();
        admin.setUsername("admin");                              // 用户名
        admin.setPassword(passwordEncoder.encode("admin123"));   // BCrypt 加密密码（明文 admin123）
        admin.setRole("ADMIN");                                   // 角色：管理员
        admin.setRealName("系统管理员");                          // 真实姓名
        admin.setStatus(1);                                       // 启用状态（1=启用）
        sysUserMapper.insert(admin);                              // INSERT INTO sys_user
        log.info("管理员创建成功：admin/admin123");

        // 教师1
        SysUser teacher1 = new SysUser();
        teacher1.setUsername("teacher1");
        teacher1.setPassword(passwordEncoder.encode("teacher123"));
        teacher1.setRole("TEACHER");
        teacher1.setRealName("张教授");
        teacher1.setStatus(1);
        sysUserMapper.insert(teacher1);

        // 教师2
        SysUser teacher2 = new SysUser();
        teacher2.setUsername("teacher2");
        teacher2.setPassword(passwordEncoder.encode("teacher123"));
        teacher2.setRole("TEACHER");
        teacher2.setRealName("李副教授");
        teacher2.setStatus(1);
        sysUserMapper.insert(teacher2);

        // 学生1
        SysUser student1 = new SysUser();
        student1.setUsername("student1");
        student1.setPassword(passwordEncoder.encode("student123"));
        student1.setRole("STUDENT");
        student1.setRealName("赵同学");
        student1.setStatus(1);
        sysUserMapper.insert(student1);

        // 学生2
        SysUser student2 = new SysUser();
        student2.setUsername("student2");
        student2.setPassword(passwordEncoder.encode("student123"));
        student2.setRole("STUDENT");
        student2.setRealName("钱同学");
        student2.setStatus(1);
        sysUserMapper.insert(student2);

        // 学生3
        SysUser student3 = new SysUser();
        student3.setUsername("student3");
        student3.setPassword(passwordEncoder.encode("student123"));
        student3.setRole("STUDENT");
        student3.setRealName("孙同学");
        student3.setStatus(1);
        sysUserMapper.insert(student3);
        log.info("用户创建完成：1管理员 + 2教师 + 3学生");

        // ===== 2. 创建教师档案（2 个） =====
        log.info("---- 初始化教师信息 ----");

        Teacher t1 = new Teacher();
        t1.setTeacherNo("T2024001");            // 工号
        t1.setName("张教授");                    // 姓名
        t1.setGender("男");                      // 性别
        t1.setTitle("教授");                      // 职称
        t1.setDepartment("计算机科学与技术学院");   // 院系
        t1.setPhone("13800000001");              // 电话
        teacherMapper.insert(t1);

        Teacher t2 = new Teacher();
        t2.setTeacherNo("T2024002");
        t2.setName("李副教授");
        t2.setGender("女");
        t2.setTitle("副教授");
        t2.setDepartment("数学与统计学院");
        t2.setPhone("13800000002");
        teacherMapper.insert(t2);
        log.info("教师信息初始化完成");

        // ===== 3. 创建班级（1 个） =====
        log.info("---- 初始化班级信息 ----");

        ClassInfo classInfo = new ClassInfo();
        classInfo.setClassName("软件工程1班");      // 班级名称
        classInfo.setGrade("2024级");               // 年级
        classInfo.setHeadTeacherId(t1.getId());     // 班主任 = 张教授（使用插入后自动获得的主键 ID）
        classInfoMapper.insert(classInfo);
        log.info("班级创建成功：软件工程1班");

        // ===== 4. 创建学生档案（3 个） =====
        log.info("---- 初始化学生信息 ----");

        Student s1 = new Student();
        s1.setStudentNo("20240001");               // 学号
        s1.setName("赵同学");                       // 姓名
        s1.setGender("男");                         // 性别
        s1.setAge(20);                              // 年龄
        s1.setPhone("13900000001");                 // 电话
        s1.setEmail("zhao@example.com");            // 邮箱
        s1.setClassId(classInfo.getId());           // 所属班级（关联班级表外键）
        studentMapper.insert(s1);

        Student s2 = new Student();
        s2.setStudentNo("20240002");
        s2.setName("钱同学");
        s2.setGender("女");
        s2.setAge(21);
        s2.setPhone("13900000002");
        s2.setEmail("qian@example.com");
        s2.setClassId(classInfo.getId());           // 同班
        studentMapper.insert(s2);

        Student s3 = new Student();
        s3.setStudentNo("20240003");
        s3.setName("孙同学");
        s3.setGender("男");
        s3.setAge(19);
        s3.setPhone("13900000003");
        s3.setEmail("sun@example.com");
        s3.setClassId(classInfo.getId());           // 同班
        studentMapper.insert(s3);
        log.info("学生信息初始化完成：3个学生");

        // ===== 5. 创建课程（3 门） =====
        log.info("---- 初始化课程信息 ----");

        Course c1 = new Course();
        c1.setCourseNo("CS101");                                // 课程编号
        c1.setName("Java 程序设计");                             // 课程名称
        c1.setCredit(3.0);                                       // 学分（3.0 学分）
        c1.setTeacherId(t1.getId());                             // 授课教师（关联教师表外键）
        c1.setSemester("2024-2025-1");                           // 学期
        c1.setDescription("学习 Java SE 基础语法、面向对象编程、集合框架等"); // 课程描述
        courseMapper.insert(c1);

        Course c2 = new Course();
        c2.setCourseNo("MATH201");
        c2.setName("高等数学");
        c2.setCredit(4.0);                                       // 高数 4 学分
        c2.setTeacherId(t2.getId());
        c2.setSemester("2024-2025-1");
        c2.setDescription("学习微积分、线性代数等高等数学知识");
        courseMapper.insert(c2);

        Course c3 = new Course();
        c3.setCourseNo("CS201");
        c3.setName("数据结构与算法");
        c3.setCredit(3.5);                                       // 3.5 学分
        c3.setTeacherId(t1.getId());                             // 也是张教授授课
        c3.setSemester("2024-2025-1");
        c3.setDescription("学习常见的数据结构和算法设计与分析");
        courseMapper.insert(c3);
        log.info("课程信息初始化完成：3门课程");

        // ===== 打印初始化结果 =====
        log.info("===== 测试数据初始化完成 =====");
        log.info("管理员：admin / admin123");
        log.info("教师1：teacher1 / teacher123");
        log.info("教师2：teacher2 / teacher123");
        log.info("学生1：student1 / student123");
        log.info("学生2：student2 / student123");
        log.info("学生3：student3 / student123");
    }
}
