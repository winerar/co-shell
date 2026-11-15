"""Generated boards: the whole picture in «Отчёты» (#189, research reports-set.md П2/П3) — chosen А · Б · А (poll 2026-11-15).
Three new cards in the feed after «По месяцам»: income vs expenses with the leftover (savings rate), net worth over
time (a line), spending by family member. Poll: one question per card; common boards — the long list behind a
net-worth point (24 accounts) and edge cases.
Data — November 2026 shown on the 11th, USD by the day's rate; the month is not over, so its leftover is «пока».

ROW — (key, row title, sticky text) for the «Экраны» page; BOARDS — (name, title, fn, key).
"""
from mockup_plan_bricks import *  # noqa: F401,F403
from mockup_parts import ACCOUNTS, GROUPS, acc_row, group_head
from mockup_screens_reports import REP_TABS, chart_card

ROW = ('picture', 'Картина целиком (#189)',
       'Выбрано (опрос 2026-11-15, А · Б · А): доходы и расходы — парные столбцы + остаток месяца; капитал — линия + '
       '«отложили / курсы»; по участникам — кто внёс. Карточки — в ленте «Отчётов» после «По месяцам»; тап по любому числу — операции.')

# USD, June … November (November so far, the 11th). September is a minus month.
IE = [('июн', 1650, 1180), ('июл', 1600, 1320), ('авг', 1700, 1090), ('сен', 1300, 1410), ('окт', 1620, 1350), ('ноя', 1580, 760)]
# Net worth at month end, USD: December 2025 … November 2026 (now).
NW = [('д', 6100), ('я', 6150), ('ф', 6230), ('м', 6090), ('а', 6200), ('м', 6280), ('и', 6750), ('и', 6990), ('а', 7580),
      ('с', 7440), ('о', 7700), ('н', 8503)]
NW_WHY = [('с', 0, -110, -30), ('о', 0, 270, -10), ('н', 1, 820, -17)]  # month, partial, saved, rates
WHO = [('Миша', 'М', CYAN, 430, 57, [('Дом', 262), ('Продукты', 70), ('Кафе', 38)]),
       ('Аня', 'А', '#C79BFF', 330, 43, [('Образование', 120), ('Аптека', 70), ('Продукты', 51)])]


def fmt(v, sign=False):
    s = '{:,}'.format(abs(v)).replace(',', ' ')
    return ('+' if v > 0 else '−' if v < 0 else '') + s if sign else ('−' + s if v < 0 else s)


def legend(*items):
    dots = ''.join('<span style="display: inline-flex; align-items: center; gap: 5px;"><span style="width: 9px; height: 9px; border-radius: 3px; '
                   'background: %s;"></span>%s</span>' % (c, t) for c, t in items)
    return '<div style="margin-top: 8px; display: flex; gap: 14px; font-size: 11px; color: %s;">%s</div>' % (MUTED, dots)


def stat_row(cells, mt=10):
    out = ''.join('<div style="display: flex; flex-direction: column; gap: 2px; min-width: 0;"><span style="font-size: 11px; color: %s;">%s</span>'
                  '<span style="%s font-size: 15px; font-weight: 600; color: %s; white-space: nowrap;">%s</span></div>' % (MUTED, t, MONO, c, v)
                  for t, v, c in cells)
    return ('<div style="margin-top: %dpx; padding-top: 10px; border-top: 1px solid %s; display: grid; grid-template-columns: repeat(%d, minmax(0, 1fr)); gap: 8px;">%s</div>'
            % (mt, DIV, len(cells), out))


# ── Q1: income and expenses ──────────────────────────────────────────────────────────────────────────────────────────

def pairs(h=96, hi=4):
    top = max(max(i, e) for _, i, e in IE)
    cols = ''
    for k, (m, inc, exp) in enumerate(IE):
        on = k == hi
        a = 1 if on else 0.45
        cols += ('<div style="display: flex; flex-direction: column; align-items: center; gap: 4px; %s">'
                 '<div style="display: flex; align-items: flex-end; gap: 3px; height: %dpx;">'
                 '<span style="width: 14px; height: %dpx; border-radius: 4px 4px 1px 1px; background: %s; opacity: %s;"></span>'
                 '<span style="width: 14px; height: %dpx; border-radius: 4px 4px 1px 1px; background: %s; opacity: %s;"></span></div>'
                 '<span style="font-size: 11px; color: %s;">%s</span></div>'
                 % ('border-radius: 10px; background: %s; padding: 4px 0;' % CHIP if on else 'padding: 4px 0;', h,
                    round(inc / top * h), CYAN, a, round(exp / top * h), PINK, a, INK if on else MUTED, m))
    return '<div style="margin-top: 10px; display: grid; grid-template-columns: repeat(6, minmax(0, 1fr)); gap: 2px;">%s</div>' % cols


def ie_a():
    inner = (pairs() + legend((CYAN, 'пришло'), (PINK, 'ушло'))
             + stat_row([('Октябрь · пришло', '1 620', CYAN), ('ушло', '1 350', PINK), ('остаток', '+270', OK)])
             + '<div style="margin-top: 8px; font-size: 12px; color: %s;">Отложили <b style="%s color: %s;">17%%</b> дохода · в среднем за 5 мес. — 19%%</div>' % (MUTED, MONO, INK))
    return chart_card('Доходы и расходы', 'тап — месяц', inner)


def feed(card_html, title='Отчёты'):
    from mockup_screens_reports import columns, MONTHS
    months = chart_card('По месяцам', 'расходы', columns(MONTHS, 50), 'margin-top: 4px;')
    return tab_page(title, month_switch('Ноябрь 2026', 'USD') + months + card_html, 'rep', REP_TABS)


def board_ie_a():
    return feed(ie_a())


# ── Q2: net worth ────────────────────────────────────────────────────────────────────────────────────────────────────

def line(w=330, h=110, sel=11):
    lo, hi = min(v for _, v in NW), max(v for _, v in NW)
    lo, hi = lo - (hi - lo) * 0.15, hi + (hi - lo) * 0.1
    xs = [round(8 + i * (w - 16) / (len(NW) - 1), 1) for i in range(len(NW))]
    ys = [round(h - (v - lo) / (hi - lo) * h, 1) for _, v in NW]
    pts = ' '.join('%s,%s' % p for p in zip(xs, ys))
    area = 'M%s,%s ' % (xs[0], h) + ' '.join('L%s,%s' % p for p in zip(xs, ys)) + ' L%s,%s Z' % (xs[-1], h)
    grid = ''.join('<line x1="0" x2="%d" y1="%d" y2="%d" stroke="%s" stroke-width="1" stroke-dasharray="2 4"/>' % (w, y, y, LINE2) for y in (h // 3, 2 * h // 3))
    dot = ('<line x1="%s" x2="%s" y1="0" y2="%d" stroke="%s" stroke-width="1"/><circle cx="%s" cy="%s" r="5" fill="%s" stroke="%s" stroke-width="2"/>'
           % (xs[sel], xs[sel], h, MUTED2, xs[sel], ys[sel], LIME, CARD))
    svg_ = ('<svg width="%d" height="%d" viewBox="0 0 %d %d" style="display: block; margin-top: 12px; overflow: visible;">%s'
            '<path d="%s" fill="%s" fill-opacity="0.10"/><polyline points="%s" fill="none" stroke="%s" stroke-width="2.5" stroke-linejoin="round" stroke-linecap="round"/>%s</svg>'
            % (w, h, w, h, grid, area, LIME, pts, LIME, dot))
    months = ''.join('<span style="font-size: 10px; color: %s; text-align: center;">%s</span>' % (INK if i == sel else MUTED, m) for i, (m, _) in enumerate(NW))
    return svg_ + '<div style="margin-top: 6px; display: grid; grid-template-columns: repeat(12, minmax(0, 1fr));">%s</div>' % months


def nw_head(value='8 503', sub='+2 403 за год · +39%'):
    return ('<div style="margin-top: 4px; display: flex; align-items: baseline; gap: 8px;"><span style="%s font-size: 24px; font-weight: 700;">%s '
            '<span style="font-size: 14px; color: %s;">USD</span></span><span style="font-size: 12px; color: %s;">%s</span></div>' % (MONO, value, MUTED, OK, sub))


def why_rows():
    out = ''
    for k, (m, part, saved, rates) in enumerate(reversed(NW_WHY)):
        name = {'с': 'Сентябрь', 'о': 'Октябрь', 'н': 'Ноябрь'}[m]
        out += ('<div style="display: flex; align-items: center; gap: 8px; padding: 7px 0; font-size: 13px; %s"><span style="flex-grow: 1; color: %s;">%s%s</span>'
                '<span style="%s width: 64px; text-align: right; color: %s;">%s</span><span style="%s width: 52px; text-align: right; color: %s;">%s</span>'
                '<span style="%s width: 58px; text-align: right; font-weight: 600; color: %s;">%s</span></div>'
                % ('border-top: 1px solid %s;' % DIV if k else '', INK2, name, ' <span style="font-size: 10px; color: %s;">пока</span>' % MUTED if part else '',
                   MONO, OK if saved >= 0 else PINK, fmt(saved, True), MONO, MUTED, fmt(rates, True), MONO, OK if saved + rates >= 0 else PINK, fmt(saved + rates, True)))
    head = ('<div style="margin-top: 10px; padding-top: 8px; border-top: 1px solid %s; display: flex; gap: 8px; font-size: 11px; color: %s;"><span style="flex-grow: 1;">Почему изменился</span>'
            '<span style="width: 64px; text-align: right;">отложили</span><span style="width: 52px; text-align: right;">курсы</span><span style="width: 58px; text-align: right;">итого</span></div>' % (DIV, MUTED))
    return head + out


def nw_b():
    return chart_card('Капитал', 'все счета', nw_head() + line(h=92) + why_rows())


def board_nw_b():
    return feed(nw_b())


# ── Q3: by family member ─────────────────────────────────────────────────────────────────────────────────────────────

def who_bar(name, ini, color, usd, pct, sub, top=430):
    return ('<div style="display: flex; align-items: center; gap: 10px; padding: 8px 0;">%s<div style="flex-grow: 1; min-width: 0;">'
            '<div style="display: flex; justify-content: space-between; font-size: 14px;"><span>%s</span><span style="%s">%d <span style="color: %s;">· %d%%</span></span></div>'
            '<div style="margin-top: 5px; height: 10px; width: %d%%; border-radius: 5px; background: %s;"></div>'
            '<div style="margin-top: 5px; font-size: 11px; color: %s; white-space: nowrap; overflow: hidden; text-overflow: ellipsis;">%s</div></div></div>'
            % (badge(ini, color, size=32, radius=16), name, MONO, usd, MUTED, pct, round(usd / top * 100), color, MUTED, sub))


def who_a():
    rows = ''.join(who_bar(n, i, c, u, p, ' · '.join('%s %d' % t for t in top)) for n, i, c, u, p, top in WHO)
    return chart_card('Кто сколько потратил', 'по тому, кто внёс', '<div style="margin-top: 4px;">%s</div>' % rows
                      + note('Траты общие — делим по тому, кто записал. Тап — его операции.', mt=6))


def board_who_a():
    return feed(who_a())


# ── Common ───────────────────────────────────────────────────────────────────────────────────────────────────────────

EXTRA = [('Ualá', 'UA', '#3A6BFF', '#FFFFFF', 'Карта', '84 000', 'ARS', '82', 'cards'),
         ('BBVA', 'BB', '#004481', '#FFFFFF', 'Карта', '52 300', 'ARS', '51', 'cards'),
         ('Brubank', 'BR', '#6A00F4', '#FFFFFF', 'Карта', '41 000', 'ARS', '40', 'cards'),
         ('Сбер', 'СБ', '#21A038', '#FFFFFF', 'Карта', '6 200', 'RUB', '64', 'cards'),
         ('Wise', 'WI', '#9FE870', '#163300', 'Карта', '214', 'EUR', '231', 'cards'),
         ('OKX', 'OK', '#EEF5E6', '#050805', 'Биржа', '120', 'USDT', '120', 'crypto'),
         ('Евро', '€', '#7DD3FC', '#0B0F0A', 'Наличные', '150', 'EUR', '162', 'cash'),
         ('Рубли', '₽', '#FCA5A5', '#0B0F0A', 'Наличные', '9 000', 'RUB', '93', 'cash'),
         ('Копилка', 'КП', '#FDE68A', '#1A1400', 'Наличные', '45 000', 'ARS', '44', 'cash'),
         ('Вклад ARS', 'ВА', '#FFD43B', '#1A1400', 'Вклад', '600 000', 'ARS', '585', 'deposit'),
         ('Брокер', 'БР', '#A78BFA', '#FFFFFF', 'Вклад', '900', 'USD', '900', 'deposit'),
         ('Долг Лёше', 'ДЛ', '#FF8A5C', '#1A0A00', 'Долг', '−200', 'USD', '−200', 'deposit')]


def nw_drill():
    accs = ACCOUNTS + EXTRA
    head = chart_card('Капитал на 31 октября', 'тап — другой месяц', nw_head('7 700', '+260 за месяц') + line(h=60, sel=10), 'margin-top: 4px;')
    chips = '<div style="display: flex; gap: 8px; margin-top: 12px;">%s</div>' % ''.join(chip(t, t == 'Группами') for t in ['Группами', 'По сумме', 'По изменению'])
    cards = [a for a in accs if a[8] == 'cards']
    body = (head + '<div style="margin-top: 12px;">%s</div>' % search('Найти счёт · %d' % len(accs)) + chips
            + group_head('Карты и банки', len(cards), '3 642') + card(''.join(acc_row(a) for a in cards[:5]))
            + group_head('Крипто', sum(a[8] == 'crypto' for a in accs), '2 430', open_=False)
            + group_head('Наличные', sum(a[8] == 'cash' for a in accs), '1 816', open_=False)
            + group_head('Вклады и долги', sum(a[8] == 'deposit' for a in accs), '2 785', open_=False))
    return inner_screen('Капитал · октябрь', body)


def edge_cases():
    partial = message('Ноябрь ещё идёт: остаток +820 — «пока». Норма сбережений — по закрытым месяцам.', 'info', 'Месяц не закончен')
    minus = card('<div style="padding: 12px 14px; font-size: 13px; line-height: 1.45;">Сентябрь: пришло 1 300, ушло 1 410 — остаток '
                 '<b style="%s color: %s;">−110</b>. Красным, без упрёков: «потратили больше, чем пришло».</div>' % (MONO, PINK), extra='margin-top: 10px;')
    rates = message('Капитал в USD по курсу на конец месяца. Нет курса для AMD — счёт без учёта, строкой «без учёта: AMD».', 'warn', 'Без курса')
    wallet = card('<div style="padding: 12px 14px; font-size: 13px; line-height: 1.45;">Операции из Wallet без автора — строкой '
                  '<b>«Из Wallet · 1 240»</b> под участниками; переводы и «Корректировка баланса» — не траты и не доход.</div>', extra='margin-top: 10px;')
    short = note('История короче двух месяцев — столбцы за сколько есть, без «в среднем». Один участник — карточки «Кто сколько» нет.')
    talk = note('TalkBack: «Октябрь: пришло 1 620, ушло 1 350, остаток плюс 270 долларов, отложили 17 процентов».')
    return inner_screen('Особые случаи', partial + minus + '<div style="margin-top: 10px;">%s</div>' % rates + wallet + short + talk)


BOARDS = [  # name, title, fn, row key
    ('Rp189IeA', 'Выбрано (А): доходы и расходы — парные столбцы + остаток месяца', board_ie_a, 'picture'),
    ('Rp189NwB', 'Выбрано (Б): капитал — линия + «отложили / курсы»', board_nw_b, 'picture'),
    ('Rp189WhoA', 'Выбрано (А): по участникам — кто внёс', board_who_a, 'picture'),
    ('Rp189NwDrill', 'Общее: тап по точке капитала — 24 счёта на конец месяца', nw_drill, 'picture'),
    ('Rp189Edge', 'Общее: месяц не закончен, минус, без курса, Wallet, TalkBack', edge_cases, 'picture'),
]
