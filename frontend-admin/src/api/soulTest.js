import request from './request'

/**
 * 灵魂测试管理接口
 */

/**
 * 题目列表（含选项）
 * @param {{status?, pageNum?, pageSize?}} params
 */
export function getQuestionList(params) {
  return request.get('/admin/soulTest/question/list', { params })
}

/** 保存题目（含选项） */
export function saveQuestion(data) {
  return request.post('/admin/soulTest/question/save', data)
}

/** 结果类型列表 */
export function getResultList() {
  return request.get('/admin/soulTest/result/list')
}

/** 保存结果类型 */
export function saveResult(data) {
  return request.post('/admin/soulTest/result/save', data)
}
