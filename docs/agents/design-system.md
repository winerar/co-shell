# Дизайн-система (`core:designsystem`)

Своя дизайн-система поверх Material 3, на которую по одному переезжают все экраны (ROADMAP
§«Дизайн-система»). Ответы дизайнеру/владельцу — `docs/research/design-questions.md`; решённое оттуда
переносится сюда и в токены.

## Что где лежит
```
core/designsystem/src/main/kotlin/app/coshell/core/designsystem/
  tokens/   Spacing (отступы), Radii (+ CoShellShapes для Material), Motion (длительности, кривые)
  theme/    CoShellTheme, цвета (Color.kt), шрифты (Fonts.kt), стили текста и сумм (Type.kt)
  catalog/  TokensCatalog — витрина токенов (@Preview светлая/тёмная); растёт в каталог компонентов
  category/ CategoryBadge, палитра и иконки категорий
core/designsystem/src/main/res/font/jetbrains_mono.ttf   шрифт цифр (лицензия — core/designsystem/fonts/)
```

## Токены — правила
- **Отступы** — только `Spacing.xxs…xxl` (2, 4, 8, 12, 16, 24, 32 dp; сетка 4 dp, кроме 2). Край экрана
  и промежуток между блоками — `Spacing.l`. Новые экраны — без чисел `N.dp` для отступов.
- **Скругления** — `Radii` (8/12/16/28 dp) или `MaterialTheme.shapes.*` (те же значения подставлены в тему).
- **Движение** — `Motion.short()/medium()/enter()/exit()` и `Motion.*_MS`; своих `tween(300)` в экранах нет.
  Анимировать только `alpha`/`translation`/`scale` (`performance.md` §«Анимации»), все токены ≤ 400 мс.
- **Цвета** — `MaterialTheme.colorScheme.*`; цвета сумм — `LocalAmountPalette` (доход/расход).
- Тест `core/designsystem/src/test/.../TokensTest.kt` следит за сеткой и порядком значений.

## Шрифты
- **Цифры и суммы — JetBrains Mono** (решение владельца, winerar/co-shell#25): моноширинный, цифры не
  прыгают при изменении. Стили: `AmountHeroStyle` (главный итог «Обзора», 40 sp, с автоуменьшением),
  `AmountTitleStyle` (22 sp), `AmountInputStyle` (поле ввода, 24 sp), `AmountTextStyle` (строки списков, 16 sp).
  Сумму в экране — только этими стилями, не `typography.*`.
- Файл — вариативный (одна ось `wght`, в коде веса 400/500/600) и **урезан** до цифр, латиницы и знаков
  валют: 77 КБ вместо 300 КБ (`performance.md` — бюджет APK). Кириллицы в нём нет — для дат словами он
  не подходит. Пересобрать (JetBrains Mono 2.304, `pip install fonttools`):
  ```
  pyftsubset "JetBrainsMono[wght].ttf" --output-file=jetbrains_mono.ttf --layout-features='tnum,zero,kern,calt' \
    --unicodes="U+0020-007E,U+00A0,U+00A2-00A5,U+00B1,U+00B7,U+00D7,U+2009,U+2013,U+2014,U+202F,U+2026,U+2192,U+2212,U+2248,U+20A0-20BF,U+058F"
  ```
  затем узкий пробел U+202F и U+2009 направить на глиф пробела (в шрифте их нет; разделитель тысяч
  `MoneyFormat.NARROW_NBSP`) — `fontTools`: в каждой unicode-таблице `cmap` `cmap[0x202F] = cmap[0x20]`.
- Шрифт основного текста — пока системный (Roboto); выбор — `design-questions.md` §3.

## Компоненты
- Сейчас общие блоки форм — в `app/.../ui/form/` (верхняя панель, кнопки, диалоги). Они переезжают в
  `core:designsystem` как свои компоненты поверх Material 3, когда будут ответы по формам и навигации.
- Новый компонент: без строк приложения (тексты — параметрами), с `@Preview` светлой и тёмной темы,
  добавить его в `catalog/`.

## Порядок переезда (план)
1. ✔ Токены отступов, скруглений, движения; шрифт цифр; витрина токенов.
2. Ответы на вопросы дизайнеру → цвета, шрифт текста, формы, навигация (`design-questions.md`).
3. Базовые компоненты: кнопки, поля, карточки, строки списка, диалоги, шторки, нижняя панель.
4. Экраны по одному: «Обзор» → ввод операции → «Операции» → остальные; после каждого — числа `dp` и
   `tween` в экране заменены токенами.
5. Правило в `code-style.md`: в экранах только компоненты и токены дизайн-системы.
