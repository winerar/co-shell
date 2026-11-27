#!/usr/bin/env python3
"""Poll boards for #795 (quick entry from outside the app: home-screen widget, quick-settings tile) and #796 (labels in the bottom bar).
Usage: python3 scripts/agent/mockup_poll_quick795.py <out-dir>  → <out-dir>/img/*.png (dark and «@light»), then
`agent.sh poll docs/design/poll-795-796.json --img <out-dir>/img`.
Standalone proposals on purpose: the bottom bar variants must not enter the canvas (`test_mockup_rules.py` keeps its bar equal to the code).
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from mockup_poll_f_common import *  # noqa: E402,F401,F403
import mockup_poll_f_common as C  # noqa: E402

B = C.B


def launcher(t, body, cap):
    """A phone home screen: time, a row of other apps, then [body] — the widget under test; the caption sits in the lower part."""
    apps = ''.join('<span style="width:56px;height:56px;border-radius:18px;background:%s"></span>' % C.mix(t['page'], t['ink'], .12 + i * .03) for i in range(4))
    return ('<div style="padding:56px 20px 0"><div style="font-size:44px;font-weight:300">9:41</div><div style="font-size:14px;color:%s;margin-top:2px">пятница, 27 ноября</div></div>'
            '<div style="padding:28px 20px 0">%s</div><div style="display:flex;justify-content:space-between;padding:28px 28px 0">%s</div>%s'
            % (t['muted'], body, apps, C.caption(t, cap, 0)))


def widget(t, inner, h=None, extra=''):
    return ('<div style="border-radius:28px;background:%s;border:1px solid %s;padding:14px 16px;%s%s">%s</div>'
            % (t['surf'], t['line'], ('height:%dpx;box-sizing:border-box;' % h) if h else '', extra, inner))


def pill_button(t, icon, text, primary):
    st = 'background:%s;color:%s;box-shadow:inset 0 0 0 2px %s;font-weight:700' % (t['prim'], t['onp'], C.edge(t)) if primary else \
        'background:%s;color:%s' % (t['wash'], t['ink'])
    return ('<span style="flex:1;height:48px;border-radius:999px;display:flex;align-items:center;justify-content:center;gap:8px;font-size:15px;%s">%s%s</span>'
            % (st, B.svg(icon, t['onp'] if primary else t['ink'], 20, 2), text))


def wid_a(t):
    body = widget(t, '<div style="display:flex;gap:10px">%s%s</div>' % (pill_button(t, 'out', 'Расход', True), pill_button(t, 'in', 'Доход', False)), h=76,
                  extra='display:flex;align-items:center;')
    return launcher(t, body, 'А: узкий виджет 4×1 — две кнопки, «Расход» и «Доход»: нажали — сразу шторка ввода нужного вида. Больше ничего не показывает.')


def wid_b(t):
    daily = ('<div style="display:flex;justify-content:space-between;align-items:baseline"><span style="font-size:14px;color:%s">Сегодня можно</span>'
             '<span style="font-size:12px;color:%s">co-shell</span></div><div style="%sfont-size:28px;margin-top:2px">≈ 9 800 <span style="font-size:14px">ARS</span></div>'
             '<div style="margin-top:8px">%s</div>' % (t['muted'], t['muted'], C.MONO, C.meter(t, 0.25)))
    btns = '<div style="display:flex;gap:10px;margin-top:12px">%s%s</div>' % (pill_button(t, 'out', 'Расход', True), pill_button(t, 'in', 'Доход', False))
    return launcher(t, widget(t, daily + btns), 'Б: виджет 4×2 — «Сегодня можно» (как на «Обзоре») и те же две кнопки. Приятно видеть, сколько осталось, но занимает два ряда.')


def wid_c(t):
    plus = ('<span style="width:72px;height:72px;border-radius:50%%;background:%s;display:flex;align-items:center;justify-content:center;box-shadow:inset 0 0 0 2px %s">%s</span>'
            % (t['prim'], C.edge(t), B.svg('plus', t['onp'], 34, 2.2)))
    return launcher(t, '<div style="display:flex;gap:16px;align-items:center">%s<span style="font-size:14px;color:%s">значок «+»: нажали — шторка расхода</span></div>' % (plus, t['muted']),
                    'В: самый тихий — круглая «+» 1×1, как значок приложения. Нажали — расход; «Доход» остаётся в приложении. Удержание значка уже даёт расход и доход (подсказка про ярлыки).')


def qs_tile(t):
    def tile(icon, label, on=False, ours=False):
        bg = t['prim'] if ours else (t['wash'] if on else C.mix(t['page'], t['ink'], .1))
        ink = t['onp'] if ours else t['ink']
        return ('<div style="height:64px;border-radius:32px;background:%s;color:%s;display:flex;align-items:center;gap:10px;padding:0 16px;font-size:14px;%s">%s<span>%s</span></div>'
                % (bg, ink, ('box-shadow:inset 0 0 0 2px %s;font-weight:700' % C.edge(t)) if ours else '', B.svg(icon, ink, 22, 1.9), label))
    grid = ''.join(tile(i, l, on) for i, l, on in [('wifi', 'Wi-Fi', True), ('bolt', 'Экономия', False), ('lock', 'Поворот', False), ('cloud', 'Облако', True)])
    body = '<div style="padding:56px 16px 0"><div style="display:grid;grid-template-columns:1fr 1fr;gap:10px">%s%s</div></div>' % (grid, tile('out', 'Расход', ours=True))
    return body + C.caption(t, 'Плитка в шторке быстрых настроек: «Расход». Нажали — открылось приложение сразу на вводе траты. Добавляется человеком вручную, сама не появится.', 0)


def bar_shell(t, inner, h=64, b=14):
    return ('<div style="position:absolute;left:32px;right:32px;bottom:%dpx;height:%dpx;border-radius:999px;background:%s;display:flex;align-items:center;'
            'justify-content:space-between;padding:0 6px;box-sizing:border-box">%s</div>' % (b, h, t['bar'], inner))


def plus_btn(t):
    return '<span style="width:48px;height:48px;border-radius:50%%;background:%s;display:flex;align-items:center;justify-content:center">%s</span>' % (t['prim'], B.svg('plus', t['onp'], 26, 2.2))


def screen_body(t, cap):
    rows = ''.join(C.row(t, ic, col, ti, 'сегодня', am) for ic, col, ti, am in
                   [('cafe', '#FFB86B', 'Кафе', '−2 450 ARS'), ('cart', '#7FD6A0', 'Продукты', '−18 300 ARS'), ('taxi', '#FFE14D', 'Такси', '−6 500 ARS')])
    return C.topbar(t, 'Операции', back=False) + '<div style="padding-top:72px">' + C.card(t, '', '', rows) + '</div>' + cap


def bar_a(t):
    pill = ('<span style="height:44px;padding:0 16px;border-radius:999px;background:%s;color:%s;display:flex;align-items:center;gap:8px;font-size:14px;font-weight:700;'
            'box-shadow:inset 0 0 0 2px %s">%s<span>Операции</span></span>' % (t['prim'], t['onp'], C.edge(t), B.svg(B.ICON['list'], t['onp'], 22, 1.8)))
    other = lambda k: '<span style="width:44px;display:flex;justify-content:center">%s</span>' % B.svg(B.ICON[k], t['muted'], 24, 1.7)  # noqa: E731
    bar = bar_shell(t, other('home') + pill + plus_btn(t) + other('chart') + other('more'))
    return screen_body(t, bar + C.caption(t, 'А: подпись только у выбранной вкладки — пилюля раскрывается и называет экран, остальные — значки. Панель той же высоты.', 0))


def bar_b(t):
    def tab(k, label, on=False):
        ink = t['onp'] if on else t['muted']
        bg = ('background:%s;box-shadow:inset 0 0 0 2px %s;' % (t['prim'], C.edge(t))) if on else ''
        return ('<span style="width:66px;height:56px;border-radius:24px;%sdisplay:flex;flex-direction:column;align-items:center;justify-content:center;gap:2px;color:%s;font-size:10px;letter-spacing:-.2px">%s<span>%s</span></span>'
                % (bg, ink, B.svg(B.ICON[k], ink, 22, 1.7), label))
    bar = bar_shell(t, tab('home', 'Обзор') + tab('list', 'Операции', True) + plus_btn(t) + tab('chart', 'Отчёты') + tab('more', 'Ещё'), h=72)
    return screen_body(t, bar + C.caption(t, 'Б: подписи под всеми значками — ничего не надо угадывать, но панель выше (72 вместо 64), а «Операции» едва влезает: на узком экране или крупном шрифте слово придётся сокращать.', 0))


def bar_c(t):
    other = lambda k, on=False: ('<span style="width:64px;height:44px;border-radius:999px;display:flex;align-items:center;justify-content:center;%s">%s</span>'  # noqa: E731
                                 % (('background:%s;' % t['prim']) if on else '', B.svg(B.ICON[k], t['onp'] if on else t['muted'], 24, 1.7)))
    bar = bar_shell(t, other('home') + other('list', True) + plus_btn(t) + other('chart') + other('more'))
    return screen_body(t, bar + C.caption(t, 'В: как сейчас — значки без подписей (подпись слышит TalkBack). Решение 2026-11-25 «значок говорит сам» остаётся.', 0))


BOARDS = [('Wid795A', wid_a, 390, 820), ('Wid795B', wid_b, 390, 820), ('Wid795C', wid_c, 390, 820), ('Qs795', qs_tile, 390, 820),
          ('Bar796A', bar_a, 390, 820), ('Bar796B', bar_b, 390, 820), ('Bar796C', bar_c, 390, 820)]

if __name__ == '__main__':
    C.run(BOARDS, sys.argv[1])
