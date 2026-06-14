<template>
  <div class="page-container">
    <h2 class="page-title">教育费用</h2>

    <div v-if="userStore.currentFamily">
      <div class="query-bar">
        <el-select v-model="query.memberId" placeholder="选择成员" clearable style="width:160px"><el-option v-for="m in members" :key="m.userId" :label="m.userName||'用户#'+m.userId" :value="m.userId" /></el-select>
        <el-date-picker v-model="query.semesterDateStart" type="date" placeholder="日期起" value-format="YYYY-MM-DD" clearable style="width:150px" />
        <el-date-picker v-model="query.semesterDateEnd" type="date" placeholder="日期止" value-format="YYYY-MM-DD" clearable style="width:150px" />
        <el-button type="primary" @click="handleQuery">查询</el-button>
        <el-button type="success" @click="showEduDialog(null)">新增教育费用</el-button>
      </div>

      <div class="table-container">
        <el-table :data="list" v-loading="loading" border stripe show-summary :summary-method="summaryMethod">
          <el-table-column label="成员" width="100"><template #default="{ row }">{{ getMemberName(row.memberId) }}</template></el-table-column>
          <el-table-column prop="semesterDate" label="学期" width="120" />
          <el-table-column prop="tuition" label="学费" width="90" align="right" />
          <el-table-column prop="mealFee" label="伙食费" width="90" align="right" />
          <el-table-column prop="accommodationFee" label="住宿费" width="90" align="right" />
          <el-table-column label="明细" width="100" align="right">
            <template #default="{ row }">
              <el-button v-if="row._itemCount>0" link type="primary" size="small" @click="openItems(row)">¥{{ row._itemTotal.toFixed(2) }}</el-button>
              <el-button v-else link type="success" size="small" @click="openItems(row)">添加</el-button>
            </template>
          </el-table-column>
          <el-table-column label="合计" width="100" align="right"><template #default="{ row }"><strong style="color:#f56c6c">{{ row._total||0 }}</strong></template></el-table-column>
          <el-table-column prop="remark" label="备注" min-width="100" show-overflow-tooltip />
          <el-table-column label="附件" width="90"><template #default="{ row }"><template v-if="row._attachCount"><el-image :src="row.attachments[0].fileUrl" :preview-src-list="row.attachments.map(a=>a.fileUrl)" fit="cover" style="width:32px;height:32px;border-radius:4px;cursor:pointer" preview-teleported /><span style="font-size:11px;color:#909399;margin-left:4px;">{{ row._attachCount }}张</span></template></template></el-table-column>
          <el-table-column label="操作" width="140" fixed="right"><template #default="{ row }"><el-button link type="primary" size="small" @click="showEduDialog(row)">编辑</el-button><el-button link type="danger" size="small" @click="handleDelete(row)">删除</el-button></template></el-table-column>
        </el-table>
        <div class="pagination-wrapper">
          <el-pagination v-model:currentPage="pageNum" v-model:pageSize="pageSize" :total="total" :page-sizes="[10,20,50]" layout="total,sizes,prev,pager,next" :hide-on-single-page="false" @current-change="fetchData" @size-change="fetchData" />
        </div>
      </div>
    </div>
    <div v-else class="empty-tip"><el-empty description="请先创建或切换到家庭" /></div>

    <!-- 教育费用 新增/编辑 -->
    <el-dialog :close-on-click-modal="false" v-model="showEduDlg" :title="isEduEdit?'编辑教育费用':'新增教育费用'" width="600px">
      <el-form :model="eduForm" label-width="100px">
        <el-row :gutter="16"><el-col :span="12"><el-form-item label="成员" required><el-select v-model="eduForm.memberId" placeholder="选择成员" style="width:100%"><el-option v-for="m in members" :key="m.userId" :label="m.userName||'用户#'+m.userId" :value="m.userId" /></el-select></el-form-item></el-col><el-col :span="12"><el-form-item label="学期" required><el-date-picker v-model="eduForm.semesterDate" type="date" placeholder="选择日期" value-format="YYYY-MM-DD" style="width:100%" /></el-form-item></el-col></el-row>
        <el-row :gutter="16"><el-col :span="8"><el-form-item label="学费"><el-input-number v-model="eduForm.tuition" :min="0" :precision="2" style="width:100%" /></el-form-item></el-col><el-col :span="8"><el-form-item label="伙食费"><el-input-number v-model="eduForm.mealFee" :min="0" :precision="2" style="width:100%" /></el-form-item></el-col><el-col :span="8"><el-form-item label="住宿费"><el-input-number v-model="eduForm.accommodationFee" :min="0" :precision="2" style="width:100%" /></el-form-item></el-col></el-row>
        <el-form-item label="备注"><el-input v-model="eduForm.remark" type="textarea" :rows="2" /></el-form-item>
        <el-divider content-position="left" class="section-divider">附件</el-divider>
        <el-form-item label="上传图片">
          <AttachmentUpload prefix="education" :list="eduAttList" @remove="removeEduAtt" />
        </el-form-item>
      </el-form>
      <template #footer><el-button @click="showEduDlg=false">取消</el-button><el-button type="primary" @click="saveEdu" :loading="eduSaving">保存</el-button></template>
    </el-dialog>

    <!-- 明细管理弹窗 -->
    <el-dialog :close-on-click-modal="false" v-model="showItemsDlg" :title="currentEdu?'明细费用 - '+getMemberName(currentEdu.memberId)+' '+currentEdu.semesterDate:''" width="700px">
      <div style="margin-bottom:12px;"><el-button type="primary" size="small" @click="showItemForm(null)"><el-icon><Plus /></el-icon>添加费用项</el-button></div>
      <el-table :data="itemList" border size="small" v-loading="itemsLoading">
        <el-table-column prop="costDate" label="日期" width="120" />
        <el-table-column label="类型" width="120"><template #default="{row}">{{ getItemTypeLabel(row.itemType) }}</template></el-table-column>
        <el-table-column prop="amount" label="金额" width="100" align="right" />
        <el-table-column prop="remark" label="备注" min-width="120" show-overflow-tooltip />
        <el-table-column label="附件" width="80"><template #default="{row}"><el-tag v-if="(row.attachments||[]).length" size="small" type="info">{{ (row.attachments||[]).length }}张</el-tag></template></el-table-column>
        <el-table-column label="操作" width="120"><template #default="{row}"><el-button link type="primary" size="small" @click="showItemForm(row)">编辑</el-button><el-button link type="danger" size="small" @click="delItem(row)">删除</el-button></template></el-table-column>
      </el-table>
      <template #footer><el-button @click="showItemsDlg=false">关闭</el-button></template>
    </el-dialog>

    <!-- 明细编辑弹窗 -->
    <el-dialog :close-on-click-modal="false" v-model="showItemDlg" :title="isItemEdit?'编辑明细':'新增明细'" width="500px" append-to-body>
      <el-form :model="itemForm" label-width="80px">
        <el-form-item label="日期"><el-date-picker v-model="itemForm.costDate" type="date" placeholder="选择日期" value-format="YYYY-MM-DD" style="width:100%" /></el-form-item>
        <el-form-item label="类型" required><el-select v-model="itemForm.itemType" placeholder="选择类型" style="width:100%"><el-option v-for="t in itemTypes" :key="t.value" :label="t.label" :value="t.value" /></el-select></el-form-item>
        <el-form-item label="金额" required><el-input-number v-model="itemForm.amount" :min="0" :precision="2" style="width:200px" /></el-form-item>
        <el-form-item label="备注"><el-input v-model="itemForm.remark" /></el-form-item>
        <el-form-item label="附件">
          <AttachmentUpload prefix="education_item" :list="itemAttList" @remove="removeItemAtt" />
        </el-form-item>
      </el-form>
      <template #footer><el-button @click="showItemDlg=false">取消</el-button><el-button type="primary" @click="saveItem" :loading="itemSaving">保存</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { educationApi, familyApi, dictApi } from '@/api'
import { useUserStore } from '@/stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import AttachmentUpload from '@/components/AttachmentUpload.vue'

const userStore = useUserStore()
const loading = ref(false); const list = ref([]); const total = ref(0)
const pageNum = ref(1); const pageSize = ref(10); const members = ref([]); const itemTypes = ref([])
const query = ref({ memberId: '', semesterDateStart: '', semesterDateEnd: '' })

// Education dialog
const showEduDlg = ref(false); const isEduEdit = ref(false); const eduSaving = ref(false)
const eduForm = ref({ memberId: null, semesterDate: '', tuition: null, mealFee: null, accommodationFee: null, remark: '' })
const eduAttList = ref([])

// Items dialog
const showItemsDlg = ref(false); const currentEdu = ref(null); const itemList = ref([]); const itemsLoading = ref(false)

// Item edit dialog
const showItemDlg = ref(false); const isItemEdit = ref(false); const itemSaving = ref(false); const currentItemId = ref(null)
const itemForm = ref({ costDate: '', itemType: '', amount: null, remark: '' })
const itemAttList = ref([])

function getMemberName(id) { return members.value.find(m => m.userId === id)?.userName || '用户#'+id }
function getItemTypeLabel(v) { const t = itemTypes.value.find(i => i.value === v); return t?.label || v }

async function loadMembers() { if (!userStore.currentFamily) return; try { const res = await familyApi.getMembers(userStore.currentFamily.id); members.value = res.data||[] } catch(e) {} }
async function loadItemTypes() { try { const res = await dictApi.getDataByCode('education_item_type'); itemTypes.value = res.data||[] } catch(e) {} }

function summaryMethod({ columns, data }) {
  const sums = Array(columns.length).fill(null)
  columns.forEach((col, idx) => {
    if (idx === 0) { sums[idx] = '本页合计'; return }
    if (['tuition','mealFee','accommodationFee'].includes(col.property)) sums[idx] = data.reduce((s,r)=>s+(Number(r[col.property])||0),0).toFixed(2)
  })
  return sums
}

async function fetchData() {
  if (!userStore.currentFamily) return; loading.value = true
  try {
    const params = { pageNum: pageNum.value, pageSize: pageSize.value, familyId: userStore.currentFamily.id, ...(query.value.memberId?{memberId:query.value.memberId}:{}), ...(query.value.semesterDateStart?{semesterDateStart:query.value.semesterDateStart}:{}), ...(query.value.semesterDateEnd?{semesterDateEnd:query.value.semesterDateEnd}:{}) }
    const res = await educationApi.page(params); const d = res.data
    list.value = (d.records||[]).map(r => {
      const base = Number(r.tuition||0)+Number(r.mealFee||0)+Number(r.accommodationFee||0)
      const itemTotal = (r.items||[]).reduce((s,i)=>s+(Number(i.amount)||0),0)
      return { ...r, _itemCount: (r.items||[]).length, _itemTotal: itemTotal, _total: (base+itemTotal).toFixed(2), _attachCount: (r.attachments||[]).length }
    })
    total.value = Number(d.total)||0; pageNum.value = Number(d.current)||1; pageSize.value = Number(d.size)||10
  } finally { loading.value = false }
}

function handleQuery() { pageNum.value = 1; fetchData() }

// === Education CRUD ===
function showEduDialog(row) {
  isEduEdit.value = !!row
  eduForm.value = row ? { memberId: row.memberId, semesterDate: row.semesterDate, tuition: row.tuition, mealFee: row.mealFee, accommodationFee: row.accommodationFee, remark: row.remark } : { memberId: null, semesterDate: '', tuition: null, mealFee: null, accommodationFee: null, remark: '' }
  eduAttList.value = row?.attachments ? row.attachments.map(a=>({...a,_isNew:false})) : []
  showEduDlg.value = true
}

async function saveEdu() {
  if (!eduForm.value.memberId) { ElMessage.warning('请选择成员'); return }
  if (!eduForm.value.semesterDate) { ElMessage.warning('请选择学期'); return }
  eduSaving.value = true
  try {
    const education = { ...eduForm.value, id: isEduEdit.value ? eduForm.value.id : undefined }
    let eduId = education.id
    if (isEduEdit.value) { await educationApi.update({ education, items: [] }) } else { const res = await educationApi.add({ education, items: [] }); eduId = res.data?.id||eduId }
    for (const att of eduAttList.value) { if (att._isNew && eduId) { try { await educationApi.addAttachment(eduId, { fileName: att.fileName, fileUrl: att.fileUrl, fileSize: att.fileSize }) } catch(e) {} } }
    ElMessage.success('保存成功'); showEduDlg.value = false; fetchData()
  } finally { eduSaving.value = false }
}

function removeEduAtt(idx) { const att = eduAttList.value[idx]; if (att&&!att._isNew&&att.id) educationApi.deleteAttachment(att.id).catch(()=>{}); eduAttList.value.splice(idx,1) }

async function handleDelete(row) { try { await ElMessageBox.confirm('确认删除该教育费用记录？','提示'); await educationApi.delete(row.id); ElMessage.success('删除成功'); fetchData() } catch(e) { if(e!=='cancel') throw e } }

// === Items CRUD ===
async function openItems(row) {
  currentEdu.value = row; showItemsDlg.value = true; itemsLoading.value = true
  try { const res = await educationApi.getItems(row.id); itemList.value = res.data||[] } catch(e) { itemList.value = row.items||[] }
  finally { itemsLoading.value = false }
}

function showItemForm(row) {
  isItemEdit.value = !!row; currentItemId.value = row?.id || null
  itemForm.value = row ? { costDate: row.costDate||'', itemType: row.itemType, amount: row.amount, remark: row.remark||'' } : { costDate: '', itemType: '', amount: null, remark: '' }
  itemAttList.value = row?.attachments ? row.attachments.map(a=>({...a,_isNew:false})) : []
  showItemDlg.value = true
}

async function saveItem() {
  if (!itemForm.value.itemType) { ElMessage.warning('请选择类型'); return }
  itemSaving.value = true
  try {
    let itemId = currentItemId.value
    if (isItemEdit.value) { await educationApi.updateItem(itemId, itemForm.value) } else { const res = await educationApi.addItem(currentEdu.value.id, itemForm.value); itemId = res.data?.id||itemId }
    for (const att of itemAttList.value) { if (att._isNew && itemId) { try { await educationApi.addItemAttachment(itemId, { fileName: att.fileName, fileUrl: att.fileUrl, fileSize: att.fileSize }) } catch(e) {} } }
    ElMessage.success('保存成功'); showItemDlg.value = false
    // Refresh items
    itemsLoading.value = true
    try { const res = await educationApi.getItems(currentEdu.value.id); itemList.value = res.data||[] } catch(e) {}
    finally { itemsLoading.value = false }
    fetchData()
  } finally { itemSaving.value = false }
}

function removeItemAtt(idx) { const att = itemAttList.value[idx]; if (att&&!att._isNew&&att.id) educationApi.deleteItemAttachment(att.id).catch(()=>{}); itemAttList.value.splice(idx,1) }

async function delItem(row) {
  try { await ElMessageBox.confirm('确认删除该明细？','提示'); await educationApi.deleteItem(row.id)
    itemsLoading.value = true
    try { const res = await educationApi.getItems(currentEdu.value.id); itemList.value = res.data||[] } catch(e) {}
    finally { itemsLoading.value = false }
    ElMessage.success('删除成功'); fetchData()
  } catch(e) { if(e!=='cancel') throw e }
}

watch(() => userStore.currentFamilyId, () => { query.value = { memberId: '', semesterDateStart: '', semesterDateEnd: '' }; pageNum.value = 1; loadMembers(); fetchData() })
onMounted(() => { loadMembers(); loadItemTypes(); fetchData() })
</script>

<style lang="scss" scoped>
.table-container { background: #fff; border-radius: 12px; padding: 20px; box-shadow: 0 1px 3px rgba(0,0,0,0.06); }
.pagination-wrapper { margin-top: 20px; display: flex; justify-content: flex-end; position: relative; z-index: 1; }
.empty-tip { padding: 80px 0; display: flex; justify-content: center; }
:deep(.section-divider .el-divider__text) { font-weight: 600; color: #409eff; font-size: 14px; }
</style>
