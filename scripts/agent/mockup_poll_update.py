#!/usr/bin/env python3
"""Poll boards for the update screen: less black, more of the two palette colours (palette «Лайм», real app colours).

Usage: python3 scripts/agent/mockup_poll_update.py <out-dir>  → <out-dir>/img/*.png (dark and «@light»).
Then: agent.sh poll docs/design/poll-update-1118.json --img <out-dir>/img
"""
import math
import os
import subprocess
import sys

HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, HERE)
import mockup_nav_poll as N  # noqa: E402
import mockup_bricks as B  # noqa: E402

MONO = "font-family:'JetBrains Mono',monospace;"


def hx(c):
    return tuple(int(c[i:i + 2], 16) for i in (1, 3, 5))


def mix(a, b, t):
    x, y = hx(a), hx(b)
    return '#%02X%02X%02X' % tuple(round(x[i] + (y[i] - x[i]) * t) for i in range(3))


def theme(dark):
    """The colours of «Лайм» as ColorSchemes.kt derives them."""
    prim, onp = '#B8FF2E', '#0E1F00'
    if dark:
        acc, bg = '#C27BFF', '#050805'
        page = mix(bg, prim, .06)
        return dict(dark=True, page=page, surf=mix(page, prim, .07), disc=mix(page, acc, .2), wash=mix(page, acc, .26), acc=acc, prim=prim,
                    onp=onp, ink=mix('#FFFFFF', page, .08), muted=mix('#FFFFFF', page, .32), line=mix(page, prim, .45), bar=mix(page, acc, .14),
                    warm='#FFE14D')
    acc, bg = '#7A2BD6', '#F7FFEA'
    ink = mix('#000000', bg, .1)
    return dict(dark=False, page='#FFFFFF', surf=mix('#FFFFFF', prim, .12), disc=mix('#FFFFFF', acc, .14), wash=mix('#FFFFFF', acc, .18), acc=acc,
                prim=prim, onp=onp, ink=ink, muted=mix('#000000', bg, .36), line=mix('#FFFFFF', ink, .42), bar=mix('#FFFFFF', acc, .1), warm='#6E6200')


def phone(t, body, h):
    return '<div style="width:390px;height:%dpx;position:relative;overflow:hidden;background:%s;border-radius:24px;color:%s">%s</div>' % (h, t['page'], t['ink'], body)


def topbar(t):
    return ('<div style="margin:8px 32px;height:56px;border-radius:999px;background:%s;display:flex;align-items:center;padding:0 4px">'
            '<span style="width:48px;display:flex;justify-content:center">%s</span><b style="font-size:22px">Обновление</b></div>' % (t['bar'], N.glyph(N.ARROW, t['ink'], 22)))


def check_edge(t, size=56):
    """Lime check mark with a thin deep-green edge in the light theme (plain lime in the dark one)."""
    if t['dark']:
        return check(t['prim'], size)
    return B.svg('check', mix(t['prim'], '#000000', .5), size, 4.6).replace('</svg>', '') + '<path d="%s" stroke="%s" stroke-width="2.4" fill="none" stroke-linecap="round" stroke-linejoin="round"/></svg>' % (B.ICON['check'], t['prim'])


def check(color, size=40):
    return B.svg('check', color, size, 2.4)


def ring(t, v, frac, mid, avail=False):
    """v: A — now (graphite arc, neon track in light); B — arc in the second colour on its wash; C — B + thin inner neon ring and a neon bead."""
    r, sw, c = 78, 8, 2 * math.pi * 78
    dark = t['dark']
    arc = t['prim'] if dark else (t['ink'] if v == 'A' else (t['acc'] if v in 'BCR' else t['prim']))
    edge = ''
    if not dark and v in 'PQ':  # neon arc on white: thin deep-green edge (fillEdge)
        edge = ('<circle cx="84" cy="84" r="%d" fill="none" stroke="%s" stroke-width="%d" stroke-linecap="round" stroke-dasharray="%.1f %.1f" transform="rotate(-90 84 84)"/>'
                % (r, mix(t['prim'], '#000000', .5), sw + 3, c * frac, c))
    track = t['wash'] if dark else (t['prim'] if v == 'A' else t['wash'])
    extra = ''
    if v == 'C':
        extra = '<circle cx="84" cy="84" r="64" fill="none" stroke="%s" stroke-width="2" opacity=".9"/>' % t['prim']
        a = -math.pi / 2 + 2 * math.pi * frac
        if 0 < frac < 1:
            extra += ('<circle cx="%.1f" cy="%.1f" r="7" fill="%s" stroke="%s" stroke-width="2"/>' % (84 + r * math.cos(a), 84 + r * math.sin(a), t['prim'], t['acc']))
    disc = t['prim'] if avail else t['disc']
    return ('<div style="position:relative;width:168px;height:168px;margin:0 auto"><svg width="168" height="168">'
            '<circle cx="84" cy="84" r="%d" fill="none" stroke="%s" stroke-width="%d"/>%s'
            '<circle cx="84" cy="84" r="%d" fill="none" stroke="%s" stroke-width="%d" stroke-linecap="round" stroke-dasharray="%.1f %.1f" transform="rotate(-90 84 84)"/>%s</svg>'
            '<div style="position:absolute;left:18px;top:18px;right:18px;bottom:18px;border-radius:50%%;background:%s;display:flex;flex-direction:column;align-items:center;justify-content:center">%s</div></div>'
            % (r, track, sw, edge, r, arc, sw, c * frac, c, extra, disc, mid))


def texts(t, title, sub):
    return ('<h2 style="margin:22px 0 0;text-align:center;font-size:22px;font-weight:600">%s</h2>'
            '<div style="text-align:center;margin-top:8px;font-size:14px;color:%s">%s</div>' % (title, t['muted'], sub))


def btn(t, label, filled=True):
    st = ('background:%s;color:%s' % (t['prim'], t['onp'])) if filled else 'border:1.5px solid %s;color:%s' % (t['line'], t['ink'])
    return ('<div style="position:absolute;left:16px;right:16px;bottom:16px;height:52px;border-radius:999px;display:flex;align-items:center;justify-content:center;font-weight:600;%s">%s</div>' % (st, label))


def ring_down(t, v):
    mid = ('<span style="%sfont-size:30px;font-weight:700">45%%</span><span style="%sfont-size:12px;color:%s">3,6 из 8,1 МБ</span>' % (MONO, MONO, t['muted']))
    return phone(t, topbar(t) + '<div style="padding-top:36px">%s%s</div>' % (ring(t, v, .45, mid), texts(t, 'Скачиваем…', 'Сборка 400 · 8,1 МБ')), 460)


def ring_ok(t, v):
    ic = t['ink'] if (v == 'A' and not t['dark']) else (t['prim'] if v == 'A' else t['acc'])
    mark = check_edge(t) if v in 'PR' else (check(t['prim'] if t['dark'] else mix(t['prim'], '#000000', .62), 56) if v == 'Q' else check(ic, 56))
    return phone(t, topbar(t) + '<div style="padding-top:36px">%s%s</div>' % (ring(t, v, 0, mark), texts(t, 'У вас последняя версия', 'Проверено только что')) + btn(t, 'Проверить снова', False), 460)


def vcol(t, label, name, build, color, extra=''):
    return ('<div style="flex:1">%s<div style="font-size:12px;color:%s">%s</div><div style="%sfont-size:24px;font-weight:700;color:%s">%s</div>'
            '<div style="font-size:12px;color:%s;margin-top:2px">%s</div></div>' % (extra, t['muted'], label, MONO, color, name, t['muted'], build))


def versions(t, v):
    card = 'border-radius:24px;border:1px solid %s;' % t['line']
    if v == 'A':
        return ('<div style="margin:0 16px;%sbackground:%s;padding:16px;display:flex;align-items:center">%s<span style="color:%s;font-size:20px;padding:0 12px">→</span>%s</div>'
                % (card, t['surf'], vcol(t, 'Сейчас', '0.36', 'сборка 399', t['ink']), t['muted'], vcol(t, 'Новая', '0.37', 'сборка 400', t['ink'])))
    if v == 'B':  # the new one on a neon fill
        return ('<div style="margin:0 16px;display:flex;gap:8px;align-items:stretch">'
                '<div style="flex:1;%sbackground:%s;padding:16px">%s</div>'
                '<div style="flex:1;border-radius:24px;border:1px solid %s;background:%s;color:%s;padding:16px">'
                '<div style="font-size:12px;opacity:.75">Новая</div><div style="%sfont-size:24px;font-weight:700">0.37</div><div style="font-size:12px;opacity:.75;margin-top:2px">сборка 400</div></div></div>'
                % (card, t['surf'], vcol(t, 'Сейчас', '0.36', 'сборка 399', t['ink']).replace('flex:1', ''),
                   mix(t['prim'], t['onp'], .35), t['prim'], t['onp'], MONO))
    if v in ('S', 'N'):  # tile styles swapped: «Сейчас» takes the accent frame, «Новая» the calm tile (N — neon)
        arrow = N.glyph('M5 12h14M13 6l6 6-6 6', '#FFFFFF' if not t['dark'] else '#1A0830', 18, 2.4)
        circ = ('<span style="width:36px;height:36px;border-radius:50%%;background:%s;display:flex;align-items:center;justify-content:center;flex:none;margin:0 -18px;z-index:1;border:3px solid %s">%s</span>' % (t['acc'], t['page'], arrow))
        new_bg = t['prim'] if v == 'N' else t['surf']
        new_ink = t['onp'] if v == 'N' else t['ink']
        return ('<div style="margin:0 16px;display:flex;align-items:center">'
                '<div style="flex:1;border-radius:24px;border:1px solid %s;background:%s;padding:16px 28px 16px 16px">%s</div>%s'
                '<div style="flex:1;border-radius:24px;border:1px solid %s;background:%s;padding:16px 16px 16px 28px;color:%s">%s'
                '<div style="margin-top:8px;display:inline-block;padding:2px 10px;border-radius:999px;background:%s;color:%s;font-size:12px;font-weight:600">+1 сборка · 8,1 МБ</div></div></div>'
                % (t['acc'], mix(t['page'], t['acc'], .12), vcol(t, 'Сейчас', '0.36', 'сборка 399', t['acc']).replace('flex:1', ''), circ,
                   t['line'], new_bg, new_ink, vcol(t, 'Новая', '0.37', 'сборка 400', new_ink).replace('flex:1', ''),
                   t['acc'], '#FFFFFF' if not t['dark'] else '#1A0830'))
    chip = ('<span style="width:36px;height:36px;border-radius:50%%;background:%s;color:%s;display:flex;align-items:center;justify-content:center;flex:none;margin:0 -18px;z-index:1;border:3px solid %s">%s</span>'
            % (t['acc'], '#FFFFFF' if not t['dark'] else '#1A0830', t['page'], N.glyph('M5 12h14M13 6l6 6-6 6', '#FFFFFF' if not t['dark'] else '#1A0830', 18, 2.4)))
    return ('<div style="margin:0 16px;display:flex;align-items:center">'
            '<div style="flex:1;%sbackground:%s;padding:16px 28px 16px 16px">%s</div>%s'
            '<div style="flex:1;border-radius:24px;border:1px solid %s;background:%s;padding:16px 16px 16px 28px">%s'
            '<div style="margin-top:8px;display:inline-block;padding:2px 10px;border-radius:999px;background:%s;color:%s;font-size:12px;font-weight:600">+1 сборка · 8,1 МБ</div></div></div>'
            % (card, t['surf'], vcol(t, 'Сейчас', '0.36', 'сборка 399', t['ink']).replace('flex:1', ''), chip,
               t['acc'], mix(t['page'], t['acc'], .12), vcol(t, 'Новая', '0.37', 'сборка 400', t['acc']).replace('flex:1', ''), t['acc'],
               '#FFFFFF' if not t['dark'] else '#1A0830'))


def ver_board(t, v):
    return phone(t, '<div style="padding-top:24px">%s%s</div>' % (texts(t, 'Доступна версия 0.37', 'Сборка 400 · 8,1 МБ'), '<div style="height:20px"></div>' + versions(t, v)) + btn(t, 'Обновить'), 360)


NOTES = [('Новая шторка ввода: сначала счёт', 'new'), ('Счета группами, поиск по счетам', 'new'), ('Исправлен перенос курса на тёмной теме', 'fix')]


def notes(t, v):
    head_col = t['ink'] if v == 'A' else t['acc']
    items = ''
    for text, kind in NOTES:
        if v == 'A':
            mark = '<span style="width:6px;height:6px;border-radius:50%%;background:%s;flex:none;margin-top:8px"></span>' % t['muted']
            tag = ''
        elif v == 'B':
            mark = '<span style="width:8px;height:8px;border-radius:50%%;background:%s;border:1.5px solid %s;flex:none;margin-top:7px"></span>' % (t['prim'], mix(t['prim'], t['onp'], .45))
            tag = ''
        else:
            mark = ''
            col = (t['prim'], t['onp'], 'Новое') if kind == 'new' else (mix('#FFFFFF', t['acc'], .16) if not t['dark'] else mix(t['page'], t['acc'], .3), t['acc'], 'Исправлено')
            tag = '<span style="flex:none;padding:1px 8px;border-radius:999px;background:%s;color:%s;font-size:11px;font-weight:600;white-space:nowrap;height:20px;line-height:20px;margin-top:1px">%s</span>' % col
        items += '<div style="display:flex;gap:10px;padding:6px 0;font-size:14px;line-height:1.4">%s%s<span style="flex:1">%s</span></div>' % (mark, tag, text)
    return phone(t, '<div style="margin:20px 16px;border-radius:24px;border:1px solid %s;background:%s;padding:16px"><div style="font-size:13px;font-weight:700;color:%s;margin-bottom:6px">Что нового в 0.37</div>%s</div>'
                 % (t['line'], t['surf'], head_col, items), 250)


def done(t, v):
    ic = t['ink'] if (v == 'A' and not t['dark']) else (t['prim'] if v == 'A' else t['acc'])
    halo, dots = '', ''
    if v == 'B':  # one flash in the third colour + a few sparks, then it settles
        halo = '<div style="position:absolute;left:-10px;top:-10px;right:-10px;bottom:-10px;border-radius:50%%;border:2px solid %s;opacity:.35"></div>' % t['acc']
        for i in range(8):
            a = i * math.pi / 4
            dots += '<span style="position:absolute;left:%.0fpx;top:%.0fpx;width:8px;height:8px;border-radius:50%%;background:%s;border:1px solid %s"></span>' % (
                84 + 98 * math.cos(a) - 4, 84 + 98 * math.sin(a) - 4, t['acc'] if i % 2 else t['prim'], t['acc'])
    inner = ring(t, 'B' if v == 'B' else 'A', 0, check(ic, 56))
    inner = inner.replace('position:relative;width:168px', 'position:relative;width:168px') if v == 'A' else inner
    return phone(t, topbar(t) + '<div style="padding-top:52px"><div style="position:relative;width:168px;margin:0 auto">%s%s%s</div>%s</div>'
                 % (halo, inner, dots, texts(t, 'Обновлено до 0.37', 'Готово, всё на месте')), 400)


def bars(t, v):
    edge_on = v == 'P' and not t['dark']
    arc = t['ink'] if (v == 'A' and not t['dark']) else (t['prim'] if v == 'A' else t['acc'])
    track = t['prim'] if (v == 'A' and not t['dark']) else t['wash']
    if v == 'P':
        arc, track = t['prim'], t['wash']
    ed = ('box-shadow:0 0 0 1.5px %s;' % mix(t['prim'], '#000000', .5)) if edge_on else ''

    def line(name, val, frac):
        return ('<div style="padding:10px 0"><div style="display:flex;justify-content:space-between;font-size:14px"><span>%s</span><span style="%scolor:%s;font-size:13px">%s</span></div>'
                '<div style="margin-top:8px;height:8px;border-radius:99px;background:%s"><div style="width:%d%%;height:8px;border-radius:99px;background:%s;%s"></div></div></div>' % (name, MONO, t['muted'], val, track, frac, arc, ed))
    cols = ''.join('<span style="width:22px;height:%dpx;border-radius:6px;background:%s;%s"></span>' % (h, arc if i == 4 else track, ed if i == 4 else '') for i, h in enumerate([34, 52, 28, 60, 72, 40, 22]))
    return phone(t, '<div style="margin:20px 16px;border-radius:24px;border:1px solid %s;background:%s;padding:8px 16px">%s%s</div>'
                 '<div style="margin:0 16px;display:flex;gap:10px;align-items:flex-end;height:80px;padding:0 16px">%s</div>'
                 % (t['line'], t['surf'], line('Продукты', '130 000 / 350 000', 37), line('Кафе', '45 000 / 80 000', 56), cols), 330)


def main():
    out = sys.argv[1]
    os.makedirs(os.path.join(out, 'html'), exist_ok=True)
    spec = []
    for v in 'ABC':
        spec += [('RingDown' + v, ring_down, v, 460), ('RingOk' + v, ring_ok, v, 460), ('Ver' + v, ver_board, v, 360), ('Notes' + v, notes, v, 250)]
    for v in 'AB':
        spec += [('Done' + v, done, v, 400), ('Bars' + v, bars, v, 330)]
    for v in 'PQR':
        spec += [('RingDown' + v, ring_down, v, 460), ('RingOk' + v, ring_ok, v, 460)]
    spec += [('VerS', ver_board, 'S', 360), ('VerN', ver_board, 'N', 360), ('BarsP', bars, 'P', 330)]
    for name, fn, v, h in spec:
        for suffix, dark in (('', True), ('@light', False)):
            t = theme(dark)
            open(os.path.join(out, 'html', name + suffix + '.html'), 'w', encoding='utf-8').write(N.page(fn(t, v), 390, h, dict(page=t['page'])))
    js = os.path.join(out, 'render.js')
    open(js, 'w').write('''const { chromium } = require('playwright'); const fs = require('fs'); const path = require('path');
(async () => { const [dir] = process.argv.slice(2); const b = await chromium.launch(); fs.mkdirSync(path.join(dir, 'img'), { recursive: true });
for (const f of fs.readdirSync(path.join(dir, 'html'))) { const html = fs.readFileSync(path.join(dir, 'html', f), 'utf8');
 const m = /width:(\\d+)px;height:(\\d+)px;position:relative/.exec(html);
 const p = await b.newPage({ viewport: { width: +m[1], height: +m[2] }, deviceScaleFactor: 2 }); await p.goto('file://' + path.join(dir, 'html', f));
 await p.waitForTimeout(150); await p.screenshot({ path: path.join(dir, 'img', f.replace('.html', '.png')) }); await p.close(); }
await b.close(); })();''')
    subprocess.run(['node', js, os.path.abspath(out)], check=True, cwd=out)
    print('%d boards → %s/img' % (len(spec) * 2, out))


if __name__ == '__main__':
    main()
