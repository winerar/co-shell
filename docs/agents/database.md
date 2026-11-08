# База данных (Room)

Room — единственный источник истины для UI. Код — `core/data` (KMP: android + jvm).

## Где что
| Что | Где |
|---|---|
| Таблицы (`*Entity`) | `core/data/src/commonMain/.../db/Entities.kt` |
| DAO | `db/Daos.kt` |
| БД, версия схемы | `db/CoShellDatabase.kt` |
| entity ↔ модель | `db/Mappers.kt` (каждое поле покрыто round-trip тестом) |
| Реализации репозиториев | `repository/RoomRepositories.kt` |
| Hilt-провайдеры | `app/.../di/DataModule.kt` (в `core:data` Hilt нет — модуль KMP) |
| Экспорт схем | `core/data/schemas/` — **коммитится** |

## Хранение
- Деньги: `*_minor: Long` + код валюты `TEXT` рядом. Никаких REAL.
- Дата операции: `TEXT` `yyyy-MM-dd` (сортируется как строка). `updated_at` — epoch ms.
- Enum — по имени (`EXPENSE`), не по ordinal: переупорядочивание enum не ломает данные.
- Метки — одна строка через `\u001F`.
- Внешних ключей нет: синхронизация может привезти операцию раньше счёта.
- Удаление — мягкое (`deleted = 1`); DAO-запросы для UI фильтруют `deleted = 0`.
- Локальные правки через репозитории ставят `updated_at = now` (вход для last-write-wins).

## Миграции
1. Изменил entity → увеличь `version` в `CoShellDatabase`.
2. Простые изменения (новая колонка с default, новая таблица) — `autoMigrations = [AutoMigration(from, to)]`.
3. Переименования/удаления — `@RenameColumn`/`@DeleteColumn` в `AutoMigrationSpec` или ручная `Migration`.
4. **Никогда** `fallbackToDestructiveMigration`: это единственная копия данных семьи (до синхронизации, этап 4).
5. Новый JSON в `schemas/` коммитится вместе с изменением.

## Тесты
- `./gradlew :core:data:jvmTest` — настоящий SQLite (bundled driver) в памяти, без эмулятора.
- Новый запрос в DAO → тест в `core/data/src/jvmTest/.../RoomRepositoriesTest.kt`.
- Robolectric не используем: его Android-jar (~150 МБ) не скачивается через прокси облачной песочницы.
