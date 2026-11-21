#!/usr/bin/env python3
"""Shared bricks of the «ИИ на телефоне» poll boards (#583: #584 #585 #586 #587 #592 #593 #594). Colours — real «Лайм» (`mockup_poll_update.theme`).

Only proposals live here (standalone PNGs for the poll); after the owner's «ок» the winners go to the canvas boards and the component cards.
"""
import os
import sys

HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, HERE)
import mockup_poll_accounts4 as A  # noqa: E402
import mockup_poll_update as U  # noqa: E402

MONO = U.MONO
P = dict(
    spark='M12 3l1.8 5.2L19 10l-5.2 1.8L12 17l-1.8-5.2L5 10l5.2-1.8zM18 15l.8 2.2L21 18l-2.2.8L18 21l-.8-2.2L15 18l2.2-.8z',
    qr='M4 8V5a1 1 0 0 1 1-1h3M16 4h3a1 1 0 0 1 1 1v3M20 16v3a1 1 0 0 1-1 1h-3M8 20H5a1 1 0 0 1-1-1v-3M8 8h3v3H8zM13 8h3v3h-3zM8 13h3v3H8zM13 13h3v3',
    mic='M12 3a3 3 0 0 1 3 3v5a3 3 0 0 1-6 0V6a3 3 0 0 1 3-3zM6 11a6 6 0 0 0 12 0M12 17v4',
    hist='M4 12a8 8 0 1 0 3-6.2M4 4v4h4M12 8v4l3 2',
    chev='M9 6l6 6-6 6', back='M15 6l-6 6 6 6', check='M5 12l5 5 9-10', close='M6 6l12 12M18 6L6 18',
    note='M6 3h9l4 4v14H6zM9 12h7M9 16h5', bell='M6 16V11a6 6 0 0 1 12 0v5l2 2H4zM10 20a2 2 0 0 0 4 0',
    draft='M5 4h14v16H5zM8 9h8M8 13h8M8 17h4', chip='M7 7h10v10H7zM4 9h3M4 15h3M17 9h3M17 15h3M9 4v3M15 4v3M9 17v3M15 17v3',
    cloud='M7 18a4 4 0 0 1-.5-8A6 6 0 0 1 18 9a4.5 4.5 0 0 1 0 9zM12 9v6M9 12l3 3 3-3', trash='M5 7h14M10 7V4h4v3M7 7l1 13h8l1-13',
    gear='M12 8a4 4 0 1 0 0 8 4 4 0 0 0 0-8zM12 2v3M12 19v3M2 12h3M19 12h3', wallet='M4 7h15a1 1 0 0 1 1 1v10a1 1 0 0 1-1 1H5a1 1 0 0 1-1-1zM4 7l12-3v3M16 13h3',
    home='M4 11l8-7 8 7v9H4z', list='M5 6h14M5 12h14M5 18h14', chart='M5 20V10M12 20V4M19 20v-8', more='M5 12h.01M12 12h.01M19 12h.01', plus='M12 5v14M5 12h14',
)


def ic(name, color, size=20, w=1.8):
    return ('<svg width="%d" height="%d" viewBox="0 0 24 24" fill="none" stroke="%s" stroke-width="%s" stroke-linecap="round" stroke-linejoin="round" style="flex:none"><path d="%s"/></svg>'
            % (size, size, color, w, P[name]))


def ink_acc(t):
    return t['acc'] if t['dark'] else '#5B1FA8'


def tag(t, text, icon='spark', warn=False):
    """Status capsule (Style: height 22, tone 18 %, colour only in the point/icon)."""
    col = t['warm'] if warn else ink_acc(t)
    bg = U.mix(t['page'], t['warm'] if warn else t['acc'], .18)
    return ('<span style="display:inline-flex;align-items:center;gap:4px;height:22px;padding:0 8px;border-radius:999px;background:%s;font-size:12px;white-space:nowrap">%s<span style="color:%s">%s</span></span>'
            % (bg, ic(icon, col, 14, 2), t['ink'], text))


def caption(t, text, icon='hist'):
    """The quiet grey line: where an automatic value came from."""
    return '<span style="display:inline-flex;align-items:center;gap:5px;font-size:13px;color:%s;letter-spacing:0">%s%s</span>' % (t['muted'], ic(icon, t['muted'], 14, 2), text)


def round_btn(t, name, size=44, filled=False):
    bg = t['prim'] if filled else t['bar']
    col = t['onp'] if filled else t['ink']
    return '<span style="width:%dpx;height:%dpx;flex:none;border-radius:50%%;background:%s;display:flex;align-items:center;justify-content:center">%s</span>' % (size, size, bg, ic(name, col, size // 2, 1.9))


def top(t, title, right=''):
    return ('<div style="margin:8px 32px 12px;height:56px;border-radius:999px;background:%s;display:flex;align-items:center;padding:0 4px;gap:4px">'
            '<span style="width:48px;display:flex;justify-content:center">%s</span><b style="font-size:22px;flex:1;white-space:nowrap">%s</b>%s</div>' % (t['bar'], ic('back', t['ink'], 22, 1.9), title, right))


def screen(t, title, body, h=844, right=''):
    return U.phone(t, '<div style="position:absolute;inset:0;padding-top:30px">%s%s</div>' % (top(t, title, right), body), h)


def card(t, body, pad='14px 16px', extra=''):
    return '<div style="margin:0 16px 10px;padding:%s;border-radius:24px;background:%s;border:1px solid %s;%s">%s</div>' % (pad, t['surf'], t['line'], extra, body)


def cta(t, label, filled=True, bottom=16, icon=None):
    st = ('background:%s;color:%s;font-weight:700;box-shadow:inset 0 0 0 2px %s' % (t['prim'], t['onp'], A.edge(t))) if filled else 'background:%s;color:%s' % (t['bar'], t['ink'])
    glyph = ic(icon, t['onp'] if filled else t['ink'], 20, 2) + ' ' if icon else ''
    return ('<div style="position:absolute;left:16px;right:16px;bottom:%dpx;height:52px;border-radius:999px;display:flex;align-items:center;justify-content:center;gap:6px;font-size:15px;%s">%s%s</div>'
            % (bottom, st, glyph, label))


def btn_row(t, label, filled=False, icon=None, h=48):
    st = ('background:%s;color:%s;font-weight:700;box-shadow:inset 0 0 0 2px %s' % (t['prim'], t['onp'], A.edge(t))) if filled else 'background:%s;color:%s' % (t['bar'], t['ink'])
    glyph = ic(icon, t['onp'] if filled else t['ink'], 18, 2) if icon else ''
    return '<div style="height:%dpx;border-radius:999px;display:flex;align-items:center;justify-content:center;gap:6px;font-size:14px;%s">%s%s</div>' % (h, st, glyph, label)


def row(t, icon, title, sub='', right='', color=None):
    col = color or t['acc']
    return ('<div style="display:flex;align-items:center;gap:12px;min-height:56px;padding:6px 0"><span style="width:32px;height:32px;border-radius:10px;flex:none;background:%s;display:flex;align-items:center;justify-content:center">%s</span>'
            '<span style="flex:1;min-width:0"><div style="font-size:16px">%s</div><div style="font-size:13px;color:%s">%s</div></span><span style="display:flex;align-items:center;gap:6px;font-size:14px;color:%s">%s</span></div>'
            % (U.mix(t['page'], col, .18), ic(icon, ink_acc(t), 18, 1.9), title, t['muted'], sub, t['muted'], right))


def overlay(base, extra):
    i = base.rfind('</div>')
    return base[:i] + '<div style="position:absolute;inset:0;background:rgba(0,0,0,.45)"></div>' + extra + base[i:]


def dialog(t, icon, title, text, buttons, top_px=270):
    return ('<div style="position:absolute;left:24px;right:24px;top:%dpx;padding:20px;border-radius:28px;background:%s;border:1px solid %s"><div style="width:48px;height:48px;border-radius:16px;background:%s;display:flex;align-items:center;justify-content:center;margin-bottom:12px">%s</div>'
            '<div style="font-size:18px;font-weight:700">%s</div><div style="font-size:14px;color:%s;margin-top:6px;line-height:1.4">%s</div><div style="display:flex;flex-direction:column;gap:8px;margin-top:16px">%s</div></div>'
            % (top_px, t['page'], t['line'], U.mix(t['page'], t['acc'], .18), ic(icon, ink_acc(t), 26, 1.8), title, t['muted'], text, buttons))


def navbar(t, active=0):
    cells = ''
    for i, k in enumerate(['home', 'list', 'plus', 'chart', 'more']):
        if k == 'plus':
            cells += '<span style="width:48px;height:48px;border-radius:50%%;background:%s;display:flex;align-items:center;justify-content:center">%s</span>' % (t['prim'], ic('plus', t['onp'], 26, 2.2))
        else:
            on = i == active
            cells += ('<span style="height:40px;display:flex;align-items:center;justify-content:center;border-radius:999px;margin:0 2px;%s">%s</span>'
                      % (('background:%s;box-shadow:inset 0 0 0 2px %s' % (t['prim'], A.edge(t))) if on else '', ic(k, t['onp'] if on else t['muted'], 24, 1.8)))
    return ('<div style="position:absolute;left:32px;right:32px;bottom:14px;height:56px;border-radius:999px;background:%s;display:grid;grid-template-columns:repeat(5,1fr);align-items:center;padding:0 4px">%s</div>' % (t['bar'], cells))


def cat_card(t, picked, right='', names=None):
    """The «Категория» card of the entry sheet with one chosen cell (ring), [right] — the caption of an automatic choice."""
    names = names or [('Кафе', 'cafe'), ('Продукты', 'cart'), ('Такси', 'taxi'), ('Дом', 'home'), ('Здоровье', 'heart'), ('Одежда', 'bag'), ('Подарки', 'gift')]
    cells = ''
    for i, (nm, _) in enumerate(names):
        ring = 'box-shadow:0 0 0 2px %s;' % A.edge(t) if i == picked else ''
        fill = t['prim'] if i == picked else U.mix(t['page'], t['acc'], .22)
        cells += ('<div style="display:flex;flex-direction:column;align-items:center;gap:2px;width:25%%"><span style="width:36px;height:36px;border-radius:50%%;background:%s;%s"></span>'
                  '<span style="font-size:12px;font-weight:%d">%s</span></div>' % (fill, ring, 700 if i == picked else 400, nm))
    cells += ('<div style="display:flex;flex-direction:column;align-items:center;gap:2px;width:25%%"><span style="height:36px;padding:0 12px;border-radius:999px;background:%s;display:flex;align-items:center;font-size:13px">Все 24</span></div>' % t['bar'])
    return A.card(t, 'КАТЕГОРИЯ', '<div style="display:flex;flex-wrap:wrap;gap:4px 0;padding-top:4px">%s</div>' % cells, right)
