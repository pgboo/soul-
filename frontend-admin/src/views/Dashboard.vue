<template>
  <div class="admin-page" v-loading="loading">
    <div class="dashboard-header">
      <h2 class="page-title">数据看板</h2>
      <el-button :icon="Refresh" @click="loadData">刷新</el-button>
    </div>

    <!-- 数据卡片 -->
    <div class="stat-grid">
      <div
        v-for="card in cards"
        :key="card.key"
        class="stat-card"
        :style="{ borderTopColor: card.color }"
      >
        <div class="stat-icon" :style="{ background: card.color }">
          <el-icon :size="22"><component :is="card.icon" /></el-icon>
        </div>
        <div class="stat-body">
          <div class="stat-label">{{ card.label }}</div>
          <div class="stat-value">{{ formatNum(summary[card.key]) }}</div>
        </div>
      </div>
    </div>

    <!-- 待处理提醒 -->
    <el-alert
      v-if="pendingReport > 0"
      class="pending-alert"
      :title="`当前有 ${pendingReport} 条待处理举报，请及时处理`"
      type="warning"
      show-icon
      :closable="false"
    />

    <div class="admin-card quick-entry">
      <h3 class="card-title">快捷入口</h3>
      <div class="entry-grid">
        <div
          v-for="entry in entries"
          :key="entry.path"
          class="entry-item"
          @click="router.push(entry.path)"
        >
          <el-icon :size="24" :color="entry.color">
            <component :is="entry.icon" />
          </el-icon>
          <span>{{ entry.label }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Refresh } from '@element-plus/icons-vue'
import { getDashboardSummary } from '@/api/dashboard'

/**
 * 后台首页 - 数据看板
 * 展示用户总数、今日新增、动态数、评论数、聊天消息数、举报数、待处理举报数
 */
const router = useRouter()
const loading = ref(false)

const summary = reactive({
  userTotal: 0,
  userToday: 0,
  dynamicTotal: 0,
  dynamicToday: 0,
  commentTotal: 0,
  commentToday: 0,
  messageTotal: 0,
  messageToday: 0,
  reportTotal: 0,
  reportToday: 0,
  reportPending: 0
})

const cards = [
  { key: 'userTotal', label: '用户总数', icon: 'User', color: '#2b6cb0' },
  { key: 'userToday', label: '今日新增用户', icon: 'UserFilled', color: '#38a169' },
  { key: 'dynamicTotal', label: '瞬间总数', icon: 'Picture', color: '#805ad5' },
  { key: 'dynamicToday', label: '今日新增瞬间', icon: 'Promotion', color: '#9f7aea' },
  { key: 'commentTotal', label: '评论总数', icon: 'ChatLineSquare', color: '#dd6b20' },
  { key: 'commentToday', label: '今日新增评论', icon: 'ChatSquare', color: '#ed8936' },
  { key: 'messageTotal', label: '聊天消息数', icon: 'ChatDotRound', color: '#0987a0' },
  { key: 'messageToday', label: '今日消息数', icon: 'Message', color: '#319795' },
  { key: 'reportTotal', label: '举报总数', icon: 'Warning', color: '#c53030' },
  { key: 'reportToday', label: '今日新增举报', icon: 'WarningFilled', color: '#e53e3e' },
  { key: 'reportPending', label: '待处理举报', icon: 'Bell', color: '#d69e2e' }
]

const entries = [
  { path: '/users', label: '用户管理', icon: 'User', color: '#2b6cb0' },
  { path: '/dynamics', label: '瞬间审核', icon: 'Picture', color: '#805ad5' },
  { path: '/reports', label: '举报处理', icon: 'Warning', color: '#c53030' },
  { path: '/tags', label: '标签维护', icon: 'CollectionTag', color: '#38a169' }
]

const pendingReport = computed(() => Number(summary.reportPending) || 0)

function formatNum(val) {
  const num = Number(val)
  if (Number.isNaN(num)) return 0
  return num.toLocaleString('en-US')
}

async function loadData() {
  loading.value = true
  try {
    const data = await getDashboardSummary()
    if (data && typeof data === 'object') {
      Object.keys(summary).forEach((k) => {
        summary[k] = data[k] ?? 0
      })
    }
  } catch (e) {
    // 错误已由拦截器提示
  } finally {
    loading.value = false
  }
}

onMounted(loadData)
</script>

<style scoped>
.dashboard-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.page-title {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #1f2d3d;
}

.stat-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 16px;
  margin-bottom: 16px;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 14px;
  background: #fff;
  border-radius: 6px;
  padding: 18px 20px;
  border-top: 3px solid var(--admin-primary);
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.06);
}

.stat-icon {
  width: 46px;
  height: 46px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  flex-shrink: 0;
}

.stat-body {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.stat-label {
  font-size: 13px;
  color: #909399;
}

.stat-value {
  font-size: 24px;
  font-weight: 700;
  color: #1f2d3d;
  line-height: 1.2;
}

.pending-alert {
  margin-bottom: 16px;
}

.card-title {
  margin: 0 0 16px;
  font-size: 15px;
  font-weight: 600;
  color: #1f2d3d;
}

.entry-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
  gap: 12px;
}

.entry-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 20px 12px;
  border: 1px solid var(--admin-border);
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s;
  font-size: 14px;
  color: #606266;
}

.entry-item:hover {
  border-color: var(--admin-primary);
  color: var(--admin-primary);
  box-shadow: 0 2px 8px rgba(43, 108, 176, 0.15);
}
</style>
