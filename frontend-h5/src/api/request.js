import axios from 'axios'
import { showToast, showDialog } from 'vant'
import router from '@/router'
import { apiBaseUrl, isNativeApp, currentBase } from '@/utils/asset'

/**
 * axios 实例封装。
 * 统一处理：baseURL、鉴权 Header、响应解包、错误提示、401 自动跳登录。
 */
const request = axios.create({
  // baseURL 在请求拦截器里逐次设置（见下），这里给个初始值即可。
  // 原因：服务器地址允许在运行时修改，写死在这里的话改完必须重启 App 才生效。
  baseURL: apiBaseUrl(),
  timeout: 15000
})

// 请求拦截器：逐次解析 baseURL + 自动携带 token
request.interceptors.request.use(
  (config) => {
    config.baseURL = apiBaseUrl()
    const token = localStorage.getItem('soul_token')
    if (token) {
      config.headers = config.headers || {}
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

// 响应拦截器：解包 Result<T>，统一错误处理
request.interceptors.response.use(
  (response) => {
    const res = response.data
    // 非标准结构（如直接返回文件流）直接透传
    if (res == null || typeof res !== 'object' || !('code' in res)) {
      return res
    }
    if (res.code === 200) {
      return res.data
    }
    // 401 未登录 / Token 过期
    if (res.code === 401) {
      handleUnauthorized()
      return Promise.reject(res)
    }
    showToast(res.message || '操作失败')
    return Promise.reject(res)
  },
  (error) => {
    const status = error?.response?.status
    if (status === 401) {
      handleUnauthorized()
      return Promise.reject(error)
    }

    // 连不上服务器：App 里多半是后端地址失效（免费隧道域名会变），
    // 给一条自助修复的路，避免用户只能干等重新发包。
    if (!error?.response) {
      handleNetworkError()
      return Promise.reject(error)
    }

    showToast(error?.message || '网络异常，请稍后再试')
    return Promise.reject(error)
  }
)

/** 网络层失败（请求根本没到达服务器） */
function handleNetworkError() {
  if (!isNativeApp()) {
    showToast('网络异常，请检查网络后重试')
    return
  }
  const base = currentBase()
  showDialog({
    title: '连不上服务器',
    message: base
      ? `当前地址：\n${base}\n\n如果测试服务器换了地址，请修改后重试。`
      : '请填写测试服务器地址后重试。',
    showCancelButton: true,
    confirmButtonText: '修改地址',
    cancelButtonText: '重试',
    cancelButtonColor: '#8a8a8a'
  })
    .then(() => {
      const current = router.currentRoute.value
      router.push({ name: 'server-setting', query: { redirect: current.fullPath } })
    })
    .catch(() => {
      // 用户选了「重试」，刷新当前页重新走一遍请求
      window.location.reload()
    })
}

/** 处理未登录：清理本地态并跳转登录页 */
function handleUnauthorized() {
  localStorage.removeItem('soul_token')
  localStorage.removeItem('soul_user')
  const current = router.currentRoute.value
  if (current && current.name !== 'login') {
    router.replace({ name: 'login', query: { redirect: current.fullPath } })
  }
}

export default request
