import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue')
  },
  {
    path: '/',
    component: () => import('../views/Layout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('../views/Dashboard.vue')
      },
      {
        path: 'students',
        name: 'StudentList',
        component: () => import('../views/student/StudentList.vue')
      },
      {
        path: 'teachers',
        name: 'TeacherList',
        component: () => import('../views/teacher/TeacherList.vue')
      },
      {
        path: 'classes',
        name: 'ClassList',
        component: () => import('../views/class/ClassList.vue')
      },
      {
        path: 'courses',
        name: 'CourseList',
        component: () => import('../views/course/CourseList.vue')
      },
      {
        path: 'scores',
        name: 'ScoreList',
        component: () => import('../views/score/ScoreList.vue')
      },
      {
        path: 'enrollments',
        name: 'EnrollmentList',
        component: () => import('../views/enrollment/EnrollmentList.vue')
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

const whiteList = ['/login']

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  if (token) {
    if (to.path === '/login') {
      next('/dashboard')
    } else {
      next()
    }
  } else {
    if (whiteList.includes(to.path)) {
      next()
    } else {
      next('/login')
    }
  }
})

export default router
