<template>
  <div class="game-page">
    <!-- 頂部導航欄 -->
    <el-header class="game-header">
      <div class="header-content">
        <h1>老虎機遊戲</h1>
        <div class="user-info">
          <el-tag size="large" type="success">
            <el-icon><User /></el-icon>
            {{ userStore.username }}
          </el-tag>
          <el-tag size="large" type="warning">
            <el-icon><Coin /></el-icon>
            餘額: {{ userStore.balance.toFixed(2) }} 元
          </el-tag>
          <el-button type="info" @click="router.push('/wallet')">
            <el-icon><Wallet /></el-icon>
            錢包
          </el-button>
          <el-button type="primary" @click="router.push('/statistics')">
            <el-icon><DataAnalysis /></el-icon>
            統計
          </el-button>
          <el-button type="danger" @click="handleLogout">
            <el-icon><SwitchButton /></el-icon>
            登出
          </el-button>
        </div>
      </div>
    </el-header>

    <!-- 主要遊戲區域 -->
    <el-main class="game-main">
      <el-card class="game-card">
        <!-- 老虎機 -->
        <SlotMachine
          :result="spinResult?.result"
          :is-spinning="isSpinning"
          :is-win="spinResult?.isWin"
          :win-amount="spinResult?.winAmount"
        />

        <!-- 投注控制 -->
        <div class="bet-controls">
          <el-form :inline="true" :model="betForm" class="bet-form">
            <el-form-item label="投注金額">
              <el-input-number
                v-model="betForm.amount"
                :min="10"
                :max="1000"
                :step="10"
                :disabled="isSpinning"
              />
            </el-form-item>
            <el-form-item>
              <el-button
                type="primary"
                size="large"
                :loading="isSpinning"
                :disabled="isSpinning || userStore.balance < betForm.amount"
                @click="handleSpin"
              >
                <el-icon v-if="!isSpinning"><CaretRight /></el-icon>
                {{ isSpinning ? '旋轉中...' : '開始旋轉' }}
              </el-button>
            </el-form-item>
          </el-form>

          <div class="quick-bet-buttons">
            <el-button
              v-for="amount in [10, 50, 100, 500, 1000]"
              :key="amount"
              :disabled="isSpinning"
              @click="betForm.amount = amount"
            >
              {{ amount }}
            </el-button>
          </div>
        </div>

        <!-- 遊戲規則 -->
        <el-collapse class="game-rules">
          <el-collapse-item title="遊戲規則" name="1">
            <div class="rules-content">
              <h3>符號賠率（三個相同）</h3>
              <ul>
                <li>7️⃣ 7️⃣ 7️⃣ = 88倍（最高賠率）</li>
                <li>💎 💎 💎 = 34倍</li>
                <li>⭐ ⭐ ⭐ = 13.5倍</li>
                <li>🍉 🍉 🍉 = 8.8倍</li>
                <li>🍊 🍊 🍊 = 4.9倍</li>
                <li>🍋 🍋 🍋 = 3.4倍</li>
                <li>🍒 🍒 🍒 = 2.4倍</li>
              </ul>
              <h3>特殊規則</h3>
              <ul>
                <li>兩個相同符號 = 1.74倍（返還部分投注）</li>
                <li>最小投注：10 元</li>
                <li>最大投注：1000 元</li>
                <li>目標 RTP：90%</li>
              </ul>
            </div>
          </el-collapse-item>
        </el-collapse>
      </el-card>
    </el-main>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'
import { spin as apiSpin } from '@/api/game'
import SlotMachine from '@/components/SlotMachine.vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  User,
  Coin,
  Wallet,
  DataAnalysis,
  SwitchButton,
  CaretRight
} from '@element-plus/icons-vue'
import type { SpinResult } from '@/types'

const router = useRouter()
const userStore = useUserStore()

const isSpinning = ref(false)
const spinResult = ref<SpinResult | null>(null)

const betForm = reactive({
  amount: 100
})

const handleSpin = async () => {
  if (userStore.balance < betForm.amount) {
    ElMessage.error('餘額不足！')
    return
  }

  isSpinning.value = true
  spinResult.value = null

  // 1. 投注時立即扣除餘額
  const betAmount = betForm.amount
  const balanceBeforeBet = userStore.balance
  userStore.setBalance(balanceBeforeBet - betAmount)

  try {
    const response = await apiSpin({ amount: betAmount })
    if (response.success && response.data) {
      spinResult.value = response.data

      // 2. 旋轉結束後，如果贏了，加回贏得金額
      setTimeout(() => {
        if (response.data.isWin && response.data.winAmount > 0) {
          // 從當前餘額加上贏得金額
          userStore.setBalance(userStore.balance + response.data.winAmount)
        }

        // 顯示結果訊息
        if (response.data.isWin) {
          ElMessage.success({
            message: response.data.message,
            duration: 5000
          })
        }
      }, 2000)
    }
  } catch (error) {
    console.error('Spin error:', error)
    // 如果請求失敗，返還投注金額
    userStore.setBalance(balanceBeforeBet)
    ElMessage.error('投注失敗，請重試')
  } finally {
    // 旋轉動畫持續2秒
    setTimeout(() => {
      isSpinning.value = false
    }, 2000)
  }
}

const handleLogout = async () => {
  try {
    await ElMessageBox.confirm('確定要登出嗎？', '提示', {
      confirmButtonText: '確定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await userStore.doLogout()
    router.push('/login')
  } catch (error) {
    // 用戶取消
  }
}
</script>

<style scoped lang="scss">
.game-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.game-header {
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

    .user-info {
      display: flex;
      gap: 15px;
      align-items: center;

      .el-tag {
        padding: 10px 20px;
        font-size: 16px;
      }
    }
  }
}

.game-main {
  flex: 1;
  padding: 40px 20px;

  .game-card {
    max-width: 1200px;
    margin: 0 auto;
    background: rgba(255, 255, 255, 0.95);
    border-radius: 20px;
    padding: 30px;
  }
}

.bet-controls {
  margin-top: 40px;
  text-align: center;

  .bet-form {
    display: flex;
    justify-content: center;
    align-items: center;
    gap: 20px;
    margin-bottom: 20px;

    .el-form-item {
      margin-bottom: 0;
    }

    .el-input-number {
      width: 200px;
    }
  }

  .quick-bet-buttons {
    display: flex;
    justify-content: center;
    gap: 10px;
    flex-wrap: wrap;
  }
}

.game-rules {
  margin-top: 40px;

  .rules-content {
    h3 {
      margin-top: 15px;
      margin-bottom: 10px;
      color: #333;
    }

    ul {
      list-style: none;
      padding: 0;

      li {
        padding: 8px 0;
        font-size: 15px;
        color: #666;
      }
    }
  }
}

// 平板裝置 (768px 以下)
@media (max-width: 768px) {
  .game-header {
    padding: 0 15px;

    .header-content {
      flex-wrap: wrap;
      gap: 10px;
      padding: 10px 0;

      h1 {
        font-size: 22px;
        width: 100%;
        text-align: center;
      }

      .user-info {
        width: 100%;
        justify-content: center;
        gap: 8px;
        flex-wrap: wrap;

        .el-tag {
          padding: 8px 12px;
          font-size: 14px;
        }

        .el-button {
          padding: 8px 12px;
          font-size: 14px;
        }
      }
    }
  }

  .game-main {
    padding: 20px 15px;

    .game-card {
      border-radius: 15px;
      padding: 20px;
    }
  }

  .bet-controls {
    margin-top: 30px;

    .bet-form {
      flex-direction: column;
      gap: 15px;

      .el-input-number {
        width: 100%;
      }

      .el-button {
        width: 100%;
      }
    }

    .quick-bet-buttons {
      gap: 8px;

      .el-button {
        flex: 1 1 auto;
        min-width: 60px;
      }
    }
  }

  .game-rules {
    margin-top: 30px;
  }
}

// 手機裝置 (480px 以下)
@media (max-width: 480px) {
  .game-header {
    .header-content {
      h1 {
        font-size: 18px;
      }

      .user-info {
        gap: 6px;

        .el-tag {
          padding: 6px 10px;
          font-size: 12px;

          .el-icon {
            display: none;
          }
        }

        .el-button {
          padding: 6px 10px;
          font-size: 12px;

          .el-icon {
            margin-right: 0;
          }

          span {
            display: none;
          }
        }
      }
    }
  }

  .game-main {
    padding: 15px 10px;

    .game-card {
      padding: 15px;
    }
  }

  .bet-controls {
    margin-top: 20px;

    .bet-form {
      gap: 10px;
    }

    .quick-bet-buttons {
      gap: 6px;

      .el-button {
        min-width: 50px;
        padding: 8px 6px;
        font-size: 13px;
      }
    }
  }

  .game-rules {
    margin-top: 20px;

    .rules-content {
      ul li {
        font-size: 13px;
      }
    }
  }
}
</style>
