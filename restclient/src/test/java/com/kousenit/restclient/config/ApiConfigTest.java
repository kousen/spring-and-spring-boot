package com.kousenit.restclient.config;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ApiConfigTest {
    private final Logger logger = LoggerFactory.getLogger(ApiConfigTest.class);

    @Autowired
    private ApiConfig apiConfig;

    @Test
    void testApplicationPropertiesInjection() {
        // Test @Value injection from application.properties
        assertEquals("REST Client Demo", apiConfig.getApplicationName());
        assertEquals("1.0.0", apiConfig.getApplicationVersion());
        assertEquals("Demonstrates RestClient and WebClient with external APIs", apiConfig.getApplicationDescription());
        
        logger.info("Application Info: {}", apiConfig.getApplicationInfo());
    }

    @Test
    void testSystemPropertyInjection() {
        // Test @Value injection of system properties
        assertNotNull(apiConfig.getJavaVersion());
        assertNotEquals("unknown", apiConfig.getJavaVersion());
        
        assertNotNull(apiConfig.getUserName());
        assertNotEquals("anonymous", apiConfig.getUserName());
        
        logger.info("Java Version from system property: {}", apiConfig.getJavaVersion());
        logger.info("User Name from system property: {}", apiConfig.getUserName());
    }

    @Test
    void testEnvironmentVariableInjection() {
        // Test @Value injection of environment variables
        assertNotNull(apiConfig.getHomeDirectory());
        // On most systems, HOME should be set
        if (!System.getProperty("os.name").toLowerCase().contains("windows")) {
            assertNotEquals("unknown", apiConfig.getHomeDirectory());
        }
        
        logger.info("Home Directory from environment variable: {}", apiConfig.getHomeDirectory());
    }

    @Test
    void testDefaultValues() {
        // This test demonstrates what happens when properties aren't found
        // The actual values will come from the system, but the test shows the pattern
        
        // If we had a missing property, it would use the default after the colon
        // For example: @Value("${missing.property:default-value}")
        
        assertTrue(apiConfig.getApplicationInfo().contains("REST Client Demo"));
        logger.info("Complete application info: {}", apiConfig.getApplicationInfo());
    }
}