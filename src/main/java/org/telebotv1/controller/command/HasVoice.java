package org.telebotv1.controller.command;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telebotv1.controller.Bot;
import org.telebotv1.service.SendService;
import org.telebotv1.service.TranslatorService;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.objects.File;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.Voice;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class HasVoice implements Command {

    private final TranslatorService translatorService;
    private final SendService sendService;

    @Override
    public boolean isApplicable(Update update) {
        return update.hasMessage() && update.getMessage().hasVoice();
    }

    @Override
    public void process(Bot bot, Update update) {
        Message recivedMessage = update.getMessage();
        String chatId = recivedMessage.getChatId().toString();
        Voice recievedVoice = update.getMessage().getVoice();

        String fileId = recievedVoice.getFileId();
        GetFile getVoiceFileRequest = new GetFile();
        getVoiceFileRequest.setFileId(fileId);

        java.io.File audioFile;
        String transcribtedText = null;
        try {
            File voiceTeleFile = bot.execute(getVoiceFileRequest);
            audioFile = bot.downloadFile(voiceTeleFile.getFilePath());
            transcribtedText = translatorService.transcribe(audioFile);
        } catch (TelegramApiException e) {
            log.error("Voice. Get and Download file. \n{}\nStack: {}", e.getMessage(), e.getStackTrace());
        }

        if (null != transcribtedText && !transcribtedText.isBlank()) {
            List<String> messages = translatorService.translate(transcribtedText);
            sendService.sendMessageForEachLanguage(bot, chatId, messages);
        }
    }
}
