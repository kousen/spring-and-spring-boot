package com.kousenit.restclient.services;

import com.kousenit.restclient.json.Post;
import com.kousenit.restclient.json.User;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;

@Service
public class JsonPlaceholderService {
    private final RestClient restClient;
    private final WebClient webClient;

    public JsonPlaceholderService() {
        this.restClient = RestClient.create("https://jsonplaceholder.typicode.com");
        this.webClient = WebClient.create("https://jsonplaceholder.typicode.com");
    }

    // Synchronous methods using RestClient
    public List<User> getAllUsersSync() {
        return restClient.get()
                .uri("/users")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(new ParameterizedTypeReference<List<User>>() {});
    }

    public Optional<User> getUserByIdSync(Long id) {
        try {
            User user = restClient.get()
                    .uri("/users/{id}", id)
                    .accept(MediaType.APPLICATION_JSON)
                    .retrieve()
                    .body(User.class);
            return Optional.ofNullable(user);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public List<Post> getPostsByUserIdSync(Long userId) {
        return restClient.get()
                .uri("/users/{userId}/posts", userId)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(new ParameterizedTypeReference<List<Post>>() {});
    }

    // Asynchronous methods using WebClient
    public Flux<User> getAllUsersAsync() {
        return webClient.get()
                .uri("/users")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToFlux(User.class)
                .log();
    }

    public Mono<User> getUserByIdAsync(Long id) {
        return webClient.get()
                .uri("/users/{id}", id)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(User.class)
                .log();
    }

    public Flux<Post> getPostsByUserIdAsync(Long userId) {
        return webClient.get()
                .uri("/users/{userId}/posts", userId)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToFlux(Post.class)
                .log();
    }
}