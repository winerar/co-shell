# Структура проекта

```
app/                    Android-приложение: Application, MainActivity, экраны, DI-модули
core/model/             KMP (jvm): Money, валюты, форматирование + доменные сущности
                        (Account, Category, Transaction, Budget), IdGenerator
core/domain/            KMP (jvm): интерфейсы репозиториев, use cases, категории по умолчанию
core/data/              KMP (android + jvm для тестов): Room, DAO, репозитории, JSON-бэкап
core/designsystem/      Android library: тема CoShellTheme, цвета, типографика, общие компоненты
scripts/                служебные скрипты (конвертация иконок)
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
| Переиспользуемый Compose-компонент без строк приложения | `core:designsystem` |
| Общий блок экранов со строками приложения (поля и кнопки форм, диалоги) | `app/.../ui/<тема>/` (напр. `ui/form/`) |
| Экран | `app/.../<feature>/<screen>/` (см. ниже); при росте — модуль `feature:<name>` |
| Маршрут навигации | `app/.../navigation/Destinations.kt` (`@Serializable`), граф и нижняя панель — `CoShellNavHost.kt` |
| Иконка | `core:designsystem` res/drawable, правила — `code-style.md` §«Иконки» |

## Файлы: маленькие и по одной теме
Цель — чтобы человек или агент открыл **один короткий файл** и понял его целиком.
- Один файл — один логический блок (экран, компонент, валидация, маппинг). Ориентир — **до ~200 строк**;
  больше — дели. Имя файла = главная сущность в нём.
- В начале файла/класса — KDoc на 1–3 строки: что это и где соседние части.
- Чистая логика (валидация, расчёты, построение списков) — в отдельный файл функцией без Android,
  чтобы тестировать без ViewModel.

## Экран = пакет `<feature>/<screen>/`
Образец — `transactions/edit/`:
| Файл | Что внутри |
|---|---|
| `<Name>Route.kt` | достаёт ViewModel, `<Name>Actions` (все лямбды экрана) |
| `<Name>Screen.kt` | stateless-раскладка блоков, диалоги; без деталей блоков |
| `<Name>UiState.kt` | состояние с готовыми строками + вычисляемые свойства |
| `<Name>ViewModel.kt` | загрузка, приём ввода, сохранение; логику делегирует |
| `<Name>Previews.kt` | `@Preview` в `CoShellTheme` |
| `<Тема>.kt` (напр. `TransactionForm.kt`) | чистая логика экрана (валидация и т. п.) |
| `components/<Block>.kt` | по файлу на блок формы (`AmountField`, `DateChips`…) |
Так устроены все экраны: `accounts/list|edit`, `categories/list|edit`, `transactions/list|edit`.
Общее для фичи (напр. `AccountTypeLabel.kt`) лежит в `<feature>/`.

Аргументы маршрута ViewModel читает из `SavedStateHandle` по имени свойства (не `toRoute()` — так
работает в unit-тестах без Android). Общее для нескольких экранов — на уровень `<feature>/`
(напр. `transactions/AddTransactionFab.kt`), в `ui/` или в `core:designsystem`.

## Переиспользование (пожелание владельца)
Если блок повторяется на двух экранах или явно понадобится ещё где-то — делай его универсальным
компонентом с параметрами, а не копируй. Готовые общие блоки форм — `ui/form/`: `EditTopBar`,
`NameField`, `FormSection`/`FormHint`, `EditFooter` (Сохранить/Архив/Удалить), `DeleteButton`,
`ConfirmDeleteDialog`, `DeleteBlockedDialog`. Сначала проверь там, потом пиши новое.

## Новый модуль
1. `include(":path:name")` в `settings.gradle.kts`.
2. `build.gradle.kts` по образцу соседнего модуля того же типа.
3. Обновить этот файл.
