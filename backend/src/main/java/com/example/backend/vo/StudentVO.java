/*
 * ================================================================
 * 文件位置:  src/main/java/com/example/backend/vo/StudentVO.java
 * 对应前端:  frontend/src/views/student/StudentList.vue（学生列表页面）
 *           frontend/src/views/student/StudentDetail.vue（学生详情页面）
 *           frontend/src/api/student.js（前端 API 调用）
 *
 * 数据流向（以查询学生列表为例）:
 *   前端请求 GET /api/students/page
 *     ↓ StudentController → StudentService
 *   StudentService 执行 SQL:
 *     SELECT s.*, c.class_name AS class_name
 *     FROM student s
 *     LEFT JOIN class_info c ON s.class_id = c.id
 *     ↓ 返回 StudentVO 列表 → JSON 序列化
 *   前端收到: {
 *     code: 200,
 *     data: {
 *       records: [{
 *         id: 12, studentNo: 'S2024001', name: '张三', gender: '男',
 *         age: 20, phone: '13800138000', email: 'zhangsan@example.com',
 *         classId: 3, className: '软件工程1班', createTime: '2024-09-01T00:00:00'
 *       }],
 *       total: 100
 *     }
 *   }
 *
 * StudentVO 的字段来源:
 *   来自 student 表:  id, studentNo, name, gender, age, phone, email, classId, createTime
 *   来自 class_info 表: className（LEFT JOIN class_info ON class_id = id）
 *   StudentVO 比 StudentDTO 多了: id, className, createTime
 *
 * JS 类比:
 *   // 前端表格列定义
 *   <el-table :data="studentList">
 *     <el-table-column prop="studentNo" label="学号" />
 *     <el-table-column prop="name" label="姓名" />
 *     <el-table-column prop="gender" label="性别" />
 *     <el-table-column prop="age" label="年龄" />
 *     <el-table-column prop="className" label="班级" />
 *     <el-table-column prop="phone" label="手机" />
 *     <el-table-column prop="email" label="邮箱" />
 *     <el-table-column prop="createTime" label="创建时间" />
 *   </el-table>
 *
 *   // TypeScript 类型
 *   interface StudentVO {
 *     id: number;
 *     studentNo: string;
 *     name: string;
 *     gender: string;
 *     age: number;
 *     phone: string;
 *     email: string;
 *     classId: number;
 *     className: string;      // 联表查询得到的班级名称
 *     createTime: string;
 *   }
 * ================================================================
 */

// ==================== 1. 包声明 ====================

/*
 * package com.example.backend.vo;
 *   声明当前文件属于 vo 包。
 *   所有视图对象都在这个包下，职责是向后传递给前端。
 */
package com.example.backend.vo; // 声明 VO 包

// ==================== 2. 导入其他类（import） ====================

/*
 * import lombok.Data;
 *   Lombok 注解：自动生成 getter/setter 方法。
 *   StudentVO 有 10 个字段，@Data 自动生成对应的 getter/setter。
 */
import lombok.Data; // Lombok 注解：自动生成 getter/setter/toString/equals/hashCode

/*
 * import java.time.LocalDateTime;
 *   用于 createTime 字段，表示学生记录创建时间。
 *   Jackson 序列化后前端收到的是 ISO 8601 格式字符串。
 */
import java.time.LocalDateTime; // Java 8 时间 API

// ==================== 3. 类的声明和注解 ====================

/*
 * @Data
 *   自动生成 10 个字段的所有 getter/setter 方法。
 */
@Data // 自动生成 Getter/Setter/toString/equals/hashCode

/*
 * public class StudentVO { ... }
 *
 *   Student ← 业务领域：学生
 *   VO      ← 类型：视图对象
 */
public class StudentVO {

    // ==================== 4. 字段声明 ====================

    /*
     * private Long id;
     *
     *   用途:       学生 ID（主键）
     *   数据来源:   student 表的 id 列（BIGINT AUTO_INCREMENT）
     *   前端使用:   点击"编辑"/"删除"按钮时获取当前行的学生 ID
     *   示例值:     12L
     */
    private Long id; // 学生 ID（数据库主键）

    // ================================================================

    /*
     * private String studentNo;
     *
     *   用途:       学号
     *   数据来源:   student 表的 student_no 列
     *   前端显示:   学生列表的"学号"列
     *   示例值:     "S2024001"
     */
    private String studentNo; // 学号

    // ================================================================

    /*
     * private String name;
     *
     *   用途:       学生姓名
     *   数据来源:   student 表的 name 列
     *   前端显示:   学生列表的"姓名"列
     *   示例值:     "张三"
     *
     *   在 VO 中，name 是暴露给前端的最终展示值，不经过加工。
     *   但前端可以用这个值做链接跳转（如点击姓名进入学生详情页）。
     */
    private String name; // 姓名

    // ================================================================

    /*
     * private String gender;
     *
     *   用途:       性别
     *   数据来源:   student 表的 gender 列
     *   前端显示:   学生列表的"性别"列
     *   可选值:     "男"、"女"
     *
     *   前端可以用不同颜色展示:
     *     "男" → 蓝色标签， "女" → 粉色标签
     */
    private String gender; // 性别

    // ================================================================

    /*
     * private Integer age;
     *
     *   用途:       年龄
     *   数据来源:   student 表的 age 列
     *   前端显示:   学生列表的"年龄"列
     *   示例值:     20
     *
     *   注意：和 StudentDTO 一样，这里存的是年龄而不是出生日期。
     *   在实际项目中，回到 VO 层也可以做动态计算。
     *   但当前项目直接从数据库查 age 列，不做处理。
     */
    private Integer age; // 年龄

    // ================================================================

    /*
     * private String phone;
     *
     *   用途:       手机号码
     *   数据来源:   student 表的 phone 列
     *   前端显示:   学生列表的"手机"列（通常不突出显示，隐私信息）
     *   示例值:     "13800138000"
     *
     *   安全提示:
     *     手机和邮箱属于个人隐私信息，生产环境中需要：
     *     1. 脱敏显示（如 "138****8000"）
     *     2. 权限控制（只有管理员和班主任能看到）
     *     3. 传输加密（HTTPS）
     */
    private String phone; // 手机号

    // ================================================================

    /*
     * private String email;
     *
     *   用途:       电子邮箱
     *   数据来源:   student 表的 email 列
     *   前端显示:   学生列表的"邮箱"列
     *   示例值:     "zhangsan@example.com"
     *
     *   前端可以用 mailto: 链接：
     *     <a :href="`mailto:${row.email}`">{{ row.email }}</a>
     *   点击后自动打开系统默认邮件客户端。
     */
    private String email; // 邮箱

    // ================================================================

    /*
     * private Long classId;
     *
     *   用途:       所属班级 ID
     *   数据来源:   student 表的 class_id 列
     *   前端使用:   如果需要跳转到班级详情，用这个 ID
     *              <router-link :to="`/class/${row.classId}`">
     *   示例值:     3L
     *
     *   注意：虽然前端一般展示 className（班级名称），但保留 classId
     *   是为了前端做链接跳转等需要 ID 的操作。
     */
    private Long classId; // 班级 ID

    // ================================================================

    /*
     * private String className;
     *
     *   用途:       班级名称
     *   数据来源:   不是 student 表的字段！
     *              通过 SQL LEFT JOIN class_info 表查询得到：
     *              LEFT JOIN class_info c ON s.class_id = c.id → c.class_name AS class_name
     *   前端显示:   学生列表的"班级"列
     *   示例值:     "软件工程1班"
     *
     *   这是 StudentVO 和 StudentDTO 的关键区别：
     *     StudentDTO 没有 className —— 前端只需要传 classId
     *     StudentVO 有 className —— 前端需要展示班级名称
     *   同样的，Student（Entity）也没有 className，Entity 和数据库表精确对应。
     */
    private String className; // 班级名称（通过 LEFT JOIN 查询得到）

    // ================================================================

    /*
     * private LocalDateTime createTime;
     *
     *   用途:       学生记录创建时间
     *   数据来源:   student 表的 create_time 列
     *   前端显示:   学生列表的"创建时间"列
     *   序列化格式: "2024-09-01T00:00:00"
     *
     *   Jackson 序列化 LocalDateTime 的行为:
     *     Java 对象: LocalDateTime.of(2024, 9, 1, 10, 30, 0)
     *     JSON 输出: "2024-09-01T10:30:00"
     *     如果没有特殊配置，Jackson 会用这个默认的 ISO 8601 格式。
     *     如果需要自定义格式（如 "2024-09-01 10:30:00"），
     *     可以在 application.yml 中配置 spring.jackson.date-format。
     */
    private LocalDateTime createTime; // 创建时间
}
