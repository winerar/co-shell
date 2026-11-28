#!/usr/bin/env python3
"""Poll boards for #818: the entry sheet without scrolling (worst case: receipt in another currency, payee, expense).

Real dp: phone 393×873 (owner's Xiaomi), sheet content from 102 dp (status 30 + gap 24 + handle 48), keypad 4×56 + 3×8 = 248
+ 12 bottom → cards get 873 − 102 − 260 − type bar 48 − ribbon 48 − gaps ≈ 403 dp. A dashed line marks the top of the keypad.
Usage: python3 scripts/agent/mockup_poll_entry818.py <out-dir>  → <out-dir>/img/*.png, then poll-page.py <spec> <poll> --img <out-dir>/img
"""
import os
import sys

HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, HERE)
import mockup_bricks as B  # noqa: E402
import mockup_poll_accounts as P  # noqa: E402
import mockup_poll_accounts4 as A  # noqa: E402
import mockup_poll_f_common as C  # noqa: E402
import mockup_poll_qr526 as Q  # noqa: E402
import mockup_poll_update as U  # noqa: E402

MONO = A.MONO
W = 393
CATS = [('Продукты', 'cart', '#9BC53D'), ('Кафе', 'cup', '#E3A008'), ('Такси', 'taxi', '#F2E14C'), ('Аптека', 'file', '#FF3B4E'),
        ('Аренда', 'home', '#22D3EE'), ('Связь', 'file', '#B266FF'), ('Кино', 'file', '#14B8A6')]
GAP = 6


def link(t, text):
    return '<span style="font-size:13px;letter-spacing:0;color:%s;white-space:nowrap">%s</span>' % (t['acc'] if t['dark'] else '#5B1FA8', text)


def card(t, label, body, right=''):
    """CoShellFormSection: caption in caps, a link or a value on the right."""
    return ('<div style="margin:0 16px %dpx;padding:10px 14px 12px;border-radius:24px;background:%s;border:1px solid %s"><div style="display:flex;justify-content:space-between;'
            'align-items:center;font-size:12px;letter-spacing:.04em;color:%s;height:20px">%s<span style="display:flex;gap:14px">%s</span></div>%s</div>' % (
                GAP, t['surf'], t['line'], t['muted'], label, right, body))


def type_bar(t):
    cells = ''.join('<span style="flex:1;height:40px;border-radius:999px;display:flex;align-items:center;justify-content:center;font-size:16px;%s">%s</span>' % (
        ('background:%s;color:%s;box-shadow:inset 0 0 0 2px %s' % (t['prim'], t['onp'], A.edge(t))) if i == 0 else 'color:%s' % t['muted'], s)
        for i, s in enumerate(['↗', '↙', '⇄']))
    btn = lambda x: Q.round_btn(t, x, 48)  # noqa: E731
    return ('<div style="display:flex;gap:4px;align-items:center;margin:0 16px %dpx;height:48px"><div style="flex:1;padding:4px;border-radius:999px;background:%s;display:flex;gap:4px">%s</div>%s%s%s</div>' % (
        GAP, t['bar'], cells, btn('▤'), btn(Q.qr(22)), btn('☰')))


def ribbon(t):
    return A.strip(t, 0, '0 16px %dpx' % GAP)


def amount_field(t, value='13,10', cur='USDT', color='#FF4D7A'):
    c = color if t['dark'] else '#C8174A'
    return ('<div style="margin-top:6px;height:56px;border-radius:16px;border:1px solid %s;background:%s;display:flex;align-items:center;justify-content:space-between;padding:0 8px 0 16px">'
            '<span style="%sfont-size:30px;font-weight:700;color:%s">%s</span><span style="padding:8px 14px;border-radius:999px;background:%s;font-size:14px">%s</span></div>' % (
                t['line'], t['bar'], MONO, c, value, t['surf'], cur))


def receipt_big(t):
    """Today: «Сумма покупки» is a second full field + rate line under it."""
    return ('<div style="margin-top:8px;padding-top:8px;border-top:1px solid %s;display:flex;align-items:center;gap:8px"><div style="flex:1;height:56px;border-radius:16px;border:2px solid %s;'
            'background:%s;padding:4px 8px 0 16px;display:flex;align-items:center;justify-content:space-between"><div><div style="font-size:12px;color:%s">Сумма покупки</div>'
            '<div style="%sfont-size:22px">13 300</div></div><span style="padding:6px 12px;border-radius:999px;border:1px solid %s;font-size:14px">ARS ▾</span></div>'
            '<span style="width:40px;text-align:center;font-size:20px">✕</span></div><div style="font-size:13px;color:%s;margin:4px 0 0 16px;%s">1 USDT = 1 015,27 ARS</div>' % (
                t['line'], A.edge(t), t['bar'], t['muted'], MONO, t['line'], t['muted'], MONO))


def receipt_line(t):
    """Variants: «Сумма покупки» in one line 40 dp — a pill with the amount (tap → keypad types into it), rate, ✕."""
    return ('<div style="margin-top:8px;display:flex;align-items:center;gap:8px;height:40px"><span style="height:40px;padding:0 6px 0 14px;border-radius:999px;background:%s;'
            'box-shadow:inset 0 0 0 1px %s;display:flex;align-items:center;gap:8px;white-space:nowrap;flex:none"><span style="font-size:12px;color:%s">Покупка</span><b style="%sfont-size:17px">13 300</b>'
            '<span style="padding:4px 10px;border-radius:999px;background:%s;font-size:13px">ARS ▾</span></span><span style="flex:1;font-size:12px;color:%s;%swhite-space:nowrap">'
            '%s</span><span style="width:32px;text-align:center;font-size:18px">✕</span></div>' % (
                t['bar'], t['line'], t['muted'], MONO, t['surf'], t['muted'], MONO, '≈ 1 015,27 за 1' if RATE else ''))


def cat_cell(t, nm, ic, col, on=False):
    ring = 'box-shadow:0 0 0 2px %s,0 0 0 4px %s;' % (t['surf'], A.edge(t)) if on else ''
    return ('<div style="flex:1;display:flex;flex-direction:column;align-items:center;gap:2px;min-width:0"><span style="display:flex;border-radius:50%%;%s">%s</span>'
            '<span style="font-size:12px;white-space:nowrap;%s">%s</span></div>' % (ring, C.badge(t, ic, col, 38), 'font-weight:700' if on else '', nm))


def all_cell(t, n=26):
    return ('<div style="flex:1;display:flex;flex-direction:column;align-items:center;gap:2px"><span style="width:38px;height:38px;border-radius:50%%;border:1.5px dashed %s;display:flex;'
            'align-items:center;justify-content:center;font-size:13px">%d</span><span style="font-size:12px">Все</span></div>' % (t['muted'], n))


def cats(t, rows=2):
    cells = [cat_cell(t, nm, ic, col, nm == 'Кафе') for nm, ic, col in CATS]
    if rows == 1:
        body = '<div style="display:flex;margin-top:6px">%s%s</div>' % (''.join(cells[:4]), all_cell(t))
    else:
        body = '<div style="display:flex;margin-top:6px">%s</div><div style="display:flex;margin-top:6px">%s%s</div>' % (''.join(cells[:4]), ''.join(cells[4:7]), all_cell(t))
    return card(t, 'КАТЕГОРИЯ · ЧАСТЫЕ', body, link(t, 'Кафе ✓'))


def tile(t, icon, label, value, empty=False):
    return ('<div style="flex:1;height:40px;border-radius:999px;background:%s;display:flex;align-items:center;gap:8px;padding:0 12px;min-width:0"><span style="font-size:15px">%s</span>'
            '<div style="min-width:0"><div style="font-size:11px;color:%s">%s</div><div style="font-size:13px;white-space:nowrap;overflow:hidden;text-overflow:ellipsis;color:%s">%s</div></div></div>' % (
                t['bar'], icon, t['muted'], label, t['muted'] if empty else t['ink'], value))


def details_tiles(t):
    """Today: tiles two by two — date, note, payee, tags, «Это возврат»."""
    row = lambda a, b: '<div style="display:flex;gap:6px;margin-top:6px">%s%s</div>' % (a, b)  # noqa: E731
    body = (row(tile(t, '📅', 'Дата', 'Сегодня, 28 нояб.'), tile(t, '📝', 'Заметка', 'Без заметки', True))
            + row(tile(t, '🛍', 'Получатель', 'Кофейня «Зерно»'), tile(t, '🏷', 'Теги', 'Без тегов', True))
            + row(tile(t, '☐', 'Это возврат', 'Вернули на счёт', True), '<div style="flex:1"></div>'))
    return card(t, 'ДЕТАЛИ', body)


def chip(t, text, icon='', grow=False, muted=False):
    return ('<span style="height:40px;padding:0 %dpx;border-radius:999px;background:%s;display:flex;align-items:center;gap:6px;font-size:14px;white-space:nowrap;%s%s">%s%s</span>' % (
        12 if text else 0, t['bar'], 'flex:1;min-width:0;overflow:hidden;' if grow else '', 'width:40px;justify-content:center;' if not text else '',
        '<span style="font-size:15px">%s</span>' % icon if icon else '', '<span style="overflow:hidden;text-overflow:ellipsis;color:%s">%s</span>' % (t['muted'] if muted else t['ink'], text) if text else ''))


def details_row(t):
    """Variant A: one line of chips, no card — date and payee in words, empty note and tags as icons (words appear when filled)."""
    return ('<div style="display:flex;gap:6px;margin:0 16px %dpx">%s%s%s%s</div>' % (
        GAP, chip(t, 'Сегодня', '📅'), chip(t, 'Кофейня «Зерно»', '🛍', grow=True), chip(t, '', '📝'), chip(t, '', '🏷')))


def details_link(t):
    """Variant B: one row in a card; a tap opens a small sheet with all the details."""
    return ('<div style="margin:0 16px %dpx;height:48px;border-radius:24px;background:%s;border:1px solid %s;display:flex;align-items:center;gap:10px;padding:0 16px;font-size:14px">'
            '<span style="font-size:12px;letter-spacing:.04em;color:%s">ДЕТАЛИ</span><span style="flex:1;white-space:nowrap;overflow:hidden;text-overflow:ellipsis">Сегодня · Кофейня «Зерно»</span>'
            '<span style="color:%s">›</span></div>' % (GAP, t['surf'], t['line'], t['muted'], t['muted']))


def amount_card(t, big_receipt):
    right = link(t, '↩ Возврат') + link(t, '+ Чаевые') if not big_receipt else link(t, '+ Чаевые')
    return card(t, 'СУММА · СПИСАНО', amount_field(t) + (receipt_big(t) if big_receipt else receipt_line(t)), right)


KGAP = 8
RATE = True


def keypad_top(h, key):
    return h - 12 - (4 * key + 3 * KGAP)


def keypad(t, h, key=56):
    top = keypad_top(h, key)
    k = lambda x: '<span style="height:%dpx;border-radius:24px;background:%s;border:1px solid %s;display:flex;align-items:center;justify-content:center;font-size:22px;%s">%s</span>' % (key, t['bar'], t['line'], MONO, x)  # noqa: E731
    keys = ''.join(k(x) for x in ['7', '8', '9', '⌫', '4', '5', '6', '+', '1', '2', '3', '−', ',', '0', '000'])
    done = ('<span style="border-radius:24px;background:%s;color:%s;font-weight:700;display:flex;gap:6px;align-items:center;justify-content:center;font-size:16px;border:1.5px solid %s">✓ Готово</span>'
            % (t['prim'], t['onp'], U.mix(t['prim'], '#000000', .5)))
    return ('<div style="position:absolute;left:0;right:0;top:%dpx;bottom:0;background:%s;padding:0 16px;z-index:4"><div style="display:grid;grid-template-columns:repeat(4,1fr);gap:%dpx">%s%s</div></div>'
            % (top, t['page'], KGAP, keys, done))


def edge_line(t, y, fits, text):
    color = '#2FBF71' if fits else '#FF4D4D'
    return ('<div style="position:absolute;left:0;right:0;top:%dpx;border-top:2px dashed %s;z-index:7"></div><div style="position:absolute;right:12px;top:%dpx;z-index:7;padding:2px 8px;'
            'border-radius:999px;background:%s;color:#fff;font-size:11px;font-weight:700">%s</div>' % (y - 1, color, y - 20, color, text))


def screen(t, body, h=873, key=56, fits=True, note='', hide_keys=False, w=W):
    """Status strip 30 + sheet from 54 (handle 48 → content from 102), keypad at the bottom."""
    handle = '<div style="height:48px;display:flex;justify-content:center;padding-top:10px"><span style="width:36px;height:4px;border-radius:2px;background:%s"></span></div>' % t['line']
    sheet = ('<div style="position:absolute;inset:0;background:rgba(0,0,0,.4)"></div><div style="position:absolute;left:0;right:0;top:54px;bottom:0;background:%s;border-radius:28px 28px 0 0;'
             'overflow:hidden">%s%s</div>' % (t['page'], handle, body))
    pad = '' if hide_keys else keypad(t, h, key)
    line = edge_line(t, keypad_top(h, key), fits, note) if note and not hide_keys else ''
    return U.phone(t, sheet + pad + line, h).replace('width:390px', 'width:%dpx' % w, 1)


def now(t):
    return screen(t, type_bar(t) + ribbon(t) + amount_card(t, True) + cats(t) + details_tiles(t), fits=False, note='сейчас: ≈140 dp под клавиатурой')


def var_a(t):
    return screen(t, type_bar(t) + ribbon(t) + amount_card(t, False) + cats(t, 2) + details_row(t), note='влезает · запас ≈40 dp')


def var_a_small(t):
    """Short phone 360×740: one row of categories, keys 48 dp with 6 dp gaps, blocks 4 dp apart, the rate only on the pill's sheet."""
    global GAP, KGAP, RATE
    GAP, KGAP, RATE = 4, 6, False
    try:
        return screen(t, type_bar(t) + ribbon(t) + amount_card(t, False) + cats(t, 1) + details_row(t), h=740, key=48, w=360, note='360×740: влезает')
    finally:
        GAP, KGAP, RATE = 6, 8, True


def var_b(t):
    return screen(t, type_bar(t) + ribbon(t) + amount_card(t, False) + cats(t, 2) + details_link(t), note='влезает · запас ≈30 dp')


def var_b_sheet(t):
    """B: the small sheet «Детали» over the entry sheet (today's tiles, nothing else changes)."""
    body = ''.join('<div style="display:flex;gap:6px;margin-top:8px">%s%s</div>' % pair for pair in [
        (tile(t, '📅', 'Дата', 'Сегодня, 28 нояб.'), tile(t, '📝', 'Заметка', 'Без заметки', True)),
        (tile(t, '🛍', 'Получатель', 'Кофейня «Зерно»'), tile(t, '🏷', 'Теги', 'Без тегов', True)),
        (tile(t, '☐', 'Это возврат', 'Вернули на счёт', True), '<div style="flex:1"></div>')])
    base = screen(t, type_bar(t) + ribbon(t) + amount_card(t, False) + cats(t, 2) + details_link(t))
    over = ('<div style="position:absolute;inset:0;background:rgba(0,0,0,.35);z-index:8"></div><div style="position:absolute;left:0;right:0;bottom:0;z-index:9;background:%s;'
            'border-radius:28px 28px 0 0;padding:16px 16px 24px"><div style="display:flex;justify-content:space-between;align-items:center"><b style="font-size:20px">Детали</b>'
            '<span style="height:40px;padding:0 18px;border-radius:999px;background:%s;color:%s;font-weight:700;display:flex;align-items:center">Готово</span></div>%s</div>' % (
                t['page'], t['prim'], t['onp'], body))
    return base[:-6] + over + '</div>'


def var_c(t):
    """C: the keypad hides once the amount is typed and something else is tapped; a tap on the amount brings it back."""
    bar = ('<div style="position:absolute;left:16px;right:16px;bottom:16px;height:56px;border-radius:999px;background:%s;color:%s;font-weight:700;font-size:16px;display:flex;'
           'align-items:center;justify-content:center;box-shadow:inset 0 0 0 1.5px %s;z-index:4">✓ Готово</div>' % (t['prim'], t['onp'], A.edge(t)))
    body = type_bar(t) + ribbon(t) + amount_card(t, True) + cats(t) + details_tiles(t)
    return screen(t, body, hide_keys=True)[:-6] + bar + '</div>'


BOARDS = [('E818Now', now, W, 873), ('E818A', var_a, W, 873), ('E818ASmall', var_a_small, 360, 740),
          ('E818B', var_b, W, 873), ('E818BSheet', var_b_sheet, W, 873), ('E818C', var_c, W, 873)]

if __name__ == '__main__':
    C.run(BOARDS, sys.argv[1])
