package com.kousenit.restclient.json;

public record Address(
        String street,
        String suite,
        String city,
        String zipcode,
        Geo geo
) {}