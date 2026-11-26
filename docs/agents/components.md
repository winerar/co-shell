# Компоненты `core:designsystem`

Что есть и как пользоваться. Общие правила дизайн-системы — `design-system.md`, движение — `motion.md`.

## Утверждённый набор
Только компоненты досок «Компоненты»; правила, единый стиль и тесты `ApprovedComponentsTest`/`ComponentRuleTest` — [`components-approved.md`](components-approved.md).

## Что есть
- **Поля** — `CoShellTextField` (К6 = 8А, #200: заливка `surfaceContainer`, рамка 1 dp `outline` (≥ 3:1, Style8) → ink в фокусе,
  радиус 16, подпись внутри; `BasicTextField` + `TextFieldDefaults.DecorationBox`, рамка только вокруг поля; `error` краснит поле и заменяет `hint` под ним, `prefix`/`suffix`,
  на всю ширину своего места), `CoShellAmountField` (десятичная клавиатура, шрифт сумм, «0», код валюты
  после числа).
  Сумма в редакторе — [`components-keypad.md`](components-keypad.md). Клавиатуры: `TextKeyboard` (с заглавной, «Далее»), `CodeKeyboard` (коды валют).
- **Поиск над списком** — `CoShellSearchField` (К2, #198): пилюля 48 dp на `surfaceContainer`, лупа, подсказка внутри,
  «✕» очищает (`clearLabel` — для TalkBack, в app — `R.string.search_clear`); «Поиск» на клавиатуре прячет её.
  Везде, где ищут по списку (Операции, Счета, «Найти счёт» в шторке, «Все шаблоны»), — только он.
- **Секции форм** (#290) — `CoShellFormSection(title, trailing)`: карточка-сущность с подписью капсом; справа — `CoShellSectionLink`
  («+ Чек», «В шаблон») или текст. Внутри — `CoShellDetailTile(icon, label, value, empty)` (плитка 40 dp) и `CoShellCheckTile` (галочка словами),
  `CoShellSwitchLine` (переключатель в одну строку с подписью), `CoShellChoicePill` (мелкие пилюли выбора — 5 в строку, «Тип» счёта).
  В app (`ui/form`, `ui/sheet`): `IconStrip`/`ColorStrip` (одна строка + «Все 52»/«+29»), `IconColorSheet` (все иконки и цвета,
  `extra` — банки), `EditTitleActions` (архив и удаление у заголовка), `FormSheet` (маленькая шторка «…· Готово» поверх формы; `actionLabel`/`onAction` — своё действие вместо «Готово», «Сохранить баланс» #581).
- **Лента счетов** — `CoShellAccountRibbon(accounts: List<RibbonAccount>, selectedId, onSelect, onAll, allLabel)` (#505, доска `CompChoice`): значки 44 в ячейках 48, выбранный — кольцо `themeEdge()`, в конце «Все ›».
- **Галочка** — `CoShellCheckMark(ToggleableState)` (#141): квадрат 22 dp — рамка outline / неон + рамка цветом текста с галкой или чертой
  («часть папки»); только показывает — переключает строка (`CoShellListRow(onClick)`). Шторки фильтров «Операций».
- **Радио** — `CoShellRadioMark(selected)` (#352): кольцо 22 dp outline, выбранное — неон + рамка цветом текста с точкой; один из списка (язык).
- **Сегменты** — `CoShellSegmented(labels, selected, onSelect, enabled)` (К3, #202; Style1): залитая дорожка-пилюля, сегменты 36
  (ячейка 44), одна пилюля скользит к выбранному на `Motion.spatial()` (`drawBehind` — только перерисовка); пилюля — неон + рамка 2 dp + жирный
  везде, и для «как показать» (фиолетовый `SegmentTone.View` убран опросом «Единый стиль»).
  `icons` (по значку на подпись): не влезает самая широкая подпись — все сегменты показывают значки, подпись остаётся для TalkBack (#321, тип в шторке ввода: ↗ ↙ ⇄).
  Material `SegmentedButton` не используем. Дорожку можно тянуть пальцем (вправо — следующий, края не отдаёт).
- **Свайп сегментов** (#173, схема А): `val swipe = rememberSegmentSwipe()` → содержимому `Modifier.segmentSwipe(swipe,
  selected, count, onSelect)`, сегментам `swipe = swipe` — пилюля едет за пальцем, содержимое наклоняется (как 19В).
  Жест сначала листает сегменты; с крайнего наружу не берётся → уходит нижним вкладкам; круче 30° — прокрутка.
  Где: «Категории» (Расходы | Доходы), «Счета» (группировка). Сегменты-поля в редакторах — только тап/дорожка.
- **Карточка с оборотом** — `CoShellFlipCard(flipped, onFlip, flipLabel, front, back)` (#519, доски `Flip519*`): нажатие переворачивает карточку-героя вокруг горизонтальной оси (`rotationX` + `Sizes.flipDepth`, пружина `Motion.flip`); обе стороны всегда в композиции, высота плывёт от одной к другой, скрытая сторона — вне дерева доступности; «↻» в углу — подсказка и подпись для TalkBack; текст верхней строки лица/оборота — с `padding(end = FlipCueClearance)`, чтобы «↻» его не закрывал. Состояние `flipped` хранит экран (`rememberSaveable`). «Обзор» (`TotalCard`/`TotalBack`) и «Бюджеты» (`BudgetSummaryCard`/`SummaryBack`).
- **Порядок перетаскиванием** — `CoShellReorderColumn(state, keys, onDrop, canNest)` + `CoShellReorderRow(state, key, moveUp/Down)` (#338, доски `Fb338*`):
  удержать строку и тянуть, остальные расступаются (`Motion.spatialFast`), у края список листается сам; поднятая — рамка `primary` без тени, цель «внутрь» —
  пунктир (середина строки, `canNest`); для TalkBack — действия «Выше/Ниже». Список не ленивый (счета, категории). В app — `ui/order/OrderScreen`.
- **Заголовок группы** — `CoShellSectionLabel(text, tone)` (К4, #199): 13 sp / 600, разрядка 0.04 em; `Muted` — серый как
  написано («Тема»), `Accent` — заглавные акцентом («СЕГОДНЯ», «НЕДАВНИЕ»). Без своих отступов; в app — `ui/list/SectionTitle`
  (с отступами экрана), `DayHeader`, `GroupTitle` шторки, «Мои отзывы» — все на нём. Новых заголовков мимо него не делать.
- **Строки в карточке** — `CoShellRowGroup { row { … } }` (К5, #199): фон `surfaceContainer`, рамка `outlineVariant`, углы 16 dp,
  линия 1 dp (`surfaceContainerHighest`) между строками; `if` вокруг `row` можно. Длинный `LazyColumn` — `Modifier.rowGroupSegment(
  RowGroupPosition(i, n))` на каждом элементе (кусок карточки, список остаётся ленивым; так «Курсы»). Служебные экраны — только так.
- **Строки списков** — `CoShellListRow` (текстовая или со слотами: заголовок bodyLarge, подпись серым,
  `leading`/`trailing` серым; `onClick` → `Tactile.Row`; `onLongClick` — режим выбора списка; `enabled = false`
  → 38 %); прозрачная, `containerColor` — фон (выбранная строка). `CoShellSwitchRow` — вся строка переключает.
  `dense = true` — плотность меню (≥ 52 dp, подпись `bodySmall`). **Строка меню** — `CoShellMenuRow(icon, title, onClick, subtitle)`
  (#155): иконка второго цвета в круге его тона 18 % (`MenuIcon`, #442 Б — у всех строк один цвет), шеврон; группы «Ещё»;
  `mark` — сразу за заголовком: метка «новое» = `CoShellStatusChip(…, StatusTone.Fresh)` (неон палитры в точке, доска Style2, #335).
- **Модальные окна (#345, вариант В)** — вопрос: `CoShellAlertDialog` — карточка по центру (иконка в квадрате, вопрос,
  последствия, пилюли «Отмена · Действие»; `destructive` — красные иконка-корзина и кнопка; без окна — `CoShellConfirmCard`; длинный текст прокручивается над кнопками, кнопки не сжимаются — `ConfirmCardOverflowTest`).
  Форма или выбор — только шторка `CoShellModalSheet` (в app — `ui/sheet/ModalSheet`): заголовок + подзаголовок + × (`headerAction` — ещё одна кнопка-значок перед ×, так режим конвертера),
  кнопка внизу во всю ширину; числа — своей клавиатурой (`CoShellKeypad`, `showPlus = false` для курса). Выбор одного —
  `ui/form/ChoiceSheet` (строки 52 dp с ✓, поиск от 9 вариантов, `extra` — свой код валюты; `group` — подпись над строками, `icon`/`color` — значок категории, название до 2 строк; строки лежат в своей колонке: колонка шторки раздвигает детей на 12 dp; категории (все три выбора: «Все категории», шаблон, лимит) — `ui/form/categoryLines`: как экран «Категории» — родитель «С подкатегориями», под ним подкатегории с отступом и малым значком (`ChoiceOption.indent`), без подписей групп; #439); числа 1–31 — `CoShellDayGrid`;
  месяцы/кварталы/годы — `CoShellLabelGrid(labels, selected, columns)` (#140); свои дни — `ui/form/DateRangeSheet`.
  Material-диалоги в app запрещены (`TactileRuleTest`).
- **Формы** — `CoShellSection` (заголовок + содержимое + красная ошибка), `CoShellHint`, `CoShellMessage`
  (итог действия: блок на ступень светлее фона или контейнер ошибки).
- **Шторка и панель** — `CoShellSheet` + `CoShellSheetLayout` (действие закреплено внизу, `titleAction` — текст-кнопка
  в строке заголовка, «Сбросить» в фильтрах; вне навигации — внутри `Dialog(properties = CoShellSheetDialogProperties)`), `CoShellBottomBar`
  (`CoShellTab`: иконка, подпись, выбрана, `badge` — неоновая точка «есть новое»; «+» в середине, `addConfirm` —
  галочка после сохранения, `addLocked` — приглушённый «+» с замком, ввод закрыт до обновления, #334) — §«Навигация». **Пилюля-уведомление** — `CoShellNoticePill` (иконка в круге, одна
  строка, «×»; всплывает пружиной над панелью; сейчас — «Версия N · Обновить», `update/notice/UpdatePill`).
  **Готово** — `CoShellCheckMark(size, color, over)` (лайм с обводкой), `CoShellArrowChip()` (стрелка между двумя плитками), `CoShellSuccessBurst(content)` (один раз ореол и искры + `Confirm`; «Обновлено», #опрос 11-18).
  **Тег** — `CoShellTagPill(name, color)` (точка + имя в пилюле цвета тега, ≤ 132 dp), `CoShellTagMore(n)` — «+N»,
  `CoShellTagDot(color)` — точка в строках выбора; цвет без выбора — `CategoryPalette.forKey(id)`.
  Тег-переключатель — `CoShellTagChip(name, color, selected, onClick, removeLabel)` (34 dp; вкл — заливка цвета тега, «✕» — в
  выбранных), `CoShellTagMoreChip(label)` — пунктирная «Все N» (#293, строка в шторке ввода и шторка «Теги»).
- **Веер над «+»** — `CoShellActionFan(actions, enabled) { «+» }`, `FanAction(icon, label, onSelect)` (#623, доски `HomeHoldFan`, `CompChoice`): удержание раскрывает на дуге круглые кнопки (`Sizes.fanButton` 56, `fanRadius` 120), ведут палец и отпускают — действие, мимо — закрыть; короткое нажатие как раньше. Геометрия — `ActionFanGeometry`; цели — `navigation/FanTarget`.
- **Кнопка добавления** — `CoShellAddFab(label, onClick)`: **круглая «+» 56 dp без текста**, внизу справа (`Scaffold(floatingActionButton)`),
  одна на экран-список; `label` — только имя для озвучки («Новый счёт»). Под списком — `Sizes.fabClearance`. Ни «+» в шапке, ни строки
  «добавить» в списке, ни пунктира (выбор владельца 2026-11-16, #408; такой стиль — для всех будущих кнопок добавления; `AddButtonRuleTest`).
  Пустой экран — только текст с подсказкой «Нажмите «+»» (своей кнопки-призыва нет, 2026-11-16). Категории и группы — одной кнопкой, вид выбирается в редакторе. Ссылки внутри карточек («Добавить подкатегорию») остаются.
  Карточка состояния служебного экрана (курсы, автокопия) — `app/.../ui/hero/StatusCard`. **Все карточки-герои —
  на градиенте темы** (`CoShellCard(tone = CardTone.Hero)`, владелец 2026-11-12, #277; на досках — `hero_card`); `CardTone.Warning`/`Danger` — только для состояний из `CardToneRuleTest`. `CoShellOutlinedButton(color = …)` — контур и текст цвета
  (фиолетовая «Синхронизировать сейчас» высотой `Sizes.button`). **Пустой слот** — `Modifier.dashedBorder(color, radius)`
  (пунктир: «+ Новый шаблон», «Пригласить»).
- **Пузырь-подсказка у удержанного элемента** — `CoShellPeekBubble(lines, footnote)` (попап над элементом, справа, не берёт касаний;
  `PeekCard` — то же без попапа для превью) + жест `Modifier.holdToPeek(enabled, onTap, onPeek, onRelease)` (#318: держишь — виден, отпустил — исчез).
- **Снэкбар** — `CoShellSnackbarHost` (выезжает снизу, полоска оставшегося времени); **тема (палитра)** — `CoShellPaletteSwatch` (квадрат в фоне темы, по 4 в ряд, #271).
- Тест `app/.../TactileRuleTest` запрещает в `app` импорт Material-кнопок, чипов, полей, диалогов,
  `ListItem`, `Card`, `ModalBottomSheet`, `NavigationBar` — только компоненты отсюда. Обёртки со строками приложения (`ui/form/ConfirmDialog`,
  `ui/list/ActionRow`, `EditFooter`, `DateChips`) остаются в `app`, но строятся на этих компонентах.
- Новый компонент: без строк приложения (тексты — параметрами), с `@Preview` светлой и тёмной темы,
  добавить его в `catalog/`.
- **Клавиатура суммы** и поле суммы в редакторах — [`components-keypad.md`](components-keypad.md).
- **PIN** (#145, доска `Priv145LockA`) — `CoShellPinPad(onKey: (PinKey), backspaceLabel, biometricLabel)` (клетки `CoShellKeypad`, отпечаток слева от нуля,
  `biometricLabel = null` — без него) и `CoShellPinDots(filled, description, error)`; экран — `app/.../privacy/pin/PinLayout`.
- **Скрытые суммы** (#145) — `amount/LocalAmountsHidden` + `shownAmount(text)`/`maskAmounts` («•••• USD»): `AmountText`, `RollingAmountText`, подписи графиков,
  `AccountCard`, `DayHeader`, пузырь сумм читают сами; сумма в своём `Text` или фразе — через `shownAmount` (в app фразы — `sentenceAmounts()`).
  `CoShellSwitchRow(enabled = false)` — 38 %, не переключается («Отпечаток» без PIN).
- **Значок счёта** — `category/AccountBadge` (скруглённый квадрат цвета счёта, 2 буквы названия или буквы банка,
  чернила тёмные/белые по яркости — `inkOn`); везде, где показан счёт (список, выбор счёта, лента в шторке); `initials = false` + `Sizes.miniBadge` —
  мини-значок 16 dp без букв в подписи строки операции (#505, на досках — `mini_badge`).
- **Цвета банков** — `category/BankColors`: группы по странам (Аргентина, Армения, Россия, крипто/кошельки),
  `suggest(name)` — пресет по слову названия (подсказка «Похоже на банк …» в редакторе счёта), `byColor`.
  Цвет бренда хранится как есть и не перекрашивается темой; не должен совпадать со swatch палитры (тест).
  **Цвет нового банка и сверка (владелец 2026-11-18, #505):** не «на глаз», а по иконке самого сервиса — `agent.sh bankcolors` (фавикон → частый насыщенный цвет); расхождение (`LOOK`) судим глазами по `--sheet` и меняем только если иконка — знак бренда (Tether, Revolut, ВТБ оставлены: фавикон ≠ знак). Новый пресет — ещё и домен в `bank-colors.py` (тест это проверяет). Логотипы: только свободные (Simple Icons, CC0) и только у сервисов, не у банков; публикация — без логотипов банков.
- **Прогресс** — `CoShellLinearProgress()` (бесконечная) и `CoShellLinearProgress(progress)` (заполнение плывёт пружиной).
- **Графики** — `charts/` (#168, доска `RepA`; витрина `catalog/ChartsCatalog`, эталоны `charts_*`). Масштаб один на график:
  `ChartScale.of(значения + призраки).of(v)` → 0..1, столбцы и полосы от нуля. Появление — один раз (`rememberChartEntrance`,
  `Motion.spatialSlow`), смена чисел — `Motion.spatial`; анимированное значение читаем только в отрисовке/размещении.
  - `CoShellBarRow(label, value, fraction, color, share, ghost, ghostDescription, leading, onClick)` — полоса категории, серая черта — прошлый период.
  - `CoShellColumns(List<ChartColumn>, selected, average, onSelect)` — столбцы по месяцам: выбранный — акцент, число над столбцом,
    призрак — пунктирный контур, `average` — пунктир поперёк; TalkBack — `ChartColumn.description`.
  - `CoShellDonut(slices, description) { центр }` — кольцо ≤ `DONUT_MAX_SLICES` + «Другое» (хвост группирует вызывающий), дуги — `donutArcs`; центр ограничен отверстием (84 % его диаметра): длинная сумма ужимается, а не наезжает на кольцо (#554).
  - `CoShellHeatGrid(weekdays, leadingBlanks, cells: List<HeatCell>, selected, onSelect)` + `CoShellHeatLegend(less, more, trailing)` — «Календарь трат» (#553, доска `RepCalendar`, эталоны `heat_*`): ячейка 48 dp, уровень 0–4 — доля неона темы (`fillEdge`), сегодня — кольцо `themeEdge()`, выбранный — толще, будущие — тихие.
  - **«Пуля» бюджета** — `CoShellBullet(fraction, tone, mark)`: заполнение = потрачено/лимит, черта = темп на сегодня;
    `BulletTone` NORMAL/WARNING/DANGER/IDLE, цвет для текста того же состояния — `bulletColor`; высоты `Sizes.bullet*`.
  Дорожка нейтральная в обеих темах. Остальные графики — #168.
- **Панель действий** — `CoShellActionBar` (`components/ActionBar.kt`): та же капсула и пружина, что у нижней
  панели, но с кнопками (режим выбора 19В). Экран ставит её вместо вкладок через `ui/bar/ReplaceBottomBar`;
  шапка режима — `ui/top/SelectionTopBar` («✕ Выбрано N»), смена с шапкой вкладки — `TabOrSelectionTopBar`.
- **Вкладки листаются свайпом везде (владелец 2026-11-11, #173):** новые сегменты-вкладки — сразу с `segmentSwipe` (выше).

**Скриншот-эталоны:** `ComponentsCatalog`/`TokensCatalog` снимаются в `CatalogScreenshotTest` (светлая и тёмная) —
компонент из каталога защищён от случайных изменений вида; новый компонент добавляй в каталог (`build-and-test.md` §«Тесты»).

Шапки экранов (`TabTopBar`, `ScreenTopBar`, выбор, поиск) — [`components-topbars.md`](components-topbars.md).
