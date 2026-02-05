#!/bin/bash

# Slot Game Wallet API Test Script
# 此腳本用於測試存款和提款API

echo "==================================="
echo "Slot Game Wallet API 測試腳本"
echo "==================================="
echo ""

# 顏色定義
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 1. 登入獲取token
echo -e "${YELLOW}步驟 1: 登入獲取 token...${NC}"
LOGIN_RESPONSE=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"testuser001","password":"password123"}')

echo "$LOGIN_RESPONSE" | jq '.'

TOKEN=$(echo "$LOGIN_RESPONSE" | grep -o '"token":"[^"]*"' | cut -d'"' -f4)

if [ -z "$TOKEN" ]; then
  echo -e "${RED}❌ 登入失敗，無法獲取 token${NC}"
  exit 1
fi

echo -e "${GREEN}✅ 登入成功${NC}"
echo ""

# 2. 查詢初始餘額
echo -e "${YELLOW}步驟 2: 查詢初始餘額...${NC}"
BALANCE_RESPONSE=$(curl -s -X GET http://localhost:8080/api/game/balance \
  -H "Authorization: Bearer $TOKEN")

echo "$BALANCE_RESPONSE" | jq '.'
INITIAL_BALANCE=$(echo "$BALANCE_RESPONSE" | grep -o '"balance":[0-9.]*' | cut -d':' -f2)
echo -e "${GREEN}初始餘額: $INITIAL_BALANCE${NC}"
echo ""

# 3. 測試存款
echo -e "${YELLOW}步驟 3: 測試存款 500...${NC}"
DEPOSIT_RESPONSE=$(curl -s -X POST http://localhost:8080/api/wallet/deposit \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"amount":500}')

echo "$DEPOSIT_RESPONSE" | jq '.'

if echo "$DEPOSIT_RESPONSE" | grep -q '"success":true'; then
  echo -e "${GREEN}✅ 存款成功${NC}"
else
  echo -e "${RED}❌ 存款失敗${NC}"
fi
echo ""

# 4. 查詢存款後餘額
echo -e "${YELLOW}步驟 4: 查詢存款後餘額...${NC}"
BALANCE_AFTER_DEPOSIT=$(curl -s -X GET http://localhost:8080/api/game/balance \
  -H "Authorization: Bearer $TOKEN")

echo "$BALANCE_AFTER_DEPOSIT" | jq '.'
NEW_BALANCE=$(echo "$BALANCE_AFTER_DEPOSIT" | grep -o '"balance":[0-9.]*' | cut -d':' -f2)
echo -e "${GREEN}存款後餘額: $NEW_BALANCE${NC}"
echo ""

# 5. 測試全額提款
echo -e "${YELLOW}步驟 5: 測試全額提款...${NC}"
WITHDRAW_RESPONSE=$(curl -s -X POST http://localhost:8080/api/wallet/withdraw-all \
  -H "Authorization: Bearer $TOKEN")

echo "$WITHDRAW_RESPONSE" | jq '.'

if echo "$WITHDRAW_RESPONSE" | grep -q '"success":true'; then
  echo -e "${GREEN}✅ 提款成功${NC}"
else
  echo -e "${RED}❌ 提款失敗${NC}"
fi
echo ""

# 6. 查詢提款後餘額
echo -e "${YELLOW}步驟 6: 查詢提款後餘額...${NC}"
FINAL_BALANCE=$(curl -s -X GET http://localhost:8080/api/game/balance \
  -H "Authorization: Bearer $TOKEN")

echo "$FINAL_BALANCE" | jq '.'
BALANCE_AFTER_WITHDRAW=$(echo "$FINAL_BALANCE" | grep -o '"balance":[0-9.]*' | cut -d':' -f2)
echo -e "${GREEN}提款後餘額: $BALANCE_AFTER_WITHDRAW${NC}"

if [ "$BALANCE_AFTER_WITHDRAW" = "0.00" ]; then
  echo -e "${GREEN}✅ 提款驗證成功，餘額已清零${NC}"
else
  echo -e "${RED}❌ 提款驗證失敗，餘額應該為 0${NC}"
fi
echo ""

# 7. 測試餘額為0時再次提款（應該失敗）
echo -e "${YELLOW}步驟 7: 測試餘額為0時再次提款（應該失敗）...${NC}"
WITHDRAW_FAIL_RESPONSE=$(curl -s -X POST http://localhost:8080/api/wallet/withdraw-all \
  -H "Authorization: Bearer $TOKEN")

echo "$WITHDRAW_FAIL_RESPONSE" | jq '.'

if echo "$WITHDRAW_FAIL_RESPONSE" | grep -q '"success":false'; then
  echo -e "${GREEN}✅ 驗證成功，餘額為0時無法提款${NC}"
else
  echo -e "${RED}❌ 驗證失敗，應該拒絕提款${NC}"
fi
echo ""

echo "==================================="
echo -e "${GREEN}測試完成！${NC}"
echo "==================================="
