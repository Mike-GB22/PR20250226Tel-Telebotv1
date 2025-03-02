package org.telebotv1.config;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Slf4j
@Configuration
@Getter
@RequiredArgsConstructor
public class OpenAiConfig {

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

    @Value("${config.openai.delay}")
    private int delay;

//    @Value("${config.openai.chat.languages}")
    private List<String> translateToLanguages;

    @Value("${config.openai.chat.default_language}")
    private String defaultLanguage;

    private final LanguagesListConfig languagesListConfig;

    @PostConstruct
    public void postConstructor() {
        translateToLanguages = languagesListConfig.getLanguages();
        log.info("\n (i) translateToLanguages: {}", translateToLanguages);
    }
}
