package com.oreilly.demo.config;

import com.oreilly.demo.json.Greeting;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// JavaConfig approach --> @Configuration class with @Bean methods
@Configuration
public class AppConfig {

    @Bean // @Scope("prototype")
    // @Lazy(value = false)
    public Greeting defaultGreeting() {
        return new Greeting("Hello, World!");
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
