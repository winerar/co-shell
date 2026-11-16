"""Colour maths for the theme and palette audits (#385, #105, 1.0 themes): sRGB ↔ OKLCH, WCAG contrast, CIEDE2000.

Pure Python, no deps. Hex strings are 'RRGGBB' (no '#').
"""
import math


def rgb(h):
    h = h.lstrip('#')
    return tuple(int(h[i:i + 2], 16) / 255 for i in (0, 2, 4))


def hexs(c):
    return ''.join('%02X' % round(max(0.0, min(1.0, v)) * 255) for v in c)


def _lin(c):
    return c / 12.92 if c <= 0.04045 else ((c + 0.055) / 1.055) ** 2.4


def _gam(c):
    return 12.92 * c if c <= 0.0031308 else 1.055 * c ** (1 / 2.4) - 0.055


def lum(h):
    r, g, b = (_lin(v) for v in rgb(h))
    return 0.2126 * r + 0.7152 * g + 0.0722 * b


def contrast(a, b):
    la, lb = lum(a), lum(b)
    return (max(la, lb) + 0.05) / (min(la, lb) + 0.05)


def mix(a, b, t):
    """Plain sRGB lerp, as Compose `lerp(Color, Color, t)` does for these colours."""
    x, y = rgb(a), rgb(b)
    return hexs([x[i] + (y[i] - x[i]) * t for i in range(3)])


def to_oklch(h):
    r, g, b = (_lin(v) for v in rgb(h))
    l_ = (0.4122214708 * r + 0.5363325363 * g + 0.0514459929 * b) ** (1 / 3)
    m_ = (0.2119034982 * r + 0.6806995451 * g + 0.1073969566 * b) ** (1 / 3)
    s_ = (0.0883024619 * r + 0.2817188376 * g + 0.6299787005 * b) ** (1 / 3)
    L = 0.2104542553 * l_ + 0.7936177850 * m_ - 0.0040720468 * s_
    A = 1.9779984951 * l_ - 2.4285922050 * m_ + 0.4505937099 * s_
    B = 0.0259040371 * l_ + 0.7827717662 * m_ - 0.8086757660 * s_
    return L, math.hypot(A, B), math.degrees(math.atan2(B, A)) % 360


def oklch_rgb(L, C, H):
    A, B = C * math.cos(math.radians(H)), C * math.sin(math.radians(H))
    l_ = (L + 0.3963377774 * A + 0.2158037573 * B) ** 3
    m_ = (L - 0.1055613458 * A - 0.0638541728 * B) ** 3
    s_ = (L - 0.0894841775 * A - 1.2914855480 * B) ** 3
    r = 4.0767416621 * l_ - 3.3077115913 * m_ + 0.2309699292 * s_
    g = -1.2684380046 * l_ + 2.6097574011 * m_ - 0.3413193965 * s_
    b = -0.0041960863 * l_ - 0.7034186147 * m_ + 1.7076147010 * s_
    return r, g, b


def in_gamut(L, C, H, eps=1e-4):
    return all(-eps <= v <= 1 + eps for v in oklch_rgb(L, C, H))


def oklch(L, C, H):
    """Hex of an OKLCH colour, chroma reduced until it fits sRGB."""
    while C > 0 and not in_gamut(L, C, H):
        C -= 0.002
    return hexs([_gam(max(0.0, min(1.0, v))) for v in oklch_rgb(L, max(C, 0), H)])


def lab(h):
    r, g, b = (_lin(v) for v in rgb(h))
    x = (0.4124 * r + 0.3576 * g + 0.1805 * b) / 0.95047
    y = 0.2126 * r + 0.7152 * g + 0.0722 * b
    z = (0.0193 * r + 0.1192 * g + 0.9505 * b) / 1.08883

    def f(v):
        return v ** (1 / 3) if v > 0.008856 else 7.787 * v + 16 / 116
    return 116 * f(y) - 16, 500 * (f(x) - f(y)), 200 * (f(y) - f(z))


def de00(h1, h2):
    (l1, a1, b1), (l2, a2, b2) = lab(h1), lab(h2)
    cb = (math.hypot(a1, b1) + math.hypot(a2, b2)) / 2
    g = 0.5 * (1 - math.sqrt(cb ** 7 / (cb ** 7 + 25 ** 7)))
    a1, a2 = (1 + g) * a1, (1 + g) * a2
    c1, c2 = math.hypot(a1, b1), math.hypot(a2, b2)
    hh1, hh2 = math.degrees(math.atan2(b1, a1)) % 360, math.degrees(math.atan2(b2, a2)) % 360
    dh = 0 if c1 * c2 == 0 else (hh2 - hh1 - 360 if hh2 - hh1 > 180 else hh2 - hh1 + 360 if hh2 - hh1 < -180 else hh2 - hh1)
    d_h = 2 * math.sqrt(c1 * c2) * math.sin(math.radians(dh / 2))
    lb, cbp = (l1 + l2) / 2, (c1 + c2) / 2
    hb = hh1 + hh2 if c1 * c2 == 0 else ((hh1 + hh2) / 2 if abs(hh1 - hh2) <= 180 else (hh1 + hh2 + 360) / 2)
    t = (1 - 0.17 * math.cos(math.radians(hb - 30)) + 0.24 * math.cos(math.radians(2 * hb))
         + 0.32 * math.cos(math.radians(3 * hb + 6)) - 0.2 * math.cos(math.radians(4 * hb - 63)))
    sl = 1 + 0.015 * (lb - 50) ** 2 / math.sqrt(20 + (lb - 50) ** 2)
    sc, sh = 1 + 0.045 * cbp, 1 + 0.015 * cbp * t
    rt = -2 * math.sqrt(cbp ** 7 / (cbp ** 7 + 25 ** 7)) * math.sin(math.radians(60 * math.exp(-((hb - 275) / 25) ** 2)))
    dl, dc = l2 - l1, c2 - c1
    return math.sqrt((dl / sl) ** 2 + (dc / sc) ** 2 + (d_h / sh) ** 2 + rt * (dc / sc) * (d_h / sh))


def with_lum(h, target, lo=0.0, hi=1.0):
    """Same OKLCH hue and chroma ratio, lightness moved until WCAG luminance ≈ target."""
    L, C, H = to_oklch(h)
    for _ in range(40):
        mid = (lo + hi) / 2
        if lum(oklch(mid, C, H)) < target:
            lo = mid
        else:
            hi = mid
    return oklch((lo + hi) / 2, C, H)
