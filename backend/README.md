# 学生教育管理系统 - 后端

基于 **Spring Boot 3.4.5** + **MyBatis-Plus 3.5.6** 的 RESTful API 后端服务。

## 技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| Java | 21 | 运行环境 |
| Spring Boot | 3.4.5 | 核心框架 |
| MyBatis-Plus | 3.5.6 | ORM 框架 |
| Spring Security | 6.4.5 | 安全认证 |
| JWT (jjwt) | 0.12.5 | Token 认证 |
| SpringDoc OpenAPI | 2.7.0 | 接口文档 |
| MySQL | 8.0 | 数据库 |
| HikariCP | - | 数据库连接池 |
| Lombok | 1.18.36 | 代码简化 |
| Hutool | 5.8.28 | 工具库 |

## 项目结构

```
backend/
├── pom.xml                          # Maven 依赖配置
├── src/main/java/com/example/backend/
│   ├── BackendApplication.java      # 应用主入口
│   ├── common/                      # 公共类
│   │   ├── Result.java              # 统一响应格式
│   │   ├── ResultCode.java          # 状态码枚举
│   │   └── BusinessException.java   # 业务异常
│   ├── config/                      # 配置类
│   │   └── MyBatisPlusConfig.java   # MyBatis-Plus 分页插件
│   ├── controller/                  # 控制器层（接口）
│   │   ├── AuthController.java      # 登录注册
│   │   ├── StudentController.java   # 学生管理
│   │   ├── TeacherController.java   # 教师管理
│   │   ├── ClassController.java     # 班级管理
│   │   ├── CourseController.java    # 课程管理
│   │   ├── ScoreController.java     # 成绩管理
│   │   └── EnrollmentController.java # 选课管理
│   ├── dto/                         # 数据传输对象
│   ├── entity/                      # 数据库实体
│   ├── handler/                     # 全局异常处理
│   ├── init/                        # 数据初始化（测试数据）
│   ├── mapper/                      # MyBatis Mapper 接口
│   ├── security/                    # 安全配置
│   │   ├── SecurityConfig.java      # Spring Security 配置
│   │   ├── JwtUtil.java             # JWT 工具类
│   │   ├── JwtAuthenticationFilter.java # JWT 过滤器
│   │   └── UserDetailsServiceImpl.java  # 用户详情服务
│   ├── service/                     # 服务层（业务逻辑）
│   └── vo/                          # 视图对象（返回给前端）
└── src/main/resources/
    ├── application.yml              # 应用配置
    └── schema.sql                   # 数据库建表脚本
```

## 数据库表

| 表名 | 说明 | 主要字段 |
|------|------|----------|
| sys_user | 系统用户 | username, password, role, real_name |
| student | 学生信息 | student_no, name, gender, age, class_id |
| teacher | 教师信息 | teacher_no, name, title, email |
| class_info | 班级信息 | class_name, grade, head_teacher_id |
| course | 课程信息 | course_no, name, credit, teacher_id, classroom |
| course_enrollment | 选课记录 | student_id, course_id |
| score | 成绩记录 | student_id, course_id, score, exam_type |

## 快速开始

### 环境要求

- JDK 21+
- Maven 3.9+
- MySQL 8.0+

### 1. 创建数据库

MySQL 连接信息（已配置在 `application.yml`）：
```
地址: 127.0.0.1:3306
数据库: learning（应用启动时自动创建）
用户名: root
密码: skydave2020DH
```

### 2. 启动应用

```bash
cd backend
mvn spring-boot:run
```

应用启动后：
- 自动执行 `schema.sql` 创建数据库表
- 自动插入测试数据（首次启动时）
- 监听端口：**8088**

### 3. 接口文档

启动后访问：http://localhost:8088/swagger-ui.html

## API 模块

| 模块 | 前缀 | 说明 |
|------|------|------|
| 认证管理 | `/api/auth` | 登录、注册 |
| 学生管理 | `/api/students` | 学生 CRUD |
| 教师管理 | `/api/teachers` | 教师 CRUD |
| 班级管理 | `/api/classes` | 班级 CRUD |
| 课程管理 | `/api/courses` | 课程 CRUD |
| 成绩管理 | `/api/scores` | 成绩录入与查询 |
| 选课管理 | `/api/enrollments` | 选课与退课 |

## 认证方式

所有 API 请求需在 Header 中携带 JWT Token：

```
Authorization: Bearer <token>
```

登录接口 `/api/auth/login` 返回 Token，无需认证。

## 权限角色

| 角色 | 权限 |
|------|------|
| ADMIN | 所有操作 |
| TEACHER | 查看学生/班级/课程，管理成绩 |
| STUDENT | 查看课程，选课退课，查看成绩 |
