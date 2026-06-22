<template>
  <div class="page-container">
    <h2 class="page-title">班级管理</h2>

    <el-card shadow="never" class="search-card">
      <el-form :model="searchForm" inline>
        <el-form-item label="班级名称">
          <el-input v-model="searchForm.className" placeholder="请输入班级名称" clearable />
        </el-form-item>
        <el-form-item label="年级">
          <el-input v-model="searchForm.grade" placeholder="请输入年级" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" style="margin-top: 16px">
      <div class="toolbar">
        <el-button type="primary" @click="handleAdd">新增班级</el-button>
      </div>
      <el-table :data="tableData" v-loading="loading" border stripe style="width: 100%">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="className" label="班级名称" width="150" />
        <el-table-column prop="grade" label="年级" width="100" />
        <el-table-column prop="headTeacherName" label="班主任" width="120" />
        <el-table-column prop="studentCount" label="学生人数" width="100" />
        <el-table-column prop="createTime" label="创建时间" width="170" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" link @click="handleViewStudents(row)">查看学生</el-button>
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

    <!-- 新增/编辑弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="520px"
      :close-on-click-modal="false"
    >
      <el-form ref="formRef" :model="formData" :rules="formRules" label-width="80px">
        <el-form-item label="班级名称" prop="className">
          <el-input v-model="formData.className" placeholder="请输入班级名称" />
        </el-form-item>
        <el-form-item label="年级" prop="grade">
          <el-input v-model="formData.grade" placeholder="请输入年级" />
        </el-form-item>
        <el-form-item label="班主任" prop="headTeacherId">
          <el-select v-model="formData.headTeacherId" placeholder="请选择班主任" clearable style="width: 100%">
            <el-option
              v-for="item in teacherList"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitLoading" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 查看学生弹窗 -->
    <el-dialog v-model="studentDialogVisible" title="班级学生列表" width="700px">
      <el-table :data="studentList" v-loading="studentLoading" border stripe>
        <el-table-column prop="studentNo" label="学号" width="120" />
        <el-table-column prop="name" label="姓名" width="100" />
        <el-table-column prop="gender" label="性别" width="70">
          <template #default="{ row }">
            {{ row.gender === 'MALE' ? '男' : row.gender === 'FEMALE' ? '女' : row.gender }}
          </template>
        </el-table-column>
        <el-table-column prop="age" label="年龄" width="70" />
        <el-table-column prop="phone" label="手机" width="130" />
        <el-table-column prop="email" label="邮箱" />
      </el-table>
      <template #footer>
        <el-button @click="studentDialogVisible = false">关 闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import * as classApi from '../../api/class'
import * as teacherApi from '../../api/teacher'

const loading = ref(false)
const tableData = ref([])
const pagination = reactive({ page: 1, size: 10, total: 0 })
const searchForm = reactive({ className: '', grade: '' })

const dialogVisible = ref(false)
const submitLoading = ref(false)
const isEdit = ref(false)
const editId = ref(null)
const formRef = ref(null)
const teacherList = ref([])

const studentDialogVisible = ref(false)
const studentList = ref([])
const studentLoading = ref(false)

const dialogTitle = computed(() => isEdit.value ? '编辑班级' : '新增班级')

const formData = reactive({ className: '', grade: '', headTeacherId: null })

const formRules = {
  className: [{ required: true, message: '请输入班级名称', trigger: 'blur' }],
  grade: [{ required: true, message: '请输入年级', trigger: 'blur' }]
}

async function fetchData() {
  loading.value = true
  try {
    const params = { page: pagination.page, size: pagination.size, ...searchForm }
    const res = await classApi.getPage(params)
    if (res.data) {
      tableData.value = res.data.records || []
      pagination.total = res.data.total || 0
    }
  } finally {
    loading.value = false
  }
}

async function fetchTeacherList() {
  try {
    const res = await teacherApi.getPage({ page: 1, size: 1000 })
    if (res.data) {
      teacherList.value = res.data.records || []
    }
  } catch {}
}

function handleSearch() {
  pagination.page = 1
  fetchData()
}

function handleReset() {
  searchForm.className = ''
  searchForm.grade = ''
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
    className: row.className || '',
    grade: row.grade || '',
    headTeacherId: row.headTeacherId || null
  })
  dialogVisible.value = true
}

function resetForm() {
  formData.className = ''
  formData.grade = ''
  formData.headTeacherId = null
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
        await classApi.update(data)
        ElMessage.success('更新成功')
      } else {
        await classApi.create(data)
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
  ElMessageBox.confirm(`确定要删除班级 ${row.className} 吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    await classApi.remove(row.id)
    ElMessage.success('删除成功')
    fetchData()
  }).catch(() => {})
}

async function handleViewStudents(row) {
  studentDialogVisible.value = true
  studentLoading.value = true
  try {
    const res = await classApi.getStudents(row.id)
    studentList.value = res.data || []
  } finally {
    studentLoading.value = false
  }
}

onMounted(() => {
  fetchData()
  fetchTeacherList()
})
</script>

<style scoped>
.page-container { padding: 0; }
.page-title { margin-bottom: 16px; font-size: 20px; color: #303133; }
.search-card { margin-bottom: 0; }
.toolbar { margin-bottom: 16px; }
.pagination { display: flex; justify-content: flex-end; margin-top: 16px; }
</style>
