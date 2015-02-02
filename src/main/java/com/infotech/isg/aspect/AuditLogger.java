package com.infotech.isg.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Around;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * audit logger.
 *
 * @author Sevak Gharibian
 */
@Aspect
@Component
public class AuditLogger {

    private static final Logger LOG = LoggerFactory.getLogger(AuditLogger.class);

    // around execution any public method in ISGBalanceService API
    @Around("execution(public * com.infotech.isg.service..*.*(..))")
    public Object auditLog(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.currentTimeMillis();
        Object result = joinPoint.proceed();
        LOG.info("{}.{}({}): {} in {} msec", joinPoint.getSignature().getDeclaringType().getSimpleName(),
                 joinPoint.getSignature().getName(),
                 joinPoint.getArgs(),
                 result,
                 System.currentTimeMillis() - start);
        return result;
    }
}
