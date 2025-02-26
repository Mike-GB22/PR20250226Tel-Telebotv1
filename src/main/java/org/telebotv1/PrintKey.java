package org.telebotv1;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class PrintKey {
    @Value("${tokens.telegram:NOT_FOUND}")
    private  String telegramKey;

    @Value("${tokens.openai:NOT_FOUND}")
    private String openaiKey;

    @PostConstruct
    public void printSecrets() {
        System.out.println(telegramKey + " - " + openaiKey);
    }

}
