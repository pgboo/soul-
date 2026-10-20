<script setup>
/**
 * 加载态组件：骨架屏风格，营造柔和过渡。
 */
defineProps({
  /** 骨架卡片数量 */
  count: { type: Number, default: 3 }
})
</script>

<template>
  <div class="skeleton-list">
    <div v-for="n in count" :key="n" class="skeleton-card">
      <div class="sk-head">
        <div class="sk-avatar shimmer"></div>
        <div class="sk-head-lines">
          <div class="sk-line shimmer" style="width: 90px"></div>
          <div class="sk-line shimmer" style="width: 60px"></div>
        </div>
      </div>
      <div class="sk-line shimmer" style="width: 100%"></div>
      <div class="sk-line shimmer" style="width: 80%"></div>
      <div class="sk-block shimmer"></div>
    </div>
  </div>
</template>

<style scoped lang="scss">
.skeleton-list {
  padding: 12px 16px;
}
.skeleton-card {
  background: #fff;
  border-radius: var(--soul-radius);
  padding: 16px;
  margin-bottom: 12px;
  box-shadow: var(--soul-shadow);
}
.sk-head {
  display: flex;
  align-items: center;
  margin-bottom: 14px;
}
.sk-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  margin-right: 10px;
}
.sk-head-lines {
  flex: 1;
}
.sk-line {
  height: 12px;
  border-radius: 6px;
  margin-bottom: 8px;
}
.sk-block {
  height: 120px;
  border-radius: 12px;
  margin-top: 10px;
}
.shimmer {
  background: linear-gradient(90deg, #eee9f7 25%, #f7f4fd 37%, #eee9f7 63%);
  background-size: 400% 100%;
  animation: shimmer 1.4s ease infinite;
}
@keyframes shimmer {
  0% {
    background-position: 100% 50%;
  }
  100% {
    background-position: 0 50%;
  }
}
</style>
