package org.telebotv1.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.client.RestTemplate;

@RequiredArgsConstructor
public class OpenAiClient {

    @Value("${secrets.openai.token}")
    private String apiKey;

    //@Value("${}")
    private String chatApiUrl;

    private String chatModel;
    private String systemRole;

    private String transcriptionApiUrl;
    private String voiceMode;

    private String language;


    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
}
