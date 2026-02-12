package com.unknown.csbot.handler;

import com.unknown.csbot.keyboard.KeyboardFactory;
import com.unknown.csbot.model.UserSession;
import com.unknown.csbot.util.BotMessages;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

@Component
@RequiredArgsConstructor
public class StartHandler {

    private final KeyboardFactory keyboardFactory;

    public SendMessage handle(Long chatId, UserSession session) {
        session.reset();

        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText(BotMessages.WELCOME);
        message.setReplyMarkup(keyboardFactory.mainMenuKeyboard());
        return message;
    }
}
