import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { login as loginApi } from '../api/auth'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || 'null'))

  const isLoggedIn = computed(() => !!token.value)
  const isAdmin = computed(() => userInfo.value?.role === 'ADMIN')
  const isTeacher = computed(() => userInfo.value?.role === 'TEACHER')
  const isStudent = computed(() => userInfo.value?.role === 'STUDENT')

  async function login(loginForm) {
    const res = await loginApi(loginForm.username, loginForm.password)
    token.value = res.data.token
    userInfo.value = res.data.userInfo || {
      username: loginForm.username,
      realName: res.data.realName || res.data.username,
      role: res.data.role
    }
    localStorage.setItem('token', token.value)
    localStorage.setItem('userInfo', JSON.stringify(userInfo.value))
  }

  function logout() {
    token.value = ''
    userInfo.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
    const router = useRouter()
    router.push('/login')
  }

  return {
    token,
    userInfo,
    isLoggedIn,
    isAdmin,
    isTeacher,
    isStudent,
    login,
    logout
  }
})
