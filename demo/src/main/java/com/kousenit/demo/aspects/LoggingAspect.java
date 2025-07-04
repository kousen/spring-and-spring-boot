package com.kousenit.demo.aspects;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * Aspect for cross-cutting concerns like logging and performance monitoring.
 * Demonstrates various AOP advice types and pointcut expressions.
 */
@Component
@Aspect
public class LoggingAspect {
    private final Logger logger = LoggerFactory.getLogger(LoggingAspect.class);

    /**
     * Log method entry for all controller methods.
     * Uses @Before advice to log before method execution.
     */
    @Before("execution(* com.kousenit.demo.controllers.*.*(..))")
    public void logMethodCalls(JoinPoint joinPoint) {
        logger.info("Entering method: {}", joinPoint.getSignature());
        logger.info("with args: {}", Arrays.toString(joinPoint.getArgs()));
    }

    /**
     * Measure execution time for all controller methods.
     * Uses @Around advice to wrap method execution with timing logic.
     */
    @Around("execution(* com.kousenit.demo.controllers.*.*(..))")
    public Object measureExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.nanoTime();
        
        try {
            // Proceed with the original method call
            Object result = joinPoint.proceed();
            
            long endTime = System.nanoTime();
            logger.info("Method {} executed in {} ms", 
                       joinPoint.getSignature().getName(), 
                       (endTime - startTime) / 1_000_000);
            
            return result;
        } catch (Exception e) {
            long endTime = System.nanoTime();
            logger.error("Method {} failed after {} ms with exception: {}", 
                        joinPoint.getSignature().getName(), 
                        (endTime - startTime) / 1_000_000, 
                        e.getMessage());
            throw e;
        }
    }

    /**
     * Time methods annotated with @Timed.
     * Demonstrates custom annotation-based pointcuts.
     */
    @Around("@annotation(timed)")
    public Object timeAnnotatedMethods(ProceedingJoinPoint joinPoint, Timed timed) throws Throwable {
        long startTime = System.nanoTime();
        
        try {
            Object result = joinPoint.proceed();
            long duration = (System.nanoTime() - startTime) / 1_000_000;
            
            String description = timed.description().isEmpty() ? 
                joinPoint.getSignature().getName() : timed.description();
            logger.info("@Timed method '{}' executed in {} ms", description, duration);
            
            return result;
        } catch (Exception e) {
            long duration = (System.nanoTime() - startTime) / 1_000_000;
            logger.error("@Timed method '{}' failed after {} ms", 
                        joinPoint.getSignature().getName(), duration);
            throw e;
        }
    }

    /**
     * Log method return values.
     * Uses @AfterReturning advice to log successful method completions.
     */
    @AfterReturning(pointcut = "execution(* com.kousenit.demo.controllers.*.*(..))", 
                    returning = "result")
    public void logMethodReturn(JoinPoint joinPoint, Object result) {
        logger.info("Method {} returned: {}", 
                   joinPoint.getSignature().getName(), result);
    }

    /**
     * Log exceptions thrown by controller methods.
     * Uses @AfterThrowing advice to log when methods throw exceptions.
     */
    @AfterThrowing(pointcut = "execution(* com.kousenit.demo.controllers.*.*(..))", 
                   throwing = "exception")
    public void logMethodException(JoinPoint joinPoint, Exception exception) {
        logger.error("Method {} threw exception: {}", 
                    joinPoint.getSignature().getName(), 
                    exception.getMessage());
    }
}
