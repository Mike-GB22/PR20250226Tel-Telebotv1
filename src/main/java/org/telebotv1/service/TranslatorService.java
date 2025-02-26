package org.telebotv1.service;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TranslatorService {
    //private final OpenAiClient openAiClient;

    public List<String> translate(String message) {
        return List.of(message);
    }
}
