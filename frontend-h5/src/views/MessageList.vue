<script setup>
import { ref, onMounted, onActivated } from 'vue'
import { useRouter } from 'vue-router'
import { imApi } from '@/api'
import SoulAvatar from '@/components/SoulAvatar.vue'
import EmptyState from '@/components/EmptyState.vue'
import LoadingState from '@/components/LoadingState.vue'
import TabBar from '@/components/TabBar.vue'

const router = useRouter()

const conversations = ref([])
const loading = ref(true)
const total = ref(0)
const pageNum = ref(1)

async function loadList(reset = true) {
  if (reset) {
    pageNum.value = 1
    loading.value = true
  }
  try {
    const data = await imApi.conversationList({
      pageNum: pageNum.value,
      pageSize: 20
    })
    const records = data?.records || []
    total.value = data?.total || 0
    // AI 会话（sessionType=3）置顶排序
    conversations.value = records.sort((a, b) => {
      if (a.topFlag !== b.topFlag) return a.topFlag ? -1 : 1
      return 0
    })
  } catch (e) {
    conversations.value = []
  } finally {
    loading.value = false
  }
}

function goChat(conv) {
  router.push({ name: 'chat', params: { sessionUuid: conv.sessionUuid } })
}

onMounted(() => loadList(true))
onActivated(() => loadList(true))
</script>

<template>
  <div class="page messages-page">
    <div class="messages-header">
      <h1 class="messages-title gradient-text">消息</h1>
    </div>

    <LoadingState v-if="loading" :count="3" />

    <EmptyState
      v-else-if="!conversations.length"
      emoji="💌"
      text="还没有人在敲你的门"
      sub-text="去匹配页认识新朋友吧"
      action-text="去匹配"
      @action="router.push({ name: 'match' })"
    />

    <div v-else class="conv-list">
      <div
        v-for="conv in conversations"
        :key="conv.sessionUuid"
        class="conv-item"
        @click="goChat(conv)"
      >
        <div class="conv-avatar-wrap">
          <SoulAvatar :src="conv.targetUser?.avatar" :size="52" />
          <span v-if="conv.unreadCount > 0" class="unread-badge">
            {{ conv.unreadCount > 99 ? '99+' : conv.unreadCount }}
          </span>
        </div>
        <div class="conv-body">
          <div class="conv-top">
            <span class="conv-name ellipsis">
              <i v-if="conv.sessionType === 3" class="ai-tag">AI</i>
              {{ conv.targetUser?.nickname || '神秘用户' }}
            </span>
            <span class="conv-time">{{ conv.lastMsgTime }}</span>
          </div>
          <div class="conv-preview ellipsis">{{ conv.lastMsgPreview || '...' }}</div>
        </div>
      </div>
    </div>

    <TabBar />
  </div>
</template>

<style scoped lang="scss">
.messages-page {
  padding-bottom: 80px;
}
.messages-header {
  padding: 24px 20px 8px;
}
.messages-title {
  font-size: 26px;
  font-weight: 800;
  margin: 0;
}
.conv-list {
  padding: 8px 0;
}
.conv-item {
  display: flex;
  align-items: center;
  padding: 14px 20px;
  transition: background 0.15s ease;
  &:active {
    background: #f7f5fd;
  }
}
.conv-avatar-wrap {
  position: relative;
  margin-right: 14px;
}
.unread-badge {
  position: absolute;
  top: -4px;
  right: -4px;
  min-width: 18px;
  height: 18px;
  padding: 0 5px;
  border-radius: 9px;
  background: linear-gradient(90deg, #ff6fa5, #ff4d8d);
  color: #fff;
  font-size: 11px;
  line-height: 18px;
  text-align: center;
  box-shadow: 0 2px 6px rgba(255, 111, 165, 0.5);
}
.conv-body {
  flex: 1;
  overflow: hidden;
}
.conv-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.conv-name {
  font-size: 16px;
  font-weight: 600;
  color: var(--soul-text);
  display: flex;
  align-items: center;
}
.ai-tag {
  font-style: normal;
  font-size: 10px;
  color: #fff;
  background: linear-gradient(90deg, #7c5cff, #a78bfa);
  border-radius: 6px;
  padding: 1px 5px;
  margin-right: 6px;
}
.conv-time {
  font-size: 12px;
  color: var(--soul-text-hint);
  flex-shrink: 0;
  margin-left: 8px;
}
.conv-preview {
  font-size: 13px;
  color: var(--soul-text-hint);
  margin-top: 4px;
}
</style>
