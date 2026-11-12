# Дизайн Co-Shell — всё в одном месте

**Холст:** https://claude.ai/artifact/Kfegn8qM8QEnPKr7HRYpw5 — закреплён в боковой панели claude.ai
(«Co-Shell — дизайн»). Вкладки вверху холста: **Экраны · Воркфлоу · Компоненты · Предложения · История решений**.
**Правило:** любое визуальное изменение сначала показываем здесь макетом, код — после «ок» владельца
(`docs/agents/workflow.md` §«Визуальные изменения»).

Макеты живые: кнопка Play на доске — нажимать вкладки, переключатели, шторку.
**Темы:** доска «Тема макетов» (слева на «Экранах», «Воркфлоу», «Компонентах») перекрашивает весь холст —
тёмная/светлая × 6 палитр, по формулам `core/designsystem/.../theme/` (`Color.kt`, `PaletteSpecs.kt`). Палитры на
доске «Настройки» делают то же. Одна доска в другой теме — её Tweaks «Тема», «Палитра».

Холст — источник истины по виду. `mockups/` — копия его файлов в репозитории (читать без сети, видеть
историю правок в git). Поменял холст → скопируй файлы сюда тем же коммитом, что и код.

## Экраны → код (`app/src/main/kotlin/app/coshell/`)
Ряды на вкладке «Экраны»: слева основная доска экрана, правее — его состояния.

| Ряд | Доски (`mockups/`) | Код |
|---|---|---|
| Обзор | `Home`, `HomeEmpty` (первый запуск), `HomeAttention` (нет курса), `HomeSync` (ошибка синка, 17А), `UpdateWhatsNew` | `home/`, `home/sync/`, `update/notice/` |
| Операции | `Operations` (19В: удержание → выбор, свайп вкладок), `OpsSelect`, `OpsUndo`, `OpsEmpty`, `OpsNothing` | `transactions/list/`, `navigation/TabSwipe` |
| Шторка ввода | `Main` (шаги 1–2), `EntryTransfer`, `EntryReceipt`, `EntryEdit` (правка, дубль, удалить), `EntryError`, `EntryNoAccounts` | `transactions/edit/`, `components/keypad/` |
| Счета и «Ещё» | `Accounts`, `AccountsSearch`, `AccountsEmpty`, `More`, `UpdatePill`, `Update` | `accounts/list/`, `more/`, `update/` |
| Счёт и категории | `AccountEdit`, `AccountEditExisting` (блок валюты, ошибки), `AccountDelete`, `AccountBlocked`, `Colors`, `Categories`, `CategoryEdit`, `CategoryBlocked` | `accounts/edit/`, `categories/`, `category/BankColors` |
| Курсы и валюты | `Rates`, `RatesRefreshing`, `RatesFailed`, `RatesEmpty`, `RatesManual`, `ReportCurrencies`, `CurrencyAdd` | `rates/list/`, `currencies/` |
| Семья | `FamilySignedOut`, `FamilyNoAccess`, `FamilyInvited`, `FamilyJoinMode`, `Family`, `FamilyInviteSent`, `FamilyInviteDialog`, `FamilyOffline`, `FamilyNotConfigured` | `family/` |
| Настройки, копия, стирание, отзыв | `Settings`, `Backup`, `BackupAuto`, `BackupError`, `BackupRestore`, `Wipe` (слово СТЕРЕТЬ), `WipeFailed`, `Feedback`, `FeedbackSent`, `FeedbackError` | `settings/`, `theme/`, `backup/`, `wipe/`, `feedback/` |
| Шаблоны | `TplA` (шторка «+», выбран А), `TplAll` (32 шаблона), `TplEntry`, `TplSave`, `TplEdit` | `templates/` (`strip/`, `list/`, `edit/`), пилюля и диалог — `transactions/edit/components/TemplateBits`; доски — `mockup_screens_{templates,budgets,reports}.py` |
| Бюджеты | `BudA` (выбран А), `BudMany` (18 лимитов), `BudDetail`, `BudEdit`, `BudHome`, `BudFirst` | — (#167, #103) |
| Отчёты | `RepA` (выбран А), `RepAll` (24 категории), `RepDrill`, `RepPeriod` | — (#168, #140) |
| Импорт из Wallet | `ImportStart`, `ImportError`, `ImportSummary`, `ImportAccounts`, `ImportCategories`, `ImportTransfers`, `ImportPreview`, `ImportDone` | `importer/wallet/` |
| Воркфлоу | `Flows`: 16 главных действий по шагам; каждый шаг ведёт на доску | `navigation/`; генератор `scripts/agent/gen-flows-board.py` |
| Компоненты | `Components` | `core/designsystem/` (`components.md`) |
| Предложения | Сверху — **макеты по отзывам**, выбрано 2026-11-11: `Fb154*` (#154 + #155, вариант А: вкладки, плитки, «Ещё», настройки), `Fb157*` (#157, вариант А: отзывы со статусом); **ждут выбора** (2026-11-12): `Fb181*` (папки и категории), `Fb105*` (цвета, замер — `palette-distinct.py`), `Fb173*` (свайп сегментов) (генератор `mockup_screens_feedback.py`). Ниже — `Ideas`: 14 улучшений и **выбранные** варианты 3–12 (`Idea3B`, `Idea4A…Idea12A`; выбор #159 от 2026-11-11, генератор `mockup_screens_ideas.py`) | — |
| История | `History`: ссылки на ранние страницы выбора | — |

Нижняя панель — `CoShellBottomBar` + `navigation/MainBottomBar`: плавающая капсула 64 dp, вкладки без подписей,
пилюля 44 dp, «+» 52 dp (`Sizes.bottomBar*`).

## Расхождения кода с досками (долг, задача winerar/co-shell#115)
Ревью 2026-11-10 — `review-2026-11-10.md`. Доски состояний (2026-11-11) нарисованы по коду и строкам
`strings.xml`, но уже в целевом виде ревью: поле с заливкой (К6), строки в карточках (К4/К5), поиск-пилюля (К2).
Код к ним ещё не приведён. Расхождение 19В: при листании вкладок экран лишь «наклоняется» за пальцем
(¼ пути), соседняя вкладка въезжает после отпускания — вкладки живут в `NavHost`, не в пейджере.

## Ранние страницы (до холста)
- Новый облик (пружины, палитры, вёрстка): https://claude.ai/artifact/WqocoPvC6g6mLRt31WvcCW
- Выбор дизайна (#62–#66): https://claude.ai/artifact/DHcu66PAJq6rZwJL7uhhbo
- Иконка (#66): https://claude.ai/artifact/VYVhJ7roxaTLXpaBXhRFYh

## Правила для агента
- Новый экран или заметная переделка — сначала доска (со списками — и доска на 20–30+ элементов) на этом же холсте (не новый артефакт), потом код.
  Варианты на выбор — одна доска на вариант рядом; **после выбора невыбранные доски удалить**.
- Доски рисуем только во «Вольт · тёмная» (цвета — `scripts/agent/mockup_bricks.py`), без своих `dark`-tweak;
  `scripts/agent/mockup_theme.py` переводит цвета в токены темы (повторный запуск безопасен). Цвет, который не должен
  перекрашиваться (свотчи палитр), — в JS без `#` (`'#' + 'B8FF2E'`).
- Состояния экранов — из кирпичей в `scripts/agent/mockup_screens_*.py`; `python3 scripts/agent/gen-mockups.py`
  пишет доски, переводит на темы все доски и `canvas.json` (ряды — `LAYOUT`). Ручные доски (`Main`, `Accounts`…)
  он не трогает, кроме перевода цветов. Проверить без холста: `node scripts/agent/render-mockups.js <папка> light Манго <доски>`.
- Публикация: скопировать `mockups/*` в `<scratch>/project/`, `Artifact publish` с `root`, `file_path` = `project/canvas.json`,
  остальные в `files`, удалённые — `null` (порядок — инструкции типа в ответе `Artifact read`).
- Общий переключатель темы — `localStorage['coshell-mock-theme']` + событие `storage` (доски — iframe одного источника).
- Не сделанное из макетов — в `docs/STATUS.md` («Не сделано из мокапов»).
- Правила токенов и компонентов — `docs/agents/design-system.md`, `components.md`, `motion.md`.
