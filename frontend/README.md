# 学生教育管理系统 - 前端

基于 **Vue 3** + **Vite** + **Element Plus** 构建的后台管理系统前端。

## 技术栈

| 技术 | 说明 |
|------|------|
| Vue 3 | 渐进式 JavaScript 框架（Composition API） |
| Vite | 前端构建工具 |
| Element Plus | UI 组件库 |
| Vue Router | 路由管理 |
| Pinia | 状态管理 |
| Axios | HTTP 请求 |

## 项目结构

```
frontend/
├── index.html              # HTML 入口
├── vite.config.js          # Vite 配置（含 API 代理）
├── src/
│   ├── main.js             # 应用入口
│   ├── App.vue             # 根组件
│   ├── router/index.js     # 路由配置
│   ├── stores/auth.js      # 认证状态管理
│   ├── api/                # API 接口封装
│   │   ├── request.js      # Axios 实例（拦截器）
│   │   ├── auth.js         # 登录注册
│   │   ├── student.js      # 学生管理
│   │   ├── teacher.js      # 教师管理
│   │   ├── class.js        # 班级管理
│   │   ├── course.js       # 课程管理
│   │   ├── score.js        # 成绩管理
│   │   └── enrollment.js   # 选课管理
│   └── views/              # 页面组件
│       ├── Login.vue       # 登录页
│       ├── Register.vue    # 注册页
│       ├── Dashboard.vue   # 首页仪表盘
│       ├── student/        # 学生管理
│       ├── teacher/        # 教师管理
│       ├── class/          # 班级管理
│       ├── course/         # 课程管理
│       ├── score/          # 成绩管理
│       └── enrollment/     # 选课管理
```

## 快速开始

### 安装依赖

```bash
cd frontend
npm install
```

### 启动开发服务器

```bash
npm run dev
```

默认访问：http://localhost:5173

### 构建生产版本

```bash
npm run build
```

## Vite 代理配置

开发环境下，前端请求 `/api` 路径会自动代理到后端：

```
/api/** → http://localhost:8088/api/**
```

无需额外配置跨域。

## 页面功能

| 页面 | 功能说明 |
|------|----------|
| 登录 | 用户名密码登录，角色鉴权 |
| 注册 | 新用户注册（默认学生角色） |
| 仪表盘 | 系统概览 |
| 学生管理 | 学生信息增删改查 |
| 教师管理 | 教师信息增删改查 |
| 班级管理 | 班级信息管理 |
| 课程管理 | 课程信息管理 |
| 成绩管理 | 成绩录入与查看 |
| 选课管理 | 学生选课与退课 |

## 认证流程

1. 用户登录 → 后端返回 JWT Token
2. Token 存入 localStorage
3. Axios 拦截器自动在请求头加上 `Authorization: Bearer <token>`
4. Token 过期自动跳转登录页
