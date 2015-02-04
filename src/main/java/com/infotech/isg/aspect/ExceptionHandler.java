package com.infotech.isg.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Around;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * generic exception handler for uncaught exceptions.
 *
 * @author Sevak Gharibian
 */
@Aspect
@Component
public class ExceptionHandler {

    private static final Logger LOG = LoggerFactory.getLogger(ExceptionHandler.class);

    // around execution any public method in service API
    @Around("execution(public * com.infotech.isg.service..*.*(..))")
    public Object translateException(ProceedingJoinPoint joinPoint) throws Throwable {
        Object result = null;
        try {
            result = joinPoint.proceed();
        } catch (RuntimeException e) {
            LOG.error("exception/error handler, exception logged", e);
        }
        return result;
    }
}
