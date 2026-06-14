<template>
  <div class="upload-area">
    <el-upload
      :action="uploadUrl"
      :headers="uploadHeaders"
      :show-file-list="false"
      :on-success="handleSuccess"
      :before-upload="beforeUpload"
      accept="image/*"
    >
      <el-button type="primary" size="small">选择图片</el-button>
      <template #tip>
        <span style="color:#909399;font-size:12px;margin-left:8px;">最多{{ max }}张</span>
      </template>
    </el-upload>
    <div v-if="list.length > 0" class="image-list">
      <div v-for="(att, idx) in list" :key="att.id || idx" class="image-item">
        <el-image
          :src="att.fileUrl"
          fit="cover"
          style="width:90px;height:90px;border-radius:6px;cursor:pointer"
          :preview-src-list="list.map(a => a.fileUrl)"
          preview-teleported
        />
        <el-button class="image-remove" circle size="small" type="danger" @click="$emit('remove', idx)">
          <el-icon><Close /></el-icon>
        </el-button>
      </div>
    </div>
    <div v-else style="color:#909399;font-size:13px;padding:8px 0;">暂未上传图片</div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Close } from '@element-plus/icons-vue'

const props = defineProps({
  prefix: { type: String, default: 'file' },
  list: { type: Array, default: () => [] },
  max: { type: Number, default: 10 }
})

defineEmits(['uploaded', 'remove'])

const uploadUrl = '/bus/api/file/upload?prefix=' + props.prefix
const uploadHeaders = { Authorization: 'Bearer ' + localStorage.getItem('token') }

function beforeUpload(file) {
  if (!file.type.startsWith('image/')) { ElMessage.warning('只能上传图片'); return false }
  if (file.size > 10 * 1024 * 1024) { ElMessage.warning('图片不能超过10MB'); return false }
  if (props.list.length >= props.max) { ElMessage.warning('最多上传' + props.max + '张'); return false }
  return true
}

function handleSuccess(res, file) {
  if (res.code !== 200) { ElMessage.error(res.msg || '上传失败'); return }
  props.list.push({ id: null, fileName: file.name, fileUrl: res.data, fileSize: file.size, _isNew: true })
}
</script>

<style lang="scss" scoped>
.image-list { display: flex; flex-wrap: wrap; gap: 8px; margin-top: 8px; }
.image-item { position: relative; }
.image-remove { position: absolute; top: -8px; right: -8px; width: 20px; height: 20px; }
</style>
