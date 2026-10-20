<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { showToast, showConfirmDialog } from 'vant'
import { soulTestApi } from '@/api'
import { useUserStore } from '@/stores/user'
import EmptyState from '@/components/EmptyState.vue'
import LoadingState from '@/components/LoadingState.vue'

const router = useRouter()
const userStore = useUserStore()

const questions = ref([])
const currentIndex = ref(0)
// 答案映射：{ questionId: optionId }
const answers = ref({})
const loading = ref(true)
const submitting = ref(false)

const currentQuestion = computed(() => questions.value[currentIndex.value])
const total = computed(() => questions.value.length)
const percent = computed(() =>
  total.value ? Math.round(((currentIndex.value + 1) / total.value) * 100) : 0
)
const currentAnswer = computed(() =>
  currentQuestion.value ? answers.value[currentQuestion.value.questionId] : null
)
const isLast = computed(() => currentIndex.value === total.value - 1)
const allAnswered = computed(() =>
  questions.value.every((q) => answers.value[q.questionId])
)

function choose(option) {
  if (!currentQuestion.value) return
  answers.value[currentQuestion.value.questionId] = option.optionId
}

function prev() {
  if (currentIndex.value > 0) {
    currentIndex.value -= 1
  }
}

function next() {
  if (!currentAnswer.value) {
    showToast('选择一项再继续吧')
    return
  }
  if (!isLast.value) {
    currentIndex.value += 1
  } else {
    handleSubmit()
  }
}

async function handleSubmit() {
  if (!allAnswered.value) {
    showToast('还有题目没有作答哦')
    return
  }
  try {
    await showConfirmDialog({
      title: '提交测试',
      message: '提交后将生成你的灵魂类型，确定吗？'
    })
  } catch (e) {
    return
  }

  submitting.value = true
  try {
    const payload = questions.value.map((q) => ({
      questionId: q.questionId,
      optionId: answers.value[q.questionId]
    }))
    await soulTestApi.submit(payload)
    userStore.markComplete('test')
    showToast('测试完成')
    router.replace({ name: 'soul-result' })
  } catch (e) {
    // 拦截器已提示
  } finally {
    submitting.value = false
  }
}

async function loadQuestions() {
  loading.value = true
  try {
    const data = await soulTestApi.questions()
    questions.value = data?.questions || []
  } catch (e) {
    questions.value = []
  } finally {
    loading.value = false
  }
}

onMounted(loadQuestions)
</script>

<template>
  <div class="page test-page">
    <LoadingState v-if="loading" :count="1" />

    <EmptyState
      v-else-if="!questions.length"
      emoji="🧩"
      text="题目准备中"
      sub-text="灵魂测试马上就好"
      action-text="先去广场逛逛"
      @action="router.replace({ name: 'square' })"
    />

    <template v-else>
      <div class="test-header">
        <div class="progress-row">
          <span class="progress-text">
            {{ currentIndex + 1 }} / {{ total }}
          </span>
          <span class="progress-percent">{{ percent }}%</span>
        </div>
        <div class="progress-bar">
          <div class="progress-fill" :style="{ width: percent + '%' }"></div>
        </div>
      </div>

      <div class="question-card">
        <p class="question-title">{{ currentQuestion.title }}</p>
        <p v-if="currentQuestion.description" class="question-desc">
          {{ currentQuestion.description }}
        </p>

        <div class="options">
          <div
            v-for="opt in currentQuestion.options"
            :key="opt.optionId"
            class="option"
            :class="{ selected: currentAnswer === opt.optionId }"
            @click="choose(opt)"
          >
            <span class="option-dot"></span>
            <span class="option-content">{{ opt.content }}</span>
          </div>
        </div>
      </div>

      <div class="test-footer">
        <button
          class="soul-btn soul-btn--ghost test-nav-btn"
          :disabled="currentIndex === 0"
          @click="prev"
        >
          上一题
        </button>
        <button
          class="soul-btn test-nav-btn"
          :disabled="submitting"
          @click="next"
        >
          {{ isLast ? (submitting ? '提交中...' : '提交') : '下一题' }}
        </button>
      </div>
    </template>
  </div>
</template>

<style scoped lang="scss">
.test-page {
  padding-bottom: 100px;
}
.test-header {
  padding: 20px 20px 0;
}
.progress-row {
  display: flex;
  justify-content: space-between;
  font-size: 13px;
  color: var(--soul-text-sub);
  margin-bottom: 8px;
}
.progress-percent {
  color: var(--soul-primary);
  font-weight: 600;
}
.progress-bar {
  height: 8px;
  background: #efedf6;
  border-radius: 4px;
  overflow: hidden;
}
.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #7c5cff, #ff6fa5);
  border-radius: 4px;
  transition: width 0.3s ease;
}
.question-card {
  margin: 20px 16px;
  background: #fff;
  border-radius: 20px;
  box-shadow: var(--soul-shadow);
  padding: 24px 20px;
}
.question-title {
  font-size: 19px;
  font-weight: 700;
  line-height: 1.5;
  margin: 0 0 8px;
  color: var(--soul-text);
}
.question-desc {
  font-size: 13px;
  color: var(--soul-text-hint);
  margin: 0 0 20px;
}
.options {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.option {
  display: flex;
  align-items: center;
  padding: 16px;
  border-radius: 14px;
  background: #f8f6fd;
  border: 1.5px solid transparent;
  transition: all 0.18s ease;
  &.selected {
    background: rgba(124, 92, 255, 0.08);
    border-color: var(--soul-primary);
    .option-dot {
      border-color: var(--soul-primary);
      background: var(--soul-primary);
      box-shadow: inset 0 0 0 3px #fff;
    }
    .option-content {
      color: var(--soul-primary);
      font-weight: 600;
    }
  }
}
.option-dot {
  width: 20px;
  height: 20px;
  border-radius: 50%;
  border: 2px solid #ddd8ec;
  margin-right: 12px;
  flex-shrink: 0;
  transition: all 0.18s ease;
}
.option-content {
  font-size: 15px;
  color: var(--soul-text);
  line-height: 1.4;
}
.test-footer {
  position: fixed;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 100%;
  max-width: 500px;
  display: flex;
  gap: 12px;
  padding: 12px 16px calc(16px + env(safe-area-inset-bottom));
  background: rgba(255, 255, 255, 0.94);
  backdrop-filter: blur(12px);
  border-top: 1px solid var(--soul-line);
}
.test-nav-btn {
  flex: 1;
  height: 46px;
}
</style>
