import request from '@/utils/request'

// 认证接口
export const authApi = {
  login(data) {
    return request.post('/system/auth/login', data)
  },
  register(data) {
    return request.post('/system/auth/register', data)
  },
  sendEmailCode(email) {
    return request.post(`/system/auth/sendEmailCode?email=${email}`)
  }
}

// 用户管理接口
export const userApi = {
  page(params) {
    return request.get('/system/user/page', { params })
  },
  getById(id) {
    return request.get(`/system/user/${id}`)
  },
  getUserRoleIds(userId) {
    return request.get(`/system/user/${userId}/roleIds`)
  },
  assignRoles(userId, roleIds) {
    return request.put(`/system/user/${userId}/roles`, roleIds)
  },
  setStatus(userId, status) {
    return request.put(`/system/user/${userId}/status?status=${status}`)
  },
  resetPassword(data) {
    return request.put('/system/user/resetPassword', data)
  }
}

// 角色管理接口
export const roleApi = {
  page(params) {
    return request.get('/system/role/page', { params })
  },
  list() {
    return request.get('/system/role/list')
  },
  getById(id) {
    return request.get(`/system/role/${id}`)
  },
  add(data) {
    return request.post('/system/role', data)
  },
  update(data) {
    return request.put('/system/role', data)
  },
  delete(id) {
    return request.delete(`/system/role/${id}`)
  },
  getMenuIds(roleId) {
    return request.get(`/system/role/${roleId}/menuIds`)
  },
  assignMenus(roleId, menuIds) {
    return request.put(`/system/role/${roleId}/menus`, menuIds)
  }
}

// 菜单管理接口
export const menuApi = {
  getTree() {
    return request.get('/system/menu/tree')
  },
  getById(id) {
    return request.get(`/system/menu/${id}`)
  },
  add(data) {
    return request.post('/system/menu', data)
  },
  update(data) {
    return request.put('/system/menu', data)
  },
  delete(id) {
    return request.delete(`/system/menu/${id}`)
  }
}

// 字典管理接口
export const dictApi = {
  // 字典类型
  pageType(params) {
    return request.get('/system/dict/type/page', { params })
  },
  listType() {
    return request.get('/system/dict/type/list')
  },
  addType(data) {
    return request.post('/system/dict/type', data)
  },
  updateType(data) {
    return request.put('/system/dict/type', data)
  },
  deleteType(id) {
    return request.delete(`/system/dict/type/${id}`)
  },
  // 字典数据
  pageData(params) {
    return request.get('/system/dict/data/page', { params })
  },
  getDataByCode(code) {
    return request.get(`/system/dict/data/code/${code}`)
  },
  addData(data) {
    return request.post('/system/dict/data', data)
  },
  updateData(data) {
    return request.put('/system/dict/data', data)
  },
  deleteData(id) {
    return request.delete(`/system/dict/data/${id}`)
  }
}

// 家庭管理接口
export const familyApi = {
  getMyFamilies() {
    return request.get('/bus/family/my')
  },
  create(data) {
    return request.post('/bus/family', data)
  },
  update(data) {
    return request.put('/bus/family', data)
  },
  invite(familyId, email) {
    return request.post(`/bus/family/${familyId}/invite`, { email })
  },
  removeMember(familyId, userId) {
    return request.delete(`/bus/family/${familyId}/member/${userId}`)
  },
  getMembers(familyId) {
    return request.get(`/bus/family/${familyId}/members`)
  },
  setDefault(familyId) {
    return request.put(`/bus/family/${familyId}/default`)
  },
  page(params) {
    return request.get('/bus/family/page', { params })
  },
  getById(id) {
    return request.get(`/bus/family/${id}`)
  }
}

// 住址管理接口
export const addressApi = {
  listByFamily(familyId) {
    return request.get(`/bus/address/list/${familyId}`)
  },
  page(params) {
    return request.get('/bus/address/page', { params })
  },
  getById(id) {
    return request.get(`/bus/address/${id}`)
  },
  add(data) {
    return request.post('/bus/address', data)
  },
  update(data) {
    return request.put('/bus/address', data)
  },
  delete(id) {
    return request.delete(`/bus/address/${id}`)
  }
}

// 账单管理接口
export const billApi = {
  page(params) {
    return request.get('/bus/bill/page', { params })
  },
  getById(id) {
    return request.get(`/bus/bill/${id}`)
  },
  add(data) {
    return request.post('/bus/bill', data)
  },
  update(data) {
    return request.put('/bus/bill', data)
  },
  delete(id) {
    return request.delete(`/bus/bill/${id}`)
  },
  getAttachments(billId) {
    return request.get(`/bus/bill/${billId}/attachments`)
  },
  addAttachment(billId, data) {
    return request.post(`/bus/bill/${billId}/attachment`, data)
  },
  deleteAttachment(id) {
    return request.delete(`/bus/bill/attachment/${id}`)
  }
}

// 物件管理接口
export const itemApi = {
  page(params) {
    return request.get('/bus/item/page', { params })
  },
  listByAddress(addressId) {
    return request.get(`/bus/item/listByAddress/${addressId}`)
  },
  getById(id) {
    return request.get(`/bus/item/${id}`)
  },
  add(data) {
    return request.post('/bus/item', data)
  },
  update(data) {
    return request.put('/bus/item', data)
  },
  delete(id) {
    return request.delete(`/bus/item/${id}`)
  },
  getAttachments(itemId) {
    return request.get(`/bus/item/${itemId}/attachments`)
  },
  addAttachment(itemId, data) {
    return request.post(`/bus/item/${itemId}/attachment`, data)
  },
  deleteAttachment(id) {
    return request.delete(`/bus/item/attachment/${id}`)
  },
  // 物件费用
  costPage(params) {
    return request.get('/bus/item/cost/page', { params })
  },
  addCost(data) {
    return request.post('/bus/item/cost', data)
  },
  updateCost(data) {
    return request.put('/bus/item/cost', data)
  },
  deleteCost(id) {
    return request.delete(`/bus/item/cost/${id}`)
  },
  getCostAttachments(costId) {
    return request.get(`/bus/item/cost/${costId}/attachments`)
  },
  addCostAttachment(costId, data) {
    return request.post(`/bus/item/cost/${costId}/attachment`, data)
  },
  deleteCostAttachment(id) {
    return request.delete(`/bus/item/cost/attachment/${id}`)
  }
}

// 储蓄管理接口
export const savingApi = {
  getItems(familyId, memberId) {
    return request.get(`/bus/saving/items/${familyId}/${memberId}`)
  },
  addItem(data) {
    return request.post('/bus/saving/item', data)
  },
  updateItem(data) {
    return request.put('/bus/saving/item', data)
  },
  deleteItem(id) {
    return request.delete(`/bus/saving/item/${id}`)
  },
  page(params) {
    return request.get('/bus/saving/page', { params })
  },
  create(data) {
    return request.post('/bus/saving/create', data)
  },
  update(data) {
    return request.put('/bus/saving', data)
  },
  delete(id) {
    return request.delete(`/bus/saving/${id}`)
  },
  batchSaveRecords(savingId, records) {
    return request.post(`/bus/saving/records/${savingId}`, records)
  },
  getRecords(savingId) {
    return request.get(`/bus/saving/records/${savingId}`)
  }
}

// 文件上传接口
export const fileApi = {
  upload(file) {
    const formData = new FormData()
    formData.append('file', file)
    return request.post('/bus/api/file/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
  }
}

// 仪表盘数据接口
export const dashboardApi = {
  savingTrend(familyId) {
    return request.get('/bus/dashboard/saving-trend', { params: { familyId } })
  },
  billCompare(familyId, addressId) {
    return request.get('/bus/dashboard/bill-compare', { params: { familyId, ...(addressId ? { addressId } : {}) } })
  }
}
