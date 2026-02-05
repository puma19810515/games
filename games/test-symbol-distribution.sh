#!/bin/bash

# 測試符號分佈
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"puma","password":"1qaz2wsx"}' | jq -r '.data.token')

echo "測試 20 次旋轉，查看符號分佈："
echo ""

for i in {1..20}; do
  RESULT=$(curl -s -X POST http://localhost:8080/api/game/spin \
    -H "Authorization: Bearer $TOKEN" \
    -H "Content-Type: application/json" \
    -d '{"amount":100}' | jq -r '.data.result | join(" ")')
  echo "#$i: $RESULT"
done
