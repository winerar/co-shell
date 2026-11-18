#!/usr/bin/env python3
"""Poll boards for four design feedbacks before 1.0: #441 panels, #442 second accent, #456 one meter, #443 icon-button size.

Usage: python3 scripts/agent/mockup_poll_1_0b.py <out-dir>  → <out-dir>/img/*.png (dark and «@light»).
Then: agent.sh poll docs/design/poll-feedback-1-0.json --img <out-dir>/img
Reuses the themes and the screen drawing of mockup_nav_poll.py.
"""
import os
import subprocess
import sys

HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, HERE)
import mockup_nav_poll as N  # noqa: E402
import mockup_bricks as B  # noqa: E402

# Second accent (violet in «Лайм»): dark / light
SECOND = {'dark': '#B266FF', 'light': '#7A2BD6'}
MONO = "font-family:'JetBrains Mono',monospace;"


def top_flat(t, title, actions=''):
    return ('<div style="display:flex;align-items:center;height:64px;padding:0 8px 0 16px"><h1 style="margin:0;font-size:28px;flex:1;color:%s">%s</h1>%s</div>'
            % (t['ink'], title, actions))


def icons(t, names, size=24, target=48):
    return ''.join('<span style="width:%dpx;height:%dpx;display:flex;align-items:center;justify-content:center">%s</span>'
                   % (target, target, N.glyph(B.ICON[n], t['ink'], size)) for n in names)


def nav_bar(t, h, inset, plus, icon):
    cells = ''
    for i, k in enumerate(['home', 'list', '+', 'chart', 'more']):
        if k == '+':
            cells += '<span style="width:%dpx;height:%dpx;border-radius:50%%;background:%s;display:flex;align-items:center;justify-content:center">%s</span>' % (plus, plus, t['lime'], B.svg('plus', t['onlime'], 24, 2.2))
        else:
            on = i == 1
            pill = ('<span style="position:absolute;width:%dpx;height:%dpx;border-radius:999px;background:%s"></span>' % (h + 4, h - 16, t['lime'])) if on else ''
            cells += '<span style="position:relative;display:flex;justify-content:center;align-items:center">%s<span style="position:relative">%s</span></span>' % (pill, B.svg(B.ICON[k], t['onlime'] if on else t['muted'], icon, 1.7))
    return ('<div style="position:absolute;left:%dpx;right:%dpx;bottom:16px;height:%dpx;border-radius:999px;background:%s;display:grid;grid-template-columns:repeat(5,1fr);align-items:center;padding:0 4px">%s</div>'
            % (inset, inset, h, t['bar'], cells))


def rows(t):
    return ''.join(N.row(*r[:3], t, r[3]) for r in [('Продукты', 'Вчера', '−3 200', '#8BC34A'), ('Кафе', '12 ноя', '−1 450', '#FF9800'),
                                                   ('Зарплата', '10 ноя', '+120 000', '#00D1FF'), ('Такси', '9 ноя', '−900', '#FFEB3B'), ('Аптека', '8 ноя', '−2 300', '#E91E63')])


def phone(t, body, h=640):
    return '<div style="width:390px;height:%dpx;position:relative;overflow:hidden;background:%s;border-radius:24px">%s</div>' % (h, t['bg'], body)


def panels(kind, t):
    hero = '<div style="margin:4px 16px 8px;border-radius:24px;padding:16px;height:84px;background:linear-gradient(135deg,#314617,#0E2523,#271E36);color:#EEF5E6;font-size:12px">Ноябрь</div>'
    body = '<div style="padding:0 20px">%s</div>' % rows(t)
    if kind == 'B':  # both bars: floating, narrow capsules
        head = ('<div style="margin:10px 28px 0;height:52px;border-radius:999px;background:%s;display:flex;align-items:center;padding:0 8px 0 20px"><b style="flex:1;font-size:18px;color:%s">Операции</b>%s</div>'
                % (t['bar'], t['ink'], icons(t, ['search'], 22, 40)))
        return phone(t, head + '<div style="height:8px"></div>' + hero + body + nav_bar(t, 52, 40, 44, 22))
    if kind == 'C':  # bottom narrow and floating, top stays flat
        return phone(t, top_flat(t, 'Операции', icons(t, ['search'])) + hero + body + nav_bar(t, 52, 40, 44, 22))
    return phone(t, top_flat(t, 'Операции', icons(t, ['search'])) + hero + body + nav_bar(t, 64, 16, 48, 24))


def chip(text, color, fg, bg=None):
    return '<span style="height:32px;padding:0 14px;border-radius:999px;display:inline-flex;align-items:center;font-size:13px;color:%s;background:%s">%s</span>' % (fg, bg or 'transparent', text)


def accent(kind, t, mode):
    s = SECOND[mode]
    link = t['lime'] if kind == 'A' else s
    badge = t['lime'] if kind in 'AB' else s
    chips = (chip('Расходы ✕', 0, t['onlime'], t['lime']) + chip('Категория ▾', 0, t['ink2'], t['chip']) + chip('Ноябрь ▾', 0, t['ink2'], t['chip']))
    if kind == 'C':
        chips = (chip('Расходы ✕', 0, t['onlime'], t['lime']) + chip('Категория ▾', 0, s, t['chip']) + chip('Ноябрь ▾', 0, s, t['chip']))
    seg = lambda c: '<span style="display:inline-block;width:10px;height:10px;border-radius:50%%;background:%s;margin-right:6px"></span>' % c  # noqa: E731
    card = ('<div style="margin:12px 16px;border-radius:24px;background:%s;padding:16px"><div style="display:flex;justify-content:space-between;font-size:13px;color:%s"><b style="color:%s">На что ушли деньги</b><span style="color:%s">Все категории · 8</span></div>'
            '<div style="font-size:12px;color:%s;margin-top:10px">%s%s</div><div style="margin-top:12px;display:flex;gap:8px;align-items:center;font-size:13px;color:%s">%sДоход %s Расход</div></div>'
            % (t['card'], t['muted'], t['ink'], link, t['muted'], seg(t['lime']) + 'Дом ', seg(s if kind != 'A' else '#00BCD4') + 'Продукты', t['ink2'], seg(s if kind == 'C' else t['lime']), seg(t['lime'])))
    note = '<div style="margin:0 16px;font-size:13px;color:%s">%s</div>' % (link if kind == 'C' else t['muted'], 'в прошлом месяце к 11-му было 820')
    tags = ('<div style="margin:12px 16px;display:flex;gap:8px">%s</div>' % ''.join(
        '<span style="height:26px;padding:0 10px;border-radius:999px;display:inline-flex;align-items:center;font-size:12px;color:%s;background:%s">%s</span>' % (badge, t['chip'], n) for n in ['#отпуск', '#дети', '#машина']))
    return phone(t, top_flat(t, 'Отчёты') + '<div style="padding:0 16px;display:flex;gap:8px">%s</div>' % chips + card + note + tags, 420)


def bar(t, kind, frac, color, mark=None, h=8):
    track = t['chip'] if kind != 'A' else 'rgba(128,128,128,.25)'
    m = '<span style="position:absolute;left:%d%%;top:-3px;width:2px;height:%dpx;background:%s"></span>' % (mark, h + 6, t['ink']) if mark else ''
    return ('<span style="position:relative;display:block;height:%dpx;border-radius:999px;background:%s"><span style="display:block;width:%d%%;height:100%%;border-radius:999px;background:%s"></span>%s</span>'
            % (h, track, frac, color, m))


def meter(kind, t):
    """A = today (three looks); B = one meter: track + fill + «норма» mark; C = one thicker meter with the value inside the track."""
    lime, warn, pink = t['lime'], '#FFB547', '#FF5C7F'
    def line(title, val, frac, col, mark):
        if kind == 'A':
            h = {'Осталось': 6, 'Кафе': 8, 'Еда': 12}.get(title, 8)
            b = bar(t, 'A', frac, col, mark if title != 'Еда' else None, h)
        elif kind == 'B':
            b = bar(t, 'B', frac, col, mark, 8)
        else:
            b = bar(t, 'B', frac, col, None, 16)
            b = b[:-len('</span>')] + '<span style="position:absolute;left:10px;top:0;line-height:16px;font-size:10px;color:%s">%s</span></span>' % (t['onlime'], val)
        return ('<div style="padding:10px 0"><div style="display:flex;justify-content:space-between;font-size:14px;color:%s"><span>%s</span><span style="%s;color:%s;font-size:13px">%s</span></div><div style="margin-top:8px">%s</div></div>'
                % (t['ink2'], title, MONO, t['muted'], val, b))
    hero = ('<div style="margin:8px 16px;border-radius:24px;padding:16px;background:linear-gradient(135deg,#314617,#0E2523,#271E36);color:#EEF5E6"><div style="font-size:12px;opacity:.7">Осталось</div>'
            '<div style="%s;font-size:26px;color:#B8FF2E">540 USD</div><div style="margin-top:12px">%s</div></div>' % (MONO, bar(t, kind if kind != 'A' else 'A', 53, warn, 50, 6 if kind == 'A' else 8)))
    body = ('<div style="margin:0 16px;border-radius:24px;background:%s;padding:4px 16px">%s%s%s</div>'
            % (t['card'], line('Кафе', '95 000 / 80 000', 100, pink, 60), line('Продукты', '130 000 / 350 000', 37, lime, 40), line('Еда', '24 600 · 38 %', 38, '#8BC34A', 0)))
    return phone(t, top_flat(t, 'Бюджеты') + hero + body, 470)


def buttons(kind, t):
    size, target = {'A': (24, 48), 'B': (20, 48), 'C': (20, 40)}[kind]
    bar_ = ('<div style="display:flex;align-items:center;height:64px;padding:0 4px">%s<h1 style="margin:0;font-size:22px;flex:1;color:%s;padding-left:4px">Счёт Galicia</h1>%s</div>'
            % (icons(t, ['back'], size, target), t['ink'], icons(t, ['search', 'more'], size, target)))
    hit = ('<div style="margin:16px;font-size:12px;color:%s">Зона нажатия %d dp%s</div>' % (t['muted'], target, '' if target >= 48 else ' — меньше 48 dp, нарушает правило доступности'))
    return phone(t, bar_ + hit + '<div style="padding:0 20px">%s</div>' % rows(t)[:2000], 330)


def main():
    out = sys.argv[1]
    os.makedirs(os.path.join(out, 'html'), exist_ok=True)
    pages = {}
    for k in 'ABC':
        for suffix, t, mode in (('', N.DARK, 'dark'), ('@light', N.LIGHT, 'light')):
            for name, fn, h in (('Pan', panels, 640), ('Acc', None, 420), ('Met', meter, 470), ('Btn', buttons, 330)):
                body = accent(k, t, mode) if fn is None else fn(k, t)
                pages['%s%s%s' % (name, k, suffix)] = N.page(body, 390, h, t)
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
