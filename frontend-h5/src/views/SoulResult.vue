<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { soulTestApi } from '@/api'
import EmptyState from '@/components/EmptyState.vue'
import LoadingState from '@/components/LoadingState.vue'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const result = ref(null)
const loading = ref(true)
const loadFailed = ref(false)

async function loadResult() {
  loading.value = true
  loadFailed.value = false
  try {
    const data = await soulTestApi.result()
    result.value = data
  } catch (e) {
    loadFailed.value = true
  } finally {
    loading.value = false
  }
}

function goSquare() {
  router.replace({ name: 'square' })
}

function retest() {
  router.replace({ name: 'soul-test' })
}

onMounted(loadResult)
</script>

<template>
  <div class="page result-page">
    <LoadingState v-if="loading" :count="1" />

    <EmptyState
      v-else-if="loadFailed || !result"
      emoji="🌀"
      text="还没找到你的灵魂类型"
      sub-text="完成测试即可解锁"
      action-text="去测试"
      @action="retest"
    />

    <template v-else>
      <div class="result-hero nebula-bg">
        <div class="hero-glow"></div>
        <div class="hero-icon">{{ result.icon || '✨' }}</div>
        <div class="hero-label">你的灵魂类型</div>
        <h1 class="hero-name">{{ result.name }}</h1>
        <div class="hero-code">{{ result.resultCode }}</div>
      </div>

      <div class="soul-card">
        <div class="section-title">💜 关于你</div>
        <p class="section-body">{{ result.description || '你是独一无二的存在。' }}</p>
      </div>

      <div class="soul-card">
        <div class="section-title">🤝 社交建议</div>
        <p class="section-body">{{ result.socialAdvice || '多去认识和你同频的人吧。' }}</p>
      </div>

      <div class="result-footer">
        <button class="soul-btn" @click="goSquare">进入广场</button>
        <button class="back-link" @click="retest">重新测试</button>
      </div>
    </template>
  </div>
</template>

<style scoped lang="scss">
.result-page {
  padding-bottom: 32px;
}
.result-hero {
  position: relative;
  padding: 48px 24px 40px;
  border-radius: 0 0 32px 32px;
  text-align: center;
  color: #fff;
  overflow: hidden;
}
.hero-glow {
  position: absolute;
  width: 200px;
  height: 200px;
  background: rgba(255, 255, 255, 0.25);
  border-radius: 50%;
  filter: blur(50px);
  top: -40px;
  right: -40px;
}
.hero-icon {
  font-size: 56px;
  margin-bottom: 12px;
  position: relative;
}
.hero-label {
  font-size: 13px;
  opacity: 0.9;
  letter-spacing: 2px;
  position: relative;
}
.hero-name {
  font-size: 30px;
  font-weight: 800;
  margin: 8px 0 4px;
  position: relative;
}
.hero-code {
  font-size: 12px;
  opacity: 0.75;
  letter-spacing: 4px;
  position: relative;
}
.section-title {
  font-size: 15px;
  font-weight: 700;
  margin-bottom: 10px;
}
.section-body {
  font-size: 15px;
  line-height: 1.7;
  color: var(--soul-text-sub);
  margin: 0;
}
.result-footer {
  padding: 12px 16px 0;
}
.back-link {
  display: block;
  margin: 16px auto 0;
  border: none;
  background: transparent;
  color: var(--soul-text-sub);
  font-size: 14px;
}
</style>
