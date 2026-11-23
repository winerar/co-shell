#!/usr/bin/env python3
"""Poll boards for #553: «Календарь трат» in «Отчёты» (month grid, brightness by amount).
Usage: python3 scripts/agent/mockup_poll_calendar553.py <out-dir>  → <out-dir>/img/*.png, then poll-page.py <spec> <poll-dir> --img <out-dir>/img
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from mockup_poll_f_common import *  # noqa: E402,F401,F403
import mockup_poll_f_common as C  # noqa: E402

H, TODAY = 780, 23
# November 2026 starts on Sunday: Mon-first grid → six blanks, then the 1st. Spent per day, USD (the 14th is the rent).
SPENT = {1: 12, 2: 48, 3: 31, 4: 0, 5: 66, 6: 22, 7: 140, 8: 9, 9: 35, 10: 54, 11: 0, 12: 41, 13: 18, 14: 612, 15: 26, 16: 73, 17: 0,
         18: 44, 19: 37, 20: 95, 21: 15, 22: 28, 23: 51}
MONTHS = 'Ноябрь 2026'


def level(v, vals):
    """Quantile bucket 0..4 (0 = no spending): one huge day must not flatten the rest."""
    if v <= 0:
        return 0
    s = sorted(x for x in vals if x > 0)
    return 1 + sum(1 for q in (.25, .5, .75) if v > s[int(q * (len(s) - 1))])


def cell_bg(t, lv):
    if lv == 0:
        return t['wash'] if False else mix(t['page'], t['acc'], .08 if t['dark'] else .06)
    f = [0, .18, .38, .62, .95][lv]
    return mix(t['page'] if t['dark'] else '#FFFFFF', t['prim'], f)


def cell(t, d, vals, sel=None, amount=False, size=44, empty=False):
    if d is None:
        return '<span style="height:%dpx"></span>' % size
    v = SPENT.get(d, 0) if not empty else 0
    future = d > TODAY
    lv = level(v, vals)
    bg = cell_bg(t, lv) if not future else 'transparent'
    ring = ''
    if d == sel:
        ring = 'box-shadow:inset 0 0 0 2px %s;' % edge(t)
    elif d == TODAY:
        ring = 'box-shadow:inset 0 0 0 1.5px %s;' % edge(t)
    elif lv >= 3 and not t['dark']:
        ring = 'box-shadow:inset 0 0 0 1px %s;' % mix(t['prim'], '#000000', .5)
    ink = (t['onp'] if lv >= 3 else t['ink']) if not future else t['muted']
    amt = ''
    if amount and v:
        amt = '<span style="%sfont-size:9px;opacity:.85">%s</span>' % (C.MONO, ('%dк' % round(v / 1000) if v >= 1000 else str(v)) if v < 1000 else '0,6к')
    return ('<span style="height:%dpx;border-radius:14px;background:%s;%scolor:%s;display:flex;flex-direction:column;align-items:center;justify-content:center;font-size:15px;'
            'line-height:1.1;%s">%s%s</span>' % (size, bg, ring, ink, 'opacity:.55;' if future else '', d, amt))


def grid(t, sel=None, amount=False, size=44, empty=False, w=None):
    vals = list(SPENT.values())
    days = [None] * 6 + list(range(1, 31))
    head = ''.join('<span style="text-align:center;font-size:12px;color:%s">%s</span>' % (t['muted'], d) for d in 'ПВСЧПСВ')
    cells = ''.join(cell(t, d, vals, sel, amount, size, empty) for d in days)
    return '<div style="display:grid;grid-template-columns:repeat(7,1fr);gap:4px;margin-top:10px">%s%s</div>' % (head, cells)


def legend(t, empty=False):
    sw = ''.join('<span style="width:18px;height:18px;border-radius:6px;background:%s"></span>' % cell_bg(t, i) for i in range(1, 5))
    n = 30 if empty else 4
    return ('<div style="display:flex;align-items:center;gap:6px;margin-top:10px;font-size:12px;color:%s"><span>меньше</span>%s<span>больше</span>'
            '<span style="flex:1;text-align:right">без трат: %d дней</span></div>' % (t['muted'], sw, n))


def period(t):
    b = lambda p: '<span style="width:48px;height:48px;display:flex;align-items:center;justify-content:center">%s</span>' % glyph(p, t['ink'])  # noqa: E731
    return ('<div style="margin:0 16px;height:52px;display:flex;align-items:center">%s<b style="flex:1;text-align:center;font-size:16px">%s</b>%s%s</div>' % (
        b(CHEV_L), MONTHS, b(CHEV_R), C.pill(t, 'USD ▾', h=36)))


def day_ops(t, label='Среда, 18 ноября'):
    rows = (C.row(t, 'cart', '#7FD6A0', 'Продукты', 'Карта · Диа', '−28,40 USD') + C.row(t, 'cafe', '#FFB86B', 'Кафе', 'Наличные', '−9,60 USD') +
            C.row(t, 'taxi', '#6BB8FF', 'Такси', 'Карта', '−6,00 USD'))
    return ('<div style="margin:12px 16px 0;padding:14px 0 4px;border-radius:24px;background:%s;border:1px solid %s"><div style="display:flex;justify-content:space-between;'
            'padding:0 16px;font-size:16px;font-weight:600"><span>%s</span><span style="font-size:14px;font-weight:400;color:%s">потрачено 44 USD</span></div>%s</div>' % (t['surf'], t['line'], label, t['muted'], rows))


def rep_cards(t):
    bars = ''.join('<div style="display:flex;justify-content:space-between;font-size:14px;margin-top:8px"><span>%s</span><span style="%s">%s</span></div>%s' % (
        n, C.MONO, a, C.meter(t, w)) for n, a, w in [('Продукты', '410', .8), ('Дом', '680', 1.0), ('Кафе', '120', .3)])
    return C.card(t, 'На что ушли деньги', 'ноябрь', bars)


def calendar_card(t, sel=18, amount=False, empty=False, size=44):
    return C.card(t, 'Календарь трат', 'нажмите на день', grid(t, sel, amount, size, empty) + legend(t, empty))


def tab_a(t):
    body = C.topbar(t, 'Отчёты', back=False) + '<div style="padding-top:72px">' + C.segmented(t, ['Обзор', 'Календарь'], 1) + period(t) + \
        calendar_card(t, 18).replace('margin:12px 16px 0', 'margin:0 16px 0') + day_ops(t) + '</div>' + C.nav(t)
    return body + C.caption(t, 'А: «Календарь» — вторая вкладка внутри «Отчётов» (листается свайпом). Под сеткой — операции выбранного дня.', 870)


def card_b(t):
    body = C.topbar(t, 'Отчёты', back=False) + '<div style="padding-top:72px">' + period(t) + calendar_card(t, None).replace('margin:12px 16px 0', 'margin:0 16px 0') + rep_cards(t) + '</div>' + C.nav(t)
    return body + C.caption(t, 'Б: календарь — первая карточка на экране «Отчёты». Нажатие на день открывает операции шторкой.', 770)


def sheet_c(t):
    ops = (C.row(t, 'cart', '#7FD6A0', 'Продукты', 'Карта · Диа', '−28,40 USD') + C.row(t, 'cafe', '#FFB86B', 'Кафе', 'Наличные', '−9,60 USD') +
           C.row(t, 'taxi', '#6BB8FF', 'Такси', 'Карта', '−6,00 USD'))
    page = C.topbar(t, 'Отчёты', back=False) + '<div style="padding-top:72px">' + period(t) + calendar_card(t, 18).replace('margin:12px 16px 0', 'margin:0 16px 0') + '</div>'
    return page + C.sheet(t, 'Среда, 18 ноября', ops, 330, sub='Потрачено 44 USD · операций: 3') + C.caption(t, 'Б: нажатие на день открывает шторку с его операциями — календарь остаётся целиком на виду.', 100)


def num_a(t):
    return C.topbar(t, 'Отчёты', back=False) + '<div style="padding-top:72px">' + period(t) + calendar_card(t, 18).replace('margin:12px 16px 0', 'margin:0 16px 0') + '</div>' + \
        C.caption(t, 'А: в ячейке только число дня. Яркость показывает сумму; точная — после нажатия.', 560) + C.nav(t)


def num_b(t):
    return C.topbar(t, 'Отчёты', back=False) + '<div style="padding-top:72px">' + period(t) + calendar_card(t, 18, amount=True, size=52).replace('margin:12px 16px 0', 'margin:0 16px 0') + '</div>' + \
        C.caption(t, 'Б: под числом дня — сумма мелко (в основной валюте). Читается без нажатий, но плотнее.', 640) + C.nav(t)


def empty(t):
    return C.topbar(t, 'Отчёты', back=False) + '<div style="padding-top:72px">' + C.segmented(t, ['Обзор', 'Календарь'], 1) + period(t) + \
        calendar_card(t, None, empty=True).replace('margin:12px 16px 0', 'margin:0 16px 0') + \
        '<div style="margin:12px 16px 0;font-size:14px;color:%s;text-align:center">Трат в этом месяце пока нет. Нажмите «+», чтобы записать первую.</div>' % t['muted'] + C.nav(t)


def stress(t):
    """320 dp wide: cells shrink to 36, the legend wraps; font ×1,3 imitated by larger text."""
    inner = grid(t, 18, size=36).replace('font-size:15px', 'font-size:19px').replace('font-size:12px', 'font-size:16px')
    lg = legend(t).replace('font-size:12px', 'font-size:16px').replace('flex:1;text-align:right', 'width:100%;text-align:left')
    lg = lg.replace('display:flex;align-items:center', 'display:flex;flex-wrap:wrap;align-items:center')
    return C.topbar(t, 'Отчёты', back=False) + '<div style="padding-top:72px">' + period(t) + C.card(t, 'Календарь трат', '', inner + lg).replace('margin:12px 16px 0', 'margin:0 12px 0') + '</div>'


BOARDS = [('CalTabA', tab_a, 390, 940), ('CalCardB', card_b, 390, 860), ('CalSheetC', sheet_c, 390, H), ('CalNumA', num_a, 390, 700), ('CalNumB', num_b, 390, 760),
          ('CalEmpty', empty, 390, 620), ('CalStress', stress, 320, 700)]

if __name__ == '__main__':
    C.run(BOARDS, sys.argv[1])
