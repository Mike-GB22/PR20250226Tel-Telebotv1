package org.telebotv1.controller.command;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telebotv1.service.MediaGroupService;
import org.telebotv1.service.SendService;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Component
@RequiredArgsConstructor
public class isMediaGroup implements Command {

    private final MediaGroupService mediaGroupService;
    private final SendService sendService;

    @Override
    public boolean isApplicable(Update update) {
        return update.hasMessage() && null != update.getMessage().getMediaGroupId();
    }

    @Override
    public void process(Update update) {
        String mediaGroupId = update.getMessage().getMediaGroupId();

        Map<String, List<Update>> mapUpdates = mediaGroupService.getMapUpdatedWithMediaGroupId();
        mapUpdates.computeIfAbsent(mediaGroupId, k -> new ArrayList<>());
        List<Update> updates = mapUpdates.get(mediaGroupId);
        updates.add(update);

        Map<String, LocalDateTime> mapTimesRefresh = mediaGroupService.getMapUpdatedWithLastUpdateTime();
        mapTimesRefresh.put(mediaGroupId, LocalDateTime.now());

        String chatId = update.getMessage().getChatId().toString();
        String messages = String.format("We have in mediaGroupId [%s] already [%d] Updates", mediaGroupId, updates.size());
        sendService.sendMessage(chatId, messages);
        log.info("\n (r) MediaGroupService: {}", messages);

        CompletableFuture<Void> timer = CompletableFuture.runAsync(mediaGroupService::timer);
    }
}
