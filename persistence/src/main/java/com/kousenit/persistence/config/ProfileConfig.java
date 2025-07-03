package com.kousenit.persistence.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class ProfileConfig {
    private static final Logger logger = LoggerFactory.getLogger(ProfileConfig.class);

    @Value("${app.name}")
    private String appName;

    @Value("${app.environment}")
    private String environment;

    @Value("${app.description}")
    private String description;

    /**
     * Bean that's only active in development profile
     */
    @Bean
    @Profile("dev")
    public DatabaseInfo developmentDatabaseInfo() {
        logger.info("Creating development database info bean");
        return new DatabaseInfo("H2", "In-Memory", "Development", true);
    }

    /**
     * Bean that's only active in test profile
     */
    @Bean
    @Profile("test")
    public DatabaseInfo testDatabaseInfo() {
        logger.info("Creating test database info bean");
        return new DatabaseInfo("H2", "In-Memory", "Testing", false);
    }

    /**
     * Bean that's only active in production profile
     */
    @Bean
    @Profile("prod")
    public DatabaseInfo productionDatabaseInfo() {
        logger.info("Creating production database info bean");
        return new DatabaseInfo("PostgreSQL", "Docker Container", "Production", false);
    }

    /**
     * Bean that's active in development OR test profiles
     */
    @Bean
    @Profile({"dev", "test"})
    public FeatureToggle h2ConsoleFeature() {
        logger.info("Enabling H2 console feature for dev/test profiles");
        return new FeatureToggle("h2-console", true);
    }

    /**
     * Bean that's active in production profile
     */
    @Bean
    @Profile("prod")
    public FeatureToggle productionFeatures() {
        logger.info("Enabling production features");
        return new FeatureToggle("production-monitoring", true);
    }

    /**
     * Bean that's NOT active in production (using ! prefix)
     */
    @Bean
    @Profile("!prod")
    public FeatureToggle debugFeature() {
        logger.info("Enabling debug features for non-production environments");
        return new FeatureToggle("debug-logging", true);
    }

    /**
     * Information about the current application environment
     */
    @Bean
    public ApplicationInfo applicationInfo() {
        return new ApplicationInfo(appName, environment, description);
    }

    // Inner classes for demonstration
    public static class DatabaseInfo {
        private final String type;
        private final String location;
        private final String purpose;
        private final boolean consoleEnabled;

        public DatabaseInfo(String type, String location, String purpose, boolean consoleEnabled) {
            this.type = type;
            this.location = location;
            this.purpose = purpose;
            this.consoleEnabled = consoleEnabled;
        }

        public String getType() { return type; }
        public String getLocation() { return location; }
        public String getPurpose() { return purpose; }
        public boolean isConsoleEnabled() { return consoleEnabled; }

        @Override
        public String toString() {
            return String.format("DatabaseInfo{type='%s', location='%s', purpose='%s', consoleEnabled=%s}",
                    type, location, purpose, consoleEnabled);
        }
    }

    public static class FeatureToggle {
        private final String name;
        private final boolean enabled;

        public FeatureToggle(String name, boolean enabled) {
            this.name = name;
            this.enabled = enabled;
        }

        public String getName() { return name; }
        public boolean isEnabled() { return enabled; }

        @Override
        public String toString() {
            return String.format("FeatureToggle{name='%s', enabled=%s}", name, enabled);
        }
    }

    public static class ApplicationInfo {
        private final String name;
        private final String environment;
        private final String description;

        public ApplicationInfo(String name, String environment, String description) {
            this.name = name;
            this.environment = environment;
            this.description = description;
        }

        public String getName() { return name; }
        public String getEnvironment() { return environment; }
        public String getDescription() { return description; }

        @Override
        public String toString() {
            return String.format("ApplicationInfo{name='%s', environment='%s', description='%s'}",
                    name, environment, description);
        }
    }
}