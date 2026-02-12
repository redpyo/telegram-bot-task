package com.unknown.csbot.bot;

import com.unknown.csbot.config.BotConfig;
import com.unknown.csbot.handler.MessageRouter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Slf4j
@Component
public class CsBot extends TelegramLongPollingBot {

    private final BotConfig botConfig;
    private final MessageRouter messageRouter;

    public CsBot(BotConfig botConfig, MessageRouter messageRouter) {
        super(botConfig.getToken());
        this.botConfig = botConfig;
        this.messageRouter = messageRouter;
    }

    @Override
    public String getBotUsername() {
        return botConfig.getUsername();
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            Long chatId = update.getMessage().getChatId();
            String username = update.getMessage().getFrom().getUserName();
            String text = update.getMessage().getText();

            log.info("[수신] chatId={}, user={}, text=\"{}\"", chatId, username, text);

            SendMessage response = messageRouter.route(update);

            log.info("[발신] chatId={}, text=\"{}\"", response.getChatId(),
                    response.getText().length() > 100
                            ? response.getText().substring(0, 100) + "..."
                            : response.getText());
            try {
                execute(response);
                log.debug("[전송완료] chatId={}", chatId);
            } catch (TelegramApiException e) {
                log.error("[전송실패] chatId={}, error={}", chatId, e.getMessage(), e);
            }
        } else {
            log.debug("[수신-무시] updateId={}, 텍스트 메시지가 아님", update.getUpdateId());
        }
    }
}
