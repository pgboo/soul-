<template>
  <div class="admin-page">
    <!-- 搜索栏 -->
    <div class="admin-card filter-bar">
      <el-select v-model="query.status" placeholder="处理状态" clearable>
        <el-option label="全部" :value="null" />
        <el-option label="待处理" :value="1" />
        <el-option label="已处理" :value="2" />
        <el-option label="驳回" :value="3" />
      </el-select>
      <el-select v-model="query.targetType" placeholder="举报对象" clearable>
        <el-option label="全部" :value="null" />
        <el-option label="动态" :value="1" />
        <el-option label="评论" :value="2" />
        <el-option label="用户" :value="3" />
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
        empty-text="暂无举报数据"
      >
        <el-table-column prop="reportId" label="ID" width="80" />
        <el-table-column label="举报对象" width="100">
          <template #default="{ row }">
            {{ targetTypeText(row.targetType) }}
          </template>
        </el-table-column>
        <el-table-column prop="reasonType" label="原因类型" width="120">
          <template #default="{ row }">{{ row.reasonType || '-' }}</template>
        </el-table-column>
        <el-table-column prop="reason" label="举报原因" min-width="150" show-overflow-tooltip>
          <template #default="{ row }">{{ row.reason || '-' }}</template>
        </el-table-column>
        <el-table-column prop="targetSummary" label="被举报内容摘要" min-width="180" show-overflow-tooltip>
          <template #default="{ row }">{{ row.targetSummary || '-' }}</template>
        </el-table-column>
        <el-table-column label="举报人" width="130">
          <template #default="{ row }">
            {{ row.reporterNickname || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="处理状态" width="110">
          <template #default="{ row }">
            <el-tag v-if="row.status === 1" type="danger" size="small">待处理</el-tag>
            <el-tag v-else-if="row.status === 2" type="success" size="small">已处理</el-tag>
            <el-tag v-else type="info" size="small">已驳回</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="举报时间" min-width="170">
          <template #default="{ row }">{{ row.createTime || '-' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openEvidence(row)">证据</el-button>
            <el-button
              link
              type="primary"
              :disabled="row.status !== 1"
              @click="openHandle(row)"
            >
              处理
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="query.pageNum"
          v-model:page-size="query.pageSize"
          :page-sizes="[10, 20, 50]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          background
          @size-change="onSearch"
          @current-change="loadList"
        />
      </div>
    </div>

    <!-- 证据查看弹窗 -->
    <el-dialog v-model="evidenceVisible" title="举报证据" width="560px">
      <div v-if="evidence" class="evidence">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="举报对象">
            {{ targetTypeText(evidence.targetType) }}（ID: {{ evidence.targetId }}）
          </el-descriptions-item>
          <el-descriptions-item label="举报人">
            {{ evidence.reporterNickname || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="原因类型">
            {{ evidence.reasonType || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="举报原因">
            {{ evidence.reason || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="被举报内容摘要">
            <div class="evidence-content">{{ evidence.targetSummary || '(无内容)' }}</div>
          </el-descriptions-item>
          <el-descriptions-item v-if="evidence.handleResult" label="处理结果">
            {{ evidence.handleResult }}
          </el-descriptions-item>
          <el-descriptions-item v-if="evidence.handleTime" label="处理时间">
            {{ evidence.handleTime }}
          </el-descriptions-item>
        </el-descriptions>
      </div>
      <el-empty v-else description="暂无证据" />
    </el-dialog>

    <!-- 处理弹窗 -->
    <el-dialog v-model="handleVisible" title="处理举报" width="500px">
      <el-form label-position="top">
        <el-form-item label="处理动作">
          <el-radio-group v-model="handleForm.handleType">
            <el-radio value="reject">驳回举报</el-radio>
            <el-radio value="offline">下架内容</el-radio>
            <el-radio value="mute">禁言用户</el-radio>
            <el-radio value="ban">封禁用户</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="handleForm.handleType === 'mute'" label="禁言时长">
          <el-select v-model="handleForm.muteDays" style="width: 100%">
            <el-option label="永久禁言" :value="null" />
            <el-option label="1 天" :value="1" />
            <el-option label="3 天" :value="3" />
            <el-option label="7 天" :value="7" />
            <el-option label="30 天" :value="30" />
          </el-select>
        </el-form-item>
        <el-form-item label="处理结果备注">
          <el-input
            v-model="handleForm.handleResult"
            type="textarea"
            :rows="3"
            maxlength="200"
            show-word-limit
            placeholder="请填写处理结果备注"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="handleVisible = false">取消</el-button>
        <el-button type="primary" :loading="handleLoading" @click="confirmHandle">
          提交处理
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, RefreshLeft } from '@element-plus/icons-vue'
import { getReportList, handleReport } from '@/api/report'

/**
 * 举报管理
 * 列表 + 证据查看 + 处理动作（驳回 / 下架 / 禁言 / 封禁）+ 处理结果备注
 * 字段口径以后端 DTO 为准：status(1待处理/2已处理/3驳回)
 */
const loading = ref(false)
const list = ref([])
const total = ref(0)

const query = reactive({
  status: null,
  targetType: null,
  pageNum: 1,
  pageSize: 10
})

function targetTypeText(type) {
  return { 1: '动态', 2: '评论', 3: '用户' }[type] ?? '未知'
}

async function loadList() {
  loading.value = true
  try {
    const params = { pageNum: query.pageNum, pageSize: query.pageSize }
    if (query.status !== null && query.status !== '') params.status = query.status
    if (query.targetType !== null && query.targetType !== '')
      params.targetType = query.targetType

    const data = await getReportList(params)
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
  query.status = null
  query.targetType = null
  query.pageNum = 1
  loadList()
}

// ===== 证据 =====
const evidenceVisible = ref(false)
const evidence = ref(null)
function openEvidence(row) {
  evidence.value = row
  evidenceVisible.value = true
}

// ===== 处理 =====
const handleVisible = ref(false)
const handleLoading = ref(false)
const handleRow = ref({})
const handleForm = reactive({ handleType: 'reject', muteDays: null, handleResult: '' })

function openHandle(row) {
  handleRow.value = { ...row }
  handleForm.handleType = 'reject'
  handleForm.muteDays = null
  handleForm.handleResult = ''
  handleVisible.value = true
}

async function confirmHandle() {
  if (!handleForm.handleType) {
    ElMessage.warning('请选择处理动作')
    return
  }
  try {
    await ElMessageBox.confirm('确定提交该处理结果吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
  } catch (e) {
    return
  }
  handleLoading.value = true
  try {
    await handleReport({
      reportId: handleRow.value.reportId,
      handleType: handleForm.handleType,
      muteDays:
        handleForm.handleType === 'mute' ? handleForm.muteDays : undefined,
      handleResult: handleForm.handleResult || undefined
    })
    ElMessage.success('处理成功')
    handleVisible.value = false
    loadList()
  } catch (e) {
    // 拦截器已提示
  } finally {
    handleLoading.value = false
  }
}

onMounted(loadList)
</script>

<style scoped>
.evidence-content {
  white-space: pre-wrap;
  word-break: break-word;
  line-height: 1.6;
}

.evidence-media {
  display: flex;
  flex-wrap: wrap;
}
</style>
