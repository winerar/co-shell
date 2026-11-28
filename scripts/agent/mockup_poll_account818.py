#!/usr/bin/env python3
"""Poll boards for #818, step 2: the account sheet without scrolling (worst case: an existing credit card in a family).

Real dp as in `mockup_poll_entry818.py`: phone 393×873, the sheet's content from 102 dp; «Сохранить» 48 dp pinned 16 dp above
the bottom — the dashed line is its top: everything above must fit. Short phone 360×740 — the second board of a variant.
Usage: python3 scripts/agent/mockup_poll_account818.py <out-dir>  → <out-dir>/img/*.png, then poll-page.py <spec> <poll> --img <out-dir>/img
"""
import os
import sys

HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, HERE)
import mockup_poll_accounts4 as A  # noqa: E402
import mockup_poll_entry818 as E  # noqa: E402
import mockup_poll_f_common as C  # noqa: E402
import mockup_poll_update as U  # noqa: E402

MONO = A.MONO
W = 393
COLORS = ['#FF2D3D', '#FF8A80', '#FF7A00', '#FFD3B8']


def title(t):
    icons = ('<span style="width:48px;display:flex;justify-content:center">%s</span><span style="width:48px;display:flex;justify-content:center;color:#FF4D6A">%s</span>'
             % (C.glyph('M4 7h16v3H4zM6 10v9h12v-9M10 14h4', t['ink']), C.glyph('M5 7h14M10 7V4h4v3M7 7l1 13h8l1-13', '#FF4D6A')))
    return '<div style="display:flex;align-items:center;margin:0 16px 8px;height:48px"><b style="font-size:24px;flex:1">Счёт</b>%s</div>' % icons


def preview(t, kind='Кредитная карта'):
    return ('<div style="margin:0 16px 6px;padding:12px 16px;border-radius:24px;background:%s;color:%s"><div style="display:flex;justify-content:space-between;font-size:14px">'
            '<span>Galicia · карта</span><span>%s</span></div><div style="%sfont-size:22px;margin-top:4px">−120 000 ARS</div></div>' % (t['prim'], t['onp'], kind, MONO))


def name_row(t, badge_note=False):
    dot = ('<span style="position:absolute;right:-2px;bottom:-2px;width:20px;height:20px;border-radius:50%%;background:%s;box-shadow:0 0 0 2px %s;display:flex;align-items:center;'
           'justify-content:center;font-size:11px">✎</span>' % (t['bar'], t['page'])) if badge_note else ''
    badge = ('<span style="position:relative;width:56px;height:56px;border-radius:16px;background:#F26E22;color:#1A0A00;font-weight:700;display:flex;align-items:center;'
             'justify-content:center;flex:none">GA%s</span>' % dot)
    field = ('<div style="flex:1;height:56px;border-radius:16px;border:1px solid %s;background:%s;padding:6px 16px;box-sizing:border-box"><div style="font-size:12px;color:%s">Название</div>'
             '<div style="font-size:16px">Galicia · карта</div></div>' % (t['line'], t['bar'], t['muted']))
    return '<div style="display:flex;gap:8px;margin:0 16px 6px">%s%s</div>' % (badge, field)


def tiles(t, *rows):
    out = ''
    for r in rows:
        cells = ''.join(E.tile(t, *c) for c in r)
        out += '<div style="display:flex;gap:6px;margin-top:6px">%s</div>' % cells
    return out


CUR = ('💵', 'Валюта', 'ARS · не изменить')
START = ('👛', 'Начальный баланс', '0')
NOW = ('↻', 'Баланс сейчас', '−120 000 ARS')
TERMS = ('💳', 'Условия карты', 'Лимит 500 000 · до 10-го · от 5 %')
DIGITS = ('🧾', 'Для чеков', 'Не заданы', True)
TYPE = ('🗂', 'Тип', 'Кредитка')


def money(t, extra_rows=()):
    return E.card(t, 'ДЕНЬГИ', tiles(t, (CUR, START), (NOW,), (TERMS,), *extra_rows))


def types(t):
    chips = ''.join(C.pill(t, s, s == 'Кредитка') for s in ['Наличные', 'Карта', 'Вклад', 'Крипто', 'Другое', 'Проездной', 'Кредитка'])
    return E.card(t, 'ТИП', '<div style="display:flex;flex-wrap:wrap;gap:6px;margin-top:6px">%s</div>' % chips)


def digits_card(t):
    return E.card(t, 'ДЛЯ ЧЕКОВ', tiles(t, (('🧾', 'Последние цифры карты или счёта', 'Не заданы', True),)))


def colors(t):
    ring = '<span style="width:36px;height:36px;border-radius:50%%;box-shadow:inset 0 0 0 2px %s;display:flex;align-items:center;justify-content:center">✓</span>' % A.edge(t)
    dots = ''.join('<span style="width:36px;height:36px;border-radius:50%%;background:%s"></span>' % c for c in COLORS)
    more = '<span style="height:36px;padding:0 12px;border-radius:999px;box-shadow:inset 0 0 0 1px %s;display:flex;align-items:center;font-size:14px">+20</span>' % t['line']
    return E.card(t, 'ЦВЕТ', '<div style="display:flex;gap:10px;margin-top:6px;align-items:center">%s%s%s</div>' % (ring, dots, more), E.link(t, 'Как у Galicia'))


def switch(t, on):
    return ('<span style="width:48px;height:28px;border-radius:999px;flex:none;position:relative;background:%s;box-shadow:inset 0 0 0 2px %s"><span style="position:absolute;top:4px;%s:4px;'
            'width:20px;height:20px;border-radius:50%%;background:%s"></span></span>' % (t['prim'] if on else t['wash'], A.edge(t), 'right' if on else 'left', t['onp'] if on else t['muted']))


def line(t, title_, sub, on):
    return ('<div style="display:flex;align-items:center;gap:12px;margin-top:6px"><div style="flex:1"><div style="font-size:15px">%s</div><div style="font-size:12px;color:%s">%s</div></div>%s</div>'
            % (title_, t['muted'], sub, switch(t, on)))


def balance(t):
    return E.card(t, 'БАЛАНС', line(t, 'Учитывать в общем балансе', 'Выключите для долгов и чужих денег', False) + line(t, 'Только у меня', 'Семья не увидит счёт и его операции', False))


def screen(t, body, h=873, w=W, note='', fits=True):
    handle = '<div style="height:48px;display:flex;justify-content:center;padding-top:10px"><span style="width:36px;height:4px;border-radius:2px;background:%s"></span></div>' % t['line']
    save_top = h - 16 - 48
    save = ('<div style="position:absolute;left:16px;right:16px;top:%dpx;height:48px;border-radius:999px;background:%s;color:%s;font-weight:700;display:flex;align-items:center;'
            'justify-content:center;z-index:4;box-shadow:inset 0 0 0 1px %s">Сохранить</div><div style="position:absolute;left:0;right:0;top:%dpx;bottom:0;background:%s;z-index:3"></div>'
            % (save_top, t['prim'], t['onp'], A.edge(t), save_top - 8, t['page']))
    sheet = ('<div style="position:absolute;inset:0;background:rgba(0,0,0,.4)"></div><div style="position:absolute;left:0;right:0;top:54px;bottom:0;background:%s;border-radius:28px 28px 0 0;'
             'overflow:hidden">%s%s</div>' % (t['page'], handle, body))
    edge = E.edge_line(t, save_top - 8, fits, note) if note else ''
    return U.phone(t, sheet + save + edge, h).replace('width:390px', 'width:%dpx' % w, 1)


def now(t):
    return screen(t, title(t) + preview(t) + name_row(t) + money(t) + types(t) + digits_card(t) + colors(t) + balance(t), fits=False, note='сейчас: ≈180 dp под кнопкой')


def var_a(t, short=False):
    """A: colour → the badge left of the name (tap — the icon/colour sheet, «Как у Galicia» there); «Для чеков» → a tile in «Деньги»."""
    body = title(t) + ('' if short else preview(t)) + name_row(t, True) + money(t, ((DIGITS,),)) + types(t) + balance(t)
    return screen(t, body, *((740, 360) if short else (873, W)), fits=not short, note='360×740: не влезает' if short else 'влезает впритык')


def var_b(t, short=False):
    """B: «Тип» → a tile in «Деньги» (a list sheet on tap); «Для чеков» → a tile beside it; the colour card stays."""
    body = title(t) + ('' if short else preview(t)) + name_row(t) + money(t, ((TYPE, DIGITS),)) + colors(t) + balance(t)
    return screen(t, body, *((740, 360) if short else (873, W)), fits=not short, note='360×740: не влезает' if short else 'влезает · запас ≈60 dp')


def var_c(t, short=False):
    """C: both — colour in the badge and «Тип» as a tile: the shortest form; the preview card stays even on a short phone."""
    body = title(t) + preview(t) + name_row(t, True) + money(t, ((TYPE, DIGITS),)) + balance(t)
    return screen(t, body, *((740, 360) if short else (873, W)), note='360×740: влезает' if short else 'влезает · запас ≈110 dp')


BOARDS = [('Acc818Now', now, W, 873), ('Acc818A', var_a, W, 873), ('Acc818ASmall', lambda t: var_a(t, True), 360, 740),
          ('Acc818B', var_b, W, 873), ('Acc818BSmall', lambda t: var_b(t, True), 360, 740),
          ('Acc818C', var_c, W, 873), ('Acc818CSmall', lambda t: var_c(t, True), 360, 740)]

if __name__ == '__main__':
    C.run(BOARDS, sys.argv[1])
