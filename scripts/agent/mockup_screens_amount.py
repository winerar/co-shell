"""Generated boards: «Сумма в редакторе» (#300) — the owner's answer of the poll 2026-11-24, А.

Every amount of an editor (budget limit, template amount, debt and its return, credit limit and minimum payment) is shown by a field
that only shows the number; a tap raises the sheet with the own keypad and «Готово» — never the system keyboard. The sheet is the one
of the manual rate (`FbModalARate`), without «+» and «−» (a single number, not a sum).

ROW — (key, row title, sticky text) for the «Экраны» page; BOARDS — (name, title, fn, row key).
"""
from mockup_bricks import *  # noqa: F401,F403
import mockup_screens_fb_modals as md
from mockup_screens_main import keypad

ROW = ('amount300', 'Сумма в редакторе (#300)',
       'Выбрано (опрос 2026-11-24, А): поле суммы только показывает число, нажатие поднимает шторку со своей клавиатурой и «Готово»; '
       '«+» и «−» — только в шторке ввода операции. Так же — начальный и текущий баланс счёта, сумма шаблона, долга и возврата, кредитный лимит.')


def amount_sheet():
    fig = ('<div style="margin-top: 16px; display: flex; align-items: center; justify-content: flex-end; min-height: 52px;">'
           '<span style="%s font-size: 40px; font-weight: 700;">600 000</span><span style="width: 2px; height: 34px; background: %s; margin-left: 2px;"></span>'
           '<span style="%s font-size: 18px; color: %s; margin-left: 8px;">ARS</span></div>' % (MONO, LIME, MONO, MUTED))
    return md.modal_sheet(md.budget_bg(), 'Лимит', fig + '<div style="margin-top: 14px;">%s</div>' % keypad('Готово'), top=330)


BOARDS = [  # name, title, fn, row key
    ('Amount300Sheet', 'Выбрано (А): сумма в редакторе — шторка со своей клавиатурой', amount_sheet, 'amount300'),
]
