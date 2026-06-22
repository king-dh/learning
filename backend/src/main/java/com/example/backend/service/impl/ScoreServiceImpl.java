/*
 * ================================================================
 * 文件位置:  src/main/java/com/example/backend/service/impl/ScoreServiceImpl.java
 * 架构层级:  Service 实现类（业务逻辑的真正执行者）
 * 对应接口:  src/main/java/com/example/backend/service/ScoreService.java
 * 被调用者:  src/main/java/com/example/backend/controller/ScoreController.java
 *
 * 调用链路（以"分页查询成绩"为例）:
 * 前端成绩管理页面 → axios GET /api/scores/page?pageNum=1
 *                 → ScoreController.page()
 *                 → ScoreService.pageQuery()     ← 接口
 *                 → ScoreServiceImpl.pageQuery()  ← 本文件
 *                 → 查 score 表 → 联查 student 表获学生姓名
 *                               → 联查 course 表获课程名称
 *                 → 返回分页 JSON
 *
 * 成绩 = 学生 + 课程 + 分数值，是学生和课程之间"多对多"关系的属性数据。
 * 一张成绩表关联三张表：student、course、score 本身。
 *
 * JS 类比（Express + Mongoose）:
 *   const ScoreService = {
 *     pageQuery: async (dto) => {
 *       return Score.find(query).populate('studentId', 'name').populate('courseId', 'name')
 *     },
 *     save: async (dto) => { await Score.create(dto) },
 *     update: async (dto) => { await Score.updateById(dto) },
 *     delete: async (id) => { await Score.deleteById(id) },
 *     getByStudentId: async (studentId) => { return Score.find({ studentId }).populate('...') }
 *   }
 * ================================================================
 */

// ==================== 1. 包声明 ====================
package com.example.backend.service.impl; // 声明在 service/impl 子包（impl = implementation，实现类）

// ==================== 2. 导入其他类（import） ====================

/*
 * Hutool 的 BeanUtil：对象属性拷贝工具。
 * 作用：把一个对象的同名字段值复制到另一个对象中。
 * 类比 JS 的 Object.assign(dest, src) 或展开运算符 { ...src }。
 *
 * 例如：
 *   Score score = { studentId: 1, courseId: 5, score: 85, semester: "2024-秋" }
 *   ScoreVO vo = new ScoreVO();
 *   BeanUtil.copyProperties(score, vo); // 把 score 的同名字段复制到 vo
 *   // vo 现在有：{ studentId: 1, courseId: 5, score: 85, semester: "2024-秋" }
 */
import cn.hutool.core.bean.BeanUtil; // Hutool 属性复制

/*
 * MyBatis-Plus 核心查询类：
 * - LambdaQueryWrapper：条件构造器，拼 WHERE 子句
 * - IPage：分页结果接口
 * - Page：分页对象（实现了 IPage）
 */
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper; // Lambda 条件构造器
import com.baomidou.mybatisplus.core.metadata.IPage;                       // 分页结果接口
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;         // 分页对象

// --- Entity（数据库表映射实体）---
// 每个 Entity 对应数据库中的一张表，字段对应表中的列
import com.example.backend.entity.Course; // 课程实体（course 表）
import com.example.backend.entity.Score;  // 成绩实体（score 表）
import com.example.backend.entity.Student; // 学生实体（student 表）

// --- Mapper（数据库操作层）---
import com.example.backend.mapper.CourseMapper;  // 课程 Mapper
import com.example.backend.mapper.ScoreMapper;   // 成绩 Mapper
import com.example.backend.mapper.StudentMapper; // 学生 Mapper

// --- Service 接口 & VO ---
import com.example.backend.service.ScoreService; // 本文件要实现的接口
import com.example.backend.vo.ScoreVO;           // 成绩视图对象（含学生名、课程名）

// --- Lombok ---
import lombok.RequiredArgsConstructor; // 为 final 字段生成构造器（用于依赖注入）
import lombok.extern.slf4j.Slf4j;     // 自动创建日志对象 log

// --- Spring 框架 ---
import org.springframework.stereotype.Service;                   // 标记为 Service Bean
import org.springframework.transaction.annotation.Transactional; // 声明式事务

// --- Java 标准库 ---
import java.util.List; // 列表接口

// ==================== 3. 类的声明和注解 ====================

/*
 * @Slf4j：Lombok 自动生成日志对象
 *   编译后等价于：
 *     private static final Logger log = LoggerFactory.getLogger(ScoreServiceImpl.class);
 *   然后代码中可以直接用 log.info()、log.error() 等方法记录日志。
 *   类比 JS 中全局的 console.log / console.error。
 */
@Slf4j

/*
 * @Service：告诉 Spring 这是一个 Service Bean，纳入 IoC 容器管理。
 *   Controller 通过 @RequiredArgsConstructor 注入时，Spring 在容器中找到这个 Bean 并注入。
 */
@Service

/*
 * @RequiredArgsConstructor：Lombok 为所有 final 字段生成构造函数。
 *   本类有 3 个 final 字段（scoreMapper, studentMapper, courseMapper），
 *   编译后自动生成包含这 3 个参数的构造函数。
 *   Spring 通过这个构造函数完成依赖注入。
 */
@RequiredArgsConstructor

/*
 * public class ScoreServiceImpl implements ScoreService
 *
 *   实现 ScoreService 接口，必须提供接口中定义的所有方法的具体代码。
 *   这是面向接口编程的体现：ScoreController 只依赖 ScoreService 接口，
 *   不依赖本实现类。如果将来要换一种实现（比如加缓存），只需改写本类。
 */
public class ScoreServiceImpl implements ScoreService { // 成绩服务实现类

    // ==================== 4. 字段声明 ====================

    /*
     * 三个 Mapper 字段，通过构造器注入：
     *
     * scoreMapper   → 操作 score 表（成绩主表）
     * studentMapper → 操作 student 表（用于查询学生姓名）
     * courseMapper  → 操作 course 表（用于查询课程名称）
     *
     * 为什么需要 3 个 Mapper？
     *   因为 ScoreVO 返回的数据不仅包含成绩自身，还包含学生姓名和课程名称。
     *   这两个附加信息需要从 student 表和 course 表中查询。
     *   这种方式叫"手动联表组装"。另一种方式是在 Mapper 的 SQL 中写 JOIN。
     */
    private final ScoreMapper scoreMapper;      // 成绩 Mapper
    private final StudentMapper studentMapper;  // 学生 Mapper（查学生姓名用）
    private final CourseMapper courseMapper;    // 课程 Mapper（查课程名称用）

    // ==================== 5. 方法实现 ====================

    /*
     * ================================================================
     * 方法 1：分页条件查询成绩列表
     *
     * 返回值 IPage<ScoreVO> 包含：
     *   - records：当前页的成绩列表
     *   - total：符合条件的总记录数
     *   - current：当前页码
     *   - size：每页大小
     *
     * 每个 ScoreVO 包含：
     *   - 成绩基本信息（分数、学期等）
     *   - studentName：学生姓名（通过 studentMapper 查询）
     *   - courseName：课程名称（通过 courseMapper 查询）
     * ================================================================
     */
    @Override
    public IPage<ScoreVO> pageQuery(com.example.backend.dto.ScoreQueryDTO dto) {

        /*
         * 第1步：创建分页对象
         *
         * new Page<>(dto.getPageNum(), dto.getPageSize());
         *
         *   Page 是 MyBatis-Plus 的分页对象，构造参数：
         *     第一个参数：当前页码（从 1 开始）
         *     第二个参数：每页显示条数
         *
         *   例如 Page<>(1, 10) 表示查第 1 页，每页 10 条。
         *   最终 SQL 会自动加上 LIMIT 0,10（或 LIMIT 10 OFFSET 0）。
         *
         *   类比 JS：
         *     const skip = (pageNum - 1) * pageSize
         *     const page = await Score.find(query).skip(skip).limit(pageSize)
         */
        Page<Score> page = new Page<>(dto.getPageNum(), dto.getPageSize()); // 创建分页对象

        /*
         * 第2步：构建查询条件
         *
         * LambdaQueryWrapper<Score> qw = new LambdaQueryWrapper<>();
         *
         *   创建一个成绩表的条件构造器。
         *   然后根据 DTO 中的参数动态添加条件：
         *     - 如果 studentId 不为空 → 添加 studentId = ? 条件
         *     - 如果 courseId 不为空 → 添加 courseId = ? 条件
         *     - 如果 semester 不为空 → 添加 semester = ? 条件
         *
         *   这是"动态查询"：不确定前端传了哪些条件，只对传了的部分加 WHERE。
         */
        LambdaQueryWrapper<Score> qw = new LambdaQueryWrapper<>(); // 创建条件构造器

        /*
         * if (dto.getStudentId() != null) qw.eq(Score::getStudentId, dto.getStudentId());
         *
         *   如果前端传了 studentId，就添加"student_id = ?"的查询条件。
         *   这是精确查询，不是模糊搜索。
         *
         *   Score::getStudentId 是方法引用，MyBatis-Plus 解析出字段名 "student_id"。
         *
         *   最终 SQL 增加：WHERE student_id = ?
         */
        if (dto.getStudentId() != null) { // 前端传了学生 ID
            qw.eq(Score::getStudentId, dto.getStudentId()); // 添加条件：student_id = ?
        }
        if (dto.getCourseId() != null) { // 前端传了课程 ID
            qw.eq(Score::getCourseId, dto.getCourseId()); // 添加条件：course_id = ?
        }
        if (dto.getSemester() != null && !dto.getSemester().isEmpty()) { // 前端传了学期
            qw.eq(Score::getSemester, dto.getSemester()); // 添加条件：semester = ?
        }

        /*
         * qw.orderByDesc(Score::getCreateTime);
         *
         *   添加排序条件：按创建时间倒序排列（最新的排最前面）。
         *
         *   orderByDesc = order by descending = 降序排列
         *   对应的 SQL：ORDER BY create_time DESC
         *
         *   如果想升序排列，用 orderByAsc。
         */
        qw.orderByDesc(Score::getCreateTime); // 按创建时间倒序

        /*
         * 第3步：执行分页查询
         *
         * scoreMapper.selectPage(page, qw);
         *
         *   selectPage 是 MyBatis-Plus 的通用分页查询方法。
         *   第一个参数：分页对象（指定第几页、每页多少条）
         *   第二个参数：条件构造器（WHERE 条件）
         *
         *   这个方法做了两件事：
         *     1. 自动执行 COUNT 查询获取总数（设置到 page.total）
         *     2. 执行分页查询获取当前页的记录（设置到 page.records）
         *
         *   最终生成的 SQL 两条：
         *     1. SELECT COUNT(*) FROM score WHERE student_id = ? AND course_id = ? AND semester = ?
         *     2. SELECT * FROM score WHERE ... ORDER BY create_time DESC LIMIT ?, ?
         *
         *   类比 JS：
         *     const total = await Score.countDocuments(query)
         *     const records = await Score.find(query).sort('-createTime').skip(skip).limit(limit)
         *     return { records, total }
         */
        IPage<Score> sp = scoreMapper.selectPage(page, qw); // 执行分页查询

        /*
         * 第4步：转换为 ScoreVO 列表
         *
         * sp.convert(score -> { ... })
         *
         *   convert() 是 Page 类的方法，用于把分页结果从一种类型转为另一种类型。
         *   这里的逻辑是把每行 Score 实体转成 ScoreVO 视图对象，同时补充学生名和课程名。
         *
         *   score -> { ... } 是 Java 的 Lambda 表达式（箭头函数）。
         *
         *   类比 JS：
         *     return sp.convert(score => {
         *       const vo = { ...score } // 复制基本属性
         *       if (score.studentId) {
         *         const s = await Student.findById(score.studentId)
         *         vo.studentName = s?.name
         *       }
         *       if (score.courseId) {
         *         const c = await Course.findById(score.courseId)
         *         vo.courseName = c?.name
         *       }
         *       return vo
         *     })
         */
        return sp.convert(score -> { // 遍历每条 Score 记录，转成 ScoreVO
            /*
             * BeanUtil.copyProperties(score, ScoreVO.class);
             *
             *   把 Score 实体中的同名字段复制到新创建的 ScoreVO 对象中。
             *   例如：score.id → vo.id, score.score → vo.score, score.semester → vo.semester
             *
             *   但是 Score 中没有 studentName 和 courseName，所以下面要手动补充。
             */
            ScoreVO vo = BeanUtil.copyProperties(score, ScoreVO.class); // 复制基础属性

            // 查询学生姓名
            if (score.getStudentId() != null) { // 如果成绩关联了学生
                /*
                 * studentMapper.selectById(score.getStudentId());
                 *
                 *   按主键 ID 查询学生。因为 score 表中存的是 student_id 外键，
                 *   用它去 student 表查即可。这是"手动联表"的开发方式。
                 *
                 *   缺点：如果成绩列表有 100 条，这里会执行 100 次额外查询（N+1 问题）。
                 *   优点：代码简单直观。
                 *
                 *   生产环境优化方案：
                 *     1. 预加载：先收集所有 studentId，批量查询后放入 Map
                 *     2. 或直接在 Mapper 中写 JOIN SQL，一次查出来
                 *     3. 或使用缓存
                 */
                Student s = studentMapper.selectById(score.getStudentId()); // 查询学生
                if (s != null) {
                    vo.setStudentName(s.getName()); // 设置学生姓名
                }
            }

            // 查询课程名称（逻辑同上）
            if (score.getCourseId() != null) { // 如果成绩关联了课程
                Course c = courseMapper.selectById(score.getCourseId()); // 查询课程
                if (c != null) {
                    vo.setCourseName(c.getName()); // 设置课程名称
                }
            }

            return vo; // 返回组装好的 ScoreVO
        });
    }

    /*
     * ================================================================
     * 方法 2：新增成绩
     *
     * 数据流转：
     *   前端 POST JSON { studentId: 1, courseId: 5, score: 85, semester: "2024-秋" }
     *        → Spring 转成 ScoreDTO
     *        → 本方法接收 dto
     *        → BeanUtil.copyProperties 复制到 Score 实体
     *        → Mapper 插入数据库
     * ================================================================
     */
    @Override
    @Transactional(rollbackFor = Exception.class) // 事务：插入失败自动回滚
    public void save(com.example.backend.dto.ScoreDTO dto) {

        /*
         * BeanUtil.copyProperties(dto, Score.class);
         *
         *   把 ScoreDTO 中的同名字段复制到新创建的 Score 实体对象中。
         *   DTO 和 Entity 通常字段名相同，所以直接拷贝即可。
         *
         *   ScoreDTO（前端来的）  →  Score（数据库实体）
         *     studentId           →  studentId
         *     courseId            →  courseId
         *     score               →  score
         *     semester            →  semester
         *
         *   为什么需要这一步转换？
         *     不能直接把 DTO 插入数据库，因为 DTO 和 Entity 虽然字段相似但职责不同：
         *     - DTO：为前端服务，可能包含额外的校验注解
         *     - Entity：为数据库服务，与数据库表结构严格对应
         */
        Score score = BeanUtil.copyProperties(dto, Score.class); // DTO → Entity

        /*
         * scoreMapper.insert(score);
         *
         *   MyBatis-Plus 通用插入方法。
         *   如果实体类配置了 @TableId(type = IdType.AUTO)，会自动生成主键。
         *   如果配置了自动填充（如 create_time），也会在当前操作中填值。
         *
         *   最终 SQL（约等于）：
         *     INSERT INTO score (student_id, course_id, score, semester, ...) VALUES (?, ?, ?, ?, ...)
         *
         *   类比 JS：
         *     await Score.create(dto)
         */
        scoreMapper.insert(score); // 插入数据库
    }

    /*
     * ================================================================
     * 方法 3：修改成绩
     *
     *   dto 中必须包含 id 字段，用于定位要修改的记录。
     *   没有额外的业务检查（如分数范围），直接更新。
     * ================================================================
     */
    @Override
    @Transactional(rollbackFor = Exception.class) // 事务
    public void update(com.example.backend.dto.ScoreDTO dto) {
        /*
         * BeanUtil.copyProperties(dto, Score.class)：DTO → Entity
         * scoreMapper.updateById(score)：MyBatis-Plus 通用更新方法
         *
         *   updateById 按实体中的主键字段（@TableId 标记的字段）定位记录，
         *   然后更新其他非空字段。
         *
         *   最终 SQL：
         *     UPDATE score SET student_id=?, course_id=?, score=?, semester=?, ... WHERE id=?
         *
         *   类比 JS：
         *     await Score.updateById(dto.id, dto)
         */
        Score score = BeanUtil.copyProperties(dto, Score.class); // DTO → Entity
        scoreMapper.updateById(score); // 按主键更新
    }

    /*
     * ================================================================
     * 方法 4：删除成绩
     *
     *   deleteById(id)：按主键删除。
     *   没有做关联检查（因为成绩是独立记录，不会导致孤儿数据问题）。
     * ================================================================
     */
    @Override
    @Transactional(rollbackFor = Exception.class) // 事务
    public void delete(Long id) {

        /*
         * scoreMapper.deleteById(id);
         *
         *   最终 SQL：DELETE FROM score WHERE id = ?
         *
         *   类比 JS：
         *     await Score.deleteById(id)
         */
        scoreMapper.deleteById(id); // 按主键删除
    }

    /*
     * ================================================================
     * 方法 5：根据学生 ID 查询该学生的所有成绩
     *
     * 这个方法直接调用 Mapper 中的自定义方法 selectByStudentId，
     * 该方法的 SQL 已写了联表查询（JOIN student、JOIN course），
     * 所以返回的 List<ScoreVO> 已经包含了学生姓名和课程名称。
     *
     * 注意：这里返回的是 List 而非分页，因为一个学生的成绩数量通常不大。
     * ================================================================
     */
    @Override
    public List<ScoreVO> getByStudentId(Long studentId) {

        /*
         * scoreMapper.selectByStudentId(studentId);
         *
         *   这是 Mapper 中的自定义方法，不是 MyBatis-Plus 的通用方法。
         *   SQL 可能在 ScoreMapper.java 的注解中，或在 ScoreMapper.xml 中。
         *
         *   这个方法内部可能写了类似这样的 SQL：
         *     SELECT s.*, stu.name as student_name, c.name as course_name
         *     FROM score s
         *     LEFT JOIN student stu ON s.student_id = stu.id
         *     LEFT JOIN course c ON s.course_id = c.id
         *     WHERE s.student_id = ?
         *
         *   由于 SQL 中已经 JOIN 了 student 和 course 表，
         *   不需要在 Java 代码中额外查询，直接返回即可。
         *
         *   类比 JS：
         *     return await Score.find({ studentId })
         *       .populate('studentId', 'name')
         *       .populate('courseId', 'name')
         */
        return scoreMapper.selectByStudentId(studentId); // 返回含联表数据的列表
    }
}
