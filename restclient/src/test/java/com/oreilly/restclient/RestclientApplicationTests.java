package com.oreilly.restclient;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertNotNull;

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

}
