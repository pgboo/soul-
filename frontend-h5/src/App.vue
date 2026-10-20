<script setup>
import { onMounted } from 'vue'
import { useUserStore } from '@/stores/user'

// 应用根组件：启动时从 localStorage 恢复登录态
const userStore = useUserStore()

onMounted(() => {
  userStore.restore()
})
</script>

<template>
  <router-view v-slot="{ Component }">
    <transition name="fade" mode="out-in">
      <component :is="Component" />
    </transition>
  </router-view>
</template>

<style lang="scss">
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.22s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
