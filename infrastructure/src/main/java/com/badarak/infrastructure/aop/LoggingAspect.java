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
    private final Logger log = LoggerFactory.getLogger(LoggingAspect.class);

    @Around("execution(* com.badarak.domain.service.*.*(..)) || execution(* com.badarak.infrastructure.rest..*(..))")
    public Object logAround(ProceedingJoinPoint pjp) throws Throwable {
        String method = pjp.getSignature().toShortString();
        log.info("Entering");

        Object result = pjp.proceed();
        log.info("Exiting {} -> {}", method, result == null ? "void" : result.getClass().getSimpleName());
        return result;
    }
}
