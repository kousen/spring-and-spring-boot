package com.kousenit.restclient.json;

public record Post(
        Long userId,
        Long id,
        String title,
        String body
) {}