#!/usr/bin/env python3
"""Poll boards for #558 (личные счета), #556 (поездки), #557 (фото к операции), #606 (автокопия), session 2026-11-23.
Usage: python3 scripts/agent/mockup_poll_f2.py <out-dir>  → <out-dir>/img/*.png, then poll-page.py <spec> <poll-dir> --img <out-dir>/img
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from mockup_poll_f_common import *  # noqa: E402,F401,F403
import mockup_poll_f_common as C  # noqa: E402


def group(t, rows):
    return '<div style="margin:8px 16px 0;border-radius:24px;background:%s;border:1px solid %s;padding:4px 0;overflow:hidden">%s</div>' % (t['surf'], t['line'], ''.join(rows))


def page(t, title, inner, cap, active='more', back=True):
    return C.topbar(t, title, back=back) + '<div style="padding-top:72px">' + inner + '</div>' + C.nav(t, active) + C.caption(t, cap, 0)


def lock(t, size=14):
    return B.svg(B.ICON['lock'], t['muted'], size, 1.8)


def toggle(t, label, sub, on):
    knob = ('<span style="width:52px;height:32px;border-radius:999px;background:%s;box-shadow:inset 0 0 0 1px %s;position:relative;flex:none">'
            '<span style="position:absolute;top:4px;%s:4px;width:24px;height:24px;border-radius:50%%;background:%s"></span></span>' % (
                t['prim'] if on else t['wash'], C.edge(t), 'right' if on else 'left', t['onp'] if on else t['muted']))
    return ('<div style="display:flex;align-items:center;gap:12px;padding:10px 16px;min-height:56px"><div style="flex:1"><div style="font-size:16px">%s</div>'
            '<div style="font-size:13px;color:%s">%s</div></div>%s</div>' % (label, t['muted'], sub, knob))


def tile(t, label, value):
    return ('<div style="display:flex;justify-content:space-between;align-items:center;height:48px;padding:0 16px;border-radius:999px;background:%s;margin:8px 16px 0;font-size:15px">'
            '<span style="color:%s">%s</span><span>%s</span></div>' % (t['wash'], t['muted'], label, value))


# ---------- #558 personal accounts
def acc_row(t, name, sub, amt, color, mine=False):
    ic = C.badge(t, 'wallet', color, 40, False)
    tag = (' <span style="color:%s">%s</span>' % (t['muted'], lock(t))) if mine else ''
    return ('<div style="display:flex;align-items:center;gap:12px;padding:8px 16px;min-height:60px">%s<div style="flex:1"><div style="font-size:16px">%s%s</div>'
            '<div style="font-size:13px;color:%s">%s</div></div><div style="%sfont-size:15px">%s</div></div>' % (ic, name, tag, t['muted'], sub, C.MONO, amt))


def priv_a(t):
    rows = [acc_row(t, 'Карта Galicia', 'общий', '412 300 ARS', '#FF8A3D'), acc_row(t, 'USDT-кошелёк', 'общий', '1 250 USDT', '#26A17B'),
            acc_row(t, 'Моя копилка', 'только у меня', '86 000 ARS', '#9C8CFF', True), acc_row(t, 'Наличные', 'общий', '18 000 ARS', '#6BB8FF')]
    body = C.card(t, 'Счета', 'замок — виден только вам', '', 'padding:10px 16px') + group(t, rows)
    return page(t, 'Счета', body, 'А: пометка у счёта. Замок и подпись «только у меня» в списке; второй участник счёта не видит и не получает синком.')


def priv_b(t):
    rows = [acc_row(t, 'Карта Galicia', '', '412 300 ARS', '#FF8A3D'), acc_row(t, 'USDT-кошелёк', '', '1 250 USDT', '#26A17B'), acc_row(t, 'Наличные', '', '18 000 ARS', '#6BB8FF')]
    mine = [acc_row(t, 'Моя копилка', '', '86 000 ARS', '#9C8CFF', True)]
    body = (C.card(t, 'Общие', 'видят оба', '', 'padding:10px 16px') + group(t, rows) +
            C.card(t, 'Только мои', 'остаются на этом телефоне', '', 'padding:10px 16px') + group(t, mine))
    return page(t, 'Счета', body, 'Б: два раздела — «Общие» и «Только мои». Личные хранятся только на телефоне (не синк, но в резервной копии).')


def priv_edit(t):
    body = (C.card(t, 'Моя копилка', '', '') + toggle(t, 'Только у меня', 'Второй участник не увидит счёт и операции по нему', True).replace('<div style="display', '<div style="margin:0 8px;display', 1) +
            C.card(t, '', '', '<div style="font-size:14px;color:%s">Перевод с общего счёта на личный второй увидит как «Перевод на личный счёт» без названия.</div>' % t['muted']))
    return page(t, 'Счёт', body, 'Общее: переключатель в редакторе счёта и пояснение про переводы.', 'more')


# ---------- #556 trips
def trip_a(t):
    rows = [C.row(t, 'cafe', '#FFB86B', 'Кафе и рестораны', '38 операций', '610 USD'), C.row(t, 'house', '#6BB8FF', 'Жильё', '2 операции', '720 USD'),
            C.row(t, 'bus', '#7FD6A0', 'Транспорт', '14 операций', '260 USD'), C.row(t, 'gift', '#E58BD0', 'Покупки', '9 операций', '180 USD')]
    hero = C.hero(t, '<div style="font-size:14px;color:%s">Армения · 5–19 октября · 2 человека</div><div style="%sfont-size:32px;font-weight:600;margin:4px 0">1 770 USD</div>'
                  '<div style="font-size:13px">126 USD в день · 885 USD на человека</div><div style="margin-top:10px">%s</div><div style="font-size:12px;margin-top:4px">Бюджет 2 000 USD · осталось 230</div>' % (
                      t['muted'], C.MONO, C.meter(t, .885)))
    return page(t, 'Поездка', hero + group(t, rows), 'А: поездка — отдельный экран «Ещё → Деньги → Поездки»: итог, в день, на человека, разбивка. Траты за даты получают тег сами.')


def trip_b(t):
    chip = '<div style="margin:12px 16px 0;padding:12px 16px;border-radius:24px;background:%s;border:1px solid %s;display:flex;align-items:center;gap:12px">%s<div style="flex:1"><div style="font-size:16px;font-weight:600">Армения</div>' \
           '<div style="font-size:13px;color:%s">5–19 октября · 1 770 из 2 000 USD</div></div><span style="color:%s">›</span></div>' % (t['surf'], t['line'], C.badge(t, 'bus', '#7FD6A0'), t['muted'], t['muted'])
    ov = C.card(t, 'Сейчас', 'идёт поездка', '', 'padding:10px 16px') + chip + C.card(t, 'Месяц', 'октябрь', '<div style="font-size:14px;color:%s">Остальные плитки «Обзора»…</div>' % t['muted'])
    return page(t, 'Обзор', ov, 'Б: поездка — карточка на «Обзоре» на время дат, итог открывается из неё; отдельного раздела в «Ещё» нет, прошлые — в «Отчётах».', 'home', False)


def trip_new(t):
    body = (tile(t, 'Название', 'Армения') + tile(t, 'Даты', '5 окт — 19 окт') + tile(t, 'Людей', '2') + tile(t, 'Бюджет', '2 000 USD') +
            '<div style="margin:12px 16px 0">%s</div>' % C.button(t, 'Сохранить') +
            '<div style="margin:10px 16px 0;font-size:13px;color:%s">Траты за эти дни получат тег «Армения». Тег можно снять с операции.</div>' % t['muted'])
    return page(t, 'Новая поездка', body, 'Общее: форма поездки — четыре поля, бюджет и людей можно пропустить.')


# ---------- #557 photos
def photo_box(t, i, big=False, add=False):
    sz = 96 if not big else 140
    if add:
        return ('<span style="width:%dpx;height:%dpx;border-radius:16px;border:2px dashed %s;display:inline-flex;align-items:center;justify-content:center;color:%s;font-size:28px">+</span>' % (sz, sz, t['muted'], t['muted']))
    cols = ['#6BB8FF', '#FFB86B', '#7FD6A0']
    return ('<span style="width:%dpx;height:%dpx;border-radius:16px;background:%s;display:inline-flex;align-items:center;justify-content:center;box-shadow:inset 0 0 0 1px %s">%s</span>' % (
        sz, sz, C.mix(t['page'], cols[i % 3], .35), t['line'], B.svg(B.ICON['file'], t['ink'], 30, 1.6)))


def photo_a(t):
    sec = C.card(t, 'Фото', '2 из 3', '<div style="display:flex;gap:8px;margin-top:10px">%s%s%s</div><div style="font-size:13px;color:%s;margin-top:8px">Только на этом телефоне и в резервной копии: на второй телефон не передаётся.</div>' % (
        photo_box(t, 0), photo_box(t, 1), photo_box(t, 0, add=True), t['muted']))
    top = tile(t, 'Сумма', '3 500 ARS') + tile(t, 'Категория', 'Техника')
    return page(t, 'Операция', top + sec, 'А: карточка «Фото» в шторке операции — миниатюры, «+» добавляет (камера или галерея), до 3.', 'list')


def photo_b(t):
    top = tile(t, 'Сумма', '3 500 ARS') + tile(t, 'Категория', 'Техника') + tile(t, 'Фото', '2 · гарантия')
    row = ('<div style="margin:8px 16px 0;border-radius:24px;background:%s;border:1px solid %s;padding:6px 0">%s</div>' % (
        t['surf'], t['line'], C.row(t, 'film', '#6BB8FF', 'Холодильник', 'Техника · с фото', '−3 500 ARS')))
    return page(t, 'Операции', top.replace('Операция', '') + row, 'Б: фото — строка-плитка «Фото» в «Деталях» (экономит место); в списке у операции значок скрепки.', 'list')


# ---------- #606 autocopy
def copy_a(t):
    seg = C.segmented(t, ['Каждый день', 'Раз в неделю', 'Раз в месяц'], 1)
    days = '<div style="display:flex;gap:6px;margin:12px 16px 0;justify-content:space-between">%s</div>' % ''.join(
        '<span style="width:44px;height:44px;border-radius:50%%;display:inline-flex;align-items:center;justify-content:center;font-size:14px;%s">%s</span>' % (
            ('background:%s;color:%s;font-weight:700;box-shadow:inset 0 0 0 2px %s' % (t['prim'], t['onp'], C.edge(t))) if d == 'Вс' else 'background:%s' % t['wash'], d)
        for d in ['Пн', 'Вт', 'Ср', 'Чт', 'Пт', 'Сб', 'Вс'])
    body = (C.card(t, 'Автокопия', 'в папку на телефоне', '') + seg + days + tile(t, 'Время', '03:00') + tile(t, 'Хранить', 'последние 5 копий') +
            '<div style="margin:10px 16px 0;font-size:13px;color:%s">Следующая: воскресенье, 03:00</div>' % t['muted'])
    return page(t, 'Резервная копия', body, 'А: три сегмента — день / неделя / месяц; для недели дни, для месяца — число (сетка 1–31).')


def copy_b(t):
    body = (C.card(t, 'Автокопия', '', '') + tile(t, 'Как часто', 'Раз в неделю ›') + tile(t, 'День', 'Воскресенье ›') + tile(t, 'Время', '03:00 ›') + tile(t, 'Хранить', '5 копий ›') +
            '<div style="margin:10px 16px 0;font-size:13px;color:%s">Каждая строка открывает маленькую шторку выбора.</div>' % t['muted'])
    return page(t, 'Резервная копия', body, 'Б: строки-плитки, каждая открывает шторку выбора (как «Валюты итогов»); экран не меняется при смене периода.')


BOARDS = [('PrivA', priv_a, 390, 820), ('PrivB', priv_b, 390, 820), ('PrivEdit', priv_edit, 390, 700),
          ('TripA', trip_a, 390, 820), ('TripB', trip_b, 390, 760), ('TripNew', trip_new, 390, 700),
          ('PhotoA', photo_a, 390, 760), ('PhotoB', photo_b, 390, 760),
          ('CopyA', copy_a, 390, 760), ('CopyB', copy_b, 390, 760)]

if __name__ == '__main__':
    C.run(BOARDS, sys.argv[1])
