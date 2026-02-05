import request from '@/utils/request'
import type { ApiResponse, RtpStatistics, AllGamesRtpStatistics } from '@/types'

// 查詢單個遊戲的 RTP 統計
export const getRtpStatistics = (gameCode: string) => {
  return request<ApiResponse<RtpStatistics>>({
    url: `/rtp/statistics/${gameCode}`,
    method: 'get'
  })
}

// 查詢所有遊戲的 RTP 統計
export const getAllGamesRtpStatistics = () => {
  return request<ApiResponse<AllGamesRtpStatistics>>({
    url: '/rtp/statistics/all',
    method: 'get'
  })
}

// 重置 RTP 統計
export const resetRtpStatistics = (gameCode: string) => {
  return request<ApiResponse<null>>({
    url: `/rtp/reset/${gameCode}`,
    method: 'post'
  })
}
