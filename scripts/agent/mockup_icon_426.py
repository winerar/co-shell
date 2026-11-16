#!/usr/bin/env python3
"""Poll boards for the 1.0 app icon (#426): variants of the shell-purse on launcher masks and home screens.

Usage: python3 scripts/agent/mockup_icon_426.py <out-dir>  → <out-dir>/img/*.png (Playwright, ×2).
Geometry — icon_426.py (the same paths go into the Android vectors after the owner's «ок»).
"""
import os
import subprocess
import sys

HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, HERE)
import icon_426 as I  # noqa: E402
from mockup_bricks import ICON  # noqa: E402

ROOT = os.path.dirname(os.path.dirname(HERE))
FONT = os.path.join(ROOT, 'core/designsystem/src/main/res/font')
LIME, VIOLET, GRAPHITE, CYAN = '#B8FF2E', '#B266FF', '#050805', '#2BE4FF'
INK = '#0B1405'
S = I.Shape()

# id: (title, background (colour or gradient stops), foreground layers as SVG)
CLIP = '<clipPath id="half%s"><rect x="0" y="0" width="54" height="108"/></clipPath>'


def two_tone(uid):
    full = I.svg_paths([(S.body(), VIOLET, True)])
    left = '<g clip-path="url(#half%s)">%s</g>' % (uid, I.svg_paths([(S.body(), LIME, True)]))
    rest = I.svg_paths([(S.frame(), LIME, False), (S.ball(-1), LIME, False), (S.ball(1), VIOLET, False)])
    return '<defs>%s</defs>%s%s%s' % (CLIP % uid, full, left, rest)


def outline(color, color2):
    out = []
    for d, c, w in I.outline_layers(S, color, color2):
        out.append('<path d="%s" fill="%s"%s/>' % (d, c, '' if w == 0 else ' stroke="%s" stroke-width="%s" fill-opacity="0" '
                                                   'stroke-linecap="round" stroke-linejoin="round"' % (c, w)))
    return ''.join(out)


VARIANTS = {
    'A': ('Лайм на графите', GRAPHITE, lambda u: I.svg_paths(I.layers(S, LIME, LIME, LIME, VIOLET))),
    'B': ('Графит на лайме', LIME, lambda u: I.svg_paths(I.layers(S, INK, INK, INK, '#7A2BD6'))),
    'C': ('Градиент 1.0', [LIME, CYAN], lambda u: I.svg_paths(I.layers(S, INK, INK, INK, '#7A2BD6'))),
    'D': ('Две створки', GRAPHITE, two_tone),
    'E': ('Жирная линия', GRAPHITE, lambda u: outline(LIME, VIOLET)),
}
MONO = {'light': ('#D9E7C6', '#2C3A1C'), 'dark': ('#2A3322', '#D3EBB0')}
_uid = [0]


def icon(v, size, mask='circle', mono=None):
    """The adaptive icon as a launcher shows it: the 72-unit centre of the 108 canvas, clipped by the mask."""
    _uid[0] += 1
    u = '%s%d' % (v, _uid[0])
    title, bg, fg = VARIANTS[v]
    if mono:
        bgc, fgc = MONO[mono]
        paint = '<rect x="0" y="0" width="108" height="108" fill="%s"/>' % bgc
        art = (outline(fgc, fgc) if v == 'E' else I.svg_paths(I.layers(S, fgc, fgc, fgc, fgc)))
    else:
        if isinstance(bg, list):
            paint = ('<defs><linearGradient id="g%s" x1="0" y1="0" x2="1" y2="1"><stop offset="0" stop-color="%s"/>'
                     '<stop offset="1" stop-color="%s"/></linearGradient></defs><rect width="108" height="108" fill="url(#g%s)"/>'
                     % (u, bg[0], bg[1], u))
        else:
            paint = '<rect width="108" height="108" fill="%s"/>' % bg
        art = fg(u)
    shape = {'circle': '<circle cx="54" cy="54" r="36"/>',
             'squircle': '<path d="M54,18 C82,18 90,26 90,54 C90,82 82,90 54,90 C26,90 18,82 18,54 C18,26 26,18 54,18 Z"/>',
             'rounded': '<rect x="18" y="18" width="72" height="72" rx="15"/>'}[mask]
    return ('<svg width="%d" height="%d" viewBox="18 18 72 72"><defs><clipPath id="m%s">%s</clipPath></defs>'
            '<g clip-path="url(#m%s)">%s%s</g></svg>' % (size, size, u, shape, u, paint, art))


def dummy(color, glyph, size):
    return ('<div class="row" style="width:%dpx;height:%dpx;border-radius:50%%;background:%s;justify-content:center">'
            '<svg width="%d" height="%d" viewBox="0 0 24 24" fill="none" stroke="#fff" stroke-width="2" stroke-linecap="round" '
            'stroke-linejoin="round"><path d="%s"/></svg></div>' % (size, size, color, size // 2, size // 2, ICON[glyph]))


def home(vs, dark, w, size=60, labels=None):
    wall = ('radial-gradient(circle at 20% 10%, #3B2A6B 0, transparent 55%), radial-gradient(circle at 90% 90%, #0F4A4A 0, transparent 50%), #0C0E1A'
            if dark else 'radial-gradient(circle at 15% 15%, #FFE3C7 0, transparent 55%), radial-gradient(circle at 85% 80%, #CFE4FF 0, transparent 55%), #F4F0FA')
    fg = '#F2F2F2' if dark else '#1A1A1A'
    cells = []
    for item in vs:
        if isinstance(item, tuple):
            pic, label = dummy(item[0], item[1], size), item[2]
        else:
            pic, label = icon(item, size), (labels or {}).get(item, 'Co-Shell')
        cells.append('<div style="display:flex;flex-direction:column;align-items:center;gap:7px;width:%dpx">%s'
                     '<span style="font-size:12.5px;color:%s;text-shadow:%s">%s</span></div>'
                     % (size + 26, pic, fg, '0 1px 2px #0008' if dark else 'none', label))
    return ('<div class="row" style="width:%dpx;height:%dpx;border-radius:22px;background:%s;justify-content:space-evenly">%s</div>'
            % (w, size + 70, wall, ''.join(cells)))


NEIGH = [('#3D7BFF', 'calendar', 'Календарь'), ('#F5B400', 'note', 'Заметки'), ('#21B35B', 'phone', 'Телефон')]


def caption(t):
    return '<div style="font-size:13px;color:#9AA594;margin-bottom:8px">%s</div>' % t


def board(v):
    masks = ''.join('<div style="display:flex;flex-direction:column;align-items:center;gap:8px">%s<span class="cap">%s</span></div>'
                    % (icon(v, 120, m, mono), t) for m, mono, t in
                    [('circle', None, 'круг'), ('squircle', None, 'сквиркл'), ('rounded', None, 'квадрат'),
                     ('circle', 'light', 'тематическая, светлая'), ('circle', 'dark', 'тематическая, тёмная')])
    small = ''.join(icon(v, s) for s in (48, 36, 24))
    rows = [NEIGH[0], v, NEIGH[1], NEIGH[2]]
    body = ('<div style="padding:24px;display:flex;flex-direction:column;gap:20px">'
            '<div style="font-size:22px;font-weight:700;color:#EEF5E6">%s · %s</div>'
            '<div class="row" style="gap:26px;align-items:flex-start">%s<div style="display:flex;flex-direction:column;gap:12px;align-items:center;'
            'margin-left:auto">%s<span class="cap">мелкие размеры</span></div></div>'
            '<div class="row" style="gap:20px">%s%s</div></div>'
            % ('АБВГД'['ABCDE'.index(v)], VARIANTS[v][0], masks, '<div class="row" style="gap:14px">%s</div>' % small,
               home(rows, True, 456), home(rows, False, 456)))
    return page(body, 980, 384)


def compare():
    names = {v: 'АБВГД'['ABCDE'.index(v)] for v in VARIANTS}
    body = ('<div style="padding:24px;display:flex;flex-direction:column;gap:16px">%s%s%s%s</div>'
            % (caption('Все варианты рядом — тёмные обои'), home(list(VARIANTS), True, 932, 64, names),
               caption('Светлые обои'), home(list(VARIANTS), False, 932, 64, names)))
    return page(body, 980, 400)


def page(body, w, h):
    return '''<!doctype html><html lang="ru"><head><meta charset="utf-8"><style>
@font-face{font-family:Onest;src:url(file://%s/onest.ttf)}
*{box-sizing:border-box} body{margin:0;width:%dpx;height:%dpx;background:#141714;font-family:Onest,sans-serif;overflow:hidden}
.row{display:flex;align-items:center} .cap{font-size:12px;color:#9AA594}
</style></head><body>%s</body></html>''' % (FONT, w, h, body)


def main():
    out = sys.argv[1]
    os.makedirs(os.path.join(out, 'html'), exist_ok=True)
    pages = {'Icon426' + v: board(v) for v in VARIANTS}
    pages['Icon426All'] = compare()
    for name, html in pages.items():
        open(os.path.join(out, 'html', name + '.html'), 'w', encoding='utf-8').write(html)
    js = os.path.join(out, 'render.js')
    open(js, 'w').write('''const { chromium } = require('playwright'); const fs = require('fs'); const path = require('path');
(async () => { const [dir] = process.argv.slice(2); const b = await chromium.launch(); fs.mkdirSync(path.join(dir, 'img'), { recursive: true });
for (const f of fs.readdirSync(path.join(dir, 'html'))) { const html = fs.readFileSync(path.join(dir, 'html', f), 'utf8');
 const m = /width:(\\d+)px;height:(\\d+)px;background/.exec(html);
 const p = await b.newPage({ viewport: { width: +m[1], height: +m[2] }, deviceScaleFactor: 2 }); await p.goto('file://' + path.join(dir, 'html', f));
 await p.waitForTimeout(150); await p.screenshot({ path: path.join(dir, 'img', f.replace('.html', '.png')) }); await p.close(); }
await b.close(); })();''')
    subprocess.run(['node', js, os.path.abspath(out)], check=True, cwd=out)
    print('%d boards → %s/img' % (len(pages), out))


if __name__ == '__main__':
    main()
