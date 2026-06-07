<template>
  <div class="register-page">
    <div class="bg-decoration">
      <div class="orb orb-1"></div>
      <div class="orb orb-2"></div>
      <div class="orb orb-3"></div>
    </div>
    <div class="register-container">
      <div class="register-card">
        <div class="register-header">
          <div class="logo-icon">
            <svg width="48" height="48" viewBox="0 0 48 48" fill="none">
              <rect width="48" height="48" rx="12" fill="url(#logo-gradient)"/>
              <text x="24" y="32" text-anchor="middle" font-size="26" fill="white" font-weight="700">财</text>
              <defs>
                <linearGradient id="logo-gradient" x1="0" y1="0" x2="48" y2="48">
                  <stop offset="0%" stop-color="#409eff"/>
                  <stop offset="100%" stop-color="#337ecc"/>
                </linearGradient>
              </defs>
            </svg>
          </div>
          <h1 class="logo-text">财小账</h1>
          <p class="subtitle">注册新账号</p>
        </div>

        <el-form ref="formRef" :model="form" :rules="rules" class="register-form">
          <el-form-item prop="username">
            <el-input v-model="form.username" placeholder="用户名" :prefix-icon="User" size="large" />
          </el-form-item>
          <el-form-item prop="email">
            <el-input v-model="form.email" placeholder="邮箱" :prefix-icon="Message" size="large" />
          </el-form-item>
          <el-form-item prop="emailCode">
            <div class="code-input">
              <el-input v-model="form.emailCode" placeholder="邮箱验证码" size="large" />
              <el-button :disabled="countdown > 0" size="large" class="code-btn" @click="sendCode">
                {{ countdown > 0 ? `${countdown}s` : '获取验证码' }}
              </el-button>
            </div>
          </el-form-item>
          <el-form-item prop="password">
            <el-input v-model="form.password" type="password" placeholder="密码" :prefix-icon="Lock" size="large" show-password />
          </el-form-item>
          <el-form-item prop="confirmPassword">
            <el-input v-model="form.confirmPassword" type="password" placeholder="确认密码" :prefix-icon="Lock" size="large" show-password />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" size="large" class="submit-btn" :loading="loading" @click="handleRegister">
              注 册
            </el-button>
          </el-form-item>
        </el-form>

        <div class="register-footer">
          <span>已有账号？</span>
          <router-link to="/login" class="login-link">立即登录</router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { authApi } from '@/api'
import { ElMessage } from 'element-plus'
import { User, Lock, Message } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref(null)
const loading = ref(false)
const countdown = ref(0)
let timer = null

const form = reactive({ username: '', email: '', emailCode: '', password: '', confirmPassword: '' })

const validatePass = (rule, value, callback) => {
  if (value === '') callback(new Error('请确认密码'))
  else if (value !== form.password) callback(new Error('两次输入密码不一致'))
  else callback()
}

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }, { min: 2, max: 50, message: '用户名长度为2-50个字符', trigger: 'blur' }],
  email: [{ required: true, message: '请输入邮箱', trigger: 'blur' }, { type: 'email', message: '邮箱格式不正确', trigger: 'blur' }],
  emailCode: [{ required: true, message: '请输入验证码', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }, { min: 6, max: 32, message: '密码长度为6-32个字符', trigger: 'blur' }],
  confirmPassword: [{ required: true, message: '请确认密码', trigger: 'blur' }, { validator: validatePass, trigger: 'blur' }]
}

async function sendCode() {
  if (!form.email) { ElMessage.warning('请先输入邮箱'); return }
  try {
    await authApi.sendEmailCode(form.email)
    ElMessage.success('验证码已发送')
    countdown.value = 60
    timer = setInterval(() => { countdown.value--; if (countdown.value <= 0) clearInterval(timer) }, 1000)
  } catch (e) { }
}

async function handleRegister() {
  if (!formRef.value) return
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    await userStore.register({ ...form })
    ElMessage.success('注册成功')
    router.push('/dashboard')
  } catch (e) { } finally { loading.value = false }
}
</script>

<style lang="scss" scoped>
.register-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 40%, #0f3460 100%);
  padding: 16px;
  position: relative;
  overflow: hidden;
}

.bg-decoration {
  position: absolute; inset: 0; pointer-events: none;
}

.orb {
  position: absolute; border-radius: 50%; filter: blur(80px); opacity: 0.4;
  animation: float 20s ease-in-out infinite;
  &.orb-1 { width: 400px; height: 400px; background: radial-gradient(circle, #409eff, #337ecc); top: -10%; left: -10%; }
  &.orb-2 { width: 350px; height: 350px; background: radial-gradient(circle, #67c23a, #529b2e); bottom: -15%; right: -10%; animation-delay: -5s; }
  &.orb-3 { width: 200px; height: 200px; background: radial-gradient(circle, #e6a23c, #d4880f); top: 50%; left: 50%; animation-delay: -10s; }
}

@keyframes float {
  0%, 100% { transform: translate(0,0) scale(1); }
  33% { transform: translate(60px,-40px) scale(1.1); }
  66% { transform: translate(-30px,30px) scale(0.9); }
}

.register-container { width: 100%; max-width: 460px; position: relative; z-index: 1; }

.register-card {
  background: rgba(255,255,255,0.95); backdrop-filter: blur(20px);
  border-radius: 24px; padding: 44px 36px 32px;
  box-shadow: 0 25px 80px rgba(0,0,0,0.3);
  transition: transform 0.3s;
  &:hover { transform: translateY(-2px); }
}

.register-header {
  text-align: center; margin-bottom: 32px;
  .logo-icon { margin-bottom: 16px; display: inline-block; }
  .logo-text { font-size: 26px; font-weight: 700; color: #1a1a2e; margin-bottom: 6px; letter-spacing: 2px; }
  .subtitle { font-size: 14px; color: #909399; margin: 0; letter-spacing: 1px; }
}

.register-form {
  :deep(.el-input__wrapper) { border-radius: 12px; padding: 4px 16px; background: #f5f7fa; transition: all 0.3s; &:hover { background: #ecf5ff; } }
  :deep(.el-input__inner) { height: 44px; }
  :deep(.el-form-item) { margin-bottom: 22px; }
  .submit-btn { width: 100%; height: 48px; font-size: 16px; border-radius: 12px; font-weight: 600; letter-spacing: 4px; background: linear-gradient(135deg,#409eff,#337ecc); border: none; transition: all 0.3s;
    &:hover { transform: translateY(-1px); box-shadow: 0 8px 25px rgba(64,158,255,0.4); }
  }
}

.code-input { display: flex; gap: 10px;
  .el-input { flex: 1; }
  .code-btn { flex-shrink: 0; border-radius: 12px; min-width: 120px; }
}

.register-footer {
  text-align: center; margin-top: 24px; font-size: 14px; color: #909399;
  .login-link { color: #409eff; text-decoration: none; margin-left: 4px; font-weight: 500; transition: color 0.2s;
    &:hover { color: #337ecc; text-decoration: underline; }
  }
}
</style>
