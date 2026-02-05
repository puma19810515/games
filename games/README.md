# Slot Game API

一個基於Spring Boot的老虎機遊戲後端系統，提供JWT + Redis認證、投注和派彩功能。

## 功能特點

- JWT + Redis認證系統（註冊/登入/登出）
- 無狀態session管理
- Token黑名單機制（登出後token立即失效）
- 老虎機遊戲投注
- 自動派彩機制
- 餘額管理
- 交易記錄追蹤

## 技術棧

- Spring Boot 3.5.3
- Spring Security + JWT
- Redis（Token存儲）
- Spring Data JPA
- MySQL 8.0
- Lombok
- Maven

## 快速開始

### 1. 啟動資料庫和Redis

```bash
docker-compose up -d mysql redis
```

### 2. 編譯並運行應用

```bash
mvn clean install
mvn spring-boot:run
```

應用將在 http://localhost:8080 啟動

## API 端點

### 認證相關

#### 註冊
```bash
POST /api/auth/register
Content-Type: application/json

{
  "username": "player1",
  "password": "password123"
}
```

**回應：**
```json
{
  "success": true,
  "message": "Registration successful",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "username": "player1",
    "balance": 1000.00
  }
}
```

#### 登入
```bash
POST /api/auth/login
Content-Type: application/json

{
  "username": "player1",
  "password": "password123"
}
```

**回應：**
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "username": "player1",
    "balance": 1000.00
  }
}
```

#### 登出
```bash
POST /api/auth/logout
Authorization: Bearer {your_jwt_token}
```

**回應：**
```json
{
  "success": true,
  "message": "Logout successful",
  "data": null
}
```

**注意：** 登出後，該token將從Redis中移除並立即失效，無法再使用。

### 遊戲相關

#### 下注/旋轉
```bash
POST /api/game/spin
Authorization: Bearer {your_jwt_token}
Content-Type: application/json

{
  "amount": 100
}
```

**回應：**
```json
{
  "success": true,
  "message": "Success",
  "data": {
    "betId": 1,
    "result": ["🍒", "🍒", "🍒"],
    "betAmount": 100.00,
    "winAmount": 200.00,
    "isWin": true,
    "balanceBefore": 1000.00,
    "balanceAfter": 1100.00,
    "message": "Congratulations! You won 200.00!"
  }
}
```

#### 查詢餘額
```bash
GET /api/game/balance
Authorization: Bearer {your_jwt_token}
```

**回應：**
```json
{
  "success": true,
  "message": "Success",
  "data": {
    "username": "player1",
    "balance": 1100.00
  }
}
```

### 錢包相關

#### 存款（金額轉入）
```bash
POST /api/wallet/deposit
Authorization: Bearer {your_jwt_token}
Content-Type: application/json

{
  "amount": 500
}
```

**回應：**
```json
{
  "success": true,
  "message": "Deposit successful",
  "data": {
    "username": "player1",
    "balanceBefore": 1000.00,
    "balanceAfter": 1500.00,
    "amount": 500.00,
    "transactionType": "DEPOSIT",
    "message": "Deposit successful"
  }
}
```

#### 全額提款（金額轉出）
```bash
POST /api/wallet/withdraw-all
Authorization: Bearer {your_jwt_token}
```

**回應：**
```json
{
  "success": true,
  "message": "Withdraw all successful",
  "data": {
    "username": "player1",
    "balanceBefore": 1500.00,
    "balanceAfter": 0.00,
    "amount": 1500.00,
    "transactionType": "WITHDRAW",
    "message": "Withdraw all successful"
  }
}
```

**注意：** 全額提款會將帳戶餘額清零，提取所有可用餘額。

### RTP統計相關

#### 查詢RTP統計
```bash
GET /api/rtp/statistics
Authorization: Bearer {your_jwt_token}
```

**回應：**
```json
{
  "success": true,
  "message": "RTP statistics retrieved successfully",
  "data": {
    "targetRtp": 95.0,
    "actualRtp": 94.85,
    "totalBetAmount": 10000.00,
    "totalWinAmount": 9485.00,
    "totalBetCount": 100,
    "averageBet": 100.00,
    "averageWin": 94.85,
    "rtpDifference": -0.15,
    "rtpStatus": "OPTIMAL"
  }
}
```

**RTP狀態說明：**
- `OPTIMAL`: 實際RTP與目標RTP差異在±2%內
- `HIGH`: 實際RTP超過目標RTP 2%以上
- `LOW`: 實際RTP低於目標RTP 2%以上

#### 重置RTP統計
```bash
POST /api/rtp/reset
Authorization: Bearer {your_jwt_token}
```

**回應：**
```json
{
  "success": true,
  "message": "RTP statistics reset successfully",
  "data": null
}
```

## 遊戲規則

### RTP（玩家回報率）
- **目標RTP**: 90%
- **說明**: 長期來看，玩家每投注100元，平均可獲得90元回報
- **計算方式**: RTP = (總贏得金額 / 總投注金額) × 100%
- **統計週期**: 所有RTP統計數據保留30天

### 老虎機符號
- 💎 鑽石
- 7️⃣ 幸運7
- ⭐ 星星
- 🍉 西瓜
- 🍊 橘子
- 🍋 檸檬
- 🍒 櫻桃

### 符號出現機率（權重系統）
- 🍒 櫻桃: 15 (最常見)
- 🍋 檸檬: 13
- 🍊 橘子: 10
- 🍉 西瓜: 8
- ⭐ 星星: 6
- 💎 鑽石: 5 (稀有)
- 7️⃣ 幸運7: 1 (極稀有)

### 賠率表（三個相同符號）
- 💎 💎 💎 = 88倍 (極高賠率)
- 7️⃣ 7️⃣ 7️⃣ = 34倍 (最高賠率！)
- ⭐ ⭐ ⭐ = 13.5倍
- 🍉 🍉 🍉 = 8.8倍
- 🍊 🍊 🍊 = 4.9倍
- 🍋 🍋 🍋 = 3.4倍
- 🍒 🍒 🍒 = 2.4倍

### 特殊規則
- 兩個相同符號 = 1.74倍（返還一半投注金額）
- 最小投注金額：10
- 最大投注金額：1000
- 註冊初始餘額：1000

### 遊戲機制
- **權重隨機系統**: 稀有符號出現機率較低，但賠率更高
- **動態RTP**: 系統自動追蹤和計算實際RTP
- **公平性保證**: 符號權重和賠率配置可查看，確保遊戲公平

## 配置

可在 `application.yml` 中修改以下配置：

```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379
      password:
      database: 0

jwt:
  secret: 你的JWT密鑰
  expiration: 86400000  # Token有效期（毫秒）

game:
  slot:
    symbols:
      - CHERRY
      - LEMON
      - ORANGE
      - WATERMELON
      - STAR
      - DIAMOND
      - SEVEN
    min-bet: 10
    max-bet: 1000
    initial-balance: 1000
    target-rtp: 90.0  # 目標RTP百分比 (90% = 長期回報率)
    # 符號權重 - 數字越大，出現機率越高
    # 總權重58，調整後實際RTP約90%
    symbol-weights:
      CHERRY: 15 # 常見 (24.14%)
      LEMON: 13 # 常見 (22.41%)
      ORANGE: 10 # 普通 (17.24%)
      WATERMELON: 8 # 較少 (13.79%)
      STAR: 6 # 少見 (10.34%)
      DIAMOND: 5 # 稀有 (8.62%)
      SEVEN: 1 # 極稀有 (3.45%)
    # 三個相同符號的賠率倍數
    # 三個相同符號期望貢獻：約16%
    payout-multipliers:
      CHERRY: 2.4 # 最常見，小贏
      LEMON: 3.4 # 常見，中等贏
      ORANGE: 4.9 # 普通，不錯的贏
      WATERMELON: 8.8 # 較少見，好贏
      STAR: 13.5 # 少見，大贏
      DIAMOND: 34 # 稀有，巨大贏利
      SEVEN: 88 # 極稀有，最高賠率（777出現率約0.004%）
    symbol-display:
      CHERRY: "🍒"
      LEMON: "🍋"
      ORANGE: "🍊"
      WATERMELON: "🍉"
      STAR: "⭐"
      DIAMOND: "💎"
      SEVEN: "7️⃣"
    # 兩個相同符號的賠率
    # 兩個相同符號期望貢獻：約74%（總RTP = 16% + 74% = 90%）
    two-match-multiplier: 1.74
```

**RTP配置說明：**
- `target-rtp`: 設定遊戲目標RTP（建議90-98之間）
- `symbol-weights`: 控制各符號出現機率，權重總和為100
- `payout-multipliers`: 設定三個相同符號的賠率倍數
- `symbol-display`: 定義符號對應的表情符號
- `two-match-multiplier`: 兩個相同符號的賠率倍數

## 資料庫架構

### users（用戶表）
- id: 用戶ID
- username: 用戶名（唯一）
- password: 加密密碼
- balance: 帳戶餘額
- created_at: 創建時間
- updated_at: 更新時間

### bets（投注記錄表）
- id: 投注ID
- user_id: 用戶ID
- bet_amount: 投注金額
- win_amount: 獲勝金額
- result: 旋轉結果
- is_win: 是否獲勝
- created_at: 創建時間

### transactions（交易記錄表）
- id: 交易ID
- user_id: 用戶ID
- type: 交易類型（REGISTER/BET/WIN/DEPOSIT/WITHDRAW）
- amount: 交易金額
- balance_before: 交易前餘額
- balance_after: 交易後餘額
- description: 描述
- bet_id: 關聯投注ID
- created_at: 創建時間

## 測試範例

使用 curl 進行測試：

```bash
# 註冊新用戶
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"player1","password":"password123"}'

# 登入（獲取token）
TOKEN=$(curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"player1","password":"password123"}' \
  | jq -r '.data.token')

# 查詢餘額
curl -X GET http://localhost:8080/api/game/balance \
  -H "Authorization: Bearer $TOKEN"

# 下注
curl -X POST http://localhost:8080/api/game/spin \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"amount":100}'

# 存款
curl -X POST http://localhost:8080/api/wallet/deposit \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"amount":500}'

# 全額提款
curl -X POST http://localhost:8080/api/wallet/withdraw-all \
  -H "Authorization: Bearer $TOKEN"

# 查詢RTP統計
curl -X GET http://localhost:8080/api/rtp/statistics \
  -H "Authorization: Bearer $TOKEN"

# 登出
curl -X POST http://localhost:8080/api/auth/logout \
  -H "Authorization: Bearer $TOKEN"
```

## 安全性

- 密碼使用BCrypt加密
- 使用JWT進行身份驗證，配合Redis存儲實現完全無狀態
- JWT token存儲在Redis中，支持主動失效機制
- 登出功能會立即從Redis移除token，實現真正的登出
- 所有遊戲API需要JWT token驗證
- Token驗證同時檢查JWT簽名和Redis中的存在性
- Session管理採用完全無狀態設計

## 認證流程說明

1. **註冊/登入**：生成JWT token並存入Redis（有效期24小時）
2. **API請求**：提取Bearer token → 驗證JWT簽名 → 檢查Redis中是否存在 → 授權通過
3. **登出**：從Redis刪除token，token立即失效
4. **Token過期**：Redis自動過期清除（TTL機制）

## 開發建議

- 建議使用Postman或類似工具進行API測試
- JWT token有效期為24小時（Redis TTL自動管理）
- 確保MySQL和Redis服務正常運行
- 生產環境請更換JWT密鑰
- Redis密碼建議在生產環境中設置
