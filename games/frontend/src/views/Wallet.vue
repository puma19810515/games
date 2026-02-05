<template>
  <div class="wallet-page">
    <!-- 頂部導航欄 -->
    <el-header class="wallet-header">
      <div class="header-content">
        <el-button @click="router.push('/game')" :icon="ArrowLeft">
          返回遊戲
        </el-button>
        <h1>錢包管理</h1>
        <div class="balance-display">
          <el-tag size="large" type="warning">
            <el-icon><Coin /></el-icon>
            當前餘額: {{ userStore.balance.toFixed(2) }} 元
          </el-tag>
        </div>
      </div>
    </el-header>

    <!-- 主要內容區域 -->
    <el-main class="wallet-main">
      <el-row :gutter="30">
        <!-- 存款卡片 -->
        <el-col :xs="24" :sm="12">
          <el-card class="action-card deposit-card">
            <template #header>
              <div class="card-header">
                <el-icon class="header-icon"><Download /></el-icon>
                <span>存款</span>
              </div>
            </template>

            <el-form :model="depositForm" label-width="100px">
              <el-form-item label="存款金額">
                <el-input-number
                  v-model="depositForm.amount"
                  :min="1"
                  :max="100000"
                  :step="100"
                  style="width: 100%"
                />
              </el-form-item>

              <div class="quick-amount-buttons">
                <el-button
                  v-for="amount in [100, 500, 1000, 5000, 10000]"
                  :key="amount"
                  @click="depositForm.amount = amount"
                >
                  +{{ amount }}
                </el-button>
              </div>

              <el-form-item>
                <el-button
                  type="primary"
                  :loading="depositLoading"
                  @click="handleDeposit"
                  style="width: 100%"
                  size="large"
                >
                  <el-icon><Check /></el-icon>
                  確認存款
                </el-button>
              </el-form-item>
            </el-form>

            <el-alert
              title="存款後金額將立即加入您的帳戶餘額"
              type="info"
              :closable="false"
            />
          </el-card>
        </el-col>

        <!-- 提款卡片 -->
        <el-col :xs="24" :sm="12">
          <el-card class="action-card withdraw-card">
            <template #header>
              <div class="card-header">
                <el-icon class="header-icon"><Upload /></el-icon>
                <span>提款</span>
              </div>
            </template>

            <div class="withdraw-info">
              <p>可提款金額：</p>
              <h2>{{ userStore.balance.toFixed(2) }} 元</h2>
            </div>

            <el-button
              type="danger"
              :loading="withdrawLoading"
              :disabled="userStore.balance <= 0"
              @click="handleWithdrawAll"
              style="width: 100%"
              size="large"
            >
              <el-icon><Money /></el-icon>
              全額提款
            </el-button>

            <el-alert
              title="提款後會將所有餘額轉出，帳戶餘額將歸零"
              type="warning"
              :closable="false"
              style="margin-top: 20px"
            />
          </el-card>
        </el-col>
      </el-row>

      <!-- 交易說明 -->
      <el-card class="info-card" style="margin-top: 30px">
        <template #header>
          <div class="card-header">
            <el-icon class="header-icon"><InfoFilled /></el-icon>
            <span>重要說明</span>
          </div>
        </template>

        <el-row :gutter="20">
          <el-col :xs="24" :sm="8">
            <div class="info-item">
              <el-icon class="info-icon" color="#67c23a"><CircleCheck /></el-icon>
              <h3>存款</h3>
              <p>存款金額無上限，立即到帳，可用於遊戲投注</p>
            </div>
          </el-col>
          <el-col :xs="24" :sm="8">
            <div class="info-item">
              <el-icon class="info-icon" color="#e6a23c"><Warning /></el-icon>
              <h3>提款</h3>
              <p>提款會將所有餘額轉出，請確認後再操作</p>
            </div>
          </el-col>
          <el-col :xs="24" :sm="8">
            <div class="info-item">
              <el-icon class="info-icon" color="#409eff"><Clock /></el-icon>
              <h3>即時處理</h3>
              <p>所有交易即時處理，無需等待審核</p>
            </div>
          </el-col>
        </el-row>
      </el-card>
    </el-main>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { deposit as apiDeposit, withdrawAll as apiWithdrawAll } from '@/api/wallet'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  ArrowLeft,
  Coin,
  Download,
  Upload,
  Check,
  Money,
  InfoFilled,
  CircleCheck,
  Warning,
  Clock
} from '@element-plus/icons-vue'

const router = useRouter()
const userStore = useUserStore()

const depositLoading = ref(false)
const withdrawLoading = ref(false)

const depositForm = reactive({
  amount: 1000
})

const handleDeposit = async () => {
  if (depositForm.amount <= 0) {
    ElMessage.error('請輸入有效的存款金額')
    return
  }

  depositLoading.value = true
  try {
    const response = await apiDeposit({ amount: depositForm.amount })
    if (response.success && response.data) {
      ElMessage.success(response.data.message)
      // 更新餘額
      userStore.setBalance(response.data.balanceAfter)
      // 重置表單
      depositForm.amount = 1000
    }
  } catch (error) {
    console.error('Deposit error:', error)
  } finally {
    depositLoading.value = false
  }
}

const handleWithdrawAll = async () => {
  try {
    await ElMessageBox.confirm(
      `確定要提取全部餘額 ${userStore.balance.toFixed(2)} 元嗎？提款後帳戶餘額將歸零。`,
      '確認提款',
      {
        confirmButtonText: '確認提款',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    withdrawLoading.value = true
    try {
      const response = await apiWithdrawAll()
      if (response.success && response.data) {
        ElMessage.success(response.data.message)
        // 更新餘額
        userStore.setBalance(response.data.balanceAfter)
      }
    } catch (error) {
      console.error('Withdraw error:', error)
    } finally {
      withdrawLoading.value = false
    }
  } catch (error) {
    // 用戶取消
  }
}
</script>

<style scoped lang="scss">
.wallet-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.wallet-header {
  background: rgba(255, 255, 255, 0.95);
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  padding: 0 20px;

  .header-content {
    max-width: 1400px;
    margin: 0 auto;
    display: flex;
    justify-content: space-between;
    align-items: center;
    height: 100%;

    h1 {
      margin: 0;
      color: #333;
      font-size: 28px;
    }

    .balance-display {
      .el-tag {
        padding: 10px 20px;
        font-size: 16px;
      }
    }
  }
}

.wallet-main {
  flex: 1;
  padding: 40px 20px;
  max-width: 1400px;
  margin: 0 auto;
  width: 100%;
}

.action-card {
  background: rgba(255, 255, 255, 0.95);
  border-radius: 15px;
  height: 100%;

  .card-header {
    display: flex;
    align-items: center;
    gap: 10px;
    font-size: 20px;
    font-weight: bold;

    .header-icon {
      font-size: 24px;
    }
  }

  .quick-amount-buttons {
    display: flex;
    gap: 10px;
    flex-wrap: wrap;
    margin-bottom: 20px;
  }
}

.deposit-card {
  .card-header {
    color: #67c23a;
  }
}

.withdraw-card {
  .card-header {
    color: #f56c6c;
  }

  .withdraw-info {
    text-align: center;
    margin: 30px 0;

    p {
      font-size: 16px;
      color: #666;
      margin-bottom: 10px;
    }

    h2 {
      font-size: 36px;
      color: #303133;
      margin: 0;
    }
  }
}

.info-card {
  background: rgba(255, 255, 255, 0.95);
  border-radius: 15px;

  .card-header {
    display: flex;
    align-items: center;
    gap: 10px;
    font-size: 20px;
    font-weight: bold;
    color: #409eff;

    .header-icon {
      font-size: 24px;
    }
  }

  .info-item {
    text-align: center;
    padding: 20px;

    .info-icon {
      font-size: 48px;
      margin-bottom: 15px;
    }

    h3 {
      margin: 10px 0;
      font-size: 18px;
      color: #303133;
    }

    p {
      color: #909399;
      font-size: 14px;
      line-height: 1.6;
    }
  }
}

// 平板裝置 (768px 以下)
@media (max-width: 768px) {
  .wallet-header {
    padding: 0 15px;

    .header-content {
      flex-wrap: wrap;
      gap: 10px;
      padding: 10px 0;

      h1 {
        font-size: 22px;
        order: 1;
        flex: 1;
      }

      .el-button {
        order: 0;
        font-size: 14px;
      }

      .balance-display {
        order: 2;
        width: 100%;
        text-align: center;

        .el-tag {
          padding: 8px 15px;
          font-size: 14px;
        }
      }
    }
  }

  .wallet-main {
    padding: 20px 15px;
  }

  .action-card {
    margin-bottom: 20px;

    .card-header {
      font-size: 18px;
    }

    .quick-amount-buttons {
      gap: 8px;

      .el-button {
        flex: 1;
        min-width: 60px;
      }
    }
  }

  .withdraw-card {
    .withdraw-info {
      margin: 20px 0;

      h2 {
        font-size: 28px;
      }
    }
  }

  .info-card {
    .info-item {
      margin-bottom: 15px;

      .info-icon {
        font-size: 36px;
      }
    }
  }
}

// 手機裝置 (480px 以下)
@media (max-width: 480px) {
  .wallet-header {
    .header-content {
      h1 {
        font-size: 18px;
      }

      .el-button {
        font-size: 12px;
        padding: 6px 10px;
      }

      .balance-display {
        .el-tag {
          padding: 6px 12px;
          font-size: 12px;

          .el-icon {
            display: none;
          }
        }
      }
    }
  }

  .wallet-main {
    padding: 15px 10px;
  }

  .action-card {
    .card-header {
      font-size: 16px;

      .header-icon {
        font-size: 20px;
      }
    }

    .quick-amount-buttons {
      .el-button {
        min-width: 50px;
        font-size: 13px;
      }
    }

    :deep(.el-form-item__label) {
      font-size: 13px;
    }
  }

  .withdraw-card {
    .withdraw-info {
      margin: 15px 0;

      p {
        font-size: 14px;
      }

      h2 {
        font-size: 24px;
      }
    }
  }

  .info-card {
    .card-header {
      font-size: 16px;
    }

    .info-item {
      padding: 15px;

      .info-icon {
        font-size: 28px;
        margin-bottom: 10px;
      }

      h3 {
        font-size: 16px;
      }

      p {
        font-size: 12px;
      }
    }
  }
}
</style>
