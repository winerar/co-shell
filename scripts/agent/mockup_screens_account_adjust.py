"""«Исправить баланс» (#581, poll 2026-11-21 «А»): «Сохранить баланс» records the adjustment at once, the account sheet stays
with the new balance and the «Баланс исправлен» pill. Boards `AccountAdjust`, `AccountAdjusted`; imported by mockup_screens_forms.py.
(#575 «А» — the pencil — is drawn straight in `BudDetail`.)
"""
from mockup_bricks import *  # noqa: F401,F403
from mockup_screens_fb_290 import acc_form_a
from mockup_screens_fb_hints import pill_box

REAL, DIFF = '257 300,00', '+12 000,00'


def _amount_sheet(right, hint, foot=''):
    head = ('<div style="padding: 6px 16px 0;"><div style="display: flex; align-items: center; height: 48px;"><b style="flex-grow: 1; font-size: 20px;">Исправить баланс</b>%s</div>'
            % right)
    body = field('Реальный баланс', REAL + ' <span style="font-size: 14px; color: %s;">ARS</span>' % MUTED, hint=hint, mono=True)
    return scrim(acc_form_a()) + sheet(head + body + foot + '</div>', 470)


def adjust_save():
    """«Сохранить баланс» records it now (no «Готово»); closed without it — the number is dropped."""
    return board('Исправить баланс', _amount_sheet(
        '', 'Разница: %s ARS — не трата и не доход' % DIFF,
        button('Сохранить баланс', mt=16)))


def adjust_saved():
    """After: back on the account sheet, the balance is already new, a pill says what was recorded."""
    html = acc_form_a().replace('245 300,00 ARS', REAL + ' ARS').replace('245 300,00', REAL)
    return board('Счёт: баланс исправлен', html + pill_box('Баланс исправлен: %s ARS' % DIFF, 'check'))


BOARDS = [('AccountAdjust', adjust_save), ('AccountAdjusted', adjust_saved)]
