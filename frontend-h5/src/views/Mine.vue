<script setup>
import { ref, computed, onMounted, onActivated } from 'vue'
import { useRouter } from 'vue-router'
import { showConfirmDialog } from 'vant'
import { userApi } from '@/api'
import { useUserStore } from '@/stores/user'
import SoulAvatar from '@/components/SoulAvatar.vue'
import TabBar from '@/components/TabBar.vue'

const router = useRouter()
const userStore = useUserStore()

const profile = ref(null)
const loading = ref(true)

const completePercent = computed(() => profile.value?.completePercent ?? 0)

const menus = [
  { icon: '✏️', label: '编辑资料', route: 'edit-profile' },
  { icon: '⭐', label: '我的收藏', route: 'collect' },
  { icon: '🔒', label: '隐私设置', route: 'privacy' },
  { icon: '🚫', label: '黑名单', route: 'blacklist' }
]

async function loadProfile() {
  loading.value = true
  try {
    profile.value = await userApi.profile()
  } catch (e) {
    profile.value = null
  } finally {
    loading.value = false
  }
}

function goMenu(route) {
  router.push({ name: route })
}

async function handleLogout() {
  try {
    await showConfirmDialog({
      title: '退出登录',
      message: '确定要退出当前账号吗？'
    })
  } catch (e) {
    return
  }
  await userStore.logout()
  router.replace({ name: 'login' })
}

onMounted(loadProfile)
onActivated(loadProfile)
</script>

<template>
  <div class="page mine-page">
    <div class="mine-hero nebula-bg">
      <div class="mine-user" @click="router.push({ name: 'user-home', params: { id: userStore.userId } })">
        <SoulAvatar :src="profile?.avatar || userStore.avatar" :size="72" />
        <div class="mine-info">
          <h2 class="mine-nickname">{{ profile?.nickname || userStore.nickname || '未设置昵称' }}</h2>
          <p class="mine-sign">{{ profile?.sign || '还没有个性签名' }}</p>
        </div>
      </div>

      <div class="complete-bar">
        <div class="complete-info">
          <span>资料完整度</span>
          <span class="complete-percent">{{ completePercent }}%</span>
        </div>
        <div class="complete-track">
          <div class="complete-fill" :style="{ width: completePercent + '%' }"></div>
        </div>
      </div>
    </div>

    <div class="soul-card menu-card">
      <div
        v-for="menu in menus"
        :key="menu.route"
        class="menu-item"
        @click="goMenu(menu.route)"
      >
        <span class="menu-icon">{{ menu.icon }}</span>
        <span class="menu-label">{{ menu.label }}</span>
        <span class="menu-arrow">›</span>
      </div>
    </div>

    <div class="logout-wrap">
      <button class="logout-btn" @click="handleLogout">退出登录</button>
    </div>

    <TabBar />
  </div>
</template>

<style scoped lang="scss">
.mine-page {
  padding-bottom: 80px;
}
.mine-hero {
  padding: 40px 24px 28px;
  border-radius: 0 0 28px 28px;
  color: #fff;
}
.mine-user {
  display: flex;
  align-items: center;
}
.mine-info {
  margin-left: 16px;
  flex: 1;
  overflow: hidden;
}
.mine-nickname {
  font-size: 22px;
  font-weight: 800;
  margin: 0 0 6px;
}
.mine-sign {
  font-size: 13px;
  opacity: 0.9;
  margin: 0;
}
.complete-bar {
  margin-top: 22px;
}
.complete-info {
  display: flex;
  justify-content: space-between;
  font-size: 13px;
  opacity: 0.92;
  margin-bottom: 8px;
}
.complete-percent {
  font-weight: 700;
}
.complete-track {
  height: 8px;
  background: rgba(255, 255, 255, 0.3);
  border-radius: 4px;
  overflow: hidden;
}
.complete-fill {
  height: 100%;
  background: #fff;
  border-radius: 4px;
  transition: width 0.35s ease;
}
.menu-card {
  padding: 4px 16px;
}
.menu-item {
  display: flex;
  align-items: center;
  padding: 16px 0;
  border-bottom: 1px solid var(--soul-line);
  &:last-child {
    border-bottom: none;
  }
}
.menu-icon {
  font-size: 18px;
  margin-right: 14px;
}
.menu-label {
  flex: 1;
  font-size: 15px;
  color: var(--soul-text);
}
.menu-arrow {
  color: var(--soul-text-hint);
  font-size: 20px;
}
.logout-wrap {
  padding: 20px 16px 0;
}
.logout-btn {
  width: 100%;
  height: 46px;
  border: none;
  border-radius: 23px;
  background: #fff;
  color: #ff6fa5;
  font-size: 15px;
  font-weight: 600;
  box-shadow: var(--soul-shadow);
}
</style>
