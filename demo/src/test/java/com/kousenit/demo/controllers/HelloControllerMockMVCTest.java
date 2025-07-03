package com.kousenit.demo.controllers;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.assertj.MockMvcTester;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(HelloController.class)
public class HelloControllerMockMVCTest {
    @Autowired  // Ask Spring for an instance of this class from the Application Context
    private MockMvc mvc;

    @Autowired
    private MockMvcTester mvcTester;

    @Test
    void autowiringWorked() {
        assertNotNull(mvc);
        assertNotNull(mvcTester);
    }

    // Newer approach, using MockMvcTester
    @Nested
    class MockMvcTesterTests {
        @Test
        public void testHelloWithoutName() {
            assertThat(mvcTester.get().uri("/hello").accept(MediaType.TEXT_HTML))
                    .hasStatusOk()
                    .hasViewName("welcome")
                    .model().containsEntry("user", "World");
        }

        @Test
        public void testHelloWithName() {
            assertThat(mvcTester.get()
                    .uri("/hello?name={name}", "Dolly")
                    .accept(MediaType.TEXT_HTML))
                    .hasStatusOk()
                    .hasViewName("welcome")
                    .model().containsEntry("user", "Dolly");
        }
    }

    // Older approach, using MockMvc
    @Nested
    class MockMvcTests {
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
}