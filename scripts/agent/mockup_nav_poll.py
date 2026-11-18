#!/usr/bin/env python3
"""Poll boards for the two open «Расхождения» (#115): the «назад» glyph and tab swiping (19В).

Usage: python3 scripts/agent/mockup_nav_poll.py <out-dir>  → <out-dir>/img/*.png (Playwright).
Then: agent.sh poll docs/design/poll-nav-1-0.json --img <out-dir>/img
Q1 «назад»: A = Material arrow (code today), B = chevron on the track (boards), C = chevron without the track.
Q2 tab swipe: A = lean (code today), B = real pager, C = lean + the bar's pill follows the finger. Filmstrips of 4 frames.
"""
import os
import subprocess
import sys

HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, HERE)
import mockup_bricks as B  # noqa: E402

ROOT = os.path.dirname(os.path.dirname(HERE))
FONT = os.path.join(ROOT, 'core/designsystem/src/main/res/font')

DARK = dict(bg=B.BG, card=B.CARD, chip=B.CHIP, ink=B.INK, ink2=B.INK2, muted=B.MUTED, line=B.LINE, lime=B.LIME, onlime=B.ONLIME, bar=B.BAR, page='#141714')
LIGHT = dict(bg='#F5F8EF', card='#FFFFFF', chip='#E4EADB', ink='#10160D', ink2='#33402B', muted='#5C6856', line='#D5DDCB', lime='#6FA800',
             onlime='#FFFFFF', bar='#E4EADB', page='#E9EEE1')

ARROW = 'M5 12h14M11 6l-6 6 6 6'  # arrow_back (Material), stroke version of ic_arrow_back
CHEV = 'M15 6l-6 6 6 6'


def glyph(path, color, size=24, w=1.9):
    return ('<svg width="%d" height="%d" viewBox="0 0 24 24" fill="none" stroke="%s" stroke-width="%s" stroke-linecap="round" '
            'stroke-linejoin="round"><path d="%s"/></svg>' % (size, size, color, w, path))


def back_button(kind, t):
    path = ARROW if kind == 'A' else CHEV
    track = 'background:%s;' % t['chip'] if kind in 'AB' else ''
    return ('<span style="width:48px;height:48px;flex:none;border-radius:50%%;%sdisplay:flex;align-items:center;justify-content:center;">%s</span>'
            % (track, glyph(path, t['ink'])))


def row(title, sub, amt, t, col):
    return ('<div style="display:flex;align-items:center;gap:12px;height:56px;"><span style="width:36px;height:36px;border-radius:50%%;background:%s;opacity:.9"></span>'
            '<span style="flex:1;display:flex;flex-direction:column"><span style="font-size:15px;color:%s">%s</span><span style="font-size:12px;color:%s">%s</span></span>'
            '<span style="font-family:\'JetBrains Mono\',monospace;font-size:14px;color:%s">%s</span></div>'
            % (col, t['ink2'], title, t['muted'], sub, t['ink'], amt))


def q1(kind, t):
    """Inner screen «Счёт»: top bar with «назад», a card and rows — the bar is what differs."""
    bar = ('<div style="display:flex;align-items:center;gap:4px;height:64px;padding:0 8px;">%s<h1 style="margin:0;font-size:22px;font-weight:700;flex:1;color:%s">'
           'Счёт Galicia</h1>%s</div>' % (back_button(kind, t), t['ink'], back_button('B', t).replace(glyph(CHEV, t['ink']), glyph('M5 12h.01M12 12h.01M19 12h.01', t['ink']))))
    card = ('<div style="margin:8px 16px;border-radius:24px;background:%s;padding:16px;"><div style="font-size:12px;color:%s">Баланс</div>'
            '<div style="font-family:\'JetBrains Mono\',monospace;font-size:26px;color:%s;margin-top:4px">42 180,50 <span style="font-size:14px;color:%s">ARS</span></div></div>'
            % (t['card'], t['muted'], t['ink'], t['muted']))
    rows = '<div style="padding:0 20px">%s%s%s</div>' % (row('Продукты', 'Вчера', '−3 200', t, '#8BC34A'), row('Кафе', '12 ноя', '−1 450', t, '#FF9800'), row('Зарплата', '10 ноя', '+120 000', t, '#00D1FF'))
    return '<div style="width:390px;height:330px;background:%s;overflow:hidden">%s%s%s</div>' % (t['bg'], bar, card, rows)


TABS = [('home', 'Обзор'), ('list', 'Операции'), ('chart', 'Отчёты'), ('more', 'Ещё')]


def nav(pill, t):
    """Bottom bar: 2 tabs, «+», 2 tabs as in the boards; [pill] — x of the active pill's centre in slot units (0…4)."""
    slots = ['home', 'list', '+', 'chart', 'more']
    cells = ''
    for i, k in enumerate(slots):
        if k == '+':
            cells += ('<span style="width:48px;height:48px;border-radius:50%%;background:%s;display:flex;align-items:center;justify-content:center;z-index:2">%s</span>'
                      % (t['lime'], B.svg('plus', t['onlime'], 26, 2.2)))
        else:
            near = max(0.0, 1 - abs(pill - i))
            cells += '<span style="z-index:2;display:flex;justify-content:center">%s</span>' % B.svg(B.ICON[k], t['onlime'] if near > .5 else t['muted'], 24, 1.7)
    left = 'calc(6px + (100%% - 12px) * %.4f / 5 - 32px)' % (pill + .5)
    return ('<div style="position:absolute;left:16px;right:16px;bottom:14px;height:64px;border-radius:999px;background:%s;display:grid;'
            'grid-template-columns:repeat(5,1fr);align-items:center;padding:0 6px;"><span style="position:absolute;top:10px;left:%s;width:64px;height:44px;'
            'border-radius:999px;background:%s;z-index:1"></span>%s</div>' % (t['bar'], left, t['lime'], cells))


def screen(title, hero, t, rows):
    body = ''.join(row(a, b, c, t, d) for a, b, c, d in rows)
    return ('<div style="width:390px;height:100%%;background:%s;"><h1 style="margin:0;height:64px;display:flex;align-items:center;padding:0 16px;font-size:28px;color:%s">%s</h1>'
            '<div style="margin:4px 16px 8px;border-radius:24px;padding:16px;height:96px;background:linear-gradient(135deg,#314617,#0E2523,#271E36);color:#EEF5E6">'
            '<div style="font-size:12px;opacity:.7">%s</div></div><div style="padding:0 20px">%s</div></div>' % (t['bg'], t['ink'], title, hero, body))


OPS = ('Операции', 'Ноябрь', [('Продукты', 'Вчера', '−3 200', '#8BC34A'), ('Кафе', '12 ноя', '−1 450', '#FF9800'), ('Зарплата', '10 ноя', '+120 000', '#00D1FF'), ('Такси', '9 ноя', '−900', '#FFEB3B')])
REP = ('Отчёты', 'Расходы · ноябрь', [('Еда', '38 %', '24 600', '#8BC34A'), ('Дом', '21 %', '13 500', '#00BCD4'), ('Транспорт', '12 %', '7 700', '#FFEB3B'), ('Здоровье', '8 %', '5 100', '#E91E63')])


def frame(cap, left_screen, right_screen, offset, scale_neighbour_gap, pill, t, note=''):
    """One 390×720 phone: the current screen shifted by [offset] px, the neighbour (or nothing) next to it."""
    cur = '<div style="position:absolute;top:0;bottom:0;width:390px;left:%dpx">%s</div>' % (offset, left_screen)
    nb = ''
    if right_screen:
        nb = '<div style="position:absolute;top:0;bottom:0;width:390px;left:%dpx">%s</div>' % (offset + (390 if offset <= 0 else -390), right_screen)
    return ('<div style="display:flex;flex-direction:column;gap:8px;align-items:center"><div style="width:390px;height:720px;position:relative;overflow:hidden;'
            'border-radius:28px;background:%s;border:1px solid %s">%s%s%s</div><div style="font-size:15px;color:%s;font-weight:700">%s</div>'
            '<div style="font-size:13px;color:%s;height:34px;text-align:center;width:390px">%s</div></div>'
            % (t['bg'], t['line'], cur, nb, nav(pill, t), t['ink'], cap, t['muted'], note))


def strip(kind, t):
    ops, rep = screen(*OPS[:2], t, OPS[2]), screen(*REP[:2], t, REP[2])
    # finger drags left: frame positions — start, 90 px, 190 px (past the threshold), released
    if kind == 'A':  # code today: the screen leans a quarter of the way, the neighbour appears only after release
        fr = [('1. Палец лёг', ops, None, 0, 1.0, 'Операции'), ('2. Ведёт влево', ops, None, -22, 1.0, 'Экран лишь наклоняется на ¼ пути'),
              ('3. Прошёл порог', ops, None, -48, 1.0, 'Соседа ещё не видно, внизу «Операции»'), ('4. Отпустил', rep, None, 0, 3.0, 'Отчёты въезжают с анимацией')]
        pills = [1, 1, 1, 3]
    elif kind == 'B':  # real pager: both screens follow the finger 1:1
        fr = [('1. Палец лёг', ops, rep, 0, 1, 'Операции'), ('2. Ведёт влево', ops, rep, -90, 1, 'Сосед выезжает вслед за пальцем'),
              ('3. Прошёл порог', ops, rep, -190, 1, 'Обе вкладки видны, как в галерее'), ('4. Отпустил', ops, rep, -390, 1, 'Доезжает и встаёт')]
        pills = [1, 1, 1, 3]
    else:  # C: lean + the bar's pill follows the finger
        fr = [('1. Палец лёг', ops, None, 0, 1, 'Операции'), ('2. Ведёт влево', ops, None, -22, 1, 'Экран наклоняется, пилюля внизу едет к «Отчётам»'),
              ('3. Прошёл порог', ops, None, -48, 1, 'Пилюля уже почти на «Отчётах»'), ('4. Отпустил', rep, None, 0, 1, 'Отчёты въезжают, пилюля на месте')]
        pills = [1, 1.8, 2.7, 3]
    html = ''
    for (cap, a, b, off, _, note), p in zip(fr, pills):
        slot = p  # pill position in slots (0…4; «+» is slot 2)
        html += frame(cap, a, b, off, 0, slot, t, note)
    return '<div style="display:flex;gap:18px;padding:20px;background:%s">%s</div>' % (t['page'], html)


def page(body, w, h, t):
    return ('<!doctype html><html lang="ru"><head><meta charset="utf-8"><style>@font-face{font-family:Onest;src:url(file://%s/onest.ttf)}'
            '@font-face{font-family:"JetBrains Mono";src:url(file://%s/jetbrains_mono.ttf)}*{box-sizing:border-box}'
            'body{margin:0;width:%dpx;height:%dpx;background:%s;font-family:Onest,sans-serif;overflow:hidden}</style></head><body>%s</body></html>'
            % (FONT, FONT, w, h, t['page'], body))


def main():
    out = sys.argv[1]
    os.makedirs(os.path.join(out, 'html'), exist_ok=True)
    pages = {}
    for k in 'ABC':
        for suffix, t in (('', DARK), ('@light', LIGHT)):
            pages['Back%s%s' % (k, suffix)] = page(q1(k, t), 390, 330, t)
    for k in 'ABC':
        pages['Swipe' + k] = page(strip(k, DARK), 4 * 390 + 3 * 18 + 40, 720 + 40 + 60, DARK)
    for name, html in pages.items():
        open(os.path.join(out, 'html', name + '.html'), 'w', encoding='utf-8').write(html)
    js = os.path.join(out, 'render.js')
    open(js, 'w').write('''const { chromium } = require('playwright'); const fs = require('fs'); const path = require('path');
(async () => { const [dir] = process.argv.slice(2); const b = await chromium.launch(); fs.mkdirSync(path.join(dir, 'img'), { recursive: true });
for (const f of fs.readdirSync(path.join(dir, 'html'))) { const html = fs.readFileSync(path.join(dir, 'html', f), 'utf8');
 const m = /width:(\\d+)px;height:(\\d+)px;background/.exec(html);
 const p = await b.newPage({ viewport: { width: +m[1], height: +m[2] }, deviceScaleFactor: f.startsWith('Back') ? 2 : 1 }); await p.goto('file://' + path.join(dir, 'html', f));
 await p.waitForTimeout(150); await p.screenshot({ path: path.join(dir, 'img', f.replace('.html', '.png')) }); await p.close(); }
await b.close(); })();''')
    subprocess.run(['node', js, os.path.abspath(out)], check=True, cwd=out)
    print('%d boards → %s/img' % (len(pages), out))


if __name__ == '__main__':
    main()
