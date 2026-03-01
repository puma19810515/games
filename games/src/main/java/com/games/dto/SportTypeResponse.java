package com.games.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.games.serializer.LocalDateTimeEpochMillisSerializer;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class SportTypeResponse {

    private Long id;

    /**
     * sport code
     * example: FOOTBALL, CRICKET
     */
    private String code;

    /**
     * sport display name
     */
    private String name;

    /**
     * display order
     */
    private Integer displayOrder;

    /**
     * 0 = disabled
     * 1 = enabled
     */
    private Integer status;

    /**
     * PostgreSQL TIMESTAMP WITH TIME ZONE
     */
    @JsonSerialize(using = LocalDateTimeEpochMillisSerializer.class)
    private LocalDateTime createdAt;
    @JsonSerialize(using = LocalDateTimeEpochMillisSerializer.class)
    private LocalDateTime updatedAt;
}
