import { createRouter, createWebHashHistory } from 'vue-router'

/**
 * 路由表。采用 hash 模式，便于 H5 部署与刷新。
 * meta.requiresAuth: 是否需要登录；meta.guard: 是否参与初始化分流守卫。
 */
const routes = [
  {
    path: '/login',
    name: 'login',
    component: () => import('@/views/Login.vue'),
    meta: { title: '登录', requiresAuth: false }
  },
  {
    // 服务器地址设置：内测期公网域名会变，留一个运行时可改的入口，避免重新发包
    path: '/server',
    name: 'server-setting',
    component: () => import('@/views/ServerSetting.vue'),
    meta: { title: '服务器设置', requiresAuth: false }
  },
  {
    path: '/',
    redirect: '/square'
  },

  // ---- 初始化流程（参与分流守卫） ----
  {
    path: '/profile-init',
    name: 'profile-init',
    component: () => import('@/views/ProfileInit.vue'),
    meta: { title: '完善资料', requiresAuth: true, guard: true }
  },
  {
    path: '/tag-select',
    name: 'tag-select',
    component: () => import('@/views/TagSelect.vue'),
    meta: { title: '选择标签', requiresAuth: true, guard: true }
  },
  {
    path: '/soul-test',
    name: 'soul-test',
    component: () => import('@/views/SoulTest.vue'),
    meta: { title: '灵魂测试', requiresAuth: true, guard: true }
  },
  {
    path: '/soul-result',
    name: 'soul-result',
    component: () => import('@/views/SoulResult.vue'),
    meta: { title: '测试结果', requiresAuth: true }
  },

  // ---- 主功能页 ----
  {
    path: '/square',
    name: 'square',
    component: () => import('@/views/Square.vue'),
    meta: { title: '广场', requiresAuth: true }
  },
  {
    path: '/dynamic/:id',
    name: 'dynamic-detail',
    component: () => import('@/views/DynamicDetail.vue'),
    meta: { title: '瞬间详情', requiresAuth: true }
  },
  {
    path: '/publish',
    name: 'publish',
    component: () => import('@/views/Publish.vue'),
    meta: { title: '发布瞬间', requiresAuth: true }
  },
  {
    path: '/match',
    name: 'match',
    component: () => import('@/views/Match.vue'),
    meta: { title: '匹配', requiresAuth: true }
  },
  {
    path: '/user/:id',
    name: 'user-home',
    component: () => import('@/views/UserHome.vue'),
    meta: { title: '用户主页', requiresAuth: true }
  },
  {
    path: '/messages',
    name: 'messages',
    component: () => import('@/views/MessageList.vue'),
    meta: { title: '消息', requiresAuth: true }
  },
  {
    path: '/chat/:sessionUuid',
    name: 'chat',
    component: () => import('@/views/ChatDetail.vue'),
    meta: { title: '聊天', requiresAuth: true }
  },
  {
    path: '/mine',
    name: 'mine',
    component: () => import('@/views/Mine.vue'),
    meta: { title: '我的', requiresAuth: true }
  },
  {
    path: '/edit-profile',
    name: 'edit-profile',
    component: () => import('@/views/EditProfile.vue'),
    meta: { title: '编辑资料', requiresAuth: true }
  },
  {
    path: '/collect',
    name: 'collect',
    component: () => import('@/views/Collect.vue'),
    meta: { title: '我的收藏', requiresAuth: true }
  },
  {
    path: '/privacy',
    name: 'privacy',
    component: () => import('@/views/Privacy.vue'),
    meta: { title: '隐私设置', requiresAuth: true }
  },
  {
    path: '/blacklist',
    name: 'blacklist',
    component: () => import('@/views/Blacklist.vue'),
    meta: { title: '黑名单', requiresAuth: true }
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'not-found',
    redirect: '/square'
  }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes,
  scrollBehavior() {
    return { top: 0 }
  }
})

export default router
