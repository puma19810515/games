# 敏感資訊加密配置指南

## 概述

本專案使用 Jasypt (Java Simplified Encryption) 來加密 `application.yml` 中的敏感資訊，包括：
- 資料庫密碼
- Redis 密碼
- JWT Secret

## 加密步驟

### 1. 生成加密字串

執行 `JasyptEncryptorUtil` 工具類來生成加密字串：

```bash
# 使用 IDE 執行 JasyptEncryptorUtil.main() 方法
# 或使用命令行：
cd /path/to/project
mvn compile exec:java -Dexec.mainClass="com.games.util.JasyptEncryptorUtil"
```

輸出範例：
```
========== Jasypt 加密工具 ==========
加密密鑰: GamesSecretKey2026!@#

資料庫密碼:
  原始: your_db_password
  加密: ENC(xxxxx)

Redis 密碼:
  原始: your_redis_password
  加密: ENC(yyyyy)

JWT Secret:
  原始: your_jwt_secret
  加密: ENC(zzzzz)
========================================
```

### 2. 更新 application.yml

將加密後的字串放入配置文件：

```yaml
spring:
  datasource:
    master:
      password: ENC(xxxxx)  # 加密後的資料庫密碼
    slave:
      password: ENC(xxxxx)
  data:
    redis:
      password: ENC(yyyyy)  # 加密後的 Redis 密碼

jwt:
  secret: ENC(zzzzz)  # 加密後的 JWT Secret
```

### 3. 設定加密密鑰

啟動應用程式時，必須提供加密密鑰。有以下幾種方式：

#### 方式 A: 環境變數（推薦用於生產環境）
```bash
export JASYPT_ENCRYPTOR_PASSWORD=GamesSecretKey2026!@#
java -jar games.jar
```

#### 方式 B: JVM 參數
```bash
java -Djasypt.encryptor.password=GamesSecretKey2026!@# -jar games.jar
```

#### 方式 C: IntelliJ IDEA 設定
1. 打開 Run/Debug Configurations
2. 在 Environment variables 中添加：
   ```
   JASYPT_ENCRYPTOR_PASSWORD=GamesSecretKey2026!@#
   ```

## 加密配置說明

| 設定項 | 值 | 說明 |
|-------|-----|------|
| Algorithm | PBEWITHHMACSHA512ANDAES_256 | 加密算法 |
| Key Obtention Iterations | 1000 | 密鑰生成迭代次數 |
| Salt Generator | RandomSaltGenerator | 隨機鹽值生成器 |
| IV Generator | RandomIvGenerator | 隨機初始化向量 |
| String Output Type | base64 | 輸出格式 |

## 安全建議

1. **永遠不要將加密密鑰提交到版本控制**
   - 使用環境變數或安全的密鑰管理服務

2. **使用不同的密鑰**
   - 開發、測試、生產環境應使用不同的加密密鑰

3. **定期更換密鑰**
   - 建議每季度更換一次加密密鑰

4. **密鑰強度要求**
   - 至少 16 個字元
   - 包含大小寫字母、數字和特殊字元

## 故障排除

### 錯誤：Jasypt encryptor password not found!
確保設定了 `JASYPT_ENCRYPTOR_PASSWORD` 環境變數或 JVM 參數。

### 錯誤：EncryptionOperationNotPossibleException
- 檢查加密字串格式是否正確（必須是 `ENC(xxx)` 格式）
- 確認使用的密鑰與加密時的密鑰一致

### 錯誤：BadPaddingException
- 密鑰錯誤或加密字串損壞
- 重新生成加密字串

## 配合環境變數的混合模式

你也可以同時使用環境變數和加密：

```yaml
spring:
  datasource:
    password: ${DB_PASSWORD:ENC(default_encrypted_value)}
```

這樣：
- 如果設定了 `DB_PASSWORD` 環境變數，會使用環境變數的值
- 如果沒有設定，會使用加密的預設值（需要 Jasypt 解密）

## 相關文件

- [Jasypt 官方文檔](http://www.jasypt.org/)
- [jasypt-spring-boot GitHub](https://github.com/ulisesbocchio/jasypt-spring-boot)
