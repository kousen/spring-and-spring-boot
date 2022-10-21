package com.oreilly.astro.services;

import com.oreilly.astro.entities.AstroResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@SpringBootTest
public class AstroServiceUnitTest {
    @InjectMocks
    private AstroService service = new AstroService(new RestTemplateBuilder());

    @Mock
    private RestTemplate template;

    private final AstroResponse stub = new AstroResponse();

    @BeforeEach
    public void setUp() {
        stub.setNumber(6);
        stub.setMessage("success");
        when(template.getForObject(anyString(), eq(AstroResponse.class)))
                .thenReturn(stub);
    }

    @Test
    public void testAstroWithMocks() {
        AstroResponse response = service.getAstronauts();
        System.out.println(response);
        assertEquals("success", response.getMessage());
        assertEquals(6, response.getNumber());
        //assertEquals(response.getNumber(), response.getPeople().size());
    }
}