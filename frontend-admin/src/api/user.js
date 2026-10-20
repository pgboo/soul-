import request from './request'

/**
 * 用户管理接口
 * 字段口径以后端 DTO 为准
 */

/**
 * 用户列表（搜索 + 分页）
 * @param {{nickname?: string, phone?: string, status?: number, pageNum?: number, pageSize?: number}} params
 *   status: 1 正常 / 2 封禁
 * @returns 分页 records: AdminUserResp
 *   { userId, userNo, nickname, avatar, gender(0未知/1男/2女), phoneMask, status(1正常/2封禁),
 *     riskLevel(0正常/1观察), muteEndTime(字符串，null=未禁言), profileCompleteFlag, tagFlag, testFlag, createTime }
 */
export function getUserList(params) {
  return request.get('/admin/user/list', { params })
}

/**
 * 用户详情
 * @param {{userId: number|string}} params
 * @returns AdminUserDetailResp:
 *   { userId, userNo, nickname, avatar, gender, birthday, city, sign, intro, soulId,
 *     completePercent, phoneMask, status, riskLevel, muteEndTime, riskRecords[] }
 *   riskRecords 项: { recordId, riskType(1禁言/2封禁/3观察), reason, startTime, endTime, operatorId, status(0解除/1生效), createTime }
 */
export function getUserDetail(params) {
  return request.get('/admin/user/detail', { params })
}

/**
 * 禁言 / 解除禁言
 * @param {{userId, mute: boolean, muteDays?: number|null, reason?: string}} data
 *   mute: true=禁言 / false=解除
 *   muteDays: >0 天数；null 或省略=永久禁言；解除时忽略
 */
export function muteUser(data) {
  return request.post('/admin/user/mute', data)
}

/**
 * 封禁 / 解封
 * @param {{userId, ban: boolean, reason?: string}} data
 *   ban: true=封禁 / false=解封
 */
export function banUser(data) {
  return request.post('/admin/user/ban', data)
}
