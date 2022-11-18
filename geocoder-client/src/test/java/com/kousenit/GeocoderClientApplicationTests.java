package com.kousenit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class GeocoderClientApplicationTests {

    @Autowired
    private TestRestTemplate restTemplate;

	  @Test
	  public void contextLoads() {}

    @Test
    public void testRootJsp() {
        ResponseEntity<String> entity =
            restTemplate.getForEntity("/", String.class);
        assertThat(entity.getStatusCode()).isEqualTo(HttpStatus.OK);
        System.out.println(entity.getBody());
    }
}
