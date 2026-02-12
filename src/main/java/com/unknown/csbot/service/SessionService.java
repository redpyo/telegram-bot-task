package com.unknown.csbot.service;

import com.unknown.csbot.enums.MenuState;
import com.unknown.csbot.model.UserSession;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

@Service
public class SessionService {

    private final ConcurrentHashMap<Long, UserSession> sessions = new ConcurrentHashMap<>();

    public UserSession getOrCreate(Long chatId) {
        return sessions.computeIfAbsent(chatId, UserSession::new);
    }

    public void setState(Long chatId, MenuState state) {
        getOrCreate(chatId).setCurrentState(state);
    }

    public void pushState(Long chatId, MenuState newState) {
        getOrCreate(chatId).pushAndSetState(newState);
    }

    public MenuState popState(Long chatId) {
        return getOrCreate(chatId).goBack();
    }

    public void clear(Long chatId) {
        getOrCreate(chatId).reset();
    }
}
