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

    private String ownerId;

    public SendService(TelegramConfig telegramConfig) {
        ownerId = telegramConfig.getOwnerId();
    }

    public void sendMessageForEachLanguage(Bot bot, String chatId, List<String> messages) {
        for (String translatedMessage: messages) {
            sendMessage(bot, chatId, translatedMessage);
            if (!ownerId.equals(chatId)) {
                sendMessage(bot, ownerId, String.format("User: %s%n%s", chatId, translatedMessage));
            }
        }
    }

    public void sendPhotoForEachLanguage(Bot bot, String chatId, Message receivedMessage, List<String> messages) {
        for (String translatedMessage: messages) {
            sendPhoto(bot, chatId, receivedMessage, translatedMessage);
            if (!ownerId.equals(chatId)) {
                sendPhoto(bot, ownerId, receivedMessage, String.format("User: %s%n%s", chatId, translatedMessage));
            }
        }
    }

    public void sendVideoForEachLanguage(Bot bot, String chatId, Message receivedMessage, List<String> messages) {
        for (String translatedMessage: messages) {
            sendVideo(bot, chatId, receivedMessage, translatedMessage);
            if (!ownerId.equals(chatId)) {
                sendVideo(bot, ownerId, receivedMessage, String.format("User: %s%n%s", chatId, translatedMessage));
            }
        }
    }

    private void sendMediaGroupForEachLanguage(Bot bot, String chatId, Message receivedMessage, List<String> messages) {
        for (String translatedMessage: messages) {
            sendMediaGroup(bot, chatId, receivedMessage, translatedMessage);
            if (!ownerId.equals(chatId)) {
                sendMediaGroup(bot, ownerId, receivedMessage, String.format("User: %s%n%s", chatId, translatedMessage));
            }
        }
    }

    private void sendMessage(Bot bot, String chatId, String text) {
        SendMessage newTextMessage = new SendMessage();
        newTextMessage.setChatId(chatId);
        newTextMessage.enableMarkdown(true);
        newTextMessage.setText(text);
        try {
            bot.execute(newTextMessage);
        } catch (TelegramApiException e) {
            log.error("Error: {}, stack {}", e.getMessage(), e.getStackTrace());
        }
    }

    private void sendPhoto(Bot bot, String chatId, Message receivedMessage, String text) {
        List<PhotoSize> photoSizes = receivedMessage.getPhoto();

        SendPhoto newPhotoMessage = new SendPhoto();
        newPhotoMessage.setChatId(chatId);
        newPhotoMessage.setCaption(text);

        String fileId = photoSizes.get(photoSizes.size() - 1).getFileId();
        newPhotoMessage.setPhoto(new InputFile(fileId));

        try {
            bot.execute(newPhotoMessage);
        } catch (TelegramApiException e) {
            log.error("Error: {}, stack {}", e.getMessage(), e.getStackTrace());
        }
    }

    private void sendVideo(Bot bot, String chatId, Message receivedMessage, String text) {
        SendVideo newVideoMessage = new SendVideo();
        newVideoMessage.setChatId(chatId);
        newVideoMessage.setCaption(text);

        String fileId = receivedMessage.getVideo().getFileId();
        newVideoMessage.setVideo(new InputFile(fileId));

        try {
            bot.execute(newVideoMessage);
        } catch (TelegramApiException e) {
            log.error("Error: {}, stack {}", e.getMessage(), e.getStackTrace());
        }
    }

    private void sendMediaGroup(Bot bot, String chatId, Message receivedMessage, String text) {
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
            bot.execute(newMediaMessage);
        } catch (TelegramApiException e) {
            log.error("Error: {}, stack {}", e.getMessage(), e.getStackTrace());
        }
    }
}
