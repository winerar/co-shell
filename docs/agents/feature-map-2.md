# Карта фич: продолжение

Часть `feature-map.md` (лимит ~120 строк).

`components/ManualRateDialog.kt`; вход в конвертер — строка `ConverterEntry` сверху).
Конвертер (#319, доски `Conv2*`, «ок» 2026-11-18) — `app/.../converter/`: шторка `ConverterSheet.kt` (маршрут `ConverterDestination`, окно — `dialog<>` в `CoShellNavHost`), `components/ConverterPair.kt` («Отдаю / Получаю» на `CoShellAmountSlot`) и `ConverterAll.kt` («Все валюты»), `ConverterViewModel` + чистая логика `ConverterInput.kt`/`ConverterMath.kt`, вход — `ConverterEntry` (карточка «Обзора» = `HomeCard.CONVERTER`, строка «Ещё → Деньги», строка на «Курсах»). Режим и последняя пара — `ConverterSettings` (`core:domain/settings`, `settings/PrefsConverterSettings`, на этом телефоне); сумма не запоминается.
Валюты отчётов (первая — основная): контракт и правила списка — `core:domain/settings/ReportCurrencies.kt`,
хранение — общие настройки семьи `core:data/repository/RoomFamilySettings` (там же день начала месяца, `MonthStartSettings`;
перенос старого списка с телефона — `app/.../currencies/data/LegacyReportCurrencies`), экран «Ещё → Валюты отчётов» — `currencies/list/`.
Общий баланс «Обзора» — `core:domain/rates/BalanceTotals.kt` + `home/` (карточка — `home/components/TotalCard`);
траты/доход месяца и траты по дням — `core:domain/rates/MonthSpending.kt`, состояние «Обзора» — `home/HomeState.kt`,
две плитки (траты за месяц; «Внимание» при проблеме с курсом, иначе «Бюджет · N дней» при лимитах, иначе «Без категории») — `home/components/HomeTiles`; бюджет на «Обзоре» (#184, `BudHome`) — `home/budget/` (`HomeBudget` — итог месяца и «горящие» конверты, свой ViewModel), плитка `components/BudgetTile`, блок `components/HotBudgets`; последние операции — `home/components/RecentCard` (строки — из `transactions/list`).
Плашка ошибки синка — `home/sync/` (ViewModel, правила показа) + `home/components/SyncBanner`; причина — `core:data/sync/SyncIssue`.
«Резервная копия» — `app/.../backup/` (герой-кольцо `components/BackupHero`, автокопия `auto/`, правила `AutoBackupPolicy`).
«Стереть все данные» — `app/.../wipe/` (экран, копия `WipeBackup`) + `core:domain/DataWipe.kt` + `core:data/db/WipeDao`.
Общие форматтеры — `app/.../ui/format/` (`formatRate`, `formatDayMonth`); выбор даты `DateChips` — `ui/form/`. Ленты цвета и иконок, ячейки выбора, сумма на клавиатуре, шапка, меню, календари — компоненты `core:designsystem` (`Strips.kt`, `AmountSlot.kt`, `TopBar.kt`, `DropdownMenu.kt`, `DatePickers.kt`; #367).
У `update/` ещё `data/` (version.json, загрузка, SHA-256), `install/` (PackageInstaller, «вручную» — #217), `log/` (#216), `components/` (кольцо, карточки версий «Сейчас → Новая», «Ваша версия» — #268), `past/` («Прошлые версии» из `releases.json` — #284), `UpdateController`
(синглтон: проверка/скачивание/установка, живёт дольше экрана) и `notice/` (пилюля над панелью, точка на «Ещё»,
«Что нового»; подключены в `CoShellNavHost`); решения — в `core:domain/update/`. Сбои — `crash/` (#218); тех. данные отзыва — `feedback/data/AndroidDiagnosticsProvider`; целостность — `core:data/integrity/`;
общее для фичи (напр. `AccountTypeLabel.kt`) лежит в `<feature>/`.
Импорт из Wallet (этап 5): разбор CSV — `core:domain/importer/csv/CsvReader` (общий RFC 4180), формат Wallet —
`core:domain/importer/wallet/` (`WalletCsvParser` → `WalletRecord`/ошибки с номером строки, `WalletTransferMatcher`,
`WalletSummary`); образец для тестов — `WalletSamples` (копия `docs/samples/wallet-export-sample.csv`).
Запись импорта: `core:domain/importer/ImportRepository` (+ `WalletImportPlanner`, `WalletImportUseCase`),
`core:data/db/ImportDao` + `repository/RoomImportRepository`. Экран-мастер — `app/.../importer/wallet/`
(черновик выбора — `WalletImportDraft`, шаги — `components/*Step.kt`), копия перед импортом — `importer/PreImportBackup`.
Несовместимая версия у второго участника (#334): `core:data/sync/VersionGate.kt` (`peer_schema` пишет `SyncEngine` при pull), замок ввода — `app/.../gate/` (`InputLock` + `LocalInputLock.guard`, окно — `InputLockNote.kt`), плашка — `home/components/VersionGateBanner`, «+» с замком — `CoShellBottomBar(addLocked)`; провод — `CoShellNavHost` → `MainShell`.
Экспорт CSV (#204, экран — после «ок»): `core:domain/export/CsvExport` — файл «;», суммы с запятой, BOM, фильтр периода и счетов.
Ручной порядок (#338, доски `Fb338Acc`/`Fb338Cat`): `core:domain/Reorder` (номера, шаг 1024) + `order/ManualOrder` (первое перетаскивание
нумерует весь список как на экране, вложение и вынос категории) + `ReorderUseCase`; режим «Порядок» — ⇅ в шапке «Счетов» и «Категорий»
(`accounts/list/components/AccountsOrder`, `categories/list/components/CategoriesOrder` + `CategoryOrderRows`), каркас — `ui/order/OrderScreen`.
Корректировка баланса из шторки счёта (2026-11-20, опрос: «А · плитка в «Деньги»», запись при «Сохранить»): `core:domain/AdjustBalanceUseCase` (разница
реальный − текущий → доход/расход в «Корректировка баланса», дата — сейчас; та же разница = 0 — ничего), плитка «Баланс сейчас» и шторка
`accounts/edit/components/ActualBalanceField` (`MoneySection`), состояние — `AccountEditUiState.actualBalance/adjustment`; доска `AccountEdit`.
Умная категория (#584, опрос 2026-11-20, всё «А»): `core:domain/categoryguess/CategoryGuess` (получатель → продавец чека → первая строка заметки; самая частая за 200 последних, ничья — к новой; архивные/чужого типа/перевод — нет; названия сравниваются нормализованными), в шторке ввода — `TransactionEditViewModel.withGuessedCategory` из уже загруженных «500 последних» (обе истории семьи), `categoryGuessed` в `TransactionEditUiState`; ручной выбор (`categoryTouched`) не перетирается. Подпись в карточке «Категория» — `transactions/edit/components/GuessCaption` (серая строка со значком часов вместо «Кафе ✓», пока нет ручного выбора): «по прошлым операциям» (история) или «по названию» (словарь; опрос 2026-11-21 «А»); `GuessCaptionTest`.
Словарь брендов (#584, идея владельца 2026-11-21): `core:domain/categoryguess/BrandDictionary` (+ `BrandsArgentina.kt`, `BrandsRussiaWorld.kt`) — бренд или вид места («farmacia», «Disco», «Uber», «аптека») → ключ значка стартовой категории (`food`, `taxi`…) → своя категория человека с этим значком (переименование и язык не мешают; архивные не берём); сравнение без регистра, акцентов и «ё», самое длинное совпадение, короткие и общие слова — только как всё название. `CategoryGuess.suggest` = история семьи, иначе словарь (только расход); в шторке — `categoryGuessSource` (HISTORY/DICTIONARY).
Быстрые заметки, правила без ИИ (#586, 2026-11-21): `core:domain/quickentry/QuickNoteParser.parse(строка, валюта)` → `QuickNote` (сумма и валюта через `AmountInText`, «30к»/«1,5к» = тысячи, день сегодня/вчера/позавчера ru/es/en → `daysAgo`, слово счёта `AccountWord` CARD/CASH, название); `null` — суммы нет, строка для модели. Экрана и хранения заметок пока нет (ждут доски).
