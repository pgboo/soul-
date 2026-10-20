<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { showToast, showLoadingToast, closeToast } from 'vant'
import { dynamicApi, topicApi, uploadApi } from '@/api'
import { resolveAssetUrl } from '@/utils/asset'

const router = useRouter()

const content = ref('')
const mediaUrls = ref([])
const topicId = ref(null)
const anonymousFlag = ref(false)
const visibleScope = ref(1)
const submitting = ref(false)

const topics = ref([])
const showTopicPicker = ref(false)
const showScopePicker = ref(false)
const newTopicName = ref('')
const creatingTopic = ref(false)

const MAX_TOPIC_LEN = 20

const fileInput = ref(null)
const MAX_IMAGES = 9

const scopeOptions = [
  { name: '公开', value: 1 },
  { name: '仅好友', value: 2 },
  { name: '仅自己', value: 3 }
]
const scopeText = computed(
  () => scopeOptions.find((s) => s.value === visibleScope.value)?.name || '公开'
)
const topicName = computed(
  () => topics.value.find((t) => t.topicId === topicId.value)?.name || ''
)

const canSubmit = computed(
  () => content.value.trim().length > 0 || mediaUrls.value.length > 0
)

function pickImage() {
  if (mediaUrls.value.length >= MAX_IMAGES) {
    showToast(`最多上传 ${MAX_IMAGES} 张图片`)
    return
  }
  fileInput.value?.click()
}

async function onFileChange(e) {
  const files = Array.from(e.target.files || [])
  e.target.value = ''
  if (!files.length) return
  showLoadingToast({ message: '上传中...', forbidClick: true, duration: 0 })
  try {
    for (const file of files) {
      if (mediaUrls.value.length >= MAX_IMAGES) break
      if (file.size > 5 * 1024 * 1024) {
        showToast('图片不能超过 5MB')
        continue
      }
      const data = await uploadApi.image(file, 'dynamic')
      mediaUrls.value.push(data.url)
    }
    showToast('上传成功')
  } catch (err) {
    // 拦截器已提示
  } finally {
    closeToast()
  }
}

function removeImage(idx) {
  mediaUrls.value.splice(idx, 1)
}

function onTopicSelect(action) {
  topicId.value = action.topicId
  showTopicPicker.value = false
}

/** 自拟话题：创建后立即选中（同名话题后端会复用） */
async function handleCreateTopic() {
  const name = newTopicName.value.trim().replace(/^#+\s*/, '')
  if (!name) {
    showToast('请输入话题名')
    return
  }
  if (name.length > MAX_TOPIC_LEN) {
    showToast(`话题名不能超过 ${MAX_TOPIC_LEN} 个字`)
    return
  }
  if (creatingTopic.value) return
  creatingTopic.value = true
  try {
    const data = await topicApi.create(name)
    const item = {
      topicId: data.topicId,
      name: data.name,
      hotValue: data.hotValue || 0
    }
    const idx = topics.value.findIndex((t) => t.topicId === item.topicId)
    if (idx >= 0) topics.value.splice(idx, 1, item)
    else topics.value.unshift(item)
    topicId.value = item.topicId
    newTopicName.value = ''
    showTopicPicker.value = false
    showToast(`已选择话题 #${item.name}`)
  } catch (e) {
    // 拦截器已提示
  } finally {
    creatingTopic.value = false
  }
}

function clearTopic() {
  topicId.value = null
}

function onScopeSelect(action) {
  visibleScope.value = action.value
  showScopePicker.value = false
}

async function loadTopics() {
  try {
    const data = await topicApi.list()
    topics.value = Array.isArray(data) ? data : []
  } catch (e) {
    topics.value = []
  }
}

async function handlePublish() {
  if (!canSubmit.value) {
    showToast('写点什么或配张图吧')
    return
  }
  if (submitting.value) return
  submitting.value = true
  try {
    await dynamicApi.publish({
      content: content.value.trim(),
      mediaUrls: mediaUrls.value,
      topicId: topicId.value,
      anonymousFlag: anonymousFlag.value,
      visibleScope: visibleScope.value
    })
    showToast('发布成功')
    router.replace({ name: 'square' })
  } catch (e) {
    // 拦截器已提示
  } finally {
    submitting.value = false
  }
}

onMounted(loadTopics)
</script>

<template>
  <div class="page publish-page">
    <van-nav-bar
      title="发布瞬间"
      left-arrow
      fixed
      placeholder
      @click-left="router.back()"
    >
      <template #right>
        <span
          class="nav-publish"
          :class="{ disabled: !canSubmit || submitting }"
          @click="handlePublish"
        >
          {{ submitting ? '发布中' : '发布' }}
        </span>
      </template>
    </van-nav-bar>

    <div class="soul-card">
      <textarea
        v-model="content"
        class="publish-textarea"
        maxlength="500"
        rows="6"
        placeholder="此刻的心情，想和谁说？"
      ></textarea>
      <div class="char-count">{{ content.length }}/500</div>
    </div>

    <!-- 图片 -->
    <div class="soul-card">
      <div class="section-label">图片（{{ mediaUrls.length }}/{{ MAX_IMAGES }}）</div>
      <div class="image-grid">
        <div v-for="(url, idx) in mediaUrls" :key="idx" class="image-item">
          <img :src="resolveAssetUrl(url)" alt="img" />
          <span class="image-del" @click="removeImage(idx)">×</span>
        </div>
        <div
          v-if="mediaUrls.length < MAX_IMAGES"
          class="image-add"
          @click="pickImage"
        >
          ＋
        </div>
      </div>
      <input
        ref="fileInput"
        type="file"
        accept="image/*"
        multiple
        class="hidden-input"
        @change="onFileChange"
      />
    </div>

    <!-- 选项 -->
    <div class="soul-card">
      <div class="setting-row" @click="showTopicPicker = true">
        <span class="setting-label"># 话题</span>
        <span class="setting-value">
          {{ topicName || '选择一个话题' }}
          <i v-if="topicId" class="setting-clear" @click.stop="clearTopic">×</i>
        </span>
      </div>
      <div class="setting-row" @click="showScopePicker = true">
        <span class="setting-label">👁 可见范围</span>
        <span class="setting-value">{{ scopeText }}</span>
      </div>
      <div class="setting-row">
        <span class="setting-label">🎭 匿名发布</span>
        <van-switch v-model="anonymousFlag" size="22" />
      </div>
    </div>

    <van-popup v-model:show="showTopicPicker" position="bottom" round>
      <div class="picker-title">选择话题</div>

      <!-- 自拟话题 -->
      <div class="topic-create">
        <input
          v-model="newTopicName"
          class="topic-input"
          type="text"
          :maxlength="MAX_TOPIC_LEN"
          placeholder="自拟话题，如：周末去哪儿"
          @keyup.enter="handleCreateTopic"
        />
        <button
          class="topic-create-btn"
          :disabled="!newTopicName.trim() || creatingTopic"
          @click="handleCreateTopic"
        >
          {{ creatingTopic ? '创建中' : '创建' }}
        </button>
      </div>
      <div class="topic-tip">输入后点「创建」即可自拟话题；同名话题会自动复用</div>

      <div class="picker-sub">热门话题</div>
      <div v-if="!topics.length" class="picker-empty">暂时没有可选话题</div>
      <div v-else class="topic-list">
        <div
          v-for="t in topics"
          :key="t.topicId"
          class="topic-item"
          :class="{ 'topic-item-active': t.topicId === topicId }"
          @click="onTopicSelect({ topicId: t.topicId })"
        >
          <span class="topic-name"># {{ t.name }}</span>
          <span class="topic-hot">{{ t.hotValue ? '🔥 ' + t.hotValue : '' }}</span>
        </div>
      </div>
    </van-popup>

    <van-action-sheet
      v-model:show="showScopePicker"
      :actions="scopeOptions"
      cancel-text="取消"
      @select="onScopeSelect"
    />
  </div>
</template>

<style scoped lang="scss">
.publish-page {
  padding-bottom: 32px;
}
.nav-publish {
  font-size: 15px;
  font-weight: 600;
  color: var(--soul-primary);
  &.disabled {
    opacity: 0.45;
  }
}
.publish-textarea {
  width: 100%;
  border: none;
  outline: none;
  font-size: 16px;
  line-height: 1.6;
  font-family: inherit;
  color: var(--soul-text);
  resize: none;
  background: transparent;
  &::placeholder {
    color: var(--soul-text-hint);
  }
}
.char-count {
  text-align: right;
  font-size: 12px;
  color: var(--soul-text-hint);
  margin-top: 4px;
}
.section-label {
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 12px;
}
.image-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 8px;
}
.image-item {
  position: relative;
  aspect-ratio: 1;
}
.image-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  border-radius: 10px;
}
.image-del {
  position: absolute;
  top: -6px;
  right: -6px;
  width: 20px;
  height: 20px;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.55);
  color: #fff;
  font-size: 14px;
  line-height: 20px;
  text-align: center;
}
.image-add {
  aspect-ratio: 1;
  border-radius: 10px;
  background: #f4f1fb;
  color: var(--soul-text-hint);
  font-size: 26px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.hidden-input {
  display: none;
}
.setting-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 14px 0;
  border-bottom: 1px solid var(--soul-line);
  &:last-child {
    border-bottom: none;
  }
}
.setting-label {
  font-size: 15px;
  color: var(--soul-text);
}
.setting-value {
  font-size: 14px;
  color: var(--soul-text-sub);
  display: flex;
  align-items: center;
}
.setting-clear {
  font-style: normal;
  color: #ff6fa5;
  margin-left: 6px;
}
.picker-title {
  text-align: center;
  font-size: 16px;
  font-weight: 600;
  padding: 18px 0 10px;
}
.topic-create {
  display: flex;
  gap: 10px;
  padding: 4px 20px 0;
}
.topic-input {
  flex: 1;
  min-width: 0;
  height: 40px;
  padding: 0 14px;
  border-radius: 20px;
  border: 1px solid var(--soul-line);
  background: #f7f5fc;
  font-size: 14px;
  font-family: inherit;
  color: var(--soul-text);
  outline: none;
  &::placeholder {
    color: var(--soul-text-hint);
  }
  &:focus {
    border-color: var(--soul-primary);
  }
}
.topic-create-btn {
  flex: 0 0 auto;
  height: 40px;
  padding: 0 18px;
  border: none;
  border-radius: 20px;
  background: linear-gradient(90deg, #7c5cff, #a78bfa);
  color: #fff;
  font-size: 14px;
  font-weight: 600;
  &:disabled {
    opacity: 0.45;
  }
}
.topic-tip {
  font-size: 12px;
  color: var(--soul-text-hint);
  padding: 8px 20px 0;
}
.picker-sub {
  font-size: 13px;
  font-weight: 600;
  color: var(--soul-text-sub);
  padding: 16px 20px 4px;
}
.picker-empty {
  text-align: center;
  color: var(--soul-text-hint);
  font-size: 14px;
  padding: 20px 0 40px;
}
.topic-list {
  max-height: 40vh;
  overflow-y: auto;
  padding: 0 20px 20px;
}
.topic-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 0;
  border-bottom: 1px solid var(--soul-line);
  &.topic-item-active .topic-name {
    color: var(--soul-primary);
    font-weight: 600;
  }
}
.topic-name {
  font-size: 15px;
  color: var(--soul-text);
}
.topic-hot {
  font-size: 13px;
  color: var(--soul-text-hint);
}
</style>
