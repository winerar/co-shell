#!/usr/bin/env python3
"""Proposal for the 1.0 themes and the 24-colour icon palette (#385, #354, #105): specs, derived schemes, contrast audit.

The derivation mirrors core/designsystem/.../theme/ColorSchemes.kt and Color.kt (neutral ladder from the background,
primary = fill with dark ink). Usage: python3 scripts/agent/themes_1_0.py [--json out.json]  → prints the audit.
"""
import itertools
import json
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from colorlab import contrast, de00, mix, with_lum  # noqa: E402

# name: (ru, group, kind, primary, onPrimary, accent d/l, tertiary d/l, income d/l, expense d/l, bg d/l, hero light ×3, note)
THEMES = [
    ('Lime', 'Лайм', 'Фрукты', 'неон', 'B8FF2E', '0E1F00', 'C27BFF', '7A2BD6', 'FFE14D', '6E6200', '2BE4FF', '016D86',
     'FF4D6D', 'C60B3D', '050805', 'F7FFEA', ['D9FF8F', 'B8F5FF', 'E8D4FF'], 'кислотный лайм + фиолетовый'),
    ('Garnet', 'Гранат', 'Фрукты', 'неон', 'FF3D6E', '1F0009', '2EE6BE', '056E59', 'FFC94D', '7A5600', '5CC8FF', '036797',
     'FF9466', 'AB3903', '12060A', 'FFF4F6', ['FFC2D3', 'FFE2C4', 'B8F5E6'], 'рубиновый + бирюзовый'),
    ('Banana', 'Банан', 'Фрукты', 'неон', 'FFE135', '2A2200', '9D8CFF', '4B3BCF', 'FF9F5C', 'A54704', '5CF08A', '027235',
     'FF5C7A', 'C60B3D', '0A0903', 'FFFBE5', ['FFF08A', 'FFE0B0', 'D8D2FF'], 'жёлтый + индиго'),
    ('Mate', 'Мате', 'Напитки', 'пастель', 'B5D98A', '1A2A08', 'FFA77A', 'A1451A', 'EAD88A', '6E5E00', '6BE0D8', '016D67',
     'FF7088', 'BE1140', '0B0D08', 'F6F8EC', ['D7EDBF', 'F5DCC5', 'C6EEE9'], 'травяной + медный'),
    ('Espresso', 'Эспрессо', 'Напитки', 'неон', 'C67F4D', '1F0C00', '4DA6FF', '1F5FA8', 'EBC096', '7A4E1E', '7FE0B0', '006F47',
     'FF5C8A', 'C0084C', '110B08', 'FBF6F0', ['E8C3A3', 'C8E0FF', 'F2DDC9'], 'кофейный + электрик'),
    ('Kvas', 'Квас', 'Напитки', 'неон', 'FFB020', '2A1700', '3DDBD9', '036D73', 'FF7A5C', 'AE3818', '9CF06B', '3B6E03',
     'FF5C7A', 'C4063B', '0D0904', 'FFF8EB', ['FFD98A', 'FFE9C8', 'B8F0EE'], 'янтарь + бирюза'),
    ('Sakura', 'Сакура', 'Растения', 'пастель', 'FFB3CF', '3A0A1E', '8EE6A0', '0A7030', 'C9B6FF', '5B3FD0', '7FD0FF', '00658A',
     'FF6B5C', 'C0181A', '120A0E', 'FFF5F8', ['FFD6E4', 'FFE9F0', 'D2F3DA'], 'розовый + листва'),
    ('Cornflower', 'Василёк', 'Растения', 'неон', '5C8DFF', '000B2A', 'FFCC4D', '7A5600', 'FF7A5C', 'B33114', '6BF08F', '01712F',
     'FF5C8A', 'C0084C', '060912', 'F4F7FF', ['BFD3FF', 'FFE9B0', 'D4E0FF'], 'синий + пшеничный'),
    ('Lotus', 'Лотос', 'Растения', 'пастель', 'CDB4FF', '1E0A3A', 'C7F25C', '4C6902', 'FF8AC8', 'AD2C6B', '5CE6FF', '05697F',
     'FF5C6E', 'C3013A', '0E0814', 'FBF5FF', ['ECCBFF', 'E8F7C2', 'FFD6EC'], 'сиреневый + лист лотоса'),
]
KEYS = ['id', 'ru', 'group', 'kind', 'primary', 'onPrimary', 'accentDark', 'accentLight', 'tertiaryDark', 'tertiaryLight',
        'incomeDark', 'incomeLight', 'expenseDark', 'expenseLight', 'bgDark', 'bgLight', 'heroLight', 'note']

# 24 icon colours (v2, owner 2026-11-16: v1's deeper light shades looked alike): the same fill in both themes, ink INK on it;
# ΔE2000 ≥ 16 between any two. A fill under 3:1 on its surface gets a thin ring of its own deeper shade (ring()).
SWATCHES = [
    ('red', 'FF2039', 'FF2039'),
    ('coral', 'FF8D85', 'FF8D85'),
    ('orange', 'FF7A00', 'FF7A00'),
    ('peach', 'FFD4BA', 'FFD4BA'),
    ('caramel', '9E7756', '9E7756'),
    ('amber', 'E0A800', 'E0A800'),
    ('sand', 'BCB38F', 'BCB38F'),
    ('yellow', 'F2E640', 'F2E640'),
    ('lime', '95C11F', '95C11F'),
    ('sage', '759273', '759273'),
    ('green', '0EEF82', '0EEF82'),
    ('seafoam', 'AEDFD9', 'AEDFD9'),
    ('teal', '00C2A2', '00C2A2'),
    ('cyan', '08CEFE', '08CEFE'),
    ('sky', '019DFD', '019DFD'),
    ('steel', '9DADBB', '9DADBB'),
    ('periwinkle', 'D7D9FF', 'D7D9FF'),
    ('indigo', '6077DB', '6077DB'),
    ('violet', 'B15FFE', 'B15FFE'),
    ('lavender', 'E5A6FD', 'E5A6FD'),
    ('magenta', 'E501B6', 'E501B6'),
    ('mauve', '9E7087', '9E7087'),
    ('pink', 'FF79B4', 'FF79B4'),
    ('blush', 'FECBDC', 'FECBDC'),
]
INK = '0B0B0B'


def ring(fill, surface):
    """Ring for a pale fill (< 3:1 on the surface): the same hue, deep enough for 3:1; None when not needed."""
    if contrast(fill, surface) >= 3:
        return None
    return with_lum(fill, 0.2) if surface_light(surface) else with_lum(fill, 0.45)


def surface_light(c):
    return contrast(c, '000000') > contrast(c, 'FFFFFF')


def readable_on(color, backgrounds, toward, need=4.6):
    """`color` moved toward `toward` in 5 % steps until it reads on every background (Theme.kt does the same)."""
    c, k = color, 0.0
    while min(contrast(c, b) for b in backgrounds) < need and k < 1:
        k += 0.05
        c = mix(color, toward, k)
    return c


def hero_strong(t):
    """Light hero gradient «Б»: the primary itself, a lighter primary, a light accent."""
    return [t['primary'], mix(t['primary'], 'FFFFFF', .3), mix(t['accentDark'], 'FFFFFF', .2)]


def theme(row):
    return dict(zip(KEYS, row))


def neutrals(bg, dark):
    if dark:
        return dict(lowest=mix(bg, '000000', .35), background=bg, low=mix(bg, 'FFFFFF', .03), container=mix(bg, 'FFFFFF', .055),
                    high=mix(bg, 'FFFFFF', .085), highest=mix(bg, 'FFFFFF', .115), line=mix(bg, 'FFFFFF', .26),
                    lineSoft=mix(bg, 'FFFFFF', .15), text=mix('FFFFFF', bg, .08), textMuted=mix('FFFFFF', bg, .38))
    return dict(lowest='FFFFFF', background=bg, low=mix(bg, '000000', .035), container=mix(bg, '000000', .06),
                high=mix(bg, '000000', .085), highest=mix(bg, '000000', .11), line=mix(bg, '000000', .48),
                lineSoft=mix(bg, '000000', .17), text=mix('000000', bg, .1), textMuted=mix('000000', bg, .36))


def scheme(t, dark):
    """The colours a screen uses, as ColorSchemes.kt derives them (+ the proposed 1.0 fixes, marked NEW)."""
    n = neutrals(t['bgDark' if dark else 'bgLight'], dark)
    acc = t['accentDark' if dark else 'accentLight']
    s = dict(n, primary=t['primary'], onPrimary=t['onPrimary'], accent=acc, income=t['incomeDark' if dark else 'incomeLight'],
             expense=t['expenseDark' if dark else 'expenseLight'], tertiary=t['tertiaryDark' if dark else 'tertiaryLight'],
             card='FFFFFF' if not dark else n['container'], track=n['high'])
    s['secondaryContainer'] = mix(t['accentDark'], n['background'], .68) if dark else mix(t['accentDark'], 'FFFFFF', .78)
    s['onSecondaryContainer'] = mix(t['accentDark'], 'FFFFFF', .75) if dark else mix(t['accentLight'], '000000', .55)
    # NEW (#385): field and chip borders, empty checkbox — the muted text colour (≥ 3:1); selected state in the light
    # theme — an ink pill (onSurface) with the primary as its label, like the approved graphite-on-neon progress.
    s['border'] = n['textMuted']
    s['selectedFill'] = t['primary'] if dark else n['text']
    s['selectedInk'] = t['onPrimary'] if dark else t['primary']
    return s


def audit(t, dark):
    s = scheme(t, dark)
    rows = []

    def need(label, fg, bg, lim):
        rows.append((label, contrast(fg, bg), lim))
    for surf in ('background', 'card', 'high'):
        need('text/' + surf, s['text'], s[surf], 4.5)
        need('muted/' + surf, s['textMuted'], s[surf], 4.5)
        need('income/' + surf, s['income'], s[surf], 4.5)
        need('expense/' + surf, s['expense'], s[surf], 4.5)
        need('accent/' + surf, s['accent'], s[surf], 4.5)
    need('onPrimary/primary', s['onPrimary'], s['primary'], 4.5)
    need('onSecCont/secCont', s['onSecondaryContainer'], s['secondaryContainer'], 4.5)
    need('selected/track', s['selectedFill'], s['track'], 3)
    need('selectedInk/fill', s['selectedInk'], s['selectedFill'], 4.5)
    need('border/card', s['border'], s['card'], 3)
    if dark:
        need('primary/background', s['primary'], s['background'], 3)
        bg = t['bgDark']
        stops = [mix(bg, t[k], a) for k, a in (('primary', .30), ('incomeDark', .16), ('accentDark', .24))]
        amount = readable_on(t['primary'], stops, toward='FFFFFF')
        for i, stop in enumerate(stops):
            need('hero amount/stop%d' % i, amount, stop, 4.5)
            need('hero text/stop%d' % i, s['text'], stop, 4.5)
    else:  # hero «Б» (owner 2026-11-16): strong light gradient from the primary, dark ink on it
        for i, stop in enumerate(hero_strong(t)):
            need('hero ink/stop%d' % i, s['text'], stop, 4.5)
    for name, d, lt in SWATCHES:
        fill = d if dark else lt
        need('sw %s ink' % name, INK, fill, 4.5)
        r = ring(fill, s['card'])
        need('sw %s/card' % name, r or fill, s['card'], 3)
    return rows


def main():
    worst = []
    for row in THEMES:
        t = theme(row)
        for dark in (True, False):
            bad = [(lab, round(v, 2), lim) for lab, v, lim in audit(t, dark) if v < lim]
            mn = min(v for _, v, _ in audit(t, dark))
            print('%-10s %-6s min %.2f  %s' % (t['ru'], 'тёмная' if dark else 'светлая', mn, bad or 'ok'))
            worst += bad
    prim = [(de00(a[4], b[4]), a[1], b[1]) for a, b in itertools.combinations(THEMES, 2)]
    print('closest primaries (ΔE00):', ', '.join('%s/%s %.0f' % (a, b, d) for d, a, b in sorted(prim)[:5]))
    for k, idx in (('dark', 1), ('light', 2)):
        ps = sorted((de00(a[idx], b[idx]), a[0], b[0]) for a, b in itertools.combinations(SWATCHES, 2))
        print('swatches %s: min ΔE00 %.1f (%s/%s)' % (k, *ps[0]))
    if '--json' in sys.argv:
        out = sys.argv[sys.argv.index('--json') + 1]
        json.dump({'themes': [dict(theme(r), dark=scheme(theme(r), True), light=scheme(theme(r), False)) for r in THEMES],
                   'swatches': SWATCHES, 'ink': INK}, open(out, 'w'), ensure_ascii=False, indent=1)
    return 1 if worst else 0


if __name__ == '__main__':
    sys.exit(main())
