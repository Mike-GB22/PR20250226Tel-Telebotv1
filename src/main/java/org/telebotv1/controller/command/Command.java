package org.telebotv1.controller.command;

import org.telebotv1.controller.Bot;
import org.telegram.telegrambots.meta.api.objects.Update;

public interface Command {
    boolean isApplicable(Update update);

    void process(Update update);
}
