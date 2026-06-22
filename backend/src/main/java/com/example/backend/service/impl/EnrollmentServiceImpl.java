/*
 * ================================================================
 * 文件位置:  src/main/java/com/example/backend/service/impl/EnrollmentServiceImpl.java
 * 架构层级:  Service 实现类（业务逻辑的真正执行者）
 * 对应接口:  src/main/java/com/example/backend/service/EnrollmentService.java
 * 被调用者:  src/main/java/com/example/backend/controller/EnrollmentController.java
 *
 * 调用链路（以"选课"为例）:
 * 前端选课页面 → axios POST /api/enrollments/enroll { studentId: 1, courseId: 5 }
 *              → EnrollmentController.enroll()
 *              → EnrollmentService.enroll()     ← 接口（定义规范）
 *              → EnrollmentServiceImpl.enroll()  ← 本文件（执行具体逻辑）
 *              → 验证学生存在 → 验证课程存在 → 检查是否重复选课
 *              → INSERT INTO course_enrollment → 返回成功
 *
 * 本文件在架构中的职责
 * ┌─────────────────────────────────────────────────┐
 * │  Controller  (接收请求，不写业务)               │
 * │       ↓ 调用                                    │
 * │  Service    (接口：定义能干什么)                 │
 * │       ↓ 实现                                    │
 * │  ServiceImpl (本文件：具体怎么干)  ← 你在这      │
 * │       ↓ 调用                                    │
 * │  Mapper     (数据库操作)                         │
 * │       ↓ 操作                                    │
 * │  Database   (MySQL)                             │
 * └─────────────────────────────────────────────────┘
 *
 * JS 类比（Express + Mongoose）:
 *   // EnrollmentServiceImpl 就像这样一组业务函数：
 *   const EnrollmentService = {
 *     getByUsername: async (username) => { ... },
 *     getStudentIdByUsername: async (username) => { ... },
 *     getByStudentId: async (studentId) => { ... },
 *     enroll: async (dto) => { // 验证 + 插入
 *     unenroll: async (id) => { // 检查 + 删除
 *   }
 * ================================================================
 */

// ==================== 1. 包声明 ====================
package com.example.backend.service.impl; // 声明在 service/impl 子包下（impl = implementation，实现）

// ==================== 2. 导入其他类（import） ====================

// --- 2.1 第三方工具库 ---
// Hutool 是国产 Java 工具库，BeanUtil 用于在对象之间复制同名属性的值
// 相当于 JS 的 Object.assign(objA, objB) 或 lodash 的 _.assign
import cn.hutool.core.bean.BeanUtil; // Hutool 属性复制工具

// --- 2.2 MyBatis-Plus 框架 ---
/*
 * LambdaQueryWrapper：MyBatis-Plus 的"查询条件构造器"。
 * 它是一个 SQL WHERE 子句的构建器，能帮你动态拼 SQL 条件。
 * "Lambda" 的意思是：通过方法引用（如 Entity::getField）来指定字段名，
 * 而不是写字符串（如 "student_id"），这样就算改了字段名编译器也会报错。
 *
 * 类比 JS：
 *   // LambdaQueryWrapper 就像 Sequelize 的 where 条件对象：
 *   { studentId: dto.studentId, courseId: dto.courseId }
 *   // 但 MyBatis-Plus 的方法引用更安全（编译时检查）
 */
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper; // Lambda 条件构造器

// --- 2.3 本项目内部的类 ---
// 自定义的业务异常类，抛出后由 GlobalExceptionHandler 统一捕获转成 JSON 错误响应
import com.example.backend.common.BusinessException; // 业务异常
import com.example.backend.common.ResultCode;         // 统一状态码枚举

// --- DTO（前端传来的请求数据）---
import com.example.backend.dto.EnrollmentDTO; // 选课请求 DTO：{ studentId, courseId }

// --- Entity（数据库表对应的实体类）---
import com.example.backend.entity.Course;           // 课程实体 → 对应 course 表
import com.example.backend.entity.CourseEnrollment; // 选课实体 → 对应 course_enrollment 表（中间表）
import com.example.backend.entity.Student;          // 学生实体 → 对应 student 表
import com.example.backend.entity.SysUser;          // 系统用户实体 → 对应 sys_user 表（登录用户表）

// --- Mapper（数据库操作层）---
import com.example.backend.mapper.CourseEnrollmentMapper; // 选课 Mapper（操作 course_enrollment 表）
import com.example.backend.mapper.CourseMapper;           // 课程 Mapper（操作 course 表）
import com.example.backend.mapper.StudentMapper;          // 学生 Mapper（操作 student 表）
import com.example.backend.mapper.SysUserMapper;          // 用户 Mapper（操作 sys_user 表）

// --- Service 接口 & VO ---
import com.example.backend.service.EnrollmentService; // 本文件要实现的接口
import com.example.backend.vo.EnrollmentVO;           // 选课视图对象（返回给前端的组合数据）

// --- 2.4 Lombok 注解 ---
/*
 * Lombok 是 Java 的"代码生成器"——通过注解在编译时自动添加样板代码。
 * 你看到的 @RequiredArgsConstructor 不是装饰器，而是编译指令。
 * 编译后 class 文件里会自动多出构造函数代码，源文件不需要手写。
 */
import lombok.RequiredArgsConstructor; // 构造器注入：为 final 字段自动生成构造函数
import lombok.extern.slf4j.Slf4j;     // 日志：自动创建 log 对象（基于 SLF4J + Logback）

// --- 2.5 Spring 框架注解 ---
import org.springframework.stereotype.Service;                   // @Service：标识为业务 Bean
import org.springframework.transaction.annotation.Transactional; // @Transactional：声明式事务

// --- 2.6 Java 标准库 ---
import java.util.List; // 列表接口

// ==================== 3. 类的声明和注解 ====================

/*
 * @Slf4j
 *   Lombok 注解，编译后自动生成一行代码：
 *     private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(EnrollmentServiceImpl.class);
 *   然后你就可以在代码里直接写 log.info(...)、log.error(...)，不用手写 Logger。
 *
 *   类比 JS：
 *     // 相当于自动帮你创建了一个 console 对象
 *     const log = { info: console.log, error: console.error }
 */
@Slf4j

/*
 * @Service
 *   告诉 Spring："这个类是一个 Service 层的 Bean，请帮我管理它的生命周期"。
 *
 *   被 @Service 标注的类会在 Spring 启动时被扫描到，注册到 IoC 容器中。
 *   当 Controller 需要注入一个 EnrollmentService 类型的 Bean 时，
 *   Spring 发现容器里只有一个 EnrollmentServiceImpl，就自动注入它。
 *
 *   类比 JS 中的概念：
 *     // 没有 IoC 容器时（手动管理依赖）
 *     const service = new EnrollmentServiceImpl(mapper)
 *
 *     // 有 IoC 容器后（Spring 自动管理）
 *     // @Service 就像 register('enrollmentService', new EnrollmentServiceImpl(...))
 *     // 其他地方通过 @RequiredArgsConstructor 自动拿
 *
 *   @Service 和 @Component 的区别：
 *     - @Service 是 @Component 的子注解，语义上表示"这是业务层的 Bean"
 *     - @Component 是通用注解
 *     - @Repository 用于 DAO/Mapper 层
 *     - @Controller 用于控制器层
 *     - 功能上它们几乎一样，只是给开发者看的"标签"不同
 *     - 未来 Spring 可能会对 @Service 增加特定功能（AOP 增强等）
 */
@Service

/*
 * @RequiredArgsConstructor
 *   Lombok 注解：为所有 final 字段自动生成构造函数。
 *
 *   本类中有 4 个 final 字段：
 *     - courseEnrollmentMapper
 *     - courseMapper
 *     - studentMapper
 *     - sysUserMapper
 *
 *   Lombok 编译后等价于手写了：
 *     public EnrollmentServiceImpl(
 *         CourseEnrollmentMapper courseEnrollmentMapper,
 *         CourseMapper courseMapper,
 *         StudentMapper studentMapper,
 *         SysUserMapper sysUserMapper
 *     ) {
 *         this.courseEnrollmentMapper = courseEnrollmentMapper;
 *         this.courseMapper = courseMapper;
 *         this.studentMapper = studentMapper;
 *         this.sysUserMapper = sysUserMapper;
 *     }
 *
 *   Spring 的依赖注入过程：
 *     1. Spring 启动时扫描到 @Service，决定要创建 EnrollmentServiceImpl 的实例
 *     2. 发现这个类只有一个构造函数（Lombok 生成的），且参数都是接口类型
 *     3. Spring 在容器中查找这些接口对应的 Bean：
 *        - 找到 CourseEnrollmentMapper 的实现类（MyBatis 自动生成的代理）
 *        - 找到 CourseMapper 的实现类
 *        - 找到 StudentMapper 的实现类
 *        - 找到 SysUserMapper 的实现类
 *     4. 把这些 Bean 传入构造函数，创建实例
 *     5. 把创建好的实例注册到容器中
 *
 *   这种"通过构造函数注入依赖"的方式，叫"构造器注入"（Constructor Injection），
 *   是目前 Spring 社区推荐的最佳实践，因为：
 *     - final 字段保证不可变性
 *     - 不需要 @Autowired 注解（Spring 4.3+ 单构造函数自动装配）
 *     - 方便单元测试（new 的时候直接传 Mock 对象）
 *
 *   类比 JS 的依赖注入：
 *     class EnrollmentServiceImpl {
 *       #enrollmentMapper; #courseMapper; #studentMapper; #userMapper;
 *       constructor(em, cm, sm, um) {
 *         this.#enrollmentMapper = em; // final = 只在这里赋值，之后不可改
 *         ...
 *       }
 *     }
 */
@RequiredArgsConstructor

/*
 * public class EnrollmentServiceImpl implements EnrollmentService { ... }
 *
 *   public                     —— 公开类，任何地方都能使用
 *   class                      —— 类定义关键字
 *   EnrollmentServiceImpl      —— 类名：接口名 + Impl 后缀（Java 命名惯例）
 *   implements EnrollmentService —— implements 关键字：声明这个类实现了 EnrollmentService 接口
 *                                 "实现"意味着：必须提供接口中所有方法的具体代码
 *
 *   如果漏写了接口里的某个方法，编译器会直接报错，保证不遗漏。
 */
public class EnrollmentServiceImpl implements EnrollmentService { // 选课服务实现类

    // ==================== 4. 字段声明（依赖注入的接收方） ====================

    /*
     * private final CourseEnrollmentMapper courseEnrollmentMapper;
     *
     *   private    —— 封装：这个字段只有本类能访问
     *   final      —— 不可变：赋值后不能修改（类似 JS 的 const）
     *   CourseEnrollmentMapper —— 类型：操作 course_enrollment 表的 Mapper
     *   courseEnrollmentMapper —— 变量名：驼峰命名
     *
     *   这些字段的值由 Spring 通过构造函数注入，不需要我们手动 new。
     *   有 4 个 Mapper，说明这个 Service 需要操作 4 张表，业务比较复杂。
     */
    private final CourseEnrollmentMapper courseEnrollmentMapper; // 选课表 Mapper
    private final CourseMapper courseMapper;                     // 课程表 Mapper（用于验证课程存在）
    private final StudentMapper studentMapper;                   // 学生表 Mapper（用于验证学生存在）
    private final SysUserMapper sysUserMapper;                   // 用户表 Mapper（用于用户名→学生关联）

    // ==================== 5. 方法实现 ====================

    /*
     * ================================================================
     * 方法 1：根据登录用户名查询该用户已选课程列表
     *
     * 这个方法通过"用户名 → 真实姓名 → 学生ID → 选课列表"的链路，
     * 把登录用户和选课数据关联起来。
     *
     * 为什么这么绕？
     *   因为 sys_user（登录表）和 student（学生表）是两个独立的表。
     *   一个人既可以是系统用户，也可以是学生（比如学生登录）。
     *   它们通过 realName / name 这个"自然键"关联，而不是外键。
     *
     *   这种设计在实际项目中很常见：用户表是通用的鉴权表，
     *   学生表是业务表，两者通过业务字段关联而非数据库外键。
     * ================================================================
     */
    @Override // @Override 注解：声明这个方法是重写/实现接口中的方法（可选的，但写了编译器会帮你检查）
    public List<EnrollmentVO> getByUsername(String username) {

        /*
         * 第1步：通过用户名查询 sys_user 表，获取该用户的真实姓名
         *
         * LambdaQueryWrapper<SysUser> userWrapper = new LambdaQueryWrapper<>();
         *   创建 SysUser 表的查询条件构造器。
         *   泛型 <SysUser> 表示这个 Wrapper 是为 SysUser 实体服务的。
         *
         *   LambdaQueryWrapper 的核心概念：
         *     - 它是一个"条件对象"，用来拼 SQL 的 WHERE 子句
         *     - 每个方法调用都在往 WHERE 里加条件
         *     - 最后传给 Mapper 的查询方法，生成完整 SQL
         *
         *   类比 JS：
         *     const query = {}                    // new LambdaQueryWrapper<>()
         *     query.username = username           // .eq(SysUser::getUsername, username)
         *     const user = await SysUser.findOne(query) // sysUserMapper.selectOne(queryWrapper)
         */
        LambdaQueryWrapper<SysUser> userWrapper = new LambdaQueryWrapper<>(); // 创建 SysUser 查询条件构造器

        /*
         * userWrapper.eq(SysUser::getUsername, username);
         *
         *   .eq() 是 "equals" 的缩写，表示"等于"条件
         *   SysUser::getUsername 是 Java 的"方法引用"（Method Reference）语法
         *
         *   方法引用 SysUser::getUsername 的意思是：
         *     "取 SysUser 类的 getUsername() 这个方法，但不是调用它，而是把它当作一个函数引用传进去"
         *
         *   MyBatis-Plus 通过反射解析这个引用，获取对应的数据库字段名 "username"，
         *   然后构建 SQL：WHERE username = 'student1'
         *
         *   为什么不用字符串 "username"？
         *     如果用字符串，将来改了实体类的字段名（如 username → loginName），
         *     这里的字符串不会报错，导致运行时 Bug。
         *     用方法引用的话，改字段名后编译直接报错，提前发现问题。
         *
         *   类比 JS：
         *     // 字符串方式（不安全，运行时才报错）
         *     db.query("SELECT * FROM user WHERE username = ?", [username])
         *     // Lambda/方法引用方式（安全，编译时报错）
         *     User.where({ username: username }) // TypeScript 的类型检查
         */
        userWrapper.eq(SysUser::getUsername, username); // 添加条件：username = 传入的用户名

        /*
         * sysUserMapper.selectOne(queryWrapper);
         *
         *   selectOne()：MyBatis-Plus 的通用查询方法，返回一条记录。
         *   如果查不到，返回 null。
         *   如果查到多条（不应该出现），会抛出 TooManyResultsException。
         *
         *   最终生成的 SQL（约等于）：
         *     SELECT * FROM sys_user WHERE username = 'student1' LIMIT 1
         */
        SysUser user = sysUserMapper.selectOne(userWrapper); // 执行查询，获取用户对象

        // 如果用户不存在或没有真实姓名，无法继续
        if (user == null || user.getRealName() == null) { // 用户信息不完整
            throw new BusinessException(ResultCode.BUSINESS_ERROR.getCode(), "用户信息不完整"); // 抛业务异常
        }

        /*
         * 第2步：用真实姓名匹配 student 表中的 name 字段，找到对应的学生
         *
         * 这步的逻辑是：
         *   sys_user.real_name = student.name → 两者是同一个人的不同标识
         *
         * 类比 JS：
         *   const student = await Student.findOne({ name: user.realName })
         */
        LambdaQueryWrapper<Student> studentWrapper = new LambdaQueryWrapper<>(); // 创建 Student 查询条件构造器
        studentWrapper.eq(Student::getName, user.getRealName()); // 条件：name = 用户的真实姓名
        Student student = studentMapper.selectOne(studentWrapper); // 执行查询

        if (student == null) { // 没有匹配的学生记录
            throw new BusinessException(ResultCode.BUSINESS_ERROR.getCode(), "未找到关联的学生信息"); // 抛异常
        }

        /*
         * 第3步：用找到的学生 ID，查询该学生的所有选课记录
         *
         * 调用 getByStudentId 方法（本类的另一个方法），复用逻辑。
         * 这样就不用在两个方法里重复写联表查询的代码。
         */
        return getByStudentId(student.getId()); // 复用已有查询，返回选课列表
    }

    /*
     * ================================================================
     * 方法 2：根据用户名获取对应的学生 ID
     *
     * 这是一个辅助方法，和 getByUsername 的前两步逻辑完全一样，
     * 只是最后返回学生 ID 而不是选课列表。
     *
     * 用于那些只需要学生 ID 的场景（如前端需要 studentId 来做进一步操作）。
     * ================================================================
     */
    @Override
    public Long getStudentIdByUsername(String username) {
        // 查 sys_user 表获取用户信息
        LambdaQueryWrapper<SysUser> userWrapper = new LambdaQueryWrapper<>();
        userWrapper.eq(SysUser::getUsername, username);
        SysUser user = sysUserMapper.selectOne(userWrapper);

        if (user == null || user.getRealName() == null) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR.getCode(), "用户信息不完整");
        }

        // 查 student 表匹配真实姓名
        LambdaQueryWrapper<Student> studentWrapper = new LambdaQueryWrapper<>();
        studentWrapper.eq(Student::getName, user.getRealName());
        Student student = studentMapper.selectOne(studentWrapper);

        if (student == null) {
            throw new BusinessException(ResultCode.BUSINESS_ERROR.getCode(), "未找到关联的学生信息");
        }

        return student.getId(); // 返回学生主键 ID
    }

    /*
     * ================================================================
     * 方法 3：根据学生 ID 查询该学生的所有选课记录
     *
     * 这个方法调用的是 Mapper 中自定义的联表查询方法，
     * 不是 MyBatis-Plus 的通用方法。
     *
     * 返回的 List<EnrollmentVO> 包含：
     *   - 选课记录 ID
     *   - 学生姓名
     *   - 课程名称
     *   - 教师姓名
     *   - 学分
     *   - 学期
     * 这些字段来自 course_enrollment JOIN course JOIN teacher 三表联查。
     * ================================================================
     */
    @Override
    public List<EnrollmentVO> getByStudentId(Long studentId) {
        // 调用 Mapper 中自定义的联表查询方法
        // 这个方法在 CourseEnrollmentMapper.java 中定义，SQL 在对应的 XML 或注解中
        // 不是 MyBatis-Plus 的通用方法，而是自己写的复杂查询
        return courseEnrollmentMapper.selectByStudentId(studentId); // 返回联表查询结果
    }

    /*
     * ================================================================
     * 方法 4：选课（核心业务）
     *
     * 这是一个写操作，所以加了 @Transactional 注解保证事务一致性。
     *
     * 业务规则（按执行顺序）：
     *   1. 学生必须存在（student 表中有记录）
     *   2. 课程必须存在（course 表中有记录）
     *   3. 不能重复选同一门课（course_enrollment 表中没有相同 courseId + studentId 的记录）
     *
     *   全部检查通过后，才执行 INSERT。
     * ================================================================
     */
    @Override
    @Transactional(rollbackFor = Exception.class) // 声明式事务：遇到任何异常自动回滚
    /*
     * @Transactional(rollbackFor = Exception.class) 详解：
     *
     *   这个注解告诉 Spring："这个方法里的所有数据库操作要放在同一个事务里执行"。
     *
     *   事务（Transaction）的 ACID 特性（类比 JS 理解）：
     *     - 原子性（Atomicity）：要么全做，要么全不做（类似 JS 的 Promise.all）
     *     - 一致性（Consistency）：数据库从一个一致状态到另一个一致状态
     *     - 隔离性（Isolation）：并发操作互不干扰
     *     - 持久性（Durability）：提交后永久保存
     *
     *   rollbackFor = Exception.class：
     *     指定哪些异常触发回滚。这里配置"所有 Exception 都回滚"。
     *     默认情况下 Spring 只对 RuntimeException 回滚，不处理 CheckedException，
     *     所以显式指定 rollbackFor 是一个好习惯。
     *
     *   在这个方法中，如果 INSERT 失败了，之前的状态会被回滚。
     *   但由于这里只做了一次 INSERT（之前只是查询），实际上回滚的只是这次插入。
     *
     *   什么是回滚？
     *     类比 Git 的 reset --hard：把数据库回到这个方法执行之前的状态。
     *     但这个概念在 Git 中不够准确。事务更像是一次"原子操作"。
     *
     *   JS 类比：
     *     // MongoDB 没有原生事务概念（4.0+ 有 session），但在关系型数据库中：
     *     await db.transaction(async (trx) => {
     *       // 如果这里抛异常，前面的操作全部撤销
     *       await trx('enrollments').insert({ studentId, courseId })
     *     })
     */
    public void enroll(EnrollmentDTO dto) {

        /*
         * 第1步：验证学生是否存在
         *
         * studentMapper.selectById(dto.getStudentId());
         *   selectById()：MyBatis-Plus 的通用方法，按主键查询。
         *   生成的 SQL：SELECT * FROM student WHERE id = ?
         *
         *   dto.getStudentId()：从 DTO 中取出 studentId（前端传来的）
         */
        Student student = studentMapper.selectById(dto.getStudentId()); // 按主键查询学生
        if (student == null) { // 学生记录不存在的处理
            /*
             * throw new BusinessException(ResultCode.BUSINESS_ERROR.getCode(), "学生不存在");
             *
             *   BusinessException 是自定义的运行时异常，继承了 RuntimeException。
             *   抛出后会被 Spring 的全局异常处理器 GlobalExceptionHandler 捕获。
             *
             *   ResultCode.BUSINESS_ERROR.getCode()：获取业务错误对应的数字状态码（如 5001）
             *
             *   对比直接 throw new RuntimeException()：
             *     - BusinessException 带有业务语义（状态码 + 错误描述）
             *     - 全局异常处理器可以根据状态码返回不同格式的 JSON
             *     - 方便前端统一处理错误
             *
             *   JS 类比：
             *     throw new AppError('BUSINESS_ERROR', '学生不存在')
             *     // 被 Express 的全局错误中间件捕获并返回 JSON
             */
            throw new BusinessException(ResultCode.BUSINESS_ERROR.getCode(), "学生不存在");
        }

        // 第2步：验证课程是否存在
        Course course = courseMapper.selectById(dto.getCourseId()); // 按主键查询课程
        if (course == null) { // 课程不存在
            throw new BusinessException(ResultCode.BUSINESS_ERROR.getCode(), "课程不存在");
        }

        /*
         * 第3步：检查是否已经选过该课程（防止重复选课）
         *
         * 构建条件：WHERE student_id = dto.studentId AND course_id = dto.courseId
         *
         * queryWrapper.eq(CourseEnrollment::getStudentId, dto.getStudentId());
         *   .eq() 是 LambdaQueryWrapper 的链式调用，每次调用返回自身（this），
         *   所以可以连续 .eq().eq() 构建多个 AND 条件。
         *
         *   这叫做"流式 API"（Fluent API），类似于 JS 的 Builder 模式：
         *     queryBuilder.eq('studentId', id).eq('courseId', id).build()
         *
         *   selectCount()：查符合条件的记录数。生成的 SQL：
         *     SELECT COUNT(*) FROM course_enrollment WHERE student_id = ? AND course_id = ?
         */
        LambdaQueryWrapper<CourseEnrollment> queryWrapper = new LambdaQueryWrapper<>(); // 条件构造器
        queryWrapper.eq(CourseEnrollment::getStudentId, dto.getStudentId()); // 条件1：student_id = 学生ID
        queryWrapper.eq(CourseEnrollment::getCourseId, dto.getCourseId());     // 条件2：course_id = 课程ID

        if (courseEnrollmentMapper.selectCount(queryWrapper) > 0) { // 如果已有记录（count > 0）
            throw new BusinessException(ResultCode.BUSINESS_ERROR.getCode(), "已选过该课程，不能重复选择");
        }

        /*
         * 第4步：创建选课实体并保存
         *
         * new CourseEnrollment()：创建一个空的选课实体对象。
         * enrollment.setStudentId(...)：设置学生 ID。
         * enrollment.setCourseId(...)：设置课程 ID。
         *
         * courseEnrollmentMapper.insert(enrollment)：
         *   MyBatis-Plus 的通用插入方法，把实体对象的所有非空字段插入数据库。
         *   如果实体类配置了自动填充（create_time 等），这些字段也会自动填。
         *
         *   生成的 SQL（约等于）：
         *     INSERT INTO course_enrollment (student_id, course_id) VALUES (?, ?)
         *
         *   类比 JS：
         *     await Enrollment.create({ studentId: dto.studentId, courseId: dto.courseId })
         */
        CourseEnrollment enrollment = new CourseEnrollment(); // 创建选课实体对象
        enrollment.setStudentId(dto.getStudentId()); // 设置学生 ID
        enrollment.setCourseId(dto.getCourseId());     // 设置课程 ID
        courseEnrollmentMapper.insert(enrollment);     // 插入数据库

        // 记录日志：大括号 {} 是 SLF4J 的占位符，运行时会被后面的参数依次替换
        // log.info("选课成功，学生ID：{}，课程ID：{}，课程名：{}", a, b, c)
        // 输出：选课成功，学生ID：1，课程ID：5，课程名：高等数学
        log.info("选课成功，学生ID：{}，课程ID：{}，课程名：{}",
                dto.getStudentId(), dto.getCourseId(), course.getName());
    }

    /*
     * ================================================================
     * 方法 5：退课（取消选课记录）
     *
     * 参数是选课记录的主键 ID（course_enrollment 表的 id），
     * 不是学生 ID 也不是课程 ID。
     *
     * 前端通常会从选课列表中获取每条记录的 id，点击"退课"时传给后端。
     * ================================================================
     */
    @Override
    @Transactional(rollbackFor = Exception.class) // 声明式事务：删除操作也在事务中执行
    public void unenroll(Long id) {

        /*
         * 第1步：检查选课记录是否存在
         *
         * selectById(id)：按主键查询。
         * 如果记录已经被删了（并发操作），返回 null，我们抛出异常。
         */
        CourseEnrollment enrollment = courseEnrollmentMapper.selectById(id); // 按主键查询
        if (enrollment == null) { // 记录不存在
            throw new BusinessException(ResultCode.BUSINESS_ERROR.getCode(), "选课记录不存在");
        }

        /*
         * 第2步：删除选课记录
         *
         * deleteById(id)：按主键删除。
         * 生成的 SQL：DELETE FROM course_enrollment WHERE id = ?
         *
         * 类比 JS：
         *   await Enrollment.deleteById(id)
         */
        courseEnrollmentMapper.deleteById(id); // 执行删除
        log.info("退课成功，选课记录ID：{}", id); // 记录日志
    }
}
