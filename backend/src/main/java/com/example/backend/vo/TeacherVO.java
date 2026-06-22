/*
 * ================================================================
 * 文件位置:  src/main/java/com/example/backend/vo/TeacherVO.java
 * 对应前端:  frontend/src/views/teacher/TeacherList.vue（教师列表页面）
 *           frontend/src/views/teacher/TeacherDetail.vue（教师详情页面）
 *           frontend/src/api/teacher.js（前端 API 调用）
 *
 * 数据流向（以查询教师列表为例）:
 *   前端请求 GET /api/teachers/page
 *     ↓ TeacherController → TeacherService
 *   TeacherService 执行 SQL:
 *     SELECT * FROM teacher
 *     （教师表的所有字段直接返回，不需要 JOIN）
 *     ↓ 返回 TeacherVO 列表 → JSON 序列化
 *   前端收到: {
 *     code: 200,
 *     data: {
 *       records: [{
 *         id: 5, teacherNo: 'T2024001', name: '张教授', gender: '男',
 *         title: '教授', department: '计算机学院', phone: '13800138000',
 *         createTime: '2024-09-01T00:00:00'
 *       }],
 *       total: 50
 *     }
 *   }
 *
 * TeacherVO 的字段来源:
 *   所有字段都来自 teacher 表（没有 JOIN 其他表）
 *   TeacherVO 比 TeacherDTO 多了: id, createTime
 *   TeacherVO 没有额外联表的字段（如院系名本身就在 teacher 表中）
 *
 *   注意：教师查询不需要 JOIN 其他表，因为：
 *     - 教师是独立的实体（不像学生属于班级、课程属于教师）
 *     - 教师的所有信息都可以从 teacher 表直接获取
 *     - 不像 StudentVO 需要 JOIN class_info 取班级名
 *
 * JS 类比:
 *   // 前端表格列定义
 *   <el-table :data="teacherList">
 *     <el-table-column prop="teacherNo" label="工号" />
 *     <el-table-column prop="name" label="姓名" />
 *     <el-table-column prop="gender" label="性别" />
 *     <el-table-column prop="title" label="职称" />
 *     <el-table-column prop="department" label="院系" />
 *     <el-table-column prop="phone" label="电话" />
 *     <el-table-column prop="createTime" label="入职时间" />
 *   </el-table>
 *
 *   // TypeScript 类型
 *   interface TeacherVO {
 *     id: number;
 *     teacherNo: string;
 *     name: string;
 *     gender: string;
 *     title: string;
 *     department: string;
 *     phone: string;
 *     createTime: string;
 *   }
 * ================================================================
 */

// ==================== 1. 包声明 ====================

/*
 * package com.example.backend.vo;
 *   声明当前文件属于 vo 包。
 */
package com.example.backend.vo; // 声明 VO 包

// ==================== 2. 导入其他类（import） ====================

/*
 * import lombok.Data;
 *   Lombok 注解：自动生成 getter/setter 方法。
 *   TeacherVO 有 8 个字段，@Data 一行注解搞定。
 */
import lombok.Data; // Lombok 注解：自动生成 getter/setter/toString/equals/hashCode

/*
 * import java.time.LocalDateTime;
 *   Java 8 时间 API 的日期+时间类型。
 *   用于 createTime 字段，Jackson 自动序列化成 ISO 8601 格式。
 */
import java.time.LocalDateTime; // Java 8 时间 API

// ==================== 3. 类的声明和注解 ====================

/*
 * @Data
 *   自动生成 8 个字段的全部 getter/setter/toString/equals/hashCode。比如手写节约很多代码。
 */
@Data // 自动生成 Getter/Setter/toString/equals/hashCode

/*
 * public class TeacherVO { ... }
 *
 *   Teacher ← 业务领域：教师
 *   VO      ← 类型：视图对象
 */
public class TeacherVO {

    // ==================== 4. 字段声明 ====================

    /*
     * private Long id;
     *
     *   用途:       教师 ID（数据库主键）
     *   数据来源:   teacher 表的 id 列（BIGINT AUTO_INCREMENT）
     *   前端使用:   编辑/删除教师时获取行数据 ID
     *   示例值:     5L
     */
    private Long id; // 教师 ID（数据库主键）

    // ================================================================

    /*
     * private String teacherNo;
     *
     *   用途:       教师工号（业务编号）
     *   数据来源:   teacher 表的 teacher_no 列
     *   前端显示:   教师列表的"工号"列
     *   示例值:     "T2024001"
     *
     *   工号 vs ID:
     *     id         —— 数据库自动生成（1, 2, 3...）
     *     teacherNo  —— 有编码规则的业务编号（如 "T2024001"）
     */
    private String teacherNo; // 教师工号（业务编号）

    // ================================================================

    /*
     * private String name;
     *
     *   用途:       教师姓名
     *   数据来源:   teacher 表的 name 列
     *   前端显示:   教师列表的"姓名"列
     *   示例值:     "张教授"
     */
    private String name; // 教师姓名

    // ================================================================

    /*
     * private String gender;
     *
     *   用途:       性别
     *   数据来源:   teacher 表的 gender 列
     *   前端显示:   教师列表的"性别"列
     *   可选值:     "男"、"女"
     *
     *   和 TeacherDTO 的 gender 一致，用 String 类型。
     */
    private String gender; // 性别

    // ================================================================

    /*
     * private String title;
     *
     *   用途:       职称（学术头衔）
     *   数据来源:   teacher 表的 title 列
     *   前端显示:   教师列表的"职称"列
     *   可选值:     "教授"、"副教授"、"讲师"、"助教"
     *   示例值:     "教授"
     *
     *   前端可以根据职称用不同的颜色/标签展示：
     *     教授 → 金色标签
     *     副教授 → 蓝色标签
     *     讲师 → 绿色标签
     *     助教 → 灰色标签
     */
    private String title; // 职称

    // ================================================================

    /*
     * private String department;
     *
     *   用途:       所属院系
     *   数据来源:   teacher 表的 department 列
     *   前端显示:   教师列表的"院系"列
     *   示例值:     "计算机学院"
     *
     *   和 TeacherDTO 不同的一点是 VO 的 department 字段可能经过处理后返回。
     *   但当前项目直接映射数据库原值。
     */
    private String department; // 院系

    // ================================================================

    /*
     * private String phone;
     *
     *   用途:       联系电话
     *   数据来源:   teacher 表的 phone 列
     *   前端显示:   教师列表的"电话"列
     *   示例值:     "13800138000"
     *
     *   安全提示: 电话号码是个人敏感信息，生产环境应做好权限控制。
     */
    private String phone; // 联系电话

    // ================================================================

    /*
     * private LocalDateTime createTime;
     *
     *   用途:       教师记录创建时间（可以是入职时间）
     *   数据来源:   teacher 表的 create_time 列
     *   前端显示:   教师列表的"入职时间"列（也可以叫"创建时间"）
     *   序列化格式: "2024-09-01T00:00:00"
     *
     *   注意：TeacherDTO 没有 createTime 字段，
     *   因为创建时间由数据库自动填充，前端不需要传。
     */
    private LocalDateTime createTime; // 入职时间（数据记录创建时间）
}
