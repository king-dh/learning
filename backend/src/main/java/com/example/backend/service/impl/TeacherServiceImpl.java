/*
 * ================================================================
 * 文件位置:  src/main/java/com/example/backend/service/impl/TeacherServiceImpl.java
 * 架构层级:  Service 实现类（业务逻辑的真正执行者）
 * 对应接口:  src/main/java/com/example/backend/service/TeacherService.java
 * 被调用者:  src/main/java/com/example/backend/controller/TeacherController.java
 *
 * 调用链路（以"分页查询教师"为例）:
 * 前端教师管理页面 → axios GET /api/teachers/page?pageNum=1&name=李
 *                 → TeacherController.page()
 *                 → TeacherService.pageQuery()     ← 接口
 *                 → TeacherServiceImpl.pageQuery()  ← 本文件
 *                 → MyBatis-Plus 查 teacher 表
 *                 → 返回分页 JSON
 *
 * 教师服务的职责：
 *   对 teacher 表进行标准的增删改查 + 分页查询。
 *   是最标准的 CRUD Service，没有跨表查询（TeacherVO 只是 Teacher 实体的映射）。
 *
 * JS 类比（Express + Mongoose）:
 *   const TeacherService = {
 *     pageQuery: async (dto) => Teacher.find(query).skip(skip).limit(limit),
 *     getById: async (id) => Teacher.findById(id),
 *     save: async (dto) => Teacher.create(dto),
 *     update: async (dto) => Teacher.updateById(dto.id, dto),
 *     delete: async (id) => Teacher.deleteById(id)
 *   }
 * ================================================================
 */

// ==================== 1. 包声明 ====================
package com.example.backend.service.impl; // 声明在 service/impl 子包下

// ==================== 2. 导入其他类（import） ====================

/*
 * Hutool 的 BeanUtil：对象属性拷贝。
 * 用于在 DTO 和 Entity 之间进行字段复制，避免手写 setter。
 */
import cn.hutool.core.bean.BeanUtil; // Hutool 属性复制工具

/*
 * MyBatis-Plus 分页相关的三个核心类：
 *
 * LambdaQueryWrapper：SQL WHERE 条件构建器，通过方法引用（Teacher::getName）
 *                     指定查询条件，编译时检查字段名是否正确。
 *
 * IPage：分页结果接口，定义分页数据的通用结构（records, total, current, size）。
 *
 * Page：分页对象，实现了 IPage 接口，在查询前指定页码和每页大小，
 *       查询后自动填充 total 和 records。
 */
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper; // Lambda 条件构造器
import com.baomidou.mybatisplus.core.metadata.IPage;                       // 分页结果接口
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;         // 分页对象

// --- 本项目内部 ---
import com.example.backend.common.BusinessException; // 业务异常（不含堆栈的错误信息向）
import com.example.backend.common.ResultCode;         // 状态码枚举
import com.example.backend.dto.TeacherDTO;            // 新增/修改教师时的请求数据
import com.example.backend.dto.TeacherQueryDTO;       // 分页查询时的请求参数
import com.example.backend.entity.Teacher;            // 教师实体 → teacher 表
import com.example.backend.mapper.TeacherMapper;      // 教师 Mapper（MyBatis-Plus 的 BaseMapper 增强接口）
import com.example.backend.service.TeacherService;    // 本文件要实现的接口
import com.example.backend.vo.TeacherVO;              // 教师视图对象

// --- Lombok ---
import lombok.RequiredArgsConstructor; // 为 final 字段生成构造器
import lombok.extern.slf4j.Slf4j;     // 日志注解

// --- Spring ---
import org.springframework.stereotype.Service;                   // Service Bean 注解
import org.springframework.transaction.annotation.Transactional; // 事务注解

// ==================== 3. 类的声明和注解 ====================

/*
 * @Slf4j：Lombok 自动注入日志对象 log。
 *   编译后等价于：
 *     private static final Logger log = LoggerFactory.getLogger(TeacherServiceImpl.class);
 *   可在代码中直接使用 log.info()、log.debug()、log.error() 等。
 *
 *   类比 JS 中的 console.log，但更强大（可以分级别，可以输出到文件）。
 */
@Slf4j

/*
 * @Service：标记为 Service 层 Bean。
 *   这个注解有双重作用：
 *     1. 标记语义：告诉开发者这是业务逻辑层
 *     2. 自动扫描：Spring Boot 启动时扫到这个注解，把本类注册为 Bean
 *
 *   类比 JS 中的 "服务注册"：
 *     app.service('TeacherService', TeacherServiceImpl) // Angular 风格
 */
@Service

/*
 * @RequiredArgsConstructor：构造器注入。
 *   只有一个 final 字段 teacherMapper，所以生成：
 *     public TeacherServiceImpl(TeacherMapper teacherMapper) {
 *         this.teacherMapper = teacherMapper;
 *     }
 *
 *   Spring 启动时：
 *     1. 发现 @Service 注解，决定创建 TeacherServiceImpl 实例
 *     2. 发现构造函数需要一个 TeacherMapper 参数
 *     3. 在容器中查找 TeacherMapper 对应的 Bean（MyBatis 自动生成的代理）
 *     4. 注入 Bean，完成实例化
 *
 *   这种方式的优点：
 *     - final 字段保证不可变
 *     - 不需要 @Autowired 注解
 *     - 测试时可以手动 new TeacherServiceImpl(mockMapper)
 */
@RequiredArgsConstructor

/*
 * public class TeacherServiceImpl implements TeacherService
 *
 *   这是所有 Service 中最简单的一个实现类：
 *     - 只有 1 个 Mapper 依赖
 *     - 没有跨表查询
 *     - CRUD 逻辑很直接
 */
public class TeacherServiceImpl implements TeacherService { // 教师服务实现类

    // ==================== 4. 字段声明 ====================

    /*
     * private final TeacherMapper teacherMapper;
     *
     *   唯一依赖：TeacherMapper 接口。
     *   其实现类由 MyBatis Plus 在启动时通过动态代理生成。
     *   我们只需使用接口定义的方法。
     *
     *   类比 JS：
     *     const teacherMapper = {
     *       selectPage: (page, query) => { ... },
     *       selectById: (id) => { ... },
     *       insert: (entity) => { ... },
     *       updateById: (entity) => { ... },
     *       deleteById: (id) => { ... }
     *     }
     */
    private final TeacherMapper teacherMapper; // 教师 Mapper（MyBatis-Plus 通用接口）

    // ==================== 5. 方法实现 ====================

    /*
     * ================================================================
     * 方法 1：分页条件查询教师列表
     *
     * 查询条件支持：
     *   - name：按姓名模糊搜索（LIKE '%关键词%'）
     *   - department：按院系模糊搜索
     * 排序：按创建时间倒序
     * ================================================================
     */
    @Override
    public IPage<TeacherVO> pageQuery(TeacherQueryDTO dto) {

        /*
         * new Page<>(dto.getPageNum(), dto.getPageSize());
         *
         *   创建 MyBatis-Plus 分页对象。
         *
         *   页面从 1 开始（不是 0），每页条数由前端传入。
         *
         *   例如 Page<>(2, 15) 表示查第 2 页，每页 15 条。
         *
         *   类比 JS：
         *     const page = { page: 2, limit: 15 }
         *     // Mongoose: Model.find().skip((2-1)*15).limit(15)
         */
        Page<Teacher> page = new Page<>(dto.getPageNum(), dto.getPageSize()); // 创建分页对象

        /*
         * LambdaQueryWrapper<Teacher> queryWrapper = new LambdaQueryWrapper<>();
         *
         *   创建 Teacher 表的条件构造器。
         *
         *   这个构造器相当于一个可变的 SQL WHERE 子句，你可以：
         *     - .eq() → 等于（=）
         *     - .ne() → 不等于（!=）
         *     - .like() → 模糊搜索（LIKE '%x%'）
         *     - .gt() → 大于（>）
         *     - .lt() → 小于（<）
         *     - .between() → 在...之间（BETWEEN）
         *     - .in() → 在列表中（IN）
         *     - .orderByAsc() / .orderByDesc() → 排序
         */
        LambdaQueryWrapper<Teacher> queryWrapper = new LambdaQueryWrapper<>(); // 条件构造器

        /*
         * 动态添加查询条件：只有在参数不为空时才添加。
         *
         * dto.getName() != null && !dto.getName().isEmpty()
         *   先判断是否为 null（没传这个参数），再判断是否为空字符串（传了但没填值）。
         *
         *   注意 null 和 "" 的区别：
         *     - null：字段不存在或值为 null
         *     - ""（空字符串）：字段存在但值为空
         *     两者都要排除，所以用两个条件。
         */
        if (dto.getName() != null && !dto.getName().isEmpty()) { // 姓名参数有效
            /*
             * queryWrapper.like(Teacher::getName, dto.getName());
             *
             *   .like()：模糊搜索，等价于 SQL 的 WHERE name LIKE '%关键词%'
             *
             *   Teacher::getName 是方法引用，MyBatis-Plus 解析为字段名 "name"。
             *
             *   为什么用方法引用而不是字符串？
             *     编译时安全：如果将来 Teacher 类的 getName 方法被改名或删除，
             *     这里编译会报错，而不是运行时才暴露问题。
             *
             *   类比 JS：
             *     if (dto.name) query.name = { $regex: dto.name, $options: 'i' }
             *     // 或 Sequelize: { name: { [Op.like]: `%${dto.name}%` } }
             */
            queryWrapper.like(Teacher::getName, dto.getName()); // 模糊搜索：LIKE '%name%'
        }
        if (dto.getDepartment() != null && !dto.getDepartment().isEmpty()) { // 院系参数有效
            queryWrapper.like(Teacher::getDepartment, dto.getDepartment()); // 模糊搜索：LIKE '%department%'
        }

        /*
         * queryWrapper.orderByDesc(Teacher::getCreateTime);
         *
         *   按创建时间倒序排列（最新的教师排在最前面）。
         *
         *   对应 SQL：ORDER BY create_time DESC
         *
         *   如果想按姓名升序：queryWrapper.orderByAsc(Teacher::getName)
         */
        queryWrapper.orderByDesc(Teacher::getCreateTime); // 按创建时间倒序排列

        /*
         * teacherMapper.selectPage(page, queryWrapper);
         *
         *   selectPage 是 MyBatis-Plus 的核心分页方法，做了两件事：
         *     1. 执行 COUNT 查询（拿到总记录数，存到 page.total）
         *     2. 执行 SELECT 查询（拿到当前页的记录，存到 page.records）
         *
         *   返回 IPage<Teacher>，其中：
         *     page.records  → List<Teacher>，当前页的教师列表
         *     page.total    → long，符合条件的总数
         *     page.current  → long，当前页码
         *     page.size     → long，每页大小
         *     page.pages    → long，总页数（total/size 向上取整）
         *
         *   最终生成两条 SQL：
         *     1. SELECT COUNT(*) FROM teacher WHERE name LIKE ? AND department LIKE ?
         *     2. SELECT * FROM teacher WHERE name LIKE ? AND department LIKE ?
         *            ORDER BY create_time DESC LIMIT ?, ?
         */
        IPage<Teacher> teacherPage = teacherMapper.selectPage(page, queryWrapper); // 执行分页查询

        /*
         * teacherPage.convert(t -> BeanUtil.copyProperties(t, TeacherVO.class));
         *
         *   convert() 是 Page 类的方法，用于转换分页结果中的数据类型。
         *
         *   逻辑：遍历 teacherPage.records 中的每个 Teacher 对象，
         *        用 BeanUtil.copyProperties 复制到 TeacherVO 中，
         *        生成新的 List<TeacherVO> 填回分页对象。
         *
         *   t -> BeanUtil.copyProperties(t, TeacherVO.class)
         *     这是 Java 的 Lambda 表达式（箭头函数）：
         *     t 是参数名（代表每行 Teacher），-> 后面是返回值表达式。
         *
         *   为什么需要转换？
         *     数据库查出来的是 Teacher（包含所有数据库字段），
         *     但返回给前端的应该是 TeacherVO（可能只包含部分字段或格式化后的数据）。
         *     在这个项目中，TeacherVO 和 Teacher 字段几乎一样，所以直接拷贝。
         *
         *   类比 JS：
         *     page.records = page.records.map(t => ({ ...t }))
         */
        return teacherPage.convert(t -> BeanUtil.copyProperties(t, TeacherVO.class)); // 转为 VO 列表返回
    }

    /*
     * ================================================================
     * 方法 2：根据 ID 查询教师详情
     *
     * 这是最简单的查询方法：按主键查一条记录。
     * ================================================================
     */
    @Override
    public TeacherVO getById(Long id) {

        /*
         * teacherMapper.selectById(id);
         *
         *   selectById：MyBatis-Plus 的通用主键查询方法。
         *   只要实体类用 @TableId 标注了主键字段，这个方法就能工作。
         *
         *   生成的 SQL：SELECT * FROM teacher WHERE id = ?
         *
         *   如果查不到，返回 null。
         *
         *   类比 JS：
         *     const teacher = await Teacher.findById(id)
         */
        Teacher teacher = teacherMapper.selectById(id); // 按主键查询

        if (teacher == null) { // 教师不存在
            /*
             * throw new BusinessException(ResultCode.BUSINESS_ERROR.getCode(), "教师不存在");
             *
             *   抛出业务异常，而不是返回 null。
             *   为什么要抛异常而不是返回 null？
             *     - 返回 null：Controller 不知道出了什么事，可能把 null 返回给前端
             *     - 抛异常：明确告诉上层"操作失败了"，全局异常处理器能统一处理
             *
             *   类比 JS：
             *     if (!teacher) throw new AppError('教师不存在')
             *     // Express 错误中间件捕获并返回 400
             */
            throw new BusinessException(ResultCode.BUSINESS_ERROR.getCode(), "教师不存在");
        }

        /*
         * BeanUtil.copyProperties(teacher, TeacherVO.class);
         *
         *   把 Teacher 实体转为 TeacherVO 视图对象后返回。
         *   如果 Teacher 和 TeacherVO 字段完全一样，这就是一行转发。
         *   如果不一样（如 VO 多了字段），需要后续 set。
         */
        return BeanUtil.copyProperties(teacher, TeacherVO.class); // 转为 VO 返回
    }

    /*
     * ================================================================
     * 方法 3：新增教师
     *
     * 数据流转：
     *   前端 POST JSON → Spring 转 TeacherDTO → 本方法
     *        → DTO 转 Entity → INSERT INTO teacher → 成功
     * ================================================================
     */
    @Override
    @Transactional(rollbackFor = Exception.class) // 事务
    public void save(TeacherDTO dto) {

        /*
         * BeanUtil.copyProperties(dto, Teacher.class);
         *
         *   把 DTO 的所有同名字段复制到新 Teacher 实例中：
         *     dto.name        → teacher.name
         *     dto.teacherNo   → teacher.teacherNo
         *     dto.department  → teacher.department
         *     ...             → ...
         */
        Teacher teacher = BeanUtil.copyProperties(dto, Teacher.class); // DTO → Entity

        /*
         * teacherMapper.insert(teacher);
         *
         *   插入数据库。如果实体类配置了主键自增策略（@TableId(type = IdType.AUTO)），
         *   插入后 teacher.getId() 会返回数据库自动生成的 ID。
         *
         *   最终 SQL：INSERT INTO teacher (name, teacher_no, department, ...) VALUES (?, ?, ?, ...)
         *
         *   类比 JS：
         *     await Teacher.create(dto)
         */
        teacherMapper.insert(teacher); // 插入数据库

        /*
         * log.info("新增教师成功，工号：{}，姓名：{}", dto.getTeacherNo(), dto.getName());
         *
         *   SLF4J 日志：{} 是占位符，依次被后面的参数替换。
         *   输出效果：新增教师成功，工号：T2024001，姓名：李老师
         *
         *   日志级别（从低到高）：
         *     trace < debug < info < warn < error
         *   生产环境通常只输出 info 及以上级别。
         *
         *   类比 JS：
         *     console.log(`新增教师成功，工号：${dto.teacherNo}，姓名：${dto.name}`)
         */
        log.info("新增教师成功，工号：{}，姓名：{}", dto.getTeacherNo(), dto.getName()); // 记录日志
    }

    /*
     * ================================================================
     * 方法 4：修改教师信息
     *
     *   按主键 id 更新记录。dto 中必须包含 id。
     * ================================================================
     */
    @Override
    @Transactional(rollbackFor = Exception.class) // 事务
    public void update(TeacherDTO dto) {
        // DTO → Entity → 按主键更新
        Teacher teacher = BeanUtil.copyProperties(dto, Teacher.class); // DTO → Entity
        /*
         * teacherMapper.updateById(teacher);
         *
         *   按实体中的主键字段（id）定位记录，更新其他非空字段。
         *
         *   最终 SQL：UPDATE teacher SET name=?, teacher_no=?, department=?, ... WHERE id=?
         *
         *   类比 JS：
         *     await Teacher.updateById(dto.id, dto)
         */
        teacherMapper.updateById(teacher); // 按主键更新
        log.info("修改教师信息成功"); // 记录日志
    }

    /*
     * ================================================================
     * 方法 5：删除教师
     *
     *   按主键 id 删除记录。
     *   注意：这里没有检查外键关联（如该教师是否还有课程），
     *   如果 course 表中有 teacher_id 引用这个教师，数据库会报外键约束错误。
     * ================================================================
     */
    @Override
    @Transactional(rollbackFor = Exception.class) // 事务
    public void delete(Long id) {
        /*
         * teacherMapper.deleteById(id);
         *
         *   最终 SQL：DELETE FROM teacher WHERE id = ?
         *
         *   类比 JS：
         *     await Teacher.deleteById(id)
         */
        teacherMapper.deleteById(id); // 按主键删除
        log.info("删除教师成功，ID：{}", id); // 记录日志
    }
}
