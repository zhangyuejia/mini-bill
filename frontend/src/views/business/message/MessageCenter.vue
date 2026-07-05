<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title">消息中心</h2>
      <div class="page-header-actions">
        <el-button v-if="unreadTotal > 0" type="primary" @click="handleMarkAllRead" :loading="markingAll">
          全部标为已读（{{ unreadTotal }}条未读）
        </el-button>
      </div>
    </div>

    <div class="query-bar">
      <el-select v-model="query.type" placeholder="消息类型" clearable style="width:160px">
        <el-option label="账单校验" value="bill_inspection" />
      </el-select>
      <el-select v-model="query.isRead" placeholder="读取状态" clearable style="width:140px">
        <el-option label="未读" :value="0" />
        <el-option label="已读" :value="1" />
      </el-select>
      <el-button type="primary" @click="handleQuery">查询</el-button>
    </div>

    <div class="table-container">
      <el-table :data="list" v-loading="loading" border stripe
        :row-class-name="tableRowClassName"
        @row-click="handleRowClick">
        <el-table-column label="标题" min-width="160" show-overflow-tooltip>
          <template #default="{ row }">
            <span :style="{ fontWeight: row.isRead === 0 ? '600' : 'normal' }">{{ row.title }}</span>
          </template>
        </el-table-column>
        <el-table-column label="类型" width="100" align="center">
          <template #default="{ row }">
            <el-tag v-if="row.type === 'bill_inspection'" type="warning" size="small">账单校验</el-tag>
            <el-tag v-else size="small">{{ row.type }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="内容" min-width="240" show-overflow-tooltip>
          <template #default="{ row }">
            <el-popover placement="bottom" :width="400" trigger="click" :show-after="200">
              <template #reference>
                <span class="content-preview">{{ row.content }}</span>
              </template>
              <div class="content-detail">{{ row.content }}</div>
            </el-popover>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag :type="row.isRead === 0 ? 'danger' : 'info'" size="small">
              {{ row.isRead === 0 ? '未读' : '已读' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="时间" width="170" align="center">
          <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.isRead === 0" link type="primary" size="small" @click.stop="handleMarkRead(row)">标为已读</el-button>
            <el-button v-if="row.relatedBillId" link type="primary" size="small" @click.stop="handleViewBill(row)">查看账单</el-button>
            <el-button link type="danger" size="small" @click.stop="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination v-model:currentPage="pageNum" v-model:pageSize="pageSize"
          :total="total" :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          :hide-on-single-page="false"
          @current-change="fetchData" @size-change="fetchData" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { messageApi } from '@/api'
import { useMessageStore } from '@/stores/message'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const messageStore = useMessageStore()

const loading = ref(false)
const list = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const query = ref({ type: '', isRead: '' })
const markingAll = ref(false)
const unreadTotal = ref(0)

onMounted(() => {
  fetchData()
})

async function fetchData() {
  loading.value = true
  try {
    const params = { pageNum: pageNum.value, pageSize: pageSize.value }
    if (query.value.type) params.type = query.value.type
    if (query.value.isRead !== '' && query.value.isRead != null) params.isRead = query.value.isRead

    const res = await messageApi.page(params)
    const data = res.data
    list.value = data.records || []
    total.value = data.total || 0

    // 获取未读总数（不受筛选条件影响）
    try {
      const countRes = await messageApi.getUnreadCount()
      unreadTotal.value = countRes.data ?? 0
      messageStore.unreadCount = unreadTotal.value
    } catch (e) { /* ignore */ }
  } catch (e) { /* 由request拦截器统一处理 */ }
  finally { loading.value = false }
}

function handleQuery() {
  pageNum.value = 1
  fetchData()
}

async function handleMarkRead(row) {
  try {
    await messageApi.markAsRead(row.id)
    row.isRead = 1
    messageStore.decreaseUnread(1)
    unreadTotal.value = Math.max(0, unreadTotal.value - 1)
    ElMessage.success('已标记为已读')
  } catch (e) { /* ignore */ }
}

async function handleMarkAllRead() {
  markingAll.value = true
  try {
    await messageApi.markAllAsRead()
    list.value.forEach(item => { item.isRead = 1 })
    messageStore.unreadCount = 0
    unreadTotal.value = 0
    ElMessage.success('已全部标为已读')
  } catch (e) { /* ignore */ }
  finally { markingAll.value = false }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm('确定删除此消息？', '提示', { type: 'warning' })
    await messageApi.delete(row.id)
    list.value = list.value.filter(item => item.id !== row.id)
    total.value--
    if (row.isRead === 0) {
      messageStore.decreaseUnread(1)
      unreadTotal.value = Math.max(0, unreadTotal.value - 1)
    }
    ElMessage.success('已删除')
    // 当前页删空时回上一页
    if (list.value.length === 0 && pageNum.value > 1) {
      pageNum.value--
      fetchData()
    }
  } catch (e) { /* 取消或失败，忽略 */ }
}

function handleViewBill(row) {
  if (row.relatedBillId) {
    router.push({ path: '/bill' })
  }
}

function handleRowClick(row) {
  // 点击行可展开查看详情
}

function tableRowClassName({ row }) {
  return row.isRead === 0 ? 'unread-row' : ''
}

function formatTime(time) {
  if (!time) return '-'
  // 兼容各种日期格式
  const d = new Date(time)
  if (isNaN(d.getTime())) return time
  const pad = n => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`
}
</script>

<style lang="scss" scoped>
.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;

  .page-title {
    margin-bottom: 0;
  }

  .page-header-actions {
    display: flex;
    gap: 8px;
  }
}

.content-preview {
  cursor: pointer;
  color: #606266;
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;

  &:hover {
    color: #409eff;
  }
}

.content-detail {
  white-space: pre-wrap;
  line-height: 1.8;
  color: #303133;
}

:deep(.unread-row) {
  background-color: #ecf5ff !important;
}
</style>
