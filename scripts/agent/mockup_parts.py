"""Larger components for boards, made of mockup_bricks: account tiles, rows with money, swatches, rings, banners.

Same rule as the bricks: only the «Вольт · тёмная» constants below/in mockup_bricks — mockup_theme.py turns them
into theme tokens. Brand and category colours (banks, palette swatches) stay fixed on purpose.
"""
from mockup_bricks import *  # noqa: F401,F403

LIMEBG, CYANBG, VIOLETBG, YELLOWBG, ORANGEBG = '#1D2A0B', '#062224', '#1B0F2B', '#262305', '#2A140B'
SELBG = '#16200F'

# name, ini, colour, ink, kind, balance, cur, ≈ USD, group
ACCOUNTS = [
    ('Galicia', 'GA', '#F26E22', '#1A0A00', 'Карта', '1 250 000', 'ARS', '1 220', 'cards'),
    ('Mercado Pago', 'MP', '#00B1EA', '#002A3A', 'Кошелёк', '96 400', 'ARS', '94', 'cards'),
    ('Santander', 'SA', '#EC0000', '#FFFFFF', 'Карта', '310 000', 'ARS', '302', 'cards'),
    ('Ameria', 'AM', '#B01E2F', '#FFFFFF', 'личный · Аня', '410 000', 'AMD', '1 060', 'cards'),
    ('Inecobank', 'IN', '#0096D6', '#FFFFFF', 'Карта', '120 000', 'AMD', '310', 'cards'),
    ('Т-Банк', 'Т', '#FFDD2D', '#1A1400', 'Карта', '18 500', 'RUB', '190', 'cards'),
    ('Binance', 'BN', '#F0B90B', '#1A1400', 'Биржа', '1 840', 'USDT', '1 840', 'crypto'),
    ('Bybit Card', 'BY', '#F7A600', '#1A1400', 'Карта', '470', 'USDT', '470', 'crypto'),
    ('Песо', 'P', '#A8FF3E', '#0B0F0A', 'Наличные', '310 000', 'ARS', '302', 'cash'),
    ('Доллары', '$', '#2BE4FF', '#0B0F0A', 'Наличные', '1 000', 'USD', '1 000', 'cash'),
    ('Драмы', '֏', '#D36BFF', '#0B0F0A', 'Наличные', '83 000', 'AMD', '215', 'cash'),
    ('Вклад', 'ВК', '#FFD43B', '#1A1400', 'Вклад', '1 500', 'USD', '1 500', 'deposit'),
]
GROUPS = [('cards', 'Карты и банки', '3 176'), ('crypto', 'Крипто', '2 310'), ('cash', 'Наличные', '1 517'), ('deposit', 'Вклады', '1 500')]


def acc(name):
    return next(a for a in ACCOUNTS if a[0] == name)


def in_group(g):
    return [a for a in ACCOUNTS if a[8] == g]


def money(amt, cur, second='', color=INK, size=15):
    """Amount with currency and an optional muted second line (≈ USD, receipt)."""
    s = '<span style="%s font-size: 12px; color: %s;">%s</span>' % (MONO, MUTED, second) if second else ''
    return '<span style="display: flex; flex-direction: column; align-items: flex-end;">%s%s</span>' % (amount(amt, cur, color, size), s)


def acc_row(a):
    return row(a[0], a[4], badge(a[1], a[2], a[3], 40, 12), money(a[5], a[6], '≈ %s USD' % a[7]))


def group_head(title, count, total, open_=True):
    """Accent group header with count, total and a fold chevron."""
    return ('<div style="display: flex; align-items: center; gap: 6px; margin: 18px 4px 8px;"><span style="font-size: 13px; font-weight: 600; color: %s; '
            'letter-spacing: 0.04em; text-transform: uppercase;">%s</span><span style="font-size: 12px; color: %s;">%s</span><span style="flex-grow: 1;"></span>%s%s</div>'
            % (LIME, title, MUTED, count, amount(total, 'USD', INK2, 13), svg('down' if open_ else 'chev', MUTED, 18)))


def big_total(caption, total, cur, size=34):
    return ('<div style="font-size: 13px; color: %s;">%s</div><div style="margin-top: 4px; %s font-size: %dpx; font-weight: 700;">%s '
            '<span style="font-size: 18px; color: %s;">%s</span></div>' % (MUTED, caption, MONO, size, total, MUTED, cur))


def acc_tile(a, on=False):
    """Account tile in the entry sheet (step 1): badge, name, balance; ring when chosen."""
    st = 'border: 2px solid %s; background: %s;' % (WARN, ORANGEBG) if on else 'border: 1px solid %s; background: %s;' % (LINE, CARD)
    return ('<div style="display: flex; flex-direction: column; gap: 6px; padding: 10px; border-radius: 16px; min-width: 0; %s">%s'
            '<span style="font-size: 13px; font-weight: 600; white-space: nowrap; overflow: hidden; text-overflow: ellipsis;">%s</span>'
            '<span style="%s font-size: 11px; color: %s; white-space: nowrap;">%s %s</span></div>' % (st, badge(a[1], a[2], a[3], 26, 8), a[0], MONO, MUTED, a[5], a[6]))


def acc_recent(a, on=False):
    ring = 'border: 2px solid %s; background: %s;' % (WARN, ORANGEBG) if on else 'border: 2px solid transparent;'
    return ('<div style="display: flex; flex-direction: column; align-items: center; gap: 6px; padding: 8px 2px; border-radius: 16px; min-width: 0; %s">%s'
            '<span style="font-size: 11px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; max-width: 100%%;">%s</span></div>' % (ring, badge(a[1], a[2], a[3], 40, 12), a[0]))


def grid(cells, cols=3, gap=8):
    return '<div style="display: grid; grid-template-columns: repeat(%d, minmax(0, 1fr)); gap: %dpx;">%s</div>' % (cols, gap, ''.join(cells))


def nav_tile(icon, title, sub, color, bg):
    """Big tile on «Ещё»."""
    return ('<div style="display: flex; flex-direction: column; justify-content: space-between; gap: 18px; min-height: 120px; padding: 14px; box-sizing: border-box; '
            'border-radius: 16px; background: %s; border: 1px solid %s;">%s<span style="display: flex; flex-direction: column; gap: 2px;">'
            '<span style="font-size: 16px; font-weight: 600;">%s</span><span style="font-size: 12px; color: %s;">%s</span></span></div>'
            % (CARD, LINE, tile_icon(icon, color, bg), title, MUTED, sub))


def swatch(color, on=False, size=34, new=False):
    ring = 'border: 2px solid %s;' % INK if on else 'border: 2px solid transparent;'
    dot = ('<span style="position: absolute; top: 0; right: 0; width: 8px; height: 8px; border-radius: 50%%; background: %s; border: 2px solid %s;"></span>'
           % (INK, BG)) if new else ''
    return ('<span style="position: relative; display: inline-flex; padding: 2px; border-radius: 50%%; %s"><span style="width: %dpx; height: %dpx; '
            'border-radius: 50%%; background: %s;"></span>%s</span>' % (ring, size, size, color, dot))


def icon_cell(icon, on=False, color=None):
    st = 'background: %s; border: 1px solid %s;' % (color or WARN, color or WARN) if on else 'background: %s; border: 1px solid %s;' % (CARD, LINE)
    return ('<span style="height: 44px; border-radius: 12px; %s display: flex; align-items: center; justify-content: center;">%s</span>'
            % (st, svg(icon, ONLIME if on else INK2, 20, 1.7)))


def ring(frac, inner, color=LIME, size=168, width=10):
    """Progress ring with content in the middle (update, backup). Colour via --ring: the theme keeps it a neon fill, not ink."""
    r = (size - width) / 2
    c = 2 * 3.14159 * r
    return ('<div style="position: relative; width: %dpx; height: %dpx; margin: 0 auto;"><svg width="%d" height="%d" aria-hidden="true">'
            '<circle cx="%g" cy="%g" r="%g" fill="none" stroke="%s" stroke-width="%d"></circle>'
            '<circle cx="%g" cy="%g" r="%g" fill="none" style="--ring: %s; stroke: var(--ring);" stroke-width="%d" stroke-linecap="round" stroke-dasharray="%g %g" transform="rotate(-90 %g %g)"></circle></svg>'
            '<div style="position: absolute; inset: 0; display: flex; flex-direction: column; align-items: center; justify-content: center; text-align: center;">%s</div></div>'
            % (size, size, size, size, size / 2, size / 2, r, LINE2, width, size / 2, size / 2, r, color, width, c * frac, c, size / 2, size / 2, inner))


def top_bar(title, lead='back', actions=''):
    """Inner screen header with any leading icon (back, close)."""
    return ('<div style="display: flex; align-items: center; gap: 4px; height: 64px; padding: 0 8px;">%s'
            '<h1 style="margin: 0; font-size: 22px; font-weight: 700; flex-grow: 1;">%s</h1>%s</div>' % (icon_button(lead, 'Назад' if lead == 'back' else 'Закрыть'), title, actions))


def dashed(text, mt=12):
    return ('<div style="margin-top: %dpx; height: 52px; border-radius: 999px; border: 1px dashed %s; color: %s; font-size: 15px; font-weight: 600; '
            'display: flex; align-items: center; justify-content: center;">＋ %s</div>' % (mt, LINE2, LIME, text))


def pill(text, kind='lime'):
    st = {'lime': (LIME, ONLIME), 'violet': (VIOLETPILL, VIOLETINK), 'pink': (PINKBG, PINK), 'warn': (WARNBG, WARN), 'muted': (CHIP, INK2)}[kind]
    return ('<span style="flex: none; height: 24px; padding: 0 10px; border-radius: 999px; background: %s; color: %s; font-size: 12px; font-weight: 600; '
            'display: inline-flex; align-items: center; white-space: nowrap;">%s</span>' % (st[0], st[1], text))


def fab(text, icon='plus'):
    return ('<div style="position: absolute; right: 16px; bottom: 24px; height: 56px; padding: 0 22px; border-radius: 999px; background: %s; color: %s; '
            'font-size: 16px; font-weight: 600; display: flex; align-items: center; gap: 8px;">%s%s</div>' % (LIME, ONLIME, svg(icon, ONLIME, 22, 2.2), text))


def textarea(placeholder, count, value=''):
    v = ('<span style="color: %s;">%s</span>' % (INK, value)) if value else '<span style="color: %s;">%s</span>' % (MUTED2, placeholder)
    return ('<div style="margin-top: 12px; display: flex; flex-direction: column; min-height: 180px; padding: 12px 14px; border-radius: 12px; background: %s; '
            'border: 1px solid %s; font-size: 15px; line-height: 1.45;">%s<span style="flex-grow: 1;"></span><span style="text-align: right; font-size: 12px; color: %s; %s">%s / 500</span></div>'
            % (FIELD, LINE2, v, MUTED, MONO, count))


def footer(*buttons):
    return '<div style="position: absolute; left: 16px; right: 16px; bottom: 24px;">%s</div>' % ''.join(buttons)


def toast(text, bottom=110):
    return ('<div style="position: absolute; left: 50%%; transform: translateX(-50%%); bottom: %dpx; height: 44px; padding: 0 18px; border-radius: 999px; background: %s; '
            'display: flex; align-items: center; gap: 8px; font-size: 14px; white-space: nowrap;">%s%s</div>' % (bottom, CHIP, svg('check', OK, 18, 2.2), text))


def count_grid(items):
    return grid(['<div style="padding: 12px; border-radius: 14px; background: %s;"><div style="%s font-size: 20px; font-weight: 700;">%s</div>'
                 '<div style="font-size: 12px; color: %s;">%s</div></div>' % (CHIP, MONO, n, MUTED, t) for n, t in items], 2)
