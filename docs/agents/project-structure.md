# Структура проекта

```
app/                    Android-приложение: Application, MainActivity, экраны, DI-модули
core/model/             KMP (jvm): Money, валюты, форматирование + доменные сущности
                        (Account, Category, Transaction, Budget), IdGenerator
core/domain/            KMP (jvm): интерфейсы репозиториев, use cases, категории по умолчанию
core/data/              KMP (android + jvm для тестов): Room, DAO, репозитории, JSON-бэкап,
                        sync/ (SyncEngine), remote/ (Firestore REST), auth/ (Firebase Auth REST), family/
core/designsystem/      Android library: тема CoShellTheme, токены, шрифт цифр, общие компоненты (`design-system.md`)
scripts/                служебные скрипты (check.sh, иконки, файлы обновлений, отзывы → issues) + test_*.py
firebase/               firestore.rules (вставляются в консоль вручную), rules-test/ (тесты правил, Node)
docs/                   ARCHITECTURE.md, ROADMAP.md, agents/*.md, setup/ (шаги для владельца),
                        whats-new.md (текст «Что нового»), samples/ (тестовые файлы форматов)
.github/workflows/      CI (ci.yml), release-ключ (release-key.yml), отзывы → issues (feedback.yml),
                        отпечатки ключей для входа Google (signing-fingerprint.yml)
gradle/libs.versions.toml  все версии зависимостей и плагинов
```

Пакеты: `app.coshell.<модуль>` (например `app.coshell.core.model`).

## Куда класть новое
| Что | Куда |
|---|---|
| Доменная модель, value-класс, чистая логика | `core:model` (`commonMain`) |
| Use case, интерфейс репозитория | `core:domain` |
| Room, реализации репозиториев | `core:data/commonMain` |
| Android-специфичное в data (Context) | `core:data/androidMain`; HTTP-клиент и Credential Manager — `app/.../family/data/` |
| Hilt-модули (связывание интерфейсов с реализациями) | `app/.../di/` |
| Переиспользуемый Compose-компонент без строк приложения | `core:designsystem` |
| Общий блок экранов со строками приложения | `app/.../ui/<тема>/`: `ui/form/` (формы, диалоги), `ui/list/` (строки списков) |
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
Так устроены все экраны: `accounts/list|edit`, `categories/list|edit`, `transactions/list|edit`, `backup/`, `update/`,
`feedback/` (+ `data/`: отправка в Firestore REST, сведения о телефоне; решения — `core:domain/feedback/`).
`shake/` — не экран: `ShakeEffect` (акселерометр → вибрация → колбэк, подключён в `CoShellNavHost`),
`ShakeSettingsViewModel` (выключатель, им пользуются `CoShellNavHost` и «Ещё»), `data/PrefsShakeSettings`;
распознавание толчков — `core:domain/shake/ShakeDetector`.
`theme/` — не экран: `ThemeSettingsViewModel` (читают `MainActivity` и «Ещё»), `components/ThemeModeRow`,
`data/PrefsThemeSettings`; режим `ThemeMode` — `core:domain/settings/ThemeSettings.kt`.
`settings/` — экран «Настройки» (#30); `more/` — меню разделов «Ещё».
Курсы валют: тип `Rate` и `ExchangeRate` — `core:model`; поиск курса и пересчёт (`RateBook`,
`CurrencyConverter`) и загрузка с перебором источников (`RatesSource`, `RatesProvider`) — `core:domain/rates/`;
источники и разбор JSON — `core:data/rates/` (`CurrencyApiSource`, `OpenErApiSource`, порт `HttpGet`);
HTTP на Android — `app/.../rates/data/UrlConnectionHttpGet`, DI — `di/RatesModule`; ежедневная загрузка —
`RefreshRatesUseCase` (`core:domain/rates/`) и `app/.../rates/work/` (`RatesRefreshWorker`, `RatesRefreshScheduler`,
`WorkManagerRatesRefresher` — «Обновить сейчас» за интерфейсом `rates/RatesRefresher`;
WorkManager создаётся через `CoShellApp.workManagerConfiguration`, стандартный инициализатор убран в манифесте). Хранение — контракт
`ExchangeRateRepository` (`core:domain/rates/`), Room — `db/RateEntities.kt`, `db/RateDao.kt`,
`repository/RoomExchangeRateRepository.kt`; DI — `di/DataModule`. Экран «Ещё → Курсы валют» — `rates/list/`
(курсы на сегодня — `core:domain/rates/CurrentRates.kt`, диалог ручного курса — `ManualRateForm.kt` +
`components/ManualRateDialog.kt`).
Валюты отчётов (первая — основная): контракт и правила списка — `core:domain/settings/ReportCurrencies.kt`,
хранение — `app/.../currencies/data/PrefsReportCurrencySettings`, экран «Ещё → Валюты отчётов» — `currencies/list/`.
Общий баланс «Обзора» — `core:domain/rates/BalanceTotals.kt` + `home/` (карточка — `home/components/TotalCard`).
Общие форматтеры — `app/.../ui/format/` (`formatRate`, `formatDayMonth`); выбор даты `DateChips` — `ui/form/`.
У `update/` ещё `data/` (version.json, загрузка, SHA-256), `install/` (PackageInstaller) и `startup/`
(диалоги при запуске, подключены в `CoShellNavHost`); решения — в `core:domain/update/`.
Общее для фичи (напр. `AccountTypeLabel.kt`) лежит в `<feature>/`.

Аргументы маршрута ViewModel читает из `SavedStateHandle` по имени свойства (не `toRoute()` — так
работает в unit-тестах без Android). Общее для нескольких экранов — на уровень `<feature>/`
(напр. `transactions/AddTransactionFab.kt`), в `ui/` или в `core:designsystem`.

## Переиспользование (пожелание владельца)
Если блок повторяется на двух экранах или явно понадобится ещё где-то — делай его универсальным
компонентом с параметрами, а не копируй. Шторка-редактор — `ui/sheet/` (`EditSheetHost`, `EditSheetLayout`
с закреплённой «Сохранить»), заголовок вкладки — `ui/top/TabTopBar`, «Отменить» — `undo/UndoCenter`.
Готовые общие блоки форм — `ui/form/`:
`NameField`, `FormSection`/`FormHint`, `EditFooter` (Архив/Удалить), `DeleteButton`,
`ConfirmDialog` (+ `ConfirmDeleteDialog`, `DeleteBlockedDialog`), `MessageCard` (итог действия); строки меню — `ui/list/ActionRow`, строка-переключатель настройки — `ui/list/SwitchRow`.
Значок категории — `core:designsystem` `category/`: `CategoryBadge` (иконка в круге цвета категории),
`IconBadge` (то же для любой иконки), набор `CategoryIcons` (ключ → иконка) и `CategoryPalette`.
Сначала проверь там, потом пиши новое.

## Новый модуль
1. `include(":path:name")` в `settings.gradle.kts`.
2. `build.gradle.kts` по образцу соседнего модуля того же типа.
3. Обновить этот файл.
