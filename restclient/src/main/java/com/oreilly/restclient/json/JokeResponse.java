package com.oreilly.restclient.json;

import java.util.List;

public class JokeResponse {
    private String id;
    private String url;
    private String iconUrl;
    private String value;

    // available categories: "animal", "career", "celebrity", "dev",
    // "explicit", "fashion", "food", "history", "money", "movie",
    // "music", "political", "religion", "science", "sport", "travel"
    private List<String> categories;

    public List<String> getCategories() {
        return categories;
    }

    public String getIconUrl() {
        return iconUrl;
    }

    public String getId() {
        return id;
    }

    public String getUrl() {
        return url;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return "JokeResponse{" +
               "id='" + id + '\'' +
               ", url='" + url + '\'' +
               ", iconUrl='" + iconUrl + '\'' +
               ", value='" + value + '\'' +
               ", categories=" + categories +
               '}';
    }
}
