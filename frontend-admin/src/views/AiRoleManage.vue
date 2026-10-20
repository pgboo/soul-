<template>
  <div class="admin-page">
    <!-- 搜索栏 -->
    <div class="admin-card filter-bar">
      <el-input
        v-model="query.keyword"
        placeholder="角色名称"
        clearable
        :prefix-icon="Search"
        @keyup.enter="onSearch"
        @clear="onSearch"
      />
      <el-select v-model="query.status" placeholder="状态" clearable>
        <el-option label="全部" :value="null" />
        <el-option label="启用" :value="1" />
        <el-option label="禁用" :value="0" />
      </el-select>
      <el-button type="primary" :icon="Search" @click="onSearch">搜索</el-button>
      <el-button :icon="RefreshLeft" @click="onReset">重置</el-button>
      <el-button type="primary" :icon="Plus" @click="openDialog()">新增角色</el-button>
    </div>

    <div class="admin-card">
      <el-table
        :data="list"
        v-loading="loading"
        border
        stripe
        empty-text="暂无 AI 角色数据"
      >
        <el-table-column prop="roleId" label="ID" width="80" />
        <el-table-column label="头像" width="80">
          <template #default="{ row }">
            <el-avatar v-if="row.avatar" :size="40" :src="row.avatar" />
            <el-avatar v-else :size="40">{{ (row.name || 'A').charAt(0) }}</el-avatar>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="名称" min-width="130" />
        <el-table-column prop="welcomeText" label="欢迎语" min-width="200" show-overflow-tooltip />
        <el-table-column label="Prompt" min-width="200">
          <template #default="{ row }">
            <el-text truncated>{{ row.prompt || '-' }}</el-text>
          </template>
        </el-table-column>
        <el-table-column prop="sort" label="排序" width="80" />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag v-if="row.status === 1" type="success" size="small">启用</el-tag>
            <el-tag v-else type="info" size="small">禁用</el-tag>
          </template>
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
          :page-sizes="[10, 20, 50]"
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
      :title="form.roleId ? '编辑 AI 角色' : '新增 AI 角色'"
      width="620px"
      top="6vh"
    >
      <el-form :model="form" label-position="top">
        <el-form-item label="角色名称" required>
          <el-input v-model="form.name" placeholder="如 小灵" maxlength="20" />
        </el-form-item>
        <el-form-item label="头像 URL">
          <el-input v-model="form.avatar" placeholder="头像图片地址" />
        </el-form-item>
        <el-form-item label="欢迎语">
          <el-input
            v-model="form.welcomeText"
            type="textarea"
            :rows="2"
            maxlength="200"
            show-word-limit
            placeholder="初次进入聊天时展示的欢迎语"
          />
        </el-form-item>
        <el-form-item label="Prompt 人设">
          <el-input
            v-model="form.prompt"
            type="textarea"
            :rows="8"
            maxlength="2000"
            show-word-limit
            placeholder="定义 AI 角色的人设与回复风格"
          />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sort" :min="0" :max="9999" />
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
import { getAiRoleList, saveAiRole, deleteAiRole } from '@/api/aiRole'

/**
 * AI 角色管理
 * 角色列表 + 表单（名称/头像/欢迎语/Prompt 编辑器/排序/状态）
 * 字段口径以后端 DTO 为准：name, welcomeText, status(0禁用/1启用)
 */
const loading = ref(false)
const saving = ref(false)
const list = ref([])
const total = ref(0)
const query = reactive({ keyword: '', status: null, pageNum: 1, pageSize: 10 })

async function loadList() {
  loading.value = true
  try {
    const params = { pageNum: query.pageNum, pageSize: query.pageSize }
    if (query.keyword) params.keyword = query.keyword
    if (query.status !== null && query.status !== '') params.status = query.status
    const data = await getAiRoleList(params)
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
  query.status = null
  query.pageNum = 1
  loadList()
}

const dialogVisible = ref(false)
const form = reactive({
  roleId: null,
  name: '',
  avatar: '',
  welcomeText: '',
  prompt: '',
  status: 1,
  sort: 0
})

function openDialog(row) {
  if (row) {
    form.roleId = row.roleId
    form.name = row.name
    form.avatar = row.avatar || ''
    form.welcomeText = row.welcomeText || ''
    form.prompt = row.prompt || ''
    form.status = row.status ?? 1
    form.sort = row.sort ?? 0
  } else {
    form.roleId = null
    form.name = ''
    form.avatar = ''
    form.welcomeText = ''
    form.prompt = ''
    form.status = 1
    form.sort = 0
  }
  dialogVisible.value = true
}

async function onSave() {
  if (!form.name.trim()) {
    ElMessage.warning('请输入角色名称')
    return
  }
  saving.value = true
  try {
    await saveAiRole({
      roleId: form.roleId || undefined,
      name: form.name.trim(),
      avatar: form.avatar,
      welcomeText: form.welcomeText,
      prompt: form.prompt,
      status: form.status,
      sort: form.sort
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
    await ElMessageBox.confirm(`确定删除 AI 角色「${row.name}」吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteAiRole({ roleId: row.roleId })
    ElMessage.success('删除成功')
    loadList()
  } catch (e) {
    // 取消或失败
  }
}

onMounted(loadList)
</script>

<style scoped></style>
