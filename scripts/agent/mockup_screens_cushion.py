"""Generated boards: «Подушка» (#241) and the limit notification (#186) — chosen А · Б · А (poll 2026-11-25).

- `Cush241Reports` — the card «Подушка» in the «Отчёты» feed after «Капитал»: months the money lasts, one phrase, scale 0–6 months.
- `Cush241Edge` — few months of history · no rate · little money (calm, no red) · nothing on the accounts · no full month yet.
- `Not186B` — the notification: first what is still available, then a calm word (as the hint `limit_80`); the pace case.
The switch «Лимиты» lives on `Set1Notices` (mockup_screens_fb_set.py), group «Бюджет» — the notification is #745's `NoticeKind.LIMIT`.
Data: 9 000 USD on the accounts in the total, average spending 2 000 USD over the last six full months → «≈ 4,5 мес.».

ROW — (key, row title, sticky text) for the «Экраны» page; BOARDS — (name, title, fn, key).
"""
from mockup_plan_bricks import *  # noqa: F401,F403
from mockup_catalog_parts import col
from mockup_screens_reports import chart_card
from mockup_screens_recurring import notif
import mockup_screens_picture as picture

ROW = ('cushion', 'Подушка и лимиты (#241, #186)',
       'Выбрано (опрос 2026-11-25, А · Б · А): карточка «Подушка» в ленте «Отчётов» после «Капитала»; уведомление «Лимиты» (#745) — '
       '«ещё можно…» при 80 % лимита и «траты опережают месяц» при темпе быстрее плана; выключатель — «Ещё → Настройки → Уведомления → Бюджет».')

GOAL = 6  # the scale: half a year


def scale(months):
    marks = ''.join('<span>%s</span>' % m for m in ('0', '3', '6 мес.'))
    return (bullet(min(months / GOAL, 1), 'ok', pace=None, mt=12)
            + '<div style="display: flex; justify-content: space-between; font-size: 11px; color: %s; margin-top: 4px;">%s</div>' % (MUTED, marks))


def cushion_card(value='4,5', months=4.5, gloss='Денег хватит при тратах как в среднем за полгода: 2 000 USD в месяц.', money='9 000', extra='margin-top: 10px;'):
    big = ('<div style="display: flex; align-items: baseline; gap: 6px; margin-top: 6px;"><span style="%s font-size: 24px; font-weight: 700;">≈ %s</span>'
           '<span style="font-size: 14px; color: %s;">мес.</span></div>' % (MONO, value, MUTED))
    text = '<div style="font-size: 13px; color: %s; margin-top: 4px; line-height: 1.4;">%s</div>' % (MUTED, gloss)
    return chart_card('Подушка', 'на счетах %s USD' % money, big + text + scale(months), extra)


def reports():
    return picture.feed(picture.nw_b() + cushion_card())


def edge():
    few = cushion_card(gloss='Посчитано по 2 месяцам — с ростом записей станет точнее.', extra='margin-top: 4px;')
    norates = cushion_card(gloss='Без учёта: AMD — нет курса. «Ещё → Курсы».')
    low = cushion_card('0,8', 0.8, 'Денег хватит на ближайшие 3 недели. Лимит — ориентир, а не запрет: посмотрите «Бюджеты».', '1 600')
    empty = cushion_card('0', 0, 'На счетах в общем балансе сейчас нет денег.', '0')
    nodata = chart_card('Подушка', '', '<div style="font-size: 13px; color: %s; margin-top: 6px; line-height: 1.4;">Появится после первого полного месяца записей.</div>' % MUTED)
    return inner_screen('Особые случаи', few + norates + low + empty + nodata)


def notices():
    body = col(label('80 % лимита — сколько ещё можно', top=6),
               notif('Продукты: ещё можно 18 000 ARS', 'Это 20 % лимита на 12 дней.', ['Открыть']),
               label('Траты идут быстрее плана', top=10),
               notif('Кафе: траты опережают месяц', 'Если так пойдёт, лимит закончится раньше срока. Загляните в «Бюджеты».', ['Открыть']),
               note('Не больше одного уведомления в день (#745); один и тот же случай — один раз. Нажатие — «Бюджеты». '
                    'Включается в «Ещё → Настройки → Уведомления → Бюджет → Лимиты».'), gap=8)
    return inner_screen('Уведомления', body)


BOARDS = [  # name, title, fn, row key
    ('Cush241Reports', 'Выбрано (А): «Подушка» в ленте «Отчётов»', reports, 'cushion'),
    ('Cush241Edge', 'Подушка: мало истории, нет курса, мало денег, пусто', edge, 'cushion'),
    ('Not186B', 'Выбрано (Б): уведомление «ещё можно»', notices, 'cushion'),
]
