import { defineStore } from 'pinia'
import { userApi } from '@/api'

/**
 * 用户全局状态：token、用户基础信息、初始化完成状态、资料。
 * 登录态持久化到 localStorage，应用启动时 restore()。
 */
export const useUserStore = defineStore('user', {
  state: () => ({
    token: '',
    userId: null,
    nickname: '',
    avatar: '',
    /** 资料是否完成 */
    profileComplete: false,
    /** 标签是否完成 */
    tagComplete: false,
    /** 灵魂测试是否完成 */
    testComplete: false,
    /** 我的资料详情 */
    profile: null
  }),

  getters: {
    isLogin: (state) => !!state.token,
    /**
     * 根据完成状态计算下一步路由名。
     * @returns {'profile-init'|'tag-select'|'soul-test'|'square'}
     */
    nextStep(state) {
      if (!state.profileComplete) return 'profile-init'
      if (!state.tagComplete) return 'tag-select'
      if (!state.testComplete) return 'soul-test'
      return 'square'
    }
  },

  actions: {
    /** 从 localStorage 恢复登录态 */
    restore() {
      const token = localStorage.getItem('soul_token')
      const raw = localStorage.getItem('soul_user')
      if (token) {
        this.token = token
      }
      if (raw) {
        try {
          const data = JSON.parse(raw)
          this.userId = data.userId ?? null
          this.nickname = data.nickname || ''
          this.avatar = data.avatar || ''
          this.profileComplete = !!data.profileComplete
          this.tagComplete = !!data.tagComplete
          this.testComplete = !!data.testComplete
        } catch (e) {
          // 本地数据损坏则忽略
          localStorage.removeItem('soul_user')
        }
      }
    },

    /** 登录成功后写入登录态 */
    setLogin(loginResp) {
      this.token = loginResp.token || ''
      this.userId = loginResp.userId ?? null
      this.nickname = loginResp.nickname || ''
      this.avatar = loginResp.avatar || ''
      this.profileComplete = !!loginResp.profileComplete
      this.tagComplete = !!loginResp.tagComplete
      this.testComplete = !!loginResp.testComplete
      this.persist()
    },

    /** 拉取并更新初始化状态（分流用） */
    async fetchInitStatus() {
      const data = await userApi.initStatus()
      this.profileComplete = !!data.profileComplete
      this.tagComplete = !!data.tagComplete
      this.testComplete = !!data.testComplete
      this.persist()
      return data
    },

    /** 拉取我的资料 */
    async fetchProfile() {
      const data = await userApi.profile()
      this.profile = data
      this.nickname = data.nickname || this.nickname
      this.avatar = data.avatar || this.avatar
      this.persist()
      return data
    },

    /** 标记某阶段完成 */
    markComplete(step) {
      if (step === 'profile') this.profileComplete = true
      if (step === 'tag') this.tagComplete = true
      if (step === 'test') this.testComplete = true
      this.persist()
    },

    /** 持久化到 localStorage */
    persist() {
      if (this.token) {
        localStorage.setItem('soul_token', this.token)
      }
      const data = {
        userId: this.userId,
        nickname: this.nickname,
        avatar: this.avatar,
        profileComplete: this.profileComplete,
        tagComplete: this.tagComplete,
        testComplete: this.testComplete
      }
      localStorage.setItem('soul_user', JSON.stringify(data))
    },

    /** 退出登录：调后端 + 清本地 */
    async logout() {
      try {
        await userApi.logout()
      } catch (e) {
        // 即便后端失败也清理本地态
      }
      this.reset()
    },

    /** 彻底清理登录态 */
    reset() {
      this.token = ''
      this.userId = null
      this.nickname = ''
      this.avatar = ''
      this.profileComplete = false
      this.tagComplete = false
      this.testComplete = false
      this.profile = null
      localStorage.removeItem('soul_token')
      localStorage.removeItem('soul_user')
    }
  }
})
