# Текущее состояние (handoff между сессиями)

> Обновляется в конце **каждой** сессии. Коротко: только то, что нужно следующей сессии.
> История решений — в ARCHITECTURE §«Принятые решения», план — в ROADMAP.

**Обновлено:** 2026-11-08 · **Текущий этап:** 1 (локальная версия) — слой данных готов, дальше экраны

## Где мы
- Модули: `app`, `core:model`, `core:domain`, `core:data`, `core:designsystem` (структура — `docs/agents/project-structure.md`).
- `core:model`: Money/валюты + сущности Account, Category, Transaction, Budget, `IdGenerator`.
- `core:domain`: интерфейсы репозиториев, `SaveTransactionUseCase` (проверка валют/категорий),
  `DeleteAccount/CategoryUseCase` (используемое не удаляется — только архив), `SeedDefaultCategoriesUseCase`.
- `core:data`: Room (схема v1 в `core/data/schemas/`), мягкое удаление, баланс счетов SQL-агрегатом,
  JSON-бэкап (`JsonBackupRepository`). Правила — `docs/agents/database.md`.
- `app`: Hilt `DataModule`; при первом запуске создаются категории по умолчанию; главный экран читает
  настоящие счета из Room (пока пусто — добавлять счета негде).
- Тесты: model 24, domain 9, data 11, app 1 — всё зелёное; `check assembleDebug` локально проходит.
- Формат CSV Wallet разобран на реальном файле → ARCHITECTURE §8, синтетический образец
  `docs/samples/wallet-export-sample.csv`.
- Приложение **всё ещё ни разу не запускалось** на телефоне/эмуляторе.

## Следующие шаги (этап 1)
1. Навигация (Navigation Compose, type-safe routes; зависимость уже в version catalog) + нижняя панель.
2. Экраны счетов: список с балансами, создание/редактирование (валюта, тип, начальный баланс), архив.
3. Быстрый ввод операции: сумма (`MoneyParser`) → категория → счёт → сохранить через `SaveTransactionUseCase`;
   режим перевода с двумя суммами.
4. Список операций с группировкой по дням, редактирование, удаление.
5. Категории: список и редактирование.
6. Настройки: экспорт/импорт JSON через Storage Access Framework (логика готова в `BackupRepository`).
7. Первый запуск APK на эмуляторе или телефоне.

## Открытые вопросы к владельцу
- Нет. (Закрыты 2026-11-08: живём в Аргентине, доход в AMD — см. `owner.md`; метка в Wallet всегда одна.)

## Окружение
- Облачное окружение **co-shell**: разрешён `dl.google.com`, setup-скрипт ставит Android SDK
  (platform 37.0, build-tools 36.0.0) и пишет `local.properties` — **проверено**, работает.
- Сессию создавать **сразу в окружении co-shell**: при запуске в Default скрипт не выполняется
  (тогда SDK — вручную по `docs/agents/build-and-test.md`, «Установка SDK вручную»).
- Maven Central из песочницы иногда отвечает 429 на большие файлы (так отпал Robolectric) — повторить позже.
- Версии: Gradle 9.9.0, AGP 9.5.1, Kotlin 2.4.21, KSP 2.3.13, Hilt 2.60.1, Compose BOM 2026.09.00,
  Room 2.8.5, kotlinx-datetime 0.8.0.

## Последняя сессия
2026-11-08 (2): этап 1 — домен, Room-слой с тестами, JSON-бэкап, подключение к app; разобран формат CSV Wallet.
