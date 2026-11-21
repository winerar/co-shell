"""Generated boards: feedback of 2026-11-20 — #575 (pencil for «Изменить лимит») and #581 (balance adjustment needs «Сохранить»).

Proposals for the owner's poll (`poll-nov20.json`); after the answer the winner moves to the screen boards and these leave the canvas.
Imported by mockup_screens_feedback.py.
"""
from mockup_bricks import *  # noqa: F401,F403
from mockup_screens_budgets import envelope_detail
from mockup_screens_fb_290 import acc_form_a
from mockup_screens_fb_hints import pill_box

FEEDBACK = [
    (575, 'Конверт: значок «Изменить лимит» — карандаш вместо документа · 2026-11-20',
     'Сейчас в шапке конверта значок-документ, по нему не понять, что это правка. А — карандаш (как «изменить» везде), Б — слово «Изменить».'),
    (581, 'Исправить баланс: непонятно, что нужно ещё «Сохранить» · 2026-11-20',
     'Сейчас «Готово» в маленькой шторке только закрывает её, а корректировка запишется после «Сохранить» в шторке счёта. '
     'А — кнопка «Сохранить баланс» записывает корректировку сразу, плашка «Баланс исправлен». Б — как сейчас, но плитка говорит «Нажмите «Сохранить»».'),
]

PEN = 'M4 20h4L19 9l-4-4L4 16zM14 6l4 4'
NOW, REAL, DIFF = '245 300,00', '257 300,00', '+12 000,00'


def _bar_icon(path):
    return ('<span style="width: 48px; height: 48px; border-radius: 50%%; display: flex; align-items: center; justify-content: center;">'
            '<svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="%s" stroke-width="1.6" stroke-linejoin="round" stroke-linecap="round">'
            '<path d="%s"/></svg></span>' % (MUTED, path))


def _envelope(actions):
    html = envelope_detail()
    return html.replace(icon_button('note', 'Изменить лимит', MUTED), actions)


def pen():
    return board('Конверт: карандаш', _envelope(_bar_icon(PEN)))


def word():
    return board('Конверт: «Изменить»', _envelope(
        '<span style="height: 48px; padding: 0 12px; display: flex; align-items: center; color: %s; font-size: 15px; font-weight: 600;">Изменить</span>' % CYAN))


def _amount_sheet(right, hint, foot=''):
    head = ('<div style="padding: 6px 16px 0;"><div style="display: flex; align-items: center; height: 48px;"><b style="flex-grow: 1; font-size: 20px;">Исправить баланс</b>%s</div>'
            % right)
    body = field('Реальный баланс', REAL + ' <span style="font-size: 14px; color: %s;">ARS</span>' % MUTED, hint=hint, mono=True)
    return scrim(acc_form_a()) + sheet(head + body + foot + '</div>', 470)


def adjust_now():
    """Today: «Готово» only closes; the adjustment waits for «Сохранить» of the account sheet behind."""
    return board('Исправить баланс: сейчас', _amount_sheet(
        '<span style="color: %s; font-weight: 600;">Готово</span>' % LIME, 'Корректировка при сохранении: %s ARS' % DIFF))


def adjust_save():
    """A: one button records it now; no «Готово»."""
    return board('Исправить баланс: А', _amount_sheet(
        icon_button('close', 'Закрыть', MUTED), 'Сейчас: %s ARS. Разница %s ARS — не трата и не доход.' % (NOW, DIFF),
        button('Сохранить баланс', mt=16)))


def adjust_saved():
    """A, after: back on the account sheet, the balance is already new, a pill says what was recorded."""
    html = acc_form_a().replace('245 300,00 ARS', REAL + ' ARS').replace('245 300,00', REAL)
    return board('Исправить баланс: А, после', html + pill_box('Баланс исправлен: %s ARS' % DIFF, 'check'))


def adjust_pending():
    """B: as today, but the tile says what is left to do."""
    html = acc_form_a().replace('Баланс сейчас · Исправить', 'Будет %s · нажмите «Сохранить»' % REAL)
    return board('Исправить баланс: Б', html)


BOARDS = [  # name, title, fn, issue number
    ('Fb575Pen', '#575 · А: карандаш «Изменить лимит» в шапке конверта', pen, 575),
    ('Fb575Word', '#575 · Б: слово «Изменить» в шапке конверта', word, 575),
    ('Fb581Now', '#581 · сейчас: «Готово» только закрывает, запись — после «Сохранить» счёта', adjust_now, 581),
    ('Fb581A', '#581 · А: «Сохранить баланс» записывает корректировку сразу', adjust_save, 581),
    ('Fb581ASaved', '#581 · А, после: баланс уже новый, плашка «Баланс исправлен»', adjust_saved, 581),
    ('Fb581B', '#581 · Б: как сейчас, плитка подсказывает «нажмите «Сохранить»»', adjust_pending, 581),
]
