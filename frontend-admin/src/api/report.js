import request from './request'

/**
 * 举报管理接口
 * 字段口径以后端 DTO 为准
 */

/**
 * 举报列表
 * @param {{status?, targetType?, pageNum?, pageSize?}} params
 *   status: 1 待处理 / 2 已处理 / 3 驳回
 *   targetType: 1 动态 / 2 评论 / 3 用户
 * @returns 分页 records:
 *   { reportId, reporterId, reporterNickname, targetType, targetId, targetSummary,
 *     reasonType, reason, status(1待处理/2已处理/3驳回), handleResult, handlerId, handleTime, createTime }
 */
export function getReportList(params) {
  return request.get('/admin/report/list', { params })
}

/**
 * 处理举报
 * @param {{reportId, handleType: string, muteDays?: number|null, handleResult?: string}} data
 *   handleType: reject(驳回) / offline(下架动态) / mute(禁言被举报用户) / ban(封禁被举报用户)
 *   muteDays: handleType=mute 时有效，null=永久
 *   handleResult: 处理结果备注
 */
export function handleReport(data) {
  return request.post('/admin/report/handle', data)
}
