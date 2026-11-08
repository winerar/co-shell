# Текущее состояние (handoff между сессиями)

> Обновляется в конце **каждой** сессии. Коротко: только то, что нужно следующей сессии.
> История решений — в ARCHITECTURE §«Принятые решения», план — в ROADMAP.

**Обновлено:** 2026-11-08 · **Текущий этап:** 1 (локальная версия), в работе

## Где мы
- Каркас проекта готов: `app`, `core:model` (KMP, jvm), `core:designsystem`; Hilt; демо-экран со счетами.
- Модель денег: `Money`, `CurrencyCode`, `Currencies`, `MoneyFormat`, `MoneyParser`.
- Сущности в `core:model`: `Account`, `Category`, `Transaction` (+ `effectOn` для баланса), `SyncMeta`, типизированные id.
- `core:domain` (KMP): `AccountRepository`, `CategoryRepository`, `TransactionRepository`; `balanceOf`,
  `ObserveAccountsWithBalances`, `SaveTransaction` (сверка валют и типа категории). Всего 41 тест.
- CI зелёный: `check` + debug APK в артефактах (GitHub Actions).
- Приложение собрано, но **ещё ни разу не запускалось** на телефоне/эмуляторе.

## Следующие шаги (этап 1)
1. Модуль `core:data`: Room (entities, DAO, `exportSchema` + стратегия миграций), реализации
   репозиториев из `core:domain`, Hilt-модуль; маппинг entity ↔ модель с тестами.
2. Категории по умолчанию (сидинг при первом запуске).
3. Экраны: счета, быстрый ввод операции, список операций; навигация.
4. Экспорт/импорт JSON. Budget — отложен до этапа отчётов (в домене его пока нет).
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
2026-11-08 (2): сущности в `core:model`, модуль `core:domain` с репозиториями, балансами и `SaveTransaction`.
