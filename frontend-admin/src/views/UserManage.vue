<template>
  <div class="admin-page">
    <!-- 搜索栏 -->
    <div class="admin-card filter-bar">
      <el-input
        v-model="query.nickname"
        placeholder="昵称"
        clearable
        :prefix-icon="Search"
        @keyup.enter="onSearch"
        @clear="onSearch"
      />
      <el-input
        v-model="query.phone"
        placeholder="手机号"
        clearable
        :prefix-icon="Iphone"
        @keyup.enter="onSearch"
        @clear="onSearch"
      />
      <el-select v-model="query.status" placeholder="用户状态" clearable>
        <el-option label="全部" :value="null" />
        <el-option label="正常" :value="1" />
        <el-option label="已封禁" :value="2" />
      </el-select>
      <el-button type="primary" :icon="Search" @click="onSearch">搜索</el-button>
      <el-button :icon="RefreshLeft" @click="onReset">重置</el-button>
    </div>

    <!-- 表格 -->
    <div class="admin-card">
      <el-table
        :data="list"
        v-loading="loading"
        border
        stripe
        :empty-text="'暂无用户数据'"
      >
        <el-table-column prop="userId" label="用户ID" width="100" />
        <el-table-column prop="userNo" label="灵魂号" width="110">
          <template #default="{ row }">{{ row.userNo || '-' }}</template>
        </el-table-column>
        <el-table-column prop="nickname" label="昵称" min-width="140">
          <template #default="{ row }">
            {{ row.nickname || '未完善资料' }}
          </template>
        </el-table-column>
        <el-table-column prop="gender" label="性别" width="80">
          <template #default="{ row }">
            {{ genderText(row.gender) }}
          </template>
        </el-table-column>
        <el-table-column prop="phoneMask" label="手机号" width="140">
          <template #default="{ row }">{{ row.phoneMask || '-' }}</template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="110">
          <template #default="{ row }">
            <el-tag v-if="row.status === 1" type="success" size="small">正常</el-tag>
            <el-tag v-else type="danger" size="small">已封禁</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="禁言" width="150">
          <template #default="{ row }">
            <el-tag v-if="isMuted(row)" type="warning" size="small">
              至 {{ row.muteEndTime }}
            </el-tag>
            <el-tag v-else type="info" size="small" effect="plain">未禁言</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="风险等级" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.riskLevel === 1" type="danger" size="small" effect="plain">观察中</el-tag>
            <span v-else class="text-muted">正常</span>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="注册时间" min-width="170">
          <template #default="{ row }">{{ row.createTime || '-' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDetail(row)">详情</el-button>
            <el-button
              link
              :type="isMuted(row) ? 'success' : 'warning'"
              @click="openMute(row)"
            >
              {{ isMuted(row) ? '解除禁言' : '禁言' }}
            </el-button>
            <el-button
              link
              :type="row.status === 1 ? 'danger' : 'success'"
              @click="openBan(row)"
            >
              {{ row.status === 1 ? '封禁' : '解封' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="query.pageNum"
          v-model:page-size="query.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          background
          @size-change="onSearch"
          @current-change="loadList"
        />
      </div>
    </div>

    <!-- 用户详情弹窗 -->
    <el-dialog v-model="detailVisible" title="用户详情" width="680px">
      <div v-loading="detailLoading">
        <el-descriptions :column="2" border v-if="detail">
          <el-descriptions-item label="用户ID">{{ detail.userId }}</el-descriptions-item>
          <el-descriptions-item label="灵魂号">{{ detail.userNo || '-' }}</el-descriptions-item>
          <el-descriptions-item label="昵称">{{ detail.nickname || '-' }}</el-descriptions-item>
          <el-descriptions-item label="手机号">{{ detail.phoneMask || '-' }}</el-descriptions-item>
          <el-descriptions-item label="性别">{{ genderText(detail.gender) }}</el-descriptions-item>
          <el-descriptions-item label="生日">{{ detail.birthday || '-' }}</el-descriptions-item>
          <el-descriptions-item label="城市">{{ detail.city || '-' }}</el-descriptions-item>
          <el-descriptions-item label="灵魂号 ID">{{ detail.soulId || '-' }}</el-descriptions-item>
          <el-descriptions-item label="资料完整度">
            {{ detail.completePercent != null ? detail.completePercent + '%' : '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="注册时间">{{ detail.createTime || '-' }}</el-descriptions-item>
          <el-descriptions-item label="签名" :span="2">
            {{ detail.sign || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="简介" :span="2">
            {{ detail.intro || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag v-if="detail.status === 1" type="success" size="small">正常</el-tag>
            <el-tag v-else type="danger" size="small">已封禁</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="禁言">
            <el-tag v-if="isMuted(detail)" type="warning" size="small">
              至 {{ detail.muteEndTime }}
            </el-tag>
            <el-tag v-else type="info" size="small" effect="plain">未禁言</el-tag>
          </el-descriptions-item>
        </el-descriptions>

        <!-- 风控记录 -->
        <div v-if="detail && detail.riskRecords && detail.riskRecords.length" class="risk-block">
          <div class="risk-title">风控记录</div>
          <el-table :data="detail.riskRecords" size="small" border>
            <el-table-column label="类型" width="90">
              <template #default="{ row }">{{ riskTypeText(row.riskType) }}</template>
            </el-table-column>
            <el-table-column prop="reason" label="原因" min-width="140" show-overflow-tooltip />
            <el-table-column prop="startTime" label="开始时间" min-width="150" />
            <el-table-column prop="endTime" label="结束时间" min-width="150">
              <template #default="{ row }">{{ row.endTime || '永久' }}</template>
            </el-table-column>
            <el-table-column label="状态" width="90">
              <template #default="{ row }">
                <el-tag v-if="row.status === 1" type="danger" size="small">生效中</el-tag>
                <el-tag v-else type="info" size="small">已解除</el-tag>
              </template>
            </el-table-column>
          </el-table>
        </div>

        <el-empty v-else-if="!detailLoading" description="暂无详情" />
      </div>
    </el-dialog>

    <!-- 禁言 / 封禁原因弹窗 -->
    <el-dialog v-model="actionVisible" :title="actionTitle" width="480px">
      <el-form label-position="top">
        <el-form-item v-if="actionType === 'mute' && !isMuted(actionTarget)" label="禁言时长">
          <el-select v-model="actionForm.muteDays" style="width: 100%">
            <el-option label="永久禁言" :value="null" />
            <el-option label="1 天" :value="1" />
            <el-option label="3 天" :value="3" />
            <el-option label="7 天" :value="7" />
            <el-option label="30 天" :value="30" />
          </el-select>
        </el-form-item>
        <el-form-item label="操作原因（可选）">
          <el-input
            v-model="actionForm.reason"
            type="textarea"
            :rows="3"
            maxlength="200"
            show-word-limit
            placeholder="请填写操作原因"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="actionVisible = false">取消</el-button>
        <el-button type="primary" :loading="actionLoading" @click="confirmAction">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, RefreshLeft, Iphone } from '@element-plus/icons-vue'
import { getUserList, getUserDetail, muteUser, banUser } from '@/api/user'

/**
 * 用户管理
 * 搜索 + 表格 + 分页 + 详情弹窗 + 禁言/封禁操作（需填原因）
 * 字段口径以后端 DTO 为准
 */
const loading = ref(false)
const list = ref([])
const total = ref(0)

const query = reactive({
  nickname: '',
  phone: '',
  status: null,
  pageNum: 1,
  pageSize: 10
})

/** 性别文案 */
function genderText(gender) {
  if (gender === 1) return '男'
  if (gender === 2) return '女'
  return '未知'
}

/** 是否处于禁言中：后端用 muteEndTime 判断（null=未禁言） */
function isMuted(row) {
  if (!row) return false
  return !!(row.muteEndTime && String(row.muteEndTime).trim())
}

/** 风控类型文案 */
function riskTypeText(type) {
  return { 1: '禁言', 2: '封禁', 3: '观察' }[type] ?? '未知'
}

async function loadList() {
  loading.value = true
  try {
    const params = {
      pageNum: query.pageNum,
      pageSize: query.pageSize
    }
    if (query.nickname) params.nickname = query.nickname
    if (query.phone) params.phone = query.phone
    if (query.status !== null && query.status !== '') params.status = query.status

    const data = await getUserList(params)
    list.value = data?.records || []
    total.value = data?.total || 0
  } catch (e) {
    list.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

function onSearch() {
  query.pageNum = 1
  loadList()
}

function onReset() {
  query.nickname = ''
  query.phone = ''
  query.status = null
  query.pageNum = 1
  loadList()
}

// ===== 详情 =====
const detailVisible = ref(false)
const detailLoading = ref(false)
const detail = ref(null)

async function openDetail(row) {
  detailVisible.value = true
  detailLoading.value = true
  detail.value = null
  try {
    const data = await getUserDetail({ userId: row.userId })
    detail.value = data || null
  } catch (e) {
    detail.value = null
  } finally {
    detailLoading.value = false
  }
}

// ===== 禁言 / 封禁 =====
const actionVisible = ref(false)
const actionLoading = ref(false)
const actionType = ref('mute') // mute | ban
const actionTarget = ref({})
const actionForm = reactive({ muteDays: null, reason: '' })

const actionTitle = computed(() => {
  if (actionType.value === 'mute') {
    return isMuted(actionTarget.value) ? '解除禁言' : '禁言用户'
  }
  return actionTarget.value.status === 1 ? '封禁用户' : '解除封禁'
})

function openMute(row) {
  actionType.value = 'mute'
  actionTarget.value = { ...row }
  actionForm.muteDays = null
  actionForm.reason = ''
  actionVisible.value = true
}

function openBan(row) {
  actionType.value = 'ban'
  actionTarget.value = { ...row }
  actionForm.muteDays = null
  actionForm.reason = ''
  actionVisible.value = true
}

async function confirmAction() {
  actionLoading.value = true
  try {
    if (actionType.value === 'mute') {
      const mute = !isMuted(actionTarget.value)
      await muteUser({
        userId: actionTarget.value.userId,
        mute,
        muteDays: mute ? actionForm.muteDays : undefined,
        reason: actionForm.reason || undefined
      })
      ElMessage.success(mute ? '禁言成功' : '解除禁言成功')
    } else {
      const ban = actionTarget.value.status === 1
      await banUser({
        userId: actionTarget.value.userId,
        ban,
        reason: actionForm.reason || undefined
      })
      ElMessage.success(ban ? '封禁成功' : '解除封禁成功')
    }
    actionVisible.value = false
    loadList()
  } catch (e) {
    // 拦截器已提示
  } finally {
    actionLoading.value = false
  }
}

onMounted(loadList)
</script>

<style scoped>
.tag-item {
  margin: 0 6px 6px 0;
}

.risk-block {
  margin-top: 16px;
}

.risk-title {
  font-size: 14px;
  font-weight: 600;
  color: #1f2d3d;
  margin-bottom: 8px;
}
</style>
