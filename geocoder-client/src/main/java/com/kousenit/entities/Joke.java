package com.kousenit.entities;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Joke {
    private String type;
    private Value value;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Value getValue() {
        return value;
    }

    public void setValue(Value value) {
        this.value = value;
    }

    public class Value {
        private int id;
        private String joke;

        public String getJoke() {
            return joke;
        }

        public void setJoke(String joke) {
            this.joke = joke;
        }
    }

    public String toString() {
        return value.joke;
    }
}
