package com.badarak.infrastructure.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggingAspect {
    Logger logger = LoggerFactory.getLogger(LoggingAspect.class);

    @Around("execution(* com.badarak.domain.service..*(..)) || execution(* com.badarak.infrastructure.rest..*(..))")
    public Object logAround(ProceedingJoinPoint pjp) throws Throwable {
        String method = pjp.getSignature().toShortString();
        logger.info("Entering {}", method);

        try {
            Object result = pjp.proceed();
            logger.info("Exiting {} -> {}", method, result == null ? "void" : result.getClass().getSimpleName());
            return result;
        } catch (Throwable e) {
            logger.error("Exception in {} {}", method, e.getMessage(), e);
            throw e;
        }
    }
}
