package com.oreilly.demo;

import com.oreilly.demo.json.Greeting;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import java.text.NumberFormat;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class DemoApplicationTests {
	@Autowired
	private ApplicationContext context;

	@Test
	void autowiringWorked() {
		assertNotNull(context);
		System.out.println(context.getClass().getName());
	}

	@Test
	void contextLoads() {
		int count = context.getBeanDefinitionCount();
		System.out.println("There are " + count + " beans in the application context");
		for (String name : context.getBeanDefinitionNames()) {
			System.out.println(name);
		}
	}

	@Test
	void verifyHelloControllerIsInAppContext() {
		List<String> beans = Arrays.stream(context.getBeanDefinitionNames()).toList();
		assertTrue(beans.contains("helloController"));
	}

	@Test
	void verifyNoDefaultGreetingInAppCtx() {
		assertThrows(Exception.class, () -> context.getBean(Greeting.class));
	}

	@Test
	void checkSingletonBehavior() {
		NumberFormat nf1 = context.getBean("usCurrencyFormat", NumberFormat.class);
		NumberFormat nf2 = context.getBean("usCurrencyFormat", NumberFormat.class);

		assertSame(nf1, nf2);
	}

}
