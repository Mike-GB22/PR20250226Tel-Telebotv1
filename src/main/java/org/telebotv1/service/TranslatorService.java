package org.telebotv1.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telebotv1.client.OpenAiClient;

import java.util.LinkedList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TranslatorService {
    private final OpenAiClient openAiClient;

    public List<String> translate(String message) {
        String systemPrompt = openAiClient.getSystemRole();
        //List<String> languages = openAiClient.getTranslateToLanguages();
        List<String> languages = List.of("Russian", "English", "German");

        List<String> translated = new LinkedList<>();
        for (String language : languages) {
            translated
                    .add(openAiClient.makePromptRequest(message, String.format(systemPrompt, language)));
        }
        return translated;
    }
}
