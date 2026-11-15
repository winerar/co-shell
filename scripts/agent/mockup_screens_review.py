"""Generated boards: «Итог месяца» (#236, calculation #235, research reports-set.md П1) — variants for the owner's poll.
Where it shows up: А — card on «Обзор» the first 7 days of the month (recommended), Б — first card of the «Отчёты» feed,
В — the screen opens by itself once a month. Common: the review screen, the long limits list, edge cases.
Data — October 2026 (shown on 2 November): spent 1 350, came 2 100 USD, usual = average of 3 months before.

ROW — (key, row title, sticky text) for the «Экраны» page; BOARDS — (name, title, fn, key).
"""
from mockup_plan_bricks import *  # noqa: F401,F403
from mockup_screens_main import balance_hero, recent_card, tile, tiles, RECENT
from mockup_screens_reports import REP_TABS, STRUCT, chart_card, hbar, total_line

ROW = ('mon', 'Итог месяца',
       'Макет #236 — ждёт выбора владельца. Итог прошлого месяца: потрачено / пришло / остаток и норма сбережений, '
       '3 главных изменения к среднему за 3 месяца (без мелочи: меньше 5% обычных трат не показываем), лимиты: уложились / перерасход. '
       'Тап по изменению — «Из чего сложилось» (#188). Суммы — по курсу на день операции.')

# name, this month, usual, difference, percent — sorted by |difference| (as `usualChanges`)
CHANGES = [('Подарки', '120', '30', '+90', '+300%', True), ('Дом', '290', '365', '−75', '−21%', False),
           ('Кафе', '180', '110', '+70', '+64%', True)]
OVER = [('Кафе', '95 000', '80 000', 'ARS', 'перерасход 15 000'), ('Подарки', '120', '50', 'USD', 'перерасход 70')]


def money(value, size=15, color=INK, cur='USD'):
    return '<span style="%s font-size: %dpx; font-weight: 700; color: %s; white-space: nowrap;">%s <span style="font-size: %dpx; color: %s;">%s</span></span>' % (
        MONO, size, color, value, max(11, size // 2), HERO_HINT, cur)


def review_hero(month='октябрь', saved='750', rate='отложили 36% дохода', spent='1 350', came='2 100', color=LIME):
    pair = ''.join('<div style="flex: 1;"><div style="font-size: 12px; color: %s;">%s</div><div style="margin-top: 2px;">%s</div></div>'
                   % (HERO_HINT, t, money(v, 16)) for t, v in [('Потрачено', spent), ('Пришло', came)])
    return hero_card('<div style="font-size: 13px;">Остаток за %s</div>'
                     '<div style="margin-top: 4px; display: flex; flex-wrap: wrap; align-items: baseline; column-gap: 10px;">%s<span style="font-size: 13px; color: %s;">%s</span></div>'
                     '<div style="margin-top: 14px; display: flex; gap: 12px;">%s</div>' % (month, money(saved, 32, color), HERO_HINT, rate, pair),
                     pad='16px', extra='margin-top: 4px;')


def change_row(name, now, usual, diff, pct, up, div=True):
    col = PINK if up else OK
    return ('<div style="display: flex; align-items: center; gap: 12px; padding: 10px 14px; %s">%s<span style="flex-grow: 1; display: flex; flex-direction: column; gap: 2px;">'
            '<span style="font-size: 15px;">%s</span><span style="%s font-size: 12px; color: %s;">%s · обычно %s</span></span>'
            '<span style="display: flex; flex-direction: column; align-items: flex-end; gap: 2px;"><span style="%s font-size: 15px; font-weight: 700; color: %s;">%s</span>'
            '<span style="font-size: 12px; color: %s;">%s</span></span>%s</div>'
            % ('border-top: 1px solid %s;' % DIV if div else '', cat_icon(name, 36), name, MONO, MUTED, now, usual, MONO, col, diff, col, pct, chevron()))


def changes_block(months=3):
    return (label('Главное к среднему за %d мес.' % months) + card(''.join(change_row(*c, div=i > 0) for i, c in enumerate(CHANGES))))


def tally(kept, total):
    """Limits summary: one square per limit — lime kept, pink over."""
    sq = ''.join('<span style="flex: 1; height: 8px; border-radius: 3px; background: %s;"></span>' % (LIME if i < kept else PINK) for i in range(total))
    return ('<div style="padding: 12px 14px;"><div style="display: flex; align-items: baseline; gap: 8px;"><span style="font-size: 15px; flex-grow: 1;">Уложились '
            '<b>%d из %d</b></span><span style="font-size: 12px; color: %s;">перерасход · %d</span></div><div style="margin-top: 10px; display: flex; gap: 3px;">%s</div></div>'
            % (kept, total, PINK, total - kept, sq))


def over_row(name, spent, limit, cur, cap):
    frac = float(spent.replace(' ', '')) / float(limit.replace(' ', ''))
    return ('<div style="display: flex; gap: 10px; align-items: center; padding: 8px 14px; border-top: 1px solid %s;">%s<div style="flex-grow: 1;">'
            '<div style="display: flex; align-items: baseline; gap: 8px;"><span style="flex-grow: 1; font-size: 14px;">%s</span><span style="%s font-size: 12px;">%s'
            '<span style="color: %s;"> / %s %s</span></span></div>%s%s</div></div>'
            % (DIV, cat_icon(name, 28), name, MONO, spent, MUTED, limit, cur, bullet(frac, 'over', None, 5, 6), caption(cap, 'over')))


def limits_block(kept=4, total=6, rows=OVER):
    more = '<div style="padding: 12px 14px; border-top: 1px solid %s; display: flex; align-items: center; font-size: 14px; color: %s;"><span style="flex-grow: 1;">Все лимиты октября</span>%s</div>' % (DIV, LIME, chevron())
    return label('Лимиты') + card(tally(kept, total) + ''.join(over_row(*r) for r in rows) + more)


RATE_NOTE = 'Суммы в USD — по курсу на день операции.'


def review_body():
    return review_hero() + changes_block() + limits_block() + note(RATE_NOTE)


def review_screen():
    return inner_screen('Итог октября', review_body())


def mini_card(close=True):
    """Compact card for «Обзор» / the feed: saved + rate, the three changes in one line, limits — tap opens the screen."""
    x = icon_button('close', 'Скрыть до следующего месяца', MUTED) if close else chevron()
    chg = ' · '.join('<span style="color: %s;">%s %s</span>' % (PINK if up else OK, n, d) for n, _, _, d, _, up in CHANGES)
    return card('<div style="display: flex; align-items: center; margin: -6px -6px 0 0;"><span style="font-size: 15px; font-weight: 600; flex-grow: 1;">Итог октября</span>%s</div>'
                '<div style="display: flex; align-items: baseline; gap: 10px;">%s<span style="font-size: 12px; color: %s;">остаток · 36%% дохода</span></div>'
                '<div style="margin-top: 8px; %s font-size: 12px;">%s</div>'
                '<div style="margin-top: 8px; display: flex; align-items: center; gap: 6px; font-size: 12px; color: %s;">Лимиты: уложились 4 из 6'
                '<span style="flex-grow: 1;"></span><span style="color: %s;">Подробнее ›</span></div>'
                % (x, money('750', 22, LIME), MUTED, MONO, chg, MUTED, LIME), pad='14px', radius=20, extra='margin-top: 10px;')


def var_a_home():
    body = balance_hero() + mini_card() + tiles(tile('Траты за ноябрь', '64', 'USD · 2 дня'), tile('Без категории', '3', 'операции')) + recent_card(RECENT[:2])
    return tab_page('Обзор', body, 'home', REP_TABS, icon_button('search', 'Поиск'))


def var_b_feed():
    struct = chart_card('На что ушли деньги', 'черта — октябрь', total_line('64', 'за 2 дня') + ''.join(hbar(*s) for s in STRUCT[:2]))
    return tab_page('Отчёты', month_switch('Ноябрь 2026', 'USD') + mini_card(False) + struct, 'rep', REP_TABS)


def var_c_auto():
    top = ('<div style="display: flex; align-items: center; gap: 4px; height: 64px; padding: 0 8px 0 16px;"><h1 style="margin: 0; font-size: 22px; '
           'font-weight: 700; flex-grow: 1;">Итог октября</h1>%s</div>' % icon_button('close', 'Закрыть', INK))
    body = (review_hero() + changes_block() + limits_block(rows=OVER[:1]) + button('Понятно', mt=14)
            + note('Откроется сам 1-го числа, один раз. Потом — в «Отчётах» у прошлого месяца.', 8))
    return top + '<div style="padding: 0 16px;">%s</div>' % body


LIMITS18 = [('Кафе', '95 000', '80 000', 'ARS', 'over'), ('Подарки', '120', '50', 'USD', 'over'), ('Подписки', '27 000', '25 000', 'ARS', 'over'),
            ('Дом', '372 000', '400 000', 'ARS', 'ok'), ('Продукты', '338 000', '350 000', 'ARS', 'ok'), ('Образование', '160', '160', 'USD', 'ok'),
            ('Транспорт', '52 000', '60 000', 'ARS', 'ok'), ('Спорт', '45 000', '45 000', 'ARS', 'ok'), ('Аптека', '31 000', '40 000', 'ARS', 'ok'),
            ('Такси', '18 000', '30 000', 'ARS', 'ok'), ('Красота', '22 000', '40 000', 'ARS', 'ok'), ('Одежда', '61 000', '100 000', 'ARS', 'ok'),
            ('Связь', '19 000', '25 000', 'ARS', 'ok'), ('Книги', '20', '40', 'USD', 'ok'), ('Врачи', '0', '50 000', 'ARS', 'idle'),
            ('Питомцы', '12 000', '30 000', 'ARS', 'ok'), ('Доставка', '9 000', '20 000', 'ARS', 'ok'), ('Кино', '6 000', '15 000', 'ARS', 'ok')]


def limit_row(name, spent, limit, cur, state, div=True):
    frac = float(spent.replace(' ', '')) / float(limit.replace(' ', ''))
    s, l = (float(x.replace(' ', '')) for x in (spent, limit))
    cap = ('перерасход %s' % '{:,.0f}'.format(s - l).replace(',', ' ') if state == 'over'
           else 'не тратили' if state == 'idle' else 'в лимите · осталось %s' % '{:,.0f}'.format(l - s).replace(',', ' '))
    return ('<div style="display: flex; gap: 10px; align-items: center; padding: 8px 14px; %s">%s<div style="flex-grow: 1;">'
            '<div style="display: flex; align-items: baseline; gap: 8px;"><span style="flex-grow: 1; font-size: 14px;">%s</span><span style="%s font-size: 12px;">%s'
            '<span style="color: %s;"> / %s %s</span></span></div>%s%s</div></div>'
            % ('border-top: 1px solid %s;' % DIV if div else '', cat_icon(name, 28), name, MONO, spent, MUTED, limit, cur,
               bullet(frac, state, None, 5, 6), caption(cap, state)))


def limits_long():
    flt = '<div style="display: flex; gap: 8px; margin-top: 4px; overflow: hidden;">%s</div>' % ''.join(
        chip(t, t.startswith('Все')) for t in ['Все · 18', 'Перерасход · 3', 'В лимите · 15'])
    head = card(tally(15, 18), extra='margin-top: 12px;')
    rows = card(''.join(limit_row(*r, div=i > 0) for i, r in enumerate(LIMITS18)), extra='margin-top: 12px;')
    return inner_screen('Лимиты октября', flt + head + rows)


def edge_cases():
    first = message('Сравнивать пока не с чем: главные изменения появятся, когда будет хотя бы месяц истории.', 'info', 'Нет истории — вместо изменений')
    no_inc = review_hero('октябрь', '−1 350', 'дохода нет — без %', '1 350', '0', PINK)
    short = label('Месяц истории: «к среднему за 1 мес.»') + card(change_row(*CHANGES[0], div=False))
    no_lim = note('Лимитов в октябре не было — блок «Лимиты» не показываем.', 10)
    miss = message('Без учёта: VES — нет курса на 3 дня операций.', 'warn')
    return inner_screen('Итог октября', no_inc + first + short + no_lim + miss + note(RATE_NOTE))


BOARDS = [  # name, title, fn, row key
    ('Mon236A', 'А (рекомендую): «Обзор» 1–7 числа — карточка «Итог октября»', var_a_home, 'mon'),
    ('Mon236B', 'Б: первая карточка ленты «Отчётов»', var_b_feed, 'mon'),
    ('Mon236C', 'В: экран открывается сам 1-го числа, один раз', var_c_auto, 'mon'),
    ('Mon236Screen', 'Общее: экран «Итог октября» (тап по карточке)', review_screen, 'mon'),
    ('Mon236Limits', 'Общее: «Все лимиты октября» — 18 лимитов', limits_long, 'mon'),
    ('Mon236Edge', 'Общее: нет дохода, мало истории, без лимитов, нет курса', edge_cases, 'mon'),
]
