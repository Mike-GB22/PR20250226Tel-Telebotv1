package org.telebotv1.controller;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telebotv1.config.TelegramConfig;
import org.telebotv1.controller.command.Command;
import org.telebotv1.service.TranslatorService;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.send.SendMediaGroup;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.File;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.Video;
import org.telegram.telegrambots.meta.api.objects.Voice;
import org.telegram.telegrambots.meta.api.objects.media.InputMedia;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaPhoto;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaVideo;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class Bot extends TelegramLongPollingBot {

    private final TelegramConfig telegramConfig;
    private final List<Command> commands;

    private final String botName;
    private final String ownerId;


    public Bot (TelegramConfig telegramConfig,
                List<Command> commands) {
        super(telegramConfig.getToken());
        this.telegramConfig = telegramConfig;
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
                    .forEach(c -> c.process(this, update));
        }
    }

    public void sendMessageForEachLanguage(String chatId, List<String> messages) {
        for (String translatedMessage: messages) {
            sendMessage(chatId, translatedMessage);
            if (!ownerId.equals(chatId)) {
                sendMessage(ownerId, String.format("User: %s%n%s", chatId, translatedMessage));
            }
        }
    }

    private void sendPhotoForEachLanguage(String chatId, Message receivedMessage, List<String> messages) {
        for (String translatedMessage: messages) {
            sendPhoto(chatId, receivedMessage, translatedMessage);
            if (!ownerId.equals(chatId)) {
                sendPhoto(ownerId, receivedMessage, String.format("User: %s%n%s", chatId, translatedMessage));
            }
        }
    }

    private void sendMediaGroupForEachLanguage(String chatId, Message receivedMessage, List<String> messages) {
        for (String translatedMessage: messages) {
            sendMediaGroup(chatId, receivedMessage, translatedMessage);
            if (!ownerId.equals(chatId)) {
                sendMediaGroup(ownerId, receivedMessage, String.format("User: %s%n%s", chatId, translatedMessage));
            }
        }
    }

    private void sendMessage(String chatId, String text) {
        SendMessage newTextMessage = new SendMessage();
        newTextMessage.setChatId(chatId);
        newTextMessage.enableMarkdown(true);
        newTextMessage.setText(text);
        try {
            this.execute(newTextMessage);
        } catch (TelegramApiException e) {
            log.error("Error: {}, stack {}", e.getMessage(), e.getStackTrace());
        }
    }

    private void sendPhoto(String chatId, Message receivedMessage, String text) {
        List<PhotoSize> photos = receivedMessage.getPhoto();
        int countOfPhotos = photos.size();

        if (countOfPhotos > 1 || receivedMessage.hasVideo()) {
            sendMediaGroup(chatId, receivedMessage, text);
        } else {
            SendPhoto newPhotoMessage = new SendPhoto();
            newPhotoMessage.setChatId(chatId);
            newPhotoMessage.setCaption(text);

            String fileId = photos.get(countOfPhotos - 1).getFileId();
            newPhotoMessage.setPhoto(new InputFile(fileId));

            try {
                this.execute(newPhotoMessage);
            } catch (TelegramApiException e) {
                log.error("Error: {}, stack {}", e.getMessage(), e.getStackTrace());
            }
        }
    }

    private void sendMediaGroup(String chatId, Message receivedMessage, String text) {
        List<InputMedia> mediaList = new ArrayList<>();
        if (receivedMessage.hasPhoto()) {
            List<PhotoSize> photos = receivedMessage.getPhoto();
            int countOfPhotos = photos.size();

            for (int i = 0; i < countOfPhotos; i++) {
                String fileId = photos.get(i).getFileId();
                System.out.println(i + " --> " + fileId);
                InputMediaPhoto inputMediaPhoto = new InputMediaPhoto(fileId);
                mediaList.add(inputMediaPhoto);
            }
        }

        if (receivedMessage.hasVideo()) {
            Video video = receivedMessage.getVideo();
            InputMediaVideo inputMediaVideo = new InputMediaVideo(video.getFileId());
            mediaList.add(inputMediaVideo);
        }

        SendMediaGroup newMediaMessage = new SendMediaGroup();
        newMediaMessage.setChatId(chatId);
        if (!mediaList.isEmpty()) {
            mediaList.get(0).setCaption(text);
        }
        mediaList = mediaList.stream().limit(10).toList();
        newMediaMessage.setMedias(mediaList);

        try {
            this.execute(newMediaMessage);
        } catch (TelegramApiException e) {
            log.error("Error: {}, stack {}", e.getMessage(), e.getStackTrace());
        }
    }
}
