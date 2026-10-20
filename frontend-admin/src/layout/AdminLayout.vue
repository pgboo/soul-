<template>
  <el-container class="admin-layout">
    <!-- 顶部 Header -->
    <el-header class="admin-header">
      <div class="header-left">
        <el-icon class="collapse-btn" @click="toggleCollapse">
          <Fold v-if="!collapsed" />
          <Expand v-else />
        </el-icon>
        <span class="logo-text">Soul 社交 · 后台管理</span>
      </div>
      <div class="header-right">
        <el-dropdown @command="onCommand">
          <span class="user-info">
            <el-avatar :size="28" class="user-avatar">
              {{ adminStore.displayName.charAt(0) }}
            </el-avatar>
            <span class="user-name">{{ adminStore.displayName }}</span>
            <el-icon><ArrowDown /></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </el-header>

    <el-container class="admin-body">
      <!-- 左侧菜单 -->
      <el-aside
        class="admin-aside"
        :width="collapsed ? '64px' : '210px'"
      >
        <el-menu
          :default-active="activeMenu"
          :collapse="collapsed"
          :collapse-transition="false"
          router
          class="admin-menu"
        >
          <el-menu-item
            v-for="item in menuItems"
            :key="item.path"
            :index="item.path"
          >
            <el-icon><component :is="item.icon" /></el-icon>
            <template #title>{{ item.title }}</template>
          </el-menu-item>
        </el-menu>
      </el-aside>

      <!-- 右侧内容区 -->
      <el-main class="admin-main">
        <el-breadcrumb separator="/" class="admin-breadcrumb">
          <el-breadcrumb-item>首页</el-breadcrumb-item>
          <el-breadcrumb-item>{{ currentTitle }}</el-breadcrumb-item>
        </el-breadcrumb>
        <router-view v-slot="{ Component }">
          <keep-alive :max="8">
            <component :is="Component" />
          </keep-alive>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { useAdminStore } from '@/stores/admin'

/**
 * 后台三段式布局：顶部 Header + 左侧菜单 + 右侧内容区
 * 侧边栏可折叠
 */
const route = useRoute()
const router = useRouter()
const adminStore = useAdminStore()

const collapsed = ref(false)

/** 从路由表提取的一级菜单项 */
const menuItems = [
  { path: '/dashboard', title: '数据看板', icon: 'DataLine' },
  { path: '/users', title: '用户管理', icon: 'User' },
  { path: '/tags', title: '标签管理', icon: 'CollectionTag' },
  { path: '/soul-test', title: '灵魂测试', icon: 'MagicStick' },
  { path: '/dynamics', title: '瞬间管理', icon: 'Picture' },
  { path: '/reports', title: '举报管理', icon: 'Warning' },
  { path: '/sensitive-words', title: '敏感词管理', icon: 'Filter' },
  { path: '/ai-roles', title: 'AI 角色管理', icon: 'ChatDotRound' }
]

const activeMenu = computed(() => route.path)

const currentTitle = computed(() => route.meta?.title || '')

function toggleCollapse() {
  collapsed.value = !collapsed.value
}

/** 退出登录二次确认 */
function onCommand(command) {
  if (command === 'logout') {
    ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
      .then(() => {
        adminStore.logout()
        router.replace('/login')
      })
      .catch(() => {})
  }
}

onMounted(() => {
  // 从 localStorage 恢复管理员昵称
  adminStore.restore()
})
</script>

<style scoped>
.admin-layout {
  height: 100vh;
}

.admin-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 56px;
  padding: 0 20px;
  background: var(--admin-header-bg);
  color: #fff;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 14px;
}

.collapse-btn {
  font-size: 20px;
  cursor: pointer;
  color: #c0c4cc;
  transition: color 0.2s;
}

.collapse-btn:hover {
  color: #fff;
}

.logo-text {
  font-size: 16px;
  font-weight: 600;
  letter-spacing: 0.5px;
}

.header-right {
  display: flex;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  color: #e5e7eb;
  outline: none;
}

.user-avatar {
  background: var(--admin-primary);
  color: #fff;
  font-size: 13px;
}

.user-name {
  font-size: 14px;
}

.admin-body {
  height: calc(100vh - 56px);
}

.admin-aside {
  background: var(--admin-sidebar-bg);
  border-right: 1px solid var(--admin-border);
  transition: width 0.25s;
  overflow-x: hidden;
}

.admin-menu {
  border-right: none;
  height: 100%;
}

.admin-main {
  padding: 0;
  background: var(--admin-content-bg);
  overflow-y: auto;
  display: flex;
  flex-direction: column;
}

.admin-breadcrumb {
  padding: 16px 16px 0;
  margin: 0;
}

.admin-main > :deep(.router-view-content) {
  padding: 16px;
}
</style>
