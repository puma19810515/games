#!/bin/bash

echo "========================================="
echo "  老虎機遊戲前端啟動腳本"
echo "========================================="
echo ""

# 檢查 Node.js 是否安裝
if ! command -v node &> /dev/null; then
    echo "❌ 錯誤: Node.js 未安裝"
    echo "請訪問 https://nodejs.org/ 下載並安裝 Node.js"
    exit 1
fi

echo "✅ Node.js 版本: $(node -v)"
echo "✅ npm 版本: $(npm -v)"
echo ""

# 檢查 node_modules 是否存在
if [ ! -d "node_modules" ]; then
    echo "📦 首次運行，正在安裝依賴..."
    npm install
    if [ $? -ne 0 ]; then
        echo "❌ 依賴安裝失敗"
        exit 1
    fi
    echo "✅ 依賴安裝完成"
    echo ""
fi

# 檢查後端服務
echo "🔍 檢查後端服務..."
if curl -s http://localhost:8080 > /dev/null 2>&1; then
    echo "✅ 後端服務運行正常"
else
    echo "⚠️  警告: 無法連接到後端服務 (http://localhost:8080)"
    echo "   請確保後端服務已啟動"
fi
echo ""

# 啟動開發服務器
echo "🚀 啟動開發服務器..."
echo "   前端將運行在: http://localhost:3000"
echo "   按 Ctrl+C 停止服務器"
echo ""
npm run serve
