// API Response Types
export interface ApiResponse<T = any> {
  success: boolean
  message: string
  data: T
}

// User Types
export interface User {
  username: string
  balance: number
}

// Auth Types
export interface LoginRequest {
  username: string
  password: string
}

export interface RegisterRequest {
  username: string
  password: string
}

export interface AuthResponse {
  token: string
  username: string
  balance: number
}

// Game Types
export interface SpinRequest {
  amount: number
}

export interface SpinResult {
  betId: number
  result: string[]
  betAmount: number
  winAmount: number
  isWin: boolean
  balanceBefore: number
  balanceAfter: number
  message: string
}

export interface BalanceResponse {
  username: string
  balance: number
}

// Wallet Types
export interface DepositRequest {
  amount: number
}

export interface WalletResponse {
  username: string
  balanceBefore: number
  balanceAfter: number
  amount: number
  transactionType: 'DEPOSIT' | 'WITHDRAW'
  message: string
}

// RTP Types
export interface RtpStatistics {
  gameCode?: string              // 遊戲代碼（系統總體統計時為undefined）
  gameName?: string              // 遊戲名稱（可選）
  targetRtp: number
  actualRtp: number
  totalBetAmount: number
  totalWinAmount: number
  totalBetCount: number
  averageBet: number
  averageWin: number
  rtpDifference: number
  rtpStatus: 'OPTIMAL' | 'HIGH' | 'LOW'
}

// 所有遊戲的RTP統計
export interface AllGamesRtpStatistics {
  games: RtpStatistics[]         // 每個遊戲的統計
  systemRtp: RtpStatistics       // 系統總體統計
}

// Symbol Config
export interface SymbolConfig {
  name: string
  display: string
  weight: number
  multiplier: number
}
