-- 建立資料庫
CREATE DATABASE neondb
WITH ENCODING 'UTF8'
LC_COLLATE='en_US.utf8'
LC_CTYPE='en_US.utf8'
TEMPLATE=template0;

\c neondb

-- =============================================
-- 基礎表
-- =============================================

-- MERCHANTS 商戶表
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

COMMENT ON TABLE merchants IS '商戶表';
COMMENT ON COLUMN merchants.id IS '商戶ID，主鍵';
COMMENT ON COLUMN merchants.username IS '商戶帳號，唯一';
COMMENT ON COLUMN merchants.name IS '商戶名稱';
COMMENT ON COLUMN merchants.balance IS '商戶餘額';
COMMENT ON COLUMN merchants.api_key IS 'API金鑰，唯一';
COMMENT ON COLUMN merchants.remark IS '備註';
COMMENT ON COLUMN merchants.settlement_flag IS '結算週期：DAY-日結, WEEK-週結, MONTH-月結';
COMMENT ON COLUMN merchants.settlement_ratio IS '結算比例(%)';
COMMENT ON COLUMN merchants.status IS '狀態：0-停用, 1-啟用';
COMMENT ON COLUMN merchants.created_at IS '建立時間';
COMMENT ON COLUMN merchants.updated_at IS '更新時間';

CREATE INDEX idx_merchants_settlement_flag ON merchants(settlement_flag);
CREATE INDEX idx_merchants_username ON merchants(username);
CREATE INDEX idx_merchants_created_at ON merchants(created_at);

-- USERS 會員表
CREATE TABLE users (
  id BIGSERIAL PRIMARY KEY,
  merchant_id BIGINT NOT NULL REFERENCES merchants(id),
  username VARCHAR(50) NOT NULL,
  version BIGINT NOT NULL DEFAULT 0,
  password VARCHAR(255) NOT NULL,
  sport_balance NUMERIC(15,2) NOT NULL DEFAULT 0.00,
  game_balance NUMERIC(15,2) NOT NULL DEFAULT 0.00,
  created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
  UNIQUE(merchant_id, username)
);

COMMENT ON TABLE users IS '會員表';
COMMENT ON COLUMN users.id IS '會員ID，主鍵';
COMMENT ON COLUMN users.merchant_id IS '所屬商戶ID';
COMMENT ON COLUMN users.username IS '會員帳號';
COMMENT ON COLUMN users.version IS '樂觀鎖版本號';
COMMENT ON COLUMN users.password IS '密碼(加密)';
COMMENT ON COLUMN users.sport_balance IS '體育會員餘額';
COMMENT ON COLUMN users.game_balance IS '其他遊戲會員餘額';
COMMENT ON COLUMN users.created_at IS '建立時間';
COMMENT ON COLUMN users.updated_at IS '更新時間';

CREATE INDEX idx_users_merchant_id ON users(merchant_id);
CREATE INDEX idx_users_username ON users(merchant_id, username);

-- BETS 投注表
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

COMMENT ON TABLE bets IS '投注表（老虎機等遊戲）';
COMMENT ON COLUMN bets.id IS '投注ID，主鍵';
COMMENT ON COLUMN bets.merchant_id IS '商戶ID';
COMMENT ON COLUMN bets.user_id IS '會員ID';
COMMENT ON COLUMN bets.bet_amount IS '投注金額';
COMMENT ON COLUMN bets.win_amount IS '贏取金額';
COMMENT ON COLUMN bets.result IS '遊戲結果（JSON格式）';
COMMENT ON COLUMN bets.is_win IS '是否贏：TRUE-贏, FALSE-輸';
COMMENT ON COLUMN bets.game_code IS '遊戲代碼';
COMMENT ON COLUMN bets.created_at IS '投注時間';

CREATE INDEX idx_bets_user_created ON bets(user_id, created_at);
CREATE INDEX idx_bets_created ON bets(created_at);

-- TRANSACTIONS 錢包交易流水表
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

COMMENT ON TABLE transactions IS '錢包交易流水表';
COMMENT ON COLUMN transactions.id IS '交易ID，主鍵';
COMMENT ON COLUMN transactions.merchant_id IS '商戶ID';
COMMENT ON COLUMN transactions.user_id IS '會員ID';
COMMENT ON COLUMN transactions.type IS '交易類型：DEPOSIT-存款, WITHDRAW-提款, BET-投注, WIN-派彩, REFUND-退款';
COMMENT ON COLUMN transactions.amount IS '交易金額';
COMMENT ON COLUMN transactions.balance_before IS '交易前餘額';
COMMENT ON COLUMN transactions.balance_after IS '交易後餘額';
COMMENT ON COLUMN transactions.description IS '交易說明';
COMMENT ON COLUMN transactions.bet_id IS '關聯投注ID';
COMMENT ON COLUMN transactions.created_at IS '交易時間';

CREATE INDEX idx_transactions_user_id ON transactions(user_id);
CREATE INDEX idx_transactions_type ON transactions(type);
CREATE INDEX idx_transactions_created_at ON transactions(created_at);

-- GAME_SETTING 遊戲設定表
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

COMMENT ON TABLE game_setting IS '遊戲設定表';
COMMENT ON COLUMN game_setting.id IS '遊戲設定ID，主鍵';
COMMENT ON COLUMN game_setting.version IS '樂觀鎖版本號';
COMMENT ON COLUMN game_setting.game_code IS '遊戲代碼，唯一';
COMMENT ON COLUMN game_setting.name IS '遊戲名稱';
COMMENT ON COLUMN game_setting.min_bet IS '最小投注金額';
COMMENT ON COLUMN game_setting.max_bet IS '最大投注金額';
COMMENT ON COLUMN game_setting.category IS '遊戲分類：0-老虎機, 1-棋牌, 2-真人';
COMMENT ON COLUMN game_setting.rtp_set IS 'RTP設定(%)';
COMMENT ON COLUMN game_setting.game_settings IS '遊戲參數設定（JSON格式）';
COMMENT ON COLUMN game_setting.two_match_multiplier IS '兩個符號匹配倍數';
COMMENT ON COLUMN game_setting.status IS '狀態：0-停用, 1-啟用';
COMMENT ON COLUMN game_setting.created_at IS '建立時間';
COMMENT ON COLUMN game_setting.updated_at IS '更新時間';

CREATE INDEX idx_game_setting_game_code ON game_setting(game_code);
CREATE INDEX idx_game_setting_created_at ON game_setting(created_at);

-- MERCHANTS_PROFIT_REPORT 商戶利潤報表
CREATE TABLE merchants_profit_report (
  id BIGSERIAL PRIMARY KEY,
  merchant_id BIGINT NOT NULL REFERENCES merchants(id),
  total_balance NUMERIC(15,2) NOT NULL DEFAULT 0.00,
  settle_status INT NOT NULL DEFAULT 0,
  description VARCHAR(255),
  created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE merchants_profit_report IS '商戶利潤報表';
COMMENT ON COLUMN merchants_profit_report.id IS '報表ID，主鍵';
COMMENT ON COLUMN merchants_profit_report.merchant_id IS '商戶ID';
COMMENT ON COLUMN merchants_profit_report.total_balance IS '總利潤金額';
COMMENT ON COLUMN merchants_profit_report.settle_status IS '結算狀態：0-未結算, 1-已結算';
COMMENT ON COLUMN merchants_profit_report.description IS '報表說明';
COMMENT ON COLUMN merchants_profit_report.created_at IS '建立時間';

CREATE INDEX idx_merchant_profit_id ON merchants_profit_report(merchant_id);
CREATE INDEX idx_merchant_profit_created_at ON merchants_profit_report(created_at);

-- =============================================
-- 體育博弈系統擴展表
-- =============================================

-- ODDS_FORMATS 賠率格式表
CREATE TABLE odds_formats (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(20) NOT NULL UNIQUE,
    name VARCHAR(50) NOT NULL,
    name_en VARCHAR(50) NOT NULL,
    description VARCHAR(255),
    status INT NOT NULL DEFAULT 1,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE odds_formats IS '賠率格式表';
COMMENT ON COLUMN odds_formats.id IS '賠率格式ID，主鍵';
COMMENT ON COLUMN odds_formats.code IS '賠率格式代碼：ASIAN-亞洲盤, EUROPEAN-歐洲盤, HONGKONG-香港盤, MALAY-馬來盤, INDO-印尼盤, AMERICAN-美國盤, INDIAN-印度盤';
COMMENT ON COLUMN odds_formats.name IS '中文名稱';
COMMENT ON COLUMN odds_formats.name_en IS '英文名稱';
COMMENT ON COLUMN odds_formats.description IS '格式說明';
COMMENT ON COLUMN odds_formats.status IS '狀態：0-停用, 1-啟用';
COMMENT ON COLUMN odds_formats.created_at IS '建立時間';

CREATE INDEX idx_odds_formats_code ON odds_formats(code);

-- SPORT_TYPES 體育球種表
CREATE TABLE sport_types (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(20) NOT NULL UNIQUE,
    name VARCHAR(50) NOT NULL,
    display_order INT NOT NULL DEFAULT 0,
    status INT NOT NULL DEFAULT 1,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE sport_types IS '體育球種表';
COMMENT ON COLUMN sport_types.id IS '球種ID，主鍵';
COMMENT ON COLUMN sport_types.code IS '球種代碼：FOOTBALL-足球, BASKETBALL-籃球, BASEBALL-棒球, TENNIS-網球, ESPORTS-電競';
COMMENT ON COLUMN sport_types.name IS '球種名稱';
COMMENT ON COLUMN sport_types.display_order IS '顯示順序';
COMMENT ON COLUMN sport_types.status IS '狀態：0-停用, 1-啟用';
COMMENT ON COLUMN sport_types.created_at IS '建立時間';
COMMENT ON COLUMN sport_types.updated_at IS '更新時間';

CREATE INDEX idx_sport_types_code ON sport_types(code);
CREATE INDEX idx_sport_types_status ON sport_types(status);

CREATE TABLE countries (
    code CHAR(2) PRIMARY KEY,
    code_alpha3 CHAR(3) NOT NULL UNIQUE,
    code_numeric CHAR(3) NOT NULL UNIQUE,
    name_en VARCHAR(100) NOT NULL,
    name_zh VARCHAR(100) NOT NULL,
    region VARCHAR(50),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE countries IS 'ISO 3166-1 國家表';

COMMENT ON COLUMN countries.code IS 'ISO alpha-2 code';
COMMENT ON COLUMN countries.code_alpha3 IS 'ISO alpha-3 code';
COMMENT ON COLUMN countries.code_numeric IS 'ISO numeric code';
COMMENT ON COLUMN countries.name_en IS '英文名稱';
COMMENT ON COLUMN countries.name_zh IS '中文名稱';
COMMENT ON COLUMN countries.region IS '地區';

CREATE INDEX idx_countries_name_en ON countries(name_en);
CREATE INDEX idx_countries_name_zh ON countries(name_zh);

-- LEAGUES 聯賽表
CREATE TABLE leagues (
    id BIGSERIAL PRIMARY KEY,
    sport_type_id BIGINT NOT NULL REFERENCES sport_types(id),
    external_league_id VARCHAR(50),
    name VARCHAR(100) NOT NULL,
    country_code CHAR(2),
    display_order INT NOT NULL DEFAULT 0,
    status INT NOT NULL DEFAULT 1,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE leagues IS '聯賽表';
COMMENT ON COLUMN leagues.id IS '聯賽ID，主鍵';
COMMENT ON COLUMN leagues.sport_type_id IS '所屬球種ID';
COMMENT ON COLUMN leagues.external_league_id IS '外部聯賽ID（爬蟲來源）';
COMMENT ON COLUMN leagues.name IS '聯賽名稱';
COMMENT ON COLUMN leagues.country_code IS '所屬國家/地區代號（ISO alpha-2）';
COMMENT ON COLUMN leagues.display_order IS '顯示順序';
COMMENT ON COLUMN leagues.status IS '狀態：0-停用, 1-啟用';
COMMENT ON COLUMN leagues.created_at IS '建立時間';
COMMENT ON COLUMN leagues.updated_at IS '更新時間';

CREATE INDEX idx_leagues_sport_type ON leagues(sport_type_id);
CREATE INDEX idx_leagues_external_id ON leagues(external_league_id);
CREATE INDEX idx_leagues_status ON leagues(status);

-- TEAMS 隊伍表
CREATE TABLE teams (
    id BIGSERIAL PRIMARY KEY,
    sport_type_id BIGINT NOT NULL REFERENCES sport_types(id),
    external_team_id VARCHAR(50),
    name VARCHAR(100) NOT NULL,
    short_name VARCHAR(20),
    logo_url VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE teams IS '隊伍表';
COMMENT ON COLUMN teams.id IS '隊伍ID，主鍵';
COMMENT ON COLUMN teams.sport_type_id IS '所屬球種ID';
COMMENT ON COLUMN teams.external_team_id IS '外部隊伍ID（爬蟲來源）';
COMMENT ON COLUMN teams.name IS '隊伍全名';
COMMENT ON COLUMN teams.short_name IS '隊伍簡稱';
COMMENT ON COLUMN teams.logo_url IS '隊徽圖片URL';
COMMENT ON COLUMN teams.created_at IS '建立時間';
COMMENT ON COLUMN teams.updated_at IS '更新時間';

CREATE INDEX idx_teams_sport_type ON teams(sport_type_id);
CREATE INDEX idx_teams_external_id ON teams(external_team_id);

-- SPORT_EVENTS 體育賽事表
CREATE TABLE sport_events (
    id BIGSERIAL PRIMARY KEY,
    sport_type_id BIGINT NOT NULL REFERENCES sport_types(id),
    league_id BIGINT REFERENCES leagues(id),
    external_event_id VARCHAR(50) UNIQUE,
    home_team_id BIGINT NOT NULL REFERENCES teams(id),
    away_team_id BIGINT NOT NULL REFERENCES teams(id),
    home_team_name VARCHAR(100) NOT NULL,
    away_team_name VARCHAR(100) NOT NULL,
    start_time TIMESTAMP WITH TIME ZONE NOT NULL,
    home_score INT,
    away_score INT,
    home_score_half INT,
    away_score_half INT,
    betting_status VARCHAR(20) NOT NULL DEFAULT 'OPEN'
    status VARCHAR(20) NOT NULL DEFAULT 'UPCOMING',
    settle_status VARCHAR(20) NOT NULL DEFAULT 'UNSETTLED',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE sport_events IS '體育賽事表（由爬蟲服務填入）';
COMMENT ON COLUMN sport_events.id IS '賽事ID，主鍵';
COMMENT ON COLUMN sport_events.sport_type_id IS '球種ID';
COMMENT ON COLUMN sport_events.league_id IS '聯賽ID';
COMMENT ON COLUMN sport_events.external_event_id IS '外部賽事ID（爬蟲來源），唯一';
COMMENT ON COLUMN sport_events.home_team_id IS '主隊ID';
COMMENT ON COLUMN sport_events.away_team_id IS '客隊ID';
COMMENT ON COLUMN sport_events.home_team_name IS '主隊名稱（冗餘欄位）';
COMMENT ON COLUMN sport_events.away_team_name IS '客隊名稱（冗餘欄位）';
COMMENT ON COLUMN sport_events.start_time IS '開賽時間';
COMMENT ON COLUMN sport_events.home_score IS '主隊全場比分';
COMMENT ON COLUMN sport_events.away_score IS '客隊全場比分';
COMMENT ON COLUMN sport_events.home_score_half IS '主隊半場比分';
COMMENT ON COLUMN sport_events.away_score_half IS '客隊半場比分';
COMMENT ON COLUMN sport_events.betting_status IS '投注狀態 OPEN-可投注, LOCKED-鎖盤, CLOSED-關閉, SETTLED-已結算';
COMMENT ON COLUMN sport_events.status IS '賽事狀態：UPCOMING-未開賽, LIVE-進行中, FINISHED-已結束, CANCELLED-取消, POSTPONED-延期';
COMMENT ON COLUMN sport_events.settle_status IS '結算狀態：UNSETTLED-未結算, SETTLED-已結算, VOID-作廢';
COMMENT ON COLUMN sport_events.created_at IS '建立時間';
COMMENT ON COLUMN sport_events.updated_at IS '更新時間';

CREATE INDEX idx_sport_events_sport_type ON sport_events(sport_type_id);
CREATE INDEX idx_sport_events_league ON sport_events(league_id);
CREATE INDEX idx_sport_events_external_id ON sport_events(external_event_id);
CREATE INDEX idx_sport_events_start_time ON sport_events(start_time);
CREATE INDEX idx_sport_events_status ON sport_events(status);
CREATE INDEX idx_sport_events_settle_status ON sport_events(settle_status);
CREATE INDEX idx_sport_events_betting_status ON sport_events(betting_status);

-- BET_TYPES 玩法類型表
CREATE TABLE bet_types (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(30) NOT NULL UNIQUE,
    name VARCHAR(50) NOT NULL,
    odds_format_id BIGINT REFERENCES odds_formats(id),
    description VARCHAR(255),
    status INT NOT NULL DEFAULT 1,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE bet_types IS '玩法類型表';
COMMENT ON COLUMN bet_types.id IS '玩法ID，主鍵';
COMMENT ON COLUMN bet_types.code IS '玩法代碼：AH-亞洲讓球, OU-大小, 1X2-獨贏, CS-波膽, BTTS-兩隊進球, OE-單雙';
COMMENT ON COLUMN bet_types.name IS '玩法名稱';
COMMENT ON COLUMN bet_types.odds_format_id IS '預設賠率格式ID';
COMMENT ON COLUMN bet_types.description IS '玩法說明';
COMMENT ON COLUMN bet_types.status IS '狀態：0-停用, 1-啟用';
COMMENT ON COLUMN bet_types.created_at IS '建立時間';

CREATE INDEX idx_bet_types_code ON bet_types(code);
CREATE INDEX idx_bet_types_odds_format ON bet_types(odds_format_id);

-- MARKET_LINES 盤口賠率線表
CREATE TABLE market_lines (
    id BIGSERIAL PRIMARY KEY,
    event_id BIGINT NOT NULL REFERENCES sport_events(id),
    bet_type_id BIGINT NOT NULL REFERENCES bet_types(id),
    odds_format_id BIGINT NOT NULL REFERENCES odds_formats(id),
    external_market_id VARCHAR(50),
    handicap NUMERIC(5,2),
    home_odds NUMERIC(10,4),
    away_odds NUMERIC(10,4),
    draw_odds NUMERIC(10,4),
    over_odds NUMERIC(10,4),
    under_odds NUMERIC(10,4),
    yes_odds NUMERIC(10,4),
    no_odds NUMERIC(10,4),
    odd_odds NUMERIC(10,4),
    even_odds NUMERIC(10,4),
    score_odds JSONB,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE market_lines IS '盤口賠率線表（由爬蟲服務填入）';
COMMENT ON COLUMN market_lines.id IS '盤口ID，主鍵';
COMMENT ON COLUMN market_lines.event_id IS '賽事ID';
COMMENT ON COLUMN market_lines.bet_type_id IS '玩法類型ID';
COMMENT ON COLUMN market_lines.odds_format_id IS '賠率格式ID';
COMMENT ON COLUMN market_lines.external_market_id IS '外部盤口ID（爬蟲來源）';
COMMENT ON COLUMN market_lines.handicap IS '讓球/大小值，如 -0.5, 0.25, 2.5, 215.5';
COMMENT ON COLUMN market_lines.home_odds IS '主隊賠率';
COMMENT ON COLUMN market_lines.away_odds IS '客隊賠率';
COMMENT ON COLUMN market_lines.draw_odds IS '和局賠率（歐洲盤用）';
COMMENT ON COLUMN market_lines.over_odds IS '大盤賠率';
COMMENT ON COLUMN market_lines.under_odds IS '小盤賠率';
COMMENT ON COLUMN market_lines.yes_odds IS '是賠率（兩隊進球等）';
COMMENT ON COLUMN market_lines.no_odds IS '否賠率';
COMMENT ON COLUMN market_lines.odd_odds IS '單數賠率';
COMMENT ON COLUMN market_lines.even_odds IS '雙數賠率';
COMMENT ON COLUMN market_lines.score_odds IS '波膽賠率（JSON格式），如 {"1-0": 7.50, "2-1": 9.50}';
COMMENT ON COLUMN market_lines.is_active IS '是否有效：TRUE-有效, FALSE-停用';
COMMENT ON COLUMN market_lines.created_at IS '建立時間';
COMMENT ON COLUMN market_lines.updated_at IS '更新時間';

CREATE INDEX idx_market_lines_event ON market_lines(event_id);
CREATE INDEX idx_market_lines_bet_type ON market_lines(bet_type_id);
CREATE INDEX idx_market_lines_odds_format ON market_lines(odds_format_id);
CREATE INDEX idx_market_lines_active ON market_lines(is_active);

-- SPORT_BETS 體育投注主表
CREATE TABLE sport_bets (
    id BIGSERIAL PRIMARY KEY,
    merchant_id BIGINT NOT NULL REFERENCES merchants(id),
    user_id BIGINT NOT NULL REFERENCES users(id),
    bet_type VARCHAR(20) NOT NULL,
    stake NUMERIC(15,4) NOT NULL,
    total_odds NUMERIC(15,4),
    potential_win NUMERIC(15,4),
    win_amount NUMERIC(15,4) DEFAULT 0,
    valid_bet NUMERIC(15,4),
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    cashout_amount NUMERIC(15,4),
    cancel_reason VARCHAR(255),
    placed_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    settled_at TIMESTAMP WITH TIME ZONE,
    cancelled_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE sport_bets IS '體育投注主表';
COMMENT ON COLUMN sport_bets.id IS '投注ID，主鍵';
COMMENT ON COLUMN sport_bets.merchant_id IS '商戶ID';
COMMENT ON COLUMN sport_bets.user_id IS '會員ID';
COMMENT ON COLUMN sport_bets.bet_type IS '投注類型：SINGLE-單注, PARLAY-串關';
COMMENT ON COLUMN sport_bets.stake IS '投注金額';
COMMENT ON COLUMN sport_bets.total_odds IS '總賠率（串關為各腿相乘）';
COMMENT ON COLUMN sport_bets.potential_win IS '預計最大贏取金額';
COMMENT ON COLUMN sport_bets.win_amount IS '實際贏取金額';
COMMENT ON COLUMN sport_bets.valid_bet IS '有效投注額';
COMMENT ON COLUMN sport_bets.status IS '投注狀態：PENDING-待結算, SETTLED-已結算, CANCELLED-已取消, CASHED_OUT-提前結算';
COMMENT ON COLUMN sport_bets.cashout_amount IS '提前兌現金額';
COMMENT ON COLUMN sport_bets.cancel_reason IS '取消原因';
COMMENT ON COLUMN sport_bets.placed_at IS '下注時間';
COMMENT ON COLUMN sport_bets.settled_at IS '結算時間';
COMMENT ON COLUMN sport_bets.cancelled_at IS '取消時間';
COMMENT ON COLUMN sport_bets.created_at IS '建立時間';
COMMENT ON COLUMN sport_bets.updated_at IS '更新時間';

CREATE INDEX idx_sport_bets_merchant ON sport_bets(merchant_id);
CREATE INDEX idx_sport_bets_user ON sport_bets(user_id);
CREATE INDEX idx_sport_bets_status ON sport_bets(status);
CREATE INDEX idx_sport_bets_placed_at ON sport_bets(placed_at);
CREATE INDEX idx_sport_bets_user_placed ON sport_bets(user_id, placed_at);

-- BET_LEGS 投注明細表（單腿）
CREATE TABLE bet_legs (
    id BIGSERIAL PRIMARY KEY,
    bet_id BIGINT NOT NULL REFERENCES sport_bets(id),
    event_id BIGINT NOT NULL REFERENCES sport_events(id),
    market_line_id BIGINT NOT NULL REFERENCES market_lines(id),
    bet_type_code VARCHAR(30) NOT NULL,
    odds_format_code VARCHAR(20) NOT NULL,
    selection VARCHAR(20) NOT NULL,
    handicap NUMERIC(5,2),
    odds NUMERIC(10,4) NOT NULL,
    odds_decimal NUMERIC(10,4) NOT NULL,
    result VARCHAR(20) DEFAULT 'PENDING',
    result_factor NUMERIC(5,2) DEFAULT 1.00,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE bet_legs IS '投注明細表（每一腿）';
COMMENT ON COLUMN bet_legs.id IS '明細ID，主鍵';
COMMENT ON COLUMN bet_legs.bet_id IS '所屬投注ID';
COMMENT ON COLUMN bet_legs.event_id IS '賽事ID';
COMMENT ON COLUMN bet_legs.market_line_id IS '盤口ID';
COMMENT ON COLUMN bet_legs.bet_type_code IS '玩法代碼';
COMMENT ON COLUMN bet_legs.odds_format_code IS '賠率格式代碼';
COMMENT ON COLUMN bet_legs.selection IS '選擇項：HOME-主隊, AWAY-客隊, OVER-大, UNDER-小, DRAW-平, YES-是, NO-否, ODD-單, EVEN-雙';
COMMENT ON COLUMN bet_legs.handicap IS '讓球/大小值';
COMMENT ON COLUMN bet_legs.odds IS '原始賠率（依賠率格式）';
COMMENT ON COLUMN bet_legs.odds_decimal IS '轉換後的歐洲盤賠率（用於統一計算）';
COMMENT ON COLUMN bet_legs.result IS '結果：WIN-贏, LOSE-輸, PUSH-和, HALF_WIN-半贏, HALF_LOSE-半輸, VOID-作廢, PENDING-待定';
COMMENT ON COLUMN bet_legs.result_factor IS '結算係數：WIN=1.0, HALF_WIN=0.5, PUSH=0(退款), HALF_LOSE=-0.5, LOSE=-1.0, VOID=0(退款)';
COMMENT ON COLUMN bet_legs.created_at IS '建立時間';
COMMENT ON COLUMN bet_legs.updated_at IS '更新時間';

CREATE INDEX idx_bet_legs_bet ON bet_legs(bet_id);
CREATE INDEX idx_bet_legs_event ON bet_legs(event_id);
CREATE INDEX idx_bet_legs_result ON bet_legs(result);

-- SPORT_TRANSACTIONS 體育投注交易流水表
CREATE TABLE sport_transactions (
    id BIGSERIAL PRIMARY KEY,
    merchant_id BIGINT NOT NULL REFERENCES merchants(id),
    user_id BIGINT NOT NULL REFERENCES users(id),
    sport_bet_id BIGINT REFERENCES sport_bets(id),
    type VARCHAR(30) NOT NULL,
    amount NUMERIC(15,4) NOT NULL,
    balance_before NUMERIC(15,4),
    balance_after NUMERIC(15,4),
    description VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

COMMENT ON TABLE sport_transactions IS '體育投注交易流水表';
COMMENT ON COLUMN sport_transactions.id IS '交易ID，主鍵';
COMMENT ON COLUMN sport_transactions.merchant_id IS '商戶ID';
COMMENT ON COLUMN sport_transactions.user_id IS '會員ID';
COMMENT ON COLUMN sport_transactions.sport_bet_id IS '關聯體育投注ID';
COMMENT ON COLUMN sport_transactions.type IS '交易類型：SPORT_BET-體育投注, SPORT_WIN-體育派彩, SPORT_REFUND-體育退款, SPORT_CANCEL-體育取消';
COMMENT ON COLUMN sport_transactions.amount IS '交易金額';
COMMENT ON COLUMN sport_transactions.balance_before IS '交易前餘額';
COMMENT ON COLUMN sport_transactions.balance_after IS '交易後餘額';
COMMENT ON COLUMN sport_transactions.description IS '交易說明';
COMMENT ON COLUMN sport_transactions.created_at IS '交易時間';

CREATE INDEX idx_sport_trans_user ON sport_transactions(user_id);
CREATE INDEX idx_sport_trans_bet ON sport_transactions(sport_bet_id);
CREATE INDEX idx_sport_trans_type ON sport_transactions(type);
CREATE INDEX idx_sport_trans_created ON sport_transactions(created_at);

-- SPORT_MERCHANTS_PROFIT_REPORT 體育商戶利潤報表
CREATE TABLE sport_merchants_profit_report (
    id BIGSERIAL PRIMARY KEY,
    merchant_id BIGINT NOT NULL REFERENCES merchants(id),
    sport_type_id BIGINT REFERENCES sport_types(id),
    report_date DATE NOT NULL,
    total_bets INT NOT NULL DEFAULT 0,
    total_stake NUMERIC(15,4) NOT NULL DEFAULT 0,
    total_valid_bet NUMERIC(15,4) NOT NULL DEFAULT 0,
    total_payout NUMERIC(15,4) NOT NULL DEFAULT 0,
    total_profit NUMERIC(15,4) NOT NULL DEFAULT 0,
    settle_status INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    UNIQUE(merchant_id, sport_type_id, report_date)
);

COMMENT ON TABLE sport_merchants_profit_report IS '體育商戶利潤報表';
COMMENT ON COLUMN sport_merchants_profit_report.id IS '報表ID，主鍵';
COMMENT ON COLUMN sport_merchants_profit_report.merchant_id IS '商戶ID';
COMMENT ON COLUMN sport_merchants_profit_report.sport_type_id IS '球種ID（NULL表示所有球種）';
COMMENT ON COLUMN sport_merchants_profit_report.report_date IS '報表日期';
COMMENT ON COLUMN sport_merchants_profit_report.total_bets IS '總投注筆數';
COMMENT ON COLUMN sport_merchants_profit_report.total_stake IS '總投注金額';
COMMENT ON COLUMN sport_merchants_profit_report.total_valid_bet IS '總有效投注額';
COMMENT ON COLUMN sport_merchants_profit_report.total_payout IS '總派彩金額';
COMMENT ON COLUMN sport_merchants_profit_report.total_profit IS '總利潤（投注-派彩）';
COMMENT ON COLUMN sport_merchants_profit_report.settle_status IS '結算狀態：0-未結算, 1-已結算';
COMMENT ON COLUMN sport_merchants_profit_report.created_at IS '建立時間';
COMMENT ON COLUMN sport_merchants_profit_report.updated_at IS '更新時間';

CREATE INDEX idx_sport_profit_merchant ON sport_merchants_profit_report(merchant_id);
CREATE INDEX idx_sport_profit_date ON sport_merchants_profit_report(report_date);

-- =============================================
-- 初始化資料
-- =============================================

-- 基礎遊戲設定
INSERT INTO game_setting (id, game_code, name, min_bet, max_bet, rtp_set, game_settings, two_match_multiplier, status)
VALUES (1, '0000', 'Slot Sample', 10.00, 1000.00, 90.00,
'{
  "symbols":["ONE","TWO","THREE","FOUR","FIVE","SIX","SEVEN"],
  "symbolWeights":[15.0,13.0,10.0,8.0,6.0,5.0,1.0],
  "payoutMultipliers":[2.4,3.4,4.9,8.8,13.5,34.0,88.0],
  "display":["🍒", "🍋", "🍊","🍉","⭐","💎","7️⃣"],
  "isImage": false
}', 1.74, 1);

-- 基礎商戶
INSERT INTO merchants (id, username, name, api_key, remark, settlement_flag, settlement_ratio, status)
VALUES (1, 'myGames', 'myGames', 's4vLDyaWvXAv8EyQckKt2UPBD5JC6Jsz', 'myGames', 'DAY', 60.00, 1);

-- 賠率格式
INSERT INTO odds_formats (code, name, name_en, description, status) VALUES
('ASIAN', '亞洲盤', 'Asian Odds', '最常見於亞洲，水錢制，如 0.85/-0.95', 1),
('EUROPEAN', '歐洲盤', 'European Odds', 'Decimal格式，如 1.85, 2.10', 1),
('HONGKONG', '香港盤', 'Hong Kong Odds', '亞洲常用，淨賠率，如 0.85 表示贏0.85倍', 1),
('MALAY', '馬來盤', 'Malay Odds', '可正可負，正數同香港盤，負數為反向', 1),
('INDO', '印尼盤', 'Indonesian Odds', '類似馬來盤，正負相反', 1),
('AMERICAN', '美國盤', 'American Odds', '正負值表示，如 +150 / -200', 1),
('INDIAN', '印度盤', 'Indian Odds', '印度市場使用，同香港盤', 1);

-- 球種
INSERT INTO sport_types (code, name, display_order, status) VALUES
('FOOTBALL', '足球', 1, 1),
('BASKETBALL', '籃球', 2, 1),
('BASEBALL', '棒球', 3, 1),
('TENNIS', '網球', 4, 1),
('ESPORTS', '電競', 5, 1),
('HOCKEY', '冰球', 6, 1),
('VOLLEYBALL', '排球', 7, 1),
('BADMINTON', '羽毛球', 8, 1);

-- 玩法類型
INSERT INTO bet_types (code, name, odds_format_id, description, status) VALUES
-- 亞洲盤玩法
('AH', '亞洲讓球', 1, '亞洲讓球盤，支援半球/四分盤，可半贏半輸', 1),
('AH_HALF', '半場亞洲讓球', 1, '半場亞洲讓球盤', 1),
('OU', '亞洲大小', 1, '亞洲大小盤，支援半球/四分盤', 1),
('OU_HALF', '半場亞洲大小', 1, '半場亞洲大小盤', 1),
-- 歐洲盤玩法
('1X2', '獨贏', 2, '歐洲三項盤（主/和/客）', 1),
('1X2_HALF', '半場獨贏', 2, '半場歐洲三項盤', 1),
('DC', '雙重機會', 2, '雙重機會（主或和/客或和/主或客）', 1),
('DNB', '讓球和局退款', 2, '和局退款盤', 1),
-- 波膽玩法
('CS', '波膽', 2, '正確比分', 1),
('CS_HALF', '半場波膽', 2, '半場正確比分', 1),
-- 進球玩法
('BTTS', '兩隊都進球', 2, '兩隊是否都進球', 1),
('TG', '總進球數', 2, '總進球數區間', 1),
-- 單雙玩法
('OE', '單雙', 2, '總分單雙', 1),
-- 香港盤玩法
('HK_AH', '香港讓球', 3, '香港盤讓球', 1),
('HK_OU', '香港大小', 3, '香港盤大小', 1),
-- 馬來盤玩法
('MY_AH', '馬來讓球', 4, '馬來盤讓球，可正可負', 1),
('MY_OU', '馬來大小', 4, '馬來盤大小', 1),
-- 印尼盤玩法
('ID_AH', '印尼讓球', 5, '印尼盤讓球', 1),
('ID_OU', '印尼大小', 5, '印尼盤大小', 1),
-- 美國盤玩法
('US_ML', '美式獨贏', 6, '美國盤獨贏（Money Line）', 1),
('US_SPREAD', '美式讓分', 6, '美國盤讓分（Point Spread）', 1),
('US_TOTAL', '美式大小', 6, '美國盤大小（Total）', 1);

INSERT INTO countries
(code, code_alpha3, code_numeric, name_en, name_zh, region)
VALUES

-- Asia
('TW','TWN','158','Taiwan','台灣','Asia'),
('CN','CHN','156','China','中國','Asia'),
('JP','JPN','392','Japan','日本','Asia'),
('KR','KOR','410','South Korea','韓國','Asia'),
('HK','HKG','344','Hong Kong','香港','Asia'),
('SG','SGP','702','Singapore','新加坡','Asia'),
('IN','IND','356','India','印度','Asia'),
('TH','THA','764','Thailand','泰國','Asia'),
('VN','VNM','704','Vietnam','越南','Asia'),
('MY','MYS','458','Malaysia','馬來西亞','Asia'),
('PH','PHL','608','Philippines','菲律賓','Asia'),
('ID','IDN','360','Indonesia','印尼','Asia'),

-- Europe
('GB','GBR','826','United Kingdom','英國','Europe'),
('ES','ESP','724','Spain','西班牙','Europe'),
('IT','ITA','380','Italy','義大利','Europe'),
('DE','DEU','276','Germany','德國','Europe'),
('FR','FRA','250','France','法國','Europe'),
('NL','NLD','528','Netherlands','荷蘭','Europe'),
('PT','PRT','620','Portugal','葡萄牙','Europe'),

-- North America
('US','USA','840','United States','美國','North America'),
('CA','CAN','124','Canada','加拿大','North America'),
('MX','MEX','484','Mexico','墨西哥','North America'),

-- South America
('BR','BRA','076','Brazil','巴西','South America'),
('AR','ARG','032','Argentina','阿根廷','South America'),

-- Oceania
('AU','AUS','036','Australia','澳洲','Oceania'),
('NZ','NZL','554','New Zealand','紐西蘭','Oceania'),

-- Africa
('ZA','ZAF','710','South Africa','南非','Africa'),
('EG','EGY','818','Egypt','埃及','Africa');

-- 範例聯賽
INSERT INTO leagues (sport_type_id, external_league_id, name, country_code, display_order, status) VALUES
(1, 'EPL', '英格蘭超級聯賽', 'GB', 1, 1),
(1, 'LALIGA', '西班牙甲級聯賽', 'ES', 2, 1),
(1, 'SERIE_A', '義大利甲級聯賽', 'IT', 3, 1),
(1, 'BUNDESLIGA', '德國甲級聯賽', 'DE', 4, 1),
(1, 'LIGUE_1', '法國甲級聯賽', 'FR', 5, 1),
(2, 'NBA', '美國職業籃球聯賽', 'US', 1, 1),
(2, 'CBA', '中國籃球協會聯賽', 'CN', 2, 1);

-- 範例隊伍
INSERT INTO teams (sport_type_id, external_team_id, name, short_name) VALUES
(1, 'MU', '曼徹斯特聯', '曼聯'),
(1, 'MC', '曼徹斯特城', '曼城'),
(1, 'LIV', '利物浦', '利物浦'),
(1, 'CHE', '切爾西', '切爾西'),
(1, 'ARS', '阿森納', '阿森納'),
(1, 'TOT', '托特納姆熱刺', '熱刺'),
(2, 'LAL', '洛杉磯湖人', '湖人'),
(2, 'GSW', '金州勇士', '勇士');

-- 範例賽事
INSERT INTO sport_events (sport_type_id, league_id, external_event_id, home_team_id, away_team_id, home_team_name, away_team_name, start_time, status, settle_status, betting_status) VALUES
(1, 1, 'EPL_20250201_001', 1, 2, '曼徹斯特聯', '曼徹斯特城', '2025-02-01 20:00:00+00', 'UPCOMING', 'UNSETTLED', 'OPEN'),
(1, 1, 'EPL_20250201_002', 3, 4, '利物浦', '切爾西', '2025-02-01 17:30:00+00', 'UPCOMING', 'UNSETTLED', 'OPEN'),
(2, 6, 'NBA_20250201_001', 7, 8, '洛杉磯湖人', '金州勇士', '2025-02-02 03:00:00+00', 'UPCOMING', 'UNSETTLED', 'OPEN');

-- 範例盤口：足球 曼聯 vs 曼城 (event_id = 1)
-- 亞洲讓球盤
INSERT INTO market_lines (event_id, bet_type_id, odds_format_id, handicap, home_odds, away_odds, is_active) VALUES
(1, 1, 1, 0.00, 0.95, 0.95, TRUE),
(1, 1, 1, -0.25, 1.05, 0.85, TRUE),
(1, 1, 1, -0.50, 1.15, 0.75, TRUE),
(1, 1, 1, -0.75, 1.25, 0.65, TRUE),
(1, 1, 1, -1.00, 1.40, 0.55, TRUE);

-- 亞洲大小盤
INSERT INTO market_lines (event_id, bet_type_id, odds_format_id, handicap, over_odds, under_odds, is_active) VALUES
(1, 3, 1, 2.00, 1.10, 0.80, TRUE),
(1, 3, 1, 2.25, 0.95, 0.95, TRUE),
(1, 3, 1, 2.50, 0.85, 1.05, TRUE),
(1, 3, 1, 2.75, 0.75, 1.15, TRUE),
(1, 3, 1, 3.00, 0.70, 1.20, TRUE);

-- 歐洲獨贏盤
INSERT INTO market_lines (event_id, bet_type_id, odds_format_id, home_odds, draw_odds, away_odds, is_active) VALUES
(1, 5, 2, 2.80, 3.40, 2.50, TRUE);

-- 歐洲雙重機會盤
INSERT INTO market_lines (event_id, bet_type_id, odds_format_id, home_odds, draw_odds, away_odds, is_active) VALUES
(1, 7, 2, 1.45, 1.35, 1.55, TRUE);

-- 香港盤讓球
INSERT INTO market_lines (event_id, bet_type_id, odds_format_id, handicap, home_odds, away_odds, is_active) VALUES
(1, 14, 3, -0.50, 0.85, 0.95, TRUE),
(1, 14, 3, -1.00, 1.10, 0.80, TRUE);

-- 馬來盤讓球
INSERT INTO market_lines (event_id, bet_type_id, odds_format_id, handicap, home_odds, away_odds, is_active) VALUES
(1, 16, 4, -0.50, 0.85, -1.05, TRUE),
(1, 16, 4, -1.00, 1.10, -0.80, TRUE);

-- 印尼盤讓球
INSERT INTO market_lines (event_id, bet_type_id, odds_format_id, handicap, home_odds, away_odds, is_active) VALUES
(1, 18, 5, -0.50, -1.18, 0.95, TRUE),
(1, 18, 5, -1.00, 1.10, -1.25, TRUE);

-- 美國盤獨贏
INSERT INTO market_lines (event_id, bet_type_id, odds_format_id, home_odds, away_odds, is_active) VALUES
(1, 20, 6, 180, 150, TRUE);

-- 波膽
INSERT INTO market_lines (event_id, bet_type_id, odds_format_id, score_odds, is_active) VALUES
(1, 9, 2, '{"1-0": 7.50, "2-0": 12.00, "2-1": 9.50, "3-0": 21.00, "3-1": 17.00, "3-2": 29.00, "0-0": 11.00, "1-1": 6.50, "2-2": 15.00, "3-3": 51.00, "0-1": 8.00, "0-2": 13.00, "1-2": 10.00, "0-3": 23.00, "1-3": 19.00, "2-3": 31.00}', TRUE);

-- 兩隊都進球
INSERT INTO market_lines (event_id, bet_type_id, odds_format_id, yes_odds, no_odds, is_active) VALUES
(1, 11, 2, 1.85, 1.95, TRUE);

-- 單雙盤
INSERT INTO market_lines (event_id, bet_type_id, odds_format_id, odd_odds, even_odds, is_active) VALUES
(1, 13, 2, 1.90, 1.90, TRUE);

-- 範例盤口：籃球 湖人 vs 勇士 (event_id = 3)
-- 亞洲讓分盤
INSERT INTO market_lines (event_id, bet_type_id, odds_format_id, handicap, home_odds, away_odds, is_active) VALUES
(3, 1, 1, -5.5, 0.90, 1.00, TRUE),
(3, 1, 1, -6.0, 0.95, 0.95, TRUE),
(3, 1, 1, -6.5, 1.00, 0.90, TRUE);

-- 籃球大小盤
INSERT INTO market_lines (event_id, bet_type_id, odds_format_id, handicap, over_odds, under_odds, is_active) VALUES
(3, 3, 1, 215.5, 0.90, 1.00, TRUE),
(3, 3, 1, 216.0, 0.95, 0.95, TRUE),
(3, 3, 1, 216.5, 1.00, 0.90, TRUE);

-- 美式讓分盤
INSERT INTO market_lines (event_id, bet_type_id, odds_format_id, handicap, home_odds, away_odds, is_active) VALUES
(3, 21, 6, -5.5, -110, -110, TRUE);

-- 美式大小盤
INSERT INTO market_lines (event_id, bet_type_id, odds_format_id, handicap, over_odds, under_odds, is_active) VALUES
(3, 22, 6, 215.5, -110, -110, TRUE);
