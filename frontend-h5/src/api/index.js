import request from './request'

/**
 * 接口定义层 —— 严格按 docs/03-API接口文档.md 契约实现。
 * 每个方法返回 Promise<data>（已由 axios 拦截器解包 Result）。
 */

// ==================== 账号登录 ====================
export const authApi = {
  /** 发送验证码 */
  sendCode: (phone) => request.post('/mini/auth/code', { phone }),
  /** 手机号验证码登录 */
  login: (phone, code) => request.post('/mini/auth/login', { phone, code }),
  /** 退出登录 */
  logout: () => request.post('/mini/auth/logout')
}

// ==================== 用户资料 ====================
export const userApi = {
  /** 登录后初始化状态（分流） */
  initStatus: () => request.get('/mini/user/initStatus'),
  /** 我的资料 */
  profile: () => request.get('/mini/user/profile'),
  /** 完善资料 */
  completeProfile: (data) => request.post('/mini/user/profile/complete', data),
  /** 编辑资料 */
  updateProfile: (data) => request.post('/mini/user/profile/update', data),
  /** 用户主页 */
  home: (userId) => request.get(`/mini/user/home/${userId}`),
  /** 更新隐私设置 */
  updatePrivacy: (data) => request.post('/mini/user/privacy/update', data),
  /** 拉黑用户 */
  blacklistAdd: (targetUserId) =>
    request.post('/mini/user/blacklist/add', { targetUserId }),
  /** 取消拉黑 */
  blacklistRemove: (targetUserId) =>
    request.post('/mini/user/blacklist/remove', { targetUserId }),
  /** 黑名单列表 */
  blacklistList: (params) => request.get('/mini/user/blacklist/list', { params })
}

// ==================== 兴趣标签 ====================
export const tagApi = {
  /** 标签列表（分类聚合） */
  list: () => request.get('/mini/tag/list'),
  /** 保存我的标签 */
  save: (tagIds) => request.post('/mini/user/tag/save', { tagIds })
}

// ==================== 灵魂测试 ====================
export const soulTestApi = {
  /** 题目列表 */
  questions: () => request.get('/mini/soulTest/questions'),
  /** 提交答案 */
  submit: (answers) => request.post('/mini/soulTest/submit', { answers }),
  /** 我的测试结果 */
  result: () => request.get('/mini/soulTest/result')
}

// ==================== 广场瞬间 ====================
export const dynamicApi = {
  /** 发布瞬间 */
  publish: (data) => request.post('/mini/dynamic/publish', data),
  /** 瞬间列表 */
  list: (params) => request.get('/mini/dynamic/list', { params }),
  /** 瞬间详情 */
  detail: (id) => request.get(`/mini/dynamic/detail/${id}`),
  /** 删除瞬间 */
  remove: (id) => request.post(`/mini/dynamic/delete/${id}`),
  /** 点赞/取消点赞 */
  like: (dynamicId, action) =>
    request.post('/mini/dynamic/like', { dynamicId, action }),
  /** 收藏/取消收藏 */
  collect: (dynamicId, action) =>
    request.post('/mini/dynamic/collect', { dynamicId, action }),
  /** 发表评论/回复 */
  comment: (data) => request.post('/mini/dynamic/comment', data),
  /** 评论列表 */
  commentList: (params) => request.get('/mini/dynamic/comment/list', { params })
}

// ==================== 话题 ====================
export const topicApi = {
  /** 话题列表 */
  list: () => request.get('/mini/topic/list'),
  /** 自拟话题（同名已存在则复用），返回 { topicId, name, ... } */
  create: (name) => request.post('/mini/topic/create', { name })
}

// ==================== 匹配 ====================
export const matchApi = {
  /** 推荐用户 */
  recommend: (params) => request.get('/mini/match/recommend', { params }),
  /** 不感兴趣 */
  dislike: (targetUserId, reason) =>
    request.post('/mini/match/dislike', { targetUserId, reason })
}

// ==================== 打招呼 ====================
export const greetApi = {
  /** 发送打招呼 */
  send: (toUserId, content) => request.post('/mini/greet/send', { toUserId, content })
}

// ==================== IM 聊天 ====================
export const imApi = {
  /** 会话列表 */
  conversationList: (params) =>
    request.get('/mini/im/conversation/list', { params }),
  /** 消息列表 */
  messageList: (params) => request.get('/mini/im/message/list', { params }),
  /** 发送消息 */
  sendMessage: (sessionUuid, content) =>
    request.post('/mini/im/message/send', { sessionUuid, content }),
  /** 标记已读 */
  readConversation: (sessionUuid) =>
    request.post('/mini/im/conversation/read', { sessionUuid }),
  /** 撤回消息（2 分钟内，仅本人消息） */
  recallMessage: (messageUuid) =>
    request.post('/mini/im/message/recall', { messageUuid }),
  /** 删除消息（仅自己不可见） */
  deleteMessage: (messageUuid) =>
    request.post('/mini/im/message/delete', { messageUuid })
}

// ==================== 站内通知 ====================
export const noticeApi = {
  /** 通知列表 */
  list: (params) => request.get('/mini/notice/list', { params }),
  /** 通知已读 */
  read: (noticeIds) => request.post('/mini/notice/read', { noticeIds })
}

// ==================== 文件上传 ====================
export const uploadApi = {
  /**
   * 上传图片
   * @param {File} file 图片文件
   * @param {string} bizType avatar / dynamic
   */
  image: (file, bizType = 'dynamic') => {
    const form = new FormData()
    form.append('file', file)
    form.append('bizType', bizType)
    return request.post('/mini/upload/image', form, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  }
}
