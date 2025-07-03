package com.kousenit.restclient.services;

import com.kousenit.restclient.json.Address;
import com.kousenit.restclient.json.Company;
import com.kousenit.restclient.json.Post;
import com.kousenit.restclient.json.User;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class JsonPlaceholderServiceTest {
    private final Logger logger = LoggerFactory.getLogger(JsonPlaceholderServiceTest.class);

    @Autowired
    private JsonPlaceholderService service;

    @Test
    void getAllUsersSync() {
        List<User> users = service.getAllUsersSync();
        assertNotNull(users);
        assertFalse(users.isEmpty());
        assertEquals(10, users.size()); // JSON Placeholder has 10 users
        
        User firstUser = users.get(0);
        assertNotNull(firstUser.name());
        assertNotNull(firstUser.email());
        assertNotNull(firstUser.address());
        assertNotNull(firstUser.company());
        
        logger.info("First user: {}", firstUser);
    }

    @Test
    void getUserByIdSync() {
        Optional<User> userOpt = service.getUserByIdSync(1L);
        assertTrue(userOpt.isPresent());
        
        User user = userOpt.get();
        assertEquals(1L, user.id());
        assertEquals("Leanne Graham", user.name());
        assertNotNull(user.address().geo());
        
        logger.info("User 1: {}", user);
    }

    @Test
    void getUserByIdSyncNotFound() {
        Optional<User> userOpt = service.getUserByIdSync(999L);
        assertFalse(userOpt.isPresent());
    }

    @Test
    void getPostsByUserIdSync() {
        List<Post> posts = service.getPostsByUserIdSync(1L);
        assertNotNull(posts);
        assertFalse(posts.isEmpty());
        assertEquals(10, posts.size()); // Each user has 10 posts
        
        posts.forEach(post -> {
            assertEquals(1L, post.userId());
            assertNotNull(post.title());
            assertNotNull(post.body());
        });
        
        logger.info("User 1 has {} posts", posts.size());
    }

    @Test
    void getAllUsersAsync() {
        List<User> users = service.getAllUsersAsync()
                .collectList()
                .block(Duration.ofSeconds(5));
                
        assertNotNull(users);
        assertEquals(10, users.size());
        logger.info("Retrieved {} users asynchronously", users.size());
    }

    @Test
    void getUserByIdAsync() {
        User user = service.getUserByIdAsync(1L)
                .block(Duration.ofSeconds(5));
                
        assertNotNull(user);
        assertEquals("Leanne Graham", user.name());
        logger.info("User retrieved asynchronously: {}", user.name());
    }

    @Test
    void getAllUsersAsyncWithStepVerifier() {
        service.getAllUsersAsync()
                .as(StepVerifier::create)
                .expectNextCount(10)
                .verifyComplete();
    }

    @Test
    void getComplexUserData() {
        // Demonstrate working with nested objects
        Optional<User> userOpt = service.getUserByIdSync(1L);
        assertTrue(userOpt.isPresent());
        
        User user = userOpt.get();
        Address address = user.address();
        Company company = user.company();
        
        assertNotNull(address.city());
        assertNotNull(address.geo().lat());
        assertNotNull(company.name());
        
        logger.info("User {} lives in {} and works at {}", 
                user.name(), address.city(), company.name());
    }
}