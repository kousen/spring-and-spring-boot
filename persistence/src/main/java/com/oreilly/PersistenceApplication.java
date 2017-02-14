package com.oreilly;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PersistenceApplication {
    // private Logger logger = LoggerFactory.getLogger(PersistenceApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(PersistenceApplication.class, args);
    }

//    @Bean
//    public CommandLineRunner run(@Qualifier("jpaOfficerDAO") OfficerDAO dao) {
//        return args -> {
//            Officer pike = new Officer(Rank.CAPTAIN, "Christopher", "Pike");
//            logger.info("Before: pike.getId()=" + pike.getId());
//            pike = dao.save(pike);
//            logger.info("After: pike.getId()=" + pike.getId());
//        };
//    }
}
