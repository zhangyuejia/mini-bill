<template>
  <div class="page-container">
    <h2 class="page-title">家庭管理</h2>

    <div class="query-bar">
      <el-button type="success" @click="showCreateDialog = true">创建家庭</el-button>
    </div>

    <div class="table-container">
      <el-table :data="families" v-loading="loading" border stripe>
        <el-table-column prop="name" label="家庭名称" min-width="160" />
        <el-table-column label="户主" width="120">
          <template #default="{ row }">{{ row.ownerName || '用户#' + row.ownerId }}</template>
        </el-table-column>
        <el-table-column label="默认" width="80">
          <template #default="{ row }"><el-tag v-if="row.defaultFlag === 1" type="success" size="small" effect="dark">默认</el-tag></template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="320" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" size="small" @click="showEdit(row)">编辑</el-button>
            <el-button link type="primary" size="small" @click="showMembers(row)">成员管理</el-button>
            <el-button link type="primary" size="small" @click="showInvite(row)">邀请成员</el-button>
            <el-button v-if="row.defaultFlag !== 1" link type="warning" size="small" @click="setDefault(row)">设为默认</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="showCreateDialog" title="创建家庭" width="400px">
      <el-form label-width="80px"><el-form-item label="家庭名称"><el-input v-model="createForm.name" /></el-form-item></el-form>
      <template #footer><el-button @click="showCreateDialog = false">取消</el-button><el-button type="primary" @click="createFamily" :loading="creating">创建</el-button></template>
    </el-dialog>

    <el-dialog v-model="showMemberDialog" :title="`成员管理 - ${currentFamily?.name}`" width="500px">
      <el-table :data="members" border stripe size="small">
        <el-table-column label="用户名" min-width="120"><template #default="{ row }">{{ row.userName || '用户#' + row.userId }}</template></el-table-column>
        <el-table-column prop="role" label="角色" width="100"><template #default="{ row }"><el-tag :type="row.role === 'owner' ? 'warning' : 'info'" size="small" effect="dark">{{ row.role === 'owner' ? '户主' : '成员' }}</el-tag></template></el-table-column>
        <el-table-column prop="joinTime" label="加入时间" width="160" />
        <el-table-column label="操作" width="100"><template #default="{ row }"><el-button v-if="row.role !== 'owner'" link type="danger" size="small" @click="removeMember(row)">移除</el-button></template></el-table-column>
      </el-table>
      <template #footer><el-button @click="showMemberDialog = false">关闭</el-button></template>
    </el-dialog>

    <el-dialog v-model="showInviteDialog" title="邀请成员" width="400px">
      <el-form label-width="80px"><el-form-item label="用户邮箱"><el-input v-model="inviteForm.email" placeholder="请输入用户注册的邮箱" /></el-form-item></el-form>
      <template #footer><el-button @click="showInviteDialog = false">取消</el-button><el-button type="primary" @click="inviteMember" :loading="inviting">邀请</el-button></template>
    </el-dialog>

    <el-dialog v-model="showEditDialog" title="编辑家庭" width="400px">
      <el-form label-width="80px"><el-form-item label="家庭名称"><el-input v-model="editForm.name" /></el-form-item></el-form>
      <template #footer><el-button @click="showEditDialog = false">取消</el-button><el-button type="primary" @click="editFamily" :loading="editing">保存</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { familyApi } from '@/api'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false); const families = ref([])
const showCreateDialog = ref(false); const creating = ref(false); const createForm = ref({ name: '' })
const showMemberDialog = ref(false); const showInviteDialog = ref(false); const currentFamily = ref(null); const members = ref([])
const inviteForm = ref({ email: '' }); const inviting = ref(false)
const showEditDialog = ref(false); const editing = ref(false); const editForm = ref({ id: '', name: '' })

async function fetchFamilies() { loading.value = true; try { const res = await familyApi.getMyFamilies(); families.value = res.data || [] } finally { loading.value = false } }
async function createFamily() {
  if (!createForm.value.name) { ElMessage.warning('请输入家庭名称'); return }
  creating.value = true; try { await familyApi.create({ name: createForm.value.name }); ElMessage.success('创建成功'); showCreateDialog.value = false; createForm.value.name = ''; fetchFamilies() } finally { creating.value = false }
}
async function showMembers(family) { currentFamily.value = family; const res = await familyApi.getMembers(family.id); members.value = res.data || []; showMemberDialog.value = true }
async function removeMember(member) {
  try { await ElMessageBox.confirm('确认移出该成员？', '提示'); await familyApi.removeMember(currentFamily.value.id, member.userId); ElMessage.success('已移出'); showMembers(currentFamily.value) } catch (e) { if (e !== 'cancel') throw e }
}
function showInvite(family) { currentFamily.value = family; inviteForm.value.email = ''; showInviteDialog.value = true }
async function inviteMember() {
  if (!inviteForm.value.email) { ElMessage.warning('请输入用户邮箱'); return }
  inviting.value = true; try { await familyApi.invite(currentFamily.value.id, inviteForm.value.email); ElMessage.success('邀请成功'); showInviteDialog.value = false } finally { inviting.value = false }
}
function showEdit(family) { editForm.value = { id: family.id, name: family.name }; showEditDialog.value = true }
async function editFamily() {
  if (!editForm.value.name) { ElMessage.warning('请输入家庭名称'); return }
  editing.value = true; try { await familyApi.update({ id: editForm.value.id, name: editForm.value.name }); ElMessage.success('修改成功'); showEditDialog.value = false; fetchFamilies() } finally { editing.value = false }
}
async function setDefault(family) { try { await familyApi.setDefault(family.id); ElMessage.success('已设为默认家庭'); fetchFamilies() } catch (e) { } }
onMounted(() => fetchFamilies())
</script>

<style lang="scss" scoped>
.table-container { background: #fff; border-radius: 12px; padding: 20px; box-shadow: 0 1px 3px rgba(0,0,0,0.06); }
</style>
