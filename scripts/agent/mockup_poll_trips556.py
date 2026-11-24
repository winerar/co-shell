#!/usr/bin/env python3
"""Poll boards for #556 (планирование поездок: расписание и расходы), session 2026-11-23.
Usage: python3 scripts/agent/mockup_poll_trips556.py <out-dir>  → <out-dir>/img/*.png, then poll-page.py <spec> <poll-dir> --img <out-dir>/img
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from mockup_poll_f_common import *  # noqa: E402,F401,F403
import mockup_poll_f_common as C  # noqa: E402


def page(t, title, inner, cap, active='more'):
    return C.topbar(t, title) + '<div style="padding-top:72px">' + inner + '</div>' + C.nav(t, active) + C.caption(t, cap, 0)


def group(t, rows):
    return '<div style="margin:8px 16px 0;border-radius:24px;background:%s;border:1px solid %s;padding:4px 0;overflow:hidden">%s</div>' % (t['surf'], t['line'], ''.join(rows))


def tabs(t, sel):
    return C.segmented(t, ['План', 'Расходы', 'Итог'], sel)


def day_head(t, text, sub):
    return '<div style="margin:14px 20px 0;display:flex;justify-content:space-between;font-size:13px;color:%s"><b style="letter-spacing:.04em">%s</b><span>%s</span></div>' % (t['muted'], text, sub)


def plan_a(t):
    d1 = group(t, [C.row(t, 'bus', '#7FD6A0', 'Перелёт Буэнос-Айрес → Ереван', '06:40 · билеты куплены', '640 USD'), C.row(t, 'house', '#6BB8FF', 'Заселение: Airbnb', '15:00 · 5 ночей', '360 USD')])
    d2 = group(t, [C.row(t, 'cafe', '#FFB86B', 'Обед в центре', '13:00 · план', '≈ 40 USD'), C.row(t, 'film', '#E58BD0', 'Каскад и музеи', '16:00 · план', '≈ 25 USD')])
    body = tabs(t, 0) + day_head(t, 'ПН, 5 ОКТЯБРЯ', 'день 1 · 1 000 USD') + d1 + day_head(t, 'ВТ, 6 ОКТЯБРЯ', 'день 2 · ≈ 65 USD') + d2
    return page(t, 'Армения', body, 'А: план по дням. Пункт — название, время, сумма (факт — без «≈», план — с «≈»); итог дня справа.')


def plan_b(t):
    items = [('Перелёт', 'bus', '#7FD6A0', 'куплено', '640 USD'), ('Жильё', 'house', '#6BB8FF', 'куплено', '720 USD'), ('Еда', 'cafe', '#FFB86B', 'по 50 в день', '≈ 700 USD'),
             ('Экскурсии', 'film', '#E58BD0', 'идеи: Гарни, Гегард', '≈ 200 USD'), ('Подарки', 'gift', '#9C8CFF', 'пока ничего', '≈ 100 USD')]
    body = tabs(t, 0) + C.card(t, 'Статьи расходов', 'всего ≈ 2 360 USD', '', 'padding:10px 16px') + group(t, [C.row(t, i, c, n, s, a) for n, i, c, s, a in items]) + \
        '<div style="margin:10px 20px 0;font-size:13px;color:%s">Идеи без дат: «Гарни и Гегард», «Севан», «Каскад».</div>' % t['muted']
    return page(t, 'Армения', body, 'Б: план статьями без привязки к дням; идеи — простым списком. Проще, но нет расписания.')


def fact_a(t):
    rows = [('Жильё', 'house', '#6BB8FF', '720 из 720', 1.0), ('Еда', 'cafe', '#FFB86B', '610 из 700', .87), ('Перелёт', 'bus', '#7FD6A0', '620 из 640', .97), ('Экскурсии', 'film', '#E58BD0', '0 из 200', 0), ('Подарки', 'gift', '#9C8CFF', '0 из 100', 0)]
    out = ''
    for n, i, c, s, f in rows:
        out += ('<div style="padding:8px 16px">%s<div style="margin-top:6px">%s</div></div>' % (C.row(t, i, c, n, '', s + ' USD').replace('padding:8px 16px', 'padding:0'), C.meter(t, f)))
    body = tabs(t, 2) + C.hero(t, '<div style="font-size:14px;color:%s">План 2 360 · потрачено 1 950 USD</div><div style="%sfont-size:28px;font-weight:600;margin:4px 0">Осталось 410 USD</div><div style="margin-top:8px">%s</div>' % (t['muted'], C.MONO, C.meter(t, .83))) + group(t, [out])
    return page(t, 'Армения', body, 'А: план и факт рядом — полоса по каждой статье; траты считаются сами по тегу поездки.')


def fact_b(t):
    body = tabs(t, 1) + C.card(t, 'Запланировано', 'ещё не потрачено', '', 'padding:10px 16px') + group(t, [C.row(t, 'cafe', '#FFB86B', 'Обед в центре', 'завтра · план', '≈ 40 USD'), C.row(t, 'film', '#E58BD0', 'Каскад и музеи', 'завтра · план', '≈ 25 USD')]) + \
        C.card(t, 'Уже потрачено', '12 операций', '', 'padding:10px 16px') + group(t, [C.row(t, 'bus', '#7FD6A0', 'Такси из аэропорта', 'сегодня', '−18 USD')]) + \
        '<div style="margin:10px 16px 0">%s</div>' % C.button(t, 'Записать план как трату')
    return page(t, 'Армения', body, 'Б: запланированная трата — «заготовка»; кнопка превращает её в обычную операцию с тегом поездки.')


BOARDS = [('TripPlanA', plan_a, 390, 820), ('TripPlanB', plan_b, 390, 820), ('TripFactA', fact_a, 390, 820), ('TripFactB', fact_b, 390, 820)]

if __name__ == '__main__':
    C.run(BOARDS, sys.argv[1])
