package org.telebotv1.controller;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telebotv1.service.TranslatorService;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;

@Slf4j
@Component
public class Bot extends TelegramLongPollingBot {

    private final TranslatorService translatorService;

    @Value("${secrets.telegram.name:NOT_FOUND}")
    private String name;

    @Value("${secrets.telegram.ownerId}")
    private String ownerId;

    public Bot (@Value("${secrets.telegram.token}") String token, TranslatorService translator) {
        super(token);
        translatorService = translator;
    }

    @Override
    public String getBotUsername() {
        return name;
    }

    @PostConstruct
    public void printSecrets() {
        System.out.println(" TelegramBot name is: @" + name);
        System.out.println(" Owner ID is: " + ownerId);
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage()) {
            Message message = update.getMessage();
            String chatId = message.getChatId().toString();
            //String userName = message.getFrom().toString();
            if (message.hasText()) {
                becomeTextMessageTranslateAndSend(chatId, message.getText());
            }
        }
    }

    private void becomeTextMessageTranslateAndSend(String chatId, String message) {
        if (!message.isBlank()) {
             List<String> messages = translateMessage(message);
            for (String translatedMessage: messages) {
                sendMessageAndDuplicate(chatId, translatedMessage);
            }
        }
    }

    private List<String> translateMessage(String message) {
        return translatorService.translate(message);
    }

    private void sendMessageAndDuplicate(String chatId, String text) {
        sendMessage(chatId, text);
        sendMessage(ownerId, String.format("User: %s%n%s", chatId, text));
    }

    private void sendMessage(String chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);
        try {
            this.execute(message);
        } catch (TelegramApiException e) {
            log.error("Error: {}, stack {}", e.getMessage(), e.getStackTrace());
        }
    }
}
