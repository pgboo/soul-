import { defineStore } from 'pinia'
import { adminLogin } from '@/api/auth'
import { TOKEN_KEY, ADMIN_INFO_KEY } from '@/api/request'

/**
 * 管理员登录态 store
 */
export const useAdminStore = defineStore('admin', {
  state: () => ({
    token: localStorage.getItem(TOKEN_KEY) || '',
    account: '',
    nickname: ''
  }),

  getters: {
    isLoggedIn: (state) => !!state.token,
    displayName: (state) => state.nickname || state.account || '管理员'
  },

  actions: {
    /**
     * 登录并持久化 token
     * @param {{account: string, password: string}} payload
     */
    async login(payload) {
      const data = await adminLogin(payload)
      this.token = data?.token || ''
      this.account = data?.account || payload.account
      this.nickname = data?.nickname || ''
      localStorage.setItem(TOKEN_KEY, this.token)
      localStorage.setItem(
        ADMIN_INFO_KEY,
        JSON.stringify({ account: this.account, nickname: this.nickname })
      )
      return data
    },

    /** 从 localStorage 恢复管理员信息 */
    restore() {
      try {
        const raw = localStorage.getItem(ADMIN_INFO_KEY)
        if (raw) {
          const info = JSON.parse(raw)
          this.account = info.account || ''
          this.nickname = info.nickname || ''
        }
      } catch (e) {
        this.account = ''
        this.nickname = ''
      }
    },

    /** 退出登录 */
    logout() {
      this.token = ''
      this.account = ''
      this.nickname = ''
      localStorage.removeItem(TOKEN_KEY)
      localStorage.removeItem(ADMIN_INFO_KEY)
    }
  }
})
