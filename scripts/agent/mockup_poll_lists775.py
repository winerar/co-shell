#!/usr/bin/env python3
"""Poll boards for #775 (examples of normal and compact lists in «Внешний вид») and #777 («Другое» in «Уведомления»).
Usage: python3 scripts/agent/mockup_poll_lists775.py <out-dir>  → <out-dir>/img/*.png, then poll-page.py <spec> <poll-dir> --img <out-dir>/img
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from mockup_poll_f_common import *  # noqa: E402,F401,F403
import mockup_poll_f_common as C  # noqa: E402

B = C.B
# (icon, colour, title, account colour, subtitle, amount, kind)
OPS = [('cart', '#7FD6A0', 'Продукты', '#F26E22', 'Galicia · Coto', '−32 000 ARS', 'e'),
       ('cafe', '#FFB86B', 'Кафе', '#26A17B', 'USDT-карта', '−13,10 USDT', 'e'),
       ('salary', '#6BB8FF', 'Зарплата', '#B01E2F', 'Ameria', '+1 200 USD', 'i'),
       ('bus', '#E58BD0', 'Транспорт', '#00B1EA', 'Mercado Pago · SUBE', '−2 500 ARS', 'e')]


def amount_color(t, kind):
    return t['acc'] if kind == 'i' else ('#FF7A90' if t['dark'] else '#C2264B')


def op(t, o, compact, size=40, font=16):
    icon, color, title, acc, sub, amt, kind = o
    pad, min_h = (4, 44) if compact else (12, 64)
    mini = '<span style="width:12px;height:12px;border-radius:4px;background:%s;flex:none"></span>' % acc
    return ('<div style="display:flex;align-items:center;gap:12px;padding:%dpx 14px;min-height:%dpx;box-sizing:border-box">%s<div style="flex:1;min-width:0">'
            '<div style="font-size:%dpx;white-space:nowrap;overflow:hidden;text-overflow:ellipsis">%s</div><div style="display:flex;align-items:center;gap:4px;font-size:%dpx;color:%s;'
            'white-space:nowrap;overflow:hidden">%s<span style="overflow:hidden;text-overflow:ellipsis">%s</span></div></div><div style="%sfont-size:%dpx;color:%s;white-space:nowrap">%s</div></div>' % (
                pad, min_h, C.badge(t, icon, color, size), font, title, font - 3, t['muted'], mini, sub, C.MONO, font - 1, amount_color(t, kind), amt))


def ops_card(t, compact, n=4, extra=''):
    sep = '<div style="height:1px;background:%s;margin:0 14px"></div>' % C.mix(t['line'], t['page'], .45)
    return ('<div style="margin:12px 16px 0;border-radius:24px;background:%s;border:1px solid %s;overflow:hidden;%s">%s</div>' % (
        t['surf'], t['line'], extra, sep.join(op(t, o, compact) for o in OPS[:n])))


def label(t, text, top=18):
    return '<div style="margin:%dpx 20px 0;font-size:13px;font-weight:600;color:%s">%s</div>' % (top, t['muted'], text)


def note(t, text):
    return '<div style="margin:8px 20px 0;font-size:13px;line-height:1.35;color:%s">%s</div>' % (t['muted'], text)


def look_top(t):
    row = ('<div style="margin:8px 16px 0;padding:8px 16px;border-radius:24px;background:%s;border:1px solid %s;display:flex;align-items:center;gap:12px;min-height:52px">%s'
           '<div style="flex:1"><div style="font-size:16px">Палитра</div><div style="font-size:13px;color:%s">Лайм</div></div>%s</div>' % (
               t['surf'], t['line'], C.badge(t, 'spark', '#B8FF2E', 32), t['muted'], C.glyph(C.CHEV_R, t['muted'], 20)))
    return C.topbar(t, 'Внешний вид') + '<div style="padding-top:64px">' + label(t, 'Тема', 8) + C.segmented(t, ['Система', 'Тёмная', 'Светлая'], 1) + label(t, 'Палитра') + row


def look_a(t, sel):
    body = (label(t, 'Списки') + C.segmented(t, ['Обычная', 'Компактная'], sel)
            + ops_card(t, sel == 1, extra='') + note(t, 'Так будут выглядеть строки в «Операциях» и «Счетах».'))
    cap = ('А: переключатель как сейчас, под ним — пример строк. Нажали «Компактная» — пример сразу сжимается (плавно). '
           'Строка — та же, что в «Операциях».')
    return look_top(t) + body + C.caption(t, cap, 0)


def look_a1(t):
    return look_a(t, 0)


def look_a2(t):
    return look_a(t, 1)


def sample(t, title, compact, on):
    ring = ('box-shadow:0 0 0 2px %s;' % C.edge(t)) if on else ''
    rows = ''.join(op(t, o, compact, size=28 if compact else 32, font=13).replace('padding:%dpx 14px' % (4 if compact else 12), 'padding:%dpx 8px' % (2 if compact else 7))
                   .replace('gap:12px', 'gap:8px') for o in OPS[:3])
    check = ('<span style="width:22px;height:22px;border-radius:50%%;background:%s;display:inline-flex;align-items:center;justify-content:center">%s</span>' % (
        t['prim'], B.svg('check', t['onp'], 14, 2.6))) if on else '<span style="width:22px;height:22px;border-radius:50%%;box-shadow:inset 0 0 0 1.5px %s"></span>' % t['muted']
    return ('<div style="flex:1;min-width:0"><div style="border-radius:20px;background:%s;border:1px solid %s;overflow:hidden;padding:4px 0;%s">%s</div>'
            '<div style="display:flex;align-items:center;justify-content:center;gap:8px;margin-top:8px;font-size:14px;%s">%s%s</div></div>' % (
                t['surf'], t['line'], ring, rows, 'font-weight:700' if on else '', check, title))


def look_b(t):
    body = (label(t, 'Списки') + '<div style="display:flex;gap:12px;margin:12px 16px 0">%s%s</div>' % (sample(t, 'Обычная', False, True), sample(t, 'Компактная', True, False))
            + note(t, 'Строки в «Операциях» и «Счетах».'))
    cap = 'Б: вместо переключателя — два образца рядом, нажимаете на нужный (как выбор палитры). Видно сразу оба, но строки мелкие — это уменьшенная копия.'
    return look_top(t) + body + C.caption(t, cap, 0)


def ops_page(t, compact, cap):
    day = '<div style="display:flex;justify-content:space-between;margin:14px 20px 4px;font-size:13px;font-weight:600;color:%s"><span>СЕГОДНЯ</span><span style="%s">−34 500 ARS</span></div>' % (
        C.ink2(t), C.MONO)
    rows = OPS * 3
    sep = '<div style="height:1px;background:%s;margin:0 14px"></div>' % C.mix(t['line'], t['page'], .45)
    lst = sep.join(op(t, o, compact) for o in rows)
    return C.topbar(t, 'Операции', back=False) + '<div style="padding-top:64px">' + day + lst + '</div>' + C.nav(t, 'list') + C.caption(t, cap, 0)


def ops_normal(t):
    return ops_page(t, False, 'Для сравнения: «Операции», обычная плотность — 10 строк на экране.')


def ops_compact(t):
    return ops_page(t, True, 'Компактная — 13–14 строк: те же данные, меньше воздуха между строками.')


# ---- #777: «Другое» in «Уведомления» ----------------------------------------------------------------------------------

def pills_card(t, title, labels, sel, other=None):
    pills = ''.join(C.pill(t, s, i == sel) for i, s in enumerate(labels))
    if other is not None:
        pills += C.pill(t, other or 'Другое…', other not in (None, ''), extra='' if other else 'box-shadow:inset 0 0 0 1.5px %s;background:transparent' % t['line'])
    return ('<div style="margin:12px 16px 0;padding:12px 16px;border-radius:24px;background:%s;border:1px solid %s"><div style="font-size:16px">%s</div>'
            '<div style="display:flex;flex-wrap:wrap;gap:8px;margin-top:10px">%s</div></div>' % (t['surf'], t['line'], title, pills))


def stepper(t, value, sub='', big=True):
    minus = ('<span style="width:48px;height:48px;border-radius:50%%;background:%s;display:inline-flex;align-items:center;justify-content:center;flex:none;font-size:26px;color:%s">−</span>' % (t['wash'], t['ink']))
    plus = ('<span style="width:48px;height:48px;border-radius:50%%;background:%s;display:inline-flex;align-items:center;justify-content:center;flex:none;font-size:26px;color:%s">+</span>' % (t['wash'], t['ink']))
    s = '<div style="font-size:12px;color:%s;text-align:center">%s</div>' % (t['muted'], sub) if sub else ''
    return ('<div style="display:flex;align-items:center;gap:12px;margin-top:10px">%s<div style="flex:1;text-align:center"><div style="%sfont-size:%dpx;font-weight:600">%s</div>%s</div>%s</div>' % (
        minus, C.MONO if any(ch.isdigit() for ch in value[:2]) else '', 28 if big else 20, value, s, plus))


def stepper_card(t, title, value, sub=''):
    return ('<div style="margin:12px 16px 0;padding:12px 16px;border-radius:24px;background:%s;border:1px solid %s"><div style="font-size:16px">%s</div>%s</div>' % (
        t['surf'], t['line'], title, stepper(t, value, sub, big=False)))


def notices_top(t):
    sw = ('<span style="width:52px;height:32px;border-radius:999px;background:%s;box-shadow:inset 0 0 0 1px %s;display:inline-flex;align-items:center;justify-content:flex-end;padding:0 4px;box-sizing:border-box">'
          '<span style="width:24px;height:24px;border-radius:50%%;background:%s"></span></span>' % (t['prim'], C.edge(t), t['onp']))
    row = ('<div style="margin:8px 16px 0;padding:8px 16px;border-radius:24px;background:%s;border:1px solid %s;display:flex;align-items:center;gap:12px;min-height:60px">%s'
           '<div style="flex:1"><div style="font-size:16px">Напоминания о платежах</div><div style="font-size:13px;color:%s">В день платежа</div></div>%s</div>' % (
               t['surf'], t['line'], C.badge(t, 'repeat' if 'repeat' in B.ICON else 'calendar', '#C79BFF', 36), t['muted'], sw))
    return C.topbar(t, 'Уведомления') + '<div style="padding-top:64px">' + label(t, 'Платежи', 8) + row


def notices_a(t):
    body = (pills_card(t, 'Время напоминания о платежах', ['8:00', '9:00', '12:00', '18:00'], 1, '')
            + pills_card(t, 'Предупреждать', ['В тот же день', 'За 1 день', 'За 3 дня'], -1, 'За 5 дней')
            + label(t, 'Траты') + pills_card(t, 'Когда напоминать', ['19:00', '20:00', '21:00', '22:00'], 2, ''))
    cap = 'А: частые значения — кнопками, последняя «Другое…» открывает шторку со счётчиком. Своё значение встаёт на место «Другое» (здесь «За 5 дней»).'
    return notices_top(t) + body + C.caption(t, cap, 0)


def notices_a_sheet(t):
    body = (stepper(t, 'За 5 дней', 'до платежа') + '<div style="display:flex;flex-wrap:wrap;gap:8px;margin-top:16px;justify-content:center">%s</div>' % (
        ''.join(C.pill(t, s) for s in ['В тот же день', 'За 1 день', 'За 3 дня', 'За неделю']))
            + '<div style="margin-top:20px">%s</div>' % C.button(t, 'Готово'))
    page = notices_top(t) + pills_card(t, 'Время напоминания о платежах', ['8:00', '9:00', '12:00', '18:00'], 1, '')
    return page + C.sheet(t, 'Предупреждать', body, 330) + C.caption(t, 'А: шторка «Другое…»: счётчик от «В тот же день» до 30 дней; удерживайте «+», чтобы листать быстрее.', 0)


def notices_a_time(t):
    body = (stepper(t, '7:30', 'шаг 15 минут') + '<div style="display:flex;flex-wrap:wrap;gap:8px;margin-top:16px;justify-content:center">%s</div>' % (
        ''.join(C.pill(t, s, s == '7:30') for s in ['7:00', '7:30', '10:00', '20:00']))
            + '<div style="margin-top:20px">%s</div>' % C.button(t, 'Готово'))
    page = notices_top(t) + pills_card(t, 'Время напоминания о платежах', ['8:00', '9:00', '12:00', '18:00'], -1, '7:30')
    return page + C.sheet(t, 'Время напоминания', body, 330) + C.caption(t, 'А: время «Другое…» — тот же счётчик, шаг 15 минут.', 0)


def notices_b(t):
    body = (stepper_card(t, 'Время напоминания о платежах', '9:00', 'шаг 15 минут')
            + pills_card(t, 'Предупреждать', ['В тот же день', 'За 1 день', 'За 3 дня'], -1, 'За 5 дней')
            + label(t, 'Траты') + stepper_card(t, 'Когда напоминать', '21:00', 'шаг 15 минут'))
    cap = 'Б: время — счётчик прямо в карточке (−/+, удержание листает быстрее), дни — кнопки и «Другое…» со счётчиком, как в А.'
    return notices_top(t) + body + C.caption(t, cap, 0)


def notices_c(t):
    body = (stepper_card(t, 'Время напоминания о платежах', '9:00', 'шаг 15 минут')
            + stepper_card(t, 'Предупреждать', 'За 1 день', 'до платежа')
            + label(t, 'Траты') + stepper_card(t, 'Когда напоминать', '21:00', 'шаг 15 минут'))
    cap = 'В: всё — счётчиками, без кнопок и шторок. Компактно, но до далёкого значения дольше нажимать.'
    return notices_top(t) + body + C.caption(t, cap, 0)


BOARDS = [('Look775A1', look_a1, 390, 760), ('Look775A2', look_a2, 390, 760), ('Look775B', look_b, 390, 760),
          ('Ops775Normal', ops_normal, 390, 844), ('Ops775Compact', ops_compact, 390, 844),
          ('Notice777A', notices_a, 390, 760), ('Notice777ASheet', notices_a_sheet, 390, 760), ('Notice777ATime', notices_a_time, 390, 760),
          ('Notice777B', notices_b, 390, 760), ('Notice777C', notices_c, 390, 760)]

if __name__ == '__main__':
    C.run(BOARDS, sys.argv[1])
