package com.kousenit.demo.aspects;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Custom annotation for timing method execution.
 * Methods annotated with @Timed will have their execution time logged.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Timed {
    /**
     * Optional description for the timed method.
     * If empty, the method name will be used.
     */
    String description() default "";
}