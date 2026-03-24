-- =============================================
-- Migration: 新增 Cancel 和 Cashout 功能欄位
-- 日期: 2026-03-24
-- =============================================

-- 為 sport_bets 表新增 cashout 和 cancel 相關欄位
ALTER TABLE sport_bets ADD COLUMN IF NOT EXISTS cashout_amount NUMERIC(15,4);
ALTER TABLE sport_bets ADD COLUMN IF NOT EXISTS cancel_reason VARCHAR(255);
ALTER TABLE sport_bets ADD COLUMN IF NOT EXISTS cancelled_at TIMESTAMP WITH TIME ZONE;

-- 新增欄位註解
COMMENT ON COLUMN sport_bets.cashout_amount IS '提前兌現金額';
COMMENT ON COLUMN sport_bets.cancel_reason IS '取消原因';
COMMENT ON COLUMN sport_bets.cancelled_at IS '取消時間';

-- 建立索引優化查詢
CREATE INDEX IF NOT EXISTS idx_sport_bets_cancelled_at ON sport_bets(cancelled_at);
