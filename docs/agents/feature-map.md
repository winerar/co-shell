# Карта фич: где что лежит

Выделено из `project-structure.md` (правила структуры — там). По абзацу на фичу: модель, логика, Room, экраны.
Добавил фичу или переложил файлы — обнови абзац.

Порядок списков (#338): по умолчанию алфавит — `core:model/NameOrder.kt` (`ACCOUNT_ORDER`, `nameOrder`) и `CATEGORY_ORDER`
(`CategoryTree.kt`); счета и категории сортирует `RoomAccountRepository`/`RoomCategoryRepository`, группы — `AccountGroupsBuilder`.

`shake/` — не экран: `ShakeEffect` (акселерометр → вибрация → колбэк, подключён в `CoShellNavHost`),
`ShakeSettingsViewModel` (выключатель, им пользуются `CoShellNavHost` и «Ещё»), `data/PrefsShakeSettings`;
распознавание толчков — `core:domain/shake/ShakeDetector`.
Мелкие удобства (#317, пока только расчёты — экраны ждут опроса): «экраном вниз» — `core:domain/shake/FaceDownDetector` (#324),
встряхнуть после сохранения — `shake/ShakeIntent` (#325); сумма в других валютах — `rates/AmountInCurrencies` (#318), конвертер — пакет `converter/` (#319, ниже);
`core:domain/quickentry/`: `AmountInText` — сумма из текста «Поделиться»/буфера (#320, #321), `Tips` + `TipSettings` — чаевые с другого счёта (#322; экран — `transactions/edit/tip/` + `components/TipSheet.kt`: «＋ Чаевые» в блоке суммы, две операции, счёт и категория запоминаются в `PrefsTipSettings`),
`EntryDraft` — черновик шторки (#323); уведомления о тратах партнёра — `family/PartnerSpendAlerts` (#326).
`greeting/` — одноразовое поздравление версии 1.0 (уведомление `GreetingNotifier` + пилюля `GreetingPill` на «Обзоре», доска `HomeGreeting`): правило «кому и когда» — `core:domain/greeting/ReleaseGreeting` (участник, не владелец семьи; версия ≥ 1.0), флаги «показано» — `GreetingStore`; после релиза пакет можно удалить.
`theme/` — не экран: `ThemeSettingsViewModel` (читают `MainActivity` и «Ещё»), `components/ThemeModePicker`, `PaletteGrid`, `AppearanceSummary`,
`data/PrefsThemeSettings`; режим `ThemeMode` — `core:domain/settings/ThemeSettings.kt`.
`settings/` — «Ещё → Настройки»: корень из групп (`SettingsScreen`), «Экраны» (`ScreensSettings.kt`, `HomeSettings.kt` — карточки «Обзора»), «Деньги и даты», «Ввод операции» (встряхнуть, вибрация), «Уведомления» (напоминания и время), «Данные и копия» (`SettingsGroups.kt`); экспорт CSV — `export/`; уведомление о трате партнёра — `family/sync/PartnerSpendNotifier`; параметры экранов — `ScreenOptions` (`core:domain/settings`, `PrefsScreenOptions`, `LocalScreenOptions`), плотность — `LocalCompactLists`; доски `Set1*`, план — `docs/design/settings-customization.md`); `appearance/` — «Настройки → Внешний вид» (язык, тема, палитра); `language/` — язык приложения (`AppLanguage`, шторка `LanguageSheet`, #352); `more/` — «Ещё»: карточка версии
(`components/VersionCard`) и группы строк (`components/MoreSections`), данные строк — `navigation/MoreRoute`.
Метка «новое» (#335): ключи мест — `newmark/NewFeatures` (новое место → ключ + `MoreItem.newKey`), правило 7 дней / до открытия —
`core:domain/newmark/NewMarks`, хранение на телефоне — `newmark/PrefsNewMarkStore`, `di/NewMarkModule` (обновление или чистая установка).
Бюджеты (#167): модель `core:model/Budget.kt`, логика — `core:domain/budgets/` (`BudgetPlan` — лимит месяца,
`SaveBudgetUseCase`, `RemoveBudgetUseCase`); Room — `BudgetDao`, `RoomBudgetRepository`. Расчёт (#182): `BudgetPeriod` (месяц по дню
начала), `BudgetLedger` (потрачено, среднее за 3 мес.), `BudgetProgress` (темп, состояние, прогноз), `BudgetCalculator` (перенос, итог месяца). Экраны (#103) — `app/.../budgets/`: `BudgetSource` (общий снимок: суммы по дням из SQL, #262),
`list/` («Бюджеты»: `BudgetBoard.kt` — конверты, итог, фильтр; шестерёнка — день начала месяца, #213), `detail/` (экран конверта #183:
`BudgetDetailBoard.kt` — шапка и категории папки; операции периода — `BudgetSource.observeEnvelope`), `edit/` (редактор лимита,
`BudgetForm.kt`); вход — «Ещё».
Теги при вводе (#293): `transactions/edit/EntryTags.kt` (список, недавние, создание, `mergeLabels` — сохранение в `labels`),
`components/TagPickerSheet` (шторка «Теги», открывается плиткой «Теги»), `NewTagDialog`.
Шторка ввода без прокрутки (#290, доска `Fb290A`): шаг 2 — `components/DetailsStep` из карточек `AmountBlock` (сумма, строка чека,
валюта — `ReceiptCurrencySheet`), `CategoryBlock` (частые — `frequentCategories` в `CategoryOptions.kt`, «Все» — `AllCategoriesSheet`
по полкам `categoryShelves`), `DetailsBlock` (плитки 2×2; шторки даты и заметки — `DetailSheets.kt`); образец — `EntrySheetSamples.kt`.
Возврат (#208): чип в `transactions/edit/components/DetailsStep` (`refund` в форме → `INCOME`), `Row.refund` в списке, `isRefund` + траты «Обзора» — `core:domain/rates/MonthSpending.kt`.
Отчёты: расчёт (#187) — `core:domain/reports/`: `ReportDay` (сумма дня) + `ReportRepository`, `ReportLedger` (курс дня, возвраты),
Расчёты без экранов (экраны — отдельные задачи): `CategoryMonthTable`/`categoryMonthTable` + `YearSummary` (#239, таблица «категории × месяцы», итог года), `PaidWith`/`paidWith` (#242, «Чем платили»; данные — `ReportRepository.observeAccountDays` ← `ReportDao.observeAccountDays`).
`ReportPeriod` (месяц/квартал/год/свои дни, `previous`, `shift`, `columnMonths`), `spendingBreakdown` + `spendingByFolder` (черта прошлого),
`categorySpending` («Все категории»), `monthlySeries`, `periodComparison`; «Итог месяца» (#235) — `monthReview`
(остаток, норма сбережений, `reviewStart`), `usualChanges` (3 главных изменения к среднему за 3 мес., порог 5 % обычных трат), `limitsTally`;
тренд (#237) — `categoryTrend` (12 мес. + средние за 3/12, `trendCategoryIds`), «выше/ниже обычного» — `UsualMark.kt` (`usualMarks` по папкам, `usualCategoryMarks` по категориям, к тому же числу прошлых месяцев, ≥ 20 % и ≥ 5 % обычных трат, не раньше 7-го дня);
когда показывать карточку — `monthReviewDue` (1–7 день месяца, ✕ — `settings/MonthReviewSettings`, на телефоне: `reports/review/data/PrefsMonthReviewSettings`).
Экран (#236, `Mon236*`) — `app/.../reports/review/`: `MonthReviewBoard` (сборка из `BudgetSource.Snapshot`), `MonthReviewScreen`
(герой, изменения → «Из чего сложилось», лимиты), `MonthLimitsScreen` («Лимиты октября», та же ViewModel); карточка на «Обзоре» —
`home/components/MonthReviewCard` (считает `HomeBudgetViewModel` → `buildHomeReview`, только когда пора); Room — `ReportDao`, `RoomReportRepository`.
Вкладка (#140, доски `RepA`/`RepAll`/`RepPeriod`) — `reports/`: `ReportsViewModel` (дни за `loadRange`), `ReportsBoard` (чистая сборка),
`components/` (`PeriodSwitch`, `SpendingCard`, `MonthsCard`, `ReportPeriodSheet`), `all/` — «Все категории» на ViewModel вкладки;
`category/` — «Из чего сложилось» (#188, `RepDrill`): тап по полосе/строке → `ReportCategoryDestination` (период, валюта, сравнение в маршруте),
своя ViewModel (дни + операции категории за период), доля и подкатегории — `core:domain/reports/categoryDrill`.
Тренд на экранах (#238, `Tr238DrillA`/`Tr238MarkA`): пилюля «↑ 45%» — `CoShellChangePill` (ds `charts/`, слот `badge` у `CoShellBarRow`),
`reports/components/UsualMarks.kt` (текст, цвет, TalkBack); `Mark` в `ReportsUiState` (только для месяца); «Все категории» — чип «Необычные · N»;
«Из чего сложилось» — строка пометки и `category/TrendCard` (столбцы истории, черта среднего за год, тап — тот месяц: `onMonth` во ViewModel).
Картина целиком (#189, `Rp189*`): «Доходы и расходы» — `incomeExpenses` (domain) → `IncomeBoard` → `components/IncomeCard` (ds `CoShellPairedColumns`);
«Капитал» — `netWorth` (начальный остаток + `BalanceDay` из `ReportDao.observeBalanceDays`, курс на конец месяца, «отложили / курсы») → `CapitalBoard` →
`CapitalCard` (ds `CoShellLine`); «Кто сколько потратил» — `ReportDay.authorId` → `spendingByMember` → `MembersBoard` → `MembersCard` («Я» — всё, что не внесли другие).
Хвост (#373, `Rp189NwDrill`): тап по линии (`CoShellLine.onSelect`) → `reports/capital/` — `accountsAt` (domain: счета на дату и изменение за месяц) →
`CapitalMonthBoard` (группы — `buildAccountGroups`, строки — `AccountRow`/`AccountGroup` из «Счетов») → `CapitalMonthScreen`; тап по участнику →
«Операции» через `PRESET_MEMBER` (`transactions/list/MemberPreset`, как `PRESET_UNCATEGORIZED` у «Внимания»).
Поиск и фильтры «Операций» (#141, вариант А): разбор суммы `=4500`/`>10000`/`3000-5000` — `core:domain/search/AmountQuery`,
периоды со стрелками — `search/PeriodRange`, итог под фильтрами (4А) — `search/FilterTotal`; Room — `TransactionFilter`
(`accountIds`, `categoryIds`, `author`) → `TransactionDao.observe`. Экран — `transactions/list/`: удержание суммы → пузырь в валютах отчётов (`AmountPeek.kt`, расчёт `core:domain/rates/AmountInCurrencies`, #318); `ListFilter` (что выбрано, что идёт в Room),
`FilterChoices` (что предлагают шторки, переключение папок), `FilterLabels` (подписи чипов), `FilterTotalText`; чипы и итог —
`components/FilterBar`, `FilterTotalCard`; шторки — `components/CategoriesSheet`, `PeriodSheet`, `AccountsSheet`, `TagsSheet` (обёртка — `FilterSheet`).
Теги в списке (#294): пилюли строки — `TransactionDays` (`Row.tags`, `moreTags`; удалённые не видны), фильтр `ListFilter.tagIds` — только в памяти (в Room не идёт).
С фильтром грузятся все подходящие строки (для итога), список показывается страницами в памяти.
Шаблоны операций (#169, #104): модель `core:model/TransactionTemplate.kt`, логика — `core:domain/templates/`
(`SaveTemplateUseCase`, `SaveAsTemplateUseCase`, `draftFrom` → `TransactionDraft` с `gaps`, `TemplateUsage` — счётчик «Часто»),
Room — `db/TemplateDao.kt`, `repository/RoomTemplateRepository.kt`; повтор (#143) — `core:model/RecurringSchedule.kt`,
`core:domain/recurring/` (даты, «Скоро», автопроведение, `edited` — правка правила), `core:data/repository/ScheduleCodec.kt`;
в редакторе — `templates/edit/components/RepeatBlock.kt` (плитки «Детали»), `RepeatSheet.kt` + `RepeatDraft.kt` (шторка), текст правила — `app/.../recurring/RecurrenceText.kt`. «Скоро · 7 дней» на «Обзоре» — `home/upcoming/` (`UpcomingViewModel`, `UpcomingCard`); «Провести» — `TransactionEditDestination.paymentDate` → `EntryTemplates.paid`; в шторке (#404) метка «⟳ Свет · 15 нояб.», «Пропустить этот раз» и «В прошлый раз · в среднем» — `transactions/edit/EntryPayment.kt` (`PaymentUi`), расчёт — `core:domain/recurring/PaymentHistory.kt`. «Регулярные платежи» (#403, «Все N ›» в «Скоро» и строка «Ещё → Деньги» — вход, когда «Скоро» скрыт) — `recurring/list/` (`recurringList` — чипы, поиск, месяцы; `monthAheadUi` — герой «До конца ноября»; расчёт — `core:domain/recurring/PaymentList.kt`); метка срока и сумма — общие `recurring/PaymentLabels.kt`. Особые случаи (#406): «Провести оба»/«Пропустить» в «Скоро» — `MissedPaymentsUseCase` (`core:domain/recurring/MissedPayments.kt`); «на паузе» (счёт в архиве: нет в «Скоро», напоминании, итоге месяца; сообщение в «Регулярных платежах» — `components/PausedMessage.kt`) — `PausedPayments.kt`. Платёж в другой валюте (#415): `TransactionTemplate.isForeign`/`postsByItself` — сам и кнопкой из уведомления не проводится (`PaymentPoster`), шторка с чеком — `TransactionDraft.receipt` → `filledFrom`, списанное по курсу — `withDebitSuggestion`, без курса — подсказка `templateHint`; в редакторе галочка «Платёж в другой валюте» — `templates/edit/components/ForeignCurrency.kt`. Фон и уведомления — `recurring/notify/` (`RecurringPaymentsWorker` + `Scheduler`: в 9:00 и при запуске; `PaymentNotices`, «Отменить» — `UndoPaymentReceiver`, «Провести 650 USD» и «Завтра» — `ReminderActionReceiver` (в другой валюте — «Ввести сумму») (`PostPaymentUseCase`, `ReminderLog.snooze`), «Изменить сумму» → шторка — `PaymentLinks` (MainActivity → `CoShellNavHost`), «≈ итог» — `remindTotal`, напоминание уходит, когда платёж провели где угодно (в т.ч. на другом телефоне) — `ReminderWatch` (`stillToRemind`, #406), выключатель «Напоминания о платежах» во «Внешнем виде» — `ReminderSettings`/`PrefsReminderSettings`; разрешение — `NotificationPermissionAsk`). Экраны — `app/.../templates/`: `TemplateCatalog` (общий источник),
`components/` (чип, полоса), `strip/` (полоса в шторке «+»), `list/` («Все шаблоны»), `edit/` (редактор), `data/PrefsTemplateUsage`;
в шторке операции — `transactions/edit/EntryTemplates.kt` и `components/TemplateBits.kt` (пилюля, «Сохранить как шаблон»).
Теги операций (#129): модель и ссылки — `core:model/Tag.kt` (`Tag`, `TagIds`, `Transaction.tagIds`), логика — `core:domain/tags/`
(`SaveTagUseCase`, `EnsureLabelTagsUseCase`, `TagRanking` — недавние/частые/поиск), Room — `db/TagDao.kt`, `repository/RoomTagRepository.kt`.
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
Ручной порядок (#338, экран — после «ок»): `core:domain/Reorder` — новые `sortOrder` при перетаскивании (меняется одна строка, шаг 1024).
