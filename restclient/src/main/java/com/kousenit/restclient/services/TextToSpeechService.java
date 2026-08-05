package com.kousenit.restclient.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

@Service
public class TextToSpeechService {
    private final RestClient client;

    public TextToSpeechService(RestClient.Builder builder,
                               @Value("${OPENAI_API_KEY:}") String apiKey) {
        client = builder.baseUrl("https://api.openai.com")
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .build();
    }

    // Request body for the OpenAI speech endpoint; serialized to JSON by Jackson
    public record TtsRequest(String model, String input, String voice) {
    }

    /**
     * Converts text to speech and streams the resulting mp3 directly into a file.
     * The exchange(...) callback exposes the raw response body as an InputStream,
     * so this is the RestClient analog of Java's HttpClient with
     * HttpResponse.BodyHandlers.ofFile(path) — the audio is never buffered
     * in memory as a byte array.
     */
    public Path speak(String text, Path outputFile) {
        return client.post()
                .uri("/v1/audio/speech")
                .contentType(MediaType.APPLICATION_JSON)
                .body(new TtsRequest("gpt-4o-mini-tts", text, "alloy"))
                .exchange((request, response) -> {
                    if (!response.getStatusCode().is2xxSuccessful()) {
                        throw new IllegalStateException(
                                "TTS request failed with status " + response.getStatusCode());
                    }
                    try (var body = response.getBody()) {
                        Files.copy(body, outputFile, StandardCopyOption.REPLACE_EXISTING);
                    }
                    return outputFile;
                });
    }
}
