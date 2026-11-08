# Структура проекта

```
app/                    Android-приложение: Application, MainActivity, экраны, DI-модули
core/model/             KMP (jvm): Money, валюты, форматирование + доменные сущности
                        (Account, Category, Transaction, Budget), IdGenerator
core/domain/            KMP (jvm): интерфейсы репозиториев, use cases, категории по умолчанию
core/data/              KMP (android + jvm для тестов): Room, DAO, репозитории, JSON-бэкап
core/designsystem/      Android library: тема CoShellTheme, цвета, типографика, общие компоненты
docs/                   ARCHITECTURE.md, ROADMAP.md, agents/*.md, samples/ (тестовые файлы форматов)
.github/workflows/      CI
gradle/libs.versions.toml  все версии зависимостей и плагинов
```

Пакеты: `app.coshell.<модуль>` (например `app.coshell.core.model`).

## Куда класть новое
| Что | Куда |
|---|---|
| Доменная модель, value-класс, чистая логика | `core:model` (`commonMain`) |
| Use case, интерфейс репозитория | `core:domain` |
| Room, реализации репозиториев | `core:data/commonMain` |
| Android-специфичное в data (Context, позже Firestore) | `core:data/androidMain` |
| Hilt-модули (связывание интерфейсов с реализациями) | `app/.../di/` |
| Переиспользуемый Compose-компонент | `core:designsystem` |
| Экран | сейчас `app/.../<feature>/`; при росте — модуль `feature:<name>` |

## Экран = 4 файла
`<Name>Route` (достаёт ViewModel) · `<Name>Screen` (stateless, с `@Preview`) ·
`<Name>ViewModel` (`StateFlow<UiState>`) · `<Name>UiState`.

## Новый модуль
1. `include(":path:name")` в `settings.gradle.kts`.
2. `build.gradle.kts` по образцу соседнего модуля того же типа.
3. Обновить этот файл.
