<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import { tagApi } from '@/api'
import { useUserStore } from '@/stores/user'
import EmptyState from '@/components/EmptyState.vue'
import LoadingState from '@/components/LoadingState.vue'

const router = useRouter()
const userStore = useUserStore()

const MIN_TAG = 3
const MAX_TAG = 10

const categories = ref([])
const activeCategory = ref(0)
const selectedIds = ref([])
const loading = ref(true)
const submitting = ref(false)

const currentTags = computed(() => {
  const cat = categories.value[activeCategory.value]
  return cat ? cat.tags || [] : []
})

const hotTags = computed(() => {
  const list = []
  categories.value.forEach((c) => {
    ;(c.tags || []).forEach((t) => {
      if (t.hot) list.push(t)
    })
  })
  return list.slice(0, 8)
})

// 已选标签对象列表（跨分类去重展示）
const selectedTags = computed(() => {
  const map = new Map()
  categories.value.forEach((c) => {
    ;(c.tags || []).forEach((t) => {
      map.set(t.tagId, t)
    })
  })
  return selectedIds.value.map((id) => map.get(id)).filter(Boolean)
})

const canSubmit = computed(
  () => selectedIds.value.length >= MIN_TAG && selectedIds.value.length <= MAX_TAG
)

function isSelected(tagId) {
  return selectedIds.value.includes(tagId)
}

/** 标签是否可用（设计预留 disabled 字段，禁用不可选） */
function isDisabled(tag) {
  return tag.status === 0 || tag.disabled === true
}

function toggleTag(tag) {
  if (isDisabled(tag)) {
    showToast('该标签暂不可选')
    return
  }
  const id = tag.tagId
  const idx = selectedIds.value.indexOf(id)
  if (idx >= 0) {
    selectedIds.value.splice(idx, 1)
  } else {
    if (selectedIds.value.length >= MAX_TAG) {
      showToast(`最多选择 ${MAX_TAG} 个标签`)
      return
    }
    selectedIds.value.push(id)
  }
}

function removeTag(tagId) {
  const idx = selectedIds.value.indexOf(tagId)
  if (idx >= 0) selectedIds.value.splice(idx, 1)
}

async function loadTags() {
  loading.value = true
  try {
    const data = await tagApi.list()
    // 契约：data 为分类数组；兼容后端若包一层 { categories: [...] } 的情况
    const list = Array.isArray(data) ? data : data?.categories
    categories.value = Array.isArray(list) ? list : []
  } catch (e) {
    categories.value = []
  } finally {
    loading.value = false
  }
}

async function handleSubmit() {
  if (!canSubmit.value) {
    showToast(`请选择 ${MIN_TAG}-${MAX_TAG} 个标签`)
    return
  }
  submitting.value = true
  try {
    await tagApi.save(selectedIds.value)
    userStore.markComplete('tag')
    showToast('标签已保存')
    router.replace({ name: 'soul-test' })
  } catch (e) {
    // 拦截器已提示
  } finally {
    submitting.value = false
  }
}

onMounted(loadTags)
</script>

<template>
  <div class="page tag-page">
    <div class="tag-header">
      <h1 class="tag-title gradient-text">你的兴趣画像</h1>
      <p class="tag-sub">选择 {{ MIN_TAG }}-{{ MAX_TAG }} 个标签，让我们为你找到同频的人</p>
    </div>

    <!-- 已选区 -->
    <div class="selected-bar">
      <div class="selected-info">
        <span class="selected-count" :class="{ ok: canSubmit }">
          {{ selectedIds.length }}/{{ MAX_TAG }}
        </span>
        <span class="selected-hint">
          {{ canSubmit ? '不错，继续挑选吧' : `还需 ${Math.max(0, MIN_TAG - selectedIds.length)} 个` }}
        </span>
      </div>
      <div v-if="selectedTags.length" class="selected-chips">
        <span
          v-for="tag in selectedTags"
          :key="tag.tagId"
          class="chip chip-selected"
          @click="removeTag(tag.tagId)"
        >
          {{ tag.name }} <i class="chip-x">×</i>
        </span>
      </div>
    </div>

    <LoadingState v-if="loading" :count="2" />

    <EmptyState
      v-else-if="!categories.length"
      emoji="🏷️"
      text="标签还在路上"
      sub-text="稍后再来看看吧"
    />

    <template v-else>
      <!-- 热门 -->
      <div v-if="hotTags.length" class="tag-section">
        <div class="section-title">🔥 热门标签</div>
        <div class="tag-grid">
          <span
            v-for="tag in hotTags"
            :key="'hot-' + tag.tagId"
            class="chip"
            :class="{ 'chip-selected': isSelected(tag.tagId), disabled: isDisabled(tag) }"
            @click="toggleTag(tag)"
          >
            {{ tag.name }}
          </span>
        </div>
      </div>

      <!-- 分类 Tab -->
      <van-tabs v-model:active="activeCategory" sticky offset-top="0" class="tag-tabs">
        <van-tab
          v-for="cat in categories"
          :key="cat.categoryId"
          :title="cat.name"
        >
          <div class="tag-grid tag-grid-pad">
            <span
              v-for="tag in cat.tags"
              :key="tag.tagId"
              class="chip"
              :class="{ 'chip-selected': isSelected(tag.tagId), disabled: isDisabled(tag) }"
              @click="toggleTag(tag)"
            >
              {{ tag.name }}
            </span>
          </div>
        </van-tab>
      </van-tabs>
    </template>

    <div class="tag-footer">
      <button
        class="soul-btn"
        :disabled="!canSubmit || submitting"
        @click="handleSubmit"
      >
        {{ submitting ? '保存中...' : '下一步' }}
      </button>
    </div>
  </div>
</template>

<style scoped lang="scss">
.tag-page {
  padding-bottom: 100px;
}
.tag-header {
  padding: 28px 24px 12px;
}
.tag-title {
  font-size: 26px;
  font-weight: 800;
  margin: 0 0 6px;
}
.tag-sub {
  font-size: 14px;
  color: var(--soul-text-sub);
  margin: 0;
}
.selected-bar {
  margin: 12px 16px;
  background: linear-gradient(120deg, rgba(124, 92, 255, 0.08), rgba(255, 111, 165, 0.08));
  border-radius: var(--soul-radius);
  padding: 14px 16px;
}
.selected-info {
  display: flex;
  align-items: baseline;
  gap: 8px;
  margin-bottom: 8px;
}
.selected-count {
  font-size: 18px;
  font-weight: 700;
  color: var(--soul-text-hint);
  &.ok {
    color: var(--soul-primary);
  }
}
.selected-hint {
  font-size: 13px;
  color: var(--soul-text-sub);
}
.selected-chips {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}
.chip {
  display: inline-block;
  padding: 8px 16px;
  border-radius: 18px;
  font-size: 13px;
  color: var(--soul-text-sub);
  background: #f4f1fb;
  border: 1px solid transparent;
  transition: all 0.18s ease;
  &.chip-selected {
    color: #fff;
    background: linear-gradient(90deg, #7c5cff, #a78bfa);
    box-shadow: 0 4px 12px rgba(124, 92, 255, 0.28);
  }
  &.disabled {
    opacity: 0.4;
  }
}
.chip-x {
  font-style: normal;
  margin-left: 2px;
  opacity: 0.8;
}
.tag-section {
  padding: 8px 16px 0;
}
.section-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--soul-text);
  margin-bottom: 12px;
}
.tag-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}
.tag-grid-pad {
  padding: 16px;
}
.tag-tabs {
  margin-top: 16px;
}
.tag-footer {
  position: fixed;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%);
  width: 100%;
  max-width: 500px;
  padding: 12px 16px calc(16px + env(safe-area-inset-bottom));
  background: rgba(255, 255, 255, 0.94);
  backdrop-filter: blur(12px);
  border-top: 1px solid var(--soul-line);
}
</style>
