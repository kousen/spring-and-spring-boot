package com.kousenit.restclient.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@SpringBootTest
class TextToSpeechServiceTest {
    private final Logger logger = LoggerFactory.getLogger(TextToSpeechServiceTest.class);

    @Autowired
    private TextToSpeechService service;

    @BeforeEach
    void checkForApiKey() {
        String key = System.getenv("OPENAI_API_KEY");
        assumeTrue(key != null && !key.isBlank(),
                "OPENAI_API_KEY not set; skipping TTS test");
    }

    @Test
    void speakSpanish() throws IOException {
        // TTS handles non-English text, accents included
        Path mp3 = speakToFile(
                "¡Hola! Bienvenidos al curso de Spring Boot. ¡Que se diviertan!",
                "bienvenida.mp3");
        logger.info("Wrote {} ({} bytes) — play it!", mp3.toAbsolutePath(), Files.size(mp3));
    }

    @Test
    void speakHindi() throws IOException {
        // Devanagari script goes straight through — no transliteration needed
        Path mp3 = speakToFile(
                "नमस्ते! स्प्रिंग बूट कोर्स में आपका स्वागत है। खूब मज़ा कीजिए!",
                "swagat.mp3");
        logger.info("Wrote {} ({} bytes) — play it!", mp3.toAbsolutePath(), Files.size(mp3));
    }

    private Path speakToFile(String text, String fileName) throws IOException {
        Path output = Path.of("build", fileName);
        Files.createDirectories(output.getParent());
        Path mp3 = service.speak(text, output);
        assertTrue(Files.exists(mp3));
        assertTrue(Files.size(mp3) > 1_000, "mp3 file should not be empty");
        return mp3;
    }
}
