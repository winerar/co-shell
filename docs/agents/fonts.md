# Шрифты дизайн-системы

Часть `design-system.md`; решения владельца — `docs/research/design-questions.md` §3.

## Шрифты (решения владельца — `design-questions.md` §3)
- **Текст — Onest** (`TextFontFamily`): вся `MaterialTheme.typography` — шкала Material 3 в Onest; заголовки
  SemiBold, подписи кнопок/вкладок Medium, основной текст Regular. Отдельные размеры в экранах не задавать.
  Файл вариативный (`wght`), урезан до латиницы, кириллицы и знаков — 77 КБ. Пересобрать (Onest из
  google/fonts `ofl/onest/Onest[wght].ttf`):
  ```
  pyftsubset "Onest[wght].ttf" --output-file=onest.ttf --layout-features='kern,liga,calt,tnum,case,locl' \
    --unicodes="U+0020-007E,U+00A0-00FF,U+0401,U+0404,U+0406,U+0407,U+0410-044F,U+0451,U+0454,U+0456,U+0457,U+0490,U+0491,U+2009,U+2013,U+2014,U+2018-201E,U+2022,U+2026,U+202F,U+2039,U+203A,U+20AC,U+20BD,U+2116,U+2190-2193,U+2212,U+2248,U+2260,U+2264,U+2265"
  ```
- **Суммы на экране — `AmountText`** (`amount/`): код валюты после числа («1 234,50 USD») мельче (0.8) и
  серый `onSurfaceVariant`; копейки того же размера и цвета, что и сумма. `minFontSize` — автоуменьшение
  длинной суммы. Символ перед числом ($) не используется: у USD/ARS/USDT он одинаковый.
- **Курсы** — тем же моноширинным шрифтом: `RateTextStyle` («1 USD = 1 234,5 ARS»). Даты и прочие числа
  в тексте — Onest.
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
