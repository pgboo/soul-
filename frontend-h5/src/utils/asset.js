/**
 * 静态资源 / 接口地址解析。
 *
 * 地址来源有两级，优先级从高到低：
 *   1. 用户在「服务器设置」里填的地址（存 localStorage）—— App 内可随时改，不用重新打包
 *   2. 构建时注入的 VITE_API_BASE —— 网页部署为空（走同源），打包 App 时为后端公网地址
 *
 * 为什么需要第 1 级：内网穿透的免费域名会变（电脑关机 / 网络切换 / 隧道重启都会换），
 * 而 App 里编译死的地址一变就整包失效。留一个运行时可改的入口，
 * 地址变了只要在 App 里改一下即可，不必重新出包。
 */

const BUILD_BASE = String(import.meta.env.VITE_API_BASE || '').replace(/\/+$/, '')
const LS_KEY = 'soul_server_base'

/** 去掉首尾空白和结尾斜杠 */
function normalize(url) {
  return String(url || '').trim().replace(/\/+$/, '')
}

/** 当前生效的后端根地址（不含 /api）。用户设置优先于构建值。 */
export function currentBase() {
  const saved = normalize(localStorage.getItem(LS_KEY))
  return saved || BUILD_BASE
}

/** 构建时写死的默认地址（用于「恢复默认」展示） */
export function defaultBase() {
  return BUILD_BASE
}

/** 是否正在使用用户自定义地址 */
export function hasCustomServer() {
  return !!normalize(localStorage.getItem(LS_KEY))
}

/**
 * 保存自定义服务器地址。
 * 传空字符串表示清除自定义、恢复为构建时默认地址。
 * @returns {string} 保存后实际生效的根地址
 */
export function setServerBase(url) {
  const v = normalize(url)
  if (v) {
    localStorage.setItem(LS_KEY, v)
  } else {
    localStorage.removeItem(LS_KEY)
  }
  return currentBase()
}

/** 把后端返回的相对资源地址（如 /upload/avatar/x.png）补成可直接访问的 URL */
export function resolveAssetUrl(url) {
  if (!url) return ''
  // 已是绝对地址 / data URI / blob / file 协议，原样返回
  if (/^(https?:)?\/\//i.test(url) || /^(data|blob|file):/i.test(url)) return url
  const base = currentBase()
  if (!base) return url
  return base + (url.startsWith('/') ? url : '/' + url)
}

/** 后端接口根地址（含 /api），每次调用都重新解析，保证改完设置立刻生效 */
export function apiBaseUrl() {
  return currentBase() + '/api'
}

/** 是否运行在 App（Capacitor WebView）里 */
export function isNativeApp() {
  return !!(typeof window !== 'undefined' && window.Capacitor?.isNativePlatform?.())
}

export default resolveAssetUrl
