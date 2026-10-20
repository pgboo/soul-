<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import { matchApi, greetApi } from '@/api'
import SoulAvatar from '@/components/SoulAvatar.vue'
import EmptyState from '@/components/EmptyState.vue'
import LoadingState from '@/components/LoadingState.vue'
import TabBar from '@/components/TabBar.vue'

const router = useRouter()

const list = ref([])
const loading = ref(true)
const refreshing = ref(false)
const greetingId = ref(null)

const defaultGreetings = [
  '嗨，看到你也喜欢{tag}～',
  '我们好像有很多共同话题，聊聊天？',
  '你的标签很有意思，交个朋友吧！'
]

async function loadRecommend() {
  loading.value = true
  try {
    const data = await matchApi.recommend({ pageNum: 1, pageSize: 10 })
    list.value = data?.records || []
  } catch (e) {
    list.value = []
  } finally {
    loading.value = false
  }
}

async function refresh() {
  refreshing.value = true
  await loadRecommend()
  refreshing.value = false
  showToast('已为你换一批')
}

/** 不感兴趣 —— 从当前列表移除 + 记录 */
async function onDislike(user) {
  const idx = list.value.findIndex((u) => u.userId === user.userId)
  if (idx >= 0) list.value.splice(idx, 1)
  try {
    await matchApi.dislike(user.userId, '不感兴趣')
  } catch (e) {
    // 已移除，失败也不恢复
  }
}

/** 打招呼 */
async function onGreet(user) {
  if (greetingId.value) return
  greetingId.value = user.userId
  const tag = user.tags?.[0] || '这个'
  const content = defaultGreetings[0].replace('{tag}', tag)
  try {
    await greetApi.send(user.userId, content)
    showToast('打招呼成功，去消息看看吧')
  } catch (e) {
    // 拦截器已提示
  } finally {
    greetingId.value = null
  }
}

function goUser(user) {
  router.push({ name: 'user-home', params: { id: user.userId } })
}

onMounted(loadRecommend)
</script>

<template>
  <div class="page match-page">
    <div class="match-header">
      <div class="header-row">
        <h1 class="match-title gradient-text">遇见灵魂</h1>
        <button class="refresh-btn" @click="refresh">
          {{ refreshing ? '...' : '换一批' }}
        </button>
      </div>
      <p class="match-sub">根据你的标签与灵魂类型推荐</p>
    </div>

    <LoadingState v-if="loading" :count="2" />

    <EmptyState
      v-else-if="!list.length"
      emoji="💫"
      text="暂时没有遇到合适的灵魂"
      sub-text="完善资料和标签，会更容易被看见"
      action-text="换一批"
      @action="refresh"
    />

    <div v-else class="match-list">
      <div v-for="user in list" :key="user.userId" class="match-card">
        <div class="card-top" @click="goUser(user)">
          <div class="avatar-wrap">
            <SoulAvatar :src="user.avatar" :size="64" />
            <div class="match-score">
              <span class="score-num">{{ user.matchScore }}</span>
              <span class="score-label">匹配</span>
            </div>
          </div>
          <div class="card-info">
            <div class="card-name-row">
              <span class="card-name">{{ user.nickname }}</span>
              <span v-if="user.age" class="card-age">{{ user.age }}岁</span>
            </div>
            <div class="card-meta">
              <span v-if="user.city">📍 {{ user.city }}</span>
            </div>
            <div v-if="user.reason" class="card-reason">{{ user.reason }}</div>
          </div>
        </div>

        <div v-if="user.tags && user.tags.length" class="card-tags">
          <span v-for="(t, i) in user.tags" :key="i" class="tag">{{ t }}</span>
        </div>

        <div class="card-actions">
          <button class="act-btn act-dislike" @click="onDislike(user)">不感兴趣</button>
          <button
            class="act-btn act-greet"
            :disabled="greetingId === user.userId"
            @click="onGreet(user)"
          >
            {{ greetingId === user.userId ? '发送中...' : '打招呼' }}
          </button>
        </div>
      </div>
    </div>

    <TabBar />
  </div>
</template>

<style scoped lang="scss">
.match-page {
  padding-bottom: 80px;
}
.match-header {
  padding: 24px 20px 8px;
}
.header-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.match-title {
  font-size: 26px;
  font-weight: 800;
  margin: 0;
}
.refresh-btn {
  border: none;
  background: rgba(124, 92, 255, 0.1);
  color: var(--soul-primary);
  font-size: 13px;
  font-weight: 600;
  padding: 8px 16px;
  border-radius: 18px;
}
.match-sub {
  font-size: 13px;
  color: var(--soul-text-sub);
  margin: 8px 0 0;
}
.match-list {
  padding: 8px 16px;
}
.match-card {
  background: #fff;
  border-radius: 20px;
  box-shadow: var(--soul-shadow);
  padding: 18px;
  margin-bottom: 14px;
}
.card-top {
  display: flex;
  align-items: center;
}
.avatar-wrap {
  position: relative;
  margin-right: 14px;
}
.match-score {
  position: absolute;
  bottom: -6px;
  left: 50%;
  transform: translateX(-50%);
  background: linear-gradient(90deg, #7c5cff, #ff6fa5);
  color: #fff;
  border-radius: 10px;
  padding: 2px 8px;
  display: flex;
  align-items: baseline;
  gap: 2px;
  white-space: nowrap;
  box-shadow: 0 3px 8px rgba(124, 92, 255, 0.35);
}
.score-num {
  font-size: 13px;
  font-weight: 700;
}
.score-label {
  font-size: 9px;
}
.card-info {
  flex: 1;
  overflow: hidden;
}
.card-name-row {
  display: flex;
  align-items: center;
  gap: 8px;
}
.card-name {
  font-size: 17px;
  font-weight: 700;
}
.card-age {
  font-size: 12px;
  color: var(--soul-text-sub);
  background: #f4f1fb;
  border-radius: 8px;
  padding: 1px 8px;
}
.card-meta {
  font-size: 12px;
  color: var(--soul-text-hint);
  margin-top: 4px;
}
.card-reason {
  font-size: 13px;
  color: var(--soul-primary);
  margin-top: 6px;
}
.card-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin: 14px 0;
}
.tag {
  font-size: 12px;
  color: var(--soul-text-sub);
  background: #f4f1fb;
  border-radius: 14px;
  padding: 4px 12px;
}
.card-actions {
  display: flex;
  gap: 12px;
}
.act-btn {
  flex: 1;
  height: 42px;
  border: none;
  border-radius: 21px;
  font-size: 14px;
  font-weight: 600;
}
.act-dislike {
  background: #f4f1fb;
  color: var(--soul-text-sub);
}
.act-greet {
  background: linear-gradient(90deg, #7c5cff, #ff6fa5);
  color: #fff;
  box-shadow: 0 6px 14px rgba(124, 92, 255, 0.3);
}
</style>
