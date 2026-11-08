# Архитектура co-shell (черновик)

## 1. Ключевые требования, определяющие архитектуру
1. **Совместность** — несколько членов семьи видят и правят один бюджет с разных телефонов.
2. **Offline-first** — трату в магазине без связи нужно записать сразу, синхронизировать потом.
3. **Конфликты** — двое правят одно и то же офлайн → нужна предсказуемая стратегия слияния.
4. **Деньги** — никаких `Double`: суммы хранятся в минимальных единицах (`Long` копеек) + код валюты.
5. **Приватность** — финансовые данные семьи; доступ только участникам «семьи».

## 2. Стек
| Слой | Выбор | Почему |
|---|---|---|
| Язык | Kotlin 2.x | стандарт для Android |
| UI | Jetpack Compose + Material 3 | декларативный UI, меньше кода |
| Навигация | Navigation Compose (type-safe routes) | официальная |
| DI | Hilt | стандарт, хорошо дружит с ViewModel/WorkManager |
| Асинхронность | Coroutines + Flow | реактивные данные из БД в UI |
| Локальная БД | Room (SQLite) | источник истины на устройстве |
| Фоновая синхронизация | WorkManager | переживает перезапуски, учитывает сеть |
| Бэкенд/синк | **открытый вопрос, см. §5** | |
| Настройки | DataStore | |
| Графики | Vico | Compose-нативные графики |
| Тесты | JUnit5, Turbine, MockK, Room in-memory, Compose UI tests | |
| Сборка | Gradle KTS + version catalog + convention plugins | |
| CI | GitHub Actions: lint, detekt, unit-тесты, сборка APK | |

minSdk 26, targetSdk — актуальный.

## 3. Архитектурный стиль
**Clean Architecture (облегчённая) + MVVM с однонаправленным потоком данных (UDF).**

```
UI (Compose screen)
   ↑ state (StateFlow<UiState>)     ↓ events (onAction)
ViewModel
   ↓
UseCase (domain, чистый Kotlin, только там, где есть логика)
   ↓
Repository (interface в domain, реализация в data)
   ↓                       ↓
Room (источник истины)   SyncEngine ⇄ Remote API
```

- UI читает **только из Room** через Flow. Сеть никогда не пишет в UI напрямую.
- Запись: сначала в Room (с пометкой `pendingSync`), затем WorkManager отправляет на сервер.
- Domain-слой не знает об Android — легко тестируется.

### Модули
```
:app                      — точка входа, навигация, DI-граф
:core:model               — доменные модели (Money, Transaction, Category…)
:core:domain              — use cases, интерфейсы репозиториев
:core:data                — реализации репозиториев
:core:database            — Room: entities, DAO, миграции
:core:sync                — SyncEngine, WorkManager workers
:core:network             — клиент бэкенда
:core:designsystem        — тема, общие Compose-компоненты
:feature:auth             — вход, создание/присоединение к семье
:feature:transactions     — список и ввод операций
:feature:accounts         — счета/кошельки
:feature:categories
:feature:budgets          — лимиты по категориям на месяц
:feature:reports          — аналитика, графики
:feature:settings
```
Начинаем с меньшего числа модулей (`app`, `core:*` объединённые) и дробим по мере роста — не стоит переусложнять MVP.

## 4. Модель данных (ядро)
- **Family** — id, name, baseCurrency
- **Member** — userId, familyId, role (owner / member), displayName
- **Account** — кошелёк/карта/наличные: id, familyId, name, currency, ownerMemberId?, isShared
- **Category** — id, familyId, name, icon, color, type (expense/income), parentId?
- **Transaction** — id (UUID, генерируется на клиенте), familyId, accountId, categoryId,
  amountMinor: Long, currency, type (expense/income/transfer), date, note, authorMemberId,
  transferAccountId?
- **Budget** — familyId, categoryId, month, limitMinor
- **RecurringRule** (позже) — шаблон регулярных платежей

Служебные поля всех синхронизируемых сущностей: `updatedAt`, `updatedBy`, `deleted` (soft delete / tombstone), `version`.

**Конфликты:** last-write-wins на уровне записи по серверному времени — для бюджета этого достаточно (одну трату редко правят двое одновременно). Удаления — через tombstones, чтобы не «воскресали».

## 5. Открытый вопрос: бэкенд синхронизации
| Вариант | Плюсы | Минусы |
|---|---|---|
| **A. Firebase (Auth + Firestore)** | минимум своего бэкенда, офлайн-кэш и realtime из коробки, security rules | vendor lock-in; доступность сервисов Google зависит от региона |
| **B. Supabase (Postgres + Auth + RLS)** | SQL, открытый код, можно self-host | синк офлайн придётся писать самим |
| **C. Свой бэкенд (Ktor + Postgres)** | полный контроль, Kotlin везде | больше всего работы и инфраструктуры |
| **D. Только локально + экспорт/импорт** | проще всего | нет совместности — противоречит идее |

**Рекомендация:** Room — источник истины + свой тонкий `SyncEngine` за интерфейсом `RemoteDataSource`.
Первая реализация — **Firebase** (быстрее всего до рабочей совместной версии). Благодаря
интерфейсу бэкенд можно позже заменить на Supabase/свой без переписывания UI и domain.
Если Google-сервисы в вашем регионе недоступны — сразу берём Supabase или свой Ktor.
