import request from '@/utils/request'
import type { ApiResponse, LoginRequest, RegisterRequest, AuthResponse } from '@/types'

// 註冊
export const register = (data: RegisterRequest) => {
  return request<ApiResponse<AuthResponse>>({
    url: '/auth/register',
    method: 'post',
    data
  })
}

// 登入
export const login = (data: LoginRequest) => {
  return request<ApiResponse<AuthResponse>>({
    url: '/auth/login',
    method: 'post',
    data
  })
}

// 登出
export const logout = () => {
  return request<ApiResponse<null>>({
    url: '/auth/logout',
    method: 'post'
  })
}
