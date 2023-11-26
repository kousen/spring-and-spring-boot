package com.oreilly.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.text.NumberFormat;
import java.util.Locale;

// JavaConfig approach --> @Configuration class with @Bean methods
@Configuration
public class AppConfig {

    @Bean
    public NumberFormat usCurrencyFormat() {
        return NumberFormat.getCurrencyInstance(Locale.US);
    }

    @Bean
    public NumberFormat deCurrencyFormat() {
        return NumberFormat.getCurrencyInstance(Locale.GERMANY);
    }

    @Bean
    public NumberFormat inCurrencyFormat() {
        return NumberFormat.getCurrencyInstance(new Locale("hi", "IN"));
    }

}
