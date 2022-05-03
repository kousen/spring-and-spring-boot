package com.oreilly.restclient;

import com.oreilly.restclient.config.MyProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(MyProperties.class)
public class RestclientApplication {

	public static void main(String[] args) {
		SpringApplication.run(RestclientApplication.class, args);
	}

}
