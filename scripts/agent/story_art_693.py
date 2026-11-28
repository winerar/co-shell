#!/usr/bin/env python3
"""Vector schemes of the release stories (#693, boards Rel693S1…S5): three layers per story, each tinted by the theme in Compose.

ink    — lines and outlines (onSurface);
accent — filled shapes and strokes in the theme's main colour (primary);
edge   — a hairline round the accent shapes (fillEdge: lime on white needs it, design-system.md «Тонкая граница»).
Text is never drawn here: words live in strings (AGENTS.md rule 9). Cut-outs (the «+» in the button) use evenOdd fills.

Usage: python3 scripts/agent/story_art_693.py --write   → app/src/main/res/drawable/story_<name>_<layer>.xml
       python3 scripts/agent/story_art_693.py --check   → exit 1 if the files differ from what this script makes
"""
import os
import sys

ROOT = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
OUT = os.path.join(ROOT, 'app', 'src', 'main', 'res', 'drawable')
W, H = 300, 180


def rrect(x, y, w, h, r):
    return ('M%g,%g h%g a%g,%g 0 0 1 %g,%g v%g a%g,%g 0 0 1 %g,%g h%g a%g,%g 0 0 1 %g,%g v%g a%g,%g 0 0 1 %g,%gz' % (
        x + r, y, w - 2 * r, r, r, r, r, h - 2 * r, r, r, -r, r, -(w - 2 * r), r, r, -r, -r, -(h - 2 * r), r, r, r, -r))


def circle(cx, cy, r):
    return 'M%g,%g a%g,%g 0 1 0 %g,0 a%g,%g 0 1 0 %g,0z' % (cx - r, cy, r, r, 2 * r, r, r, -2 * r)


def line(*pts):
    return 'M' + ' L'.join('%g,%g' % p for p in pts)


def polygon(*pts):
    return line(*pts) + 'z'


def dashed(x1, x2, y, dash=8, gap=7):
    out, x = [], x1
    step = 1 if x2 > x1 else -1
    while (x2 - x) * step > 0:
        end = x + step * min(dash, abs(x2 - x))
        out.append('M%g,%g L%g,%g' % (x, y, end, y))
        x = end + step * gap
    return ' '.join(out)


# Each story: list of (layer, kind, path[, extra]) — kind: 'fill' | 'stroke' | 'evenodd'.
STORIES = {
    'debts': [
        ('accent', 'fill', rrect(20, 20, 120, 36, 18)),
        ('ink', 'stroke', rrect(160, 20, 120, 36, 18)),
        ('ink', 'stroke', circle(70, 120, 26)),
        ('ink', 'stroke', circle(230, 120, 26)),
        ('accent', 'stroke', line((104, 112), (196, 112)) + ' ' + line((186, 102), (196, 112), (186, 122)), 3),
        ('ink', 'stroke', dashed(196, 104, 134) + ' ' + line((114, 124), (104, 134), (114, 144)), 3),
    ],
    'accounts': [
        ('accent', 'fill', rrect(30, 10, 110, 70, 14)),
        ('ink', 'stroke', rrect(160, 10, 110, 70, 14)),
        ('ink', 'stroke', rrect(30, 100, 110, 70, 14)),
        ('ink', 'stroke', rrect(160, 100, 110, 70, 14)),
        ('ink', 'stroke', rrect(42, 112, 40, 6, 3) + ' ' + rrect(172, 22, 40, 6, 3) + ' ' + rrect(172, 112, 40, 6, 3)),
        ('ink', 'stroke', rrect(240, 146, 18, 14, 3) + ' M244,146 v-4 a5,5 0 0 1 10,0 v4'),
    ],
    'fan': [
        ('ink', 'stroke', circle(70, 78, 24)),
        ('ink', 'stroke', circle(150, 46, 24)),
        ('ink', 'stroke', circle(230, 78, 24)),
        ('ink', 'stroke', rrect(60, 70, 20, 16, 3) + ' ' + line((140, 46), (160, 46)) + ' ' + line((140, 38), (160, 38)) + ' ' + line((140, 54), (154, 54))
         + ' ' + rrect(220, 68, 20, 20, 4)),
        ('ink', 'stroke', line((92, 98), (128, 138)) + ' ' + line((150, 74), (150, 124)) + ' ' + line((208, 98), (172, 138)), 1.5),
        ('accent', 'evenodd', circle(150, 150, 24) + ' ' + rrect(147, 138, 6, 24, 3) + ' ' + rrect(138, 147, 24, 6, 3)),
    ],
    'receipt': [
        ('ink', 'stroke', 'M30,10 h90 v150 l-15,-10 -15,10 -15,-10 -15,10 -15,-10 -15,10z'),
        ('ink', 'stroke', rrect(44, 32, 60, 7, 3.5) + ' ' + rrect(44, 50, 44, 7, 3.5) + ' ' + rrect(44, 90, 60, 7, 3.5)),
        ('accent', 'stroke', line((134, 85), (174, 85)) + ' ' + line((166, 75), (176, 85), (166, 95)), 3),
        ('accent', 'evenodd', rrect(186, 50, 96, 64, 12) + ' ' + rrect(198, 66, 40, 7, 3.5) + ' ' + rrect(198, 92, 14, 7, 3.5) + ' ' + rrect(240, 92, 30, 7, 3.5)),
    ],
    'recap': [
        ('ink', 'stroke', rrect(95, 5, 110, 170, 18)),
        ('ink', 'stroke', 'M182,104 a34,34 0 1 1 -32,-46', 12),  # the rest of the ring; the accent arc is the month's share
        ('accent', 'stroke', 'M150,58 a34,34 0 0 1 32,46', 12),
        ('accent', 'fill', rrect(107, 16, 20, 4, 2) + ' ' + rrect(131, 16, 20, 4, 2)),
        ('ink', 'stroke', rrect(155, 16, 20, 4, 2) + ' ' + rrect(179, 16, 14, 4, 2), 1),
        ('ink', 'stroke', 'M232,110 v24 h36 v-24 M250,92 v32 M240,102 l10,-10 10,10', 2.5),
    ],
    # Release 2026-11-27 (#794, #789, #785, #154, #555): bulk edit, repeats and duplicates, smart reports, own bottom bar, widgets.
    'bulk': [
        ('ink', 'stroke', rrect(30, 15, 240, 40, 12) + ' ' + rrect(30, 70, 240, 40, 12) + ' ' + rrect(30, 125, 240, 40, 12)),
        ('accent', 'evenodd', ' '.join(rrect(44, y, 20, 20, 5) + ' ' + polygon((47, y + 10), (50, y + 7), (53, y + 10.5), (59, y + 3.5), (62, y + 6.5), (53, y + 16.5))
                                       for y in (25, 80))),
        ('ink', 'stroke', rrect(44, 135, 20, 20, 5)),
        ('ink', 'stroke', ' '.join(rrect(80, y + 17, 100, 6, 3) + ' ' + rrect(222, y + 16, 34, 7, 3.5) for y in (15, 70, 125))),
    ],
    'repeat': [
        ('ink', 'stroke', rrect(20, 36, 150, 44, 12) + ' ' + rrect(34, 54, 70, 7, 3.5) + ' ' + rrect(122, 54, 34, 7, 3.5)),
        ('accent', 'evenodd', rrect(20, 100, 150, 44, 12) + ' ' + rrect(34, 118, 70, 7, 3.5) + ' ' + rrect(122, 118, 34, 7, 3.5)),
        ('ink', 'stroke', 'M270,90 A40,40 0 1 1 258.3,61.7 ' + line((246, 60), (258.3, 61.7), (257, 49)), 3),
        ('accent', 'fill', circle(230, 90, 8)),
    ],
    'insights': [
        ('ink', 'stroke', rrect(40, 100, 30, 60, 8) + ' ' + rrect(85, 80, 30, 80, 8) + ' ' + rrect(130, 110, 30, 50, 8) + ' ' + rrect(220, 90, 30, 70, 8)),
        ('accent', 'fill', rrect(175, 40, 30, 120, 8)),
        ('ink', 'stroke', dashed(24, 276, 100), 1.5),
        ('accent', 'stroke', line((190, 30), (190, 8)) + ' ' + line((182, 16), (190, 8), (198, 16)), 3),
        ('ink', 'stroke', line((24, 166), (276, 166)), 1.5),
    ],
    'bar': [
        ('ink', 'stroke', rrect(80, 12, 140, 52, 14) + ' ' + circle(106, 38, 10) + ' ' + rrect(126, 34, 76, 7, 3.5)),
        ('accent', 'stroke', line((106, 72), (106, 100)) + ' ' + line((98, 92), (106, 100), (114, 92)), 3),
        ('ink', 'stroke', rrect(20, 110, 260, 56, 28) + ' ' + circle(60, 138, 10) + ' ' + circle(195, 138, 10) + ' ' + circle(240, 138, 10)),
        ('ink', 'stroke', circle(106, 138, 14), 2.5),
        ('accent', 'evenodd', circle(150, 138, 20) + ' ' + polygon((147, 127), (153, 127), (153, 135), (161, 135), (161, 141), (153, 141),
                                                                  (153, 149), (147, 149), (147, 141), (139, 141), (139, 135), (147, 135))),
    ],
    'widgets': [
        ('accent', 'evenodd', rrect(30, 20, 150, 70, 18) + ' ' + rrect(44, 36, 56, 38, 12) + ' ' + rrect(110, 36, 56, 38, 12)),
        ('ink', 'stroke', line((72, 48), (72, 62)) + ' ' + line((65, 55), (79, 55)) + ' ' + line((131, 55), (145, 55)), 2.5),
        ('ink', 'stroke', ' '.join(rrect(x, y, 32, 32, 10) for x in (200, 240) for y in (20, 58))),
        ('ink', 'stroke', ' '.join(rrect(x, 122, 32, 32, 10) for x in (30, 75, 120, 165, 210))),
    ],
    'widget_width': [  # 2026-11-28 (#821): a narrow «Шаблоны» widget stretched wide, two more templates appear
        ('ink', 'stroke', rrect(20, 20, 130, 56, 18) + ' ' + rrect(32, 32, 70, 32, 16) + ' ' + circle(124, 48, 13)),
        ('ink', 'stroke', line((162, 48), (266, 48)) + ' ' + line((256, 40), (266, 48), (256, 56)), 2),
        ('ink', 'stroke', rrect(20, 104, 260, 56, 18) + ' ' + rrect(32, 116, 45, 32, 16) + ' ' + rrect(83, 116, 45, 32, 16) + ' ' + circle(249, 132, 13)),
        ('accent', 'fill', rrect(134, 116, 45, 32, 16) + ' ' + rrect(185, 116, 45, 32, 16)),
    ],
    'stories_back': [  # 2026-11-28: a tap on the left part of a story card and an arrow back to the previous one
        ('ink', 'stroke', rrect(30, 25, 60, 130, 14) + ' ' + rrect(130, 10, 110, 160, 18)),
        ('ink', 'stroke', rrect(142, 22, 40, 4, 2) + ' ' + rrect(188, 22, 40, 4, 2), 1),
        ('ink', 'stroke', circle(168, 90, 24), 1.5),
        ('accent', 'fill', circle(168, 90, 13)),
        ('accent', 'stroke', line((122, 90), (98, 90)) + ' ' + line((108, 80), (98, 90), (108, 100)), 3),
    ],
    'more_tiles': [  # 2026-11-28: «Ещё» as a grid of icons four in a row, one of them picked
        ('ink', 'stroke', rrect(20, 14, 260, 152, 24)),
        ('ink', 'stroke', ' '.join(circle(62 + 59 * c, 50 + 62 * r, 16) for r in range(2) for c in range(4) if (r, c) != (0, 2))),
        ('ink', 'stroke', ' '.join(rrect(46 + 59 * c, 76 + 62 * r, 32, 5, 2.5) for r in range(2) for c in range(4)), 1.5),
        ('accent', 'fill', circle(180, 50, 16)),
    ],
    'settings_search': [  # 2026-11-28: «Найти настройку» — a search pill and the one row it found
        ('ink', 'stroke', rrect(20, 16, 260, 44, 22) + ' ' + circle(48, 36, 8) + ' ' + line((54, 42), (60, 48))),
        ('ink', 'stroke', rrect(76, 34, 70, 5, 2.5), 1.5),
        ('accent', 'fill', rrect(20, 84, 260, 56, 18)),
        ('ink', 'stroke', rrect(40, 100, 120, 6, 3) + ' ' + rrect(40, 116, 80, 5, 2.5) + ' ' + line((250, 104), (258, 112), (250, 120)), 1.5),
        ('ink', 'stroke', dashed(30, 270, 162), 1.5),
    ],
    # «Знакомство» (#598, owner poll 2026-11-25 «А»; boards Tour598S3, S4, S6): accounts, fan and recap are shared with the release stories.
    'categories': [
        ('accent', 'fill', circle(70, 50, 26)),
        ('ink', 'stroke', circle(150, 50, 26) + ' ' + circle(230, 50, 26) + ' ' + circle(70, 130, 26) + ' ' + circle(150, 130, 26)),
        ('ink', 'stroke', 'M230,116 v28 M216,130 h28', 3),
        ('ink', 'stroke', rrect(140, 40, 20, 18, 4) + ' ' + line((222, 58), (230, 40), (238, 58)) + ' ' + line((60, 122), (80, 138)) + ' ' + circle(150, 130, 8)),
    ],
    'budgets': [
        ('ink', 'stroke', rrect(30, 30, 240, 20, 10) + ' ' + rrect(30, 80, 240, 20, 10) + ' ' + rrect(30, 130, 240, 20, 10)),
        ('accent', 'fill', rrect(30, 30, 150, 20, 10) + ' ' + rrect(30, 80, 90, 20, 10) + ' ' + rrect(30, 130, 200, 20, 10)),
        ('ink', 'stroke', line((210, 20), (210, 160)), 2.5),
    ],
    'family': [
        ('ink', 'stroke', circle(80, 70, 22) + ' M44,140 a36,30 0 0 1 72,0'),
        ('accent', 'fill', circle(160, 70, 22)),
        ('ink', 'stroke', 'M124,140 a36,30 0 0 1 72,0'),
        ('ink', 'stroke', 'M222,112 h44 a14,14 0 0 0 -2,-28 a20,20 0 0 0 -38,4 a12,12 0 0 0 -4,24z', 2.5),
        ('accent', 'stroke', line((244, 104), (244, 124)) + ' ' + line((236, 116), (244, 124), (252, 116)), 3),
    ],
}

LAYERS = ('ink', 'accent', 'edge')


def path_xml(kind, data, width):
    attrs = ['android:pathData="%s"' % data]
    if kind == 'stroke':
        attrs += ['android:strokeColor="@android:color/white"', 'android:strokeWidth="%g"' % width,
                  'android:strokeLineCap="round"', 'android:strokeLineJoin="round"']
    else:
        attrs += ['android:fillColor="@android:color/white"']
        if kind == 'evenodd':
            attrs += ['android:fillType="evenOdd"']
    return '    <path\n        %s />' % '\n        '.join(attrs)


def layer_paths(name, layer):
    out = []
    for item in STORIES[name]:
        role, kind, data = item[0], item[1], item[2]
        width = item[3] if len(item) > 3 else 2
        if layer == 'edge':
            if role == 'accent' and kind != 'stroke':
                out.append(path_xml('stroke', data, 1.5))
        elif role == layer:
            out.append(path_xml(kind, data, width))
    return out


def xml(name, layer):
    paths = layer_paths(name, layer)
    head = ('<?xml version="1.0" encoding="utf-8"?>\n'
            '<!-- Story scheme «%s», layer «%s» (#693, #598): generated by scripts/agent/story_art_693.py, tinted in Compose. -->\n'
            '<vector xmlns:android="http://schemas.android.com/apk/res/android"\n'
            '    android:width="%ddp"\n    android:height="%ddp"\n    android:viewportWidth="%d"\n    android:viewportHeight="%d">\n') % (name, layer, W, H, W, H)
    return head + '\n'.join(paths) + '\n</vector>\n'


def svg(name, ink, accent, edge, width=300):
    """The same scheme as inline SVG for the boards (`mockup_screens_release.py`): one source for the app and the canvas."""
    colour = {'ink': ink, 'accent': accent, 'edge': edge}
    out = []
    for layer in ('accent', 'edge', 'ink'):
        for item in STORIES[name]:
            role, kind, data = item[0], item[1], item[2]
            w = item[3] if len(item) > 3 else 2
            if layer == 'edge':
                if role != 'accent' or kind == 'stroke':
                    continue
                kind, w = 'stroke', 1.5
            elif role != layer:
                continue
            c = colour[layer]
            if kind == 'stroke':
                out.append('<path d="%s" fill="none" stroke="%s" stroke-width="%g" stroke-linecap="round" stroke-linejoin="round"/>' % (data, c, w))
            else:
                out.append('<path d="%s" fill="%s"%s/>' % (data, c, ' fill-rule="evenodd"' if kind == 'evenodd' else ''))
    return '<svg width="%d" height="%d" viewBox="0 0 %d %d">%s</svg>' % (width, width * H // W, W, H, ''.join(out))


def files():
    for name in STORIES:
        for layer in LAYERS:
            if layer_paths(name, layer):
                yield os.path.join(OUT, 'story_%s_%s.xml' % (name, layer)), xml(name, layer)


def main():
    mode = sys.argv[1] if len(sys.argv) > 1 else '--check'
    stale = []
    for path, text in files():
        if mode == '--write':
            open(path, 'w', encoding='utf-8').write(text)
        elif not os.path.exists(path) or open(path, encoding='utf-8').read() != text:
            stale.append(os.path.relpath(path, ROOT))
    if stale:
        sys.exit('stale (run --write):\n' + '\n'.join(stale))
    print('OK: %d story layers' % len(list(files())))


if __name__ == '__main__':
    main()
