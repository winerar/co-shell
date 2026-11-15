"""Generated boards: category trend and «выше обычного» (#238, calculation #237, research reports-set.md П1) — poll variants.
Chosen by the owner (poll 2026-11-15, А + А): the year of one category — its own card «По месяцам» with the average
line in «Из чего сложилось» (#188); the mark at a bar of «На что ушли деньги» — a pill next to the amount.
Common: the long list with marks, edge cases.
Data — November 2026 shown on the 11th: the mark compares with what the 3 months before spent by the 11th.

ROW — (key, row title, sticky text) for the «Экраны» page; BOARDS — (name, title, fn, key).
"""
from mockup_plan_bricks import *  # noqa: F401,F403
from mockup_screens_reports import ALL, MONTHS, REP_TABS, STRUCT, chart_card, total_line

ROW = ('trend', 'Тренд категории (#238)',
       'Опрос: где показать год одной категории (А — карточка «По месяцам», Б — полоска в шапке) и как пометить '
       '«выше обычного» (А — пилюля у суммы, Б — подпись под полосой). Пометка — только при заметной разнице: '
       '≥ 20 % и ≥ 5 % обычных трат, к тому же числу прошлых трёх месяцев, не раньше 7-го.')

# Кафе, USD: December 2025 … November 2026 (so far, the 11th); usual by the 11th — 61, average of 12 months — 65.
YEAR = [('д', 70), ('я', 64), ('ф', 58), ('м', 66), ('а', 72), ('м', 60), ('и', 68), ('и', 75), ('а', 58), ('с', 62), ('о', 66), ('н', 89)]
MARKS = {'Кафе': (True, '+45%', 'к 11-му обычно 61'), 'Продукты': (False, '−33%', 'к 11-му обычно 180')}


def year_columns(h=96, avg=65, avg_label='в среднем 65'):
    top = max(v for _, v in YEAR)
    cols = ''
    for i, (m, v) in enumerate(YEAR):
        on = i == len(YEAR) - 1
        val = '<span style="%s font-size: 10px; color: %s;">%s</span>' % (MONO, INK, v) if on else '<span style="font-size: 10px;">&nbsp;</span>'
        cols += ('<div style="display: flex; flex-direction: column; align-items: center; justify-content: flex-end; gap: 4px;">%s'
                 '<span style="width: 64%%; height: %dpx; border-radius: 5px 5px 2px 2px; background: %s;"></span>'
                 '<span style="font-size: 10px; color: %s;">%s</span></div>' % (val, round(v / top * h), LIME if on else LINE2, INK if on else MUTED, m))
    y = round(avg / top * h) + 18
    line = ('<span style="position: absolute; left: 0; right: 0; bottom: %dpx; border-top: 1px dashed %s;"></span>'
            '<span style="position: absolute; right: 0; bottom: %dpx; font-size: 10px; color: %s; background: %s; padding: 0 4px;">%s</span>'
            % (y, MUTED2, y + 2, MUTED, CARD, avg_label))
    return ('<div style="position: relative; margin-top: 12px; display: grid; grid-template-columns: repeat(12, minmax(0, 1fr)); align-items: end; height: %dpx;">%s%s</div>'
            % (h + 36, line, cols))


def pill(text, up):
    color, bg = (PINK, PINKBG) if up else (OK, '#0B1A10')
    arrow = '↑' if up else '↓'
    return ('<span style="display: inline-flex; align-items: center; gap: 2px; height: 20px; padding: 0 7px; border-radius: 999px; background: %s; '
            'color: %s; %s font-size: 11px; font-weight: 600; white-space: nowrap;">%s %s</span>' % (bg, color, MONO, arrow, text.lstrip('+−')))


def mark_line(name):
    up, pct, sub = MARKS[name]
    return ('<div style="margin-top: 8px; display: flex; align-items: center; gap: 8px; font-size: 12px; color: %s;">%s<span>%s · %s</span></div>'
            % (MUTED, pill(pct, up), 'больше обычного' if up else 'меньше обычного', sub))


def cafe_ops():
    from mockup_screens_main import tx
    return card(day('10 НОЯБРЯ', '−38 000', 'ARS') + tx('Кафе', 'Galicia · Аня · Café Martínez', '−38 000', 'ARS', 'e', 'cafe', WARN)
                + day('8 НОЯБРЯ', '−24 500', 'ARS') + tx('Кафе', 'USDT · Миша · Starbucks', '−24 500', 'ARS', 'e', 'cafe', WARN)
                + day('3 НОЯБРЯ', '−31 000', 'ARS') + tx('Кафе', 'Наличные · Миша', '−31 000', 'ARS', 'e', 'cafe', WARN))


def drill_a():
    head = chart_card('Кафе за ноябрь', '89 USD · 12% трат', mark_line('Кафе'), 'margin-top: 4px;')
    trend = chart_card('По месяцам', 'год · черта — среднее', year_columns()
                       + '<div style="margin-top: 6px; display: flex; gap: 16px; font-size: 12px; color: %s;"><span>За 3 мес.: <b style="%s color: %s;">62</b></span>'
                         '<span>За год: <b style="%s color: %s;">65</b></span></div>' % (MUTED, MONO, INK, MONO, INK))
    body = head + trend + label('Из чего сложилось · 3 операции') + cafe_ops() + note('Тап по столбцу — тот месяц. Суммы в USD — по курсу на день операции.')
    return inner_screen('Кафе', body)


def bar_row(name, usd, pct, last, top=300, mode='pill'):
    w, g = round(usd / top * 100), round(last / top * 100)
    m = MARKS.get(name)
    right = '%d <span style="color: %s;">· %d%%</span>' % (usd, MUTED, pct)
    if m and mode == 'pill':
        right = pill(m[1], m[0]) + ' ' + right
    cap = ('<div style="margin-top: 4px; font-size: 11px; color: %s;">%s · %s</div>' % (PINK if m[0] else OK, m[1], m[2])
           if m and mode == 'caption' else '')
    return ('<div style="display: flex; align-items: center; gap: 10px; padding: 7px 0;">%s<div style="flex-grow: 1; min-width: 0;">'
            '<div style="display: flex; justify-content: space-between; align-items: center; gap: 6px; font-size: 13px;"><span>%s</span><span style="%s display: flex; align-items: center; gap: 6px;">%s</span></div>'
            '<div style="position: relative; margin-top: 5px; height: 10px;"><span style="position: absolute; left: 0; top: 0; bottom: 0; width: %d%%; border-radius: 5px; background: %s;"></span>'
            '<span style="position: absolute; left: %d%%; top: -3px; width: 2px; height: 16px; background: %s;"></span></div>%s</div></div>'
            % (cat_icon(name, 30), name, MONO, right, w, CAT[name][1], g, MUTED2, cap))


def reports(mode):
    from mockup_screens_reports import columns
    struct = chart_card('На что ушли деньги', 'черта — октябрь', total_line('760', 'за 11 дней · в октябре к 11-му было 820')
                        + ''.join(bar_row(*s, mode=mode) for s in STRUCT)
                        + '<div style="font-size: 12px; color: %s; padding: 4px 0 2px 40px;">Другое · Транспорт и ещё 3 · 70 · 9%%</div>' % MUTED)
    months = chart_card('По месяцам', 'расходы', columns(MONTHS, 90), 'margin-top: 10px;')
    return tab_page('Отчёты', month_switch('Ноябрь 2026', 'USD') + struct + months, 'rep', REP_TABS)


def reports_a():
    return reports('pill')


def all_marked():
    marks = dict(MARKS, **{'Подписки': (True, '+50%', ''), 'Такси': (False, '−30%', '')})
    rows = ''
    for i, (n, u, p, l) in enumerate(ALL + [('Связь', 2, 0, 3), ('Кино', 2, 0, 0)]):
        if n not in CAT:
            continue
        right = '%d <span style="color: %s;">· %d%%</span>' % (u, MUTED, p)
        if n in marks:
            right = pill(marks[n][1], marks[n][0]) + ' ' + right
        rows += ('<div style="display: flex; align-items: center; gap: 10px; padding: 9px 14px; %s">%s<span style="flex-grow: 1; font-size: 14px;">%s</span>'
                 '<span style="%s font-size: 13px; display: flex; align-items: center; gap: 6px;">%s</span></div>'
                 % ('border-top: 1px solid %s;' % DIV if rows else '', cat_icon(n, 28), n, MONO, right))
    sort = '<div style="display: flex; gap: 8px; margin-top: 12px;">%s</div>' % ''.join(
        chip(t, t == 'По сумме') for t in ['По сумме', 'По росту', 'Необычные · 4', 'Папками'])
    body = search('Найти категорию · 24') + sort + '<div style="margin-top: 12px;">%s</div>' % card(rows)
    return inner_screen('Все категории · ноябрь', body)


def edge_cases():
    early = message('3 ноября: пометок пока нет — до 7-го числа одна покупка ничего не говорит.', 'info', 'Начало месяца')
    rent = card('<div style="padding: 12px 14px; font-size: 13px; line-height: 1.45;">«Дом» 5-го: аренда 262 — <b>без пометки</b>: '
                'к 5-му в прошлые месяцы было столько же. Сравниваем с тем же числом, а не с долей месяца.</div>', extra='margin-top: 10px;')
    new = card('<div style="padding: 10px 14px; display: flex; align-items: center; gap: 8px; font-size: 13px;">%s<span style="flex-grow: 1;">Подарки</span>'
               '<span style="display: inline-flex; height: 20px; padding: 0 7px; align-items: center; border-radius: 999px; background: %s; color: %s; font-size: 11px; font-weight: 600;">новое</span>'
               '<span style="%s">120</span></div>' % (cat_icon('Подарки', 28) if 'Подарки' in CAT else '', PINKBG, PINK, MONO), extra='margin-top: 10px;')
    short = note('Истории меньше трёх месяцев — среднее за сколько есть («за 1 мес.»); нет совсем — столбцы без черты, пометок нет.')
    talk = note('TalkBack: «Кафе, 89 долларов, 12 процентов трат, на 45 процентов больше обычного».')
    return inner_screen('Пометки: особые случаи', early + rent + new + short + talk)


BOARDS = [  # name, title, fn, row key
    ('Tr238DrillA', 'Выбрано (А): «Из чего сложилось» — карточка «По месяцам», черта среднего', drill_a, 'trend'),
    ('Tr238MarkA', 'Выбрано (А): пометка — пилюля «↑ 45%» у суммы', reports_a, 'trend'),
    ('Tr238All', 'Общее: «Все категории» — пометки и фильтр «Необычные»', all_marked, 'trend'),
    ('Tr238Edge', 'Общее: начало месяца, аренда, «новое», TalkBack', edge_cases, 'trend'),
]
