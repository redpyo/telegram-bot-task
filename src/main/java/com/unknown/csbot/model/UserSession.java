package com.unknown.csbot.model;

import com.unknown.csbot.enums.MenuState;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class UserSession {

    private Long chatId;
    private MenuState currentState;
    private Deque<MenuState> stateHistory = new ArrayDeque<>();
    private Map<String, Object> data = new HashMap<>();

    public UserSession(Long chatId) {
        this.chatId = chatId;
        this.currentState = MenuState.MAIN_MENU;
    }

    public void pushAndSetState(MenuState newState) {
        stateHistory.push(currentState);
        currentState = newState;
    }

    public MenuState goBack() {
        if (stateHistory.isEmpty()) {
            return null;
        }
        currentState = stateHistory.pop();
        return currentState;
    }

    public void reset() {
        currentState = MenuState.MAIN_MENU;
        stateHistory.clear();
        data.clear();
    }
}
