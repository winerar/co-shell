#!/usr/bin/env python3
"""Mockups for the 1.0 colour work (#385 contrast, #354 themes, #105 icon palette): standalone HTML → PNG for the poll.

Usage: python3 scripts/agent/mockup_themes_1_0.py <out-dir>
Writes <out-dir>/html/*.html and renders <out-dir>/img/*.png (Playwright). Colours come from themes_1_0.py.
"""
import os
import subprocess
import sys

HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, HERE)
from colorlab import contrast, mix, to_oklch, oklch  # noqa: E402
from mockup_bricks import ICON  # noqa: E402
import themes_1_0 as T  # noqa: E402

ROOT = os.path.dirname(os.path.dirname(HERE))
FONT = os.path.join(ROOT, 'core/designsystem/src/main/res/font')
SW = {n: (d, lt) for n, d, lt in T.SWATCHES}


def svg(name, color, size=20, width=1.9):
    return ('<svg width="%d" height="%d" viewBox="0 0 24 24" fill="none" stroke="#%s" stroke-width="%s" stroke-linecap="round" '
            'stroke-linejoin="round"><path d="%s"/></svg>' % (size, size, color, width, ICON.get(name, name)))


def page(body, w, h, bg='0B0B0B'):
    return '''<!doctype html><html lang="ru"><head><meta charset="utf-8"><style>
@font-face{font-family:Onest;src:url(file://%s/onest.ttf)}
@font-face{font-family:Mono;src:url(file://%s/jetbrains_mono.ttf)}
*{box-sizing:border-box} body{margin:0;width:%dpx;height:%dpx;background:#%s;font-family:Onest,sans-serif;overflow:hidden}
.mono{font-family:Mono,monospace;font-variant-numeric:tabular-nums}
.row{display:flex;align-items:center}
</style></head><body>%s</body></html>''' % (FONT, FONT, w, h, bg, body)


def ink_on(fill, base, need=4.5, dark=False):
    """A shade of `base` readable on `fill` (tag labels, #385): darker in the light theme, lighter in the dark."""
    L, C, H = to_oklch(base)
    c = base
    while contrast(c, fill) < need and 0.05 < L < 0.98:
        L += 0.01 if dark else -0.01
        c = oklch(L, C, H)
    return c


def icon_dot(name, glyph, dark, size=38, surface=None, ring=True):
    d, lt = SW[name]
    fill = d if dark else lt
    r = T.ring(fill, surface or ('1A1A1A' if dark else 'FFFFFF')) if ring else None
    return ('<div class="row" style="width:%dpx;height:%dpx;border-radius:50%%;background:#%s;justify-content:center;flex:none;%s">%s</div>'
            % (size, size, fill, 'box-shadow:inset 0 0 0 1.5px #%s;' % r if r else '', svg(glyph, T.INK, size * 0.5)))


def tag(name, label, s, dark):
    base = SW[name][0 if dark else 1]
    fill = mix(base, s['background'], .78) if dark else mix(base, 'FFFFFF', .84)
    return ('<span style="padding:3px 9px;border-radius:999px;background:#%s;color:#%s;font-size:12px;font-weight:600">%s</span>'
            % (fill, ink_on(fill, base, dark=dark), label))


def hero_bg(t, dark, strong=False):
    if strong and not dark:
        stops = [t['primary'], mix(t['primary'], 'FFFFFF', .3), mix(t['accentDark'], 'FFFFFF', .2)]
    elif dark:
        bg = t['bgDark']
        stops = [mix(bg, t['primary'], .30), mix(bg, t['incomeDark'], .16), mix(bg, t['accentDark'], .24)]
    else:
        stops = t['heroLight']
    return 'linear-gradient(135deg,%s)' % ','.join('#' + c for c in stops)


def segmented(items, sel, s, variant='A', dark=False):
    out = []
    for it in items:
        on = it == sel
        if on:
            st = selected_style(s, variant, dark)
        else:
            st = 'color:#%s;' % s['textMuted']
        out.append('<div class="row" style="flex:1;justify-content:center;height:34px;border-radius:999px;font-size:13px;font-weight:600;%s">%s</div>'
                   % (st, ('✓ ' if on and variant == 'C' else '') + it))
    return '<div class="row" style="background:#%s;border-radius:999px;padding:3px;gap:2px">%s</div>' % (s['track'], ''.join(out))


def selected_style(s, variant, dark):
    if dark or variant == 'A':
        return 'background:#%s;color:#%s;' % (s['selectedFill'], s['selectedInk'])
    p, onp = s['primary'], s['onPrimary']
    if variant == 'B':
        return 'background:#%s;color:#%s;box-shadow:inset 0 0 0 2px #%s;' % (p, onp, s['text'])
    return 'background:#%s;color:#%s;' % (p, onp)  # C: neon fill + check mark (today's look)


OPS = [('Супермаркет', 'Продукты', 'cart', 'lime', '−42 300', 'ARS', 'e', [('teal', 'семья')]),
       ('Зарплата', 'Зарплата', 'salary', 'green', '+850 000', 'AMD', 'i', []),
       ('Кафе «Ла Бока»', 'Кафе', 'cafe', 'orange', '−18 500', 'ARS', 'e', [('violet', 'отпуск')]),
       ('Такси', 'Транспорт', 'taxi', 'amber', '−6 200', 'ARS', 'e', []),
       ('Аптека', 'Здоровье', 'pill', 'red', '−9 870', 'ARS', 'e', [('pink', 'дети')]),
       ('Интернет', 'Дом', 'wifi', 'sky', '−12 000', 'AMD', 'e', [])]


def screen(t, dark, variant='A', w=360, h=740, accent=''):
    s = T.scheme(t, dark)
    tint = mix(t['primary'], s['background'], .62) if accent == 'bar' and not dark else None
    hero_ink = s['text'] if dark else T.neutrals(t['bgLight'], False)['text']
    amount_col = t['primary'] if dark else hero_ink
    hero = ('<div style="margin:6px 16px 0;border-radius:24px;padding:16px 18px;background:%s;color:#%s">'
            '<div style="font-size:13px;opacity:.8">Свободно в ноябре</div>'
            '<div class="mono" style="font-size:30px;font-weight:700;color:#%s;margin:4px 0 8px">1 284 500 <span style="font-size:15px;opacity:.7">ARS</span></div>'
            '<div class="row" style="gap:16px;font-size:12px"><span>Доходы <b class="mono">+2,1 млн</b></span><span>Расходы <b class="mono">−816 тыс.</b></span></div>'
            '<div style="margin-top:10px;height:6px;border-radius:3px;background:rgba(%s,.16)"><div style="width:62%%;height:6px;border-radius:3px;background:#%s"></div></div>'
            '</div>') % (hero_bg(t, dark, accent == 'hero'), hero_ink, amount_col, '255,255,255' if dark else '0,0,0', amount_col)
    chips = ''.join('<span style="padding:6px 12px;border-radius:999px;font-size:12px;font-weight:600;%s">%s</span>' % (
        selected_style(s, variant, dark) if i == 0 else 'color:#%s;box-shadow:inset 0 0 0 1px #%s;' % (s['text'], s['border']), c)
        for i, c in enumerate(['Все', 'Расходы', 'Доходы', 'Переводы']))
    rows = []
    for name, sub, glyph, sw, amt, cur, kind, tags in OPS:
        col = s['expense'] if kind == 'e' else s['income']
        tg = ''.join(tag(a, b, s, dark) for a, b in tags)
        rows.append('<div class="row" style="gap:12px;padding:8px 0">%s<div style="flex:1;min-width:0"><div style="font-size:14px;font-weight:600;color:#%s">%s</div>'
                    '<div class="row" style="gap:6px;margin-top:2px"><span style="font-size:12px;color:#%s">%s</span>%s</div></div>'
                    '<div class="mono" style="font-size:14px;font-weight:700;color:#%s">%s <span style="font-weight:500;color:#%s;font-size:11px">%s</span></div></div>'
                    % (icon_dot(sw, glyph, dark, surface=s['card']), s['text'], name, s['textMuted'], sub, tg, col, amt, s['textMuted'], cur))
    if accent == 'detail' and not dark:
        rows.insert(0, '<div class="row" style="gap:8px;padding:8px 0 2px;font-size:13px;font-weight:700">Сегодня<span style="padding:1px 8px;border-radius:999px;background:#%s;color:#%s;font-size:12px">6</span>'
                       '<span style="margin-left:auto;width:36px;height:20px;border-radius:10px;background:#%s;position:relative"><span style="position:absolute;right:2px;top:2px;width:16px;height:16px;border-radius:50%%;background:#%s"></span></span></div>'
                       % (t['primary'], t['onPrimary'], t['primary'], t['onPrimary']))
    card_bg = s['card']
    tabs = []
    for i, (lbl, ic) in enumerate([('Обзор', 'home'), ('Операции', 'list'), ('Счета', 'wallet'), ('Ещё', 'more')]):
        on = i == 0
        pill = ('<div class="row" style="justify-content:center;width:56px;height:30px;border-radius:999px;%s">%s</div>'
                % (selected_style(s, variant, dark) if on else '', svg(ic, (s['selectedInk'] if (dark or variant == 'A') else s['onPrimary']) if on else s['textMuted'], 20)))
        if on and accent == 'detail' and not dark:
            pill += '<span style="width:18px;height:4px;border-radius:2px;background:#%s;margin-top:-1px"></span>' % t['primary']
        tabs.append('<div style="flex:1;display:flex;flex-direction:column;align-items:center;gap:3px">%s<span style="font-size:11px;font-weight:%s;color:#%s">%s</span></div>'
                    % (pill, 700 if on else 500, s['text'] if on else s['textMuted'], lbl))
    body = ('<div style="position:relative;width:%dpx;height:%dpx;background:#%s;color:#%s;overflow:hidden;border-radius:28px">'
            '<div style="background:%s"><div class="row" style="height:28px;padding:0 20px;font-size:12px;color:#%s;justify-content:space-between"><span>9:41</span><span>●●●</span></div>'
            '<div class="row" style="height:48px;padding:0 16px;justify-content:space-between"><b style="font-size:22px">Обзор</b>'
            '<div class="row" style="width:40px;height:40px;border-radius:50%%;background:#%s;justify-content:center">%s</div></div></div>'
            '%s<div style="padding:12px 16px 0">%s</div>'
            '<div class="row" style="gap:6px;padding:10px 16px 0">%s</div>'
            '<div style="margin:10px 16px 0;padding:4px 14px;border-radius:20px;background:#%s">%s</div>'
            '<div class="row" style="position:absolute;right:16px;bottom:84px;height:52px;padding:0 20px;gap:8px;border-radius:18px;background:#%s;color:#%s;font-weight:700;font-size:15px">%s Добавить</div>'
            '<div class="row" style="position:absolute;left:0;right:0;bottom:0;height:72px;background:#%s;padding:0 8px">%s</div>'
            '</div>') % (w, h, s['background'], s['text'], ('linear-gradient(#%s,#%s)' % (tint, s['background'])) if tint else 'none', s['textMuted'], s['high'], svg('search', s['text']), hero,
                         segmented(['Месяц', 'Неделя', 'Год'], 'Месяц', s, variant, dark), chips, card_bg, ''.join(rows),
                         t['primary'], t['onPrimary'], svg('plus', t['onPrimary'], 20, 2.4), tint or s['low'], ''.join(tabs))
    return body


def dots(t):
    cols = [(t['primary'], 'основной'), (t['accentDark'], 'второй'), (t['incomeDark'], 'доход'), (t['expenseDark'], 'расход')]
    return ''.join('<div class="row" style="gap:6px;font-size:13px;color:#ccc"><span style="width:18px;height:18px;border-radius:50%%;background:#%s"></span>%s</div>' % c for c in cols)


def theme_board(t):
    head = ('<div class="row" style="gap:14px;padding:18px 24px 12px;color:#fff"><div style="font-size:30px;font-weight:700">%s</div>'
            '<div style="font-size:14px;color:#aaa">%s · %s · %s</div><div class="row" style="gap:14px;margin-left:auto">%s</div></div>'
            % (t['ru'], t['group'], t['kind'], t['note'], dots(t)))
    body = head + '<div class="row" style="gap:24px;padding:0 24px">%s%s</div>' % (screen(t, True), screen(t, False))
    return page(body, 792, 820, '141414')


def overview():
    cells = []
    for row in T.THEMES:
        t = T.theme(row)
        dk, lt = T.scheme(t, True), T.scheme(t, False)
        half = lambda s, dark: ('<div style="flex:1;background:#%s;padding:10px;display:flex;flex-direction:column;gap:8px">'  # noqa: E731
                                '<div style="height:34px;border-radius:12px;background:%s"></div>'
                                '<div class="row" style="gap:6px"><span style="flex:1;height:22px;border-radius:999px;background:#%s"></span>'
                                '<span style="width:22px;height:22px;border-radius:50%%;background:#%s"></span></div>'
                                '<div class="mono" style="font-size:11px;font-weight:700;color:#%s">+850 000</div>'
                                '<div class="mono" style="font-size:11px;font-weight:700;color:#%s">−42 300</div></div>'
                                % (s['background'], hero_bg(t, dark), t['primary'], s['accent'], s['income'], s['expense']))
        cells.append('<div style="border-radius:20px;overflow:hidden;background:#222"><div style="display:flex;height:150px">%s%s</div>'
                     '<div style="padding:8px 12px;color:#fff"><b style="font-size:16px">%s</b> <span style="color:#999;font-size:12px">%s · %s</span></div></div>'
                     % (half(dk, True), half(lt, False), t['ru'], t['kind'], t['note']))
    body = ('<div style="padding:20px 24px;color:#fff;font-size:22px;font-weight:700">9 тем: фрукты · напитки · растения</div>'
            '<div style="display:grid;grid-template-columns:repeat(3,1fr);gap:16px;padding:0 24px">%s</div>' % ''.join(cells))
    return page(body, 900, 720, '141414')


def phone(inner, s, title='Тема'):
    return ('<div style="position:relative;width:390px;height:844px;background:#%s;color:#%s;overflow:hidden">'
            '<div class="row" style="height:28px;padding:0 20px;font-size:12px;color:#%s;justify-content:space-between"><span>9:41</span><span>●●●</span></div>'
            '<div class="row" style="height:56px;padding:0 8px;gap:4px"><div class="row" style="width:44px;height:44px;border-radius:50%%;background:#%s;justify-content:center">%s</div>'
            '<b style="font-size:22px">%s</b></div>%s</div>') % (s['background'], s['text'], s['textMuted'], s['high'], svg('back', s['text']), title, inner)


def mode_seg(s, dark):
    return '<div style="padding:4px 16px 0">%s</div>' % segmented(['Системная', 'Светлая', 'Тёмная'], 'Тёмная' if dark else 'Светлая', s, 'A', dark)


def mini(t, dark, w, h, r=14):
    s = T.scheme(t, dark)
    return ('<div style="width:%dpx;height:%dpx;border-radius:%dpx;background:#%s;box-shadow:inset 0 0 0 1px rgba(128,128,128,.35);padding:7px;display:flex;flex-direction:column;gap:5px;overflow:hidden">'
            '<div style="height:%dpx;border-radius:9px;background:%s"></div>'
            '<div class="row" style="gap:4px"><span style="width:14px;height:14px;border-radius:50%%;background:#%s"></span><span style="flex:1;height:5px;border-radius:3px;background:#%s"></span></div>'
            '<div class="row" style="gap:4px"><span style="width:14px;height:14px;border-radius:50%%;background:#%s"></span><span style="flex:1;height:5px;border-radius:3px;background:#%s"></span></div>'
            '<div class="row" style="margin-top:auto;justify-content:flex-end"><span style="width:34px;height:16px;border-radius:6px;background:#%s"></span></div></div>'
            ) % (w, h, r, s['background'], int(h * 0.3), hero_bg(t, dark), s['accent'], s['lineSoft'], s['income'], s['lineSoft'], t['primary'])


def picker(variant, dark):
    cur = T.theme(T.THEMES[0])
    s = T.scheme(cur, dark)
    themes = [T.theme(r) for r in T.THEMES]
    ring = lambda on: 'box-shadow:0 0 0 3px #%s, 0 0 0 5px #%s;' % (s['background'], s['text']) if on else ''  # noqa: E731
    if variant == 'A':
        tiles = []
        for i, t in enumerate(themes):
            on = i == 0
            tiles.append('<div style="display:flex;flex-direction:column;align-items:center;gap:6px"><div style="border-radius:16px;position:relative;%s">%s%s</div>'
                         '<span style="font-size:13px;font-weight:%s">%s</span></div>'
                         % (ring(on), mini(t, dark, 106, 150), '<div class="row" style="position:absolute;top:6px;right:6px;width:22px;height:22px;border-radius:50%%;background:#%s;justify-content:center">%s</div>' % (s['text'], svg('check', s['background'], 14, 3)) if on else '',
                            700 if on else 500, t['ru']))
        inner = (mode_seg(s, dark) + '<div style="padding:16px 16px 0;font-size:13px;color:#%s">Видно, как выглядят экраны. Нажмите — приложение сразу перекрасится.</div>' % s['textMuted']
                 + '<div style="display:grid;grid-template-columns:repeat(3,1fr);gap:16px 10px;padding:14px 16px 0">%s</div>' % ''.join(tiles))
    elif variant == 'B':
        t0 = themes[0]
        big = ('<div class="row" style="gap:10px;justify-content:center;padding:14px 16px 0">%s%s</div>' % (mini(t0, True, 160, 250, 20), mini(t0, False, 160, 250, 20)))
        circles = []
        for i, t in enumerate(themes):
            on = i == 0
            circles.append('<div style="display:flex;flex-direction:column;align-items:center;gap:6px;width:68px"><div style="width:54px;height:54px;border-radius:50%%;'
                           'background:conic-gradient(#%s 0 50%%, #%s 50%% 75%%, #%s 75%%);%s"></div><span style="font-size:12px;font-weight:%s">%s</span></div>'
                           % (t['primary'], t['accentDark'], t['bgDark'] if dark else t['bgLight'], ring(on), 700 if on else 500, t['ru']))
        inner = (big + '<div style="text-align:center;margin-top:10px;font-weight:700;font-size:18px">Лайм</div><div style="text-align:center;font-size:13px;color:#%s">неон · кислотный лайм + фиолетовый</div>' % s['textMuted']
                 + '<div style="display:flex;flex-wrap:wrap;gap:12px 6px;justify-content:center;padding:16px 12px 0">%s</div>' % ''.join(circles) + '<div style="margin-top:14px">%s</div>' % mode_seg(s, dark))
    else:
        rows = []
        for i, t in enumerate(themes):
            on = i == 0
            sw = ('<div style="width:40px;height:40px;border-radius:50%%;background:linear-gradient(135deg,#%s 50%%,#%s 50%%);flex:none"></div>' % (t['primary'], t['accentDark']))
            radio = ('<div class="row" style="width:22px;height:22px;border-radius:50%%;justify-content:center;%s">%s</div>'
                     % ('background:#%s' % s['text'] if on else 'box-shadow:inset 0 0 0 2px #%s' % s['border'], '<span style="width:8px;height:8px;border-radius:50%%;background:#%s"></span>' % s['background'] if on else ''))
            rows.append('<div class="row" style="gap:14px;height:62px;padding:0 14px;border-radius:16px;%s">%s<div style="flex:1"><div style="font-size:15px;font-weight:600">%s</div>'
                        '<div style="font-size:12px;color:#%s">%s · %s</div></div>%s</div>'
                        % ('background:#%s;' % s['high'] if on else '', sw, t['ru'], s['textMuted'], t['kind'], t['note'], radio))
        inner = mode_seg(s, dark) + '<div style="padding:12px 8px 0">%s</div>' % ''.join(rows)
    return page(phone(inner, s), 390, 844, s['background'])


def selected_board():
    cols = []
    for v, title, d in (('A', 'А: чернильная пилюля', 'выбранное — тёмная заливка, подпись цветом темы'),
                        ('B', 'Б: неон + обводка', 'заливка темы и тёмная рамка 2 dp'),
                        ('C', 'В: как сейчас + галочка', 'заливка темы, отличие по оттенку и галочке')):
        shots = ''.join('<div>%s</div>' % screen(T.theme(T.THEMES[i]), False, v, 300, 560) for i in (0, 2))
        cols.append('<div><div style="color:#fff;font-size:18px;font-weight:700">%s</div><div style="color:#aaa;font-size:13px;margin:2px 0 10px">%s</div>'
                    '<div class="row" style="gap:10px;align-items:flex-start">%s</div></div>' % (title, d, shots))
    return page('<div class="row" style="gap:28px;padding:20px;align-items:flex-start">%s</div>' % ''.join(cols), 1960, 680, '141414')


def accent_board():
    cols = []
    for v, title, d in (('detail', 'А: детали', 'бейджи, переключатели, полоска под вкладкой — заливкой темы'),
                        ('hero', 'Б: яркая главная карточка', 'градиент карточки начинается с основного цвета'),
                        ('bar', 'В: тонированные панели', 'верх экрана и нижняя панель с оттенком основного цвета')):
        shots = ''.join('<div>%s</div>' % screen(T.theme(T.THEMES[i]), False, 'A', 300, 600, v) for i in (0, 7, 8))
        cols.append('<div><div style="color:#fff;font-size:18px;font-weight:700">%s</div><div style="color:#aaa;font-size:13px;margin:2px 0 10px">%s</div>'
                    '<div class="row" style="gap:10px;align-items:flex-start">%s</div></div>' % (title, d, shots))
    return page('<div class="row" style="gap:28px;padding:20px;align-items:flex-start">%s</div>' % ''.join(cols), 2940, 700, '141414')


GLYPHS = ['cart', 'house', 'taxi', 'cafe', 'gift', 'pill', 'film', 'phone', 'school', 'bus', 'wifi', 'salary',
          'spark', 'cloud', 'bolt', 'chart', 'budget', 'user', 'mail', 'note', 'calendar', 'lock', 'folder', 'rate']
RU = {'red': 'красный', 'coral': 'коралл', 'orange': 'оранжевый', 'peach': 'персик', 'caramel': 'карамель', 'amber': 'янтарь',
      'sand': 'песок', 'yellow': 'жёлтый', 'lime': 'лайм', 'sage': 'шалфей', 'green': 'зелёный', 'seafoam': 'морская пена',
      'teal': 'бирюза', 'cyan': 'циан', 'sky': 'небо', 'steel': 'сталь', 'periwinkle': 'барвинок', 'indigo': 'индиго',
      'violet': 'фиолетовый', 'lavender': 'лаванда', 'magenta': 'маджента', 'mauve': 'мальва', 'pink': 'розовый', 'blush': 'румянец'}


def palette_board(ring=True):
    halves = []
    for dark in (True, False):
        t = T.theme(T.THEMES[0])
        s = T.scheme(t, dark)
        cells = ''.join('<div style="display:flex;flex-direction:column;align-items:center;gap:6px;width:92px">%s<span style="font-size:11px;color:#%s">%s</span></div>'
                        % (icon_dot(n, g, dark, 48, s['background'], ring), s['textMuted'], RU[n]) for (n, _, _), g in zip(T.SWATCHES, GLYPHS))
        acc = ''.join('<div class="row" style="gap:12px;padding:7px 0">%s<div style="flex:1"><div style="font-size:14px;font-weight:600;color:#%s">%s</div>'
                      '<div style="font-size:12px;color:#%s">%s</div></div></div>' % (
                          '<div class="row" style="width:40px;height:40px;border-radius:12px;background:#%s;justify-content:center;font-weight:700;font-size:14px;color:#%s">%s</div>'
                          % (SW[n][0 if dark else 1], T.INK, a), s['text'], name, s['textMuted'], sub)
                      for n, a, name, sub in (('sky', 'GA', 'Galicia', 'ARS · карта'), ('amber', 'AM', 'Ameria', 'AMD'), ('violet', 'US', 'USDT-карта', 'USDT'),
                                              ('sage', 'НЛ', 'Наличные', 'USD')))
        halves.append('<div style="flex:1;background:#%s;border-radius:28px;padding:20px"><div style="font-size:18px;font-weight:700;color:#%s;margin-bottom:14px">%s</div>'
                      '<div style="display:flex;flex-wrap:wrap;gap:16px 4px">%s</div><div style="margin-top:16px;padding:6px 16px;border-radius:20px;background:#%s">%s</div></div>'
                      % (s['background'], s['text'], 'Тёмная тема' if dark else 'Светлая тема', cells, s['card'], acc))
    head = ('<div style="padding:18px 24px 4px;color:#fff;font-size:22px;font-weight:700">24 цвета значков</div>'
            '<div style="padding:0 24px 14px;color:#aaa;font-size:13px">Одни и те же цвета в обеих темах: любые два различаются на ΔE2000 ≥ 16,5 (было 11,5). Рисунок тёмный на любом цвете (≥ 4,6:1). '
            + ('Бледный кружок на светлом фоне — с тонким ободком своего цвета (≥ 3:1).' if ring else 'Без ободков: бледные кружки на светлом фоне видны слабее.') + '</div>')
    return page(head + '<div class="row" style="gap:20px;padding:0 24px;align-items:stretch">%s</div>' % ''.join(halves), 1340, 900, '141414')


def picker_palette_board():
    """Colour picker in the category editor: 24 in a 6×4 grid, fits without scrolling."""
    out = []
    for dark in (True, False):
        t = T.theme(T.THEMES[0])
        s = T.scheme(t, dark)
        cells = []
        for i, (n, d, lt) in enumerate(T.SWATCHES):
            on = i == 13
            fill = d if dark else lt
            surf = s['card'] if not dark else s['high']
            r = T.ring(fill, surf)
            sh = ['inset 0 0 0 1.5px #%s' % r] if r else []
            if on:
                sh += ['0 0 0 3px #%s' % surf, '0 0 0 5px #%s' % s['text']]
            cells.append('<div class="row" style="width:46px;height:46px;border-radius:50%%;background:#%s;justify-content:center;%s">%s</div>'
                         % (fill, 'box-shadow:%s;' % ','.join(sh) if sh else '', svg('check', T.INK, 22, 3) if on else ''))
        out.append('<div style="width:390px;background:#%s;border-radius:28px 28px 0 0;padding:18px 20px 24px;color:#%s">'
                   '<div style="width:36px;height:4px;border-radius:2px;background:#%s;margin:0 auto 14px"></div><b style="font-size:18px">Цвет</b>'
                   '<div style="display:grid;grid-template-columns:repeat(6,1fr);gap:14px 8px;margin-top:16px;justify-items:center">%s</div></div>'
                   % (s['card'] if not dark else s['high'], s['text'], s['lineSoft'], ''.join(cells)))
    return page('<div class="row" style="gap:24px;padding:24px;align-items:flex-end">%s</div>' % ''.join(out), 852, 420, '141414')


def main():
    out = sys.argv[1]
    os.makedirs(os.path.join(out, 'html'), exist_ok=True)
    pages = {'ThemesAll': (overview(), 900, 720), 'Selected': (selected_board(), 1960, 680), 'Palette24': (palette_board(), 1340, 900), 'Palette24NoRing': (palette_board(False), 1340, 900),
             'PalettePicker': (picker_palette_board(), 852, 420), 'Accent': (accent_board(), 2940, 700)}
    for row in T.THEMES:
        pages['Theme' + row[0]] = (theme_board(T.theme(row)), 792, 820)
    for v in 'ABC':
        pages['Picker' + v] = (picker(v, True), 390, 844)
        pages['Picker' + v + '@light'] = (picker(v, False), 390, 844)
    for name, (html, w, h) in pages.items():
        open(os.path.join(out, 'html', name + '.html'), 'w', encoding='utf-8').write(html)
    js = os.path.join(out, 'render.js')
    open(js, 'w').write('''const { chromium } = require('playwright'); const fs = require('fs'); const path = require('path');
(async () => { const [dir] = process.argv.slice(2); const b = await chromium.launch(); fs.mkdirSync(path.join(dir, 'img'), { recursive: true });
for (const f of fs.readdirSync(path.join(dir, 'html'))) { const html = fs.readFileSync(path.join(dir, 'html', f), 'utf8');
 const w = +/width:(\\d+)px;height:(\\d+)px;background/.exec(html)[1], h = +/width:(\\d+)px;height:(\\d+)px;background/.exec(html)[2];
 const p = await b.newPage({ viewport: { width: w, height: h }, deviceScaleFactor: 2 }); await p.goto('file://' + path.join(dir, 'html', f));
 await p.waitForTimeout(150); await p.screenshot({ path: path.join(dir, 'img', f.replace('.html', '.png')) }); await p.close(); }
await b.close(); })();''')
    subprocess.run(['node', js, os.path.abspath(out)], check=True, cwd=out)
    print('%d boards → %s/img' % (len(pages), out))


if __name__ == '__main__':
    main()
