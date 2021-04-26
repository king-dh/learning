import Vue from 'vue'
import Router from 'vue-router'
import Index from '@/views/index'

Vue.use(Router)

export default new Router({
  routes: [
    {
      path: '/',
      name: 'Index',
      component: Index
    },
    {
      path: '/activity/index',
      name: 'activity',
      component: () => import("@/views/activity")
    },
    {
      path: '/test1',
      name: 'activity',
      component: () => import("@/views/page/test1")
    },
    {
      path: '/less',
      name: 'activity',
      component: () => import("@/views/page/less")
    },
    {
      path: '/table',
      name: 'activity',
      component: () => import("@/views/page/table")
    },
    {
      path: '/store',
      name: 'activity',
      component: () => import("@/views/page/store")
    }
  ]
})
