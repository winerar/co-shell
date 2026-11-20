#!/usr/bin/env python3
"""Poll boards for #568: paging periods forward in «Отчёты» (columns window, pinned arrows, swipe).

Standalone PNGs like mockup_poll_receipts561.py: `RpXxx.png` dark + `RpXxx@light.png`.
Usage: python3 scripts/agent/mockup_poll_reports568.py <out-dir>  → <out-dir>/img/*.png   then   poll-page.py <spec> <poll-dir> --img <out-dir>/img
"""
import os
import subprocess
import sys

HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, HERE)
import mockup_bricks as B  # noqa: E402
import mockup_nav_poll as N  # noqa: E402
import mockup_poll_accounts4 as A  # noqa: E402
import mockup_poll_update as U  # noqa: E402

MONO = U.MONO
MONTHS = ['янв', 'фев', 'мар', 'апр', 'май', 'июн', 'июл', 'авг', 'сен', 'окт', 'ноя']
SPENT = [9800, 11200, 8400, 10100, 12600, 7156, 13730, 25873, 6202, 11056, 2519]
CHEV_L, CHEV_R = 'M15 6l-6 6 6 6', 'M9 6l6 6-6 6'
H = 760


def ink2(t):
    return t['acc'] if t['dark'] else '#5B1FA8'


def topbar(t, sub=None):
    inner = '<b style="font-size:22px;padding-left:16px">Отчёты</b>'
    if sub:
        inner = ('<span style="width:48px;display:flex;justify-content:center">%s</span><div style="flex:1;text-align:center;line-height:1.15">'
                 '<b style="font-size:17px">Отчёты</b><div style="font-size:13px;color:%s">%s</div></div><span style="width:48px;display:flex;justify-content:center">%s</span>' % (
                     N.glyph(CHEV_L, t['ink'], 22), t['muted'], sub, N.glyph(CHEV_R, t['ink'], 22)))
    return ('<div style="position:absolute;top:8px;left:32px;right:32px;height:56px;border-radius:999px;background:%s;display:flex;align-items:center;'
            'padding:0 4px;z-index:3">%s</div>' % (t['bar'], inner))


def period_row(t, title, pinned=False):
    st = ('position:absolute;top:72px;left:16px;right:16px;z-index:3;border-radius:999px;background:%s;box-shadow:0 0 0 1px %s' % (t['bar'], t['line'])) if pinned else 'margin:0 16px'
    btn = lambda p: '<span style="width:48px;height:48px;display:flex;align-items:center;justify-content:center">%s</span>' % N.glyph(p, t['ink'], 22)  # noqa: E731
    return ('<div style="%s;height:52px;display:flex;align-items:center">%s<b style="flex:1;text-align:center;font-size:16px;font-weight:600">%s</b>%s'
            '<span style="height:36px;padding:0 12px;margin-right:6px;border-radius:999px;background:%s;display:flex;align-items:center;font-size:14px">USD ▾</span></div>' % (
                st, btn(CHEV_L), title, btn(CHEV_R), t['surf'] if pinned else t['bar']))


def card(t, head, right, body, extra=''):
    return ('<div style="margin:12px 16px 0;padding:14px 16px;border-radius:24px;background:%s;border:1px solid %s;%s"><div style="display:flex;justify-content:space-between;'
            'font-size:16px"><span>%s</span><span style="font-size:14px;color:%s">%s</span></div>%s</div>' % (t['surf'], t['line'], extra, head, t['muted'], right, body))


def columns(t, first, sel, now=10):
    """«По месяцам»: 6 columns from month index [first], [sel] highlighted, months after [now] — empty future."""
    top = max(SPENT[first:first + 6])
    cols = ''
    for i in range(first, first + 6):
        future = i > now
        v = 0 if future else SPENT[i]
        h = max(10, round(v / top * 120)) if not future else 0
        on = i == sel
        fill = ('background:%s;box-shadow:inset 0 0 0 1.5px %s' % (t['prim'], A.edge(t))) if on else 'background:%s' % t['wash']
        num = '' if future else '{:,}'.format(v).replace(',', ' ')
        cols += ('<div style="flex:1;display:flex;flex-direction:column;align-items:center;justify-content:flex-end;gap:6px">'
                 '<span style="%sfont-size:11px;color:%s">%s</span><span style="width:70%%;height:%dpx;border-radius:10px;%s"></span>'
                 '<span style="font-size:14px;%s">%s</span></div>' % (MONO, t['ink'] if on else t['muted'], num, h, fill,
                                                                      'font-weight:700' if on else 'color:%s' % t['muted'], MONTHS[i] if i < len(MONTHS) else 'дек'))
    return '<div style="display:flex;height:180px;margin-top:12px">%s</div>' % cols


def spending(t, title):
    rows = ''.join('<div style="display:flex;justify-content:space-between;font-size:14px;margin-top:8px"><span>%s</span><span style="%s">%s</span></div>'
                   '<div style="height:8px;border-radius:999px;background:%s;margin-top:4px"><div style="width:%d%%;height:8px;border-radius:999px;background:%s"></div></div>' % (
                       n, MONO, a, t['wash'], w, t['prim']) for n, a, w in [('Продукты', '2 410', 82), ('Дом', '1 380', 48), ('Кафе', '760', 27)])
    return card(t, 'На что ушли деньги', title, rows)


def nav(t):
    cells = ''
    for k in ['home', 'list', '+', 'chart', 'more']:
        if k == '+':
            cells += '<span style="width:48px;height:48px;border-radius:50%%;background:%s;display:flex;align-items:center;justify-content:center">%s</span>' % (t['prim'], B.svg('plus', t['onp'], 26, 2.2))
        elif k == 'chart':
            cells += '<span style="height:44px;width:64px;border-radius:999px;background:%s;display:flex;align-items:center;justify-content:center">%s</span>' % (t['prim'], B.svg(B.ICON[k], t['onp'], 24, 1.7))
        else:
            cells += '<span style="display:flex;justify-content:center">%s</span>' % B.svg(B.ICON[k], t['muted'], 24, 1.7)
    return ('<div style="position:absolute;left:32px;right:32px;bottom:16px;height:56px;border-radius:999px;background:%s;display:grid;'
            'grid-template-columns:repeat(5,1fr);align-items:center;justify-items:center;z-index:3">%s</div>' % (t['bar'], cells))


def caption(t, text, top):
    return ('<div style="position:absolute;left:16px;right:16px;top:%dpx;z-index:4;padding:8px 12px;border-radius:16px;background:%s;color:%s;font-size:13px;'
            'line-height:1.35;box-shadow:inset 0 0 0 1px %s">%s</div>' % (top, t['acc'] if t['dark'] else '#5B1FA8', '#FFFFFF' if not t['dark'] else '#1A0830', ink2(t), text))


def screen(t, body, extra=''):
    return U.phone(t, topbar(t) + '<div style="padding-top:72px">%s</div>' % body + extra + nav(t), H)


def months_screen(t, first, sel, note):
    body = period_row(t, '%s 2026' % ['Январь', 'Февраль', 'Март', 'Апрель', 'Май', 'Июнь', 'Июль', 'Август', 'Сентябрь', 'Октябрь', 'Ноябрь'][sel])
    body += card(t, 'По месяцам', 'расходы', columns(t, first, sel))
    body += spending(t, 'июнь')
    return screen(t, body, caption(t, note, 600))


def col_now(t, v):
    return months_screen(t, 0, 5, 'Сейчас: выбрали июнь — столбики кончаются июнем, июль–ноябрь пропали. Вперёд — только стрелкой.')


def col_a(t, v):
    return months_screen(t, 5, 5, 'А: окно стоит до текущего месяца, июнь выделен. Июль–ноябрь видны — нажмите, чтобы перейти вперёд.')


def col_b(t, v):
    return months_screen(t, 3, 5, 'Б: выбранный месяц в середине: два месяца до и три после. У текущего месяца окно прижато к нему.')


def scrolled(t):
    """Body scrolled past the period row: the bottom of «По месяцам» and «Доходы и расходы»."""
    inc = ''.join('<div style="flex:1;display:flex;flex-direction:column;align-items:center;justify-content:flex-end;gap:6px"><div style="display:flex;gap:4px;align-items:flex-end">'
                  '<span style="width:16px;height:%dpx;border-radius:6px;background:%s"></span><span style="width:16px;height:%dpx;border-radius:6px;background:%s"></span></div>'
                  '<span style="font-size:14px;color:%s">%s</span></div>' % (a, U.mix(t['page'], '#3BA6B8', .7), b, U.mix(t['page'], '#E0566E', .7), t['muted'], m)
                  for a, b, m in [(70, 46, 'июн'), (40, 90, 'июл'), (70, 140, 'авг'), (170, 40, 'сен'), (14, 70, 'окт'), (20, 16, 'ноя')])
    return (card(t, 'По месяцам', 'расходы', columns(t, 5, 5), 'margin-top:-80px') +
            card(t, 'Доходы и расходы', 'нажмите на месяц', '<div style="display:flex;height:200px;margin-top:12px">%s</div>' % inc))


def stick_a(t, v):
    body = '<div style="padding-top:72px;height:%dpx;overflow:hidden">%s</div>' % (H, scrolled(t))
    return U.phone(t, topbar(t) + body + period_row(t, 'Июнь 2026', True) + caption(t, 'А: строка периода прилипает под шапкой — стрелки всегда под рукой.', 610) + nav(t), H)


def stick_b(t, v):
    body = '<div style="padding-top:60px;height:%dpx;overflow:hidden">%s</div>' % (H, scrolled(t))
    return U.phone(t, topbar(t, 'Июнь 2026 · USD') + body + caption(t, 'Б: при прокрутке период и стрелки переезжают в шапку, отдельной строки нет.', 610) + nav(t), H)


def arrows(t, top, h, label):
    return ('<div style="position:absolute;left:8px;right:8px;top:%dpx;height:%dpx;border-radius:24px;border:2px dashed %s;z-index:2;display:flex;align-items:flex-end;'
            'justify-content:center;padding-bottom:6px"><span style="font-size:12px;padding:2px 10px;border-radius:999px;background:%s;color:%s">%s</span></div>' % (
                top, h, ink2(t), t['page'], ink2(t), label))


def swipe_screen(t, zone, note):
    body = period_row(t, 'Июнь 2026') + spending(t, 'июнь') + card(t, 'По месяцам', 'расходы', columns(t, 5, 5))
    return screen(t, body, zone + caption(t, note, 610))


def swipe_a(t, v):
    return swipe_screen(t, arrows(t, 68, 250, '← свайп: месяц · ниже — вкладки'),
                        'А: месяц листается свайпом по строке периода и карточке «На что ушли деньги». Ниже свайп, как везде, меняет вкладку.')


def swipe_b(t, v):
    return swipe_screen(t, arrows(t, 68, 560, '← свайп: месяц везде'),
                        'Б: на «Отчётах» свайп всюду листает месяц. Вкладку здесь меняют только кнопками панели.')


BOARDS = [('RpColNow', col_now), ('RpColA', col_a), ('RpColB', col_b), ('RpStickA', stick_a), ('RpStickB', stick_b), ('RpSwipeA', swipe_a), ('RpSwipeB', swipe_b)]


def main():
    out = sys.argv[1]
    os.makedirs(os.path.join(out, 'html'), exist_ok=True)
    for name, fn in BOARDS:
        for suffix, dark in (('', True), ('@light', False)):
            t = U.theme(dark)
            open(os.path.join(out, 'html', name + suffix + '.html'), 'w', encoding='utf-8').write(N.page(fn(t, name), 390, H, dict(page=t['page'])))
    js = os.path.join(out, 'render.js')
    src = open(os.path.join(HERE, 'mockup_poll_update.py'), encoding='utf-8').read()
    open(js, 'w').write(src[src.index("'''const") + 3:src.index("''')")].replace('\\\\d', '\\d'))
    subprocess.run(['node', js, os.path.abspath(out)], check=True, cwd=out)
    print('boards → %s/img' % out)


if __name__ == '__main__':
    main()
