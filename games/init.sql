CREATE DATABASE IF NOT EXISTS taskdb 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

USE taskdb;

-- USERS
CREATE TABLE users (
  id BIGINT NOT NULL,
  username VARCHAR(50) NOT NULL UNIQUE,
  version BIGINT NOT NULL DEFAULT 0,
  password VARCHAR(255) NOT NULL,
  balance DECIMAL(15,2) NOT NULL DEFAULT 0.00,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  INDEX idx_username (username)
) ENGINE=InnoDB;

-- BETS
CREATE TABLE bets (
  id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  bet_amount DECIMAL(15,2) NOT NULL,
  win_amount DECIMAL(15,2) NOT NULL DEFAULT 0.00,
  result VARCHAR(500) NOT NULL,
  is_win BOOLEAN NOT NULL DEFAULT FALSE,
  game_code VARCHAR(10) NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  INDEX idx_user_id (user_id),
  INDEX idx_created_at (created_at),
  CONSTRAINT fk_bet_user FOREIGN KEY (user_id) REFERENCES users(id)
) ENGINE=InnoDB;

-- WALLET TRANSACTIONS
CREATE TABLE transactions (
  id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  type VARCHAR(20) NOT NULL,
  amount DECIMAL(15,2) NOT NULL,
  balance_before DECIMAL(15,2),
  balance_after DECIMAL(15,2),
  description VARCHAR(255),
  bet_id BIGINT,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  INDEX idx_user_id (user_id),
  INDEX idx_type (type),
  INDEX idx_created_at (created_at),
  CONSTRAINT fk_tx_user FOREIGN KEY (user_id) REFERENCES users(id),
  CONSTRAINT fk_tx_bet FOREIGN KEY (bet_id) REFERENCES bets(id)
) ENGINE=InnoDB;

-- GAME SETTING
CREATE TABLE game_setting (
  id BIGINT NOT NULL,
  version BIGINT NOT NULL DEFAULT 0,
  game_code VARCHAR(10) NOT NULL,
  name VARCHAR(100) NOT NULL,
  min_bet DECIMAL(15,2) NOT NULL,
  max_bet DECIMAL(15,2) NOT NULL,
  category int NOT NULL DEFAULT 0,
  rtp_set DECIMAL(15,2) NOT NULL,
  game_settings TEXT NOT NULL,
  two_match_multiplier DECIMAL(15,2) NOT NULL DEFAULT 2.00,
  status int NOT NULL DEFAULT 0,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE(game_code),
  INDEX idx_game_code (game_code),
  INDEX idx_created_at (created_at)
) ENGINE=InnoDB;

INSERT INTO game_setting (id, game_code, name, min_bet, max_bet, rtp_set, game_settings, two_match_multiplier, status) VALUES (1, '0000', 'Slot Sample', 10.00, 1000.00, 90.00, '{"symbols":["ONE","TWO","THREE","FOUR","FIVE","SIX","SEVEN"],"symbolWeights":[15.0,13.0,10.0,8.0,6.0,5.0,1.0],"payoutMultipliers":[2.4,3.4,4.9,8.8,13.5,34.0,88.0],"display":["🍒", "🍋", "🍊","🍉","⭐","💎","7️⃣"], "isImage" : false}', 1.66, 1);
commit;
