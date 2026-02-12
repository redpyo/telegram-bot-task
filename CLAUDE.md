# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Telegram customer service bot (고객 응대 봇) built with Java 17+ and Spring Boot 3.x. The bot handles four menu-driven flows: registration, Telegram ID change, solution inquiry, and general inquiry. DB is mocked via `DbService` interface (future real DB support ready).

The specification is in [telegram-bot-task-list.md](telegram-bot-task-list.md) (Korean). All user-facing bot messages are in Korean.

## Build & Run

```bash
# Build
mvnw clean package

# Run (requires BOT_TOKEN and BOT_USERNAME env vars)
set BOT_TOKEN=<token>
set BOT_USERNAME=<name>
mvnw spring-boot:run

# Run tests
mvnw test
```

**Build tool:** Maven (pom.xml), matching the powerball reference project.
**Dependencies:** Spring Boot 3.5.6, `telegrambots-spring-boot-starter:6.9.7.1`, Spring JDBC, MySQL Connector, Lombok.

## Architecture

### State Machine Pattern

The bot uses a **state-based routing** architecture. Each user has a `UserSession` containing:
- `currentState` (`MenuState` enum) — tracks where the user is in a conversation flow
- `stateHistory` (`Deque<MenuState>`) — enables back-navigation by pushing/popping states
- `data` (`Map<String, Object>`) — collects user input across steps

**MenuState values:** `MAIN_MENU`, `REG_NAME`, `REG_PHONE`, `REG_AGREE`, `REG_CONFIRM`, `CHG_OLD_ID`, `CHG_NEW_ID`, `CHG_CONFIRM`, `SOL_CATEGORY`, `SOL_CONTENT`, `SOL_CONFIRM`, `INQ_CONTENT`, `INQ_CONFIRM`

### Request Flow

```
Telegram Update → CsBot.onUpdateReceived() → MessageRouter.route() → Handler → SendMessage
```

`MessageRouter` dispatches based on priority:
1. `/start` command → `StartHandler`
2. Navigation buttons (⬅ 뒤로가기, 🏠 처음으로, ❌ 취소) → `NavigationHandler`
3. Main menu selection → respective handler's first step
4. Current state → respective handler continues the flow

### Handler Pattern

Each conversation flow has a dedicated handler (`@Component`):
- `StartHandler` — `/start` + welcome message with main menu keyboard
- `RegisterHandler` — 3 steps: name → phone → terms agreement → submit
- `ChangeIdHandler` — 3 steps: old ID → new ID → confirmation → submit
- `SolutionHandler` — 2 steps: category selection (A/B/C) → content → submit
- `InquiryHandler` — 1 step: content → submit
- `NavigationHandler` — shared back/home/cancel logic using the state stack

### Key Components

- **`KeyboardFactory`** — creates `ReplyKeyboardMarkup` instances (main menu, navigation, confirm, agree, category)
- **`SessionService`** — `ConcurrentHashMap<Long, UserSession>` for in-memory session management
- **`DbService`** interface + **`MockDbService`** — logs submissions; swap to real implementation for DB support
- **`BotMessages`** — Korean string constants for all bot responses

### Package Layout

Base package: `com.unknown.csbot`

| Package | Purpose |
|---------|---------|
| `config/` | `BotConfig` — binds `bot.token` and `bot.username` from `application.properties` |
| `bot/` | `CsBot` — `TelegramLongPollingBot` implementation |
| `enums/` | `MenuState`, `MainMenu` (menu label ↔ state mapping) |
| `model/` | `UserSession`, `RegistrationData`, `IdChangeData`, `InquiryData` |
| `handler/` | `MessageRouter` + all flow handlers |
| `keyboard/` | `KeyboardFactory` |
| `service/` | `SessionService`, `DbService`, `MockDbService` |
| `util/` | `BotMessages` |

## Database

Currently DB is disabled (`DataSourceAutoConfiguration` excluded in application.properties). To enable MySQL:
1. Uncomment the `spring.datasource.*` lines in `application.properties`
2. Remove the `spring.autoconfigure.exclude` line
3. Replace `MockDbService` with a real implementation using `JdbcTemplate` (see powerball's `DataRepository` pattern)

## Configuration

`application.properties` uses environment variables with defaults:
```properties
bot.token=${BOT_TOKEN:your-bot-token}
bot.username=${BOT_USERNAME:your-bot-name}
```
