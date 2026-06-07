<template>
  <div class="page-container">
    <h2 class="page-title">菜单管理</h2>

    <div class="query-bar">
      <el-button type="success" @click="showEditDialog(null)">新增菜单</el-button>
    </div>

    <div class="table-container">
      <el-table :data="menuTree" v-loading="loading" border stripe row-key="id" default-expand-all>
        <el-table-column prop="name" label="菜单名称" min-width="200" />
        <el-table-column prop="icon" label="图标" width="80">
          <template #default="{ row }"><el-icon v-if="row.icon"><component :is="row.icon" /></el-icon></template>
        </el-table-column>
        <el-table-column prop="type" label="类型" width="100">
          <template #default="{ row }"><el-tag :type="row.type === 0 ? '' : row.type === 1 ? 'primary' : 'warning'" size="small" effect="dark">{{ { 0: '目录', 1: '菜单', 2: '按钮' }[row.type] || '-' }}</el-tag></template>
        </el-table-column>
        <el-table-column prop="path" label="路由路径" min-width="150" />
        <el-table-column prop="component" label="组件路径" min-width="200" />
        <el-table-column prop="permission" label="权限标识" min-width="150" />
        <el-table-column prop="sort" label="排序" width="70" />
        <el-table-column prop="visible" label="状态" width="80">
          <template #default="{ row }"><el-tag :type="row.visible === 0 ? 'success' : 'info'" size="small" effect="dark">{{ row.visible === 0 ? '显示' : '隐藏' }}</el-tag></template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="showEditDialog(row)">编辑</el-button>
            <el-button link type="primary" size="small" @click="showEditDialog(null, row.id)">新增子级</el-button>
            <el-button link type="danger" size="small" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="showDialog" :title="isEdit ? '编辑菜单' : '新增菜单'" width="550px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="上级菜单"><el-tree-select v-model="form.parentId" :data="menuTree" :props="{ label: 'name', value: 'id', children: 'children' }" placeholder="顶级菜单" clearable check-strictly style="width:100%" /></el-form-item>
        <el-form-item label="菜单名称"><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="菜单类型"><el-radio-group v-model="form.type"><el-radio :value="0">目录</el-radio><el-radio :value="1">菜单</el-radio><el-radio :value="2">按钮</el-radio></el-radio-group></el-form-item>
        <el-form-item label="路由路径" v-if="form.type !== 2"><el-input v-model="form.path" /></el-form-item>
        <el-form-item label="组件路径" v-if="form.type === 1"><el-input v-model="form.component" /></el-form-item>
        <el-form-item label="权限标识" v-if="form.type === 2"><el-input v-model="form.permission" /></el-form-item>
        <el-form-item label="图标"><el-input v-model="form.icon" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="form.sort" :min="0" /></el-form-item>
        <el-form-item label="显示状态"><el-switch v-model="form.visible" :active-value="0" :inactive-value="1" /></el-form-item>
      </el-form>
      <template #footer><el-button @click="showDialog = false">取消</el-button><el-button type="primary" @click="save" :loading="saving">保存</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { menuApi } from '@/api'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false); const menuTree = ref([]); const showDialog = ref(false); const isEdit = ref(false); const saving = ref(false)
const form = ref({ name: '', path: '', component: '', permission: '', type: 1, parentId: null, sort: 0, icon: '', visible: 0 })

async function fetchTree() { loading.value = true; try { const res = await menuApi.getTree(); menuTree.value = res.data || [] } finally { loading.value = false } }
function showEditDialog(row, parentId) {
  isEdit.value = !!row; form.value = row ? { ...row } : { name: '', path: '', component: '', permission: '', type: 1, parentId: parentId || null, sort: 0, icon: '', visible: 0 }; showDialog.value = true
}
async function save() {
  saving.value = true
  try { isEdit.value ? await menuApi.update(form.value) : await menuApi.add(form.value); ElMessage.success(isEdit.value ? '修改成功' : '新增成功'); showDialog.value = false; fetchTree() } finally { saving.value = false }
}
async function handleDelete(row) {
  try { await ElMessageBox.confirm(`确认删除菜单「${row.name}」？`, '提示'); await menuApi.delete(row.id); ElMessage.success('删除成功'); fetchTree() } catch (e) { if (e !== 'cancel') throw e }
}
onMounted(() => fetchTree())
</script>

<style lang="scss" scoped>
.table-container { background: #fff; border-radius: 12px; padding: 20px; box-shadow: 0 1px 3px rgba(0,0,0,0.06); }
</style>
