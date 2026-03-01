package com.games.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.games.serializer.LocalDateTimeEpochMillisSerializer;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class LeagueResponse {

    private Long id;

    private String sportTypeCode;

    private String sportTypeName;

    private String externalLeagueId;

    private String name;

    private String countryName;

    private Integer displayOrder;

    private Integer status;

    @JsonSerialize(using = LocalDateTimeEpochMillisSerializer.class)
    private LocalDateTime createdAt;

    @JsonSerialize(using = LocalDateTimeEpochMillisSerializer.class)
    private LocalDateTime updatedAt;

}
