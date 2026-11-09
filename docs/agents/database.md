# База данных (Room)

Код — `core/data` (KMP: таргеты `android` и `jvm`). Схема — `core/data/src/commonMain/.../db/`.

## Соглашения
- Деньги — `<имя>_minor INTEGER` + колонка валюты (`currency`, `to_currency`…). Время — epoch ms UTC.
- В каждой синхронизируемой таблице: `updated_at`, `updated_by`, `deleted`, `pending_sync`.
- **Удаление мягкое**: `markDeleted` ставит `deleted = 1` (tombstone для синка). Все запросы
  фильтруют `deleted = 0`. Физически строки не удаляем.
- **Без foreign keys**: при синхронизации записи приходят в любом порядке.
- Enum-ы хранятся строкой (`name`), списки (`labels`) — JSON-массивом.
- Entity ≠ доменная модель: маппинг в `repository/Mappers.kt`, наружу отдаём только модели `core:model`.
- DAO-функции — только `suspend` или `Flow` (требование Room KMP).
- Многотабличная запись — `@Transaction`-функция в DAO (пример — `BackupDao.upsertAll`).
- `@Upsert` при конфликте по **уникальному индексу** (не по PK) молча пропускает запись —
  проверяй уникальность сам (см. импорт Wallet, ARCHITECTURE §8).

## Миграции
1. Изменил entity → увеличь `version` в `CoShellDatabase`.
2. `./gradlew :core:data:jvmTest` перегенерирует `core/data/schemas/<version>.json` — **закоммить его**.
3. Добавь `AutoMigration(from, to)` в `@Database` или ручную `Migration`, если auto не справляется
   (переименование, перенос данных).
4. Тест миграции на JVM — `jvmTest/.../MigrationTest.kt` (`MigrationTestHelper` из `room-testing` +
   `BundledSQLiteDriver`): создать старую версию, вставить данные SQL-ом, мигрировать, проверить.
5. `fallbackToDestructiveMigration` **запрещён**: до этапа 4 данные живут только на телефоне.

## Курсы (схема v2)
- `manual_rates` — ручные курсы, синхронизируемая таблица (поля синка, мягкое удаление), **в бэкапе**
  (`manualRates`). `id` = `<дата>:<A>:<B>` (валюты по алфавиту, `manualRateId`): одна запись на пару и
  день в любом направлении — сохранение `ARS → USD` заменяет `USD → ARS` того же дня, и на двух
  телефонах это один документ.
- `downloaded_rates` — кэш загрузки, ключ `(date, from_currency, to_currency)`, плюс `source` и
  `downloaded_at`; не синхронизируется, **не в бэкапе**. Сохранять только используемые валюты:
  `snapshot.keepOnly(repo.usedCurrencies())` (USD остаётся всегда — для кросс-курсов).
- Дата — текст `YYYY-MM-DD`, курс — текст `Rate.toPlainString()`; `REAL` запрещён.

## Тесты
- `core/data/src/jvmTest`: `createInMemoryCoShellDatabase()` — настоящий SQLite в памяти, без эмулятора.
- Flow-запросы — через Turbine (`flow.test { awaitItem() }`).

## Резервная копия (JSON)
- `backup/BackupFormat.kt` — формат файла, **не зависит от схемы Room**. Несовместимое изменение →
  `BackupFile.CURRENT_VERSION++`; новое необязательное поле с дефолтом — без повышения версии.
- Импорт: добавляет/перезаписывает по `id`, остальное не трогает; атомарный; сначала все записи
  проходят проверки доменных моделей, затем одна транзакция. Исключение — **дубли категорий**
  (`backup/CategoryDuplicates.kt`): локальная категория с тем же типом, названием (без регистра) и
  папкой, но другим `id` (засеянные по умолчанию на новом телефоне) мягко удаляется, её операции,
  бюджеты и подкатегории переходят к категории из копии. Разбор и сборка JSON — на `Dispatchers.Default`.
- UI: `app/.../backup/` — файл выбирает системный диалог (Storage Access Framework), чтение/запись —
  `DocumentStorage` (интерфейс; на Android — `ContentResolverDocumentStorage`). Перед импортом — подтверждение.
