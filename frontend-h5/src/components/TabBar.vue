<script setup>
import { useRouter, useRoute } from 'vue-router'
import SoulAvatar from '@/components/SoulAvatar.vue'

/**
 * 底部导航栏：广场 / 匹配 / 消息 / 我的。
 */
const router = useRouter()
const route = useRoute()

const tabs = [
  { name: 'square', label: '广场', icon: '🪐' },
  { name: 'match', label: '匹配', icon: '💫' },
  { name: 'messages', label: '消息', icon: '💌' },
  { name: 'mine', label: '我的', icon: '🙂' }
]

function go(name) {
  if (route.name !== name) {
    router.push({ name })
  }
}
</script>

<template>
  <div class="tab-bar">
    <div
      v-for="tab in tabs"
      :key="tab.name"
      class="tab-item"
      :class="{ active: route.name === tab.name }"
      @click="go(tab.name)"
    >
      <span class="tab-icon">{{ tab.icon }}</span>
      <span class="tab-label">{{ tab.label }}</span>
    </div>
  </div>
</template>

<style scoped lang="scss">
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
  transition: color 0.2s ease;
  &.active {
    color: var(--soul-primary);
    .tab-icon {
      transform: translateY(-1px) scale(1.1);
    }
  }
}
.tab-icon {
  font-size: 20px;
  transition: transform 0.2s ease;
  line-height: 1;
}
.tab-label {
  font-size: 11px;
  margin-top: 3px;
}
</style>
