package com.oreilly.restclient;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import java.text.NumberFormat;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

@SpringBootTest
class RestclientApplicationTests {
	@Autowired
	private ApplicationContext context;

	@Test
	void contextLoads() {
		assertNotNull(context);
		System.out.println(context.getClass().getName());
		int count = context.getBeanDefinitionCount();
		System.out.println("There are " + count + " beans in the application context");
		String[] names = context.getBeanDefinitionNames();
		Arrays.stream(names)
				.sorted()
				.forEach(System.out::println);
	}

	@Test
	void checkConfigProperties() {

	}

	@Test
	void showSingletonBehavior() {
		NumberFormat nf1 = context.getBean("indiaNumberFormat", NumberFormat.class);
		NumberFormat nf2 = context.getBean("indiaNumberFormat", NumberFormat.class);

		assertSame(nf1, nf2); // two references to the same instance
	}

	@Test  // Autowire by type, then by name
	void useNumberFormat(@Autowired @Qualifier("indiaNumberFormat") NumberFormat nf) {
		double amount = 12345678.9012345;
		System.out.println(nf.format(amount));
	}
}
