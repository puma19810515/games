package com.games.config;

import org.jasypt.encryption.StringEncryptor;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.jasypt.encryption.pbe.config.SimpleStringPBEConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Jasypt 加密配置
 *
 * 用於加密 application.yml 中的敏感資訊（如資料庫密碼、API 金鑰等）
 *
 * 使用方式：
 * 1. 在 application.yml 中使用 ENC(加密字串) 格式
 * 2. 啟動時設定環境變數 JASYPT_ENCRYPTOR_PASSWORD
 *
 * 範例：
 *   password: ENC(xxxxx)
 *
 * 啟動命令：
 *   java -jar app.jar -Djasypt.encryptor.password=密鑰
 *   或
 *   export JASYPT_ENCRYPTOR_PASSWORD=密鑰
 */
@Configuration
public class JasyptConfig {

    /**
     * 自定義加密器
     * Bean 名稱必須為 "jasyptStringEncryptor"
     */
    @Bean("jasyptStringEncryptor")
    public StringEncryptor stringEncryptor() {
        StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
        SimpleStringPBEConfig config = new SimpleStringPBEConfig();

        // 從環境變數讀取加密密鑰
        String password = System.getenv("JASYPT_ENCRYPTOR_PASSWORD");
        if (password == null || password.isEmpty()) {
            password = System.getProperty("jasypt.encryptor.password");
        }
        if (password == null || password.isEmpty()) {
            throw new IllegalStateException(
                "Jasypt encryptor password not found! " +
                "Please set JASYPT_ENCRYPTOR_PASSWORD environment variable " +
                "or -Djasypt.encryptor.password system property"
            );
        }

        config.setPassword(password);
        config.setAlgorithm("PBEWITHHMACSHA512ANDAES_256");
        config.setKeyObtentionIterations("1000");
        config.setPoolSize("1");
        config.setProviderName("SunJCE");
        config.setSaltGeneratorClassName("org.jasypt.salt.RandomSaltGenerator");
        config.setIvGeneratorClassName("org.jasypt.iv.RandomIvGenerator");
        config.setStringOutputType("base64");

        encryptor.setConfig(config);
        return encryptor;
    }
}
