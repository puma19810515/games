-- 建立資料庫
CREATE DATABASE neondb
WITH ENCODING 'UTF8'
LC_COLLATE='en_US.utf8'
LC_CTYPE='en_US.utf8'
TEMPLATE=template0;

\c neondb

-- MERCHANTS
CREATE TABLE merchants (
  id BIGSERIAL PRIMARY KEY,
  username VARCHAR(50) NOT NULL UNIQUE,
  name VARCHAR(100) NOT NULL,
  balance NUMERIC(15,2) NOT NULL DEFAULT 0.00,
  api_key VARCHAR(64) NOT NULL UNIQUE,
  remark VARCHAR(100) NOT NULL,
  settlement_flag VARCHAR(8) NOT NULL,
  settlement_ratio NUMERIC(5,2) NOT NULL,
  status INT NOT NULL DEFAULT 0,
  created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_merchants_settlement_flag ON merchants(settlement_flag);
CREATE INDEX idx_merchants_username ON merchants(username);
CREATE INDEX idx_merchants_created_at ON merchants(created_at);

-- USERS
CREATE TABLE users (
  id BIGSERIAL PRIMARY KEY,
  merchant_id BIGINT NOT NULL REFERENCES merchants(id),
  username VARCHAR(50) NOT NULL,
  version BIGINT NOT NULL DEFAULT 0,
  password VARCHAR(255) NOT NULL,
  balance NUMERIC(15,2) NOT NULL DEFAULT 0.00,
  created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
  UNIQUE(merchant_id, username)
);

CREATE INDEX idx_users_merchant_id ON users(merchant_id);
CREATE INDEX idx_users_username ON users(merchant_id, username);

-- BETS
CREATE TABLE bets (
  id BIGSERIAL PRIMARY KEY,
  merchant_id BIGINT NOT NULL REFERENCES merchants(id),
  user_id BIGINT NOT NULL REFERENCES users(id),
  bet_amount NUMERIC(15,2) NOT NULL,
  win_amount NUMERIC(15,2) NOT NULL DEFAULT 0.00,
  result TEXT NOT NULL,
  is_win BOOLEAN NOT NULL DEFAULT FALSE,
  game_code VARCHAR(10) NOT NULL,
  created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_bets_user_created ON bets(user_id, created_at);
CREATE INDEX idx_bets_created ON bets(created_at);

-- WALLET TRANSACTIONS
CREATE TABLE transactions (
  id BIGSERIAL PRIMARY KEY,
  merchant_id BIGINT NOT NULL REFERENCES merchants(id),
  user_id BIGINT NOT NULL REFERENCES users(id),
  type VARCHAR(20) NOT NULL,
  amount NUMERIC(15,2) NOT NULL,
  balance_before NUMERIC(15,2),
  balance_after NUMERIC(15,2),
  description VARCHAR(255),
  bet_id BIGINT REFERENCES bets(id),
  created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_transactions_user_id ON transactions(user_id);
CREATE INDEX idx_transactions_type ON transactions(type);
CREATE INDEX idx_transactions_created_at ON transactions(created_at);

-- GAME SETTING
CREATE TABLE game_setting (
  id BIGSERIAL PRIMARY KEY,
  version BIGINT NOT NULL DEFAULT 0,
  game_code VARCHAR(10) NOT NULL UNIQUE,
  name VARCHAR(100) NOT NULL,
  min_bet NUMERIC(15,2) NOT NULL,
  max_bet NUMERIC(15,2) NOT NULL,
  category INT NOT NULL DEFAULT 0,
  rtp_set NUMERIC(15,2) NOT NULL,
  game_settings JSONB NOT NULL,
  two_match_multiplier NUMERIC(15,2) NOT NULL DEFAULT 2.00,
  status INT NOT NULL DEFAULT 0,
  created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_game_setting_game_code ON game_setting(game_code);
CREATE INDEX idx_game_setting_created_at ON game_setting(created_at);

-- MERCHANT PROFIT REPORT
CREATE TABLE merchants_profit_report (
  id BIGSERIAL PRIMARY KEY,
  merchant_id BIGINT NOT NULL REFERENCES merchants(id),
  total_balance NUMERIC(15,2) NOT NULL DEFAULT 0.00,
  settle_status INT NOT NULL DEFAULT 0,
  description VARCHAR(255),
  created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_merchant_profit_id ON merchants_profit_report(merchant_id);
CREATE INDEX idx_merchant_profit_created_at ON merchants_profit_report(created_at);

-- 初始化資料
INSERT INTO game_setting (id, game_code, name, min_bet, max_bet, rtp_set, game_settings, two_match_multiplier, status)
VALUES (1, '0000', 'Slot Sample', 10.00, 1000.00, 90.00,
'{
  "symbols":["ONE","TWO","THREE","FOUR","FIVE","SIX","SEVEN"],
  "symbolWeights":[15.0,13.0,10.0,8.0,6.0,5.0,1.0],
  "payoutMultipliers":[2.4,3.4,4.9,8.8,13.5,34.0,88.0],
  "display":["🍒", "🍋", "🍊","🍉","⭐","💎","7️⃣"],
  "isImage": false
}', 1.74, 1);

INSERT INTO merchants (id, username, name, api_key, remark, settlement_flag, settlement_ratio, status)
VALUES (1, 'myGames', 'myGames', 's4vLDyaWvXAv8EyQckKt2UPBD5JC6Jsz', 'myGames', 'DAY', 60.00, 1);
