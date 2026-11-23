"""Debts and loans (#552; poll 2026-11-23, all «А»): «Мне должны» / «Я должен», a return day, a note; issue and return move the account but are
neither spending nor income.

- `DebtList` — the screen: tabs, the total per currency, lines with the meter of what came back, closed debts folded (the due day is never red).
- `DebtEmpty` — nothing yet.
- `DebtNew` — the new-debt sheet; `DebtDetail` — a debt's card; `DebtReturn` — «Вернули часть» over the card.
Imported by mockup_screens_forms.py. The poll variants (not chosen) lived in mockup_poll_debts552.py.
"""
from mockup_parts import *  # noqa: F401,F403
import mockup_screens_fb_290 as p
import mockup_screens_fb_modals as md

ROWS = [('Саша', 'Осталось 120 USD из 300 USD · до 5 декабря · за билеты', '120', 'USD', '#6BB8FF', 'СА'),
        ('Мама', 'срок не задан', '45 000', 'RUB', '#FFB86B', 'МА'),
        ('Игорь', 'через 3 дня · за ремонт', '80', 'USD', '#C79BFF', 'ИГ'),
        ('Лена', 'срок прошёл 4 дня назад · за обед', '15 000', 'ARS', '#8BC34A', 'ЛЕ')]


def line(r):
    return row(r[0], r[1], badge(r[5], r[4], '#0B0F0A', 40, 20), amount(r[2], r[3], INK, 15))


def tabs(sel=0):
    return '<div style="padding: 4px 0 0;">%s</div>' % segmented(['Мне должны', 'Я должен'], ['Мне должны', 'Я должен'][sel])


def screen(sel=0):
    totals = note('Мне должны: 200 USD · 45 000 RUB · 15 000 ARS', mt=10)
    body = tabs(sel) + totals + '<div style="height: 8px;"></div>' + group([line(r) for r in ROWS]) + p.small_link('Закрытые: 2')
    return inner_screen('Долги', body) + fab('Новый долг')


def list_():
    return board('Долги', screen())


def empty_():
    return board('Долги: пусто', inner_screen('Долги', '') + empty('user', 'Долгов пока нет. Нажмите «+», чтобы записать первый.') + fab('Новый долг'))


def new_sheet():
    f = lambda lbl, val, hint='': field(lbl, val, hint=hint, mono=True)  # noqa: E731
    body = (segmented(['Мне должны', 'Я должен'], 'Мне должны') + '<div style="height: 8px;"></div>' + field('Кто', 'Саша') + f('Сумма', '300 USD')
            + '<div style="height: 8px;"></div>' + p.detail('wallet', 'С какого счёта', 'Наличные USD') + '<div style="height: 6px;"></div>'
            + p.detail('refresh', 'Срок возврата', '5 декабря') + field('Заметка', 'за билеты')
            + note('Сумма изменит баланс счёта, но не попадёт в расходы и доходы.', mt=8) + button('Сохранить', mt=10))
    return board('Новый долг', md.modal_sheet(screen(), 'Новый долг', body, '', top=90))


def card_body():
    return ('<div style="padding: 0 20px;"><div style="font-size: 12px; color: %s;">Мне должны</div><div style="margin-top: 4px; %s font-size: 28px; font-weight: 700;">120,00 '
            '<span style="font-size: 14px;">USD</span></div><div style="margin-top: 4px; font-size: 13px; color: %s;">Всего 300,00 USD · возвращено 180,00 USD</div>%s'
            '<div style="margin-top: 8px; font-size: 14px;">до 5 декабря</div><div style="margin-top: 2px; font-size: 14px; color: %s;">за билеты</div></div>'
            % (MUTED, MONO, MUTED, progress(0.6, mt=10), INK2))


def detail():
    ret = group([row('12 нояб. · 100,00 USD', '', '', '', min_h=44), row('20 нояб. · 80,00 USD', '', '', '', min_h=44)])
    body = (card_body() + '<div style="padding: 10px 20px 0;">' + p.block('Возвраты', ret) + button('Вернули часть', mt=8) + button('Закрыть долг', 'outline', mt=8)
            + button('Удалить', 'outline', mt=8) + '</div>')
    return board('Долг: карточка', scrim(screen()) + sheet(md.sheet_head('Саша') + body, 90))


def return_sheet():
    f = field('Сумма', '120 <span style="font-size: 14px; color: %s;">USD</span>' % MUTED, mono=True)
    body = f + '<div style="height: 6px;"></div>' + p.detail('wallet', 'На какой счёт', 'Наличные USD') + button('Записать', mt=12)
    return board('Долг: вернули часть', md.modal_sheet(scrim(screen()), 'Вернули часть', body, '', top=330))


BOARDS = [('DebtList', list_), ('DebtEmpty', empty_), ('DebtNew', new_sheet), ('DebtDetail', detail), ('DebtReturn', return_sheet)]
