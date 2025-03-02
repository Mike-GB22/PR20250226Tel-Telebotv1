package org.telebotv1.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import org.telebotv1.config.OpenAiConfig;
import org.telebotv1.dto.GptRequestDto;
import org.telebotv1.dto.GptResponseDto;
import org.telebotv1.dto.VoiceTranscriptionDto;

import java.io.File;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OpenAiClient {


    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final OpenAiConfig openAiConfig;


    public String makePromptRequest(String userPrompt) {
        return makePromptRequest(userPrompt, String.format(openAiConfig.getSystemRole(), openAiConfig.getDefaultLanguage()));
    }

    public String makePromptRequest(String userPrompt, String systemPrompt) {
        StringBuilder stringBuilder = new StringBuilder();
//        stringBuilder
//                .append("Model: ").append(chatModel)
//                .append(sendPromptRequestToChatApi(userPrompt, systemPrompt, chatModel));
        stringBuilder
                .append("Model: *").append(openAiConfig.getChatModelMini()).append("*\n")
                .append(sendPromptRequestToChatApi(userPrompt, systemPrompt, openAiConfig.getChatModelMini()));
        return stringBuilder.toString();
    }

    private String sendPromptRequestToChatApi(String userPrompt, String systemPrompt, String currentChatModel) {
        String response = "Server error";

        HttpHeaders headers = getHttpHeaders(MediaType.APPLICATION_JSON);
        GptRequestDto body = getGptRequest(userPrompt, systemPrompt, currentChatModel);
        HttpEntity<GptRequestDto> request = new HttpEntity<>(body, headers);
        try {
            log.info("Request HttpEntity to restTemplate: \n{}", objectMapper.writeValueAsString(request.getBody()));
            ResponseEntity<String> responseEntity = restTemplate.postForEntity(openAiConfig.getChatApiUrl(), request, String.class);
            log.info("ResponseString from restEntity: \n{}", responseEntity);
            String responseString = responseEntity.getBody();
            log.info("ResponseString from restTemplate: \n{}", responseString);
            response = responseString;

            GptResponseDto responseDto = objectMapper.readValue(responseString, GptResponseDto.class);
            response = responseDto.getMessage();
        } catch (IllegalStateException | JsonProcessingException | HttpClientErrorException e) {
            log.error("JSON to GptResponseDto: {},/n{}", e.getMessage(), e.getStackTrace());
            response += "\n" + e.getMessage();
        }
        return response;
    }

    private HttpHeaders getHttpHeaders(MediaType contentType) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(contentType);
        headers.setBearerAuth(openAiConfig.getApiKey());

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
            ResponseEntity<String> responseEntity = restTemplate.postForEntity(openAiConfig.getTranscriptionApiUrl(), request, String.class);
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
        body.add("model", openAiConfig.getVoiceModel());
        body.add("language", openAiConfig.getVoiceLanguage());

        FileSystemResource fileResource = new FileSystemResource(file);
        body.add("file", fileResource);

        return body;
    }
}
