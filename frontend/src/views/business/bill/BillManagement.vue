<template>
  <div class="page-container">
    <h2 class="page-title">房租水电管理</h2>

    <div v-if="userStore.currentFamily">
      <div class="query-bar">
        <el-select v-model="query.addressId" placeholder="选择住址" clearable style="width:160px"><el-option v-for="a in addresses" :key="a.id" :label="a.name" :value="a.id" /></el-select>
        <el-date-picker v-model="query.periodStart" type="month" placeholder="账期起" value-format="YYYYMM" clearable style="width:150px" />
        <el-date-picker v-model="query.periodEnd" type="month" placeholder="账期止" value-format="YYYYMM" clearable style="width:150px" />
        <el-button type="primary" @click="handleQuery">查询</el-button>
        <el-button type="success" @click="showDialog(null)">新增账单</el-button>
      </div>

      <div class="table-container">
        <el-table :data="list" v-loading="loading" border stripe show-summary :summary-method="summaryMethod">
          <el-table-column label="住址" width="110"><template #default="{ row }">{{ getAddressName(row.addressId) }}</template></el-table-column>
          <el-table-column label="账期" width="110"><template #default="{ row }">{{ formatPeriod(row.period) }}</template></el-table-column>
          <el-table-column prop="rent" label="房租" width="100" align="right" />
          <el-table-column prop="waterAmount" label="水费" width="100" align="right" />
          <el-table-column prop="electricAmount" label="电费" width="100" align="right" />
          <el-table-column prop="otherFee" label="其他" width="100" align="right" />
          <el-table-column prop="managementFee" label="管理费" width="100" align="right" />
          <el-table-column prop="roundingAmount" label="抹零" width="100" align="right" />
          <el-table-column prop="totalAmount" label="合计" width="100" align="right"><template #default="{ row }"><strong style="color:#f56c6c">{{ row.totalAmount||0 }}</strong></template></el-table-column>
          <el-table-column prop="remark" label="备注" min-width="120" show-overflow-tooltip />
          <el-table-column label="附件" width="90"><template #default="{ row }"><template v-if="row._attachCount"><el-image :src="row.attachments[0].fileUrl" :preview-src-list="row.attachments.map(a=>a.fileUrl)" fit="cover" style="width:32px;height:32px;border-radius:4px;cursor:pointer" preview-teleported /><span style="font-size:11px;color:#909399;margin-left:4px;">{{ row._attachCount }}张</span></template></template></el-table-column>
          <el-table-column label="操作" width="140" fixed="right">
            <template #default="{ row }"><el-button link type="primary" size="small" @click="showDialog(row)">编辑</el-button><el-button link type="danger" size="small" @click="handleDelete(row)">删除</el-button></template>
          </el-table-column>
        </el-table>
        <div class="pagination-wrapper">
          <el-pagination v-model:currentPage="pageNum" v-model:pageSize="pageSize" :total="total" :page-sizes="[10, 20, 50]" layout="total, sizes, prev, pager, next" :hide-on-single-page="false" @current-change="fetchData" @size-change="fetchData" />
        </div>
      </div>
    </div>
    <div v-else class="empty-tip"><el-empty description="请先创建或切换到家庭" /></div>

    <el-dialog :close-on-click-modal="false" v-model="showDlg" :title="isEdit ? '编辑账单' : '新增账单'" width="800px" class="bill-dialog">
      <el-form ref="formRef" :model="form" label-width="110px">
        <el-row :gutter="16"><el-col :span="12"><el-form-item label="住址" required><el-select v-model="form.addressId" placeholder="选择住址" style="width:100%" @change="onAddressChange"><el-option v-for="a in addresses" :key="a.id" :label="a.name" :value="a.id" /></el-select></el-form-item></el-col><el-col :span="12"><el-form-item label="账期" required><el-date-picker v-model="form.period" type="month" placeholder="选择月份" value-format="YYYYMM" style="width:100%" /></el-form-item></el-col></el-row>
        <el-divider content-position="left" class="section-divider">房租</el-divider>
        <el-form-item label="房租"><el-input-number v-model="form.rent" :min="0" :precision="2" style="width:200px" /></el-form-item>
        <el-divider content-position="left" class="section-divider">电费</el-divider>
        <el-row :gutter="16"><el-col :span="8"><el-form-item label="上月表底"><el-input-number v-model="form.electricPrevReading" :min="0" :precision="2" style="width:100%" /></el-form-item></el-col><el-col :span="8"><el-form-item label="本月表底"><el-input-number v-model="form.electricCurrReading" :min="0" :precision="2" style="width:100%" /></el-form-item></el-col><el-col :span="8"><el-form-item label="单价"><el-input-number v-model="form.electricUnitPrice" :min="0" :precision="2" style="width:100%" /></el-form-item></el-col></el-row>
        <el-form-item label="金额"><el-input-number v-model="form.electricAmount" :min="0" :precision="2" style="width:200px" /><span class="calc-hint">= {{ calculatedElectric }}</span></el-form-item>
        <el-divider content-position="left" class="section-divider">水费</el-divider>
        <el-row :gutter="16"><el-col :span="8"><el-form-item label="上月表底"><el-input-number v-model="form.waterPrevReading" :min="0" :precision="2" style="width:100%" /></el-form-item></el-col><el-col :span="8"><el-form-item label="本月表底"><el-input-number v-model="form.waterCurrReading" :min="0" :precision="2" style="width:100%" /></el-form-item></el-col><el-col :span="8"><el-form-item label="单价"><el-input-number v-model="form.waterUnitPrice" :min="0" :precision="2" style="width:100%" /></el-form-item></el-col></el-row>
        <el-form-item label="金额"><el-input-number v-model="form.waterAmount" :min="0" :precision="2" style="width:200px" /><span class="calc-hint">= {{ calculatedWater }}</span></el-form-item>
        <el-divider content-position="left" class="section-divider">管理费</el-divider>
        <el-form-item label="管理费"><el-input-number v-model="form.managementFee" :min="0" :precision="2" style="width:200px" /></el-form-item>
        <el-divider content-position="left" class="section-divider">其他</el-divider>
        <el-form-item label="其他费用"><el-input-number v-model="form.otherFee" :min="0" :precision="2" style="width:200px" /></el-form-item>
        <el-form-item label="抹零"><el-input-number v-model="form.roundingAmount" :precision="2" style="width:200px" /></el-form-item>
        <el-form-item label="合计"><strong style="color:#f56c6c;font-size:18px">{{ calculatedTotal }}</strong></el-form-item>
        <el-form-item label="备注"><el-input v-model="form.remark" type="textarea" :rows="2" /></el-form-item>
        <el-divider content-position="left" class="section-divider">附件（水电单图片）</el-divider>
        <el-form-item label="上传图片">
          <AttachmentUpload prefix="bill" :list="attachmentList" @remove="removeAttachment" />
        </el-form-item>
      </el-form>
      <template #footer><el-button @click="showDlg = false">取消</el-button><el-button type="primary" @click="save" :loading="saving">保存</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { billApi, addressApi } from '@/api'
import { useUserStore } from '@/stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import AttachmentUpload from '@/components/AttachmentUpload.vue'

const userStore = useUserStore()
const loading = ref(false); const list = ref([]); const total = ref(0)
const pageNum = ref(1); const pageSize = ref(10); const addresses = ref([]); const query = ref({ addressId: '', periodStart: '', periodEnd: '' })
const showDlg = ref(false); const isEdit = ref(false); const saving = ref(false); const form = ref(createEmptyForm()); const formRef = ref(null); const attachmentList = ref([])

function createEmptyForm() { return { familyId: userStore.currentFamily?.id, addressId: '', period: '', rent: null, managementFee: null, otherFee: null, totalAmount: 0, remark: '', waterPrevReading: null, waterCurrReading: null, waterUnitPrice: null, waterAmount: null, electricPrevReading: null, electricCurrReading: null, electricUnitPrice: null, electricAmount: null, roundingAmount: null } }
const calculatedWater = computed(() => { const { waterCurrReading, waterPrevReading, waterUnitPrice } = form.value; return (waterCurrReading && waterPrevReading && waterUnitPrice) ? ((waterCurrReading - waterPrevReading) * waterUnitPrice).toFixed(2) : '-' })
const calculatedElectric = computed(() => { const { electricCurrReading, electricPrevReading, electricUnitPrice } = form.value; return (electricCurrReading && electricPrevReading && electricUnitPrice) ? ((electricCurrReading - electricPrevReading) * electricUnitPrice).toFixed(2) : '-' })
const calculatedTotal = computed(() => [form.value.rent, form.value.waterAmount, form.value.electricAmount, form.value.managementFee, form.value.otherFee, form.value.roundingAmount].reduce((s, v) => s + (Number(v) || 0), 0).toFixed(2))

function formatPeriod(p) { if (!p) return '-'; const s = String(p); return s.length === 6 ? s.slice(0,4)+'年'+s.slice(4)+'月' : s }
async function loadAddresses() { if (!userStore.currentFamily) return; const res = await addressApi.listByFamily(userStore.currentFamily.id); addresses.value = res.data || [] }
function getAddressName(id) { return addresses.value.find(a => String(a.id) === String(id))?.name || '-' }
function getAddressById(id) { return addresses.value.find(a => String(a.id) === String(id)) }

function onAddressChange(val) {
  if (!val) return
  const addr = getAddressById(val)
  if (!addr) return
  if (addr.defaultRent != null && form.value.rent == null) form.value.rent = addr.defaultRent
  if (addr.defaultElectricPrice != null && form.value.electricUnitPrice == null) form.value.electricUnitPrice = addr.defaultElectricPrice
  if (addr.defaultWaterPrice != null && form.value.waterUnitPrice == null) form.value.waterUnitPrice = addr.defaultWaterPrice
  if (addr.defaultManagementFee != null && form.value.managementFee == null) form.value.managementFee = addr.defaultManagementFee
}

function summaryMethod({ columns, data }) {
  const sums = Array(columns.length).fill(null)
  columns.forEach((col, idx) => {
    if (idx === 0) { sums[idx] = '本页合计'; return }
    const prop = col.property
    if (['rent','waterAmount','electricAmount','managementFee','otherFee','totalAmount','roundingAmount'].includes(prop)) {
      sums[idx] = data.reduce((s, r) => s + (Number(r[prop]) || 0), 0).toFixed(2)
    }
  })
  return sums
}

async function fetchData() {
  if (!userStore.currentFamily) return; loading.value = true
  try {
    const params = { pageNum: pageNum.value, pageSize: pageSize.value, familyId: userStore.currentFamily.id, ...(query.value.addressId ? { addressId: query.value.addressId } : {}) }
    if (query.value.periodStart) params.periodStart = Number(query.value.periodStart)
    if (query.value.periodEnd) params.periodEnd = Number(query.value.periodEnd)
    const res = await billApi.page(params); const d = res.data
    list.value = (d.records || []).map(r => ({ ...r, _attachCount: (r.attachments || []).length })); total.value = Number(d.total) || 0; pageNum.value = Number(d.current) || 1; pageSize.value = Number(d.size) || 10
  } finally { loading.value = false }
}

function handleQuery() { pageNum.value = 1; fetchData() }

function showDialog(row) {
  isEdit.value = !!row; form.value = row ? { ...row, period: row.period ? String(row.period) : "" } : { ...createEmptyForm(), familyId: userStore.currentFamily?.id }; attachmentList.value = []
  // 确保所有数字字段为 Number 类型（API 返回的可能为字符串）
  if (row) {
    form.value.rent = Number(form.value.rent) || null
    form.value.waterAmount = Number(form.value.waterAmount) || null
    form.value.electricAmount = Number(form.value.electricAmount) || null
    form.value.managementFee = Number(form.value.managementFee) || null
    form.value.otherFee = Number(form.value.otherFee) || null
    form.value.totalAmount = Number(form.value.totalAmount) || null
    form.value.roundingAmount = Number(form.value.roundingAmount) || null
    form.value.waterPrevReading = Number(form.value.waterPrevReading) || null
    form.value.waterCurrReading = Number(form.value.waterCurrReading) || null
    form.value.waterUnitPrice = Number(form.value.waterUnitPrice) || null
    form.value.electricPrevReading = Number(form.value.electricPrevReading) || null
    form.value.electricCurrReading = Number(form.value.electricCurrReading) || null
    form.value.electricUnitPrice = Number(form.value.electricUnitPrice) || null
  }
  // 解析历史数据：从备注中提取管理费
  if (row && row.remark) {
    const m = row.remark.match(/管理费(\d+(\.\d+)?)/)
    if (m) {
      form.value.managementFee = parseFloat(m[1])
      form.value.remark = form.value.remark.replace(/管理费\d+(\.\d+)?[，,、\s]*/, "").trim()
    }
  }
  if (row) { attachmentList.value = (row.attachments || []).map(a => ({ ...a, _isNew: false })) }
  // 触发水电费自动计算
  calcElectric(); calcWater()
  showDlg.value = true
}

async function save() {
  if (!form.value.addressId) { ElMessage.warning('请选择住址'); return }
  if (!form.value.period) { ElMessage.warning('请选择账期'); return }
  saving.value = true; try { const billData = { ...form.value, familyId: userStore.currentFamily?.id }; if (billData.period) billData.period = Number(billData.period); let billId = form.value.id; if (isEdit.value) { await billApi.update(billData) } else { const res = await billApi.add(billData); billId = res.data?.id || billId }; for (const att of attachmentList.value) { if (att._isNew && billId) { try { await billApi.addAttachment(billId, { fileName: att.fileName, fileUrl: att.fileUrl, fileSize: att.fileSize }) } catch (e) { } } }; ElMessage.success('保存成功'); showDlg.value = false; fetchData() } finally { saving.value = false }
}

async function handleDelete(row) { try { await ElMessageBox.confirm('确认删除该账单？','提示'); await billApi.delete(row.id); ElMessage.success('删除成功'); fetchData() } catch(e) { if(e!=='cancel') throw e } }

function removeAttachment(idx) { const att = attachmentList.value[idx]; if (att && !att._isNew && att.id) { billApi.deleteAttachment(att.id).catch(()=>{}) }; attachmentList.value.splice(idx, 1) }

watch(() => userStore.currentFamilyId, () => { query.value = { addressId: '', periodStart: '', periodEnd: '' }; pageNum.value = 1; loadAddresses(); fetchData() })
onMounted(() => { loadAddresses(); fetchData() })
function calcElectric() {
  const { electricCurrReading, electricPrevReading, electricUnitPrice } = form.value
  if (electricCurrReading && electricPrevReading && electricUnitPrice) {
    form.value.electricAmount = Number(((electricCurrReading - electricPrevReading) * electricUnitPrice).toFixed(2))
  }
}
function calcWater() {
  const { waterCurrReading, waterPrevReading, waterUnitPrice } = form.value
  if (waterCurrReading && waterPrevReading && waterUnitPrice) {
    form.value.waterAmount = Number(((waterCurrReading - waterPrevReading) * waterUnitPrice).toFixed(2))
  }
}
watch([() => form.value.electricPrevReading, () => form.value.electricCurrReading, () => form.value.electricUnitPrice], calcElectric)
watch([() => form.value.waterPrevReading, () => form.value.waterCurrReading, () => form.value.waterUnitPrice], calcWater)
</script>

<style lang="scss" scoped>
.table-container { background: #fff; border-radius: 12px; padding: 20px; box-shadow: 0 1px 3px rgba(0,0,0,0.06); }
.pagination-wrapper { margin-top: 20px; display: flex; justify-content: flex-end; position: relative; z-index: 1; }
.calc-hint { margin-left: 12px; color: #909399; font-size: 12px; }
.empty-tip { padding: 80px 0; display: flex; justify-content: center; }
:deep(.section-divider .el-divider__text) { font-weight: 600; color: #409eff; font-size: 14px; }
.bill-dialog { :deep(.el-dialog__body) { max-height: 70vh; overflow-y: auto; } }
:deep(.el-table__footer-wrapper .cell) { font-weight: 700; color: #f56c6c; }
</style>
