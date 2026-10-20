<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { dynamicApi } from '@/api'
import DynamicCard from '@/components/DynamicCard.vue'
import EmptyState from '@/components/EmptyState.vue'
import LoadingState from '@/components/LoadingState.vue'

const router = useRouter()

const list = ref([])
const loading = ref(true)
const pageNum = ref(1)
const total = ref(0)
const finished = ref(false)
const loadingMore = ref(false)

async function loadList(reset = false) {
  if (reset) {
    pageNum.value = 1
    finished.value = false
    loading.value = true
  }
  try {
    const data = await dynamicApi.list({
      channel: 'collect',
      pageNum: pageNum.value,
      pageSize: 10
    })
    const records = data?.records || []
    total.value = data?.total || 0
    list.value = reset ? records : list.value.concat(records)
    if (list.value.length >= total.value || records.length === 0) {
      finished.value = true
    }
  } catch (e) {
    if (reset) list.value = []
    finished.value = true
  } finally {
    loading.value = false
    loadingMore.value = false
  }
}

function loadMore() {
  if (finished.value || loadingMore.value) return
  loadingMore.value = true
  pageNum.value += 1
  loadList(false)
}

async function onLike(item) {
  const nextLiked = !item.liked
  item.liked = nextLiked
  item.likeCount = Math.max(0, (item.likeCount || 0) + (nextLiked ? 1 : -1))
  try {
    const data = await dynamicApi.like(item.dynamicId, nextLiked ? 'like' : 'cancel')
    item.liked = data.liked
    item.likeCount = data.likeCount
  } catch (e) {
    item.liked = !nextLiked
    item.likeCount = (item.likeCount || 0) + (nextLiked ? -1 : 1)
  }
}

async function onCollect(item) {
  const nextCollected = !item.collected
  const idx = list.value.findIndex((d) => d.dynamicId === item.dynamicId)
  try {
    await dynamicApi.collect(item.dynamicId, nextCollected ? 'collect' : 'cancel')
    // 取消收藏后从列表移除
    if (!nextCollected && idx >= 0) {
      list.value.splice(idx, 1)
      total.value = Math.max(0, total.value - 1)
    }
  } catch (e) {
    // 拦截器已提示
  }
}

function goDetail(item) {
  router.push({ name: 'dynamic-detail', params: { id: item.dynamicId } })
}

onMounted(() => loadList(true))
</script>

<template>
  <div class="page collect-page">
    <van-nav-bar title="我的收藏" left-arrow fixed placeholder @click-left="router.back()" />

    <LoadingState v-if="loading" :count="3" />

    <EmptyState
      v-else-if="!list.length"
      emoji="⭐"
      text="收藏夹还空着"
      sub-text="喜欢的内容，值得被留住"
      action-text="去广场看看"
      @action="router.push({ name: 'square' })"
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
      <div v-if="list.length < total" class="load-more" @click="loadMore">
        {{ loadingMore ? '加载中...' : '加载更多' }}
      </div>
    </template>
  </div>
</template>

<style scoped lang="scss">
.load-more {
  text-align: center;
  font-size: 13px;
  color: var(--soul-primary);
  padding: 16px 0;
}
</style>
