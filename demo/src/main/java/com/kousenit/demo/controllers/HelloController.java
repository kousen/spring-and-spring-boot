package com.kousenit.demo.controllers;

import com.kousenit.demo.aspects.Timed;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class HelloController {

    @GetMapping("/hello")
    @Timed(description = "Hello page rendering")
    public String sayHello(@RequestParam(defaultValue = "World") String name,
                           Model model) {
        model.addAttribute("user", name);
        
        // Simulate some processing time for demonstration
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        return "welcome";
    }
}
