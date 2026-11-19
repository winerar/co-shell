#!/usr/bin/env python3
"""Poll boards for #513, round 2: where the word hints live on sheets (`entry_minimal`, `keypad_math`, `entry_refund`,
`currency_receipt`, `tags`) — the entry sheet has no room for one more line («без прокрутки», #290).

Usage: python3 scripts/agent/mockup_poll_hints_entry.py <out-dir>  → <out-dir>/img/*.png (dark + «@light»), for `poll-page.py --img`.
Builds on the #505 sheet pieces of `mockup_poll_accounts4.py`; account badges are letters only (no network, no logos).
"""
import os
import subprocess
import sys

HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, HERE)
import mockup_nav_poll as N  # noqa: E402
import mockup_poll_accounts as P  # noqa: E402
import mockup_poll_accounts4 as E  # noqa: E402
import mockup_poll_update as U  # noqa: E402

_badge = P.badge
P.badge = lambda acc, size=40, style='A', t=None: _badge(acc, size, 'L', t)  # letters: the logos need files from an earlier round

MONO = E.MONO
KEY_H = {'A': 46, 'B': 50, 'C': 50}


def hint_line(t, text, close=True, pad='0 12px 8px'):
    """CoShellHint (П1): a calm line on the soft block, «✕» = shown. Same look as on the «Счета» board."""
    x = ('<span style="width:40px;height:40px;flex:none;display:flex;align-items:center;justify-content:center;color:%s;font-size:18px">✕</span>' % t['muted']) if close else ''
    return ('<div style="margin:%s;padding:2px 0 2px 14px;border-radius:16px;background:%s;display:flex;align-items:center;gap:4px;font-size:13px;line-height:1.35;min-height:44px">'
            '<span style="flex:1">%s</span>%s</div>' % (pad, t['bar'], text, x))


def coach(t, text):
    """CoShellCoachCard (П2) in the dimmed strip above the sheet: text + «Понятно» in one row, covers nothing, non-modal."""
    return ('<div style="position:absolute;left:16px;right:16px;top:8px;height:60px;padding:0 8px 0 16px;border-radius:24px;background:%s;border:1px solid %s;display:flex;align-items:center;gap:8px;color:%s">'
            '<span style="flex:1;font-size:13px;line-height:1.3">%s</span><span style="color:%s;font-size:14px;font-weight:700;padding:0 8px">Понятно</span></div>'
            % (t['surf'], t['line'], t['ink'], text, t['acc'] if t['dark'] else '#5B1FA8'))


def amount_with_caption(t):
    """В: the explanation is the caption under the amount field itself; goes away with the first «+»."""
    field = ('<div style="margin-top:6px;height:52px;border-radius:16px;border:2px solid %s;background:%s;display:flex;align-items:center;justify-content:space-between;padding:0 8px 0 14px">'
             '<span style="%sfont-size:26px;color:%s">0</span><span style="padding:8px 14px;border-radius:999px;background:%s;font-size:14px">ARS</span></div>'
             '<div style="font-size:12px;color:%s;padding:4px 4px 0">Можно складывать и вычитать: 300 + 150</div>' % (E.edge(t), t['bar'], MONO, '#E0406A' if not t['dark'] else '#FF8FA8', t['surf'], t['muted']))
    return E.card(t, 'СУММА', field, '+ Чаевые &nbsp;&nbsp; + Сумма покупки')


def entry(t, v):
    seg, strip, cats, det, kp = E.seg(t), E.strip(t), E.cats(t), E.details(t), E.keypad(t, KEY_H[v])
    amount = E.amount_card(t)
    if v == 'A':
        return E.sheet(t, seg + hint_line(t, 'Нужны только сумма и счёт. Остальное можно пропустить.') + strip + amount + cats + det + kp)
    if v == 'B':
        return E.sheet(t, seg + strip + amount + cats + det + kp) + coach(t, 'Нужны только сумма и счёт. Остальное можно пропустить.')
    return E.sheet(t, seg + strip + amount_with_caption(t) + cats + det + kp)


def sheet_rows(t, rows):
    out = ''.join('<div style="display:flex;align-items:center;gap:12px;min-height:52px;padding:0 16px;font-size:15px">%s<span style="flex:1">%s</span>%s</div>' % (
        '<span style="width:12px;height:12px;border-radius:50%%;background:%s"></span>' % c, n, '<span style="color:%s;font-size:13px">%s</span>' % (t['muted'], r)) for n, c, r in rows)
    return '<div style="margin:0 12px;border-radius:16px;border:1px solid %s;background:%s;overflow:hidden">%s</div>' % (t['line'], t['bar'], out)


def sheet_title(t, title, sub):
    return ('<div style="padding:10px 20px 8px;display:flex;align-items:center"><div style="flex:1"><div style="font-size:20px;font-weight:700">%s</div>'
            '<div style="font-size:13px;color:%s;margin-top:2px">%s</div></div><span style="width:48px;height:48px;display:flex;align-items:center;justify-content:center;font-size:20px">✕</span></div>'
            % (title, t['muted'], sub))


def tags_sheet(t, hint):
    rows = sheet_rows(t, [('Отпуск', '#FF8FA8', '12'), ('Подарки', '#FFE14D', '7'), ('Дети', '#7DE3FF', '5'), ('Работа', '#C27BFF', '3')])
    body = sheet_title(t, 'Теги', 'Выберите метки для операции') + (hint_line(t, 'Тег — цветная метка операции, например «отпуск» или «подарки».') if hint else '') + rows
    return E.sheet(t, body, h=520)


def currency_sheet(t, hint):
    rows = sheet_rows(t, [('ARS', '#7DE3FF', 'песо'), ('USD', '#B8FF2E', 'доллар'), ('EUR', '#C27BFF', 'евро'), ('RUB', '#FF8FA8', 'рубль')])
    body = sheet_title(t, 'Валюта покупки', 'Если списали в другой валюте') + (hint_line(t, 'Укажите сумму покупки, и курс посчитается сам.') if hint else '') + rows
    return E.sheet(t, body, h=520)


BOARDS = [('HintEntA', lambda t: entry(t, 'A')), ('HintEntB', lambda t: entry(t, 'B')), ('HintEntC', lambda t: entry(t, 'C')),
          ('HintTagsWith', lambda t: tags_sheet(t, True)), ('HintTagsWithout', lambda t: tags_sheet(t, False)),
          ('HintCurWith', lambda t: currency_sheet(t, True)), ('HintCurWithout', lambda t: currency_sheet(t, False))]


def main():
    out = sys.argv[1]
    os.makedirs(os.path.join(out, 'html'), exist_ok=True)
    for name, fn in BOARDS:
        for suffix, dark in (('', True), ('@light', False)):
            t = U.theme(dark)
            open(os.path.join(out, 'html', name + suffix + '.html'), 'w', encoding='utf-8').write(N.page(fn(t), 390, 844, dict(page=t['page'])))
    js = os.path.join(out, 'render.js')
    src = open(os.path.join(HERE, 'mockup_poll_update.py'), encoding='utf-8').read()
    open(js, 'w').write(src[src.index("'''const") + 3:src.index("''')")].replace('\\\\d', '\\d'))
    subprocess.run(['node', js, os.path.abspath(out)], check=True, cwd=out)
    print('boards → %s/img' % out)


if __name__ == '__main__':
    main()
