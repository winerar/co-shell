# Клавиатура суммы и поле суммы

Часть `components.md`: как вводится число в приложении.

- **Клавиатура суммы** — `components/keypad/CoShellKeypad` (решение владельца 2026-11-09: «удобно одним пальцем»):
  3×4 цифры, `000`, `,` (скрыта у валют без дробной части); правая колонка «стереть · `+` · `−` · «Готово»» (опрос 2026-11-18, #291: «Готово» в один ряд, все клавиши ≥ 48 dp; без `+`/`−` — «Готово» высокая);
  фиксированная высота (шторка не прыгает; `compact` — клавиши 48 и зазор 6 на коротком телефоне ниже `Sizes.shortScreen`, #818), каждая клавиша — тактильная, тик клавиатуры. Логика ввода —
  чистая `AmountKeys.apply/normalize/display` (тест `AmountKeysTest`): текст вида `"32000 + 1500,5 − 200"` (`AmountKeys.terms` — слагаемые со знаками); сумму
  считает экран (`parsePositiveAmount` в `transactions/edit/ReceiptForm.kt`, итог должен быть больше нуля; конвертер — не ниже нуля).
- **Сумма в редакторе (#300):** `CoShellTextField`/`CoShellAmountField(onClick)` — поле только показывает число, нажатие зовёт `onClick`, системной клавиатуры нет;
  в `app` — `ui/form/AmountSheetField` (поле + шторка `AmountSheet` с `CoShellKeypad`, без «+»/«−»; доска `Amount300Sheet`, тест `AmountSheetFieldTest`). Новое поле суммы в редакторе — только им;
  баланс, который может быть ниже нуля (начальный, текущий, Wallet), — `signed = true`: над числом «В плюсе · В минусе» (`CoShellSegmented`, опрос 2026-11-26, В; доска `Amount300Sign`), число без знака, минус ставит `AmountSign.signed` (`core:model`).
- **Остальные числа в редакторах (2026-11-26) — тоже без системной клавиатуры:** день месяца 1–31 — `ui/form/DaySheetField` (сетка `CoShellDayGrid`, `clearable` — «Убрать» у необязательного: день выписки);
  проценты, дни, цифры карты — `ui/form/NumberSheetField` / `NumberSheet` (`CoShellKeypad` без «+»/«−», логика `NumberKeys`: без группировки, нули впереди сохраняются, `maxWhole`/`fractionDigits`; тест `NumberKeysTest`).
  `KeyboardType.Number/Decimal` в `app` больше нет — новое числовое поле только так.
