# Шапки экранов

Часть `components.md`: шапки вкладок и внутренних экранов — в `app`, на компонентах `core:designsystem`.

## Шапки (`app/.../ui/top/`)
- `TabTopBar(title, scroll, subtitle)` — шапка вкладки, «Живая строка» (опрос шапок 2026-11-15, А, «без эффекта прозрачности»).
  Капсула `CoShellTopBar` (56 dp, `Sizes.panelInset` от краёв, опрос #441 Б); при прокрутке заголовок не меняется, под ним появляется `subtitle` «где я»
  (в «Операциях» — день вверху списка и итог, `dayAt`; поэтому дни больше не липнут). При входе заголовок въезжает снизу.
- `TabOrSelectionTopBar` — меняет её на `SelectionTopBar`: «✕ Выбрано N» выезжает сверху, число перекатывается.
- Поиск вкладки — `TabTopBar(search = TabSearch(…))` (#394 Б): лупа среди действий, поле на месте заголовка, «✕»/«Назад»
  очищают и закрывают. В «Операциях» строки поиска над чипами больше нет.
- `ScreenTopBar` — внутренние экраны; та же капсула, без линии (#394 Б снята #441) — из `LocalScreenScroll`, который ставит
  `ScreenScrollHost` на каждый экран навигации (`navigation/ScreenDestination.kt`: `screen<T>` вместо `composable<T>`).
  Линия — общий `Modifier.scrollHairline`. Макет-страница: `docs/design/topbar-headers.html`.

## Из `core:designsystem` (#367)
- `CoShellTopBar` + `TopBarScroll` (`components/TopBar.kt`) — единственная обёртка над Material `TopAppBar`; `TabTopBar`/`ScreenTopBar`/`SelectionTopBar` собираются на ней.
- `CoShellLoading(size)` — круглая загрузка (24 в строке, 44 на экране), `CoShellDropdownMenu` + `CoShellMenuItem(checked, destructive, enabled)` — меню (доска `CompNew`, вариант А).
- `CoShellDatePicker` / `CoShellDateRangePicker` (`components/DatePickers.kt`) — обёртка над Material-календарём с цветами выбора темы; состояние — `rememberDatePick` / `rememberDateRangePick` (миллисекунды UTC). Свой календарь с доски (полоса периода) — позже.
