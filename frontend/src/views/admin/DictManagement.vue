<template>
  <div class="page-container">
    <h2 class="page-title">字典管理</h2>

    <el-tabs v-model="activeTab" class="dict-tabs">
      <el-tab-pane label="字典类型" name="type">
        <div class="query-bar">
          <el-input v-model="keyword" placeholder="搜索字典名称/编码" clearable style="width:280px" @clear="fetchTypes" prefix-icon="Search" />
          <el-button type="primary" @click="fetchTypes">查询</el-button>
          <el-button type="success" @click="showTypeDialog(null)">新增字典类型</el-button>
        </div>
        <div class="table-container">
          <el-table :data="typeList" v-loading="loading" border stripe>
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="name" label="字典名称" min-width="160" />
            <el-table-column prop="code" label="字典编码" min-width="160" />
            <el-table-column prop="status" label="状态" width="100"><template #default="{ row }"><el-tag :type="row.status === 0 ? 'success' : 'danger'" size="small" effect="dark">{{ row.status === 0 ? '正常' : '禁用' }}</el-tag></template></el-table-column>
            <el-table-column prop="remark" label="备注" min-width="200" show-overflow-tooltip />
            <el-table-column label="操作" width="220" fixed="right">
              <template #default="{ row }"><el-button link type="primary" size="small" @click="showTypeDialog(row)">编辑</el-button><el-button link type="primary" size="small" @click="showDictData(row)">字典数据</el-button><el-button link type="danger" size="small" @click="deleteType(row)">删除</el-button></template>
            </el-table-column>
          </el-table>
          <el-pagination v-model:current-page="typePageNum" v-model:page-size="typePageSize" :total="typeTotal" layout="prev, pager, next, total" class="pagination" @current-change="fetchTypes" />
        </div>
      </el-tab-pane>

      <el-tab-pane label="字典数据" name="data">
        <div class="query-bar">
          <span style="color:#606266;">当前字典：<strong>{{ currentDictType?.name }}</strong></span>
          <el-button type="success" size="small" @click="showDataDialog(null)">新增字典数据</el-button>
          <el-button size="small" @click="activeTab = 'type'">返回字典类型</el-button>
        </div>
        <div class="table-container">
          <el-table :data="dataList" v-loading="dataLoading" border stripe>
            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="label" label="字典标签" min-width="160" />
            <el-table-column prop="value" label="字典键值" min-width="160" />
            <el-table-column prop="sort" label="排序" width="80" />
            <el-table-column prop="status" label="状态" width="100"><template #default="{ row }"><el-tag :type="row.status === 0 ? 'success' : 'danger'" size="small" effect="dark">{{ row.status === 0 ? '正常' : '禁用' }}</el-tag></template></el-table-column>
            <el-table-column label="操作" width="160" fixed="right">
              <template #default="{ row }"><el-button link type="primary" size="small" @click="showDataDialog(row)">编辑</el-button><el-button link type="danger" size="small" @click="deleteData(row)">删除</el-button></template>
            </el-table-column>
          </el-table>
          <el-pagination v-model:current-page="dataPageNum" v-model:page-size="dataPageSize" :total="dataTotal" layout="prev, pager, next, total" class="pagination" @current-change="fetchDictData" />
        </div>
      </el-tab-pane>
    </el-tabs>

    <el-dialog v-model="showTypeDlg" :title="isEditType ? '编辑字典类型' : '新增字典类型'" width="500px">
      <el-form :model="typeForm" label-width="80px">
        <el-form-item label="字典名称"><el-input v-model="typeForm.name" /></el-form-item>
        <el-form-item label="字典编码"><el-input v-model="typeForm.code" :disabled="isEditType" /></el-form-item>
        <el-form-item label="状态"><el-switch v-model="typeForm.status" :active-value="0" :inactive-value="1" /></el-form-item>
        <el-form-item label="备注"><el-input v-model="typeForm.remark" type="textarea" :rows="3" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="showTypeDlg = false">取消</el-button><el-button type="primary" @click="saveType" :loading="savingType">保存</el-button></template>
    </el-dialog>

    <el-dialog v-model="showDataDlg" :title="isEditData ? '编辑字典数据' : '新增字典数据'" width="500px">
      <el-form :model="dataForm" label-width="80px">
        <el-form-item label="字典标签"><el-input v-model="dataForm.label" /></el-form-item>
        <el-form-item label="字典键值"><el-input v-model="dataForm.value" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="dataForm.sort" :min="0" /></el-form-item>
        <el-form-item label="状态"><el-switch v-model="dataForm.status" :active-value="0" :inactive-value="1" /></el-form-item>
        <el-form-item label="备注"><el-input v-model="dataForm.remark" type="textarea" :rows="2" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="showDataDlg = false">取消</el-button><el-button type="primary" @click="saveData" :loading="savingData">保存</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { dictApi } from '@/api'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'

const activeTab = ref('type')
const loading = ref(false); const typeList = ref([]); const typeTotal = ref(0); const typePageNum = ref(1); const typePageSize = ref(10); const keyword = ref('')
const showTypeDlg = ref(false); const isEditType = ref(false); const savingType = ref(false); const typeForm = ref({ name: '', code: '', status: 0, remark: '' })
const dataLoading = ref(false); const dataList = ref([]); const dataTotal = ref(0); const dataPageNum = ref(1); const dataPageSize = ref(10)
const currentDictType = ref(null); const showDataDlg = ref(false); const isEditData = ref(false); const savingData = ref(false)
const dataForm = ref({ label: '', value: '', sort: 0, status: 0, remark: '' })

async function fetchTypes() { loading.value = true; try { const res = await dictApi.pageType({ pageNum: typePageNum.value, pageSize: typePageSize.value, keyword: keyword.value }); const d = res.data; typeList.value = d.records || []; typeTotal.value = d.total || 0 } finally { loading.value = false } }
function showTypeDialog(row) { isEditType.value = !!row; typeForm.value = row ? { ...row } : { name: '', code: '', status: 0, remark: '' }; showTypeDlg.value = true }
async function saveType() { savingType.value = true; try { isEditType.value ? await dictApi.updateType(typeForm.value) : await dictApi.addType(typeForm.value); ElMessage.success('保存成功'); showTypeDlg.value = false; fetchTypes() } finally { savingType.value = false } }
async function deleteType(row) { try { await ElMessageBox.confirm(`确认删除字典类型「${row.name}」？`, '提示'); await dictApi.deleteType(row.id); ElMessage.success('删除成功'); fetchTypes() } catch (e) { if (e !== 'cancel') throw e } }
async function showDictData(type) { currentDictType.value = type; activeTab.value = 'data'; dataPageNum.value = 1; fetchDictData() }
async function fetchDictData() { if (!currentDictType.value) return; dataLoading.value = true; try { const res = await dictApi.pageData({ pageNum: dataPageNum.value, pageSize: dataPageSize.value, dictTypeId: currentDictType.value.id }); const d = res.data; dataList.value = d.records || []; dataTotal.value = d.total || 0 } finally { dataLoading.value = false } }
function showDataDialog(row) { isEditData.value = !!row; dataForm.value = row ? { ...row } : { label: '', value: '', sort: 0, status: 0, remark: '', dictTypeId: currentDictType.value?.id }; showDataDlg.value = true }
async function saveData() { if (!dataForm.value.dictTypeId) dataForm.value.dictTypeId = currentDictType.value?.id; savingData.value = true; try { isEditData.value ? await dictApi.updateData(dataForm.value) : await dictApi.addData(dataForm.value); ElMessage.success('保存成功'); showDataDlg.value = false; fetchDictData() } finally { savingData.value = false } }
async function deleteData(row) { try { await ElMessageBox.confirm(`确认删除字典数据「${row.label}」？`, '提示'); await dictApi.deleteData(row.id); ElMessage.success('删除成功'); fetchDictData() } catch (e) { if (e !== 'cancel') throw e } }
onMounted(() => fetchTypes())
</script>

<style lang="scss" scoped>
.dict-tabs { :deep(.el-tabs__header) { margin-bottom: 20px; } }
.table-container { background: #fff; border-radius: 12px; padding: 20px; box-shadow: 0 1px 3px rgba(0,0,0,0.06);
  .pagination { margin-top: 16px; }
}
</style>
