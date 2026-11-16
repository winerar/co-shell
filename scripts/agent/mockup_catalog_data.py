"""«Компоненты v3» — existing amounts, charts and tags (#367, part 3 of 3), each as a function of v."""
from mockup_catalog_kit import *  # noqa: F401,F403


def amt(v, text, cur, size=28, color=INK):
    if v == 'A':
        return amount_text(text, cur, size, color)
    return ('<span style="font-size: %dpx; font-weight: 700; color: %s; white-space: nowrap; font-variant-numeric: tabular-nums;"><span style="font-size: %dpx; font-weight: 600; '
            'color: %s; margin-right: 6px;">%s</span>%s</span>' % (size, color, max(11, size // 2), MUTED, cur, text))


def amounts(v):
    return col(amt(v, '1 240,50', 'USD', 34), amt(v, '−42 300', 'ARS', 22, PINK), amt(v, '+512 000', 'AMD', 22, CYAN),
               '<div style="width: 220px; overflow: hidden;">%s</div>' % amt(v, '12 345 678 901,99', 'USDT', 18),
               cap(('моно, код валюты после суммы' if v == 'A' else 'основной шрифт с ровными цифрами, код до суммы') + '; длинная — уменьшается'))


def change(v, text, color):
    if v == 'A':
        return ('<span style="height: 22px; padding: 0 8px; border-radius: 999px; background: %s; color: %s; font-size: 11px; font-weight: 700; display: inline-flex; white-space: nowrap; flex: none; '
                'align-items: center;">%s</span>' % (mix(color, 16), INK, text.replace('↑', '<span style="color: %s;">↑</span>' % color).replace('↓', '<span style="color: %s;">↓</span>' % color)))
    return ('<span style="height: 22px; padding: 0 6px; border-radius: 6px; box-shadow: inset 0 0 0 1px %s; color: %s; font-size: 11px; font-weight: 700; display: inline-flex; white-space: nowrap; flex: none; '
            'align-items: center; gap: 3px;"><span style="width: 6px; height: 6px; border-radius: 1px; background: %s;"></span>%s</span>' % (OUTLINE, INK, color, text))


def changes(v):
    return col(flexrow(change(v, '↑ 45 %', PINK), change(v, '↓ 33 %', OK), change(v, 'новое', CYAN), change(v, '0 %', MUTED)), cap('текст — цветом текста, цвет — в стрелке / метке'))


def bullet(v, frac, tone, mark=None):
    color = {'ok': LIME, 'ahead': WARN, 'over': PINK, 'idle': MUTED}[tone]
    h, rad = (10, 5) if v == 'A' else (6, 1)
    m = ''
    if mark is not None:
        m = ('<span style="position: absolute; left: %d%%; top: -4px; bottom: -4px; width: %dpx; border-radius: 2px; background: %s; box-shadow: 0 0 0 2px %s;"></span>'
             % (round(mark * 100), 3 if v == 'A' else 2, INK, CARD))
    over = ''
    if frac > 1 and v == 'B':
        over = '<span style="position: absolute; right: 0; top: -4px; bottom: -4px; width: 2px; background: %s;"></span>' % INK
    return ('<div style="position: relative; width: 100%%; height: %dpx; border-radius: %dpx; background: %s;"><div style="width: %d%%; height: 100%%; border-radius: %dpx; background: %s;"></div>%s%s</div>'
            % (h, rad, KEY2, round(min(frac, 1) * 100), rad, color, m, over))


def bullets(v):
    return col(labeled(bullet(v, 0.4, 'ok', 0.5), 'в плане · отметка «сегодня»'), labeled(bullet(v, 0.7, 'ahead', 0.5), 'быстрее плана'),
               labeled(bullet(v, 1.2, 'over'), 'перерасход'), labeled(bullet(v, 0, 'idle', 0.5), 'ещё ничего'))


def minibars(v, values, elapsed, hi, color=LIME):
    top = max(values) or 1
    rad = '3px' if v == 'A' else '0'
    bars = ''.join('<span style="flex: 1; align-self: flex-end; height: %s; border-radius: %s; background: %s; opacity: %s;"></span>'
                   % ('3px' if i >= elapsed else '%d%%' % max(8, round(x * 100 / top)), rad if i < elapsed else '50%',
                      INK if (i == hi and v == 'B') else color, '1' if i == hi else ('0.25' if i >= elapsed else '0.6')) for i, x in enumerate(values))
    return '<div style="display: flex; gap: 2px; height: 36px; width: 100%%;">%s</div>' % bars


def minis(v):
    return col(labeled(minibars(v, [3, 5, 2, 8, 4, 0, 6, 9, 3, 5, 7, 2, 4, 6, 0, 0, 0, 0, 0, 0], 14, 13), 'прошедшие · сегодня ' + ('ярче' if v == 'A' else 'цветом текста') + ' · впереди точки'),
               labeled(minibars(v, [4, 7, 5] + [0] * 17, 3, 2, CYAN), 'начало месяца'))


def bar_row(v, icon, color, lbl, value, share, frac, ghost=None, badge_html=''):
    g = ('<span style="position: absolute; left: %d%%; top: -3px; bottom: -3px; width: 2px; background: %s;"></span>' % (round(ghost * 100), MUTED2) if ghost else '')
    text = ('<div style="display: flex; align-items: center; gap: 6px; font-size: 14px; position: relative;"><span style="flex-grow: 1; min-width: 0; white-space: nowrap; overflow: hidden; '
            'text-overflow: ellipsis;">%s</span>%s<span style="%s">%s</span><span style="color: %s; white-space: nowrap;">· %s</span></div>' % (lbl, badge_html, MONO, value, MUTED, share))
    if v == 'A':
        return ('<div style="display: flex; gap: 10px; align-items: center;">%s<div style="flex-grow: 1; min-width: 0; display: flex; flex-direction: column; gap: 6px;">%s'
                '<div style="position: relative; height: 8px;"><div style="width: %d%%; height: 100%%; border-radius: 4px; background: %s;"></div>%s</div></div></div>'
                % (holder(v, icon, color, 32, False), text, round(frac * 100), color, g))
    return ('<div style="position: relative; display: flex; gap: 10px; align-items: center; padding: 6px 8px; border-radius: 10px; overflow: hidden;">'
            '<div style="position: absolute; left: 0; top: 0; bottom: 0; width: %d%%; background: %s;"></div>%s<div style="flex-grow: 1; min-width: 0;">%s</div>%s</div>'
            % (round(frac * 100), mix(color, 22), holder(v, icon, color, 28, False), text, g))


def bar_rows(v):
    return col(bar_row(v, 'cart', LIME, 'Продукты', '290', '38 %', 0.8, 0.65, change(v, '↑ 45 %', PINK)), bar_row(v, 'cafe', '#FF8A5C', 'Кафе', '120', '16 %', 0.35, 0.4),
               bar_row(v, 'taxi', '#FFD43B', 'Такси ночью из аэропорта', '8', '1 %', 0.03),
               cap('полоса под подписью' if v == 'A' else 'строка сама — полоса: заливка на долю'))


def columns(v):
    vals, labels_, chosen, avg, h = [980, 1210, 860, 1350, 1100, 1240], ['июн', 'июл', 'авг', 'сен', 'окт', 'ноя'], 5, 1120, 120
    top = max(vals)
    cols_ = ''.join('<div style="flex: 1; display: flex; flex-direction: column; align-items: center; gap: 4px; justify-content: flex-end; height: 100%%;">'
                    '<span style="font-size: 10px; color: %s;">%s</span><span style="width: %s; height: %dpx; border-radius: %s; background: %s; %s"></span>'
                    '<span style="font-size: 11px; color: %s; %s">%s</span></div>'
                    % (INK if i == chosen else MUTED, x, '70%' if v == 'A' else '86%', round(x * (h - 40) / top), '8px 8px 3px 3px' if v == 'A' else '2px',
                       LIME if i == chosen else (LINE2 if v == 'A' else KEY2), 'box-shadow: inset 0 0 0 2px %s;' % INK if i == chosen else '',
                       INK if i == chosen else MUTED, 'font-weight: 700;' if i == chosen else '', labels_[i]) for i, x in enumerate(vals))
    a = '<div style="position: absolute; left: 0; right: 0; bottom: %dpx; border-top: 1px dashed %s;"></div>' % (round(avg * (h - 40) / top) + 20, MUTED2)
    return col('<div style="position: relative; display: flex; gap: 4px; height: %dpx;">%s%s</div>' % (h, cols_, a), cap('выбранный — рамка + жирный; средняя — пунктир'))


def ring(v, slices, inner, size=116):
    th = 16 if v == 'A' else 10
    rr = (size - th) / 2
    c = 2 * 3.1416 * rr
    off, arcs = 0, ''
    for frac, color in slices:
        arcs += ('<circle cx="%g" cy="%g" r="%g" fill="none" stroke="%s" stroke-width="%d" stroke-linecap="%s" stroke-dasharray="%.1f %.1f" stroke-dashoffset="%.1f"></circle>'
                 % (size / 2, size / 2, rr, color, th, 'round' if v == 'A' and len(slices) > 1 else 'butt', max(c * frac - (th + 3 if v == 'A' and len(slices) > 1 else 2), 1), c, -off))
        off += c * frac
    return ('<div style="position: relative; width: %dpx; height: %dpx; flex: none;"><svg width="%d" height="%d" style="transform: rotate(-90deg);">%s</svg>'
            '<div style="position: absolute; inset: 0; display: flex; align-items: center; justify-content: center; flex-direction: column;">%s</div></div>' % (size, size, size, size, arcs, inner))


def donuts(v):
    sl = [(0.38, LIME), (0.22, CYAN), (0.16, VIOLET), (0.12, '#FF8A5C'), (0.12, MUTED2)]
    if v == 'A':
        return flexrow(ring(v, sl, amt(v, '760', 'USD', 17)), ring(v, [(1, LINE2)], cap('нет трат')))
    legend = col(*[flexrow('<span style="width: 10px; height: 10px; border-radius: 2px; background: %s;"></span>' % c, '<span style="font-size: 12px;">%s</span>' % n,
                           '<span style="font-size: 12px; color: %s;">%d %%</span>' % (MUTED, f * 100), gap=6, wrap=False) for (f, c), n in zip(sl, ['Продукты', 'Кафе', 'Дом', 'Такси', 'Прочее'])], gap=4)
    return flexrow(ring(v, sl, amt(v, '760', 'USD', 15)), legend, gap=14, wrap=False)


def tag_chip(v, name, color, on, remove=False):
    if v == 'A':
        st = 'background: %s; box-shadow: inset 0 0 0 2px %s;' % (mix(color, 22), INK) if on else 'box-shadow: inset 0 0 0 1px %s;' % OUTLINE
        lead = dot(color, 8)
        rad = '999px'
    else:
        st = 'background: %s; box-shadow: inset 0 0 0 2px %s;' % (mix(color, 14), INK) if on else 'box-shadow: inset 0 0 0 1px %s;' % OUTLINE
        lead = '<span style="width: 4px; height: 18px; border-radius: 2px; background: %s;"></span>' % color
        rad = '8px'
    return ('<span style="height: 34px; max-width: 170px; padding: 0 12px 0 %dpx; border-radius: %s; display: inline-flex; align-items: center; gap: 8px; %s">%s'
            '<span style="font-size: 14px; color: %s; %s white-space: nowrap; overflow: hidden; text-overflow: ellipsis;">%s</span>%s</span>'
            % (12 if v == 'A' else 8, rad, st, lead, INK, 'font-weight: 700;' if on else '', name, svg('close', INK, 14) if remove else ''))


def tag_chips(v):
    more = ('<span style="height: 34px; padding: 0 12px; border-radius: %s; border: 1px dashed %s; color: %s; font-size: 14px; display: inline-flex; align-items: center;">Все 34</span>'
            % ('999px' if v == 'A' else '8px', OUTLINE, INK))
    return col(flexrow(*[tag_chip(v, n, c, i in (0, 2), i == 0) for i, (n, c) in enumerate(TAGS)], more),
               cap('подпись — цветом текста (#385); выбран — тон + рамка + жирный; цвет тега — ' + ('точка' if v == 'A' else 'полоска')))


def tag_pill(v, name, color):
    if v == 'A':
        return ('<span style="height: 22px; max-width: 130px; padding: 0 8px 0 6px; border-radius: 999px; background: %s; display: inline-flex; align-items: center; gap: 5px;">%s'
                '<span style="font-size: 11px; color: %s; white-space: nowrap; overflow: hidden; text-overflow: ellipsis;">%s</span></span>' % (mix(color, 20), dot(color, 6), INK, name))
    return ('<span style="height: 22px; max-width: 130px; display: inline-flex; align-items: center; gap: 5px;"><span style="width: 3px; height: 12px; border-radius: 1px; background: %s;"></span>'
            '<span style="font-size: 11px; color: %s; white-space: nowrap; overflow: hidden; text-overflow: ellipsis;">%s</span></span>' % (color, INK2, name))


def tag_pills(v):
    return col(flexrow(tag_pill(v, 'Отпуск', TAGS[0][1]), tag_pill(v, 'Ремонт кухни и ванной комнаты', TAGS[2][1]), '<span style="font-size: 11px; color: %s;">+2</span>' % MUTED),
               cap('в строке операции: ' + ('пилюля с тоном' if v == 'A' else 'полоска + подпись, без фона')))


def paired(v):
    import mockup_screens_picture as pic  # approved on the screen board Rp189IeA (#189, poll А)
    return col(pic.pairs() + pic.legend((CYAN, 'пришло'), (PINK, 'ушло')), cap('пара на месяц: доход и расход от нуля; выбранная — ярко на плашке, остальные бледно; тап — месяц'))


def lines(v):
    import mockup_screens_picture as pic  # approved on the screen board Rp189NwB (#189, poll Б)
    return col(pic.line(320), cap('линия акцентом с бледной заливкой, шкала по значениям (не от нуля); точка — выбранный месяц; тап — ближайшая точка'))


DATA = [
    ('Сумма', amounts, 'AmountText(text, style, color, minFontSize)'),
    ('Изменение', changes, 'CoShellChangePill(text, color)'),
    ('Шкала бюджета', bullets, 'CoShellBullet(fraction, tone = Ok | Ahead | Over | Idle, mark)'),
    ('Мини-столбики', minis, 'CoShellMiniBars(values, color, elapsed, highlight)'),
    ('Строка-столбик', bar_rows, 'CoShellBarRow(label, value, share, fraction, ghost, badge) · CoShellSegmentBar(total, kept) — «Уложились 4 из 6» · CoShellDayHeader(title, total)'),
    ('Столбцы по месяцам', columns, 'CoShellColumns(columns, selected, average)'),
    ('Кольцо', donuts, 'CoShellDonut(slices, description, content)'),
    ('Теги — выбор', tag_chips, 'CoShellTagChip(name, color, selected, removeLabel) · CoShellTagMoreChip'),
    ('Теги — в строке', tag_pills, 'CoShellTagPill(name, color) · CoShellTagMore · CoShellTagDot'),
    ('Доходы и расходы', paired, 'CoShellPairedColumns(pairs, selected, onSelect) — доска Rp189IeA'),
    ('Линия', lines, 'CoShellLine(points, selected, description, onSelect) — доска Rp189NwB'),
]
