<template>
  <div class="page-container">
    <h2 class="page-title">家庭储蓄</h2>

    <div v-if="userStore.currentFamily">
      <div class="query-bar">
        <el-date-picker v-model="query.savingDateStart" type="date" placeholder="日期起" value-format="YYYY-MM-DD" clearable style="width:150px" />
        <el-date-picker v-model="query.savingDateEnd" type="date" placeholder="日期止" value-format="YYYY-MM-DD" clearable style="width:150px" />
        <el-button type="primary" @click="fetchData">查询</el-button>
        <el-button type="success" @click="openCreateDialog">创建储蓄</el-button>
      </div>

      <div class="table-container">
        <el-table :data="list" v-loading="loading" border stripe>
          <el-table-column label="日期" width="130"><template #default="{ row }">{{ row.savingDate || '-' }}</template></el-table-column>
          <el-table-column v-for="m in members" :key="m.userId" :label="getMemberDisplayName(m.userId)" width="150" align="right">
            <template #default="{ row }">
              {{ getMemberTotal(row, m.userId) }}
            </template>
          </el-table-column>
          <el-table-column prop="totalAmount" label="合计" width="150" align="right"><template #default="{ row }"><strong style="color:#409eff;font-size:16px;">{{ row.totalAmount || 0 }}</strong></template></el-table-column>
          <el-table-column label="操作" width="240"><template #default="{ row }"><el-button link type="primary" size="small" @click="openEditDialog(row)">编辑</el-button><el-button link type="primary" size="small" @click="openDetailDialog(row)">明细</el-button><el-button link type="danger" size="small" @click="handleDelete(row)">删除</el-button></template></el-table-column>
        </el-table>
        <el-pagination v-model:currentPage="pageNum" v-model:pageSize="pageSize" :total="total" layout="prev, pager, next, total" class="pagination" @current-change="fetchData" />
      </div>
    </div>
    <div v-else class="empty-tip"><el-empty description="请先创建或切换到家庭" /></div>

    <!-- 创建/编辑保存对话框（合并创建+明细） -->
    <el-dialog :close-on-click-modal="false" v-model="showSaveDialog" :title="isEdit ? '编辑储蓄' : '创建储蓄'" width="700px">
      <el-form :model="saveForm" label-width="80px">
        <el-form-item label="日期" required>
          <el-date-picker v-model="saveForm.savingDate" type="date" placeholder="选择日期" value-format="YYYY-MM-DD" style="width:100%" />
        </el-form-item>
        <el-divider content-position="left" class="section-divider">成员储蓄明细</el-divider>
        <div class="member-saving-section" v-for="(records, memberId) in groupedSaveRecords" :key="memberId">
          <h4><el-icon><User /></el-icon> {{ getMemberDisplayName(memberId) }}</h4>
          <div class="saving-item-row" v-for="(record) in records" :key="record.savingItemId">
            <span class="item-name">{{ getSavingItemDisplayName(record.savingItemId) }}</span>
            <el-input-number v-model="record.amount" :min="0" :precision="2" size="small" style="width:180px" />
          </div>
        </div>
        <div class="saving-total">合计：<strong>{{ saveDetailTotal }}</strong></div>
      </el-form>
      <template #footer>
        <el-button @click="showSaveDialog = false">取消</el-button>
        <el-button type="primary" @click="saveSaving" :loading="saving">保存</el-button>
      </template>
    </el-dialog>

    <!-- 明细查看对话框（只读） -->
    <el-dialog :close-on-click-modal="false" v-model="showDetailDialog" title="储蓄明细" width="650px">
      <div class="member-saving-section" v-for="(records, memberId) in groupedDetailRecords" :key="memberId">
        <h4><el-icon><User /></el-icon> {{ getMemberDisplayName(memberId) }}</h4>
        <div class="saving-item-row" v-for="(record) in records" :key="record.savingItemId">
          <span class="item-name">{{ getSavingItemDisplayName(record.savingItemId) }}</span>
          <span class="item-amount">{{ record.amount || 0 }}</span>
        </div>
      </div>
      <div class="saving-total">合计：<strong>{{ detailTotal }}</strong></div>
      <template #footer><el-button @click="showDetailDialog = false">关闭</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { savingApi, familyApi } from '@/api'
import { useUserStore } from '@/stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, User } from '@element-plus/icons-vue'

const userStore = useUserStore()
const loading = ref(false); const list = ref([]); const total = ref(0); const pageNum = ref(1); const pageSize = ref(10); const query = ref({ savingDateStart: '', savingDateEnd: '' })
const savingItems = ref([]); const memberNames = ref({})

// 创建/编辑
const showSaveDialog = ref(false); const saving = ref(false); const isEdit = ref(false); const saveForm = ref({ savingDate: '' })
const saveRecords = ref([]) // 编辑时已有的记录

// 明细查看
const showDetailDialog = ref(false); const currentSaving = ref(null); const currentRecords = ref([])

const members = ref([])

async function loadAllSavingItems() { if (!userStore.currentFamily) return; const res = await savingApi.getItems(userStore.currentFamily.id, 0); savingItems.value = res.data || [] }
async function loadMemberNames() { if (!userStore.currentFamily) return; try { const res = await familyApi.getMembers(userStore.currentFamily.id); members.value = res.data || []; const map = {}; (res.data || []).forEach(m => { map[m.userId] = m.userName || `用户#${m.userId}` }); memberNames.value = map } catch(e) {} }
function getMemberDisplayName(userId) { return memberNames.value[userId] || `用户#${userId}` }
function getSavingItemDisplayName(savingItemId) { const item = savingItems.value.find(i => i.id === savingItemId); return item?.name || `项#${savingItemId}` }

async function fetchData() {
  if (!userStore.currentFamily) return; loading.value = true
  try { const params = { pageNum: pageNum.value, pageSize: pageSize.value, familyId: userStore.currentFamily.id }; if (query.value.savingDateStart) params.savingDateStart = query.value.savingDateStart; if (query.value.savingDateEnd) params.savingDateEnd = query.value.savingDateEnd; const res = await savingApi.page(params); const d = res.data; list.value = (d.records || []).map(r => ({ ...r, _records: r.records || [] })); total.value = Number(d.total) || 0 } finally { loading.value = false }
}

function getMemberTotal(row, memberId) {
  const memberRecords = (row._records || []).filter(r => String(r.memberId) === String(memberId))
  if (memberRecords.length === 0) return 0
  return memberRecords.reduce((s, r) => s + (Number(r.amount) || 0), 0)
}

// 构建所有成员的储蓄项记录列表
function buildAllRecords() {
  const all = []
  for (const m of members.value) {
    const items = savingItems.value.filter(i => String(i.memberId) === String(m.userId))
    items.forEach(item => {
      all.push({ savingItemId: item.id, memberId: m.userId, amount: null })
    })
  }
  return all
}

// 打开创建对话框
async function openCreateDialog() {
  isEdit.value = false; currentSaving.value = null
  saveForm.value = { savingDate: '' }
  saveRecords.value = buildAllRecords()
  showSaveDialog.value = true
}

// 打开编辑对话框
async function openEditDialog(row) {
  isEdit.value = true; currentSaving.value = row
  saveForm.value = { savingDate: row.savingDate || '' }
  saveRecords.value = buildAllRecords()
  const existRecords = row.records || []; existRecords.forEach(er => { const match = saveRecords.value.find(r => r.savingItemId === er.savingItemId && r.memberId === er.memberId); if (match) match.amount = Number(er.amount) || null })
  showSaveDialog.value = true
}

// 保存（创建或编辑）
async function saveSaving() {
  if (!saveForm.value.savingDate) { ElMessage.warning('请选择日期'); return }
  saving.value = true
  try {
    const data = { ...saveForm.value, familyId: userStore.currentFamily?.id }
    let savingId = currentSaving.value?.id
    if (isEdit.value) {
      await savingApi.update({ id: savingId, savingDate: data.savingDate })
    } else {
      const res = await savingApi.create(data)
      savingId = res.data?.id
    }
    const records = saveRecords.value.filter(r => r.amount != null && Number(r.amount) > 0)
    if (savingId && records.length > 0) {
      await savingApi.batchSaveRecords(savingId, records)
    }
    ElMessage.success('保存成功')
    showSaveDialog.value = false
    fetchData()
  } finally { saving.value = false }
}

// 打开明细查看
async function openDetailDialog(saving) {
  currentSaving.value = saving
  currentRecords.value = buildAllRecords()
  const existRecords = saving.records || []; existRecords.forEach(er => { const match = currentRecords.value.find(r => r.savingItemId === er.savingItemId && r.memberId === er.memberId); if (match) match.amount = Number(er.amount) || null })
  showDetailDialog.value = true
}

async function handleDelete(row) { try { await ElMessageBox.confirm('确认删除该储蓄记录？','提示'); await savingApi.delete(row.id); ElMessage.success('删除成功'); fetchData() } catch(e) { if(e!=='cancel') throw e } }

const groupedSaveRecords = computed(() => { const groups = {}; saveRecords.value.forEach(r => { const key = r.memberId; if (!groups[key]) groups[key] = []; groups[key].push(r) }); return groups })
const groupedDetailRecords = computed(() => { const groups = {}; currentRecords.value.forEach(r => { const key = r.memberId; if (!groups[key]) groups[key] = []; groups[key].push(r) }); return groups })
const saveDetailTotal = computed(() => saveRecords.value.reduce((s, r) => s + (Number(r.amount) || 0), 0).toFixed(2))
const detailTotal = computed(() => currentRecords.value.reduce((s, r) => s + (Number(r.amount) || 0), 0).toFixed(2))

watch(() => userStore.currentFamilyId, () => { loadAllSavingItems(); loadMemberNames(); fetchData() })
onMounted(() => { loadAllSavingItems(); loadMemberNames(); fetchData() })
</script>

<style lang="scss" scoped>
.table-container { background: #fff; border-radius: 12px; padding: 20px; box-shadow: 0 1px 3px rgba(0,0,0,0.06); }
.empty-tip { padding: 80px 0; display: flex; justify-content: center; }
.member-saving-section { margin-bottom: 16px; padding: 16px; background: #f8f9fc; border-radius: 10px; border: 1px solid #ebeef5;
  h4 { margin: 0 0 12px; font-size: 14px; color: #303133; display: flex; align-items: center; gap: 6px; font-weight: 600; }
}
.saving-item-row { display: flex; align-items: center; gap: 12px; margin-bottom: 8px; .item-name { min-width: 140px; font-size: 14px; color: #606266; font-weight: 500; } .item-amount { font-size: 14px; color: #409eff; font-weight: 600; } }
.saving-total { text-align: right; padding: 16px 0 0; font-size: 16px; color: #303133; border-top: 1px solid #ebeef5; strong { color: #409eff; font-size: 20px; } }
:deep(.section-divider .el-divider__text) { font-weight: 600; color: #409eff; font-size: 13px; }
</style>
