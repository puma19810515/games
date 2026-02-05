<template>
  <div class="auth-container">
    <el-card class="auth-card">
      <template #header>
        <div class="card-header">
          <h2>註冊新帳號</h2>
        </div>
      </template>

      <el-form :model="registerForm" :rules="rules" ref="registerFormRef" label-width="100px">
        <el-form-item label="用戶名" prop="username">
          <el-input
            v-model="registerForm.username"
            placeholder="請輸入用戶名"
            :prefix-icon="User"
          />
        </el-form-item>

        <el-form-item label="密碼" prop="password">
          <el-input
            v-model="registerForm.password"
            type="password"
            placeholder="請輸入密碼"
            :prefix-icon="Lock"
            show-password
          />
        </el-form-item>

        <el-form-item label="確認密碼" prop="confirmPassword">
          <el-input
            v-model="registerForm.confirmPassword"
            type="password"
            placeholder="請再次輸入密碼"
            :prefix-icon="Lock"
            show-password
            @keyup.enter="handleRegister"
          />
        </el-form-item>

        <el-alert
          title="註冊成功後將獲得 1000 元初始餘額"
          type="success"
          :closable="false"
          style="margin-bottom: 20px"
        />

        <el-form-item>
          <el-button
            type="primary"
            :loading="loading"
            @click="handleRegister"
            style="width: 100%"
          >
            註冊
          </el-button>
        </el-form-item>

        <el-form-item>
          <el-button
            text
            @click="goToLogin"
            style="width: 100%"
          >
            已有帳號？返回登入
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

const registerFormRef = ref<FormInstance>()
const loading = ref(false)

const registerForm = reactive({
  username: '',
  password: '',
  confirmPassword: ''
})

const validateConfirmPassword = (rule: any, value: any, callback: any) => {
  if (value === '') {
    callback(new Error('請再次輸入密碼'))
  } else if (value !== registerForm.password) {
    callback(new Error('兩次輸入的密碼不一致'))
  } else {
    callback()
  }
}

const rules: FormRules = {
  username: [
    { required: true, message: '請輸入用戶名', trigger: 'blur' },
    { min: 3, max: 20, message: '用戶名長度應為 3-20 個字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '請輸入密碼', trigger: 'blur' },
    { min: 6, message: '密碼長度至少為 6 個字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

const handleRegister = async () => {
  if (!registerFormRef.value) return

  await registerFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const success = await userStore.doRegister({
          username: registerForm.username,
          password: registerForm.password
        })
        if (success) {
          router.push('/game')
        }
      } finally {
        loading.value = false
      }
    }
  })
}

const goToLogin = () => {
  router.push('/login')
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
  max-width: 500px;
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

  :deep(.el-alert) {
    font-size: 13px;
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
      width: 80px !important;
      font-size: 13px;
    }

    .el-input {
      font-size: 14px;
    }

    .el-button {
      font-size: 14px;
    }
  }

  :deep(.el-alert) {
    padding: 8px 12px;
    font-size: 12px;

    .el-alert__title {
      font-size: 12px;
    }
  }
}
</style>
