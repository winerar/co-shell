"""Generated boards: reports and charts (#140) — owner picked А (2026-11-11, #166): one scrolling feed of cards.
Boards: feed, all categories (long list), folder drill-down, period sheet.
Chart choice follows docs/research/reports-and-charts.md (bars from zero, ≤ 6 + «Другое», labels on the chart).

ROW — (key, row title, sticky text) for the «Экраны» page; BOARDS — (name, title, fn, key).
"""
from mockup_plan_bricks import *  # noqa: F401,F403

ROW = ('rep', 'Отчёты',
       'Выбран А (2026-11-11, #166): лента карточек. Одна валюта на графике (основная, меняется), пересчёт по курсу на день операции. '
       'Тап по полосе или столбцу — операции, из которых он сложился; «Другое» — все категории списком. Серая черта — прошлый месяц.')

STRUCT = [('Дом', 290, 38, 300), ('Продукты', 121, 16, 180), ('Образование', 120, 16, 160), ('Кафе', 89, 12, 110),
          ('Аптека', 70, 9, 40)]  # name, USD, %, last month USD; + «Другое» 70 (9%) = 760 USD
MONTHS = [('июн', 1180), ('июл', 1320), ('авг', 1090), ('сен', 1410), ('окт', 1350), ('ноя', 760)]
REP_TABS = (('home', 'home', 'Обзор'), ('ops', 'list', 'Операции'), ('rep', 'chart', 'Отчёты'), ('more', 'more', 'Ещё'))


def hbar(name, usd, pct, last, top=300):
    w, g = round(usd / top * 100), round(last / top * 100)
    return ('<div style="display: flex; align-items: center; gap: 10px; padding: 7px 0;">%s<div style="flex-grow: 1; min-width: 0;">'
            '<div style="display: flex; justify-content: space-between; font-size: 13px;"><span>%s</span><span style="%s">%d <span style="color: %s;">· %d%%</span></span></div>'
            '<div style="position: relative; margin-top: 5px; height: 10px;"><span style="position: absolute; left: 0; top: 0; bottom: 0; width: %d%%; border-radius: 5px; background: %s;"></span>'
            '<span style="position: absolute; left: %d%%; top: -3px; width: 2px; height: 16px; background: %s;"></span></div></div></div>'
            % (cat_icon(name, 30), name, MONO, usd, MUTED, pct, w, CAT[name][1], g, MUTED2))


def columns(data, h=120, hi=-1, avg=None):
    top = max(v for _, v in data)
    cols = ''
    for i, (m, v) in enumerate(data):
        on = i == (hi if hi >= 0 else len(data) - 1)
        cols += ('<div style="display: flex; flex-direction: column; align-items: center; justify-content: flex-end; gap: 4px;">'
                 '<span style="%s font-size: 10px; color: %s;">%s</span><span style="width: 70%%; height: %dpx; border-radius: 6px 6px 2px 2px; background: %s;"></span>'
                 '<span style="font-size: 11px; color: %s;">%s</span></div>'
                 % (MONO, INK if on else MUTED, '{:,}'.format(v).replace(',', ' '), round(v / top * h), LIME if on else LINE2, INK if on else MUTED, m))
    line = ('<span style="position: absolute; left: 0; right: 0; bottom: %dpx; border-top: 1px dashed %s;"></span>' % (round(avg / top * h) + 20, MUTED2)
            if avg else '')
    return ('<div style="position: relative; margin-top: 12px; display: grid; grid-template-columns: repeat(%d, minmax(0, 1fr)); align-items: end; height: %dpx;">%s%s</div>'
            % (len(data), h + 40, line, cols))


def chart_card(title, sub, inner, extra='margin-top: 10px;'):
    return card('<div style="display: flex; align-items: baseline; gap: 8px;"><span style="font-size: 15px; font-weight: 600; flex-grow: 1;">%s</span>'
                '<span style="font-size: 12px; color: %s;">%s</span></div>%s' % (title, MUTED, sub, inner), pad='14px 14px 10px', radius=20, extra=extra)


def total_line(value, sub, color=INK):
    return ('<div style="margin-top: 4px; display: flex; align-items: baseline; gap: 8px;"><span style="%s font-size: 24px; font-weight: 700; color: %s;">%s '
            '<span style="font-size: 14px; color: %s;">USD</span></span><span style="font-size: 12px; color: %s;">%s</span></div>' % (MONO, color, value, MUTED, OK, sub))


def reports_a():
    struct = chart_card('На что ушли деньги', 'черта — октябрь', total_line('760', 'за 11 дней · в октябре к 11-му было 820')
                        + ''.join(hbar(*s) for s in STRUCT) + '<div style="font-size: 12px; color: %s; padding: 4px 0 2px 40px;">Другое · Транспорт и ещё 3 · 70 · 9%%</div>' % MUTED)
    months = chart_card('По месяцам', 'расходы', columns(MONTHS, 90), 'margin-top: 10px;')
    body = month_switch('Ноябрь 2026', 'USD') + struct + months
    return tab_page('Отчёты', body, 'rep', REP_TABS)


def drill():
    from mockup_screens_main import tx
    bars = chart_card('Дом за ноябрь', '290 USD · 38% трат', ''.join(
        hbar(n, u, p, l, 270) for n, u, p, l in [('Аренда', 262, 90, 262), ('Коммуналка', 28, 10, 38)]), 'margin-top: 4px;')
    ops = card(day('5 НОЯБРЯ', '−280 000', 'ARS') + tx('Аренда', 'Наличные · Миша', '−280 000', 'ARS', 'e', 'house', CYAN)
               + day('3 НОЯБРЯ', '−30 000', 'ARS') + tx('Свет', 'Galicia · Аня', '−30 000', 'ARS', 'e', 'bolt', CYAN))
    body = bars + label('Из чего сложилось · 2 операции') + ops + note('Суммы в USD — по курсу на день операции.')
    return inner_screen('Дом', body)


def period_sheet():
    months = ''.join('<span style="height: 40px; border-radius: 12px; display: flex; align-items: center; justify-content: center; font-size: 14px; %s">%s</span>'
                     % ('background: %s; color: %s; font-weight: 600;' % (LIME, ONLIME) if m == 'ноя' else 'background: %s; color: %s;' % (CHIP, INK2), m)
                     for m in ['янв', 'фев', 'мар', 'апр', 'май', 'июн', 'июл', 'авг', 'сен', 'окт', 'ноя', 'дек'])
    grid = '<div style="margin-top: 12px; display: grid; grid-template-columns: repeat(4, minmax(0, 1fr)); gap: 8px;">%s</div>' % months
    cur = '<div style="display: flex; gap: 8px; flex-wrap: wrap;">%s</div>' % ''.join(chip(c, c == 'USD') for c in ['USD', 'ARS', 'AMD', 'RUB'])
    inner = ('<h2 style="margin: 4px 4px 12px; font-size: 20px;">Период и валюта</h2>' + segmented(['Месяц', 'Квартал', 'Год', 'Свой'], 'Месяц')
             + grid + label('Валюта отчёта') + cur + '<div style="margin-top: 12px;">%s</div>' % group([toggle_row('Сравнивать с прошлым периодом', 'серая черта у полос', True)])
             + button('Показать', mt=16))
    return sheet_page(inner, 260, reports_a())


ALL = [('Дом', 290, 38, 300), ('Продукты', 121, 16, 180), ('Образование', 120, 16, 160), ('Кафе', 89, 12, 110), ('Аптека', 70, 9, 40),
       ('Подписки', 21, 3, 21), ('Такси', 14, 2, 20), ('Спорт', 12, 2, 12), ('Красота', 8, 1, 0), ('Транспорт', 8, 1, 15), ('Доставка', 4, 0, 9),
       ('Книги', 3, 0, 0)]


def all_cats():
    rows = ''.join('<div style="padding: 0 14px; %s">%s</div>' % ('border-top: 1px solid %s;' % DIV if i else '', hbar(*c)) for i, c in enumerate(ALL))
    sort = '<div style="display: flex; gap: 8px; margin-top: 12px;">%s</div>' % ''.join(chip(t, t == 'По сумме') for t in ['По сумме', 'По росту', 'Папками'])
    body = search('Найти категорию · 24') + sort + '<div style="margin-top: 12px;">%s</div>' % card(rows)
    return inner_screen('Все категории · ноябрь', body)


BOARDS = [  # name, title, fn, row key
    ('RepA', 'Отчёты: лента карточек (выбран А)', reports_a, 'rep'),
    ('RepAll', 'Тап по «Другое»: все 24 категории', all_cats, 'rep'),
    ('RepDrill', 'Тап по «Дом»: из чего сложилось', drill, 'rep'),
    ('RepPeriod', 'Период и валюта — шторка', period_sheet, 'rep'),
]
