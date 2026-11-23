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
| Итоги месяца: истории | Rec554Top | month_recap | ✓ | ✓ |
| Итог месяца | Mon236Screen | month_review | ✓ | ✓ |
| Итог месяца: лимиты | Mon236Limits | month_review_limits | ✓ | ✓ |
| Операции | Operations | transactions | ✓ | ✓ |
| Операции: прокрутка | OpsScrolled | transactions_scrolled | ✓ | ✓ |
| Шторка ввода | Main | entry_sheet | ✓ | ✓ |
| Шторка: возврат | Fb208C | entry_refund | ✓ | ✓ |
| Шторка: теги | Fb129A | entry_tags | ✓ | ✓ |
| Шторка: QR чека | Qr526Filled | entry_scan | ✓ | ✓ |
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
| Настройки | Set1Root | settings | ✓ | ✓ |
| Внешний вид | Set1Look | appearance | ✓ | ✓ |
| Конфиденциальность | Priv145Settings | privacy | ✓ | ✓ |
| Вход по PIN | Priv145LockA | lock | ✓ | ✓ |
| Настройки: экраны | Set1Screens | settings_screens | ✓ | ✓ |
| Настройки: обзор | Set1Home | settings_home | ✓ | ✓ |
| Настройки: обзор, подробности | Set1HomeMore | settings_home_details | ✓ | ✓ |
| Экспорт в CSV | Fb204Export | export | ✓ | ✓ |
| Настройки: счета в балансе | Set1Balance | settings_balance | ✓ | ✓ |
| Настройки: операции | Set1Ops | settings_ops | ✓ | ✓ |
| Настройки: отчёты | Set1Reports | settings_reports | ✓ | ✓ |
| Настройки: счета | Set1Accounts | settings_accounts | ✓ | ✓ |
| Настройки: деньги и даты | Set1Money | settings_money | ✓ | ✓ |
| Настройки: ввод операции | Set1Entry | settings_input | ✓ | ✓ |
| Настройки: уведомления | Set1Notices | settings_notices | ✓ | ✓ |
| Настройки: данные и копия | Set1Data | settings_data | ✓ | ✓ |
| ИИ на телефоне | AiOff | ai_addon | ✓ | ✓ |
| ИИ на телефоне: шаги | AiStep2 | ai_addon_loading | ✓ | ✓ |
| ИИ на телефоне: скачивание дополнения | AiStep1Load | ai_addon_addon_loading | — | ✓ |
| ИИ на телефоне: включён | AiOn | ai_addon_on | ✓ | ✓ |
| ИИ на телефоне: слабый телефон | AiWeak | ai_addon_weak | ✓ | ✓ |
| Облачный разбор | CloudOff | cloud_ai | ✓ | ✓ |
| Облачный разбор: включён | CloudOn | cloud_ai_on | ✓ | ✓ |
| Облачный разбор: вопрос | CloudConsent | cloud_ai_consent | ✓ | ✓ |
| Облачный разбор: нужен вход | CloudNoLogin | cloud_ai_nologin | ✓ | ✓ |
| Быстрые заметки: облако | CloudNotes | — | — | — |
| Быстрые заметки: читает облако | CloudBusy | — | — | — |
| Черновики: лимит облака | CloudLimit | — | — | — |
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
| Быстрые заметки | Notes586Input | notes | ✓ | ✓ |
| Черновики | Notes586Drafts | drafts | ✓ | ✓ |
| Черновики: пусто | Notes586Empty2 | drafts_empty | ✓ | ✓ |
| Регулярные платежи | Rec143List | recurring_list | ✓ | ✓ |
| Шаблон: повтор | Rec143EditA | recurring_template | ✓ | ✓ |
| Импорт из Wallet | ImportStart | import_start | ✓ | ✓ |
| Отзыв | Feedback | feedback | ✓ | ✓ |
| Стереть всё | Wipe | wipe | ✓ | ✓ |

## Без полного набора эталонов

- ИИ на телефоне: скачивание дополнения (ai_addon_addon_loading: light)
- Быстрые заметки: облако (нет эталона: light, dark)
- Быстрые заметки: читает облако (нет эталона: light, dark)
- Черновики: лимит облака (нет эталона: light, dark)

## Сверка 1 в 1 (2026-11-16)

- Совпадают с досками: «Итог месяца» (+ лимиты), «Отчёты», «Все категории», «Категория», «Тренд», «Капитал по месяцам», «Валюты отчётов», «Обновление», «Семья», шторка ввода, «Провести».
- РЕШЕНО владельцем 2026-11-16 (опрос #464): пустые «Обзор» и «Счета» — код по доске (сделано); остальные расхождения закрыты перерисовкой досок по приложению (#470): «Счета», «Операции», редакторы счёта и категории (теперь = форма #290), «Курсы», «Ещё», «Резервная копия» (`Backup` выкл., `BackupAuto` вкл.), «Бюджеты», «Регулярные платежи».
- Настройки (2026-11-17): в коде пока только существующие пункты групп; остальное с досок `Set1*` (плотность, «Экраны», неделя, вибрация, Wi-Fi, время напоминаний) — по одному, `docs/design/settings-customization.md`.
- Интерактивные доски (`Wipe`, `Feedback`) в статике показывают первое состояние; эталон — состояние после тапа.
