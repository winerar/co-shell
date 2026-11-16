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
from colorlab import contrast, de00, mix  # noqa: E402

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
    ('Espresso', 'Эспрессо', 'Напитки', 'пастель', 'EBC096', '2B1708', '8EBBFF', '2D5FA8', 'C9B3FF', '5B3FD0', '7FE0B0', '006F47',
     'FF6B6B', 'BE1529', '110B08', 'FBF6F0', ['F3D9BF', 'D9E6FF', 'F7E7D6'], 'крема + голубой'),
    ('Kvas', 'Квас', 'Напитки', 'неон', 'FFB020', '2A1700', '3DDBD9', '036D73', 'FF7A5C', 'AE3818', '9CF06B', '3B6E03',
     'FF5C7A', 'C4063B', '0D0904', 'FFF8EB', ['FFD98A', 'FFE9C8', 'B8F0EE'], 'янтарь + бирюза'),
    ('Sakura', 'Сакура', 'Растения', 'пастель', 'FFB3CF', '3A0A1E', '8EE6A0', '0A7030', 'C9B6FF', '5B3FD0', '7FD0FF', '00658A',
     'FF6B5C', 'C0181A', '120A0E', 'FFF5F8', ['FFD6E4', 'FFE9F0', 'D2F3DA'], 'розовый + листва'),
    ('Cornflower', 'Василёк', 'Растения', 'неон', '5C8DFF', '000B2A', 'FFCC4D', '7A5600', 'FF7A5C', 'B33114', '6BF08F', '01712F',
     'FF5C8A', 'C0084C', '060912', 'F4F7FF', ['BFD3FF', 'FFE9B0', 'D4E0FF'], 'синий + пшеничный'),
    ('Lotus', 'Лотос', 'Растения', 'неон', 'D06BFF', '22003A', 'C7F25C', '4C6902', 'FF8AC8', 'AD2C6B', '5CE6FF', '05697F',
     'FF5C6E', 'C3013A', '0E0814', 'FBF5FF', ['ECCBFF', 'E8F7C2', 'FFD6EC'], 'орхидея + лист лотоса'),
]
KEYS = ['id', 'ru', 'group', 'kind', 'primary', 'onPrimary', 'accentDark', 'accentLight', 'tertiaryDark', 'tertiaryLight',
        'incomeDark', 'incomeLight', 'expenseDark', 'expenseLight', 'bgDark', 'bgLight', 'heroLight', 'note']

# 24 icon colours: name, dark theme fill (stored), light theme fill. Ink on both — INK. ΔE2000 ≥ 10 in each theme.
SWATCHES = [
    ('red', 'FF5468', 'EE435B'), ('terracotta', 'D9765F', 'DD4D35'), ('orange', 'FF8A3D', 'D36503'), ('peach', 'FFB08F', 'C37859'),
    ('caramel', 'C98B55', 'A47441'), ('amber', 'FFC233', 'AE861E'), ('sand', 'E6D2A6', '948558'), ('yellow', 'F2E640', '888303'),
    ('lime', 'B4F03C', '679202'), ('sage', 'A9D9A0', '618750'), ('green', '4CE07A', '069E4A'), ('seafoam', 'A6EEDD', '719386'),
    ('teal', '2FE0C8', '009584'), ('cyan', '3CD3F5', '0391AB'), ('sky', '6FB2FF', '4485D1'), ('steel', '9DADBB', '768896'),
    ('periwinkle', 'B8C6FF', '727DAD'), ('indigo', '8A8CFF', '7C86FB'), ('violet', 'B47CFF', '9F65EA'), ('lavender', 'DDBBFF', '9A79B6'),
    ('magenta', 'EE5FE0', 'E131D4'), ('mauve', 'C48FB0', '95758B'), ('pink', 'FF7AAE', 'D8508B'), ('blush', 'FFBBD6', 'C37D8E'),
]
INK = '0B0B0B'


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
    for name, d, lt in SWATCHES:
        fill = d if dark else lt
        need('sw %s ink' % name, INK, fill, 4.5)
        need('sw %s/card' % name, fill, s['card'], 3)
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
