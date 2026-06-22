/*
 * ================================================================
 * 文件位置:  src/main/java/com/example/backend/dto/EnrollmentDTO.java
 * 对应前端:  frontend/src/views/enrollment/（选课相关页面）
 *           frontend/src/api/enrollment.js（前端 API 调用）
 *
 * 数据流向（以学生选课为例）:
 *   学生在选课页面看到课程列表，点击某门课旁边的"选课"按钮
 *     ↓
 *   axios.post('/api/enrollments', { studentId: 12, courseId: 8 })
 *     ↓ JSON 请求体
 *     ↓ Vite 代理转发到 Spring Boot
 *   Spring 用 Jackson 把 JSON 自动转换成 EnrollmentDTO 对象:
 *     EnrollmentDTO { studentId=12, courseId=8 }
 *     ↓ EnrollmentController.save() → EnrollmentService
 *     ↓ 检查是否重复选课（同一个学生不能选两次同一门课）
 *     ↓ 检查课程容量（已选人数 < maxStudents）
 *     ↓ INSERT INTO enrollment ...
 *     ↓ 返回成功
 *
 * 这个 DTO 为什么只有两个字段？
 *   选课操作本身只需要两个信息：
 *     - 谁选课（studentId）
 *     - 选什么课（courseId）
 *   其他信息（如选课时间、学期等）由后端自动填充，前端不需要传。
 *
 * JS 类比:
 *   // 前端按钮点击事件
 *   const enrollCourse = (courseId: number) => {
 *     axios.post('/api/enrollments', {
 *       studentId: currentUser.studentId,  // 从登录用户信息中取
 *       courseId: courseId                  // 从列表行数据中取
 *     })
 *   }
 *
 *   // TypeScript 接口定义
 *   interface EnrollRequest {
 *     studentId: number;
 *     courseId: number;
 *   }
 * ================================================================
 */

// ==================== 1. 包声明 ====================

/*
 * package com.example.backend.dto;
 *   声明当前文件属于 dto 包。
 *   EnrollmentDTO 作为选课请求的数据载体，和所有 DTO 放在一起。
 */
package com.example.backend.dto; // 声明 DTO 包

// ==================== 2. 导入其他类（import） ====================

/*
 * import lombok.Data;
 *   Lombok 注解：编译时自动生成 getter/setter 方法。
 *   Spring 在接收 JSON 时需要调用 setStudentId() / setCourseId() 来赋值。
 */
import lombok.Data; // Lombok 注解：自动生成 getter/setter/toString/equals/hashCode

// ==================== 3. 类的声明和注解 ====================

/*
 * @Data
 *   自动生成 2 个字段的 getter/setter/toString/equals/hashCode。
 *   虽然只有 2 个字段，手写也不麻烦，但用 @Data 保持项目一致性。
 */
@Data // 自动生成 Getter/Setter/toString/equals/hashCode

/*
 * public class EnrollmentDTO { ... }
 *
 *   Enrollment   ← 业务领域：选课（Enrollment = 报名、选课）
 *   DTO          ← 类型：数据传输对象
 *
 *   命名对应的数据库表：enrollment（选课记录表）
 */
public class EnrollmentDTO {

    // ==================== 4. 字段声明 ====================

    /*
     * private Long studentId;
     *
     *   分解解释:
     *     private    —— 封装（外部通过 getter/setter 访问）
     *     Long       —— Java 的 64 位整数包装类型
     *                  可以 null（虽然这里选课时学生 ID 不会为空，但用 Long 保持一致性）
     *                  MySQL 对应: BIGINT
     *     studentId  —— 学生 ID
     *                  数据库对应列: student_id
     *
     *   用途:       标识哪位学生在选课
     *   数据来源:   前端在用户登录后，从 userStore 或 localStorage 中获取当前登录用户的学生 ID
     *              前端表单不需要用户手动输入，自动从登录状态中读取
     *   数据库关联: student 表的主键 id（BIGINT）
     *   示例值:     12L（id=12 的学生张三在选课）
     *
     *   业务含义:
     *     "学生 ID 为 12 的人想要选课程 ID 为 8 的课"
     *     后端根据 studentId 找到学生信息，根据 courseId 找到课程信息，建立关联。
     *
     *   JS 类比:
     *     // 前端从 store 中获取当前用户
     *     import { useUserStore } from '@/stores/user'
     *     const userStore = useUserStore()
     *     // 选课时自动带上学号
     *     const studentId = userStore.studentId
     */
    private Long studentId; // 学生 ID，标识哪位学生选课（从登录状态中获取）

    // ================================================================

    /*
     * private Long courseId;
     *
     *   分解解释:
     *     private    —— 封装
     *     Long       —— 64 位整数包装类型
     *     courseId   —— 课程 ID
     *                  数据库对应列: course_id
     *
     *   用途:       标识学生要选哪门课程
     *   数据来源:   前端课程列表页，每行课程数据中都有课程 ID
     *              用户点某行的"选课"按钮，把该行的 courseId 传给后端
     *   数据库关联: course 表的主键 id（BIGINT）
     *   示例值:     8L（id=8 的课程"数据结构"）
     *
     *   业务含义:
     *     结合 studentId，形成一条完整的选课记录：
     *       enrollment 表: { id: ..., student_id: 12, course_id: 8, create_time: ... }
     *     代表"学生 12 选了课程 8"。
     *
     *   JS 类比:
     *     // 课程列表，每行有选课按钮
     *     <el-table :data="courseList">
     *       <el-table-column label="操作">
     *         <template #default="{ row }">
     *           <el-button @click="handleEnroll(row.id)">选课</el-button>
     *           // row.id 就是 courseId → handleEnroll(8)
     *         </template>
     *       </el-table-column>
     *     </el-table>
     */
    private Long courseId; // 课程 ID，标识选择哪门课程（从列表行数据中获取）
}
