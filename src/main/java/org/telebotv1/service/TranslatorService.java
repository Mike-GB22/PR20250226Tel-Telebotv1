package org.telebotv1.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telebotv1.client.OpenAiClient;
import org.telebotv1.config.OpenAiConfig;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TranslatorService {
    private final OpenAiClient openAiClient;
    private final OpenAiConfig openAiConfig;

    public List<String> translate(String message) {
        List<String> translated = new LinkedList<>();
        if (null != message && !message.isBlank()) {
            String systemPrompt = openAiConfig.getSystemRole();
            List<String> languages = openAiConfig.getTranslateToLanguages();
            //List<String> languages = List.of("Russian", "English", "German", "Spanish", "Italian", "Arabic", "Serbian", "Ukrainian");

            translated.add("Original: \n" + message);
            for (String language : languages) {
                String response = openAiClient.makePromptRequest(message, String.format(systemPrompt, language));
                translated.add(language + "\n" + response);
                delay();
            }
        }
        return translated;
    }

    private void delay() {
        try {
            Thread.sleep(openAiConfig.getDelay());
        } catch (InterruptedException e) {
            log.error("Translate was interrupted.\n{}", e.getMessage());
        }
    }

    public String transcribe(File file) {
        File newFileName = new File(file.getAbsolutePath() + ".ogg");
        if (file.renameTo(newFileName)) {
            return openAiClient.transcribe(newFileName);
        }
        log.error("Translate. File wasn't rename to .ogg.");
        return "";
    }
}
