"""Base bricks of the approved set (#367): components the app uses that the first board never drew.

Same rule as mockup_bricks: only «Вольт · тёмная» constants — mockup_theme.py repaints them per theme/palette.
Names after «→» on the boards are the Compose names (ApprovedComponentsTest reads every mockup_catalog*.py).
"""
from mockup_parts import *  # noqa: F401,F403



def flexrow(*items, gap=8, wrap=True, align='center'):
    return ('<div style="display: flex; %s align-items: %s; gap: %dpx;">%s</div>'
            % ('flex-wrap: wrap;' if wrap else '', align, gap, ''.join(items)))


def col(*items, gap=10):
    return '<div style="display: flex; flex-direction: column; gap: %dpx;">%s</div>' % (gap, ''.join(items))


def cap(text):
    """State caption under a demo: «выбрано», «выключено», «длинный текст»."""
    return '<span style="font-size: 11px; color: %s;">%s</span>' % (MUTED, text)


def labeled(demo, text):
    return '<div style="display: flex; flex-direction: column; gap: 6px; align-items: flex-start;">%s%s</div>' % (demo, cap(text))


# ---- Шапки (TopAppBar → CoShellTopBar) ----
def top_bar_v2(kind, title, actions=(), scrolled=False):
    bg = 'background: %s;' % PANEL if scrolled else ''
    acts = ''.join(icon_button(a, a) for a in actions)
    if kind == 'tab':
        lead, size = '<span style="width: 8px;"></span>', 28
    elif kind == 'screen':
        lead, size = icon_button('back', 'Назад'), 22
    else:  # selection
        lead, size = icon_button('close', 'Снять выбор'), 20
    return ('<div style="display: flex; align-items: center; gap: 4px; height: 64px; padding: 0 4px; border-radius: 16px; %s">%s'
            '<span style="flex-grow: 1; min-width: 0; font-size: %dpx; font-weight: 700; white-space: nowrap; overflow: hidden; text-overflow: ellipsis;">%s</span>%s</div>'
            % (bg, lead, size, title, acts))


# ---- Меню, разделитель, загрузка ----
def menu_v2(items):
    """items: (icon, text, state) — state: '' | 'on' (check) | 'danger' | 'off' (disabled)."""
    out = []
    for icon, text, st in items:
        color = PINK if st == 'danger' else INK
        op = ' opacity: 0.38;' if st == 'off' else ''
        trail = svg('check', LIME, 18, 2.2) if st == 'on' else ''
        out.append('<div style="display: flex; align-items: center; gap: 12px; height: 48px; padding: 0 14px;%s">%s<span style="flex-grow: 1; font-size: 15px; color: %s;">%s</span>%s</div>'
                   % (op, svg(icon, PINK if st == 'danger' else MUTED, 20), color, text, trail))
    return ('<div style="width: 232px; padding: 6px 0; border-radius: 24px; background: %s; border: 1px solid %s;">%s</div>'
            % (KEY, LINE2, ''.join(out)))


def divider(inset=0):
    return '<div style="height: 1px; margin-left: %dpx; background: %s;"></div>' % (inset, LINE2)


def spinner(size=24, color=LIME, width=3):
    r = size / 2 - width
    c = 2 * 3.1416 * r
    return ('<svg class="spin" width="%d" height="%d" viewBox="0 0 %d %d" aria-label="Загрузка"><circle cx="%g" cy="%g" r="%g" fill="none" stroke="%s" '
            'stroke-width="%d" stroke-linecap="round" stroke-dasharray="%.1f %.1f"></circle></svg>' % (size, size, size, size, size / 2, size / 2, r, color, width, c * 0.7, c))


# ---- Аватар, точка, статус ----
MEMBERS = [('А', '#D36BFF'), ('М', '#2BE4FF'), ('Л', '#FFB547')]


def avatar(ini, color, size=40, ring=False, dot=None):
    rg = 'box-shadow: 0 0 0 2px %s, 0 0 0 4px %s;' % (CARD, LIME) if ring else ''
    d = ('<span style="position: absolute; right: -1px; bottom: -1px; width: %dpx; height: %dpx; border-radius: 50%%; background: %s; border: 2px solid %s;"></span>'
         % (size // 4, size // 4, dot, CARD) if dot else '')
    return ('<span style="position: relative; width: %dpx; height: %dpx; flex: none; border-radius: 50%%; background: %s; color: #0B0F0A; display: flex; '
            'align-items: center; justify-content: center; font-size: %dpx; font-weight: 700; %s">%s%s</span>' % (size, size, color, int(size * 0.4), rg, ini, d))


def avatar_stack(n=2, size=32):
    return '<span style="display: inline-flex;">%s</span>' % ''.join(
        '<span style="margin-left: %dpx; border-radius: 50%%; box-shadow: 0 0 0 2px %s;">%s</span>' % (0 if i == 0 else -8, CARD, avatar(ini, c, size))
        for i, (ini, c) in enumerate(MEMBERS[:n]))


def dot(color=LIME, size=8):
    return '<span style="display: inline-block; width: %dpx; height: %dpx; border-radius: 50%%; background: %s;"></span>' % (size, size, color)


def status_chip(text, kind):
    """A status label (Style2): 22 capsule tinted 18 %, the colour only in the dot, the words in ink."""
    col = {'new': CYAN, 'work': WARN, 'done': OK, 'no': MUTED, 'manual': VIOLET}[kind]
    return ('<span style="height: 22px; padding: 0 8px; border-radius: 999px; background: %s; color: %s; font-size: 11px; font-weight: 600; '
            'display: inline-flex; align-items: center; gap: 5px;">%s%s</span>' % (mix(col, 18), INK, dot(col, 6), text))


# ---- Полоски выбора иконки и цвета, превью карточки счёта ----
def icon_strip(icons, sel):
    return flexrow(*['<span style="width: 40px; height: 40px; flex: none; border-radius: 50%%; display: flex; align-items: center; justify-content: center; %s">%s</span>'
                     % ('background: %s; box-shadow: inset 0 0 0 2px %s;' % (LIMEBG, LIME) if i == sel else 'background: %s;' % CHIP, svg(i, LIME if i == sel else INK2, 20))
                     for i in icons], gap=6, wrap=False)


def color_strip(colors, sel):
    return flexrow(*['<span style="width: 32px; height: 32px; flex: none; border-radius: 50%%; background: %s; %s display: flex; align-items: center; justify-content: center;">%s</span>'
                     % (c, 'box-shadow: 0 0 0 2px %s, 0 0 0 4px %s;' % (CARD, INK) if c == sel else '', svg('check', '#0B0F0A', 16, 2.4) if c == sel else '')
                     for c in colors], gap=8, wrap=False)


def account_card(name, ini, color, ink, kind, bal, cur):
    return ('<div style="width: 220px; height: 128px; box-sizing: border-box; padding: 14px; border-radius: 18px; background: %s; color: %s; display: flex; flex-direction: column;">'
            '<div style="display: flex; justify-content: space-between; gap: 8px; font-size: 13px; font-weight: 600;"><span style="min-width: 0; overflow: hidden; white-space: nowrap; text-overflow: ellipsis;">%s</span><span>%s</span></div>'
            '<div style="margin-top: auto; font-size: 12px; opacity: 0.8;">%s</div><div style="%s font-size: %dpx; font-weight: 700; white-space: nowrap;">%s <span style="font-size: 12px;">%s</span></div></div>'
            % (color, ink, name, ini, kind, MONO, 22 if len(bal) < 11 else 17, bal, cur))


# ---- Календарь: один день и период ----
def calendar(sel=(), today=None, band=None, outline_sel=False, head='Ноябрь 2026'):
    """sel: chosen days (1 or 2 = period ends); band: (a, b) days between ends; outline_sel — #385 variant Б."""
    wd = ''.join('<span style="text-align: center; font-size: 11px; color: %s;">%s</span>' % (MUTED, d) for d in ['пн', 'вт', 'ср', 'чт', 'пт', 'сб', 'вс'])
    cells = ['<span></span>' for _ in range(6)]  # 1 ноября 2026 — воскресенье
    for d in range(1, 31):
        st = 'color: %s;' % INK
        if band and band[0] < d < band[1]:
            st = 'background: %s; color: %s; border-radius: 0;' % (VIOLETPILL, VIOLETINK)
        if d in sel:
            st = 'background: %s; color: %s; font-weight: 700;' % (LIME, ONLIME) + (' box-shadow: inset 0 0 0 2px %s;' % INK if outline_sel else '')
        elif d == today and not (band and band[0] < d < band[1]):
            st += ' box-shadow: inset 0 0 0 1.5px %s;' % LIME
        cells.append('<span style="height: 34px; border-radius: 50%%; display: flex; align-items: center; justify-content: center; font-size: 13px; %s">%d</span>' % (st, d))
    top = flexrow('<span style="flex-grow: 1; font-size: 15px; font-weight: 600;">%s</span>' % head, svg('back', MUTED, 18), svg('chev', MUTED, 18), gap=14, wrap=False)
    return ('<div style="padding: 12px; border-radius: 20px; background: %s; display: flex; flex-direction: column; gap: 8px;">%s'
            '<div style="display: grid; grid-template-columns: repeat(7, minmax(0, 1fr)); row-gap: 2px;">%s%s</div></div>' % (KEY, top, wd, ''.join(cells)))
