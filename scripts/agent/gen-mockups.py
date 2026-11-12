#!/usr/bin/env python3
"""Regenerate the design canvas boards and layout (docs/design/mockups/).

1. Writes every board from bricks (mockup_bricks.py → mockup_parts.py → mockup_screens_*.py, interactivity —
   mockup_logic.py). No hand-drawn boards: a board edited on the canvas is overwritten — move the change here.
2. Regenerates the «Воркфлоу» board (gen-flows-board.py).
3. Runs mockup_theme.py over every board, so all follow the canvas theme switch.
4. Writes canvas.json: pages and rows from LAYOUT below (keeps `createdOnFiles`).

Usage: python3 scripts/agent/gen-mockups.py   — then publish to the canvas (docs/design/README.md).
"""
import json
import os
import subprocess
import sys

HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, HERE)
import mockup_screens_main as main_screens  # noqa: E402
import mockup_screens_more as more_screens  # noqa: E402
import mockup_screens_ideas as idea_screens  # noqa: E402
import mockup_screens_feedback as fb_screens  # noqa: E402
import mockup_screens_templates as tpl_screens  # noqa: E402
import mockup_screens_budgets as bud_screens  # noqa: E402
import mockup_screens_reports as rep_screens  # noqa: E402
import mockup_special as special  # noqa: E402
import mockup_screens_tabs as tab_screens  # noqa: E402
import mockup_screens_ops as ops_screens  # noqa: E402
import mockup_screens_forms as form_screens  # noqa: E402
import mockup_screens_settings as set_screens  # noqa: E402
import mockup_catalog as catalog  # noqa: E402
import mockup_theme  # noqa: E402
from mockup_bricks import board  # noqa: E402

PLAN = (tpl_screens, bud_screens, rep_screens)  # rows «Шаблоны», «Бюджеты», «Отчёты» on «Экраны» (picked А, #166)

OUT = os.path.normpath(os.path.join(HERE, '../../docs/design/mockups'))

# Main boards of each row: full pages with their own logic (title, interactive) — mockup_screens_{tabs,ops,forms,settings}.py.
PAGES = {
    'Main': ('Шторка ввода: шаг 1 счёт → шаг 2 сумма (тапайте)', True), 'Operations': ('Операции: удержание → выбор, свайп вкладок (тапайте)', True),
    'Accounts': ('Счета: 12 счетов, группы (тапайте)', True), 'More': ('Ещё', False), 'HomeSync': ('Обзор: ошибка синка (тапайте «Другая причина»)', True),
    'Feedback': ('Отзыв: написать (тапайте)', True), 'Update': ('Обновление (тапайте кнопку — состояния)', True), 'UpdatePill': ('Пилюля «есть обновление»', True),
    'Colors': ('Цвет счёта: банки + палитра', True), 'Categories': ('Категории (тапайте)', True), 'CategoryEdit': ('Категория | Папка (тапайте)', True),
    'AccountEdit': ('Счёт: новый (тапайте)', True), 'Rates': ('Курсы валют', False), 'ReportCurrencies': ('Валюты отчётов', True), 'Family': ('Семья: в семье', False),
    'Backup': ('Резервная копия', False), 'Wipe': ('Стереть все данные (тапайте)', True),
    'Components': ('Компоненты: кирпичи макетов → имена в коде', True), 'History': ('История решений', False),
}
PAGE_MODULES = (tab_screens, ops_screens, form_screens, set_screens, catalog)

LAYOUT = [  # (row title, [board names]) on the «Экраны» page
    ('Обзор', ['Home', 'HomeEmpty', 'HomeAttention', 'HomeSync', 'UpdateWhatsNew']),
    ('Операции', ['Operations', 'OpsSelect', 'OpsUndo', 'OpsEmpty', 'OpsNothing']),
    ('Новая операция — шторка', ['Main', 'EntryTransfer', 'EntryReceipt', 'EntryEdit', 'EntryError', 'EntryNoAccounts']),
    ('Счета и «Ещё»', ['Accounts', 'AccountsSearch', 'AccountsEmpty', 'More', 'UpdatePill', 'Update']),
    ('Счёт и категории', ['AccountEdit', 'AccountEditExisting', 'AccountDelete', 'AccountBlocked', 'Colors', 'Categories', 'CategoryEdit', 'CategoryBlocked']),
    ('Курсы и валюты', ['Rates', 'RatesRefreshing', 'RatesFailed', 'RatesEmpty', 'RatesManual', 'ReportCurrencies', 'CurrencyAdd']),
    ('Семья и синхронизация', ['FamilySignedOut', 'FamilyNoAccess', 'FamilyInvited', 'FamilyJoinMode', 'Family', 'FamilyInviteSent', 'FamilyInviteDialog', 'FamilyOffline', 'FamilyNotConfigured']),
    ('Настройки, копия, стирание, отзыв', ['Settings', 'Backup', 'BackupAuto', 'BackupError', 'BackupRestore', 'Wipe', 'WipeFailed', 'Feedback', 'FeedbackSent', 'FeedbackError']),
    ('Импорт из Wallet', ['ImportStart', 'ImportError', 'ImportSummary', 'ImportAccounts', 'ImportCategories', 'ImportTransfers', 'ImportPreview', 'ImportDone']),
]

STICKIES = {  # row index -> sticky text (placed after the row)
    0: 'Обзор — 2 плитки (решение владельца 2026-11-10). Плашка синка появляется, только если синк не проходит дольше 2 минут или вход/доступ потерян; сама исчезает после удачной синхронизации.',
    1: 'Выбран 19В (2026-11-10): строки без свайпов; удержание — выбор нескольких, панель «Дублировать · Удалить · N». Тяните экран влево/вправо — соседняя вкладка. Тап по строке — шторка операции.',
    2: 'Шторка: шаг 1 — тип и счёт (недавние сверху, поиск внизу под пальцем), шаг 2 — своя клавиатура, сумма и категории над ней. Всё нажимаемое — в нижних 2/3.',
    3: 'Обновление вышло, пока приложение открыто: над панелью пилюля «Версия 43 · Обновить», на «Ещё» — точка. Тап — скачивание и экран обновления. × — до следующего запуска.',
    7: 'Палитры на доске «Настройки» перекрашивают весь холст — как в приложении.',
}

PITCH_X, PITCH_Y, W, H = 470, 1240, 390, 844
for _m in PLAN:
    STICKIES[len(LAYOUT)] = _m.ROW[2]
    LAYOUT.append((_m.ROW[1], [b[0] for b in _m.BOARDS]))


def write(name, html):
    with open(os.path.join(OUT, name + '.dc.html'), 'w', encoding='utf-8') as f:
        f.write(html.replace('{{div}}', ''))


def main():
    gen = {}
    for mod in PAGE_MODULES:
        for name, fn in mod.BOARDS:
            write(name, fn())
            gen[name] = PAGES[name]
    for mod in (main_screens, more_screens):
        for name, title, fn in mod.BOARDS:
            write(name, board(title.split(' · ', 1)[-1], fn()))
            gen[name] = (title.split(' · ', 1)[-1], False)
    plan_boards = [b for m in PLAN for b in m.BOARDS]
    for name, title, fn, _ in idea_screens.BOARDS + fb_screens.BOARDS + plan_boards:
        write(name, board(title, fn()))
        gen[name] = (title, False)
    for name, fn, title in [('ThemeSwitch', special.theme_switch, 'Тема макетов'), ('ThemeSwitchFlows', special.theme_switch, 'Тема макетов'),
                            ('ThemeSwitchComponents', special.theme_switch, 'Тема макетов'), ('Settings', special.settings, 'Настройки: тема и палитра перекрашивают холст'),
                            ('Ideas', special.ideas, 'Предложения по улучшению')]:
        write(name, fn())
        gen[name] = (title, name != 'Ideas')
    flows_h = int(subprocess.run([sys.executable, os.path.join(HERE, 'gen-flows-board.py')], check=True, capture_output=True, text=True).stdout.split()[-1])
    files = sorted(f for f in os.listdir(OUT) if f.endswith('.dc.html'))
    for f in files:
        p = os.path.join(OUT, f)
        with open(p, encoding='utf-8') as fh:
            src = fh.read()
        with open(p, 'w', encoding='utf-8') as fh:
            fh.write(mockup_theme.convert(src))

    titles = dict(gen, Flows=('Воркфлоу', False))
    canvas_path = os.path.join(OUT, 'canvas.json')
    old = json.load(open(canvas_path, encoding='utf-8'))
    boards, notes, order = {}, {}, []

    def add(name, x, y, w, h, page, radius=None):
        key = name + '.dc.html'
        title, inter = titles.get(name, (name, False))
        b = {'x': x, 'y': y, 'w': w, 'h': h, 'title': title, 'page': page}
        if inter:
            b['is_interactive'] = True
        if radius:
            b['radius'] = radius
        boards[key] = b
        order.append(key)

    add('ThemeSwitch', -520, 0, W, 560, 'screens', 28)
    notes['t'] = {'kind': 'title1', 'page': 'screens', 'text': 'Co-Shell · экраны и состояния', 'x': 0, 'y': -560, 'maxW': 3000, 'w': 240}
    notes['nav'] = {'page': 'screens', 'text': 'Вкладки холста: Экраны · Воркфлоу · Компоненты · Предложения · История. Слева — «Тема макетов»: перекрашивает все доски. '
                    'Оглавление в репозитории — docs/design/README.md.', 'x': -520, 'y': 600, 'w': 390}
    for r, (title, names) in enumerate(LAYOUT):
        y = r * PITCH_Y
        notes['row%d' % r] = {'kind': 'title1', 'page': 'screens', 'text': title, 'x': 0, 'y': y - 250, 'maxW': PITCH_X * len(names) - 80, 'w': 240}
        for c, name in enumerate(names):
            h = 1240 if name == 'Colors' else (1060 if name == 'Settings' else H)
            add(name, c * PITCH_X, y, W, h, 'screens', 28)
        if r in STICKIES:
            notes['s%d' % r] = {'page': 'screens', 'text': STICKIES[r], 'x': len(names) * PITCH_X, 'y': y, 'w': 390}
    add('ThemeSwitchFlows', -520, 0, W, 560, 'flows', 28)
    add('Flows', 0, 0, 1840, flows_h, 'flows')
    add('ThemeSwitchComponents', -520, 0, W, 560, 'components', 28)
    add('Components', 0, 0, catalog.W, catalog.H, 'components')
    add('Ideas', 0, 0, 1280, 1500, 'ideas')
    for r, (n, title, text) in enumerate(fb_screens.FEEDBACK):  # picked feedback variants
        y = 1900 + r * PITCH_Y
        names = [b[0] for b in fb_screens.BOARDS if b[3] == n]
        notes['fb%d' % n] = {'kind': 'title1', 'page': 'ideas', 'text': title, 'x': 0, 'y': y - 250,
                             'maxW': max(PITCH_X * len(names) - 80, 1280), 'w': 240}
        notes['fbS%d' % n] = {'page': 'ideas', 'text': text, 'x': len(names) * PITCH_X, 'y': y, 'w': 390}
        for c, name in enumerate(names):
            add(name, c * PITCH_X, y, W, H, 'ideas', 28)
    numbers = sorted({n for *_, n in idea_screens.BOARDS})
    for r, n in enumerate(numbers):
        y = 1900 + (len(fb_screens.FEEDBACK) + r) * PITCH_Y
        names = [b[0] for b in idea_screens.BOARDS if b[3] == n]
        notes['idea%d' % n] = {'kind': 'title1', 'page': 'ideas', 'text': '%d · %s' % (n, special.IDEAS[n - 1][1]), 'x': 0, 'y': y - 250,
                               'maxW': max(PITCH_X * len(names) - 80, 1280), 'w': 240}
        notes['ideaS%d' % n] = {'page': 'ideas', 'text': special.IDEAS[n - 1][3], 'x': len(names) * PITCH_X, 'y': y, 'w': 390}
        for c, name in enumerate(names):
            add(name, c * PITCH_X, y, W, H, 'ideas', 28)
    add('History', 0, 0, 1040, 640, 'history')
    canvas = {'v': 3, 'createdOnFiles': old.get('createdOnFiles'), 'title': old.get('title', 'Co-Shell — дизайн'),
              'launch': {'view': 'canvas', 'page': 'screens'},
              'pages': [{'id': 'screens', 'name': 'Экраны'}, {'id': 'flows', 'name': 'Воркфлоу'}, {'id': 'components', 'name': 'Компоненты'},
                        {'id': 'ideas', 'name': 'Предложения'}, {'id': 'history', 'name': 'История решений'}],
              'boards': boards, 'order': order, 'notes': notes, 'noteOrder': list(notes), 'designSystems': old.get('designSystems', []),
              'attachments': old.get('attachments', {})}
    missing = [k for k in boards if not os.path.exists(os.path.join(OUT, k))]
    extra = [f for f in files if f not in boards]
    if missing or extra:
        raise SystemExit('layout mismatch: missing %s, not placed %s' % (missing, extra))
    with open(canvas_path, 'w', encoding='utf-8') as f:
        json.dump(canvas, f, ensure_ascii=False, indent=2)
        f.write('\n')
    print('%d boards, %d generated' % (len(boards), len(gen)))


if __name__ == '__main__':
    main()
