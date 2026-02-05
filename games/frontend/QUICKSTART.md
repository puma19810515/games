# 快速開始指南

## 一鍵啟動（最簡單）

```bash
cd frontend
./start.sh
```

## 手動啟動步驟

### 1. 確認環境
```bash
# 檢查 Node.js 版本（需要 16.x 或更高）
node -v

# 檢查 npm 版本
npm -v
```

### 2. 安裝依賴
```bash
cd frontend
npm install
```

如果遇到錯誤，嘗試：
```bash
npm install --legacy-peer-deps
```

### 3. 確認後端運行
確保後端服務已啟動在 http://localhost:8080

測試後端：
```bash
curl http://localhost:8080/api/auth/login
```

### 4. 啟動前端
```bash
npm run serve
```

### 5. 訪問應用
打開瀏覽器訪問：http://localhost:3000

## 首次使用流程

### 步驟 1: 註冊帳號
1. 打開 http://localhost:3000
2. 自動跳轉到登入頁面
3. 點擊「還沒有帳號？點擊註冊」
4. 輸入用戶名和密碼
5. 點擊「註冊」按鈕
6. 註冊成功後自動登入

### 步驟 2: 開始遊戲
1. 註冊成功後自動進入遊戲頁面
2. 你會看到初始餘額 1000 元
3. 選擇投注金額（可以使用快速選擇按鈕）
4. 點擊「開始旋轉」按鈕
5. 等待 2 秒查看結果
6. 餘額會自動更新

### 步驟 3: 管理錢包
1. 點擊頂部的「錢包」按鈕
2. 可以存款（輸入金額或使用快速按鈕）
3. 可以全額提款（需要確認）

### 步驟 4: 查看統計
1. 點擊頂部的「統計」按鈕
2. 查看 RTP 統計數據
3. 可以重置統計（需要確認）

## 開發命令

```bash
# 安裝依賴
npm install

# 啟動開發服務器
npm run serve

# 構建生產版本
npm run build

# 代碼檢查
npm run lint
```

## 文件結構速查

```
frontend/
├── src/
│   ├── views/              # 頁面
│   │   ├── Login.vue       # 登入
│   │   ├── Register.vue    # 註冊
│   │   ├── Game.vue        # 遊戲
│   │   ├── Wallet.vue      # 錢包
│   │   └── Statistics.vue  # 統計
│   ├── components/         # 組件
│   │   └── SlotMachine.vue # 老虎機
│   ├── api/               # API
│   ├── store/             # 狀態
│   └── router/            # 路由
├── package.json           # 配置
└── start.sh              # 啟動腳本
```

## 常見問題快速解決

### Q: npm install 失敗
```bash
npm cache clean --force
rm -rf node_modules package-lock.json
npm install --legacy-peer-deps
```

### Q: 無法連接後端
確認後端服務運行：
```bash
cd ..  # 回到專案根目錄
mvn spring-boot:run
```

### Q: 頁面空白
1. 打開瀏覽器控制台（F12）
2. 查看 Console 標籤的錯誤
3. 檢查 Network 標籤的請求

### Q: 登入失效
清除瀏覽器 localStorage：
```javascript
// 在瀏覽器控制台執行
localStorage.clear()
```
然後重新登入。

## 技術支持

- 查看 README.md 了解詳細說明
- 查看 SETUP.md 了解完整設置步驟
- 查看 ../FRONTEND_GUIDE.md 了解開發指南

## 端口說明

- 前端開發服務器：http://localhost:3000
- 後端 API 服務器：http://localhost:8080
- MySQL 數據庫：localhost:3306
- Redis 緩存：localhost:6379

## 下一步

完成基本使用後，你可以：
- 自定義樣式
- 修改遊戲規則
- 添加新功能
- 查看開發文檔

---

🎰 祝你遊戲愉快！
