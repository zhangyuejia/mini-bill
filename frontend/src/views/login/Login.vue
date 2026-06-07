<template>
  <div class="login-page">
    <div class="bg-decoration">
      <div class="orb orb-1"></div>
      <div class="orb orb-2"></div>
      <div class="orb orb-3"></div>
    </div>
    <div class="login-container">
      <div class="login-card">
        <div class="login-header">
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
          <p class="subtitle">家庭财务管理系统</p>
        </div>

        <el-form ref="formRef" :model="form" :rules="rules" class="login-form" @keyup.enter="handleLogin">
          <el-form-item prop="username">
            <el-input v-model="form.username" placeholder="用户名" :prefix-icon="User" size="large" />
          </el-form-item>
          <el-form-item prop="password">
            <el-input v-model="form.password" type="password" placeholder="密码" :prefix-icon="Lock" size="large" show-password />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" size="large" class="submit-btn" :loading="loading" @click="handleLogin">
              登 录
            </el-button>
          </el-form-item>
        </el-form>

        <div class="login-footer">
          <span>还没有账号？</span>
          <router-link to="/register" class="register-link">立即注册</router-link>
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
import { User, Lock } from '@element-plus/icons-vue'

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
.login-page {
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
  position: absolute;
  inset: 0;
  pointer-events: none;
}

.orb {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  opacity: 0.4;
  animation: float 20s ease-in-out infinite;

  &.orb-1 {
    width: 400px;
    height: 400px;
    background: radial-gradient(circle, #409eff, #337ecc);
    top: -10%;
    left: -10%;
    animation-delay: 0s;
  }

  &.orb-2 {
    width: 350px;
    height: 350px;
    background: radial-gradient(circle, #67c23a, #529b2e);
    bottom: -15%;
    right: -10%;
    animation-delay: -5s;
  }

  &.orb-3 {
    width: 200px;
    height: 200px;
    background: radial-gradient(circle, #e6a23c, #d4880f);
    top: 50%;
    left: 50%;
    animation-delay: -10s;
  }
}

@keyframes float {
  0%, 100% { transform: translate(0, 0) scale(1); }
  33% { transform: translate(60px, -40px) scale(1.1); }
  66% { transform: translate(-30px, 30px) scale(0.9); }
}

.login-container {
  width: 100%;
  max-width: 420px;
  position: relative;
  z-index: 1;
}

.login-card {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border-radius: 24px;
  padding: 48px 36px 36px;
  box-shadow: 0 25px 80px rgba(0, 0, 0, 0.3);
  transition: transform 0.3s ease;

  &:hover {
    transform: translateY(-2px);
  }
}

.login-header {
  text-align: center;
  margin-bottom: 36px;

  .logo-icon {
    margin-bottom: 16px;
    display: inline-block;
  }

  .logo-text {
    font-size: 26px;
    font-weight: 700;
    color: #1a1a2e;
    margin-bottom: 6px;
    letter-spacing: 2px;
  }

  .subtitle {
    font-size: 14px;
    color: #909399;
    margin: 0;
    letter-spacing: 1px;
  }
}

.login-form {
  :deep(.el-input__wrapper) {
    border-radius: 12px;
    padding: 4px 16px;
    background: #f5f7fa;
    transition: all 0.3s;

    &:hover {
      background: #ecf5ff;
    }
  }

  :deep(.el-input__inner) {
    height: 44px;
  }

  :deep(.el-form-item) {
    margin-bottom: 24px;
  }

  .submit-btn {
    width: 100%;
    height: 48px;
    font-size: 16px;
    border-radius: 12px;
    font-weight: 600;
    letter-spacing: 4px;
    background: linear-gradient(135deg, #409eff, #337ecc);
    border: none;
    transition: all 0.3s;

    &:hover {
      transform: translateY(-1px);
      box-shadow: 0 8px 25px rgba(64, 158, 255, 0.4);
    }

    &:active {
      transform: translateY(0);
    }
  }
}

.login-footer {
  text-align: center;
  margin-top: 28px;
  font-size: 14px;
  color: #909399;

  .register-link {
    color: #409eff;
    text-decoration: none;
    margin-left: 4px;
    font-weight: 500;
    transition: color 0.2s;

    &:hover {
      color: #337ecc;
      text-decoration: underline;
    }
  }
}
</style>
