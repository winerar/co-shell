# Экран → доска → эталон (#382)

Сгенерировано `scripts/agent/screens_table.py` (не править руками). Эталон — `app/src/test/screenshots/<имя>_{light,dark}.png`.

| Экран | Доска | Эталон | светлая | тёмная |
|---|---|---|---|---|
| Обзор | Home | home | ✓ | ✓ |
| Обзор: первый запуск | HomeEmpty | home_empty | ✓ | ✓ |
| Обзор: нет курса | HomeAttention | home_attention | ✓ | ✓ |
| Обзор: бюджеты | BudHome | home_budget | ✓ | ✓ |
| Обзор: «Скоро» | Rec143HomeA | recurring_home | ✓ | ✓ |
| Обзор: итог месяца | Mon236A | home_review | ✓ | ✓ |
| Итог месяца | Mon236Screen | month_review | ✓ | ✓ |
| Итог месяца: лимиты | Mon236Limits | month_review_limits | ✓ | ✓ |
| Операции | Operations | transactions | ✓ | ✓ |
| Операции: прокрутка | OpsScrolled | transactions_scrolled | ✓ | ✓ |
| Шторка ввода | Main | entry_sheet | ✓ | ✓ |
| Шторка: возврат | Fb208C | entry_refund | ✓ | ✓ |
| Шторка: теги | Fb129A | entry_tags | ✓ | ✓ |
| Шторка: «Провести» | Rec143Post | recurring_post | ✓ | ✓ |
| Счета | Accounts | accounts | ✓ | ✓ |
| Счета: пусто | AccountsEmpty | accounts_empty | ✓ | ✓ |
| Редактор счёта | AccountEdit | account_edit | ✓ | ✓ |
| Категории | Categories | categories | ✓ | ✓ |
| Редактор категории | CategoryEdit | category_edit | ✓ | ✓ |
| Курсы | Rates | rates | ✓ | ✓ |
| Валюты отчётов | ReportCurrencies | report_currencies | ✓ | ✓ |
| Семья | Family | family | ✓ | ✓ |
| Внешний вид | Settings | appearance | ✓ | ✓ |
| Резервная копия | Backup | backup | ✓ | ✓ |
| Копия: автоматическая | BackupAuto | backup_auto | ✓ | ✓ |
| Ещё | More | more | ✓ | ✓ |
| Обновление | Update | update_available | ✓ | ✓ |
| Бюджеты | BudA | budgets | ✓ | ✓ |
| Бюджет: лимит | BudDetail | — | — | — |
| Бюджет: правка | BudEdit | — | — | — |
| Отчёты | RepA | reports | ✓ | ✓ |
| Отчёты: все категории | RepAll | report_categories | ✓ | ✓ |
| Отчёты: категория | RepDrill | report_category | ✓ | ✓ |
| Отчёты: тренд | Tr238DrillA | report_category_trend | ✓ | ✓ |
| Отчёты: картина | Rp189IeA | reports_picture | ✓ | ✓ |
| Капитал по месяцам | Rp189NwDrill | capital_month | ✓ | ✓ |
| Регулярные платежи | Rec143List | recurring_list | ✓ | ✓ |
| Шаблон: повтор | Rec143EditA | recurring_template | ✓ | ✓ |
| Импорт из Wallet | ImportStart | — | — | — |
| Отзыв | Feedback | feedback | ✓ | ✓ |
| Стереть всё | Wipe | wipe | ✓ | ✓ |

## Без полного набора эталонов

- Бюджет: лимит (нет эталона: light, dark)
- Бюджет: правка (нет эталона: light, dark)
- Импорт из Wallet (нет эталона: light, dark)

## Сверка 1 в 1 (2026-11-16)

- Совпадают с досками: «Итог месяца» (+ лимиты), «Отчёты», «Все категории», «Категория», «Тренд», «Капитал по месяцам», «Валюты отчётов», «Обновление», «Семья», шторка ввода, «Провести».
- «Обзор» и «Счета» без данных: на доске карточка (баланс) или значок над текстом и кнопка «+ Добавить счёт»; в коде — только текст и кнопка.
- «Счета»: на доске «+» в шапке и вкладка «Чьи»; в коде круглая «+» внизу справа (`CoShellAddFab`, `AddButtonRuleTest`) — доска старше правила.
- «Операции»: фильтры на доске — чипы «Расходы ✕ / Счёт / Ноябрь»; в коде «Тип / Категория / Период / Счёт» (доска старше Fb141A).
- «Редактор счёта», «Редактор категории»: код — форма без прокрутки (#290, `Fb290*`), доски `AccountEdit`/`CategoryEdit` — прежняя форма.
- «Курсы»: в коде сверху конвертер «Сколько это», на доске его нет; «Ещё», «Резервная копия», «Бюджеты», «Регулярные платежи» — доски старше кода (плитки вместо строк, кольцо «дней до копии» и т. п.).
- «Внешний вид» (код) сопоставлен с доской `Settings` — это прежний экран; актуальные — `Fb154*`, `Fb271*`, `Fb385*`.
- Нет эталона: `BudDetail`, `BudEdit`, `ImportStart` (нужны образцы данных).
- Интерактивные доски (`Wipe`, `Feedback`) в статике показывают первое состояние; эталон — состояние после тапа.
