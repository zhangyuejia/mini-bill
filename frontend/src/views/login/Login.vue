<template>
  <div class="login-page">
    <!-- 背景装饰 -->
    <div class="bg-orbs">
      <div class="orb orb-1"></div>
      <div class="orb orb-2"></div>
      <div class="orb orb-3"></div>
      <div class="orb orb-4"></div>
      <div class="orb orb-5"></div>
    </div>

    <!-- 分栏卡片 -->
    <div class="login-container">
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
              <span class="dot"></span>收支记录，一目了然
            </li>
            <li>
              <span class="dot"></span>智能统计，数据可视
            </li>
            <li>
              <span class="dot"></span>家庭协作，共同管理
            </li>
          </ul>
        </div>
      </div>

      <!-- 右侧登录表单 -->
      <div class="form-panel">
        <div class="form-inner">
          <h2 class="form-heading">欢迎回来</h2>
          <p class="form-sub">登录您的账号继续使用</p>

          <el-form ref="formRef" :model="form" :rules="rules" class="login-form" @keyup.enter="handleLogin">
            <el-form-item prop="username">
              <el-input
                v-model="form.username"
                placeholder="请输入用户名"
                :prefix-icon="User"
                size="large"
              />
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
            <el-form-item>
              <el-button
                type="primary"
                size="large"
                class="submit-btn"
                :loading="loading"
                @click="handleLogin"
              >
                登 录
              </el-button>
            </el-form-item>
          </el-form>

          <div class="login-footer">
            <span>还没有账号？</span>
            <router-link to="/register">立即注册</router-link>
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
import { ElMessage } from 'element-plus'
import { User, Lock, Coin } from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()
const formRef = ref(null)
const loading = ref(false)

const form = reactive({ username: '', password: '' })

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

async function handleLogin() {
  if (!formRef.value) return
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    await userStore.login({ ...form })
    ElMessage.success('登录成功')
    router.push('/dashboard')
  } catch (e) { /* handled */ } finally { loading.value = false }
}
</script>

<style lang="scss" scoped>
// =============================================
//  登录页 — 分栏布局
// =============================================

.login-page {
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
.login-container {
  display: flex;
  max-width: 900px;
  width: 100%;
  min-height: 540px;
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

// 网点纹路
.brand-pattern {
  position: absolute;
  inset: 0;
  background-image: radial-gradient(circle, rgba(255,255,255,0.05) 1px, transparent 1px);
  background-size: 26px 26px;
  pointer-events: none;
}

// 底部光晕
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
//  右侧 — 登录表单
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
  padding: 48px 40px;
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
  margin: 0 0 36px;
}

// 表单
.login-form {
  :deep(.el-form-item) {
    margin-bottom: 22px;
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

// 登录按钮
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

// 底部注册入口
.login-footer {
  text-align: center;
  margin-top: 28px;
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
  .login-page {
    padding: 16px;
    align-items: flex-start;
    padding-top: 40px;
  }

  .login-container {
    flex-direction: column;
    min-height: auto;
    max-width: 420px;
  }

  .brand-panel {
    flex: none;
  }

  .brand-inner {
    padding: 32px 28px;

    .brand-features {
      display: none; // 移动端隐藏特性列表，节省空间
    }

    .brand-desc {
      margin-bottom: 0;
    }
  }

  .logo-icon {
    width: 52px;
    height: 52px;
    margin-bottom: 14px;
    border-radius: 15px;
  }

  .brand-title {
    font-size: 22px;
    letter-spacing: 2px;
  }

  .form-inner {
    padding: 32px 28px;
  }

  .form-heading {
    font-size: 22px;
  }
}
</style>
