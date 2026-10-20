<script setup>
import { computed } from 'vue'
import { resolveAssetUrl } from '@/utils/asset'

/**
 * 用户头像组件：带渐变占位兜底。
 */
const props = defineProps({
  src: { type: String, default: '' },
  size: { type: Number, default: 40 },
  /** 匿名时展示 */
  anonymous: { type: Boolean, default: false }
})

/** 后端返回 /upload/xxx 相对路径；打包成 App 时需补成绝对地址 */
const resolvedSrc = computed(() => resolveAssetUrl(props.src))

const fallback = 'data:image/svg+xml;utf8,' +
  encodeURIComponent(
    `<svg xmlns="http://www.w3.org/2000/svg" width="80" height="80">
      <defs><linearGradient id="g" x1="0" y1="0" x2="1" y2="1">
        <stop offset="0" stop-color="#a78bfa"/><stop offset="1" stop-color="#ff9ec4"/>
      </linearGradient></defs>
      <rect width="80" height="80" rx="40" fill="url(#g)"/>
      <circle cx="40" cy="32" r="14" fill="#fff" opacity="0.85"/>
      <ellipse cx="40" cy="66" rx="22" ry="16" fill="#fff" opacity="0.85"/>
    </svg>`
  )

function onError(e) {
  e.target.src = fallback
}
</script>

<template>
  <img
    class="soul-avatar"
    :src="resolvedSrc || fallback"
    :style="{ width: size + 'px', height: size + 'px' }"
    @error="onError"
    alt="avatar"
  />
</template>

<style scoped lang="scss">
.soul-avatar {
  border-radius: 50%;
  object-fit: cover;
  background: #f0ecfa;
  flex-shrink: 0;
  display: block;
}
</style>
