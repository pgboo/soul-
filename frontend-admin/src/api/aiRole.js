import request from './request'

/**
 * AI 角色管理接口
 * 字段口径以后端 DTO 为准
 */

/**
 * AI 角色列表
 * @param {{keyword?, status?, pageNum?, pageSize?}} params
 *   status: 0 禁用 / 1 启用
 * @returns 分页 records: { roleId, name, avatar, welcomeText, prompt, status(0禁用/1启用), sort, createTime }
 */
export function getAiRoleList(params) {
  return request.get('/admin/ai/role/list', { params })
}

/**
 * 新增/编辑 AI 角色
 * @param {{roleId?, name, avatar?, welcomeText?, prompt?, status?, sort?}} data
 */
export function saveAiRole(data) {
  return request.post('/admin/ai/role/save', data)
}

/**
 * 删除 AI 角色
 * @param {{roleId}} data
 */
export function deleteAiRole(data) {
  return request.post('/admin/ai/role/delete', data)
}
