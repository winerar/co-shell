"""Proposal boards (#761, before the owner's «ок»): «О приложении» names the people behind the app — ideas and testing, development —
and mentions Claude. Three places (A card «Авторы», B a quiet signature at the bottom, C a row «Авторы ›» with its own sheet) and
three wordings for Claude (on the A board: the developer's line). Imported by mockup_screens_feedback.py (FEEDBACK + BOARDS).
"""
from mockup_bricks import *  # noqa: F401,F403
from mockup_parts import footer, top_bar
from mockup_screens_about import about_card, app_head, links

FEEDBACK = [
    (761, 'Задача #761 · «О приложении»: авторы · 2026-11-26',
     'Владелец: показать автора идей и разработчика. Идеи и тестирование — Анастасия Карачевцева; разработка — Александр Карачевцев; '
     'упомянуть Claude. Варианты места (А карточка, Б подпись внизу, В строка «Авторы ›») и формулировки для Claude — опросом.'),
]

IDEAS = ('АК', 'Анастасия Карачевцева', 'Идеи и тестирование', PINK)
DEV = ('АК', 'Александр Карачевцев', 'Разработка', CYAN)
CLAUDE_WITH = 'Разработка · вместе с Claude'


def person(ini, name, role, color):
    return row(name, role, badge(ini, color, size=40, radius=20), '', min_h=56)


def page(authors, extra_links=''):
    body = (app_head('Версия 26.11.63 · сборка 1300') + about_card()
            + '<div style="padding: 0 16px;">%s<div style="margin-top: 14px;">%s</div></div>' % (authors, extra_links or links()))
    return top_bar('О приложении') + '<div style="height: 690px; overflow: hidden;">%s</div>' % body + footer(button('Проверить снова', 'outline', 'refresh', 0))


def card_a(dev_role=CLAUDE_WITH, claude_row=False):
    rows = [person(*IDEAS), person(DEV[0], DEV[1], dev_role, DEV[3])]
    if claude_row:
        rows.append(row('Claude', 'ИИ от Anthropic, помощник в разработке', tile_icon('spark', VIOLET), '', min_h=56))
    return label('Авторы', top=14) + group(rows)


def signature_b():
    return ('<div style="margin-top: 18px; text-align: center; font-size: 13px; line-height: 1.6; color: %s;">'
            'Идеи и тестирование — Анастасия Карачевцева<br>Разработка — Александр Карачевцев, вместе с Claude</div>' % MUTED)


def links_c():
    return group([row('Авторы', 'Анастасия и Александр Карачевцевы', tile_icon('user', LIME), chevron(), min_h=56),
                  row('Отправить отзыв', 'Ошибка или идея', tile_icon('mail', PINK), chevron(), min_h=56)])


def sheet_c():
    rows = group([person(*IDEAS), person(DEV[0], DEV[1], CLAUDE_WITH, DEV[3])])
    return (top_bar('О приложении') + '<div style="height: 300px; overflow: hidden; opacity: 0.35;">%s</div>' % app_head('Версия 26.11.63 · сборка 1300')
            + '<div style="position: absolute; left: 0; right: 0; bottom: 0; border-radius: 28px 28px 0 0; background: %s; border: 1px solid %s; padding: 20px 16px 32px;">'
            '<div style="display: flex; align-items: center;"><h2 style="margin: 0; font-size: 22px; flex-grow: 1;">Авторы</h2>%s</div>'
            '<div style="margin-top: 12px;">%s</div></div>' % (CARD, LINE, svg('close', MUTED, 22), rows))


BOARDS = [
    ('Credits761A', 'А: карточка «Авторы» под «Принципами»', lambda: page(card_a()), 761),
    ('Credits761B', 'Б: тихая подпись внизу экрана', lambda: page(signature_b()), 761),
    ('Credits761C', 'В: строка «Авторы ›» → шторка', lambda: page('', links_c()), 761),
    ('Credits761CSheet', 'В: шторка «Авторы»', sheet_c, 761),
    ('Credits761Claude2', 'Claude отдельной строкой', lambda: page(card_a(dev_role='Разработка', claude_row=True)), 761),
]
