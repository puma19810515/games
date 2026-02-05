# 老虎機遊戲 - Vue.js 前端專案完整指南

## 專案概述

本專案為老虎機遊戲系統的前端部分，使用 Vue 3 + TypeScript + Element Plus 開發，提供完整的用戶界面和交互功能。

## 技術架構

### 核心技術
- **Vue 3.4+**: 使用 Composition API，提供更好的類型推斷和程式碼組織
- **TypeScript 5.3+**: 完整的類型安全支持
- **Element Plus 2.6+**: 企業級 UI 組件庫
- **Vue Router 4.3+**: 官方路由管理
- **Pinia 2.1+**: 新一代狀態管理
- **Axios 1.6+**: HTTP 請求庫
- **Sass**: CSS 預處理器

### 構建工具
- **Webpack 5**: 通過 Vue CLI 5 配置
- **Babel**: JavaScript 編譯器
- **PostCSS**: CSS 後處理器

## 已創建的文件列表

### 配置文件
```
frontend/
├── package.json              # 依賴和腳本配置
├── tsconfig.json            # TypeScript 編譯配置
├── vue.config.js            # Vue CLI 和 Webpack 配置
├── .gitignore               # Git 忽略文件
├── shims-vue.d.ts           # Vue 類型聲明
├── start.sh                 # 快速啟動腳本
├── README.md                # 專案說明
└── SETUP.md                 # 設置指南
```

### 源代碼文件
```
src/
├── main.ts                  # 應用入口
├── App.vue                  # 根組件
├── types/
│   └── index.ts            # TypeScript 類型定義
├── utils/
│   └── request.ts          # Axios 請求封裝
├── api/
│   ├── auth.ts             # 認證 API
│   ├── game.ts             # 遊戲 API
│   ├── wallet.ts           # 錢包 API
│   └── rtp.ts              # RTP 統計 API
├── store/
│   ├── index.ts            # Pinia 配置
│   └── user.ts             # 用戶狀態管理
├── router/
│   └── index.ts            # 路由配置
├── components/
│   └── SlotMachine.vue     # 老虎機組件
└── views/
    ├── Login.vue           # 登入頁面
    ├── Register.vue        # 註冊頁面
    ├── Game.vue            # 遊戲主頁面
    ├── Wallet.vue          # 錢包管理頁面
    └── Statistics.vue      # RTP 統計頁面
```

### 公共資源
```
public/
└── index.html              # HTML 模板
```

## 快速開始

### 方式一：使用啟動腳本（推薦）

```bash
cd frontend
./start.sh
```

### 方式二：手動啟動

```bash
cd frontend
npm install
npm run serve
```

應用將在 http://localhost:3000 啟動

## 功能詳解

### 1. 用戶認證系統

#### 註冊功能 (Register.vue)
- 用戶名驗證（3-20 個字符）
- 密碼驗證（最少 6 個字符）
- 密碼確認匹配檢查
- 註冊成功自動登入
- 初始餘額 1000 元

#### 登入功能 (Login.vue)
- 表單驗證
- JWT Token 管理
- 自動跳轉到遊戲頁面
- 錯誤提示

#### Token 管理
- Token 存儲在 localStorage
- 自動攜帶在請求頭
- 過期自動跳轉登入
- 登出清除 Token

### 2. 老虎機遊戲 (Game.vue + SlotMachine.vue)

#### 遊戲主頁面功能
- 用戶信息展示（用戶名、餘額）
- 投注金額輸入（10-1000 元）
- 快速投注按鈕（10/50/100/500/1000）
- 開始旋轉按鈕
- 遊戲規則說明（可摺疊）
- 導航到錢包和統計頁面

#### 老虎機組件功能
- 三輪老虎機顯示
- 旋轉動畫效果（2秒）
- 結果展示動畫
- 中獎提示訊息
- 符號包括：🍒 🍋 🍊 🍉 ⭐ 💎 7️⃣

#### 賠率規則
- 三個相同符號：
  - 7️⃣ 7️⃣ 7️⃣ = 88倍
  - 💎 💎 💎 = 34倍
  - ⭐ ⭐ ⭐ = 13.5倍
  - 🍉 🍉 🍉 = 8.8倍
  - 🍊 🍊 🍊 = 4.9倍
  - 🍋 🍋 🍋 = 3.4倍
  - 🍒 🍒 🍒 = 2.4倍
- 兩個相同符號 = 1.74倍

### 3. 錢包管理 (Wallet.vue)

#### 存款功能
- 金額輸入（支持輸入任意金額）
- 快速金額選擇（100/500/1000/5000/10000）
- 即時存款，立即到賬
- 餘額自動更新

#### 提款功能
- 全額提款
- 二次確認機制
- 即時處理
- 提款後餘額歸零

#### 界面特點
- 分卡片展示存款和提款
- 當前餘額實時顯示
- 操作說明和提示
- 返回遊戲快捷按鈕

### 4. RTP 統計 (Statistics.vue)

#### 統計數據展示
- **目標 RTP**: 系統設定的目標回報率（90%）
- **實際 RTP**: 實際計算的回報率
- **RTP 差異**: 實際與目標的差異百分比
- **總投注金額**: 累計投注總額
- **總贏得金額**: 累計贏得總額
- **總投注次數**: 遊戲次數
- **平均投注**: 每次平均投注額

#### RTP 狀態分類
- **OPTIMAL（正常）**: 差異在 ±2% 內
  - 顯示綠色，表示運行正常
- **HIGH（偏高）**: 實際 RTP 超過目標 2% 以上
  - 顯示黃色，玩家獲利較多
- **LOW（偏低）**: 實際 RTP 低於目標 2% 以上
  - 顯示紅色，玩家獲利較少

#### 統計功能
- 視覺化狀態展示
- 即時數據更新
- 重置統計功能（需確認）
- 詳細數據卡片展示

## API 整合說明

### 請求攔截器 (request.ts)
```typescript
// 自動添加 JWT Token
config.headers.Authorization = `Bearer ${token}`

// 統一錯誤處理
// 401 自動跳轉登入
// 顯示錯誤訊息
```

### API 端點對應

| 功能 | 前端方法 | API 端點                             | 說明 |
|------|---------|------------------------------------|------|
| 註冊 | `register()` | POST /api/auth/register            | 創建新帳號 |
| 登入 | `login()` | POST /api/auth/login               | 用戶登入 |
| 登出 | `logout()` | POST /api/auth/logout              | 用戶登出 |
| 旋轉 | `spin()` | POST /api/game/spin/{gameCode}     | 老虎機旋轉 |
| 餘額 | `getBalance()` | GET /api/game/balance              | 查詢餘額 |
| 存款 | `deposit()` | POST /api/wallet/deposit           | 帳戶存款 |
| 提款 | `withdrawAll()` | POST /api/wallet/withdraw-all      | 全額提款 |
| RTP統計 | `getRtpStatistics()` | GET /api/rtp/statistics/{gameCode} | 查詢統計 |
| 重置 | `resetRtpStatistics()` | POST /api/rtp/reset                | 重置統計 |

## 狀態管理 (Pinia)

### 用戶狀態 (user.ts)

```typescript
// 狀態
- token: string           // JWT Token
- username: string        // 用戶名
- balance: number         // 餘額
- isLoggedIn: computed    // 登入狀態

// 動作
- doLogin()              // 登入
- doRegister()           // 註冊
- doLogout()             // 登出
- updateBalance()        // 更新餘額
- setBalance()           // 設置餘額
```

## 路由配置

### 路由表
```typescript
/               → 重定向到 /game
/login          → 登入頁面（公開）
/register       → 註冊頁面（公開）
/game           → 遊戲主頁（需認證）
/wallet         → 錢包管理（需認證）
/statistics     → RTP 統計（需認證）
```

### 路由守衛
- 需認證的路由會檢查登入狀態
- 未登入用戶重定向到登入頁
- 已登入用戶訪問登入/註冊頁會重定向到遊戲頁

## 響應式設計

### 斷點配置
```scss
// Element Plus 斷點
xs: < 768px      // 手機
sm: ≥ 768px      // 平板
md: ≥ 992px      // 小桌面
lg: ≥ 1200px     // 大桌面
xl: ≥ 1920px     // 超大桌面
```

### 適配策略
- 使用 Element Plus 的柵格系統
- 彈性布局（Flexbox）
- 響應式字體和間距
- 移動端優化的觸控交互

## 開發指南

### 添加新頁面

1. 在 `src/views/` 創建新的 Vue 組件
2. 在 `src/router/index.ts` 添加路由配置
3. 如需要認證，設置 `meta: { requiresAuth: true }`
4. 在導航欄添加入口（如需要）

### 添加新 API

1. 在 `src/types/index.ts` 定義類型
2. 在 `src/api/` 創建對應的 API 文件
3. 使用 `request` 工具發送請求
4. 在組件中調用 API 方法

### 添加新狀態

1. 在 `src/store/` 創建新的 store 文件
2. 使用 `defineStore` 定義狀態和動作
3. 在組件中使用 `useXxxStore()` 訪問

### 樣式規範

- 使用 `scoped` 作用域樣式
- 使用 Sass 變量和混入
- 遵循 BEM 命名規範
- 利用 Element Plus 的主題變量

## 性能優化

### 已實現的優化
- 路由懶加載（動態 import）
- 組件按需引入
- 生產環境代碼壓縮
- 圖片和資源優化

### 建議的優化
- 添加 CDN 加速
- 實現服務端渲染（SSR）
- 添加緩存策略
- 圖片懶加載

## 部署指南

### 構建生產版本

```bash
npm run build
```

### 部署到靜態服務器

```bash
# 將 dist 目錄內容部署到服務器
# 支持: Nginx, Apache, Vercel, Netlify 等
```

### Nginx 配置示例

```nginx
server {
    listen 80;
    server_name your-domain.com;
    root /path/to/dist;
    index index.html;

    location / {
        try_files $uri $uri/ /index.html;
    }

    location /api {
        proxy_pass http://localhost:8080;
    }
}
```

## 測試建議

### 手動測試流程

1. **認證流程測試**
   - 註冊新用戶
   - 登入系統
   - 驗證 Token 存儲
   - 測試登出功能

2. **遊戲功能測試**
   - 不同金額投注
   - 驗證結果正確性
   - 檢查餘額更新
   - 測試餘額不足情況

3. **錢包功能測試**
   - 存入不同金額
   - 驗證餘額增加
   - 全額提款
   - 驗證餘額歸零

4. **統計功能測試**
   - 查看統計數據
   - 驗證計算正確性
   - 測試重置功能

### 自動化測試（待實現）

- 單元測試：使用 Jest + Vue Test Utils
- E2E 測試：使用 Cypress
- API 測試：使用 Postman/Newman

## 故障排除

### 常見問題

1. **安裝依賴失敗**
   ```bash
   npm cache clean --force
   rm -rf node_modules package-lock.json
   npm install --legacy-peer-deps
   ```

2. **編譯錯誤**
   - 檢查 Node.js 版本（需要 16+）
   - 確認 TypeScript 配置正確
   - 查看控制台錯誤訊息

3. **API 請求失敗**
   - 確認後端服務運行
   - 檢查代理配置
   - 查看網絡請求狀態

4. **樣式問題**
   - 清除瀏覽器緩存
   - 確認 Element Plus 正確引入
   - 檢查 Sass 編譯

## 安全考慮

- JWT Token 存儲在 localStorage（生產環境建議使用 HttpOnly Cookie）
- 所有敏感操作需要二次確認
- API 請求自動攜帶認證 Token
- 401 錯誤自動跳轉登入
- 輸入驗證和清理

## 未來規劃

- [ ] 添加遊戲音效
- [ ] 實現交易歷史記錄
- [ ] 添加更多老虎機主題
- [ ] 實現多語言支持（i18n）
- [ ] 添加暗黑模式
- [ ] 實現 PWA（離線支持）
- [ ] 添加社交分享功能
- [ ] 實現排行榜系統

## 相關文檔

- [Vue 3 官方文檔](https://vuejs.org/)
- [Element Plus 文檔](https://element-plus.org/)
- [TypeScript 文檔](https://www.typescriptlang.org/)
- [Pinia 文檔](https://pinia.vuejs.org/)
- [Vue Router 文檔](https://router.vuejs.org/)

## 聯絡和支持

如有問題或建議，請通過以下方式聯絡：
- GitHub Issues
- 技術文檔
- 開發團隊

---

**專案狀態**: ✅ 開發完成，可以使用
**最後更新**: 2026-01-30
