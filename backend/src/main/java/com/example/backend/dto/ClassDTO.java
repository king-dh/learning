/*
 * ================================================================
 * 文件位置:  src/main/java/com/example/backend/dto/ClassDTO.java
 * 对应前端:  frontend/src/views/class/ClassForm.vue（班级新增/编辑表单页）
 *           frontend/src/api/class.js（前端 API 调用）
 *
 * 数据流向（以新增班级为例）:
 *   用户在 ClassForm.vue 填写班级名称、年级、班主任
 *     ↓ 点击"保存"按钮
 *   axios.post('/api/classes', { className: '软件工程1班', grade: '2024级', headTeacherId: 3 })
 *     ↓ Vite 代理转发到 Spring Boot
 *   ClassController.save() 方法收到请求
 *   Spring 用 Jackson 把请求体 JSON 自动转换成 ClassDTO 对象
 *     ↓ 请求体 JSON: { "className": "软件工程1班", "grade": "2024级", "headTeacherId": 3 }
 *     ↓ Jackson 转换
 *     ↓ Java 对象:  ClassDTO { className="软件工程1班", grade="2024级", headTeacherId=3 }
 *   ClassController → ClassService → ClassMapper → INSERT INTO class_info ...
 *     ↓ 返回成功
 *   前端收到 { code: 200, message: "操作成功", data: null }
 *
 * DTO 是什么？（JS 类比）:
 *   在 JavaScript/TypeScript 中，如果你用 React + TypeScript 写表单：
 *     interface CreateClassProps {
 *       className: string;
 *       grade: string;
 *       headTeacherId: number | null;
 *     }
 *   ClassDTO 就是 Java 版本的"表单数据接口"，专门定义前端传给后端的 JSON 结构。
 *   DTO = Data Transfer Object = 数据传输对象，只负责"搬运数据"，不含业务逻辑。
 *
 * DTO vs Entity（实体类）的区别:
 *   - Entity（如 ClassInfo.java）: 和数据库表结构一一映射，有 @TableName 注解
 *   - DTO（如 ClassDTO.java）:    只包含前端需要提交的字段，可能有校验注解，和数据库表无关
 *   - 前端绝不应该直接传 Entity 给后端（暴露数据库结构，不安全）
 * ================================================================
 */

// ==================== 1. 包声明 ====================

/*
 * package 关键字：声明当前文件属于 dto 子包。
 *
 * 完整路径对应:
 *   com.example.backend.dto  →  com/example/backend/dto/  ← 文件夹路径
 *
 * 和 JavaScript 的类比:
 *   // JavaScript (ES Module)
 *   // 文件在 models/dto/ClassDTO.js
 *   // import 其他文件时: import { ClassDTO } from './dto/ClassDTO.js'
 *
 *   // Java
 *   // 文件在 dto/ClassDTO.java
 *   // package com.example.backend.dto; ← 声明自己在这个包下
 *   // 其他文件引用时: import com.example.backend.dto.ClassDTO;
 */
package com.example.backend.dto; // 声明 DTO 包，所有数据传输对象都放在这里

// ==================== 2. 导入其他类（import） ====================

/*
 * import 关键字：告诉编译器"我要用别的文件里的类"。
 * 和 JavaScript 的 import { xxx } from './xxx' 概念完全一样。
 * 区别：Java 用的是完整包路径，不是相对路径。
 */

// --- 2.1 Lombok 注解 ---
/*
 * lombok 是什么？
 *   Lombok 是一个 Java 编译期代码生成工具（不是运行时框架）。
 *   就像你在 VS Code 里装了一个插件，保存文件时自动帮你生成 getter/setter 方法。
 *   Lombok 通过注解（@Data 这类 @打头的标记）告诉编译器"请帮我生成这些代码"。
 *   编译后 .class 文件里已经有了完整的方法，所以运行时不需要 Lombok。
 *
 *   为什么用 Lombok？
 *     没有 Lombok 的话，一个 3 个字段的类需要写 getClassName()、setClassName()、
 *     getGrade()、setGrade()、getHeadTeacherId()、setHeadTeacherId()、toString()、
 *     equals()、hashCode()... 将近 100 行代码。
 *     有了 @Data，一行注解搞定所有，保持代码简洁。
 *
 *   Lombok 依赖在哪里配置的？
 *     pom.xml（Maven 的依赖配置文件）里引入了 lombok 依赖，编译时会自动生效。
 *     不需要额外安装任何东西，IDE 装个 Lombok 插件只是为了消除编辑器的"找不到方法"误报。
 */
import lombok.Data; // @Data 注解：一键生成 getter、setter、toString、equals、hashCode 方法

// ==================== 3. 类的声明和注解 ====================

/*
 * @Data
 *   Lombok 最常用的组合注解，编译时自动生成以下所有方法（不需要手动写）：
 *
 *   ① Getter 方法（每个字段都有一个 get 方法）:
 *      例如 private String className; → 自动生成:
 *        public String getClassName() { return this.className; }  // "get" + 字段名首字母大写
 *
 *   ② Setter 方法（每个字段都有一个 set 方法）:
 *      例如 private String className; → 自动生成:
 *        public void setClassName(String className) { this.className = className; }
 *
 *   ③ toString() 方法:
 *      自动生成类似 class ClassDTO { className="软件工程1班", grade="2024级", headTeacherId=3 }
 *      用于打印日志或调试时查看对象内容。没有 toString() 的话打印出来是一串内存地址。
 *
 *   ④ equals(Object o) 方法:
 *      比较两个 ClassDTO 对象是否"内容相等"（字段值一样就认为相等）。
 *      没有 equals() 的话，两个内容完全相同的对象用 == 比较会是 false（比较的是内存地址）。
 *
 *   ⑤ hashCode() 方法:
 *      根据字段值计算哈希码，HashSet/HashMap 等集合会用到。
 *      约定：如果 equals() 返回 true，hashCode() 必须相等。
 *
 *   编译前（源码）:
 *     @Data
 *     public class ClassDTO {
 *         private String className;
 *         private String grade;
 *         private Long headTeacherId;
 *     }
 *
 *   编译后（实际 .class 文件里会是）:
 *     public class ClassDTO {
 *         private String className;
 *         private String grade;
 *         private Long headTeacherId;
 *
 *         // --- 以下全由 Lombok 自动生成，你不需手写 ---
 *         public String getClassName() { return this.className; }
 *         public void setClassName(String className) { this.className = className; }
 *         public String getGrade() { return this.grade; }
 *         public void setGrade(String grade) { this.grade = grade; }
 *         public Long getHeadTeacherId() { return this.headTeacherId; }
 *         public void setHeadTeacherId(Long headTeacherId) { this.headTeacherId = headTeacherId; }
 *         public String toString() { return "ClassDTO(className=" + this.className + ", grade=" + this.grade + ", headTeacherId=" + this.headTeacherId + ")"; }
 *         public boolean equals(Object o) { ... }
 *         public int hashCode() { ... }
 *     }
 *
 *   JS/TS 类比:
 *     // JavaScript 不需要这些，因为 JS 对象天生就是动态的
 *     const dto = { className: '软件工程1班', grade: '2024级', headTeacherId: 3 }
 *     dto.className // 直接访问属性
 *     // 但在 Java 里必须通过 getClassName() / setClassName() 来读写私有字段（封装原则）
 *     // Lombok 就是帮你自动写这些 get/set 方法的工具
 */
@Data // 自动生成 Getter/Setter/toString/equals/hashCode

/*
 * public class ClassDTO { ... }
 *
 *   public  —— 访问修饰符：这个类可以被任何其他类使用（类比 JS 的 export class）
 *   class  —— 定义类的关键字
 *   ClassDTO —— 类名，必须和文件名完全一致（ClassDTO.java → class ClassDTO）
 *              命名规范：DTO 后缀表示这是一个数据传输对象
 *   { ... } —— 类体：包含字段（成员变量），没有方法（因为有 Lombok 自动生成）
 */
public class ClassDTO {

    // ==================== 4. 字段声明 ====================

    /*
     * private String className;
     *
     *   分解解释:
     *     private   —— 访问修饰符：这个字段只能在本类内部访问，外部必须通过 getter/setter
     *                 Java 的封装原则：字段私有，方法公开
     *                 JS 类比: #className（ES2020 私有字段）
     *     String    —— 数据类型：Java 的字符串类型（引用类型，不是基本类型）
     *                 JS 类比: string
     *                 MySQL 对应: VARCHAR
     *     className —— 字段名：驼峰命名（首字母小写），对应数据库列名 class_name
     *                 MyBatis-Plus 默认下划线转换规则：Java 驼峰 ↔ 数据库下划线
     *                 className ↔ class_name
     *
     *   数据来源:     前端 ClassForm.vue 表单中的"班级名称"输入框
     *   数据库对应列:  class_info 表的 class_name 列
     *   前端使用场景:  新增/编辑班级时，用户在表单中填写班级名称
     *   示例值:       "软件工程1班"、"计算机科学2班"
     */
    private String className; // 班级名称，如 "软件工程1班"

    // ================================================================

    /*
     * private String grade;
     *
     *   分解解释:
     *     private   —— 封装：外部不能直接 dto.grade 访问
     *     String    —— 字符串类型
     *     grade     —— 年级，如 "2024级"
     *
     *   数据来源:     前端 ClassForm.vue 表单中的"年级"选择/输入框
     *   数据库对应列:  class_info 表的 grade 列
     *   前端使用场景:  新增/编辑班级时指定年级；列表页可按年级筛选
     *   示例值:       "2024级"、"2023级"、"2022级"
     *
     *   JS 类比:
     *     // 前端表单数据
     *     const formData = {
     *       className: '软件工程1班',
     *       grade: '2024级',     // ← 这个字段
     *       headTeacherId: 3
     *     }
     */
    private String grade; // 所属年级，如 "2024级"

    // ================================================================

    /*
     * private Long headTeacherId;
     *
     *   分解解释:
     *     private       —— 封装
     *     Long          —— Java 的 64 位整数包装类型（注意：大写 L，是对象类型）
     *                     和 long（小写，基本类型）的区别：
     *                       Long  可以 null（对应数据库可为空的 BIGINT 列）
     *                       long  不能 null（默认值 0，分不清"没填"和"填了 0"）
     *                     这里用 Long 是因为班主任可以为空（新班级可能还没分配班主任）
     *                     JS 类比: number | null
     *                     MySQL 对应: BIGINT
     *     headTeacherId —— 字段名，驼峰命名
     *                     数据库对应列: head_teacher_id（head → teacher → id）
     *
     *   数据来源:     前端 ClassForm.vue 表单中的"班主任"下拉选择框
     *                下拉框显示的是教师姓名，但提交的是教师 ID
     *                例如用户在"班主任"下拉选中"张老师" → 实际提交 headTeacherId = 3
     *   数据库对应列:  class_info 表的 head_teacher_id 列（BIGINT 类型，可为 null）
     *   前端使用场景:  新增/编辑班级时选择班主任；列表页显示班主任姓名
     *   示例值:       3L（Java 中 Long 类型的字面量要加 L 后缀）
     *
     *   为什么存 ID 而不是姓名？
     *     - 数据库设计原则：用 ID 建立关联（外键），而不是存姓名
     *     - 如果存姓名，教师改名了，班级表里的姓名也忘了改 → 数据不一致
     *     - 存 ID，每次查班级时 LEFT JOIN teacher 表取名字 → 永远是最新的名字
     *
     *   JS 类比:
     *     // 前端下拉框组件（Element Plus / Ant Design）
     *     <el-select v-model="formData.headTeacherId">
     *       <el-option v-for="t in teacherList" :key="t.id" :label="t.name" :value="t.id" />
     *     </el-select>
     *     // 用户看到的是"张老师"（label），提交的是 3（value/ID）
     */
    private Long headTeacherId; // 班主任教师 ID，可为 null（表示未分配班主任）
}
