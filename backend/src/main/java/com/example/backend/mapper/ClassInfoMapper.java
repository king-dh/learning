/*
 * ================================================================
 * 文件位置:  src/main/java/com/example/backend/mapper/ClassInfoMapper.java
 * 对应 Entity: ClassInfo.java
 * 对应数据库表: class_info
 * 在架构中的位置:
 *
 *   Controller → Service → Mapper(当前文件) → 数据库 class_info 表
 *
 * Mapper（数据访问接口）= class_info（班级）表的数据库操作接口
 *
 * JS 类比：
 *   const ClassInfoMapper = {
 *     insert(classInfo) { ... },       // 新增班级
 *     selectById(id) { ... },          // 按ID查班级
 *     updateById(classInfo) { ... },   // 修改班级
 *     deleteById(id) { ... },          // 删除班级
 *     selectList(where) { ... },       // 查班级列表（可加条件过滤）
 *     selectPage(page, where) { ... }, // 分页查询班级
 *     selectCount(where) { ... },      // 统计班级数量
 *   }
 *   // 所有方法都从 BaseMapper 自动继承，无需自己写！
 *
 * 为什么没有自定义方法？
 *   class_info 表的所有操作都是标准 CRUD，没有特殊的复杂查询需求。
 *   比如：
 *     - 查所有班级 → mapper.selectList(null)
 *     - 查某个年级的班级 → mapper.selectList(wrapper.eq("grade", "2024级"))
 *     - 查某个班主任的班级 → mapper.selectList(wrapper.eq("headTeacherId", 5L))
 *   这些用条件构造器（Wrapper）就能实现，不需要手写 SQL。
 *
 * 条件构造器（Wrapper）使用示例（在 Service 中调用）：
 *   // 查询 2024 级的所有班级
 *   LambdaQueryWrapper<ClassInfo> wrapper = new LambdaQueryWrapper<>();
 *   wrapper.eq(ClassInfo::getGrade, "2024级");         // WHERE grade = '2024级'
 *   List<ClassInfo> list = classInfoMapper.selectList(wrapper);
 *
 *   // 查询某个班主任管理的班级
 *   wrapper.eq(ClassInfo::getHeadTeacherId, 5L);        // WHERE head_teacher_id = 5
 *
 *   // 分页查询第 1 页，每页 10 条
 *   Page<ClassInfo> page = new Page<>(1, 10);           // 第1页, 10条/页
 *   classInfoMapper.selectPage(page, null);             // 无条件分页
 *
 * 你可以把 Wrapper 理解为 JS 中的查询条件构造器：
 *   const where = { grade: "2024级" }  // JS 的查询条件
 *   // 等价于 Wrapper: wrapper.eq(ClassInfo::getGrade, "2024级")
 * ================================================================
 */

// ==================== 1. 包声明 ====================
package com.example.backend.mapper; // mapper 子包（数据访问层）

// ==================== 2. 导入其他类 ====================

/*
 * com.baomidou.mybatisplus.core.mapper.BaseMapper
 *   MyBatis-Plus 泛型接口，提供所有通用 CRUD 方法。
 *   泛型参数 <ClassInfo> 告诉框架要操作 class_info 表。
 *
 *   继承后自动获得的核心方法（不需要写任何代码）：
 *     - int insert(ClassInfo entity)              → 插入一条班级记录
 *     - int deleteById(Long id)                   → 按主键删除班级
 *     - int deleteByMap(Map<String, Object> map)  → 按条件删除班级
 *     - int updateById(ClassInfo entity)          → 按主键更新班级
 *     - ClassInfo selectById(Long id)             → 按主键查一个班级
 *     - List<ClassInfo> selectBatchIds(List)      → 按多个ID批量查
 *     - List<ClassInfo> selectByMap(Map)          → 按条件查列表
 *     - List<ClassInfo> selectList(Wrapper)       → 条件查询列表
 *     - Page<ClassInfo> selectPage(Page, Wrapper) → 分页查询
 *     - Long selectCount(Wrapper)                 → 统计数量
 *
 *   JS 类比：
 *     相当于一个内置的 ORM：
 *       classInfoDB.findMany({ where: { grade: "2024级" } })
 *       classInfoDB.create({ data: { className: "软件1班" } })
 *       classInfoDB.update({ where: { id: 1 }, data: { grade: "2025级" } })
 *       classInfoDB.delete({ where: { id: 1 } })
 */
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

/*
 * com.example.backend.entity.ClassInfo
 *   班级实体类，BaseMapper 的泛型参数。
 *   这个导入告诉编译器：BaseMapper 的具体类型是 ClassInfo。
 *
 *   注意：如果你忘记了导入 ClassInfo 但写了 BaseMapper<ClassInfo>，
 *   编译器会报错"Cannot resolve symbol 'ClassInfo'"——
 *   这说明 import 语句是必须的，不能省略。
 */
import com.example.backend.entity.ClassInfo;

/*
 * org.apache.ibatis.annotations.Mapper
 *   MyBatis 的注解，标记接口为 Mapper。
 *
 *   为什么需要 @Mapper？
 *     Spring 的组件扫描默认不识别 MyBatis 的 Mapper 接口。
 *     @Mapper 告诉 MyBatis-Spring 框架："这个接口需要我管理"。
 *     如果不加 @Mapper，Service 中 @Autowired 注入时找不到这个 Bean，启动报错。
 *
 *   替代方案：
 *     也可以在 Spring Boot 启动类上使用 @MapperScan("com.example.backend.mapper")
 *     来批量扫描某个包下所有的 Mapper 接口，这样每个 Mapper 就都不需要 @Mapper 了。
 *     但逐个写 @Mapper 更直观——打开文件就知道这是一个 Mapper 接口。
 */
import org.apache.ibatis.annotations.Mapper;

// ==================== 3. 接口的声明和注解 ====================

/*
 * @Mapper
 *   告诉 MyBatis-Spring："ClassInfoMapper 是一个数据访问接口，请创建它的代理实现"。
 *
 *   框架做的事情（对初学者来说只需要了解）：
 *     1. 扫描到 @Mapper 注解
 *     2. 使用 JDK 动态代理（java.lang.reflect.Proxy）创建实现类
 *     3. 将代理对象注册到 Spring 容器
 *     4. Service 中通过 @Autowired 注入时获取的就是这个代理对象
 *
 *   你写的：
 *     @Mapper
 *     public interface ClassInfoMapper extends BaseMapper<ClassInfo> { }
 *
 *   框架等效于帮你写了（伪代码，实际是用动态代理）：
 *     public class ClassInfoMapperImpl implements ClassInfoMapper {
 *         // 自动实现 BaseMapper 的所有方法
 *         public int insert(ClassInfo entity) {
 *             return sqlSession.insert("insert", entity);
 *         }
 *         // ... 其他方法类似
 *     }
 */
@Mapper

/*
 * public interface ClassInfoMapper extends BaseMapper<ClassInfo>
 *
 *   逐词解释：
 *     public         —— 公开接口
 *     interface      —— 接口关键字（不是 class！）
 *                      Java 中 interface 和 class 的区别：
 *                        interface：只声明方法签名，不写方法体（"做什么"）
 *                        class：声明方法签名 + 写方法体（"做什么" + "怎么做"）
 *     ClassInfoMapper—— 接口名，命名规范：实体名 + Mapper
 *                      ClassInfo + Mapper = ClassInfoMapper（操作班级表的 Mapper）
 *     extends        —— 继承关键字
 *                      Java 接口之间可以继承（extends），一个接口可以继承多个父接口
 *     BaseMapper<ClassInfo> —— 父接口 + 泛型参数
 *                      <ClassInfo> 告诉 BaseMapper："我操作的是 ClassInfo 实体"
 *                      这样 BaseMapper 里所有方法都知道操作哪个实体类。
 *
 *   类比 TypeScript：
 *     interface ClassInfoMapper extends BaseMapper<ClassInfo> {
 *       // 空接口体 ← 所有方法都从父接口继承
 *     }
 *
 *   注意：这个接口体是空的（没有声明任何方法），这完全合法！
 *   因为所有需要的方法都从 BaseMapper<ClassInfo> 继承下来了。
 *   空接口体意味着"我不需要任何自定义查询，标准 CRUD 就够了"。
 */
public interface ClassInfoMapper extends BaseMapper<ClassInfo> {
    /*
     * 空的接口体 —— 不需要任何自定义数据库操作方法。
     *
     * 所有的 CRUD 操作都已由 BaseMapper<ClassInfo> 提供：
     *   insert、deleteById、updateById、selectById、selectList、selectPage 等。
     *
     * 如果未来有特殊查询需求，可以在这里添加方法，例如：
     *   @Select("SELECT * FROM class_info WHERE grade = #{grade}")
     *   List<ClassInfo> selectByGrade(@Param("grade") String grade);
     *
     * ================================================================
     * 总结：ClassInfoMapper = class_info 表的数据库操作接口
     *   - 继承 BaseMapper<ClassInfo> 获得所有标准 CRUD 方法
     *   - 无需自定义 SQL（所有查询可用 Wrapper 条件构造器完成）
     *   - 这是最简单的 Mapper 形式：只需继承 BaseMapper
     * ================================================================
     */
}
