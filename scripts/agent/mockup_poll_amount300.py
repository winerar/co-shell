#!/usr/bin/env python3
"""Poll boards for #300: how to type a negative balance in the amount sheet (balance of an account, balance from Wallet), session 2026-11-26.
Usage: python3 scripts/agent/mockup_poll_amount300.py <out-dir>  → <out-dir>/img/*.png, then poll-page.py <spec> <poll-dir> --img <out-dir>/img
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from mockup_poll_f_common import *  # noqa: E402,F401,F403
import mockup_poll_f_common as C  # noqa: E402


def key(t, label, tone='key', span=''):
    bg = {'key': t['wash'], 'op': C.mix(t['wash'], t['acc'], .25), 'done': t['prim']}[tone]
    col = t['onp'] if tone == 'done' else (t['ink'] if tone == 'key' else C.ink2(t))
    ring = 'box-shadow:inset 0 0 0 1px %s;' % C.edge(t) if tone == 'done' else ''
    return ('<span style="border-radius:16px;background:%s;color:%s;%s display:flex;align-items:center;justify-content:center;font-size:22px;%s;%s">%s</span>'
            % (bg, col, ring, C.MONO, span, label))


def keypad(t, minus=False):
    """The own keypad of the sheet as in the app: digits in three columns, the right column «⌫ · [−] · Готово» (tall, it takes the free rows)."""
    def cell(label, tone, col, row, rows=1):
        return key(t, label, tone, 'grid-column:%d;grid-row:%d / span %d' % (col, row, rows))
    digits = ['7', '8', '9', '4', '5', '6', '1', '2', '3', ',', '0', '000']
    cells = [cell(d, 'key', i % 3 + 1, i // 3 + 1) for i, d in enumerate(digits)]
    cells.append(cell('⌫', 'op', 4, 1))
    if minus:
        cells += [cell('−', 'op', 4, 2), cell('Готово', 'done', 4, 3, 2)]
    else:
        cells.append(cell('Готово', 'done', 4, 2, 3))
    return '<div style="display:grid;grid-template-columns:repeat(4,1fr);grid-template-rows:repeat(4,52px);gap:8px;margin-top:12px">%s</div>' % ''.join(cells)


def figure(t, text, unit='ARS'):
    return ('<div style="margin-top:14px;display:flex;align-items:center;justify-content:flex-end;min-height:52px"><span style="%sfont-size:40px;font-weight:700">%s</span>'
            '<span style="width:2px;height:34px;background:%s;margin-left:2px"></span><span style="%sfont-size:18px;color:%s;margin-left:8px">%s</span></div>'
            % (C.MONO, text, t['prim'], C.MONO, t['muted'], unit))


def under(t):
    """The editor of an account behind the sheet."""
    rows = C.card(t, 'Новый счёт', '', '<div style="margin-top:8px;font-size:15px;color:%s">Карта Galicia · ARS</div>' % t['muted'])
    return C.topbar(t, 'Счёт') + '<div style="padding-top:72px">%s</div>' % rows


def switch_line(t, label, hint, on):
    knob = ('<span style="width:52px;height:32px;border-radius:999px;background:%s;box-shadow:inset 0 0 0 1px %s;position:relative;flex:none">'
            '<span style="position:absolute;top:4px;%s:4px;width:24px;height:24px;border-radius:50%%;background:%s"></span></span>' % (
                t['prim'] if on else t['wash'], C.edge(t), 'right' if on else 'left', t['onp'] if on else t['muted']))
    return ('<div style="margin-top:12px;display:flex;align-items:center;gap:12px"><div style="flex:1"><div style="font-size:16px">%s</div>'
            '<div style="font-size:13px;color:%s">%s</div></div>%s</div>' % (label, t['muted'], hint, knob))


def var_a(t):
    body = figure(t, '−120 000') + '<div style="text-align:right;font-size:13px;color:%s;margin-top:2px">Нажмите «−», чтобы баланс был ниже нуля</div>' % t['muted'] + keypad(t, minus=True)
    return under(t) + C.sheet(t, 'Баланс на старте', body, 120)


def var_b(t):
    body = figure(t, '120 000') + switch_line(t, 'Баланс в минусе', 'Например, кредитная карта', True) + keypad(t)
    return under(t) + C.sheet(t, 'Баланс на старте', body, 120)


def var_c(t):
    body = C.segmented(t, ['В плюсе', 'В минусе'], 1).replace('margin:12px 16px 0', 'margin:10px 0 0') + figure(t, '−120 000') + keypad(t)
    return under(t) + C.sheet(t, 'Баланс на старте', body, 120)


BOARDS = [('Amount300A', var_a, 390, 840), ('Amount300B', var_b, 390, 840), ('Amount300C', var_c, 390, 840)]

if __name__ == '__main__':
    C.run(BOARDS, sys.argv[1])
