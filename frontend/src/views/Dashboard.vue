<template>
  <div class="dashboard">
    <h2 class="page-title">仪表盘</h2>
    <el-row :gutter="20">
      <el-col :span="8">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-icon student-icon">
              <el-icon :size="32"><User /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">学生总数</div>
              <div class="stat-value">{{ stats.studentCount }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-icon teacher-icon">
              <el-icon :size="32"><UserFilled /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">教师总数</div>
              <div class="stat-value">{{ stats.teacherCount }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="hover">
          <div class="stat-card">
            <div class="stat-icon course-icon">
              <el-icon :size="32"><Reading /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">课程总数</div>
              <div class="stat-value">{{ stats.courseCount }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card style="margin-top: 20px" shadow="hover">
      <template #header>
        <span>欢迎使用学生教育管理系统</span>
      </template>
      <div class="welcome-content">
        <p>您好，{{ userStore.userInfo?.realName || '用户' }}！</p>
        <p class="role-desc" v-if="userStore.isAdmin">
          您拥有管理员权限，可以管理学生、教师、班级、课程和成绩信息。
        </p>
        <p class="role-desc" v-else-if="userStore.isTeacher">
          您拥有教师权限，可以管理课程和成绩信息。
        </p>
        <p class="role-desc" v-else-if="userStore.isStudent">
          您可以查看成绩和进行选课操作。
        </p>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { reactive } from 'vue'
import { User, UserFilled, Reading } from '@element-plus/icons-vue'
import { useUserStore } from '../stores/user'

const userStore = useUserStore()

const stats = reactive({
  studentCount: '--',
  teacherCount: '--',
  courseCount: '--'
})
</script>

<style scoped>
.page-title {
  margin-bottom: 20px;
  font-size: 20px;
  color: #303133;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 16px;
}

.stat-icon {
  width: 64px;
  height: 64px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
}

.student-icon { background-color: #409eff; }
.teacher-icon { background-color: #67c23a; }
.course-icon { background-color: #e6a23c; }

.stat-info {
  flex: 1;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 4px;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
}

.welcome-content p {
  line-height: 2;
  font-size: 15px;
  color: #606266;
}

.role-desc {
  color: #909399 !important;
}
</style>
