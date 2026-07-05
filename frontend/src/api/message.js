import request from '@/utils/request'

export const messageApi = {
  page(params) {
    return request.get('/bus/message/page', { params })
  },
  getUnreadCount() {
    return request.get('/bus/message/unread-count')
  },
  markAsRead(id) {
    return request.put(`/bus/message/${id}/read`)
  },
  markAllAsRead() {
    return request.put('/bus/message/read-all')
  },
  delete(id) {
    return request.delete(`/bus/message/${id}`)
  }
}

/** 构建SSE连接URL（EventSource不支持自定义Header，通过query参数传递token） */
export function getSseUrl() {
  const token = localStorage.getItem('token')
  const base = import.meta.env.VITE_API_BASE || ''
  return `${base}/bus/message/sse?token=${token}`
}
