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
    ('Обзор', 'Home', 'home'), ('Обзор: первый запуск', 'HomeEmpty', 'home_empty'),
    ('Обзор: нет курса', 'HomeAttention', 'home_attention'), ('Обзор: бюджеты', 'BudHome', 'home_budget'),
    ('Обзор: «Скоро»', 'Rec143HomeA', 'recurring_home'), ('Обзор: итог месяца', 'Mon236A', 'home_review'),
    ('Итог месяца', 'Mon236Screen', 'month_review'), ('Итог месяца: лимиты', 'Mon236Limits', 'month_review_limits'),
    ('Операции', 'Operations', 'transactions'), ('Операции: прокрутка', 'OpsScrolled', 'transactions_scrolled'),
    ('Шторка ввода', 'Main', 'entry_sheet'), ('Шторка: возврат', 'Fb208C', 'entry_refund'),
    ('Шторка: теги', 'Fb129A', 'entry_tags'), ('Шторка: QR чека', 'Qr526Filled', 'entry_scan'), ('Шторка: «Провести»', 'Rec143Post', 'recurring_post'),
    ('Счета', 'Accounts', 'accounts'), ('Счета: пусто', 'AccountsEmpty', 'accounts_empty'),
    ('Редактор счёта', 'AccountEdit', 'account_edit'),
    ('Категории', 'Categories', 'categories'), ('Редактор категории', 'CategoryEdit', 'category_edit'),
    ('Курсы', 'Rates', 'rates'), ('Конвертер: пара', 'Conv2PairType', 'converter_pair'), ('Конвертер: все валюты', 'Conv2AllType', 'converter_all'), ('Валюты отчётов', 'ReportCurrencies', 'report_currencies'),
    ('Семья', 'Family', 'family'), ('Настройки', 'Set1Root', 'settings'), ('Внешний вид', 'Set1Look', 'appearance'),
    ('Настройки: экраны', 'Set1Screens', 'settings_screens'), ('Настройки: обзор', 'Set1Home', 'settings_home'), ('Настройки: обзор, подробности', 'Set1HomeMore', 'settings_home_details'),
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
    ('Регулярные платежи', 'Rec143List', 'recurring_list'), ('Шаблон: повтор', 'Rec143EditA', 'recurring_template'),
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
