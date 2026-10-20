import { createRouter, createWebHistory } from 'vue-router'
import { TOKEN_KEY } from '@/api/request'

/**
 * 路由表
 * meta.title 用于面包屑 / 文档标题
 * meta.icon  用于侧边菜单图标
 */
const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { title: '登录', public: true }
  },
  {
    path: '/',
    component: () => import('@/layout/AdminLayout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/Dashboard.vue'),
        meta: { title: '数据看板', icon: 'DataLine' }
      },
      {
        path: 'users',
        name: 'UserManage',
        component: () => import('@/views/UserManage.vue'),
        meta: { title: '用户管理', icon: 'User' }
      },
      {
        path: 'tags',
        name: 'TagManage',
        component: () => import('@/views/TagManage.vue'),
        meta: { title: '标签管理', icon: 'CollectionTag' }
      },
      {
        path: 'soul-test',
        name: 'SoulTest',
        component: () => import('@/views/SoulTestManage.vue'),
        meta: { title: '灵魂测试', icon: 'MagicStick' }
      },
      {
        path: 'dynamics',
        name: 'DynamicManage',
        component: () => import('@/views/DynamicManage.vue'),
        meta: { title: '瞬间管理', icon: 'Picture' }
      },
      {
        path: 'reports',
        name: 'ReportManage',
        component: () => import('@/views/ReportManage.vue'),
        meta: { title: '举报管理', icon: 'Warning' }
      },
      {
        path: 'sensitive-words',
        name: 'SensitiveWord',
        component: () => import('@/views/SensitiveWordManage.vue'),
        meta: { title: '敏感词管理', icon: 'Filter' }
      },
      {
        path: 'ai-roles',
        name: 'AiRole',
        component: () => import('@/views/AiRoleManage.vue'),
        meta: { title: 'AI 角色管理', icon: 'ChatDotRound' }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/dashboard'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

/** 全局前置守卫：未登录跳登录页 */
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem(TOKEN_KEY)
  document.title = to.meta?.title
    ? `${to.meta.title} · Soul 后台管理`
    : 'Soul 后台管理'

  if (to.meta?.public) {
    // 已登录访问登录页 -> 跳首页
    if (token && to.path === '/login') {
      next('/dashboard')
      return
    }
    next()
    return
  }

  if (!token) {
    next({ path: '/login', query: { redirect: to.fullPath } })
    return
  }
  next()
})

export default router
