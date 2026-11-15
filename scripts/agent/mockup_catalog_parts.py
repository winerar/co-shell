"""Shared bricks of the approved set (#367, А «Капсула», owner 2026-11-15) — boards CompNew/CompChoice/CompRows/CompData.

Drawn after core:designsystem (sizes, colour roles); «Вольт · тёмная» constants only (mockup_theme repaints them).
"""
from mockup_catalog_bricks import *  # noqa: F401,F403

TAGS = [('Отпуск', '#4FC3F7'), ('Работа', '#FFB547'), ('Ремонт кухни и ванной комнаты', '#FF7AB8'), ('Дети', '#91E668'), ('Подарки', '#B266FF')]


# ---- Выбор ----
def check_mark(state, frame=LINE2):
    """CoShellCheckMark: 'off' — frame, 'on' — neon + check, 'some' — neon + dash."""
    if state == 'off':
        return '<span style="width: 22px; height: 22px; box-sizing: border-box; flex: none; border-radius: 7px; border: 2px solid %s;"></span>' % frame
    inner = svg('check', ONLIME, 16, 2.6) if state == 'on' else '<span style="width: 10px; height: 2px; background: %s;"></span>' % ONLIME
    return '<span style="width: 22px; height: 22px; flex: none; border-radius: 7px; background: %s; display: flex; align-items: center; justify-content: center;">%s</span>' % (LIME, inner)


def radio_mark(on, frame=LINE2):
    return ('<span style="width: 22px; height: 22px; box-sizing: border-box; flex: none; border-radius: 50%%; border: 2px solid %s; display: flex; align-items: center; '
            'justify-content: center;">%s</span>' % (LIME if on else frame, dot(LIME, 10) if on else ''))


def tile(inner, w=None):
    return ('<div style="display: flex; align-items: center; gap: 8px; height: 40px; padding: 0 10px; border-radius: 999px; background: %s; box-sizing: border-box; %s">%s</div>'
            % (KEY, 'width: %dpx;' % w if w else '', inner))


def two_lines(title, sub, sub_color=MUTED):
    return ('<span style="flex-grow: 1; min-width: 0; display: flex; flex-direction: column;"><span style="font-size: 14px; white-space: nowrap; overflow: hidden; '
            'text-overflow: ellipsis;">%s</span><span style="font-size: 11px; color: %s; white-space: nowrap; overflow: hidden; text-overflow: ellipsis;">%s</span></span>'
            % (title, sub_color, sub))


def check_tile(on, title, sub):
    return tile(check_mark('on' if on else 'off') + two_lines(title, sub), 200)


def detail_tile(icon, lbl, value, empty_=False):
    return tile(svg(icon, MUTED, 18) + two_lines(lbl, value) .replace('font-size: 14px;', 'font-size: 11px; color: %s;' % MUTED, 1)
                .replace('font-size: 11px; color: %s; white' % MUTED, 'font-size: 14px; color: %s; white' % (MUTED if empty_ else INK), 1), 180)


def switch_line(title, sub, on):
    return flexrow(two_lines(title, sub), switch(on), gap=12, wrap=False)


def expand_icon(open_):
    return '<span style="display: inline-flex; transform: rotate(%ddeg);">%s</span>' % (180 if open_ else 0, svg('down', MUTED, 22))


def day_grid(sel, marked=None, days=31):
    cells = ''.join('<span style="height: 36px; border-radius: 50%%; display: flex; flex-direction: column; align-items: center; justify-content: center; font-size: 13px; %s">%d%s</span>'
                    % ('background: %s; color: %s; font-weight: 700;' % (LIME, ONLIME) if d == sel else 'color: %s;' % INK, d,
                       dot(ONLIME if d == sel else MUTED, 4) if d == marked else '') for d in range(1, days + 1))
    return '<div style="display: grid; grid-template-columns: repeat(7, minmax(0, 1fr)); gap: 2px;">%s</div>' % cells


def label_grid(labels, sel, cols=4):
    cells = ''.join('<span style="height: 36px; border-radius: 999px; display: flex; align-items: center; justify-content: center; font-size: 13px; %s">%s</span>'
                    % ('background: %s; color: %s; font-weight: 700;' % (LIME, ONLIME) if i == sel else 'background: %s; color: %s;' % (CHIP, INK), l) for i, l in enumerate(labels))
    return '<div style="display: grid; grid-template-columns: repeat(%d, minmax(0, 1fr)); gap: 6px;">%s</div>' % (cols, cells)


# ---- Строки, формы, шторки ----
def menu_icon(icon, color):
    return '<span style="width: 32px; height: 32px; flex: none; border-radius: 8px; background: %s; display: flex; align-items: center; justify-content: center;">%s</span>' % (
        mix(color, 20), svg(icon, color, 18))


def menu_row(icon, color, title, sub='', trail=None):
    t = chevron() if trail is None else trail
    return row(title, sub, menu_icon(icon, color), t, min_h=52)


def form_section(title, inner, trailing=''):
    head = ('<div style="display: flex; align-items: center; min-height: 16px;"><span style="flex-grow: 1; font-size: 11px; font-weight: 600; letter-spacing: 0.06em; '
            'color: %s;">%s</span>%s</div>' % (MUTED, title, trailing))
    return card(head + '<div style="margin-top: 6px;">%s</div>' % inner, pad='10px 12px')


def section_link(text):
    return '<span style="font-size: 13px; font-weight: 600; color: %s;">%s</span>' % (LIME, text)


def hint(text):
    return '<span style="font-size: 12px; line-height: 1.4; color: %s;">%s</span>' % (MUTED, text)


def section_v2(title, inner, error=''):
    e = '<span style="font-size: 12px; color: %s;">%s</span>' % (PINK, error) if error else ''
    return col('<span style="font-size: 13px; font-weight: 600; color: %s;">%s</span>' % (MUTED, title), inner, e, gap=6)


def confirm_card(title, text, ok, cancel=None, destructive=False):
    icon = ('<span style="width: 44px; height: 44px; border-radius: 50%%; background: %s; display: flex; align-items: center; justify-content: center;">%s</span>'
            % (PINKBG if destructive else LIMEPILL, svg('trash' if destructive else 'info', PINK if destructive else LIME, 22)))
    btns = (button(cancel, 'outline', None, 0).replace('width: 100%', 'flex: 1') if cancel else '') + button(ok, 'danger' if destructive else 'primary', None, 0).replace('width: 100%', 'flex: 1')
    return ('<div style="padding: 20px; border-radius: 28px; background: %s; display: flex; flex-direction: column; align-items: center; gap: 10px; text-align: center;">%s'
            '<span style="font-size: 18px; font-weight: 700;">%s</span><span style="font-size: 13px; line-height: 1.4; color: %s;">%s</span>'
            '<div style="align-self: stretch; display: flex; gap: 8px; margin-top: 6px;">%s</div></div>' % (KEY, icon, title, INK2, text, btns))


def modal_sheet(title, body, sub='', action=None, action_on=True):
    s = '<span style="font-size: 12px; color: %s;">%s</span>' % (MUTED, sub) if sub else ''
    head = ('<div style="display: flex; align-items: flex-start; gap: 8px; padding: 4px 16px 8px;"><span style="flex-grow: 1; display: flex; flex-direction: column; gap: 2px;">'
            '<span style="font-size: 20px; font-weight: 700;">%s</span>%s</span>%s</div>' % (title, s, svg('close', MUTED, 22)))
    act = '<div style="padding: 8px 16px 16px;">%s</div>' % button(action, 'primary', None, 0, not action_on) if action else ''
    return ('<div style="border-radius: 28px 28px 0 0; background: %s; overflow: hidden;"><div style="display: flex; justify-content: center; padding: 10px 0 6px;">'
            '<span style="width: 36px; height: 4px; border-radius: 2px; background: %s;"></span></div>%s<div style="padding: 0 16px;">%s</div>%s</div>'
            % (SHEET, LINE2, head, body, act))


def peek_card(lines, foot):
    rows = ''.join('<div style="display: flex; justify-content: space-between; gap: 16px; font-size: 14px;"><span style="%s color: %s;">%s</span><span style="color: %s;">%s</span></div>'
                   % (MONO, INK if v else MUTED, v or 'нет курса', MUTED, c) for v, c in lines)
    return ('<div style="width: 200px; padding: 12px 14px; border-radius: 24px; background: %s; border: 1px solid %s; box-shadow: 0 12px 32px rgba(0,0,0,0.45); '
            'display: flex; flex-direction: column; gap: 6px;">%s<span style="font-size: 11px; color: %s;">%s</span></div>' % (KEY, LINE2, rows, MUTED, foot))


def action_bar(items):
    cells = ''.join('<span style="flex: 1; height: 48px; border-radius: 999px; display: flex; align-items: center; justify-content: center; gap: 6px; font-size: 14px; '
                    'font-weight: 600; color: %s;">%s%s</span>' % (PINK if ic == 'trash' else INK, svg(ic, PINK if ic == 'trash' else INK, 20), t) for ic, t in items)
    return '<div style="height: 64px; padding: 0 8px; border-radius: 999px; background: %s; display: flex; align-items: center;">%s</div>' % (KEY, cells)


def fab_round(icon='plus'):
    return ('<span style="width: 48px; height: 48px; border-radius: 18px; background: %s; display: flex; align-items: center; justify-content: center; '
            'box-shadow: 0 6px 18px rgba(0,0,0,0.35);">%s</span>' % (LIME, svg(icon, ONLIME, 26, 2.2)))


# ---- Данные ----
def amount_text(text, cur, size=28, color=INK):
    return '<span style="%s font-size: %dpx; font-weight: 700; color: %s; white-space: nowrap;">%s <span style="font-size: %dpx; color: %s;">%s</span></span>' % (
        MONO, size, color, text, max(11, size // 2), MUTED, cur)


def change_pill(text, color):
    return ('<span style="height: 20px; padding: 0 7px; border-radius: 999px; background: %s; color: %s; font-size: 11px; font-weight: 600; display: inline-flex; '
            'align-items: center;">%s</span>' % (mix(color, 14), color, text))


def bullet_v2(frac, tone='ok', mark=None):
    color = {'ok': LIME, 'ahead': WARN, 'over': PINK, 'idle': MUTED}[tone]
    m = ('<span style="position: absolute; left: %d%%; top: -3px; bottom: -3px; width: 2px; background: %s; box-shadow: 0 0 0 2px %s;"></span>'
         % (round(mark * 100), INK, CARD) if mark is not None else '')
    return ('<div style="position: relative; width: 100%%; height: 8px; border-radius: 4px; background: %s;"><div style="width: %d%%; height: 100%%; border-radius: 4px; background: %s;"></div>%s</div>'
            % (KEY2, round(min(frac, 1) * 100), color, m))


def mini_bars(values, elapsed, hi=None, color=LIME):
    top = max(values) or 1
    bars = ''.join('<span style="flex: 1; align-self: flex-end; height: %s; border-radius: 2px; background: %s; opacity: %s;"></span>'
                   % ('3px' if i >= elapsed else '%d%%' % max(8, round(v * 100 / top)), color, '1' if i == hi else ('0.25' if i >= elapsed else '0.6'))
                   for i, v in enumerate(values))
    return '<div style="display: flex; gap: 2px; height: 36px; width: 180px;">%s</div>' % bars


def bar_row(icon, color, lbl, value, share, frac, ghost=None, badge_html=''):
    g = ('<span style="position: absolute; left: %d%%; top: -3px; bottom: -3px; width: 2px; background: %s;"></span>' % (round(ghost * 100), MUTED2) if ghost else '')
    return ('<div style="display: flex; gap: 10px; align-items: center;">%s<div style="flex-grow: 1; display: flex; flex-direction: column; gap: 6px;">'
            '<div style="display: flex; align-items: center; gap: 6px; font-size: 14px;"><span style="flex-grow: 1;">%s</span>%s<span style="%s">%s</span>'
            '<span style="color: %s;">· %s</span></div><div style="position: relative; height: 8px;"><div style="width: %d%%; height: 100%%; border-radius: 4px; background: %s;">'
            '</div>%s</div></div></div>' % (dot_icon(icon, color, 32), lbl, badge_html, MONO, value, MUTED, share, round(frac * 100), color, g))


def columns_v2(vals, labels, sel, avg=None, h=110):
    top = max(vals)
    cols = ''.join('<div style="flex: 1; display: flex; flex-direction: column; align-items: center; gap: 4px; justify-content: flex-end; height: 100%%;">'
                   '<span style="font-size: 10px; color: %s;">%s</span><span style="width: 70%%; height: %dpx; border-radius: 6px 6px 2px 2px; background: %s;"></span>'
                   '<span style="font-size: 11px; color: %s;">%s</span></div>' % (INK if i == sel else MUTED, v, round(v * (h - 40) / top), LIME if i == sel else LINE2,
                                                                                  INK if i == sel else MUTED, labels[i]) for i, v in enumerate(vals))
    a = ('<div style="position: absolute; left: 0; right: 0; bottom: %dpx; border-top: 1px dashed %s;"></div>' % (round(avg * (h - 40) / top) + 20, MUTED2) if avg else '')
    return '<div style="position: relative; display: flex; gap: 4px; height: %dpx;">%s%s</div>' % (h, cols, a)


def donut(slices, inner, size=120, th=16):
    r = (size - th) / 2
    c = 2 * 3.1416 * r
    off, arcs = 0, ''
    for frac, color in slices:
        arcs += ('<circle cx="%g" cy="%g" r="%g" fill="none" stroke="%s" stroke-width="%d" stroke-dasharray="%.1f %.1f" stroke-dashoffset="%.1f"></circle>'
                 % (size / 2, size / 2, r, color, th, max(c * frac - 3, 1), c, -off))
        off += c * frac
    return ('<div style="position: relative; width: %dpx; height: %dpx;"><svg width="%d" height="%d" style="transform: rotate(-90deg);">%s</svg>'
            '<div style="position: absolute; inset: 0; display: flex; align-items: center; justify-content: center; flex-direction: column;">%s</div></div>'
            % (size, size, size, size, arcs, inner))


# ---- Теги ----
def tag_chip(name, color, on, remove=False, ink=None):
    """CoShellTagChip; ink — #385 variants of the chosen label colour (None — the tag colour)."""
    ink = ink or color
    st = ('background: %s; box-shadow: inset 0 0 0 1px %s;' % (mix(color, 18), mix(color, 45)) if on else 'box-shadow: inset 0 0 0 1px %s;' % LINE2)
    x = svg('close', ink, 14) if remove else ''
    return ('<span style="height: 34px; max-width: 180px; padding: 0 12px; border-radius: 999px; display: inline-flex; align-items: center; gap: 8px; %s">%s'
            '<span style="font-size: 14px; color: %s; white-space: nowrap; overflow: hidden; text-overflow: ellipsis;">%s</span>%s</span>'
            % (st, dot(color, 8), ink if on else INK, name, x))


def tag_more_chip(text):
    return ('<span style="height: 34px; padding: 0 12px; border-radius: 999px; border: 1px dashed %s; color: %s; font-size: 14px; display: inline-flex; align-items: center;">%s</span>'
            % (LIME, LIME, text))


def tag_pill(name, color, ink=None):
    return ('<span style="height: 22px; max-width: 132px; padding: 0 7px; border-radius: 999px; background: %s; box-shadow: inset 0 0 0 1px %s; display: inline-flex; '
            'align-items: center; gap: 5px;">%s<span style="font-size: 11px; color: %s; white-space: nowrap; overflow: hidden; text-overflow: ellipsis;">%s</span></span>'
            % (mix(color, 18), mix(color, 45), dot(color, 6), ink or color, name))


def tag_more(n):
    return '<span style="font-size: 11px; color: %s;">+%d</span>' % (MUTED, n)
