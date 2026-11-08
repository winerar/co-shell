# Текущее состояние (handoff между сессиями)

> Обновляется в конце **каждой** сессии. Коротко: только то, что нужно следующей сессии.
> История решений — в ARCHITECTURE §«Принятые решения», план — в ROADMAP.

**Обновлено:** 2026-11-08 · **Текущий этап:** 1 (локальная версия), в работе

## Где мы
- Каркас проекта готов: `app`, `core:model` (KMP, jvm), `core:designsystem`; Hilt; демо-экран со счетами.
- Модель денег: `Money`, `CurrencyCode`, `Currencies`, `MoneyFormat`, `MoneyParser`.
- Сущности в `core:model`: `Account`, `Category`, `Transaction` (+ `effectOn` для баланса), `SyncMeta`, типизированные id.
- `core:domain` (KMP): `AccountRepository`, `CategoryRepository`, `TransactionRepository`; `balanceOf`,
  `ObserveAccountsWithBalances`, `SaveTransaction` (сверка валют и типа категории).
- `core:data` (KMP android + jvm): Room v1 (accounts, categories, transactions), DAO, маппинг,
  репозитории; Hilt-провайдеры в `app/di/DataModule.kt`. Правила БД — `docs/agents/database.md`.
- Тестов всего 52; БД тестируется на JVM (bundled SQLite).
- UI пока не использует репозитории (демо-экран на старых данных).
- CI зелёный: `check` + debug APK в артефактах (GitHub Actions).
- Приложение собрано, но **ещё ни разу не запускалось** на телефоне/эмуляторе.

## Следующие шаги (этап 1)
1. Категории по умолчанию (сидинг при первом запуске, через `CategoryRepository`).
2. Навигация (Navigation Compose) + экран счетов на `ObserveAccountsWithBalances` (заменить демо),
   создание/редактирование счёта.
3. Быстрый ввод операции (`SaveTransaction`), список операций по дням, переводы/обмен.
4. Экспорт/импорт JSON. Budget — отложен до этапа 6 (в домене его пока нет).
5. Впервые запустить APK на телефоне (скачать из артефактов CI).
Модель данных — ARCHITECTURE §5 (уже включает payee, labels, importExternalId для Wallet).

## Открытые вопросы к владельцу
- Страна проживания (доступность Firebase, запасной источник курсов) — спросить до этапа 2.
- Образец CSV-экспорта из Wallet (заголовок + 3–5 обезличенных строк) — нужен до этапа 5.

## Окружение
- Облачное окружение **co-shell**: разрешён `dl.google.com`, setup-скрипт ставит Android SDK
  (platform 37.0, build-tools 36.0.0), пишет `local.properties` и заранее собирает проект (прогрев Gradle,
  лог — `/tmp/gradle-warmup.log`). Скрипт ещё не проверен в новой сессии — если SDK нет,
  см. `docs/agents/build-and-test.md`.
- Сессию создавать **сразу в окружении co-shell**: при запуске в Default скрипт не выполняется.
- Версии: Gradle 9.9.0, AGP 9.5.1, Kotlin 2.4.21, KSP 2.3.13, Hilt 2.60.1, Compose BOM 2026.09.00.

## Последняя сессия
2026-11-08 (2): сущности, `core:domain` (репозитории, балансы, `SaveTransaction`), `core:data` на Room (KMP).
