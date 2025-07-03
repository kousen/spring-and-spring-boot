package com.kousenit.demo.controllers;

import com.kousenit.demo.json.Greeting;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
public class HelloRestControllerWithMap {

    private static final Map<String, Greeting> greetingMap = new HashMap<>();

    static {
        greetingMap.put("World", new Greeting("Hello, World!"));
    }

    @GetMapping("/restwithmap")
    public ResponseEntity<Greeting> greet(@RequestParam(defaultValue = "World") String name) {
        Greeting greeting = greetingMap.get(name);
        if (greeting != null) {
            return ResponseEntity.ok(greeting);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/restwithmap/{name}")
    @ResponseStatus(HttpStatus.CREATED)
    public Greeting postGreet(@PathVariable String name) {
        Greeting greeting = greetingMap.get(name);
        if (greeting == null) {
            greeting = new Greeting("Hello, " + name + "!");
            greetingMap.put(name, greeting);
        }
        return greeting;
    }
}
