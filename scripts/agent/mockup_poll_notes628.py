#!/usr/bin/env python3
"""Poll boards for #628 (delete drafts), #643 (AI marked «бета») and #641 (quick notes on «Обзор»).

Standalone PNGs like mockup_poll_reports568.py: `NbXxx.png` dark + `NbXxx@light.png`.
Usage: python3 scripts/agent/mockup_poll_notes628.py <out-dir>  → <out-dir>/img/*.png   then   poll-page.py <spec> <poll-dir> --img <out-dir>/img
"""
import os
import subprocess
import sys

HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, HERE)
import mockup_bricks as B  # noqa: E402
import mockup_nav_poll as N  # noqa: E402
import mockup_poll_accounts4 as A  # noqa: E402
import mockup_poll_reports568 as R  # noqa: E402
import mockup_poll_update as U  # noqa: E402

MONO = U.MONO
H = 760
X = 'M7 7l10 10M17 7L7 17'
TRASH = 'M5 7h14M10 7V5h4v2M7 7l1 12h8l1-12M10 11v5M14 11v5'
NOTE = 'M6 4h9l3 3v13H6zM9 10h6M9 14h6M9 18h3'
SPARK = 'M12 3l1.8 5.2L19 10l-5.2 1.8L12 17l-1.8-5.2L5 10l5.2-1.8z'
CHEV = 'M9 6l6 6-6 6'
DRAFTS = [('кофе', 'Galicia · сегодня', '−3 500', 'Кафе · по прошлым операциям', None),
          ('тест заметки', 'Galicia · сегодня', '', None, 'Нужна сумма'),
          ('Carrefour', 'Galicia · вчера', '−12 222', 'Продукты · по названию', None),
          ('позвонить маме', 'Galicia · сегодня', '', None, 'Нужна сумма'),
          ('такси', 'Наличные · сегодня', '−2 000', 'Такси · по названию', None)]


def topbar(t, title, lead=N.ARROW, fill=None):
    return ('<div style="position:absolute;top:8px;left:32px;right:32px;height:56px;border-radius:999px;background:%s;display:flex;align-items:center;'
            'padding:0 4px;z-index:3"><span style="width:48px;display:flex;justify-content:center">%s</span><b style="font-size:22px">%s</b></div>' % (
                fill or t['bar'], N.glyph(lead, t['ink'], 22), title))


def tick(t, on):
    if on:
        return ('<span style="width:22px;height:22px;flex:none;border-radius:7px;background:%s;box-shadow:inset 0 0 0 2px %s;display:flex;align-items:center;'
                'justify-content:center">%s</span>' % (t['prim'], A.edge(t), B.svg('check', t['onp'], 16, 3)))
    return '<span style="width:22px;height:22px;flex:none;border-radius:7px;border:2px solid %s;box-sizing:border-box"></span>' % t['line']


def mark(t, text):
    return ('<span style="display:inline-flex;align-items:center;gap:6px;height:22px;padding:0 8px;border-radius:999px;background:%s;font-size:12px;width:max-content">'
            '<span style="width:6px;height:6px;border-radius:50%%;background:%s"></span>%s</span>' % (U.mix(t['page'], t['warm'], .18), t['warm'], text))


def draft_row(t, d, on, selected=False, lead=True):
    title, sub, amt, cap, need = d
    under = mark(t, need) if need else '<span style="font-size:12px;color:%s">%s</span>' % (t['muted'], cap)
    money = '<span style="%sfont-size:15px">%s</span>' % (MONO, amt) if amt else '<span style="color:%s">—</span>' % t['muted']
    bg = 'background:%s;' % U.mix(t['surf'], t['prim'], .25) if selected else ''
    return ('<div style="display:flex;align-items:center;gap:12px;min-height:64px;padding:6px 14px;border-top:1px solid %s;%s">%s'
            '<span style="flex:1;display:flex;flex-direction:column;gap:2px"><span style="font-size:16px">%s</span><span style="font-size:13px;color:%s">%s</span>%s</span>%s</div>' % (
                U.mix(t['surf'], t['line'], .5), bg, tick(t, on) if lead else '', title, t['muted'], sub, under, money))


def drafts_list(t, ticks, selected=(), lead=True):
    rows = ''.join(draft_row(t, d, ticks[i], i in selected, lead) for i, d in enumerate(DRAFTS))
    return ('<div style="padding:72px 0 0"><div style="margin:8px 20px 6px;font-size:13px;color:%s">Черновиков: 5 · готовы: 3</div>'
            '<div style="margin:0 12px;border-radius:24px;background:%s;border:1px solid %s;overflow:hidden">%s</div></div>' % (
                t['muted'], t['surf'], t['line'], rows.replace('border-top', 'border-top', 1)))


def pill_btn(t, label, filled=True, icon=None, grow=1):
    st = ('background:%s;color:%s;box-shadow:inset 0 0 0 1.5px %s' % (t['prim'], t['onp'], A.edge(t))) if filled else 'border:1.5px solid %s;color:%s' % (t['line'], t['ink'])
    ic = N.glyph(icon, t['onp'] if filled else t['ink'], 20) if icon else ''
    return ('<span style="flex:%d;height:48px;border-radius:999px;display:flex;align-items:center;justify-content:center;gap:8px;font-weight:600;%s">%s%s</span>' % (grow, st, ic, label))


def footer(t, *buttons):
    return '<div style="position:absolute;left:16px;right:16px;bottom:16px;display:flex;gap:10px">%s</div>' % ''.join(buttons)


def caption(t, text, top=560):
    return R.caption(t, text, top)


def dr_now(t, v):
    return U.phone(t, topbar(t, 'Черновики') + drafts_list(t, [True, False, True, False, True]) + footer(t, pill_btn(t, 'Записать · 3', icon=B.ICON['check']))
                   + caption(t, 'Сейчас: удалить черновик нельзя — «тест заметки» и «позвонить маме» висят, пока их не запишешь.'), H)


def dr_a(t, v):
    return U.phone(t, topbar(t, 'Черновики') + drafts_list(t, [False, True, False, True, False])
                   + footer(t, pill_btn(t, 'Удалить · 2', False, TRASH), pill_btn(t, 'Записать', icon=B.ICON['check'], grow=1))
                   + caption(t, 'А: отметили галочками ненужные — рядом с «Записать» появляется «Удалить · N». Плашка «Удалено черновиков: 2 · Отменить».'), H)


def dr_b(t, v):
    bar = ('<div style="position:absolute;left:32px;right:32px;bottom:16px;height:56px;border-radius:999px;background:%s;display:flex;align-items:center;'
           'justify-content:center;gap:10px;font-weight:600">%s<span>Удалить · 2</span></div>' % (t['bar'], N.glyph(TRASH, t['ink'], 22)))
    return U.phone(t, topbar(t, 'Выбрано 2', X, U.mix(t['bar'], t['prim'], .15)) + drafts_list(t, [False] * 5, (1, 3), lead=False) + bar
                   + caption(t, 'Б: как в «Операциях» — удержите строку, отметьте ещё; внизу «Удалить · N», «✕» — выйти. Галочки «записать» на время прячутся.'), H)


def ai_head(t, chip):
    return ('<div style="position:absolute;top:8px;left:32px;right:32px;height:56px;border-radius:999px;background:%s;display:flex;align-items:center;gap:8px;'
            'padding:0 4px;z-index:3"><span style="width:48px;display:flex;justify-content:center">%s</span><b style="font-size:22px">ИИ на телефоне</b>%s</div>' % (
                t['bar'], N.glyph(N.ARROW, t['ink'], 22), chip))


def beta(t):
    return ('<span style="display:inline-flex;align-items:center;gap:6px;height:22px;padding:0 8px;border-radius:999px;background:%s;font-size:12px">'
            '<span style="width:6px;height:6px;border-radius:50%%;background:%s"></span>бета</span>' % (U.mix(t['page'], t['acc'], .18), t['acc']))


def ai_card(t):
    return ('<div style="margin:84px 12px 0;padding:16px;border-radius:24px;background:%s;border:1px solid %s"><div style="display:flex;gap:10px;align-items:center">%s'
            '<b style="font-size:17px">Умные функции включены</b></div><div style="margin-top:8px;font-size:14px;color:%s">Модель работает на телефоне, без интернета. '
            'Разбирает быстрые заметки, которые не поняли правила.</div></div>' % (t['surf'], t['line'], N.glyph(SPARK, t['acc'] if t['dark'] else '#5B1FA8', 22), t['muted']))


def ai_draft(t, with_beta):
    tag = ' ' + beta(t) if with_beta else ''
    return ('<div style="margin:12px 12px 0;padding:12px 14px;border-radius:24px;background:%s;border:1px solid %s"><div style="font-size:12px;letter-spacing:.04em;color:%s">'
            'ЧЕРНОВИК</div><div style="display:flex;justify-content:space-between;margin-top:6px"><span style="font-size:16px">поездка</span><span style="%s">−2 000</span></div>'
            '<div style="display:flex;align-items:center;gap:6px;margin-top:4px;font-size:12px;color:%s">%s Такси · предложено ИИ%s</div></div>' % (
                t['surf'], t['line'], t['muted'], MONO, t['muted'], N.glyph(SPARK, t['prim'], 12), tag))


def beta_a(t, v):
    return U.phone(t, ai_head(t, beta(t)) + ai_card(t) + ai_draft(t, True)
                   + caption(t, 'А: метка «бета» везде, где работает ИИ: в шапке «ИИ на телефоне», у подписи «предложено ИИ», в «Ещё → Настройки → Умные функции».', 420), 620)


def beta_b(t, v):
    row = ('<div style="margin:12px 12px 0;padding:0 14px;min-height:56px;border-radius:24px;background:%s;border:1px solid %s;display:flex;align-items:center;gap:10px">'
           '%s<span style="flex:1;font-size:16px">Умные функции</span>%s%s</div>' % (t['surf'], t['line'], N.glyph(SPARK, t['acc'] if t['dark'] else '#5B1FA8', 22), beta(t),
                                                                                    N.glyph(CHEV, t['muted'], 20)))
    return U.phone(t, ai_head(t, beta(t)) + ai_card(t) + row + ai_draft(t, False)
                   + caption(t, 'Б: «бета» только на входе — в шапке «ИИ на телефоне» и в строке настроек; подписи у черновиков без метки (тише).', 420), 620)


def hero(t):
    return ('<div style="margin:84px 12px 0;padding:16px;border-radius:24px;background:linear-gradient(135deg,%s,%s);border:1px solid %s">'
            '<div style="font-size:13px;color:%s">Всего на счетах</div><div style="%sfont-size:32px;font-weight:600;margin-top:4px">1 284 300 ARS</div></div>' % (
                U.mix(t['page'], t['prim'], .28), U.mix(t['page'], t['acc'], .3), t['line'], t['muted'], MONO))


def tile(t, head, body, extra=''):
    return ('<div style="flex:1;min-height:104px;padding:12px 14px;border-radius:20px;background:%s;border:1px solid %s;%s"><div style="font-size:13px;color:%s">%s</div>%s</div>' % (
        t['surf'], t['line'], extra, t['muted'], head, body))


def notes_a(t, v):
    note = tile(t, 'Быстрые заметки', '<div style="margin-top:6px;font-size:15px">такси 2000<br><span style="color:%s">+ ещё 2 строки</span></div>'
                '<div style="margin-top:8px;font-size:13px;color:%s">Черновики · 3</div>' % (t['muted'], t['acc'] if t['dark'] else '#5B1FA8'))
    spent = tile(t, 'Траты за ноябрь', '<div style="%sfont-size:22px;margin-top:6px">1 240,50</div><div style="font-size:12px;color:%s">USD</div>' % (MONO, t['muted']))
    body = hero(t) + '<div style="display:flex;gap:10px;margin:10px 12px 0">%s%s</div>' % (note, spent)
    return U.phone(t, topbar(t, 'Обзор', B.ICON['home']).replace(N.glyph(B.ICON['home'], t['ink'], 22), '') + body + nav_home(t)
                   + caption(t, 'А: плитка «Быстрые заметки» в сетке плиток: видна последняя строка и сколько черновиков ждут. Нажмите — откроется заметка.', 400), 620)


def notes_b(t, v):
    strip = ('<div style="margin:10px 12px 0;padding:10px 14px;border-radius:24px;background:%s;border:1px solid %s;display:flex;align-items:center;gap:12px">%s'
             '<span style="flex:1"><b style="font-size:16px">Быстрые заметки</b><div style="font-size:13px;color:%s">Траты строками · черновиков 3</div></span>'
             '<span style="height:36px;padding:0 14px;border-radius:999px;background:%s;color:%s;box-shadow:inset 0 0 0 1.5px %s;display:flex;align-items:center;font-weight:600">Записать</span></div>' % (
                 t['surf'], t['line'], N.glyph(NOTE, t['acc'] if t['dark'] else '#5B1FA8', 24), t['muted'], t['prim'], t['onp'], A.edge(t)))
    spent = tile(t, 'Траты за ноябрь', '<div style="%sfont-size:22px;margin-top:6px">1 240,50</div><div style="font-size:12px;color:%s">USD</div>' % (MONO, t['muted']))
    other = tile(t, 'Без категории', '<div style="%sfont-size:22px;margin-top:6px">14</div><div style="font-size:12px;color:%s">операций</div>' % (MONO, t['muted']))
    body = hero(t) + strip + '<div style="display:flex;gap:10px;margin:10px 12px 0">%s%s</div>' % (spent, other)
    return U.phone(t, topbar(t, 'Обзор', B.ICON['home']).replace(N.glyph(B.ICON['home'], t['ink'], 22), '') + body + nav_home(t)
                   + caption(t, 'Б: строка «Быстрые заметки» под итогом — всегда (сейчас она появляется только с черновиками). «Записать» сразу открывает заметку.', 420), 620)


def nav_home(t):
    """The bottom bar with «Обзор» chosen (the shared one in mockup_poll_reports568 shows «Отчёты»)."""
    cells = ''
    for k in ['home', 'list', '+', 'chart', 'more']:
        if k == '+':
            cells += '<span style="width:48px;height:48px;border-radius:50%%;background:%s;display:flex;align-items:center;justify-content:center">%s</span>' % (t['prim'], B.svg('plus', t['onp'], 26, 2.2))
        elif k == 'home':
            cells += '<span style="height:44px;width:64px;border-radius:999px;background:%s;display:flex;align-items:center;justify-content:center">%s</span>' % (t['prim'], B.svg(B.ICON[k], t['onp'], 24, 1.7))
        else:
            cells += '<span style="display:flex;justify-content:center">%s</span>' % B.svg(B.ICON[k], t['muted'], 24, 1.7)
    return ('<div style="position:absolute;left:32px;right:32px;bottom:16px;height:56px;border-radius:999px;background:%s;display:grid;'
            'grid-template-columns:repeat(5,1fr);align-items:center;justify-items:center;z-index:3">%s</div>' % (t['bar'], cells))


BOARDS = [('NbDrNow', dr_now, H), ('NbDrA', dr_a, H), ('NbDrB', dr_b, H), ('NbBetaA', beta_a, 620), ('NbBetaB', beta_b, 620),
          ('NbNotesA', notes_a, 620), ('NbNotesB', notes_b, 620)]


def main():
    out = sys.argv[1]
    os.makedirs(os.path.join(out, 'html'), exist_ok=True)
    for name, fn, h in BOARDS:
        for suffix, dark in (('', True), ('@light', False)):
            t = U.theme(dark)
            open(os.path.join(out, 'html', name + suffix + '.html'), 'w', encoding='utf-8').write(N.page(fn(t, name), 390, h, dict(page=t['page'])))
    js = os.path.join(out, 'render.js')
    src = open(os.path.join(HERE, 'mockup_poll_update.py'), encoding='utf-8').read()
    open(js, 'w').write(src[src.index("'''const") + 3:src.index("''')")].replace('\\\\d', '\\d'))
    subprocess.run(['node', js, os.path.abspath(out)], check=True, cwd=out)
    print('boards → %s/img' % out)


if __name__ == '__main__':
    main()
