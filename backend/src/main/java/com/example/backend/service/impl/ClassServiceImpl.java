/*
 * ================================================================
 * 文件位置:  src/main/java/com/example/backend/service/impl/ClassServiceImpl.java
 * 架构层级:  Service 实现类（班级业务的实际执行者）
 * 对应接口:  src/main/java/com/example/backend/service/ClassService.java
 * 被调用者:  src/main/java/com/example/backend/controller/ClassController.java
 *
 * 调用链路（以"分页查询班级"为例）:
 * 前端班级管理页面 → axios GET /api/classes/page?pageNum=1
 *                 → ClassController.page()
 *                 → ClassService.pageQuery()     ← 接口
 *                 → ClassServiceImpl.pageQuery()  ← 本文件
 *                 → 查 class_info 表 → 联查 teacher 表获班主任姓名
 *                                   → 统计 student 表获学生人数
 *                 → 返回分页 JSON
 *
 * 班级服务涉及 3 张表：class_info（主表）、teacher（班主任）、student（学生统计）。
 * 这是典型的"聚合"查询：班级是聚合根，包含班主任姓名和学生人数两个附加信息。
 *
 * MyBatis-Plus 手动联表模式 vs JOIN 模式：
 *   本文件采用"手动联表"：先查主表，再逐行查关联表。
 *   优点：代码简单直观。缺点：存在 N+1 查询问题（100 个班级 = 1 + 100 + 100 = 201 次查询）。
 *   生产环境优化建议：用批量查询或 JOIN SQL。
 *
 * JS 类比（Express + Mongoose）:
 *   const ClassService = {
 *     pageQuery: async (dto) => {
 *       const page = await ClassInfo.find(query).skip().limit()
 *       // 手动 populate：逐个查 teacher 和 count students
 *       for (const cls of page.records) {
 *         cls.headTeacherName = (await Teacher.findById(cls.headTeacherId))?.name
 *         cls.studentCount = await Student.countDocuments({ classId: cls.id })
 *       }
 *       return page
 *     },
 *     getStudentsByClassId: async (classId) => Student.find({ classId })
 *   }
 * ================================================================
 */

// ==================== 1. 包声明 ====================
package com.example.backend.service.impl; // 声明在 service/impl 子包

// ==================== 2. 导入其他类（import） ====================

// Hutool BeanUtil：对象属性拷贝，类似 JS 的 Object.assign
import cn.hutool.core.bean.BeanUtil;

/*
 * MyBatis-Plus 分页三件套：
 *   LambdaQueryWrapper：WHERE 条件构造器（拼 SQL 的 WHERE 子句）
 *   IPage：分页结果接口（定义 records、total、current 等字段）
 *   Page：分页对象（实现了 IPage，设置 pageNum/pageSize）
 */
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

// 业务异常和状态码
import com.example.backend.common.BusinessException;
import com.example.backend.common.ResultCode;

// DTO：接收前端请求数据
import com.example.backend.dto.ClassDTO;      // 新增/修改班级 { className, grade, headTeacherId, ... }
import com.example.backend.dto.ClassQueryDTO; // 分页查询 { pageNum, pageSize, className, grade }

// Entity：数据库表映射实体
import com.example.backend.entity.ClassInfo; // 班级实体 → class_info 表
import com.example.backend.entity.Student;   // 学生实体 → student 表
import com.example.backend.entity.Teacher;   // 教师实体 → teacher 表

// Mapper：数据库操作接口（MyBatis-Plus 动态代理生成实现）
import com.example.backend.mapper.ClassInfoMapper; // 操作 class_info 表
import com.example.backend.mapper.StudentMapper;   // 操作 student 表（用于统计和查询）
import com.example.backend.mapper.TeacherMapper;   // 操作 teacher 表（查询班主任姓名）

// Service 接口 & VO
import com.example.backend.service.ClassService; // 本类要实现的接口
import com.example.backend.vo.ClassVO;           // 班级视图对象（含班主任姓名、学生人数）
import com.example.backend.vo.StudentVO;          // 学生视图对象（查询班级学生时返回）

// Lombok
import lombok.RequiredArgsConstructor; // 为 final 字段生成构造函数
import lombok.extern.slf4j.Slf4j;     // 日志注解

// Spring
import org.springframework.stereotype.Service;                   // Service Bean
import org.springframework.transaction.annotation.Transactional; // 事务

import java.util.List; // 列表
import java.util.stream.Collectors; // Stream 收集器（用于 List<Student> → List<StudentVO> 转换）

// ==================== 3. 类的声明和注解 ====================

@Slf4j
@Service
@RequiredArgsConstructor
/*
 * public class ClassServiceImpl implements ClassService
 *
 *   实现 ClassService 接口中定义的 6 个方法。
 *   这是 Service 实现类，包含了所有具体的业务逻辑。
 */
public class ClassServiceImpl implements ClassService { // 班级服务实现类

    // ==================== 4. 字段声明 ====================

    /*
     * 三个 Mapper 依赖：
     *
     * classInfoMapper → 主操作表：class_info
     * teacherMapper   → 辅助表：teacher（查班主任姓名）
     * studentMapper   → 辅助表：student（统计人数、查学生列表）
     *
     * 为什么需要 teacherMapper 和 studentMapper？
     *   因为 ClassVO 包含了班主任姓名和学生人数，这些数据来自其他表。
     *   这是 Service 层常见的"数据组装"操作。
     */
    private final ClassInfoMapper classInfoMapper; // 班级表 Mapper
    private final TeacherMapper teacherMapper;     // 教师表 Mapper（查询班主任姓名）
    private final StudentMapper studentMapper;     // 学生表 Mapper（查询学生人数和列表）

    // ==================== 5. 方法实现 ====================

    /*
     * ================================================================
     * 方法 1：分页条件查询班级列表（含班主任姓名、学生人数）
     *
     * 查询条件：className（模糊）、grade（模糊）
     * 排序：按创建时间倒序
     * 附加数据：headTeacherName（班主任姓名）、studentCount（学生人数）
     * ================================================================
     */
    @Override
    public IPage<ClassVO> pageQuery(ClassQueryDTO dto) {

        // --- 第1步：创建分页对象 ---
        // Page<>(当前页, 每页大小)，页码从 1 开始
        Page<ClassInfo> page = new Page<>(dto.getPageNum(), dto.getPageSize());

        // --- 第2步：构建动态查询条件 ---
        LambdaQueryWrapper<ClassInfo> queryWrapper = new LambdaQueryWrapper<>();

        // 班级名称模糊搜索：LIKE '%className%'
        if (dto.getClassName() != null && !dto.getClassName().isEmpty()) {
            queryWrapper.like(ClassInfo::getClassName, dto.getClassName());
        }

        // 年级模糊搜索：LIKE '%grade%'
        if (dto.getGrade() != null && !dto.getGrade().isEmpty()) {
            queryWrapper.like(ClassInfo::getGrade, dto.getGrade());
        }

        // 按创建时间倒序排列（新建班级排前面）
        queryWrapper.orderByDesc(ClassInfo::getCreateTime);

        // --- 第3步：执行分页查询 ---
        // selectPage 内部执行两次查询：COUNT（拿总数）+ SELECT（拿当前页）
        IPage<ClassInfo> classPage = classInfoMapper.selectPage(page, queryWrapper);

        // --- 第4步：转换为 ClassVO（补充班主任姓名和学生人数）---
        return classPage.convert(classInfo -> { // Lambda 表达式：遍历每条班级记录
            // 复制基础属性（id、className、grade、room 等）
            ClassVO vo = BeanUtil.copyProperties(classInfo, ClassVO.class);

            // 查询班主任姓名（手动联表）
            if (classInfo.getHeadTeacherId() != null) {
                Teacher teacher = teacherMapper.selectById(classInfo.getHeadTeacherId());
                if (teacher != null) {
                    vo.setHeadTeacherName(teacher.getName()); // 设置班主任姓名
                }
            }

            /*
             * 统计该班级的学生人数
             *
             * LambdaQueryWrapper<Student> countWrapper = ...;
             * countWrapper.eq(Student::getClassId, classInfo.getId());
             *   条件：student 表的 class_id = 当前班级的 id
             *
             * studentMapper.selectCount(countWrapper)：
             *   执行 SELECT COUNT(*) FROM student WHERE class_id = ?
             *
             * Math.toIntExact(count)：
             *   count 是 Long 类型，studentCount 是 Integer 类型。
             *   Math.toIntExact() 安全转换（超出 Integer 范围会抛异常）。
             */
            LambdaQueryWrapper<Student> countWrapper = new LambdaQueryWrapper<>();
            countWrapper.eq(Student::getClassId, classInfo.getId()); // WHERE class_id = ?
            vo.setStudentCount(Math.toIntExact(studentMapper.selectCount(countWrapper))); // 统计并设置

            return vo; // 返回组装好的 ClassVO
        });
    }

    /*
     * ================================================================
     * 方法 2：根据 ID 查询班级详情
     *
     * 和 pageQuery 的单行逻辑一样：查 classInfo + 查 teacher + 统计 student
     * ================================================================
     */
    @Override
    public ClassVO getById(Long id) {
        // 查询班级基本信息
        ClassInfo classInfo = classInfoMapper.selectById(id);
        if (classInfo == null) { // 班级不存在
            throw new BusinessException(ResultCode.BUSINESS_ERROR.getCode(), "班级不存在");
        }

        // 属性复制
        ClassVO vo = BeanUtil.copyProperties(classInfo, ClassVO.class);

        // 查询班主任姓名
        if (classInfo.getHeadTeacherId() != null) {
            Teacher teacher = teacherMapper.selectById(classInfo.getHeadTeacherId());
            if (teacher != null) {
                vo.setHeadTeacherName(teacher.getName());
            }
        }

        // 统计学生人数
        LambdaQueryWrapper<Student> countWrapper = new LambdaQueryWrapper<>();
        countWrapper.eq(Student::getClassId, id); // WHERE class_id = ?
        vo.setStudentCount(Math.toIntExact(studentMapper.selectCount(countWrapper))); // 设置人数

        return vo;
    }

    /*
     * ================================================================
     * 方法 3：新增班级
     *
     * DTO → Entity → INSERT
     * ================================================================
     */
    @Override
    @Transactional(rollbackFor = Exception.class) // 事务
    public void save(ClassDTO dto) {
        ClassInfo classInfo = BeanUtil.copyProperties(dto, ClassInfo.class); // DTO → Entity
        classInfoMapper.insert(classInfo); // INSERT INTO class_info ...
        log.info("新增班级成功，名称：{}", dto.getClassName());
    }

    /*
     * ================================================================
     * 方法 4：修改班级信息
     * ================================================================
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(ClassDTO dto) {
        ClassInfo classInfo = BeanUtil.copyProperties(dto, ClassInfo.class); // DTO → Entity
        classInfoMapper.updateById(classInfo); // UPDATE class_info SET ... WHERE id = ?
        log.info("修改班级信息成功");
    }

    /*
     * ================================================================
     * 方法 5：删除班级
     *
     * 注意：没有检查是否有学生在这个班级。如果直接删除，学生的 class_id
     * 会成为悬空引用。生产环境应该加检查或级联处理。
     * ================================================================
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        classInfoMapper.deleteById(id); // DELETE FROM class_info WHERE id = ?
        log.info("删除班级成功，ID：{}", id);
    }

    /*
     * ================================================================
     * 方法 6：查询班级下的所有学生列表
     *
     * 返回 List<StudentVO>（不是分页），因为一个班级的学生通常不会太多。
     *
     * 使用 Stream 将 List<Student> 转换为 List<StudentVO>：
     *
     * students.stream()               → 创建流（数据管道）
     *   .map(s -> ...)                → 把每个 Student 映射为 StudentVO
     *     BeanUtil.copyProperties(s, StudentVO.class) → 属性复制
     *   .collect(Collectors.toList())  → 收集回 List
     *
     * 类比 JS：
     *   students.map(s => ({ ...s }))
     *
     * 这里没有联查班级名称，因为调用者已经知道是哪个班级了。
     * ================================================================
     */
    @Override
    public List<StudentVO> getStudentsByClassId(Long classId) {
        // 构建查询条件：WHERE class_id = classId
        LambdaQueryWrapper<Student> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Student::getClassId, classId); // 条件：班级 ID

        // 查询学生列表
        List<Student> students = studentMapper.selectList(queryWrapper);

        /*
         * Stream 操作：将 List<Student> 转为 List<StudentVO>
         *
         * students.stream()：把 List 转成 Java 的 Stream（流）。
         *   Stream 是 Java 8 引入的函数式数据处理工具，类似 JS 的数组方法链。
         *
         * .map(s -> BeanUtil.copyProperties(s, StudentVO.class))：
         *   对每个 Student 对象 s，复制属性到新 StudentVO 中。
         *   map 是"映射"操作，输入一个类型，输出另一个类型。
         *
         * .collect(Collectors.toList())：
         *   把 Stream 中的元素收集到一个 List 中，结束流操作。
         *
         * 对比 JS：
         *   students.map(s => ({ ...s }))
         */
        return students.stream() // 创建流
                .map(s -> BeanUtil.copyProperties(s, StudentVO.class)) // 每个 Student → StudentVO
                .collect(Collectors.toList()); // 收集为 List
    }
}
