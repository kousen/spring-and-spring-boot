package com.oreilly.controllers;

import org.junit.Test;
import org.springframework.validation.support.BindingAwareModelMap;

import static org.junit.Assert.*;

public class HelloControllerUnitTests {
    @Test
    public void sayHello() throws Exception {
        HelloController controller = new HelloController();
        String name = controller.sayHello("Dolly", new BindingAwareModelMap());
        assertEquals("hello", name);
    }
}