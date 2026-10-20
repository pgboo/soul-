<script setup>
import SoulAvatar from './SoulAvatar.vue'
import { resolveAssetUrl } from '@/utils/asset'

/**
 * 动态卡片：广场列表 / 收藏列表复用。
 * 支持点赞/收藏状态回显与交互。
 */
const props = defineProps({
  /** 动态数据对象 */
  item: { type: Object, required: true }
})

const emit = defineEmits(['like', 'collect', 'click', 'comment'])

function goDetail() {
  emit('click', props.item)
}

function onLike(e) {
  e.stopPropagation()
  emit('like', props.item)
}

function onCollect(e) {
  e.stopPropagation()
  emit('collect', props.item)
}

function onComment(e) {
  e.stopPropagation()
  emit('comment', props.item)
}
</script>

<template>
  <div class="dyn-card" @click="goDetail">
    <div class="dyn-head">
      <SoulAvatar :src="item.author?.avatar" :size="40" />
      <div class="dyn-head-info">
        <div class="dyn-nickname ellipsis">{{ item.author?.nickname || '神秘用户' }}</div>
        <div class="dyn-time">{{ item.createTime }}</div>
      </div>
      <span v-if="item.auditStatus === 1" class="dyn-audit">审核中</span>
    </div>

    <p v-if="item.content" class="dyn-content">{{ item.content }}</p>

    <div v-if="item.mediaUrls && item.mediaUrls.length" class="dyn-media">
      <img
        v-for="(url, idx) in item.mediaUrls.slice(0, 9)"
        :key="idx"
        :src="resolveAssetUrl(url)"
        class="dyn-media-img"
        alt="media"
      />
    </div>

    <div v-if="item.topicName" class="dyn-topic"># {{ item.topicName }}</div>

    <div class="dyn-actions">
      <span class="dyn-action" :class="{ active: item.liked }" @click="onLike">
        <i class="act-icon">{{ item.liked ? '💜' : '🤍' }}</i>{{ item.likeCount || 0 }}
      </span>
      <span class="dyn-action" @click="onComment">
        <i class="act-icon">💬</i>{{ item.commentCount || 0 }}
      </span>
      <span class="dyn-action" :class="{ active: item.collected }" @click="onCollect">
        <i class="act-icon">{{ item.collected ? '⭐' : '☆' }}</i>{{ item.collectCount || 0 }}
      </span>
    </div>
  </div>
</template>

<style scoped lang="scss">
.dyn-card {
  background: #fff;
  border-radius: var(--soul-radius);
  box-shadow: var(--soul-shadow);
  padding: 16px;
  margin: 12px 16px;
  transition: transform 0.15s ease;
  &:active {
    transform: scale(0.995);
  }
}
.dyn-head {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
}
.dyn-head-info {
  flex: 1;
  margin-left: 10px;
  overflow: hidden;
}
.dyn-nickname {
  font-size: 15px;
  font-weight: 600;
  color: var(--soul-text);
}
.dyn-time {
  font-size: 12px;
  color: var(--soul-text-hint);
  margin-top: 2px;
}
.dyn-audit {
  font-size: 11px;
  color: #e08a2b;
  background: #fff6e8;
  border-radius: 10px;
  padding: 2px 8px;
}
.dyn-content {
  font-size: 15px;
  line-height: 1.6;
  color: var(--soul-text);
  margin: 0 0 12px;
  white-space: pre-wrap;
  word-break: break-word;
}
.dyn-media {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 6px;
  margin-bottom: 12px;
}
.dyn-media-img {
  width: 100%;
  aspect-ratio: 1;
  object-fit: cover;
  border-radius: 10px;
  background: #f0ecfa;
}
.dyn-topic {
  display: inline-block;
  font-size: 13px;
  color: var(--soul-primary);
  background: rgba(124, 92, 255, 0.08);
  border-radius: 12px;
  padding: 4px 12px;
  margin-bottom: 12px;
}
.dyn-actions {
  display: flex;
  gap: 28px;
  border-top: 1px solid var(--soul-line);
  padding-top: 12px;
}
.dyn-action {
  display: flex;
  align-items: center;
  font-size: 13px;
  color: var(--soul-text-sub);
  transition: color 0.15s ease;
  &.active {
    color: var(--soul-primary);
    font-weight: 600;
  }
  .act-icon {
    font-style: normal;
    margin-right: 4px;
    font-size: 15px;
  }
}
</style>
