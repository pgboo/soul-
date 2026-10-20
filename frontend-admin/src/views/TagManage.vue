<template>
  <div class="admin-page tag-page">
    <!-- 左侧分类树 -->
    <div class="admin-card category-panel">
      <div class="panel-header">
        <span class="panel-title">标签分类</span>
        <el-button link type="primary" :icon="Plus" @click="openCategoryDialog()">
          新增
        </el-button>
      </div>
      <el-menu
        :default-active="String(activeCategoryId)"
        class="category-menu"
        @select="onSelectCategory"
      >
        <el-menu-item :index="'0'">全部标签</el-menu-item>
        <el-menu-item
          v-for="cat in categories"
          :key="cat.categoryId"
          :index="String(cat.categoryId)"
        >
          <span class="menu-name">{{ cat.name }}</span>
          <el-icon class="edit-icon" @click.stop="openCategoryDialog(cat)">
            <Edit />
          </el-icon>
        </el-menu-item>
      </el-menu>
    </div>

    <!-- 右侧标签表格 -->
    <div class="admin-card tag-panel">
      <div class="table-toolbar">
        <span class="panel-title">
          {{ activeCategoryName }} · 标签列表
        </span>
        <el-button type="primary" :icon="Plus" @click="openTagDialog()">
          新增标签
        </el-button>
      </div>

      <el-table
        :data="tagList"
        v-loading="loading"
        border
        stripe
        empty-text="暂无标签数据"
      >
        <el-table-column prop="tagId" label="ID" width="80" />
        <el-table-column prop="name" label="标签名" min-width="130" />
        <el-table-column label="分类" width="120">
          <template #default="{ row }">{{ row.categoryName || '-' }}</template>
        </el-table-column>
        <el-table-column label="热门" width="80">
          <template #default="{ row }">
            <el-tag v-if="row.hotFlag === 1" type="danger" size="small">热</el-tag>
            <span v-else class="text-muted">-</span>
          </template>
        </el-table-column>
        <el-table-column label="用于匹配" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.matchFlag === 1" type="success" size="small" effect="plain">是</el-tag>
            <span v-else class="text-muted">否</span>
          </template>
        </el-table-column>
        <el-table-column label="用于推荐" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.recommendFlag === 1" type="primary" size="small" effect="plain">是</el-tag>
            <span v-else class="text-muted">否</span>
          </template>
        </el-table-column>
        <el-table-column prop="sort" label="排序" width="80" />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <el-tag v-if="row.status === 1" type="success" size="small">启用</el-tag>
            <el-tag v-else type="info" size="small">停用</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openTagDialog(row)">编辑</el-button>
            <el-button link type="danger" @click="onDeleteTag(row)">删除</el-button>
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
          @size-change="loadTags"
          @current-change="loadTags"
        />
      </div>
    </div>

    <!-- 分类弹窗 -->
    <el-dialog v-model="categoryDialogVisible" :title="categoryForm.categoryId ? '编辑分类' : '新增分类'" width="460px">
      <el-form :model="categoryForm" label-position="top">
        <el-form-item label="分类名称" required>
          <el-input v-model="categoryForm.name" placeholder="请输入分类名称" maxlength="20" />
        </el-form-item>
        <el-form-item label="图标">
          <el-input v-model="categoryForm.icon" placeholder="图标标识（可选）" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="categoryForm.sort" :min="0" :max="9999" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="categoryForm.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="categoryDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="saveCategory">保存</el-button>
      </template>
    </el-dialog>

    <!-- 标签弹窗 -->
    <el-dialog v-model="tagDialogVisible" :title="tagForm.tagId ? '编辑标签' : '新增标签'" width="480px">
      <el-form :model="tagForm" label-position="top">
        <el-form-item label="所属分类" required>
          <el-select v-model="tagForm.categoryId" placeholder="请选择分类" style="width: 100%">
            <el-option
              v-for="cat in categories"
              :key="cat.categoryId"
              :label="cat.name"
              :value="cat.categoryId"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="标签名称" required>
          <el-input v-model="tagForm.name" placeholder="请输入标签名称" maxlength="16" />
        </el-form-item>
        <el-form-item label="标签属性">
          <el-checkbox v-model="tagForm.hotFlag" :true-value="1" :false-value="0">热门标签</el-checkbox>
          <el-checkbox v-model="tagForm.matchFlag" :true-value="1" :false-value="0">用于匹配</el-checkbox>
          <el-checkbox v-model="tagForm.recommendFlag" :true-value="1" :false-value="0">用于推荐</el-checkbox>
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="tagForm.sort" :min="0" :max="9999" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="tagForm.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="tagDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="saveTagConfirm">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Edit } from '@element-plus/icons-vue'
import {
  getTagCategoryList,
  saveTagCategory,
  getTagList,
  saveTag,
  deleteTag
} from '@/api/tag'

/**
 * 标签管理
 * 左侧分类树 + 右侧标签表格；增删改查、热门标记、排序、启停
 */
const categories = ref([])
const activeCategoryId = ref(0)

const loading = ref(false)
const tagList = ref([])
const total = ref(0)
const saving = ref(false)

const query = reactive({ pageNum: 1, pageSize: 10 })

const activeCategoryName = computed(() => {
  if (!activeCategoryId.value) return '全部'
  const cat = categories.value.find((c) => c.categoryId === activeCategoryId.value)
  return cat ? cat.name : '全部'
})

async function loadCategories() {
  try {
    const data = await getTagCategoryList()
    categories.value = Array.isArray(data) ? data : data?.records || []
  } catch (e) {
    categories.value = []
  }
}

async function loadTags() {
  loading.value = true
  try {
    const params = { pageNum: query.pageNum, pageSize: query.pageSize }
    if (activeCategoryId.value) params.categoryId = activeCategoryId.value
    const data = await getTagList(params)
    tagList.value = data?.records || (Array.isArray(data) ? data : [])
    total.value = data?.total || 0
  } catch (e) {
    tagList.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

function onSelectCategory(index) {
  activeCategoryId.value = Number(index)
  query.pageNum = 1
  loadTags()
}

// ===== 分类 =====
const categoryDialogVisible = ref(false)
const categoryForm = reactive({
  categoryId: null,
  name: '',
  icon: '',
  sort: 0,
  status: 1
})

function openCategoryDialog(cat) {
  if (cat) {
    categoryForm.categoryId = cat.categoryId
    categoryForm.name = cat.name
    categoryForm.icon = cat.icon || ''
    categoryForm.sort = cat.sort ?? 0
    categoryForm.status = cat.status ?? 1
  } else {
    categoryForm.categoryId = null
    categoryForm.name = ''
    categoryForm.icon = ''
    categoryForm.sort = 0
    categoryForm.status = 1
  }
  categoryDialogVisible.value = true
}

async function saveCategory() {
  if (!categoryForm.name.trim()) {
    ElMessage.warning('请输入分类名称')
    return
  }
  saving.value = true
  try {
    await saveTagCategory({
      categoryId: categoryForm.categoryId || undefined,
      name: categoryForm.name.trim(),
      icon: categoryForm.icon,
      sort: categoryForm.sort,
      status: categoryForm.status
    })
    ElMessage.success('保存成功')
    categoryDialogVisible.value = false
    await loadCategories()
  } catch (e) {
    // 拦截器已提示
  } finally {
    saving.value = false
  }
}

// ===== 标签 =====
const tagDialogVisible = ref(false)
const tagForm = reactive({
  tagId: null,
  categoryId: null,
  name: '',
  hotFlag: 0,
  matchFlag: 0,
  recommendFlag: 0,
  sort: 0,
  status: 1
})

function openTagDialog(row) {
  if (row) {
    tagForm.tagId = row.tagId
    tagForm.categoryId = row.categoryId
    tagForm.name = row.name
    tagForm.hotFlag = row.hotFlag ?? 0
    tagForm.matchFlag = row.matchFlag ?? 0
    tagForm.recommendFlag = row.recommendFlag ?? 0
    tagForm.sort = row.sort ?? 0
    tagForm.status = row.status ?? 1
  } else {
    tagForm.tagId = null
    tagForm.categoryId = activeCategoryId.value || null
    tagForm.name = ''
    tagForm.hotFlag = 0
    tagForm.matchFlag = 0
    tagForm.recommendFlag = 0
    tagForm.sort = 0
    tagForm.status = 1
  }
  tagDialogVisible.value = true
}

async function saveTagConfirm() {
  if (!tagForm.categoryId) {
    ElMessage.warning('请选择所属分类')
    return
  }
  if (!tagForm.name.trim()) {
    ElMessage.warning('请输入标签名称')
    return
  }
  saving.value = true
  try {
    await saveTag({
      tagId: tagForm.tagId || undefined,
      categoryId: tagForm.categoryId,
      name: tagForm.name.trim(),
      hotFlag: tagForm.hotFlag,
      matchFlag: tagForm.matchFlag,
      recommendFlag: tagForm.recommendFlag,
      sort: tagForm.sort,
      status: tagForm.status
    })
    ElMessage.success('保存成功')
    tagDialogVisible.value = false
    loadTags()
  } catch (e) {
    // 拦截器已提示
  } finally {
    saving.value = false
  }
}

async function onDeleteTag(row) {
  try {
    await ElMessageBox.confirm(`确定删除标签「${row.name}」吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteTag({ tagId: row.tagId })
    ElMessage.success('删除成功')
    loadTags()
  } catch (e) {
    // 取消或失败
  }
}

onMounted(async () => {
  await loadCategories()
  await loadTags()
})
</script>

<style scoped>
.tag-page {
  display: flex;
  gap: 16px;
  align-items: flex-start;
}

.category-panel {
  width: 260px;
  flex-shrink: 0;
}

.tag-panel {
  flex: 1;
  min-width: 0;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.panel-title {
  font-size: 15px;
  font-weight: 600;
  color: #1f2d3d;
}

.category-menu {
  border-right: none;
}

.menu-name {
  flex: 1;
}

.edit-icon {
  opacity: 0;
  color: #909399;
  transition: opacity 0.2s;
}

.category-menu .el-menu-item:hover .edit-icon {
  opacity: 1;
}
</style>
