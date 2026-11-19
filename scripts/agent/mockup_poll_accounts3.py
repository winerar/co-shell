#!/usr/bin/env python3
"""Poll boards for #505, round 3: the whole operation on ONE step of the entry sheet.

Usage: python3 scripts/agent/mockup_poll_accounts3.py <out-dir>  (<out-dir>/svg from round 1)  → <out-dir>/img/*.png
Then: agent.sh poll docs/design/poll-accounts-505c.json --img <out-dir>/img
"""
import os
import subprocess
import sys

HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, HERE)
import mockup_bricks as B  # noqa: E402
import mockup_nav_poll as N  # noqa: E402
import mockup_poll_accounts as P  # noqa: E402
import mockup_poll_accounts2 as R  # noqa: E402
import mockup_poll_update as U  # noqa: E402

ACCS, MONO = R.ACCS, U.MONO
CATS = [('Продукты', 'cart'), ('Такси', 'taxi'), ('Дом', 'home'), ('Связь', 'phone'), ('Кафе', 'cart'), ('Учёба', 'school'), ('Здоровье', 'gear'), ('Счета', 'chart')]


def chip(t, text, on=False):
    st = 'background:%s;color:%s;font-weight:700;box-shadow:inset 0 0 0 2px %s' % (t['prim'], t['onp'], t['ink']) if on else 'background:%s' % t['bar']
    return '<span style="height:36px;padding:0 14px;border-radius:999px;display:flex;align-items:center;font-size:14px;white-space:nowrap;%s">%s</span>' % (st, text)


def seg(t, on=0):
    items = ''.join('<span style="flex:1;text-align:center;height:34px;line-height:34px;border-radius:999px;font-size:14px;%s">%s</span>' % (
        ('background:%s;color:%s;font-weight:700;box-shadow:inset 0 0 0 2px %s' % (t['prim'], t['onp'], t['ink'])) if i == on else '', n) for i, n in enumerate(['Расход', 'Доход', 'Перевод']))
    return '<div style="margin:0 16px 8px;padding:3px;border-radius:999px;background:%s;display:flex">%s</div>' % (t['bar'], items)


def strip(t, ids, sel=0, size=44, more=True):
    items = ''.join('<span style="display:flex;border-radius:14px;%s">%s</span>' % (
        ('box-shadow:0 0 0 2px %s,0 0 0 4px %s;' % (t['page'], t['ink'])) if i == sel else '', P.badge(ACCS[a], size, 'A')) for i, a in enumerate(ids))
    tail = chip(t, 'Все ›') if more else ''
    return '<div style="display:flex;gap:10px;padding:4px 16px 8px;align-items:center">%s%s</div>' % (items, tail)


def amount(t, text='1 250', cur='ARS', right=''):
    return ('<div style="display:flex;align-items:baseline;justify-content:space-between;padding:0 20px 4px"><span style="font-size:38px;%sfont-weight:600">%s <span style="font-size:18px;color:%s">%s</span></span>'
            '<span style="font-size:13px;color:%s">%s</span></div>' % (MONO, text, t['muted'], cur, t['muted'], right))


def cat_cells(t, n):
    cells = ''.join('<div style="display:flex;flex-direction:column;align-items:center;gap:4px;width:25%%">%s<span style="font-size:11px">%s</span></div>' % (
        B.dot_icon(ic, '#C27BFF' if t['dark'] else '#7A2BD6', 40 if i or True else 40), nm) for i, (nm, ic) in enumerate(CATS[:n]))
    return cells


def cats_grid(t, rows=2):
    cells = cat_cells(t, 7 if rows == 2 else 8)
    if rows == 2:
        cells += '<div style="display:flex;flex-direction:column;align-items:center;gap:4px;width:25%%"><span style="width:40px;height:40px;border-radius:50%%;background:%s;display:flex;align-items:center;justify-content:center">›</span><span style="font-size:11px">Все</span></div>' % t['bar']
    return '<div style="display:flex;flex-wrap:wrap;gap:8px 0;padding:4px 8px 6px">%s</div>' % cells


def cats_row(t):
    cells = ''.join('<div style="display:flex;flex-direction:column;align-items:center;gap:4px;width:56px">%s<span style="font-size:11px">%s</span></div>' % (
        B.dot_icon(ic, '#C27BFF' if t['dark'] else '#7A2BD6', 40), nm) for nm, ic in CATS[:5])
    return '<div style="display:flex;gap:8px;padding:4px 16px 6px;align-items:flex-start">%s<span style="height:40px;padding:0 12px;border-radius:999px;display:flex;align-items:center;font-size:14px;white-space:nowrap;background:%s">Все ›</span></div>' % (cells, t['bar'])


def keypad(t, key=48):
    k = lambda x: '<span style="height:%dpx;border-radius:14px;background:%s;display:flex;align-items:center;justify-content:center;font-size:20px;%s">%s</span>' % (key, t['bar'], MONO, x)  # noqa: E731
    digits = ''.join(k(x) for x in ['1', '2', '3', '4', '5', '6', '7', '8', '9', '000', '0', ','])
    right = ('<div style="display:grid;grid-template-rows:%dpx 1fr;gap:6px"><span style="border-radius:14px;background:%s;display:flex;align-items:center;justify-content:center;font-size:20px">⌫</span>'
             '<span style="border-radius:14px;background:%s;color:%s;font-weight:700;display:flex;align-items:center;justify-content:center;font-size:16px;box-shadow:%s">Готово</span></div>'
             % (key, t['bar'], t['prim'], t['onp'], '0 0 0 1.5px ' + U.mix(t['prim'], '#000000', .5) if not t['dark'] else 'none'))
    return '<div style="display:grid;grid-template-columns:repeat(3,1fr) 1.1fr;gap:6px;padding:6px 16px 0"><div style="grid-column:1/4;display:grid;grid-template-columns:repeat(3,1fr);gap:6px">%s</div>%s</div>' % (digits, right)


def sheet(t, body, h):
    top = '<div style="height:20px;display:flex;justify-content:center;padding-top:8px"><span style="width:36px;height:4px;border-radius:2px;background:%s"></span></div>' % t['line']
    return U.phone(t, '<div style="position:absolute;inset:0;background:rgba(0,0,0,.35)"></div><div style="position:absolute;left:0;right:0;bottom:0;top:%dpx;background:%s;border-radius:28px 28px 0 0">%s%s</div>' % (
        844 - h, t['page'], top, body), 844)


def one(t, v):
    ids = [0, 1, 2, 3, 4]
    chips = '<div style="display:flex;gap:8px;padding:2px 16px 4px">%s</div>' % ''.join(chip(t, x) for x in ['Сегодня', 'Заметка', '+ Чек', '# Теги'])
    if v == 'A':
        return sheet(t, seg(t) + strip(t, ids) + amount(t, right='Galicia ARS · 86 000') + cats_grid(t) + chips + keypad(t), 700)
    if v == 'B':
        return sheet(t, seg(t) + strip(t, ids) + '<div style="padding:0 20px;font-size:13px;color:%s">Galicia ARS · остаток <span style="%s">86 000 ARS</span></div>' % (t['muted'], MONO)
                     + amount(t) + cats_row(t) + chips + keypad(t, 54), 700)
    # C: account chip beside the amount, strip opens on tap
    badge = '<span style="display:flex;align-items:center;gap:6px;height:44px;padding:0 10px 0 4px;border-radius:999px;background:%s">%s<span style="font-size:13px">Galicia ARS ▾</span></span>' % (t['bar'], P.badge(ACCS[0], 36, 'A'))
    top = '<div style="display:flex;align-items:center;justify-content:space-between;padding:0 16px 4px">%s<span style="font-size:13px;color:%s">86 000 ARS</span></div>' % (badge, t['muted'])
    return sheet(t, seg(t) + top + amount(t) + cats_grid(t) + chips + keypad(t), 640)


def transfer(t, v):
    row = lambda lab, i: ('<div style="display:flex;align-items:center;gap:10px;margin:0 16px 8px;padding:8px 12px;border-radius:16px;background:%s">%s<span style="flex:1"><div style="font-size:12px;color:%s">%s</div>'
                          '<div style="font-size:15px">%s</div></span><span style="%sfont-size:13px;color:%s">%s %s</span></div>' % (t['surf'], P.badge(ACCS[i], 40, 'A'), t['muted'], lab, ACCS[i][0], MONO, t['muted'], R.BAL[i], R.CUR[i]))  # noqa: E731
    sums = ('<div style="display:flex;align-items:baseline;justify-content:space-between;padding:6px 20px"><span style="font-size:34px;%sfont-weight:600">100 <span style="font-size:16px;color:%s">USD</span></span>'
            '<span style="%sfont-size:15px;color:%s">→ 118 000 ARS · курс 1 180</span></div>' % (MONO, t['muted'], MONO, t['muted']))
    chips = '<div style="display:flex;gap:8px;padding:6px 16px 4px">%s</div>' % ''.join(chip(t, x) for x in ['Сегодня', 'Заметка'])
    return sheet(t, seg(t, 2) + row('Откуда', 5) + row('Куда', 0) + sums + chips + keypad(t), 640)


def main():
    out = sys.argv[1]
    P.SVG_DIR[0] = os.path.join(out, 'svg')
    os.makedirs(os.path.join(out, 'html'), exist_ok=True)
    spec = [('One' + v, one, v) for v in 'ABC'] + [('Trf', transfer, 'X')]
    for name, fn, v in spec:
        for suffix, dark in (('', True), ('@light', False)):
            t = U.theme(dark)
            open(os.path.join(out, 'html', name + suffix + '.html'), 'w', encoding='utf-8').write(N.page(fn(t, v), 390, 844, dict(page=t['page'])))
    js = os.path.join(out, 'render.js')
    src = open(os.path.join(HERE, 'mockup_poll_update.py'), encoding='utf-8').read()
    open(js, 'w').write(src[src.index("'''const") + 3:src.index("''')")].replace('\\\\d', '\\d'))
    subprocess.run(['node', js, os.path.abspath(out)], check=True, cwd=out)
    print('%d boards → %s/img' % (len(spec) * 2, out))


if __name__ == '__main__':
    main()
