
#!/bin/bash

# Slot Game RTP Test Script
# 此腳本用於測試RTP功能和統計

echo "==================================="
echo "Slot Game RTP 測試腳本"
echo "==================================="
echo ""

# 顏色定義
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 檢查是否安裝jq
if ! command -v jq &> /dev/null; then
    echo -e "${RED}錯誤: 需要安裝 jq 來解析 JSON${NC}"
    echo "請執行: brew install jq (MacOS) 或 sudo apt-get install jq (Ubuntu)"
    exit 1
fi

# 1. 登入獲取token
echo -e "${YELLOW}步驟 1: 登入獲取 token...${NC}"
LOGIN_RESPONSE=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"puma","password":"1qaz2wsx"}')

TOKEN=$(echo "$LOGIN_RESPONSE" | jq -r '.data.token')

if [ -z "$TOKEN" ] || [ "$TOKEN" = "null" ]; then
  echo -e "${RED}❌ 登入失敗${NC}"
  exit 1
fi

echo -e "${GREEN}✅ 登入成功${NC}"
echo ""

# 2. 重置RTP統計
echo -e "${YELLOW}步驟 2: 重置RTP統計（清除舊數據）...${NC}"
RESET_RESPONSE=$(curl -s -X POST http://localhost:8080/api/rtp/reset \
  -H "Authorization: Bearer $TOKEN")

echo "$RESET_RESPONSE" | jq '.'
echo ""

# 3. 查詢初始RTP統計
echo -e "${YELLOW}步驟 3: 查詢初始RTP統計...${NC}"
RTP_STATS=$(curl -s -X GET http://localhost:8080/api/rtp/statistics \
  -H "Authorization: Bearer $TOKEN")

echo "$RTP_STATS" | jq '.'
echo ""

# 4. 執行多次投注測試
echo -e "${YELLOW}步驟 4: 執行100次投注測試（觀察RTP變化）...${NC}"
echo -e "${BLUE}投注金額: 100 x 100次${NC}"
echo ""

TOTAL_BET=0
TOTAL_WIN=0
WIN_COUNT=0

for i in {1..1000}; do
  SPIN_RESPONSE=$(curl -s -X POST http://localhost:8080/api/game/spin \
    -H "Authorization: Bearer $TOKEN" \
    -H "Content-Type: application/json" \
    -d '{"amount":100}')

  BET_AMOUNT=$(echo "$SPIN_RESPONSE" | jq -r '.data.betAmount')
  WIN_AMOUNT=$(echo "$SPIN_RESPONSE" | jq -r '.data.winAmount')
  IS_WIN=$(echo "$SPIN_RESPONSE" | jq -r '.data.isWin')
  RESULT=$(echo "$SPIN_RESPONSE" | jq -r '.data.result | join(" ")')

  TOTAL_BET=$(echo "$TOTAL_BET + $BET_AMOUNT" | bc)
  TOTAL_WIN=$(echo "$TOTAL_WIN + $WIN_AMOUNT" | bc)

  if [ "$IS_WIN" = "true" ]; then
    WIN_COUNT=$((WIN_COUNT + 1))
    echo -e "${GREEN}#$i: $RESULT - 贏得 $WIN_AMOUNT${NC}"
  else
    echo "#$i: $RESULT - 未中獎"
  fi

  # 每10次顯示一次進度
  if [ $((i % 10)) -eq 0 ]; then
    CURRENT_RTP=$(echo "scale=2; ($TOTAL_WIN / $TOTAL_BET) * 100" | bc)
    echo -e "${BLUE}進度 $i/1000 - 當前RTP: $CURRENT_RTP%${NC}"
  fi

  # 短暫延遲避免過快請求
  sleep 0.1
done

echo ""
echo -e "${GREEN}投注測試完成！${NC}"
echo "總投注: $TOTAL_BET"
echo "總贏得: $TOTAL_WIN"
echo "中獎次數: $WIN_COUNT/1000"

MANUAL_RTP=$(echo "scale=2; ($TOTAL_WIN / $TOTAL_BET) * 100" | bc)
echo -e "${BLUE}手動計算RTP: $MANUAL_RTP%${NC}"
echo ""

# 5. 查詢最終RTP統計
echo -e "${YELLOW}步驟 5: 查詢最終RTP統計...${NC}"
FINAL_RTP_STATS=$(curl -s -X GET http://localhost:8080/api/rtp/statistics \
  -H "Authorization: Bearer $TOKEN")

echo "$FINAL_RTP_STATS" | jq '.'

# 提取關鍵數據
TARGET_RTP=$(echo "$FINAL_RTP_STATS" | jq -r '.data.targetRtp')
ACTUAL_RTP=$(echo "$FINAL_RTP_STATS" | jq -r '.data.actualRtp')
RTP_STATUS=$(echo "$FINAL_RTP_STATS" | jq -r '.data.rtpStatus')
RTP_DIFF=$(echo "$FINAL_RTP_STATS" | jq -r '.data.rtpDifference')

echo ""
echo "==================================="
echo -e "${BLUE}RTP 測試總結${NC}"
echo "==================================="
echo -e "目標RTP:   ${YELLOW}$TARGET_RTP%${NC}"
echo -e "實際RTP:   ${YELLOW}$ACTUAL_RTP%${NC}"
echo -e "RTP差異:   ${YELLOW}$RTP_DIFF%${NC}"
echo -e "RTP狀態:   ${YELLOW}$RTP_STATUS${NC}"
echo ""

# 判斷RTP狀態
if [ "$RTP_STATUS" = "OPTIMAL" ]; then
  echo -e "${GREEN}✅ RTP 在最佳範圍內 (±2%)${NC}"
elif [ "$RTP_STATUS" = "HIGH" ]; then
  echo -e "${YELLOW}⚠️  RTP 略高於目標值${NC}"
elif [ "$RTP_STATUS" = "LOW" ]; then
  echo -e "${YELLOW}⚠️  RTP 略低於目標值${NC}"
fi

echo ""
echo -e "${BLUE}說明：${NC}"
echo "- RTP在短期內可能會有波動"
echo "- 樣本數越多，RTP會越接近目標值"
echo "- 建議測試1000次以上以獲得更穩定的RTP"
echo ""
echo "==================================="
echo -e "${GREEN}測試完成！${NC}"
echo "==================================="
