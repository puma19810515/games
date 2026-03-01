package com.games.service;

import com.games.dto.SportEventResponse;
import com.games.entity.MarketLine;
import com.games.entity.SportEvent;
import com.games.enums.SportEventBettingStatus;
import com.games.repository.MarketLineRepository;
import com.games.repository.SportEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 體育賽事服務
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SportEventService {

    private final SportEventRepository sportEventRepository;
    private final MarketLineRepository marketLineRepository;

    /**
     * 查詢可投注的賽事列表
     *
     * @param sportTypeCode 球種代碼（可選）
     * @return 賽事列表
     */
    public List<SportEventResponse> getOpenEvents(String sportTypeCode) {
        List<SportEvent> events = sportEventRepository.findOpenEvents(sportTypeCode, LocalDateTime.now());
        return events.stream()
                .map(this::buildEventResponse)
                .collect(Collectors.toList());
    }

    /**
     * 查詢賽事詳情（含盤口）
     *
     * @param eventId 賽事ID
     * @return 賽事詳情
     */
    public SportEventResponse getEventDetail(Long eventId) {
        SportEvent event = sportEventRepository.findByIdWithDetails(eventId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "賽事不存在"));

        List<MarketLine> marketLines = marketLineRepository.findActiveByEventId(eventId);
        return buildEventResponseWithMarkets(event, marketLines);
    }

    /**
     * 分頁查詢賽事
     */
    public Page<SportEventResponse> getEventsByFilters(Long sportTypeId, Long leagueId, int page, int size) {
        Page<SportEvent> events = sportEventRepository.findByFilters(
                SportEventBettingStatus.OPEN,
                sportTypeId,
                leagueId,
                PageRequest.of(page, size)
        );
        return events.map(this::buildEventResponse);
    }

    /**
     * 建立賽事響應（不含盤口）
     */
    private SportEventResponse buildEventResponse(SportEvent event) {
        return SportEventResponse.builder()
                .eventId(event.getId())
                .sportTypeCode(event.getSportType() != null ? event.getSportType().getCode() : null)
                .sportTypeName(event.getSportType() != null ? event.getSportType().getName() : null)
                .leagueId(event.getLeague() != null ? event.getLeague().getId() : null)
                .leagueName(event.getLeague() != null ? event.getLeague().getName() : null)
                .homeTeamId(event.getHomeTeam() != null ? event.getHomeTeam().getId() : null)
                .homeTeamName(event.getHomeTeamName())
                .awayTeamId(event.getAwayTeam() != null ? event.getAwayTeam().getId() : null)
                .awayTeamName(event.getAwayTeamName())
                .startTime(event.getStartTime())
                .homeScore(event.getHomeScore())
                .awayScore(event.getAwayScore())
                .status(event.getSportEventStatus() != null ? event.getSportEventStatus().name() : null)
                .bettingStatus(event.getSportEventBettingStatus() != null ? event.getSportEventBettingStatus().name() : null)
                .build();
    }

    /**
     * 建立賽事響應（含盤口）
     */
    private SportEventResponse buildEventResponseWithMarkets(SportEvent event, List<MarketLine> marketLines) {
        SportEventResponse response = buildEventResponse(event);

        List<SportEventResponse.MarketLineResponse> markets = marketLines.stream()
                .map(this::buildMarketLineResponse)
                .collect(Collectors.toList());

        response.setMarketLines(markets);
        return response;
    }

    /**
     * 建立盤口響應
     */
    private SportEventResponse.MarketLineResponse buildMarketLineResponse(MarketLine ml) {
        return SportEventResponse.MarketLineResponse.builder()
                .marketLineId(ml.getId())
                .betTypeCode(ml.getBetType() != null ? ml.getBetType().getCode() : null)
                .betTypeName(ml.getBetType() != null ? ml.getBetType().getName() : null)
                .oddsFormatCode(ml.getOddsFormat() != null ? ml.getOddsFormat().getCode() : null)
                .oddsFormatName(ml.getOddsFormat() != null ? ml.getOddsFormat().getName() : null)
                .handicap(ml.getHandicap())
                .homeOdds(ml.getHomeOdds())
                .awayOdds(ml.getAwayOdds())
                .drawOdds(ml.getDrawOdds())
                .overOdds(ml.getOverOdds())
                .underOdds(ml.getUnderOdds())
                .yesOdds(ml.getYesOdds())
                .noOdds(ml.getNoOdds())
                .oddOdds(ml.getOddOdds())
                .evenOdds(ml.getEvenOdds())
                .scoreOdds(ml.getScoreOdds())
                .build();
    }
}
