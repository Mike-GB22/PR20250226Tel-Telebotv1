package org.telebotv1.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.telebotv1.dto.GptRequestDto;
import org.telebotv1.dto.GptResponseDto;
import org.telebotv1.dto.VoiceTranscriptionDto;

import java.io.File;
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
    private String voiceModel;

    @Value("${config.openai.voice.language}")
    private String voiceLanguage;

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

        HttpHeaders headers = getHttpHeaders(MediaType.APPLICATION_JSON);
        GptRequestDto body = getGptRequest(userPrompt, systemPrompt, currentChatModel);
        HttpEntity<GptRequestDto> request = new HttpEntity<>(body, headers);
        try {
            log.info("Request HttpEntity to restTemplate: \n{}", objectMapper.writeValueAsString(request.getBody()));
            ResponseEntity<String> responseEntity = restTemplate.postForEntity(chatApiUrl, request, String.class);
            log.info("ResponseString from restEntity: \n{}", responseEntity);
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

    private HttpHeaders getHttpHeaders(MediaType contentType) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(contentType);
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

    public String transcribe (File audioFile) {
        String response = "Server error";
        HttpHeaders headers = getHttpHeaders(MediaType.MULTIPART_FORM_DATA);
        MultiValueMap<String, Object> body = getVoiceRequest(audioFile);
        HttpEntity<MultiValueMap<String, Object>> request = new HttpEntity<>(body, headers);
        try {
            //log.info("Request HttpEntity to restTemplate: \n{}", objectMapper.writeValueAsString(request.getBody()));
            ResponseEntity<String> responseEntity = restTemplate.postForEntity(transcriptionApiUrl, request, String.class);
            log.info("ResponseString from restEntity: \n{}", responseEntity);
            String responseString = responseEntity.getBody();
            log.info("ResponseString from restTemplate: \n{}", responseString);
            VoiceTranscriptionDto transcriptionDto = objectMapper.readValue(responseString, VoiceTranscriptionDto.class);
            response = transcriptionDto.text();

        } catch (JsonProcessingException e) {
            log.error("JsonProcessingException transcribe: \n{}", e.getMessage());
            response += "\n" + e.getMessage();
        }

        return response;
    }

    private MultiValueMap<String, Object> getVoiceRequest(File file) {
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("model", voiceModel);
        body.add("language", voiceLanguage);

        FileSystemResource fileResource = new FileSystemResource(file);
        body.add("file", fileResource);

        return body;
    }
}
