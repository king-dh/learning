<template>
  <el-container class="layout-container">
    <el-aside width="220px" class="layout-aside">
      <div class="logo">
        <h2>教育管理系统</h2>
      </div>
      <el-menu
        :default-active="activeMenu"
        router
        background-color="#304156"
        text-color="#bfcbd9"
        active-text-color="#409eff"
      >
        <el-menu-item index="/dashboard">
          <el-icon><Odometer /></el-icon>
          <span>仪表盘</span>
        </el-menu-item>

        <el-menu-item v-if="userStore.isAdmin" index="/students">
          <el-icon><User /></el-icon>
          <span>学生管理</span>
        </el-menu-item>

        <el-menu-item v-if="userStore.isAdmin" index="/teachers">
          <el-icon><UserFilled /></el-icon>
          <span>教师管理</span>
        </el-menu-item>

        <el-menu-item v-if="userStore.isAdmin" index="/classes">
          <el-icon><School /></el-icon>
          <span>班级管理</span>
        </el-menu-item>

        <el-menu-item v-if="userStore.isAdmin || userStore.isTeacher" index="/courses">
          <el-icon><Reading /></el-icon>
          <span>课程管理</span>
        </el-menu-item>

        <el-menu-item v-if="userStore.isAdmin || userStore.isTeacher || userStore.isStudent" index="/scores">
          <el-icon><Trophy /></el-icon>
          <span>成绩管理</span>
        </el-menu-item>

        <el-menu-item v-if="userStore.isStudent" index="/enrollments">
          <el-icon><Notebook /></el-icon>
          <span>选课管理</span>
        </el-menu-item>
      </el-menu>
    </el-aside>

    <el-container>
      <el-header class="layout-header">
        <div class="header-left">
          <span class="welcome-text">欢迎，{{ userStore.userInfo?.realName || '用户' }}</span>
        </div>
        <div class="header-right">
          <el-tag type="info" style="margin-right: 16px">
            {{ roleText }}
          </el-tag>
          <el-button type="danger" size="small" @click="handleLogout">退出登录</el-button>
        </div>
      </el-header>

      <el-main class="layout-main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import {
  Odometer, User, UserFilled, School, Reading, Trophy, Notebook
} from '@element-plus/icons-vue'
import { useUserStore } from '../stores/user'

const route = useRoute()
const userStore = useUserStore()

const activeMenu = computed(() => route.path)

const roleText = computed(() => {
  const role = userStore.userInfo?.role
  if (role === 'ADMIN') return '管理员'
  if (role === 'TEACHER') return '教师'
  if (role === 'STUDENT') return '学生'
  return '未知'
})

function handleLogout() {
  ElMessageBox.confirm('确定要退出登录吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    userStore.logout()
  }).catch(() => {})
}
</script>

<style scoped>
.layout-container {
  height: 100vh;
}

.layout-aside {
  background-color: #304156;
  overflow-y: auto;
}

.logo {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #263445;
}

.logo h2 {
  color: #fff;
  font-size: 16px;
  white-space: nowrap;
}

.layout-header {
  background-color: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid #e6e6e6;
  padding: 0 20px;
}

.header-left {
  font-size: 14px;
  color: #303133;
}

.header-right {
  display: flex;
  align-items: center;
}

.layout-main {
  background-color: #f0f2f5;
  padding: 20px;
  overflow-y: auto;
}

.el-menu {
  border-right: none;
}
</style>
