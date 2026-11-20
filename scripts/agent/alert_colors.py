#!/usr/bin/env python3
"""Attention colours per theme (owner 2026-11-20): warning and danger cards must suit each of the 9 themes and catch the eye.

Warning was one amber for every theme (StatusColors) — it melted into Banana's yellow, Kvas's amber, Cornflower's wheat;
danger is the palette's own expense colour. Owner's poll 2026-11-20 chose Б → PaletteSpec.warningDark/Light. This script proposes a warning hue per theme: the one farthest (ΔE2000) from the
theme's primary, accent, tertiary, income and expense (А: only warm hues, Б: any), lightness solved for text ≥ 4.5:1 on cards.

Usage: python3 scripts/agent/alert_colors.py [--html out.html]   → table; --html — a preview page for the poll (PNG via render).
Surfaces mirror theme/Color.kt (sRGB mix here, Oklab in Compose — the Kotlin contrast tests have the last word).
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from colorlab import contrast, de00, mix, oklch  # noqa: E402
from themes_1_0 import THEMES, theme  # noqa: E402

AMBER = {'dark': 'FFD43B', 'light': '7A5400'}  # StatusColors before 2026-11-20
NEED = 4.8  # text on the washed card, a little above 4.5 for the Oklab/sRGB gap
CLASH = 20  # ΔE2000 under this = "looks like one of the theme's colours"


def surfaces(t, dark):
    p = t['primary']
    if dark:
        page = mix(t['bgDark'], p, .06)
        return page, mix(page, p, .07)
    return 'FFFFFF', mix('FFFFFF', p, .12)


def neighbours(t, dark):
    k = 'Dark' if dark else 'Light'
    return [t['primary'], t['accent' + k], t['tertiary' + k], t['income' + k], t['expense' + k]]


def solve(hue, page, surface, dark, chroma=0.19):
    """The most vivid shade of [hue] whose text reads ≥ NEED on the page, the card and its 12 % wash of itself."""
    steps = [i / 200 for i in range(200)]
    for L in (reversed(steps) if not dark else steps):
        if dark and L < 0.55:
            continue
        c = oklch(L, chroma, hue)
        card = mix(surface, c, .12)
        if min(contrast(c, page), contrast(c, surface), contrast(c, card)) >= NEED:
            return c
    return None


def warm(h):
    return 35 <= h <= 110  # orange … amber … yellow in OKLCH


def propose(t, warm_only):
    """Best hue for both modes at once: max of the smaller ΔE to the theme's colours; [warm_only] — orange…yellow only."""
    best = None
    for hue in range(0, 360, 3):
        if warm_only and not warm(hue):
            continue
        pair, score = {}, 1e9
        for mode in ('dark', 'light'):
            dark = mode == 'dark'
            page, surface = surfaces(t, dark)
            c = solve(hue, page, surface, dark)
            if c is None:
                score = -1
                break
            pair[mode] = c
            score = min(score, min(de00(c, n) for n in neighbours(t, dark)))
        if score < 0:
            continue
        if best is None or score > best[0]:
            best = (score, hue, pair)
    return best


def clash(t, colors):
    return min(min(de00(colors[m], n) for n in neighbours(t, m == 'dark')) for m in ('dark', 'light'))


def rows():
    out = []
    for row in THEMES:
        t = theme(row)
        _, _, a = propose(t, True)
        _, _, b = propose(t, False)
        out.append(dict(t=t, a=a, b=b, a_de=clash(t, a), b_de=clash(t, b), amber_de=clash(t, AMBER)))
    return out


def card(c, page, surface, ink, title, text):
    wash = mix(surface, c, .12)
    return ('<div style="background:%s;border:1px solid #%s8C;border-radius:16px;padding:10px 12px;margin-top:8px">'
            '<div style="color:#%s;font-weight:600;font-size:14px">%s</div><div style="color:#%s;font-size:12px;opacity:.8">%s</div></div>'
            % ('#' + wash, c, c, title, ink, text))


def theme_cell(t, warn, dark):
    page, surface = surfaces(t, dark)
    ink = 'F2F2F2' if dark else '1A1A1A'
    exp = t['expenseDark' if dark else 'expenseLight']
    plain = ('<div style="background:#%s;border:1px solid #%s;border-radius:16px;padding:10px 12px">'
             '<div style="color:#%s;font-size:12px">Баланс</div><div style="color:#%s;font-weight:700;font-size:18px">9 640 USD</div>'
             '<div style="margin-top:6px;display:inline-block;background:#%s;color:#%s;border-radius:99px;padding:4px 12px;font-size:12px;font-weight:600">Обновить</div></div>'
             % (surface, mix(surface, ink, .25), ink, ink, t['primary'], t['onPrimary']))
    return ('<div style="background:#%s;padding:10px;border-radius:12px;width:220px">'
            '<div style="color:#%s;font-size:12px;font-weight:700;margin-bottom:6px">%s · %s</div>%s%s%s</div>'
            % (page, ink, t['ru'], 'тёмная' if dark else 'светлая', plain,
               card(warn, page, surface, ink, 'Нет курсов', 'ARS — 3 дня назад'),
               card(exp, page, surface, ink, 'Лимит превышен', 'Продукты: +40 USD')))


def page_html(data, key, title):
    cells = ''.join('<div style="display:flex;gap:8px">%s%s</div>' % (theme_cell(r['t'], r[key]['dark'], True), theme_cell(r['t'], r[key]['light'], False))
                    for r in data)
    return ('<!doctype html><meta charset="utf-8"><body style="margin:0;background:#202020;font-family:sans-serif">'
            '<div style="padding:12px;color:#fff;font-size:16px;font-weight:700">%s</div>'
            '<div style="display:grid;grid-template-columns:repeat(3,auto);gap:12px;padding:0 12px 12px">%s</div></body>' % (title, cells))


def main():
    data = rows()
    print('%-11s %-6s | %-15s %-5s | %-15s %-5s' % ('тема', 'янтарь', 'А тёплый', 'ΔE', 'Б любой', 'ΔE'))
    for r in data:
        print('%-11s %5.1f  | %s/%s %5.1f | %s/%s %5.1f' % (r['t']['ru'], r['amber_de'], r['a']['dark'], r['a']['light'], r['a_de'],
                                                         r['b']['dark'], r['b']['light'], r['b_de']))
    if '--html' in sys.argv:
        out = sys.argv[sys.argv.index('--html') + 1]
        os.makedirs(out, exist_ok=True)
        open(os.path.join(out, 'AlertA.html'), 'w').write(page_html(data, 'a', 'А · свой тёплый цвет внимания у каждой темы'))
        open(os.path.join(out, 'AlertB.html'), 'w').write(page_html(data, 'b', 'Б · любой оттенок, самый непохожий на цвета темы'))
        cur = [dict(r, c=AMBER) for r in data]
        open(os.path.join(out, 'AlertNow.html'), 'w').write(page_html(cur, 'c', 'Сейчас · один янтарь для всех тем'))


if __name__ == '__main__':
    main()
