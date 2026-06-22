<template>
  <div class="page-container">
    <h2 class="page-title">选课管理</h2>

    <el-card shadow="never" style="margin-bottom: 16px">
      <div class="toolbar">
        <el-button type="primary" @click="handleEnroll">选课</el-button>
      </div>
    </el-card>

    <!-- 已选课程 -->
    <el-card shadow="never">
      <template #header>
        <span>我的已选课程</span>
      </template>
      <el-table :data="enrolledList" v-loading="loading" border stripe style="width: 100%" empty-text="暂未选课">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="courseNo" label="课程编号" width="120" />
        <el-table-column prop="courseName" label="课程名称" min-width="150" />
        <el-table-column prop="credit" label="学分" width="80" />
        <el-table-column prop="teacherName" label="授课教师" width="120" />
        <el-table-column prop="semester" label="学期" width="130" />
        <el-table-column prop="createTime" label="选课时间" width="170" />
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button type="danger" size="small" link @click="handleUnenroll(row)">退课</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 选课弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      title="选择课程"
      width="700px"
      :close-on-click-modal="false"
    >
      <el-table :data="availableCourses" v-loading="courseLoading" border stripe
        @selection-change="handleSelectionChange"
        empty-text="暂无可选课程"
      >
        <el-table-column type="selection" width="50" />
        <el-table-column prop="courseNo" label="课程编号" width="120" />
        <el-table-column prop="name" label="课程名称" min-width="150" />
        <el-table-column prop="credit" label="学分" width="80" />
        <el-table-column prop="teacherName" label="授课教师" width="120" />
        <el-table-column prop="semester" label="学期" width="130" />
      </el-table>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="enrollLoading" :disabled="selectedCourses.length === 0" @click="confirmEnroll">
          确定选课 ({{ selectedCourses.length }})
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import * as enrollmentApi from '../../api/enrollment'
import * as courseApi from '../../api/course'
import { useUserStore } from '../../stores/user'

const userStore = useUserStore()

const loading = ref(false)
const enrolledList = ref([])

const dialogVisible = ref(false)
const courseLoading = ref(false)
const enrollLoading = ref(false)
const availableCourses = ref([])
const selectedCourses = ref([])

async function fetchEnrolled() {
  loading.value = true
  try {
    const res = await enrollmentApi.getByStudentId(userStore.userInfo?.username || '')
    if (res.data) {
      enrolledList.value = Array.isArray(res.data) ? res.data : (res.data.records || [])
    }
  } finally {
    loading.value = false
  }
}

async function handleEnroll() {
  dialogVisible.value = true
  selectedCourses.value = []
  courseLoading.value = true
  try {
    const res = await courseApi.getPage({ page: 1, size: 1000 })
    const allCourses = res.data?.records || []
    // 过滤掉已选课程
    const enrolledIds = enrolledList.value.map(e => e.courseId || e.id)
    availableCourses.value = allCourses.filter(c => !enrolledIds.includes(c.id))
  } finally {
    courseLoading.value = false
  }
}

function handleSelectionChange(selection) {
  selectedCourses.value = selection
}

async function confirmEnroll() {
  if (selectedCourses.value.length === 0) {
    ElMessage.warning('请选择课程')
    return
  }
  enrollLoading.value = true
  try {
    for (const course of selectedCourses.value) {
      await enrollmentApi.enroll({
        studentId: userStore.userInfo?.username || userStore.userInfo?.id,
        courseId: course.id
      })
    }
    ElMessage.success(`成功选课 ${selectedCourses.value.length} 门`)
    dialogVisible.value = false
    fetchEnrolled()
  } finally {
    enrollLoading.value = false
  }
}

function handleUnenroll(row) {
  ElMessageBox.confirm(`确定要退选课程 ${row.courseName} 吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    await enrollmentApi.unenroll(row.id)
    ElMessage.success('退课成功')
    fetchEnrolled()
  }).catch(() => {})
}

onMounted(() => {
  fetchEnrolled()
})
</script>

<style scoped>
.page-container { padding: 0; }
.page-title { margin-bottom: 16px; font-size: 20px; color: #303133; }
.toolbar { margin-bottom: 0; }
</style>
