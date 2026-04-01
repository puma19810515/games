package com.games.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Swagger/OpenAPI 配置類
 *
 * 訪問路徑:
 * - Swagger UI: http://localhost:8080/swagger-ui.html
 * - OpenAPI JSON: http://localhost:8080/v3/api-docs
 * - OpenAPI YAML: http://localhost:8080/v3/api-docs.yaml
 */
@Configuration
public class SwaggerConfig {

    @Value("${server.port:8080}")
    private int serverPort;

    @Bean
    public OpenAPI customOpenAPI() {
        // 定義 JWT Bearer Token 安全方案
        final String securitySchemeName = "bearerAuth";

        // 定義 API Key 安全方案 (商戶驗證)
        final String apiKeySchemeName = "apiKey";

        return new OpenAPI()
                // API 基本資訊
                .info(new Info()
                        .title("遊戲平台 API 文檔")
                        .description("""
                                ## 遊戲平台 API 文檔
                                
                                此 API 提供以下功能：
                                
                                ### 🎰 電子遊戲
                                - 老虎機遊戲（水果盤）
                                - RTP 管理
                                
                                ### ⚽ 體育投注
                                - 單注投注
                                - 串關投注
                                - 賽事查詢
                                - 投注記錄
                                - 賽事結算
                                - 取消投注 / 提前兌現
                                
                                ### 👤 用戶管理
                                - 註冊 / 登入
                                - 錢包充值 / 提款
                                
                                ### 🔐 認證方式
                                1. **Bearer Token (JWT)**: 用於用戶身份驗證
                                2. **X-API-KEY**: 用於商戶身份驗證
                                
                                ---
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Games Platform Team")
                                .email("support@games-platform.com")
                                .url("https://games-platform.com"))
                        .license(new License()
                                .name("私有授權")
                                .url("https://games-platform.com/license")))

                // 伺服器配置
                .servers(List.of(
                        new Server()
                                .url("http://localhost:" + serverPort)
                                .description("本地開發環境"),
                        new Server()
                                .url("https://api.games-platform.com")
                                .description("正式環境")))

                // 安全配置
                .components(new Components()
                        // JWT Bearer Token
                        .addSecuritySchemes(securitySchemeName, new SecurityScheme()
                                .name(securitySchemeName)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("請輸入 JWT Token（不需要加 Bearer 前綴）"))
                        // API Key (商戶驗證)
                        .addSecuritySchemes(apiKeySchemeName, new SecurityScheme()
                                .name("X-API-KEY")
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER)
                                .description("商戶 API 金鑰")))

                // 全局安全需求
                .addSecurityItem(new SecurityRequirement()
                        .addList(securitySchemeName)
                        .addList(apiKeySchemeName));
    }
}
