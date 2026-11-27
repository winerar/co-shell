#!/usr/bin/env python3
"""Poll boards for #769: «Сколько можно тратить в день» (safe-to-spend) until the end of the month.
Usage: python3 scripts/agent/mockup_poll_daily769.py <out-dir>  → <out-dir>/img/*.png, then poll-page.py <spec> <poll-dir> --img <out-dir>/img
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from mockup_poll_f_common import *  # noqa: E402,F401,F403
import mockup_poll_f_common as C  # noqa: E402

B = C.B


def balance(t):
    return C.hero(t, '<div style="font-size:13px;color:%s">Всего на счетах</div><div style="%sfont-size:34px;margin-top:4px">1 245 300 <span style="font-size:16px">ARS</span></div>'
                     '<div style="font-size:13px;color:%s;margin-top:4px">≈ 1 020 USD</div>' % (t['muted'], C.MONO, t['muted']))


def daily_card(t, amount='9 800', sub='до конца месяца 5 дней · осталось 49 000 ARS', today=(0.25, 'сегодня уже 2 450 ARS'), tone=''):
    big = ('<div style="display:flex;align-items:baseline;justify-content:space-between"><span style="font-size:14px;color:%s">Сегодня можно</span>'
           '<span style="font-size:13px;color:%s">›</span></div><div style="%sfont-size:28px;margin-top:2px;%s">≈ %s <span style="font-size:14px">ARS</span></div>'
           '<div style="font-size:13px;color:%s;margin-top:2px">%s</div>' % (t['muted'], t['muted'], C.MONO, tone, amount, t['muted'], sub))
    bar = ('<div style="margin-top:10px">%s</div><div style="font-size:12px;color:%s;margin-top:4px">%s</div>' % (C.meter(t, today[0]), t['muted'], today[1])) if today else ''
    return C.card(t, '', '', big + bar)


def recent(t):
    rows = [C.row(t, 'cafe', '#FFB86B', 'Кафе', 'сегодня', '−2 450 ARS'), C.row(t, 'cart', '#7FD6A0', 'Продукты', 'вчера', '−18 300 ARS')]
    return C.card(t, 'Последние операции', 'Все ›', ''.join(r.replace('padding:8px 16px', 'padding:8px 0') for r in rows))


def home(t, body, cap, top=700):
    return C.topbar(t, 'Обзор', back=False) + '<div style="padding-top:72px">' + balance(t) + body + '</div>' + C.nav(t, 'home') + C.caption(t, cap, top)


def card_a(t):
    return home(t, daily_card(t) + recent(t), 'А: своя карточка под балансом: цифра на сегодня, сколько дней и денег осталось, полоска — сколько уже потрачено сегодня. Нажатие — «Как посчитано».')


def tile_b(t):
    tile = lambda lab, val, sub: ('<div style="flex:1;border-radius:20px;background:%s;border:1px solid %s;padding:12px 14px"><div style="font-size:13px;color:%s">%s</div>'  # noqa: E731
                                  '<div style="%sfont-size:20px;margin-top:4px">%s</div><div style="font-size:12px;color:%s;margin-top:2px">%s</div></div>' % (t['surf'], t['line'], t['muted'], lab, C.MONO, val, t['muted'], sub))
    tiles = '<div style="display:flex;gap:8px;margin:12px 16px 0">%s%s</div>' % (tile('Траты за ноябрь', '612 400 ARS', 'на 8 % меньше'), tile('Можно в день', '≈ 9 800 ARS', 'ещё 5 дней'))
    return home(t, tiles + recent(t), 'Б: вторая плитка «Можно в день» вместо «Бюджета» (выбирается в «Настройки → Экраны → Обзор»). Компактно, но без «сегодня уже».')


def why(t):
    line = lambda a, b, strong=False: ('<div style="display:flex;justify-content:space-between;font-size:15px;margin-top:10px;%s"><span>%s</span><span style="%s">%s</span></div>'  # noqa: E731
                                       % ('font-weight:700' if strong else '', a, C.MONO, b))
    body = ('<div style="font-size:14px;color:%s;margin-top:6px">Ноябрь, по курсу на сегодня</div>' % t['muted'] + line('Лимиты на месяц', '600 000 ARS') + line('Уже потрачено', '− 451 000 ARS')
            + line('Впереди регулярные', '− 65 000 ARS') + line('Отложить на цели', '− 35 000 ARS') + '<div style="border-top:1px solid %s;margin-top:10px"></div>' % t['line']
            + line('Осталось', '49 000 ARS', True) + line('Дней до конца месяца', '5') + line('Можно в день', '≈ 9 800 ARS', True)
            + '<div style="font-size:13px;color:%s;margin-top:12px;line-height:1.4">Это ориентир, а не запрет. Цифра пересчитывается после каждой операции.</div>' % t['muted'])
    page = C.topbar(t, 'Обзор', back=False) + '<div style="padding-top:72px">' + balance(t) + '</div>'
    return page + C.sheet(t, 'Как посчитано', body, 200) + C.caption(t, 'Нажатие на карточку — шторка «Как посчитано»: откуда цифра. Строки без данных (нет регулярных, нет целей) не показываются.', 60)


def no_limits(t):
    sub = 'по доходу за ноябрь: пришло 900 000, потрачено 612 400'
    return home(t, daily_card(t, '15 300', sub) + recent(t), 'Без лимитов — от дохода месяца: пришло минус потрачено, минус регулярные и цели, делённое на дни. Так цифра есть у всех.', 700)


def no_limits_a(t):
    hint = C.card(t, '', '', '<div style="font-size:14px;line-height:1.4">Задайте лимиты в «Бюджетах» — и здесь появится, сколько можно тратить в день.</div><div style="margin-top:8px;font-size:14px;font-weight:700;color:%s">Открыть «Бюджеты»</div>' % C.ink2(t))
    return home(t, hint + recent(t), 'Только по лимитам: без лимитов вместо цифры — приглашение задать их.', 700)


def over(t):
    return home(t, daily_card(t, '0', 'план месяца потрачен · сверх него 12 000 ARS', None, 'color:%s' % t['muted']) + recent(t),
                'План месяца потрачен: «0», без красного; сверх плана — спокойной строкой. С 1-го числа цифра начинается заново.')


BOARDS = [('DailyCardA', card_a, 390, 820), ('DailyTileB', tile_b, 390, 820), ('DailyWhy', why, 390, 820),
          ('DailyIncome', no_limits, 390, 820), ('DailyNoLimits', no_limits_a, 390, 820), ('DailyOver', over, 390, 820)]

if __name__ == '__main__':
    C.run(BOARDS, sys.argv[1])
