package com.unknown.csbot.enums;

public enum MenuState {
    MAIN_MENU,
    // 회원가입
    REG_NAME, REG_PHONE, REG_AGREE, REG_CONFIRM,
    // 아이디 변경
    CHG_OLD_ID, CHG_NEW_ID, CHG_CONFIRM,
    // 솔루션 문의
    SOL_CATEGORY, SOL_CONTENT, SOL_CONFIRM,
    // 기타 문의
    INQ_CONTENT, INQ_CONFIRM
}
