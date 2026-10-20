import request from './request'

/**
 * 数据看板接口
 */

/**
 * 看板汇总数据
 * @returns {Promise<{
 *   userTotal, userToday, dynamicTotal, dynamicToday, commentTotal, commentToday,
 *   messageTotal, messageToday, reportTotal, reportToday, reportPending
 * }>}
 */
export function getDashboardSummary() {
  return request.get('/admin/dashboard/summary')
}
