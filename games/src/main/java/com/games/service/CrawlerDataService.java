package com.games.service;

import com.games.dto.crawler.*;
import com.games.entity.*;
import com.games.enums.SportEventBettingStatus;
import com.games.enums.SportEventSettleStatus;
import com.games.enums.SportEventStatus;
import com.games.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 爬蟲資料服務
 *
 * 處理外部爬蟲推送的資料，包括：
 * - 聯賽 (leagues)
 * - 隊伍 (teams)
 * - 賽事 (sport_events)
 * - 盤口 (market_lines)
 *
 * 特點：
 * - 支援 Upsert（新增或更新）
 * - 使用外部ID作為唯一識別
 * - 批量處理提升效能
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CrawlerDataService {

    private final LeagueRepository leagueRepository;
    private final TeamRepository teamRepository;
    private final SportEventRepository sportEventRepository;
    private final MarketLineRepository marketLineRepository;
    private final SportTypeRepository sportTypeRepository;
    private final CountryRepository countryRepository;
    private final BetTypeRepository betTypeRepository;
    private final OddsFormatRepository oddsFormatRepository;

    // ==================== 聯賽處理 ====================

    /**
     * 批量處理聯賽資料
     */
    @Transactional
    public CrawlerResponse processLeagues(List<CrawlerLeagueRequest> requests, String batchId) {
        long startTime = System.currentTimeMillis();
        int inserted = 0;
        int updated = 0;
        List<CrawlerResponse.FailedItem> failedItems = new ArrayList<>();

        for (CrawlerLeagueRequest request : requests) {
            try {
                boolean isNew = upsertLeague(request);
                if (isNew) {
                    inserted++;
                } else {
                    updated++;
                }
            } catch (Exception e) {
                log.error("處理聯賽失敗: externalId={}, error={}", request.getExternalLeagueId(), e.getMessage());
                failedItems.add(CrawlerResponse.FailedItem.builder()
                        .externalId(request.getExternalLeagueId())
                        .errorMessage(e.getMessage())
                        .build());
            }
        }

        long processingTime = System.currentTimeMillis() - startTime;
        log.info("聯賽處理完成: batchId={}, inserted={}, updated={}, failed={}, time={}ms",
                batchId, inserted, updated, failedItems.size(), processingTime);

        return CrawlerResponse.builder()
                .successCount(inserted + updated)
                .insertedCount(inserted)
                .updatedCount(updated)
                .failedCount(failedItems.size())
                .totalCount(requests.size())
                .failedItems(failedItems)
                .processingTimeMs(processingTime)
                .batchId(batchId)
                .processedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Upsert 聯賽
     * @return true 如果是新增，false 如果是更新
     */
    private boolean upsertLeague(CrawlerLeagueRequest request) {
        SportType sportType = sportTypeRepository.findByCode(request.getSportTypeCode())
                .orElseThrow(() -> new IllegalArgumentException("球種不存在: " + request.getSportTypeCode()));

        Optional<League> existingLeague = leagueRepository.findByExternalLeagueId(request.getExternalLeagueId());

        if (existingLeague.isPresent()) {
            // 更新
            League league = existingLeague.get();
            league.setName(request.getName());
            league.setSportType(sportType);
            if (request.getCountryCode() != null) {
                countryRepository.findById(request.getCountryCode())
                        .ifPresent(league::setCountry);
            }
            if (request.getDisplayOrder() != null) {
                league.setDisplayOrder(request.getDisplayOrder());
            }
            if (request.getStatus() != null) {
                league.setStatus(request.getStatus());
            }
            leagueRepository.save(league);
            return false;
        } else {
            // 新增
            League league = League.builder()
                    .externalLeagueId(request.getExternalLeagueId())
                    .sportType(sportType)
                    .name(request.getName())
                    .displayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0)
                    .status(request.getStatus() != null ? request.getStatus() : 1)
                    .build();

            if (request.getCountryCode() != null) {
                countryRepository.findById(request.getCountryCode())
                        .ifPresent(league::setCountry);
            }

            leagueRepository.save(league);
            return true;
        }
    }

    // ==================== 隊伍處理 ====================

    /**
     * 批量處理隊伍資料
     */
    @Transactional
    public CrawlerResponse processTeams(List<CrawlerTeamRequest> requests, String batchId) {
        long startTime = System.currentTimeMillis();
        int inserted = 0;
        int updated = 0;
        List<CrawlerResponse.FailedItem> failedItems = new ArrayList<>();

        for (CrawlerTeamRequest request : requests) {
            try {
                boolean isNew = upsertTeam(request);
                if (isNew) {
                    inserted++;
                } else {
                    updated++;
                }
            } catch (Exception e) {
                log.error("處理隊伍失敗: externalId={}, error={}", request.getExternalTeamId(), e.getMessage());
                failedItems.add(CrawlerResponse.FailedItem.builder()
                        .externalId(request.getExternalTeamId())
                        .errorMessage(e.getMessage())
                        .build());
            }
        }

        long processingTime = System.currentTimeMillis() - startTime;
        log.info("隊伍處理完成: batchId={}, inserted={}, updated={}, failed={}, time={}ms",
                batchId, inserted, updated, failedItems.size(), processingTime);

        return CrawlerResponse.builder()
                .successCount(inserted + updated)
                .insertedCount(inserted)
                .updatedCount(updated)
                .failedCount(failedItems.size())
                .totalCount(requests.size())
                .failedItems(failedItems)
                .processingTimeMs(processingTime)
                .batchId(batchId)
                .processedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Upsert 隊伍
     */
    private boolean upsertTeam(CrawlerTeamRequest request) {
        SportType sportType = sportTypeRepository.findByCode(request.getSportTypeCode())
                .orElseThrow(() -> new IllegalArgumentException("球種不存在: " + request.getSportTypeCode()));

        Optional<Team> existingTeam = teamRepository.findByExternalTeamId(request.getExternalTeamId());

        if (existingTeam.isPresent()) {
            // 更新
            Team team = existingTeam.get();
            team.setName(request.getName());
            team.setSportType(sportType);
            if (request.getShortName() != null) {
                team.setShortName(request.getShortName());
            }
            if (request.getLogoUrl() != null) {
                team.setLogoUrl(request.getLogoUrl());
            }
            teamRepository.save(team);
            return false;
        } else {
            // 新增
            Team team = Team.builder()
                    .externalTeamId(request.getExternalTeamId())
                    .sportType(sportType)
                    .name(request.getName())
                    .shortName(request.getShortName())
                    .logoUrl(request.getLogoUrl())
                    .build();
            teamRepository.save(team);
            return true;
        }
    }

    // ==================== 賽事處理 ====================

    /**
     * 批量處理賽事資料
     */
    @Transactional
    public CrawlerResponse processEvents(List<CrawlerEventRequest> requests, String batchId) {
        long startTime = System.currentTimeMillis();
        int inserted = 0;
        int updated = 0;
        List<CrawlerResponse.FailedItem> failedItems = new ArrayList<>();

        for (CrawlerEventRequest request : requests) {
            try {
                boolean isNew = upsertEvent(request);
                if (isNew) {
                    inserted++;
                } else {
                    updated++;
                }
            } catch (Exception e) {
                log.error("處理賽事失敗: externalId={}, error={}", request.getExternalEventId(), e.getMessage());
                failedItems.add(CrawlerResponse.FailedItem.builder()
                        .externalId(request.getExternalEventId())
                        .errorMessage(e.getMessage())
                        .build());
            }
        }

        long processingTime = System.currentTimeMillis() - startTime;
        log.info("賽事處理完成: batchId={}, inserted={}, updated={}, failed={}, time={}ms",
                batchId, inserted, updated, failedItems.size(), processingTime);

        return CrawlerResponse.builder()
                .successCount(inserted + updated)
                .insertedCount(inserted)
                .updatedCount(updated)
                .failedCount(failedItems.size())
                .totalCount(requests.size())
                .failedItems(failedItems)
                .processingTimeMs(processingTime)
                .batchId(batchId)
                .processedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Upsert 賽事
     */
    private boolean upsertEvent(CrawlerEventRequest request) {
        SportType sportType = sportTypeRepository.findByCode(request.getSportTypeCode())
                .orElseThrow(() -> new IllegalArgumentException("球種不存在: " + request.getSportTypeCode()));

        Team homeTeam = teamRepository.findByExternalTeamId(request.getExternalHomeTeamId())
                .orElseThrow(() -> new IllegalArgumentException("主隊不存在: " + request.getExternalHomeTeamId()));

        Team awayTeam = teamRepository.findByExternalTeamId(request.getExternalAwayTeamId())
                .orElseThrow(() -> new IllegalArgumentException("客隊不存在: " + request.getExternalAwayTeamId()));

        League league = null;
        if (request.getExternalLeagueId() != null) {
            league = leagueRepository.findByExternalLeagueId(request.getExternalLeagueId()).orElse(null);
        }

        Optional<SportEvent> existingEvent = sportEventRepository.findByExternalEventId(request.getExternalEventId());

        if (existingEvent.isPresent()) {
            // 更新
            SportEvent event = existingEvent.get();
            event.setSportType(sportType);
            event.setLeague(league);
            event.setHomeTeam(homeTeam);
            event.setAwayTeam(awayTeam);
            event.setHomeTeamName(request.getHomeTeamName());
            event.setAwayTeamName(request.getAwayTeamName());
            event.setStartTime(request.getStartTime());

            if (request.getHomeScore() != null) {
                event.setHomeScore(request.getHomeScore());
            }
            if (request.getAwayScore() != null) {
                event.setAwayScore(request.getAwayScore());
            }
            if (request.getHomeScoreHalf() != null) {
                event.setHomeScoreHalf(request.getHomeScoreHalf());
            }
            if (request.getAwayScoreHalf() != null) {
                event.setAwayScoreHalf(request.getAwayScoreHalf());
            }
            if (request.getStatus() != null) {
                event.setSportEventStatus(SportEventStatus.valueOf(request.getStatus()));
            }
            if (request.getBettingStatus() != null) {
                event.setSportEventBettingStatus(SportEventBettingStatus.valueOf(request.getBettingStatus()));
            }

            sportEventRepository.save(event);
            return false;
        } else {
            // 新增
            SportEvent event = SportEvent.builder()
                    .externalEventId(request.getExternalEventId())
                    .sportType(sportType)
                    .league(league)
                    .homeTeam(homeTeam)
                    .awayTeam(awayTeam)
                    .homeTeamName(request.getHomeTeamName())
                    .awayTeamName(request.getAwayTeamName())
                    .startTime(request.getStartTime())
                    .homeScore(request.getHomeScore())
                    .awayScore(request.getAwayScore())
                    .homeScoreHalf(request.getHomeScoreHalf())
                    .awayScoreHalf(request.getAwayScoreHalf())
                    .sportEventStatus(request.getStatus() != null ?
                            SportEventStatus.valueOf(request.getStatus()) : SportEventStatus.UPCOMING)
                    .sportEventBettingStatus(request.getBettingStatus() != null ?
                            SportEventBettingStatus.valueOf(request.getBettingStatus()) : SportEventBettingStatus.OPEN)
                    .sportEventSettleStatus(SportEventSettleStatus.UNSETTLED)
                    .build();
            sportEventRepository.save(event);
            return true;
        }
    }

    // ==================== 盤口處理 ====================

    /**
     * 批量處理盤口資料
     */
    @Transactional
    public CrawlerResponse processMarketLines(List<CrawlerMarketLineRequest> requests, String batchId) {
        long startTime = System.currentTimeMillis();
        int inserted = 0;
        int updated = 0;
        List<CrawlerResponse.FailedItem> failedItems = new ArrayList<>();

        for (CrawlerMarketLineRequest request : requests) {
            try {
                boolean isNew = upsertMarketLine(request);
                if (isNew) {
                    inserted++;
                } else {
                    updated++;
                }
            } catch (Exception e) {
                log.error("處理盤口失敗: externalId={}, error={}", request.getExternalMarketId(), e.getMessage());
                failedItems.add(CrawlerResponse.FailedItem.builder()
                        .externalId(request.getExternalMarketId())
                        .errorMessage(e.getMessage())
                        .build());
            }
        }

        long processingTime = System.currentTimeMillis() - startTime;
        log.info("盤口處理完成: batchId={}, inserted={}, updated={}, failed={}, time={}ms",
                batchId, inserted, updated, failedItems.size(), processingTime);

        return CrawlerResponse.builder()
                .successCount(inserted + updated)
                .insertedCount(inserted)
                .updatedCount(updated)
                .failedCount(failedItems.size())
                .totalCount(requests.size())
                .failedItems(failedItems)
                .processingTimeMs(processingTime)
                .batchId(batchId)
                .processedAt(LocalDateTime.now())
                .build();
    }

    /**
     * Upsert 盤口
     */
    private boolean upsertMarketLine(CrawlerMarketLineRequest request) {
        SportEvent event = sportEventRepository.findByExternalEventId(request.getExternalEventId())
                .orElseThrow(() -> new IllegalArgumentException("賽事不存在: " + request.getExternalEventId()));

        BetType betType = betTypeRepository.findByCode(request.getBetTypeCode())
                .orElseThrow(() -> new IllegalArgumentException("玩法不存在: " + request.getBetTypeCode()));

        OddsFormat oddsFormat = oddsFormatRepository.findByCode(request.getOddsFormatCode())
                .orElseThrow(() -> new IllegalArgumentException("賠率格式不存在: " + request.getOddsFormatCode()));

        Optional<MarketLine> existingLine = marketLineRepository.findByExternalMarketId(request.getExternalMarketId());

        if (existingLine.isPresent()) {
            // 更新
            MarketLine line = existingLine.get();
            updateMarketLine(line, request, event, betType, oddsFormat);
            marketLineRepository.save(line);
            return false;
        } else {
            // 新增
            MarketLine line = createMarketLine(request, event, betType, oddsFormat);
            marketLineRepository.save(line);
            return true;
        }
    }

    private void updateMarketLine(MarketLine line, CrawlerMarketLineRequest request,
                                   SportEvent event, BetType betType, OddsFormat oddsFormat) {
        line.setEvent(event);
        line.setBetType(betType);
        line.setOddsFormat(oddsFormat);
        line.setHandicap(request.getHandicap());
        line.setHomeOdds(request.getHomeOdds());
        line.setAwayOdds(request.getAwayOdds());
        line.setDrawOdds(request.getDrawOdds());
        line.setOverOdds(request.getOverOdds());
        line.setUnderOdds(request.getUnderOdds());
        line.setYesOdds(request.getYesOdds());
        line.setNoOdds(request.getNoOdds());
        line.setOddOdds(request.getOddOdds());
        line.setEvenOdds(request.getEvenOdds());
        line.setScoreOdds(request.getScoreOdds());
        if (request.getIsActive() != null) {
            line.setIsActive(request.getIsActive());
        }
    }

    private MarketLine createMarketLine(CrawlerMarketLineRequest request,
                                         SportEvent event, BetType betType, OddsFormat oddsFormat) {
        return MarketLine.builder()
                .externalMarketId(request.getExternalMarketId())
                .event(event)
                .betType(betType)
                .oddsFormat(oddsFormat)
                .handicap(request.getHandicap())
                .homeOdds(request.getHomeOdds())
                .awayOdds(request.getAwayOdds())
                .drawOdds(request.getDrawOdds())
                .overOdds(request.getOverOdds())
                .underOdds(request.getUnderOdds())
                .yesOdds(request.getYesOdds())
                .noOdds(request.getNoOdds())
                .oddOdds(request.getOddOdds())
                .evenOdds(request.getEvenOdds())
                .scoreOdds(request.getScoreOdds())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .build();
    }
}
