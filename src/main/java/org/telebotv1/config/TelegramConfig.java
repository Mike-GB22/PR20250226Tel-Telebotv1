package org.telebotv1.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telebotv1.controller.Bot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Configuration
public class TelegramConfig {

    @Bean
    public TelegramBotsApi telegramBotsApi(Bot bot) throws TelegramApiException {
        TelegramBotsApi tba = new TelegramBotsApi(DefaultBotSession.class);
        tba.registerBot(bot);
        return tba;
    }
}
