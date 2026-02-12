package com.unknown.csbot.handler;

import com.unknown.csbot.enums.MenuState;
import com.unknown.csbot.keyboard.KeyboardFactory;
import com.unknown.csbot.model.UserSession;
import com.unknown.csbot.util.BotMessages;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;

@Component
@RequiredArgsConstructor
public class NavigationHandler {

    private static final String BTN_BACK = "⬅ 뒤로가기";
    private static final String BTN_HOME = "🏠 처음으로";
    private static final String BTN_CANCEL = "❌ 취소";

    private final KeyboardFactory keyboardFactory;

    public boolean isNavigationButton(String text) {
        return BTN_BACK.equals(text) || BTN_HOME.equals(text) || BTN_CANCEL.equals(text);
    }

    public SendMessage handle(Long chatId, String text, UserSession session) {
        return switch (text) {
            case BTN_BACK -> handleBack(chatId, session);
            case BTN_HOME -> handleHome(chatId, session);
            case BTN_CANCEL -> handleCancel(chatId, session);
            default -> handleHome(chatId, session);
        };
    }

    private SendMessage handleBack(Long chatId, UserSession session) {
        MenuState prevState = session.goBack();

        if (prevState == null || prevState == MenuState.MAIN_MENU) {
            session.reset();
            return msg(chatId, BotMessages.NO_PREV_STEP, keyboardFactory.mainMenuKeyboard());
        }

        return getPromptForState(chatId, session);
    }

    private SendMessage handleHome(Long chatId, UserSession session) {
        session.reset();
        return msg(chatId, BotMessages.WELCOME, keyboardFactory.mainMenuKeyboard());
    }

    private SendMessage handleCancel(Long chatId, UserSession session) {
        session.reset();
        return msg(chatId, BotMessages.CANCELLED, keyboardFactory.mainMenuKeyboard());
    }

    /**
     * 현재 상태에 맞는 안내 메시지 + 키보드를 반환 (뒤로가기 시 사용)
     */
    private SendMessage getPromptForState(Long chatId, UserSession session) {
        MenuState state = session.getCurrentState();

        return switch (state) {
            // 회원가입
            case REG_NAME -> msg(chatId, BotMessages.REG_NAME, keyboardFactory.navigationKeyboard());
            case REG_PHONE -> msg(chatId, BotMessages.REG_PHONE, keyboardFactory.navigationKeyboard());
            case REG_AGREE -> msg(chatId, BotMessages.REG_AGREE, keyboardFactory.agreeKeyboard());

            // 아이디 변경
            case CHG_OLD_ID -> msg(chatId, BotMessages.CHG_OLD_ID, keyboardFactory.navigationKeyboard());
            case CHG_NEW_ID -> msg(chatId, BotMessages.CHG_NEW_ID, keyboardFactory.navigationKeyboard());
            case CHG_CONFIRM -> msg(chatId,
                    String.format(BotMessages.CHG_CONFIRM,
                            session.getData().getOrDefault("oldId", ""),
                            session.getData().getOrDefault("newId", "")),
                    keyboardFactory.confirmKeyboard());

            // 솔루션 문의
            case SOL_CATEGORY -> msg(chatId, BotMessages.SOL_CATEGORY, keyboardFactory.solutionCategoryKeyboard());
            case SOL_CONTENT -> msg(chatId, BotMessages.SOL_CONTENT, keyboardFactory.navigationKeyboard());

            // 기타 문의
            case INQ_CONTENT -> msg(chatId, BotMessages.INQ_CONTENT, keyboardFactory.navigationKeyboard());

            // 기본
            default -> msg(chatId, BotMessages.WELCOME, keyboardFactory.mainMenuKeyboard());
        };
    }

    private SendMessage msg(Long chatId, String text, ReplyKeyboard keyboard) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId.toString());
        message.setText(text);
        message.setReplyMarkup(keyboard);
        return message;
    }
}
