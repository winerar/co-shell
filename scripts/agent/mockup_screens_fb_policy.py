"""Boards (#652): a link to the privacy policy (landing site, privacy/<lang>/) in «Ещё → Настройки → Помощь».

А — one row «Конфиденциальность · Что хранится и как удалить данные», tap — the policy page in the phone's browser
(it links to the «Удалить данные» page). Picked А (poll-652.json, 2026-11-25); board Б (two rows) removed.
The row exists only in builds that know the site address (`BuildConfig.SITE_URL`, `PolicyLink`). Imported by mockup_screens_feedback.py (FEEDBACK + BOARDS).
"""
from mockup_bricks import *  # noqa: F401,F403
import mockup_screens_fb_invite as inv

FEEDBACK = [
    (652, 'Политика конфиденциальности в «Настройках → Помощь» · выбрано А, 2026-11-25',
     'Строка ведёт на страницу политики на сайте приложения на языке интерфейса (открывается в браузере телефона). '
     'Одна строка «Конфиденциальность»; удаление данных — ссылкой со страницы политики. Строки нет, если сборка не знает адреса сайта (`HelpCard`, `PolicyLink`).'),
]


def variant_a():
    return inv.settings([inv.a_row(), row('Конфиденциальность', 'Что хранится и как удалить данные', '', chevron(), min_h=52)])


BOARDS = [  # name, title, fn, issue number
    ('Priv652A', 'А · Помощь: «Конфиденциальность»', variant_a, 652),
]
