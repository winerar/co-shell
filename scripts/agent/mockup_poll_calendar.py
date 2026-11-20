#!/usr/bin/env python3
"""Poll boards: the calendar (one day and a period) brought to the common style (Style1: days 44 — circles, chosen —
neon + 2 dp edge + bold, tracks, capsules). Today the app wraps Material's DatePicker/DateRangePicker (`DatePickers.kt`).

Standalone PNGs like mockup_poll_receipts561.py: `CalXxx.png` dark + `CalXxx@light.png`.
Usage: python3 scripts/agent/mockup_poll_calendar.py <out-dir>  → <out-dir>/img/*.png   then   poll-page.py <spec> <poll-dir> --img <out-dir>/img
"""
import os
import subprocess
import sys

HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, HERE)
import mockup_nav_poll as N  # noqa: E402
import mockup_poll_accounts4 as A  # noqa: E402
import mockup_poll_update as U  # noqa: E402

CHEV_L, CHEV_R = 'M15 6l-6 6 6 6', 'M9 6l6 6-6 6'
DOWN = 'M7 10l5 5 5-5'
X = 'M7 7l10 10M17 7L7 17'
WEEK = ['пн', 'вт', 'ср', 'чт', 'пт', 'сб', 'вс']
MONTHS = ['Январь', 'Февраль', 'Март', 'Апрель', 'Май', 'Июнь', 'Июль', 'Август', 'Сентябрь', 'Октябрь', 'Ноябрь', 'Декабрь']
SHORT = ['янв', 'фев', 'мар', 'апр', 'май', 'июн', 'июл', 'авг', 'сен', 'окт', 'ноя', 'дек']
TODAY = 20  # 20 November 2026, Friday
NOV = (6, 30)  # 1 November 2026 is a Sunday: 6 empty cells in a Monday week
OCT = (3, 31)


def accent(t):
    """accentInk(): neon in the dark theme, ink in the light one."""
    return t['prim'] if t['dark'] else t['ink']


def link(t):
    """secondaryInk(): links and action captions."""
    return t['acc'] if t['dark'] else '#5B1FA8'


def circle_btn(t, path, track=True):
    bg = 'background:%s;' % t['bar'] if track else ''
    return '<span style="width:48px;height:48px;flex:none;border-radius:50%%;%sdisplay:flex;align-items:center;justify-content:center">%s</span>' % (bg, N.glyph(path, t['ink'], 22, 1.8))


def chosen(t):
    return 'background:%s;color:%s;font-weight:700;box-shadow:inset 0 0 0 2px %s;' % (t['prim'], t['onp'], A.edge(t))


def day_cell(t, d, kind='', band='', tracks=False, dot=False):
    """One day in a 1/7 column: a 44 circle; kind '' | 'sel' | 'today'; band '' | 'mid' | 'start' | 'end' (period strip)."""
    if d is None:
        return '<span style="flex:1;height:48px"></span>'
    st = 'width:44px;height:44px;border-radius:50%;display:flex;align-items:center;justify-content:center;font-size:15px;position:relative;'
    if kind == 'sel':
        st += chosen(t)
    elif kind == 'today':
        st += 'color:%s;font-weight:700;box-shadow:inset 0 0 0 1.5px %s;' % (accent(t), accent(t))
        if tracks:
            st += 'background:%s;' % t['bar']
    elif tracks and not band:
        st += 'background:%s;' % t['bar']
    if d > TODAY and not kind and not band:
        st += 'color:%s;' % t['muted']
    mark = ('<span style="position:absolute;bottom:5px;width:4px;height:4px;border-radius:50%%;background:%s"></span>' % (t['onp'] if kind == 'sel' else link(t))) if dot else ''
    strip = ''
    if band:
        # endpoints: strip on the inner half; week edges: the strip is rounded off on the outer side
        left, right, rad = {'start': ('50%', '0', '0'), 'end': ('0', '50%', '0'), 'mid': ('0', '0', '0'),
                            'wstart': ('2px', '0', '22px 0 0 22px'), 'wend': ('0', '2px', '0 22px 22px 0'), 'both': ('2px', '2px', '22px')}[band]
        strip = '<span style="position:absolute;top:2px;bottom:2px;left:%s;right:%s;border-radius:%s;background:%s"></span>' % (left, right, rad, t['bar'])
    return '<span style="flex:1;height:48px;position:relative;display:flex;align-items:center;justify-content:center">%s<span style="%s">%d%s</span></span>' % (strip, st, d, mark)


def grid(t, month, sel=(), rng=None, tracks=False, dots=(), today=True):
    off, n = month
    cells = [None] * off + list(range(1, n + 1))
    cells += [None] * (-len(cells) % 7)
    rows = []
    for w in range(0, len(cells), 7):
        out = []
        week = cells[w:w + 7]
        for i, d in enumerate(week):
            band, kind = '', ''
            if d is not None and rng and rng[0] <= d <= rng[1] and rng[0] != rng[1]:
                first = i == 0 or week[i - 1] is None
                last = i == 6 or week[i + 1] is None or d == n
                if d == rng[0]:
                    band, kind = ('' if last else 'start'), 'sel'
                elif d == rng[1]:
                    band, kind = ('' if first else 'end'), 'sel'
                else:
                    band = 'both' if first and last else 'wstart' if first else 'wend' if last else 'mid'
            if d in sel:
                kind = 'sel'
            elif today and d == TODAY and not kind:
                kind = 'today'
            out.append(day_cell(t, d, kind, band, tracks, d in dots))
        # period ends keep the strip on their inner side only
        rows.append('<div style="display:flex">%s</div>' % ''.join(out))
    return ''.join(rows)


def weekdays(t):
    return '<div style="display:flex;margin-bottom:2px">%s</div>' % ''.join(
        '<span style="flex:1;text-align:center;font-size:12px;color:%s;letter-spacing:.04em">%s</span>' % (t['muted'], w) for w in WEEK)


def sheet(t, title, sub, body, h, action='Готово'):
    head = ('<div style="display:flex;align-items:flex-start;padding:4px 8px 0 20px"><div style="flex:1;padding-top:8px"><div style="font-size:20px;font-weight:700">%s</div>'
            '<div style="font-size:13px;color:%s;margin-top:2px">%s</div></div>%s</div>' % (title, t['muted'], sub, circle_btn(t, X)))
    btn = ('<div style="position:absolute;left:16px;right:16px;bottom:24px;height:48px;border-radius:999px;%sdisplay:flex;align-items:center;justify-content:center;font-size:16px">%s</div>'
           % (chosen(t).replace('font-weight:700;', 'font-weight:600;').replace('inset 0 0 0 2px', 'inset 0 0 0 1px'), action))
    return A.sheet(t, head + body + btn, h)


def nav_header(t, label, track=True):
    """‹ Ноябрь 2026 ▾ › — the month is a capsule that opens the months grid."""
    mid = ('<span style="flex:1;height:48px;margin:0 8px;border-radius:999px;%sdisplay:flex;align-items:center;justify-content:center;gap:4px;font-size:17px;font-weight:600">%s%s</span>'
           % ('background:%s;' % t['bar'] if track else '', label, N.glyph(DOWN, t['muted'], 20, 1.8)))
    return '<div style="display:flex;align-items:center;margin:12px 16px 8px">%s%s%s</div>' % (circle_btn(t, CHEV_L), mid, circle_btn(t, CHEV_R))


def quick(t, items, on=None):
    out = []
    for k in items:
        st = chosen(t) if k == on else 'background:%s;' % t['bar']
        out.append('<span style="flex:none;height:36px;padding:0 14px;border-radius:999px;display:flex;align-items:center;font-size:14px;%s">%s</span>' % (st, k))
    return '<div style="display:flex;gap:8px;margin:12px 16px 0;overflow:hidden">%s</div>' % ''.join(out)


def summary(t, text):
    return '<div style="margin:4px 20px 0;font-size:14px;color:%s">%s</div>' % (t['muted'], text)


# ── А: one month, arrows + month capsule; the same component for a day and a period ──

def a_day(t, v):
    body = nav_header(t, 'Ноябрь 2026') + '<div style="margin:0 12px">%s%s</div>' % (weekdays(t), grid(t, NOV, sel=(17,)))
    return sheet(t, 'Дата', 'вторник, 17 ноября', body, 560)


def a_range(t, v):
    body = (nav_header(t, 'Ноябрь 2026') + '<div style="margin:0 12px">%s%s</div>' % (weekdays(t), grid(t, NOV, rng=(3, 14)))
            + summary(t, '3–14 ноября · 12 дней'))
    return sheet(t, 'Выберите дни', 'нажмите первый и последний день', body, 590)


def a_months(t, v):
    """Tap on the month capsule: a grid of months (CoShellLabelGrid), year with arrows; future months muted."""
    yr = '<div style="display:flex;align-items:center;margin:12px 16px 8px">%s<span style="flex:1;text-align:center;font-size:17px;font-weight:600">2026</span>%s</div>' % (
        circle_btn(t, CHEV_L), circle_btn(t, CHEV_R))
    cells = []
    for i, m in enumerate(SHORT):
        st = chosen(t) if i == 10 else 'background:%s;' % t['bar']
        if i == 11:
            st += 'color:%s;' % t['muted']
        cells.append('<span style="height:44px;border-radius:999px;display:flex;align-items:center;justify-content:center;font-size:15px;%s">%s</span>' % (st, m))
    g = '<div style="display:grid;grid-template-columns:repeat(4,1fr);gap:8px;margin:4px 16px 0">%s</div>' % ''.join(cells)
    return sheet(t, 'Дата', 'вторник, 17 ноября', yr + g, 560)


# ── Б: months one under another (scroll), weekday row pinned ──

def month_block(t, name, month, **kw):
    return ('<div style="margin:14px 20px 4px;font-size:13px;font-weight:600;letter-spacing:.04em;color:%s">%s</div><div style="margin:0 12px">%s</div>'
            % (accent(t), name.upper(), grid(t, month, **kw)))


def b_day(t, v):
    body = ('<div style="margin:12px 12px 0;padding-bottom:4px;border-bottom:1px solid %s">%s</div>' % (t['bar'], weekdays(t))
            + '<div style="height:452px;overflow:hidden;display:flex;flex-direction:column;justify-content:flex-end">%s%s</div>' % (
                month_block(t, 'Октябрь 2026', OCT, today=False), month_block(t, 'Ноябрь 2026', NOV, sel=(17,))))
    return sheet(t, 'Дата', 'вторник, 17 ноября', body, 680)


def b_range(t, v):
    body = ('<div style="margin:12px 12px 0;padding-bottom:4px;border-bottom:1px solid %s">%s</div>' % (t['bar'], weekdays(t))
            + '<div style="height:452px;overflow:hidden;display:flex;flex-direction:column;justify-content:flex-end">%s%s</div>' % (
                month_block(t, 'Октябрь 2026', OCT, rng=(27, 99), today=False), month_block(t, 'Ноябрь 2026', NOV, rng=(0, 14))))
    return sheet(t, 'Выберите дни', '27 октября – 14 ноября · 19 дней', body, 680)


# ── В: like А, but days on tracks (every day a filled circle) inside a card, quick chips on top ──

def card(t, inner):
    return '<div style="margin:12px 16px 0;padding:4px 0 8px;border-radius:24px;background:%s;border:1px solid %s">%s</div>' % (t['surf'], t['line'], inner)


def c_day(t, v):
    inner = nav_header(t, 'Ноябрь 2026', track=False).replace('margin:12px 16px 8px', 'margin:4px 4px 4px') + '<div style="margin:0 4px">%s%s</div>' % (
        weekdays(t), grid(t, NOV, sel=(17,), tracks=True, dots=(3, 5, 9, 12, 16, 18, 19)))
    body = quick(t, ['Сегодня', 'Вчера', 'Позавчера']) + card(t, inner)
    return sheet(t, 'Дата', 'вторник, 17 ноября · точка — есть операции', body, 650)


def c_range(t, v):
    inner = nav_header(t, 'Ноябрь 2026', track=False).replace('margin:12px 16px 8px', 'margin:4px 4px 4px') + '<div style="margin:0 4px">%s%s</div>' % (
        weekdays(t), grid(t, NOV, rng=(3, 14), tracks=True))
    body = quick(t, ['Эта неделя', 'Прошлая', '7 дней', '30 дней']) + card(t, inner) + summary(t, '3–14 ноября · 12 дней')
    return sheet(t, 'Выберите дни', 'нажмите первый и последний день', body, 680)


BOARDS = [('CalADay', a_day), ('CalARange', a_range), ('CalAMonths', a_months),
          ('CalBDay', b_day), ('CalBRange', b_range),
          ('CalCDay', c_day), ('CalCRange', c_range)]


def main():
    out = sys.argv[1]
    os.makedirs(os.path.join(out, 'html'), exist_ok=True)
    for name, fn in BOARDS:
        for suffix, dark in (('', True), ('@light', False)):
            t = U.theme(dark)
            open(os.path.join(out, 'html', name + suffix + '.html'), 'w', encoding='utf-8').write(N.page(fn(t, name), 390, 844, dict(page=t['page'])))
    js = os.path.join(out, 'render.js')
    src = open(os.path.join(HERE, 'mockup_poll_update.py'), encoding='utf-8').read()
    open(js, 'w').write(src[src.index("'''const") + 3:src.index("''')")].replace('\\\\d', '\\d'))
    subprocess.run(['node', js, os.path.abspath(out)], check=True, cwd=out)
    print('boards → %s/img' % out)


if __name__ == '__main__':
    main()
