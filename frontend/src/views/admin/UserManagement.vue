<template>
  <div class="page-container">
    <h2 class="page-title">用户管理</h2>

    <div class="query-bar">
      <el-input v-model="keyword" placeholder="搜索用户名/昵称/邮箱" clearable style="width: 280px" @clear="fetchData" prefix-icon="Search" />
      <el-button type="primary" @click="fetchData">查询</el-button>
    </div>

    <div class="table-container">
      <el-table :data="list" v-loading="loading" border stripe style="width: 100%">
        <el-table-column prop="id" label="ID" width="100" />
        <el-table-column prop="username" label="用户名" min-width="120" />
        <el-table-column prop="nickname" label="昵称" min-width="120" />
        <el-table-column prop="email" label="邮箱" min-width="180" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 0 ? 'success' : 'danger'" size="small" effect="dark">
              {{ row.status === 0 ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="showAssignRole(row)">分配角色</el-button>
            <el-button link type="warning" size="small" @click="showResetPassword(row)">重置密码</el-button>
            <el-button link :type="row.status === 0 ? 'warning' : 'success'" size="small" @click="toggleStatus(row)">
              {{ row.status === 0 ? '禁用' : '启用' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination v-model:current-page="pageNum" v-model:page-size="pageSize" :total="total"
        layout="prev, pager, next, total" class="pagination" @current-change="fetchData" />
    </div>

    <el-dialog v-model="showRoleDialog" title="分配角色" width="500px">
      <el-checkbox-group v-model="selectedRoleIds" class="role-checkbox-group">
        <el-checkbox v-for="role in allRoles" :key="role.id" :label="role.id" border>{{ role.name }}</el-checkbox>
      </el-checkbox-group>
      <template #footer>
        <el-button @click="showRoleDialog = false">取消</el-button>
        <el-button type="primary" @click="saveRoles" :loading="roleLoading">保存</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="showResetDialog" title="重置密码" width="420px">
      <el-form label-width="80px">
        <el-form-item label="用户">
          <span style="color:#303133;font-size:14px;">{{ resetUserInfo || '请先选择用户' }}</span>
        </el-form-item>
        <el-form-item label="新密码">
          <el-input v-model="resetForm.newPassword" type="password" show-password placeholder="请输入新密码" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showResetDialog = false">取消</el-button>
        <el-button type="primary" @click="resetPassword" :loading="resetting">确认重置</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { userApi, roleApi } from '@/api'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'

const loading = ref(false)
const list = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const keyword = ref('')

const showRoleDialog = ref(false)
const roleLoading = ref(false)
const allRoles = ref([])
const selectedRoleIds = ref([])
const currentUserId = ref(null)

const showResetDialog = ref(false)
const resetting = ref(false)
const resetForm = ref({ userId: '', newPassword: '123456' })
const resetUserInfo = ref('')

async function fetchData() {
  loading.value = true
  try {
    const res = await userApi.page({ pageNum: pageNum.value, pageSize: pageSize.value, keyword: keyword.value })
    const data = res.data; list.value = data.records || []; total.value = data.total || 0
  } catch (e) { list.value = [] } finally { loading.value = false }
}

async function showAssignRole(row) {
  currentUserId.value = row.id; selectedRoleIds.value = []
  const roleRes = await roleApi.list(); allRoles.value = roleRes.data || []
  const userRoleRes = await userApi.getUserRoleIds(row.id); selectedRoleIds.value = userRoleRes.data || []
  showRoleDialog.value = true
}

async function saveRoles() {
  roleLoading.value = true
  try { await userApi.assignRoles(currentUserId.value, selectedRoleIds.value); ElMessage.success('角色分配成功'); showRoleDialog.value = false } finally { roleLoading.value = false }
}

async function toggleStatus(row) {
  const newStatus = row.status === 0 ? 1 : 0; const msg = newStatus === 1 ? '禁用' : '启用'
  try {
    await ElMessageBox.confirm(`确认${msg}用户「${row.username}」？`, '提示')
    await userApi.setStatus(row.id, newStatus); ElMessage.success(`${msg}成功`); fetchData()
  } catch (e) { if (e !== 'cancel') throw e }
}

function showResetPassword(row) {
  resetForm.value.userId = String(row.id); resetForm.value.newPassword = '123456'
  resetUserInfo.value = `${row.username}（ID: ${row.id}）`; showResetDialog.value = true
}

async function resetPassword() {
  if (!resetForm.value.userId) { ElMessage.warning('请选择要重置密码的用户'); return }
  resetting.value = true
  try {
    await userApi.resetPassword(resetForm.value); ElMessage.success(`用户「${resetUserInfo.value}」密码已重置为：${resetForm.value.newPassword}`); showResetDialog.value = false
  } catch (e) { } finally { resetting.value = false }
}

onMounted(() => fetchData())
</script>

<style lang="scss" scoped>
.table-container {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.06);
}

.role-checkbox-group {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;

  .el-checkbox { margin-right: 0; }
  .el-checkbox.is-bordered { border-radius: 8px; padding: 8px 16px; }
}
</style>
