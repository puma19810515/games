package com.games.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
public class ApiLogAspect {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final ObjectMapper objectMapper;

    @Autowired
    public ApiLogAspect(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Around("execution(* com.games.controller..*(..))")
    public Object logRequestResponse(ProceedingJoinPoint joinPoint) throws Throwable {

        // 1. 取得 HttpServletRequest / Response
        ServletRequestAttributes attrs =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        HttpServletRequest request = attrs.getRequest();
        HttpServletResponse response = attrs.getResponse();

        // 2. Request 基本資訊
        String uri = request.getRequestURI();
        String method = request.getMethod();
        String clientIp = request.getRemoteAddr();

        // 3. Request Body（方法參數）
        Object[] args = joinPoint.getArgs();

        long start = System.currentTimeMillis();

        Object result = null;
        try {
            result = joinPoint.proceed();
            return result;
        } finally {
            long cost = System.currentTimeMillis() - start;

            // 4. Response 狀態碼
            int status = response.getStatus();

            // 5. 統一 log
            logger.info(
                    "[API] {} {} | IP={} | status={} | cost={}ms | req={} | res={}",
                    method, uri, clientIp, status, cost,
                    toJson(args),
                    toJson(result)
            );
        }
    }

    private String toJson(Object obj) {
        if (obj == null) return "null";
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            return String.valueOf(obj);
        }
    }
}
