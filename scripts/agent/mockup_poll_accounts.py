#!/usr/bin/env python3
"""Poll boards for #505: service logos in the account badge, finding the account in an operation row.

Usage: python3 scripts/agent/mockup_poll_accounts.py <out-dir>  → <out-dir>/img/*.png (dark and «@light»).
Then: agent.sh poll docs/design/poll-accounts-505.json --img <out-dir>/img
Logos — Simple Icons (CC0), fetched to <out-dir>/svg by the caller (see poll json notes); colours — BankColors.kt.
"""
import os
import re
import subprocess
import sys

HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, HERE)
import mockup_bricks as B  # noqa: E402
import mockup_nav_poll as N  # noqa: E402
import mockup_poll_update as U  # noqa: E402

MONO = U.MONO
# (title, svg slug, brand colour, initials)
SERV = [('Binance', 'binance', '#F0B90B', 'BN'), ('Mercado Pago', 'mercadopago', '#00B1EA', 'MP'), ('Wise', 'wise', '#9FE870', 'W'),
        ('PayPal', 'paypal', '#003087', 'PP'), ('Payoneer', 'payoneer', '#FF4800', 'PY'), ('Revolut', 'revolut', '#0075EB', 'R'),
        ('Tether', 'tether', '#26A17B', '₮')]
SVG_DIR = [None]


def lum(c):
    ch = [int(c[i:i + 2], 16) / 255 for i in (1, 3, 5)]
    ch = [x / 12.92 if x <= .03928 else ((x + .055) / 1.055) ** 2.4 for x in ch]
    return .2126 * ch[0] + .7152 * ch[1] + .0722 * ch[2]


def ink_on(fill):
    return '#0B0B0B' if lum(fill) > .4 else '#FFFFFF'  # inkOn() of AccountBadge


def logo(slug, color, size):
    d = re.search(r' d="([^"]+)"', open(os.path.join(SVG_DIR[0], slug + '.svg')).read()).group(1)
    return '<svg width="%d" height="%d" viewBox="0 0 24 24"><path d="%s" fill="%s"/></svg>' % (size, size, d, color)


def badge(acc, size=40, style='A', t=None):
    """acc = (name, slug|None, colour, initials). style: A glyph on brand fill · B coloured glyph on white tile · L letters."""
    name, slug, col, ini = acc
    r = round(size * .3)
    base = 'width:%dpx;height:%dpx;flex:none;border-radius:%dpx;display:flex;align-items:center;justify-content:center;' % (size, size, r)
    if slug and style == 'A':
        return '<span style="%sbackground:%s">%s</span>' % (base, col, logo(slug, ink_on(col), round(size * .58)))
    if slug and style == 'B':
        return '<span style="%sbackground:#FFFFFF;box-shadow:inset 0 0 0 1.5px %s">%s</span>' % (base, col, logo(slug, col, round(size * .58)))
    return '<span style="%sbackground:%s;color:%s;font-weight:700;font-size:%dpx">%s</span>' % (base, col, ink_on(col), round(size * .33), ini)


A = {s[0]: (s[0], s[1], s[2], s[3]) for s in SERV}
GAL1 = ('Galicia ARS', None, '#F26E22', 'GA')
GAL2 = ('Galicia USD', None, '#F26E22', 'GA')
CASH = ('Наличные', None, '#2EC4B6', 'НА')
SBER = ('Сбер RUB', None, '#21A038', 'СБ')
OPS = [('Продукты', 'cart', GAL1, '−12 400', 'ARS', '#FF8FA8'), ('Подписка', 'phone', A['Binance'], '−9,99', 'USDT', '#FF8FA8'),
       ('Такси', 'taxi', A['Mercado Pago'], '−3 100', 'ARS', '#FF8FA8'), ('Зарплата', 'transfer', GAL2, '+1 200', 'USD', '#8FE08F'),
       ('Кофе', 'cart', CASH, '−2 500', 'ARS', '#FF8FA8'), ('Квартира', 'home', SBER, '−18 000', 'RUB', '#FF8FA8'),
       ('Продукты', 'cart', GAL1, '−8 900', 'ARS', '#FF8FA8')]


def amt(t, a, cur, col):
    return '<span style="%sfont-size:15px;color:%s;white-space:nowrap">%s <span style="font-size:12px;color:%s">%s</span></span>' % (
        MONO, col if t['dark'] else {'#FF8FA8': '#B3123A', '#8FE08F': '#1B6B1B'}[col], a, t['muted'], cur)


def op_row(t, op, v):
    title, icon, acc, a, cur, col = op
    cat = B.dot_icon(icon, '#C27BFF' if t['dark'] else '#7A2BD6', 40)
    sub = '<span style="font-size:13px;color:%s">%s</span>' % (t['muted'], acc[0])
    lead, stripe = cat, ''
    if v == 'A':  # mini badge on the category circle
        lead = '<span style="position:relative;flex:none;display:flex">%s<span style="position:absolute;right:-5px;bottom:-5px;border-radius:7px;box-shadow:0 0 0 2px %s;display:flex">%s</span></span>' % (cat, t['page'], badge(acc, 18, 'A'))
    if v == 'B':  # mini badge in the subtitle, before the name
        sub = '<span style="display:flex;align-items:center;gap:6px;font-size:13px;color:%s">%s%s</span>' % (t['muted'], badge(acc, 16, 'A'), acc[0])
    if v == 'C':  # colour stripe at the row edge
        stripe = '<span style="position:absolute;left:0;top:10px;bottom:10px;width:4px;border-radius:0 4px 4px 0;background:%s"></span>' % acc[2]
    return ('<div style="position:relative;display:flex;align-items:center;gap:12px;min-height:64px;padding:0 16px">%s%s'
            '<span style="display:flex;flex-direction:column;flex-grow:1;min-width:0"><span style="font-size:16px">%s</span>%s</span>%s</div>'
            % (stripe, lead, title, sub, amt(t, a, cur, col)))


def ops_board(t, v):
    head = '<div style="padding:12px 20px 6px;font-size:13px;font-weight:600;letter-spacing:.04em;color:%s">СЕГОДНЯ</div>' % t['acc' if t['dark'] else 'ink']
    return U.phone(t, U.topbar(t).replace('Обновление', 'Операции') + head + ''.join(op_row(t, o, v) for o in OPS[:6]), 560)


def strip_board(t, v):
    items = [('Все', None), GAL1, A['Mercado Pago'], A['Binance'], CASH, SBER]
    chips = ''
    for i, it in enumerate(items):
        on = i == 1
        ring = 'box-shadow:0 0 0 2px %s, 0 0 0 4px %s;' % (t['page'], t['ink']) if on else ''
        if it[0] == 'Все':
            chips += '<span style="height:40px;padding:0 14px;border-radius:999px;display:flex;align-items:center;font-size:14px;background:%s">Все</span>' % t['bar']
        else:
            chips += '<span style="display:flex;border-radius:12px;%s">%s</span>' % (ring, badge(it, 40, 'A'))
    dim = '' if v == 'A' else ''
    note = '<div style="padding:8px 20px;font-size:13px;color:%s">%s</div>' % (t['muted'], 'Нажмите значок — только этот счёт · ещё раз — все')
    if v == 'B':
        row = ('<div style="margin:8px 16px;height:36px;border-radius:999px;background:%s;display:flex;align-items:center;gap:8px;padding:0 6px 0 4px;width:max-content">'
               '<span style="display:flex">%s</span><span style="font-size:14px">Galicia ARS</span><span style="font-size:14px;color:%s;padding-right:8px">▾</span></div>'
               % (t['bar'], badge(GAL1, 28, 'A'), t['muted']))
        note = ''
    else:
        row = '<div style="display:flex;gap:10px;padding:10px 16px;align-items:center;overflow:hidden">%s</div>' % chips
    rows = ''.join(op_row(t, o, 'B') for o in OPS if o[2] == GAL1 or v == 'B')
    return U.phone(t, U.topbar(t).replace('Обновление', 'Операции') + row + note + rows, 420)


def services_board(t, style):
    cells = ''.join('<div style="display:flex;flex-direction:column;align-items:center;gap:8px;width:80px">%s<span style="font-size:11px;color:%s">%s</span></div>'
                    % (badge(s if style != 'L' else (s[0], None, s[2], s[3]), 48, style), t['muted'], s[0]) for s in
                    [(x[0], x[1], x[2], x[3]) for x in SERV])
    rows = ''.join('<div style="display:flex;align-items:center;gap:12px;min-height:56px;padding:0 16px">%s<span style="flex-grow:1;font-size:16px">%s</span>'
                   '<span style="%sfont-size:15px">%s</span></div>' % (badge(a if style != 'L' else (a[0], None, a[2], a[3]), 40, style), a[0], MONO, v)
                   for a, v in [(A['Mercado Pago'], '124 300 ARS'), (A['Binance'], '1 250 USDT'), (GAL1, '86 000 ARS'), (SBER, '40 000 RUB')])
    return U.phone(t, '<div style="display:flex;flex-wrap:wrap;gap:12px 0;padding:20px 12px 8px">%s</div>%s' % (cells, rows), 470)


def pair_board(t, v):
    def one(acc, cur):
        tag = ''
        if v in 'AC':
            tag = ('<span style="position:absolute;right:-4px;bottom:-4px;font-size:9px;font-weight:700;padding:1px 4px;border-radius:6px;background:%s;color:%s;'
                   'box-shadow:0 0 0 2px %s">%s</span>' % (t['ink'], t['page'], t['page'], cur))
        return ('<div style="display:flex;align-items:center;gap:12px;min-height:60px;padding:0 16px"><span style="position:relative;display:flex">%s%s</span>'
                '<span style="flex-grow:1;font-size:16px">%s</span></div>' % (badge(acc, 40, 'A'), tag, acc[0]))
    hint = ''
    if v in 'BC':
        hint = ('<div style="margin:12px 16px;padding:12px 16px;border-radius:16px;background:%s;font-size:14px;line-height:1.35">'
                '<b>Такой цвет уже у «Galicia ARS»</b><div style="color:%s;margin-top:2px">Выберите другой, чтобы счета не путались.</div></div>' % (t['bar'], t['muted']))
    return U.phone(t, one(GAL1, 'ARS') + one(GAL2, 'USD') + hint, 190 if v == 'A' else 290)


def main():
    out = sys.argv[1]
    SVG_DIR[0] = os.path.join(out, 'svg')
    os.makedirs(os.path.join(out, 'html'), exist_ok=True)
    spec = [('Svc' + v, services_board, v, 470) for v in 'ABL'] + [('Ops' + v, ops_board, v, 560) for v in 'ABCD'] + \
           [('Strip' + v, strip_board, v, 420) for v in 'AB'] + [('Pair' + v, pair_board, v, 190 if v == 'A' else 290) for v in 'ABC']
    for name, fn, v, h in spec:
        for suffix, dark in (('', True), ('@light', False)):
            t = U.theme(dark)
            open(os.path.join(out, 'html', name + suffix + '.html'), 'w', encoding='utf-8').write(N.page(fn(t, v), 390, h, dict(page=t['page'])))
    js = os.path.join(out, 'render.js')
    src = open(os.path.join(HERE, 'mockup_poll_update.py'), encoding='utf-8').read()
    open(js, 'w').write(src[src.index("'''const") + 3:src.index("''')")].replace('\\\\d', '\\d'))
    subprocess.run(['node', js, os.path.abspath(out)], check=True, cwd=out)
    print('%d boards → %s/img' % (len(spec) * 2, out))


if __name__ == '__main__':
    main()
