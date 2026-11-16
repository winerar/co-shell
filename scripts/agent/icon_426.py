#!/usr/bin/env python3
"""App icon 1.0 (#426): the shell-purse drawing as fill-only paths on the 108-unit adaptive-icon canvas.

One geometry feeds both the poll mockups (SVG, mockup_icon_426.py) and the Android vectors (vector_xml()).
Ribs are holes (evenOdd), not background-coloured strokes, so the same drawing works on any background and as the
themed (monochrome) icon. Safe zone: a 66-unit circle around (54, 54); everything here stays inside it.
"""
import math

CX = 54.0


def _f(v):
    return ('%.2f' % v).rstrip('0').rstrip('.')


def _pt(x, y):
    return '%s,%s' % (_f(x), _f(y))


def capsule(x0, y0, x1, y1, w0, w1):
    """A tapered rib from (x0,y0) (width w0) to (x1,y1) (width w1) with round ends."""
    a = math.atan2(y1 - y0, x1 - x0)
    nx, ny = -math.sin(a), math.cos(a)
    r0, r1 = w0 / 2, w1 / 2
    p = [(x0 + nx * r0, y0 + ny * r0), (x1 + nx * r1, y1 + ny * r1), (x1 - nx * r1, y1 - ny * r1), (x0 - nx * r0, y0 - ny * r0)]
    return ('M%s L%s A%s,%s 0 0,0 %s L%s A%s,%s 0 0,0 %s Z'
            % (_pt(*p[0]), _pt(*p[1]), _f(r1), _f(r1), _pt(*p[2]), _pt(*p[3]), _f(r0), _f(r0), _pt(*p[0])))


def circle(cx, cy, r):
    return 'M%s A%s,%s 0 1,0 %s A%s,%s 0 1,0 %s Z' % (_pt(cx - r, cy), _f(r), _f(r), _pt(cx + r, cy), _f(r), _f(r), _pt(cx - r, cy))


class Shape:
    """Proportions of the shell-purse. lobes — scallops on the bottom edge."""

    def __init__(self, lobes=5, top=48.5, bottom_mid=82.0, side=68.0, half=27.0, neck=17.0, gap=2.9, stems=True, rib=(2.1, 3.4), even=None):
        """gap — frame-to-body gap (None: no frame, the balls sit on a rounded body top — one silhouette); stems — clasp balls on stems or sitting on the frame;
        rib — rib widths at the clasp end and at the edge (None — no ribs)."""
        self.lobes, self.top, self.bottom_mid, self.side, self.half, self.neck = lobes, top, bottom_mid, side, half, neck
        self.gap, self.stems, self.rib = gap, stems, rib
        self.stem, self.rib_start = 2.6, 9.5
        if even:  # owner #426 round 3: every line one width — ribs, the clasp gap and the stems
            self.rib, self.gap, self.stem = (even, even), even, even
            self.rib_start = 9.5 + even * 1.6  # even ribs need room where they converge

    def junctions(self):
        pts = []
        for k in range(self.lobes + 1):
            x = CX + self.half - k * 2 * self.half / self.lobes
            t = (x - CX) / self.half
            pts.append((x, self.side + (self.bottom_mid - 4 - self.side) * (1 - t * t)))
        return pts

    def body_outline(self):
        """Purse body: narrow under the clasp, flaring down to a scalloped edge (an upside-down scallop shell)."""
        j = self.junctions()
        xr, xl = CX + self.neck, CX - self.neck
        if self.gap is None:  # no frame: rounded shoulders instead of a bar
            d = 'M%s L%s Q%s %s ' % (_pt(xl + 4, self.top), _pt(xr - 4, self.top), _pt(xr, self.top), _pt(xr + 1, self.top + 3.5))
            d += 'C%s %s %s ' % (_pt(xr + 3, self.top + 7), _pt(j[0][0] - 1, self.side - 10), _pt(*j[0]))
        else:
            d = 'M%s L%s ' % (_pt(xl, self.top), _pt(xr, self.top))
            d += 'C%s %s %s ' % (_pt(xr + 3, self.top + 3), _pt(j[0][0] - 1, self.side - 10), _pt(*j[0]))
        for a, b in zip(j, j[1:]):
            chord = math.dist(a, b)
            r = chord / 2 * 1.04
            d += 'A%s,%s 0 0,1 %s ' % (_f(r), _f(r), _pt(*b))
        if self.gap is None:
            d += 'C%s %s %s Q%s %s Z' % (_pt(j[-1][0] + 1, self.side - 10), _pt(xl - 3, self.top + 7), _pt(xl - 1, self.top + 3.5),
                                         _pt(xl, self.top), _pt(xl + 4, self.top))
        else:
            d += 'C%s %s %s Z' % (_pt(j[-1][0] + 1, self.side - 10), _pt(xl - 3, self.top + 3), _pt(xl, self.top))
        return d

    def ribs(self):  # narrow end ≥ 1.4 px at mdpi
        if not self.rib:
            return []
        w0, w1 = self.rib
        apex = (CX, self.top - 2)
        out = []
        for x, y in self.junctions()[1:-1]:
            dx, dy = x - apex[0], y - apex[1]
            ln = math.hypot(dx, dy)
            s0, s1 = self.rib_start / ln, (ln - 4.2) / ln
            out.append(capsule(apex[0] + dx * s0, apex[1] + dy * s0, apex[0] + dx * s1, apex[1] + dy * s1, w0, w1))
        return out

    def body(self, ribs=True):
        return self.body_outline() + (' ' + ' '.join(self.ribs()) if ribs and self.rib else '')

    def frame(self):
        """Kiss-lock frame: a rounded bar a little wider than the neck, a gap above the body."""
        if self.gap is None:
            return ''
        x0, x1, y0, y1, r = CX - self.neck - 2, CX + self.neck + 2, self.top - 9, self.top - self.gap, 3.05  # gap ≥ 2 px at mdpi
        return ('M%s L%s A%s,%s 0 0,1 %s L%s A%s,%s 0 0,1 %s L%s A%s,%s 0 0,1 %s L%s A%s,%s 0 0,1 %s Z'
                % (_pt(x0 + r, y0), _pt(x1 - r, y0), _f(r), _f(r), _pt(x1, y0 + r), _pt(x1, y1 - r), _f(r), _f(r), _pt(x1 - r, y1),
                   _pt(x0 + r, y1), _f(r), _f(r), _pt(x0, y1 - r), _pt(x0, y0 + r), _f(r), _f(r), _pt(x0 + r, y0)))

    def ball(self, side):
        """Clasp ball: one per family member (side −1 / +1)."""
        y0 = self.top - 9
        if not self.stems:  # sitting on the frame (or the body top): nothing thin; two clear balls, not one blob
            base = self.top + 1 if self.gap is None else y0
            return circle(CX + side * 4.4, base - 3.4, 4.4)  # touching (owner, #426)
        cx, cy, r = CX + side * 4.6, y0 - 6.2, 4.4
        h = self.stem / 2
        stem = 'M%s L%s L%s L%s Z' % (_pt(cx - h, cy), _pt(cx - h - side * .6, y0 + .5), _pt(cx + h - side * .6, y0 + .5), _pt(cx + h, cy))
        return circle(cx, cy, r) + ' ' + stem

    def frame_line(self):
        """Kiss-lock frame for the thin-line style: one stroke (a closed outline blurs into a lump at 48 px)."""
        y = self.top - 6
        return 'M%s L%s' % (_pt(CX - self.neck - .5, y), _pt(CX + self.neck + .5, y))

    def ball_ring(self, side):
        """Clasp ball for the thin-line style: a ring on a short stem (stroked)."""
        y0 = self.top - 9
        cx, cy, r = CX + side * 5, y0 - 4.6, 3.6
        return circle(cx, cy, r) + ' M%s L%s' % (_pt(cx - side * .3, cy + r), _pt(cx - side * .9, y0 + 3))


def layers(shape, body, frame, ball1, ball2, ribs=True):
    """Foreground layers: (pathData, fill, evenOdd)."""
    out = [(shape.body(ribs), body, True), (shape.frame(), frame, False), (shape.ball(-1), ball1, False), (shape.ball(1), ball2, False)]
    return [x for x in out if x[0]]


def outline_layers(shape, color, color2, width=4.6, thin=False):
    """Line variants: the same outline as strokes (pathData, colour, width; width 0 = filled).

    Bold (Д): frame and ribs a little thinner than the edge, filled clasp balls. Thin (Е, #426 note): one width everywhere,
    clasp balls as rings — the line style of the 0.x icon with the scallop edge.
    """
    if thin:
        return [(shape.body_outline(), color, width), (shape.frame_line(), color, width), (' '.join(_rib_lines(shape)), color, width),
                (shape.ball_ring(-1), color, width), (shape.ball_ring(1), color2, width)]
    return [(shape.body_outline(), color, width), (shape.frame(), color, width * .8)] + \
           [(p, color, width * .7) for p in [' '.join(_rib_lines(shape))]] + [(shape.ball(-1), color, 0), (shape.ball(1), color2, 0)]


def _rib_lines(shape):
    apex = (CX, shape.top - 2)
    out = []
    for x, y in shape.junctions()[1:-1]:
        dx, dy = x - apex[0], y - apex[1]
        ln = math.hypot(dx, dy)
        out.append('M%s L%s' % (_pt(apex[0] + dx * 10 / ln, apex[1] + dy * 10 / ln), _pt(apex[0] + dx * (ln - 6.5) / ln, apex[1] + dy * (ln - 6.5) / ln)))
    return out


def svg_paths(fills):
    out = []
    for d, color, even in fills:
        out.append('<path d="%s" fill="%s"%s/>' % (d, color, ' fill-rule="evenodd"' if even else ''))
    return ''.join(out)


def vector_xml(layers, comment):
    """Android VectorDrawable for the adaptive-icon foreground / monochrome layer.

    layers: (pathData, colour, mode) — mode True/False: fill (evenOdd or not), a number > 0: stroke of that width, 0: fill.
    """
    def one(d, c, mode):
        if mode is True or mode is False or mode == 0:
            even = '\n        android:fillType="evenOdd"' if mode is True else ''
            return '    <path\n        android:fillColor="%s"%s\n        android:pathData="%s" />\n' % (c, even, d)
        return ('    <path\n        android:strokeColor="%s"\n        android:strokeWidth="%s"\n        android:strokeLineCap="round"\n'
                '        android:strokeLineJoin="round"\n        android:pathData="%s" />\n' % (c, _f(mode), d))
    body = ''.join(one(*x) for x in layers)
    return ('<?xml version="1.0" encoding="utf-8"?>\n<!-- %s -->\n<vector xmlns:android="http://schemas.android.com/apk/res/android"\n'
            '    android:width="108dp"\n    android:height="108dp"\n    android:viewportWidth="108"\n    android:viewportHeight="108">\n%s</vector>\n'
            % (comment, body))


# The 1.0 icon (owner's choice 2026-11-16, poll round 4: «И», clasp just balls): А's silhouette, every cut 2.6 wide.
CHOSEN = dict(even=2.6, stems=False)
RES = 'app/src/main/res/drawable'


def write(root):
    """Writes the adaptive-icon foreground and the themed (monochrome) layer from CHOSEN."""
    import os
    s = Shape(**CHOSEN)
    lime, violet, white = '#FFB8FF2E', '#FFB266FF', '#FFFFFFFF'
    src = 'generated by scripts/agent/icon_426.py (write mode)'  # no double hyphen: XML comments forbid it
    files = {
        'ic_launcher_foreground.xml': (layers(s, lime, lime, lime, violet),
                                       'App icon 1.0 (#426): shell-shaped coin purse, lime on graphite, one ball per family member; ' + src),
        'ic_launcher_monochrome.xml': (layers(s, white, white, white, white), 'Themed (monochrome) app icon, Android tints it; ' + src),
    }
    for name, (ls, comment) in files.items():
        open(os.path.join(root, RES, name), 'w', encoding='utf-8').write(vector_xml(ls, comment))
        print(os.path.join(RES, name))


if __name__ == '__main__':
    import os
    import sys
    if sys.argv[1:] != ['--write']:
        sys.exit('usage: icon_426.py --write   (rewrites %s/ic_launcher_{foreground,monochrome}.xml)' % RES)
    write(os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__)))))
