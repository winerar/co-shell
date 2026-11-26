"""Savings goals (#768; poll 2026-11-26, all «А»): saved = the balance of the goal's account, the plan a month, yearly sinking funds.

- `GoalList` — the screen: the hero with «Накоплено» / «Откладывать в месяц», goals by deadline with meters, reached ones folded.
- `GoalMany` — 12+ goals: no meters in rows (the meter is in the goal's sheet).
- `GoalEmpty` — nothing yet, with the word hint.
- `GoalNew` — the new-goal sheet; `GoalDetail` — an existing goal: progress, «Пополнить», the same form; `GoalHome` — the card on «Обзоре».
Imported by mockup_screens_forms.py. The poll (variant Б not chosen) lived in mockup_poll_goals768.py.
"""
from mockup_parts import *  # noqa: F401,F403
import mockup_screens_fb_290 as p
import mockup_screens_fb_modals as md

# (icon, color, name, when, saved, currency, target, fraction)
GOALS = [('gift', '#7FD6A0', 'Отпуск', 'К 1 июля · ≈ 125 USD в месяц', '1 250', 'USD', '2 000 USD', 0.62),
         ('house', '#FFB86B', 'Страховка квартиры', 'Каждый год, 15 марта · ≈ 45 USD в месяц', '180', 'USD', '360 USD', 0.5),
         ('school', '#6BB8FF', 'Курсы английского', 'К 1 февраля · ≈ 180 000 ARS в месяц', '240 000', 'ARS', '600 000 ARS', 0.4),
         ('wallet', '#E58BD0', 'Новый велосипед', 'Без срока · осталось 410 USD', '40', 'USD', '450 USD', 0.09)]
MORE = ['Ремонт кухни', 'Ноутбук', 'Подарки к праздникам', 'Налог на машину', 'Зубной врач', 'Телефон', 'Годовой абонемент', 'Шины']


def trail(saved, cur, target):
    return ('<span style="display: flex; flex-direction: column; align-items: flex-end;">%s<span style="font-size: 12px; color: %s;">из %s</span></span>'
            % (amount(saved, cur, INK, 15), MUTED, target))


def line(g, meter=True):
    sub = g[3] + (progress(g[7], mt=6) if meter else '')
    return row(g[2], sub, dot_icon(g[0], g[1]), trail(g[4], g[5], g[6]), min_h=64)


def totals(saved='1 635 USD', month='≈ 350 USD'):
    cell = lambda l, v: ('<div style="flex: 1;"><div style="font-size: 12px; color: H1;">%s</div><div style="margin-top: 2px; %s font-size: 22px; font-weight: 700; color: HERO_AMT;">%s</div></div>'  # noqa: E731
                         % (l, MONO, v))
    return hero_card('<div style="display: flex; gap: 12px;">%s%s</div><div style="margin-top: 8px; font-size: 12px; color: H1;">по курсу на сегодня</div>'
                     % (cell('Накоплено', saved), cell('Откладывать в месяц', month)))


def screen():
    body = ('<div style="padding: 8px 16px 0;">%s</div><div style="height: 12px;"></div>' % totals() + group([line(g) for g in GOALS])
            + '<div style="padding: 12px 24px;">%s</div>' % p.small_link('Достигнутые: 1 · показать'))
    return inner_screen('Цели', body) + fab('Новая цель')


def list_():
    return board('Цели', screen())


def many():
    rows = [line((['gift', 'house', 'school', 'cart', 'bolt', 'user'][i % 6], ['#7FD6A0', '#FFB86B', '#6BB8FF', '#E58BD0', '#FFE14D', '#9C8CFF'][i % 6], n,
                  'К %d %s · ≈ %d USD в месяц' % (i + 3, ['января', 'марта', 'мая', 'июля'][i % 4], 40 + 15 * i), str(50 * (i + 1)), 'USD', '%d USD' % (1000 + 250 * i), 0), meter=False)
            for i, n in enumerate(MORE)]
    body = ('<div style="padding: 8px 16px 0;">%s</div><div style="height: 12px;"></div>' % totals('12 480 USD', '≈ 1 900 USD') + group(rows)
            + '<div style="padding: 12px 24px;">%s</div>' % p.small_link('Достигнутые: 4 · показать'))
    return board('Цели: много', inner_screen('Цели', body) + fab('Новая цель'))


def empty_():
    hint = '<div style="padding: 8px 16px 0;">%s</div>' % note('Цель — сумма, на которую копите. Укажите срок, и приложение подскажет, сколько откладывать в месяц.', mt=0)
    return board('Цели: пусто', inner_screen('Цели', hint) + empty('flag', 'Целей пока нет. Нажмите «+», чтобы начать копить на отпуск, ремонт или годовой платёж.') + fab('Новая цель'))


def ribbon():
    cells = ''.join(badge(i, c, '#0B0F0A', 44, 12).replace('style="', 'style="%s' % ('box-shadow: 0 0 0 2px %s; ' % LIME if n == 0 else ''), 1)
                    for n, (i, c) in enumerate([('КО', '#7FD6A0'), ('ВК', '#FFB86B'), ('НА', '#6BB8FF')]))
    return ('<div style="font-size: 12px; color: %s; margin-top: 10px;">Где копите</div><div style="display: flex; gap: 8px; align-items: center; margin-top: 6px;">%s'
            '<span style="font-size: 13px; color: %s;">Все ›</span></div>' % (MUTED, cells, MUTED))


def form(plan=True):
    yearly = row('Каждый год', 'Для страховки, налогов, годовых подписок', '', switch(False), min_h=52).replace('padding: 8px 16px', 'padding: 8px 0')
    hint = 'Откладывайте ≈ 125 USD в месяц, чтобы успеть к 1 июля' if plan else 'Накоплено — баланс этого счёта. Переводите на него деньги, и цель растёт сама.'
    return (field('Название', 'Отпуск') + field('Сколько нужно', '2 000 USD', mono=True) + '<div style="height: 8px;"></div>'
            + p.detail('calendar', 'К какому дню', '1 июля 2027') + ribbon() + yearly + note(hint, mt=4))


def new_sheet():
    return board('Новая цель', md.modal_sheet(screen(), 'Новая цель', form() + button('Сохранить', mt=10), '', top=110))


def detail():
    head = ('<div style="%s font-size: 28px; font-weight: 700;">1 250 <span style="font-size: 14px;">USD</span></div>'
            '<div style="margin-top: 4px; font-size: 13px; color: %s;">1 250 USD из 2 000 USD · осталось 750 USD</div>%s%s' % (MONO, MUTED, progress(0.62, mt=8), button('Пополнить', 'outline', mt=10)))
    body = head + form() + button('Сохранить', mt=10)
    return board('Цель', md.modal_sheet(screen(), 'Цель', body, '', top=60))


def home():
    card_ = card(''.join(line(g) for g in GOALS[:2]), pad='4px 0')
    body = ('<div style="padding: 8px 16px 0;"><div style="display: flex; justify-content: space-between; align-items: center;">%s%s</div>'
            '<div style="height: 8px;"></div>%s</div>' % (label('Цели', top=0), p.small_link('Все ›'), card_))
    return board('Обзор: цели', tab_screen('Обзор', body, 'home'))


BOARDS = [('GoalList', list_), ('GoalMany', many), ('GoalEmpty', empty_), ('GoalNew', new_sheet), ('GoalDetail', detail), ('GoalHome', home)]
