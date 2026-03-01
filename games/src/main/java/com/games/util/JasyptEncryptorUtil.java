package com.games.util;

import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.jasypt.encryption.pbe.config.SimpleStringPBEConfig;

/**
 * Jasypt 密碼加密工具類
 *
 * 使用方式：
 * 1. 設定 JASYPT_ENCRYPTOR_PASSWORD 環境變數或直接在代碼中指定密鑰
 * 2. 執行 main 方法生成加密字串
 * 3. 將加密後的字串放入 application.yml，格式為 ENC(加密字串)
 */
public class JasyptEncryptorUtil {

    /**
     * 加密密鑰（生產環境請使用環境變數）
     * 可透過環境變數 JASYPT_ENCRYPTOR_PASSWORD 設定
     */
    private static final String ENCRYPTOR_PASSWORD = "GamesSecretKey2026!@#";

    public static void main(String[] args) {
        // 要加密的敏感資料
        String dbPassword = "npg_rvi4syEgn7qU";
        String redisPassword = "Ad1RAAIncDIxN2UyMDIzYTgyNTU0ZDRhODhiZmI4YWE1ZDk1YTg4MnAyNTY2NTc";
        String jwtSecret = "GamesSuperSecretKeyForJWTTokenGenerationPumaMustBeAtLeast256BitsLong123456789";

        System.out.println("========== Jasypt 加密工具 ==========");
        System.out.println("加密密鑰: " + ENCRYPTOR_PASSWORD);
        System.out.println();

        System.out.println("資料庫密碼:");
        System.out.println("  原始: " + dbPassword);
        System.out.println("  加密: ENC(" + encrypt(dbPassword) + ")");
        System.out.println();

        System.out.println("Redis 密碼:");
        System.out.println("  原始: " + redisPassword);
        System.out.println("  加密: ENC(" + encrypt(redisPassword) + ")");
        System.out.println();

        System.out.println("JWT Secret:");
        System.out.println("  原始: " + jwtSecret);
        System.out.println("  加密: ENC(" + encrypt(jwtSecret) + ")");
        System.out.println();

        System.out.println("========================================");
        System.out.println("請將加密後的字串複製到 application.yml");
        System.out.println("啟動時需設定環境變數: JASYPT_ENCRYPTOR_PASSWORD=" + ENCRYPTOR_PASSWORD);
    }

    /**
     * 加密字串
     */
    public static String encrypt(String plainText) {
        StandardPBEStringEncryptor encryptor = createEncryptor();
        return encryptor.encrypt(plainText);
    }

    /**
     * 解密字串
     */
    public static String decrypt(String encryptedText) {
        StandardPBEStringEncryptor encryptor = createEncryptor();
        return encryptor.decrypt(encryptedText);
    }

    /**
     * 建立加密器
     */
    private static StandardPBEStringEncryptor createEncryptor() {
        StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();
        SimpleStringPBEConfig config = new SimpleStringPBEConfig();

        config.setPassword(ENCRYPTOR_PASSWORD);
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
