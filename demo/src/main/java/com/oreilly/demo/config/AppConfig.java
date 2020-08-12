package com.oreilly.demo.config;

import com.oreilly.demo.entities.Greeting;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    @Bean // @Scope("prototype")
    // @Lazy(value = false)
    public Greeting defaultGreeting() {
        return new Greeting();
    }

    @Bean
    public Greeting greeting() {
        return new Greeting("What up?");
    }

    // in subclass, check to see if there is already
    // a bean called defaultGreeting in app context
    // if so, return it
    // if not, call super, then add it to app context and return it

    // ApplicationContext is a descendent of BeanFactory
    // ApplicationContext is eager
    // BeanFactory is lazy
}
