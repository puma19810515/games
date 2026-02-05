<template>
  <div class="auth-container">
    <el-card class="auth-card">
      <template #header>
        <div class="card-header">
          <h2>老虎機遊戲登入</h2>
        </div>
      </template>

      <el-form :model="loginForm" :rules="rules" ref="loginFormRef" label-width="80px">
        <el-form-item label="用戶名" prop="username">
          <el-input
            v-model="loginForm.username"
            placeholder="請輸入用戶名"
            :prefix-icon="User"
          />
        </el-form-item>

        <el-form-item label="密碼" prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="請輸入密碼"
            :prefix-icon="Lock"
            show-password
            @keyup.enter="handleLogin"
          />
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            :loading="loading"
            @click="handleLogin"
            style="width: 100%"
          >
            登入
          </el-button>
        </el-form-item>

        <el-form-item>
          <el-button
            text
            @click="goToRegister"
            style="width: 100%"
          >
            還沒有帳號？點擊註冊
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { User, Lock } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()

const loginFormRef = ref<FormInstance>()
const loading = ref(false)

const loginForm = reactive({
  username: '',
  password: ''
})

const rules: FormRules = {
  username: [
    { required: true, message: '請輸入用戶名', trigger: 'blur' },
    { min: 3, max: 20, message: '用戶名長度應為 3-20 個字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '請輸入密碼', trigger: 'blur' },
    { min: 6, message: '密碼長度至少為 6 個字符', trigger: 'blur' }
  ]
}

const handleLogin = async () => {
  if (!loginFormRef.value) return

  await loginFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const success = await userStore.doLogin(loginForm)
        if (success) {
          router.push('/game')
        }
      } finally {
        loading.value = false
      }
    }
  })
}

const goToRegister = () => {
  router.push('/register')
}
</script>

<style scoped lang="scss">
.auth-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  padding: 20px;
}

.auth-card {
  width: 100%;
  max-width: 450px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);

  .card-header {
    text-align: center;

    h2 {
      margin: 0;
      color: #333;
      font-size: 24px;
    }
  }
}

// 平板裝置 (768px 以下)
@media (max-width: 768px) {
  .auth-container {
    padding: 15px;
  }

  .auth-card {
    max-width: 100%;

    .card-header h2 {
      font-size: 22px;
    }
  }

  :deep(.el-form-item__label) {
    font-size: 14px;
  }
}

// 手機裝置 (480px 以下)
@media (max-width: 480px) {
  .auth-container {
    padding: 10px;
  }

  .auth-card {
    .card-header h2 {
      font-size: 20px;
    }
  }

  :deep(.el-form) {
    .el-form-item__label {
      width: 70px !important;
      font-size: 13px;
    }

    .el-input {
      font-size: 14px;
    }

    .el-button {
      font-size: 14px;
    }
  }
}
</style>
