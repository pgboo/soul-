<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import { userApi } from '@/api'
import SoulAvatar from '@/components/SoulAvatar.vue'
import EmptyState from '@/components/EmptyState.vue'
import LoadingState from '@/components/LoadingState.vue'

const router = useRouter()

const list = ref([])
const loading = ref(true)

async function loadList() {
  loading.value = true
  try {
    const data = await userApi.blacklistList({ pageNum: 1, pageSize: 50 })
    list.value = data?.records || []
  } catch (e) {
    list.value = []
  } finally {
    loading.value = false
  }
}

async function remove(user) {
  try {
    await userApi.blacklistRemove(user.userId)
    showToast('已取消拉黑')
    const idx = list.value.findIndex((u) => u.userId === user.userId)
    if (idx >= 0) list.value.splice(idx, 1)
  } catch (e) {
    // 拦截器已提示
  }
}

onMounted(loadList)
</script>

<template>
  <div class="page blacklist-page">
    <van-nav-bar title="黑名单" left-arrow fixed placeholder @click-left="router.back()" />

    <LoadingState v-if="loading" :count="3" />

    <EmptyState
      v-else-if="!list.length"
      emoji="🕊️"
      text="黑名单是空的"
      sub-text="你的世界很和平"
    />

    <div v-else class="bl-list">
      <div v-for="user in list" :key="user.userId" class="bl-item">
        <SoulAvatar :src="user.avatar" :size="48" />
        <div class="bl-info">
          <div class="bl-name">{{ user.nickname || '神秘用户' }}</div>
          <div class="bl-time">{{ user.blacklistTime }}</div>
        </div>
        <button class="bl-remove" @click="remove(user)">取消拉黑</button>
      </div>
    </div>
  </div>
</template>

<style scoped lang="scss">
.bl-list {
  padding: 8px 0;
}
.bl-item {
  display: flex;
  align-items: center;
  padding: 14px 20px;
  background: #fff;
  margin: 0 16px 10px;
  border-radius: var(--soul-radius);
  box-shadow: var(--soul-shadow);
}
.bl-info {
  flex: 1;
  margin-left: 14px;
  overflow: hidden;
}
.bl-name {
  font-size: 15px;
  font-weight: 600;
}
.bl-time {
  font-size: 12px;
  color: var(--soul-text-hint);
  margin-top: 3px;
}
.bl-remove {
  border: 1px solid var(--soul-line);
  background: #fff;
  color: var(--soul-text-sub);
  font-size: 13px;
  padding: 7px 14px;
  border-radius: 16px;
}
</style>
