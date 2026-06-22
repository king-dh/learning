<template>
  <div class="page-container">
    <h2 class="page-title">课程管理</h2>

    <el-card shadow="never" class="search-card">
      <el-form :model="searchForm" inline>
        <el-form-item label="课程名称">
          <el-input v-model="searchForm.name" placeholder="请输入课程名称" clearable />
        </el-form-item>
        <el-form-item label="课程编号">
          <el-input v-model="searchForm.courseNo" placeholder="请输入课程编号" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" style="margin-top: 16px">
      <div class="toolbar">
        <el-button type="primary" @click="handleAdd">新增课程</el-button>
      </div>
      <el-table :data="tableData" v-loading="loading" border stripe style="width: 100%">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="courseNo" label="课程编号" width="120" />
        <el-table-column prop="name" label="课程名称" min-width="150" />
        <el-table-column prop="credit" label="学分" width="80" />
        <el-table-column prop="teacherName" label="授课教师" width="120" />
        <el-table-column prop="semester" label="学期" width="120" />
        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" link @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" size="small" link @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination">
        <el-pagination
          :current-page="pagination.page"
          :page-size="pagination.size"
          @update:current-page="pagination.page = $event; fetchData()"
          @update:page-size="pagination.size = $event; fetchData()"
          :total="pagination.total"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
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
        <el-form-item label="课程编号" prop="courseNo">
          <el-input v-model="formData.courseNo" placeholder="请输入课程编号" />
        </el-form-item>
        <el-form-item label="课程名称" prop="name">
          <el-input v-model="formData.name" placeholder="请输入课程名称" />
        </el-form-item>
        <el-form-item label="学分" prop="credit">
          <el-input-number v-model="formData.credit" :min="0" :precision="1" :step="0.5" />
        </el-form-item>
        <el-form-item label="授课教师" prop="teacherId">
          <el-select v-model="formData.teacherId" placeholder="请选择授课教师" clearable style="width: 100%">
            <el-option
              v-for="item in teacherList"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="学期" prop="semester">
          <el-input v-model="formData.semester" placeholder="请输入学期，如：2024-2025-1" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="formData.description" type="textarea" :rows="3" placeholder="请输入课程描述" />
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
import * as courseApi from '../../api/course'
import * as teacherApi from '../../api/teacher'

const loading = ref(false)
const tableData = ref([])
const pagination = reactive({ page: 1, size: 10, total: 0 })
const searchForm = reactive({ name: '', courseNo: '' })

const dialogVisible = ref(false)
const submitLoading = ref(false)
const isEdit = ref(false)
const editId = ref(null)
const formRef = ref(null)
const teacherList = ref([])

const dialogTitle = computed(() => isEdit.value ? '编辑课程' : '新增课程')

const formData = reactive({
  courseNo: '',
  name: '',
  credit: 0,
  teacherId: null,
  semester: '',
  description: ''
})

const formRules = {
  courseNo: [{ required: true, message: '请输入课程编号', trigger: 'blur' }],
  name: [{ required: true, message: '请输入课程名称', trigger: 'blur' }],
  credit: [{ required: true, message: '请输入学分', trigger: 'blur' }],
  semester: [{ required: true, message: '请输入学期', trigger: 'blur' }]
}

async function fetchData() {
  loading.value = true
  try {
    const params = { page: pagination.page, size: pagination.size, ...searchForm }
    const res = await courseApi.getPage(params)
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
  searchForm.name = ''
  searchForm.courseNo = ''
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
    courseNo: row.courseNo || '',
    name: row.name || '',
    credit: row.credit || 0,
    teacherId: row.teacherId || null,
    semester: row.semester || '',
    description: row.description || ''
  })
  dialogVisible.value = true
}

function resetForm() {
  formData.courseNo = ''
  formData.name = ''
  formData.credit = 0
  formData.teacherId = null
  formData.semester = ''
  formData.description = ''
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
        await courseApi.update(data)
        ElMessage.success('更新成功')
      } else {
        await courseApi.create(data)
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
  ElMessageBox.confirm(`确定要删除课程 ${row.name} 吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    await courseApi.remove(row.id)
    ElMessage.success('删除成功')
    fetchData()
  }).catch(() => {})
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
