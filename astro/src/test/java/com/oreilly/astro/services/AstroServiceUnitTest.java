package com.oreilly.astro.services;

import com.oreilly.astro.entities.AstroResponse;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.client.RestTemplate;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class AstroServiceUnitTest {
    @InjectMocks
    private AstroService service = new AstroService(new RestTemplateBuilder());

    @Mock
    private RestTemplate template;

    private AstroResponse stub = new AstroResponse();

    @Before
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