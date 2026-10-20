<template>
  <div class="admin-page">
    <!-- 搜索栏 -->
    <div class="admin-card filter-bar">
      <el-input
        v-model="query.keyword"
        placeholder="内容关键字 / 作者昵称 / 动态ID"
        clearable
        :prefix-icon="Search"
        @keyup.enter="onSearch"
        @clear="onSearch"
      />
      <el-select v-model="query.status" placeholder="内容状态" clearable>
        <el-option label="全部" :value="null" />
        <el-option label="正常" :value="1" />
        <el-option label="已下架" :value="2" />
      </el-select>
      <el-select v-model="query.auditStatus" placeholder="审核状态" clearable>
        <el-option label="全部" :value="null" />
        <el-option label="已过审" :value="0" />
        <el-option label="待审核" :value="1" />
        <el-option label="已拒绝" :value="2" />
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
        empty-text="暂无瞬间数据"
      >
        <el-table-column prop="dynamicId" label="ID" width="90" />
        <el-table-column label="作者" min-width="130">
          <template #default="{ row }">
            {{ row.nickname || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="内容" min-width="220">
          <template #default="{ row }">
            <span class="content-cell">{{ row.content || '(无文字)' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="图片" width="90">
          <template #default="{ row }">
            <el-image
              v-if="row.mediaUrls && row.mediaUrls.length"
              :src="row.mediaUrls[0]"
              :preview-src-list="row.mediaUrls"
              fit="cover"
              style="width: 44px; height: 44px; border-radius: 4px"
              preview-teleported
            />
            <span v-else class="text-muted">-</span>
          </template>
        </el-table-column>
        <el-table-column label="互动" width="180">
          <template #default="{ row }">
            <span class="text-muted">
              赞{{ row.likeCount ?? 0 }} 评{{ row.commentCount ?? 0 }} 藏{{ row.collectCount ?? 0 }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="举报数" width="90">
          <template #default="{ row }">
            <el-tag v-if="row.reportCount > 0" type="danger" size="small" effect="plain">
              {{ row.reportCount }}
            </el-tag>
            <span v-else class="text-muted">0</span>
          </template>
        </el-table-column>
        <el-table-column label="审核状态" width="110">
          <template #default="{ row }">
            <el-tag :type="auditTagType(row.auditStatus)" size="small">
              {{ auditText(row.auditStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="内容状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusTagType(row.status)" size="small" effect="plain">
              {{ statusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="发布时间" min-width="170">
          <template #default="{ row }">{{ row.createTime || '-' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="270" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDetail(row)">详情</el-button>
            <template v-if="row.auditStatus === 1">
              <el-button link type="success" @click="openAudit(row, true)">通过</el-button>
              <el-button link type="danger" @click="openAudit(row, false)">拒绝</el-button>
            </template>
            <el-button
              v-if="row.status === 1"
              link
              type="danger"
              @click="openOffline(row)"
            >
              下架
            </el-button>
            <el-button
              v-else-if="row.status === 2"
              link
              type="success"
              @click="onRestore(row)"
            >
              恢复
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

    <!-- 详情弹窗 -->
    <el-dialog v-model="detailVisible" title="瞬间详情" width="560px">
      <div v-if="detail" class="dynamic-detail">
        <div class="detail-author">
          <strong>{{ detail.author?.nickname || detail.nickname || '-' }}</strong>
          <span class="text-muted">ID: {{ detail.dynamicId }}</span>
        </div>
        <p class="detail-content">{{ detail.content || '(无文字)' }}</p>
        <div v-if="detail.mediaUrls && detail.mediaUrls.length" class="detail-media">
          <el-image
            v-for="(url, i) in detail.mediaUrls"
            :key="i"
            :src="url"
            :preview-src-list="detail.mediaUrls"
            fit="cover"
            style="width: 100px; height: 100px; border-radius: 4px; margin: 0 8px 8px 0"
            preview-teleported
          />
        </div>
        <div class="detail-meta text-muted">
          <span>可见范围：{{ scopeText(detail.visibleScope) }}</span>
          <span class="meta-gap">匿名：{{ detail.anonymousFlag === 1 ? '是' : '否' }}</span>
          <span class="meta-gap">发布时间：{{ detail.createTime || '-' }}</span>
        </div>
      </div>
      <el-empty v-else description="暂无详情" />
    </el-dialog>

    <!-- 审核 / 下架 弹窗 -->
    <el-dialog v-model="opVisible" :title="opTitle" width="480px">
      <el-form label-position="top">
        <el-form-item label="操作原因（可选）">
          <el-input
            v-model="opForm.reason"
            type="textarea"
            :rows="3"
            maxlength="200"
            show-word-limit
            placeholder="请填写操作原因"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="opVisible = false">取消</el-button>
        <el-button type="primary" :loading="opLoading" @click="confirmOp">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, RefreshLeft } from '@element-plus/icons-vue'
import {
  getDynamicList,
  auditDynamic,
  offlineDynamic,
  restoreDynamic
} from '@/api/dynamic'

/**
 * 瞬间管理
 * 列表（状态 / 审核状态筛选）+ 详情 + 通过 / 拒绝 / 下架 / 恢复
 * 字段口径以后端 DTO 为准：status(1正常/2下架)、auditStatus(0已过审/1待审核/2拒绝)
 */
const loading = ref(false)
const list = ref([])
const total = ref(0)

const query = reactive({
  keyword: '',
  status: null,
  auditStatus: null,
  pageNum: 1,
  pageSize: 10
})

function auditText(s) {
  return { 0: '已过审', 1: '待审核', 2: '已拒绝' }[s] ?? '未知'
}
function auditTagType(s) {
  return { 0: 'success', 1: 'warning', 2: 'danger' }[s] ?? 'info'
}
function statusText(s) {
  return { 1: '正常', 2: '已下架' }[s] ?? '未知'
}
function statusTagType(s) {
  return { 1: 'success', 2: 'danger' }[s] ?? 'info'
}
function scopeText(scope) {
  return { 1: '公开', 2: '仅好友', 3: '仅自己' }[scope] ?? '-'
}

async function loadList() {
  loading.value = true
  try {
    const params = { pageNum: query.pageNum, pageSize: query.pageSize }
    if (query.keyword) params.keyword = query.keyword
    if (query.status !== null && query.status !== '') params.status = query.status
    if (query.auditStatus !== null && query.auditStatus !== '')
      params.auditStatus = query.auditStatus

    const data = await getDynamicList(params)
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
  query.keyword = ''
  query.status = null
  query.auditStatus = null
  query.pageNum = 1
  loadList()
}

// ===== 详情 =====
const detailVisible = ref(false)
const detail = ref(null)
function openDetail(row) {
  detail.value = row
  detailVisible.value = true
}

// ===== 审核 / 下架 =====
const opVisible = ref(false)
const opLoading = ref(false)
const opType = ref('audit') // audit | offline
const opRow = ref({})
const opPass = ref(true) // true=通过 / false=拒绝
const opForm = reactive({ reason: '' })

const opTitle = computed(() => {
  if (opType.value === 'audit') {
    return opPass.value ? '审核通过' : '审核拒绝'
  }
  return '下架瞬间'
})

function openAudit(row, pass) {
  opType.value = 'audit'
  opRow.value = { ...row }
  opPass.value = pass
  opForm.reason = ''
  opVisible.value = true
}

function openOffline(row) {
  opType.value = 'offline'
  opRow.value = { ...row }
  opForm.reason = ''
  opVisible.value = true
}

async function confirmOp() {
  opLoading.value = true
  try {
    if (opType.value === 'audit') {
      await auditDynamic({
        dynamicId: opRow.value.dynamicId,
        pass: opPass.value,
        reason: opForm.reason || undefined
      })
      ElMessage.success(opPass.value ? '审核通过' : '已拒绝')
    } else {
      await offlineDynamic({
        dynamicId: opRow.value.dynamicId,
        reason: opForm.reason || undefined
      })
      ElMessage.success('已下架')
    }
    opVisible.value = false
    loadList()
  } catch (e) {
    // 拦截器已提示
  } finally {
    opLoading.value = false
  }
}

async function onRestore(row) {
  try {
    await ElMessageBox.confirm('确定要恢复该瞬间吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await restoreDynamic({ dynamicId: row.dynamicId })
    ElMessage.success('已恢复')
    loadList()
  } catch (e) {
    // 取消或失败
  }
}

onMounted(loadList)
</script>

<style scoped>
.content-cell {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  line-height: 1.4;
}

.dynamic-detail {
  font-size: 14px;
}

.detail-author {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10px;
}

.detail-content {
  margin: 0 0 12px;
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-word;
}

.detail-media {
  margin-bottom: 12px;
}

.detail-meta {
  display: flex;
  gap: 16px;
  font-size: 13px;
}
</style>
