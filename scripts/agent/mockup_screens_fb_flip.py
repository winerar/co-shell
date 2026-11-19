"""Generated boards: #519 — hero cards flip on tap (owner 2026-11-19): «Обзор» and «Бюджеты».

Gesture: a tap turns the card around the HORIZONTAL axis (rotationX, like a notepad page), real depth (cameraDistance), spring with a small overshoot;
at 90° the face fades and the back appears. A round «↻» button is the TalkBack path. «Показать по валютам» leaves the front of «Обзор» —
the per-currency block moves to the back (owner 2026-11-19). Imported by mockup_screens_feedback.py (FEEDBACK + BOARDS).
"""
from mockup_bricks import *  # noqa: F401,F403
from mockup_parts import *  # noqa: F401,F403
from mockup_plan_bricks import bullet as plan_bullet, env_row, month_switch  # noqa: F401
from mockup_screens_main import RECENT, tile, tiles, recent_card

FEEDBACK = [
    (519, 'Карточки-герои переворачиваются: оборот «Обзора» и «Бюджетов» · 2026-11-19',
     'Нажатие переворачивает карточку вокруг горизонтальной оси (объёмно, пружиной). Оборот «Обзора»: итоги месяца и валюты; '
     '«Бюджетов»: куда смотреть. Раскрытие «по валютам» с лица «Обзора» убрано.'),
]

DEPTH = 'perspective: 1600px;'


def flip_button(color=HERO_HINT):
    """Round 48 dp button in the top right corner of the card (the TalkBack path of the tap)."""
    return '<div style="position: absolute; top: 4px; right: 4px;">%s</div>' % icon_button('refresh', 'Перевернуть', color)


def hero(inner, pad='18px', tilt=0, mt=8):
    """Hero card with the flip button; `tilt` — mid-flip angle in degrees (the board shows the moment of the turn)."""
    t = ('transform: rotateX(%ddeg); transform-origin: 50%% 50%%;' % tilt) if tilt else ''
    return ('<div style="margin-top: %dpx; %s"><div style="position: relative; %s">%s</div></div>'
            % (mt, DEPTH, t, hero_card(inner, pad=pad, radius=24).replace('<div style="border-radius', '<div style="border-radius', 1) + flip_button()))


def kv(title, value, cur, color=INK, big=False):
    return ('<div><div style="font-size: 12px; color: %s;">%s</div><div style="%s font-size: %dpx; font-weight: 700; color: %s;">%s'
            ' <span style="font-size: 11px; color: %s;">%s</span></div></div>' % (HERO_HINT, title, MONO, 20 if big else 15, color, value, HERO_HINT, cur))


def rowline(left, right, color=INK):
    return ('<div style="display: flex; justify-content: space-between; align-items: baseline; padding: 3px 0; font-size: 13px;">'
            '<span style="color: %s;">%s</span><span style="%s color: %s;">%s</span></div>' % (HERO_HINT, left, MONO, color, right))


def hairline():
    return '<div style="height: 1px; background: rgba(255,255,255,0.14); margin: 10px 0;"></div>'


def total_front():
    """Front of «Обзор»: no «показать по валютам» any more; the card is one calm number."""
    return ('<div style="font-size: 13px;">Общий баланс</div>'
            '<div style="margin-top: 6px; %s font-size: 34px; font-weight: 700; letter-spacing: -0.5px; color: %s;">12 480,00 <span style="font-size: 18px;">USD</span></div>'
            '<div style="margin-top: 4px; %s font-size: 13px;">≈ 15 974 400 ARS · 4 836 000 AMD</div>'
            '<div style="margin-top: 4px; font-size: 12px; color: %s;">Курс на 16 ноября</div>'
            '<div style="margin-top: 10px; font-size: 14px; font-weight: 600;">Все счета ›</div>' % (MONO, LIME, MONO, HERO_HINT))


CUR = [('USD', '4 120,00'), ('ARS', '3 902 000,00'), ('AMD', '1 240 000,00'), ('RUB', '35 000,00')]


def currencies(title='По валютам счетов'):
    return ('<div style="font-size: 12px; color: %s; margin-bottom: 2px;">%s</div>' % (HERO_HINT, title)
            + ''.join(rowline(c, '%s' % v) for c, v in CUR))


def back_month():
    """А «Месяц»: the one question of a newcomer — «в плюсе или в минусе?» — then the per-day pace and currencies."""
    head = '<div style="font-size: 13px;">Ноябрь · итоги</div>'
    trio = ('<div style="margin-top: 10px; display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); gap: 8px;">%s%s%s</div>'
            % (kv('Доходы', '+2 100', 'USD', OK), kv('Расходы', '−1 240', 'USD', PINK), kv('Разница', '+860', 'USD', LIME)))
    pace = '<div style="margin-top: 10px; font-size: 13px; color: %s;">В среднем ≈ 64 USD в день · прошло 8 дней</div>' % HERO_HINT
    return head + trio + pace + hairline() + currencies()


def back_currencies():
    """Б «Валюты»: the old breakdown first (what the tap used to show), the month in one line below."""
    return ('<div style="font-size: 13px;">Деньги по валютам</div><div style="margin-top: 6px;">%s</div>' % ''.join(rowline(c, v) for c, v in CUR)
            + hairline() + rowline('Курс на 16 ноября', 'currency-api')
            + rowline('Ноябрь', '+860 USD', LIME))


def back_trend():
    """В «Динамика»: the total by month — six columns, the current one is the accent."""
    vals = [8200, 9100, 9800, 10900, 11620, 12480]
    mons = ['июн', 'июл', 'авг', 'сен', 'окт', 'ноя']
    bars = ''.join('<div style="flex: 1; display: flex; flex-direction: column; align-items: center; justify-content: flex-end; gap: 4px;">'
                   '<div style="width: 100%%; max-width: 28px; height: %dpx; border-radius: 8px; background: %s;"></div>'
                   '<span style="font-size: 11px; color: %s;">%s</span></div>' % (round(v / 12480 * 80), LIME if i == 5 else 'rgba(184,255,46,0.35)', HERO_HINT, m)
                   for i, (v, m) in enumerate(zip(vals, mons)))
    return ('<div style="font-size: 13px;">Итог за полгода</div><div style="margin-top: 10px; display: flex; align-items: flex-end; gap: 8px; height: 112px;">%s</div>' % bars
            + '<div style="margin-top: 8px; font-size: 13px; color: %s;">С июня больше на 4 280 USD</div>' % HERO_HINT + hairline() + currencies('По валютам'))


def home(front=True, back=None, tilt=0):
    card_inner = total_front() if front else back()
    body = hero(card_inner, tilt=tilt) + tiles(tile('Траты за ноябрь', '1 240,50', 'USD'), tile('Без категории', '14', 'операций')) + recent_card(RECENT[:2])
    return tab_screen('Обзор', body, 'home', icon_button('search', 'Поиск', INK, True))


def home_front():
    return board('Обзор: лицо', home())


def home_mid():
    return board('Обзор: переворот', home(tilt=-55))


def home_back_a():
    return board('Обзор: оборот А', home(False, back_month))


def home_back_b():
    return board('Обзор: оборот Б', home(False, back_currencies))


def home_back_v():
    return board('Обзор: оборот В', home(False, back_trend))


# ---- Бюджеты ----
TOP = [('Кафе', '95 000', '80 000', 'over', 'перерасход 15 000'), ('Дом', '310 000', '400 000', 'fast', 'осталось 90 000'),
       ('Продукты', '130 000', '350 000', 'ok', 'осталось 220 000')]


def bud_front():
    from mockup_screens_budgets import summary
    return summary()


def mini_env(name, spent, limit, state, cap):
    frac = min(float(spent.replace(' ', '')) / float(limit.replace(' ', '')), 1)
    return ('<div style="padding: 4px 0;"><div style="display: flex; justify-content: space-between; font-size: 13px;"><span>%s</span>'
            '<span style="color: %s;">%s</span></div>%s</div>' % (name, HERO_HINT, cap, plan_bullet(frac, state, h=6, mt=4)))


def bud_back_a():
    """А «Куда смотреть»: how much a day is left, then the three envelopes closest to the limit."""
    head = ('<div style="font-size: 13px; color: %s;">Можно тратить в день</div><div style="%s font-size: 26px; font-weight: 700; color: %s;">≈ 28 <span style="font-size: 14px;">USD</span></div>'
            '<div style="font-size: 12px; color: %s;">до конца месяца · ориентир, а не запрет</div>' % (HERO_HINT, MONO, LIME, HERO_HINT))
    return head + hairline() + '<div style="font-size: 12px; color: %s;">Ближе всего к лимиту</div>' % HERO_HINT + ''.join(mini_env(*t) for t in TOP)


def bud_back_b():
    """Б «Темп»: where we are against the norm, last month for context, envelopes nobody touched."""
    pace = ('<div style="font-size: 13px; color: %s;">Темп месяца</div>%s<div style="margin-top: 6px; font-size: 13px;">Траты идут чуть быстрее нормы: на 1 день вперёд</div>'
            % (HERO_HINT, plan_bullet(640 / 1180, 'fast', h=10, mt=8)))
    return (pace + hairline() + rowline('Прошлый месяц', '1 090 из 1 180 USD') + rowline('Ещё не тратили', 'Подарки, Одежда')
            + rowline('Лимитов в плане', '6'))


def bud_back_v():
    """В «Список»: five envelopes as slim bars, nothing else — the quickest scan."""
    five = TOP + [('Транспорт', '18 000', '60 000', 'ok', 'осталось 42 000'), ('Образование', '120', '160', 'ok', 'осталось 40')]
    return '<div style="font-size: 13px; margin-bottom: 4px;">Все конверты</div>' + ''.join(mini_env(*t) for t in five)


def budgets(front=True, back=None, tilt=0):
    from mockup_screens_budgets import gear
    from mockup_parts import fab
    inner = bud_front() if front else back()
    t = ('transform: rotateX(%ddeg);' % tilt) if tilt else ''
    card_html = ('<div style="margin-top: 4px; %s"><div style="position: relative; %s">%s%s</div></div>'
                 % (DEPTH, t, hero_card(inner, pad='16px', radius=24), flip_button()))
    body = month_switch() + card_html + label('Конверты · 6', top=16) + card(''.join(
        plan_env(i) for i in range(3)))
    return inner_screen('Бюджеты', body, gear()) + fab('Добавить лимит')


def plan_env(i):
    from mockup_screens_budgets import ENV
    n, s, l, c, st, cap, sub = ENV[i]
    return env_row(n, s, l, c, st, cap, sub, '', i > 0)


def bud_front_board():
    return board('Бюджеты: лицо', budgets())


def bud_mid():
    return board('Бюджеты: переворот', budgets(tilt=-55))


def bud_a():
    return board('Бюджеты: оборот А', budgets(False, bud_back_a))


def bud_b():
    return board('Бюджеты: оборот Б', budgets(False, bud_back_b))


def bud_v():
    return board('Бюджеты: оборот В', budgets(False, bud_back_v))


BOARDS = [  # name, title, fn, issue number
    ('Flip519HomeFront', '#519 · «Обзор»: лицо (без «показать по валютам»), «↻» в углу', home_front, 519),
    ('Flip519HomeMid', '#519 · «Обзор»: карточка в момент переворота (≈ 55°) (≈ 55°, rotationX)', home_mid, 519),
    ('Flip519HomeA', '#519 · «Обзор», оборот А: итоги месяца + валюты', home_back_a, 519),
    ('Flip519HomeB', '#519 · «Обзор», оборот Б: валюты и курс', home_back_b, 519),
    ('Flip519HomeV', '#519 · «Обзор», оборот В: итог за полгода', home_back_v, 519),
    ('Flip519BudFront', '#519 · «Бюджеты»: лицо', bud_front_board, 519),
    ('Flip519BudMid', '#519 · «Бюджеты»: момент переворота (≈ 55°)', bud_mid, 519),
    ('Flip519BudA', '#519 · «Бюджеты», оборот А: «Куда смотреть»', bud_a, 519),
    ('Flip519BudB', '#519 · «Бюджеты», оборот Б: темп и прошлый месяц', bud_b, 519),
    ('Flip519BudV', '#519 · «Бюджеты», оборот В: все конверты', bud_v, 519),
]
