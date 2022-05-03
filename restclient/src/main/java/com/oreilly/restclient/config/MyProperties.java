package com.oreilly.restclient.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("my.service")
public class MyProperties {
    private String jokeUrl;

    public String getJokeUrl() {
        return jokeUrl;
    }

    public void setJokeUrl(String jokeUrl) {
        this.jokeUrl = jokeUrl;
        System.out.println("baseurl: " + jokeUrl);
    }
}
