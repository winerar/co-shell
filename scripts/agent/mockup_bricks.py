"""Shared bricks for generated design-canvas boards (scripts/agent/gen-mockups.py).

Everything is drawn in the «Вольт · тёмная» colours below; scripts/agent/mockup_theme.py then turns each colour
into a theme token, so the boards follow the canvas theme switch. Sizes follow core/designsystem tokens:
tab title 28, inner title 22, rows 56–64, cards radius 16–20, pill buttons 52–56, bottom bar 64.
"""
BG, CARD, FIELD, BAR, CHIP, LINE, DIV, LINE2 = '#050805', '#10160D', '#121810', '#161D12', '#1B2317', '#222B1E', '#1B2317', '#2E382A'
SHEET, KEY, KEY2 = '#11170E', '#1F271B', '#2A3326'
INK, INK2, MUTED, MUTED2 = '#EEF5E6', '#C9D3C2', '#9AA594', '#7D8A76'
LIME, ONLIME, LIMEPILL = '#B8FF2E', '#0E1F00', '#3A4A1E'
CYAN, VIOLET, VIOLETPILL, VIOLETINK, PINK, PINKBG, YELLOW = '#00F0FF', '#B266FF', '#2A2240', '#E6D6FF', '#FF5C7F', '#2B0F17', '#FFE600'
WARN, WARNBG, WARNLINE, OK = '#FFB547', '#2A1E08', '#5A3F10', '#5CFFB0'
MONO = "font-family: 'JetBrains Mono', monospace;"

ICON = {
    'back': 'M15 6l-6 6 6 6', 'close': 'M6 6l12 12M18 6L6 18', 'search': 'M20 20l-4-4M11 18a7 7 0 1 1 0-14 7 7 0 0 1 0 14z',
    'plus': 'M12 5v14M5 12h14', 'check': 'M5 12l5 5 9-10', 'chev': 'M9 6l6 6-6 6', 'down': 'M6 9l6 6 6-6',
    'home': 'M4 11l8-7 8 7v9H4z', 'list': 'M5 7h14M5 12h14M5 17h9', 'wallet': 'M3 7h18v12H3zM3 11h18', 'more': 'M5 12h.01M12 12h.01M19 12h.01',
    'cart': 'M3 4h2l2.4 11h11l2-8H6.5M9 20h.01M17 20h.01', 'cafe': 'M4 8h13v5a5 5 0 0 1-5 5H9a5 5 0 0 1-5-5zM17 9h2a2 2 0 0 1 0 4h-2',
    'taxi': 'M5 16h14l-1.5-6h-11zM7 16v2M17 16v2M8 10l1-3h6l1 3', 'salary': 'M4 8h16v11H4zM9 8V5h6v3M4 13h16',
    'transfer': 'M4 8h14l-3-3M20 16H6l3 3', 'pill': 'M8 4h8v16H8zM8 12h8', 'house': 'M4 11l8-6 8 6M6 10v9h12v-9',
    'phone': 'M8 3h8v18H8zM11 18h2', 'film': 'M4 5h16v14H4zM8 5v14M16 5v14', 'gift': 'M4 10h16v10H4zM12 10v10M4 10l2-4h12l2 4',
    'cloud': 'M7 18h10a4 4 0 0 0 .5-8 6 6 0 0 0-11.5 1.5A3.3 3.3 0 0 0 7 18z', 'refresh': 'M20 11a8 8 0 1 0-2.3 5.7M20 4v7h-7',
    'trash': 'M5 7h14M10 7V4h4v3M7 7l1 13h8l1-13', 'copy': 'M8 8h12v12H8zM16 8V4H4v12h4', 'archive': 'M4 5h16v4H4zM6 9v10h12V9M10 13h4',
    'file': 'M6 3h8l4 4v14H6zM14 3v4h4', 'upload': 'M12 16V4M7 9l5-5 5 5M5 20h14', 'download': 'M12 4v12M7 11l5 5 5-5M5 20h14',
    'user': 'M12 12a4 4 0 1 0 0-8 4 4 0 0 0 0 8zM4 21a8 8 0 0 1 16 0', 'mail': 'M3 6h18v12H3zM3 6l9 7 9-7',
    'alert': 'M12 4l9 16H3zM12 10v4M12 17v.01', 'info': 'M12 8v.01M12 11v6M12 21a9 9 0 1 1 0-18 9 9 0 0 1 0 18z',
    'google': 'M20 12h-8M20 12a8 8 0 1 1-2.3-5.7', 'logout': 'M14 4h5v16h-5M10 8l-4 4 4 4M6 12h10',
    'folder': 'M3 6h6l2 2h10v11H3z', 'calendar': 'M4 6h16v14H4zM4 10h16M8 3v4M16 3v4', 'note': 'M5 4h14v16H5zM8 9h8M8 13h8M8 17h5',
    'rate': 'M4 17l5-5 4 4 7-7M14 9h6v6', 'lock': 'M6 11h12v9H6zM9 11V8a3 3 0 0 1 6 0v3', 'spark': 'M12 3l2 6 6 2-6 2-2 6-2-6-6-2 6-2z',
}


def svg(name, color=MUTED, size=22, width=1.8):
    return ('<svg width="%d" height="%d" viewBox="0 0 24 24" fill="none" stroke="%s" stroke-width="%s" stroke-linecap="round" '
            'stroke-linejoin="round" aria-hidden="true"><path d="%s"></path></svg>' % (size, size, color, width, ICON[name]))


def board(title, body, script=''):
    return '''<!doctype html>
<html lang="ru">
<head>
<meta charset="utf-8">
<title>%s</title>
<script src="./support.js"></script>
</head>
<body>
<x-dc>
<helmet>
<link href="https://fonts.googleapis.com/css2?family=Onest:wght@400;500;600;700&amp;family=JetBrains+Mono:wght@500;700&amp;display=swap" rel="stylesheet">
<style>
body{margin:0}
button,input,textarea{font-family:inherit}
.spin{animation:spin 900ms linear infinite;transform-origin:50%% 50%%}
@keyframes spin{to{transform:rotate(360deg)}}
</style>
</helmet>
<div style="width: 390px; height: 844px; box-sizing: border-box; background: %s; font-family: Onest, system-ui, sans-serif; color: %s; position: relative; overflow: hidden;">
%s
</div>
</x-dc>
<script type="text/x-dc" data-dc-script data-props='{"$preview":{"width":390,"height":844}}'>
class Component extends DCLogic {
  renderVals() { return {%s}; }
}
</script>
</body>
</html>
''' % (title, BG, INK, body, script)


def tab_screen(title, body, active, actions='', bar=True):
    top = ('<div style="display: flex; align-items: center; gap: 4px; height: 64px; padding: 0 8px 0 16px;">'
           '<h1 style="margin: 0; font-size: 28px; font-weight: 700; flex-grow: 1;">%s</h1>%s</div>' % (title, actions))
    return top + '<div style="padding: 0 16px;">%s</div>' % body + (nav(active) if bar else '')


def inner_screen(title, body, actions=''):
    top = ('<div style="display: flex; align-items: center; gap: 4px; height: 64px; padding: 0 8px;">'
           + icon_button('back', 'Назад', INK) +
           '<h1 style="margin: 0; font-size: 22px; font-weight: 700; flex-grow: 1;">%s</h1>%s</div>' % (title, actions))
    return top + '<div style="padding: 0 16px;">%s</div>' % body


def icon_button(icon, label, color=INK, border=False):
    b = '1px solid %s' % LINE2 if border else 'none'
    return ('<button type="button" aria-label="%s" style="width: 48px; height: 48px; flex: none; border-radius: 50%%; border: %s; '
            'background: transparent; display: flex; align-items: center; justify-content: center;">%s</button>' % (label, b, svg(icon, color)))


def nav(active, dot_more=False):
    def tab(key, icon, label):
        on = key == active
        inner = svg(icon, ONLIME if on else MUTED, 24, 1.7)
        if on:
            inner = '<span style="width: 64px; height: 44px; border-radius: 999px; background: %s; display: flex; align-items: center; justify-content: center;">%s</span>' % (LIME, inner)
        dot = ('<span style="position: absolute; top: 8px; right: 18px; width: 8px; height: 8px; border-radius: 50%%; background: %s;"></span>' % LIME
               if key == 'more' and dot_more else '')
        return ('<a href="#" aria-label="%s" style="position: relative; display: flex; justify-content: center; align-items: center; height: 64px;">%s%s</a>'
                % (label, inner, dot))
    plus = ('<a href="#" aria-label="Новая операция" style="display: flex; justify-content: center;"><span style="width: 52px; height: 52px; '
            'border-radius: 50%%; background: %s; display: flex; align-items: center; justify-content: center;">%s</span></a>' % (LIME, svg('plus', ONLIME, 26, 2.2)))
    return ('<nav aria-label="Навигация" style="position: absolute; left: 16px; right: 16px; bottom: 18px; height: 64px; border-radius: 999px; '
            'background: %s; display: grid; grid-template-columns: repeat(5, minmax(0, 1fr)); align-items: center; padding: 0 6px; box-sizing: border-box;">'
            % BAR + tab('home', 'home', 'Обзор') + tab('ops', 'list', 'Операции') + plus + tab('acc', 'wallet', 'Счета') + tab('more', 'more', 'Ещё') + '</nav>')


def label(text, accent=False, top=18):
    if accent:
        return ('<div style="margin: %dpx 4px 8px; font-size: 13px; font-weight: 600; color: %s; letter-spacing: 0.04em; text-transform: uppercase;">%s</div>'
                % (top, LIME, text))
    return '<div style="margin: %dpx 4px 8px; font-size: 13px; font-weight: 600; color: %s;">%s</div>' % (top, MUTED, text)


def card(inner, pad='0', radius=16, extra=''):
    return ('<div style="border-radius: %dpx; background: %s; border: 1px solid %s; overflow: hidden; padding: %s; %s">%s</div>'
            % (radius, CARD, LINE, pad, extra, inner))


def group(rows):
    out = []
    for i, r in enumerate(rows):
        out.append(r.replace('{{div}}', 'border-top: 1px solid %s;' % DIV if i else ''))
    return card(''.join(out))


def row(title, sub='', lead='', trail='', title_color=INK, min_h=60):
    sub_html = '<span style="font-size: 12px; color: %s;">%s</span>' % (MUTED, sub) if sub else ''
    return ('<div style="display: flex; align-items: center; gap: 12px; min-height: %dpx; padding: 8px 14px; box-sizing: border-box; {{div}}">%s'
            '<span style="flex-grow: 1; min-width: 0; display: flex; flex-direction: column; gap: 2px;"><span style="font-size: 15px; color: %s;">%s</span>%s</span>%s</div>'
            % (min_h, lead, title_color, title, sub_html, trail))


def chevron():
    return svg('chev', MUTED, 18)


def switch(on=True):
    knob = 'right: 3px; background: %s;' % ONLIME if on else 'left: 3px; background: %s;' % MUTED
    return ('<span role="switch" aria-checked="%s" style="position: relative; flex: none; width: 52px; height: 32px; border-radius: 999px; background: %s; border: 1px solid %s;">'
            '<span style="position: absolute; top: 3px; %s width: 24px; height: 24px; border-radius: 50%%;"></span></span>'
            % ('true' if on else 'false', LIME if on else CHIP, LIME if on else LINE2, knob))


def badge(ini, color, ink='#0B0F0A', size=36, radius=11):
    return ('<span style="width: %dpx; height: %dpx; flex: none; border-radius: %dpx; background: %s; color: %s; display: flex; align-items: center; '
            'justify-content: center; font-size: 12px; font-weight: 700;">%s</span>' % (size, size, radius, color, ink, ini))


def dot_icon(icon, color, size=40):
    return ('<span style="width: %dpx; height: %dpx; flex: none; border-radius: 50%%; background: %s; display: flex; align-items: center; justify-content: center;">%s</span>'
            % (size, size, color, svg(icon, ONLIME, size // 2, 1.8)))


def tile_icon(icon, color=LIME, bg=CHIP):
    return ('<span style="width: 40px; height: 40px; flex: none; border-radius: 12px; background: %s; display: flex; align-items: center; justify-content: center;">%s</span>'
            % (bg, svg(icon, color, 22, 1.7)))


def amount(text, cur='', color=INK, size=15):
    c = ' <span style="color: %s; font-size: %dpx;">%s</span>' % (MUTED, size - 3, cur) if cur else ''
    return '<span style="%s font-size: %dpx; color: %s; white-space: nowrap;">%s%s</span>' % (MONO, size, color, text, c)


def tx(title, sub, amt, cur, kind, icon, color, second=''):
    col = {'e': PINK, 'i': CYAN, 't': INK}[kind]
    tail = ('<span style="display: flex; flex-direction: column; align-items: flex-end;">%s%s</span>'
            % (amount(amt, cur, col), '<span style="%s font-size: 12px; color: %s;">%s</span>' % (MONO, MUTED, second) if second else ''))
    return ('<div style="display: flex; align-items: center; gap: 12px; min-height: 64px; padding: 0 16px;">%s'
            '<span style="display: flex; flex-direction: column; flex-grow: 1; min-width: 0;"><span style="font-size: 16px;">%s</span>'
            '<span style="font-size: 13px; color: %s;">%s</span></span>%s</div>' % (dot_icon(icon, color), title, MUTED, sub, tail))


def day(title, total, cur, color=PINK):
    return ('<div style="display: flex; justify-content: space-between; align-items: baseline; padding: 14px 20px 6px;">'
            '<span style="font-size: 13px; font-weight: 600; color: %s; letter-spacing: 0.04em;">%s</span>%s</div>' % (LIME, title, amount(total, cur, color, 13)))


def button(text, kind='primary', icon=None, mt=12, disabled=False):
    styles = {
        'primary': 'border: none; background: %s; color: %s; font-weight: 600;' % (LIME, ONLIME),
        'outline': 'border: 1px solid %s; background: transparent; color: %s;' % (LINE2, INK),
        'danger': 'border: none; background: %s; color: %s; font-weight: 600;' % (PINKBG, PINK),
        'violet': 'border: 1px solid #3A2257; background: transparent; color: #D9C2FF; font-weight: 600;',
        'text': 'border: none; background: transparent; color: %s; font-weight: 600;' % LIME,
    }
    st = styles[kind] + (' opacity: 0.4;' if disabled else '')
    ic = svg(icon, ONLIME if kind == 'primary' else (PINK if kind == 'danger' else INK), 20) if icon else ''
    return ('<button type="button" style="margin-top: %dpx; width: 100%%; height: 54px; border-radius: 999px; font-size: 16px; %s display: flex; '
            'align-items: center; justify-content: center; gap: 8px;">%s%s</button>' % (mt, st, ic, text))


def field(lbl, value, hint='', error='', mono=False, placeholder=False, trail=''):
    border = PINK if error else LINE2
    vcol = MUTED2 if placeholder else INK
    under = ''
    if error:
        under = '<div style="margin: 6px 4px 0; font-size: 12px; color: %s;">%s</div>' % (PINK, error)
    elif hint:
        under = '<div style="margin: 6px 4px 0; font-size: 12px; line-height: 1.35; color: %s;">%s</div>' % (MUTED, hint)
    return ('<div style="margin-top: 12px;"><label style="display: flex; align-items: center; gap: 8px; min-height: 56px; padding: 6px 14px; box-sizing: border-box; '
            'border-radius: 12px; background: %s; border: 1px solid %s;"><span style="flex-grow: 1; display: flex; flex-direction: column; gap: 2px;">'
            '<span style="font-size: 12px; color: %s;">%s</span><span style="font-size: 16px; color: %s; %s">%s</span></span>%s</label>%s</div>'
            % (FIELD, border, PINK if error else MUTED, lbl, vcol, MONO if mono else '', value, trail, under))


def search(placeholder, value=''):
    v = ('<span style="flex-grow: 1; color: %s; font-size: 15px;">%s</span>' % (INK, value) if value
         else '<span style="flex-grow: 1; color: %s; font-size: 15px;">%s</span>' % (MUTED2, placeholder))
    x = svg('close', MUTED, 18) if value else ''
    return ('<label style="display: flex; align-items: center; gap: 10px; height: 48px; padding: 0 16px; border-radius: 999px; background: %s;">%s%s%s</label>'
            % (FIELD, svg('search', MUTED, 20), v, x))


def segmented(options, selected, tone='violet'):
    on_bg, on_ink = (LIME, ONLIME) if tone == 'lime' else (VIOLETPILL, VIOLETINK)
    cells = ''.join('<span style="height: 40px; border-radius: 999px; display: flex; align-items: center; justify-content: center; font-size: 14px; %s">%s</span>'
                    % ('background: %s; color: %s; font-weight: 600;' % (on_bg, on_ink) if o == selected else 'color: %s;' % INK2, o) for o in options)
    return ('<div style="display: grid; grid-template-columns: repeat(%d, minmax(0, 1fr)); padding: 4px; border-radius: 999px; background: %s;">%s</div>'
            % (len(options), CHIP, cells))


def chip(text, on=False, icon=None):
    st = ('border: none; background: %s; color: %s; font-weight: 600;' % (VIOLETPILL, VIOLETINK) if on
          else 'border: 1px solid %s; background: transparent; color: %s;' % (LINE2, INK))
    return ('<span style="height: 36px; padding: 0 14px; border-radius: 999px; %s font-size: 13px; display: inline-flex; align-items: center; gap: 6px; '
            'white-space: nowrap;">%s%s</span>' % (st, svg(icon, MUTED, 16) if icon else '', text))


def message(text, kind='ok', title=''):
    col, bg, line, icon = {'ok': (OK, '#0B1A10', '#1F3A28', 'check'), 'error': (PINK, PINKBG, '#5C1C26', 'alert'),
                           'warn': (WARN, WARNBG, WARNLINE, 'alert'), 'info': (CYAN, '#062224', '#0E4448', 'info')}[kind]
    t = '<span style="font-size: 14px; font-weight: 600; color: %s;">%s</span>' % (col, title) if title else ''
    return ('<div style="margin-top: 12px; display: flex; gap: 10px; padding: 12px 14px; border-radius: 16px; background: %s; border: 1px solid %s;">%s'
            '<span style="display: flex; flex-direction: column; gap: 3px;">%s<span style="font-size: 13px; line-height: 1.4;">%s</span></span></div>'
            % (bg, line, svg(icon, col, 20, 1.9), t, text))


def hero(icon, title, sub='', color=LIME, ring=False, top=40):
    r = 'box-shadow: 0 0 0 10px %s;' % LIMEPILL if ring else ''
    s = '<p style="margin: 8px 12px 0; font-size: 14px; line-height: 1.45; color: %s;">%s</p>' % (MUTED, sub) if sub else ''
    return ('<div style="display: flex; flex-direction: column; align-items: center; text-align: center; padding-top: %dpx;">'
            '<span style="width: 88px; height: 88px; border-radius: 50%%; background: %s; display: flex; align-items: center; justify-content: center; %s">%s</span>'
            '<h2 style="margin: 24px 0 0; font-size: 24px;">%s</h2>%s</div>' % (top, color, r, svg(icon, ONLIME, 40, 2.2), title, s))


def empty(icon, text, cta=''):
    c = ('<span style="margin-top: 18px; height: 44px; padding: 0 18px; border-radius: 999px; background: %s; color: %s; font-weight: 600; font-size: 15px; '
         'display: inline-flex; align-items: center; gap: 6px;">%s%s</span>' % (LIME, ONLIME, svg('plus', ONLIME, 18, 2.2), cta) if cta else '')
    return ('<div style="display: flex; flex-direction: column; align-items: center; text-align: center; padding: 96px 28px 0;">'
            '<span style="width: 72px; height: 72px; border-radius: 24px; background: %s; display: flex; align-items: center; justify-content: center;">%s</span>'
            '<p style="margin: 18px 0 0; font-size: 15px; line-height: 1.45; color: %s;">%s</p>%s</div>' % (CHIP, svg(icon, LIME, 32, 1.6), INK2, text, c))


def scrim(under=''):
    return '<div style="position: absolute; inset: 0; opacity: 0.35;">%s</div><div style="position: absolute; inset: 0; background: rgba(0,0,0,0.6);"></div>' % under


def dialog(title, text, actions, body=''):
    """actions: list of (label, kind) — kind: 'text', 'primary', 'danger'."""
    btns = ''
    for lbl, kind in actions:
        st = {'text': 'background: transparent; color: %s;' % LIME, 'primary': 'background: %s; color: %s;' % (LIME, ONLIME),
              'danger': 'background: %s; color: %s;' % (PINKBG, PINK)}[kind]
        btns += ('<span style="height: 44px; padding: 0 16px; border-radius: 999px; %s font-size: 15px; font-weight: 600; display: inline-flex; align-items: center;">%s</span>'
                 % (st, lbl))
    t = '<p style="margin: 12px 0 0; font-size: 14px; line-height: 1.45; color: %s;">%s</p>' % (INK2, text) if text else ''
    return ('<div role="dialog" aria-label="%s" style="position: absolute; left: 24px; right: 24px; top: 50%%; transform: translateY(-50%%); padding: 24px; '
            'border-radius: 28px; background: %s; box-shadow: 0 16px 48px rgba(0,0,0,0.45);"><h2 style="margin: 0; font-size: 20px;">%s</h2>%s%s'
            '<div style="margin-top: 20px; display: flex; justify-content: flex-end; gap: 8px;">%s</div></div>' % (title, CHIP, title, t, body, btns))


def sheet(inner, top=56):
    return ('<div style="position: absolute; left: 0; right: 0; top: %dpx; bottom: 0; background: %s; border-radius: 28px 28px 0 0; overflow: hidden;">'
            '<div style="display: flex; justify-content: center; padding-top: 10px;"><div style="width: 36px; height: 4px; border-radius: 2px; background: #4A5444;"></div></div>'
            '%s</div>' % (top, SHEET, inner))


def snackbar(text, action='Отменить', bottom=96):
    return ('<div style="position: absolute; left: 16px; right: 16px; bottom: %dpx; border-radius: 12px; background: %s; overflow: hidden;">'
            '<div style="display: flex; align-items: center; gap: 10px; min-height: 48px; padding: 0 8px 0 16px;"><span style="flex-grow: 1; font-size: 14px;">%s</span>'
            '<span style="padding: 0 12px; color: %s; font-size: 14px; font-weight: 600;">%s</span></div><div style="height: 3px; width: 62%%; background: %s;"></div></div>'
            % (bottom, CHIP, text, LIME, action, LIME))


def progress(frac, mt=12):
    return ('<div style="margin-top: %dpx; height: 6px; border-radius: 3px; background: %s; overflow: hidden;"><div style="width: %d%%; height: 100%%; background: %s;"></div></div>'
            % (mt, LINE2, round(frac * 100), LIME))


def note(text, mt=12):
    return '<p style="margin: %dpx 4px 0; font-size: 13px; line-height: 1.45; color: %s;">%s</p>' % (mt, MUTED, text)
