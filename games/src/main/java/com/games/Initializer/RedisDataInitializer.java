package com.games.Initializer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.games.config.GameProperties;
import com.games.constant.RedisConstant;
import com.games.entity.GameSetting;
import com.games.repository.GameSettingRepository;

import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisDataInitializer implements CommandLineRunner {

    private final ObjectMapper objectMapper;
    private final GameSettingRepository gameSettingRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${register.initial-balance}")
    private BigDecimal registerInitBal;

    @Override
    public void run(String... args) {

        log.info("init redis");
        List<GameSetting> settings = gameSettingRepository.findAll();
        if (settings.isEmpty()) {
            log.warn("No game settings found in the database. Skipping Redis initialization.");
            return;
        }

        Map<String, String> redisMap = new LinkedHashMap<>();

        for (GameSetting setting : settings) {
            String gameCode = setting.getGameCode();
            try {
                GameProperties properties = new GameProperties();
                properties.setInitialBalance(registerInitBal);
                properties.setMinBet(setting.getMinBet());
                properties.setMaxBet(setting.getMaxBet());
                properties.setTargetRtp(setting.getRtpSet());
                properties.setTwoMatchMultiplier(setting.getTwoMatchMultiplier());

                String gameSetting = setting.getGameSettings();
                if (StringUtils.isNotBlank(gameSetting)) {
                    JsonNode root = objectMapper.readTree(gameSetting);
                    JsonNode symbolsNode = root.get("symbols");
                    JsonNode displayNode = root.get("display");
                    JsonNode weightsNode = root.get("symbolWeights");
                    JsonNode payoutsNode = root.get("payoutMultipliers");
                    JsonNode isImageNode = root.get("isImage");

                    if (symbolsNode != null && symbolsNode.isArray()) {
                        List<String> symbolList = IntStream.range(0, symbolsNode.size())
                                .mapToObj(i -> symbolsNode.get(i).asText())
                                .collect(Collectors.toList());
                        properties.setSymbols(symbolList);

                        LinkedHashMap<String, String> symbolDisplay = new LinkedHashMap<>();
                        LinkedHashMap<String, Double> symbolWeights = new LinkedHashMap<>();
                        LinkedHashMap<String, Double> payoutMultipliers = new LinkedHashMap<>();

                        for (int i = 0; i < symbolList.size(); i++) {
                            String sym = symbolList.get(i);
                            String d = (displayNode != null && i < displayNode.size()) ? displayNode.get(i).asText() : "";
                            double w = (weightsNode != null && i < weightsNode.size()) ? weightsNode.get(i).asDouble(0.0) : 0.0;
                            double p = (payoutsNode != null && i < payoutsNode.size()) ? payoutsNode.get(i).asDouble(0.0) : 0.0;
                            symbolWeights.put(sym, w);
                            symbolDisplay.put(sym, d);
                            payoutMultipliers.put(sym, p);
                        }
                        properties.setSymbolDisplay(symbolDisplay);
                        properties.setSymbolWeights(symbolWeights);
                        properties.setPayoutMultipliers(payoutMultipliers);
                        properties.setImage(isImageNode.isBoolean());
                    }
                }

                String json = objectMapper.writeValueAsString(properties);
                redisMap.put(gameCode, json);
            } catch (JsonProcessingException e) {
                log.error("Failed to parse/serialize game setting for gameCode={}", gameCode, e);
            } catch (Exception e) {
                log.error("Unexpected error while preparing game setting for gameCode={}", gameCode, e);
            }
        }

        if (!redisMap.isEmpty()) {
            try {
                redisTemplate.opsForHash().putAll(RedisConstant.GAME_SETTING_ALL, redisMap);
                log.info("Loaded {} game settings into Redis hash 'game_setting_all'.", redisMap.size());
            } catch (Exception e) {
                log.error("Failed to write game settings to Redis", e);
            }
        } else {
            log.warn("No valid game settings to write to Redis.");
        }
        log.info("init redis end");
    }
}
