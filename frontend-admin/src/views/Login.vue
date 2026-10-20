<template>
  <div class="login-page">
    <div class="login-card">
      <div class="login-header">
        <div class="login-logo">S</div>
        <h1 class="login-title">Soul 社交 · 后台管理</h1>
        <p class="login-subtitle">请输入管理员账号登录系统</p>
      </div>

      <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-position="top"
        @keyup.enter="onSubmit"
      >
        <el-form-item label="账号" prop="account">
          <el-input
            v-model="form.account"
            placeholder="请输入管理员账号"
            size="large"
            clearable
            :prefix-icon="User"
          />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="请输入密码"
            size="large"
            show-password
            :prefix-icon="Lock"
          />
        </el-form-item>
        <el-form-item>
          <el-button
            type="primary"
            size="large"
            class="login-btn"
            :loading="loading"
            @click="onSubmit"
          >
            登 录
          </el-button>
        </el-form-item>
      </el-form>

      <p class="login-tip text-muted">默认账号 admin / 密码 123456</p>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { useAdminStore } from '@/stores/admin'

/**
 * 后台登录页
 * 账号密码登录，默认 admin / 123456
 */
const router = useRouter()
const route = useRoute()
const adminStore = useAdminStore()

const formRef = ref(null)
const loading = ref(false)

const form = reactive({
  account: 'admin',
  password: '123456'
})

const rules = {
  account: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

async function onSubmit() {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    loading.value = true
    try {
      await adminStore.login({ account: form.account, password: form.password })
      ElMessage.success('登录成功')
      const redirect = route.query.redirect || '/dashboard'
      router.replace(redirect)
    } catch (e) {
      // 错误提示已由响应拦截器统一处理
    } finally {
      loading.value = false
    }
  })
}
</script>

<style scoped>
.login-page {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100vh;
  background: linear-gradient(135deg, #1f2d3d 0%, #2b6cb0 100%);
}

.login-card {
  width: 400px;
  padding: 40px 36px 28px;
  background: #fff;
  border-radius: 10px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.18);
}

.login-header {
  text-align: center;
  margin-bottom: 24px;
}

.login-logo {
  width: 56px;
  height: 56px;
  margin: 0 auto 12px;
  border-radius: 14px;
  background: var(--admin-primary);
  color: #fff;
  font-size: 30px;
  font-weight: 700;
  line-height: 56px;
}

.login-title {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
  color: #1f2d3d;
}

.login-subtitle {
  margin: 8px 0 0;
  font-size: 13px;
  color: #909399;
}

.login-btn {
  width: 100%;
  letter-spacing: 4px;
}

.login-tip {
  margin: 0;
  text-align: center;
  font-size: 12px;
}
</style>
