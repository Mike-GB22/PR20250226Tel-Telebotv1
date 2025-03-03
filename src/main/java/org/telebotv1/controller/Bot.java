package org.telebotv1.controller;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telebotv1.config.TelegramConfig;
import org.telebotv1.controller.command.Command;

import org.telegram.telegrambots.bots.TelegramLongPollingBot;

import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

@Slf4j
@Component
public class Bot extends TelegramLongPollingBot {

    private final List<Command> commands;

    private final String botName;
    @Getter
    private final String ownerId;

    public Bot (TelegramConfig telegramConfig,
                List<Command> commands) {
        super(telegramConfig.getToken());
        telegramConfig.setBot(this);
        this.commands = commands;

        botName = telegramConfig.getName();
        ownerId = telegramConfig.getOwnerId();
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
            commands.stream().filter(c -> c.isApplicable(update))
                    .forEach(c -> c.process(update));
        }
    }
}
