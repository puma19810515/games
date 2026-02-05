# 老虎機遊戲前端

這是一個使用 Vue 3 + TypeScript + Element Plus 開發的老虎機遊戲前端應用。

## 技術棧

- **Vue 3** - 漸進式 JavaScript 框架
- **TypeScript** - 類型安全的 JavaScript 超集
- **Element Plus** - 基於 Vue 3 的組件庫
- **Vue Router** - Vue.js 官方路由管理器
- **Pinia** - Vue 的狀態管理庫
- **Axios** - 基於 Promise 的 HTTP 客戶端
- **Sass** - CSS 預處理器

## 功能特點

### 1. 用戶認證
- 註冊新帳號
- 用戶登入
- 安全登出
- JWT Token 管理

### 2. 老虎機遊戲
- 精美的老虎機動畫效果
- 支援多種投注金額（10-1000 元）
- 即時顯示遊戲結果
- 自動更新餘額
- 完整的遊戲規則說明

### 3. 錢包管理
- 便捷的存款功能
- 快速金額選擇
- 全額提款功能
- 即時餘額更新

### 4. RTP 統計
- 即時 RTP 統計數據
- 視覺化狀態顯示
- 詳細的統計報告
- 統計數據重置功能

## 快速開始

### 1. 安裝依賴

```bash
cd frontend
npm install
```

### 2. 啟動開發服務器

```bash
npm run serve
```

應用將在 http://localhost:3000 啟動

### 3. 構建生產版本

```bash
npm run build
```

構建產物將輸出到 `dist` 目錄

## 目錄結構

```
frontend/
├── public/              # 靜態資源
│   └── index.html      # HTML 模板
├── src/
│   ├── api/            # API 調用
│   │   ├── auth.ts     # 認證相關 API
│   │   ├── game.ts     # 遊戲相關 API
│   │   ├── wallet.ts   # 錢包相關 API
│   │   └── rtp.ts      # RTP 統計 API
│   ├── assets/         # 靜態資源（圖片、樣式等）
│   ├── components/     # Vue 組件
│   │   └── SlotMachine.vue  # 老虎機組件
│   ├── router/         # 路由配置
│   │   └── index.ts    # 路由定義
│   ├── store/          # Pinia 狀態管理
│   │   ├── index.ts    # Store 入口
│   │   └── user.ts     # 用戶狀態管理
│   ├── types/          # TypeScript 類型定義
│   │   └── index.ts    # 通用類型
│   ├── utils/          # 工具函數
│   │   └── request.ts  # Axios 請求封裝
│   ├── views/          # 頁面組件
│   │   ├── Login.vue       # 登入頁面
│   │   ├── Register.vue    # 註冊頁面
│   │   ├── Game.vue        # 遊戲主頁面
│   │   ├── Wallet.vue      # 錢包管理頁面
│   │   └── Statistics.vue  # 統計頁面
│   ├── App.vue         # 根組件
│   └── main.ts         # 應用入口
├── package.json        # 項目配置
├── tsconfig.json       # TypeScript 配置
├── vue.config.js       # Vue CLI 配置
└── README.md          # 說明文件
```

## API 配置

應用通過代理方式連接後端 API，配置在 `vue.config.js` 中：

```javascript
devServer: {
  port: 3000,
  proxy: {
    '/api': {
      target: 'http://localhost:8080',
      changeOrigin: true
    }
  }
}
```

如需修改後端 API 地址，請編輯 `vue.config.js` 中的 `target` 配置。

## 頁面路由

| 路徑 | 組件 | 說明 | 是否需要認證 |
|------|------|------|------------|
| `/` | - | 重定向到 `/game` | - |
| `/login` | Login.vue | 登入頁面 | 否 |
| `/register` | Register.vue | 註冊頁面 | 否 |
| `/game` | Game.vue | 遊戲主頁面 | 是 |
| `/wallet` | Wallet.vue | 錢包管理 | 是 |
| `/statistics` | Statistics.vue | RTP 統計 | 是 |

## 狀態管理

使用 Pinia 管理用戶狀態：

- **token**: JWT 認證令牌
- **username**: 用戶名
- **balance**: 帳戶餘額
- **isLoggedIn**: 登入狀態

## 主要功能說明

### 認證流程

1. 用戶註冊或登入後，系統會返回 JWT Token
2. Token 自動存儲在 localStorage 中
3. 後續所有 API 請求自動攜帶 Token
4. 登出時清除 Token 並跳轉至登入頁

### 老虎機遊戲

1. 選擇投注金額（10-1000 元）
2. 點擊"開始旋轉"按鈕
3. 老虎機旋轉 2 秒後顯示結果
4. 自動更新餘額並顯示中獎訊息
5. 支援快速金額選擇按鈕

### 錢包管理

1. **存款**：輸入金額後點擊確認存款
2. **提款**：點擊全額提款按鈕，確認後提取所有餘額
3. 所有交易即時處理，無需等待

### RTP 統計

- 顯示目標 RTP 與實際 RTP 的對比
- 三種狀態：OPTIMAL（正常）、HIGH（偏高）、LOW（偏低）
- 詳細的統計數據：總投注、總贏得、投注次數等
- 支援重置統計數據

## 響應式設計

應用採用響應式設計，支援多種設備：

- 桌面電腦（> 1200px）
- 平板電腦（768px - 1200px）
- 手機（< 768px）

## 瀏覽器支援

- Chrome（推薦）
- Firefox
- Safari
- Edge

## 開發注意事項

1. **環境變量**：確保後端服務運行在 `http://localhost:8080`
2. **CORS**：後端需要配置 CORS 允許前端訪問
3. **Token 過期**：Token 過期後會自動跳轉到登入頁
4. **錯誤處理**：所有 API 錯誤會通過 Element Plus 的 Message 組件顯示

## 常見問題

### 1. API 請求失敗

確保後端服務已啟動並運行在 `http://localhost:8080`

### 2. 登入後無法訪問其他頁面

檢查瀏覽器控制台是否有錯誤，確認 Token 已正確存儲

### 3. 老虎機動畫卡頓

建議使用較新版本的瀏覽器，並確保硬體加速已啟用

## 授權

本專案僅供學習和研究使用。
