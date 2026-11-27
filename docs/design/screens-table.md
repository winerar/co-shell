# Экран → доска → эталон (#382)

Сгенерировано `scripts/agent/screens_table.py` (не править руками). Эталон — `app/src/test/screenshots/<имя>_{light,dark}.png`.

| Экран | Доска | Эталон | светлая | тёмная |
|---|---|---|---|---|
| Обзор | Home | home | ✓ | ✓ |
| Обзор: что нового | Rel693Home | release_home | ✓ | ✓ |
| Истории релиза | Rel693S2 | release_story_2 | ✓ | ✓ |
| Обзор: первый запуск | HomeEmpty | home_empty | ✓ | ✓ |
| Обзор: нет курса | HomeAttention | home_attention | ✓ | ✓ |
| Обзор: бюджеты | BudHome | home_budget | ✓ | ✓ |
| Обзор: «Скоро» | Rec143HomeA | recurring_home | ✓ | ✓ |
| Обзор: итог месяца | Mon236A | home_review | ✓ | ✓ |
| Итоги месяца: истории | Rec554Top | month_recap | ✓ | ✓ |
| Итог месяца | Mon236Screen | month_review | ✓ | ✓ |
| Итог месяца: лимиты | Mon236Limits | month_review_limits | ✓ | ✓ |
| Операции | Operations | transactions | ✓ | ✓ |
| Операции: прокрутка | OpsScrolled | transactions_scrolled | ✓ | ✓ |
| Шторка ввода | Main | entry_sheet | ✓ | ✓ |
| Шторка: возврат | Fb208C | entry_refund | ✓ | ✓ |
| Шторка: теги | Fb129A | entry_tags | ✓ | ✓ |
| Шторка: QR чека | Qr526Filled | entry_scan | ✓ | ✓ |
| Шторка: категория по истории | EntryGuessHist | — | — | — |
| Шторка: «Провести» | Rec143Post | recurring_post | ✓ | ✓ |
| Счета | Accounts | accounts | ✓ | ✓ |
| Счета: пусто | AccountsEmpty | accounts_empty | ✓ | ✓ |
| Редактор счёта | AccountEdit | account_edit | ✓ | ✓ |
| Категории | Categories | categories | ✓ | ✓ |
| Редактор категории | CategoryEdit | category_edit | ✓ | ✓ |
| Курсы | Rates | rates | ✓ | ✓ |
| Конвертер: пара | Conv2PairType | converter_pair | ✓ | ✓ |
| Конвертер: все валюты | Conv2AllType | converter_all | ✓ | ✓ |
| Валюты отчётов | ReportCurrencies | report_currencies | ✓ | ✓ |
| Семья | Family | family | ✓ | ✓ |
| Семья: заполнена (10 из 10) | FamilyFull | — | — | — |
| Семья: участник | FamilyMemberSheet | — | — | — |
| Семья: исключить? | FamilyRemoveConfirm | — | — | — |
| Семья: выйти? | FamilyLeave | — | — | — |
| Семья: удалить семью? | FamilyDelete | — | — | — |
| Семья: удалить аккаунт? | FamilyDeleteAccount | — | — | — |
| Семья: вас убрали | FamilyRemoved | — | — | — |
| Код приглашения | AccessGate | access_code | ✓ | ✓ |
| Доступ закрыт | AccessLocked | access_locked | ✓ | ✓ |
| Настройки | Set1Root | settings | ✓ | ✓ |
| Внешний вид | Set1Look | appearance | ✓ | ✓ |
| Конфиденциальность | Priv145Settings | privacy | ✓ | ✓ |
| Вход по PIN | Priv145LockA | lock | ✓ | ✓ |
| Настройки: экраны | Set1Screens | settings_screens | ✓ | ✓ |
| Настройки: обзор | Set1Home | settings_home | ✓ | ✓ |
| Настройки: обзор, подробности | Set1HomeMore | settings_home_details | ✓ | ✓ |
| Настройки: нижняя панель | Set1Bar | settings_bar | ✓ | ✓ |
| Экспорт в CSV | Fb204Export | export | ✓ | ✓ |
| Настройки: счета в балансе | Set1Balance | settings_balance | ✓ | ✓ |
| Настройки: операции | Set1Ops | settings_ops | ✓ | ✓ |
| Настройки: отчёты | Set1Reports | settings_reports | ✓ | ✓ |
| Настройки: счета | Set1Accounts | settings_accounts | ✓ | ✓ |
| Настройки: деньги и даты | Set1Money | settings_money | ✓ | ✓ |
| Настройки: ввод операции | Set1Entry | settings_input | ✓ | ✓ |
| Настройки: уведомления | Set1Notices | settings_notices | ✓ | ✓ |
| Настройки: данные и копия | Set1Data | settings_data | ✓ | ✓ |
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
| Отчёты: подушка | Cush241Edge | reports_cushion | ✓ | ✓ |
| Быстрые заметки | Notes586Input | notes | ✓ | ✓ |
| Черновики | Notes586Drafts | drafts | ✓ | ✓ |
| Черновики: пусто | Notes586Empty2 | drafts_empty | ✓ | ✓ |
| Долги | DebtList | debts | ✓ | ✓ |
| Долги: пусто | DebtEmpty | debts_empty | ✓ | ✓ |
| Долг: новый | DebtNew | — | — | — |
| Долг: карточка | DebtDetail | — | — | — |
| Долг: вернули часть | DebtReturn | — | — | — |
| Цели | GoalList | goals | ✓ | ✓ |
| Цели: много | GoalMany | — | — | — |
| Цели: пусто | GoalEmpty | goals_empty | ✓ | ✓ |
| Цель: новая | GoalNew | — | — | — |
| Цель | GoalDetail | goal_edit | ✓ | ✓ |
| Обзор: цели | GoalHome | — | — | — |
| Обзор: сегодня можно | DailyHome | daily | ✓ | ✓ |
| Сегодня можно: как посчитано | DailyWhy | — | — | — |
| Сегодня можно: план потрачен | DailyOver | daily_over | ✓ | ✓ |
| Отчёты: необычное и оплата в другой валюте | SmartReports | smart_reports | ✓ | ✓ |
| Оплата в другой валюте: подробнее | SmartConversion | — | — | — |
| Отчёты: личная инфляция | SmartInflation | smart_reports | ✓ | ✓ |
| Итоги месяца: не как обычно | SmartRecap | — | — | — |
| Операции: регулярный платёж и дубль | SmartOps | smart_operations | — | ✓ |
| Похоже на дубль | SmartDuplicate | — | — | — |
| Новый лимит: обычно уходит | SmartLimit | budget_edit | ✓ | ✓ |
| Цели: что если | SmartGoal | smart_goals | — | ✓ |
| Что если | SmartWhatIf | smart_whatif_sheet | ✓ | — |
| Регулярные платежи | Rec143List | recurring_list | ✓ | ✓ |
| Шаблон: повтор | Rec143EditA | recurring_template | ✓ | ✓ |
| Импорт из Wallet | ImportStart | import_start | ✓ | ✓ |
| Отзыв | Feedback | feedback | ✓ | ✓ |
| Стереть всё | Wipe | wipe | ✓ | ✓ |

## Без полного набора эталонов

- Шторка: категория по истории (нет эталона: light, dark)
- Семья: заполнена (10 из 10) (нет эталона: light, dark)
- Семья: участник (нет эталона: light, dark)
- Семья: исключить? (нет эталона: light, dark)
- Семья: выйти? (нет эталона: light, dark)
- Семья: удалить семью? (нет эталона: light, dark)
- Семья: удалить аккаунт? (нет эталона: light, dark)
- Семья: вас убрали (нет эталона: light, dark)
- Долг: новый (нет эталона: light, dark)
- Долг: карточка (нет эталона: light, dark)
- Долг: вернули часть (нет эталона: light, dark)
- Цели: много (нет эталона: light, dark)
- Цель: новая (нет эталона: light, dark)
- Обзор: цели (нет эталона: light, dark)
- Сегодня можно: как посчитано (нет эталона: light, dark)
- Оплата в другой валюте: подробнее (нет эталона: light, dark)
- Итоги месяца: не как обычно (нет эталона: light, dark)
- Операции: регулярный платёж и дубль (smart_operations: light)
- Похоже на дубль (нет эталона: light, dark)
- Цели: что если (smart_goals: light)
- Что если (smart_whatif_sheet: dark)

## Сверка 1 в 1 (2026-11-16)

- Совпадают с досками: «Итог месяца» (+ лимиты), «Отчёты», «Все категории», «Категория», «Тренд», «Капитал по месяцам», «Валюты отчётов», «Обновление», «Семья», шторка ввода, «Провести».
- РЕШЕНО владельцем 2026-11-16 (опрос #464): пустые «Обзор» и «Счета» — код по доске (сделано); остальные расхождения закрыты перерисовкой досок по приложению (#470): «Счета», «Операции», редакторы счёта и категории (теперь = форма #290), «Курсы», «Ещё», «Резервная копия» (`Backup` выкл., `BackupAuto` вкл.), «Бюджеты», «Регулярные платежи».
- Настройки (2026-11-17): в коде пока только существующие пункты групп; остальное с досок `Set1*` (плотность, «Экраны», неделя, вибрация, Wi-Fi, время напоминаний) — по одному, `docs/design/settings-customization.md`.
- Интерактивные доски (`Wipe`, `Feedback`) в статике показывают первое состояние; эталон — состояние после тапа.
