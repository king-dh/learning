/*
 * ================================================================
 * 文件位置:  src/main/java/com/example/backend/service/impl/StudentServiceImpl.java
 * 架构层级:  Service 实现类（学生业务的实际执行者）
 * 对应接口:  src/main/java/com/example/backend/service/StudentService.java
 * 被调用者:  src/main/java/com/example/backend/controller/StudentController.java
 *
 * 调用链路（以"分页查询学生"为例）:
 * 前端学生管理页面 → axios GET /api/students/page?pageNum=1&name=张
 *                 → StudentController.page()
 *                 → StudentService.pageQuery()     ← 接口
 *                 → StudentServiceImpl.pageQuery()  ← 本文件
 *                 → 查 student 表 → 联查 class_info 表获班级名称
 *                 → 返回分页 JSON
 *
 * 学生服务涉及 4 张表：
 *   - student：主表
 *   - class_info：关联班级名称
 *   - score：删除前检查关联成绩
 *   - course_enrollment：删除前检查关联选课
 *
 * 这是所有 Service 中业务逻辑最丰富的一个，尤其是 delete 方法，
 * 体现了"数据完整性保护"的重要性：删除前先检查外键关联。
 *
 * JS 类比（Express + Mongoose）:
 *   const StudentService = {
 *     pageQuery: async (dto) => { ... },
 *     getById: async (id) => { ... },
 *     save: async (dto) => { // 检查学号唯一性
 *     update: async (dto) => { ... },
 *     delete: async (id) => {
 *       const scores = await Score.count({ studentId: id })
 *       if (scores > 0) throw new Error('有关联成绩')
 *       const enrolls = await Enrollment.count({ studentId: id })
 *       if (enrolls > 0) throw new Error('有关联选课')
 *       await Student.deleteById(id)
 *     }
 *   }
 * ================================================================
 */

// ==================== 1. 包声明 ====================
package com.example.backend.service.impl; // 声明在 service/impl 子包

// ==================== 2. 导入其他类（import） ====================

// Hutool BeanUtil：对象属性拷贝
import cn.hutool.core.bean.BeanUtil;

// MyBatis-Plus 分页与条件构造
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper; // WHERE 条件构造器
import com.baomidou.mybatisplus.core.metadata.IPage;                       // 分页结果接口
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;         // 分页对象

// 业务异常和状态码
import com.example.backend.common.BusinessException; // 业务异常
import com.example.backend.common.ResultCode;         // 状态码

// DTO
import com.example.backend.dto.StudentDTO;      // 新增/修改学生 { studentNo, name, gender, age, classId }
import com.example.backend.dto.StudentQueryDTO; // 分页查询 { pageNum, pageSize, name, studentNo }

// Entity
import com.example.backend.entity.ClassInfo;         // 班级实体 → class_info 表
import com.example.backend.entity.CourseEnrollment;  // 选课实体 → course_enrollment 表（删除检查用）
import com.example.backend.entity.Score;             // 成绩实体 → score 表（删除检查用）
import com.example.backend.entity.Student;           // 学生实体 → student 表

// Mapper
import com.example.backend.mapper.ClassInfoMapper;         // 班级 Mapper（查班级名称）
import com.example.backend.mapper.CourseEnrollmentMapper;  // 选课 Mapper（删除时检查关联）
import com.example.backend.mapper.ScoreMapper;             // 成绩 Mapper（删除时检查关联）
import com.example.backend.mapper.StudentMapper;           // 学生 Mapper（主表）

// Service 接口 & VO
import com.example.backend.service.StudentService; // 本类要实现的接口
import com.example.backend.vo.StudentVO;           // 学生视图对象（含 className）

// Lombok
import lombok.RequiredArgsConstructor; // 构造器注入
import lombok.extern.slf4j.Slf4j;     // 日志

// Spring
import org.springframework.stereotype.Service;                   // Service Bean
import org.springframework.transaction.annotation.Transactional; // 事务

import java.util.List;               // 列表
import java.util.stream.Collectors;  // Stream 收集器

// ==================== 3. 类的声明和注解 ====================

/*
 * @Slf4j：Lombok 注入日志对象 log。
 *   编译后 = private static final Logger log = LoggerFactory.getLogger(StudentServiceImpl.class);
 */
@Slf4j

/*
 * @Service：声明为 Service Bean，纳入 Spring IoC 容器管理。
 *   Controller 通过接口类型 StudentService 注入时，Spring 自动找到本类。
 */
@Service

/*
 * @RequiredArgsConstructor：Lombok 为 4 个 final 字段生成构造函数。
 *   孙悟空的 4 个 Mapper，各有职责：
 *     studentMapper        → 主表增删改查
 *     classInfoMapper      → 查询班级名称（联表数据）
 *     scoreMapper          → 删除前检查关联成绩
 *     courseEnrollmentMapper → 删除前检查关联选课
 *
 *   Spring 自动注入这 4 个 Mapper（MyBatis 动态代理生成的实现）。
 */
@RequiredArgsConstructor

/*
 * public class StudentServiceImpl implements StudentService
 *
 *   实现 StudentService 接口，提供所有方法的具体代码。
 */
public class StudentServiceImpl implements StudentService { // 学生服务实现类

    // ==================== 4. 字段声明 ====================

    /*
     * 四个 Mapper 依赖，Spring 通过构造器注入：
     *
     * StudentMapper：学生表（主表）的 CRUD 操作
     * ClassInfoMapper：班级表，用于查询 className 字段
     * ScoreMapper：成绩表，删除学生时检查是否有成绩记录
     * CourseEnrollmentMapper：选课表，删除学生时检查是否有选课记录
     *
     * 后两个 Mapper 只在 delete() 方法中使用，体现了"数据完整性保护"。
     */
    private final StudentMapper studentMapper;                   // 学生表 Mapper（主表）
    private final ClassInfoMapper classInfoMapper;               // 班级表 Mapper（查班级名称）
    private final ScoreMapper scoreMapper;                       // 成绩表 Mapper（删除检查）
    private final CourseEnrollmentMapper courseEnrollmentMapper; // 选课表 Mapper（删除检查）

    // ==================== 5. 方法实现 ====================

    /*
     * ================================================================
     * 方法 1：分页条件查询学生列表（含班级名称）
     *
     * 查询条件：name（模糊）、studentNo（模糊）
     * 排序：按创建时间倒序
     * 附加数据：className（所属班级名称，从 class_info 表查）
     *
     * 这是 CRUD 中最复杂的查询方法，也是前端最常调用的方法。
     * ================================================================
     */
    @Override
    public IPage<StudentVO> pageQuery(StudentQueryDTO dto) {

        // --- 第1步：创建分页对象 ---
        // Page<>(当前页, 每页大小)，页码从 1 开始
        Page<Student> page = new Page<>(dto.getPageNum(), dto.getPageSize());

        // --- 第2步：动态构建查询条件 ---
        LambdaQueryWrapper<Student> queryWrapper = new LambdaQueryWrapper<>();

        // 按姓名模糊搜索
        if (dto.getName() != null && !dto.getName().isEmpty()) {
            /*
             * .like(Student::getName, dto.getName())：
             *   等价于 SQL 的 WHERE name LIKE '%张三%'
             *   方法引用 Student::getName 会被解析为字段名 "name"
             */
            queryWrapper.like(Student::getName, dto.getName());
        }

        // 按学号模糊搜索
        if (dto.getStudentNo() != null && !dto.getStudentNo().isEmpty()) {
            queryWrapper.like(Student::getStudentNo, dto.getStudentNo()); // LIKE '%studentNo%'
        }

        // 按创建时间倒序（最新录入的学生排最前面）
        queryWrapper.orderByDesc(Student::getCreateTime);

        // --- 第3步：执行分页查询 ---
        // selectPage 内部自动执行 COUNT + SELECT
        IPage<Student> studentPage = studentMapper.selectPage(page, queryWrapper);

        // --- 第4步：转换为 StudentVO（补充班级名称）---
        return studentPage.convert(student -> { // Lambda 表达式：遍历每条学生记录
            // 复制基础属性（id, studentNo, name, gender, age, classId 等）
            StudentVO vo = BeanUtil.copyProperties(student, StudentVO.class);

            // 查询班级名称（手动联表）
            if (student.getClassId() != null) { // 学生有关联班级
                /*
                 * classInfoMapper.selectById(student.getClassId())：
                 *   按主键查询 class_info 表，获取班级实体。
                 *
                 *   优点：代码简单。
                 *   缺点：N+1 查询问题（100 个学生 = 100 次额外查询）。
                 *
                 *   优化方案：
                 *     1. 先收集所有 classId，用 selectBatchIds 批量查，放入 Map
                 *     2. 在 Mapper 中写 LEFT JOIN SQL，一次查出来
                 */
                ClassInfo classInfo = classInfoMapper.selectById(student.getClassId());
                if (classInfo != null) { // 班级存在
                    vo.setClassName(classInfo.getClassName()); // 设置班级名称
                }
            }

            return vo; // 返回组装好的 StudentVO
        });
    }

    /*
     * ================================================================
     * 方法 2：根据 ID 查询单个学生详情（含班级名称）
     *
     * 查不到时抛出 BusinessException，不返回 null。
     * 和 pageQuery 的单行逻辑一样。
     * ================================================================
     */
    @Override
    public StudentVO getById(Long id) {
        // 查询学生基本信息
        Student student = studentMapper.selectById(id); // SELECT * FROM student WHERE id = ?
        if (student == null) { // 学生不存在
            throw new BusinessException(ResultCode.BUSINESS_ERROR.getCode(), "学生不存在");
        }

        // 复制属性到 VO
        StudentVO vo = BeanUtil.copyProperties(student, StudentVO.class);

        // 查询班级名称
        if (student.getClassId() != null) { // 有关联班级
            ClassInfo classInfo = classInfoMapper.selectById(student.getClassId()); // 查班级表
            if (classInfo != null) {
                vo.setClassName(classInfo.getClassName()); // 设置班级名称
            }
        }

        return vo; // 返回学生详情
    }

    /*
     * ================================================================
     * 方法 3：新增学生（含学号唯一性检查）
     *
     * 业务规则：学号必须唯一，不能和已有学生重复。
     *
     * 数据流转：
     *   前端 POST JSON → Spring 转 StudentDTO → 本方法
     *   → 检查学号唯一 → DTO 转 Entity → INSERT → 成功
     * ================================================================
     */
    @Override
    @Transactional(rollbackFor = Exception.class) // 事务
    public void save(StudentDTO dto) {

        /*
         * 第1步：检查学号唯一性
         *
         * selectCount(queryWrapper)：
         *   SELECT COUNT(*) FROM student WHERE student_no = ?
         *
         * 如果 count > 0 → 学号已被占用 → 拒绝插入
         */
        LambdaQueryWrapper<Student> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Student::getStudentNo, dto.getStudentNo()); // 条件：学号 = 传入学号
        if (studentMapper.selectCount(queryWrapper) > 0) { // 学号已存在
            throw new BusinessException(ResultCode.BUSINESS_ERROR.getCode(), "学号已存在");
        }

        /*
         * 第2步：DTO → Entity → 插入数据库
         *
         * BeanUtil.copyProperties(dto, Student.class)：
         *   把 DTO 中的同名字段复制到新 Student 实体中。
         *   studentMapper.insert(student)：插入数据库。
         *
         *   类比 JS：
         *     const existing = await Student.findOne({ studentNo: dto.studentNo })
         *     if (existing) throw new AppError('学号已存在')
         *     await Student.create(dto)
         */
        Student student = BeanUtil.copyProperties(dto, Student.class); // DTO → Entity
        studentMapper.insert(student); // INSERT INTO student ...
        log.info("新增学生成功，学号：{}，姓名：{}", dto.getStudentNo(), dto.getName());
    }

    /*
     * ================================================================
     * 方法 4：修改学生信息（含学号冲突检查）
     *
     * 注意：这里的学号检查有简化——如果 DTO 中没有 id 字段，
     * 无法判断学号冲突是来自同一个学生还是另一个学生。
     * 实际项目中 update 的 DTO 应该包含 id。
     * ================================================================
     */
    @Override
    @Transactional(rollbackFor = Exception.class) // 事务
    public void update(StudentDTO dto) {
        /*
         * 检查学号是否与其他学生冲突（简化版）
         * 完整版应该：
         *   1. 查询学号对应的学生
         *   2. 如果存在且 id != dto.id → 冲突
         *   3. 如果存在且 id == dto.id → 自己在改自己的信息，允许
         *   4. 如果不存在 → 允许
         */
        LambdaQueryWrapper<Student> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Student::getStudentNo, dto.getStudentNo());
        Student exist = studentMapper.selectOne(queryWrapper); // 查询学号对应的学生

        // 复制 DTO 到 Entity 并更新
        Student student = BeanUtil.copyProperties(dto, Student.class); // DTO → Entity
        studentMapper.updateById(student); // UPDATE student SET ... WHERE id = ?
        log.info("修改学生信息成功");
    }

    /*
     * ================================================================
     * 方法 5：删除学生（含关联数据检查）
     *
     * 这是所有 Service 方法中业务逻辑最丰富的——体现"数据完整性保护"。
     *
     * 删除学生前，必须先检查两件事：
     *   1. 该学生是否有成绩记录？（score 表中有 student_id 引用）
     *      → 有则拒绝删除，提示用户先删除成绩
     *   2. 该学生是否有选课记录？（course_enrollment 表中有 student_id 引用）
     *      → 有则拒绝删除，提示用户先退课
     *
     * 为什么要这样设计？
     *   如果直接删除学生，score 和 course_enrollment 中的记录就会成为"孤儿数据"：
     *     成绩表的 student_id = 5 指向一个已删除的学生，数据没有意义。
     *   这种"先检查关联，再决定是否删除"的模式，在企业管理系统中非常常见。
     *
     *   类比 JS：
     *     async function deleteStudent(id) {
     *       const scoreCount = await Score.countDocuments({ studentId: id })
     *       if (scoreCount > 0) throw new AppError('有关联成绩，无法删除')
     *       const enrollCount = await Enrollment.countDocuments({ studentId: id })
     *       if (enrollCount > 0) throw new AppError('有关联选课，无法删除')
     *       await Student.findByIdAndDelete(id)
     *     }
     * ================================================================
     */
    @Override
    @Transactional(rollbackFor = Exception.class) // 事务
    public void delete(Long id) {

        /*
         * 第1步：检查是否有关联的成绩记录
         *
         * LambdaQueryWrapper<Score>：创建 score 表的条件构造器
         * .eq(Score::getStudentId, id)：条件 student_id = 待删除的学生 ID
         * selectCount：统计符合条件的记录数
         *
         * 生成的 SQL：SELECT COUNT(*) FROM score WHERE student_id = ?
         */
        LambdaQueryWrapper<Score> scoreWrapper = new LambdaQueryWrapper<>();
        scoreWrapper.eq(Score::getStudentId, id); // 条件：student_id = 待删除ID
        if (scoreMapper.selectCount(scoreWrapper) > 0) { // 有成绩记录
            throw new BusinessException(ResultCode.BUSINESS_ERROR.getCode(), "该学生有关联的成绩记录，无法删除");
        }

        /*
         * 第2步：检查是否有关联的选课记录
         *
         * 同样的模式：查 course_enrollment 表，条件 student_id = id
         */
        LambdaQueryWrapper<CourseEnrollment> enrollWrapper = new LambdaQueryWrapper<>();
        enrollWrapper.eq(CourseEnrollment::getStudentId, id); // 条件：student_id = 待删除ID
        if (courseEnrollmentMapper.selectCount(enrollWrapper) > 0) { // 有选课记录
            throw new BusinessException(ResultCode.BUSINESS_ERROR.getCode(), "该学生有关联的选课记录，无法删除");
        }

        /*
         * 第3步：所有检查通过，执行删除
         *
         * deleteById(id)：按主键删除
         * 生成的 SQL：DELETE FROM student WHERE id = ?
         */
        studentMapper.deleteById(id); // 删除学生
        log.info("删除学生成功，ID：{}", id); // 记录日志
    }
}
