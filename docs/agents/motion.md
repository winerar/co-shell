# Движение и тактильность (`core:designsystem/motion/`, `tokens/Motion`)

Решения владельца: §6 `docs/research/design-questions.md` («живые» мелочи) и 2026-11-09 — «каждая
интерактивная мелочь живая, настоящая, объёмная, единой системой».

- **Движение** (решение владельца, §6: «живые мелочи») — `Motion.short()/medium()/enter()/exit()` и
  `Motion.*_MS`; своих `tween(300)` в экранах нет. Анимировать только `alpha`/`translation`/`scale`/`rotation`
  (`performance.md` §«Анимации»), все токены ≤ 400 мс. Переходы экранов — сдвиг на 1/10 + растворение
  (`navigation/ScreenTransitions`); строки списков — `Modifier.animateItem()`; главный итог «Обзора» —
  `amount/RollingAmountText` (цифры перелистываются вверх/вниз, сам ужимается по ширине).
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
