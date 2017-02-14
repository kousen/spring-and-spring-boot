package com.kousenit.controllers;

import com.kousenit.services.JokeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.util.Map;

@Controller
public class JokeController {
    private Logger logger = LoggerFactory.getLogger(JokeController.class);

    @Autowired
    private JokeService service;

    @GetMapping("/joke")
    public String getJoke(
            @RequestParam(name = "first", required = false, defaultValue = "Chuck") String first,
            @RequestParam(name = "last", required = false, defaultValue = "Norris") String last,
            Model model)
            throws IOException {
        model.addAttribute("joke", service.getJoke(first, last));
        return "joke";
    }
}
