#!/usr/bin/env python3
"""Poll boards (#587, stage 2): how the «ИИ на телефоне» screen turns the add-on (APK ≈ 20 MB) and the model (≈ 2,5 GB) on, how it is removed,
and the mobile-data question. The card look is decided (poll 2026-11-21, «А»); here only the two-part flow is new. Bricks — `mockup_poll_ai_more`.

Usage: python3 scripts/agent/mockup_poll_ai_addon.py <out-dir>  → <out-dir>/img/*.png   then   agent.sh poll docs/design/poll-ai-addon.json --img <out-dir>/img
"""
import os
import subprocess
import sys

HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, HERE)
import mockup_nav_poll as N  # noqa: E402
import mockup_poll_ai_more as M  # noqa: E402
import mockup_poll_ai_parts as X  # noqa: E402
import mockup_poll_update as U  # noqa: E402

ic = X.ic
FACTS = [('cloud', 'Размер', 'Дополнение 20 МБ + модель 2,5 ГБ'), ('chip', 'Память телефона', 'Нужно от 6 ГБ'),
         ('wallet', 'Приватность', 'Всё считается на телефоне, без интернета')]
TITLE = 'ИИ на телефоне'
TEXT = 'Необязательно. ИИ читает незнакомые чеки, разбирает заметки и голос и подсказывает категорию для нового получателя.'
USE = [('note', 'Незнакомые чеки', '«Прочитать с ИИ»'), ('draft', 'Быстрые заметки и голос', 'разбор сложных фраз'), ('spark', 'Категория для нового получателя', 'подсказка')]


def progress(t, frac, left, right):
    return M.meter(t, frac) + '<div style="display:flex;justify-content:space-between;font-size:13px;color:%s"><span>%s</span><span>%s</span></div>' % (t['muted'], left, right)


def step_row(t, num, title, sub, state):
    """state: 'done' (✓), 'now' (number on a filled disc), 'wait' (grey number)."""
    if state == 'done':
        disc = '<span style="width:28px;height:28px;border-radius:50%%;background:%s;display:flex;align-items:center;justify-content:center;box-shadow:0 0 0 1px %s">%s</span>' % (t['prim'], X.A.edge(t), ic('check', t['onp'], 16, 2.4))
    elif state == 'now':
        disc = '<span style="width:28px;height:28px;border-radius:50%%;background:%s;color:%s;font-weight:700;display:flex;align-items:center;justify-content:center;box-shadow:0 0 0 2px %s">%s</span>' % (t['prim'], t['onp'], X.A.edge(t), num)
    else:
        disc = '<span style="width:28px;height:28px;border-radius:50%%;background:%s;color:%s;display:flex;align-items:center;justify-content:center">%s</span>' % (t['bar'], t['muted'], num)
    return ('<div style="display:flex;align-items:center;gap:12px;min-height:56px;padding:6px 0">%s<span style="flex:1;min-width:0"><div style="font-size:16px">%s</div>'
            '<div style="font-size:13px;color:%s">%s</div></span></div>' % (disc, title, t['muted'], sub))


def steps(t, a, b):
    return X.card(t, step_row(t, 1, 'Дополнение', *a) + step_row(t, 2, 'Модель', *b), '4px 16px')


def facts(t):
    return M.facts(t, FACTS)


# --- Q1 A: one «Включить», two steps in one card ---
def off_a(t, v):
    return X.screen(t, TITLE, M.hero(t, 'Выключен', TEXT, '<div style="margin-top:8px">%s</div>' % X.tag(t, 'без него всё работает как раньше', 'check')) + facts(t) + X.cta(t, 'Включить · 2,5 ГБ', True, icon='cloud'))


def step1_a(t, v):
    body = M.hero(t, 'Шаг 1 из 2', 'Android спросит, можно ли установить дополнение. Нажмите «Установить».', X.tag(t, 'ждём ваше «Установить»', 'chip')) + steps(t, ('ждём установки', 'now'), ('потом, ≈ 2,5 ГБ', 'wait'))
    return X.screen(t, TITLE, body + X.cta(t, 'Отменить', False))


def step2_a(t, v):
    body = M.hero(t, 'Шаг 2 из 2 · скачиваю', 'Можно закрыть экран: загрузка продолжится.', progress(t, .42, '1 060 из 2 500 МБ', '42 %')) + steps(t, ('установлено', 'done'), ('скачиваю…', 'now'))
    return X.screen(t, TITLE, body + X.cta(t, 'Отменить', False))


def on_a(t, v):
    use = X.card(t, '<div style="font-size:12px;letter-spacing:.04em;color:%s;margin-bottom:2px">ГДЕ РАБОТАЕТ</div>' % t['muted'] + ''.join(X.row(t, i, a, b) for i, a, b in USE), '10px 16px')
    return X.screen(t, TITLE, M.hero(t, 'Включён', 'Работает без интернета. Занимает 2,5 ГБ.', '<div style="margin-top:8px">%s</div>' % X.tag(t, 'готов', 'check')) + use + X.cta(t, 'Удалить модель', False, icon='trash'))


# --- Q1 B: two rows, each with its own button ---
def action(t, label, filled=False):
    st = ('background:%s;color:%s;font-weight:700;box-shadow:inset 0 0 0 2px %s' % (t['prim'], t['onp'], X.A.edge(t))) if filled else 'background:%s;color:%s' % (t['bar'], t['ink'])
    return '<span style="height:36px;padding:0 14px;border-radius:999px;display:inline-flex;align-items:center;font-size:14px;%s">%s</span>' % (st, label)


def two_rows(t, r1, r2):
    return X.card(t, X.row(t, 'chip', *r1) + X.row(t, 'cloud', *r2), '4px 16px')


def off_b(t, v):
    rows = two_rows(t, ('1 · Дополнение', 'около 20 МБ · не установлено', action(t, 'Установить', True)), ('2 · Модель', '2,5 ГБ · сначала дополнение', action(t, 'Скачать')))
    return X.screen(t, TITLE, M.hero(t, 'Выключен', TEXT) + rows + facts(t))


def mid_b(t, v):
    r2 = X.card(t, X.row(t, 'chip', '1 · Дополнение', 'установлено', ic('check', X.ink_acc(t), 20, 2.4)) + '<div style="padding:6px 0 10px"><div style="display:flex;justify-content:space-between;font-size:16px"><span>2 · Модель</span>%s</div>%s</div>' % (action(t, 'Отменить'), progress(t, .42, '1 060 из 2 500 МБ', '42 %')), '4px 16px')
    return X.screen(t, TITLE, M.hero(t, 'Скачиваю…', 'Можно закрыть экран: загрузка продолжится.') + r2 + facts(t))


# --- Q2: removal ---
def del_b(t, v):
    return X.overlay(on_b_screen(t), X.dialog(t, 'trash', 'Выключить ИИ?', 'Удалим модель (2,5 ГБ) и предложим убрать дополнение. Освободится ≈ 2,5 ГБ. Включить можно снова.', X.btn_row(t, 'Выключить', True, 'trash') + X.btn_row(t, 'Отмена')))


def on_b_screen(t):
    use = X.card(t, '<div style="font-size:12px;letter-spacing:.04em;color:%s;margin-bottom:2px">ГДЕ РАБОТАЕТ</div>' % t['muted'] + ''.join(X.row(t, i, a, b) for i, a, b in USE), '10px 16px')
    return X.screen(t, TITLE, M.hero(t, 'Включён', 'Работает без интернета. Занимает 2,5 ГБ.', '<div style="margin-top:8px">%s</div>' % X.tag(t, 'готов', 'check')) + use + X.cta(t, 'Выключить ИИ', False, icon='trash'))


def del_a(t, v):
    note = '<div style="margin:2px 28px 0;font-size:13px;color:%s;line-height:1.4">Дополнение (20 МБ) останется. Убрать его можно в настройках телефона, в списке приложений.</div>' % t['muted']
    page = on_a(t, v)
    i = page.rfind('<div style="position:absolute;left:16px;right:16px;bottom:16px')
    return page[:i] + note + page[i:]


# --- Q3: mobile data ---
def data_a(t, v):
    return X.overlay(off_a(t, v), X.dialog(t, 'cloud', 'Скачать 2,5 ГБ?', 'Лучше по Wi-Fi: мобильный интернет может быть платным. Удалить можно в любой момент.', X.btn_row(t, 'Скачать', True, 'cloud') + X.btn_row(t, 'Отмена')))


def data_b(t, v):
    return X.overlay(off_a(t, v), X.dialog(t, 'cloud', 'Сейчас мобильный интернет', 'Загрузка 2,5 ГБ может стоить денег. Начнём сами, когда телефон подключится к Wi-Fi.', X.btn_row(t, 'Ждать Wi-Fi', True, 'cloud') + X.btn_row(t, 'Скачать сейчас')))


BOARDS = [('AdOffA', off_a), ('AdStep1A', step1_a), ('AdStep2A', step2_a), ('AdOnA', on_a), ('AdOffB', off_b), ('AdMidB', mid_b),
          ('AdDelA', del_a), ('AdDelB', del_b), ('AdDataA', data_a), ('AdDataB', data_b)]


def main():
    out = sys.argv[1]
    os.makedirs(os.path.join(out, 'html'), exist_ok=True)
    for name, fn in BOARDS:
        for suffix, dark in (('', True), ('@light', False)):
            t = U.theme(dark)
            open(os.path.join(out, 'html', name + suffix + '.html'), 'w', encoding='utf-8').write(N.page(fn(t, name), 390, 844, dict(page=t['page'])))
    js = os.path.join(out, 'render.js')
    src = open(os.path.join(HERE, 'mockup_poll_update.py'), encoding='utf-8').read()
    open(js, 'w').write(src[src.index("'''const") + 3:src.index("''')")].replace('\\\\d', '\\d'))
    subprocess.run(['node', js, os.path.abspath(out)], check=True, cwd=out)
    print('boards → %s/img' % out)


if __name__ == '__main__':
    main()
