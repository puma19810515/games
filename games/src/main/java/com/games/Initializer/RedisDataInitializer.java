package com.games.Initializer;

import com.games.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisDataInitializer implements CommandLineRunner {

    private final GameSettingService gameSettingService;
    private final OddsFormatService oddsFormatService;
    private final SportTypeService sportTypeService;
    private final CountryService countryService;
    private final TeamService teamService;

    @Override
    public void run(String... args) {
        log.info("init redis start");
        gameSettingService.refreshGameSettings();
        oddsFormatService.refreshOddsFormats();
        sportTypeService.refreshSportTypes();
        countryService.refreshCountries();
        teamService.refreshTeams();
        log.info("init redis end");
    }
}
