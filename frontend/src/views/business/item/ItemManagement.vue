<template>
  <div class="page-container">
    <h2 class="page-title">物件管理</h2>

    <div v-if="userStore.currentFamily">
      <div class="query-bar">
        <el-input v-model="query.name" placeholder="物件名称" clearable style="width:140px" @keyup.enter="handleQuery" />
        <el-select v-model="query.type" placeholder="物件类型" clearable style="width:130px" :loading="dictLoading"><el-option v-for="d in itemTypes" :key="d.value" :label="d.label" :value="d.value" /></el-select>
        <el-input-number v-model="query.purchaseAmountStart" :min="0" :precision="2" placeholder="金额起" style="width:130px" />
        <el-input-number v-model="query.purchaseAmountEnd" :min="0" :precision="2" placeholder="金额止" style="width:130px" />
        <el-date-picker v-model="query.purchaseDateStart" type="date" placeholder="日期起" value-format="YYYY-MM-DD" clearable style="width:140px" />
        <el-date-picker v-model="query.purchaseDateEnd" type="date" placeholder="日期止" value-format="YYYY-MM-DD" clearable style="width:140px" />
        <el-select v-model="query.addressId" placeholder="选择住址" clearable style="width:140px"><el-option v-for="a in addresses" :key="a.id" :label="a.name" :value="a.id" /></el-select>
        <el-button type="primary" @click="handleQuery">查询</el-button>
        <el-button type="success" @click="showDialog(null)">新增物件</el-button>
      </div>

      <div class="table-container">
        <el-table :data="list" v-loading="loading" border stripe>
          <el-table-column prop="name" label="名称" min-width="120" />
          <el-table-column label="住址" width="120"><template #default="{ row }">{{ row.addressName || '-' }}</template></el-table-column>
          <el-table-column label="类型" width="120"><template #default="{ row }">{{ row.typeText || row.type || '-' }}</template></el-table-column>
          <el-table-column prop="purchaseAmount" label="购买金额" width="120" align="right" />
          <el-table-column prop="purchaseDate" label="购买日期" width="120" />
          <el-table-column prop="deactivationDate" label="停用时间" width="120"><template #default="{ row }"><span v-if="row.deactivationDate" style="color:#f56c6c;">{{ row.deactivationDate }}</span><span v-else style="color:#909399;">-</span></template></el-table-column>
          <el-table-column prop="residualValue" label="残值" width="100" align="right"><template #default="{ row }">{{ row.residualValue != null ? '¥' + row.residualValue : '-' }}</template></el-table-column>
          <el-table-column prop="remark" label="备注" min-width="120" show-overflow-tooltip />
          <el-table-column label="附件" width="70"><template #default="{ row }"><el-tag v-if="row._attachCount" size="small" type="info">{{ row._attachCount }}张</el-tag></template></el-table-column>
          <el-table-column label="操作" width="140" fixed="right"><template #default="{ row }"><el-button link type="primary" size="small" @click="showDialog(row)">编辑</el-button><el-button link type="danger" size="small" @click="handleDelete(row)">删除</el-button></template></el-table-column>
        </el-table>
        <el-pagination v-model:currentPage="pageNum" v-model:pageSize="pageSize" :total="total" :page-sizes="[10, 20, 50]" layout="total, sizes, prev, pager, next" :hide-on-single-page="false" class="pagination" @current-change="fetchData" @size-change="fetchData" />
      </div>
    </div>
    <div v-else class="empty-tip"><el-empty description="请先创建或切换到家庭" /></div>

    <el-dialog v-model="showDlg" :title="isEdit ? '编辑物件' : '新增物件'" width="600px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="住址"><el-select v-model="form.addressId" placeholder="选择住址" style="width:100%"><el-option v-for="a in addresses" :key="a.id" :label="a.name" :value="a.id" /></el-select></el-form-item>
        <el-form-item label="名称"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="类型"><el-select v-model="form.type" placeholder="选择类型" style="width:100%" :loading="dictLoading"><el-option v-for="d in itemTypes" :key="d.value" :label="d.label" :value="d.value" /></el-select></el-form-item>
        <el-form-item label="购买金额"><el-input-number v-model="form.purchaseAmount" :min="0" :precision="2" style="width:200px" /></el-form-item>
        <el-form-item label="购买日期"><el-date-picker v-model="form.purchaseDate" type="date" placeholder="选择日期" value-format="YYYY-MM-DD" style="width:100%" /></el-form-item>
        <el-form-item label="停用时间"><el-date-picker v-model="form.deactivationDate" type="date" placeholder="选择日期" value-format="YYYY-MM-DD" style="width:100%" /><span style="color:#909399;font-size:12px;margin-left:8px;">填写后表示物件已停用</span></el-form-item>
        <el-form-item label="残值"><el-input-number v-model="form.residualValue" :min="0" :precision="2" style="width:200px" /><span style="color:#909399;font-size:12px;margin-left:8px;">报废或二手回收后拿回的钱</span></el-form-item>
        <el-form-item label="备注"><el-input v-model="form.remark" type="textarea" :rows="2" /></el-form-item>
        <el-divider content-position="left" class="section-divider">附件（物件图片）</el-divider>
        <el-form-item label="上传图片">
          <AttachmentUpload prefix="item" :list="attachmentList" @remove="removeAttachment" />
        </el-form-item>
      </el-form>
      <template #footer><el-button @click="showDlg = false">取消</el-button><el-button type="primary" @click="save" :loading="saving">保存</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { itemApi, addressApi, dictApi } from '@/api'
import { useUserStore } from '@/stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import AttachmentUpload from '@/components/AttachmentUpload.vue'

const userStore = useUserStore()
const loading = ref(false); const list = ref([]); const total = ref(0)
const pageNum = ref(1); const pageSize = ref(10); const addresses = ref([]); const query = ref({ addressId: '', name: '', type: '', purchaseAmountStart: null, purchaseAmountEnd: null, purchaseDateStart: '', purchaseDateEnd: '' })
const showDlg = ref(false); const isEdit = ref(false); const saving = ref(false)
const form = ref({ familyId: '', addressId: '', name: '', type: '', purchaseAmount: null, purchaseDate: '', deactivationDate: '', residualValue: null, remark: '' }); const attachmentList = ref([])
const itemTypes = ref([]); const dictLoading = ref(false)

async function loadAddresses() { if (!userStore.currentFamily) return; const res = await addressApi.listByFamily(userStore.currentFamily.id); addresses.value = res.data || [] }
function getAddressName(id) { return addresses.value.find(a => String(a.id) === String(id))?.name || '-' }
async function loadItemTypes() { dictLoading.value = true; try { const res = await dictApi.getDataByCode('item_type'); itemTypes.value = res.data || [] } finally { dictLoading.value = false } }

async function fetchData() {
  if (!userStore.currentFamily) return; loading.value = true
  try { const params = { pageNum: pageNum.value, pageSize: pageSize.value, familyId: userStore.currentFamily.id }; if (query.value.addressId) params.addressId = query.value.addressId; if (query.value.name) params.name = query.value.name; if (query.value.type) params.type = query.value.type; if (query.value.purchaseAmountStart != null) params.purchaseAmountStart = query.value.purchaseAmountStart; if (query.value.purchaseAmountEnd != null) params.purchaseAmountEnd = query.value.purchaseAmountEnd; if (query.value.purchaseDateStart) params.purchaseDateStart = query.value.purchaseDateStart; if (query.value.purchaseDateEnd) params.purchaseDateEnd = query.value.purchaseDateEnd; const res = await itemApi.page(params); const d = res.data; list.value = (d.records || []).map(r => ({ ...r, _attachCount: (r.attachments || []).length })); total.value = Number(d.total) || 0; pageNum.value = Number(d.current) || 1; pageSize.value = Number(d.size) || 10 } finally { loading.value = false }
}

function handleQuery() { pageNum.value = 1; fetchData() }

function showDialog(row) { isEdit.value = !!row; form.value = row ? { ...row } : { familyId: userStore.currentFamily?.id, addressId: '', name: '', type: '', purchaseAmount: null, purchaseDate: '', deactivationDate: '', residualValue: null, remark: '' }; attachmentList.value = []; if (row) { attachmentList.value = (row.attachments || []).map(a => ({ ...a, _isNew: false })) }; showDlg.value = true }

async function save() {
  saving.value = true; try { form.value.familyId = userStore.currentFamily?.id; let itemId = form.value.id; if (isEdit.value) { await itemApi.update(form.value) } else { const res = await itemApi.add(form.value); itemId = res.data?.id || itemId }; for (const att of attachmentList.value) { if (att._isNew && itemId) { try { await itemApi.addAttachment(itemId, { fileName: att.fileName, fileUrl: att.fileUrl, fileSize: att.fileSize }) } catch (e) {} } }; ElMessage.success('保存成功'); showDlg.value = false; fetchData() } finally { saving.value = false }
}

async function handleDelete(row) { try { await ElMessageBox.confirm(`确认删除物件「${row.name}」？`, '提示'); await itemApi.delete(row.id); ElMessage.success('删除成功'); fetchData() } catch (e) { if (e !== 'cancel') throw e } }

function removeAttachment(idx) { const att = attachmentList.value[idx]; if (att && !att._isNew && att.id) { itemApi.deleteAttachment(att.id).catch(()=>{}) }; attachmentList.value.splice(idx, 1) }

watch(() => userStore.currentFamilyId, () => { query.value = { addressId: '', name: '', type: '', purchaseAmountStart: null, purchaseAmountEnd: null, purchaseDateStart: '', purchaseDateEnd: '' }; pageNum.value = 1; loadAddresses(); fetchData() })
onMounted(() => { loadAddresses(); fetchData(); loadItemTypes() })
</script>

<style lang="scss" scoped>
.table-container { background: #fff; border-radius: 12px; padding: 20px; box-shadow: 0 1px 3px rgba(0,0,0,0.06); }
.pagination { margin-top: 20px; display: flex; justify-content: flex-end; }
.empty-tip { padding: 80px 0; display: flex; justify-content: center; }
:deep(.section-divider .el-divider__text) { font-weight: 600; color: #409eff; font-size: 14px; }
</style>
