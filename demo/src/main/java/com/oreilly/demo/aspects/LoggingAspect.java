package com.oreilly.demo.aspects;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
@Aspect
public class LoggingAspect {
    private final Logger logger = LoggerFactory.getLogger(LoggingAspect.class);

    // Only available pointcuts are public methods on Spring-managed beans
    @Before("execution(* com.oreilly.demo.*.*.*(..))")
    public void logMethodCalls(JoinPoint joinPoint) {
        logger.info("Entering  " + joinPoint.getSignature());
        logger.info("with args " + Arrays.toString(joinPoint.getArgs()));
    }
}
