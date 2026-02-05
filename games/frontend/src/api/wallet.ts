import request from '@/utils/request'
import type { ApiResponse, DepositRequest, WalletResponse } from '@/types'

// 存款
export const deposit = (data: DepositRequest) => {
  return request<ApiResponse<WalletResponse>>({
    url: '/wallet/deposit',
    method: 'post',
    data
  })
}

// 全額提款
export const withdrawAll = () => {
  return request<ApiResponse<WalletResponse>>({
    url: '/wallet/withdraw-all',
    method: 'post'
  })
}
