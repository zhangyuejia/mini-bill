import { defineStore } from 'pinia'
import { ref } from 'vue'
import { messageApi, getSseUrl } from '@/api/message'

export const useMessageStore = defineStore('message', () => {
  const unreadCount = ref(0)
  let eventSource = null

  /** 建立SSE连接 */
  function connectSSE() {
    const token = localStorage.getItem('token')
    if (!token) return

    if (eventSource) {
      eventSource.close()
    }

    const url = getSseUrl()
    eventSource = new EventSource(url)

    eventSource.addEventListener('connected', () => {
      console.log('SSE连接已建立')
    })

    eventSource.addEventListener('message', (event) => {
      try {
        const data = JSON.parse(event.data)
        if (data.unreadCount != null) {
          unreadCount.value = data.unreadCount
        }
      } catch (e) {
        // 忽略解析错误
      }
    })

    eventSource.onerror = () => {
      eventSource.close()
      eventSource = null
      // 5秒后自动重连
      setTimeout(() => connectSSE(), 5000)
    }
  }

  /** 断开SSE连接 */
  function disconnectSSE() {
    if (eventSource) {
      eventSource.close()
      eventSource = null
    }
  }

  /** 从服务端获取未读数（用于初始化和SSE断连后的同步） */
  async function fetchUnreadCount() {
    try {
      const res = await messageApi.getUnreadCount()
      unreadCount.value = res.data ?? 0
    } catch (e) {
      // 静默处理
    }
  }

  /** 减少未读数（标记已读后本地更新） */
  function decreaseUnread(n) {
    unreadCount.value = Math.max(0, unreadCount.value - n)
  }

  /** 重置状态（退出登录时） */
  function reset() {
    unreadCount.value = 0
    disconnectSSE()
  }

  return {
    unreadCount,
    connectSSE,
    disconnectSSE,
    fetchUnreadCount,
    decreaseUnread,
    reset
  }
})
