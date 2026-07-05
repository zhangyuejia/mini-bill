import { onMounted, onUnmounted } from 'vue'
import { useMessageStore } from '@/stores/message'

/**
 * SSE生命周期管理 composable
 * 组件挂载时建立SSE连接并拉取未读数，卸载时断开
 */
export function useSSE() {
  const messageStore = useMessageStore()

  onMounted(() => {
    const token = localStorage.getItem('token')
    if (token) {
      messageStore.connectSSE()
      messageStore.fetchUnreadCount()
    }
  })

  onUnmounted(() => {
    messageStore.disconnectSSE()
  })
}
