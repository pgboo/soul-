<script setup>
import { ref, computed, nextTick, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showToast, showConfirmDialog } from 'vant'
import { imApi } from '@/api'
import { useUserStore } from '@/stores/user'
import SoulAvatar from '@/components/SoulAvatar.vue'
import EmptyState from '@/components/EmptyState.vue'
import LoadingState from '@/components/LoadingState.vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const sessionUuid = route.params.sessionUuid
const messages = ref([])
const loading = ref(true)
const sending = ref(false)
const inputText = ref('')
const scrollEl = ref(null)
const otherName = ref(route.query.nickname || '聊天')
let pollTimer = null

// ---- 侧滑返回（左缘右滑退出聊天）----
const swipe = { active: false, startX: 0, startY: 0, dx: 0 }
const edgeHintOpacity = ref(0)
const edgeHintX = ref(0)
/** 触发手势的左缘宽度(px) */
const EDGE_WIDTH = 36
/** 触发返回的滑动距离(px) */
const SWIPE_TRIGGER = 80

function goBack() {
  router.back()
}

function onSwipeStart(e) {
  const t = e.touches?.[0]
  if (!t) return
  if (t.clientX <= EDGE_WIDTH) {
    swipe.active = true
    swipe.startX = t.clientX
    swipe.startY = t.clientY
    swipe.dx = 0
  }
}

function onSwipeMove(e) {
  if (!swipe.active) return
  const t = e.touches?.[0]
  if (!t) return
  const dx = t.clientX - swipe.startX
  const dy = t.clientY - swipe.startY
  // 仅响应「向右且基本水平」的滑动，避免和上下滚动冲突
  if (dx > 0 && Math.abs(dy) < 60) {
    swipe.dx = dx
    edgeHintOpacity.value = Math.min(dx / SWIPE_TRIGGER, 1) * 0.9
    edgeHintX.value = Math.min(dx * 0.3, 30)
  }
}

function onSwipeEnd() {
  if (!swipe.active) return
  if (swipe.dx > SWIPE_TRIGGER) {
    router.back()
  }
  swipe.active = false
  swipe.dx = 0
  edgeHintOpacity.value = 0
  edgeHintX.value = 0
}

const selfId = computed(() => userStore.userId)
const canSend = computed(() => inputText.value.trim().length > 0)

// ---- 消息操作弹窗 ----
const showActions = ref(false)
const activeMsg = ref(null)

// ---- 转发 ----
const showForward = ref(false)
const forwardList = ref([])
const forwardLoading = ref(false)
const forwarding = ref(false)

/** 撤回时限：2 分钟（与后端一致） */
const RECALL_WINDOW_MS = 2 * 60 * 1000

const msgActions = computed(() => {
  const msg = activeMsg.value
  if (!msg) return []
  const list = [
    { name: '复制', key: 'copy' },
    { name: '转发', key: 'forward' }
  ]
  if (canRecall(msg)) {
    list.push({ name: '撤回', key: 'recall', color: '#ee0a24' })
  }
  list.push({ name: '删除', key: 'delete', color: '#ee0a24' })
  return list
})

function parseTime(t) {
  if (!t) return 0
  const ts = new Date(String(t).replace(' ', 'T')).getTime()
  return Number.isNaN(ts) ? 0 : ts
}

function isSelf(msg) {
  return String(msg.fromUserId) === String(selfId.value)
}

function canRecall(msg) {
  return isSelf(msg) && !msg.recalled && (Date.now() - parseTime(msg.sendTime)) < RECALL_WINDOW_MS
}

function openMsgActions(msg) {
  if (msg.recalled) return
  activeMsg.value = msg
  showActions.value = true
}

function onActionSelect(action) {
  showActions.value = false
  const msg = activeMsg.value
  if (!msg) return
  if (action.key === 'copy') {
    copyText(msg.content)
  } else if (action.key === 'forward') {
    openForward(msg)
  } else if (action.key === 'recall') {
    recallMsg(msg)
  } else if (action.key === 'delete') {
    deleteMsg(msg)
  }
}

/** 复制到剪贴板（含降级方案） */
async function copyText(text) {
  try {
    if (navigator.clipboard?.writeText) {
      await navigator.clipboard.writeText(text)
    } else {
      const ta = document.createElement('textarea')
      ta.value = text
      ta.style.position = 'fixed'
      ta.style.opacity = '0'
      document.body.appendChild(ta)
      ta.select()
      document.execCommand('copy')
      document.body.removeChild(ta)
    }
    showToast('已复制')
  } catch (e) {
    showToast('复制失败')
  }
}

/** 转发：选择目标会话 */
async function openForward(msg) {
  activeMsg.value = msg
  showForward.value = true
  if (forwardList.value.length || forwardLoading.value) return
  forwardLoading.value = true
  try {
    const data = await imApi.conversationList({ pageNum: 1, pageSize: 50 })
    forwardList.value = (data?.records || []).filter((c) => c.sessionUuid !== sessionUuid)
  } catch (e) {
    forwardList.value = []
  } finally {
    forwardLoading.value = false
  }
}

async function doForward(conv) {
  const msg = activeMsg.value
  if (!msg || forwarding.value) return
  forwarding.value = true
  try {
    await imApi.sendMessage(conv.sessionUuid, msg.content)
    showForward.value = false
    showToast('已转发给 ' + (conv.targetUser?.nickname || '对方'))
  } catch (e) {
    // 拦截器已提示
  } finally {
    forwarding.value = false
  }
}

/** 撤回（仅本人消息，2 分钟内） */
async function recallMsg(msg) {
  try {
    await showConfirmDialog({ title: '撤回消息', message: '确定撤回这条消息吗？' })
  } catch (e) {
    return
  }
  try {
    await imApi.recallMessage(msg.messageUuid)
    msg.recalled = true
    msg.content = ''
    showToast('已撤回')
  } catch (e) {
    // 拦截器已提示
  }
}

/** 删除（仅自己不可见） */
async function deleteMsg(msg) {
  try {
    await showConfirmDialog({
      title: '删除消息',
      message: '删除后将从你的消息记录中移除（对方不受影响）'
    })
  } catch (e) {
    return
  }
  try {
    await imApi.deleteMessage(msg.messageUuid)
    messages.value = messages.value.filter((m) => m.messageUuid !== msg.messageUuid)
    showToast('已删除')
  } catch (e) {
    // 拦截器已提示
  }
}

async function loadMessages() {
  try {
    const data = await imApi.messageList({
      sessionUuid,
      pageNum: 1,
      pageSize: 50
    })
    const records = (data?.records || []).slice().reverse()
    messages.value = records
    if (!route.query.nickname && records.length) {
      const first = records.find((m) => String(m.fromUserId) !== String(selfId.value))
      if (first) otherName.value = '聊天'
    }
    await scrollToBottom()
  } catch (e) {
    // 拦截器已提示
  } finally {
    loading.value = false
  }
}

async function markRead() {
  try {
    await imApi.readConversation(sessionUuid)
  } catch (e) {
    // 忽略
  }
}

async function send() {
  if (!canSend.value || sending.value) return
  const content = inputText.value.trim()
  sending.value = true
  try {
    const data = await imApi.sendMessage(sessionUuid, content)
    messages.value.push({
      messageUuid: data?.messageUuid || `local-${Date.now()}`,
      fromUserId: selfId.value,
      toUserId: null,
      content,
      contentType: 'text',
      sendStatus: 1,
      readFlag: 0,
      recalled: false,
      sendTime: data?.sendTime || new Date().toISOString()
    })
    inputText.value = ''
    await scrollToBottom()
  } catch (e) {
    // 拦截器已提示
  } finally {
    sending.value = false
  }
}

async function scrollToBottom() {
  await nextTick()
  if (scrollEl.value) {
    scrollEl.value.scrollTop = scrollEl.value.scrollHeight
  }
}

function startPoll() {
  // 拉取式：每 5s 刷新消息（无 WebSocket）
  pollTimer = setInterval(() => {
    loadMessages()
    markRead()
  }, 5000)
}

onMounted(async () => {
  await loadMessages()
  await markRead()
  startPoll()
  scrollToBottom()
})

onUnmounted(() => {
  if (pollTimer) clearInterval(pollTimer)
})
</script>

<template>
  <div class="page chat-page" @touchstart.passive="onSwipeStart" @touchmove.passive="onSwipeMove" @touchend="onSwipeEnd" @touchcancel="onSwipeEnd">
    <!-- 自定义头部：大号返回键（48px 触控区） -->
    <div class="chat-header">
      <button class="back-btn" type="button" aria-label="返回" @click="goBack">
        <span class="back-arrow"></span>
      </button>
      <div class="chat-title">{{ otherName }}</div>
      <div class="header-right"></div>
    </div>
    <div class="header-placeholder"></div>

    <LoadingState v-if="loading" :count="1" />

    <div v-else ref="scrollEl" class="chat-body">
      <EmptyState
        v-if="!messages.length"
        emoji="👋"
        text="还没有消息，打个招呼吧"
      />

      <div
        v-for="msg in messages"
        :key="msg.messageUuid"
        class="msg-row"
        :class="{ self: isSelf(msg), recalled: msg.recalled }"
      >
        <template v-if="msg.recalled">
          <div class="msg-recalled">{{ isSelf(msg) ? '你' : '对方' }}撤回了一条消息</div>
        </template>
        <template v-else>
          <SoulAvatar v-if="!isSelf(msg)" :src="''" :size="36" class="msg-avatar" />
          <div class="msg-bubble" @click="openMsgActions(msg)">
            <div class="msg-text">{{ msg.content }}</div>
            <div v-if="isSelf(msg) && msg.readFlag === 1" class="msg-read">已读</div>
          </div>
        </template>
      </div>
    </div>

    <div class="chat-input-bar">
      <input
        v-model="inputText"
        class="chat-input"
        maxlength="500"
        placeholder="说点什么..."
        @keyup.enter="send"
      />
      <button
        class="chat-send"
        :class="{ disabled: !canSend }"
        :disabled="!canSend || sending"
        @click="send"
      >
        发送
      </button>
    </div>

    <!-- 消息操作弹窗：复制 / 转发 / 撤回 / 删除 -->
    <van-action-sheet
      v-model:show="showActions"
      :actions="msgActions"
      cancel-text="取消"
      close-on-click-action
      @select="onActionSelect"
    />

    <!-- 转发：选择会话 -->
    <van-popup v-model:show="showForward" position="bottom" round>
      <div class="forward-panel">
        <div class="forward-title">转发给</div>
        <LoadingState v-if="forwardLoading" :count="1" />
        <EmptyState
          v-else-if="!forwardList.length"
          emoji="💬"
          text="没有可转发的会话"
          sub-text="先去匹配页打个招呼吧"
        />
        <div v-else class="forward-list">
          <div
            v-for="conv in forwardList"
            :key="conv.sessionUuid"
            class="forward-item"
            @click="doForward(conv)"
          >
            <SoulAvatar :src="conv.targetUser?.avatar || ''" :size="40" />
            <div class="forward-info">
              <div class="forward-name">{{ conv.targetUser?.nickname || '用户' }}</div>
              <div class="forward-preview">{{ conv.lastMsgPreview || '开始聊天吧' }}</div>
            </div>
            <span class="forward-go">转发</span>
          </div>
        </div>
      </div>
    </van-popup>

    <!-- 侧滑返回提示箭头 -->
    <div
      class="edge-hint"
      :style="{ opacity: edgeHintOpacity, transform: 'translateY(-50%) translateX(' + edgeHintX + 'px)' }"
    >‹</div>
  </div>
</template>

<style scoped lang="scss">
/* 自定义头部 */
.chat-header {
  position: fixed;
  top: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 100%;
  max-width: 500px;
  z-index: 100;
  display: flex;
  align-items: center;
  height: calc(52px + env(safe-area-inset-top));
  padding-top: env(safe-area-inset-top);
  background: rgba(255, 255, 255, 0.96);
  backdrop-filter: blur(12px);
  border-bottom: 1px solid var(--soul-line);
}
.back-btn {
  width: 52px;
  height: 100%;
  min-height: 52px;
  border: none;
  background: transparent;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  -webkit-tap-highlight-color: transparent;
  &:active {
    background: #f0ebfa;
  }
}
.back-arrow {
  width: 13px;
  height: 13px;
  border-left: 3px solid var(--soul-text);
  border-bottom: 3px solid var(--soul-text);
  transform: rotate(45deg);
  margin-left: 8px;
}
.chat-title {
  flex: 1;
  text-align: center;
  font-size: 17px;
  font-weight: 600;
  color: var(--soul-text);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.header-right {
  width: 52px;
  flex-shrink: 0;
}
.header-placeholder {
  height: calc(52px + env(safe-area-inset-top));
  flex-shrink: 0;
}

/* 侧滑返回提示 */
.edge-hint {
  position: fixed;
  left: 8px;
  top: 50%;
  z-index: 200;
  font-size: 34px;
  font-weight: 300;
  color: var(--soul-primary);
  pointer-events: none;
  opacity: 0;
  transition: opacity 0.15s;
  text-shadow: 0 2px 10px rgba(124, 92, 255, 0.45);
}

.chat-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}
.chat-body {
  flex: 1;
  padding: 16px 16px 90px;
  overflow-y: auto;
}
.msg-row {
  display: flex;
  margin-bottom: 16px;
  align-items: flex-start;
  &.self {
    flex-direction: row-reverse;
    .msg-bubble {
      background: linear-gradient(120deg, #7c5cff, #9b7cff);
      color: #fff;
      border-radius: 16px 4px 16px 16px;
    }
    .msg-text {
      color: #fff;
    }
    .msg-read {
      color: rgba(255, 255, 255, 0.75);
    }
  }
  &.recalled {
    justify-content: center;
  }
}
.msg-avatar {
  margin-right: 10px;
}
.msg-bubble {
  max-width: 68%;
  background: #fff;
  border-radius: 4px 16px 16px 16px;
  padding: 10px 14px;
  box-shadow: var(--soul-shadow);
  position: relative;
  cursor: pointer;
  transition: transform 0.12s;
  &:active {
    transform: scale(0.97);
  }
}
.msg-text {
  font-size: 15px;
  line-height: 1.5;
  word-break: break-word;
  color: var(--soul-text);
}
.msg-read {
  font-size: 10px;
  color: var(--soul-text-hint);
  text-align: right;
  margin-top: 2px;
}
.msg-recalled {
  font-size: 12px;
  color: var(--soul-text-hint);
  background: rgba(124, 92, 255, 0.08);
  border-radius: 10px;
  padding: 4px 12px;
}
.chat-input-bar {
  position: fixed;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 100%;
  max-width: 500px;
  display: flex;
  gap: 10px;
  padding: 10px 16px calc(10px + env(safe-area-inset-bottom));
  background: rgba(255, 255, 255, 0.96);
  backdrop-filter: blur(12px);
  border-top: 1px solid var(--soul-line);
  z-index: 100;
}
.chat-input {
  flex: 1;
  height: 42px;
  border: none;
  outline: none;
  background: #f4f1fb;
  border-radius: 21px;
  padding: 0 18px;
  font-size: 15px;
  &::placeholder {
    color: var(--soul-text-hint);
  }
}
.chat-send {
  flex-shrink: 0;
  height: 42px;
  padding: 0 22px;
  border: none;
  border-radius: 21px;
  background: linear-gradient(90deg, #7c5cff, #ff6fa5);
  color: #fff;
  font-size: 14px;
  font-weight: 600;
  &.disabled {
    opacity: 0.5;
  }
}

/* 转发面板 */
.forward-panel {
  padding: 18px 16px calc(18px + env(safe-area-inset-bottom));
  max-height: 60vh;
  overflow-y: auto;
}
.forward-title {
  font-size: 16px;
  font-weight: 700;
  text-align: center;
  margin-bottom: 12px;
}
.forward-item {
  display: flex;
  align-items: center;
  padding: 10px 4px;
  border-bottom: 1px solid var(--soul-line);
  &:last-child {
    border-bottom: none;
  }
  &:active {
    background: #f7f4fd;
  }
}
.forward-info {
  flex: 1;
  margin-left: 12px;
  min-width: 0;
}
.forward-name {
  font-size: 15px;
  font-weight: 600;
}
.forward-preview {
  font-size: 12px;
  color: var(--soul-text-hint);
  margin-top: 2px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.forward-go {
  flex-shrink: 0;
  font-size: 13px;
  font-weight: 600;
  color: var(--soul-primary);
  border: 1px solid rgba(124, 92, 255, 0.4);
  border-radius: 14px;
  padding: 4px 14px;
}
</style>