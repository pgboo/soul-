import request from './request'

/**
 * 敏感词管理接口
 * 字段口径以后端 DTO 为准
 */

/**
 * 敏感词列表
 * @param {{keyword?, level?, status?, pageNum?, pageSize?}} params
 *   level: 1 替换 / 2 拦截
 *   status: 0 禁用 / 1 启用
 * @returns 分页 records: { wordId, word, category, level(1替换/2拦截), status(0禁用/1启用), createTime }
 */
export function getSensitiveWordList(params) {
  return request.get('/admin/sensitiveWord/list', { params })
}

/**
 * 新增/编辑敏感词
 * @param {{wordId?, word, category?, level?, status?}} data
 */
export function saveSensitiveWord(data) {
  return request.post('/admin/sensitiveWord/save', data)
}

/**
 * 删除敏感词
 * @param {{wordId}} data
 */
export function deleteSensitiveWord(data) {
  return request.post('/admin/sensitiveWord/delete', data)
}
