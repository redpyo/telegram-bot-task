package com.unknown.csbot.handler;

import com.unknown.csbot.enums.MenuState;
import com.unknown.csbot.keyboard.KeyboardFactory;
import com.unknown.csbot.model.IdChangeData;
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
public class ChangeIdHandler {

    private final KeyboardFactory keyboardFactory;
    private final DbService dbService;

    public SendMessage handleStep(Long chatId, String text, UserSession session) {
        MenuState state = session.getCurrentState();

        return switch (state) {
            case CHG_OLD_ID -> handleOldId(chatId, text, session);
            case CHG_NEW_ID -> handleNewId(chatId, text, session);
            case CHG_CONFIRM -> handleConfirm(chatId, text, session);
            default -> msg(chatId, BotMessages.WELCOME, keyboardFactory.mainMenuKeyboard());
        };
    }

    private SendMessage handleOldId(Long chatId, String text, UserSession session) {
        if (text == null) {
            return msg(chatId, BotMessages.CHG_OLD_ID, keyboardFactory.navigationKeyboard());
        }
        session.getData().put("oldId", text);
        session.pushAndSetState(MenuState.CHG_NEW_ID);
        return msg(chatId, BotMessages.CHG_NEW_ID, keyboardFactory.navigationKeyboard());
    }

    private SendMessage handleNewId(Long chatId, String text, UserSession session) {
        session.getData().put("newId", text);
        session.pushAndSetState(MenuState.CHG_CONFIRM);
        String summary = String.format(BotMessages.CHG_CONFIRM,
                session.getData().get("oldId"), text);
        return msg(chatId, summary, keyboardFactory.confirmKeyboard());
    }

    private SendMessage handleConfirm(Long chatId, String text, UserSession session) {
        if ("✅ 예".equals(text)) {
            IdChangeData data = new IdChangeData();
            data.setOldId((String) session.getData().get("oldId"));
            data.setNewId((String) session.getData().get("newId"));
            dbService.saveIdChange(data);
            session.reset();
            return msg(chatId, BotMessages.SUBMITTED, keyboardFactory.mainMenuKeyboard());
        } else {
            session.reset();
            return msg(chatId, BotMessages.CANCELLED, keyboardFactory.mainMenuKeyboard());
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
