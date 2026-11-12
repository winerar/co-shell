#!/usr/bin/env python3
"""How distinct are the category colours (#105)? CIEDE2000 between every pair, dark and light theme.

Reads CategoryPalette.kt, prints the closest pairs per theme, and a proposal: drop the later-added colour of the closest
pair until every pair is ≥ MIN apart in both themes (light theme = neon fill darkened by MUTE). Writes
scripts/agent/mockup_palette_105.json for the design boards (mockup_screens_fb_cats.py).

Usage: python3 scripts/agent/palette-distinct.py [--min 10]
"""
import json
import math
import os
import re
import sys

HERE = os.path.dirname(os.path.abspath(__file__))
SRC = os.path.join(HERE, '../../core/designsystem/src/main/kotlin/app/coshell/core/designsystem/category/CategoryPalette.kt')
OUT = os.path.join(HERE, 'mockup_palette_105.json')
CLOSE = 8.0  # below this two colours side by side read as one
MUTE = 0.12


def lab(h):
    def lin(c):
        return c / 12.92 if c <= 0.04045 else ((c + 0.055) / 1.055) ** 2.4
    r, g, b = (lin(int(h[i:i + 2], 16) / 255) for i in (0, 2, 4))
    x = (0.4124 * r + 0.3576 * g + 0.1805 * b) / 0.95047
    y = 0.2126 * r + 0.7152 * g + 0.0722 * b
    z = (0.0193 * r + 0.1192 * g + 0.9505 * b) / 1.08883

    def f(v):
        return v ** (1 / 3) if v > 0.008856 else 7.787 * v + 16 / 116
    return 116 * f(y) - 16, 500 * (f(x) - f(y)), 200 * (f(y) - f(z))


def de00(p, q):
    (l1, a1, b1), (l2, a2, b2) = p, q
    cb = (math.hypot(a1, b1) + math.hypot(a2, b2)) / 2
    g = 0.5 * (1 - math.sqrt(cb ** 7 / (cb ** 7 + 25 ** 7)))
    a1, a2 = (1 + g) * a1, (1 + g) * a2
    c1, c2 = math.hypot(a1, b1), math.hypot(a2, b2)
    h1, h2 = math.degrees(math.atan2(b1, a1)) % 360, math.degrees(math.atan2(b2, a2)) % 360
    dh = 0 if c1 * c2 == 0 else (h2 - h1 - 360 if h2 - h1 > 180 else h2 - h1 + 360 if h2 - h1 < -180 else h2 - h1)
    d_h = 2 * math.sqrt(c1 * c2) * math.sin(math.radians(dh / 2))
    lb, cbp = (l1 + l2) / 2, (c1 + c2) / 2
    hb = h1 + h2 if c1 * c2 == 0 else ((h1 + h2) / 2 if abs(h1 - h2) <= 180 else (h1 + h2 + 360) / 2)
    t = (1 - 0.17 * math.cos(math.radians(hb - 30)) + 0.24 * math.cos(math.radians(2 * hb))
         + 0.32 * math.cos(math.radians(3 * hb + 6)) - 0.2 * math.cos(math.radians(4 * hb - 63)))
    sl = 1 + 0.015 * (lb - 50) ** 2 / math.sqrt(20 + (lb - 50) ** 2)
    sc, sh = 1 + 0.045 * cbp, 1 + 0.015 * cbp * t
    rt = -2 * math.sqrt(cbp ** 7 / (cbp ** 7 + 25 ** 7)) * math.sin(math.radians(60 * math.exp(-((hb - 275) / 25) ** 2)))
    dl, dc = l2 - l1, c2 - c1
    return math.sqrt((dl / sl) ** 2 + (dc / sc) ** 2 + (d_h / sh) ** 2 + rt * (dc / sc) * (d_h / sh))


def mute(h):
    return ''.join('%02X' % round(int(h[i:i + 2], 16) * (1 - MUTE)) for i in (0, 2, 4))


def pairs(labs, idx):
    return sorted((de00(labs[a], labs[b]), a, b) for a in idx for b in idx if a < b)


def main():
    need = float(sys.argv[sys.argv.index('--min') + 1]) if '--min' in sys.argv else 10.0
    sw = [(d, lt, n.strip()) for d, lt, n in re.findall(r'Swatch\(0xFF([0-9A-F]{6}), 0xFF([0-9A-F]{6})\), // (.+)', open(SRC).read())]
    allx = range(len(sw))
    dark, light, muted = [lab(s[0]) for s in sw], [lab(s[1]) for s in sw], [lab(mute(s[0])) for s in sw]
    pd, pl = pairs(dark, allx), pairs(light, allx)
    for name, ps in (('dark', pd), ('light', pl)):
        print('%s: %d pairs < %.0f; closest:' % (name, sum(p[0] < CLOSE for p in ps), CLOSE),
              ', '.join('%s/%s %.1f' % (sw[a][2], sw[b][2], d) for d, a, b in ps[:6]))
    keep = list(allx)
    while True:
        worst = min(min(de00(dark[a], dark[b]), de00(muted[a], muted[b])) for a in keep for b in keep if a < b)
        if worst >= need:
            break
        _, a, b = min((min(de00(dark[a], dark[b]), de00(muted[a], muted[b])), a, b) for a in keep for b in keep if a < b)
        keep.remove(max(a, b))  # the later-added colour goes (the first 18 are the core neon set)
    print('proposal: %d colours, min ΔE %.1f in both themes; dropped: %s' % (len(keep), worst, ', '.join(sw[i][2] for i in allx if i not in keep)))
    data = {'sw': [(d, lt, n, mute(d)) for d, lt, n in sw], 'keep': keep,
            'close_dark': [(a, b) for d, a, b in pd if d < CLOSE], 'close_light': [(a, b) for d, a, b in pl if d < CLOSE],
            'worst_light': [(sw[a][1], sw[b][1], '%.1f' % d) for d, a, b in pl[:6]]}
    with open(OUT, 'w', encoding='utf-8') as f:
        json.dump(data, f, ensure_ascii=False)
        f.write('\n')


if __name__ == '__main__':
    main()
