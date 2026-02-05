import axios, { AxiosInstance, AxiosRequestConfig, AxiosResponse, InternalAxiosRequestConfig } from 'axios'
import { ElMessage } from 'element-plus'
import type { ApiResponse } from '@/types'

// 創建 axios 實例
const service: AxiosInstance = axios.create({
  baseURL: '/api',
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 請求攔截器
service.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    // 從 localStorage 獲取 token
    const token = localStorage.getItem('token')
    if (token && config.headers) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    console.error('Request error:', error)
    return Promise.reject(error)
  }
)

// 響應攔截器
service.interceptors.response.use(
  (response: AxiosResponse) => {
    const res = response.data

    // 如果返回的狀態碼不是 200，則顯示錯誤
    if (!res.success) {
      ElMessage.error(res.message || 'Error')
      return Promise.reject(new Error(res.message || 'Error'))
    }

    return res
  },
  (error) => {
    console.error('Response error:', error)

    // 處理 401 未授權
    if (error.response?.status === 401) {
      ElMessage.error('登入已過期，請重新登入')
      localStorage.removeItem('token')
      window.location.href = '/login'
      return Promise.reject(error)
    }

    // 處理 403 禁止訪問（可能是 token 無效）
    if (error.response?.status === 403) {
      ElMessage.error('訪問被拒絕，請重新登入')
      localStorage.removeItem('token')
      localStorage.removeItem('username')
      window.location.href = '/login'
      return Promise.reject(error)
    }

    // 顯示錯誤訊息
    const message = error.response?.data?.message || error.message || '請求失敗'
    ElMessage.error(message)

    return Promise.reject(error)
  }
)

// 包裝請求方法以返回正確的類型
const request = <T = any>(config: AxiosRequestConfig): Promise<T> => {
  return service.request<T>(config) as unknown as Promise<T>
}

export default request
