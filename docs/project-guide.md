# 텔레그램 고객 응대 봇 - 프로젝트 가이드

## 목차
1. [프로젝트 개요](#1-프로젝트-개요)
2. [텔레그램 BotFather 설정](#2-텔레그램-botfather-설정)
3. [프로젝트 구조](#3-프로젝트-구조)
4. [빌드 및 실행](#4-빌드-및-실행)
5. [아키텍처 설명](#5-아키텍처-설명)
6. [대화 흐름 상세](#6-대화-흐름-상세)
7. [향후 확장](#7-향후-확장)

---

## 1. 프로젝트 개요

텔레그램 봇 API + Spring Boot 기반 고객 응대 서비스.
메뉴 기반 대화 흐름으로 4가지 업무를 처리한다.

| 메뉴 | 기능 | 수집 정보 |
|------|------|-----------|
| 📋 회원가입 | 신규 회원 등록 | 이름, 연락처, 약관동의 |
| 🔄 아이디 변경 | 텔레그램 ID 변경 요청 | 기존 ID, 새 ID |
| 💡 솔루션 문의 | 카테고리별 기술 문의 | 카테고리(A/B/C), 내용 |
| ✍ 기타 문의 | 일반 문의 | 내용 |

**기술 스택:**
- Java 17, Spring Boot 3.5.6
- telegrambots-spring-boot-starter 6.9.7.1
- Maven, Lombok
- Spring JDBC + MySQL (현재 Mock 처리)

---

## 2. 텔레그램 BotFather 설정

### 2-1. 봇 생성

1. 텔레그램 앱에서 **@BotFather** 검색 (파란 체크마크 확인)
2. `/start`를 보내서 BotFather 시작
3. `/newbot` 명령어 입력

```
You: /newbot
BotFather: Alright, a new bot. How are we going to call it?
           Please choose a name for your bot.
You: CS고객응대봇
BotFather: Good. Now let's choose a username for your bot.
           It must end in 'bot'. Like this, for example: TetrisBot or tetris_bot.
You: my_cs_service_bot
BotFather: Done! Congratulations on your new bot. ...
           Use this token to access the HTTP API:
           7123456789:AAH_abcdefghijklmnopqrstuvwxyz1234
```

4. **토큰을 안전한 곳에 저장** (예: `7123456789:AAH_abcdefghijklmnopqrstuvwxyz1234`)
5. 토큰은 절대 외부에 노출하지 않는다 (git commit 금지)

### 2-2. 봇 명령어 등록 (선택)

```
You: /setcommands
BotFather: Choose a bot to change the list of commands.
(봇 선택)
You: start - 봇 시작
```

> 이 프로젝트에서는 `/start` 명령어만 사용한다. 나머지 메뉴는 모두 ReplyKeyboardMarkup(버튼)으로 처리된다.

### 2-3. 봇 이름(username) 확인

BotFather에서 봇을 만들 때 지정한 username이 `BOT_USERNAME` 환경변수에 들어간다.
예: `my_cs_service_bot`

---

## 3. 프로젝트 구조

```
telegram-bot-task/
├── pom.xml                                  # Maven 빌드 설정
├── mvnw.cmd                                 # Maven Wrapper (Windows)
├── .env                                     # 환경변수 (직접 생성)
├── src/main/java/com/unknown/csbot/
│   ├── CsBotApplication.java                # Spring Boot 메인
│   ├── config/
│   │   └── BotConfig.java                   # 봇 토큰/이름 바인딩
│   ├── bot/
│   │   └── CsBot.java                       # TelegramLongPollingBot 구현
│   ├── enums/
│   │   ├── MenuState.java                   # 대화 상태 Enum (13개)
│   │   └── MainMenu.java                    # 메뉴 라벨 ↔ 상태 매핑
│   ├── model/
│   │   ├── UserSession.java                 # 유저별 상태 + 스택 + 데이터
│   │   ├── RegistrationData.java            # 회원가입 DTO
│   │   ├── IdChangeData.java                # 아이디 변경 DTO
│   │   └── InquiryData.java                 # 문의 DTO
│   ├── handler/
│   │   ├── MessageRouter.java               # 메시지 라우팅 (상태 기반)
│   │   ├── StartHandler.java                # /start + 메인 메뉴
│   │   ├── NavigationHandler.java           # 뒤로가기/처음으로/취소
│   │   ├── RegisterHandler.java             # 회원가입 흐름
│   │   ├── ChangeIdHandler.java             # 아이디 변경 흐름
│   │   ├── SolutionHandler.java             # 솔루션 문의 흐름
│   │   └── InquiryHandler.java              # 기타 문의 흐름
│   ├── keyboard/
│   │   └── KeyboardFactory.java             # 키보드 생성 (5종)
│   ├── service/
│   │   ├── SessionService.java              # 세션 관리 (ConcurrentHashMap)
│   │   ├── DbService.java                   # DB 인터페이스
│   │   └── MockDbService.java               # Mock 구현 (로그 출력)
│   └── util/
│       └── BotMessages.java                 # 한국어 메시지 상수
├── src/main/resources/
│   ├── application.properties               # 설정 파일
│   └── logback.xml                          # 로깅 설정
└── src/test/java/com/unknown/csbot/
    └── CsBotApplicationTests.java           # 테스트
```

---

## 4. 빌드 및 실행

### 4-1. 환경변수 설정

프로젝트 루트에 `.env` 파일 생성:

```properties
BOT_TOKEN=7123456789:AAH_abcdefghijklmnopqrstuvwxyz1234
BOT_USERNAME=my_cs_service_bot
```

> `.env` 파일은 `.gitignore`에 추가하여 git에 올라가지 않도록 한다.

### 4-2. 터미널에서 실행

```bash
# 빌드
mvnw.cmd clean package

# 실행 (Windows - 환경변수 직접 설정)
set BOT_TOKEN=7123456789:AAH_abcdefghijklmnopqrstuvwxyz1234
set BOT_USERNAME=my_cs_service_bot
mvnw.cmd spring-boot:run
```

### 4-3. VS Code에서 실행

1. `.vscode/launch.json`이 이미 설정되어 있음
2. `.env` 파일에 토큰을 넣으면 launch.json의 `envFile`로 자동 로드
3. `F5` 키로 디버그 실행

### 4-4. 실행 확인

```
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/
```

위 Spring Boot 배너가 나오면 성공. 텔레그램 앱에서 봇을 찾아 `/start`를 보내면 메뉴가 나타난다.

---

## 5. 아키텍처 설명

### 5-1. 전체 동작 흐름

```
사용자 (텔레그램 앱)
    │
    │ 메시지 전송
    ▼
텔레그램 서버 (api.telegram.org)
    │
    │ Long Polling (봇이 주기적으로 조회)
    ▼
CsBot.onUpdateReceived(Update)
    │
    │ Update 객체에서 chatId, text 추출
    ▼
MessageRouter.route(Update)
    │
    ├─ "/start"             → StartHandler
    ├─ "⬅ 뒤로가기" 등       → NavigationHandler
    ├─ 메인메뉴 상태 + 메뉴텍스트 → 해당 Handler 첫 단계
    └─ 그 외 (현재 상태 기반)   → 해당 Handler 계속 진행
    │
    ▼
SendMessage (응답 텍스트 + 키보드)
    │
    │ execute(sendMessage)
    ▼
텔레그램 서버 → 사용자 화면에 표시
```

### 5-2. 상태 머신 + 스택 기반 네비게이션

모든 사용자는 `UserSession`을 가지며, 대화 상태를 추적한다.

```
예시: 회원가입 진행 과정

[메인 메뉴 선택]
  currentState: MAIN_MENU → REG_NAME
  stateHistory: [] → [MAIN_MENU]

[이름 입력 "홍길동"]
  currentState: REG_NAME → REG_PHONE
  stateHistory: [MAIN_MENU] → [MAIN_MENU, REG_NAME]

[⬅ 뒤로가기 클릭]
  stateHistory에서 pop → REG_NAME
  currentState: REG_PHONE → REG_NAME
  stateHistory: [MAIN_MENU, REG_NAME] → [MAIN_MENU]
  → "이름을 입력해주세요" 메시지 다시 표시
```

### 5-3. 키보드 종류 (KeyboardFactory)

| 메서드 | 버튼 | 사용 시점 |
|--------|------|-----------|
| `mainMenuKeyboard()` | 📋 회원가입 / 🔄 아이디 변경 / 💡 솔루션 문의 / ✍ 기타 문의 | 메인 메뉴 |
| `navigationKeyboard()` | ⬅ 뒤로가기 / 🏠 처음으로 / ❌ 취소 | 텍스트 입력 단계 |
| `agreeKeyboard()` | ✅ 동의 / ❌ 거부 + 네비게이션 | 약관 동의 |
| `confirmKeyboard()` | ✅ 예 / ❌ 아니오 + 네비게이션 | 변경 확인 |
| `solutionCategoryKeyboard()` | 카테고리 A / B / C + 네비게이션 | 솔루션 카테고리 선택 |

### 5-4. DB 구조

현재 `MockDbService`가 로그만 출력한다. 실제 DB 연동 시:

1. `application.properties`에서 datasource 주석 해제
2. `spring.autoconfigure.exclude` 라인 삭제
3. `DbService` 인터페이스를 구현하는 실제 클래스 작성 (JdbcTemplate 사용)

---

## 6. 대화 흐름 상세

### 6-1. 회원가입 (RegisterHandler)

```
/start → 메인 메뉴
  └─ "📋 회원가입" 선택
       │
       ▼ [REG_NAME]
       "이름을 입력해주세요."
       → 사용자: "홍길동"
       │
       ▼ [REG_PHONE]
       "연락처를 입력해주세요."
       → 사용자: "010-1234-5678"
       │
       ▼ [REG_AGREE]
       "이용약관에 동의하시겠습니까?"
       → [✅ 동의] → MockDbService.saveRegistration() → "✅ 접수되었습니다" → 메인 메뉴
       → [❌ 거부] → "이용약관에 동의하지 않으셨습니다" → 메인 메뉴
```

### 6-2. 아이디 변경 (ChangeIdHandler)

```
"🔄 텔레그램 아이디 변경" 선택
  │
  ▼ [CHG_OLD_ID]
  "기존 텔레그램 아이디를 입력해주세요."
  → 사용자: "@old_id"
  │
  ▼ [CHG_NEW_ID]
  "새 텔레그램 아이디를 입력해주세요."
  → 사용자: "@new_id"
  │
  ▼ [CHG_CONFIRM]
  "변경 내용을 확인해주세요.
   기존 아이디: @old_id
   새 아이디: @new_id
   맞으시면 '✅ 예'를 눌러주세요."
  → [✅ 예] → MockDbService.saveIdChange() → "✅ 접수되었습니다" → 메인 메뉴
  → [❌ 아니오] → "❌ 취소되었습니다" → 메인 메뉴
```

### 6-3. 솔루션 문의 (SolutionHandler)

```
"💡 솔루션 문의" 선택
  │
  ▼ [SOL_CATEGORY]
  "카테고리를 선택해주세요."
  → [카테고리 A] / [카테고리 B] / [카테고리 C] 중 택 1
  │
  ▼ [SOL_CONTENT]
  "문의 내용을 입력해주세요."
  → 사용자: "문의 내용..."
  │
  → MockDbService.saveInquiry() → "✅ 접수되었습니다" → 메인 메뉴
```

### 6-4. 기타 문의 (InquiryHandler)

```
"✍ 기타 문의" 선택
  │
  ▼ [INQ_CONTENT]
  "기타 문의 내용을 입력해주세요."
  → 사용자: "문의 내용..."
  │
  → MockDbService.saveInquiry() → "✅ 접수되었습니다" → 메인 메뉴
```

### 6-5. 네비게이션 (모든 단계 공통)

| 버튼 | 동작 |
|------|------|
| ⬅ 뒤로가기 | 스택에서 이전 상태를 꺼내서 해당 단계 메시지를 다시 표시. 스택이 비었으면 "이전 단계가 없습니다" + 메인 메뉴 |
| 🏠 처음으로 | 세션 초기화 + "안녕하세요! 무엇을 도와드릴까요?" + 메인 메뉴 |
| ❌ 취소 | 세션 초기화 + "취소되었습니다" + 메인 메뉴 |

---

## 7. 향후 확장

| 항목 | 설명 |
|------|------|
| Webhook 전환 | `TelegramLongPollingBot` → `SpringWebhookBot` + REST Controller. 서버에 HTTPS 필요 |
| 입력값 검증 | 전화번호 정규식, 아이디 형식 검증 등 |
| 관리자 알림 | 접수 시 관리자 채팅방/그룹으로 알림 전송 |
| 실제 DB 연동 | `DbService` 인터페이스를 JdbcTemplate 기반으로 구현 |
| Redis 세션 | `ConcurrentHashMap` → Redis로 전환 (서버 재시작 시 세션 유지) |
| Docker | 컨테이너화 + docker-compose로 봇 + MySQL + Redis 구성 |
