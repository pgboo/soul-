<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { showToast, showLoadingToast, closeToast } from 'vant'
import { userApi, uploadApi } from '@/api'
import { useUserStore } from '@/stores/user'
import { CITY_OPTIONS } from '@/constants/city'
import SoulAvatar from '@/components/SoulAvatar.vue'

const router = useRouter()
const userStore = useUserStore()

const form = ref({
  avatar: '',
  nickname: '',
  city: '',
  sign: '',
  intro: '',
  emotionStatus: 0,
  friendGoal: 0
})
const loading = ref(true)
const submitting = ref(false)

const fileInput = ref(null)

/** 预设默认头像（点选即换，无需上传） */
const presetAvatars = Array.from({ length: 8 }, (_, i) => '/avatars/preset-' + (i + 1) + '.svg')

const emotionOptions = [
  { name: '保密', value: 0 },
  { name: '单身', value: 1 },
  { name: '恋爱中', value: 2 }
]
const friendGoalOptions = [
  { name: '保密', value: 0 },
  { name: '交朋友', value: 1 },
  { name: '找陪伴', value: 2 },
  { name: '找对象', value: 3 }
]
const cities = CITY_OPTIONS

const showCityPicker = ref(false)
const showEmotionPicker = ref(false)
const showGoalPicker = ref(false)

const emotionText = computed(
  () => emotionOptions.find((o) => o.value === form.value.emotionStatus)?.name || '保密'
)
const goalText = computed(
  () => friendGoalOptions.find((o) => o.value === form.value.friendGoal)?.name || '保密'
)

async function loadProfile() {
  loading.value = true
  try {
    const data = await userApi.profile()
    form.value.avatar = data.avatar || ''
    form.value.nickname = data.nickname || ''
    form.value.city = data.city || ''
    form.value.sign = data.sign || ''
    form.value.intro = data.intro || ''
    form.value.emotionStatus = data.emotionStatus ?? 0
    form.value.friendGoal = data.friendGoal ?? 0
  } catch (e) {
    // 拦截器已提示
  } finally {
    loading.value = false
  }
}

function pickAvatar() {
  fileInput.value?.click()
}

async function onAvatarChange(e) {
  const file = e.target.files?.[0]
  e.target.value = ''
  if (!file) return
  if (file.size > 5 * 1024 * 1024) {
    showToast('图片不能超过 5MB')
    return
  }
  showLoadingToast({ message: '上传中...', forbidClick: true, duration: 0 })
  try {
    const data = await uploadApi.image(file, 'avatar')
    form.value.avatar = data.url
  } catch (err) {
    // 拦截器已提示
  } finally {
    closeToast()
  }
}

function onCityConfirm({ selectedOptions }) {
  form.value.city = selectedOptions[0]?.text || ''
  showCityPicker.value = false
}
function onEmotionSelect(action) {
  form.value.emotionStatus = action.value
  showEmotionPicker.value = false
}
function onGoalSelect(action) {
  form.value.friendGoal = action.value
  showGoalPicker.value = false
}

async function handleSave() {
  if (!form.value.nickname.trim()) {
    showToast('昵称不能为空')
    return
  }
  if (submitting.value) return
  submitting.value = true
  try {
    const data = await userApi.updateProfile({
      avatar: form.value.avatar,
      nickname: form.value.nickname.trim(),
      city: form.value.city,
      sign: form.value.sign.trim(),
      intro: form.value.intro.trim(),
      emotionStatus: form.value.emotionStatus,
      friendGoal: form.value.friendGoal
    })
    userStore.profile = data
    if (data?.nickname) userStore.nickname = data.nickname
    if (data?.avatar) userStore.avatar = data.avatar
    userStore.persist()
    showToast('已保存')
    router.back()
  } catch (e) {
    // 拦截器已提示
  } finally {
    submitting.value = false
  }
}

onMounted(loadProfile)
</script>

<template>
  <div class="page edit-page">
    <van-nav-bar
      title="编辑资料"
      left-arrow
      fixed
      placeholder
      @click-left="router.back()"
    >
      <template #right>
        <span class="nav-save" @click="handleSave">
          {{ submitting ? '保存中' : '保存' }}
        </span>
      </template>
    </van-nav-bar>

    <div class="soul-card">
      <div class="avatar-row" @click="pickAvatar">
        <SoulAvatar :src="form.avatar" :size="64" />
        <span class="avatar-change">更换头像</span>
        <span class="arrow">›</span>
      </div>
      <input
        ref="fileInput"
        type="file"
        accept="image/*"
        class="hidden-input"
        @change="onAvatarChange"
      />
      <!-- 预设头像 -->
      <div class="preset-block">
        <div class="preset-label">或选一个默认头像</div>
        <div class="preset-list">
          <img
            v-for="p in presetAvatars"
            :key="p"
            :src="p"
            class="preset-item"
            :class="{ selected: form.avatar === p }"
            alt="默认头像"
            @click.stop="form.avatar = p"
          />
        </div>
      </div>

      <div class="form-item">
        <label class="form-label">昵称</label>
        <input v-model="form.nickname" class="form-input" maxlength="16" placeholder="昵称" />
      </div>

      <div class="form-item" @click="showCityPicker = true">
        <label class="form-label">城市</label>
        <div class="form-value" :class="{ placeholder: !form.city }">
          {{ form.city || '请选择城市' }}
        </div>
      </div>

      <div class="form-item">
        <label class="form-label">个性签名</label>
        <input
          v-model="form.sign"
          class="form-input"
          maxlength="50"
          placeholder="一句话介绍自己"
        />
      </div>

      <div class="form-item">
        <label class="form-label">个人简介</label>
        <textarea
          v-model="form.intro"
          class="form-textarea"
          maxlength="200"
          rows="3"
          placeholder="介绍一下你自己（最多200字）"
        ></textarea>
      </div>

      <div class="form-item" @click="showEmotionPicker = true">
        <label class="form-label">情感状态</label>
        <div class="form-value">{{ emotionText }}</div>
      </div>

      <div class="form-item" @click="showGoalPicker = true">
        <label class="form-label">交友目的</label>
        <div class="form-value">{{ goalText }}</div>
      </div>
    </div>

    <van-popup v-model:show="showCityPicker" position="bottom" round>
      <van-picker
        title="选择城市"
        :columns="cities"
        @confirm="onCityConfirm"
        @cancel="showCityPicker = false"
      />
    </van-popup>
    <van-action-sheet
      v-model:show="showEmotionPicker"
      :actions="emotionOptions"
      cancel-text="取消"
      @select="onEmotionSelect"
    />
    <van-action-sheet
      v-model:show="showGoalPicker"
      :actions="friendGoalOptions"
      cancel-text="取消"
      @select="onGoalSelect"
    />
  </div>
</template>

<style scoped lang="scss">
.nav-save {
  font-size: 15px;
  font-weight: 600;
  color: var(--soul-primary);
}
.preset-block {
  padding: 12px 0 16px;
  border-bottom: 1px solid var(--soul-line);
  margin-bottom: 4px;
}
.preset-label {
  font-size: 13px;
  color: var(--soul-text-sub);
  margin-bottom: 10px;
}
.preset-list {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}
.preset-item {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  border: 2.5px solid transparent;
  padding: 1.5px;
  cursor: pointer;
  transition: transform 0.15s, border-color 0.2s, box-shadow 0.2s;
  &:active {
    transform: scale(0.92);
  }
  &.selected {
    border-color: var(--soul-primary);
    box-shadow: 0 0 0 3px rgba(124, 92, 255, 0.18);
  }
}
.avatar-row {
  display: flex;
  align-items: center;
  padding-bottom: 18px;
  border-bottom: 1px solid var(--soul-line);
}
.avatar-change {
  flex: 1;
  margin-left: 16px;
  font-size: 15px;
  color: var(--soul-text);
}
.arrow {
  color: var(--soul-text-hint);
  font-size: 20px;
}
.hidden-input {
  display: none;
}
.form-item {
  padding: 16px 0;
  border-bottom: 1px solid var(--soul-line);
  &:last-child {
    border-bottom: none;
  }
}
.form-label {
  display: block;
  font-size: 13px;
  color: var(--soul-text-sub);
  margin-bottom: 8px;
}
.form-input {
  width: 100%;
  border: none;
  outline: none;
  font-size: 15px;
  color: var(--soul-text);
  background: transparent;
  &::placeholder {
    color: var(--soul-text-hint);
  }
}
.form-value {
  font-size: 15px;
  color: var(--soul-text);
  &.placeholder {
    color: var(--soul-text-hint);
  }
}
.form-textarea {
  width: 100%;
  border: none;
  outline: none;
  font-size: 15px;
  color: var(--soul-text);
  background: transparent;
  resize: none;
  font-family: inherit;
  &::placeholder {
    color: var(--soul-text-hint);
  }
}
</style>
