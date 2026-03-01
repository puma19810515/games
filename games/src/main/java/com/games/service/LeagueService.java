package com.games.service;

import com.games.dto.BetRecordsResponse;
import com.games.dto.LeagueResponse;
import com.games.entity.Bet;
import com.games.entity.League;
import com.games.repository.LeagueRepository;
import com.games.util.PageDataResUtil;
import com.games.util.PageReqUtil;
import com.games.util.PageUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class LeagueService {

    private final LeagueRepository leagueRepository;

    public PageDataResUtil<LeagueResponse> getListByPage(PageReqUtil request) {

        Pageable pageable = PageUtils.
                genericPage(request.getPageNum(), request.getPageSize(), "id");

        Page<League> ledguePage = leagueRepository.findAll(pageable);

        List<LeagueResponse> leagueLt = ledguePage.getContent().stream().map(league -> {
            LeagueResponse resp = new LeagueResponse();
            resp.setId(league.getId());
            resp.setCountryName(league.getCountry().getNameZh());
            resp.setStatus(league.getStatus());
            resp.setExternalLeagueId(league.getExternalLeagueId());
            resp.setName(league.getName());
            resp.setDisplayOrder(league.getDisplayOrder());
            resp.setSportTypeName(league.getSportType().getName());
            resp.setSportTypeCode(league.getSportType().getCode());
            resp.setUpdatedAt(league.getUpdatedAt());
            resp.setCreatedAt(league.getCreatedAt());
            return resp;
        }).collect(Collectors.toList());

        PageDataResUtil<LeagueResponse> pageData = new PageDataResUtil<>();
        // adapt setter names to your PageDataResUtil implementation if they differ
        pageData.setList(leagueLt);
        pageData.setTotal(ledguePage.getTotalElements());
        pageData.setPage(request.getPageNum());
        pageData.setSize(request.getPageSize());
        pageData.setTotalPages(ledguePage.getTotalPages());
        return pageData;
    }
}
