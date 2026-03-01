package com.games.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class OddsFormatResponse {

    private Long id;

    /**
     * 賠率格式代碼：ASIAN-亞洲盤, EUROPEAN-歐洲盤, HONGKONG-香港盤,
     * MALAY-馬來盤, INDO-印尼盤, AMERICAN-美國盤, INDIAN-印度盤
     */
    private String code;

    /**
     * 中文名稱
     */
    private String name;

    /**
     * 英文名稱
     */
    private String nameEn;

    /**
     * 格式說明
     */
    private String description;

    /**
     * 狀態：0-停用, 1-啟用
     */
    private Integer status;
}
