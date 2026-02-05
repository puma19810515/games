package com.games.constant;

public class RedisConstant {

    public static final String REGISTER = "REGISTER";
    public static final String DEPOSIT = "DEPOSIT";
    public static final String WITHDRAW = "WITHDRAW";
    public static final String PLACE_BET = "PLACE_BET";

    public static final String RTP_GAME_BET_KEY = "rtp:game:bet";
    public static final String RTP_GAME_WIN_KEY = "rtp:game:win";
    public static final String RTP_BET_GAME_COUNT_KEY = "rtp:game:count";

    public static final String RTP_TOTAL_BET_KEY = "rtp:total:bet";
    public static final String RTP_TOTAL_WIN_KEY = "rtp:total:win";
    public static final String RTP_BET_COUNT_KEY = "rtp:total:count";
    public static final long STATS_TTL_DAYS = 30;

    public static final String GAME_SETTING_ALL = "game_setting_all";
}
