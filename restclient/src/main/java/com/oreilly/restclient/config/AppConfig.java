package com.oreilly.restclient.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import java.text.NumberFormat;
import java.util.Locale;

@Configuration
public class AppConfig {

    @Bean
    public NumberFormat defaultNumberFormat() {
        return NumberFormat.getCurrencyInstance(Locale.getDefault());
    }

    @Bean
    // @Scope("prototype")
    public NumberFormat indiaNumberFormat() {
        return NumberFormat.getCurrencyInstance(new Locale("hin", "IN"));
    }
}
