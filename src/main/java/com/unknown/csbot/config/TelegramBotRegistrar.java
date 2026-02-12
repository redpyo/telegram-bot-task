package com.unknown.csbot.config;

import com.unknown.csbot.bot.CsBot;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Slf4j
@Configuration
public class TelegramBotRegistrar {

    @Bean
    public TelegramBotsApi telegramBotsApi(CsBot csBot) throws TelegramApiException {
        TelegramBotsApi api = new TelegramBotsApi(DefaultBotSession.class);
        api.registerBot(csBot);
        log.info("텔레그램 봇 등록 완료: {}", csBot.getBotUsername());
        return api;
    }
}
