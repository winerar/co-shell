#!/usr/bin/env python3
"""Poll boards for #154, second part (session 2026-11-27): which tabs the bottom bar shows. The old board Fb154Tabs is out of date (it has «Счета» as a tab;
since #140 the bar is Обзор · Операции · [+] · Отчёты · Ещё). «Обзор» editing and the two tiles are done.
Usage: python3 scripts/agent/mockup_poll_tabs154.py <out-dir>  → <out-dir>/img/*.png, then poll-page.py <spec> <poll-dir> --img <out-dir>/img
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from mockup_poll_f_common import *  # noqa: E402,F401,F403
import mockup_poll_f_common as C  # noqa: E402
import mockup_bricks as B  # noqa: E402

CHEV = 'M6 9l6 6 6-6'


def bar(t, keys, active, plus_at=2):
    """A bottom bar of [keys] (B.ICON names) with «+» in the middle."""
    cells = ''
    for i, k in enumerate(keys):
        if i == plus_at:
            cells += '<span style="width:48px;height:48px;border-radius:50%%;background:%s;display:flex;align-items:center;justify-content:center">%s</span>' % (t['prim'], B.svg('plus', t['onp'], 26, 2.2))
        if k == active:
            cells += '<span style="height:44px;width:64px;border-radius:999px;background:%s;display:flex;align-items:center;justify-content:center">%s</span>' % (t['prim'], B.svg(B.ICON[k], t['onp'], 24, 1.7))
        else:
            cells += '<span style="display:flex;justify-content:center">%s</span>' % B.svg(B.ICON[k], t['muted'], 24, 1.7)
    return ('<div style="display:grid;grid-template-columns:repeat(5,1fr);align-items:center;justify-items:center;height:56px;border-radius:999px;background:%s;margin:0 16px">%s</div>' % (t['bar'], cells))


def slot(t, n, name, icon, fixed=False, drop=True):
    right = C.glyph('M7 11V8a5 5 0 0 1 10 0v3M6 11h12v9H6z', t['muted'], 18) if fixed else ('<span style="display:flex;align-items:center;gap:4px;font-size:14px;font-weight:600;color:%s">Сменить%s</span>' % (C.ink2(t), C.glyph(CHEV, C.ink2(t), 18)) if drop else '')
    return ('<div style="display:flex;align-items:center;gap:12px;padding:10px 16px;border-top:1px solid %s"><span style="width:40px;height:40px;border-radius:50%%;background:%s;display:flex;align-items:center;justify-content:center">%s</span>'
            '<div style="flex:1"><div style="font-size:16px">%s</div><div style="font-size:13px;color:%s">%s</div></div>%s</div>') % (
        t['line'], C.mix(t['surf'], t['prim'], .3), B.svg(B.ICON[icon], t['ink'], 22, 1.7), name, t['muted'], n, right)


def group(t, rows):
    return '<div style="margin:12px 16px;border-radius:24px;background:%s;border:1px solid %s;overflow:hidden">%s</div>' % (t['surf'], t['line'], rows.replace('border-top:1px solid %s' % t['line'], 'border-top:0', 1))


def screen(t, title, inner):
    return C.topbar(t, title) + '<div style="padding-top:72px">' + inner + '</div>'


# ---- Q1: how much of the bar is yours ------------------------------------------------------------------------------------
def scope_a(t):
    rows = slot(t, 'всегда первая', 'Обзор', 'home', fixed=True) + slot(t, 'вторая', 'Операции', 'list') + slot(t, 'третья', 'Отчёты', 'chart') + slot(t, 'всегда последняя', 'Ещё', 'more', fixed=True)
    return screen(t, 'Нижняя панель', '<div style="margin:12px 16px 0">' + bar(t, ['home', 'list', 'chart', 'more'], 'home') + '</div>' + group(t, rows) +
                  '<div style="margin:0 20px;font-size:13px;color:%s">Две вкладки на выбор: Операции, Отчёты, Счета, Бюджеты, Цели, Долги.</div>' % t['muted']) + \
        C.caption(t, 'А: две вкладки меняются на любые из шести разделов; «Обзор», «+» и «Ещё» на местах. Всё остальное по-прежнему в «Ещё».', 0)


def scope_b(t):
    rows = slot(t, 'всегда первая', 'Обзор', 'home', fixed=True) + slot(t, 'вторая · ⠿ тяните', 'Операции', 'list', drop=False) + slot(t, 'третья · ⠿ тяните', 'Отчёты', 'chart', drop=False) + slot(t, 'всегда последняя', 'Ещё', 'more', fixed=True)
    return screen(t, 'Нижняя панель', '<div style="margin:12px 16px 0">' + bar(t, ['home', 'chart', 'list', 'more'], 'home') + '</div>' + group(t, rows)) + \
        C.caption(t, 'Б: только порядок — «Операции» и «Отчёты» меняются местами. Новых вкладок нет, «Счета» остаются в «Ещё».', 0)


def scope_c(t):
    rows = slot(t, 'Обзор', 'Обзор', 'home', fixed=True)
    note = ('<div style="margin:16px;padding:16px;border-radius:24px;background:%s;border:1px solid %s;font-size:15px;line-height:1.4">'
            'Панель остаётся как есть: <b>Обзор · Операции · «+» · Отчёты · Ещё</b>. Нужное под рукой уже настраивается: карточки и плитки на «Обзоре» (удержание), «Все счета ›» там же.</div>') % (t['surf'], t['line'])
    return screen(t, 'Обзор', '<div style="margin:12px 16px 0">' + bar(t, ['home', 'list', 'chart', 'more'], 'home') + '</div>' + note) + \
        C.caption(t, 'В: не делать вкладки настраиваемыми и закрыть задачу. Меньше кода и меньше способов запутаться; «Счета» остаются в «Ещё».', 0)


# ---- Q2: where it is changed ---------------------------------------------------------------------------------------------
def where_a(t):
    rows = slot(t, 'вторая', 'Операции', 'list') + slot(t, 'третья', 'Счета', 'wallet')
    return screen(t, 'Нижняя панель', '<div style="margin:12px 16px 0">' + bar(t, ['home', 'list', 'wallet', 'more'], 'home') + '</div>' + group(t, rows)) + \
        C.caption(t, 'А: «Ещё → Настройки → Экраны → Нижняя панель» — рядом с настройкой «Обзора»; сверху живой образец панели.', 0)


def where_b(t):
    sheet = ('<div style="margin:12px 16px 0;padding:14px;border-radius:24px;background:%s;border:1px solid %s"><div style="font-size:15px;font-weight:600">Что здесь показывать</div>%s</div>') % (
        t['surf'], t['line'], ''.join('<div style="display:flex;align-items:center;gap:12px;padding:10px 0;border-top:1px solid %s"><span style="width:28px;height:28px;border-radius:50%%;background:%s;display:flex;align-items:center;justify-content:center">%s</span><span style="flex:1;font-size:15px">%s</span>%s</div>' % (
            t['line'], C.mix(t['surf'], t['prim'], .3), B.svg(B.ICON[k], t['ink'], 18, 1.7), n, C.glyph('M5 12l5 5 9-10', t['ink'], 20) if on else '') for k, n, on in [('list', 'Операции', True), ('chart', 'Отчёты', False), ('wallet', 'Счета', False), ('budget', 'Бюджеты', False)]))
    return C.topbar(t, 'Операции', back=False) + '<div style="padding-top:72px">' + sheet + '</div>' + \
        '<div style="position:absolute;left:0;right:0;bottom:16px">' + bar(t, ['home', 'list', 'chart', 'more'], 'list') + '</div>' + \
        C.caption(t, 'Б: удержание вкладки на панели открывает шторку «Что здесь показывать» с выбором раздела. Быстро, но жест надо открыть (подсказка, как у «Обзора»).', 0)


BOARDS = [('Tabs154ScopeA', scope_a, 390, 820), ('Tabs154ScopeB', scope_b, 390, 820), ('Tabs154ScopeC', scope_c, 390, 820),
          ('Tabs154WhereA', where_a, 390, 820), ('Tabs154WhereB', where_b, 390, 820)]

if __name__ == '__main__':
    C.run(BOARDS, sys.argv[1])
