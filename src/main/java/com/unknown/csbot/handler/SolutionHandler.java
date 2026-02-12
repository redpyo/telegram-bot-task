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
public class SolutionHandler {

    private final KeyboardFactory keyboardFactory;
    private final DbService dbService;

    public SendMessage handleStep(Long chatId, String text, UserSession session) {
        MenuState state = session.getCurrentState();

        return switch (state) {
            case SOL_CATEGORY -> handleCategory(chatId, text, session);
            case SOL_CONTENT -> handleContent(chatId, text, session);
            default -> msg(chatId, BotMessages.WELCOME, keyboardFactory.mainMenuKeyboard());
        };
    }

    private SendMessage handleCategory(Long chatId, String text, UserSession session) {
        if (text == null) {
            return msg(chatId, BotMessages.SOL_CATEGORY, keyboardFactory.solutionCategoryKeyboard());
        }
        session.getData().put("category", text);
        session.pushAndSetState(MenuState.SOL_CONTENT);
        return msg(chatId, BotMessages.SOL_CONTENT, keyboardFactory.navigationKeyboard());
    }

    private SendMessage handleContent(Long chatId, String text, UserSession session) {
        InquiryData data = new InquiryData();
        data.setCategory((String) session.getData().get("category"));
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
