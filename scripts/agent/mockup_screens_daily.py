"""«Сегодня можно» (#769; poll 2026-11-27 «А·Б·А»): a card under the balance on «Обзоре» — today's allowance, days and money left, the meter of
today's spending; a tap — «Как посчитано». Without limits — from the month's income; goals' monthly share is put aside first.

- `DailyHome` — «Обзор» with the card; `DailyWhy` — the sheet; `DailyOver` — the plan of the month is spent («0», no red).
Imported by mockup_screens_forms.py. The poll (variant «плитка» not chosen) lived in mockup_poll_daily769.py.
"""
from mockup_parts import *  # noqa: F401,F403
import mockup_screens_fb_modals as md
from mockup_screens_main import balance_hero


def daily(amount='9 800', sub='до конца месяца 5 дней · осталось 49 000 ARS', today=True, color=INK):
    bar = (progress(0.25, mt=10) + '<div style="margin-top: 4px; font-size: 12px; color: %s;">сегодня уже 2 450 ARS</div>' % MUTED) if today else ''
    return card('<div style="font-size: 14px; color: %s;">Сегодня можно</div><div style="margin-top: 2px; %s font-size: 22px; font-weight: 700; color: %s;">≈ %s '
                '<span style="font-size: 13px; color: %s;">ARS</span></div><div style="margin-top: 2px; font-size: 13px; color: %s;">%s</div>%s'
                % (MUTED, MONO, color, amount, MUTED, MUTED, sub, bar), pad='16px', extra='margin-top: 12px;')


def recent():
    return label('Последние операции') + card(tx('Кафе', 'сегодня', '2 450', 'ARS', 'out', 'cafe', '#FFB86B') + tx('Продукты', 'вчера', '18 300', 'ARS', 'out', 'cart', '#7FD6A0'))


def page(body):
    return tab_screen('Обзор', '<div style="padding: 0 16px;">%s</div>' % (balance_hero('1 245 300', 'ARS', '≈ 1 020 USD') + body), 'home')


def home():
    return board('Обзор: сегодня можно', page(daily() + recent()))


def why():
    line = lambda a, b, strong=False: ('<div style="display: flex; justify-content: space-between; margin-top: 10px; font-size: 15px; %s"><span>%s</span><span style="%s">%s</span></div>'  # noqa: E731
                                       % ('font-weight: 700;' if strong else '', a, MONO, b))
    body = (line('Лимиты на месяц', '600 000 ARS') + line('Уже потрачено', '− 451 000 ARS') + line('Впереди регулярные', '− 65 000 ARS') + line('Отложить на цели', '− 35 000 ARS')
            + '<div style="margin-top: 10px; border-top: 1px solid %s;"></div>' % LINE + line('Осталось', '49 000 ARS', True) + line('Дней до конца месяца', '5')
            + line('Можно в день', '≈ 9 800 ARS', True) + note('Это ориентир, а не запрет. Цифра пересчитывается после каждой операции.', mt=12))
    return board('Сегодня можно: как посчитано', md.modal_sheet(page(daily()), 'Как посчитано', body, 'ноябрь, по курсу на сегодня', top=200))


def over():
    return board('Сегодня можно: план потрачен', page(daily('0', 'план месяца потрачен · сверх него 12 000 ARS', today=False, color=MUTED) + recent()))


BOARDS = [('DailyHome', home), ('DailyWhy', why), ('DailyOver', over)]
