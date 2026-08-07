package com.kousenit.restclient.json;

import java.util.List;

public class OpenRouterRecords {
    // Request body for the chat completions endpoint (OpenAI-compatible format)
    public record ChatRequest(
            String model,
            List<Message> messages
    ) {}

    public record Message(
            String role,
            String content
    ) {}

    // Response: only the fields we care about — Spring Boot's Jackson
    // configuration ignores the rest of the payload (usage, timestamps, etc.)
    public record ChatResponse(
            String id,
            String model,
            List<Choice> choices
    ) {}

    public record Choice(
            Message message
    ) {}
}
