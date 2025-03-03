package org.telebotv1.controller.command;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telebotv1.controller.Bot;
import org.telebotv1.service.SendService;
import org.telebotv1.service.TranslatorService;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;

@Component
@RequiredArgsConstructor
public class HasText implements Command {

    private final TranslatorService translatorService;
    private final SendService sendService;

    @Override
    public boolean isApplicable(Update update) {
        return update.hasMessage() && update.getMessage().hasText();
    }

    @Override
    public void process(Update update) {
        Message recivedMessage = update.getMessage();
        String chatId = recivedMessage.getChatId().toString();
        //String userName = message.getFrom().toString();

        List<String> messages = translatorService.translate(recivedMessage.getText());
        sendService.sendMessageForEachLanguage(chatId, messages);
    }
}
