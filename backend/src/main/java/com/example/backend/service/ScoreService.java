/*
 * ================================================================
 * 文件位置:  src/main/java/com/example/backend/service/ScoreService.java
 * 架构层级:  Service 接口（业务逻辑层的"合同/规范"）
 * 实现类:    src/main/java/com/example/backend/service/impl/ScoreServiceImpl.java
 * 被调用者:  src/main/java/com/example/backend/controller/ScoreController.java
 *
 * 调用链路（以"分页查询成绩"为例）:
 * 前端成绩管理页面 → axios 发 GET /api/scores/page?pageNum=1&studentId=5
 *                 → ScoreController.page()
 *                 → ScoreService.pageQuery()（本接口）
 *                 → ScoreServiceImpl.pageQuery()（实现类）
 *                 → MyBatis-Plus 查 score 表 → 联查 student 表获学生姓名
 *                                           → 联查 course 表获课程名称
 *                 → 返回分页 JSON 给前端
 *
 * 成绩服务的特殊性：
 *   成绩是"学生"和"课程"之间的关联数据，类似于"选课"之后的结果。
 *   成绩表（score）通常有外键 student_id 和 course_id，
 *   分别指向 student 表和 course 表。
 *
 *   本接口除了标准 CRUD，还提供了按学生 ID 查全部成绩的方法，
 *   服务于前端"学生成绩单"页面。
 *
 *   JS 类比（MERN 栈）：
 *     // ScoreService = 处理学生成绩的模块
 *     // score 集合里有 studentId 和 courseId 两个外键字段
 * ================================================================
 */

// ==================== 1. 包声明 ====================
package com.example.backend.service; // 声明当前文件属于 service 子包

// ==================== 2. 导入其他类（import） ====================

// MyBatis-Plus 分页结果接口
import com.baomidou.mybatisplus.core.metadata.IPage; // MyBatis-Plus 分页结果

// --- DTO（接收前端请求数据）---
import com.example.backend.dto.ScoreDTO;      // 新增/修改成绩时的请求数据
import com.example.backend.dto.ScoreQueryDTO; // 分页查询时的请求参数（学生 ID、课程 ID、学期等）

// --- VO（返回给前端的数据）---
import com.example.backend.vo.ScoreVO; // 成绩视图对象（含学生姓名、课程名称）

// Java 标准库
import java.util.List; // 列表接口

// ==================== 3. 接口声明 ====================

/*
 * public interface ScoreService { ... }
 *
 *   成绩服务接口，定义了 5 个方法。
 *   注意这里没有 getById 方法 —— 因为成绩通常不需要按 ID 查详情，
 *   前端要么查分页列表，要么按学生 ID 查成绩单。
 */
public interface ScoreService {

    // ==================== 4. 方法签名 ====================

    /*
     * ================================================================
     * 方法 1：分页条件查询成绩列表
     *
     * IPage<ScoreVO> pageQuery(ScoreQueryDTO dto);
     *
     *   返回的分页结果中，每个 ScoreVO 包含：
     *     - 成绩基本信息（分数、学期等）
     *     - studentName（学生姓名，联查 student 表）
     *     - courseName（课程名称，联查 course 表）
     *
     *   查询条件支持：
     *     - studentId：按学生精确筛选
     *     - courseId：按课程精确筛选
     *     - semester：按学期精确筛选
     */
    IPage<ScoreVO> pageQuery(ScoreQueryDTO dto);

    /*
     * ================================================================
     * 方法 2：新增成绩
     *
     * void save(ScoreDTO dto);
     *
     *   将前端传来的成绩数据保存到 score 表。
     *   dto 包含：studentId、courseId、score、semester 等。
     */
    void save(ScoreDTO dto);

    /*
     * ================================================================
     * 方法 3：修改成绩
     *
     * void update(ScoreDTO dto);
     */
    void update(ScoreDTO dto);

    /*
     * ================================================================
     * 方法 4：删除成绩
     *
     * void delete(Long id);
     */
    void delete(Long id);

    /*
     * ================================================================
     * 方法 5：根据学生 ID 查询该学生的所有成绩
     *
     * List<ScoreVO> getByStudentId(Long studentId);
     *
     *   返回某个学生的全部成绩记录（含课程名称和学生姓名）。
     *   这是为"学生成绩单"页面服务的。
     *
     *   返回类型是 List<ScoreVO> 而不是 IPage<ScoreVO>，
     *   说明返回所有记录，不做分页。因为一个学生的成绩通常不会太多。
     *
     *   JS 类比：
     *     async function getByStudentId(studentId) {
     *       return await Score.find({ studentId })
     *         .populate('studentId', 'name')
     *         .populate('courseId', 'name')
     *     }
     */
    List<ScoreVO> getByStudentId(Long studentId);
}
