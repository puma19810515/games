package com.games.aspect;

import com.games.annotation.ReadOnly;
import com.games.config.datasource.DataSourceContextHolder;
import com.games.config.datasource.DataSourceType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.Order;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Method;

/**
 * 数据源切面
 * 根据方法注解自动切换数据源
 *
 * 作用范围：
 * - Repository 层的所有方法
 * - Service 层的所有方法（排除 TokenService，因为它只使用 Redis，不涉及数据库操作）
 *
 * 路由规则：
 * 1. 如果方法标记了 @Lock（悲观锁），使用主库
 * 2. 如果方法标记了 @Transactional 且 readOnly=false（写事务），使用主库
 * 3. 如果方法标记了 @ReadOnly，使用从库
 * 4. 如果方法标记了 @Transactional(readOnly=true)，使用从库
 * 5. 如果方法名包含 Lock，使用主库
 * 6. 方法名启发式判断（find*, get*, query* 等使用从库）
 * 7. 默认使用主库
 *
 * 优先级：悲观锁 > 写事务 > 只读注解 > 方法名启发式 > 默认主库
 */
@Aspect
@Component
@Order(1) // 确保在事务切面之前执行
@Slf4j
public class DataSourceAspect {

    /**
     * 切入点：所有 Repository 层的方法
     */
    @Pointcut("execution(* com.games.repository..*.*(..))")
    public void repositoryPointcut() {
    }

    /**
     * 切入点：所有 Service 层的方法（排除只使用 Redis 的 TokenService）
     * TokenService 只使用 Redis，不需要数据源路由，应该被排除
     */
    @Pointcut("execution(* com.games.service..*.*(..)) && !execution(* com.games.service.TokenService.*(..))")
    public void servicePointcut() {
    }

    /**
     * 环绕通知：在方法执行前设置数据源，执行后清除
     */
    @Around("repositoryPointcut() || servicePointcut()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        Class<?> declaringClass = method.getDeclaringClass();

        try {
            // 确定数据源类型
            DataSourceType dataSourceType = determineDataSource(method, declaringClass);
            DataSourceContextHolder.setDataSourceType(dataSourceType);

            log.debug("Method: {}.{} -> Using {} datasource",
                    declaringClass.getSimpleName(),
                    method.getName(),
                    dataSourceType);

            // 执行目标方法
            return joinPoint.proceed();

        } finally {
            // 方法执行完毕后清除数据源设置
            DataSourceContextHolder.clearDataSourceType();
        }
    }

    /**
     * 确定使用哪个数据源
     *
     * @param method         方法
     * @param declaringClass 声明类
     * @return 数据源类型
     */
    private DataSourceType determineDataSource(Method method, Class<?> declaringClass) {
        // 1. 检查方法是否有 @Lock 注解（悲观锁必须使用主库）
        Lock lockAnnotation = method.getAnnotation(Lock.class);
        if (lockAnnotation != null) {
            log.debug("Found @Lock annotation on method, using MASTER for pessimistic lock");
            return DataSourceType.MASTER;
        }

        // 2. 检查方法名是否包含 Lock（悲观锁相关方法必须使用主库）
        String methodName = method.getName();
        if (methodName.contains("Lock") || methodName.contains("WithLock")) {
            log.debug("Method name contains 'Lock', using MASTER");
            return DataSourceType.MASTER;
        }

        // 3. 检查方法级别的 @Transactional 注解
        Transactional methodTransactional = method.getAnnotation(Transactional.class);
        if (methodTransactional != null) {
            // 如果是写事务（readOnly=false），使用主库
            if (!methodTransactional.readOnly()) {
                log.debug("Found @Transactional(readOnly=false) on method, using MASTER");
                return DataSourceType.MASTER;
            }
            // 如果是只读事务（readOnly=true），使用从库
            log.debug("Found @Transactional(readOnly=true) on method, using SLAVE");
            return DataSourceType.SLAVE;
        }

        // 4. 检查类级别的 @Transactional 注解
        Transactional classTransactional = declaringClass.getAnnotation(Transactional.class);
        if (classTransactional != null) {
            if (!classTransactional.readOnly()) {
                log.debug("Found @Transactional(readOnly=false) on class, using MASTER");
                return DataSourceType.MASTER;
            }
            log.debug("Found @Transactional(readOnly=true) on class, using SLAVE");
            return DataSourceType.SLAVE;
        }

        // 5. 检查方法级别的 @ReadOnly 注解
        ReadOnly methodReadOnly = method.getAnnotation(ReadOnly.class);
        if (methodReadOnly != null) {
            log.debug("Found @ReadOnly on method, using SLAVE");
            return DataSourceType.SLAVE;
        }

        // 6. 检查类级别的 @ReadOnly 注解
        ReadOnly classReadOnly = declaringClass.getAnnotation(ReadOnly.class);
        if (classReadOnly != null) {
            log.debug("Found @ReadOnly on class, using SLAVE");
            return DataSourceType.SLAVE;
        }

        // 7. 方法名启发式判断
        if (methodName.startsWith("find") ||
                methodName.startsWith("get") ||
                methodName.startsWith("query") ||
                methodName.startsWith("select") ||
                methodName.startsWith("count") ||
                methodName.startsWith("exists")) {
            log.debug("Method name suggests read operation, using SLAVE");
            return DataSourceType.SLAVE;
        }

        // 8. 默认使用主库
        log.debug("No specific annotation found, using default MASTER");
        return DataSourceType.MASTER;
    }
}
