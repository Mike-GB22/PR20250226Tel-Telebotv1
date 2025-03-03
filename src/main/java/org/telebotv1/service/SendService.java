package org.telebotv1.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.telebotv1.config.TelegramConfig;
import org.telebotv1.controller.Bot;
import org.telegram.telegrambots.meta.api.methods.send.SendMediaGroup;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.send.SendVideo;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.Video;
import org.telegram.telegrambots.meta.api.objects.media.InputMedia;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaPhoto;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaVideo;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class SendService {

    private final TelegramConfig telegramConfig;
    private final String ownerId;


    public SendService(TelegramConfig telegramConfig) {
        this.telegramConfig = telegramConfig;
        ownerId = telegramConfig.getOwnerId();
    }

    public void sendMessageForEachLanguage(String chatId, List<String> messages) {
        for (String translatedMessage: messages) {
            sendMessage(chatId, translatedMessage);
            if (!ownerId.equals(chatId)) {
                sendMessage(ownerId, String.format("User: %s%n%s", chatId, translatedMessage));
            }
        }
    }

    public void sendPhotoForEachLanguage(String chatId, Message receivedMessage, List<String> messages) {
        for (String translatedMessage: messages) {
            sendPhoto(chatId, receivedMessage, translatedMessage);
            if (!ownerId.equals(chatId)) {
                sendPhoto(ownerId, receivedMessage, String.format("User: %s%n%s", chatId, translatedMessage));
            }
        }
    }

    public void sendVideoForEachLanguage(String chatId, Message receivedMessage, List<String> messages) {
        for (String translatedMessage: messages) {
            sendVideo(chatId, receivedMessage, translatedMessage);
            if (!ownerId.equals(chatId)) {
                sendVideo(ownerId, receivedMessage, String.format("User: %s%n%s", chatId, translatedMessage));
            }
        }
    }

    public void sendMediaGroupForEachLanguage(String chatId, List<InputMedia> media, List<String> messages) {
        for (String translatedMessage: messages) {
            sendMediaGroup(chatId, media, translatedMessage);
            if (!ownerId.equals(chatId)) {
                sendMediaGroup(ownerId, media, String.format("User: %s%n%s", chatId, translatedMessage));
            }
        }
    }


    public void sendMessage(String chatId, String text) {
        SendMessage newTextMessage = new SendMessage();
        newTextMessage.setChatId(chatId);
        newTextMessage.enableMarkdown(true);
        newTextMessage.setText(text);
        try {
            telegramConfig.getBot().execute(newTextMessage);
        } catch (TelegramApiException e) {
            log.error("Error: {}, stack {}", e.getMessage(), e.getStackTrace());
        }
    }

    private void sendPhoto(String chatId, Message receivedMessage, String text) {
        List<PhotoSize> photoSizes = receivedMessage.getPhoto();

        SendPhoto newPhotoMessage = new SendPhoto();
        newPhotoMessage.setChatId(chatId);
        newPhotoMessage.setCaption(text);

        String fileId = photoSizes.get(photoSizes.size() - 1).getFileId();
        newPhotoMessage.setPhoto(new InputFile(fileId));

        try {
            telegramConfig.getBot().execute(newPhotoMessage);
        } catch (TelegramApiException e) {
            log.error("Error: {}, stack {}", e.getMessage(), e.getStackTrace());
        }
    }

    private void sendVideo(String chatId, Message receivedMessage, String text) {
        SendVideo newVideoMessage = new SendVideo();
        newVideoMessage.setChatId(chatId);
        newVideoMessage.setCaption(text);

        String fileId = receivedMessage.getVideo().getFileId();
        newVideoMessage.setVideo(new InputFile(fileId));

        try {
            telegramConfig.getBot().execute(newVideoMessage);
        } catch (TelegramApiException e) {
            log.error("Error: {}, stack {}", e.getMessage(), e.getStackTrace());
        }
    }

    public void sendMediaGroup(String chatId, List<InputMedia> media, String text) {
        if (!media.isEmpty()) {
            SendMediaGroup newMediaMessage = new SendMediaGroup();
            newMediaMessage.setChatId(chatId);
            media.get(0).setCaption(text);
            newMediaMessage.setMedias(media);

            try {
                telegramConfig.getBot().execute(newMediaMessage);
            } catch (TelegramApiException e) {
                log.error("Error: {}, stack {}", e.getMessage(), e.getStackTrace());
            }
        }
    }
}
