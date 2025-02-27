package org.telebotv1.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.telebotv1.dto.GptRequestDto;
import org.telebotv1.dto.GptResponseDto;

import java.util.List;

@Slf4j
@Component
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

    @Getter
    @Value("${config.openai.chat.system_role}")
    private String systemRole;

    @Value("${config.openai.voice.endpoint}")
    private String transcriptionApiUrl;

    @Value("${config.openai.voice.model}")
    private String voiceMode;

    @Value("${config.openai.voice.language}")
    private String language;

    @Getter
    @Value("${config.openai.delay}")
    private int delay;

//    @Getter
//    @Value("${config.openai.chat.languages}")
//    private List<String> translateToLanguages;

    @Value("${config.openai.chat.default_language}")
    private String defaultLanguage;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public String makePromptRequest(String userPrompt) {
        return makePromptRequest(userPrompt, String.format(systemRole, defaultLanguage));
    }

    public String makePromptRequest(String userPrompt, String systemPrompt) {
        StringBuilder stringBuilder = new StringBuilder();
//        stringBuilder
//                .append("Model: ").append(chatModel)
//                .append(sendPromptRequestToChatApi(userPrompt, systemPrompt, chatModel));
        stringBuilder
                .append("Model: *").append(chatModelMini).append("*\n")
                .append(sendPromptRequestToChatApi(userPrompt, systemPrompt, chatModelMini));
        return stringBuilder.toString();
    }

    private String sendPromptRequestToChatApi(String userPrompt, String systemPrompt, String currentChatModel) {
        String response = "Server error";
        HttpHeaders headers = getHttpHeaders();
        GptRequestDto body = getGptRequest(userPrompt, systemPrompt, currentChatModel);
        HttpEntity<GptRequestDto> request = new HttpEntity<>(body, headers);
        try {
            log.info("Request HttpEntity to restTemplate: \n{}", objectMapper.writeValueAsString(request.getBody()));
            ResponseEntity<String> responseEntity = restTemplate.postForEntity(chatApiUrl, request, String.class);
            String responseString = responseEntity.getBody();
            log.info("ResponseString from restTemplate: \n{}", responseString);
            response = responseString;

            GptResponseDto responseDto = objectMapper.readValue(responseString, GptResponseDto.class);
            response = responseDto.getMessage();
        } catch (IllegalStateException | JsonProcessingException | HttpClientErrorException e) {
            log.error("JSON to GptResponseDto: {},/n{}", e.getMessage(), e.getStackTrace());
            response += "\n" +e.getMessage();
        }
        return response;
    }

    private HttpHeaders getHttpHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        return headers;
    }

    private GptRequestDto getGptRequest(String userPrompt, String systemPrompt, String currentChatModel) {
        return GptRequestDto.builder()
                .model(currentChatModel)
                .messages(
                        List.of(
                                GptRequestDto.Message.builder()
                                        .role(GptRequestDto.Role.SYSTEM)
                                        .content(systemPrompt)
                                        .build(),
                                GptRequestDto.Message.builder()
                                        .role(GptRequestDto.Role.USER)
                                        .content(userPrompt)
                                        .build()))
                .build();
    }
}
