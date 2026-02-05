# 前端專案設置指南

這是一個完整的 Vue 3 + TypeScript + Element Plus 老虎機遊戲前端專案。

## 一、環境要求

- Node.js 16.x 或更高版本
- npm 7.x 或更高版本

檢查當前版本：
```bash
node -v
npm -v
```

## 二、安裝步驟

### 1. 進入前端目錄

```bash
cd frontend
```

### 2. 安裝依賴

使用 npm：
```bash
npm install
```

或使用 yarn：
```bash
yarn install
```

如果安裝過程中遇到錯誤，可以嘗試：
```bash
npm install --legacy-peer-deps
```

### 3. 確認後端服務已啟動

確保後端 Spring Boot 應用已經運行在 `http://localhost:8080`

可以使用以下命令測試後端是否正常：
```bash
curl http://localhost:8080/api/auth/login
```

## 三、啟動開發服務器

```bash
npm run serve
```

成功後會看到類似輸出：
```
  App running at:
  - Local:   http://localhost:3000/
  - Network: http://192.168.x.x:3000/
```

在瀏覽器中訪問 `http://localhost:3000` 即可看到應用。

## 四、構建生產版本

```bash
npm run build
```

構建產物將輸出到 `dist` 目錄，可以部署到任何靜態文件服務器。

## 五、專案結構說明

```
frontend/
├── public/                 # 靜態文件
├── src/
│   ├── api/               # API 接口封裝
│   │   ├── auth.ts        # 認證 API
│   │   ├── game.ts        # 遊戲 API
│   │   ├── wallet.ts      # 錢包 API
│   │   └── rtp.ts         # 統計 API
│   ├── components/        # 可重用組件
│   │   └── SlotMachine.vue # 老虎機組件
│   ├── router/            # 路由配置
│   ├── store/             # 狀態管理（Pinia）
│   ├── types/             # TypeScript 類型
│   ├── utils/             # 工具函數
│   │   └── request.ts     # Axios 封裝
│   ├── views/             # 頁面組件
│   │   ├── Login.vue      # 登入頁
│   │   ├── Register.vue   # 註冊頁
│   │   ├── Game.vue       # 遊戲主頁
│   │   ├── Wallet.vue     # 錢包頁
│   │   └── Statistics.vue # 統計頁
│   ├── App.vue            # 根組件
│   └── main.ts            # 入口文件
├── package.json           # 依賴配置
├── tsconfig.json          # TS 配置
└── vue.config.js          # Vue CLI 配置
```

## 六、功能測試流程

### 1. 註冊新用戶
- 訪問 `http://localhost:3000/register`
- 輸入用戶名和密碼（密碼至少 6 個字符）
- 註冊成功後自動登入並獲得 1000 元初始餘額

### 2. 開始遊戲
- 自動跳轉到遊戲頁面
- 選擇投注金額（10-1000 元）
- 點擊"開始旋轉"按鈕
- 觀看老虎機動畫並查看結果
- 餘額會自動更新

### 3. 錢包管理
- 點擊頂部導航欄的"錢包"按鈕
- 可以進行存款操作
- 可以全額提款

### 4. 查看統計
- 點擊頂部導航欄的"統計"按鈕
- 查看 RTP 統計數據
- 可以重置統計數據

## 七、常見問題排查

### 問題 1：npm install 失敗

**解決方案：**
```bash
# 清除 npm 緩存
npm cache clean --force

# 刪除 node_modules 和 package-lock.json
rm -rf node_modules package-lock.json

# 重新安裝
npm install --legacy-peer-deps
```

### 問題 2：API 請求失敗（Network Error）

**可能原因：**
- 後端服務未啟動
- 端口被占用
- CORS 配置問題

**解決方案：**
1. 確認後端服務運行在 `http://localhost:8080`
2. 檢查 `vue.config.js` 中的代理配置
3. 確認後端已配置 CORS

### 問題 3：頁面空白或組件未顯示

**解決方案：**
1. 打開瀏覽器開發者工具（F12）
2. 查看 Console 標籤是否有錯誤
3. 檢查 Network 標籤的 API 請求狀態
4. 確認所有依賴已正確安裝

### 問題 4：Token 相關錯誤

**解決方案：**
1. 清除瀏覽器 localStorage
2. 重新登入獲取新的 Token
3. 檢查後端 JWT 配置

## 八、開發建議

### 1. 使用 Vue DevTools

安裝 Vue DevTools 瀏覽器擴展來調試 Vue 應用：
- Chrome: https://chrome.google.com/webstore
- Firefox: https://addons.mozilla.org/firefox

### 2. 代碼格式化

建議安裝 VSCode 擴展：
- Volar (Vue 3 支持)
- ESLint
- Prettier

### 3. TypeScript 支持

專案已配置 TypeScript，享受類型安全和智能提示：
```typescript
// 所有 API 響應都有明確的類型
const response: ApiResponse<SpinResult> = await spin({ amount: 100 })
```

## 九、API 端點對應

| 前端功能 | API 端點 | 說明 |
|---------|---------|------|
| 註冊 | POST /api/auth/register | 創建新帳號 |
| 登入 | POST /api/auth/login | 用戶登入 |
| 登出 | POST /api/auth/logout | 用戶登出 |
| 旋轉 | POST /api/game/spin | 老虎機旋轉 |
| 查詢餘額 | GET /api/game/balance | 獲取當前餘額 |
| 存款 | POST /api/wallet/deposit | 帳戶存款 |
| 提款 | POST /api/wallet/withdraw-all | 全額提款 |
| RTP統計 | GET /api/rtp/statistics | 查詢統計 |
| 重置統計 | POST /api/rtp/reset | 重置數據 |

## 十、下一步

完成基本設置後，你可以：

1. 自定義樣式和主題
2. 添加更多遊戲功能
3. 實現交易歷史記錄
4. 添加音效和更多動畫
5. 實現多語言支持
6. 優化移動端體驗

## 支持

如有問題，請查看：
- Vue 3 文檔: https://vuejs.org/
- Element Plus 文檔: https://element-plus.org/
- TypeScript 文檔: https://www.typescriptlang.org/
