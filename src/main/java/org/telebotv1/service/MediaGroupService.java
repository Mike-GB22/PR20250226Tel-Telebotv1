package org.telebotv1.service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telebotv1.config.TelegramConfig;
import org.telebotv1.controller.Bot;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.media.InputMedia;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaPhoto;
import org.telegram.telegrambots.meta.api.objects.media.InputMediaVideo;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MediaGroupService {

    @Getter
    private final Map<String, List<Update>> mapUpdatedWithMediaGroupId = new HashMap<>();
    @Getter
    private final Map<String, LocalDateTime> mapUpdatedWithLastUpdateTime = new HashMap<>();

    @Value("${config.telegram.waiting_time_for_media_group_sec}")
    private long WAITING_TIME_FOR_GROUP_SEC;

    private final SendService sendService;
    private final TranslatorService translatorService;


    public void timer() {
        try {
            Thread.sleep(WAITING_TIME_FOR_GROUP_SEC * 1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        LocalDateTime expiredTime = (LocalDateTime.now()).minusSeconds(WAITING_TIME_FOR_GROUP_SEC);

        List<String> expiredGroupIds = new ArrayList<>();
        for(Map.Entry<String, LocalDateTime> entry : mapUpdatedWithLastUpdateTime.entrySet()) {
            if (entry.getValue().isBefore(expiredTime)) {
                expiredGroupIds.add(entry.getKey());
            }
        }

        for (String groupId : expiredGroupIds) {
            mapUpdatedWithLastUpdateTime.remove(groupId);
            List<Update> updates = mapUpdatedWithMediaGroupId.remove(groupId);

            List<String> captions = new ArrayList<>();
            List<InputMedia> media = new ArrayList<>();
            for (Update update : updates) {
                Message message = update.getMessage();
                String caption = message.getCaption();
                if (null != caption && !caption.isBlank()) {
                    captions.add(caption);
                }
                if (message.hasVideo()) {
                    String videoFileId = message.getVideo().getFileId();
                    media.add(new InputMediaVideo(videoFileId));
                } else if (message.hasPhoto()) {
                    List<PhotoSize> photoSizes = message.getPhoto();
                    String photoFileId = photoSizes.get(photoSizes.size() - 1).getFileId();
                    media.add(new InputMediaPhoto(photoFileId));
                }
            }


            String receivedCaption = String.join("", captions);
            List<String> messages = translatorService.translate(receivedCaption);

            Message recivedMessage = updates.get(0).getMessage();
            String chatId = recivedMessage.getChatId().toString();
            if (media.size() == 1) {
                if (recivedMessage.hasVideo()) {
                    sendService.sendVideoForEachLanguage(chatId, recivedMessage, messages);
                } else if (recivedMessage.hasPhoto()) {
                    sendService.sendPhotoForEachLanguage(chatId, recivedMessage, messages);
                }
            } else {
                if (media.size() > 10) {
                    media = media.subList(0, 10);
                }
                sendService.sendMediaGroupForEachLanguage(chatId, media, messages);
            }
        }
    }
}
