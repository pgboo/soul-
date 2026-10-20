<script setup>
import { ref, onMounted, onActivated } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import { dynamicApi } from '@/api'
import DynamicCard from '@/components/DynamicCard.vue'
import EmptyState from '@/components/EmptyState.vue'
import LoadingState from '@/components/LoadingState.vue'
import TabBar from '@/components/TabBar.vue'

const router = useRouter()

const channels = [
  { name: 'recommend', label: '推荐' },
  { name: 'latest', label: '最新' },
  { name: 'city', label: '同城' }
]
const activeChannel = ref('recommend')

const list = ref([])
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)
const loading = ref(false)
const loadingMore = ref(false)
const finished = ref(false)

const tabs = [
  { name: 'square', label: '广场', icon: '🪐' },
  { name: 'match', label: '匹配', icon: '💫' },
  { name: 'messages', label: '消息', icon: '💌' },
  { name: 'mine', label: '我的', icon: '🙂' }
]

async function loadList(reset = false) {
  if (reset) {
    pageNum.value = 1
    finished.value = false
    if (list.value.length === 0) loading.value = true
  }
  const isFirst = pageNum.value === 1
  if (isFirst) loading.value = true
  else loadingMore.value = true

  try {
    const data = await dynamicApi.list({
      channel: activeChannel.value,
      pageNum: pageNum.value,
      pageSize: pageSize.value
    })
    const records = data?.records || []
    total.value = data?.total || 0
    if (reset || pageNum.value === 1) {
      list.value = records
    } else {
      list.value = list.value.concat(records)
    }
    if (list.value.length >= total.value || records.length === 0) {
      finished.value = true
    }
  } catch (e) {
    finished.value = true
  } finally {
    loading.value = false
    loadingMore.value = false
  }
}

function onChannelChange() {
  loadList(true)
}

function onLoadMore() {
  if (finished.value || loading.value || loadingMore.value) return
  pageNum.value += 1
  loadList(false)
}

/** 点赞/取消点赞 —— 乐观更新 + 接口校正 */
async function onLike(item) {
  const nextLiked = !item.liked
  const nextCount = (item.likeCount || 0) + (nextLiked ? 1 : -1)
  item.liked = nextLiked
  item.likeCount = Math.max(0, nextCount)
  try {
    const data = await dynamicApi.like(item.dynamicId, nextLiked ? 'like' : 'cancel')
    item.liked = data.liked
    item.likeCount = data.likeCount
  } catch (e) {
    // 回滚
    item.liked = !nextLiked
    item.likeCount = (item.likeCount || 0) + (nextLiked ? -1 : 1)
  }
}

/** 收藏/取消收藏 */
async function onCollect(item) {
  const nextCollected = !item.collected
  item.collected = nextCollected
  item.collectCount = Math.max(0, (item.collectCount || 0) + (nextCollected ? 1 : -1))
  try {
    const data = await dynamicApi.collect(item.dynamicId, nextCollected ? 'collect' : 'cancel')
    item.collected = data.collected
    item.collectCount = data.collectCount
  } catch (e) {
    item.collected = !nextCollected
    item.collectCount = (item.collectCount || 0) + (nextCollected ? -1 : 1)
  }
}

function goDetail(item) {
  router.push({ name: 'dynamic-detail', params: { id: item.dynamicId } })
}

function goPublish() {
  router.push({ name: 'publish' })
}

onMounted(() => loadList(true))
onActivated(() => loadList(true))
</script>

<template>
  <div class="page square-page">
    <div class="square-header">
      <h1 class="square-title">广场</h1>
    </div>

    <van-tabs
      v-model:active="activeChannel"
      sticky
      class="channel-tabs"
      @change="onChannelChange"
    >
      <van-tab
        v-for="ch in channels"
        :key="ch.name"
        :name="ch.name"
        :title="ch.label"
      />
    </van-tabs>

    <LoadingState v-if="loading" :count="3" />

    <EmptyState
      v-else-if="!list.length"
      emoji="🪐"
      text="这里还很安静，说点什么吧"
      sub-text="你的第一条瞬间，会成为别人的惊喜"
      action-text="发布瞬间"
      @action="goPublish"
    />

    <template v-else>
      <DynamicCard
        v-for="item in list"
        :key="item.dynamicId"
        :item="item"
        @like="onLike"
        @collect="onCollect"
        @click="goDetail"
        @comment="goDetail"
      />

      <div class="load-more" @click="onLoadMore">
        <span v-if="loadingMore">加载中...</span>
        <span v-else-if="finished">— 已经到底啦 —</span>
        <span v-else>点击加载更多</span>
      </div>
    </template>

    <!-- 发布按钮 -->
    <button class="publish-fab" @click="goPublish">＋</button>

    <!-- 底部导航 -->
    <div class="tab-bar">
      <div
        v-for="tab in tabs"
        :key="tab.name"
        class="tab-item"
        :class="{ active: $route.name === tab.name }"
        @click="$route.name !== tab.name && $router.push({ name: tab.name })"
      >
        <span class="tab-icon">{{ tab.icon }}</span>
        <span class="tab-label">{{ tab.label }}</span>
      </div>
    </div>
  </div>
</template>

<style scoped lang="scss">
.square-page {
  padding-bottom: 80px;
}
.square-header {
  padding: 24px 20px 8px;
}
.square-title {
  font-size: 26px;
  font-weight: 800;
  margin: 0;
  background: linear-gradient(90deg, #7c5cff, #ff6fa5);
  -webkit-background-clip: text;
  background-clip: text;
  -webkit-text-fill-color: transparent;
}
.channel-tabs {
  --van-tab-font-size: 15px;
}
.load-more {
  text-align: center;
  font-size: 13px;
  color: var(--soul-text-hint);
  padding: 20px 0 8px;
}
.publish-fab {
  position: fixed;
  right: calc(50% - 250px + 20px);
  bottom: 76px;
  width: 52px;
  height: 52px;
  border-radius: 50%;
  border: none;
  background: linear-gradient(135deg, #7c5cff, #ff6fa5);
  color: #fff;
  font-size: 28px;
  line-height: 1;
  box-shadow: 0 8px 20px rgba(124, 92, 255, 0.4);
  z-index: 98;
  &:active {
    transform: scale(0.94);
  }
}
@media (max-width: 500px) {
  .publish-fab {
    right: 20px;
  }
}
.tab-bar {
  position: fixed;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 100%;
  max-width: 500px;
  height: 56px;
  display: flex;
  background: rgba(255, 255, 255, 0.94);
  backdrop-filter: blur(12px);
  border-top: 1px solid var(--soul-line);
  z-index: 99;
  padding-bottom: env(safe-area-inset-bottom);
}
.tab-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: var(--soul-text-hint);
  &.active {
    color: var(--soul-primary);
  }
}
.tab-icon {
  font-size: 20px;
  line-height: 1;
}
.tab-label {
  font-size: 11px;
  margin-top: 3px;
}
</style>
