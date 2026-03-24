# 體育投注 API 文件

## 概述

本文件說明體育投注相關的 API 接口，包含：
- 賽事查詢
- 投注下單（單注/串關）
- 投注記錄查詢

## 認證

除了賽事查詢接口外，其他接口都需要：
1. Header: `X-API-KEY` - 商戶 API 金鑰
2. Header: `Authorization: Bearer {token}` - 用戶 JWT Token

---

## 一、賽事查詢接口

### 1.1 查詢可投注賽事列表

**接口路徑：** `GET /api/sport/event/list`

**說明：** 查詢所有可投注的賽事，可按球種過濾

**請求參數：**

| 參數 | 類型 | 必填 | 說明 |
|-----|------|-----|------|
| sportTypeCode | String | 否 | 球種代碼：FOOTBALL, BASKETBALL, BASEBALL, TENNIS, ESPORTS 等 |

**請求範例：**
```bash
curl -X GET "http://localhost:8080/api/sport/event/list?sportTypeCode=FOOTBALL"
```

**響應範例：**
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "eventId": 1,
      "sportTypeCode": "FOOTBALL",
      "sportTypeName": "足球",
      "leagueId": 1,
      "leagueName": "英格蘭超級聯賽",
      "homeTeamId": 1,
      "homeTeamName": "曼徹斯特聯",
      "awayTeamId": 2,
      "awayTeamName": "曼徹斯特城",
      "startTime": "2026-03-01T20:00:00",
      "homeScore": null,
      "awayScore": null,
      "status": "UPCOMING",
      "bettingStatus": "OPEN"
    }
  ]
}
```

---

### 1.2 查詢賽事詳情（含盤口）

**接口路徑：** `GET /api/sport/event/detail/{eventId}`

**說明：** 查詢單一賽事的詳細資訊，包含所有可投注的盤口

**請求範例：**
```bash
curl -X GET "http://localhost:8080/api/sport/event/detail/1"
```

**響應範例：**
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "eventId": 1,
    "sportTypeCode": "FOOTBALL",
    "sportTypeName": "足球",
    "leagueId": 1,
    "leagueName": "英格蘭超級聯賽",
    "homeTeamName": "曼徹斯特聯",
    "awayTeamName": "曼徹斯特城",
    "startTime": "2026-03-01T20:00:00",
    "status": "UPCOMING",
    "bettingStatus": "OPEN",
    "marketLines": [
      {
        "marketLineId": 1,
        "betTypeCode": "AH",
        "betTypeName": "亞洲讓球",
        "oddsFormatCode": "ASIAN",
        "oddsFormatName": "亞洲盤",
        "handicap": -0.50,
        "homeOdds": 1.15,
        "awayOdds": 0.75
      },
      {
        "marketLineId": 6,
        "betTypeCode": "OU",
        "betTypeName": "亞洲大小",
        "oddsFormatCode": "ASIAN",
        "handicap": 2.50,
        "overOdds": 0.85,
        "underOdds": 1.05
      },
      {
        "marketLineId": 11,
        "betTypeCode": "1X2",
        "betTypeName": "獨贏",
        "oddsFormatCode": "EUROPEAN",
        "homeOdds": 2.80,
        "drawOdds": 3.40,
        "awayOdds": 2.50
      }
    ]
  }
}
```

---

## 二、投注接口

### 2.1 下注（單注/串關）

**接口路徑：** `POST /api/sport/bet/place`

**說明：** 體育投注下單，支援單注（SINGLE）和串關（PARLAY）

**請求 Headers：**
```
X-API-KEY: {商戶API金鑰}
Authorization: Bearer {用戶Token}
Content-Type: application/json
```

**請求參數：**

| 參數 | 類型 | 必填 | 說明 |
|-----|------|-----|------|
| betType | String | 是 | 投注類型：`SINGLE`(單注) 或 `PARLAY`(串關) |
| stake | BigDecimal | 是 | 投注金額（1.00 ~ 100000.00） |
| legs | Array | 是 | 投注明細列表 |

**legs 參數：**

| 參數 | 類型 | 必填 | 說明 |
|-----|------|-----|------|
| eventId | Long | 是 | 賽事ID |
| marketLineId | Long | 是 | 盤口ID |
| selection | String | 是 | 選擇項：`HOME`, `AWAY`, `OVER`, `UNDER`, `DRAW`, `YES`, `NO`, `ODD`, `EVEN` |
| correctScore | String | 否 | 波膽比分（僅波膽玩法使用），如：`1-0`, `2-1` |

**單注請求範例：**
```json
{
  "betType": "SINGLE",
  "stake": 100.00,
  "legs": [
    {
      "eventId": 1,
      "marketLineId": 3,
      "selection": "HOME"
    }
  ]
}
```

**串關請求範例：**
```json
{
  "betType": "PARLAY",
  "stake": 50.00,
  "legs": [
    {
      "eventId": 1,
      "marketLineId": 3,
      "selection": "HOME"
    },
    {
      "eventId": 3,
      "marketLineId": 25,
      "selection": "AWAY"
    }
  ]
}
```

**響應範例：**
```json
{
  "success": true,
  "message": "Success",
  "data": {
    "betId": 2,
    "betType": "PARLAY",
    "stake": 50.00,
    "totalOdds": 4.0850,
    "potentialWin": 204.2500,
    "status": "PENDING",
    "placedAt": "2026-03-01T14:57:02.607853",
    "balanceAfter": 250.00,
    "legs": [
      {
        "legId": 2,
        "eventId": 1,
        "eventName": "曼徹斯特聯 vs 曼徹斯特城",
        "leagueName": "英格蘭超級聯賽",
        "sportTypeName": "足球",
        "startTime": "2026-03-06T04:00:00",
        "marketLineId": 3,
        "betTypeCode": "AH",
        "betTypeName": "亞洲讓球",
        "oddsFormatCode": "ASIAN",
        "selection": "HOME",
        "selectionDisplay": "曼徹斯特聯",
        "handicap": -0.50,
        "odds": 1.1500,
        "oddsDecimal": 2.1500,
        "result": "PENDING"
      },
      {
        "legId": 3,
        "eventId": 3,
        "eventName": "洛杉磯湖人 vs 金州勇士",
        "leagueName": "美國職業籃球聯賽",
        "sportTypeName": "籃球",
        "startTime": "2026-03-05T11:00:00",
        "marketLineId": 25,
        "betTypeCode": "AH",
        "betTypeName": "亞洲讓球",
        "oddsFormatCode": "ASIAN",
        "selection": "AWAY",
        "selectionDisplay": "金州勇士",
        "handicap": -6.50,
        "odds": 0.9000,
        "oddsDecimal": 1.9000,
        "result": "PENDING"
      }
    ]
  }
}
```

---

### 2.2 查詢投注記錄

**接口路徑：** `POST /api/sport/bet/records`

**說明：** 分頁查詢用戶的投注記錄

**請求參數：**

| 參數 | 類型 | 必填 | 說明 |
|-----|------|-----|------|
| page | Integer | 否 | 頁碼（從0開始），預設0 |
| size | Integer | 否 | 每頁大小，預設10 |
| betType | String | 否 | 投注類型過濾：`SINGLE`, `PARLAY` |
| status | String | 否 | 狀態過濾：`PENDING`, `SETTLED`, `CANCELLED`, `CASHED_OUT` |
| startTime | DateTime | 否 | 開始時間 |
| endTime | DateTime | 否 | 結束時間 |

**請求範例：**
```json
{
  "page": 0,
  "size": 10,
  "status": "PENDING"
}
```

---

### 2.3 查詢投注詳情

**接口路徑：** `GET /api/sport/bet/detail/{betId}`

**說明：** 查詢單筆投注的詳細資訊

**請求範例：**
```bash
curl -X GET "http://localhost:8080/api/sport/bet/detail/10001" \
  -H "X-API-KEY: s4vLDyaWvXAv8EyQckKt2UPBD5JC6Jsz" \
  -H "Authorization: Bearer {token}"
```

---

## 三、玩法說明

### 3.1 支援的玩法類型

| 代碼 | 名稱 | 說明 | 賠率格式 |
|-----|------|-----|---------|
| AH | 亞洲讓球 | 亞洲讓球盤，支援半球/四分盤 | ASIAN |
| AH_HALF | 半場亞洲讓球 | 半場亞洲讓球盤 | ASIAN |
| OU | 亞洲大小 | 亞洲大小盤 | ASIAN |
| OU_HALF | 半場亞洲大小 | 半場亞洲大小盤 | ASIAN |
| 1X2 | 獨贏 | 歐洲三項盤（主/和/客） | EUROPEAN |
| DC | 雙重機會 | 雙重機會盤 | EUROPEAN |
| CS | 波膽 | 正確比分 | EUROPEAN |
| BTTS | 兩隊都進球 | 兩隊是否都進球 | EUROPEAN |
| OE | 單雙 | 總分單雙 | EUROPEAN |
| HK_AH | 香港讓球 | 香港盤讓球 | HONGKONG |
| MY_AH | 馬來讓球 | 馬來盤讓球 | MALAY |
| ID_AH | 印尼讓球 | 印尼盤讓球 | INDO |
| US_ML | 美式獨贏 | 美國盤獨贏 | AMERICAN |
| US_SPREAD | 美式讓分 | 美國盤讓分 | AMERICAN |

### 3.2 選擇項說明

| Selection | 說明 | 適用玩法 |
|-----------|-----|---------|
| HOME | 主隊 | AH, 1X2, HK_AH, MY_AH, ID_AH, US_ML, US_SPREAD |
| AWAY | 客隊 | AH, 1X2, HK_AH, MY_AH, ID_AH, US_ML, US_SPREAD |
| DRAW | 平局 | 1X2 |
| OVER | 大 | OU, US_TOTAL |
| UNDER | 小 | OU, US_TOTAL |
| YES | 是 | BTTS |
| NO | 否 | BTTS |
| ODD | 單 | OE |
| EVEN | 雙 | OE |

### 3.3 結算結果說明

| Result | 說明 | 結算係數 |
|--------|-----|---------|
| WIN | 全贏 | 1.0 |
| HALF_WIN | 半贏 | 0.5（贏取利潤的一半） |
| PUSH | 和/走水 | 0（退還本金） |
| HALF_LOSE | 半輸 | -0.5（輸掉本金的一半） |
| LOSE | 全輸 | -1.0（輸掉全部本金） |
| VOID | 作廢 | 0（退還本金） |

### 3.4 半贏/半輸計算範例

**單注半贏計算：**
- 投注金額：100
- 歐洲盤賠率：2.15
- 半贏贏取：100 + 100 × (2.15 - 1) / 2 = 100 + 57.5 = 157.5

**單注半輸計算：**
- 投注金額：100
- 半輸退還：100 / 2 = 50

**串關計算：**
- 串關中每一腿的有效賠率會根據結果調整
- 全贏：使用原賠率
- 半贏：1 + (賠率 - 1) / 2
- 和/作廢：當作賠率 1.0
- 半輸：0.5
- 全輸：整張投注單全輸

---

## 四、賠率格式轉換

所有賠率在計算時會統一轉換為歐洲盤（Decimal）格式：

| 格式 | 轉換公式 |
|-----|---------|
| EUROPEAN | 直接使用 |
| HONGKONG | odds + 1 |
| ASIAN | odds + 1 |
| MALAY (正) | odds + 1 |
| MALAY (負) | 1 - 1/abs(odds) |
| INDO (正) | odds + 1 |
| INDO (負) | 1 + 1/abs(odds) |
| AMERICAN (正) | odds/100 + 1 |
| AMERICAN (負) | 100/abs(odds) + 1 |

---

## 五、錯誤碼說明

| HTTP 狀態碼 | 說明 |
|------------|-----|
| 200 | 成功 |
| 400 | 請求參數錯誤 |
| 401 | 未授權（缺少或無效的 API Key / Token） |
| 404 | 資源不存在（賽事/盤口不存在） |
| 429 | 請求過於頻繁 |
| 500 | 伺服器內部錯誤 |

**常見錯誤訊息：**
- `餘額不足` - 用戶餘額不足以支付投注金額
- `賽事已停止投注` - 賽事已鎖盤或關閉
- `賽事已開始` - 賽事已經開賽
- `串關不能投注同一場賽事` - 串關中包含重複的賽事
- `串關至少需要 2 腿` - 串關投注腿數不足
- `最小投注金額為 1.00` - 投注金額低於最低限制

---

## 六、取消投注（Cancel）

### 6.1 取消投注

**接口路徑：** `POST /api/sport/bet/cancel`

**說明：** 取消待結算的投注單，僅限下注後 5 分鐘內且所有賽事尚未開始

**限制條件：**
- 投注狀態必須為 `PENDING`（待結算）
- 下注後 5 分鐘內
- 所有投注腿的賽事尚未開始

**請求參數：**

| 參數 | 類型 | 必填 | 說明 |
|-----|------|-----|------|
| betId | Long | 是 | 投注單 ID |
| reason | String | 否 | 取消原因 |

**請求範例：**
```bash
curl -X POST "http://localhost:8080/api/sport/bet/cancel" \
  -H "X-API-KEY: your-api-key" \
  -H "Authorization: Bearer your-jwt-token" \
  -H "Content-Type: application/json" \
  -d '{
    "betId": 12345,
    "reason": "下錯注"
  }'
```

**響應範例：**
```json
{
  "success": true,
  "message": "Success",
  "data": {
    "betId": 12345,
    "refundAmount": 100.0000,
    "balanceAfter": 1100.0000,
    "cancelledAt": "2026-03-24T15:30:00",
    "reason": "下錯注",
    "status": "CANCELLED"
  }
}
```

**錯誤範例：**
```json
{
  "success": false,
  "message": "下注後超過 5 分鐘，無法取消",
  "data": null
}
```

---

## 七、提前兌現（Cashout）

### 7.1 查詢兌現報價

**接口路徑：** `GET /api/sport/bet/cashout/quote/{betId}`

**說明：** 查詢投注單的可兌現金額，報價有效期 30 秒

**請求範例：**
```bash
curl -X GET "http://localhost:8080/api/sport/bet/cashout/quote/12345" \
  -H "X-API-KEY: your-api-key" \
  -H "Authorization: Bearer your-jwt-token"
```

**響應範例（可兌現）：**
```json
{
  "success": true,
  "message": "Success",
  "data": {
    "betId": 12345,
    "cashoutAvailable": true,
    "unavailableReason": null,
    "originalStake": 100.0000,
    "potentialWin": 250.0000,
    "cashoutAmount": 175.0000,
    "profitLoss": 75.0000,
    "cashoutPercentage": 70.00,
    "validForSeconds": 30
  }
}
```

**響應範例（不可兌現）：**
```json
{
  "success": true,
  "message": "Success",
  "data": {
    "betId": 12345,
    "cashoutAvailable": false,
    "unavailableReason": "部分賽事已結束，無法提前兌現",
    "originalStake": 100.0000,
    "potentialWin": 250.0000,
    "cashoutAmount": null,
    "profitLoss": null,
    "cashoutPercentage": null,
    "validForSeconds": 0
  }
}
```

### 7.2 執行提前兌現

**接口路徑：** `POST /api/sport/bet/cashout`

**說明：** 執行提前兌現，將根據當前情況計算兌現金額

**限制條件：**
- 投注狀態必須為 `PENDING`（待結算）
- 所有賽事尚未結束
- 賽事未取消或延期

**請求參數：**

| 參數 | 類型 | 必填 | 說明 |
|-----|------|-----|------|
| betId | Long | 是 | 投注單 ID |
| confirmedAmount | BigDecimal | 否 | 用戶確認的兌現金額（用於驗證金額是否變化） |

**請求範例：**
```bash
curl -X POST "http://localhost:8080/api/sport/bet/cashout" \
  -H "X-API-KEY: your-api-key" \
  -H "Authorization: Bearer your-jwt-token" \
  -H "Content-Type: application/json" \
  -d '{
    "betId": 12345,
    "confirmedAmount": 175.0000
  }'
```

**響應範例：**
```json
{
  "success": true,
  "message": "Success",
  "data": {
    "betId": 12345,
    "originalStake": 100.0000,
    "potentialWin": 250.0000,
    "cashoutAmount": 175.0000,
    "profitLoss": 75.0000,
    "balanceAfter": 1175.0000,
    "cashedOutAt": "2026-03-24T16:00:00",
    "status": "CASHED_OUT"
  }
}
```

### 7.3 兌現金額計算邏輯

**單注計算：**
- **賽前**：本金 × (1 - 手續費率)，手續費率為 5%
- **進行中**：本金 × 估算勝率 × 賠率 × (1 - 手續費率)

**串關計算：**
- 計算各腿的有效賠率（考慮比分和進行狀態）
- 基礎兌現金額 = 本金 × 有效賠率 × (1 - 手續費率)
- 根據有利腿數調整金額

**限制：**
- 最低返還：本金的 10%（單注）或 5%（串關）
- 最高返還：預計最大贏取金額的 95%

### 7.4 兌現條件說明

| 條件 | 可兌現 | 說明 |
|-----|--------|-----|
| 投注狀態為 PENDING | ✅ | 只有待結算的投注可兌現 |
| 所有賽事尚未開始 | ✅ | 賽前兌現 |
| 部分賽事進行中 | ✅ | 可兌現，金額會根據比分調整 |
| 部分賽事已結束 | ❌ | 無法兌現 |
| 賽事取消/延期 | ❌ | 無法兌現，需等待系統退款 |
| 投注已結算 | ❌ | 已結算的投注無法兌現 |

---

## 八、交易類型說明

| 類型代碼 | 說明 |
|---------|-----|
| SPORT_REGISTER | 體育註冊 |
| SPORT_DEPOSIT | 體育存款 |
| SPORT_WITHDRAW | 體育提款 |
| SPORT_BET | 體育投注 |
| SPORT_WIN | 體育派彩 |
| SPORT_REFUND | 體育退款 |
| SPORT_CANCEL | 體育取消（投注取消退款） |
| SPORT_CASHOUT | 體育提前兌現 |

