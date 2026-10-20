<template>
  <div class="admin-page">
    <el-tabs v-model="activeTab" class="soul-tabs">
      <!-- 题目列表 -->
      <el-tab-pane label="测试题目" name="question">
        <div class="table-toolbar">
          <span class="panel-title">题目列表（含选项）</span>
          <el-button type="primary" :icon="Plus" @click="openQuestionDialog()">
            新增题目
          </el-button>
        </div>

        <el-table
          :data="questionList"
          v-loading="loading"
          border
          stripe
          empty-text="暂无题目数据"
        >
          <el-table-column prop="questionId" label="ID" width="80" />
          <el-table-column prop="title" label="题目" min-width="200" />
          <el-table-column label="选项数" width="100">
            <template #default="{ row }">
              {{ (row.options && row.options.length) || 0 }}
            </template>
          </el-table-column>
          <el-table-column prop="sort" label="排序" width="80" />
          <el-table-column label="状态" width="90">
            <template #default="{ row }">
              <el-tag v-if="row.status === 1" type="success" size="small">启用</el-tag>
              <el-tag v-else type="info" size="small">停用</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="120" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" @click="openQuestionDialog(row)">编辑</el-button>
            </template>
          </el-table-column>
        </el-table>

        <div class="pagination-wrap">
          <el-pagination
            v-model:current-page="query.pageNum"
            v-model:page-size="query.pageSize"
            :total="total"
            layout="total, prev, pager, next"
            background
            @current-change="loadQuestions"
          />
        </div>
      </el-tab-pane>

      <!-- 结果类型 -->
      <el-tab-pane label="结果类型" name="result">
        <div class="table-toolbar">
          <span class="panel-title">灵魂测试结果类型</span>
          <el-button type="primary" :icon="Plus" @click="openResultDialog()">
            新增类型
          </el-button>
        </div>

        <el-table
          :data="resultList"
          v-loading="resultLoading"
          border
          stripe
          empty-text="暂无结果类型"
        >
          <el-table-column prop="resultId" label="ID" width="80" />
          <el-table-column prop="resultCode" label="编码" width="100" />
          <el-table-column prop="name" label="名称" min-width="140" />
          <el-table-column prop="description" label="描述" min-width="220" show-overflow-tooltip />
          <el-table-column prop="socialAdvice" label="社交建议" min-width="220" show-overflow-tooltip />
          <el-table-column label="操作" width="120" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" @click="openResultDialog(row)">编辑</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
    </el-tabs>

    <!-- 题目编辑弹窗 -->
    <el-dialog
      v-model="questionDialogVisible"
      :title="questionForm.questionId ? '编辑题目' : '新增题目'"
      width="640px"
      top="6vh"
    >
      <el-form :model="questionForm" label-position="top">
        <el-form-item label="题目标题" required>
          <el-input v-model="questionForm.title" placeholder="请输入题目标题" maxlength="100" />
        </el-form-item>
        <el-form-item label="题目描述">
          <el-input v-model="questionForm.description" placeholder="可选" maxlength="200" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="questionForm.sort" :min="0" :max="9999" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="questionForm.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-divider content-position="left">选项配置</el-divider>
        <div v-for="(opt, index) in questionForm.options" :key="index" class="option-row">
          <el-input v-model="opt.content" placeholder="选项内容" class="opt-content" />
          <el-input v-model="opt.resultCode" placeholder="结果编码" class="opt-code" />
          <el-input-number v-model="opt.score" :min="0" :max="100" class="opt-score" />
          <el-button
            type="danger"
            :icon="Delete"
            circle
            @click="removeOption(index)"
          />
        </div>
        <el-button type="primary" plain :icon="Plus" @click="addOption">
          添加选项
        </el-button>
      </el-form>
      <template #footer>
        <el-button @click="questionDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="saveQuestionConfirm">保存</el-button>
      </template>
    </el-dialog>

    <!-- 结果类型编辑弹窗 -->
    <el-dialog
      v-model="resultDialogVisible"
      :title="resultForm.resultId ? '编辑结果类型' : '新增结果类型'"
      width="560px"
    >
      <el-form :model="resultForm" label-position="top">
        <el-form-item label="结果编码" required>
          <el-input v-model="resultForm.resultCode" placeholder="如 S / E / I" maxlength="10" />
        </el-form-item>
        <el-form-item label="名称" required>
          <el-input v-model="resultForm.name" placeholder="如 温柔倾听者" maxlength="30" />
        </el-form-item>
        <el-form-item label="图标">
          <el-input v-model="resultForm.icon" placeholder="图标标识（可选）" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="resultForm.description" type="textarea" :rows="3" maxlength="300" />
        </el-form-item>
        <el-form-item label="社交建议">
          <el-input v-model="resultForm.socialAdvice" type="textarea" :rows="3" maxlength="300" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="resultDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="saveResultConfirm">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus, Delete } from '@element-plus/icons-vue'
import {
  getQuestionList,
  saveQuestion,
  getResultList,
  saveResult
} from '@/api/soulTest'

/**
 * 灵魂测试管理
 * 题目列表（含选项编辑）+ 结果类型配置；启用/禁用
 */
const activeTab = ref('question')
const saving = ref(false)

// ===== 题目 =====
const loading = ref(false)
const questionList = ref([])
const total = ref(0)
const query = reactive({ pageNum: 1, pageSize: 10 })

async function loadQuestions() {
  loading.value = true
  try {
    const data = await getQuestionList({
      pageNum: query.pageNum,
      pageSize: query.pageSize
    })
    questionList.value = data?.records || (Array.isArray(data) ? data : [])
    total.value = data?.total || 0
  } catch (e) {
    questionList.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

const questionDialogVisible = ref(false)
const questionForm = reactive({
  questionId: null,
  title: '',
  description: '',
  sort: 0,
  status: 1,
  options: []
})

function emptyOption() {
  return { optionId: null, content: '', resultCode: '', score: 1, sort: 1 }
}

function openQuestionDialog(row) {
  if (row) {
    questionForm.questionId = row.questionId
    questionForm.title = row.title
    questionForm.description = row.description || ''
    questionForm.sort = row.sort ?? 0
    questionForm.status = row.status ?? 1
    questionForm.options = (row.options || []).map((o) => ({
      optionId: o.optionId ?? null,
      content: o.content || '',
      resultCode: o.resultCode || '',
      score: o.score ?? 1,
      sort: o.sort ?? 1
    }))
    if (!questionForm.options.length) {
      questionForm.options = [emptyOption()]
    }
  } else {
    questionForm.questionId = null
    questionForm.title = ''
    questionForm.description = ''
    questionForm.sort = 0
    questionForm.status = 1
    questionForm.options = [emptyOption()]
  }
  questionDialogVisible.value = true
}

function addOption() {
  questionForm.options.push({
    ...emptyOption(),
    sort: questionForm.options.length + 1
  })
}

function removeOption(index) {
  if (questionForm.options.length <= 1) {
    ElMessage.warning('至少保留一个选项')
    return
  }
  questionForm.options.splice(index, 1)
}

async function saveQuestionConfirm() {
  if (!questionForm.title.trim()) {
    ElMessage.warning('请输入题目标题')
    return
  }
  const invalid = questionForm.options.some((o) => !o.content.trim())
  if (invalid) {
    ElMessage.warning('选项内容不能为空')
    return
  }
  saving.value = true
  try {
    await saveQuestion({
      questionId: questionForm.questionId || undefined,
      title: questionForm.title.trim(),
      description: questionForm.description,
      sort: questionForm.sort,
      status: questionForm.status,
      options: questionForm.options.map((o, i) => ({
        optionId: o.optionId || undefined,
        content: o.content.trim(),
        resultCode: o.resultCode,
        score: o.score,
        sort: o.sort || i + 1
      }))
    })
    ElMessage.success('保存成功')
    questionDialogVisible.value = false
    loadQuestions()
  } catch (e) {
    // 拦截器已提示
  } finally {
    saving.value = false
  }
}

// ===== 结果类型 =====
const resultLoading = ref(false)
const resultList = ref([])
const resultDialogVisible = ref(false)
const resultForm = reactive({
  resultId: null,
  resultCode: '',
  name: '',
  icon: '',
  description: '',
  socialAdvice: ''
})

async function loadResults() {
  resultLoading.value = true
  try {
    const data = await getResultList()
    resultList.value = Array.isArray(data) ? data : data?.records || []
  } catch (e) {
    resultList.value = []
  } finally {
    resultLoading.value = false
  }
}

function openResultDialog(row) {
  if (row) {
    resultForm.resultId = row.resultId
    resultForm.resultCode = row.resultCode
    resultForm.name = row.name
    resultForm.icon = row.icon || ''
    resultForm.description = row.description || ''
    resultForm.socialAdvice = row.socialAdvice || ''
  } else {
    resultForm.resultId = null
    resultForm.resultCode = ''
    resultForm.name = ''
    resultForm.icon = ''
    resultForm.description = ''
    resultForm.socialAdvice = ''
  }
  resultDialogVisible.value = true
}

async function saveResultConfirm() {
  if (!resultForm.resultCode.trim()) {
    ElMessage.warning('请输入结果编码')
    return
  }
  if (!resultForm.name.trim()) {
    ElMessage.warning('请输入名称')
    return
  }
  saving.value = true
  try {
    await saveResult({
      resultId: resultForm.resultId || undefined,
      resultCode: resultForm.resultCode.trim(),
      name: resultForm.name.trim(),
      icon: resultForm.icon,
      description: resultForm.description,
      socialAdvice: resultForm.socialAdvice
    })
    ElMessage.success('保存成功')
    resultDialogVisible.value = false
    loadResults()
  } catch (e) {
    // 拦截器已提示
  } finally {
    saving.value = false
  }
}

onMounted(() => {
  loadQuestions()
  loadResults()
})
</script>

<style scoped>
.panel-title {
  font-size: 15px;
  font-weight: 600;
  color: #1f2d3d;
}

.option-row {
  display: flex;
  gap: 10px;
  align-items: center;
  margin-bottom: 10px;
}

.opt-content {
  flex: 1;
}

.opt-code {
  width: 120px;
}

.opt-score {
  width: 120px;
  flex-shrink: 0;
}
</style>
