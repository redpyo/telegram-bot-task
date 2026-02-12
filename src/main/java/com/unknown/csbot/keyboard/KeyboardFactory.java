package com.unknown.csbot.keyboard;

import com.unknown.csbot.enums.MainMenu;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.List;

@Component
public class KeyboardFactory {

    public ReplyKeyboardMarkup mainMenuKeyboard() {
        ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup();
        keyboard.setResizeKeyboard(true);
        keyboard.setOneTimeKeyboard(false);

        List<KeyboardRow> rows = new ArrayList<>();

        KeyboardRow row1 = new KeyboardRow();
        row1.add(MainMenu.REGISTER.getLabel());
        row1.add(MainMenu.CHANGE_ID.getLabel());

        KeyboardRow row2 = new KeyboardRow();
        row2.add(MainMenu.SOLUTION.getLabel());
        row2.add(MainMenu.INQUIRY.getLabel());

        rows.add(row1);
        rows.add(row2);
        keyboard.setKeyboard(rows);
        return keyboard;
    }

    public ReplyKeyboardMarkup navigationKeyboard() {
        ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup();
        keyboard.setResizeKeyboard(true);

        KeyboardRow row = new KeyboardRow();
        row.add("⬅ 뒤로가기");
        row.add("🏠 처음으로");
        row.add("❌ 취소");

        keyboard.setKeyboard(List.of(row));
        return keyboard;
    }

    public ReplyKeyboardMarkup confirmKeyboard() {
        ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup();
        keyboard.setResizeKeyboard(true);

        KeyboardRow row1 = new KeyboardRow();
        row1.add("✅ 예");
        row1.add("❌ 아니오");

        KeyboardRow row2 = new KeyboardRow();
        row2.add("⬅ 뒤로가기");
        row2.add("🏠 처음으로");
        row2.add("❌ 취소");

        keyboard.setKeyboard(List.of(row1, row2));
        return keyboard;
    }

    public ReplyKeyboardMarkup agreeKeyboard() {
        ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup();
        keyboard.setResizeKeyboard(true);

        KeyboardRow row1 = new KeyboardRow();
        row1.add("✅ 동의");
        row1.add("❌ 거부");

        KeyboardRow row2 = new KeyboardRow();
        row2.add("⬅ 뒤로가기");
        row2.add("🏠 처음으로");
        row2.add("❌ 취소");

        keyboard.setKeyboard(List.of(row1, row2));
        return keyboard;
    }

    public ReplyKeyboardMarkup solutionCategoryKeyboard() {
        ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup();
        keyboard.setResizeKeyboard(true);

        KeyboardRow row1 = new KeyboardRow();
        row1.add("카테고리 A");
        row1.add("카테고리 B");
        row1.add("카테고리 C");

        KeyboardRow row2 = new KeyboardRow();
        row2.add("⬅ 뒤로가기");
        row2.add("🏠 처음으로");
        row2.add("❌ 취소");

        keyboard.setKeyboard(List.of(row1, row2));
        return keyboard;
    }
}
