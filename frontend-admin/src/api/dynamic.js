import request from './request'

/**
 * 瞬间管理接口
 * 字段口径以后端 DTO 为准
 */

/**
 * 瞬间列表
 * @param {{keyword?, status?, auditStatus?, pageNum?, pageSize?}} params
 *   status: 1 正常 / 2 下架
 *   auditStatus: 0 已过审 / 1 待审核 / 2 拒绝
 * @returns 分页 records:
 *   { dynamicId, userId, nickname, avatar, content, mediaUrls[], status(1正常/2下架),
 *     auditStatus(0已过审/1待审核/2拒绝), anonymousFlag, visibleScope, likeCount,
 *     commentCount, collectCount, reportCount, isDelete, createTime }
 */
export function getDynamicList(params) {
  return request.get('/admin/dynamic/list', { params })
}

/**
 * 审核瞬间（通过 / 拒绝）
 * @param {{dynamicId, pass: boolean, reason?}} data  pass: true=通过 / false=拒绝
 */
export function auditDynamic(data) {
  return request.post('/admin/dynamic/audit', data)
}

/**
 * 下架瞬间
 * @param {{dynamicId, reason?}} data
 */
export function offlineDynamic(data) {
  return request.post('/admin/dynamic/offline', data)
}

/**
 * 恢复瞬间
 * @param {{dynamicId, reason?}} data
 */
export function restoreDynamic(data) {
  return request.post('/admin/dynamic/restore', data)
}
