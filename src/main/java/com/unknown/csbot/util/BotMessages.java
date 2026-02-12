package com.unknown.csbot.util;

public class BotMessages {

    public static final String WELCOME = "안녕하세요! 무엇을 도와드릴까요?";
    public static final String SUBMITTED = "✅ 접수되었습니다.\n담당자가 확인 후 안내드리겠습니다.";
    public static final String NO_PREV_STEP = "⬅ 이전 단계가 없습니다.\n메인 메뉴로 이동합니다.";
    public static final String CANCELLED = "❌ 취소되었습니다. 메인 메뉴로 이동합니다.";

    // 회원가입
    public static final String REG_NAME = "이름을 입력해주세요.";
    public static final String REG_PHONE = "연락처를 입력해주세요.";
    public static final String REG_AGREE = "이용약관에 동의하시겠습니까?";
    public static final String REG_DENIED = "이용약관에 동의하지 않으셨습니다.\n메인 메뉴로 이동합니다.";

    // 아이디 변경
    public static final String CHG_OLD_ID = "기존 텔레그램 아이디를 입력해주세요.";
    public static final String CHG_NEW_ID = "새 텔레그램 아이디를 입력해주세요.";
    public static final String CHG_CONFIRM = "변경 내용을 확인해주세요.\n\n기존 아이디: %s\n새 아이디: %s\n\n맞으시면 '✅ 예'를 눌러주세요.";

    // 솔루션 문의
    public static final String SOL_CATEGORY = "카테고리를 선택해주세요.";
    public static final String SOL_CONTENT = "문의 내용을 입력해주세요.";

    // 기타 문의
    public static final String INQ_CONTENT = "기타 문의 내용을 입력해주세요.";

    private BotMessages() {
    }
}
