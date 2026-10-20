<template>
  <div class="admin-page">
    <!-- 搜索栏 -->
    <div class="admin-card filter-bar">
      <el-input
        v-model="query.keyword"
        placeholder="敏感词关键字"
        clearable
        :prefix-icon="Search"
        @keyup.enter="onSearch"
        @clear="onSearch"
      />
      <el-select v-model="query.level" placeholder="处理方式" clearable>
        <el-option label="全部" :value="null" />
        <el-option label="替换" :value="1" />
        <el-option label="拦截" :value="2" />
      </el-select>
      <el-select v-model="query.status" placeholder="状态" clearable>
        <el-option label="全部" :value="null" />
        <el-option label="启用" :value="1" />
        <el-option label="禁用" :value="0" />
      </el-select>
      <el-button type="primary" :icon="Search" @click="onSearch">搜索</el-button>
      <el-button :icon="RefreshLeft" @click="onReset">重置</el-button>
      <el-button type="primary" :icon="Plus" @click="openDialog()">新增敏感词</el-button>
    </div>

    <!-- 表格 -->
    <div class="admin-card">
      <el-table
        :data="list"
        v-loading="loading"
        border
        stripe
        empty-text="暂无敏感词数据"
      >
        <el-table-column prop="wordId" label="ID" width="90" />
        <el-table-column prop="word" label="敏感词" min-width="180" />
        <el-table-column prop="category" label="分类" width="140">
          <template #default="{ row }">{{ row.category || '-' }}</template>
        </el-table-column>
        <el-table-column label="处理方式" width="120">
          <template #default="{ row }">
            <el-tag v-if="row.level === 1" type="warning" size="small">替换</el-tag>
            <el-tag v-else type="danger" size="small">拦截</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.status === 1" type="success" size="small">启用</el-tag>
            <el-tag v-else type="info" size="small">禁用</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" min-width="170">
          <template #default="{ row }">{{ row.createTime || '-' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDialog(row)">编辑</el-button>
            <el-button link type="danger" @click="onDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="query.pageNum"
          v-model:page-size="query.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next"
          background
          @size-change="loadList"
          @current-change="loadList"
        />
      </div>
    </div>

    <!-- 编辑弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="form.wordId ? '编辑敏感词' : '新增敏感词'"
      width="460px"
    >
      <el-form :model="form" label-position="top">
        <el-form-item label="敏感词" required>
          <el-input v-model="form.word" placeholder="请输入敏感词" maxlength="50" />
        </el-form-item>
        <el-form-item label="分类">
          <el-input v-model="form.category" placeholder="如 涉政 / 广告 / 辱骂（可选）" maxlength="30" />
        </el-form-item>
        <el-form-item label="处理方式">
          <el-select v-model="form.level" placeholder="请选择处理方式" style="width: 100%">
            <el-option label="替换（命中后替换为 *）" :value="1" />
            <el-option label="拦截（命中后禁止提交）" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="onSave">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, RefreshLeft, Plus } from '@element-plus/icons-vue'
import {
  getSensitiveWordList,
  saveSensitiveWord,
  deleteSensitiveWord
} from '@/api/sensitiveWord'

/**
 * 敏感词管理
 * 词库表格 + 增删改查
 * 字段口径以后端 DTO 为准：level(1替换/2拦截)、status(0禁用/1启用)
 */
const loading = ref(false)
const saving = ref(false)
const list = ref([])
const total = ref(0)

const query = reactive({
  keyword: '',
  level: null,
  status: null,
  pageNum: 1,
  pageSize: 10
})

async function loadList() {
  loading.value = true
  try {
    const params = { pageNum: query.pageNum, pageSize: query.pageSize }
    if (query.keyword) params.keyword = query.keyword
    if (query.level !== null && query.level !== '') params.level = query.level
    if (query.status !== null && query.status !== '') params.status = query.status
    const data = await getSensitiveWordList(params)
    list.value = data?.records || (Array.isArray(data) ? data : [])
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
  query.level = null
  query.status = null
  query.pageNum = 1
  loadList()
}

// ===== 编辑 =====
const dialogVisible = ref(false)
const form = reactive({
  wordId: null,
  word: '',
  category: '',
  level: 2,
  status: 1
})

function openDialog(row) {
  if (row) {
    form.wordId = row.wordId
    form.word = row.word
    form.category = row.category || ''
    form.level = row.level ?? 2
    form.status = row.status ?? 1
  } else {
    form.wordId = null
    form.word = ''
    form.category = ''
    form.level = 2
    form.status = 1
  }
  dialogVisible.value = true
}

async function onSave() {
  if (!form.word.trim()) {
    ElMessage.warning('请输入敏感词')
    return
  }
  saving.value = true
  try {
    await saveSensitiveWord({
      wordId: form.wordId || undefined,
      word: form.word.trim(),
      category: form.category || undefined,
      level: form.level,
      status: form.status
    })
    ElMessage.success('保存成功')
    dialogVisible.value = false
    loadList()
  } catch (e) {
    // 拦截器已提示
  } finally {
    saving.value = false
  }
}

async function onDelete(row) {
  try {
    await ElMessageBox.confirm(`确定删除敏感词「${row.word}」吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteSensitiveWord({ wordId: row.wordId })
    ElMessage.success('删除成功')
    loadList()
  } catch (e) {
    // 取消或失败
  }
}

onMounted(loadList)
</script>

<style scoped></style>
