# Структура проекта

```
app/                    Android-приложение: Application, MainActivity, экраны, DI-модули
core/model/             KMP-модуль (пока только jvm): Money, валюты, сущности (Account, Category, Transaction), id
core/domain/            KMP-модуль: интерфейсы репозиториев (`repository/`), use case (`usecase/`), расчёт балансов
core/designsystem/      Android library: тема CoShellTheme, цвета, типографика, общие компоненты
docs/                   ARCHITECTURE.md, ROADMAP.md, agents/*.md
.github/workflows/      CI
gradle/libs.versions.toml  все версии зависимостей и плагинов
```

Пакеты: `app.coshell.<модуль>` (например `app.coshell.core.model`).

## Куда класть новое
| Что | Куда |
|---|---|
| Доменная модель, value-класс, чистая логика | `core:model` (`commonMain`) |
| Use case, интерфейс репозитория | `core:domain` (`usecase/`, `repository/`) |
| Room, Firestore, реализации репозиториев | `core:data` (Android library, этап 1) |
| Переиспользуемый Compose-компонент | `core:designsystem` |
| Экран | сейчас `app/.../<feature>/`; при росте — модуль `feature:<name>` |

## Экран = 4 файла
`<Name>Route` (достаёт ViewModel) · `<Name>Screen` (stateless, с `@Preview`) ·
`<Name>ViewModel` (`StateFlow<UiState>`) · `<Name>UiState`.

## Новый модуль
1. `include(":path:name")` в `settings.gradle.kts`.
2. `build.gradle.kts` по образцу соседнего модуля того же типа.
3. Обновить этот файл.
