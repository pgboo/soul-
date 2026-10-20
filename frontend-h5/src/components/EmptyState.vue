<script setup>
/**
 * 空状态组件 —— 有情绪价值的文案，而非"暂无数据"。
 */
defineProps({
  /** 主文案 */
  text: { type: String, default: '这里还很安静，说点什么吧' },
  /** 副文案 */
  subText: { type: String, default: '' },
  /** emoji 图标 */
  emoji: { type: String, default: '✨' },
  /** 按钮文案（有值则显示按钮） */
  actionText: { type: String, default: '' }
})

const emit = defineEmits(['action'])
</script>

<template>
  <div class="empty-state">
    <div class="empty-emoji">{{ emoji }}</div>
    <p class="empty-text">{{ text }}</p>
    <p v-if="subText" class="empty-sub">{{ subText }}</p>
    <button v-if="actionText" class="soul-btn empty-btn" @click="emit('action')">
      {{ actionText }}
    </button>
  </div>
</template>

<style scoped lang="scss">
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 56px 32px;
  text-align: center;
}
.empty-emoji {
  font-size: 44px;
  margin-bottom: 14px;
  opacity: 0.85;
}
.empty-text {
  font-size: 15px;
  color: var(--soul-text-sub);
  margin: 0 0 6px;
  line-height: 1.6;
}
.empty-sub {
  font-size: 13px;
  color: var(--soul-text-hint);
  margin: 0 0 18px;
}
.empty-btn {
  width: auto;
  padding: 0 32px;
  height: 40px;
  font-size: 14px;
}
</style>
