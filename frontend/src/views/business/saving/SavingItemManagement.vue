<template>
  <div class="page-container">
    <h2 class="page-title">储蓄项管理</h2>

    <div v-if="userStore.currentFamily">
      <div class="query-bar">
        <el-select v-model="query.memberId" placeholder="选择成员" clearable style="width:160px" @change="fetchData"><el-option v-for="m in members" :key="m.userId" :label="m.userName || '用户#'+m.userId" :value="m.userId" /></el-select>
        <el-input v-model="query.keyword" placeholder="搜索储蓄项名称" clearable style="width:200px" @clear="fetchData" prefix-icon="Search" />
        <el-button type="primary" @click="fetchData">查询</el-button>
        <el-button type="success" @click="showItemDialog(null)">新增储蓄项</el-button>
      </div>

      <div class="table-container">
        <el-table :data="filteredItems" v-loading="loading" border stripe>
          <el-table-column label="成员" min-width="150"><template #default="{ row }">{{ getMemberName(row.memberId) }}</template></el-table-column>
          <el-table-column prop="name" label="储蓄项名称" min-width="200" />
          <el-table-column prop="status" label="状态" width="100"><template #default="{ row }"><el-tag :type="row.status === 0 ? 'success' : 'info'" size="small" effect="dark">{{ row.status === 0 ? '正常' : '停用' }}</el-tag></template></el-table-column>
          <el-table-column label="操作" width="160"><template #default="{ row }"><el-button link type="primary" size="small" @click="showItemDialog(row)">编辑</el-button><el-button link type="danger" size="small" @click="deleteItem(row)">删除</el-button></template></el-table-column>
        </el-table>
      </div>
    </div>
    <div v-else class="empty-tip"><el-empty description="请先创建或切换到家庭" /></div>

    <el-dialog v-model="showDlg" :title="isEdit ? '编辑储蓄项' : '新增储蓄项'" width="400px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="成员"><el-select v-model="form.memberId" placeholder="选择家庭成员" style="width:100%" :loading="membersLoading"><el-option v-for="m in members" :key="m.userId" :label="m.userName || '用户#'+m.userId" :value="m.userId" /></el-select></el-form-item>
        <el-form-item label="储蓄项名称"><el-input v-model="form.name" placeholder="如：工资储蓄、零钱储蓄" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="showDlg = false">取消</el-button><el-button type="primary" @click="save" :loading="saving">保存</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { savingApi, familyApi } from '@/api'
import { useUserStore } from '@/stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'

const userStore = useUserStore()
const loading = ref(false); const allItems = ref([]); const query = ref({ memberId: '', keyword: '' })
const members = ref([]); const membersLoading = ref(false)
const showDlg = ref(false); const isEdit = ref(false); const saving = ref(false); const form = ref({ familyId: '', memberId: null, name: '' })

const filteredItems = computed(() => {
  let items = allItems.value
  if (query.value.memberId) items = items.filter(i => i.memberId === query.value.memberId)
  if (query.value.keyword) { const kw = query.value.keyword.toLowerCase(); items = items.filter(i => i.name.toLowerCase().includes(kw)) }
  return items
})

async function loadMembers() { if (!userStore.currentFamily) return; membersLoading.value = true; try { const res = await familyApi.getMembers(userStore.currentFamily.id); members.value = res.data || [] } finally { membersLoading.value = false } }
function getMemberName(userId) { const m = members.value.find(m => m.userId === userId); return m ? (m.userName || `用户#${userId}`) : `用户#${userId}` }
async function fetchData() { if (!userStore.currentFamily) return; loading.value = true; try { const res = await savingApi.getItems(userStore.currentFamily.id, 0); allItems.value = res.data || []; if (members.value.length === 0) loadMembers() } finally { loading.value = false } }
function showItemDialog(row) { isEdit.value = !!row; form.value = row ? { ...row } : { familyId: userStore.currentFamily?.id, memberId: null, name: '' }; if (!row && members.value.length === 0) loadMembers(); showDlg.value = true }
async function save() { saving.value = true; try { isEdit.value ? await savingApi.updateItem(form.value) : await savingApi.addItem(form.value); ElMessage.success('保存成功'); showDlg.value = false; fetchData() } finally { saving.value = false } }
async function deleteItem(row) { try { await ElMessageBox.confirm(`确认删除储蓄项「${row.name}」？`, '提示'); await savingApi.deleteItem(row.id); ElMessage.success('删除成功'); fetchData() } catch (e) { if (e !== 'cancel') throw e } }
watch(() => userStore.currentFamilyId, () => { members.value = []; query.value = { memberId: '', keyword: '' }; fetchData() })
onMounted(() => { fetchData() })
</script>

<style lang="scss" scoped>
.table-container { background: #fff; border-radius: 12px; padding: 20px; box-shadow: 0 1px 3px rgba(0,0,0,0.06); }
.empty-tip { padding: 80px 0; display: flex; justify-content: center; }
</style>
