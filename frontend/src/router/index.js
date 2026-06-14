import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/Login.vue'),
    meta: { title: '登录', noAuth: true }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/login/Register.vue'),
    meta: { title: '注册', noAuth: true }
  },
  {
    path: '/',
    component: () => import('@/layout/AppLayout.vue'),
    meta: { requiresAuth: true },
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/business/dashboard/Dashboard.vue'),
        meta: { title: '首页', icon: 'House' }
      },
      // 系统管理（管理员）
      {
        path: 'user',
        name: 'User',
        component: () => import('@/views/admin/UserManagement.vue'),
        meta: { title: '用户管理', icon: 'User', roles: ['ADMIN'] }
      },
      {
        path: 'role',
        name: 'Role',
        component: () => import('@/views/admin/RoleManagement.vue'),
        meta: { title: '角色管理', icon: 'Avatar', roles: ['ADMIN'] }
      },
      {
        path: 'menu',
        name: 'Menu',
        component: () => import('@/views/admin/MenuManagement.vue'),
        meta: { title: '菜单管理', icon: 'Menu', roles: ['ADMIN'] }
      },
      {
        path: 'dict',
        name: 'Dict',
        component: () => import('@/views/admin/DictManagement.vue'),
        meta: { title: '字典管理', icon: 'Collection', roles: ['ADMIN'] }
      },
      // 业务路由（作为二级菜单，路径保持不变）
      {
        path: 'family',
        name: 'Family',
        component: () => import('@/views/business/family/FamilyManagement.vue'),
        meta: { title: '家庭管理', icon: 'HomeFilled' }
      },
      {
        path: 'address',
        name: 'Address',
        component: () => import('@/views/business/address/AddressManagement.vue'),
        meta: { title: '住址管理', icon: 'MapLocation' }
      },
      {
        path: 'bill',
        name: 'Bill',
        component: () => import('@/views/business/bill/BillManagement.vue'),
        meta: { title: '房租水电', icon: 'Ticket' }
      },
      {
        path: 'item',
        name: 'Item',
        component: () => import('@/views/business/item/ItemManagement.vue'),
        meta: { title: '物件管理', icon: 'Goods' }
      },
      {
        path: 'item-cost',
        name: 'ItemCost',
        component: () => import('@/views/business/item/ItemCostManagement.vue'),
        meta: { title: '物品流水', icon: 'Coin' }
      },
      {
        path: 'education',
        name: 'Education',
        component: () => import('@/views/business/bill/EducationManagement.vue'),
        meta: { title: '教育费用', icon: 'Coin' }
      },
      {
        path: 'maintenance',
        name: 'Maintenance',
        component: () => import('@/views/business/bill/MaintenanceManagement.vue'),
        meta: { title: '维护费用', icon: 'Tools' }
      },
      {
        path: 'saving-item',
        name: 'SavingItem',
        component: () => import('@/views/business/saving/SavingItemManagement.vue'),
        meta: { title: '储蓄项管理', icon: 'Coin' }
      },
      {
        path: 'saving',
        name: 'Saving',
        component: () => import('@/views/business/saving/SavingManagement.vue'),
        meta: { title: '家庭储蓄', icon: 'Wallet' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  document.title = to.meta.title ? `${to.meta.title} - 财小账` : '财小账'
  const token = localStorage.getItem('token')
  if (to.meta.requiresAuth && !token) {
    next('/login')
  } else if (to.meta.noAuth && token && to.path === '/login') {
    next('/dashboard')
  } else {
    next()
  }
})

export default router
