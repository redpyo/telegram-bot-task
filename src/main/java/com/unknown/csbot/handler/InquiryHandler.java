package com.unknown.csbot.handler;

import com.unknown.csbot.enums.MenuState;
import com.unknown.csbot.keyboard.KeyboardFactory;
import com.unknown.csbot.model.InquiryData;
import com.unknown.csbot.model.UserSession;
import com.unknown.csbot.service.DbService;
import com.unknown.csbot.util.BotMessages;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;

@Slf4j
@Component
@RequiredArgsConstructor
public class InquiryHandler {

    private final KeyboardFactory keyboardFactory;
    private final DbService dbService;

    public SendMessage handleStep(Long chatId, String text, UserSession session) {
        MenuState state = session.getCurrentState();

        return switch (state) {
            case INQ_CONTENT -> handleContent(chatId, text, session);
            default -> msg(chatId, BotMessages.WELCOME, keyboardFactory.mainMenuKeyboard());
        };
    }

    private SendMessage handleContent(Long chatId, String text, UserSession session) {
        if (text == null) {
            return msg(chatId, BotMessages.INQ_CONTENT, keyboardFactory.navigationKeyboard());
        }
        InquiryData data = new InquiryData();
        data.setContent(text);
        dbService.saveInquiry(data);
        session.reset();
        return msg(chatId, BotMessages.SUBMITTED, keyboardFactory.mainMenuKeyboard());
    }

    private SendMessage msg(Long chatId, String text, ReplyKeyboard keyboard) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText(text);
        message.setReplyMarkup(keyboard);
        return message;
    }
}
