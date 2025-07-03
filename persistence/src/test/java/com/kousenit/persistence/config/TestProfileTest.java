package com.kousenit.persistence.config;

import com.kousenit.persistence.config.ProfileConfig.ApplicationInfo;
import com.kousenit.persistence.config.ProfileConfig.DatabaseInfo;
import com.kousenit.persistence.config.ProfileConfig.FeatureToggle;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class TestProfileTest {
    private static final Logger logger = LoggerFactory.getLogger(TestProfileTest.class);

    @Autowired
    private DatabaseInfo databaseInfo;

    @Autowired
    private ApplicationInfo applicationInfo;

    @Autowired
    private List<FeatureToggle> features;

    @Test
    void testTestProfileConfiguration() {
        // Test database configuration
        assertEquals("H2", databaseInfo.getType());
        assertEquals("In-Memory", databaseInfo.getLocation());
        assertEquals("Testing", databaseInfo.getPurpose());
        assertFalse(databaseInfo.isConsoleEnabled(), "H2 console should be disabled in test profile");

        logger.info("Test Database Info: {}", databaseInfo);
    }

    @Test
    void testApplicationInfo() {
        assertEquals("Spring Data JPA Persistence Demo", applicationInfo.getName());
        assertEquals("test", applicationInfo.getEnvironment());
        assertNotNull(applicationInfo.getDescription());

        logger.info("Application Info: {}", applicationInfo);
    }

    @Test
    void testTestFeatures() {
        assertFalse(features.isEmpty());
        
        // Should have h2-console (but disabled) and debug features in test profile
        boolean hasH2Console = features.stream()
                .anyMatch(f -> "h2-console".equals(f.getName()));
        boolean hasDebugLogging = features.stream()
                .anyMatch(f -> "debug-logging".equals(f.getName()) && f.isEnabled());

        assertTrue(hasH2Console, "Test profile should have H2 console feature bean");
        assertTrue(hasDebugLogging, "Test profile should have debug logging enabled");

        features.forEach(feature -> logger.info("Test Feature: {}", feature));
    }
}