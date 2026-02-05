<template>
  <div class="statistics-page">
    <!-- 頂部導航欄 -->
    <el-header class="statistics-header">
      <div class="header-content">
        <el-button @click="router.push('/game')" :icon="ArrowLeft">
          返回遊戲
        </el-button>
        <h1>RTP 統計數據</h1>
        <el-button type="danger" @click="handleReset" :icon="RefreshLeft">
          重置統計
        </el-button>
      </div>
    </el-header>

    <!-- 主要內容區域 -->
    <el-main class="statistics-main">
      <div v-loading="loading" class="statistics-content">
        <el-alert
          title="RTP (Return to Player) 表示玩家長期遊戲的平均回報率"
          type="info"
          :closable="false"
          style="margin-bottom: 30px"
        />

        <!-- RTP 狀態卡片 -->
        <el-card class="status-card" v-if="statistics">
          <template #header>
            <div class="card-header">
              <el-icon class="header-icon"><TrendCharts /></el-icon>
              <span>RTP 狀態</span>
            </div>
          </template>

          <div class="rtp-status">
            <div class="status-badge" :class="statusClass">
              <el-icon class="status-icon">
                <component :is="statusIcon" />
              </el-icon>
              <span class="status-text">{{ statusText }}</span>
            </div>

            <div class="rtp-comparison">
              <div class="rtp-item">
                <p class="label">目標 RTP</p>
                <h2 class="value target">{{ statistics.targetRtp.toFixed(2) }}%</h2>
              </div>
              <div class="rtp-arrow">
                <el-icon><Right /></el-icon>
              </div>
              <div class="rtp-item">
                <p class="label">實際 RTP</p>
                <h2 class="value actual" :class="actualRtpClass">
                  {{ statistics.actualRtp.toFixed(2) }}%
                </h2>
              </div>
              <div class="rtp-item">
                <p class="label">差異</p>
                <h2 class="value difference" :class="differenceClass">
                  {{ statistics.rtpDifference > 0 ? '+' : '' }}{{ statistics.rtpDifference.toFixed(2) }}%
                </h2>
              </div>
            </div>
          </div>
        </el-card>

        <!-- 統計數據卡片 -->
        <el-row :gutter="20" style="margin-top: 30px" v-if="statistics">
          <el-col :xs="24" :sm="12" :md="6">
            <el-card class="stat-card">
              <el-statistic title="總投注金額"
                  :value="statistics.totalBetAmount.toFixed(2)">
                <template #prefix>
                  <el-icon><Money /></el-icon>
                </template>
                <template #suffix>元</template>
              </el-statistic>
            </el-card>
          </el-col>

          <el-col :xs="24" :sm="12" :md="6">
            <el-card class="stat-card">
              <el-statistic title="總贏得金額"
                  :value="statistics.totalWinAmount.toFixed(2)">
                <template #prefix>
                  <el-icon><Money /></el-icon>
                </template>
                <template #suffix>元</template>
              </el-statistic>
            </el-card>
          </el-col>

          <el-col :xs="24" :sm="12" :md="6">
            <el-card class="stat-card">
              <el-statistic title="總投注次數"
                            :value="statistics.totalBetCount">
                <template #prefix>
                  <el-icon><Money /></el-icon>
                </template>
                <template #suffix>次</template>
              </el-statistic>
            </el-card>
          </el-col>

          <el-col :xs="24" :sm="12" :md="6">
            <el-card class="stat-card">
              <el-statistic title="平均投注"
                            :value="statistics.averageBet.toFixed(2)">
                <template #prefix>
                  <el-icon><Money /></el-icon>
                </template>
                <template #suffix>元</template>
              </el-statistic>
            </el-card>
          </el-col>
        </el-row>

        <!-- 說明卡片 -->
        <el-card class="info-card" style="margin-top: 30px">
          <template #header>
            <div class="card-header">
              <el-icon class="header-icon"><QuestionFilled /></el-icon>
              <span>RTP 狀態說明</span>
            </div>
          </template>

          <el-row :gutter="20">
            <el-col :xs="24" :sm="8">
              <div class="info-item">
                <el-tag type="success" size="large">OPTIMAL</el-tag>
                <p>實際 RTP 與目標 RTP 差異在 ±2% 內，遊戲運行正常</p>
              </div>
            </el-col>
            <el-col :xs="24" :sm="8">
              <div class="info-item">
                <el-tag type="warning" size="large">HIGH</el-tag>
                <p>實際 RTP 超過目標 RTP 2% 以上，玩家獲利較多</p>
              </div>
            </el-col>
            <el-col :xs="24" :sm="8">
              <div class="info-item">
                <el-tag type="danger" size="large">LOW</el-tag>
                <p>實際 RTP 低於目標 RTP 2% 以上，玩家獲利較少</p>
              </div>
            </el-col>
          </el-row>
        </el-card>
      </div>
    </el-main>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getRtpStatistics, resetRtpStatistics } from '@/api/rtp'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  ArrowLeft,
  RefreshLeft,
  TrendCharts,
  Right,
  Money,
  QuestionFilled,
  SuccessFilled,
  WarningFilled,
  CircleCloseFilled
} from '@element-plus/icons-vue'
import type { RtpStatistics } from '@/types'

const router = useRouter()
const loading = ref(false)
const statistics = ref<RtpStatistics | null>(null)

const statusClass = computed(() => {
  if (!statistics.value) return ''
  switch (statistics.value.rtpStatus) {
    case 'OPTIMAL':
      return 'status-optimal'
    case 'HIGH':
      return 'status-high'
    case 'LOW':
      return 'status-low'
    default:
      return ''
  }
})

const statusIcon = computed(() => {
  if (!statistics.value) return SuccessFilled
  switch (statistics.value.rtpStatus) {
    case 'OPTIMAL':
      return SuccessFilled
    case 'HIGH':
      return WarningFilled
    case 'LOW':
      return CircleCloseFilled
    default:
      return SuccessFilled
  }
})

const statusText = computed(() => {
  if (!statistics.value) return ''
  switch (statistics.value.rtpStatus) {
    case 'OPTIMAL':
      return '運行正常'
    case 'HIGH':
      return '偏高'
    case 'LOW':
      return '偏低'
    default:
      return ''
  }
})

const actualRtpClass = computed(() => {
  if (!statistics.value) return ''
  if (statistics.value.actualRtp > statistics.value.targetRtp) return 'high'
  if (statistics.value.actualRtp < statistics.value.targetRtp) return 'low'
  return ''
})

const differenceClass = computed(() => {
  if (!statistics.value) return ''
  if (statistics.value.rtpDifference > 0) return 'positive'
  if (statistics.value.rtpDifference < 0) return 'negative'
  return ''
})

const fetchStatistics = async () => {
  loading.value = true
  try {
    const response = await getRtpStatistics('0000')
    if (response.success && response.data) {
      statistics.value = response.data
    }
  } catch (error) {
    console.error('Fetch statistics error:', error)
  } finally {
    loading.value = false
  }
}

const handleReset = async () => {
  try {
    await ElMessageBox.confirm(
      '確定要重置所有 RTP 統計數據嗎？此操作無法恢復。',
      '確認重置',
      {
        confirmButtonText: '確認重置',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    loading.value = true
    try {
      const response = await resetRtpStatistics('0000')
      if (response.success) {
        ElMessage.success('統計數據已重置')
        await fetchStatistics()
      }
    } catch (error) {
      console.error('Reset statistics error:', error)
    } finally {
      loading.value = false
    }
  } catch (error) {
    // 用戶取消
  }
}

onMounted(() => {
  fetchStatistics()
})
</script>

<style scoped lang="scss">
.statistics-page {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.statistics-header {
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
      flex: 1;
      text-align: center;
    }
  }
}

.statistics-main {
  flex: 1;
  padding: 40px 20px;

  .statistics-content {
    max-width: 1400px;
    margin: 0 auto;
  }
}

.status-card {
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

  .rtp-status {
    .status-badge {
      text-align: center;
      padding: 30px;
      border-radius: 10px;
      margin-bottom: 30px;

      .status-icon {
        font-size: 64px;
        margin-bottom: 15px;
      }

      .status-text {
        display: block;
        font-size: 24px;
        font-weight: bold;
      }

      &.status-optimal {
        background: linear-gradient(135deg, #67c23a22, #67c23a44);
        color: #67c23a;
      }

      &.status-high {
        background: linear-gradient(135deg, #e6a23c22, #e6a23c44);
        color: #e6a23c;
      }

      &.status-low {
        background: linear-gradient(135deg, #f56c6c22, #f56c6c44);
        color: #f56c6c;
      }
    }

    .rtp-comparison {
      display: flex;
      justify-content: space-around;
      align-items: center;
      gap: 20px;

      .rtp-item {
        text-align: center;
        flex: 1;

        .label {
          font-size: 14px;
          color: #909399;
          margin-bottom: 10px;
        }

        .value {
          font-size: 32px;
          font-weight: bold;
          margin: 0;

          &.target {
            color: #409eff;
          }

          &.actual.high {
            color: #e6a23c;
          }

          &.actual.low {
            color: #f56c6c;
          }

          &.difference.positive {
            color: #e6a23c;
          }

          &.difference.negative {
            color: #f56c6c;
          }
        }
      }

      .rtp-arrow {
        font-size: 32px;
        color: #dcdfe6;
      }
    }
  }
}

.stat-card {
  background: rgba(255, 255, 255, 0.95);
  border-radius: 15px;
  margin-bottom: 20px;
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
    color: #909399;

    .header-icon {
      font-size: 24px;
    }
  }

  .info-item {
    text-align: center;
    padding: 20px;

    .el-tag {
      margin-bottom: 15px;
    }

    p {
      color: #606266;
      font-size: 14px;
      line-height: 1.6;
    }
  }
}

// 平板裝置 (768px 以下)
@media (max-width: 768px) {
  .statistics-header {
    padding: 0 15px;

    .header-content {
      flex-wrap: wrap;
      padding: 10px 0;

      h1 {
        font-size: 22px;
        order: 1;
        width: 100%;
      }

      .el-button {
        font-size: 14px;

        &:first-child {
          order: 0;
        }

        &:last-child {
          order: 2;
        }
      }
    }
  }

  .statistics-main {
    padding: 20px 15px;
  }

  .status-card {
    .card-header {
      font-size: 18px;
    }

    .rtp-status {
      .status-badge {
        padding: 20px;

        .status-icon {
          font-size: 48px;
        }

        .status-text {
          font-size: 20px;
        }
      }

      .rtp-comparison {
        flex-direction: column;
        gap: 15px;

        .rtp-item {
          width: 100%;

          .value {
            font-size: 28px;
          }
        }

        .rtp-arrow {
          transform: rotate(90deg);
          font-size: 24px;
        }
      }
    }
  }

  .info-card {
    .info-item {
      margin-bottom: 15px;
    }
  }
}

// 手機裝置 (480px 以下)
@media (max-width: 480px) {
  .statistics-header {
    .header-content {
      gap: 8px;

      h1 {
        font-size: 18px;
      }

      .el-button {
        font-size: 12px;
        padding: 6px 10px;
      }
    }
  }

  .statistics-main {
    padding: 15px 10px;
  }

  .status-card {
    .card-header {
      font-size: 16px;

      .header-icon {
        font-size: 20px;
      }
    }

    .rtp-status {
      .status-badge {
        padding: 15px;

        .status-icon {
          font-size: 40px;
          margin-bottom: 10px;
        }

        .status-text {
          font-size: 18px;
        }
      }

      .rtp-comparison {
        gap: 10px;

        .rtp-item {
          .label {
            font-size: 12px;
          }

          .value {
            font-size: 22px;
          }
        }

        .rtp-arrow {
          font-size: 20px;
        }
      }
    }
  }

  .stat-card {
    :deep(.el-statistic) {
      .el-statistic__head {
        font-size: 13px;
      }

      .el-statistic__content {
        font-size: 20px;
      }
    }
  }

  .info-card {
    .card-header {
      font-size: 16px;

      .header-icon {
        font-size: 20px;
      }
    }

    .info-item {
      padding: 15px;

      .el-tag {
        font-size: 12px;
      }

      p {
        font-size: 12px;
      }
    }
  }
}
</style>
