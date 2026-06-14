<template>
  <div class="app-layout" :class="{ 'sidebar-collapsed': isCollapsed }">
    <div v-if="isMobile && mobileSidebarOpen" class="mobile-overlay" @click="closeMobileSidebar" />

    <aside class="sidebar" :class="{ 'mobile-open': isMobile && mobileSidebarOpen }">
      <div class="sidebar-header">
        <div class="logo">
          <svg width="28" height="28" viewBox="0 0 48 48" fill="none">
            <rect width="48" height="48" rx="12" fill="url(#sd-g)"/>
            <text x="24" y="32" text-anchor="middle" font-size="26" fill="white" font-weight="700">财</text>
            <defs><linearGradient id="sd-g" x1="0" y1="0" x2="48" y2="48">
              <stop offset="0%" stop-color="#409eff"/><stop offset="100%" stop-color="#337ecc"/>
            </linearGradient></defs>
          </svg>
          <span v-show="!isCollapsed || !isMobile || mobileSidebarOpen" class="logo-text">财小账</span>
        </div>
      </div>

      <el-scrollbar class="sidebar-menu">
        <el-menu
          :default-active="route.path"
          :collapse="isCollapsed && !isMobile"
          :router="true"
          background-color="transparent"
          text-color="rgba(255,255,255,0.65)"
          active-text-color="#409eff"
        >
          <template v-for="item in filteredMenus" :key="item.path || item.name">
            <el-menu-item v-if="!item.children && item.path" :index="item.path">
              <el-icon><component :is="item.icon || 'Menu'" /></el-icon>
              <template #title>{{ item.name }}</template>
            </el-menu-item>
            <el-sub-menu v-else :index="item.name">
              <template #title>
                <el-icon><component :is="item.icon || 'Menu'" /></el-icon>
                <span>{{ item.name }}</span>
              </template>
              <el-menu-item v-for="child in item.children" :key="child.path" :index="child.path">
                <el-icon><component :is="child.icon || 'Menu'" /></el-icon>
                <template #title>{{ child.name }}</template>
              </el-menu-item>
            </el-sub-menu>
          </template>
        </el-menu>
      </el-scrollbar>

      <div class="sidebar-footer">
        <div class="sidebar-collapse-btn" @click="toggleSidebar">
          <el-icon :size="18"><template v-if="isMobile && mobileSidebarOpen"><ArrowLeftBold /></template><template v-else><Fold v-if="!isCollapsed" /><Expand v-else /></template></el-icon>
        </div>
      </div>
    </aside>

    <div class="main-area">
      <header class="top-header">
        <div class="header-left">
          <el-button text @click="toggleSidebar" class="collapse-btn">
            <el-icon :size="20"><Fold v-if="!isCollapsed || isMobile" /><Expand v-else /></el-icon>
          </el-button>
          <el-breadcrumb>
            <el-breadcrumb-item :to="{ path: '/dashboard' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item v-if="route.meta.title">{{ route.meta.title }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>

        <div class="header-right">
          <el-dropdown v-if="userStore.families.length > 0" @command="switchFamily">
            <span class="family-switcher">
              <el-icon><HomeFilled /></el-icon>
              <span class="family-name">{{ userStore.currentFamily?.name || '选择家庭' }}</span>
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item v-for="f in userStore.families" :key="f.id" :command="f.id"
                  :class="{ active: String(f.id) === userStore.currentFamilyId }">
                  <el-icon v-if="String(f.id) === userStore.currentFamilyId"><Check /></el-icon>
                  {{ f.name }}
                </el-dropdown-item>
                <el-dropdown-item divided @click="showCreateFamily = true">
                  <el-icon><Plus /></el-icon> 创建家庭
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>

          <el-dropdown @command="handleUserCommand">
            <span class="user-info">
              <el-avatar :size="34" class="user-avatar">
                {{ userStore.userInfo?.nickname?.charAt(0) || 'U' }}
              </el-avatar>
              <span class="username">{{ userStore.userInfo?.nickname || userStore.userInfo?.username }}</span>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人信息</el-dropdown-item>
                <el-dropdown-item command="password">修改密码</el-dropdown-item>
                <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </header>

      <main class="content-area">
        <router-view />
      </main>
    </div>

    <el-dialog v-model="showCreateFamily" title="创建家庭" width="400px">
      <el-form :model="familyForm" label-width="80px">
        <el-form-item label="家庭名称">
          <el-input v-model="familyForm.name" placeholder="请输入家庭名称" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showCreateFamily = false">取消</el-button>
        <el-button type="primary" @click="createFamily" :loading="loading">创建</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { familyApi } from '@/api'
import { ElMessage } from 'element-plus'
import {
  Fold, Expand, ArrowDown, HomeFilled, Check, Plus,
  House, User, Avatar, Menu, Collection, Goods, Coin, Wallet,
  MapLocation, Ticket, Setting, ArrowLeftBold, Tools
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const isCollapsed = ref(false)
const isMobile = ref(false)
const mobileSidebarOpen = ref(false)
const showCreateFamily = ref(false)
const loading = ref(false)
const familyForm = ref({ name: '' })

const iconMap = { House, User, Avatar, Menu, Collection, HomeFilled, MapLocation, Ticket, Goods, Coin, Wallet, Setting }

const menuDefinition = [
  { path: 'dashboard', name: '首页', icon: 'House' },
  { name: '系统管理', icon: 'Setting', roles: ['ADMIN'], children: [
    { path: 'user', name: '用户管理', icon: 'User' },
    { path: 'role', name: '角色管理', icon: 'Avatar' },
    { path: 'menu', name: '菜单管理', icon: 'Menu' },
    { path: 'dict', name: '字典管理', icon: 'Collection' }
  ]},
  { name: '家庭管理', icon: 'HomeFilled', children: [
    { path: 'family', name: '家庭管理', icon: 'HomeFilled' },
    { path: 'address', name: '住址管理', icon: 'MapLocation' },
    { path: 'item', name: '物件管理', icon: 'Goods' }
  ]},
  { name: '账单管理', icon: 'Ticket', children: [
    { path: 'bill', name: '房租水电', icon: 'Ticket' },
    { path: 'item-cost', name: '物件费用', icon: 'Coin' },
    { path: 'maintenance', name: '维护费用', icon: 'Tools' }
  ]},
  { name: '财富管理', icon: 'Wallet', children: [
    { path: 'saving-item', name: '储蓄项管理', icon: 'Coin' },
    { path: 'saving', name: '家庭储蓄', icon: 'Wallet' }
  ]}
]

const filteredMenus = computed(() => {
  return menuDefinition.filter(group => {
    if (group.roles?.length > 0) return userStore.roles.some(r => group.roles.includes(r))
    if (userStore.isAdmin) return group.path === 'dashboard' || group.roles?.length > 0
    return true
  }).map(group => ({
    ...group,
    icon: iconMap[group.icon] || Menu,
    children: group.children?.map(c => ({ ...c, icon: iconMap[c.icon] || Menu }))
  }))
})

function toggleSidebar() {
  if (isMobile.value) {
    mobileSidebarOpen.value = !mobileSidebarOpen.value
  } else {
    isCollapsed.value = !isCollapsed.value
  }
}
function closeMobileSidebar() { mobileSidebarOpen.value = false }
function checkMobile() {
  const wasMobile = isMobile.value
  isMobile.value = window.innerWidth <= 768
  // Reset states when switching between mobile/desktop
  if (isMobile.value && !wasMobile) {
    isCollapsed.value = true
    mobileSidebarOpen.value = false
  } else if (!isMobile.value && wasMobile) {
    isCollapsed.value = false
    mobileSidebarOpen.value = false
  } else if (isMobile.value) {
    mobileSidebarOpen.value = false
  }
}

async function switchFamily(familyId) {
  userStore.setCurrentFamily(familyId)
  ElMessage.success('已切换至 ' + (userStore.currentFamily?.name || ''))
}

async function createFamily() {
  if (!familyForm.value.name) { ElMessage.warning('请输入家庭名称'); return }
  loading.value = true
  try {
    await familyApi.create({ name: familyForm.value.name })
    await userStore.fetchFamilies()
    showCreateFamily.value = false
    familyForm.value.name = ''
    ElMessage.success('家庭创建成功')
  } catch (e) { } finally { loading.value = false }
}

function handleUserCommand(command) {
  if (command === 'logout') { userStore.logout(); router.push('/login') }
  else if (command === 'password') ElMessage.info('密码修改功能开发中')
  else if (command === 'profile') ElMessage.info('个人信息功能开发中')
}

onMounted(() => {
  checkMobile()
  window.addEventListener('resize', checkMobile)
  if (!userStore.isAdmin) userStore.fetchFamilies()
})

watch(isMobile, (val) => { if (val) { isCollapsed.value = true; mobileSidebarOpen.value = false } else { isCollapsed.value = false; mobileSidebarOpen.value = false } })
</script>

<style lang="scss" scoped>
$sidebar-width: 240px;
$sidebar-collapsed-width: 64px;
$header-height: 60px;
$mobile-breakpoint: 768px;

.app-layout {
  display: flex;
  height: 100vh;
  overflow: hidden;
  background: #f0f2f5;
}

.sidebar {
  width: $sidebar-width;
  background: linear-gradient(180deg, #1a1a2e 0%, #16213e 100%);
  display: flex;
  flex-direction: column;
  transition: width 0.3s cubic-bezier(0.4,0,0.2,1);
  flex-shrink: 0;
  z-index: 100;
  position: relative;

  @media (max-width: $mobile-breakpoint) {
    position: fixed;
    left: 0;
    top: 0;
    bottom: 0;
    width: $sidebar-width !important;
    transform: translateX(-100%);
    transition: transform 0.3s cubic-bezier(0.4,0,0.2,1);
    box-shadow: 4px 0 24px rgba(0,0,0,0.2);

    &.mobile-open {
      transform: translateX(0);
    }
  }

  .sidebar-header {
    height: $header-height;
    display: flex;
    align-items: center;
    justify-content: center;
    border-bottom: 1px solid rgba(255,255,255,0.06);
    flex-shrink: 0;

    .logo {
      display: flex;
      align-items: center;
      gap: 10px;
    }

    .logo-text {
      color: #fff;
      font-size: 18px;
      font-weight: 700;
      letter-spacing: 2px;
    }
  }

  .sidebar-menu {
    flex: 1;
    padding: 8px 0;

    .el-menu {
      border-right: none;
      background: transparent;

      .el-menu-item, .el-sub-menu__title {
        &:hover {
          background: rgba(255,255,255,0.08) !important;
        }
      }

      .el-menu-item.is-active {
        background: linear-gradient(135deg, rgba(64,158,255,0.2), rgba(64,158,255,0.05)) !important;
        color: #409eff !important;
        font-weight: 500;

        .el-icon { color: #409eff; }
      }

      .el-sub-menu .el-menu {
        .el-menu-item {
          padding-left: 56px !important;
          height: 40px;
          line-height: 40px;

          &.is-active {
            background: linear-gradient(135deg, rgba(64,158,255,0.15), rgba(64,158,255,0.02)) !important;
            font-weight: 500;
          }
        }
      }
    }
  }

  .sidebar-footer {
    border-top: 1px solid rgba(255,255,255,0.06);
    padding: 8px 12px;
    flex-shrink: 0;

    .sidebar-collapse-btn {
      display: flex;
      align-items: center;
      justify-content: center;
      height: 36px;
      border-radius: 8px;
      color: rgba(255,255,255,0.45);
      cursor: pointer;
      transition: all 0.2s;

      &:hover { background: rgba(255,255,255,0.08); color: rgba(255,255,255,0.85); }
    }
  }
}

.main-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  min-width: 0;
}

.top-header {
  height: $header-height;
  background: rgba(255,255,255,0.9);
  backdrop-filter: blur(12px);
  border-bottom: 1px solid rgba(0,0,0,0.06);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  flex-shrink: 0;
  position: sticky;
  top: 0;
  z-index: 10;

  @media (max-width: $mobile-breakpoint) {
    padding: 0 12px;
  }

  .header-left {
    display: flex;
    align-items: center;
    gap: 12px;

    .collapse-btn {
      font-size: 18px;
      color: #606266;
      transition: color 0.2s;
      &:hover { color: #409eff; }
    }

    @media (max-width: $mobile-breakpoint) {
      .el-breadcrumb { display: none; }
    }
  }

  .header-right {
    display: flex;
    align-items: center;
    gap: 16px;

    .family-switcher {
      display: flex;
      align-items: center;
      gap: 6px;
      cursor: pointer;
      padding: 6px 14px;
      border-radius: 20px;
      font-size: 13px;
      color: #606266;
      background: #f5f7fa;
      transition: all 0.2s;

      &:hover {
        background: #ecf5ff;
        color: #409eff;
      }

      .family-name { max-width: 120px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
    }

    .user-info {
      display: flex;
      align-items: center;
      gap: 8px;
      cursor: pointer;
      padding: 4px 10px;
      border-radius: 20px;
      transition: background 0.2s;

      &:hover { background: #f5f7fa; }

      .user-avatar {
        background: linear-gradient(135deg, #409eff, #337ecc);
        font-size: 13px;
      }

      .username { font-size: 14px; color: #303133; font-weight: 500; }
    }

    @media (max-width: $mobile-breakpoint) {
      gap: 8px;
      .family-switcher {
        padding: 4px 8px;
        font-size: 12px;
        .family-name { max-width: 60px; }
      }
      .user-info {
        padding: 2px 6px;
        .username { display: none; }
      }
    }
  }
}

.content-area {
  flex: 1;
  overflow-y: auto;
  background: #f0f2f5;
}

.mobile-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0,0,0,0.4);
  z-index: 99;
  backdrop-filter: blur(4px);
  animation: fadeIn 0.25s ease;
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}

.collapse-btn {
  @media (max-width: $mobile-breakpoint) { display: block; }
}

// Collapsed state
.sidebar-collapsed {
  .sidebar { width: $sidebar-collapsed-width;
    @media (max-width: $mobile-breakpoint) { width: $sidebar-width !important; }
  }
  .family-name, .username { display: none; }
  .family-switcher { padding: 6px 10px; }
}

// Dark breadcrumb
:deep(.el-breadcrumb__inner) { color: #909399; }
:deep(.el-breadcrumb__inner.is-link) { color: #606266; &:hover { color: #409eff; } }
</style>
