# Движение и тактильность (`core:designsystem/motion/`, `tokens/Motion`)

Решения владельца: §6 `docs/research/design-questions.md` («живые» мелочи) и 2026-11-09 — «каждая
интерактивная мелочь живая, настоящая, объёмная, единой системой».

- **Темп — только пружины** (решение владельца 2026-11-09: «скорость анимации унифицировать») —
  `tokens/Motion.kt`, длительностей и кривых нет. Два семейства, как в Material 3 Expressive:
  `spatialFast/spatial/spatialSlow` (положение, размер, масштаб; ζ 0.9, k 1400/700/300),
  `effectsFast/effects/effectsSlow` (прозрачность, цвет; ζ 1, k 3800/1600/800) и `bouncy` (ζ 0.55, k 420 —
  отпускание нажатия, «щелчок на место»). Типизированные: `Motion.Resize` (IntSize), `Placement` (IntOffset —
  сдвиги экранов, цифр, строк), `Turn`. Где что: нажатие, бегунок, пилюля → `spatialFast`; шторка, экран,
  раскрытие, размеры → `spatial`; проявление → `effects`; уход/растворение → `effectsFast` (внутри экрана) или
  `effectsSlow` (уходящий экран — чтобы движение успели увидеть); блик → `effectsFast`/`effectsSlow`.
  `tween`/`spring`/кривые вне `Motion.kt` ловят `MotionTokensRuleTest` (designsystem) и `TokensRuleTest` (app).
  `MotionScheme` Material в 1.4.0 закрыт (`internal`) — встроенные компоненты пока на своих пружинах.
  Анимировать `alpha`/`translation`/`scale`/`rotation`, размер — только через примитивы ниже (`performance.md`).
  Главный итог «Обзора» — `amount/RollingAmountText` (цифры перелистываются, ужимается по ширине).
- **Размеры (#64, решение владельца: «всё, что меняет форму или размер, — с анимацией»)** — `motion/Resize.kt`:
  ничего не прыгает. Компоненты делают это **сами** (`smoothResize()` внутри `CoShellCard`, `CoShellListRow`,
  полей, кнопок, чипов). В экранах: выпадающая часть блока — `CoShellExpandable(expanded)` (раскрывается сверху
  + проявляется), стрелка «раскрыть» — `CoShellExpandIcon`, строки ленивых списков — `listItemMotion()` (нужен
  `key`), свой блок — `Modifier.smoothResize()`. Внутри колонки с `spacedBy` отступ кладём *внутрь* раскрывашки
  (`padding(top)`), а её — в колонку, где всегда что-то видно, иначе прыгает зазор. Голые `AnimatedVisibility`,
  `animateContentSize`, `animateItem` в экранах ловит `app/.../designsystem/MotionRuleTest`.
- **Откуда открывается (#63)** — каждый `tactile`-элемент пишет точку касания в `motion/PressOrigin` (живёт 1 с,
  берётся один раз). Экран **вырастает из нажатой кнопки/строки** (масштаб 0.85 → 1 + проявление, старый
  отступает до 0.94), «назад» — **сжимается обратно** в ту же точку (и следует predictive back);
  `navigation/ScreenTransitions` + `ScreenOrigins`. Без касания (встряхивание) — сдвиг на 1/8;
  вкладки нижней панели — **общая ось X**: новая въезжает на 1/8 со стороны своей вкладки, старая уходит в
  другую сторону (`tabDirection`, тест `TabDirectionTest`). Шторка (`CoShellSheet`) **вырастает из точки касания**
  (`motion/GrowFrom`, 0.3 → 1 у нижнего края); из «+» (`CoShellFab` передаёт `originColor`) ещё и начинается
  его мятным цветом, который растворяется в фон шторки.
- **Тактильная система** (решение владельца: «каждая интерактивная мелочь живая, настоящая, объёмная — единой
  системой») — `motion/`: нажатый элемент **проседает**, **наклоняется к пальцу** (3D-поворот), под пальцем
  **вспыхивает блик** (`LocalTactileLight`: белый в тёмной, мятный в светлой) и отпускается **пружиной с
  перелётом**. Уровни одной системы (`Tactile`): `Primary` («+», «Сохранить»), `Control` (кнопки, чипы, сегменты,
  переключатели, вкладки, образцы цвета/иконок), `Surface` (карточки), `Row` (строки списков — без наклона).
  Выбор элемента — короткий «щелчок на место» (`selectionPop`). Рябь Material в наших компонентах выключена.
- **Правило:** в экранах только `CoShellButton/OutlinedButton/TextButton/IconButton/Fab`, `CoShellFilterChip`,
  `CoShellSegmentedButton`, `CoShellSwitch`, `CoShellNavigationBarItem`, `CoShellCard(onClick)` и
  `Modifier.tactileClickable/tactileToggleable` (первым в цепочке). Голые Material-кнопки и `clickable`
  ловит тест `app/.../designsystem/TactileRuleTest`. Новый интерактивный компонент — обёрткой в `components/`.
- **Вибрация** — только ключевые моменты: сохранение операции (`Confirm`), свайп дошёл до «удалить»
  (`GestureThresholdActivate`), встряхивание (сила в настройках). Не на каждое нажатие.
