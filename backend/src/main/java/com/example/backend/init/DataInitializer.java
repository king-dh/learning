package com.example.backend.init; // 声明初始化包

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper; // Lambda 条件构造器
import com.example.backend.entity.ClassInfo; // 班级实体
import com.example.backend.entity.Course; // 课程实体
import com.example.backend.entity.Student; // 学生实体
import com.example.backend.entity.SysUser; // 用户实体
import com.example.backend.entity.Teacher; // 教师实体
import com.example.backend.mapper.ClassInfoMapper; // 班级 Mapper
import com.example.backend.mapper.CourseMapper; // 课程 Mapper
import com.example.backend.mapper.StudentMapper; // 学生 Mapper
import com.example.backend.mapper.SysUserMapper; // 用户 Mapper
import com.example.backend.mapper.TeacherMapper; // 教师 Mapper
import lombok.RequiredArgsConstructor; // 构造器注入
import lombok.extern.slf4j.Slf4j; // 日志
import org.springframework.boot.CommandLineRunner; // Spring Boot 启动后执行
import org.springframework.security.crypto.password.PasswordEncoder; // 密码加密器
import org.springframework.stereotype.Component; // 组件注解
import org.springframework.transaction.annotation.Transactional; // 事务

/**
 * 数据初始化器
 * 实现 CommandLineRunner 接口，在 Spring Boot 应用启动完成后自动执行
 * 检查并插入测试数据，方便开发和测试
 */
@Slf4j // 日志
@Component // 声明为 Spring 组件
@RequiredArgsConstructor // 构造器注入
public class DataInitializer implements CommandLineRunner { // 实现 CommandLineRunner

    private final SysUserMapper sysUserMapper;   // 用户 Mapper
    private final StudentMapper studentMapper;   // 学生 Mapper
    private final TeacherMapper teacherMapper;   // 教师 Mapper
    private final ClassInfoMapper classInfoMapper; // 班级 Mapper
    private final CourseMapper courseMapper;     // 课程 Mapper
    private final PasswordEncoder passwordEncoder; // 密码加密器

    /**
     * 启动后自动执行的方法
     * @param args 命令行参数
     */
    @Override
    @Transactional(rollbackFor = Exception.class) // 事务，确保初始化数据全部成功或全部回滚
    public void run(String... args) { // main 方法参数
        log.info("===== 开始检查并初始化测试数据 ====="); // 标记开始

        // 如果用户表已有数据，说明已初始化过，则跳过
        if (sysUserMapper.selectCount(new LambdaQueryWrapper<>()) > 0) { // 已有用户数据
            log.info("数据库已存在数据，跳过初始化"); // 跳过
            return; // 直接返回
        }

        log.info("数据库为空，开始插入测试数据..."); // 提示开始插入

        // ===== 1. 创建用户 =====
        log.info("---- 初始化系统用户 ----");

        // 管理员用户
        SysUser admin = new SysUser(); // 创建用户实体
        admin.setUsername("admin"); // 用户名
        admin.setPassword(passwordEncoder.encode("admin123")); // BCrypt 加密密码
        admin.setRole("ADMIN"); // 角色：管理员
        admin.setRealName("系统管理员"); // 真实姓名
        admin.setStatus(1); // 启用
        sysUserMapper.insert(admin); // 插入数据库
        log.info("管理员创建成功：admin/admin123");

        // 教师用户（2个）
        SysUser teacher1 = new SysUser();
        teacher1.setUsername("teacher1");
        teacher1.setPassword(passwordEncoder.encode("teacher123"));
        teacher1.setRole("TEACHER");
        teacher1.setRealName("张教授");
        teacher1.setStatus(1);
        sysUserMapper.insert(teacher1);

        SysUser teacher2 = new SysUser();
        teacher2.setUsername("teacher2");
        teacher2.setPassword(passwordEncoder.encode("teacher123"));
        teacher2.setRole("TEACHER");
        teacher2.setRealName("李副教授");
        teacher2.setStatus(1);
        sysUserMapper.insert(teacher2);

        // 学生用户（3个）
        SysUser student1 = new SysUser();
        student1.setUsername("student1");
        student1.setPassword(passwordEncoder.encode("student123"));
        student1.setRole("STUDENT");
        student1.setRealName("赵同学");
        student1.setStatus(1);
        sysUserMapper.insert(student1);

        SysUser student2 = new SysUser();
        student2.setUsername("student2");
        student2.setPassword(passwordEncoder.encode("student123"));
        student2.setRole("STUDENT");
        student2.setRealName("钱同学");
        student2.setStatus(1);
        sysUserMapper.insert(student2);

        SysUser student3 = new SysUser();
        student3.setUsername("student3");
        student3.setPassword(passwordEncoder.encode("student123"));
        student3.setRole("STUDENT");
        student3.setRealName("孙同学");
        student3.setStatus(1);
        sysUserMapper.insert(student3);
        log.info("用户创建完成：1管理员 + 2教师 + 3学生");

        // ===== 2. 创建教师档案 =====
        log.info("---- 初始化教师信息 ----");

        Teacher t1 = new Teacher();
        t1.setTeacherNo("T2024001"); // 工号
        t1.setName("张教授"); // 姓名（与用户一致）
        t1.setGender("男"); // 性别
        t1.setTitle("教授"); // 职称
        t1.setDepartment("计算机科学与技术学院"); // 院系
        t1.setPhone("13800000001"); // 电话
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

        // ===== 3. 创建班级 =====
        log.info("---- 初始化班级信息 ----");

        ClassInfo classInfo = new ClassInfo();
        classInfo.setClassName("软件工程1班"); // 班级名称
        classInfo.setGrade("2024级"); // 年级
        classInfo.setHeadTeacherId(t1.getId()); // 班主任：张教授
        classInfoMapper.insert(classInfo);
        log.info("班级创建成功：软件工程1班");

        // ===== 4. 创建学生档案 =====
        log.info("---- 初始化学生信息 ----");

        Student s1 = new Student();
        s1.setStudentNo("20240001"); // 学号
        s1.setName("赵同学"); // 姓名
        s1.setGender("男"); // 性别
        s1.setAge(20); // 年龄
        s1.setPhone("13900000001"); // 手机
        s1.setEmail("zhao@example.com"); // 邮箱
        s1.setClassId(classInfo.getId()); // 所属班级
        studentMapper.insert(s1);

        Student s2 = new Student();
        s2.setStudentNo("20240002");
        s2.setName("钱同学");
        s2.setGender("女");
        s2.setAge(21);
        s2.setPhone("13900000002");
        s2.setEmail("qian@example.com");
        s2.setClassId(classInfo.getId());
        studentMapper.insert(s2);

        Student s3 = new Student();
        s3.setStudentNo("20240003");
        s3.setName("孙同学");
        s3.setGender("男");
        s3.setAge(19);
        s3.setPhone("13900000003");
        s3.setEmail("sun@example.com");
        s3.setClassId(classInfo.getId());
        studentMapper.insert(s3);
        log.info("学生信息初始化完成：3个学生");

        // ===== 5. 创建课程 =====
        log.info("---- 初始化课程信息 ----");

        Course c1 = new Course();
        c1.setCourseNo("CS101"); // 课程编号
        c1.setName("Java 程序设计"); // 课程名称
        c1.setCredit(3.0); // 学分
        c1.setTeacherId(t1.getId()); // 授课教师：张教授
        c1.setSemester("2024-2025-1"); // 学期
        c1.setDescription("学习 Java SE 基础语法、面向对象编程、集合框架等"); // 描述
        courseMapper.insert(c1);

        Course c2 = new Course();
        c2.setCourseNo("MATH201");
        c2.setName("高等数学");
        c2.setCredit(4.0);
        c2.setTeacherId(t2.getId()); // 授课教师：李副教授
        c2.setSemester("2024-2025-1");
        c2.setDescription("学习微积分、线性代数等高等数学知识");
        courseMapper.insert(c2);

        Course c3 = new Course();
        c3.setCourseNo("CS201");
        c3.setName("数据结构与算法");
        c3.setCredit(3.5);
        c3.setTeacherId(t1.getId()); // 授课教师：张教授
        c3.setSemester("2024-2025-1");
        c3.setDescription("学习常见的数据结构和算法设计与分析");
        courseMapper.insert(c3);
        log.info("课程信息初始化完成：3门课程");

        log.info("===== 测试数据初始化完成 =====");
        log.info("管理员：admin / admin123");
        log.info("教师1：teacher1 / teacher123");
        log.info("教师2：teacher2 / teacher123");
        log.info("学生1：student1 / student123");
        log.info("学生2：student2 / student123");
        log.info("学生3：student3 / student123");
    }
}
