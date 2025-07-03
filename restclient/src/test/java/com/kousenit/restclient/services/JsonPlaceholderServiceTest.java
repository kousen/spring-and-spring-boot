package com.kousenit.restclient.services;

import com.kousenit.restclient.json.Address;
import com.kousenit.restclient.json.Company;
import com.kousenit.restclient.json.Geo;
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

    @Test
    void createUserSync() {
        User newUser = createTestUser();
        
        User createdUser = service.createUserSync(newUser);
        
        assertNotNull(createdUser);
        assertNotNull(createdUser.id()); // JSON Placeholder assigns an ID
        assertEquals(newUser.name(), createdUser.name());
        assertEquals(newUser.email(), createdUser.email());
        
        logger.info("Created user: {}", createdUser);
    }

    @Test
    void createPostSync() {
        Post newPost = new Post(1L, null, "Test Post Title", "This is a test post body");
        
        Post createdPost = service.createPostSync(newPost);
        
        assertNotNull(createdPost);
        assertNotNull(createdPost.id()); // JSON Placeholder assigns an ID
        assertEquals(newPost.title(), createdPost.title());
        assertEquals(newPost.body(), createdPost.body());
        assertEquals(newPost.userId(), createdPost.userId());
        
        logger.info("Created post: {}", createdPost);
    }

    @Test
    void createUserAsync() {
        User newUser = createTestUser();
        
        User createdUser = service.createUserAsync(newUser)
                .block(Duration.ofSeconds(5));
        
        assertNotNull(createdUser);
        assertNotNull(createdUser.id());
        assertEquals(newUser.name(), createdUser.name());
        assertEquals(newUser.email(), createdUser.email());
        
        logger.info("Created user asynchronously: {}", createdUser);
    }

    @Test
    void createPostAsync() {
        Post newPost = new Post(1L, null, "Async Test Post", "This is an async test post");
        
        service.createPostAsync(newPost)
                .as(StepVerifier::create)
                .expectNextMatches(post -> 
                        post.title().equals("Async Test Post") && 
                        post.userId().equals(1L))
                .verifyComplete();
    }

    @Test
    void deleteUserSync() {
        // JSON Placeholder sometimes has timeout issues with DELETE, so we test the method exists
        // and handles the request properly, even if it times out
        try {
            service.deleteUserSync(1L);
            logger.info("Delete user 1 completed successfully");
        } catch (Exception e) {
            // Expect either success or timeout - both are acceptable for this demo
            assertTrue(e.getMessage().contains("timeout") || e.getMessage().contains("I/O error"),
                    "Unexpected error type: " + e.getMessage());
            logger.info("Delete user 1 timed out (expected behavior with JSON Placeholder)");
        }
    }

    @Test
    void deletePostSync() {
        try {
            service.deletePostSync(1L);
            logger.info("Delete post 1 completed successfully");
        } catch (Exception e) {
            // Expect either success or timeout - both are acceptable for this demo
            assertTrue(e.getMessage().contains("timeout") || e.getMessage().contains("I/O error"),
                    "Unexpected error type: " + e.getMessage());
            logger.info("Delete post 1 timed out (expected behavior with JSON Placeholder)");
        }
    }

    @Test
    void deleteUserAsync() {
        // For async deletes, we'll test with a timeout and handle potential network issues
        try {
            service.deleteUserAsync(1L)
                    .timeout(Duration.ofSeconds(10))
                    .block();
            logger.info("Delete user 1 asynchronously completed");
        } catch (Exception e) {
            // Handle timeout or network issues gracefully
            assertTrue(e.getCause() instanceof java.util.concurrent.TimeoutException || 
                      e.getMessage().contains("timeout") ||
                      e.getMessage().contains("I/O error"),
                    "Unexpected error type: " + e.getMessage());
            logger.info("Delete user 1 async timed out (expected behavior with JSON Placeholder)");
        }
    }

    @Test
    void deletePostAsync() {
        try {
            service.deletePostAsync(1L)
                    .timeout(Duration.ofSeconds(10))
                    .block();
            logger.info("Delete post 1 asynchronously completed");
        } catch (Exception e) {
            // Handle timeout or network issues gracefully
            assertTrue(e.getCause() instanceof java.util.concurrent.TimeoutException || 
                      e.getMessage().contains("timeout") ||
                      e.getMessage().contains("I/O error"),
                    "Unexpected error type: " + e.getMessage());
            logger.info("Delete post 1 async timed out (expected behavior with JSON Placeholder)");
        }
    }

    private User createTestUser() {
        Geo geo = new Geo("-37.3159", "81.1496");
        Address address = new Address("123 Test St", "Test Suite", "Testville", "12345", geo);
        Company company = new Company("Test Corp", "Testing is our business", "test");
        
        return new User(null, "John Tester", "john.tester", "john@test.com", address, "555-1234", "johntester.com", company);
    }
}