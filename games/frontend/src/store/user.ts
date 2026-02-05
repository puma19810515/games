import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login, register, logout } from '@/api/auth'
import { getBalance } from '@/api/game'
import type { LoginRequest, RegisterRequest } from '@/types'
import { ElMessage } from 'element-plus'

export const useUserStore = defineStore('user', () => {
  // 狀態
  const token = ref<string>(localStorage.getItem('token') || '')
  const username = ref<string>(localStorage.getItem('username') || '')
  const balance = ref<number>(0)
  const isLoggedIn = computed(() => !!token.value)

  // 設置 token
  const setToken = (newToken: string) => {
    token.value = newToken
    localStorage.setItem('token', newToken)
  }

  // 設置用戶信息
  const setUserInfo = (name: string, bal: number) => {
    username.value = name
    balance.value = bal
    localStorage.setItem('username', name)
  }

  // 清除用戶信息
  const clearUserInfo = () => {
    token.value = ''
    username.value = ''
    balance.value = 0
    localStorage.removeItem('token')
    localStorage.removeItem('username')
  }

  // 登入
  const doLogin = async (loginData: LoginRequest) => {
    try {
      const response = await login(loginData)
      if (response.success && response.data) {
        setToken(response.data.token)
        setUserInfo(response.data.username, response.data.balance)
        ElMessage.success('登入成功！')
        return true
      }
      return false
    } catch (error) {
      console.error('Login error:', error)
      return false
    }
  }

  // 註冊
  const doRegister = async (registerData: RegisterRequest) => {
    try {
      const response = await register(registerData)
      if (response.success && response.data) {
        setToken(response.data.token)
        setUserInfo(response.data.username, response.data.balance)
        ElMessage.success('註冊成功！')
        return true
      }
      return false
    } catch (error) {
      console.error('Register error:', error)
      return false
    }
  }

  // 登出
  const doLogout = async () => {
    try {
      await logout()
      clearUserInfo()
      ElMessage.success('登出成功！')
      return true
    } catch (error) {
      console.error('Logout error:', error)
      clearUserInfo()
      return false
    }
  }

  // 更新餘額
  const updateBalance = async () => {
    try {
      const response = await getBalance()
      if (response.success && response.data) {
        balance.value = response.data.balance
      }
    } catch (error) {
      console.error('Update balance error:', error)
    }
  }

  // 手動設置餘額（用於遊戲後立即更新）
  const setBalance = (newBalance: number) => {
    balance.value = newBalance
  }

  return {
    token,
    username,
    balance,
    isLoggedIn,
    doLogin,
    doRegister,
    doLogout,
    updateBalance,
    setBalance,
    clearUserInfo
  }
})
