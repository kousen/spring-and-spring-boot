package com.kousenit.persistence.config;

import com.kousenit.persistence.config.ProfileConfig.ApplicationInfo;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles({"dev", "testing"})  // Multiple profiles example
class ProfileDemonstrationTest {
    private static final Logger logger = LoggerFactory.getLogger(ProfileDemonstrationTest.class);

    @Autowired
    private Environment environment;

    @Autowired
    private ApplicationInfo applicationInfo;

    @Test
    void testMultipleActiveProfiles() {
        String[] activeProfiles = environment.getActiveProfiles();
        assertEquals(2, activeProfiles.length);
        
        // Should have both dev and testing profiles active
        assertTrue(environment.acceptsProfiles("dev"));
        assertTrue(environment.acceptsProfiles("testing"));
        assertFalse(environment.acceptsProfiles("prod"));
        
        logger.info("Active profiles: {}", String.join(", ", activeProfiles));
    }

    @Test
    void testProfilePrecedence() {
        // When multiple profiles are active, later ones can override earlier ones
        // This test shows how profile-specific properties work together
        
        assertNotNull(applicationInfo);
        logger.info("Application running with profiles: {}", applicationInfo.getEnvironment());
    }

    @Test
    void testConditionalProfileChecks() {
        // Demonstrate how to check for profiles programmatically
        if (environment.acceptsProfiles("dev")) {
            logger.info("Development features are available");
        }
        
        if (environment.acceptsProfiles("prod")) {
            logger.info("Production features are available");
        } else {
            logger.info("Non-production environment detected");
        }
        
        // Check for specific profile combinations
        if (environment.acceptsProfiles("dev | test")) {
            logger.info("Either dev or test profile is active");
        }
        
        if (environment.acceptsProfiles("!prod")) {
            logger.info("Production profile is NOT active");
        }
    }

    @Test
    void testDefaultProfile() {
        // This test demonstrates what happens when no profiles are explicitly set
        String[] defaultProfiles = environment.getDefaultProfiles();
        logger.info("Default profiles: {}", String.join(", ", defaultProfiles));
        
        // The default profile is usually "default" unless overridden
        assertNotNull(defaultProfiles);
    }
}