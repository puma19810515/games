package com.games.filter;

import com.games.entity.Merchant;
import com.games.repository.MerchantRepository;
import io.micrometer.common.util.StringUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class HeaderLoggingFilter extends OncePerRequestFilter {

    private final MerchantRepository merchantRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();
        String method = request.getMethod();
        if ("OPTIONS".equalsIgnoreCase(method)) {
            filterChain.doFilter(request, response);
            return;
        }
        if (path.startsWith("/actuator") || path.startsWith("/health") || path.startsWith("/static/")
                || path.indexOf("/odds-format/") > 0 || path.indexOf("/sport-type/") > 0
                || path.indexOf("/league/") > 0 || path.indexOf("/sport/event/") > 0
                || path.endsWith(".css")
                || path.endsWith(".js") || path.endsWith(".png") || path.endsWith(".ico")) {
            filterChain.doFilter(request, response);
            return;
        }

        String apiKey = request.getHeader("X-API-KEY");
        if (StringUtils.isBlank(apiKey)) {
            unauthorized(response, "Missing X-API-KEY header");
            return;
        }

        // debug 用（正式環境請不要 log secret）
        log.debug("X-API-KEY present: {}", maskApiKey(apiKey));

        try {
            // Expect repository to provide a lookup by api key. Adjust method name if needed.
            Merchant merchant = findMerchantByApiKey(apiKey);
            if (merchant == null) {
                log.warn("Invalid X-API-KEY for request {} {}", request.getMethod(), request.getRequestURI());
                unauthorized(response, "Invalid API key");
                return;
            }

            // Attach merchant for downstream handlers/controllers
            request.setAttribute("merchant", merchant);

            filterChain.doFilter(request, response);
        } catch (Exception ex) {
            log.error("Error validating API key", ex);
            unauthorized(response, "API key validation error");
        }

        filterChain.doFilter(request, response);
    }

    private Merchant findMerchantByApiKey(String apiKey) {

        try {
            return merchantRepository.findByApiKey(apiKey);
        } catch (Exception e) {
            log.error("Error finding merchant by api key {}", apiKey, e);
            throw e;
        }
    }

    private String maskApiKey(String key) {
        if (key == null) return "";
        int len = key.length();
        if (len <= 4) {
            return "***";
        }
        String prefix = key.substring(0, Math.min(2, len));
        String suffix = key.substring(len - Math.min(2, len));
        return prefix + "****" + suffix;
    }

    private void unauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"error\":\"" + message + "\"}");
    }
}
