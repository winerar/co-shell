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
| Бюджет: лимит | BudDetail | budget_detail | ✓ | ✓ |
| Бюджет: правка | BudEdit | budget_edit | ✓ | ✓ |
| Отчёты | RepA | reports | ✓ | ✓ |
| Отчёты: все категории | RepAll | report_categories | ✓ | ✓ |
| Отчёты: категория | RepDrill | report_category | ✓ | ✓ |
| Отчёты: тренд | Tr238DrillA | report_category_trend | ✓ | ✓ |
| Отчёты: картина | Rp189IeA | reports_picture | ✓ | ✓ |
| Капитал по месяцам | Rp189NwDrill | capital_month | ✓ | ✓ |
| Регулярные платежи | Rec143List | recurring_list | ✓ | ✓ |
| Шаблон: повтор | Rec143EditA | recurring_template | ✓ | ✓ |
| Импорт из Wallet | ImportStart | import_start | ✓ | ✓ |
| Отзыв | Feedback | feedback | ✓ | ✓ |
| Стереть всё | Wipe | wipe | ✓ | ✓ |

## Без полного набора эталонов


## Сверка 1 в 1 (2026-11-16)

- Совпадают с досками: «Итог месяца» (+ лимиты), «Отчёты», «Все категории», «Категория», «Тренд», «Капитал по месяцам», «Валюты отчётов», «Обновление», «Семья», шторка ввода, «Провести».
- РЕШЕНО владельцем 2026-11-16 (опрос #464): пустые «Обзор» и «Счета» — код по доске (сделано); остальные расхождения закрыты перерисовкой досок по приложению (#470): «Счета», «Операции», редакторы счёта и категории (теперь = форма #290), «Курсы», «Ещё», «Резервная копия» (`Backup` выкл., `BackupAuto` вкл.), «Бюджеты», «Регулярные платежи».
- «Внешний вид» (код) сопоставлен с доской `Settings` — это прежний экран; актуальные — `Fb154*`, `Fb271*`, `Fb385*`.
- Интерактивные доски (`Wipe`, `Feedback`) в статике показывают первое состояние; эталон — состояние после тапа.
