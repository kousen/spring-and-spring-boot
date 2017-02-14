package com.kousenit;

import com.kousenit.repositories.SiteRepository;
import com.kousenit.services.GeocoderService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

import java.util.stream.Stream;

//@EnableWebMvc
@SpringBootApplication
public class GeocoderClientApplication {
    // private final Logger logger = LoggerFactory.getLogger(GeocoderClientApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(GeocoderClientApplication.class, args);
    }

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder.build();
    }

    @Bean
    public CommandLineRunner run(SiteRepository repository, GeocoderService service) throws Exception {
        return args -> Stream.of("Boston,MA", "Hartford,CT", "New York,NY",
                "Springfield,MA", "Providence,RI", "Manchester,NH")
                .map(s -> s.split(","))
                .map(service::getLatLng)
                .forEach(repository::save);
    }
}
