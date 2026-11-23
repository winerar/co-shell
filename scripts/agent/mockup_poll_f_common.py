#!/usr/bin/env python3
"""Shared bricks for the standalone poll boards of #552/#553/#554 (non-AI features, session 2026-11-23).

Each feature module has BOARDS = [(name, fn(t) -> html, width, height)] and calls run(BOARDS, out) from main.
Output: <out>/html/<Name>.html + <Name>@light.html → <out>/img/*.png (dark «Лайм» + light), then poll-page.py --img.
"""
import os
import subprocess
import sys

HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, HERE)
import mockup_bricks as B  # noqa: E402
import mockup_nav_poll as N  # noqa: E402
import mockup_poll_accounts4 as A  # noqa: E402
import mockup_poll_update as U  # noqa: E402

MONO = U.MONO
mix = U.mix
CHEV_L, CHEV_R = 'M15 6l-6 6 6 6', 'M9 6l6 6-6 6'


def ink2(t):
    return t['acc'] if t['dark'] else '#5B1FA8'


def edge(t):
    return A.edge(t)


def glyph(path, color, size=22):
    return N.glyph(path, color, size)


def phone(t, body, w, h):
    return U.phone(t, body, h).replace('width:390px', 'width:%dpx' % w, 1)


def topbar(t, title, back=True, right=''):
    left = '<span style="width:48px;display:flex;justify-content:center">%s</span>' % glyph(CHEV_L, t['ink']) if back else '<span style="width:16px"></span>'
    return ('<div style="position:absolute;top:8px;left:32px;right:32px;height:56px;border-radius:999px;background:%s;display:flex;align-items:center;'
            'padding:0 4px;z-index:3">%s<b style="font-size:%dpx;flex:1">%s</b>%s</div>' % (t['bar'], left, 20 if back else 22, title, right))


def card(t, head='', right='', body='', extra=''):
    top = ''
    if head or right:
        top = ('<div style="display:flex;justify-content:space-between;align-items:center;font-size:16px;font-weight:600"><span>%s</span>'
               '<span style="font-size:14px;font-weight:400;color:%s">%s</span></div>' % (head, t['muted'], right))
    return ('<div style="margin:12px 16px 0;padding:14px 16px;border-radius:24px;background:%s;border:1px solid %s;%s">%s%s</div>' % (t['surf'], t['line'], extra, top, body))


def hero(t, body, extra=''):
    """Main card of a screen: theme gradient (hero tone)."""
    g = ('linear-gradient(135deg,%s,%s)' % (mix(t['page'], t['prim'], .22), mix(t['page'], t['acc'], .3))) if t['dark'] else \
        ('linear-gradient(135deg,%s,%s)' % (mix('#FFFFFF', t['prim'], .55), mix('#FFFFFF', t['acc'], .25)))
    return '<div style="margin:12px 16px 0;padding:16px;border-radius:24px;background:%s;border:1px solid %s;%s">%s</div>' % (g, t['line'], extra, body)


def pill(t, text, on=False, h=36, extra=''):
    st = ('background:%s;box-shadow:inset 0 0 0 2px %s;font-weight:700' % (t['prim'], edge(t)) + ';color:' + t['onp']) if on else \
        'background:%s;color:%s' % (t['wash'], t['ink'])
    return '<span style="height:%dpx;padding:0 14px;border-radius:999px;display:inline-flex;align-items:center;font-size:14px;white-space:nowrap;%s;%s">%s</span>' % (h, st, extra, text)


def segmented(t, labels, sel):
    cells = ''.join('<span style="flex:1;height:36px;border-radius:999px;display:flex;align-items:center;justify-content:center;font-size:14px;%s">%s</span>' % (
        ('background:%s;color:%s;font-weight:700;box-shadow:inset 0 0 0 2px %s' % (t['prim'], t['onp'], edge(t))) if i == sel else 'color:%s' % t['ink'], s)
        for i, s in enumerate(labels))
    return '<div style="margin:12px 16px 0;padding:4px;border-radius:999px;background:%s;display:flex;gap:4px">%s</div>' % (t['wash'], cells)


def button(t, label, extra=''):
    return ('<div style="height:48px;border-radius:999px;background:%s;color:%s;display:flex;align-items:center;justify-content:center;font-weight:700;'
            'font-size:16px;box-shadow:inset 0 0 0 1px %s;%s">%s</div>' % (t['prim'], t['onp'], edge(t), extra, label))


def badge(t, icon, color, size=40, circle=True):
    r = '50%' if circle else '12px'
    return ('<span style="width:%dpx;height:%dpx;border-radius:%s;background:%s;display:inline-flex;align-items:center;justify-content:center;flex:none">%s</span>' % (
        size, size, r, mix(t['page'], color, .25 if t['dark'] else .3), B.svg(B.ICON.get(icon, B.ICON['user']), color if t['dark'] else mix(color, '#000000', .55), size // 2 + 2, 1.8)))


def row(t, icon, color, title, sub, right, right_sub='', circle=True):
    rs = '<div style="font-size:12px;color:%s">%s</div>' % (t['muted'], right_sub) if right_sub else ''
    return ('<div style="display:flex;align-items:center;gap:12px;padding:8px 16px;min-height:60px">%s<div style="flex:1;min-width:0"><div style="font-size:16px;white-space:nowrap;'
            'overflow:hidden;text-overflow:ellipsis">%s</div><div style="font-size:13px;color:%s">%s</div></div><div style="text-align:right"><div style="%sfont-size:15px">%s</div>%s</div></div>' % (
                badge(t, icon, color, 40, circle), title, t['muted'], sub, MONO, right, rs))


def sheet(t, title, body, h_top, w=390, sub=''):
    """Bottom sheet over a dimmed page; [h_top] is the page strip left above it."""
    s = '<div style="font-size:13px;color:%s">%s</div>' % (t['muted'], sub) if sub else ''
    return ('<div style="position:absolute;inset:0;background:rgba(0,0,0,.45);z-index:5"></div><div style="position:absolute;left:0;right:0;bottom:0;top:%dpx;z-index:6;'
            'border-radius:28px 28px 0 0;background:%s;border-top:1px solid %s;padding:12px 16px 16px"><div style="width:36px;height:4px;border-radius:2px;background:%s;margin:0 auto 10px"></div>'
            '<div style="display:flex;justify-content:space-between;align-items:center"><div><b style="font-size:20px">%s</b>%s</div>%s</div>%s</div>' % (
                h_top, t['page'], t['line'], t['muted'], title, s, glyph(B.ICON['close'], t['ink'], 22), body))


def nav(t, active='chart'):
    cells = ''
    for k in ['home', 'list', '+', 'chart', 'more']:
        if k == '+':
            cells += '<span style="width:48px;height:48px;border-radius:50%%;background:%s;display:flex;align-items:center;justify-content:center">%s</span>' % (t['prim'], B.svg('plus', t['onp'], 26, 2.2))
        elif k == active:
            cells += '<span style="height:44px;width:64px;border-radius:999px;background:%s;display:flex;align-items:center;justify-content:center">%s</span>' % (t['prim'], B.svg(B.ICON[k], t['onp'], 24, 1.7))
        else:
            cells += '<span style="display:flex;justify-content:center">%s</span>' % B.svg(B.ICON[k], t['muted'], 24, 1.7)
    return ('<div style="position:absolute;left:32px;right:32px;bottom:16px;height:56px;border-radius:999px;background:%s;display:grid;'
            'grid-template-columns:repeat(5,1fr);align-items:center;justify-content:center;justify-items:center;z-index:3">%s</div>' % (t['bar'], cells))


def caption(t, text, top, w=390):
    return ('<div style="position:absolute;left:16px;right:16px;bottom:%dpx;z-index:9;padding:8px 12px;border-radius:16px;background:%s;color:%s;font-size:13px;'
            'line-height:1.35;box-shadow:inset 0 0 0 1px %s">%s</div>' % (84, ink2(t), '#FFFFFF' if not t['dark'] else '#1A0830', ink2(t), text))


def meter(t, frac, color=None):
    c = color or t['prim']
    return ('<div style="height:8px;border-radius:999px;background:%s"><div style="width:%d%%;height:8px;border-radius:999px;background:%s;box-shadow:inset 0 0 0 1px %s"></div></div>' % (
        t['wash'], round(frac * 100), c, edge(t)))


def run(boards, out):
    os.makedirs(os.path.join(out, 'html'), exist_ok=True)
    for name, fn, w, h in boards:
        for suffix, dark in (('', True), ('@light', False)):
            t = U.theme(dark)
            open(os.path.join(out, 'html', name + suffix + '.html'), 'w', encoding='utf-8').write(N.page(phone(t, fn(t), w, h), w, h, dict(page=t['page'])))
    js = os.path.join(out, 'render.js')
    src = open(os.path.join(HERE, 'mockup_poll_update.py'), encoding='utf-8').read()
    open(js, 'w').write(src[src.index("'''const") + 3:src.index("''')")].replace('\\\\d', '\\d'))
    subprocess.run(['node', js, os.path.abspath(out)], check=True, cwd=out)
    print('%d boards → %s/img' % (len(boards) * 2, out))
