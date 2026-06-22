/*
 * ================================================================
 * 文件位置:  src/main/java/com/example/backend/service/TeacherService.java
 * 架构层级:  Service 接口（业务逻辑层的"合同/规范"）
 * 实现类:    src/main/java/com/example/backend/service/impl/TeacherServiceImpl.java
 * 被调用者:  src/main/java/com/example/backend/controller/TeacherController.java
 *
 * 调用链路（以"分页查询教师"为例）:
 * 前端教师管理页面 → axios 发 GET /api/teachers/page?pageNum=1&name=李
 *                 → TeacherController.page()
 *                 → TeacherService.pageQuery()（本接口）
 *                 → TeacherServiceImpl.pageQuery()（实现类）
 *                 → MyBatis-Plus 查 teacher 表
 *                 → 返回分页 JSON 给前端
 *
 * 为什么需要接口？
 *   接口 = 合同/规范。Controller 对接口编程，不对具体的实现类编程。
 *   如果以后要换用 JPA 或者普通的 MyBatis XML 方式，只需要新写一个实现类，
 *   Controller 完全不用改。这就是"解耦"。
 *
 *   JS 类比：
 *     // Java 接口
 *     interface TeacherService { pageQuery(dto): IPage<TeacherVO> }
 *     class TeacherServiceImpl implements TeacherService { ... }
 *
 *     // TS 中等价写法
 *     interface ITeacherService { pageQuery(dto): IPage<TeacherVO> }
 *     class TeacherServiceImpl implements ITeacherService { ... }
 * ================================================================
 */

// ==================== 1. 包声明 ====================
package com.example.backend.service; // 声明当前文件属于 service 子包

// ==================== 2. 导入其他类（import） ====================

/*
 * import 关键字：告诉编译器我要用哪些外部的类。
 * 和 JS 的 import { ... } from '...' 概念完全一样。
 * Java 的 import 必须写完整包路径。
 */

// MyBatis-Plus 分页结果接口（IPage<TeacherVO> = 包含 TeacherVO 列表的分页对象）
import com.baomidou.mybatisplus.core.metadata.IPage; // MyBatis-Plus 分页结果

// --- DTO（接收前端数据）---
import com.example.backend.dto.TeacherDTO;      // 新增/修改教师时的请求数据对象
import com.example.backend.dto.TeacherQueryDTO; // 分页查询时的请求参数对象

// --- VO（返回前端数据）---
import com.example.backend.vo.TeacherVO; // 教师视图对象（返回给前端的数据结构）

// ==================== 3. 接口声明 ====================

/*
 * public interface TeacherService { ... }
 *
 *   这是一个纯接口，里面所有方法都是抽象的（只有签名没有实现）。
 *   实现类 TeacherServiceImpl 放在 service/impl/ 文件夹下。
 *
 *   为什么要把接口和实现分开两个文件？
 *     这是 Java 工程中的最佳实践：
 *     1. 调用者（Controller）只依赖接口，不依赖具体实现
 *     2. 接口可以作为"文档"快速了解这个服务有哪些能力
 *     3. 方便单元测试用 Mock 实现替换真实实现
 *
 *   JS 类比：
 *     // 把接口理解为 TS 的 interface 或 JS 中的"规范/约定"
 *     // 在纯 JS 项目中，没有 interface 关键字，但约定俗成：
 *     // TeacherService 这个"服务模块"一定有 pageQuery/getById/save/update/delete 这些方法
 */
public interface TeacherService {

    // ==================== 4. 方法签名 ====================

    /*
     * ================================================================
     * 方法 1：分页条件查询教师列表
     *
     * IPage<TeacherVO> pageQuery(TeacherQueryDTO dto);
     *
     *   IPage<TeacherVO>   —— 返回值：MyBatis-Plus 的分页结果对象
     *                         包含 records（教师列表）、total（总数）、current（当前页）、size（每页大小）
     *   pageQuery           —— 方法名：分页查询
     *   TeacherQueryDTO     —— 参数类型：包含 name（姓名模糊搜索）、department（院系模糊搜索）、pageNum、pageSize
     *
     *   数据流转：
     *     前端传 → { pageNum: 1, pageSize: 10, name: "李", department: "计算机" }
     *           → 实现类构建查询条件 → 查数据库
     *           → 返回 IPage<TeacherVO> { records: [...], total: 15 }
     *
     *   JS 类比：
     *     async function pageQuery(dto) {
     *       const { pageNum, pageSize, name, department } = dto
     *       const query = {}
     *       if (name) query.name = { $like: `%${name}%` }  // 模糊搜索
     *       if (department) query.department = { $like: `%${department}%` }
     *       return Teacher.find(query).skip((pageNum-1)*pageSize).limit(pageSize)
     *     }
     */
    IPage<TeacherVO> pageQuery(TeacherQueryDTO dto);

    /*
     * ================================================================
     * 方法 2：根据 ID 查询教师详情
     *
     * TeacherVO getById(Long id);
     *
     *   TeacherVO  —— 返回值：单个教师的视图对象
     *   getById    —— 方法名：按主键查询
     *   Long id    —— 参数：教师的主键 ID（对应数据库 BIGINT 类型）
     *
     *   如果 ID 对应的教师不存在，实现类应抛出 BusinessException。
     */
    TeacherVO getById(Long id);

    /*
     * ================================================================
     * 方法 3：新增教师
     *
     * void save(TeacherDTO dto);
     *
     *   void        —— 返回值：无（成功不返回数据，失败靠异常通知）
     *   save        —— 方法名
     *   TeacherDTO  —— 参数：包含 teacherNo（工号）、name、department 等
     *
     *   数据流转：DTO → 复制属性到 Teacher 实体 → INSERT INTO teacher ...
     */
    void save(TeacherDTO dto);

    /*
     * ================================================================
     * 方法 4：修改教师信息
     *
     * void update(TeacherDTO dto);
     *
     *   void        —— 返回值：无
     *   update      —— 方法名
     *   TeacherDTO  —— 参数：包含 id 和要修改的字段值
     *
     *   数据流转：DTO → 复制属性到 Teacher 实体 → UPDATE teacher SET ... WHERE id = ?
     */
    void update(TeacherDTO dto);

    /*
     * ================================================================
     * 方法 5：删除教师
     *
     * void delete(Long id);
     *
     *   void   —— 返回值：无
     *   delete —— 方法名
     *   Long   —— 参数类型：要删除的教师 ID
     *
     *   数据流转：id → DELETE FROM teacher WHERE id = ?
     */
    void delete(Long id);
}
