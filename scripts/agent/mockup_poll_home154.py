#!/usr/bin/env python3
"""Poll boards for #154 (customising «Обзор»), session 2026-11-27: how a person finds the on-screen editing, what the editing mode explains,
and how a hidden card comes back. The editing itself (hold a card → ⠿ and ✕, «Готово») is decided: board Set1HomeEdit.
Usage: python3 scripts/agent/mockup_poll_home154.py <out-dir>  → <out-dir>/img/*.png, then poll-page.py <spec> <poll-dir> --img <out-dir>/img
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from mockup_poll_f_common import *  # noqa: E402,F401,F403
import mockup_poll_f_common as C  # noqa: E402

CLOSE = 'M6 6l12 12M18 6L6 18'
BULB = 'M9 18h6M10 21h4M12 3a6 6 0 0 0-4 10c1 1 1 2 1 3h6c0-1 0-2 1-3a6 6 0 0 0-4-10z'
PLUS = 'M12 5v14M5 12h14'
PENCIL = 'M4 20l4-1 11-11-3-3L5 16l-1 4z'


def balance(t):
    return C.hero(t, '<div style="font-size:13px;opacity:.8">Общий баланс</div><div style="font-size:32px;font-weight:700;font-family:monospace;margin-top:4px">1 052 USD</div>')


def tiles(t):
    one = lambda a, b: ('<div style="flex:1;padding:12px 14px;border-radius:20px;background:%s;border:1px solid %s"><div style="font-size:13px;color:%s">%s</div>'
                        '<div style="font-size:20px;font-weight:700;font-family:monospace;margin-top:4px">%s</div></div>') % (t['surf'], t['line'], t['muted'], a, b)
    return '<div style="display:flex;gap:10px;margin:0 16px">%s%s</div>' % (one('Траты за ноябрь', '1 240 USD'), one('Бюджет', '68 %'))


def recent(t):
    return C.card(t, 'Последние операции', '', ''.join([C.row(t, 'cafe', '#FFB86B', 'Кафе «Моне»', 'Сегодня', '−3 500 ARS'),
                                                      C.row(t, 'cart', '#7FD6A0', 'Coto', 'Вчера', '−12 500 ARS')]))


def tip(t, text, action='Понятно'):
    return ('<div style="margin:0 16px;padding:12px 14px;border-radius:24px;background:%s;border:1px solid %s;display:flex;align-items:center;gap:12px">'
            '<span style="width:36px;height:36px;border-radius:50%%;background:%s;display:flex;align-items:center;justify-content:center;flex:none">%s</span>'
            '<div style="flex:1;font-size:14px;line-height:1.3">%s</div><span style="font-size:14px;font-weight:600;color:%s">%s</span></div>') % (
        t['surf'], t['line'], C.mix(t['surf'], t['prim'], .35), C.glyph(BULB, t['ink'], 20), text, C.ink2(t), action)


def edit_head(t, name, dim=False):
    dot = lambda s: '<span style="width:36px;height:36px;border-radius:50%%;background:%s;display:flex;align-items:center;justify-content:center;font-size:18px">%s</span>' % (t['wash'], s)
    right = dot('⠿') + dot(C.glyph(CLOSE, t['ink'], 18)) if not dim else dot(C.glyph(PLUS, t['ink'], 18))
    return '<div style="display:flex;align-items:center;gap:8px;margin:12px 20px 6px"><span style="flex:1;font-size:13px;font-weight:600;color:%s">%s</span>%s</div>' % (t['muted'], name, right)


def dashed(t, inner, faint=False):
    return '<div style="margin:0 16px;border-radius:26px;border:1.5px dashed %s;padding:4px;%s">%s</div>' % (t['line'], 'opacity:.55;' if faint else '', inner)


def done(t):
    return '<div style="position:absolute;left:16px;right:16px;bottom:24px">%s</div>' % C.button(t, 'Готово')


# ---- Q1: how a person finds out that «Обзор» can be edited -----------------------------------------------------------
def find_a(t):
    return C.topbar(t, 'Обзор', back=False) + '<div style="padding-top:72px">' + tip(t, 'Удерживайте карточку, чтобы переставить или скрыть её.') + \
        '<div style="height:12px"></div>' + tiles(t) + recent(t) + '</div>' + C.nav(t, 'home') + \
        C.caption(t, 'А: подсказка-жест (как «Удерживайте операцию…»): карточкой вверху «Обзора», один раз, «Понятно» — навсегда. Выключается вместе с подсказками.', 0)


def find_b(t):
    pen = '<span style="width:48px;height:48px;display:flex;align-items:center;justify-content:center">%s</span>' % C.glyph(PENCIL, t['ink'], 22)
    return C.topbar(t, 'Обзор', back=False, right=pen) + '<div style="padding-top:72px">' + balance(t) + '<div style="height:12px"></div>' + tiles(t) + recent(t) + '</div>' + C.nav(t, 'home') + \
        C.caption(t, 'Б: значок карандаша в шапке «Обзора» — всегда на виду, нажатие включает правку. Удержание карточки остаётся тоже. Шапка получает ещё одну кнопку.', 0)


def find_c(t):
    return C.topbar(t, 'Обзор', back=False) + '<div style="padding-top:72px">' + balance(t) + '<div style="height:12px"></div>' + tiles(t) + recent(t) + \
        '<div style="margin:12px 16px;text-align:center;font-size:14px;font-weight:600;color:%s">Настроить «Обзор»</div></div>' % C.ink2(t) + C.nav(t, 'home') + \
        C.caption(t, 'В: ссылка «Настроить «Обзор»» под последней карточкой. Не мешает, но её видят только те, кто долистал.', 0)


# ---- Q2: what the editing mode says ------------------------------------------------------------------------------------
def mode_a(t):
    hint = '<div style="margin:8px 20px 0;font-size:14px;color:%s;line-height:1.35">Тяните ⠿, чтобы переставить. ✕ — скрыть. Баланс остаётся сверху.</div>' % t['muted']
    return C.topbar(t, 'Обзор', back=False) + '<div style="padding-top:72px">' + hint + edit_head(t, 'Плитки') + dashed(t, tiles(t).replace('margin:0 16px', 'margin:0')) + \
        edit_head(t, 'Последние операции') + dashed(t, recent(t).replace('margin:', 'margin:0;margin-x:')) + '</div>' + done(t) + \
        C.caption(t, 'А: одна строка-пояснение над карточками. Исчезает вместе с режимом правки.', 0)


def mode_b(t):
    return C.topbar(t, 'Обзор', back=False) + '<div style="padding-top:72px">' + edit_head(t, 'Плитки') + dashed(t, tiles(t).replace('margin:0 16px', 'margin:0')) + \
        edit_head(t, 'Последние операции') + dashed(t, recent(t).replace('margin:', 'margin:0;margin-x:')) + '</div>' + done(t) + \
        C.caption(t, 'Б: как сейчас, без текста: подписи, ⠿ и ✕ говорят сами (правило «значок говорит сам»). Пояснение — только в подсказке из вопроса 1.', 0)


# ---- Q3: how a hidden card comes back -----------------------------------------------------------------------------------
def back_a(t):
    bar = ('<div style="position:absolute;left:16px;right:16px;bottom:88px;padding:12px 16px;border-radius:24px;background:%s;border:1px solid %s;display:flex;align-items:center">'
           '<span style="flex:1;font-size:15px">Скрыто: 2</span><span style="font-size:14px;font-weight:600;color:%s">Вернуть</span></div>') % (t['surf'], t['line'], C.ink2(t))
    return C.topbar(t, 'Обзор', back=False) + '<div style="padding-top:72px">' + edit_head(t, 'Плитки') + dashed(t, tiles(t).replace('margin:0 16px', 'margin:0')) + '</div>' + bar + done(t) + \
        C.caption(t, 'А: плашка над «Готово»: «Скрыто: 2 · Вернуть». Нажатие — шторка со списком скрытых, у каждой «Показать». Нет скрытых — плашки нет.', 0)


def back_b(t):
    ghost = lambda name: ('<div style="margin:6px 16px 0;padding:12px 16px;border-radius:24px;border:1.5px dashed %s;display:flex;align-items:center;gap:8px;color:%s">'
                          '<span style="flex:1;font-size:14px">%s · скрыта</span>%s</div>') % (t['line'], t['muted'], name, C.glyph(PLUS, t['ink'], 20))
    return C.topbar(t, 'Обзор', back=False) + '<div style="padding-top:72px">' + edit_head(t, 'Плитки') + dashed(t, tiles(t).replace('margin:0 16px', 'margin:0')) + \
        ghost('Конвертер') + ghost('Последние операции') + '</div>' + done(t) + \
        C.caption(t, 'Б: скрытые остаются в списке — тихой пунктирной строкой с «+» на своём месте. Нажатие возвращает карточку. Всё видно сразу, без шторки.', 0)


def empty_common(t):
    return C.topbar(t, 'Обзор', back=False) + '<div style="padding-top:72px">' + edit_head(t, 'Скоро', dim=True).replace('⠿', '⠿') + \
        dashed(t, '<div style="padding:18px 16px;font-size:14px;color:%s">Регулярных платежей пока нет — карточка покажется, когда они появятся.</div>' % t['muted'], faint=True) + \
        edit_head(t, 'Плитки') + dashed(t, tiles(t).replace('margin:0 16px', 'margin:0')) + '</div>' + done(t) + \
        C.caption(t, 'Для всех вариантов: пустые карточки («Скоро», «Итог месяца», «Горит в бюджете») в режиме правки видны приглушённо — их можно переставить и скрыть заранее.', 0)


BOARDS = [('Home154FindA', find_a, 390, 820), ('Home154FindB', find_b, 390, 820), ('Home154FindC', find_c, 390, 820),
          ('Home154ModeA', mode_a, 390, 820), ('Home154ModeB', mode_b, 390, 820),
          ('Home154BackA', back_a, 390, 820), ('Home154BackB', back_b, 390, 820), ('Home154Empty', empty_common, 390, 820)]

if __name__ == '__main__':
    C.run(BOARDS, sys.argv[1])
