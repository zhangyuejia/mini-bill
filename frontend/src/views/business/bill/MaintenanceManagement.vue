<template>
  <div class="page-container">
    <h2 class="page-title">维护费用</h2>

    <div v-if="userStore.currentFamily">
      <div class="query-bar">
        <el-select v-model="query.addressId" placeholder="选择住址" clearable style="width:160px"><el-option v-for="a in addresses" :key="a.id" :label="a.name" :value="a.id" /></el-select>
        <el-select v-model="query.type" placeholder="维护类型" clearable style="width:150px"><el-option v-for="t in maintenanceTypes" :key="t.value" :label="t.label" :value="t.value" /></el-select>
        <el-date-picker v-model="query.costDateStart" type="date" placeholder="日期起" value-format="YYYY-MM-DD" clearable style="width:150px" />
        <el-date-picker v-model="query.costDateEnd" type="date" placeholder="日期止" value-format="YYYY-MM-DD" clearable style="width:150px" />
        <el-button type="primary" @click="handleQuery">查询</el-button>
        <el-button type="success" @click="showDialog(null)">新增费用</el-button>
      </div>

      <div class="table-container">
        <el-table :data="list" v-loading="loading" border stripe show-summary :summary-method="summaryMethod">
          <el-table-column label="住址" width="150"><template #default="{ row }">{{ row.addressName || '-' }}</template></el-table-column>
          <el-table-column label="类型" width="100"><template #default="{ row }">{{ row.typeText || '-' }}</template></el-table-column>
          <el-table-column prop="costDate" label="日期" width="130" />
          <el-table-column prop="cost" label="费用" width="130" align="right"><template #default="{ row }"><strong>{{ row.cost }}</strong></template></el-table-column>
          <el-table-column prop="remark" label="备注" min-width="150" show-overflow-tooltip />
          <el-table-column label="附件" width="80"><template #default="{ row }"><el-tag v-if="row._attachCount" size="small" type="info">{{ row._attachCount }}张</el-tag></template></el-table-column>
          <el-table-column label="操作" width="140" fixed="right"><template #default="{ row }"><el-button link type="primary" size="small" @click="showDialog(row)">编辑</el-button><el-button link type="danger" size="small" @click="handleDelete(row)">删除</el-button></template></el-table-column>
        </el-table>
        <div class="pagination-wrapper">
          <el-pagination v-model:currentPage="pageNum" v-model:pageSize="pageSize" :total="total" :page-sizes="[10, 20, 50]" layout="total, sizes, prev, pager, next" :hide-on-single-page="false" @current-change="fetchData" @size-change="fetchData" />
        </div>
      </div>
    </div>
    <div v-else class="empty-tip"><el-empty description="请先创建或切换到家庭" /></div>

    <el-dialog :close-on-click-modal="false" v-model="showDlg" :title="isEdit ? '编辑维护费用' : '新增维护费用'" width="550px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="住址" required><el-select v-model="form.addressId" placeholder="选择住址" style="width:100%"><el-option v-for="a in addresses" :key="a.id" :label="a.name" :value="a.id" /></el-select></el-form-item>
        <el-form-item label="类型"><el-select v-model="form.type" placeholder="选择类型" style="width:100%"><el-option v-for="t in maintenanceTypes" :key="t.value" :label="t.label" :value="t.value" /></el-select></el-form-item>
        <el-form-item label="日期" required><el-date-picker v-model="form.costDate" type="date" placeholder="选择日期" value-format="YYYY-MM-DD" style="width:100%" /></el-form-item>
        <el-form-item label="费用" required><el-input-number v-model="form.cost" :min="0" :precision="2" style="width:200px" /></el-form-item>
        <el-form-item label="备注"><el-input v-model="form.remark" type="textarea" :rows="2" /></el-form-item>
        <el-divider content-position="left" class="section-divider">附件（费用凭证）</el-divider>
        <el-form-item label="上传图片">
          <AttachmentUpload prefix="maintenance" :list="attachmentList" @remove="removeAttachment" />
        </el-form-item>
      </el-form>
      <template #footer><el-button @click="showDlg = false">取消</el-button><el-button type="primary" @click="save" :loading="saving">保存</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { maintenanceApi, addressApi, dictApi } from '@/api'
import { useUserStore } from '@/stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import AttachmentUpload from '@/components/AttachmentUpload.vue'

const userStore = useUserStore()
const loading = ref(false); const list = ref([]); const total = ref(0)
const pageNum = ref(1); const pageSize = ref(10); const addresses = ref([]); const maintenanceTypes = ref([]); const query = ref({ addressId: '', type: '', costDateStart: '', costDateEnd: '' })
const showDlg = ref(false); const isEdit = ref(false); const saving = ref(false); const form = ref({ addressId: '', type: '', costDate: '', cost: null, remark: '' }); const attachmentList = ref([])

async function loadAddresses() { if (!userStore.currentFamily) return; const res = await addressApi.listByFamily(userStore.currentFamily.id); addresses.value = res.data || [] }
async function loadMaintenanceTypes() { try { const res = await dictApi.getDataByCode('maintenance_type'); maintenanceTypes.value = res.data || [] } catch(e) {} }

function summaryMethod({ columns, data }) {
  const sums = Array(columns.length).fill(null)
  columns.forEach((col, idx) => {
    if (idx === 0) { sums[idx] = '本页合计'; return }
    if (col.property === 'cost') { sums[idx] = data.reduce((s, r) => s + (Number(r.cost) || 0), 0).toFixed(2) }
  })
  return sums
}

async function fetchData() {
  if (!userStore.currentFamily) return; loading.value = true
  try { const params = { pageNum: pageNum.value, pageSize: pageSize.value, familyId: userStore.currentFamily.id, ...(query.value.addressId ? { addressId: query.value.addressId } : {}), ...(query.value.type ? { type: query.value.type } : {}), ...(query.value.costDateStart ? { costDateStart: query.value.costDateStart } : {}), ...(query.value.costDateEnd ? { costDateEnd: query.value.costDateEnd } : {}) }; const res = await maintenanceApi.page(params); const d = res.data; list.value = (d.records || []).map(r => ({ ...r, _attachCount: (r.attachments || []).length })); total.value = Number(d.total) || 0; pageNum.value = Number(d.current) || 1; pageSize.value = Number(d.size) || 10 } finally { loading.value = false }
}

function handleQuery() { pageNum.value = 1; fetchData() }

function showDialog(row) { isEdit.value = !!row; form.value = row ? { ...row } : { addressId: query.value.addressId || '', type: '', costDate: '', cost: null, remark: '' }; attachmentList.value = []; if (row) { attachmentList.value = (row.attachments || []).map(a => ({ ...a, _isNew: false })) }; showDlg.value = true }

async function save() {
  if (!form.value.addressId) { ElMessage.warning('请选择住址'); return }
  if (!form.value.costDate) { ElMessage.warning('请选择日期'); return }
  saving.value = true; try { let id = form.value.id; if (isEdit.value) { await maintenanceApi.update(form.value) } else { const res = await maintenanceApi.add(form.value); id = res.data?.id || id }; for (const att of attachmentList.value) { if (att._isNew && id) { try { await maintenanceApi.addAttachment(id, { fileName: att.fileName, fileUrl: att.fileUrl, fileSize: att.fileSize }) } catch (e) {} } }; ElMessage.success('保存成功'); showDlg.value = false; fetchData() } finally { saving.value = false }
}

async function handleDelete(row) { try { await ElMessageBox.confirm('确认删除该维护费用记录？','提示'); await maintenanceApi.delete(row.id); ElMessage.success('删除成功'); fetchData() } catch(e) { if(e!=='cancel') throw e } }

function removeAttachment(idx) { const att = attachmentList.value[idx]; if (att && !att._isNew && att.id) { maintenanceApi.deleteAttachment(att.id).catch(()=>{}) }; attachmentList.value.splice(idx, 1) }

watch(() => userStore.currentFamilyId, () => { query.value = { addressId: '', type: '', costDateStart: '', costDateEnd: '' }; pageNum.value = 1; loadAddresses(); fetchData() })
onMounted(() => { loadAddresses(); loadMaintenanceTypes(); fetchData() })
</script>

<style lang="scss" scoped>
.table-container { background: #fff; border-radius: 12px; padding: 20px; box-shadow: 0 1px 3px rgba(0,0,0,0.06); }
.pagination-wrapper { margin-top: 20px; display: flex; justify-content: flex-end; position: relative; z-index: 1; }
.empty-tip { padding: 80px 0; display: flex; justify-content: center; }
:deep(.section-divider .el-divider__text) { font-weight: 600; color: #409eff; font-size: 14px; }
</style>
