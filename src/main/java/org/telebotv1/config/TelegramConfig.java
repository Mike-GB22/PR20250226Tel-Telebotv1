package org.telebotv1.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telebotv1.controller.Bot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Getter
@Configuration
public class TelegramConfig {

    @Value("${secrets.telegram.name:NOT_FOUND}")
    private String name;

    @Value("${secrets.telegram.ownerId}")
    private String ownerId;

    @Value("${secrets.telegram.token}")
    private String token;

    @Bean
    public TelegramBotsApi telegramBotsApi(Bot bot) throws TelegramApiException {
        TelegramBotsApi tba = new TelegramBotsApi(DefaultBotSession.class);
        tba.registerBot(bot);
        return tba;
    }
}
