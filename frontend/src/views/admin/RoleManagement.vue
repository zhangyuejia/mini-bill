<template>
  <div class="page-container">
    <h2 class="page-title">角色管理</h2>

    <div class="query-bar">
      <el-input v-model="keyword" placeholder="搜索角色名称/编码" clearable style="width: 280px" @clear="fetchData" prefix-icon="Search" />
      <el-button type="primary" @click="fetchData">查询</el-button>
      <el-button type="success" @click="showEditDialog(null)">新增角色</el-button>
    </div>

    <div class="table-container">
      <el-table :data="list" v-loading="loading" border stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="角色名称" min-width="140" />
        <el-table-column prop="code" label="角色编码" min-width="140" />
        <el-table-column prop="sort" label="排序" width="80" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }"><el-tag :type="row.status === 0 ? 'success' : 'danger'" size="small" effect="dark">{{ row.status === 0 ? '正常' : '禁用' }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" min-width="200" show-overflow-tooltip />
        <el-table-column label="操作" width="240" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="showEditDialog(row)">编辑</el-button>
            <el-button link type="primary" size="small" @click="showAssignMenu(row)">分配菜单</el-button>
            <el-button link type="danger" size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-pagination v-model:current-page="pageNum" v-model:page-size="pageSize" :total="total" layout="prev, pager, next, total" class="pagination" @current-change="fetchData" />
    </div>

    <el-dialog v-model="showDialog" :title="isEdit ? '编辑角色' : '新增角色'" width="500px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="角色名称"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="角色编码"><el-input v-model="form.code" :disabled="isEdit" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="form.sort" :min="0" /></el-form-item>
        <el-form-item label="状态"><el-switch v-model="form.status" :active-value="0" :inactive-value="1" /></el-form-item>
        <el-form-item label="备注"><el-input v-model="form.remark" type="textarea" :rows="3" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="showDialog = false">取消</el-button><el-button type="primary" @click="save" :loading="saving">保存</el-button></template>
    </el-dialog>

    <el-dialog v-model="showMenuDialog" title="分配菜单权限" width="450px">
      <el-tree ref="menuTreeRef" :data="menuTree" show-checkbox node-key="id" :props="{ label: 'name', children: 'children' }" default-expand-all />
      <template #footer><el-button @click="showMenuDialog = false">取消</el-button><el-button type="primary" @click="saveMenuAssign" :loading="menuLoading">保存</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { roleApi, menuApi } from '@/api'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'

const loading = ref(false); const list = ref([]); const total = ref(0)
const pageNum = ref(1); const pageSize = ref(10); const keyword = ref('')
const showDialog = ref(false); const isEdit = ref(false); const saving = ref(false)
const form = ref({ name: '', code: '', sort: 0, status: 0, remark: '' })
const showMenuDialog = ref(false); const menuLoading = ref(false)
const menuTree = ref([]); const menuTreeRef = ref(null); const currentRoleId = ref(null)

async function fetchData() {
  loading.value = true
  try { const res = await roleApi.page({ pageNum: pageNum.value, pageSize: pageSize.value, keyword: keyword.value }); const d = res.data; list.value = d.records || []; total.value = d.total || 0 } finally { loading.value = false }
}
function showEditDialog(row) { isEdit.value = !!row; form.value = row ? { ...row } : { name: '', code: '', sort: 0, status: 0, remark: '' }; showDialog.value = true }
async function save() {
  saving.value = true
  try { isEdit.value ? await roleApi.update(form.value) : await roleApi.add(form.value); ElMessage.success('保存成功'); showDialog.value = false; fetchData() } finally { saving.value = false }
}
async function handleDelete(row) {
  try { await ElMessageBox.confirm(`确认删除角色「${row.name}」？`, '提示'); await roleApi.delete(row.id); ElMessage.success('删除成功'); fetchData() } catch (e) { if (e !== 'cancel') throw e }
}
async function showAssignMenu(row) {
  currentRoleId.value = row.id; showMenuDialog.value = true
  const treeRes = await menuApi.getTree(); menuTree.value = treeRes.data || []
  const menuRes = await roleApi.getMenuIds(row.id); menuTreeRef.value?.setCheckedKeys(menuRes.data || [])
}
async function saveMenuAssign() {
  menuLoading.value = true
  try { const ids = [...(menuTreeRef.value?.getCheckedKeys() || []), ...(menuTreeRef.value?.getHalfCheckedKeys() || [])]; await roleApi.assignMenus(currentRoleId.value, ids); ElMessage.success('菜单分配成功'); showMenuDialog.value = false } finally { menuLoading.value = false }
}
onMounted(() => fetchData())
</script>

<style lang="scss" scoped>
.table-container { background: #fff; border-radius: 12px; padding: 20px; box-shadow: 0 1px 3px rgba(0,0,0,0.06); }
</style>
