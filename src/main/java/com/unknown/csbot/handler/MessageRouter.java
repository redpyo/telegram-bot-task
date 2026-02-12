package com.unknown.csbot.handler;

import com.unknown.csbot.enums.MainMenu;
import com.unknown.csbot.enums.MenuState;
import com.unknown.csbot.model.UserSession;
import com.unknown.csbot.service.SessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

@Slf4j
@Component
@RequiredArgsConstructor
public class MessageRouter {

    private final SessionService sessionService;
    private final StartHandler startHandler;
    private final NavigationHandler navigationHandler;
    private final RegisterHandler registerHandler;
    private final ChangeIdHandler changeIdHandler;
    private final SolutionHandler solutionHandler;
    private final InquiryHandler inquiryHandler;

    public SendMessage route(Update update) {
        String text = update.getMessage().getText();
        Long chatId = update.getMessage().getChatId();
        UserSession session = sessionService.getOrCreate(chatId);

        log.info("[라우팅] chatId={}, state={}, input=\"{}\"", chatId, session.getCurrentState(), text);

        // 1. /start 명령어
        if ("/start".equals(text)) {
            log.info("[라우팅] chatId={} → StartHandler", chatId);
            return startHandler.handle(chatId, session);
        }

        // 2. 네비게이션 버튼
        if (navigationHandler.isNavigationButton(text)) {
            log.info("[라우팅] chatId={} → NavigationHandler ({})", chatId, text);
            return navigationHandler.handle(chatId, text, session);
        }

        // 3. 메인 메뉴 선택
        if (session.getCurrentState() == MenuState.MAIN_MENU) {
            MainMenu menu = MainMenu.fromLabel(text);
            if (menu != null) {
                log.info("[라우팅] chatId={} → {} (메뉴선택)", chatId, menu.name());
                return routeToHandler(chatId, session, menu);
            }
        }

        // 4. 현재 상태 기반 위임
        log.info("[라우팅] chatId={} → state {} 기반 핸들러", chatId, session.getCurrentState());
        return routeByState(chatId, text, session);
    }

    private SendMessage routeToHandler(Long chatId, UserSession session, MainMenu menu) {
        session.pushAndSetState(menu.getFirstState());

        return switch (menu) {
            case REGISTER -> registerHandler.handleStep(chatId, null, session);
            case CHANGE_ID -> changeIdHandler.handleStep(chatId, null, session);
            case SOLUTION -> solutionHandler.handleStep(chatId, null, session);
            case INQUIRY -> inquiryHandler.handleStep(chatId, null, session);
        };
    }

    private SendMessage routeByState(Long chatId, String text, UserSession session) {
        MenuState state = session.getCurrentState();

        return switch (state) {
            case REG_NAME, REG_PHONE, REG_AGREE, REG_CONFIRM ->
                    registerHandler.handleStep(chatId, text, session);
            case CHG_OLD_ID, CHG_NEW_ID, CHG_CONFIRM ->
                    changeIdHandler.handleStep(chatId, text, session);
            case SOL_CATEGORY, SOL_CONTENT, SOL_CONFIRM ->
                    solutionHandler.handleStep(chatId, text, session);
            case INQ_CONTENT, INQ_CONFIRM ->
                    inquiryHandler.handleStep(chatId, text, session);
            default ->
                    startHandler.handle(chatId, session);
        };
    }
}
