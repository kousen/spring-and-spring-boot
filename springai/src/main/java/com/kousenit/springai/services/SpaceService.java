package com.kousenit.springai.services;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SpaceService {
    private final ChatClient chatClient;

    public SpaceService(ChatClient.Builder builder) {
        this.chatClient = builder.build();
    }

    // Simple text in, text out
    public String askQuestion(String question) {
        return chatClient.prompt()
                .user(question)
                .call()
                .content();
    }

    // Structured output: the model's response is mapped onto a Java record
    public SpaceStation describeStation(String stationName) {
        return chatClient.prompt()
                .user(u -> u.text("Describe the space station named {name}")
                        .param("name", stationName))
                .call()
                .entity(SpaceStation.class);
    }

    public record SpaceStation(
            String name,
            String operator,
            int yearLaunched,
            List<String> participatingCountries) {
    }
}
