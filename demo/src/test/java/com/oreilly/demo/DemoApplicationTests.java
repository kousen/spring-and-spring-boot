package com.oreilly.demo;

import com.oreilly.demo.entities.Greeting;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
public class DemoApplicationTests {
	@Autowired
	private ApplicationContext context;

	@Autowired @Qualifier("defaultGreeting")
	private Greeting greeting;

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
		List<String> beans = Arrays.stream(context.getBeanDefinitionNames())
				.collect(Collectors.toList());
		assertTrue(beans.contains("helloController"));
	}

	@Test
	void verifyDefaultGreetingInAppCtx() {
		Greeting greeting = context.getBean("defaultGreeting", Greeting.class);
		assertNotNull(greeting);
	}

	@Test
	void checkSingletonBehavior() {
		Greeting greeting1 = context.getBean("defaultGreeting", Greeting.class);
		Greeting greeting2 = context.getBean("defaultGreeting", Greeting.class);

		// both references assigned to same instance
		// By default, Spring manages singletons
		assertSame(greeting1, greeting2);
		System.out.println(greeting1.getMessage());
		greeting1.setMessage("What up?");

		System.out.println(greeting2.getMessage());
	}

}
