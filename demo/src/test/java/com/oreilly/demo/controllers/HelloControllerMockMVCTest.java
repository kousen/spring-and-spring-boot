package com.oreilly.demo.controllers;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(HelloController.class)
public class HelloControllerMockMVCTest {
    @Autowired  // Ask Spring for an instance of this class from the Application Context
    private MockMvc mvc;

    @Test
    void autowiringWorked() {
        assertNotNull(mvc);
    }

    @Test
    public void testHelloWithoutName() throws Exception {
        mvc.perform(get("/hello").accept(MediaType.TEXT_HTML))
                .andExpectAll(
                        status().isOk(),
                        view().name("welcome"),
                        model().attribute("user", is("World"))
                );
    }

    @Test
    public void testHelloWithName() throws Exception {
        mvc.perform(get("/hello").param("name", "Dolly")
                        .accept(MediaType.TEXT_HTML))
                .andExpectAll(
                        status().isOk(),
                        view().name("welcome"),
                        model().attribute("user", is("Dolly"))
                );
    }
}