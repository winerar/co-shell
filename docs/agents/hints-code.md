# Обучение новичка: где в коде

Часть [`hints.md`](hints.md) (что и когда показывать — там).

## Хронология (#513)
**Сделано (2026-11-19):** `HintId`, `HintPlanner`, `HintStore`, `FirstSteps` (+ тесты); `app/.../hints/` — `PrefsHintStore`, `HintsViewModel`, `FirstStepsRow`/`FirstStepsSheet` на «Обзоре»; «Подсказки» и «Показать заново» в «Ещё → Настройки → Помощь» (до 2026-11-20 — «Ещё → Приложение»).
**Сделано (2026-11-19, ч. 2):** `HintCenter` (решает «можно ли сейчас», `LocalHintHost` — в превью и тестах `null`, подсказок нет) и `WordHint(id, text)` — строка П1 с «✕»; подключены: Обзор (без операций), Счета, Категории, Бюджеты, Отчёты, Семья (`HintCenterTest`).
Показ = «показано» сразу (при выдаче), «✕» только закрывает; задержка 1,5 с после открытия экрана.
**Сделано (ч. 3):** `HintsCoverageTest` — каждый `HintId` назван в этом файле; у каждого, что показывает экран, есть `hint_<key>` (до публикации — на русском; en/es — если ключ не в `translations-pending.txt`).
**Решено опросом 2026-11-19 (все А, доски `Hint513MomentLong/Finale/Reset`, «ок» получен):** карточка-жест, плашка до 2 строк + «Открыть», моменты вне дневного лимита, финал на месте строки «Первые шаги», сброс — плашка.
**Сделано (ч. 4, 2026-11-19):** `CoShellCoachCard` (карточка-жест) и `CoShellNoticePill(actionLabel)` в `core:designsystem` (карточка на доске «Компоненты» → «Уведомления»); `HintHost.offer(тексты)` выбирает первую по приоритету и кладёт на `banner`,
`HintBannerHost` (в `overBar` `CoShellNavHost`, уступает плашке обновления и приветствию; плашка уходит сама через 8 с, карточка — по «Понятно»), `CoachHint(тексты, enabled)` на экране;
`HintPlanner`: моменты вне лимита «заход/день» (`HintCenterTest`, `HintPlannerTest`). Жесты: `flip_card` («Обзор» с операциями), `edit_tap` (от 1 операции) и `hold_select` (от 3) в «Операциях».
Моменты `first_entry`/`first_limit` — по **смене** данных за сессию (`StepsMoments.between`, при старте не срабатывают); финал `StepsFinale` (`CoShellSuccessBurst`, 7 с или нажатие, один раз — ключ `steps_finale`); «Подсказки включены» после сброса.
**Сделано (ч. 5), убрано 2026-11-25:** `flip_card` на «Бюджетах» — карточка жеста видна только на вкладках, а «Бюджеты» — внутренний экран.
**Сделано (ч. 6):** `swipe_tabs` (после 2-й смены вкладки кнопкой, счётчик в `MainBottomBar`).
**Сделано (ч. 5):** `TimeMoments` (чистая логика, `TimeMomentsTest`) + `HintsViewModel` раз за запуск (через 6 с) предлагает `first_week`, `welcome_back`, `backup`, `theme`; `HintStore.firstSeenDay/firstEntryDay` (на устройстве, сброс подсказок их не трогает). Без кнопки «Открыть» — путь в тексте.
**Сделано (ч. 6):** жест `template` — `RepeatedSpending.hasRun` (три последние траты: одна категория и один счёт), карточка в «Операциях» (`TransactionsUiState.repeatedSpending`, без фильтра).
**Сделано (ч. 7):** «Открыть» у плашек `backup`/`theme`/`month_end` (`HintTarget`, `HintBannerHost(onOpen)`); `month_end` — первые 3 дня календарного месяца, один раз за всё время, точка на «Отчётах» до первого захода (`HintHost.reportsDot`); `limit_80` — ближайший к концу лимит из 80–99 % (`LimitWatch`), текст с названием категории и остатком (`HintBanner.args`).
**Решено опросом 2026-11-19 (`poll-513b.json`: шторка — Б, «Теги»/«Валюта» — А; доски `Hint513EntryCoach`, `Hint513Tags`, `Hint513Currency`):** слова шторки ввода — карточка `SheetCoach` над шторкой (`entry_minimal` → `keypad_math` → `entry_refund`: при открытии новой операции выдаётся первая ещё не показанная, не чаще одной в день); `tags` и `currency_receipt` — `WordHint` под заголовком шторок «Теги» и «Валюта покупки».
**Сделано (ч. 8):** `tags` в «Операциях» от 10 строк без фильтра (`TAGS_HINT_ROWS`, строка П1 внутри элемента фильтров — `dayAt` не сдвигается); `currency_account` — момент-плашка: `AccountEditViewModel` ставит `foreignCurrencyAdded` (новый счёт, валюта не в валютах итогов), `AccountEditRoute` предлагает её до закрытия шторки.
**Сделано (#526):** `scan_receipt` — `WordHint` в шторке ввода (новая операция, `DetailsStep`).
**Осталось:** ничего.
- `core:domain/hints/`: `HintId`, `HintTrigger`, `HintPlanner` (чистая логика «что показать», тесты), `HintsSettings` (интерфейс);
  шаги — `FirstSteps` (`FirstStepsState` из данных). Хранение — `app/.../hints/` (настройки устройства).
- UI: `app/.../hints/` — блоки на утверждённых компонентах (`CoShellHint`, `CoShellCoachCard`, `CoShellNoticePill`, шторка). Строки —
  `strings_hints.xml` на всех языках. Новый вид подсказки/плашки — сначала доска «Компоненты» (`components-approved.md`).
- Тесты: `HintPlannerTest` (порядок, лимиты, сброс), `HintsCoverageTest` — у каждого `HintId` есть строка во всех языках и запись в
  таблицах этого файла; убранный экран/жест — в `retired`.
