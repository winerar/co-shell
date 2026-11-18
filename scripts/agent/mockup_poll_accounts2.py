#!/usr/bin/env python3
"""Poll boards for #505, round 2: choosing the account in the entry sheet — compact and quick.

Usage: python3 scripts/agent/mockup_poll_accounts2.py <out-dir>  (<out-dir>/svg from round 1)  → <out-dir>/img/*.png
Then: agent.sh poll docs/design/poll-accounts-505b.json --img <out-dir>/img
"""
import os
import subprocess
import sys

HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, HERE)
import mockup_nav_poll as N  # noqa: E402
import mockup_poll_accounts as P  # noqa: E402
import mockup_poll_update as U  # noqa: E402

A = P.A
ACCS = [P.GAL1, A['Mercado Pago'], A['Binance'], P.CASH, P.SBER, P.GAL2, A['Wise'], A['Revolut'], A['PayPal']]
BAL = ['86 000', '124 300', '1 250', '12 000', '40 000', '1 200', '310', '95', '42']
CUR = ['ARS', 'ARS', 'USDT', 'ARS', 'RUB', 'USD', 'EUR', 'EUR', 'USD']
MONO = U.MONO


def title(t, text):
    return '<div style="padding:16px 20px 8px;font-size:20px;font-weight:700">%s</div>' % text


def seg(t):
    items = ''.join('<span style="flex:1;text-align:center;height:36px;line-height:36px;border-radius:999px;font-size:14px;%s">%s</span>' % (
        ('background:%s;color:%s;font-weight:700;box-shadow:inset 0 0 0 2px %s' % (t['prim'], t['onp'], t['ink'])) if i == 0 else '', n) for i, n in enumerate(['Расход', 'Доход', 'Перевод']))
    return '<div style="margin:0 16px;padding:4px;border-radius:999px;background:%s;display:flex">%s</div>' % (t['bar'], items)


def label(t, text):
    return '<div style="padding:14px 20px 6px;font-size:13px;font-weight:600;letter-spacing:.04em;color:%s">%s</div>' % (t['acc'] if t['dark'] else t['ink'], text)


def search(t):
    return ('<div style="margin:10px 16px;height:48px;border-radius:999px;background:%s;display:flex;align-items:center;padding:0 18px;color:%s;font-size:15px">⌕&nbsp; Найти счёт</div>' % (t['bar'], t['muted']))


def tile(t, i, big=False):
    return ('<div style="display:flex;align-items:center;gap:10px;padding:10px 12px;border-radius:16px;background:%s;box-shadow:inset 0 0 0 1px %s">%s<span style="min-width:0"><div style="font-size:14px;white-space:nowrap">%s</div>'
            '<div style="%sfont-size:12px;color:%s">%s %s</div></span></div>' % (t['surf'], t['line'], P.badge(ACCS[i], 40, 'A'), ACCS[i][0], MONO, t['muted'], BAL[i], CUR[i]))


def cell(t, i, ring=False):
    r = 'box-shadow:0 0 0 2px %s,0 0 0 4px %s;' % (t['page'], t['ink']) if ring else ''
    return ('<div style="display:flex;flex-direction:column;align-items:center;gap:6px;width:84px"><span style="display:flex;border-radius:14px;%s">%s</span>'
            '<span style="font-size:12px;text-align:center;line-height:1.15">%s</span></div>' % (r, P.badge(ACCS[i], 48, 'A'), ACCS[i][0]))


def current(t, v):  # step 1 as now: recent 2x2, groups with headings, search at the bottom
    rec = '<div style="display:grid;grid-template-columns:1fr 1fr;gap:8px;padding:0 16px">%s</div>' % ''.join(tile(t, i) for i in range(4))
    grid = lambda ids: '<div style="display:flex;flex-wrap:wrap;gap:10px 0;padding:0 12px">%s</div>' % ''.join(cell(t, i) for i in ids)  # noqa: E731
    body = title(t, 'Откуда списали?') + seg(t) + label(t, 'НЕДАВНИЕ') + rec + label(t, 'КАРТЫ') + grid([0, 5, 6]) + label(t, 'КРИПТО И КОШЕЛЬКИ') + grid([2, 7, 8]) + search(t)
    return U.phone(t, body, 700)


def compact(t, v):  # step 1, compact: one grid, 4 columns, recent first, search in the title
    head = ('<div style="display:flex;align-items:center;padding:16px 20px 8px"><span style="flex:1;font-size:20px;font-weight:700">Откуда списали?</span>'
            '<span style="width:48px;height:48px;border-radius:50%%;background:%s;display:flex;align-items:center;justify-content:center;font-size:20px">⌕</span></div>' % t['bar'])
    cells = ''.join(cell(t, i).replace('width:84px', 'width:25%') for i in range(9))
    grid = '<div style="display:flex;flex-wrap:wrap;gap:12px 0;padding:8px 8px 16px">%s</div>' % cells
    return U.phone(t, head + seg(t) + grid + '<div style="padding:0 20px;font-size:12px;color:%s">Недавние — первыми</div>' % t['muted'], 420)


def strip_sheet(t, v):  # no account step: strip above the amount; C adds a name + balance line
    items = ''.join('<span style="display:flex;border-radius:14px;%s">%s</span>' % (
        ('box-shadow:0 0 0 2px %s,0 0 0 4px %s;' % (t['page'], t['ink'])) if i == 0 else '', P.badge(ACCS[i], 44, 'A')) for i in range(5))
    more = '<span style="height:44px;padding:0 14px;border-radius:999px;display:flex;align-items:center;font-size:14px;background:%s">Все ›</span>' % t['bar']
    info = ('<div style="padding:0 20px 4px;font-size:13px;color:%s">Galicia ARS · <span style="%s">86 000 ARS</span></div>' % (t['muted'], MONO)) if v == 'C' else ''
    amount = '<div style="padding:8px 20px;font-size:40px;%sfont-weight:600">1 250 <span style="font-size:20px;color:%s">ARS</span></div>' % (MONO, t['muted'])
    cats = '<div style="display:flex;gap:8px;padding:4px 16px 8px">%s</div>' % ''.join(
        '<span style="height:36px;padding:0 14px;border-radius:999px;display:flex;align-items:center;font-size:14px;background:%s">%s</span>' % (t['bar'], n) for n in ['Сегодня', 'Заметка', '+ Чек'])
    pad = '<div style="margin:8px 16px;height:200px;border-radius:20px;background:%s;opacity:.7"></div>' % t['bar']
    body = (title(t, 'Расход') + '<div style="display:flex;gap:10px;padding:4px 16px 8px;align-items:center">%s%s</div>' % (items, more) + info + amount + cats + pad)
    return U.phone(t, body, 600 if v == 'C' else 580)


def main():
    out = sys.argv[1]
    P.SVG_DIR[0] = os.path.join(out, 'svg')
    os.makedirs(os.path.join(out, 'html'), exist_ok=True)
    spec = [('Cur', current, 'X', 700), ('Cmp', compact, 'A', 420), ('StrB', strip_sheet, 'B', 580), ('StrC', strip_sheet, 'C', 600)]
    for name, fn, v, h in spec:
        for suffix, dark in (('', True), ('@light', False)):
            t = U.theme(dark)
            open(os.path.join(out, 'html', name + suffix + '.html'), 'w', encoding='utf-8').write(N.page(fn(t, v), 390, h, dict(page=t['page'])))
    js = os.path.join(out, 'render.js')
    src = open(os.path.join(HERE, 'mockup_poll_update.py'), encoding='utf-8').read()
    open(js, 'w').write(src[src.index("'''const") + 3:src.index("''')")].replace('\\\\d', '\\d'))
    for f in os.listdir(os.path.join(out, 'html')):  # render only this round's boards
        pass
    subprocess.run(['node', js, os.path.abspath(out)], check=True, cwd=out)
    print('%d boards → %s/img' % (len(spec) * 2, out))


if __name__ == '__main__':
    main()
