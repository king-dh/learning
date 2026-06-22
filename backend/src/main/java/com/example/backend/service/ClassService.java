/*
 * ================================================================
 * 文件位置:  src/main/java/com/example/backend/service/ClassService.java
 * 架构层级:  Service 接口（业务逻辑层的"合同/规范"）
 * 实现类:    src/main/java/com/example/backend/service/impl/ClassServiceImpl.java
 * 被调用者:  src/main/java/com/example/backend/controller/ClassController.java
 *
 * 调用链路（以"分页查询班级"为例）:
 * 前端班级管理页面 → axios 发 GET /api/classes/page?pageNum=1
 *                 → ClassController.page()
 *                 → ClassService.pageQuery()（本接口）
 *                 → ClassServiceImpl.pageQuery()（实现类）
 *                 → MyBatis-Plus 查 class_info 表 → 联查 teacher 表获班主任姓名
 *                                               → 统计 student 表获学生人数
 *                 → 返回分页 JSON 给前端
 *
 * 接口是一种"能力声明"：
 *   这个接口告诉所有调用者："任何实现了 ClassService 的类，
 *   都能做班级的增删改查、分页查询和查班级学生列表"。
 *   至于具体怎么实现（用 MyBatis-Plus 还是 JPA，要不要加缓存），
 *   调用者不用关心。
 *
 *   JS 类比：
 *     // TS 中
 *     interface IClassService {
 *       pageQuery(dto: ClassQueryDTO): IPage<ClassVO>;
 *       getById(id: number): ClassVO;
 *       save(dto: ClassDTO): void;
 *       update(dto: ClassDTO): void;
 *       delete(id: number): void;
 *       getStudentsByClassId(classId: number): StudentVO[];
 *     }
 * ================================================================
 */

// ==================== 1. 包声明 ====================
package com.example.backend.service; // 声明当前文件属于 service 子包（业务逻辑层接口）

// ==================== 2. 导入其他类（import） ====================

// MyBatis-Plus 分页结果接口
import com.baomidou.mybatisplus.core.metadata.IPage; // MyBatis-Plus 分页结果

// --- DTO（接收前端请求数据）---
import com.example.backend.dto.ClassDTO;      // 新增/修改班级时的请求数据
import com.example.backend.dto.ClassQueryDTO; // 分页查询时的请求参数（班级名称、年级等）

// --- VO（返回给前端的视图数据）---
import com.example.backend.vo.ClassVO;   // 班级视图对象（含班主任姓名、学生人数）
import com.example.backend.vo.StudentVO;  // 学生视图对象（查询班级学生列表时返回）

// Java 标准库
import java.util.List; // 列表接口（类似于 JS 的 Array）

// ==================== 3. 接口声明 ====================

/*
 * public interface ClassService { ... }
 *
 *   ClassService 定义了 6 个方法，其中前 5 个是标准 CRUD + 分页，
 *   第 6 个是特殊的"查询班级下所有学生"。
 *
 *   注意命名规范：接口名 = 名词 + Service，方法名 = 动词 + 宾语（或按约定命名）。
 *   Java 社区习惯：pageQuery（分页查询）、getById（按 ID 查）、save（新增）、
 *                 update（修改）、delete（删除）。
 */
public interface ClassService {

    // ==================== 4. 方法签名 ====================

    /*
     * ================================================================
     * 方法 1：分页条件查询班级列表
     *
     * IPage<ClassVO> pageQuery(ClassQueryDTO dto);
     *
     *   返回的分页结果中，每个 ClassVO 包含：
     *     - 班级基本信息（名称、年级、教室等）
     *     - headTeacherName（班主任姓名，通过联查 teacher 表获得）
     *     - studentCount（班级学生人数，通过统计 student 表获得）
     *
     *   查询条件支持：
     *     - className：班级名称模糊搜索
     *     - grade：年级模糊搜索
     */
    IPage<ClassVO> pageQuery(ClassQueryDTO dto);

    /*
     * ================================================================
     * 方法 2：根据 ID 查询班级详情
     *
     * ClassVO getById(Long id);
     *
     *   返回单个班级的完整信息，含班主任姓名和学生人数。
     */
    ClassVO getById(Long id);

    /*
     * ================================================================
     * 方法 3：新增班级
     *
     * void save(ClassDTO dto);
     */
    void save(ClassDTO dto);

    /*
     * ================================================================
     * 方法 4：修改班级信息
     *
     * void update(ClassDTO dto);
     */
    void update(ClassDTO dto);

    /*
     * ================================================================
     * 方法 5：删除班级
     *
     * void delete(Long id);
     */
    void delete(Long id);

    /*
     * ================================================================
     * 方法 6：查询班级下的所有学生列表
     *
     * List<StudentVO> getStudentsByClassId(Long classId);
     *
     *   List<StudentVO>  —— 返回值：学生视图对象列表（不是分页，是全部学生）
     *   getStudentsByClassId —— 方法名：按班级 ID 查询学生
     *   Long classId     —— 参数：班级主键 ID
     *
     *   这个方法的典型使用场景：
     *   前端班级详情页面，点击某个班级后展示该班级的所有学生。
     *
     *   数据流转：
     *     前端传 classId = 1
     *           → 实现类查询 WHERE class_id = 1
     *           → 返回 List<StudentVO> [{ id: 1, name: "张三", ... }, { id: 2, name: "李四", ... }]
     *
     *   JS 类比：
     *     async function getStudentsByClassId(classId) {
     *       return await Student.find({ classId })
     *     }
     */
    List<StudentVO> getStudentsByClassId(Long classId);
}
