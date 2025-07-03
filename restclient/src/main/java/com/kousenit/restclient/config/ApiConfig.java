package com.kousenit.restclient.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApiConfig {
    
    // Inject values from application.properties
    @Value("${app.name}")
    private String applicationName;
    
    @Value("${app.version}")
    private String applicationVersion;
    
    @Value("${app.description}")
    private String applicationDescription;
    
    // Inject system properties with defaults
    @Value("${java.version:unknown}")
    private String javaVersion;
    
    @Value("${user.name:anonymous}")
    private String userName;
    
    // Environment variable with default
    @Value("${HOME:unknown}")
    private String homeDirectory;
    
    // Getter methods for demonstration
    public String getApplicationName() {
        return applicationName;
    }
    
    public String getApplicationVersion() {
        return applicationVersion;
    }
    
    public String getApplicationDescription() {
        return applicationDescription;
    }
    
    public String getJavaVersion() {
        return javaVersion;
    }
    
    public String getUserName() {
        return userName;
    }
    
    public String getHomeDirectory() {
        return homeDirectory;
    }
    
    public String getApplicationInfo() {
        return String.format("%s v%s - %s (Java %s, User: %s)", 
                applicationName, applicationVersion, applicationDescription, 
                javaVersion, userName);
    }
}