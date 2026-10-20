<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showToast, showConfirmDialog } from 'vant'
import { userApi, greetApi } from '@/api'
import SoulAvatar from '@/components/SoulAvatar.vue'
import EmptyState from '@/components/EmptyState.vue'
import LoadingState from '@/components/LoadingState.vue'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const userId = route.params.id
const info = ref(null)
const loading = ref(true)
const loadFailed = ref(false)
const greeting = ref(false)

const isSelf = computed(() => String(userStore.userId) === String(userId))

const genderText = computed(() => {
  if (!info.value) return ''
  return info.value.gender === 1 ? '♂' : info.value.gender === 2 ? '♀' : ''
})

async function loadInfo() {
  loading.value = true
  loadFailed.value = false
  try {
    info.value = await userApi.home(userId)
  } catch (e) {
    loadFailed.value = true
  } finally {
    loading.value = false
  }
}

async function onGreet() {
  if (!info.value || greeting.value) return
  if (info.value.blacklisted) {
    showToast('你已拉黑对方，无法打招呼')
    return
  }
  greeting.value = true
  try {
    const data = await greetApi.send(info.value.userId, '')
    showToast('打招呼成功')
    info.value.greetedToday = true
    if (data?.sessionUuid) {
      router.push({ name: 'chat', params: { sessionUuid: data.sessionUuid } })
    }
  } catch (e) {
    // 拦截器已提示
  } finally {
    greeting.value = false
  }
}

function goChat() {
  // 会话标识按约定 user_self + user_target 拼接（后端约定 sessionUuid）
  const sessionUuid = `${userStore.userId}_${info.value.userId}`
  router.push({ name: 'chat', params: { sessionUuid } })
}

async function onBlacklist() {
  if (!info.value) return
  if (info.value.blacklisted) {
    try {
      await userApi.blacklistRemove(info.value.userId)
      info.value.blacklisted = false
      showToast('已取消拉黑')
    } catch (e) {
      // 拦截器已提示
    }
    return
  }
  try {
    await showConfirmDialog({
      title: '拉黑用户',
      message: '拉黑后将无法互相私信、打招呼，确定吗？'
    })
  } catch (e) {
    return
  }
  try {
    await userApi.blacklistAdd(info.value.userId)
    info.value.blacklisted = true
    showToast('已拉黑')
  } catch (e) {
    // 拦截器已提示
  }
}

onMounted(loadInfo)
</script>

<template>
  <div class="page userhome-page">
    <van-nav-bar left-arrow fixed placeholder @click-left="router.back()" />

    <LoadingState v-if="loading" :count="1" />

    <EmptyState
      v-else-if="loadFailed || !info"
      emoji="🚪"
      text="无法查看这个主页"
      sub-text="可能对方设置了权限"
      action-text="返回"
      @action="router.back()"
    />

    <template v-else>
      <div class="profile-hero nebula-bg">
        <SoulAvatar :src="info.avatar" :size="88" />
        <h1 class="hero-nickname">{{ info.nickname }}</h1>
        <div class="hero-meta">
          <span v-if="genderText" class="meta-item">{{ genderText }}</span>
          <span v-if="info.age" class="meta-item">{{ info.age }}岁</span>
          <span v-if="info.city" class="meta-item">📍 {{ info.city }}</span>
        </div>
        <div v-if="info.soulId" class="hero-soulid">Soul ID: {{ info.soulId }}</div>
      </div>

      <div class="soul-card">
        <div class="info-stats">
          <div class="stat-item">
            <div class="stat-num">{{ info.dynamicCount || 0 }}</div>
            <div class="stat-label">瞬间</div>
          </div>
          <div class="stat-item">
            <div class="stat-num">{{ (info.tags || []).length }}</div>
            <div class="stat-label">标签</div>
          </div>
        </div>
        <p v-if="info.sign" class="info-sign">{{ info.sign }}</p>
        <p v-if="info.intro" class="info-intro">{{ info.intro }}</p>
      </div>

      <div v-if="info.tags && info.tags.length" class="soul-card">
        <div class="section-title">兴趣标签</div>
        <div class="tag-list">
          <span v-for="t in info.tags" :key="t.tagId" class="tag">{{ t.name }}</span>
        </div>
      </div>

      <div v-if="info.soulResult" class="soul-card soul-result-card">
        <div class="sr-left">
          <div class="sr-code-label">灵魂类型</div>
          <div class="sr-name">{{ info.soulResult.name }}</div>
        </div>
        <div class="sr-right">✨</div>
      </div>

      <div class="action-bar">
        <template v-if="!isSelf">
          <button
            class="action-btn action-secondary"
            :class="{ active: info.blacklisted }"
            @click="onBlacklist"
          >
            {{ info.blacklisted ? '已拉黑' : '拉黑' }}
          </button>
          <button class="action-btn action-secondary" @click="goChat">私信</button>
          <button
            class="action-btn action-primary"
            :disabled="info.greetedToday || greeting"
            @click="onGreet"
          >
            {{ info.greetedToday ? '今日已打招呼' : greeting ? '发送中...' : '打招呼' }}
          </button>
        </template>
        <button v-else class="action-btn action-primary" @click="router.push({ name: 'edit-profile' })">
          编辑我的资料
        </button>
      </div>
    </template>
  </div>
</template>

<style scoped lang="scss">
.userhome-page {
  padding-bottom: 90px;
}
.profile-hero {
  padding: 36px 24px 28px;
  text-align: center;
  color: #fff;
  border-radius: 0 0 28px 28px;
}
.hero-nickname {
  font-size: 24px;
  font-weight: 800;
  margin: 14px 0 8px;
}
.hero-meta {
  display: flex;
  justify-content: center;
  gap: 12px;
  font-size: 13px;
  opacity: 0.92;
}
.hero-soulid {
  font-size: 12px;
  opacity: 0.75;
  margin-top: 8px;
}
.info-stats {
  display: flex;
  gap: 40px;
  padding-bottom: 14px;
  border-bottom: 1px solid var(--soul-line);
  margin-bottom: 14px;
}
.stat-item {
  text-align: center;
}
.stat-num {
  font-size: 20px;
  font-weight: 700;
  color: var(--soul-primary);
}
.stat-label {
  font-size: 12px;
  color: var(--soul-text-hint);
  margin-top: 2px;
}
.info-sign {
  font-size: 15px;
  margin: 0 0 6px;
}
.info-intro {
  font-size: 14px;
  color: var(--soul-text-sub);
  margin: 0;
  line-height: 1.6;
}
.section-title {
  font-size: 14px;
  font-weight: 700;
  margin-bottom: 12px;
}
.tag-list {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}
.tag {
  font-size: 13px;
  color: var(--soul-primary);
  background: rgba(124, 92, 255, 0.08);
  border-radius: 14px;
  padding: 6px 14px;
}
.soul-result-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: linear-gradient(120deg, rgba(124, 92, 255, 0.1), rgba(255, 111, 165, 0.1));
}
.sr-code-label {
  font-size: 12px;
  color: var(--soul-text-sub);
}
.sr-name {
  font-size: 18px;
  font-weight: 700;
  color: var(--soul-primary);
  margin-top: 4px;
}
.sr-right {
  font-size: 30px;
}
.action-bar {
  position: fixed;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 100%;
  max-width: 500px;
  display: flex;
  gap: 12px;
  padding: 12px 16px calc(12px + env(safe-area-inset-bottom));
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(12px);
  border-top: 1px solid var(--soul-line);
  z-index: 100;
}
.action-btn {
  flex: 1;
  height: 44px;
  border: none;
  border-radius: 22px;
  font-size: 14px;
  font-weight: 600;
}
.action-secondary {
  background: #f4f1fb;
  color: var(--soul-text-sub);
  &.active {
    color: #ff6fa5;
  }
}
.action-primary {
  background: linear-gradient(90deg, #7c5cff, #ff6fa5);
  color: #fff;
  box-shadow: 0 6px 14px rgba(124, 92, 255, 0.3);
  &:disabled {
    opacity: 0.5;
    box-shadow: none;
  }
}
</style>
