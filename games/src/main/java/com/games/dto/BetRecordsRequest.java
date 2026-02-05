package com.games.dto;

import com.games.util.PageReqUtil;
import lombok.Data;

@Data
public class BetRecordsRequest extends PageReqUtil {
    private String gameCode;
    private String startTime;
    private String endTime;
}
