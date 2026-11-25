"""Proposal boards (#671): the «Код приглашения» row in «Ещё → Настройки → Помощь» (owner's poll 2026-11-24, А: in «Помощь»).

А — one row «Код приглашения · Выдан: <label>», tap — the code screen with «Отмена» (the same `AccessScreen` as from «Доступ закрыт»).
Б — two rows: the code itself (who it is for, masked code, no tap) and «Ввести другой код».
The row exists only in builds with the gate on (`ACCESS_GATE=true`). Imported by mockup_screens_feedback.py (FEEDBACK + BOARDS).
"""
from mockup_bricks import *  # noqa: F401,F403
import mockup_screens_fb_setcards as sc

FEEDBACK = [
    (671, 'Код приглашения в «Настройках → Помощь» · 2026-11-25',
     'А — одна строка «Код приглашения · Выдан: Алекс», нажатие — экран кода с «Отмена». Если код отозван — подпись «Больше не действует». '
     'Б — две строки: сам код (для кого, маска кода) и «Ввести другой код». Строка есть только в сборке с кодами приглашения.'),
]

LABEL = 'Алекс'


def help_section(rows):
    return sc.section(*sc.RU['help'][0], group(rows), '')


def hints_rows():
    return [sc.sw(*sc.RU['help'][1], True), sc.val(*sc.RU['help'][2])]


def settings(rows):
    """The bottom of «Настройки» (as board Set1RootMore): «Умные функции», then «Помощь» with the new rows."""
    smart = sc.section(*sc.RU['smart'][0], group([sc.val(*sc.RU['smart'][1], beta=True), sc.val(*sc.RU['smart'][2], beta=True)]), '')
    return inner_screen(sc.RU['title'], smart + help_section(hints_rows() + rows))


def a_row(revoked=False):
    if revoked:
        return row('Код приглашения', '<span style="color: %s;">Больше не действует</span>' % WARN, '', chevron(), min_h=52)
    return row('Код приглашения', 'Выдан: %s' % LABEL, '', chevron(), min_h=52)


def variant_a():
    return settings([a_row()])


def variant_a_revoked():
    return settings([a_row(revoked=True)])


def variant_b():
    code = row('Код приглашения', 'Выдан: %s · <span style="%s">K7QX-••••-9WTA</span>' % (LABEL, MONO), '', '', min_h=52)
    other = row('Ввести другой код', '', '', chevron(), min_h=52)
    return settings([code, other])


def change():
    """Tap on the row: the code screen over the app (as from «Доступ закрыт»), «Отмена» — back to «Настройки»."""
    return (empty('lock', 'Co-Shell для приглашённых')
                 + '<p style="margin: 12px 28px 0; text-align: center; font-size: 14px; line-height: 1.4; color: %s;">Введите код, который вам прислали. Он нужен один раз — потом всё работает без интернета.</p>' % INK2
                 + '<div style="margin: 16px 16px 0;">' + field('Код приглашения', 'XXXX-XXXX-XXXX', placeholder=True, mono=True)
                 + button('Продолжить', 'primary', mt=16, disabled=True) + button('Где взять код?', 'text', mt=4) + button('Отмена', 'text', mt=0) + '</div>')


BOARDS = [  # name, title, fn, issue number
    ('Inv671A', 'А · Помощь: «Код приглашения · Выдан: …»', variant_a, 671),
    ('Inv671ARevoked', 'А · код отозван: «Больше не действует»', variant_a_revoked, 671),
    ('Inv671Change', 'А и Б · нажатие: экран кода с «Отмена»', change, 671),
    ('Inv671B', 'Б · две строки: код и «Ввести другой код»', variant_b, 671),
]
