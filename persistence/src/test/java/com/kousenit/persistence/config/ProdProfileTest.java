package com.kousenit.persistence.config;

import com.kousenit.persistence.config.ProfileConfig.ApplicationInfo;
import com.kousenit.persistence.config.ProfileConfig.DatabaseInfo;
import com.kousenit.persistence.config.ProfileConfig.FeatureToggle;
import com.kousenit.persistence.dao.OfficerRepository;
import com.kousenit.persistence.entities.Officer;
import com.kousenit.persistence.entities.Rank;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.DockerClientFactory;

import javax.sql.DataSource;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@SpringBootTest
@ActiveProfiles("prod")
@Testcontainers
@Transactional
class ProdProfileTest {
    private static final Logger logger = LoggerFactory.getLogger(ProdProfileTest.class);

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("officers_db")
            .withUsername("test_user")
            .withPassword("test_password");

    @BeforeAll
    static void checkDockerAvailability() {
        try {
            boolean dockerAvailable = DockerClientFactory.instance().isDockerAvailable();
            assumeTrue(dockerAvailable, "Docker is not available - skipping Testcontainers tests");
            logger.info("Docker is available - proceeding with Testcontainers tests");
        } catch (Exception e) {
            assumeTrue(false, "Docker check failed: " + e.getMessage());
        }
    }

    @Autowired
    private DatabaseInfo databaseInfo;

    @Autowired
    private ApplicationInfo applicationInfo;

    @Autowired
    private List<FeatureToggle> features;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private OfficerRepository repository;

    @Test
    void testPostgreSQLContainer() {
        assertTrue(postgres.isRunning(), "PostgreSQL container should be running");
        logger.info("PostgreSQL container started on port: {}", postgres.getFirstMappedPort());
        logger.info("JDBC URL: {}", postgres.getJdbcUrl());
    }

    @Test
    void testProdProfileConfiguration() {
        // Test database configuration
        assertEquals("PostgreSQL", databaseInfo.getType());
        assertEquals("Docker Container", databaseInfo.getLocation());
        assertEquals("Production", databaseInfo.getPurpose());
        assertFalse(databaseInfo.isConsoleEnabled(), "H2 console should be disabled in prod profile");

        logger.info("Prod Database Info: {}", databaseInfo);
    }

    @Test
    void testApplicationInfo() {
        assertEquals("Spring Data JPA Persistence Demo", applicationInfo.getName());
        assertEquals("prod", applicationInfo.getEnvironment());
        assertNotNull(applicationInfo.getDescription());

        logger.info("Application Info: {}", applicationInfo);
    }

    @Test
    void testProdFeatures() {
        assertFalse(features.isEmpty());
        
        // Should have production monitoring but NO debug features
        boolean hasProductionMonitoring = features.stream()
                .anyMatch(f -> "production-monitoring".equals(f.getName()) && f.isEnabled());
        boolean hasDebugLogging = features.stream()
                .anyMatch(f -> "debug-logging".equals(f.getName()));

        assertTrue(hasProductionMonitoring, "Prod profile should have production monitoring enabled");
        assertFalse(hasDebugLogging, "Prod profile should NOT have debug logging");

        features.forEach(feature -> logger.info("Prod Feature: {}", feature));
    }

    @Test
    void testDatabaseConnection() {
        assertNotNull(dataSource, "DataSource should be configured");
        
        // Test that we can actually use the PostgreSQL database
        Officer officer = new Officer(Rank.CAPTAIN, "Jean-Luc", "Picard");
        Officer saved = repository.save(officer);
        
        assertNotNull(saved.getId(), "Officer should be saved with generated ID");
        logger.info("Successfully saved officer to PostgreSQL: {}", saved);
        
        // Clean up
        repository.delete(saved);
    }

    @Test
    void testPostgreSQLSpecificFeatures() {
        // This test demonstrates that we're actually using PostgreSQL
        // PostgreSQL supports more advanced features than H2
        
        long initialCount = repository.count();
        
        // Save multiple officers
        Officer kirk = repository.save(new Officer(Rank.CAPTAIN, "James T.", "Kirk"));
        Officer spock = repository.save(new Officer(Rank.COMMANDER, "S'chn T'gai", "Spock"));
        Officer mccoy = repository.save(new Officer(Rank.COMMANDER, "Leonard", "McCoy"));
        
        assertEquals(initialCount + 3, repository.count());
        
        // Test custom query
        List<Officer> captains = repository.findByRank(Rank.CAPTAIN);
        assertTrue(captains.stream().anyMatch(o -> "Kirk".equals(o.getLastName())));
        
        logger.info("Found {} captains in PostgreSQL database", captains.size());
        
        // Clean up
        repository.deleteAll(List.of(kirk, spock, mccoy));
    }
}