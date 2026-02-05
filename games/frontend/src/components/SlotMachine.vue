<template>
  <div class="slot-machine">
    <div class="reels-container">
      <div
        v-for="(symbol, index) in displaySymbols"
        :key="index"
        class="reel"
        :class="{ spinning: isSpinning }"
      >
        <div class="symbol">{{ symbol }}</div>
      </div>
    </div>

    <div class="result-message" v-if="resultMessage">
      <el-alert
        :title="resultMessage"
        :type="isWin ? 'success' : 'info'"
        :closable="false"
        show-icon
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'

interface Props {
  result?: string[]
  isSpinning: boolean
  isWin?: boolean
  winAmount?: number
}

const props = withDefaults(defineProps<Props>(), {
  result: () => ['🍒', '🍒', '🍒'],
  isSpinning: false,
  isWin: false,
  winAmount: 0
})

const displaySymbols = ref<string[]>(['🍒', '🍒', '🍒'])
const resultMessage = ref<string>('')

// 監聽結果變化
watch(() => props.result, (newResult) => {
  if (newResult && newResult.length === 3) {
    // 旋轉結束後更新顯示
    setTimeout(() => {
      displaySymbols.value = [...newResult]

      if (props.isWin && props.winAmount) {
        resultMessage.value = `恭喜中獎！贏得 ${props.winAmount} 元！`
      } else {
        resultMessage.value = '很遺憾，請再試一次！'
      }

      // 5秒後清除訊息
      setTimeout(() => {
        resultMessage.value = ''
      }, 5000)
    }, 2000)
  }
})

// 監聽旋轉狀態
watch(() => props.isSpinning, (spinning) => {
  if (spinning) {
    resultMessage.value = ''
    // 旋轉時隨機更換符號動畫
    const symbols = ['🍒', '🍋', '🍊', '🍉', '⭐', '💎', '7️⃣']
    const interval = setInterval(() => {
      if (props.isSpinning) {
        displaySymbols.value = [
          symbols[Math.floor(Math.random() * symbols.length)],
          symbols[Math.floor(Math.random() * symbols.length)],
          symbols[Math.floor(Math.random() * symbols.length)]
        ]
      } else {
        clearInterval(interval)
      }
    }, 100)
  }
})
</script>

<style scoped lang="scss">
.slot-machine {
  margin: 30px 0;

  .reels-container {
    display: flex;
    justify-content: center;
    gap: 20px;
    padding: 40px;
    background: linear-gradient(145deg, #1e1e1e, #2d2d2d);
    border-radius: 20px;
    box-shadow: 0 10px 40px rgba(0, 0, 0, 0.3);
  }

  .reel {
    width: 120px;
    height: 140px;
    background: linear-gradient(145deg, #ffffff, #f0f0f0);
    border-radius: 15px;
    display: flex;
    align-items: center;
    justify-content: center;
    box-shadow:
      inset 0 -3px 10px rgba(0, 0, 0, 0.1),
      0 5px 15px rgba(0, 0, 0, 0.2);
    transition: all 0.3s ease;

    &.spinning {
      animation: spin 0.1s linear infinite;
    }

    .symbol {
      font-size: 80px;
      user-select: none;
      animation: symbolPulse 0.5s ease-in-out;
    }
  }

  .result-message {
    margin-top: 30px;
    text-align: center;
    animation: fadeIn 0.5s ease-in-out;
  }

  // 平板裝置 (768px 以下)
  @media (max-width: 768px) {
    margin: 20px 0;

    .reels-container {
      gap: 15px;
      padding: 30px 20px;
      border-radius: 15px;
    }

    .reel {
      width: 90px;
      height: 110px;
      border-radius: 12px;

      .symbol {
        font-size: 60px;
      }
    }

    .result-message {
      margin-top: 20px;
    }
  }

  // 手機裝置 (480px 以下)
  @media (max-width: 480px) {
    margin: 15px 0;

    .reels-container {
      gap: 10px;
      padding: 20px 15px;
      border-radius: 12px;
    }

    .reel {
      width: 70px;
      height: 85px;
      border-radius: 10px;

      .symbol {
        font-size: 45px;
      }
    }

    .result-message {
      margin-top: 15px;
    }
  }
}

@keyframes spin {
  0%, 100% {
    transform: translateY(-5px);
  }
  50% {
    transform: translateY(5px);
  }
}

@keyframes symbolPulse {
  0% {
    transform: scale(0.8);
    opacity: 0.5;
  }
  100% {
    transform: scale(1);
    opacity: 1;
  }
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
