<template>
  <div class="register-page">
    <!-- 背景装饰 -->
    <div class="bg-orbs">
      <div class="orb orb-1"></div>
      <div class="orb orb-2"></div>
      <div class="orb orb-3"></div>
      <div class="orb orb-4"></div>
      <div class="orb orb-5"></div>
    </div>

    <!-- 分栏卡片 -->
    <div class="register-container">
      <!-- 左侧品牌面板 -->
      <div class="brand-panel">
        <div class="brand-pattern"></div>
        <div class="brand-glow"></div>
        <div class="brand-inner">
          <div class="logo-icon">
            <el-icon :size="28"><Coin /></el-icon>
          </div>
          <h1 class="brand-title">财小账</h1>
          <p class="brand-desc">家庭财务管理系统</p>
          <ul class="brand-features">
            <li>
              <span class="dot"></span>注册账号，开启智能记账
            </li>
            <li>
              <span class="dot"></span>加入家庭，协作管理开支
            </li>
            <li>
              <span class="dot"></span>数据可视，掌握财务状况
            </li>
          </ul>
        </div>
      </div>

      <!-- 右侧注册表单 -->
      <div class="form-panel">
        <div class="form-inner">
          <h2 class="form-heading">创建账号</h2>
          <p class="form-sub">注册后即可开始使用</p>

          <el-form ref="formRef" :model="form" :rules="rules" class="register-form" @keyup.enter="handleRegister">
            <el-form-item prop="username">
              <el-input
                v-model="form.username"
                placeholder="请输入用户名"
                :prefix-icon="User"
                size="large"
              />
            </el-form-item>
            <el-form-item prop="email">
              <el-input
                v-model="form.email"
                placeholder="请输入邮箱"
                :prefix-icon="Message"
                size="large"
              />
            </el-form-item>
            <el-form-item prop="emailCode">
              <div class="code-row">
                <el-input
                  v-model="form.emailCode"
                  placeholder="邮箱验证码"
                  size="large"
                  class="code-input"
                />
                <el-button
                  :disabled="countdown > 0"
                  size="large"
                  class="code-btn"
                  @click="sendCode"
                >
                  {{ countdown > 0 ? `${countdown}s` : '获取验证码' }}
                </el-button>
              </div>
            </el-form-item>
            <el-form-item prop="password">
              <el-input
                v-model="form.password"
                type="password"
                placeholder="请输入密码"
                :prefix-icon="Lock"
                size="large"
                show-password
              />
            </el-form-item>
            <el-form-item prop="confirmPassword">
              <el-input
                v-model="form.confirmPassword"
                type="password"
                placeholder="请确认密码"
                :prefix-icon="Lock"
                size="large"
                show-password
              />
            </el-form-item>
            <el-form-item>
              <el-button
                type="primary"
                size="large"
                class="submit-btn"
                :loading="loading"
                @click="handleRegister"
              >
                注 册
              </el-button>
            </el-form-item>
          </el-form>

          <div class="register-footer">
            <span>已有账号？</span>
            <router-link to="/login">立即登录</router-link>
          </div>
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
import { User, Lock, Message, Coin } from '@element-plus/icons-vue'

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
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 50, message: '用户名长度为2-50个字符', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '邮箱格式不正确', trigger: 'blur' }
  ],
  emailCode: [{ required: true, message: '请输入验证码', trigger: 'blur' }],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 32, message: '密码长度为6-32个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validatePass, trigger: 'blur' }
  ]
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
// =============================================
//  注册页 — 分栏布局（与登录页一致）
// =============================================

.register-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #0f0c29 0%, #1a1a2e 30%, #16213e 60%, #0f3460 100%);
  padding: 24px;
  position: relative;
  overflow: hidden;
}

// ---------------------------------------------
//  背景光晕
// ---------------------------------------------
.bg-orbs {
  position: absolute;
  inset: 0;
  pointer-events: none;
}

.orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(120px);
  opacity: 0.22;

  &.orb-1 {
    width: 600px; height: 600px;
    background: radial-gradient(circle, #409eff, #2563eb);
    top: -25%; left: -15%;
    animation: driftA 30s ease-in-out infinite;
  }
  &.orb-2 {
    width: 450px; height: 450px;
    background: radial-gradient(circle, #6366f1, #4f46e5);
    bottom: -20%; right: -12%;
    animation: driftB 35s ease-in-out infinite;
  }
  &.orb-3 {
    width: 350px; height: 350px;
    background: radial-gradient(circle, #06b6d4, #0891b2);
    top: 45%; left: 55%;
    animation: driftC 25s ease-in-out infinite;
  }
  &.orb-4 {
    width: 280px; height: 280px;
    background: radial-gradient(circle, #8b5cf6, #7c3aed);
    top: -8%; right: 25%;
    animation: driftD 28s ease-in-out infinite;
  }
  &.orb-5 {
    width: 320px; height: 320px;
    background: radial-gradient(circle, #ec4899, #db2777);
    bottom: 5%; left: 35%;
    animation: driftE 22s ease-in-out infinite;
  }
}

@keyframes driftA {
  0%, 100% { transform: translate(0, 0) scale(1); }
  25%  { transform: translate(90px, -60px) scale(1.08); }
  50%  { transform: translate(30px, 40px) scale(0.95); }
  75%  { transform: translate(-50px, -20px) scale(1.05); }
}
@keyframes driftB {
  0%, 100% { transform: translate(0, 0) scale(1); }
  25%  { transform: translate(-70px, -50px) scale(1.1); }
  50%  { transform: translate(40px, -30px) scale(0.92); }
  75%  { transform: translate(-20px, 60px) scale(1.04); }
}
@keyframes driftC {
  0%, 100% { transform: translate(0, 0); }
  33%  { transform: translate(-60px, -45px); }
  66%  { transform: translate(40px, 35px); }
}
@keyframes driftD {
  0%, 100% { transform: translate(0, 0) scale(1); }
  50%  { transform: translate(55px, -35px) scale(1.12); }
}
@keyframes driftE {
  0%, 100% { transform: translate(0, 0); }
  33%  { transform: translate(-40px, -25px); }
  66%  { transform: translate(50px, 20px); }
}

// ---------------------------------------------
//  主容器 — 分栏卡片
// ---------------------------------------------
.register-container {
  display: flex;
  max-width: 900px;
  width: 100%;
  min-height: 620px;
  border-radius: 20px;
  overflow: hidden;
  box-shadow:
    0 0 0 1px rgba(255,255,255,0.08),
    0 30px 100px rgba(0,0,0,0.45);
  position: relative;
  z-index: 1;
  animation: cardIn 0.7s cubic-bezier(0.22, 1, 0.36, 1) both;
}

@keyframes cardIn {
  from {
    opacity: 0;
    transform: translateY(30px) scale(0.97);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

// ---------------------------------------------
//  左侧 — 品牌面板
// ---------------------------------------------
.brand-panel {
  flex: 0 0 42%;
  background: linear-gradient(160deg, #1a1a2e 0%, #162447 50%, #0f3460 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
}

.brand-pattern {
  position: absolute;
  inset: 0;
  background-image: radial-gradient(circle, rgba(255,255,255,0.05) 1px, transparent 1px);
  background-size: 26px 26px;
  pointer-events: none;
}

.brand-glow {
  position: absolute;
  bottom: -30%;
  left: -20%;
  width: 350px;
  height: 350px;
  background: radial-gradient(circle, rgba(64,158,255,0.12), transparent);
  border-radius: 50%;
  pointer-events: none;
}

.brand-inner {
  text-align: center;
  position: relative;
  z-index: 1;
  padding: 48px 36px;
}

.logo-icon {
  width: 64px;
  height: 64px;
  margin: 0 auto 20px;
  background: linear-gradient(135deg, #409eff, #6366f1);
  border-radius: 18px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  box-shadow: 0 8px 32px rgba(64, 158, 255, 0.35);
  transition: transform 0.3s;

  &:hover {
    transform: scale(1.05);
  }
}

.brand-title {
  font-size: 28px;
  font-weight: 700;
  color: #fff;
  margin: 0 0 8px;
  letter-spacing: 3px;
}

.brand-desc {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.55);
  margin: 0 0 36px;
  letter-spacing: 0.5px;
}

.brand-features {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: 14px;
  align-items: center;

  li {
    display: flex;
    align-items: center;
    gap: 10px;
    font-size: 13.5px;
    color: rgba(255, 255, 255, 0.72);
    letter-spacing: 0.3px;

    .dot {
      width: 6px;
      height: 6px;
      border-radius: 50%;
      background: rgba(64, 158, 255, 0.7);
      flex-shrink: 0;
      box-shadow: 0 0 8px rgba(64, 158, 255, 0.4);
    }
  }
}

// ---------------------------------------------
//  右侧 — 注册表单
// ---------------------------------------------
.form-panel {
  flex: 1;
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
}

.form-inner {
  width: 100%;
  max-width: 380px;
  padding: 40px;
}

.form-heading {
  font-size: 26px;
  font-weight: 700;
  color: #1a1a2e;
  margin: 0 0 6px;
  letter-spacing: 0.5px;
}

.form-sub {
  font-size: 14px;
  color: #909399;
  margin: 0 0 32px;
}

// 表单
.register-form {
  :deep(.el-form-item) {
    margin-bottom: 18px;
  }

  :deep(.el-input__wrapper) {
    border-radius: 10px;
    padding: 2px 14px;
    background: #f5f7fa;
    box-shadow: none;
    border: 2px solid transparent;
    transition: all 0.25s;

    &:hover {
      background: #eef2f7;
    }

    &.is-focus {
      background: #fff;
      border-color: #409eff;
      box-shadow: 0 0 0 3px rgba(64, 158, 255, 0.1);
    }
  }

  :deep(.el-input__inner) {
    height: 46px;
    font-size: 14px;
  }

  :deep(.el-input__prefix) {
    color: #a8abb2;
    margin-right: 4px;
  }

  :deep(.el-input__suffix) {
    color: #a8abb2;
  }
}

// 验证码行
.code-row {
  display: flex;
  gap: 10px;

  .code-input {
    flex: 1;
  }

  .code-btn {
    flex-shrink: 0;
    min-width: 120px;
    border-radius: 10px;
    font-size: 13px;
  }
}

// 注册按钮
.submit-btn {
  width: 100%;
  height: 48px;
  font-size: 16px;
  font-weight: 600;
  border-radius: 10px;
  letter-spacing: 4px;
  border: none !important;
  background: linear-gradient(135deg, #409eff, #6366f1) !important;
  transition: all 0.3s;
  margin-top: 6px;

  &:hover {
    transform: translateY(-1px);
    box-shadow: 0 8px 28px rgba(64, 158, 255, 0.4) !important;
  }

  &:active {
    transform: translateY(0);
  }

  &.is-loading {
    background: linear-gradient(135deg, #6db5ff, #8b8cf7) !important;
  }
}

// 底部登录入口
.register-footer {
  text-align: center;
  margin-top: 24px;
  font-size: 14px;
  color: #909399;

  a {
    color: #409eff;
    text-decoration: none;
    font-weight: 500;
    margin-left: 4px;
    transition: all 0.2s;

    &:hover {
      color: #6366f1;
    }
  }
}

// ---------------------------------------------
//  移动端适配
// ---------------------------------------------
@media (max-width: 768px) {
  .register-page {
    padding: 16px;
    align-items: flex-start;
    padding-top: 24px;
  }

  .register-container {
    flex-direction: column;
    min-height: auto;
    max-width: 420px;
  }

  .brand-panel {
    flex: none;
  }

  .brand-inner {
    padding: 28px 24px;

    .brand-features {
      display: none;
    }

    .brand-desc {
      margin-bottom: 0;
    }
  }

  .logo-icon {
    width: 48px;
    height: 48px;
    margin-bottom: 12px;
    border-radius: 14px;
  }

  .brand-title {
    font-size: 22px;
    letter-spacing: 2px;
  }

  .form-inner {
    padding: 28px 24px;
  }

  .form-heading {
    font-size: 22px;
  }
}
</style>
