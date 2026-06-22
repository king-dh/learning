-- ============================================================
-- 学生教育管理系统 - 数据库建表脚本
-- Spring Boot 启动时自动执行（spring.sql.init.mode=always）
-- ============================================================

-- 1. 系统用户表（存储登录用户和角色信息）
CREATE TABLE IF NOT EXISTS sys_user (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '用户ID，主键自增',
    username    VARCHAR(50)  NOT NULL UNIQUE COMMENT '登录用户名，唯一',
    password    VARCHAR(200) NOT NULL COMMENT 'BCrypt加密后的密码',
    role        VARCHAR(20)  NOT NULL COMMENT '角色：ADMIN/TEACHER/STUDENT',
    real_name   VARCHAR(50)  COMMENT '真实姓名',
    status      INT DEFAULT 1 COMMENT '状态：1启用 0禁用',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统用户表';

-- 2. 教师表
CREATE TABLE IF NOT EXISTS teacher (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '教师ID',
    teacher_no  VARCHAR(20) NOT NULL UNIQUE COMMENT '教师工号',
    name        VARCHAR(50) NOT NULL COMMENT '教师姓名',
    gender      VARCHAR(10) COMMENT '性别',
    title       VARCHAR(50) COMMENT '职称（教授/副教授/讲师等）',
    department  VARCHAR(100) COMMENT '所属院系',
    phone       VARCHAR(20) COMMENT '联系电话',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='教师表';

-- 3. 班级表
CREATE TABLE IF NOT EXISTS class_info (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '班级ID',
    class_name       VARCHAR(100) NOT NULL COMMENT '班级名称',
    grade            VARCHAR(20)  COMMENT '年级',
    head_teacher_id  BIGINT COMMENT '班主任教师ID',
    create_time      DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (head_teacher_id) REFERENCES teacher(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='班级表';

-- 4. 学生表
CREATE TABLE IF NOT EXISTS student (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '学生ID',
    student_no  VARCHAR(20) NOT NULL UNIQUE COMMENT '学号，唯一',
    name        VARCHAR(50) NOT NULL COMMENT '学生姓名',
    gender      VARCHAR(10) COMMENT '性别',
    age         INT COMMENT '年龄',
    phone       VARCHAR(20) COMMENT '联系电话',
    email       VARCHAR(100) COMMENT '电子邮箱',
    class_id    BIGINT COMMENT '所属班级ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (class_id) REFERENCES class_info(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学生表';

-- 5. 课程表
CREATE TABLE IF NOT EXISTS course (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '课程ID',
    course_no   VARCHAR(20) NOT NULL UNIQUE COMMENT '课程编号',
    name        VARCHAR(100) NOT NULL COMMENT '课程名称',
    credit      DOUBLE COMMENT '学分',
    teacher_id  BIGINT COMMENT '授课教师ID',
    semester    VARCHAR(20) COMMENT '学期（如2024-2025-1）',
    description TEXT COMMENT '课程描述',
    classroom   VARCHAR(50) COMMENT '上课教室',
    max_students INT COMMENT '最大容纳学生数',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (teacher_id) REFERENCES teacher(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课程表';

-- 6. 成绩表
CREATE TABLE IF NOT EXISTS score (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '成绩ID',
    student_id  BIGINT NOT NULL COMMENT '学生ID',
    course_id   BIGINT NOT NULL COMMENT '课程ID',
    score       DOUBLE COMMENT '分数（0-100）',
    semester    VARCHAR(20) COMMENT '学期',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (student_id) REFERENCES student(id) ON DELETE CASCADE,
    FOREIGN KEY (course_id) REFERENCES course(id) ON DELETE CASCADE,
    UNIQUE KEY uk_student_course_semester (student_id, course_id, semester)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='成绩表';

-- 7. 选课表
CREATE TABLE IF NOT EXISTS course_enrollment (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '选课记录ID',
    student_id  BIGINT NOT NULL COMMENT '学生ID',
    course_id   BIGINT NOT NULL COMMENT '课程ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '选课时间',
    FOREIGN KEY (student_id) REFERENCES student(id) ON DELETE CASCADE,
    FOREIGN KEY (course_id) REFERENCES course(id) ON DELETE CASCADE,
    UNIQUE KEY uk_student_course (student_id, course_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='选课表';
