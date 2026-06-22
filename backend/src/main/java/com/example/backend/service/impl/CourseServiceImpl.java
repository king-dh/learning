/*
 * ================================================================
 * 文件位置:  src/main/java/com/example/backend/service/impl/CourseServiceImpl.java
 * 架构层级:  Service 实现类（课程业务的实际执行者）
 * 对应接口:  src/main/java/com/example/backend/service/CourseService.java
 * 被调用者:  src/main/java/com/example/backend/controller/CourseController.java
 *
 * 调用链路（以"分页查询课程"为例）:
 * 前端课程管理页面 → axios GET /api/courses/page?pageNum=1
 *                 → CourseController.page()
 *                 → CourseService.pageQuery()     ← 接口
 *                 → CourseServiceImpl.pageQuery()  ← 本文件
 *                 → 查 course 表 → 联查 teacher 表获教师姓名
 *                 → 返回分页 JSON
 *
 * 课程服务涉及 2 张表：course（主表）+ teacher（授课教师信息）。
 * 每个课程必须关联一位教师（teacher_id 外键）。
 *
 * 本类完整演示了 MyBatis-Plus 的标准 CRUD 模式：
 *   - 分页查询：Page + LambdaQueryWrapper + selectPage + convert
 *   - 按 ID 查：selectById + 手动联表组装
 *   - 新增：BeanUtil 拷贝 + insert
 *   - 修改：BeanUtil 拷贝 + updateById
 *   - 删除：deleteById
 *
 * JS 类比（Express + Mongoose）:
 *   const CourseService = {
 *     pageQuery: async (dto) => {
 *       const page = await Course.find(query).skip().limit()
 *       page.records = page.records.map(c => ({ ...c, teacherName: '...' }))
 *       return page
 *     },
 *     getByTeacherId: async (teacherId) => Course.find({ teacherId })
 *   }
 * ================================================================
 */

// ==================== 1. 包声明 ====================
package com.example.backend.service.impl; // 声明在 service/impl 子包

// ==================== 2. 导入其他类（import） ====================

// Hutool BeanUtil：在 DTO、Entity、VO 之间复制同名字段
// 类比 JS 的 { ...obj } 或 Object.assign
import cn.hutool.core.bean.BeanUtil;

// MyBatis-Plus 分页和条件构造
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper; // SQL WHERE 构建器
import com.baomidou.mybatisplus.core.metadata.IPage;                       // 分页结果
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;         // 分页对象

// 业务异常和状态码
import com.example.backend.common.BusinessException; // 业务异常
import com.example.backend.common.ResultCode;         // 状态码

// DTO
import com.example.backend.dto.CourseDTO;      // { courseNo, name, credit, teacherId, semester }
import com.example.backend.dto.CourseQueryDTO; // { pageNum, pageSize, name, teacherId, semester }

// Entity
import com.example.backend.entity.Course;  // 课程实体 → course 表
import com.example.backend.entity.Teacher; // 教师实体 → teacher 表（用于查教师姓名）

// Mapper
import com.example.backend.mapper.CourseMapper;  // 操作 course 表
import com.example.backend.mapper.TeacherMapper; // 操作 teacher 表（查授课教师姓名）

// Service 接口 & VO
import com.example.backend.service.CourseService; // 本类要实现的接口
import com.example.backend.vo.CourseVO;           // 课程视图对象（含 teacherName）

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
 * @Slf4j：自动创建 log 对象，可在代码中用 log.info() 记录日志。
 *   类比 JS：const log = console
 */
@Slf4j

/*
 * @Service：告诉 Spring 这是一个 Service Bean。
 *   启动时被扫描并注册到 IoC 容器。Controller 通过接口类型注入时，
 *   Spring 在容器中找到这个唯一的 CourseService 实现类。
 *
 *   @Service 和 @Component 功能完全相同，但语义不同：
 *     @Service → "这是业务逻辑层的 Bean"
 *     @Component → "这是一个通用 Bean"
 *   语义标注有助于代码可读性和未来的 AOP 切面配置。
 */
@Service

/*
 * @RequiredArgsConstructor：为所有 final 字段生成构造函数。
 *   本类有 2 个 final 字段 → 生成包含 2 个参数的构造函数。
 *   Spring 自动查找参数对应的 Bean 并注入。
 *
 *   这就是"构造器注入"（Constructor Injection），Spring 官方推荐的方式。
 *   类比 JS：
 *     class CourseServiceImpl {
 *       #courseMapper; #teacherMapper;
 *       constructor(cm, tm) { this.#courseMapper = cm; this.#teacherMapper = tm; }
 *     }
 */
@RequiredArgsConstructor

/*
 * public class CourseServiceImpl implements CourseService
 *
 *   implements 关键字声明本类实现了 CourseService 接口。
 *   编译器会检查是否实现了接口中的所有方法。
 */
public class CourseServiceImpl implements CourseService { // 课程服务实现类

    // ==================== 4. 字段声明 ====================

    /*
     * 两个 Mapper 依赖，由 Spring 通过构造器注入：
     *
     * courseMapper  → 主表 course 的增删改查
     * teacherMapper → 辅助表 teacher，用于查询授课教师姓名
     *
     * 为什么需要 teacherMapper？
     *   因为 CourseVO 包含了 teacherName 字段（教师姓名），
     *   而 course 表只存了 teacher_id（外键），需要通过 teacherMapper 查询姓名。
     *
     *   "ID 到名称的转换"是 Service 层的常见职责。
     *   数据库存的是"关系"（外键 ID），前端需要的是"展示"（名称），
     *   Service 层负责完成这个转换。
     */
    private final CourseMapper courseMapper;   // 课程 Mapper
    private final TeacherMapper teacherMapper; // 教师 Mapper（查教师姓名用）

    // ==================== 5. 方法实现 ====================

    /*
     * ================================================================
     * 方法 1：分页条件查询课程列表（含教师姓名）
     *
     * 查询条件：name（模糊）、teacherId（精确）、semester（精确）
     * 排序：按创建时间倒序
     * 附加数据：teacherName（授课教师姓名）
     * ================================================================
     */
    @Override
    public IPage<CourseVO> pageQuery(CourseQueryDTO dto) {

        // --- 第1步：创建分页对象 ---
        // Page<>(当前页, 每页大小)
        Page<Course> page = new Page<>(dto.getPageNum(), dto.getPageSize());

        // --- 第2步：动态构建查询条件 ---
        LambdaQueryWrapper<Course> queryWrapper = new LambdaQueryWrapper<>();

        // 课程名称模糊搜索
        if (dto.getName() != null && !dto.getName().isEmpty()) {
            queryWrapper.like(Course::getName, dto.getName()); // LIKE '%name%'
        }

        // 按教师 ID 精确筛选
        if (dto.getTeacherId() != null) {
            queryWrapper.eq(Course::getTeacherId, dto.getTeacherId()); // teacher_id = ?
        }

        // 按学期精确筛选
        if (dto.getSemester() != null && !dto.getSemester().isEmpty()) {
            queryWrapper.eq(Course::getSemester, dto.getSemester()); // semester = ?
        }

        // 按创建时间倒序
        queryWrapper.orderByDesc(Course::getCreateTime);

        // --- 第3步：执行分页查询 ---
        // selectPage 内部执行：COUNT + SELECT LIMIT
        IPage<Course> coursePage = courseMapper.selectPage(page, queryWrapper);

        // --- 第4步：转换为 CourseVO（补充教师姓名）---
        return coursePage.convert(course -> { // 遍历每条课程记录
            CourseVO vo = BeanUtil.copyProperties(course, CourseVO.class); // 复制基础属性

            // 查询授课教师姓名（手动联表）
            if (course.getTeacherId() != null) { // 如果课程关联了教师
                Teacher teacher = teacherMapper.selectById(course.getTeacherId()); // 按主键查教师
                if (teacher != null) { // 教师存在
                    vo.setTeacherName(teacher.getName()); // 设置教师姓名
                }
            }

            return vo; // 返回组装好的 CourseVO
        });
    }

    /*
     * ================================================================
     * 方法 2：根据 ID 查询课程详情（含教师姓名）
     *
     * 查不到则抛异常，而不是返回 null。
     * ================================================================
     */
    @Override
    public CourseVO getById(Long id) {
        // 查询课程基本信息
        Course course = courseMapper.selectById(id); // 按主键查询
        if (course == null) { // 不存在
            throw new BusinessException(ResultCode.BUSINESS_ERROR.getCode(), "课程不存在");
        }

        // 复制属性到 VO
        CourseVO vo = BeanUtil.copyProperties(course, CourseVO.class);

        // 查询授课教师姓名
        if (course.getTeacherId() != null) { // 有关联教师
            Teacher teacher = teacherMapper.selectById(course.getTeacherId()); // 查教师表
            if (teacher != null) {
                vo.setTeacherName(teacher.getName()); // 设置教师姓名
            }
        }

        return vo;
    }

    /*
     * ================================================================
     * 方法 3：新增课程
     * ================================================================
     */
    @Override
    @Transactional(rollbackFor = Exception.class) // 事务：插入失败自动回滚
    public void save(CourseDTO dto) {
        // DTO → Entity：将前端数据转为数据库实体
        Course course = BeanUtil.copyProperties(dto, Course.class);
        courseMapper.insert(course); // INSERT INTO course ...
        log.info("新增课程成功，编号：{}，名称：{}", dto.getCourseNo(), dto.getName());
    }

    /*
     * ================================================================
     * 方法 4：修改课程信息
     * ================================================================
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(CourseDTO dto) {
        Course course = BeanUtil.copyProperties(dto, Course.class); // DTO → Entity
        courseMapper.updateById(course); // UPDATE course SET ... WHERE id = ?
        log.info("修改课程信息成功");
    }

    /*
     * ================================================================
     * 方法 5：删除课程
     *
     * 注意：没有检查是否有学生选了这门课（course_enrollment 表）。
     * 如果有选课记录引用被删除的课程，会导致数据不一致。
     * 生产环境应该先检查或级联处理。
     * ================================================================
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        courseMapper.deleteById(id); // DELETE FROM course WHERE id = ?
        log.info("删除课程成功，ID：{}", id);
    }

    /*
     * ================================================================
     * 方法 6：根据教师 ID 查询该教师的所有课程
     *
     * 调用 Mapper 中自定义的 selectByTeacherId 方法。
     * 这在 CourseMapper 中是自定义方法，可能注解 SQL 或 XML SQL。
     *
     * 然后使用 Stream 将 List<Course> 转为 List<CourseVO>，
     * 并为每个 CourseVO 补充教师姓名。
     *
     * Stream 操作详解：
     *   courses.stream()               → 开启流
     *   .map(course -> { ... })        → 逐条转换（类比 JS 的 .map()）
     *   .collect(Collectors.toList())  → 收集为 List（类比 JS 的 map 自动返回数组）
     *
     * 类比 JS：
     *   const vos = courses.map(course => ({
     *     ...course,
     *     teacherName: teacher?.name
     *   }))
     * ================================================================
     */
    @Override
    public List<CourseVO> getByTeacherId(Long teacherId) {
        // 调用 Mapper 中自定义方法：SELECT * FROM course WHERE teacher_id = ?
        List<Course> courses = courseMapper.selectByTeacherId(teacherId);

        // 转换为 CourseVO 列表，补充教师姓名
        return courses.stream() // 创建流：Stream<Course>
                .map(course -> { // map 操作：Course → CourseVO
                    CourseVO vo = BeanUtil.copyProperties(course, CourseVO.class); // 复制属性

                    // 查询教师姓名
                    Teacher teacher = teacherMapper.selectById(teacherId); // 按主键查教师
                    if (teacher != null) {
                        vo.setTeacherName(teacher.getName()); // 设置教师姓名
                    }

                    return vo; // 返回转换后的 VO
                })
                .collect(Collectors.toList()); // 收集：Stream<CourseVO> → List<CourseVO>
    }
}
