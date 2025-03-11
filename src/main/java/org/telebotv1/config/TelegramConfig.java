package org.telebotv1.config;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telebotv1.controller.Bot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.util.List;

@Slf4j
@Configuration
@Getter
@RequiredArgsConstructor
public class TelegramConfig {

    private final AllowedClientsConfig allowedClientsConfig;

    @Setter
    private Bot bot;

    @Value("${secrets.telegram.name:NOT_FOUND}")
    private String name;

    @Value("${secrets.telegram.ownerId}")
    private String ownerId;

    @Value("${secrets.telegram.token}")
    private String token;

    @Value("${config.telegram.all_clients_are_allowed:true}")
    private boolean allClientsAllowed;

    private List<String> allowedClients;

    @PostConstruct
    public void postConstruct() {
        allowedClients = allowedClientsConfig.getAllowedClients();
        log.info("\n (i) allowed clients: {}", allowedClients);
    }

    @Bean
    public TelegramBotsApi telegramBotsApi(Bot bot) throws TelegramApiException {
        TelegramBotsApi tba = new TelegramBotsApi(DefaultBotSession.class);
        tba.registerBot(bot);
        return tba;
    }
}
