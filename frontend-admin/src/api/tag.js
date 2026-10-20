import request from './request'

/**
 * 标签管理接口
 */

/** 标签分类列表 */
export function getTagCategoryList() {
  return request.get('/admin/tag/category/list')
}

/** 新增/编辑分类 */
export function saveTagCategory(data) {
  return request.post('/admin/tag/category/save', data)
}

/**
 * 标签列表（后台）
 * @param {{categoryId?, status?, pageNum?, pageSize?}} params
 */
export function getTagList(params) {
  return request.get('/admin/tag/list', { params })
}

/** 新增/编辑标签 */
export function saveTag(data) {
  return request.post('/admin/tag/save', data)
}

/** 删除标签（逻辑删除） */
export function deleteTag(data) {
  return request.post('/admin/tag/delete', data)
}
