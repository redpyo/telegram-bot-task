package com.unknown.csbot.handler;

import com.unknown.csbot.enums.MenuState;
import com.unknown.csbot.keyboard.KeyboardFactory;
import com.unknown.csbot.model.RegistrationData;
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
public class RegisterHandler {

    private final KeyboardFactory keyboardFactory;
    private final DbService dbService;

    public SendMessage handleStep(Long chatId, String text, UserSession session) {
        MenuState state = session.getCurrentState();

        return switch (state) {
            case REG_NAME -> handleName(chatId, text, session);
            case REG_PHONE -> handlePhone(chatId, text, session);
            case REG_AGREE -> handleAgree(chatId, text, session);
            default -> msg(chatId, BotMessages.WELCOME, keyboardFactory.mainMenuKeyboard());
        };
    }

    private SendMessage handleName(Long chatId, String text, UserSession session) {
        if (text == null) {
            return msg(chatId, BotMessages.REG_NAME, keyboardFactory.navigationKeyboard());
        }
        session.getData().put("name", text);
        session.pushAndSetState(MenuState.REG_PHONE);
        return msg(chatId, BotMessages.REG_PHONE, keyboardFactory.navigationKeyboard());
    }

    private SendMessage handlePhone(Long chatId, String text, UserSession session) {
        session.getData().put("phone", text);
        session.pushAndSetState(MenuState.REG_AGREE);
        return msg(chatId, BotMessages.REG_AGREE, keyboardFactory.agreeKeyboard());
    }

    private SendMessage handleAgree(Long chatId, String text, UserSession session) {
        if ("✅ 동의".equals(text)) {
            RegistrationData data = new RegistrationData();
            data.setName((String) session.getData().get("name"));
            data.setPhone((String) session.getData().get("phone"));
            data.setAgreedToTerms(true);
            dbService.saveRegistration(data);
            session.reset();
            return msg(chatId, BotMessages.SUBMITTED, keyboardFactory.mainMenuKeyboard());
        } else {
            session.reset();
            return msg(chatId, BotMessages.REG_DENIED, keyboardFactory.mainMenuKeyboard());
        }
    }

    private SendMessage msg(Long chatId, String text, ReplyKeyboard keyboard) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText(text);
        message.setReplyMarkup(keyboard);
        return message;
    }
}
