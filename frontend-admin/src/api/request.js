import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '@/router'

/** 后台 token 在 localStorage 中的键名 */
export const TOKEN_KEY = 'soul_admin_token'
export const ADMIN_INFO_KEY = 'soul_admin_info'

/**
 * axios 实例
 * baseURL 使用 /api，由 vite proxy 转发到 http://localhost:8080
 * 统一超时 15s
 */
const service = axios.create({
  baseURL: '/api',
  timeout: 15000,
  headers: { 'Content-Type': 'application/json' }
})

/** 请求拦截器：自动附加 Bearer token */
service.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem(TOKEN_KEY)
    if (token) {
      config.headers = config.headers || {}
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

/** 清除登录态并跳转登录页 */
function redirectToLogin(message) {
  localStorage.removeItem(TOKEN_KEY)
  localStorage.removeItem(ADMIN_INFO_KEY)
  if (message) {
    ElMessage.error(message)
  }
  const current = router.currentRoute.value
  if (current.path !== '/login') {
    router.replace({
      path: '/login',
      query: { redirect: current.fullPath }
    })
  }
}

/**
 * 响应拦截器
 * - code=200 返回 data（直接给业务使用）
 * - code=401 清登录态跳登录页
 * - 其他 code≠200 用 ElMessage 提示并 reject
 */
service.interceptors.response.use(
  (response) => {
    const res = response.data
    // 非标准返回（如文件流等）直接透传
    if (res === null || res === undefined || typeof res !== 'object') {
      return res
    }
    const { code, message, data } = res
    if (code === 200) {
      return data
    }
    if (code === 401) {
      redirectToLogin(message || '登录已过期，请重新登录')
      return Promise.reject(new Error(message || '未登录'))
    }
    ElMessage.error(message || '请求失败')
    return Promise.reject(new Error(message || '请求失败'))
  },
  (error) => {
    const status = error?.response?.status
    if (status === 401) {
      redirectToLogin('登录已过期，请重新登录')
    } else if (status === 403) {
      ElMessage.error('无权限访问')
    } else if (status === 404) {
      ElMessage.error('接口不存在')
    } else if (status >= 500) {
      ElMessage.error('系统繁忙，请稍后重试')
    } else if (error.code === 'ECONNABORTED') {
      ElMessage.error('请求超时，请检查网络')
    } else {
      ElMessage.error(error?.message || '网络异常')
    }
    return Promise.reject(error)
  }
)

export default service
