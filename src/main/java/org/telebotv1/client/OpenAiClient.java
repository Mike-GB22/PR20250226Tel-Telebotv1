package org.telebotv1.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;

//@Component
@RequiredArgsConstructor
public class OpenAiClient {

    @Value("${secrets.openai.token}")
    private String apiKey;

    @Value("${config.openai.chat.endpoint}")
    private String chatApiUrl;

    @Value("${config.openai.chat.model}")
    private String chatModel;

    @Value("${config.openai.chat.model_mini}")
    private String chatModelMini;

    @Value("${config.openai.chat.system_role}")
    private String systemRole;

    @Value("${config.openai.voice.endpoint}")
    private String transcriptionApiUrl;

    @Value("${config.openai.voice.model}")
    private String voiceMode;

    @Value("${config.openai.voice.language}")
    private String language;

    @Value("${config.openai.languages}")
    private List<String> translateToLanguages;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public String promptModel(String prompt) {

        return "";
    }

    private HttpHeaders getHttpHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        return headers;

    }
}
