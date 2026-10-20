<template>
  <div class="server-setting">
    <van-nav-bar title="服务器设置" left-arrow @click-left="onBack" />

    <div class="tip">
      <p>测试服务器用的是临时公网地址，<strong>换了地址后 App 会连不上</strong>。</p>
      <p>在这里填上新地址即可继续使用，<strong>不需要重新安装 App</strong>。</p>
    </div>

    <van-cell-group inset class="form">
      <van-field
        v-model="input"
        label="服务器地址"
        placeholder="https://xxxx.trycloudflare.com"
        clearable
        :border="false"
        autocomplete="off"
      />
    </van-cell-group>

    <div class="status" :class="statusClass">
      <span class="dot" />
      <span>{{ statusText }}</span>
    </div>

    <div class="actions">
      <van-button
        type="primary"
        block
        round
        :loading="saving"
        @click="onSave"
      >
        保存并重试
      </van-button>
      <van-button
        plain
        block
        round
        class="btn-test"
        :loading="testing"
        @click="onTest"
      >
        测试连接
      </van-button>
      <van-button
        v-if="hasCustom"
        plain
        block
        round
        class="btn-reset"
        @click="onReset"
      >
        恢复默认地址
      </van-button>
    </div>

    <div class="current">
      <p class="current-label">当前生效地址</p>
      <p class="current-value">{{ current }}</p>
      <p class="current-label">App 内置默认地址</p>
      <p class="current-value">{{ fallback }}</p>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showToast } from 'vant'
import {
  currentBase,
  defaultBase,
  setServerBase,
  hasCustomServer
} from '@/utils/asset'

const route = useRoute()
const router = useRouter()

const input = ref('')
const testing = ref(false)
const saving = ref(false)
// '' | 'ok' | 'fail'
const result = ref('')

const current = ref('')
const fallback = ref('')
const hasCustom = ref(false)

onMounted(() => {
  refresh()
  // 没设置过就先把内置地址填进去，方便用户在此基础上改
  input.value = currentBase() || ''
})

function refresh() {
  current.value = currentBase() || '（同源，相对路径）'
  fallback.value = defaultBase() || '（无，网页同源部署）'
  hasCustom.value = hasCustomServer()
}

const statusClass = computed(() => (result.value ? `is-${result.value}` : ''))
const statusText = computed(() => {
  if (result.value === 'ok') return '连接正常'
  if (result.value === 'fail') return '连不上，请检查地址是否正确'
  return '修改后建议先点「测试连接」'
})

/** 规范化用户输入：允许只填域名，自动补 https:// */
function normalizeInput() {
  let v = String(input.value || '').trim().replace(/\/+$/, '')
  if (!v) return ''
  if (!/^https?:\/\//i.test(v)) v = 'https://' + v
  return v
}

/** 直接 fetch 探测，绕开 axios（否则测的还是旧地址） */
async function probe(base) {
  const ctrl = new AbortController()
  const timer = setTimeout(() => ctrl.abort(), 12000)
  try {
    const res = await fetch(`${base}/api/tag/list`, {
      method: 'GET',
      signal: ctrl.signal,
      headers: { Accept: 'application/json' }
    })
    return res.ok
  } catch (e) {
    return false
  } finally {
    clearTimeout(timer)
  }
}

async function onTest() {
  const base = normalizeInput()
  if (!base) {
    showToast('请先填写服务器地址')
    return
  }
  testing.value = true
  result.value = ''
  try {
    const ok = await probe(base)
    result.value = ok ? 'ok' : 'fail'
    showToast(ok ? '连接正常' : '连不上，请检查地址')
  } finally {
    testing.value = false
  }
}

async function onSave() {
  const base = normalizeInput()
  saving.value = true
  try {
    // 有填地址就先探一次，通不通都允许保存（可能只是暂时没开）
    if (base) {
      const ok = await probe(base)
      result.value = ok ? 'ok' : 'fail'
      if (!ok) {
        showToast('该地址暂时连不上，已保存，稍后可重试')
      }
    }
    setServerBase(base)
    refresh()
    showToast('已保存')

    setTimeout(() => {
      const redirect = route.query.redirect
      if (redirect) {
        router.replace(String(redirect))
      } else {
        router.replace({ name: 'login' })
      }
    }, 600)
  } finally {
    saving.value = false
  }
}

function onReset() {
  setServerBase('')
  input.value = defaultBase() || ''
  result.value = ''
  refresh()
  showToast('已恢复默认地址')
}

function onBack() {
  if (window.history.length > 1) router.back()
  else router.replace({ name: 'login' })
}
</script>

<style scoped lang="scss">
.server-setting {
  min-height: 100vh;
  background: var(--soul-bg);
  padding-bottom: 40px;
}

.tip {
  padding: 16px 20px 4px;
  color: var(--soul-text-sub);
  font-size: 13px;
  line-height: 1.7;

  strong {
    color: var(--soul-primary);
    font-weight: 600;
  }

  p + p {
    margin-top: 6px;
  }
}

.form {
  margin-top: 12px;
}

.status {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 10px 22px 0;
  font-size: 12px;
  color: var(--soul-text-hint);

  .dot {
    width: 6px;
    height: 6px;
    border-radius: 50%;
    background: var(--soul-text-hint);
  }

  &.is-ok {
    color: #22a06b;
    .dot {
      background: #22a06b;
    }
  }

  &.is-fail {
    color: #e5484d;
    .dot {
      background: #e5484d;
    }
  }
}

.actions {
  padding: 20px 20px 0;

  .btn-test,
  .btn-reset {
    margin-top: 12px;
  }

  .btn-reset {
    color: var(--soul-text-sub);
    border-color: var(--soul-line);
  }
}

.current {
  margin: 28px 20px 0;
  padding: 14px 16px;
  border-radius: var(--soul-radius);
  background: var(--soul-card);
  box-shadow: var(--soul-shadow);

  .current-label {
    font-size: 12px;
    color: var(--soul-text-hint);
    margin-bottom: 2px;
  }

  .current-value {
    font-size: 13px;
    color: var(--soul-text);
    word-break: break-all;
    margin-bottom: 12px;

    &:last-child {
      margin-bottom: 0;
    }
  }
}
</style>
