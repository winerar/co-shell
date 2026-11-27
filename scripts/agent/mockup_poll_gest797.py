#!/usr/bin/env python3
"""Poll boards for #797 («Помощь → Жесты»: a cheat sheet of every gesture) and #770 (balance forecast for 30–60 days).
Usage: python3 scripts/agent/mockup_poll_gest797.py <out-dir>  → <out-dir>/img/*.png (dark and «@light»), then
`agent.sh poll docs/design/poll-797-770.json --img <out-dir>/img`. Standalone proposals: after the owner's «ок» the winner goes to the canvas.
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from mockup_poll_f_common import *  # noqa: E402,F401,F403
import mockup_poll_f_common as C  # noqa: E402

B = C.B
WARN = '#FFB020'  # drawn amber on the boards; the runtime paints the theme's own attention colour


# ---- #797 · gestures ------------------------------------------------------------------------------------------------

GESTURES = [
    ('«Обзор»', [('Нажмите на карточку', 'перевернуть: на обороте самое полезное за месяц'), ('Удерживайте карточку', 'переставить или скрыть')]),
    ('«+»', [('Нажмите', 'новая операция'), ('Удерживайте', 'веером: QR чека, заметка, шаблон')]),
    ('«Операции»', [('Нажмите на операцию', 'поправить'), ('Удерживайте операцию', 'выбрать несколько'), ('Удерживайте сумму', 'увидеть её в других валютах')]),
    ('Везде', [('Листайте вбок', 'сменить вкладку'), ('Встряхните телефон', 'отменить только что сохранённую операцию'), ('Удерживайте значок приложения', 'расход или доход сразу')]),
]


def gest_row(t, title, text, first=False):
    line = '' if first else 'border-top:1px solid %s;' % t['line']
    return ('<div style="padding:10px 16px;%s"><div style="font-size:15px;font-weight:600">%s</div><div style="font-size:13px;color:%s">%s</div></div>' % (line, title, t['muted'], text))


def gest_a(t):
    blocks = ''
    for head, items in GESTURES:
        rows = ''.join(gest_row(t, a, b, i == 0) for i, (a, b) in enumerate(items))
        blocks += ('<div style="margin:12px 16px 0"><div style="font-size:13px;font-weight:600;color:%s;margin:0 4px 6px;letter-spacing:.04em;text-transform:uppercase">%s</div>'
                   '<div style="border-radius:24px;background:%s;border:1px solid %s;overflow:hidden">%s</div></div>' % (C.ink2(t), head, t['surf'], t['line'], rows))
    return (C.topbar(t, 'Жесты') + '<div style="padding-top:72px">%s</div>' % blocks
            + C.caption(t, 'А: по экранам — человек ищет «что делать на “Операциях”». «Нажмите — X, удерживайте — Y» сказано словами заголовка строки. Открывается из «Ещё → Настройки → Помощь → Жесты».', 0))


def gest_b(t):
    groups = [('Нажмите', ['Карточка «Обзора» — перевернуть', '«+» — новая операция', 'Операция — поправить']),
              ('Удерживайте', ['Карточку «Обзора» — переставить или скрыть', '«+» — QR чека, заметка, шаблон', 'Операцию — выбрать несколько', 'Сумму — увидеть в других валютах', 'Значок приложения — расход или доход']),
              ('Листайте и встряхивайте', ['Вбок — сменить вкладку', 'Встряхните — отменить только что сохранённое'])]
    blocks = ''
    for head, items in groups:
        rows = ''.join('<div style="padding:9px 16px;font-size:14px;%s">%s</div>' % ('' if i == 0 else 'border-top:1px solid %s;' % t['line'], x) for i, x in enumerate(items))
        blocks += ('<div style="margin:12px 16px 0"><div style="font-size:13px;font-weight:600;color:%s;margin:0 4px 6px">%s</div>'
                   '<div style="border-radius:24px;background:%s;border:1px solid %s;overflow:hidden">%s</div></div>' % (C.ink2(t), head, t['surf'], t['line'], rows))
    return (C.topbar(t, 'Жесты') + '<div style="padding-top:72px">%s</div>' % blocks
            + C.caption(t, 'Б: по движению пальца — три коротких списка: нажать, удерживать, листать. Короче, но экран, к которому относится жест, назван в конце строки.', 0))


def gest_c(t):
    allr = [(a, b) for _, items in GESTURES for a, b in items]
    rows = ''.join(gest_row(t, a, b, i == 0) for i, (a, b) in enumerate(allr))
    return (C.topbar(t, 'Жесты') + '<div style="padding-top:72px"><div style="margin:12px 16px 0;border-radius:24px;background:%s;border:1px solid %s;overflow:hidden">%s</div></div>' % (t['surf'], t['line'], rows)
            + C.caption(t, 'В: один плоский список из 10 строк без заголовков — самый простой, но придётся пролистать целиком.', 0))


# ---- #770 · forecast ------------------------------------------------------------------------------------------------

def chart(t, low_below=False, w=326, h=130):
    """Balance: the last 14 days solid, 60 days ahead dashed; zero line; the lowest point marked."""
    zero = 96
    past = [64, 62, 66, 60, 58, 61, 56, 54, 57, 52, 50, 53, 48, 46]
    fut = [46, 52, 56, 62, 68, 72, 78, 74, 70, 64, 58, 56, 52, 50, 48, 46, 44, 42, 40, 38, 36]
    if low_below:
        fut = [46, 54, 62, 72, 84, 92, 100, 108, 104, 98, 90, 84, 78, 72, 66, 60, 54, 50, 46, 42, 40]
    n = len(past) + len(fut)
    step = w / (n - 1)
    pts = lambda arr, off: ' '.join('%.1f,%d' % ((off + i) * step, v) for i, v in enumerate(arr))  # noqa: E731
    low_i = max(range(len(fut)), key=lambda i: fut[i])  # the lowest balance is the lowest point on screen: the largest y
    lx, ly = (len(past) + low_i) * step, fut[low_i]
    mark = WARN if low_below else C.ink2(t)
    return ('<svg width="%d" height="%d" viewBox="0 0 %d 130" preserveAspectRatio="none" fill="none"><line x1="0" y1="%d" x2="%d" y2="%d" stroke="%s" stroke-width="1" stroke-dasharray="2 4"/>'
            '<polyline points="%s" stroke="%s" stroke-width="2.4" stroke-linecap="round" stroke-linejoin="round"/>'
            '<polyline points="%s" stroke="%s" stroke-width="2.4" stroke-dasharray="5 5" stroke-linecap="round"/>'
            '<line x1="%.1f" y1="0" x2="%.1f" y2="%d" stroke="%s" stroke-width="1" stroke-dasharray="1 3"/>'
            '<circle cx="%.1f" cy="%d" r="5" fill="%s" stroke="%s" stroke-width="2"/></svg>'
            % (w, h, w, zero, w, zero, t['muted'], pts(past, 0), t['prim'] if not t['dark'] else t['prim'], pts([past[-1]] + fut, len(past) - 1), mark,
               len(past) * step, len(past) * step, 130, t['muted'], lx, ly, mark, t['page']))


def fc_card(t, low_below=False, head_extra=''):
    verdict = ('<div style="font-size:13px;color:%s;margin-top:2px">Не хватит с 3 декабря · дальше −40 USD</div>' % WARN) if low_below else \
        '<div style="font-size:13px;color:%s;margin-top:2px">Самая низкая точка: 12 декабря, ≈ 80 USD</div>' % t['muted']
    seg = '<div style="margin:10px 0 0">%s</div>' % C.segmented(t, ['30 дней', '60 дней'], 0).replace('margin:12px 16px 0;', 'margin:0;')
    chips = '<div style="display:flex;gap:8px;margin-top:10px">%s%s</div>' % (C.pill(t, 'Все счета', True, 32), C.pill(t, 'Galicia', False, 32))
    axis = '<div style="display:flex;justify-content:space-between;font-size:11px;color:%s;margin-top:2px"><span>14 ноября</span><span>сегодня</span><span>26 января</span></div>' % t['muted']
    body = ('<div style="font-size:12px;color:%s;margin-top:6px">%s</div>' % (t['muted'], 'сплошная — было, пунктир — ожидаем') + verdict + chips
            + '<div style="margin-top:8px">%s</div>%s%s' % (chart(t, low_below), axis, seg))
    return C.card(t, 'Прогноз остатка', head_extra, body)


def fc_a(t):
    return (C.topbar(t, 'Отчёты', back=False) + '<div style="padding-top:72px">%s</div>' % fc_card(t) + C.nav(t)
            + C.caption(t, 'А: карточка в «Отчётах» — линия остатка: сплошная — что было, пунктир — что ожидаем (платежи с «Повтором» + средние траты за 3 месяца). Отметка самой низкой точки, переключатель «30 · 60 дней», чипы «Все счета» или один счёт.', 0))


def fc_a_low(t):
    return (C.topbar(t, 'Отчёты', back=False) + '<div style="padding-top:72px">%s</div>' % fc_card(t, True) + C.nav(t)
            + C.caption(t, 'А, когда не хватает: линия уходит под нулевую пунктирную черту, подпись — в цвете внимания (не красным, без «превышено»). Слова: «Не хватит с 3 декабря».', 0))


def fc_b(t):
    spark = chart(t, False, 150, 56)
    body = ('<div style="display:flex;align-items:center;justify-content:space-between;gap:12px"><div><div style="font-size:16px;font-weight:600">Денег хватит на 60 дней</div>'
            '<div style="font-size:13px;color:%s;margin-top:2px">Низшая точка 12 декабря, ≈ 80 USD</div></div>%s</div>' % (t['muted'], spark))
    low = ('<div style="margin-top:14px;display:flex;justify-content:space-between;align-items:center"><div><div style="font-size:16px;font-weight:600;color:%s">Не хватит с 3 декабря</div>'
           '<div style="font-size:13px;color:%s;margin-top:2px">Дальше −40 USD</div></div>%s</div>' % (WARN, t['muted'], chart(t, True, 150, 56)))
    return (C.topbar(t, 'Отчёты', back=False) + '<div style="padding-top:72px">%s%s</div>' % (C.card(t, 'Прогноз остатка', 'Подробнее ›', body), C.card(t, 'Если денег не хватает', '', low)) + C.nav(t)
            + C.caption(t, 'Б: короткая карточка-вердикт со спарклайном; «Подробнее» открывает полный график. Меньше цифр на «Отчётах», но детали на втором экране.', 0))


def fc_c(t):
    weeks = [('7 дек', 52), ('14 дек', 36), ('21 дек', 24), ('28 дек', 30), ('4 янв', 40), ('11 янв', 44), ('18 янв', 50), ('25 янв', 56)]
    cols = ''.join('<div style="flex:1;display:flex;flex-direction:column;align-items:center;justify-content:flex-end;gap:4px;height:100%%"><span style="font-size:10px;color:%s">%d</span>'
                   '<span style="width:70%%;height:%dpx;border-radius:6px 6px 2px 2px;background:%s;box-shadow:inset 0 0 0 1px %s"></span><span style="font-size:10px;color:%s">%s</span></div>'
                   % (t['muted'], v * 2, v * 1.5, WARN if v < 30 else t['prim'], C.edge(t), t['muted'], d) for d, v in weeks)
    body = ('<div style="font-size:13px;color:%s;margin:2px 0 8px">Остаток на конец недели, USD · самая низкая — 21 декабря, ≈ 48</div><div style="display:flex;gap:4px;height:130px">%s</div>' % (t['muted'], cols))
    return (C.topbar(t, 'Отчёты', back=False) + '<div style="padding-top:72px">%s</div>' % C.card(t, 'Прогноз остатка', '8 недель', body) + C.nav(t)
            + C.caption(t, 'В: столбцы по неделям — как «Столбцы по месяцам» в отчётах (привычный компонент). Слабее показывает, в какой именно день провал.', 0))


BOARDS = [('Gest797A', gest_a, 390, 960), ('Gest797B', gest_b, 390, 960), ('Gest797C', gest_c, 390, 960),
          ('Fc770A', fc_a, 390, 820), ('Fc770ALow', fc_a_low, 390, 820), ('Fc770B', fc_b, 390, 820), ('Fc770C', fc_c, 390, 820)]

if __name__ == '__main__':
    C.run(BOARDS, sys.argv[1])
