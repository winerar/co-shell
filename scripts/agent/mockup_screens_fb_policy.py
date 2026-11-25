"""Boards (#652): a link to the privacy policy (landing site, privacy/<lang>/) in «Ещё → Настройки → Помощь».

А — one row «Конфиденциальность · Что хранится и как удалить данные», tap — the policy page in the phone's browser
(it links to the «Удалить данные» page). Б — two rows: «Политика конфиденциальности» and «Удалить данные».
The rows exist only in builds that know the landing address. Imported by mockup_screens_feedback.py (FEEDBACK + BOARDS).
"""
from mockup_bricks import *  # noqa: F401,F403
import mockup_screens_fb_invite as inv

FEEDBACK = [
    (652, 'Политика конфиденциальности в «Настройках → Помощь» · опрос',
     'Строка ведёт на страницу политики на сайте приложения на языке интерфейса (открывается в браузере телефона). '
     'А — одна строка, удаление данных — ссылкой со страницы политики; Б — две строки.'),
]


def variant_a():
    return inv.settings([inv.a_row(), row('Конфиденциальность', 'Что хранится и как удалить данные', '', chevron(), min_h=52)])


def variant_b():
    return inv.settings([inv.a_row(),
                         row('Политика конфиденциальности', 'Что хранится и что уходит в интернет', '', chevron(), min_h=52),
                         row('Удалить данные', 'Как стереть всё на телефоне и в облаке', '', chevron(), min_h=52)])


BOARDS = [  # name, title, fn, issue number
    ('Priv652A', 'А · Помощь: «Конфиденциальность»', variant_a, 652),
    ('Priv652B', 'Б · Помощь: политика и удаление данных', variant_b, 652),
]
