import request from './request'

/**
 * 后台认证接口
 */

/**
 * 后台登录
 * @param {{account: string, password: string}} data
 * @returns {Promise<{token: string, account: string, nickname: string}>}
 */
export function adminLogin(data) {
  return request.post('/admin/auth/login', data)
}
