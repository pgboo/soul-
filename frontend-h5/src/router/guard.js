import { showToast } from 'vant'
import router from './index'
import { useUserStore } from '@/stores/user'

/**
 * 全局前置守卫：
 * 1. 未登录访问受保护页面 → /login
 * 2. 已登录访问登录页 → 按初始化状态分流
 * 3. 初始化流程守卫：访问主功能页但未完成初始化 → 强制回到对应步骤
 */
router.beforeEach(async (to, from, next) => {
  const userStore = useUserStore()

  // 恢复本地登录态（防止刷新后 store 为空）
  if (!userStore.token) {
    userStore.restore()
  }

  const requiresAuth = to.meta.requiresAuth !== false

  // 未登录
  if (requiresAuth && !userStore.isLogin) {
    next({ name: 'login', query: to.fullPath !== '/login' ? { redirect: to.fullPath } : {} })
    return
  }

  // 已登录访问登录页 → 分流
  if (to.name === 'login' && userStore.isLogin) {
    // 确认最新初始化状态
    try {
      await userStore.fetchInitStatus()
    } catch (e) {
      // 拉取失败时按本地状态分流
    }
    next({ name: userStore.nextStep })
    return
  }

  // 初始化流程守卫：所有主功能页若初始化未完成，重定向到对应步骤
  // 注意：服务器设置页必须放行 —— 连不上服务器时它正是用来救场的，
  // 若在此处被拦截，用户会陷入「连不上 → 被弹走 → 还是连不上」的死循环。
  if (
    userStore.isLogin &&
    to.name !== 'login' &&
    to.name !== 'soul-result' &&
    to.name !== 'server-setting'
  ) {
    // 免于拦截的初始化页面本身
    const initPages = ['profile-init', 'tag-select', 'soul-test']
    const step = userStore.nextStep

    if (step !== 'square' && !initPages.includes(to.name)) {
      // 尚未完成初始化却访问主功能页 → 拉最新状态再决定
      try {
        await userStore.fetchInitStatus()
      } catch (e) {
        showToast('网络异常')
      }
      next({ name: userStore.nextStep })
      return
    }

    // 已跳到某初始化步骤，但用户手动回到更前置的步骤也允许（顺序前进由页面控制）
  }

  // 设置标题
  if (to.meta.title) {
    document.title = `${to.meta.title} · Soul`
  }

  next()
})
