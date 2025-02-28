package org.telebotv1.controller;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
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

    private final TranslatorService translatorService;

    @Value("${secrets.telegram.name:NOT_FOUND}")
    private String name;

    @Value("${secrets.telegram.ownerId}")
    private String ownerId;

    @Value("${secrets.telegram.token}")
    private String token;

    public Bot (@Value("${secrets.telegram.token}") String token, TranslatorService translator) {
        super(token);
        translatorService = translator;
    }

    @Override
    public String getBotUsername() {
        return name;
    }

    @PostConstruct
    public void printInfo() {
        log.info("\n TelegramBot name is: @{}\n Owner ID is: {}", name, ownerId);
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage()) {
            Message recivedMessage = update.getMessage();
            String chatId = recivedMessage.getChatId().toString();
            //String userName = message.getFrom().toString();
            if (recivedMessage.hasText()) {
                List<String> messages = translateOneTextToManyLanguages(recivedMessage.getText());
                sendMessageForEachLanguage(chatId, messages);
            } else if (recivedMessage.hasPhoto() || recivedMessage.hasVideo()) {
                List<String> messages = translateOneTextToManyLanguages(recivedMessage.getCaption());
                if (recivedMessage.hasPhoto() && recivedMessage.hasVideo()) {
                    sendPhotoForEachLanguage(chatId, recivedMessage, messages);
                } else {
                    sendMediaGroupForEachLanguage(chatId, recivedMessage, messages);
                }
            } else if (recivedMessage.hasVoice()) {
                Voice recievedVoice = update.getMessage().getVoice();
                System.out.println("recievedVoice: " + recievedVoice);
                String fileId = recievedVoice.getFileId();
                System.out.println("fileId: " + fileId);
                GetFile getVoiceFileRequest = new GetFile();
                getVoiceFileRequest.setFileId(fileId);

                java.io.File audioFile;
                String transcribtedText = null;
                try {
                    File voiceTeleFile = this.execute(getVoiceFileRequest);
                    System.out.println("File: " + voiceTeleFile);
                    System.out.println("FileURL: " + voiceTeleFile.getFileUrl(token));
                    System.out.println("FilePath: " + voiceTeleFile.getFilePath());

                    audioFile = this.downloadFile(voiceTeleFile.getFilePath());
                    System.out.println("audioFile: " + audioFile);
                    System.out.println("audioFile.getAbsolutePath(): " + audioFile.getAbsolutePath());
                    System.out.println("audioFile.getAbsoluteFile(): " + audioFile.getAbsoluteFile());
                    System.out.println("audioFile.toURI(): " + audioFile.toURI());
                    System.out.println("audioFile.getCanonicalPath(): " + audioFile.getCanonicalPath());
                    System.out.println("audioFile.getName(): " + audioFile.getName());

                    transcribtedText = translatorService.transcribe(audioFile);
                    System.out.println("transcribtedText: " + transcribtedText);
                    System.out.println();

                } catch (TelegramApiException e) {
                    log.error("Voice. Get and Download file. \n{}\nStack: {}", e.getMessage(), e.getStackTrace());
                } catch (IOException e) {
                    log.error("Voice. IOException. \n{}\nStack: {}", e.getMessage(), e.getStackTrace());
                }

                if (null != transcribtedText && !transcribtedText.isBlank()) {
                    List<String> messages = translateOneTextToManyLanguages(transcribtedText);
                    sendMessageForEachLanguage(chatId, messages);
                }
            }
        }
    }

    private List<String> translateOneTextToManyLanguages(String text) {
        if (text == null || text.isBlank()) {
            return new ArrayList<>();
        }
        return translatorService.translate(text);
    }

    private void sendMessageForEachLanguage(String chatId, List<String> messages) {
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
