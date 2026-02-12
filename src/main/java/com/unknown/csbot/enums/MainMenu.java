package com.unknown.csbot.enums;

import lombok.Getter;

@Getter
public enum MainMenu {
    REGISTER("📋 회원가입", MenuState.REG_NAME),
    CHANGE_ID("🔄 텔레그램 아이디 변경", MenuState.CHG_OLD_ID),
    SOLUTION("💡 솔루션 문의", MenuState.SOL_CATEGORY),
    INQUIRY("✍ 기타 문의", MenuState.INQ_CONTENT);

    private final String label;
    private final MenuState firstState;

    MainMenu(String label, MenuState firstState) {
        this.label = label;
        this.firstState = firstState;
    }

    public static MainMenu fromLabel(String label) {
        for (MainMenu menu : values()) {
            if (menu.label.equals(label)) {
                return menu;
            }
        }
        return null;
    }
}
