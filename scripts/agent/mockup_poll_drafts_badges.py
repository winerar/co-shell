#!/usr/bin/env python3
"""Poll boards: badges of the account and the category in «Черновики» (one look for the same data, owner rule 2026-11-20).

Standalone PNGs like mockup_poll_notes628.py (bricks reused): `DbXxx.png` dark + `DbXxx@light.png`.
Usage: python3 scripts/agent/mockup_poll_drafts_badges.py <out-dir>  → <out-dir>/img/*.png   then   poll-page.py <spec> <poll-dir> --img <out-dir>/img
"""
import os
import subprocess
import sys

HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, HERE)
import mockup_nav_poll as N  # noqa: E402
import mockup_poll_notes628 as P  # noqa: E402
import mockup_poll_update as U  # noqa: E402

MONO = U.MONO
H = 760
GALICIA, CASH = '#E8590C', '#2F9E44'
CAT = {'Кафе': '#F59F00', 'Продукты': '#40C057', 'Такси': '#339AF0'}
# title, account, account colour, day, amount, category, source, missing
ROWS = [('кофе', 'Galicia', GALICIA, 'сегодня', '−3 500', 'Кафе', 'по прошлым операциям', None),
        ('тест заметки', 'Galicia', GALICIA, 'сегодня', '', None, None, 'Нужна сумма'),
        ('Carrefour', 'Galicia', GALICIA, 'вчера', '−12 222', 'Продукты', 'по названию', None),
        ('позвонить маме', 'Galicia', GALICIA, 'сегодня', '', None, None, 'Нужна сумма'),
        ('такси', 'Наличные', CASH, 'сегодня', '−2 000', 'Такси', 'по названию', None)]
TICKS = [True, False, True, False, True]


def acc_badge(c, size=16):
    return '<span style="width:%dpx;height:%dpx;flex:none;border-radius:5px;background:%s;display:inline-block"></span>' % (size, size, c)


def cat_badge(t, c, size):
    return ('<span style="width:%dpx;height:%dpx;flex:none;border-radius:50%%;background:%s;display:inline-flex;align-items:center;justify-content:center">'
            '<span style="width:%dpx;height:%dpx;border-radius:50%%;background:%s"></span></span>' % (size, size, U.mix(t['surf'], c, .18), size // 2, size // 2, c))


def row(t, r, on, mode):
    title, acc, ac, day, amt, cat, src, need = r
    money = '<span style="%sfont-size:15px">%s</span>' % (MONO, amt) if amt else '<span style="color:%s">—</span>' % t['muted']
    sub_badge = acc_badge(ac) if mode != 'now' else ''
    sub = ('<span style="display:flex;align-items:center;gap:6px;font-size:13px;color:%s">%s%s · %s</span>' % (t['muted'], sub_badge, acc, day))
    if need:
        under = P.mark(t, need)
    elif mode == 'A':
        under = ('<span style="display:flex;align-items:center;gap:6px;font-size:12px;color:%s">%s%s · %s</span>' % (t['muted'], cat_badge(t, CAT[cat], 18), cat, src))
    else:
        under = '<span style="font-size:12px;color:%s">%s · %s</span>' % (t['muted'], cat, src)
    lead = P.tick(t, on) if mode != 'B' else ''
    if mode == 'B':
        lead = cat_badge(t, CAT[cat], 40) if cat else ('<span style="width:40px;height:40px;flex:none;border-radius:50%%;border:1.5px dashed %s;box-sizing:border-box"></span>' % t['line'])
    tail = '<span style="margin-left:4px;flex:none;display:flex">%s</span>' % P.tick(t, on) if mode == 'B' else ''
    return ('<div style="display:flex;align-items:center;gap:12px;min-height:64px;padding:6px 14px;border-top:1px solid %s">%s'
            '<span style="flex:1;display:flex;flex-direction:column;gap:2px"><span style="font-size:16px">%s</span>%s%s</span>%s%s</div>' % (
                U.mix(t['surf'], t['line'], .5), lead, title, sub, under, money, tail))


def screen(t, mode, cap):
    rows = ''.join(row(t, r, TICKS[i], mode) for i, r in enumerate(ROWS))
    body = ('<div style="padding:72px 0 0"><div style="margin:8px 20px 6px;font-size:13px;color:%s">Черновиков: 5 · готовы: 3</div>'
            '<div style="margin:0 12px;border-radius:24px;background:%s;border:1px solid %s;overflow:hidden">%s</div></div>' % (t['muted'], t['surf'], t['line'], rows))
    return U.phone(t, P.topbar(t, 'Черновики') + body + P.footer(t, P.pill_btn(t, 'Записать · 3', icon=P.B.ICON['check']))
                   + P.caption(t, cap, 520), H)


BOARDS = [
    ('DbNow', lambda t, v: screen(t, 'now', 'Сейчас: счёт и категория — только словами. В «Операциях» и «Счетах» те же данные со значками.')),
    ('DbA', lambda t, v: screen(t, 'A', 'А: галочка остаётся слева. Рядом со счётом — мини-значок цвета счёта (как в «Операциях»), перед категорией — малый значок категории.')),
    ('DbB', lambda t, v: screen(t, 'B', 'Б: слева значок категории, как в «Операциях»; у строки без категории — пунктирный круг. Галочка «записать» переезжает вправо.')),
]


def main():
    out = sys.argv[1]
    os.makedirs(os.path.join(out, 'html'), exist_ok=True)
    for name, fn in BOARDS:
        for suffix, dark in (('', True), ('@light', False)):
            t = U.theme(dark)
            open(os.path.join(out, 'html', name + suffix + '.html'), 'w', encoding='utf-8').write(N.page(fn(t, name), 390, H, dict(page=t['page'])))
    js = os.path.join(out, 'render.js')
    src = open(os.path.join(HERE, 'mockup_poll_update.py'), encoding='utf-8').read()
    open(js, 'w').write(src[src.index("'''const") + 3:src.index("''')")].replace('\\\\d', '\\d'))
    subprocess.run(['node', js, os.path.abspath(out)], check=True, cwd=out)
    print('boards → %s/img' % out)


if __name__ == '__main__':
    main()
