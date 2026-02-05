import request from '@/utils/request'
import type { ApiResponse, SpinRequest, SpinResult, BalanceResponse } from '@/types'

// 旋轉/下注
export const spin = (data: SpinRequest, gameCode: string = '0000') => {
  return request<ApiResponse<SpinResult>>({
    url: `/game/spin/${gameCode}`,
    method: 'post',
    data
  })
}

// 查詢餘額
export const getBalance = () => {
  return request<ApiResponse<BalanceResponse>>({
    url: '/game/balance',
    method: 'get'
  })
}
