# Telegram 고객 응대 봇

Telegram Bot API + Spring Boot 기반 고객 응대 서비스 봇입니다.
메뉴 기반 대화 흐름으로 **회원가입**, **텔레그램 아이디 변경**, **솔루션 문의**, **기타 문의**를 처리합니다.

## 기술 스택

| 항목 | 선택 |
|------|------|
| Framework | Spring Boot 3.5.6 |
| Telegram 라이브러리 | telegrambots-spring-boot-starter 6.9.7.1 |
| 빌드 도구 | Maven |
| Java 버전 | 17+ |
| 상태 관리 | In-Memory `ConcurrentHashMap` |
| DB | Mock Service (인터페이스 분리, 추후 실제 DB 교체 가능) |

## 주요 기능

### 대화 흐름

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

### 네비게이션

- **⬅ 뒤로가기** — 상태 스택에서 이전 단계로 복귀
- **🏠 처음으로** — 세션 초기화 후 메인 메뉴로 이동
- **❌ 취소** — 현재 진행 취소 후 메인 메뉴로 이동

## 아키텍처

### 요청 처리 흐름

```
Telegram Update → CsBot.onUpdateReceived() → MessageRouter.route() → Handler → SendMessage
```

`MessageRouter`는 우선순위 기반으로 분기합니다:

1. `/start` 명령어 → `StartHandler`
2. 네비게이션 버튼 → `NavigationHandler`
3. 메인 메뉴 선택 → 해당 핸들러의 첫 단계
4. 현재 상태 기반 → 해당 핸들러 위임

### 상태 머신

각 사용자는 `UserSession`을 통해 대화 상태를 관리합니다:

- `currentState` — 현재 대화 위치 (`MenuState` enum)
- `stateHistory` — 뒤로가기를 위한 상태 스택 (`Deque<MenuState>`)
- `data` — 단계별 수집 데이터 (`Map<String, Object>`)

### 프로젝트 구조

```
src/main/java/com/unknown/csbot/
├── CsBotApplication.java          # Spring Boot 메인
├── config/
│   └── BotConfig.java             # 봇 토큰, 이름 설정
├── bot/
│   └── CsBot.java                 # TelegramLongPollingBot 구현체
├── enums/
│   ├── MenuState.java             # 대화 상태 Enum (13개 상태)
│   └── MainMenu.java              # 메인 메뉴 항목 Enum
├── model/
│   ├── UserSession.java           # 유저별 대화 상태 + 입력 데이터
│   ├── RegistrationData.java      # 회원가입 수집 데이터
│   ├── IdChangeData.java          # 아이디 변경 수집 데이터
│   └── InquiryData.java           # 문의 수집 데이터
├── handler/
│   ├── MessageRouter.java         # 메시지 라우팅 (상태 기반 분기)
│   ├── StartHandler.java          # /start, 메인 메뉴
│   ├── RegisterHandler.java       # 회원가입 흐름
│   ├── ChangeIdHandler.java       # 아이디 변경 흐름
│   ├── SolutionHandler.java       # 솔루션 문의 흐름
│   ├── InquiryHandler.java        # 기타 문의 흐름
│   └── NavigationHandler.java     # 뒤로가기/처음으로/취소 공통 처리
├── keyboard/
│   └── KeyboardFactory.java       # ReplyKeyboardMarkup 생성
├── service/
│   ├── SessionService.java        # 세션(상태) 관리
│   ├── DbService.java             # DB 인터페이스
│   └── MockDbService.java         # DB Mock 구현
└── util/
    └── BotMessages.java           # 메시지 텍스트 상수 (한국어)
```

## 빌드 및 실행

### 사전 요구사항

- Java 17 이상
- [BotFather](https://t.me/BotFather)에서 생성한 Telegram Bot Token

### 빌드

```bash
mvnw clean package
```

### 실행

환경변수 `BOT_TOKEN`과 `BOT_USERNAME`을 설정한 후 실행합니다.

```bash
# Windows
set BOT_TOKEN=<your-bot-token>
set BOT_USERNAME=<your-bot-username>
mvnw spring-boot:run

# Linux / macOS
export BOT_TOKEN=<your-bot-token>
export BOT_USERNAME=<your-bot-username>
./mvnw spring-boot:run
```

### 테스트

```bash
mvnw test
```

## 설정

`application.properties`에서 환경변수를 통해 설정합니다:

```properties
bot.token=${BOT_TOKEN:your-bot-token}
bot.username=${BOT_USERNAME:your-bot-name}
```

### DB 연동 (선택)

현재 DB는 비활성화 상태이며 `MockDbService`로 대체됩니다. 실제 MySQL 연동 시:

1. `application.properties`의 `spring.datasource.*` 주석 해제
2. `spring.autoconfigure.exclude` 라인 제거
3. `DbService` 인터페이스의 실제 구현체 작성
