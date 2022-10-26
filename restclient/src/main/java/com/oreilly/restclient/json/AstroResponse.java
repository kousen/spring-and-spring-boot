package com.oreilly.restclient.json;

import java.util.List;

public class AstroResponse {
    private String message;
    private int number;
    private List<Assignment> people;

    public AstroResponse() {
    }

    public AstroResponse(String message, int number, List<Assignment> people) {
        this.message = message;
        this.number = number;
        this.people = people;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public List<Assignment> getPeople() {
        return people;
    }

    public void setPeople(List<Assignment> people) {
        this.people = people;
    }

    @Override
    public String toString() {
        return "AstroResponse{" +
                "message='" + message + '\'' +
                ", number=" + number +
                ", people=" + people +
                '}';
    }
}
