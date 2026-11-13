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
| Бюджеты | `BudA` (выбран А), `BudMany` (18 лимитов), `BudDetail`, `BudEdit`, `BudHome`, `BudFirst` | `budgets/` (#167, #103); `BudHome` — `home/budget/`, `home/components/{BudgetTile,HotBudgets}` (#184) |
| Отчёты | `RepA` (выбран А), `RepAll` (24 категории), `RepDrill`, `RepPeriod` | — (#168, #140) |
| Импорт из Wallet | `ImportStart`, `ImportError`, `ImportSummary`, `ImportAccounts`, `ImportCategories`, `ImportTransfers`, `ImportPreview`, `ImportDone` | `importer/wallet/` |
| Воркфлоу | `Flows`: 19 главных действий по шагам (с шаблонами, бюджетами, отчётами); каждый шаг ведёт на доску | `navigation/`; генератор `scripts/agent/gen-flows-board.py` |
| Компоненты | `Components`: каталог кирпичей макетов (`mockup_catalog.py`) → имена в коде | `core/designsystem/` (`components.md`) |
| Предложения | Сверху — **макеты по отзывам**; выбрано 2026-11-13 (опрос): `Fb129A`, `Fb129AOps` + `Fb129Pick`, `Fb129All` (#129 теги: А строка недавних), `Fb180A` + `Fb180List` (#180 типы счетов: А сетка), `Fb208C` + `Fb208Show` (#208 возврат: В чип), `Fb284A` (#284 прошлые версии: А карточки) — генераторы `mockup_screens_fb_{tags,more}.py`; выбрано 2026-11-11: `Fb154*` (#154 + #155, вариант А: вкладки, плитки, «Ещё», настройки), `Fb157*` (#157, вариант А: отзывы со статусом); выбрано 2026-11-12: `Fb181V` (#181, В: папка — квадрат с числом), `Fb268A/B`, `Fb269A` (#268: версия 0.N + сборка, «что нового», ручная установка сразу), `Fb271A`, `Fb271Pal` (#271: 7 сочных + 7 нежных тем-фруктов, `mockup_screens_fb_nov12.py`); `Fb105B` (#105, Б: 24 различимых цвета, замер — `palette-distinct.py`, `Fb105Now`), `Fb173A` (#173, А: сначала сегменты, с края — вкладка), `Fb141A*` (#141, А: чипы, у каждого своя шторка, `mockup_screens_fb_search.py`) (генератор `mockup_screens_feedback.py`). Ниже — `Ideas`: 14 улучшений и **выбранные** варианты 3–12 (`Idea3B`, `Idea4A…Idea12A`; выбор #159 от 2026-11-11, генератор `mockup_screens_ideas.py`) | — |
| История | `History`: ссылки на ранние страницы выбора | — |

Нижняя панель — `CoShellBottomBar` + `navigation/MainBottomBar`: плавающая капсула 64 dp, вкладки без подписей,
пилюля 44 dp, «+» 52 dp (`Sizes.bottomBar*`).

## Расхождения кода с досками (долг, задача winerar/co-shell#115)
Ревью 2026-11-10 — `review-2026-11-10.md`. Доски состояний (2026-11-11) нарисованы по коду и строкам
`strings.xml`, но уже в целевом виде ревью: поле с заливкой (К6), строки в карточках (К4/К5), поиск-пилюля (К2).
Код к ним ещё не приведён. Расхождение 19В: при листании вкладок экран лишь «наклоняется» за пальцем
(¼ пути), соседняя вкладка въезжает после отпускания — вкладки живут в `NavHost`, не в пейджере.
Сверка #202 (2026-11-12): сегменты К3 и поля К6 совпадают. Осталось: «Категории» — на доске карточки с чипами
детей, в коде строки (ждёт выбора #181); `CategoryEdit` — «Тип · Папка» сводкой, в коде сегменты и чипы папок;
на «Счетах» в коде есть сворачиваемая группа «Архив · N», на доске (v37) архива нет.
Сверка #203 (2026-11-12): «Семья» и «Курсы» приведены к доскам (эталоны `family_*`, `rates_*`); без данных пока —
«вошёл · N операций» у участника и изменение курса за день. Стрелка «назад» в коде — Material, на досках — шеврон (на всех экранах).

## Ранние страницы (до холста)
- Новый облик (пружины, палитры, вёрстка): https://claude.ai/artifact/WqocoPvC6g6mLRt31WvcCW
- Выбор дизайна (#62–#66): https://claude.ai/artifact/DHcu66PAJq6rZwJL7uhhbo
- Иконка (#66): https://claude.ai/artifact/VYVhJ7roxaTLXpaBXhRFYh

## Правила для агента
- **Карточка-герой (главный блок экрана) — всегда на градиенте темы** (владелец 2026-11-12, #277): на досках кирпич
  `hero_card`, в коде `CoShellCard(accent = true)` / `StatusCard`. Цветная заливка или рамка — только у сообщений (ошибка, внимание).
- Новый экран или заметная переделка — сначала доска (со списками — и доска на 20–30+ элементов) на этом же холсте (не новый артефакт), потом код.
  Варианты на выбор — одна доска на вариант рядом; **после выбора невыбранные доски удалить**.
- **Все доски — из кирпичей, ручных нет** (с 2026-11-12). Слои: `mockup_bricks.py` (примитивы: строка, поле, кнопка,
  панель…) → `mockup_parts.py` (крупнее: строка счёта, плитки, свотчи, кольцо, данные счетов `ACCOUNTS`) → доски
  `mockup_screens_*.py`; главные доски рядов — `mockup_screens_{tabs,ops,forms,settings}.py`, «Компоненты» и «История» —
  `mockup_catalog.py` (новый кирпич → добавь его в каталог). Правка доски на холсте затрётся — переноси в генератор.
- Интерактив — `mockup_logic.py`: `go(группа, значение, кирпичи)` / `flip(флаг, …)` делают кирпичи нажимаемыми,
  `per()` / `when()` / `toggled()` показывают нужное состояние; JS пишется сам по `{{go_…}}`, `{{is_…}}`. Своя логика
  (свайп «Операций», палитра «Цвета») — `extra`/`methods` у `proto()` или `board(..., script)`.
- Цвета — только константы кирпичей («Вольт · тёмная»), без своих `dark`-tweak; `mockup_theme.py` переводит их в токены
  темы. Цвет, который не должен перекрашиваться (свотчи палитр, банки), — в JS без `#` (`'#' + 'B8FF2E'`); чернила на
  фирменных значках (`#0B0F0A`, `#1A1400`…) — в `KEEP` у `mockup_theme.py`. Главная карточка экрана — `hero_card`
  (градиент `HERO_BG`, роли `H0…H2` → `CO_HERO` по палитрам и темам, как `heroDarkStops`/`heroLight` в коде, #249).
- `agent.sh mockups` пишет все доски, «Воркфлоу», `canvas.json` и проверяет логику (`check-mockups.js`). Без холста:
  `agent.sh render <доски>` или `node scripts/agent/render-mockups.js <папка> light Манго <доски>`.
- Публикация: скопировать `mockups/*` в `<scratch>/project/`, `Artifact publish` с `root`, `file_path` = `project/canvas.json`,
  остальные в `files`, удалённые — `null` (порядок — инструкции типа в ответе `Artifact read`).
- Общий переключатель темы — `localStorage['coshell-mock-theme']` + событие `storage` (доски — iframe одного источника).
- Не сделанное из макетов — в `docs/STATUS.md` («Не сделано из мокапов»).
- Правила токенов и компонентов — `docs/agents/design-system.md`, `components.md`, `motion.md`.
