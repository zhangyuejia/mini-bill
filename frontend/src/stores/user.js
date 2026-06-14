import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authApi, familyApi } from '@/api'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || '{}'))
  const permissions = ref(JSON.parse(localStorage.getItem('permissions') || '[]'))
  const roles = ref(JSON.parse(localStorage.getItem('roles') || '[]'))
  const menus = ref(JSON.parse(localStorage.getItem('menus') || '[]'))
  const families = ref(JSON.parse(localStorage.getItem('families') || '[]'))
  const currentFamilyId = ref(localStorage.getItem('currentFamilyId') || '')

  const isLoggedIn = computed(() => !!token.value)
  const isAdmin = computed(() => roles.value.includes('ADMIN'))
  const currentFamily = computed(() =>
    families.value.find(f => String(f.id) === String(currentFamilyId.value))
  )

  async function login(loginData) {
    const res = await authApi.login(loginData)
    const data = res.data
    token.value = data.token
    userInfo.value = data.userInfo
    permissions.value = data.permissions || []
    roles.value = data.roles || []
    menus.value = data.menus || []

    localStorage.setItem('token', data.token)
    localStorage.setItem('userInfo', JSON.stringify(data.userInfo))
    localStorage.setItem('permissions', JSON.stringify(data.permissions || []))
    localStorage.setItem('roles', JSON.stringify(data.roles || []))
    localStorage.setItem('menus', JSON.stringify(data.menus || []))

    return data
  }

  async function register(registerData) {
    const res = await authApi.register(registerData)
    const data = res.data
    token.value = data.token
    userInfo.value = data.userInfo
    permissions.value = data.permissions || []
    roles.value = data.roles || []
    menus.value = data.menus || []

    localStorage.setItem('token', data.token)
    localStorage.setItem('userInfo', JSON.stringify(data.userInfo))
    localStorage.setItem('permissions', JSON.stringify(data.permissions || []))
    localStorage.setItem('roles', JSON.stringify(data.roles || []))
    localStorage.setItem('menus', JSON.stringify(data.menus || []))

    return data
  }

  async function fetchFamilies() {
    try {
      const res = await familyApi.getMyFamilies()
      families.value = res.data || []
      localStorage.setItem('families', JSON.stringify(families.value))
      if (families.value.length > 0 && !currentFamilyId.value) {
        const defaultFamily = families.value.find(f => f.defaultFlag === 1) || families.value[0]
        currentFamilyId.value = String(defaultFamily.id)
        localStorage.setItem('currentFamilyId', currentFamilyId.value)
      }
    } catch (e) {
      families.value = []
    }
  }

  function setCurrentFamily(familyId) {
    currentFamilyId.value = String(familyId)
    localStorage.setItem('currentFamilyId', currentFamilyId.value)
  }

  function logout() {
    token.value = ''
    userInfo.value = {}
    permissions.value = []
    roles.value = []
    menus.value = []
    families.value = []
    currentFamilyId.value = ''
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
    localStorage.removeItem('permissions')
    localStorage.removeItem('roles')
    localStorage.removeItem('menus')
    localStorage.removeItem('families')
    localStorage.removeItem('currentFamilyId')
  }

  return {
    token, userInfo, permissions, roles, menus, families, currentFamilyId,
    isLoggedIn, isAdmin, currentFamily,
    login, register, fetchFamilies, setCurrentFamily, logout
  }
})
