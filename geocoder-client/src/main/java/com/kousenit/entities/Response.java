package com.kousenit.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Response {
    private List<Result> results;
    private String status;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<Result> getResults() {
        return results;
    }

    public void setResults(List<Result> results) {
        this.results = results;
    }

    public Location getLocation() {
        return results.get(0).getGeometry().getLocation();
    }

    public String getFormattedAddress() {
        return results.get(0).getFormattedAddress();
    }
}
