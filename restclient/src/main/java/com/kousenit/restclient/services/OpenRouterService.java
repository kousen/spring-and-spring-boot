package com.kousenit.restclient.services;

import com.kousenit.restclient.json.OpenRouterRecords.ChatRequest;
import com.kousenit.restclient.json.OpenRouterRecords.ChatResponse;
import com.kousenit.restclient.json.OpenRouterRecords.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

/**
 * Chat with any of the hundreds of models on OpenRouter using a single API key.
 * OpenRouter mirrors OpenAI's chat completions wire format, so the same
 * RestClient + records approach used elsewhere in this project works here —
 * only the base URL and the model string change.
 *
 * The default model "openrouter/free" is a router that picks one of the
 * currently available free models; the response reports which one answered.
 */
@Service
public class OpenRouterService {
    private final Logger logger = LoggerFactory.getLogger(OpenRouterService.class);

    private final RestClient client;
    private final String defaultModel;

    public OpenRouterService(@Value("${OPENROUTER_API_KEY:}") String apiKey,
                             @Value("${openrouter.model:openrouter/free}") String defaultModel) {
        client = RestClient.builder()
                .baseUrl("https://openrouter.ai/api/v1")
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .build();
        this.defaultModel = defaultModel;
    }

    public String chat(String prompt) {
        return chat(defaultModel, prompt);
    }

    public String chat(String model, String prompt) {
        ChatResponse response = client.post()
                .uri("/chat/completions")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new ChatRequest(model, List.of(new Message("user", prompt))))
                .retrieve()
                .body(ChatResponse.class);
        if (response == null || response.choices().isEmpty()) {
            throw new IllegalStateException("No response from OpenRouter for model " + model);
        }
        logger.info("Requested model {}, served by {}", model, response.model());
        return response.choices().getFirst().message().content();
    }
}
