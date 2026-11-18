#!/usr/bin/env python3
"""Poll boards for #505, round 3b: the real entry-sheet look (cards «Сумма / Категория / Детали», big keypad) on ONE step.

Based on the owner's phone screenshots (2026-11-18): the cards and the keypad stay; the account strip and the type switch
join the sheet, the cards get denser so everything fits one screen (sheet ≈ 770 dp of 844).
Usage: python3 scripts/agent/mockup_poll_accounts4.py <out-dir>  (<out-dir>/svg from round 1)  → <out-dir>/img/*.png
"""
import os
import subprocess
import sys

HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, HERE)
import mockup_bricks as B  # noqa: E402
import mockup_nav_poll as N  # noqa: E402
import mockup_poll_accounts as P  # noqa: E402
import mockup_poll_accounts2 as R  # noqa: E402
import mockup_poll_update as U  # noqa: E402

ACCS, MONO = R.ACCS, U.MONO
CATS = [('Дети', 'user'), ('Документы', 'file'), ('Продукты', 'cart'), ('Такси', 'taxi'), ('Еда вне дома', 'cart'), ('Налоги', 'file'), ('Дом', 'home')]


def edge(t):
    """Outline of a selected/filled thing: always a shade of the theme (owner 2026-11-18: no black or white borders)."""
    return U.mix(t['prim'], '#FFFFFF', .35) if t['dark'] else U.mix(t['prim'], '#000000', .5)


def card(t, label, body, right='', pad='10px 14px'):
    return ('<div style="margin:0 12px 8px;padding:%s;border-radius:24px;background:%s;border:1px solid %s"><div style="display:flex;justify-content:space-between;align-items:center;'
            'font-size:12px;letter-spacing:.04em;color:%s;min-height:18px">%s<span style="letter-spacing:0;font-size:13px;color:%s">%s</span></div>%s</div>' % (
                pad, t['surf'], t['line'], t['muted'], label, t['acc'] if t['dark'] else '#5B1FA8', right, body))


def seg(t, on=0, tpl=True):
    items = ''.join('<span style="flex:1;text-align:center;height:36px;line-height:36px;border-radius:999px;font-size:14px;%s">%s</span>' % (
        ('background:%s;color:%s;font-weight:700;box-shadow:inset 0 0 0 2px %s' % (t['prim'], t['onp'], edge(t))) if i == on else '', n) for i, n in enumerate(['Расход', 'Доход', 'Перевод']))
    btn = '<span style="width:44px;height:44px;flex:none;border-radius:50%%;background:%s;display:flex;align-items:center;justify-content:center;font-size:18px" title="Шаблоны">☰</span>' % t['bar'] if tpl else ''
    return '<div style="display:flex;gap:8px;align-items:center;margin:0 12px 8px"><div style="flex:1;padding:4px;border-radius:999px;background:%s;display:flex">%s</div>%s</div>' % (t['bar'], items, btn)


def pill(t, text, h=40):
    return '<span style="height:%dpx;padding:0 14px;border-radius:999px;display:flex;align-items:center;font-size:14px;white-space:nowrap;background:%s">%s</span>' % (h, t['bar'], text)


def strip(t, sel=0, margin='0 12px 8px'):
    items = ''.join('<span style="display:flex;border-radius:14px;%s">%s</span>' % (
        ('box-shadow:0 0 0 2px %s,0 0 0 4px %s;' % (t['surf' if margin == '0' else 'page'], edge(t))) if i == sel else '', P.badge(ACCS[a], 44, 'A')) for i, a in enumerate([4, 0, 1, 2, 3]))
    return '<div style="display:flex;gap:10px;margin:%s;align-items:center;padding:2px 4px">%s%s</div>' % (margin, items, pill(t, 'Все ›', 44))


def amount_card(t, inner_strip=False):
    field = ('<div style="margin-top:6px;height:52px;border-radius:16px;border:2px solid %s;background:%s;display:flex;align-items:center;justify-content:space-between;padding:0 8px 0 14px">'
             '<span style="%sfont-size:26px;color:%s">1 250</span><span style="padding:8px 14px;border-radius:999px;background:%s;font-size:14px">RUB</span></div>' % (edge(t), t['bar'], MONO, '#E0406A' if not t['dark'] else '#FF8FA8', t['surf']))
    body = (strip(t, 0, '6px 0 6px') if inner_strip else '') + field
    return card(t, 'СУММА', body, '+ Чаевые &nbsp;&nbsp; + Сумма покупки')


def cat_cell(t, nm, ic, w='25%'):
    return '<div style="display:flex;flex-direction:column;align-items:center;gap:2px;width:%s">%s<span style="font-size:11px;white-space:nowrap">%s</span></div>' % (w, B.dot_icon(ic, '#C27BFF' if t['dark'] else '#7A2BD6', 36), nm)


def cats(t, ribbon=False):
    if ribbon:
        body = '<div style="display:flex;align-items:flex-start;gap:6px;padding-top:4px">%s<span style="margin-left:auto;height:36px;padding:0 12px;border-radius:999px;display:flex;align-items:center;font-size:14px;white-space:nowrap;background:%s">Все 25 ›</span></div>' % (
            ''.join(cat_cell(t, nm, ic, '58px') for nm, ic in CATS[:4]), t['bar'])
        return card(t, 'КАТЕГОРИЯ · ЧАСТЫЕ', body)
    cells = ''.join(cat_cell(t, nm, ic) for nm, ic in CATS) + '<div style="display:flex;flex-direction:column;align-items:center;gap:2px;width:25%%"><span style="width:36px;height:36px;border-radius:50%%;border:1.5px dashed %s;display:flex;align-items:center;justify-content:center;font-size:13px">25</span><span style="font-size:11px">Все</span></div>' % t['muted']
    return card(t, 'КАТЕГОРИЯ · ЧАСТЫЕ', '<div style="display:flex;flex-wrap:wrap;gap:4px 0;padding-top:4px">%s</div>' % cells)


def details(t, tiles=False):
    if tiles:
        tile = lambda a, b: '<div style="flex:1;padding:6px 12px;border-radius:16px;background:%s"><div style="font-size:11px;color:%s">%s</div><div style="font-size:14px">%s</div></div>' % (t['bar'], t['muted'], a, b)  # noqa: E731
        return card(t, 'ДЕТАЛИ', '<div style="display:flex;gap:8px;margin-top:4px">%s%s</div><div style="display:flex;gap:8px;margin-top:6px">%s%s</div>' % (
            tile('Дата', 'Сегодня, 18 нояб…'), tile('Заметка', 'Без заметки'), tile('Теги', 'Без тегов'), tile('Возврат', '☐ вернули на счёт')))
    chips = ''.join('<span style="flex:1;height:40px;border-radius:999px;display:flex;align-items:center;justify-content:center;font-size:13px;white-space:nowrap;background:%s">%s</span>' % (t['bar'], x)
                    for x in ['Сегодня', 'Заметка', 'Теги', 'Возврат'])
    return '<div style="display:flex;gap:6px;margin:0 12px 8px">%s</div>' % chips


def keypad(t, key=50, layout='K0'):
    """K0 now (⌫ and + in the right column, tall «Готово»); K2 «−» under «+», «Готово» one row; K3 «+», «−», tall «Готово», ⌫ inside the amount field."""
    k = lambda x, extra='': '<span style="height:%dpx;border-radius:24px;background:%s;border:1px solid %s;display:flex;align-items:center;justify-content:center;font-size:22px;%s%s">%s</span>' % (key, t['bar'], t['line'], MONO, extra, x)  # noqa: E731
    done = lambda rows: ('<span style="grid-row:%s;grid-column:4;border-radius:24px;background:%s;color:%s;font-weight:700;display:flex;flex-direction:%s;gap:6px;align-items:center;justify-content:center;'
                         'font-size:16px;border:1.5px solid %s">✓<span>Готово</span></span>' % (rows, t['prim'], t['onp'], 'row' if rows == '4' else 'column', U.mix(t['prim'], '#000000', .5)))  # noqa: E731
    if layout == 'K2':
        keys = ''.join(k(x) for x in ['7', '8', '9', '⌫', '4', '5', '6', '+', '1', '2', '3', '−', ',', '0', '000']) + done('4')
    elif layout == 'K3':
        keys = ''.join(k(x) for x in ['7', '8', '9', '+', '4', '5', '6', '−', '1', '2', '3']) + done('3/5') + ''.join(k(x) for x in [',', '0', '000'])
    else:
        keys = ''.join(k(x) for x in ['7', '8', '9', '⌫', '4', '5', '6', '+', '1', '2', '3']) + done('3/5') + ''.join(k(x) for x in [',', '0', '000'])
    return '<div style="display:grid;grid-template-columns:repeat(4,1fr);gap:6px;padding:0 12px">%s</div>' % keys


def sheet(t, body, h=770):
    top = '<div style="height:20px;display:flex;justify-content:center;padding-top:8px"><span style="width:36px;height:4px;border-radius:2px;background:%s"></span></div>' % t['line']
    return U.phone(t, '<div style="position:absolute;inset:0;background:rgba(0,0,0,.35)"></div><div style="position:absolute;left:0;right:0;bottom:0;top:%dpx;background:%s;border-radius:28px 28px 0 0;padding-top:6px">%s%s</div>' % (
        844 - h, t['page'], top, body), 844)


def one(t, v):
    if v == 'A':  # cards stay; strip + type on top; details as one chip row
        return sheet(t, seg(t) + strip(t) + amount_card(t) + cats(t) + details(t) + keypad(t))
    if v == 'B':  # category ribbon; details tiles stay as they are now
        return sheet(t, seg(t) + strip(t) + amount_card(t) + cats(t, ribbon=True) + details(t, tiles=True) + keypad(t, 52))
    return sheet(t, seg(t) + amount_card(t, inner_strip=True) + cats(t) + details(t) + keypad(t, 54))  # C: the strip lives inside the «Сумма» card


def on_pill(t, text, grow=1):
    """A filled detail: the selection look (neon + ink border) with the value in place of the label."""
    return ('<span style="flex:%d 1 auto;height:40px;padding:0 10px;border-radius:999px;display:flex;align-items:center;justify-content:center;font-size:13px;font-weight:700;white-space:nowrap;overflow:hidden;'
            'text-overflow:ellipsis;background:%s;color:%s;box-shadow:inset 0 0 0 2px %s">%s</span>' % (grow, t['prim'], t['onp'], edge(t), text))


def filled(t, v):
    kp = v[2:] or 'K0'
    v = v[:2]
    """Everything entered: account + name line, «1 250 + 125» with total, tips on, category picked, details with values, enabled «Готово»."""
    line = '<div style="margin:-2px 16px 8px;display:flex;justify-content:space-between;font-size:13px;color:%s"><span>Сбер счёт RUB</span><span style="%s">остаток 40 000 RUB</span></div>' % (t['muted'], MONO)
    field = ('<div style="margin-top:6px;height:56px;border-radius:16px;border:2px solid %s;background:%s;display:flex;align-items:center;justify-content:space-between;padding:0 8px 0 14px">'
             '<span style="%sfont-size:26px;color:%s">1 375</span><span style="display:flex;align-items:center;gap:6px">__BS__<span style="padding:8px 14px;border-radius:999px;background:%s;font-size:14px">RUB</span></span></div>'
             '<div style="%sfont-size:12px;color:%s;padding:4px 4px 0">1 250 + 125 · чаевые 10 %%</div>' % (edge(t), t['bar'], MONO, '#E0406A' if not t['dark'] else '#FF8FA8', t['surf'], MONO, t['muted']))
    field = field.replace('__BS__', '<span style="width:40px;height:40px;border-radius:50%%;background:%s;display:flex;align-items:center;justify-content:center;font-size:18px">⌫</span>' % t['surf'] if kp == 'K3' else '')
    amount = card(t, 'СУММА', field, '<span style="font-weight:700">✓ Чаевые 10 %</span> &nbsp;&nbsp; + Сумма покупки')
    cells = ''
    for i, (nm, ic) in enumerate(CATS):
        on = nm == 'Продукты'
        ring = 'box-shadow:0 0 0 2px %s,0 0 0 4px %s;border-radius:50%%;' % (t['surf'], edge(t)) if on else ''
        cells += ('<div style="display:flex;flex-direction:column;align-items:center;gap:2px;width:25%%;opacity:%s"><span style="display:flex;%s">%s</span><span style="font-size:11px;font-weight:%s;white-space:nowrap">%s</span></div>'
                  % (1 if on else .55, ring, B.dot_icon(ic, '#C27BFF' if t['dark'] else '#7A2BD6', 36), 700 if on else 400, nm))
    cells += '<div style="display:flex;flex-direction:column;align-items:center;gap:2px;width:25%%;opacity:.55"><span style="width:36px;height:36px;border-radius:50%%;border:1.5px dashed %s;display:flex;align-items:center;justify-content:center;font-size:13px">25</span><span style="font-size:11px">Все</span></div>' % t['muted']
    cat = card(t, 'КАТЕГОРИЯ · ЧАСТЫЕ', '<div style="display:flex;flex-wrap:wrap;gap:4px 0;padding-top:4px">%s</div>' % cells, 'Продукты')
    if v == 'F1':
        det = '<div style="display:flex;gap:6px;margin:0 12px 8px">%s%s%s%s</div>' % (on_pill(t, '◷ 17 нояб.', 1), on_pill(t, '✎ Кофе с Настей', 2.8), on_pill(t, '# Отпуск +1', 1.4), on_pill(t, '↩ Возврат', 1.2))
    else:
        tile = lambda a, b: '<div style="flex:1;padding:6px 12px;border-radius:16px;background:%s;box-shadow:inset 0 0 0 2px %s"><div style="font-size:11px;color:%s">%s</div><div style="font-size:14px;font-weight:700;white-space:nowrap;overflow:hidden;text-overflow:ellipsis">%s</div></div>' % (t['bar'], edge(t), t['muted'], a, b)  # noqa: E731
        det = '<div style="margin:0 12px 8px;display:flex;flex-direction:column;gap:6px"><div style="display:flex;gap:6px">%s%s</div><div style="display:flex;gap:6px">%s%s</div></div>' % (
            tile('Дата', '17 нояб., вчера'), tile('Заметка', 'Кофе с Настей'), tile('Теги', 'Отпуск, Дети'), tile('Возврат', '☑ вернули на счёт'))
    return sheet(t, seg(t) + strip(t) + line + amount + cat + det + keypad(t, 46 if v == 'F2' else 50, kp))


def transfer(t, v):
    acc = lambda lab, i: ('<div style="display:flex;align-items:center;gap:10px;margin-top:4px;padding:6px 10px;border-radius:16px;background:%s">%s<span style="flex:1"><div style="font-size:11px;color:%s">%s</div>'
                          '<div style="font-size:15px">%s</div></span><span style="%sfont-size:13px;color:%s">%s %s</span></div>' % (t['bar'], P.badge(ACCS[i], 40, 'A'), t['muted'], lab, ACCS[i][0], MONO, t['muted'], R.BAL[i], R.CUR[i]))  # noqa: E731
    sums = ('<div style="margin-top:6px;height:52px;border-radius:16px;border:2px solid %s;background:%s;display:flex;align-items:center;justify-content:space-between;padding:0 14px"><span style="%sfont-size:26px">100</span>'
            '<span style="%sfont-size:13px;color:%s">USD → 118 000 ARS · курс 1 180</span></div>' % (edge(t), t['bar'], MONO, MONO, t['muted']))
    return sheet(t, seg(t, 2) + card(t, 'ПЕРЕВОД', acc('Откуда', 5) + acc('Куда', 0)) + card(t, 'СУММА', sums) + details(t) + keypad(t))


def main():
    out = sys.argv[1]
    P.SVG_DIR[0] = os.path.join(out, 'svg')
    os.makedirs(os.path.join(out, 'html'), exist_ok=True)
    for name, fn, v in [('One' + v, one, v) for v in 'ABC'] + [('Trf', transfer, 'X'), ('Fil2', filled, 'F2'), ('KpB', filled, 'F2K2'), ('KpC', filled, 'F2K3')]:
        for suffix, dark in (('', True), ('@light', False)):
            t = U.theme(dark)
            open(os.path.join(out, 'html', name + suffix + '.html'), 'w', encoding='utf-8').write(N.page(fn(t, v), 390, 844, dict(page=t['page'])))
    js = os.path.join(out, 'render.js')
    src = open(os.path.join(HERE, 'mockup_poll_update.py'), encoding='utf-8').read()
    open(js, 'w').write(src[src.index("'''const") + 3:src.index("''')")].replace('\\\\d', '\\d'))
    subprocess.run(['node', js, os.path.abspath(out)], check=True, cwd=out)
    print('boards → %s/img' % out)


if __name__ == '__main__':
    main()
