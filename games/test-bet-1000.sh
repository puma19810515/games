#!/bin/bash

# 老虎機投注測試腳本 - 1000次投注測試
# 顯示每次投注的詳細結果

echo "=========================================="
echo "老虎機 1000次投注測試"
echo "=========================================="
echo ""

# 顏色定義
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# 檢查是否安裝jq
if ! command -v jq &> /dev/null; then
    echo -e "${RED}錯誤: 需要安裝 jq 來解析 JSON${NC}"
    echo "請執行: brew install jq (MacOS) 或 sudo apt-get install jq (Ubuntu)"
    exit 1
fi

# 配置參數
BASE_URL="http://localhost:8080"
USERNAME="puma"
PASSWORD="1qaz2wsx"
BET_AMOUNT=100
TEST_COUNT=1000

# 1. 登入獲取token
echo -e "${YELLOW}🔑 正在登入...${NC}"
LOGIN_RESPONSE=$(curl -s -X POST "$BASE_URL/api/auth/login" \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"$USERNAME\",\"password\":\"$PASSWORD\"}")

TOKEN=$(echo "$LOGIN_RESPONSE" | jq -r '.data.token')

if [ -z "$TOKEN" ] || [ "$TOKEN" = "null" ]; then
  echo -e "${RED}❌ 登入失敗，請確認：${NC}"
  echo "   1. 服務是否啟動 (http://localhost:8080)"
  echo "   2. 用戶名和密碼是否正確"
  echo ""
  echo "回應內容："
  echo "$LOGIN_RESPONSE" | jq '.'
  exit 1
fi

INITIAL_BALANCE=$(echo "$LOGIN_RESPONSE" | jq -r '.data.balance')
echo -e "${GREEN}✅ 登入成功${NC}"
echo -e "${CYAN}初始餘額: $INITIAL_BALANCE${NC}"
echo ""

# 2. 檢查餘額是否足夠
REQUIRED_BALANCE=$(echo "$BET_AMOUNT * $TEST_COUNT" | bc)
echo -e "${BLUE}測試資訊：${NC}"
echo "   投注金額: $BET_AMOUNT"
echo "   測試次數: $TEST_COUNT"
echo "   所需餘額: $REQUIRED_BALANCE"
echo ""

if (( $(echo "$INITIAL_BALANCE < $REQUIRED_BALANCE" | bc -l) )); then
  echo -e "${YELLOW}⚠️  警告：餘額可能不足完成所有測試${NC}"
  echo -e "${YELLOW}   可能會在測試中途停止${NC}"
  echo ""
fi

# 3. 執行1000次投注測試
echo "=========================================="
echo -e "${YELLOW}🎰 開始投注測試...${NC}"
echo "=========================================="
echo ""

# 統計變數
TOTAL_BET=0
TOTAL_WIN=0
WIN_COUNT=0
LOSS_COUNT=0
MAX_WIN=0
MAX_WIN_ROUND=0

# 使用臨時文件記錄符號組合（兼容 bash 3.2）
SYMBOL_STATS_FILE=$(mktemp)

for i in $(seq 1 $TEST_COUNT); do
  SPIN_RESPONSE=$(curl -s -X POST "$BASE_URL/api/game/spin/0000" \
    -H "Authorization: Bearer $TOKEN" \
    -H "Content-Type: application/json" \
    -d "{\"amount\":$BET_AMOUNT}")

  # 解析回應
  SUCCESS=$(echo "$SPIN_RESPONSE" | jq -r '.success')

  if [ "$SUCCESS" != "true" ]; then
    echo -e "${RED}❌ 第 $i 次投注失敗${NC}"
    ERROR_MSG=$(echo "$SPIN_RESPONSE" | jq -r '.message')
    echo -e "${RED}   錯誤訊息: $ERROR_MSG${NC}"

    # 如果是餘額不足，結束測試
    if [[ "$ERROR_MSG" == *"Insufficient"* ]]; then
      echo -e "${YELLOW}⚠️  餘額不足，測試提前結束${NC}"
      break
    fi
    continue
  fi

  BET_AMOUNT_RESP=$(echo "$SPIN_RESPONSE" | jq -r '.data.betAmount')
  WIN_AMOUNT=$(echo "$SPIN_RESPONSE" | jq -r '.data.winAmount')
  IS_WIN=$(echo "$SPIN_RESPONSE" | jq -r '.data.isWin')
  BALANCE_AFTER=$(echo "$SPIN_RESPONSE" | jq -r '.data.balanceAfter')

  # 取得結果符號
  SYMBOL1=$(echo "$SPIN_RESPONSE" | jq -r '.data.result[0]')
  SYMBOL2=$(echo "$SPIN_RESPONSE" | jq -r '.data.result[1]')
  SYMBOL3=$(echo "$SPIN_RESPONSE" | jq -r '.data.result[2]')
  RESULT="$SYMBOL1 $SYMBOL2 $SYMBOL3"

  # 更新統計
  TOTAL_BET=$(echo "$TOTAL_BET + $BET_AMOUNT_RESP" | bc)
  TOTAL_WIN=$(echo "$TOTAL_WIN + $WIN_AMOUNT" | bc)

  # 記錄符號組合統計（寫入臨時文件）
  echo "$RESULT" >> "$SYMBOL_STATS_FILE"

  # 顯示結果
  printf "${CYAN}#%-3d${NC} | " "$i"
  printf "%s | " "$RESULT"

  if [ "$IS_WIN" = "true" ]; then
    WIN_COUNT=$((WIN_COUNT + 1))

    # 檢查是否為最大贏得
    if (( $(echo "$WIN_AMOUNT > $MAX_WIN" | bc -l) )); then
      MAX_WIN=$WIN_AMOUNT
      MAX_WIN_ROUND=$i
    fi

    printf "${GREEN}贏得: %-8s${NC} | " "$WIN_AMOUNT"
    printf "${GREEN}餘額: %-10s${NC}\n" "$BALANCE_AFTER"
  else
    LOSS_COUNT=$((LOSS_COUNT + 1))
    printf "${RED}未中獎%-8s${NC} | " ""
    printf "餘額: %-10s\n" "$BALANCE_AFTER"
  fi

  # 每100次顯示進度統計
  if [ $((i % 100)) -eq 0 ]; then
    CURRENT_RTP=$(echo "scale=2; ($TOTAL_WIN / $TOTAL_BET) * 100" | bc)
    echo -e "${BLUE}────────────────────────────────────────${NC}"
    echo -e "${BLUE}進度: $i/$TEST_COUNT | 當前RTP: $CURRENT_RTP% | 中獎率: $(echo "scale=2; ($WIN_COUNT / $i) * 100" | bc)%${NC}"
    echo -e "${BLUE}────────────────────────────────────────${NC}"
  fi

  # 短暫延遲避免過快請求
  sleep 0.05
done

# 4. 顯示最終統計
echo ""
echo "=========================================="
echo -e "${YELLOW}📊 測試結果統計${NC}"
echo "=========================================="
echo ""

FINAL_RTP=$(echo "scale=2; ($TOTAL_WIN / $TOTAL_BET) * 100" | bc)
WIN_RATE=$(echo "scale=2; ($WIN_COUNT / $TEST_COUNT) * 100" | bc)
AVG_WIN=$(echo "scale=2; $TOTAL_WIN / $WIN_COUNT" | bc 2>/dev/null || echo "0")
NET_RESULT=$(echo "scale=2; $TOTAL_WIN - $TOTAL_BET" | bc)

echo -e "${CYAN}基本統計：${NC}"
echo "   測試次數:     $TEST_COUNT"
echo "   中獎次數:     $WIN_COUNT"
echo "   未中獎次數:   $LOSS_COUNT"
echo "   中獎率:       ${WIN_RATE}%"
echo ""

echo -e "${CYAN}金額統計：${NC}"
echo "   總投注:       $TOTAL_BET"
echo "   總贏得:       $TOTAL_WIN"
echo "   淨損益:       $NET_RESULT"
echo "   平均贏得:     $AVG_WIN"
echo "   最大單次贏得: $MAX_WIN (第 $MAX_WIN_ROUND 次)"
echo ""

echo -e "${CYAN}RTP統計：${NC}"
echo "   實際RTP:      ${FINAL_RTP}%"
echo "   目標RTP:      90.0%"
echo ""

# 顯示符號組合統計（前10名）
echo -e "${CYAN}符號組合統計（出現次數 Top 10）：${NC}"
if [ -s "$SYMBOL_STATS_FILE" ]; then
  sort "$SYMBOL_STATS_FILE" | uniq -c | sort -rn | head -10 | while read count symbols; do
    printf "   %-20s : %3d 次\n" "$symbols" "$count"
  done
else
  echo "   (無統計數據)"
fi
echo ""

# 清理臨時文件
rm -f "$SYMBOL_STATS_FILE"

# RTP評估
echo -e "${CYAN}RTP評估：${NC}"
if (( $(echo "$FINAL_RTP >= 88 && $FINAL_RTP <= 92" | bc -l) )); then
  echo -e "   ${GREEN}✅ RTP在正常範圍內 (88%-92%)${NC}"
elif (( $(echo "$FINAL_RTP > 92" | bc -l) )); then
  echo -e "   ${YELLOW}⚠️  RTP偏高，玩家獲利較多${NC}"
else
  echo -e "   ${YELLOW}⚠️  RTP偏低，玩家損失較多${NC}"
fi
echo ""

# 最終餘額
FINAL_BALANCE=$(curl -s -X GET "$BASE_URL/api/game/balance" \
  -H "Authorization: Bearer $TOKEN" | jq -r '.data.balance')

echo -e "${CYAN}餘額變化：${NC}"
echo "   初始餘額:     $INITIAL_BALANCE"
echo "   最終餘額:     $FINAL_BALANCE"
echo "   變化金額:     $(echo "scale=2; $FINAL_BALANCE - $INITIAL_BALANCE" | bc)"
echo ""

echo "=========================================="
echo -e "${GREEN}✅ 測試完成！${NC}"
echo "=========================================="
echo ""
echo -e "${BLUE}💡 提示：${NC}"
echo "   - 1000次測試能提供較穩定的RTP參考值"
echo "   - 如需更精確的統計，建議測試10000次以上"
echo "   - 使用 ./test-rtp.sh 可查看更詳細的RTP分析"
echo ""
