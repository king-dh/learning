<template>
  <div class="page-container">
    <h2 class="page-title">成绩管理</h2>

    <el-card shadow="never" class="search-card">
      <el-form :model="searchForm" inline>
        <el-form-item label="学生" v-if="!isStudent">
          <el-select v-model="searchForm.studentId" placeholder="请选择学生" clearable filterable style="width: 180px">
            <el-option v-for="s in studentList" :key="s.id" :label="s.name" :value="s.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="课程">
          <el-select v-model="searchForm.courseId" placeholder="请选择课程" clearable filterable style="width: 180px">
            <el-option v-for="c in courseList" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="学期">
          <el-input v-model="searchForm.semester" placeholder="请输入学期" clearable style="width: 150px" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" style="margin-top: 16px">
      <div class="toolbar" v-if="!isStudent">
        <el-button type="primary" @click="handleAdd">新增成绩</el-button>
      </div>
      <el-table :data="tableData" v-loading="loading" border stripe style="width: 100%">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="studentName" label="学生姓名" width="120" />
        <el-table-column prop="courseName" label="课程名称" min-width="150" />
        <el-table-column prop="score" label="分数" width="100" />
        <el-table-column prop="semester" label="学期" width="130" />
        <el-table-column label="操作" width="160" fixed="right" v-if="!isStudent">
          <template #default="{ row }">
            <el-button type="primary" size="small" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" size="small" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @size-change="fetchData"
          @current-change="fetchData"
        />
      </div>
    </el-card>

    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="520px"
      :close-on-click-modal="false"
    >
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="80px">
        <el-form-item label="学生" prop="studentId">
          <el-select v-model="formData.studentId" placeholder="请选择学生" :disabled="isEdit" filterable style="width: 100%">
            <el-option v-for="s in studentList" :key="s.id" :label="s.name" :value="s.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="课程" prop="courseId">
          <el-select v-model="formData.courseId" placeholder="请选择课程" :disabled="isEdit" filterable style="width: 100%">
            <el-option v-for="c in courseList" :key="c.id" :label="c.name" :value="c.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="分数" prop="score">
          <el-input-number v-model="formData.score" :min="0" :max="100" :precision="1" :step="0.5" />
        </el-form-item>
        <el-form-item label="学期" prop="semester">
          <el-input v-model="formData.semester" placeholder="请输入学期" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import * as scoreApi from '../../api/score'
import * as studentApi from '../../api/student'
import * as courseApi from '../../api/course'
import { useUserStore } from '../../stores/user'

const userStore = useUserStore()
const isStudent = computed(() => userStore.isStudent)

const loading = ref(false)
const tableData = ref([])
const pagination = reactive({ page: 1, size: 10, total: 0 })
const searchForm = reactive({ studentId: null, courseId: null, semester: '' })

const studentList = ref([])
const courseList = ref([])

const dialogVisible = ref(false)
const submitLoading = ref(false)
const isEdit = ref(false)
const editId = ref(null)
const formRef = ref(null)

const dialogTitle = computed(() => isEdit.value ? '编辑成绩' : '新增成绩')

const formData = reactive({
  studentId: null,
  courseId: null,
  score: 0,
  semester: ''
})

const formRules = {
  studentId: [{ required: true, message: '请选择学生', trigger: 'change' }],
  courseId: [{ required: true, message: '请选择课程', trigger: 'change' }],
  score: [{ required: true, message: '请输入分数', trigger: 'blur' }]
}

async function fetchData() {
  loading.value = true
  try {
    let res
    if (isStudent.value && userStore.userInfo) {
      // 学生只能看自己的成绩
      res = await scoreApi.getByStudentId(userStore.userInfo.username || '')
    } else {
      const params = { page: pagination.page, size: pagination.size, ...searchForm }
      res = await scoreApi.getPage(params)
    }
    if (res.data) {
      tableData.value = res.data.records || res.data || []
      pagination.total = res.data.total || 0
    }
  } finally {
    loading.value = false
  }
}

async function fetchStudentList() {
  try {
    const res = await studentApi.getPage({ page: 1, size: 1000 })
    if (res.data) {
      studentList.value = res.data.records || []
    }
  } catch {}
}

async function fetchCourseList() {
  try {
    const res = await courseApi.getPage({ page: 1, size: 1000 })
    if (res.data) {
      courseList.value = res.data.records || []
    }
  } catch {}
}

function handleSearch() {
  pagination.page = 1
  fetchData()
}

function handleReset() {
  searchForm.studentId = null
  searchForm.courseId = null
  searchForm.semester = ''
  handleSearch()
}

function handleAdd() {
  isEdit.value = false
  editId.value = null
  resetForm()
  dialogVisible.value = true
}

function handleEdit(row) {
  isEdit.value = true
  editId.value = row.id
  Object.assign(formData, {
    studentId: row.studentId || null,
    courseId: row.courseId || null,
    score: row.score || 0,
    semester: row.semester || ''
  })
  dialogVisible.value = true
}

function resetForm() {
  formData.studentId = null
  formData.courseId = null
  formData.score = 0
  formData.semester = ''
  formRef.value?.resetFields()
}

async function handleSubmit() {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    submitLoading.value = true
    try {
      const data = { ...formData }
      if (isEdit.value) {
        data.id = editId.value
        await scoreApi.update(data)
        ElMessage.success('更新成功')
      } else {
        await scoreApi.create(data)
        ElMessage.success('新增成功')
      }
      dialogVisible.value = false
      fetchData()
    } finally {
      submitLoading.value = false
    }
  })
}

function handleDelete(row) {
  ElMessageBox.confirm(`确定要删除该成绩记录吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    await scoreApi.remove(row.id)
    ElMessage.success('删除成功')
    fetchData()
  }).catch(() => {})
}

onMounted(() => {
  fetchData()
  if (!isStudent.value) {
    fetchStudentList()
  }
  fetchCourseList()
})
</script>

<style scoped>
.page-container { padding: 0; }
.page-title { margin-bottom: 16px; font-size: 20px; color: #303133; }
.search-card { margin-bottom: 0; }
.toolbar { margin-bottom: 16px; }
.pagination { display: flex; justify-content: flex-end; margin-top: 16px; }
</style>
