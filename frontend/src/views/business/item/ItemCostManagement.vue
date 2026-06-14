<template>
  <div class="page-container">
    <h2 class="page-title">物件费用管理</h2>

    <div v-if="userStore.currentFamily">
      <div class="query-bar">
        <el-select v-model="query.addressId" placeholder="选择住址" clearable style="width:160px" @change="loadItems"><el-option v-for="a in addresses" :key="a.id" :label="a.name" :value="a.id" /></el-select>
        <el-select v-model="query.itemId" placeholder="选择物件" clearable style="width:160px"><el-option v-for="i in items" :key="i.id" :label="i.name" :value="i.id" /></el-select>
        <el-date-picker v-model="query.costDateStart" type="date" placeholder="日期起" value-format="YYYY-MM-DD" clearable style="width:150px" />
        <el-date-picker v-model="query.costDateEnd" type="date" placeholder="日期止" value-format="YYYY-MM-DD" clearable style="width:150px" />
        <el-button type="primary" @click="handleQuery">查询</el-button>
        <el-button type="success" @click="showDialog(null)">新增费用</el-button>
      </div>

      <div class="table-container">
        <el-table :data="list" v-loading="loading" border stripe show-summary :summary-method="summaryMethod">
          <el-table-column label="住址" width="120"><template #default="{ row }">{{ row.addressName || '-' }}</template></el-table-column>
          <el-table-column label="物件" width="120"><template #default="{ row }">{{ row.itemName || '-' }}</template></el-table-column>
          <el-table-column prop="costDate" label="日期" width="120" />
          <el-table-column prop="mileage" label="里程" width="100" align="right" />
          <el-table-column prop="cost" label="费用" width="120" align="right"><template #default="{ row }"><strong>{{ row.cost }}</strong></template></el-table-column>
          <el-table-column prop="remark" label="备注" min-width="120" show-overflow-tooltip />
          <el-table-column label="附件" width="70"><template #default="{ row }"><el-tag v-if="row._attachCount" size="small" type="info">{{ row._attachCount }}张</el-tag></template></el-table-column>
          <el-table-column label="操作" width="140" fixed="right"><template #default="{ row }"><el-button link type="primary" size="small" @click="showDialog(row)">编辑</el-button><el-button link type="danger" size="small" @click="handleDelete(row)">删除</el-button></template></el-table-column>
        </el-table>
        <div class="pagination-wrapper">
          <el-pagination v-model:currentPage="pageNum" v-model:pageSize="pageSize" :total="total" :page-sizes="[10, 20, 50]" layout="total, sizes, prev, pager, next" :hide-on-single-page="false" @current-change="fetchData" @size-change="fetchData" />
        </div>
      </div>
    </div>
    <div v-else class="empty-tip"><el-empty description="请先创建或切换到家庭" /></div>

    <el-dialog :close-on-click-modal="false" v-model="showDlg" title="物件费用" width="550px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="住址"><el-select v-model="formAddressId" placeholder="选择住址" style="width:100%" @change="loadItemsForForm"><el-option v-for="a in addresses" :key="a.id" :label="a.name" :value="a.id" /></el-select></el-form-item>
        <el-form-item label="物件"><el-select v-model="form.itemId" placeholder="选择物件" style="width:100%"><el-option v-for="i in formItems" :key="i.id" :label="i.name" :value="i.id" /></el-select></el-form-item>
        <el-form-item label="日期"><el-date-picker v-model="form.costDate" type="date" placeholder="选择日期" value-format="YYYY-MM-DD" style="width:100%" /></el-form-item>
        <el-form-item label="里程" v-if="showMileage"><el-input-number v-model="form.mileage" :min="0" :precision="2" style="width:200px" /><span style="color:#909399;font-size:12px;margin-left:8px;">交通工具时填写</span></el-form-item>
        <el-form-item label="费用"><el-input-number v-model="form.cost" :min="0" :precision="2" style="width:200px" /></el-form-item>
        <el-form-item label="备注"><el-input v-model="form.remark" type="textarea" :rows="2" /></el-form-item>
        <el-divider content-position="left" class="section-divider">附件（费用凭证图片）</el-divider>
        <el-form-item label="上传图片">
          <AttachmentUpload prefix="cost" :list="attachmentList" @remove="removeAttachment" />
        </el-form-item>
      </el-form>
      <template #footer><el-button @click="showDlg = false">取消</el-button><el-button type="primary" @click="save" :loading="saving">保存</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { itemApi, addressApi } from '@/api'
import { useUserStore } from '@/stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import AttachmentUpload from '@/components/AttachmentUpload.vue'

const userStore = useUserStore()
const loading = ref(false); const list = ref([]); const total = ref(0)
const pageNum = ref(1); const pageSize = ref(10)
const addresses = ref([]); const items = ref([]); const formItems = ref([]); const query = ref({ addressId: '', itemId: '', costDateStart: '', costDateEnd: '' })
const showDlg = ref(false); const isEdit = ref(false); const saving = ref(false); const formAddressId = ref(''); const form = ref({ itemId: '', costDate: '', mileage: null, cost: null, remark: '' }); const attachmentList = ref([])

const ITEM_TYPES = { HOME_APPLIANCE: 'home_appliance', VEHICLE: 'vehicle' }
const showMileage = computed(() => { const selected = formItems.value.find(i => i.id === form.value.itemId); return selected?.type === ITEM_TYPES.VEHICLE || selected?.type === '交通工具' })

async function loadAddresses() { if (!userStore.currentFamily) return; const res = await addressApi.listByFamily(userStore.currentFamily.id); addresses.value = res.data || [] }
async function loadItems() { if (!query.value.addressId) { items.value = []; return }; const res = await itemApi.listByAddress(query.value.addressId); items.value = res.data || [] }
async function loadItemsForForm() { if (!formAddressId.value) { formItems.value = []; return }; const res = await itemApi.listByAddress(formAddressId.value); formItems.value = res.data || [] }
function getAddressName(id) { return addresses.value.find(a => String(a.id) === String(id))?.name || '-' }
function getItemName(id) { return items.value.find(i => String(i.id) === String(id))?.name || formItems.value.find(i => i.id === id)?.name || '-' }
function summaryMethod({ columns, data }) {
  const sums = Array(columns.length).fill(null)
  columns.forEach((col, idx) => {
    if (idx === 0) { sums[idx] = '本页合计'; return }
    if (col.property === 'cost') {
      sums[idx] = data.reduce((s, r) => s + (Number(r.cost) || 0), 0).toFixed(2)
    }
  })
  return sums
}

async function fetchData() {
  if (!userStore.currentFamily) return; loading.value = true
  try { const params = { pageNum: pageNum.value, pageSize: pageSize.value, familyId: userStore.currentFamily.id, ...(query.value.addressId ? { addressId: query.value.addressId } : {}), ...(query.value.itemId ? { itemId: query.value.itemId } : {}), ...(query.value.costDateStart ? { costDateStart: query.value.costDateStart } : {}), ...(query.value.costDateEnd ? { costDateEnd: query.value.costDateEnd } : {}) }; const res = await itemApi.costPage(params); const d = res.data; list.value = (d.records || []).map(r => ({ ...r, _attachCount: (r.attachments || []).length })); total.value = Number(d.total) || 0; pageNum.value = Number(d.current) || 1; pageSize.value = Number(d.size) || 10 } finally { loading.value = false }
}

function handleQuery() { pageNum.value = 1; fetchData() }

function showDialog(row) { isEdit.value = !!row; form.value = row ? { ...row } : { itemId: '', costDate: '', mileage: null, cost: null, remark: '' }; formAddressId.value = (row && row.addressId) ? row.addressId : (query.value.addressId || ''); attachmentList.value = []; if (row) { attachmentList.value = (row.attachments || []).map(a => ({ ...a, _isNew: false })) }; showDlg.value = true; if (formAddressId.value) loadItemsForForm() }

async function save() {
  saving.value = true; try { let costId = form.value.id; if (isEdit.value) { await itemApi.updateCost(form.value) } else { const res = await itemApi.addCost(form.value); costId = res.data?.id || costId }; for (const att of attachmentList.value) { if (att._isNew && costId) { try { await itemApi.addCostAttachment(costId, { fileName: att.fileName, fileUrl: att.fileUrl, fileSize: att.fileSize }) } catch (e) {} } }; ElMessage.success('保存成功'); showDlg.value = false; fetchData() } finally { saving.value = false }
}

async function handleDelete(row) { try { await ElMessageBox.confirm('确认删除该费用记录？','提示'); await itemApi.deleteCost(row.id); ElMessage.success('删除成功'); fetchData() } catch(e) { if(e!=='cancel') throw e } }

function removeAttachment(idx) { const att = attachmentList.value[idx]; if (att && !att._isNew && att.id) { itemApi.deleteCostAttachment(att.id).catch(()=>{}) }; attachmentList.value.splice(idx, 1) }

watch(() => userStore.currentFamilyId, () => { query.value = { addressId: '', itemId: '', costDateStart: '', costDateEnd: '' }; pageNum.value = 1; loadAddresses(); fetchData() })
onMounted(() => { loadAddresses(); fetchData() })
</script>

<style lang="scss" scoped>
.table-container { background: #fff; border-radius: 12px; padding: 20px; box-shadow: 0 1px 3px rgba(0,0,0,0.06); }
.pagination-wrapper { margin-top: 20px; display: flex; justify-content: flex-end; position: relative; z-index: 1; }
.empty-tip { padding: 80px 0; display: flex; justify-content: center; }
:deep(.section-divider .el-divider__text) { font-weight: 600; color: #409eff; font-size: 14px; }
</style>
