# Карта фич: где что лежит

Выделено из `project-structure.md` (правила структуры — там). По абзацу на фичу: модель, логика, Room, экраны.
Добавил фичу или переложил файлы — обнови абзац.

`shake/` — не экран: `ShakeEffect` (акселерометр → вибрация → колбэк, подключён в `CoShellNavHost`),
`ShakeSettingsViewModel` (выключатель, им пользуются `CoShellNavHost` и «Ещё»), `data/PrefsShakeSettings`;
распознавание толчков — `core:domain/shake/ShakeDetector`.
`theme/` — не экран: `ThemeSettingsViewModel` (читают `MainActivity` и «Ещё»), `components/ThemeModePicker`, `PaletteGrid`, `AppearanceSummary`,
`data/PrefsThemeSettings`; режим `ThemeMode` — `core:domain/settings/ThemeSettings.kt`.
`appearance/` — «Внешний вид» (тема, палитра, встряхивание; бывшие «Настройки», #155); `more/` — «Ещё»: карточка версии
(`components/VersionCard`) и группы строк (`components/MoreSections`), данные строк — `navigation/MoreRoute`.
Бюджеты (#167): модель `core:model/Budget.kt`, логика — `core:domain/budgets/` (`BudgetPlan` — лимит месяца,
`SaveBudgetUseCase`, `RemoveBudgetUseCase`); Room — `BudgetDao`, `RoomBudgetRepository`. Расчёт (#182): `BudgetPeriod` (месяц по дню
начала), `BudgetLedger` (потрачено, среднее за 3 мес.), `BudgetProgress` (темп, состояние, прогноз), `BudgetCalculator` (перенос, итог месяца). Экраны (#103) — `app/.../budgets/`: `BudgetSource` (общий снимок: суммы по дням из SQL, #262),
`list/` («Бюджеты»: `BudgetBoard.kt` — конверты, итог, фильтр; шестерёнка — день начала месяца, #213), `detail/` (экран конверта #183:
`BudgetDetailBoard.kt` — шапка и категории папки; операции периода — `BudgetSource.observeEnvelope`), `edit/` (редактор лимита,
`BudgetForm.kt`); вход — «Ещё».
Отчёты (#187, без экранов): `core:domain/reports/` — `ReportDay` (сумма дня) + `ReportRepository`, `ReportLedger` (курс дня, возвраты),
`spendingBreakdown` (папки, доли, «Другое»), `monthlySeries`, `periodComparison`; Room — `ReportDao`, `RoomReportRepository`.
Поиск и фильтры «Операций» (#141, вариант А): разбор суммы `=4500`/`>10000`/`3000-5000` — `core:domain/search/AmountQuery`,
периоды со стрелками — `search/PeriodRange`, итог под фильтрами (4А) — `search/FilterTotal`; Room — `TransactionFilter`
(`accountIds`, `categoryIds`, `author`) → `TransactionDao.observe`. Экран — `transactions/list/`: `ListFilter` (что выбрано, что идёт в Room),
`FilterChoices` (что предлагают шторки, переключение папок), `FilterLabels` (подписи чипов), `FilterTotalText`; чипы и итог —
`components/FilterBar`, `FilterTotalCard`; шторки — `components/CategoriesSheet`, `PeriodSheet`, `AccountsSheet` (обёртка — `FilterSheet`).
С фильтром грузятся все подходящие строки (для итога), список показывается страницами в памяти.
Шаблоны операций (#169, #104): модель `core:model/TransactionTemplate.kt`, логика — `core:domain/templates/`
(`SaveTemplateUseCase`, `SaveAsTemplateUseCase`, `draftFrom` → `TransactionDraft` с `gaps`, `TemplateUsage` — счётчик «Часто»),
Room — `db/TemplateDao.kt`, `repository/RoomTemplateRepository.kt`. Экраны — `app/.../templates/`: `TemplateCatalog` (общий источник),
`components/` (чип, полоса), `strip/` (полоса в шторке «+»), `list/` («Все шаблоны»), `edit/` (редактор), `data/PrefsTemplateUsage`;
в шторке операции — `transactions/edit/EntryTemplates.kt` и `components/TemplateBits.kt` (пилюля, «Сохранить как шаблон»).
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
хранение — общие настройки семьи `core:data/repository/RoomFamilySettings` (там же день начала месяца, `MonthStartSettings`;
перенос старого списка с телефона — `app/.../currencies/data/LegacyReportCurrencies`), экран «Ещё → Валюты отчётов» — `currencies/list/`.
Общий баланс «Обзора» — `core:domain/rates/BalanceTotals.kt` + `home/` (карточка — `home/components/TotalCard`);
траты/доход месяца и траты по дням — `core:domain/rates/MonthSpending.kt`, состояние «Обзора» — `home/HomeState.kt`,
две плитки (траты за месяц; «Внимание» при проблеме с курсом, иначе «Бюджет · N дней» при лимитах, иначе «Без категории») — `home/components/HomeTiles`; бюджет на «Обзоре» (#184, `BudHome`) — `home/budget/` (`HomeBudget` — итог месяца и «горящие» конверты, свой ViewModel), плитка `components/BudgetTile`, блок `components/HotBudgets`; последние операции — `home/components/RecentCard` (строки — из `transactions/list`).
Плашка ошибки синка — `home/sync/` (ViewModel, правила показа) + `home/components/SyncBanner`; причина — `core:data/sync/SyncIssue`.
«Резервная копия» — `app/.../backup/` (герой-кольцо `components/BackupHero`, автокопия `auto/`, правила `AutoBackupPolicy`).
«Стереть все данные» — `app/.../wipe/` (экран, копия `WipeBackup`) + `core:domain/DataWipe.kt` + `core:data/db/WipeDao`.
Общие форматтеры — `app/.../ui/format/` (`formatRate`, `formatDayMonth`); выбор даты `DateChips` — `ui/form/`.
У `update/` ещё `data/` (version.json, загрузка, SHA-256), `install/` (PackageInstaller, «вручную» — #217), `log/` (#216), `components/` (кольцо, карточки версий «Сейчас → Новая», «Ваша версия» — #268), `UpdateController`
(синглтон: проверка/скачивание/установка, живёт дольше экрана) и `notice/` (пилюля над панелью, точка на «Ещё»,
«Что нового»; подключены в `CoShellNavHost`); решения — в `core:domain/update/`. Сбои — `crash/` (#218); тех. данные отзыва — `feedback/data/AndroidDiagnosticsProvider`; целостность — `core:data/integrity/`;
общее для фичи (напр. `AccountTypeLabel.kt`) лежит в `<feature>/`.
Импорт из Wallet (этап 5): разбор CSV — `core:domain/importer/csv/CsvReader` (общий RFC 4180), формат Wallet —
`core:domain/importer/wallet/` (`WalletCsvParser` → `WalletRecord`/ошибки с номером строки, `WalletTransferMatcher`,
`WalletSummary`); образец для тестов — `WalletSamples` (копия `docs/samples/wallet-export-sample.csv`).
Запись импорта: `core:domain/importer/ImportRepository` (+ `WalletImportPlanner`, `WalletImportUseCase`),
`core:data/db/ImportDao` + `repository/RoomImportRepository`. Экран-мастер — `app/.../importer/wallet/`
(черновик выбора — `WalletImportDraft`, шаги — `components/*Step.kt`), копия перед импортом — `importer/PreImportBackup`.
