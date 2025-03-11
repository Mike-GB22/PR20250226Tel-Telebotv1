package org.telebotv1.controller;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telebotv1.config.TelegramConfig;
import org.telebotv1.controller.command.Command;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;

import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class Bot extends TelegramLongPollingBot {

    private final List<Command> commands;

    private final String botName;
    @Getter
    private final String ownerId;
    private final boolean allClientsAllowed;
    private final List<String> allowedClients;

    public Bot (TelegramConfig telegramConfig,
                List<Command> commands) {
        super(telegramConfig.getToken());
        telegramConfig.setBot(this);
        this.commands = commands;

        botName = telegramConfig.getName();
        ownerId = telegramConfig.getOwnerId();
        allClientsAllowed = telegramConfig.isAllClientsAllowed();
        if (allClientsAllowed) {
            allowedClients = new ArrayList<>();
        } else {
            allowedClients = telegramConfig.getAllowedClients();
        }
    }

    @Override
    public String getBotUsername() {
        return botName;
    }

    @PostConstruct
    public void printInfo() {
        log.info("\n (i) TelegramBot name is: @{}\n (i) Owner ID is: {}", botName, ownerId);
    }

    @Override
    public void onUpdateReceived(Update update) {
        log.info("\n (i) Was Update received: {}", update);
        if (update.hasMessage()) {
            String chatId = update.getMessage().getChatId().toString();

            if (isClientAllowed(chatId)) {
                commands.stream().filter(c -> c.isApplicable(update))
                        .forEach(c -> c.process(update));
            } else {
                log.warn("\n  (w) Client: {} is not allowed!", chatId);
            }
        } else {
            log.warn("\n  (w) Update has not Message!");
        }
    }

    private boolean isClientAllowed(String chatId) {
        return allClientsAllowed
                || (null != allowedClients && allowedClients.contains(chatId));
    }
}
