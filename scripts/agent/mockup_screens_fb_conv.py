"""Generated boards: the converter sheet (#319, owner «ок» 2026-11-18).

Entries are on the canonical boards (`Home`, `More`, `Rates`). The converter is a sheet with the amount keypad: «Пара» («Отдаю / Получаю»)
by default, a button in the header switches to «Все валюты»; the last pair and mode are remembered. The amount starts empty (grey «0»).
Imported by mockup_screens_feedback.py (FEEDBACK + BOARDS).
"""
from mockup_bricks import *  # noqa: F401,F403
from mockup_parts import *  # noqa: F401,F403
from mockup_screens_main import home, keypad

FEEDBACK = [
    (319, 'Конвертер: два режима · 2026-11-18',
     'Вход — карточка «Конвертер» на «Обзоре», строка в «Ещё → Деньги» и на «Курсах». Шторка открывается парой «Отдаю / Получаю»; '
     'кнопка в шапке переключает на «Все валюты» и обратно. Запоминаем последнюю пару и режим. Сумма сначала пустая.'),
]

# Totals currencies of the family; the first is the main one. (code, bare result for 100 USD)
CUR = [('USD', '100'), ('ARS', '101 500'), ('AMD', '38 700'), ('RUB', '9 150'), ('USDT', '100,10'),
       ('EUR', '92,40'), ('BRL', '566,20'), ('GBP', '78,90')]
EMPTY = '0'


def num(text, size=17, color=INK):
    return '<span style="%s font-size: %dpx; font-weight: 700; color: %s;">%s</span>' % (MONO, size, color, text)


def caret():
    return '<span style="width: 2px; height: 30px; background: %s; margin-left: 2px; align-self: center;"></span>' % LIME


def cur_pill(code):
    return ('<span style="display: inline-flex; align-items: center; gap: 6px; height: 36px; padding: 0 12px; border-radius: 999px; border: 1px solid %s; '
            'font-size: 14px; font-weight: 700;">%s<span style="color: %s;">▾</span></span>' % (LINE2, code, MUTED))


def side(title, value, code, on, empty=False):
    border = '2px solid %s' % INK if on else '1px solid %s' % LINE2
    val = (num(EMPTY, 30, MUTED) + (caret() if on else '')) if empty else num(value, 30, INK)
    return ('<div style="padding: 12px 16px; border-radius: 16px; background: %s; border: %s;"><div style="font-size: 12px; color: %s;">%s</div>'
            '<div style="display: flex; align-items: center; gap: 8px; margin-top: 4px;">%s<span style="flex-grow: 1;"></span>%s</div></div>'
            % (CHIP, border, MUTED, title, val, cur_pill(code)))


def swap():
    return '<div style="display: flex; justify-content: center;">%s</div>' % icon_button('transfer', 'Поменять валюты местами')


def all_row(code, value, on, empty=False, h=52):
    val = num(EMPTY, 18, MUTED) if empty else num(value, 18, LIME if on else INK)
    return ('<div style="display: flex; align-items: center; min-height: %dpx; padding: 0 14px; border-radius: 18px; background: %s;">'
            '<span style="font-size: 16px;">%s</span><span style="flex-grow: 1;"></span>%s</div>' % (h, CHIP if on else 'transparent', code, val))


def all_list(n):
    return '<div style="display: flex; flex-direction: column; gap: 2px;">%s</div>' % ''.join(
        all_row(c, v, c == 'USD', False, 48 if n > 5 else 52) for c, v in CUR[:n])


def mode_head(mode):
    """Sheet header with the mode button: «Пара» shows the «Все» icon and vice versa; the subtitle says what is on."""
    icon, label_txt, to = ('list', 'Пара валют', 'Показать все валюты') if mode == 'pair' else ('transfer', 'Все валюты', 'Показать пару')
    return ('<div style="display: flex; align-items: flex-start; gap: 4px; padding: 8px 8px 0 20px;"><div style="flex-grow: 1; padding-top: 10px;">'
            '<h2 style="margin: 0; font-size: 22px; font-weight: 700;">Конвертер</h2>'
            '<div style="margin-top: 4px; font-size: 13px; color: %s;">Курс на сегодня</div></div>%s%s</div>'
            % (MUTED, icon_button(icon, to, INK), icon_button('close', 'Закрыть', MUTED)))


def mode_sheet(mode, body, top=70):
    inner = (mode_head(mode) + '<div style="padding: 8px 20px 0;">%s</div>' % body
             + '<div style="position: absolute; left: 12px; right: 12px; bottom: 14px;">%s</div>' % keypad('Готово'))
    return scrim(home()) + sheet(inner, top)


def pair_body(a, b, empty=False):
    """The pair: «Отдаю» is the active side (a tap on the other side types there); «⇅» flips the currencies."""
    return ('<div style="margin-top: 6px;">%s%s%s</div>' % (side('Отдаю', a, 'USD', True, empty), swap(), side('Получаю', b, 'ARS', False, empty))
            + note('1 USD = 1 015 ARS', 6))


def sheet_pair_empty():
    return mode_sheet('pair', pair_body('', '', True))


def sheet_pair_typed():
    return mode_sheet('pair', pair_body('100', '101 500'))


def sheet_all_typed():
    return mode_sheet('all', all_list(5) + note('Нажмите на валюту — посчитаем из неё', 10))


def sheet_all_many():
    return mode_sheet('all', all_list(8) + note('Ещё валюты — прокрутите список', 8), 40)


BOARDS = [  # name, title, fn, issue number
    ('Conv2PairEmpty', 'Конвертер · «Пара» (по умолчанию): пусто', sheet_pair_empty, 319),
    ('Conv2PairType', 'Конвертер · «Пара»: набрано 100 USD', sheet_pair_typed, 319),
    ('Conv2AllType', 'Конвертер · «Все валюты» (кнопка в шапке)', sheet_all_typed, 319),
    ('Conv2AllMany', 'Конвертер · «Все валюты»: 8 валют', sheet_all_many, 319),
]
