<template>
  <div class="page-container">
    <h2 class="page-title">住址管理</h2>

    <div v-if="userStore.currentFamily">
      <div class="query-bar">
        <span>当前家庭：<strong style="color:#303133;">{{ userStore.currentFamily.name }}</strong></span>
        <el-button type="success" @click="showDialog(null)">新增住址</el-button>
      </div>

      <div class="table-container">
        <el-table :data="list" v-loading="loading" border stripe>
          <el-table-column prop="name" label="住址名称" min-width="120" />
          <el-table-column label="地址" min-width="200"><template #default="{ row }">{{ [row.province, row.city, row.district].filter(Boolean).join(' ') }} {{ row.streetNumber || '' }}</template></el-table-column>
          <el-table-column label="默认房租" width="100" align="right"><template #default="{ row }">{{ row.defaultRent || '-' }}</template></el-table-column>
          <el-table-column label="电费单价" width="100" align="right"><template #default="{ row }">{{ row.defaultElectricPrice || '-' }}</template></el-table-column>
          <el-table-column label="水费单价" width="100" align="right"><template #default="{ row }">{{ row.defaultWaterPrice || '-' }}</template></el-table-column>
          <el-table-column label="管理费" width="100" align="right"><template #default="{ row }">{{ row.defaultManagementFee || "-" }}</template></el-table-column>
          <el-table-column label="地址图片" width="80"><template #default="{ row }"><el-image v-if="row.addressImage" :src="row.addressImage" style="width:48px;height:48px;border-radius:4px;cursor:pointer" fit="cover" :preview-src-list="[row.addressImage]" preview-teleported /><span v-else style="color:#909399;font-size:12px;">无</span></template></el-table-column>
          <el-table-column prop="createTime" label="创建时间" width="170" />
          <el-table-column label="操作" width="140" fixed="right"><template #default="{ row }"><el-button link type="primary" size="small" @click="showDialog(row)">编辑</el-button><el-button link type="danger" size="small" @click="handleDelete(row)">删除</el-button></template></el-table-column>
        </el-table>
      </div>
    </div>
    <div v-else class="empty-tip"><el-empty description="请先创建或切换到家庭" /></div>

    <el-dialog v-model="showDlg" :title="isEdit ? '编辑住址' : '新增住址'" width="600px">
      <el-form :model="form" label-width="110px">
        <el-form-item label="住址名称"><el-input v-model="form.name" placeholder="如：老家、租房地" /></el-form-item>
        <el-form-item label="省/市/区"><el-space><el-input v-model="form.province" placeholder="省" style="width:120px" /><el-input v-model="form.city" placeholder="市" style="width:120px" /><el-input v-model="form.district" placeholder="区" style="width:120px" /></el-space></el-form-item>
        <el-form-item label="门牌号"><el-input v-model="form.streetNumber" /></el-form-item>
        <el-divider content-position="left" class="section-divider">默认费用（在新增水电账单时自动填入）</el-divider>
        <el-form-item label="房租"><el-input-number v-model="form.defaultRent" :min="0" :precision="2" style="width:240px" /></el-form-item>
        <el-form-item label="电费单价"><el-input-number v-model="form.defaultElectricPrice" :min="0" :precision="2" style="width:240px" /></el-form-item>
        <el-form-item label="管理费"><el-input-number v-model="form.defaultManagementFee" :min="0" :precision="2" style="width:240px" /></el-form-item>
        <el-form-item label="水费单价"><el-input-number v-model="form.defaultWaterPrice" :min="0" :precision="2" style="width:240px" /></el-form-item>
        <el-form-item label="地址图片">
          <el-upload :action="uploadUrl" :headers="uploadHeaders" :show-file-list="false" :on-success="handleUploadSuccess" :before-upload="beforeUpload" accept="image/*">
            <el-button type="primary" size="small">选择图片</el-button>
            <template #tip><span style="color:#909399;font-size:12px;margin-left:8px;">支持 JPG/PNG</span></template>
          </el-upload>
          <div v-if="form.addressImage" class="upload-preview"><el-image :src="form.addressImage" style="width:200px;height:140px;border-radius:8px;margin-top:8px;" fit="cover" :preview-src-list="[form.addressImage]" preview-teleported /><el-button size="small" type="danger" link @click="form.addressImage = ''" style="margin-top:4px;">删除图片</el-button></div>
        </el-form-item>
      </el-form>
      <template #footer><el-button @click="showDlg = false">取消</el-button><el-button type="primary" @click="save" :loading="saving">保存</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { addressApi } from '@/api'
import { useUserStore } from '@/stores/user'
import { ElMessage, ElMessageBox } from 'element-plus'

const userStore = useUserStore()
const loading = ref(false); const list = ref([])
const showDlg = ref(false); const isEdit = ref(false); const saving = ref(false)
const form = ref({ name: '', province: '', city: '', district: '', streetNumber: '', defaultRent: null, defaultElectricPrice: null, defaultWaterPrice: null, defaultManagementFee: null, addressImage: '' })
const uploadUrl = '/bus/api/file/upload?prefix=address'
const uploadHeaders = { Authorization: 'Bearer ' + localStorage.getItem('token') }

function beforeUpload(file) { if (!file.type.startsWith('image/')) { ElMessage.warning('只能上传图片'); return false } if (file.size > 10*1024*1024) { ElMessage.warning('图片不能超过10MB'); return false } return true }
function handleUploadSuccess(res) { if (res.code === 200) { form.value.addressImage = res.data; ElMessage.success('上传成功') } else { ElMessage.error(res.msg || '上传失败') } }

async function fetchData() { if (!userStore.currentFamily) return; loading.value = true; try { const res = await addressApi.listByFamily(userStore.currentFamily.id); list.value = res.data || [] } finally { loading.value = false } }
function showDialog(row) { isEdit.value = !!row; form.value = row ? { ...row } : { name: '', province: '', city: '', district: '', streetNumber: '', defaultRent: null, defaultElectricPrice: null, defaultWaterPrice: null, defaultManagementFee: null, addressImage: '', familyId: userStore.currentFamily?.id }; showDlg.value = true }
async function save() {
  saving.value = true; try { form.value.familyId = userStore.currentFamily?.id; isEdit.value ? await addressApi.update(form.value) : await addressApi.add(form.value); ElMessage.success('保存成功'); showDlg.value = false; fetchData() } finally { saving.value = false }
}
async function handleDelete(row) { try { await ElMessageBox.confirm(`确认删除住址「${row.name}」？`, '提示'); await addressApi.delete(row.id); ElMessage.success('删除成功'); fetchData() } catch (e) { if (e !== 'cancel') throw e } }
watch(() => userStore.currentFamilyId, () => fetchData())
onMounted(() => fetchData())
</script>

<style lang="scss" scoped>
.table-container { background: #fff; border-radius: 12px; padding: 20px; box-shadow: 0 1px 3px rgba(0,0,0,0.06); }
.empty-tip { padding: 80px 0; display: flex; justify-content: center; }
.upload-preview { display: flex; flex-direction: column; align-items: flex-start; }
:deep(.section-divider .el-divider__text) { font-weight: 600; color: #409eff; font-size: 13px; }
</style>
