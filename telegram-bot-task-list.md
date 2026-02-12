# 텔레그램 고객 응대 봇 - Java Spring Boot 구현 작업 리스트

## 개요
텔레그램 봇 API + Spring Boot 기반 고객 응대 서비스.
메뉴 기반 대화 흐름으로 회원가입, 아이디 변경, 솔루션 문의, 기타 문의를 처리한다.
DB 연동은 Mock 처리하고, 대화 흐름과 메뉴 구조에 집중한다.

---

## 대화 흐름 설계

```
/start
  └─ 메인 메뉴 (ReplyKeyboardMarkup)
       ├─ 📋 회원가입
       │    ├─ 이름 입력
       │    ├─ 연락처 입력
       │    ├─ 이용약관 동의 (동의 / 거부)
       │    └─ ✅ 접수 완료 → 메인 메뉴
       │
       ├─ 🔄 텔레그램 아이디 변경
       │    ├─ 기존 아이디 입력
       │    ├─ 새 아이디 입력
       │    ├─ 확인 (예 / 아니오)
       │    └─ ✅ 접수 완료 → 메인 메뉴
       │
       ├─ 💡 솔루션 문의
       │    ├─ 카테고리 선택 (A / B / C)
       │    ├─ 문의 내용 입력
       │    └─ ✅ 접수 완료 → 메인 메뉴
       │
       └─ ✍ 기타 문의
            ├─ 문의 내용 입력
            └─ ✅ 접수 완료 → 메인 메뉴

* 모든 단계 하단 키보드: [⬅ 뒤로가기] [🏠 처음으로] [❌ 취소]
```

---

## 기술 스택

| 항목 | 선택 |
|------|------|
| Framework | Spring Boot 3.x |
| Telegram 라이브러리 | [TelegramBots Spring Boot Starter](https://github.com/rubenlagus/TelegramBots) (`telegrambots-spring-boot-starter`) |
| 빌드 도구 | Gradle (Kotlin DSL) 또는 Maven |
| Java 버전 | 17+ |
| 상태 관리 | In-Memory `ConcurrentHashMap` (추후 Redis 전환 가능) |
| DB | Mock Service (인터페이스만 정의) |

---

## 프로젝트 구조

```
telegram-cs-bot/
├── build.gradle.kts
├── src/main/java/com/example/csbot/
│   ├── CsBotApplication.java                # Spring Boot 메인
│   ├── config/
│   │   └── BotConfig.java                   # 봇 토큰, 이름 설정
│   ├── bot/
│   │   └── CsBot.java                       # TelegramLongPollingBot 구현체
│   ├── enums/
│   │   ├── MenuState.java                   # 대화 상태 Enum
│   │   └── MainMenu.java                    # 메인 메뉴 항목 Enum
│   ├── model/
│   │   ├── UserSession.java                 # 유저별 대화 상태 + 입력 데이터
│   │   ├── RegistrationData.java            # 회원가입 수집 데이터
│   │   ├── IdChangeData.java               # 아이디 변경 수집 데이터
│   │   └── InquiryData.java                # 문의 수집 데이터
│   ├── handler/
│   │   ├── MessageRouter.java              # 메시지 라우팅 (상태 기반 분기)
│   │   ├── StartHandler.java               # /start, 메인 메뉴
│   │   ├── RegisterHandler.java            # 회원가입 흐름
│   │   ├── ChangeIdHandler.java            # 아이디 변경 흐름
│   │   ├── SolutionHandler.java            # 솔루션 문의 흐름
│   │   ├── InquiryHandler.java             # 기타 문의 흐름
│   │   └── NavigationHandler.java          # 뒤로가기/처음으로/취소 공통 처리
│   ├── keyboard/
│   │   └── KeyboardFactory.java            # ReplyKeyboardMarkup 생성
│   ├── service/
│   │   ├── SessionService.java             # 세션(상태) 관리
│   │   └── MockDbService.java              # DB Mock (인터페이스 + 로그 출력)
│   └── util/
│       └── BotMessages.java                # 메시지 텍스트 상수
└── src/main/resources/
    └── application.yml                      # 봇 토큰 설정
```

---

## 작업 리스트

### Phase 1: 프로젝트 세팅

- [ ] **1-1.** Spring Boot 프로젝트 생성 + `build.gradle.kts` 의존성 추가
  ```kotlin
  dependencies {
      implementation("org.springframework.boot:spring-boot-starter")
      implementation("org.telegram:telegrambots-spring-boot-starter:6.9.7.1")
      compileOnly("org.projectlombok:lombok")
      annotationProcessor("org.projectlombok:lombok")
  }
  ```
- [ ] **1-2.** `application.yml` 작성
  ```yaml
  bot:
    token: ${BOT_TOKEN:your-bot-token}
    username: ${BOT_USERNAME:your-bot-name}
  ```
- [ ] **1-3.** `BotConfig.java` — `@ConfigurationProperties`로 토큰/이름 바인딩
- [ ] **1-4.** `CsBot.java` — `TelegramLongPollingBot` 상속, `onUpdateReceived()` 기본 뼈대
- [ ] **1-5.** 로컬 실행 확인 — `/start` 입력 시 "안녕하세요" 응답 확인

---

### Phase 2: Enum 및 모델 정의

- [ ] **2-1.** `MenuState.java` — 대화 상태 Enum
  ```java
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
  ```
- [ ] **2-2.** `MainMenu.java` — 메뉴 텍스트 ↔ 상태 매핑 Enum
- [ ] **2-3.** `UserSession.java` — chatId, 현재 상태, 이전 상태 스택(`Deque`), 수집 데이터
- [ ] **2-4.** `RegistrationData`, `IdChangeData`, `InquiryData` — 각 흐름별 DTO

---

### Phase 3: 공통 컴포넌트

- [ ] **3-1.** `KeyboardFactory.java` — 키보드 생성 유틸 (`@Component`)
  - `mainMenuKeyboard()` → 📋 회원가입 / 🔄 텔레그램 아이디 변경 / 💡 솔루션 문의 / ✍ 기타 문의
  - `navigationKeyboard()` → ⬅ 뒤로가기 / 🏠 처음으로 / ❌ 취소
  - `confirmKeyboard()` → ✅ 예 / ❌ 아니오
  - `agreeKeyboard()` → ✅ 동의 / ❌ 거부
  - `solutionCategoryKeyboard()` → 카테고리 A / B / C
- [ ] **3-2.** `BotMessages.java` — 메시지 상수
  ```java
  public static final String WELCOME = "안녕하세요! 무엇을 도와드릴까요?";
  public static final String SUBMITTED = "✅ 접수되었습니다.\n담당자가 확인 후 안내드리겠습니다.";
  public static final String NO_PREV_STEP = "⬅ 이전 단계가 없습니다.\n메인 메뉴로 이동합니다.";
  public static final String CANCELLED = "❌ 취소되었습니다. 메인 메뉴로 이동합니다.";
  ```
- [ ] **3-3.** `SessionService.java` — `ConcurrentHashMap<Long, UserSession>` 기반 세션 관리
  - `getOrCreate(chatId)` / `setState()` / `pushState()` / `popState()` / `clear()`
- [ ] **3-4.** `MockDbService.java` — DB 연동 Mock
  - `saveRegistration(data)` → 로그 출력 후 true 반환
  - `saveIdChange(data)` → 로그 출력
  - `saveInquiry(data)` → 로그 출력
  - 추후 실제 Repository로 교체할 인터페이스 분리 (`DbService` 인터페이스)

---

### Phase 4: 메시지 라우터 + 네비게이션

- [ ] **4-1.** `MessageRouter.java` — 핵심 분기 로직 (`@Component`)
  ```java
  public SendMessage route(Update update) {
      String text = update.getMessage().getText();
      Long chatId = update.getMessage().getChatId();

      // 1. /start 명령어 → StartHandler
      // 2. 네비게이션 버튼 (뒤로가기, 처음으로, 취소) → NavigationHandler
      // 3. 메인 메뉴 선택 → 해당 핸들러의 첫 단계
      // 4. 현재 상태 기반 → 해당 핸들러 위임
  }
  ```
- [ ] **4-2.** `NavigationHandler.java` — 공통 네비게이션 (`@Component`)
  - "⬅ 뒤로가기" → 세션의 상태 스택에서 pop → 해당 단계 메시지 재출력
  - 스택 비어있으면 → "이전 단계가 없습니다" + 메인 메뉴
  - "🏠 처음으로" → 세션 초기화 + 메인 메뉴
  - "❌ 취소" → 세션 초기화 + 취소 메시지 + 메인 메뉴

---

### Phase 5: 각 메뉴 핸들러 구현

- [ ] **5-1.** `StartHandler.java`
  - `/start` → 환영 메시지 + 메인 메뉴 키보드

- [ ] **5-2.** `RegisterHandler.java` — 회원가입 (3단계)
  - `REG_NAME` → "이름을 입력해주세요" + 네비게이션 키보드
  - `REG_PHONE` → 이름 저장 → "연락처를 입력해주세요"
  - `REG_AGREE` → 연락처 저장 → "이용약관에 동의하시겠습니까?" + 동의 키보드
  - 동의 → `MockDbService.saveRegistration()` → 접수 완료 → 메인 메뉴
  - 거부 → 취소 → 메인 메뉴

- [ ] **5-3.** `ChangeIdHandler.java` — 아이디 변경 (3단계)
  - `CHG_OLD_ID` → "기존 텔레그램 아이디를 입력해주세요"
  - `CHG_NEW_ID` → "새 텔레그램 아이디를 입력해주세요"
  - `CHG_CONFIRM` → 입력 요약 표시 + 확인 키보드 → 접수 완료 / 취소

- [ ] **5-4.** `SolutionHandler.java` — 솔루션 문의 (2단계)
  - `SOL_CATEGORY` → "카테고리를 선택해주세요" + 카테고리 키보드
  - `SOL_CONTENT` → "문의 내용을 입력해주세요"
  - 접수 완료 → 메인 메뉴

- [ ] **5-5.** `InquiryHandler.java` — 기타 문의 (1단계)
  - `INQ_CONTENT` → "기타 문의 내용을 입력해주세요"
  - 접수 완료 → 메인 메뉴

---

### Phase 6: 통합 및 테스트

- [ ] **6-1.** `CsBot.onUpdateReceived()`에서 `MessageRouter.route()` 호출
  ```java
  @Override
  public void onUpdateReceived(Update update) {
      if (update.hasMessage() && update.getMessage().hasText()) {
          SendMessage response = messageRouter.route(update);
          try {
              execute(response);
          } catch (TelegramApiException e) {
              log.error("메시지 전송 실패", e);
          }
      }
  }
  ```
- [ ] **6-2.** 전체 흐름 통합 테스트 (로컬 BotFather 테스트 봇)
  - /start → 메뉴 선택 → 단계별 입력 → 접수 완료 메시지 확인
  - 뒤로가기 → 이전 단계로 복귀 확인
  - 처음으로 / 취소 → 메인 메뉴 복귀 확인
  - 메인 메뉴에서 뒤로가기 → "이전 단계가 없습니다" 확인

---

### Phase 7: 개선 및 배포 준비 (추후)

- [ ] **7-1.** Webhook 전환 — `SpringWebhookBot` + `@RestController` 방식
- [ ] **7-2.** 입력값 유효성 검증 (전화번호 정규식, 아이디 형식 등)
- [ ] **7-3.** 관리자 알림 — 접수 시 관리자 채팅방/그룹으로 알림 전송
- [ ] **7-4.** 실제 DB 연동 — Spring Data JPA + MySQL or PostgreSQL
- [ ] **7-5.** 세션 저장소 Redis 전환 (서버 재시작 시 세션 유지)
- [ ] **7-6.** Docker 컨테이너화 + 배포

---

## 핵심 설계: 뒤로가기 (상태 스택)

```java
public class UserSession {
    private MenuState currentState;
    private Deque<MenuState> stateHistory = new ArrayDeque<>();
    private Map<String, Object> data = new HashMap<>();

    public void pushAndSetState(MenuState newState) {
        stateHistory.push(currentState);
        currentState = newState;
    }

    public MenuState goBack() {
        if (stateHistory.isEmpty()) {
            return null;  // → "이전 단계가 없습니다" 트리거
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
```

---

## Claude Code 프롬프트 순서

```
1단계: "Spring Boot 3 + telegrambots-spring-boot-starter로 프로젝트 생성해줘.
       Gradle Kotlin DSL 사용. 프로젝트 구조는 [위 구조] 참고."

2단계: "MenuState Enum, MainMenu Enum, UserSession, RegistrationData,
       IdChangeData, InquiryData 모델 클래스 만들어줘."

3단계: "KeyboardFactory, BotMessages, SessionService, MockDbService 만들어줘."

4단계: "MessageRouter와 NavigationHandler 만들어줘. 뒤로가기는 상태 스택 패턴 사용."

5단계: "RegisterHandler, ChangeIdHandler, SolutionHandler, InquiryHandler 만들어줘."

6단계: "CsBot에 전부 통합하고, 전체 대화 흐름이 동작하도록 연결해줘."
```
