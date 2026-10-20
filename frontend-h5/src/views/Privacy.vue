<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import { userApi } from '@/api'

const router = useRouter()

// 隐私开关。默认值按需求：展示类默认开、被推荐/陌生人私信默认开。
const settings = ref({
  showAge: true,
  showCity: true,
  showOnline: true,
  allowRecommend: true,
  allowStrangerMsg: true
})

const loading = ref(true)
const saving = ref(false)

const items = [
  { key: 'showAge', label: '展示年龄', desc: '关闭后主页不显示你的年龄' },
  { key: 'showCity', label: '展示城市', desc: '关闭后主页不显示你的城市' },
  { key: 'showOnline', label: '展示在线状态', desc: '关闭后不展示你的在线状态' },
  { key: 'allowRecommend', label: '允许被推荐', desc: '关闭后不会出现在别人的匹配页' },
  { key: 'allowStrangerMsg', label: '允许陌生人私信', desc: '关闭后陌生人无法直接给你发消息' }
]

async function loadSettings() {
  loading.value = true
  try {
    const data = await userApi.profile()
    // 部分实现可能不带隐私字段，按已有字段合并
    if (data) {
      settings.value = {
        showAge: data.showAge ?? true,
        showCity: data.showCity ?? true,
        showOnline: data.showOnline ?? true,
        allowRecommend: data.allowRecommend ?? true,
        allowStrangerMsg: data.allowStrangerMsg ?? true
      }
    }
  } catch (e) {
    // 使用默认值
  } finally {
    loading.value = false
  }
}

/** 有开关变更即保存（实时生效） */
async function onToggle() {
  if (saving.value) return
  saving.value = true
  try {
    await userApi.updatePrivacy({
      showAge: settings.value.showAge,
      showCity: settings.value.showCity,
      showOnline: settings.value.showOnline,
      allowRecommend: settings.value.allowRecommend,
      allowStrangerMsg: settings.value.allowStrangerMsg
    })
  } catch (e) {
    // 拦截器已提示
  } finally {
    saving.value = false
  }
}

onMounted(loadSettings)
</script>

<template>
  <div class="page privacy-page">
    <van-nav-bar title="隐私设置" left-arrow fixed placeholder @click-left="router.back()" />

    <div class="privacy-hint">设置将实时生效，保护你的边界感 🛡️</div>

    <div class="soul-card">
      <div v-for="item in items" :key="item.key" class="privacy-item">
        <div class="privacy-text">
          <div class="privacy-label">{{ item.label }}</div>
          <div class="privacy-desc">{{ item.desc }}</div>
        </div>
        <van-switch v-model="settings[item.key]" size="22" @change="onToggle" />
      </div>
    </div>
  </div>
</template>

<style scoped lang="scss">
.privacy-hint {
  font-size: 13px;
  color: var(--soul-text-hint);
  padding: 20px 24px 4px;
}
.privacy-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 0;
  border-bottom: 1px solid var(--soul-line);
  &:last-child {
    border-bottom: none;
  }
}
.privacy-text {
  flex: 1;
  margin-right: 16px;
}
.privacy-label {
  font-size: 15px;
  color: var(--soul-text);
}
.privacy-desc {
  font-size: 12px;
  color: var(--soul-text-hint);
  margin-top: 4px;
}
</style>
