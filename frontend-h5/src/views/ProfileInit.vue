<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { showToast, showLoadingToast, closeToast, showConfirmDialog } from 'vant'
import { userApi, uploadApi } from '@/api'
import { useUserStore } from '@/stores/user'
import { CITY_OPTIONS } from '@/constants/city'
import SoulAvatar from '@/components/SoulAvatar.vue'

const router = useRouter()
const userStore = useUserStore()

const form = ref({
  avatar: '',
  nickname: '',
  gender: 1,
  birthday: '',
  city: '',
  sign: ''
})

const showGenderPicker = ref(false)
const showDatePicker = ref(false)
const showCityPicker = ref(false)
const submitting = ref(false)

const genderText = computed(() => (form.value.gender === 1 ? '男生' : '女生'))
const cities = CITY_OPTIONS

/** 预设默认头像（选填，不选后端会按用户 ID 分配一个） */
const presetAvatars = Array.from({ length: 8 }, (_, i) => '/avatars/preset-' + (i + 1) + '.svg')

const canSubmit = computed(
  () =>
    !!form.value.nickname.trim() &&
    !!form.value.gender &&
    !!form.value.birthday &&
    !!form.value.city
)

/**
 * 缺失项提示。
 * 按钮若用 disabled 拦下点击，用户得不到任何反馈，只会觉得「点了没用」，
 * 因此改为可点击 + 明确告知缺哪几项。
 */
const missingTip = computed(() => {
  const missing = []
  if (!form.value.nickname.trim()) missing.push('昵称')
  if (!form.value.gender) missing.push('性别')
  if (!form.value.birthday) missing.push('生日')
  if (!form.value.city) missing.push('城市')
  return `请先完善：${missing.join('、')}`
})

// 头像上传
const fileInput = ref(null)
function pickAvatar() {
  fileInput.value?.click()
}
async function onAvatarChange(e) {
  const file = e.target.files?.[0]
  if (!file) return
  if (file.size > 5 * 1024 * 1024) {
    showToast('图片不能超过 5MB')
    return
  }
  showLoadingToast({ message: '上传中...', forbidClick: true, duration: 0 })
  try {
    const data = await uploadApi.image(file, 'avatar')
    form.value.avatar = data.url
    showToast('头像上传成功')
  } catch (err) {
    // 拦截器已提示
  } finally {
    closeToast()
    e.target.value = ''
  }
}

// 性别选择
const genderActions = [
  { name: '男生', value: 1 },
  { name: '女生', value: 2 }
]
function onGenderSelect(action) {
  form.value.gender = action.value
  showGenderPicker.value = false
}

// 生日选择
function onBirthdayConfirm({ selectedValues }) {
  form.value.birthday = selectedValues.map((v) => String(v).padStart(2, '0')).join('-')
  showDatePicker.value = false
}

/**
 * 生日选择器的当前值。
 * 不设置时 Vant 会默认选中第一项，也就是 min-date（1970-01-01），
 * 用户不滚动直接点确认就会填成 1970 年出生，因此显式给一个合理默认值。
 */
const birthdayPickerValue = ref(['2000', '01', '01'])

function openDatePicker() {
  birthdayPickerValue.value = form.value.birthday
    ? form.value.birthday.split('-')
    : ['2000', '01', '01']
  showDatePicker.value = true
}

// 城市选择
function onCityConfirm({ selectedOptions }) {
  form.value.city = selectedOptions[0]?.text || ''
  showCityPicker.value = false
}

const minBirthday = new Date(1970, 0, 1)
const maxBirthday = new Date()

async function handleSubmit() {
  if (!canSubmit.value) {
    showToast(missingTip.value)
    return
  }
  submitting.value = true
  try {
    const data = await userApi.completeProfile({
      avatar: form.value.avatar,
      nickname: form.value.nickname.trim(),
      gender: form.value.gender,
      birthday: form.value.birthday,
      city: form.value.city,
      sign: form.value.sign.trim()
    })
    userStore.markComplete('profile')
    userStore.profile = data
    showToast('资料已保存')
    router.replace({ name: 'tag-select' })
  } catch (e) {
    // 拦截器已提示
  } finally {
    submitting.value = false
  }
}

async function handleBack() {
  try {
    await showConfirmDialog({
      title: '返回登录页',
      message: '将退出当前账号并返回登录页，已填写的内容不会保存',
      confirmButtonText: '退出并返回',
      cancelButtonText: '继续填写'
    })
  } catch (e) {
    return
  }
  await userStore.logout()
  router.replace({ name: 'login' })
}
</script>

<template>
  <div class="page init-page">
    <div class="init-header">
      <h1 class="init-title gradient-text">创建你的灵魂名片</h1>
      <p class="init-sub">让我们更懂你一点</p>
    </div>

    <div class="soul-card">
      <!-- 头像 -->
      <div class="avatar-row" @click="pickAvatar">
        <SoulAvatar :src="form.avatar" :size="72" />
        <div class="avatar-tip">
          <div class="avatar-main">上传头像 <span class="opt-tag">选填</span></div>
          <div class="avatar-sub">点击选择一张喜欢的照片</div>
        </div>
        <span class="arrow">›</span>
      </div>

      <!-- 预设头像：不强制，不选会随机分配 -->
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
        <div class="preset-hint">不选也没关系，会自动分配一个可爱的</div>
      </div>
      <input
        ref="fileInput"
        type="file"
        accept="image/*"
        class="hidden-input"
        @change="onAvatarChange"
      />

      <!-- 昵称 -->
      <div class="form-item">
        <label class="form-label">昵称 <span class="req">*</span></label>
        <input
          v-model="form.nickname"
          class="form-input"
          maxlength="16"
          placeholder="取一个有趣的名字（1-16字）"
        />
      </div>

      <!-- 性别 -->
      <div class="form-item" @click="showGenderPicker = true">
        <label class="form-label">性别 <span class="req">*</span></label>
        <div class="form-value" :class="{ placeholder: !form.gender }">
          {{ genderText }}
        </div>
      </div>

      <!-- 生日 -->
      <div class="form-item" @click="openDatePicker">
        <label class="form-label">生日 <span class="req">*</span></label>
        <div class="form-value" :class="{ placeholder: !form.birthday }">
          {{ form.birthday || '请选择生日' }}
        </div>
      </div>

      <!-- 城市 -->
      <div class="form-item" @click="showCityPicker = true">
        <label class="form-label">城市 <span class="req">*</span></label>
        <div class="form-value" :class="{ placeholder: !form.city }">
          {{ form.city || '请选择城市' }}
        </div>
      </div>

      <!-- 签名 -->
      <div class="form-item">
        <label class="form-label">个性签名</label>
        <textarea
          v-model="form.sign"
          class="form-textarea"
          maxlength="50"
          rows="3"
          placeholder="用一句话介绍自己（选填，最多50字）"
        ></textarea>
      </div>
    </div>

    <div class="init-footer">
      <button
        class="soul-btn"
        :disabled="submitting"
        @click="handleSubmit"
      >
        {{ submitting ? '保存中...' : '下一步' }}
      </button>
      <button class="back-link" @click="handleBack">返回</button>
    </div>

    <!-- 选择器 -->
    <van-action-sheet
      v-model:show="showGenderPicker"
      :actions="genderActions"
      cancel-text="取消"
      @select="onGenderSelect"
    />
    <van-popup v-model:show="showDatePicker" position="bottom" round>
      <van-date-picker
        v-model="birthdayPickerValue"
        title="选择生日"
        :min-date="minBirthday"
        :max-date="maxBirthday"
        @confirm="onBirthdayConfirm"
        @cancel="showDatePicker = false"
      />
    </van-popup>
    <van-popup v-model:show="showCityPicker" position="bottom" round>
      <van-picker
        title="选择城市"
        :columns="cities"
        @confirm="onCityConfirm"
        @cancel="showCityPicker = false"
      />
    </van-popup>
  </div>
</template>

<style scoped lang="scss">
.init-page {
  padding-top: 8px;
}
.init-header {
  padding: 28px 24px 12px;
}
.init-title {
  font-size: 26px;
  font-weight: 800;
  margin: 0 0 6px;
}
.init-sub {
  font-size: 14px;
  color: var(--soul-text-sub);
  margin: 0;
}
.opt-tag {
  font-size: 11px;
  font-weight: 400;
  color: var(--soul-text-hint);
  background: #f0ebfa;
  border-radius: 8px;
  padding: 1px 6px;
  margin-left: 6px;
  vertical-align: 1px;
}
.preset-block {
  padding: 14px 0 18px;
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
  width: 46px;
  height: 46px;
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
.preset-hint {
  font-size: 11px;
  color: var(--soul-text-hint);
  margin-top: 10px;
}
.hidden-input {
  display: none;
}
.avatar-row {
  display: flex;
  align-items: center;
  padding-bottom: 20px;
  border-bottom: 1px solid var(--soul-line);
  margin-bottom: 4px;
}
.avatar-tip {
  flex: 1;
  margin-left: 16px;
}
.avatar-main {
  font-size: 15px;
  font-weight: 600;
}
.avatar-sub {
  font-size: 12px;
  color: var(--soul-text-hint);
  margin-top: 4px;
}
.arrow {
  color: var(--soul-text-hint);
  font-size: 22px;
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
.req {
  color: #ff6fa5;
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
.init-footer {
  padding: 20px 16px 32px;
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
