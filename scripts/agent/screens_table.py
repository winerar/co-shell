#!/usr/bin/env python3
"""Table «screen → board → reference» (#382, release-checklist §5): writes docs/design/screens-table.md.

The mapping below is curated by hand (board = file in docs/design/mockups/, reference = base name of PNGs in
app/src/test/screenshots/ as `<base>_light.png` / `<base>_dark.png`). Files are checked on disk, so the table
cannot lie. `--check` fails when the file is stale or a named board does not exist.
"""
import glob, os, sys

ROOT = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
BOARDS = os.path.join(ROOT, 'docs/design/mockups')
SHOTS = os.path.join(ROOT, 'app/src/test/screenshots')
OUT = os.path.join(ROOT, 'docs/design/screens-table.md')

# (screen, board or None, reference base or None)
ROWS = [
    ('Обзор', 'Home', 'home'), ('Обзор: что нового', 'Rel693Home', 'release_home'), ('Истории релиза', 'Rel693S2', 'release_story_2'), ('Обзор: первый запуск', 'HomeEmpty', 'home_empty'),
    ('Обзор: нет курса', 'HomeAttention', 'home_attention'), ('Обзор: бюджеты', 'BudHome', 'home_budget'),
    ('Обзор: «Скоро»', 'Rec143HomeA', 'recurring_home'), ('Обзор: итог месяца', 'Mon236A', 'home_review'),
    ('Итоги месяца: истории', 'Rec554Top', 'month_recap'), ('Итог месяца', 'Mon236Screen', 'month_review'), ('Итог месяца: лимиты', 'Mon236Limits', 'month_review_limits'),
    ('Операции', 'Operations', 'transactions'), ('Операции: прокрутка', 'OpsScrolled', 'transactions_scrolled'),
    ('Шторка ввода', 'Main', 'entry_sheet'), ('Шторка: возврат', 'Fb208C', 'entry_refund'),
    ('Шторка: теги', 'Fb129A', 'entry_tags'), ('Шторка: QR чека', 'Qr526Filled', 'entry_scan'), ('Шторка: категория по истории', 'EntryGuessHist', None), ('Шторка: «Провести»', 'Rec143Post', 'recurring_post'),
    ('Счета', 'Accounts', 'accounts'), ('Счета: пусто', 'AccountsEmpty', 'accounts_empty'),
    ('Редактор счёта', 'AccountEdit', 'account_edit'),
    ('Категории', 'Categories', 'categories'), ('Редактор категории', 'CategoryEdit', 'category_edit'),
    ('Курсы', 'Rates', 'rates'), ('Конвертер: пара', 'Conv2PairType', 'converter_pair'), ('Конвертер: все валюты', 'Conv2AllType', 'converter_all'), ('Валюты отчётов', 'ReportCurrencies', 'report_currencies'),
    ('Семья', 'Family', 'family'), ('Семья: заполнена (10 из 10)', 'FamilyFull', None), ('Семья: участник', 'FamilyMemberSheet', None),
    ('Семья: исключить?', 'FamilyRemoveConfirm', None), ('Семья: выйти?', 'FamilyLeave', None), ('Семья: удалить семью?', 'FamilyDelete', None),
    ('Семья: удалить аккаунт?', 'FamilyDeleteAccount', None), ('Семья: вас убрали', 'FamilyRemoved', None),
    ('Код приглашения', 'AccessGate', 'access_code'), ('Доступ закрыт', 'AccessLocked', 'access_locked'), ('Настройки', 'Set1Root', 'settings'), ('Внешний вид', 'Set1Look', 'appearance'),
    ('Конфиденциальность', 'Priv145Settings', 'privacy'), ('Вход по PIN', 'Priv145LockA', 'lock'),
    ('Настройки: экраны', 'Set1Screens', 'settings_screens'), ('Настройки: обзор', 'Set1Home', 'settings_home'), ('Настройки: обзор, подробности', 'Set1HomeMore', 'settings_home_details'), ('Настройки: нижняя панель', 'Set1Bar', 'settings_bar'),
    ('Экспорт в CSV', 'Fb204Export', 'export'), ('Настройки: счета в балансе', 'Set1Balance', 'settings_balance'),
    ('Настройки: операции', 'Set1Ops', 'settings_ops'),
    ('Настройки: отчёты', 'Set1Reports', 'settings_reports'), ('Настройки: счета', 'Set1Accounts', 'settings_accounts'),
    ('Настройки: деньги и даты', 'Set1Money', 'settings_money'), ('Настройки: ввод операции', 'Set1Entry', 'settings_input'),
    ('Настройки: уведомления', 'Set1Notices', 'settings_notices'), ('Настройки: данные и копия', 'Set1Data', 'settings_data'),
   
   

    ('Резервная копия', 'Backup', 'backup'), ('Копия: автоматическая', 'BackupAuto', 'backup_auto'),
    ('Ещё', 'More', 'more'), ('Обновление', 'Update', 'update_available'),
    ('Бюджеты', 'BudA', 'budgets'), ('Бюджет: лимит', 'BudDetail', 'budget_detail'), ('Бюджет: правка', 'BudEdit', 'budget_edit'),
    ('Отчёты', 'RepA', 'reports'), ('Отчёты: все категории', 'RepAll', 'report_categories'),
    ('Отчёты: категория', 'RepDrill', 'report_category'), ('Отчёты: тренд', 'Tr238DrillA', 'report_category_trend'),
    ('Отчёты: картина', 'Rp189IeA', 'reports_picture'), ('Капитал по месяцам', 'Rp189NwDrill', 'capital_month'),
    ('Отчёты: подушка', 'Cush241Edge', 'reports_cushion'), ('Отчёты: постоянные траты', 'Fixed771Reports', 'reports_fixed'),
    ('Быстрые заметки', 'Notes586Input', 'notes'), ('Черновики', 'Notes586Drafts', 'drafts'), ('Черновики: пусто', 'Notes586Empty2', 'drafts_empty'),
    ('Долги', 'DebtList', 'debts'), ('Долги: пусто', 'DebtEmpty', 'debts_empty'), ('Долг: новый', 'DebtNew', None), ('Долг: карточка', 'DebtDetail', None), ('Долг: вернули часть', 'DebtReturn', None),
    ('Цели', 'GoalList', 'goals'), ('Цели: много', 'GoalMany', None), ('Цели: пусто', 'GoalEmpty', 'goals_empty'), ('Цель: новая', 'GoalNew', None), ('Цель', 'GoalDetail', 'goal_edit'), ('Обзор: цели', 'GoalHome', None),
    ('Обзор: сегодня можно', 'DailyHome', 'daily'), ('Сегодня можно: как посчитано', 'DailyWhy', None), ('Сегодня можно: план потрачен', 'DailyOver', 'daily_over'),
    ('Отчёты: необычное и оплата в другой валюте', 'SmartReports', 'smart_reports'), ('Оплата в другой валюте: подробнее', 'SmartConversion', None),
    ('Отчёты: личная инфляция', 'SmartInflation', 'smart_reports'), ('Итоги месяца: не как обычно', 'SmartRecap', None),
    ('Операции: регулярный платёж и дубль', 'SmartOps', 'smart_operations'), ('Похоже на дубль', 'SmartDuplicate', None),
    ('Помощь: жесты', 'Gest797', 'gestures'), ('Отчёты: прогноз остатка', 'Forecast770', 'forecast'), ('Прогноз остатка: не хватает', 'Forecast770Low', None),
    ('Новый лимит: обычно уходит', 'SmartLimit', 'budget_edit'), ('Цели: что если', 'SmartGoal', 'smart_goals'), ('Что если', 'SmartWhatIf', 'smart_whatif_sheet'),
    ('Регулярные платежи', 'Rec143List', 'recurring_list'), ('Регулярные: за год', 'Fixed771Year', 'recurring_year'), ('Шаблон: повтор', 'Rec143EditA', 'recurring_template'),
    ('Импорт из Wallet', 'ImportStart', 'import_start'), ('Отзыв', 'Feedback', 'feedback'), ('Стереть всё', 'Wipe', 'wipe'),
]


# Review 2026-11-16 (#382): board (dark, 390 dp, rendered by `agent.sh render`) next to the reference, one by one.
FINDINGS = [
    'Совпадают с досками: «Итог месяца» (+ лимиты), «Отчёты», «Все категории», «Категория», «Тренд», «Капитал по месяцам», «Валюты отчётов», «Обновление», «Семья», шторка ввода, «Провести».',
    'РЕШЕНО владельцем 2026-11-16 (опрос #464): пустые «Обзор» и «Счета» — код по доске (сделано); остальные расхождения закрыты перерисовкой досок по приложению (#470): «Счета», «Операции», редакторы счёта и категории (теперь = форма #290), «Курсы», «Ещё», «Резервная копия» (`Backup` выкл., `BackupAuto` вкл.), «Бюджеты», «Регулярные платежи».',
    'Настройки (2026-11-17): в коде пока только существующие пункты групп; остальное с досок `Set1*` (плотность, «Экраны», неделя, вибрация, Wi-Fi, время напоминаний) — по одному, `docs/design/settings-customization.md`.',
    'Интерактивные доски (`Wipe`, `Feedback`) в статике показывают первое состояние; эталон — состояние после тапа.',
]


def build():
    lines, bad = [], []
    gaps = []
    for screen, board, ref in ROWS:
        if board and not os.path.exists(f'{BOARDS}/{board}.dc.html'):
            bad.append(f'{screen}: нет доски {board}')
        has = {t: bool(ref) and os.path.exists(f'{SHOTS}/{ref}_{t}.png') for t in ('light', 'dark')}
        mark = lambda ok: '✓' if ok else '—'
        lines.append(f'| {screen} | {board or "—"} | {ref or "—"} | {mark(has["light"])} | {mark(has["dark"])} |')
        if not (has['light'] and has['dark']):
            gaps.append(f'{screen} ({ref or "нет эталона"}: ' + ', '.join(t for t, ok in has.items() if not ok) + ')')
    head = ['# Экран → доска → эталон (#382)', '',
            'Сгенерировано `scripts/agent/screens_table.py` (не править руками). Эталон — `app/src/test/screenshots/<имя>_{light,dark}.png`.', '',
            '| Экран | Доска | Эталон | светлая | тёмная |', '|---|---|---|---|---|']
    tail = ['', '## Без полного набора эталонов', ''] + [f'- {g}' for g in gaps]
    tail += ['', '## Сверка 1 в 1 (2026-11-16)', ''] + [f'- {f}' for f in FINDINGS]
    return '\n'.join(head + lines + tail) + '\n', bad, gaps


if __name__ == '__main__':
    text, bad, gaps = build()
    if '--check' in sys.argv:
        stale = not os.path.exists(OUT) or open(OUT).read() != text
        for b in bad: print('✗', b)
        if stale: print('✗ docs/design/screens-table.md устарел — scripts/agent/screens_table.py')
        sys.exit(1 if bad or stale else 0)
    open(OUT, 'w').write(text)
    print(f'{len(ROWS)} rows, {len(gaps)} with gaps')
    for b in bad: print('✗', b)
