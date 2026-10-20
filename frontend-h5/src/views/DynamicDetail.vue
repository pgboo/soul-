<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showToast, showConfirmDialog } from 'vant'
import { dynamicApi } from '@/api'
import SoulAvatar from '@/components/SoulAvatar.vue'
import EmptyState from '@/components/EmptyState.vue'
import { resolveAssetUrl } from '@/utils/asset'
import LoadingState from '@/components/LoadingState.vue'

const route = useRoute()
const router = useRouter()

const dynamicId = route.params.id
const detail = ref(null)
const comments = ref([])
const loading = ref(true)
const loadFailed = ref(false)
const sending = ref(false)
const commentText = ref('')
const replyTarget = ref(null) // { commentId, userId, nickname }
const pageNum = ref(1)
const total = ref(0)
const loadingMore = ref(false)

const canSend = computed(() => commentText.value.trim().length > 0)

async function loadDetail() {
  loading.value = true
  loadFailed.value = false
  try {
    detail.value = await dynamicApi.detail(dynamicId)
  } catch (e) {
    loadFailed.value = true
  } finally {
    loading.value = false
  }
}

async function loadComments(reset = false) {
  if (reset) pageNum.value = 1
  try {
    const data = await dynamicApi.commentList({
      dynamicId,
      pageNum: pageNum.value,
      pageSize: 10
    })
    const records = data?.records || []
    total.value = data?.total || 0
    comments.value = reset ? records : comments.value.concat(records)
  } catch (e) {
    if (reset) comments.value = []
  }
}

function loadMoreComments() {
  if (loadingMore.value) return
  if (comments.value.length >= total.value) return
  loadingMore.value = true
  pageNum.value += 1
  loadComments(false).finally(() => {
    loadingMore.value = false
  })
}

async function onLike() {
  if (!detail.value) return
  const nextLiked = !detail.value.liked
  detail.value.liked = nextLiked
  detail.value.likeCount = Math.max(0, (detail.value.likeCount || 0) + (nextLiked ? 1 : -1))
  try {
    const data = await dynamicApi.like(detail.value.dynamicId, nextLiked ? 'like' : 'cancel')
    detail.value.liked = data.liked
    detail.value.likeCount = data.likeCount
  } catch (e) {
    detail.value.liked = !nextLiked
    detail.value.likeCount = (detail.value.likeCount || 0) + (nextLiked ? -1 : 1)
  }
}

async function onCollect() {
  if (!detail.value) return
  const nextCollected = !detail.value.collected
  detail.value.collected = nextCollected
  detail.value.collectCount = Math.max(
    0,
    (detail.value.collectCount || 0) + (nextCollected ? 1 : -1)
  )
  try {
    const data = await dynamicApi.collect(
      detail.value.dynamicId,
      nextCollected ? 'collect' : 'cancel'
    )
    detail.value.collected = data.collected
    detail.value.collectCount = data.collectCount
  } catch (e) {
    detail.value.collected = !nextCollected
    detail.value.collectCount = (detail.value.collectCount || 0) + (nextCollected ? -1 : 1)
  }
}

function startReply(comment) {
  replyTarget.value = {
    commentId: comment.commentId,
    userId: comment.user?.userId,
    nickname: comment.user?.nickname
  }
}

function cancelReply() {
  replyTarget.value = null
}

async function sendComment() {
  if (!canSend.value || sending.value) return
  sending.value = true
  try {
    await dynamicApi.comment({
      dynamicId: Number(dynamicId),
      content: commentText.value.trim(),
      parentId: replyTarget.value ? replyTarget.value.commentId : 0,
      replyUserId: replyTarget.value ? replyTarget.value.userId : 0
    })
    showToast('评论成功')
    commentText.value = ''
    replyTarget.value = null
    if (detail.value) {
      detail.value.commentCount = (detail.value.commentCount || 0) + 1
    }
    await loadComments(true)
  } catch (e) {
    // 拦截器已提示
  } finally {
    sending.value = false
  }
}

async function handleDelete() {
  try {
    await showConfirmDialog({
      title: '删除瞬间',
      message: '删除后将无法恢复，确定吗？'
    })
  } catch (e) {
    return
  }
  try {
    await dynamicApi.remove(dynamicId)
    showToast('已删除')
    router.back()
  } catch (e) {
    // 拦截器已提示
  }
}

function goUser() {
  if (detail.value?.author?.userId) {
    router.push({ name: 'user-home', params: { id: detail.value.author.userId } })
  }
}

onMounted(() => {
  loadDetail()
  loadComments(true)
})
</script>

<template>
  <div class="page detail-page">
    <van-nav-bar title="瞬间详情" left-arrow @click-left="router.back()" fixed placeholder />

    <LoadingState v-if="loading" :count="1" />

    <EmptyState
      v-else-if="loadFailed || !detail"
      emoji="🌫️"
      text="这条瞬间已经飘走了"
      sub-text="可能被删除或暂不可见"
      action-text="回到广场"
      @action="router.replace({ name: 'square' })"
    />

    <template v-else>
      <div class="soul-card">
        <div class="author-row" @click="goUser">
          <SoulAvatar :src="detail.author?.avatar" :size="44" />
          <div class="author-info">
            <div class="author-name">{{ detail.author?.nickname || '神秘用户' }}</div>
            <div class="author-time">{{ detail.createTime }}</div>
          </div>
          <span v-if="detail.isAuthor" class="delete-btn" @click.stop="handleDelete">
            删除
          </span>
        </div>

        <p v-if="detail.content" class="detail-content">{{ detail.content }}</p>

        <div v-if="detail.mediaUrls && detail.mediaUrls.length" class="detail-media">
          <img
            v-for="(url, idx) in detail.mediaUrls"
            :key="idx"
            :src="resolveAssetUrl(url)"
            class="detail-media-img"
            alt="media"
          />
        </div>

        <div v-if="detail.topicName" class="detail-topic"># {{ detail.topicName }}</div>

        <div class="detail-actions">
          <span class="detail-action" :class="{ active: detail.liked }" @click="onLike">
            {{ detail.liked ? '💜' : '🤍' }} {{ detail.likeCount || 0 }}
          </span>
          <span class="detail-action" :class="{ active: detail.collected }" @click="onCollect">
            {{ detail.collected ? '⭐' : '☆' }} {{ detail.collectCount || 0 }}
          </span>
        </div>
      </div>

      <!-- 评论列表 -->
      <div class="comment-section">
        <div class="comment-title">全部评论 ({{ total }})</div>

        <EmptyState
          v-if="!comments.length"
          emoji="💬"
          text="还没有人说话"
          sub-text="来抢个沙发吧"
        />

        <div v-else class="comment-list">
          <div v-for="c in comments" :key="c.commentId" class="comment-item">
            <SoulAvatar :src="c.user?.avatar" :size="36" />
            <div class="comment-body">
              <div class="comment-name">{{ c.user?.nickname || '神秘用户' }}</div>
              <div class="comment-content">
                <span v-if="c.replyUser" class="reply-to">
                  回复 {{ c.replyUser.nickname }}：
                </span>{{ c.content }}
              </div>
              <div class="comment-meta">
                <span>{{ c.createTime }}</span>
                <span class="comment-reply" @click="startReply(c)">回复</span>
              </div>
            </div>
          </div>

          <div
            v-if="comments.length < total"
            class="load-more"
            @click="loadMoreComments"
          >
            {{ loadingMore ? '加载中...' : '加载更多评论' }}
          </div>
        </div>
      </div>
    </template>

    <!-- 评论输入栏 -->
    <div v-if="detail" class="comment-input-bar">
      <div v-if="replyTarget" class="reply-bar">
        回复 @{{ replyTarget.nickname }}
        <span class="reply-cancel" @click="cancelReply">×</span>
      </div>
      <div class="input-row">
        <input
          v-model="commentText"
          class="comment-input"
          maxlength="200"
          :placeholder="replyTarget ? '回复点什么...' : '说点什么...'"
          @keyup.enter="sendComment"
        />
        <button
          class="send-btn"
          :class="{ disabled: !canSend }"
          :disabled="!canSend || sending"
          @click="sendComment"
        >
          发送
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped lang="scss">
.detail-page {
  padding-bottom: 90px;
}
.author-row {
  display: flex;
  align-items: center;
  margin-bottom: 14px;
}
.author-info {
  flex: 1;
  margin-left: 12px;
}
.author-name {
  font-size: 16px;
  font-weight: 600;
}
.author-time {
  font-size: 12px;
  color: var(--soul-text-hint);
  margin-top: 2px;
}
.delete-btn {
  font-size: 13px;
  color: #ff6fa5;
}
.detail-content {
  font-size: 16px;
  line-height: 1.7;
  white-space: pre-wrap;
  word-break: break-word;
  margin: 0 0 14px;
}
.detail-media {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 14px;
}
.detail-media-img {
  width: 100%;
  border-radius: 12px;
  background: #f0ecfa;
}
.detail-topic {
  display: inline-block;
  font-size: 13px;
  color: var(--soul-primary);
  background: rgba(124, 92, 255, 0.08);
  border-radius: 12px;
  padding: 4px 12px;
  margin-bottom: 14px;
}
.detail-actions {
  display: flex;
  gap: 32px;
  border-top: 1px solid var(--soul-line);
  padding-top: 14px;
}
.detail-action {
  font-size: 14px;
  color: var(--soul-text-sub);
  &.active {
    color: var(--soul-primary);
    font-weight: 600;
  }
}
.comment-section {
  margin: 20px 16px 0;
}
.comment-title {
  font-size: 15px;
  font-weight: 700;
  margin-bottom: 12px;
}
.comment-item {
  display: flex;
  margin-bottom: 18px;
}
.comment-body {
  flex: 1;
  margin-left: 10px;
}
.comment-name {
  font-size: 13px;
  color: var(--soul-text-sub);
  font-weight: 600;
}
.comment-content {
  font-size: 15px;
  line-height: 1.5;
  margin: 4px 0;
  word-break: break-word;
}
.reply-to {
  color: var(--soul-primary);
}
.comment-meta {
  display: flex;
  gap: 16px;
  font-size: 12px;
  color: var(--soul-text-hint);
}
.comment-reply {
  color: var(--soul-primary);
}
.load-more {
  text-align: center;
  font-size: 13px;
  color: var(--soul-primary);
  padding: 8px 0;
}
.comment-input-bar {
  position: fixed;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 100%;
  max-width: 500px;
  background: rgba(255, 255, 255, 0.96);
  backdrop-filter: blur(12px);
  border-top: 1px solid var(--soul-line);
  padding: 10px 16px calc(10px + env(safe-area-inset-bottom));
  z-index: 100;
}
.reply-bar {
  font-size: 12px;
  color: var(--soul-text-sub);
  margin-bottom: 6px;
}
.reply-cancel {
  color: #ff6fa5;
  margin-left: 8px;
}
.input-row {
  display: flex;
  gap: 10px;
  align-items: center;
}
.comment-input {
  flex: 1;
  height: 40px;
  border: none;
  outline: none;
  background: #f4f1fb;
  border-radius: 20px;
  padding: 0 16px;
  font-size: 14px;
  &::placeholder {
    color: var(--soul-text-hint);
  }
}
.send-btn {
  flex-shrink: 0;
  height: 40px;
  padding: 0 20px;
  border: none;
  border-radius: 20px;
  background: linear-gradient(90deg, #7c5cff, #ff6fa5);
  color: #fff;
  font-size: 14px;
  font-weight: 600;
  &.disabled {
    opacity: 0.5;
  }
}
</style>
