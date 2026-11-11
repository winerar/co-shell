"""Generated boards: budgets (#103, answers in #139), «Предложения» sheet. Variant А — envelope list with bullet bars,
Б — «горит» first, envelopes as tiles. Shared: limit editor, envelope detail, «Обзор» tile, first run from past months.

ROW — (key, title, sticky text); BOARDS — (name, title, fn, key).
"""
from mockup_plan_bricks import *  # noqa: F401,F403
from mockup_screens_main import balance_hero, tile, tiles

ROW = ('bud', 'Бюджеты · отзыв #103',
       'Лимит — на папку («Дом») или категорию, в своей валюте; траты в других валютах — по курсу на день операции. '
       'Полоса — сколько потрачено, тонкая черта — где «должны быть» сегодня (11 из 30 дней). Цвет: лайм — в плане, '
       'оранжевый — обгоняете темп, розовый — перерасход. Месяц — с 1-го (меняется), перенос остатка — выключатель у лимита (#139). '
       'А — список конвертов. Б — сначала «горящие», остальные плитками. Рекомендую А: всё сравнимо на одной шкале.')

ENV = [  # name, spent, limit, cur, state, caption, sub
    ('Дом', '310 000', '400 000', 'ARS', 'fast', 'обгоняете темп · осталось 90 000 на 19 дней', 'папка · 3 категории'),
    ('Кафе', '95 000', '80 000', 'ARS', 'over', 'перерасход 15 000', ''),
    ('Продукты', '130 000', '350 000', 'ARS', 'ok', 'осталось 220 000 · 11 600 в день', ''),
    ('Образование', '120', '160', 'USD', 'ok', 'осталось 40', ''),
    ('Транспорт', '18 000', '60 000', 'ARS', 'ok', 'осталось 42 000', ''),
    ('Подарки', '0', '50', 'USD', 'idle', 'не тратили', ''),
]


def summary(compact=False):
    big = ('<div style="display: flex; align-items: baseline; gap: 8px;"><span style="font-size: 13px; color: %s;">Осталось</span><span style="flex-grow: 1;"></span>'
           '<span style="font-size: 12px; color: %s;">потрачено 640 из 1 180 USD</span></div>'
           '<div style="margin-top: 4px; %s font-size: %dpx; font-weight: 700;">540 <span style="font-size: 16px; color: %s;">USD</span></div>'
           % (MUTED, MUTED, MONO, 26 if compact else 34, MUTED))
    fc = ('<div style="margin-top: 10px; display: flex; align-items: center; gap: 6px; font-size: 12px; color: %s;">%s'
          'К концу месяца при таком темпе ≈ 1 260 USD — на 80 больше плана</div>' % (WARN, svg('rate', WARN, 16)))
    return card(big + bullet(640 / 1180, 'fast', h=10, mt=12) + fc, pad='16px', radius=24, extra='margin-top: 4px;')


def gear():
    return icon_button('gear', 'Настройки бюджета', MUTED) + icon_button('plus', 'Новый лимит')


def budgets_a():
    rows = [env_row(n, s, l, c, st, cap, sub, chevron() if n == 'Дом' else '', i > 0) for i, (n, s, l, c, st, cap, sub) in enumerate(ENV[:5])]
    body = month_switch() + summary() + label('Конверты · 6', top=16) + card(''.join(rows))
    return tab_page('Бюджеты', body, 'budget', actions=gear())


def env_tile(n, s, l, c, st, cap, sub):
    frac = float(s.replace(' ', '')) / float(l.replace(' ', ''))
    left = float(l.replace(' ', '')) - float(s.replace(' ', ''))
    val = ('−' if left < 0 else '') + '{:,.0f}'.format(abs(left)).replace(',', ' ')
    return ('<div style="padding: 12px; border-radius: 20px; background: %s; border: 1px solid %s;"><div style="display: flex; align-items: center; gap: 8px;">%s'
            '<span style="font-size: 14px; font-weight: 600;">%s</span></div><div style="margin-top: 10px; %s font-size: 18px; font-weight: 700; color: %s;">%s '
            '<span style="font-size: 12px; color: %s;">%s</span></div><div style="font-size: 12px; color: %s;">осталось из %s</div>%s</div>'
            % (CARD, LINE, cat_icon(n, 28), n, MONO, PINK if st == 'over' else INK, val, MUTED, c, MUTED, l, bullet(frac, st, h=6, mt=10)))


def budgets_b():
    hot = card(env_row(*ENV[1][:6], div=False) + env_row(*ENV[0][:6]), extra='border-color: %s;' % WARNLINE)
    grid = ('<div style="display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 10px;">%s</div>'
            % ''.join(env_tile(*e) for e in ENV[2:6]))
    line = ('<div style="display: flex; align-items: baseline; gap: 8px; margin-top: 2px;"><span style="%s font-size: 22px; font-weight: 700;">540 USD</span>'
            '<span style="font-size: 13px; color: %s;">осталось из 1 180 · 19 дней</span></div>' % (MONO, MUTED))
    body = month_switch() + line + label('Горит · 2', top=12) + hot + label('В плане · 4', top=14) + grid
    return tab_page('Бюджеты', body, 'budget', actions=gear())


def budget_edit():
    inside = group([row('Аренда', 'без своего лимита', cat_icon('Аренда', 32), '<span style="color: %s; font-size: 13px;">＋ лимит</span>' % LIME, min_h=52),
                    row('Коммуналка', 'лимит 70 000 ARS', cat_icon('Коммуналка', 32), chevron(), min_h=52),
                    row('Связь', 'без своего лимита', cat_icon('Связь', 32), '<span style="color: %s; font-size: 13px;">＋ лимит</span>' % LIME, min_h=52)])
    body = (segmented(['Папка', 'Категория'], 'Папка') + field('Что', 'Дом · Аренда, Коммуналка, Связь', trail=chevron())
            + field('В месяц', '400 000', 'В среднем за 3 месяца — 372 000 ARS', mono=True, trail=chip('ARS ▾'))
            + label('Внутри папки · необязательно', top=14) + inside
            + '<div style="margin-top: 10px;">%s</div>' % group([toggle_row('Переносить остаток', 'недотрата/перерасход → в следующий месяц', False)])
            + field('Заметка для себя', 'Аренда 280 000 до 5-го, свет и газ ~60 000') + button('Сохранить', mt=16))
    return inner_screen('Лимит', body)


def envelope_detail():
    from mockup_screens_main import tx
    top = card('<div style="display: flex; align-items: baseline; justify-content: space-between;"><span style="%s font-size: 26px; font-weight: 700;">90 000 '
               '<span style="font-size: 14px; color: %s;">ARS</span></span><span style="font-size: 12px; color: %s;">осталось · 19 дней</span></div>'
               % (MONO, MUTED, MUTED) + bullet(310 / 400, 'fast', h=10, mt=12)
               + caption('Потрачено 310 000 из 400 000 · к сегодня должно быть ≈ 147 000', 'fast'), pad='16px', radius=20)
    cats = card(env_row('Аренда', '280 000', '280 000', 'ARS', 'ok', 'оплачено полностью', 'без своего лимита', div=False)
                + env_row('Коммуналка', '30 000', '70 000', 'ARS', 'ok', 'осталось 40 000'))
    nt = message('Аренда 280 000 до 5-го, свет и газ ~60 000', 'info', 'Заметка')
    ops = card(tx('Аренда', 'Наличные · Миша', '−280 000', 'ARS', 'e', 'house', CYAN) + tx('Свет', 'Galicia · Аня', '−30 000', 'ARS', 'e', 'bolt', CYAN))
    body = top + nt + label('Категории') + cats + label('Операции за ноябрь') + ops
    return inner_screen('Дом · ноябрь', body, icon_button('note', 'Изменить лимит', MUTED))


def home_budget():
    bt = ('<div style="padding: 14px; border-radius: 20px; background: %s; border: 1px solid %s;"><div style="font-size: 12px; color: %s;">Бюджет · 19 дней</div>'
          '<div style="margin-top: 6px; %s font-size: 18px; font-weight: 700;">540 <span style="font-size: 12px; color: %s;">USD</span></div>%s</div>'
          % (CARD, WARNLINE, WARN, MONO, MUTED, bullet(640 / 1180, 'fast', h=6, mt=8)))
    hot = card(env_row(*ENV[1][:6], div=False) + env_row(*ENV[0][:6]))
    body = balance_hero() + tiles(tile('Траты за ноябрь', '760,40', 'USD'), bt) + label('Горит в бюджете') + hot
    return tab_page('Обзор', body, 'home', actions=icon_button('search', 'Поиск'))


def first_run():
    sug = [('Дом', '372 000 ARS', True), ('Продукты', '340 000 ARS', True), ('Кафе', '78 000 ARS', True), ('Транспорт', '55 000 ARS', True),
           ('Образование', '160 USD', True), ('Подарки', '38 USD', False)]
    rows = group([row(n, 'в среднем за 3 мес.', cat_icon(n, 34), '<span style="display: flex; align-items: center; gap: 10px;">%s'
                      '<span style="width: 24px; height: 24px; border-radius: 7px; %s display: flex; align-items: center; justify-content: center;">%s</span></span>'
                      % (amount(a, '', INK, 14), 'background: %s;' % LIME if on else 'border: 2px solid %s;' % LINE2, svg('check', ONLIME, 16, 2.6) if on else ''),
                      min_h=54) for n, a, on in sug])
    body = (hero('budget', 'Бюджет на ноябрь', 'Начнём с ваших трат за 3 месяца. Отметьте нужное — суммы поправите потом.', top=4)
            + label('Предлагаем лимиты', top=16) + rows + button('Создать 5 лимитов', mt=14)
            + note('Месяц считается с 1-го числа — можно поменять в настройках бюджета.'))
    return inner_screen('', body)


BOARDS = [  # name, title, fn, row key
    ('BudA', 'А · Бюджеты: список конвертов (рекомендую)', budgets_a, 'bud'),
    ('BudB', 'Б · Бюджеты: сначала «горит», плитки', budgets_b, 'bud'),
    ('BudDetail', 'Конверт «Дом»: категории, заметка, операции', envelope_detail, 'bud'),
    ('BudEdit', 'Лимит на папку: сумма, подлимиты, перенос, заметка', budget_edit, 'bud'),
    ('BudHome', '«Обзор»: плитка бюджета и «горит»', home_budget, 'bud'),
    ('BudFirst', 'Первый запуск: лимиты из прошлых месяцев', first_run, 'bud'),
]
