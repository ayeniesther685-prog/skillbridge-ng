package org.skillbridge.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Component
public class GeminiClient {

    private final AiProperties props;
    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public GeminiClient(AiProperties props, ObjectMapper objectMapper) {
        this.props = props;
        this.objectMapper = objectMapper;
        this.restClient = RestClient.builder()
                .baseUrl(props.getBaseUrl())
                .build();
    }

    /**
     * Sends a prompt to Gemini and returns the response text.
     * Throws IllegalStateException if the API call fails.
     */
    public String generate(String userMessage, String systemContext) {
        if (props.getApiKey() == null || props.getApiKey().isBlank()) {
            throw new IllegalStateException("GEMINI_API_KEY is not configured");
        }

        Map<String, Object> body = Map.of(
                "contents", List.of(
                        Map.of(
                                "role", "user",
                                "parts", List.of(Map.of("text",
                                        (systemContext == null ? "" : systemContext + "\n\n") + userMessage))
                        )
                ),
                "generationConfig", Map.of(
                        "temperature", 0.7,
                        "maxOutputTokens", 512,
                        "topP", 0.95
                )
        );

        try {
            String responseBody = restClient.post()
                    .uri("/models/{model}:generateContent?key={key}",
                            props.getModel(), props.getApiKey())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(String.class);

            JsonNode root = objectMapper.readTree(responseBody);
            JsonNode text = root
                    .path("candidates").get(0)
                    .path("content")
                    .path("parts").get(0)
                    .path("text");

            if (text.isMissingNode() || text.isNull()) {
                throw new IllegalStateException("Gemini returned no text. Response: " + responseBody);
            }
            return text.asText();

        } catch (IllegalStateException rethrow) {
            throw rethrow;
        } catch (Exception ex) {
            throw new IllegalStateException("Gemini API call failed: " + ex.getMessage(), ex);
        }
    }

    /**
     * Extracts token count from a response if available, else estimates.
     * Gemini returns usageMetadata.promptTokenCount + candidatesTokenCount.
     */
    public int estimateTokens(String text) {
        if (text == null) return 0;
        // Rough estimate: 1 token ≈ 4 characters
        return Math.max(1, text.length() / 4);
    }
}