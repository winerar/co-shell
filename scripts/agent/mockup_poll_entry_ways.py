#!/usr/bin/env python3
"""Poll boards for #623 (hold «+»), #602 (forgot PIN), #323 (unsaved entry), #321 (amount from clipboard).

Standalone PNGs like mockup_poll_notes628.py (whose bricks are reused): `EwXxx.png` dark + `EwXxx@light.png`.
Usage: python3 scripts/agent/mockup_poll_entry_ways.py <out-dir>  → <out-dir>/img/*.png   then   poll-page.py <spec> <poll-dir> --img <out-dir>/img
"""
import math
import os
import subprocess
import sys

HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, HERE)
import mockup_bricks as B  # noqa: E402
import mockup_nav_poll as N  # noqa: E402
import mockup_poll_accounts4 as A  # noqa: E402
import mockup_poll_notes628 as P  # noqa: E402
import mockup_poll_qr526 as Q  # noqa: E402
import mockup_poll_update as U  # noqa: E402

MONO = U.MONO
H = 844
NOTE = P.NOTE
QR_PATH = 'M4 8V5a1 1 0 0 1 1-1h3M16 4h3a1 1 0 0 1 1 1v3M20 16v3a1 1 0 0 1-1 1h-3M8 20H5a1 1 0 0 1-1-1v-3M9 9h2v2H9zM13 9h2v2h-2zM9 13h2v2H9z'
TEMPLATE = 'M4 6h16M4 12h16M4 18h10'
MIC = 'M12 4a3 3 0 0 1 3 3v5a3 3 0 0 1-6 0V7a3 3 0 0 1 3-3zM6 11a6 6 0 0 0 12 0M12 17v3'
CLIP = 'M9 4h6v3H9zM7 6H6v14h12V6h-1M9 12h6M9 16h4'
LOCK = 'M7 11V8a5 5 0 0 1 10 0v3M6 11h12v9H6z'
PHONE = 'M8 3h8v18H8zM11 18h2'


def caption(t, text, top=620):
    return ('<div style="position:absolute;left:16px;right:16px;top:%dpx;padding:10px 14px;border-radius:16px;background:%s;font-size:13px;line-height:1.35;'
            'color:%s;z-index:5">%s</div>' % (top, t['surf'], t['ink'], text))


def home(t, over='', cap=''):
    body = P.hero(t) + '<div style="display:flex;gap:12px;margin:12px 12px 0">%s%s</div>' % (
        P.tile(t, 'Продукты', '<div style="%sfont-size:17px;margin-top:6px">12 000 / 40 000</div>' % MONO),
        P.tile(t, 'Быстрые заметки', '<div style="margin-top:6px;font-size:15px">такси 2000</div>'))
    return U.phone(t, P.topbar(t, 'Обзор', B.ICON['home']) + body + P.nav_home(t) + over + cap, H)


def circle(t, path, x, y, size=52, label=None, hot=False):
    bg = ('background:%s;box-shadow:inset 0 0 0 2px %s' % (t['prim'], A.edge(t))) if hot else 'background:%s;border:1px solid %s' % (t['surf'], t['line'])
    ink = t['onp'] if hot else t['ink']
    lab = ('<span style="position:absolute;left:%dpx;top:%dpx;transform:translateX(-50%%);font-size:12px;color:%s;white-space:nowrap">%s</span>' % (
        x + size // 2, y + size + 4, t['ink'], label)) if label else ''
    return ('<span style="position:absolute;left:%dpx;top:%dpx;width:%dpx;height:%dpx;border-radius:50%%;display:flex;align-items:center;justify-content:center;%s;z-index:6">%s</span>%s' % (
        x, y, size, size, bg, N.glyph(path, ink, 24), lab))


def scrim(t):
    return '<div style="position:absolute;inset:0;background:%s;opacity:.55;z-index:4"></div>' % ('#000' if t['dark'] else '#FFF')


def fan(t, v):
    """A: round buttons on a half circle above the held «+» (centre of the bar, 195 px)."""
    items = [(QR_PATH, 'QR чека'), (NOTE, 'Заметка'), (TEMPLATE, 'Шаблон'), (MIC, 'Голос')]
    cx, cy, r = 195, 770, 118
    out = scrim(t)
    for i, (p, lab) in enumerate(items):
        ang = math.radians(180 - 36 - i * (108 / 3))
        x, y = cx + r * math.cos(ang) - 26, cy - r * math.sin(ang) - 26
        out += circle(t, p, round(x), round(y), 52, lab, hot=(i == 1))
    return home(t, out, caption(t, 'А: удержали «+» — способы веером. Не отпуская, ведёте палец к нужному, отпускаете: откроется. Коротко нажали — всё как сейчас.', 150))


def stack(t, v):
    """B: pills with words in a column above «+»."""
    items = [(QR_PATH, 'Сканировать QR'), (NOTE, 'Быстрая заметка'), (TEMPLATE, 'Из шаблона'), (MIC, 'Голосом')]
    out = scrim(t)
    for i, (p, lab) in enumerate(items):
        y = 560 + i * 54
        hot = i == 1
        st = ('background:%s;color:%s;box-shadow:inset 0 0 0 2px %s' % (t['prim'], t['onp'], A.edge(t))) if hot else 'background:%s;border:1px solid %s;color:%s' % (t['surf'], t['line'], t['ink'])
        out += ('<span style="position:absolute;left:104px;right:104px;top:%dpx;height:46px;border-radius:999px;display:flex;align-items:center;gap:10px;padding:0 16px;font-size:15px;%s;z-index:6">%s%s</span>' % (
            y, st, N.glyph(p, t['onp'] if hot else t['ink'], 22), lab))
    return home(t, out, caption(t, 'Б: удержали «+» — столбик с подписями. Слова вместо значков: понятнее новичку, но занимает больше места.', 150))


def hold_now(t, v):
    return home(t, '', caption(t, 'Сейчас: «+» — только новая операция. QR, заметки и шаблоны — внутри шторки или в «Ещё».', 150))


def lock(t, below, cap, h=H):
    dots = ''.join('<span style="width:14px;height:14px;border-radius:50%%;border:2px solid %s"></span>' % t['line'] for _ in range(4))
    keys = ''.join('<span style="height:56px;border-radius:999px;background:%s;display:flex;align-items:center;justify-content:center;%sfont-size:22px">%s</span>' % (
        t['surf'], MONO, k) for k in '123456789 0⌫')
    body = ('<div style="position:absolute;top:70px;left:0;right:0;text-align:center">%s<div style="margin-top:10px;font-size:20px;font-weight:600">Введите PIN</div>'
            '<div style="display:flex;gap:14px;justify-content:center;margin-top:18px">%s</div></div>'
            '<div style="position:absolute;left:36px;right:36px;top:250px;display:grid;grid-template-columns:repeat(3,1fr);gap:10px">%s</div>%s' % (
                N.glyph(LOCK, t['ink'], 40), dots, keys, below))
    return U.phone(t, body + caption(t, cap, 640), h)


def link(t, text):
    return '<div style="position:absolute;left:0;right:0;top:560px;text-align:center;font-size:15px;color:%s;font-weight:600">%s</div>' % (t['acc'], text)


def pin_now(t, v):
    return lock(t, '', 'Сейчас: забыли PIN — выхода нет, остаётся переустановить приложение (данные на телефоне пропадут).')


def sheet_over(t, title, text, buttons, icon):
    return ('<div style="position:absolute;inset:0;background:%s;opacity:.6"></div>'
            '<div style="position:absolute;left:24px;right:24px;top:210px;padding:20px;border-radius:28px;background:%s;border:1px solid %s;z-index:6">'
            '<div style="width:48px;height:48px;border-radius:16px;background:%s;display:flex;align-items:center;justify-content:center">%s</div>'
            '<div style="margin-top:12px;font-size:20px;font-weight:600">%s</div><div style="margin-top:6px;font-size:14px;color:%s;line-height:1.4">%s</div>'
            '<div style="display:flex;gap:10px;margin-top:16px">%s</div></div>' % (
                '#000' if t['dark'] else '#FFF', t['page'], t['line'], t['disc'], N.glyph(icon, t['ink'], 24), title, t['muted'], text, buttons))


def pin_a(t, v):
    return lock(t, link(t, 'Забыли PIN?') + sheet_over(t, 'Сбросить PIN?', 'Подтвердите блокировкой телефона — отпечатком, лицом или кодом. Данные не тронем, PIN зададите заново.',
                                                    P.pill_btn(t, 'Отмена', False) + P.pill_btn(t, 'Подтвердить', icon=N.glyph(PHONE, t['onp'], 20)), PHONE),
                'А: «Забыли PIN?» → подтверждаете блокировкой самого телефона → PIN сброшен, всё на месте. Это вход «по своему телефону», чужой его не пройдёт.')


def pin_b(t, v):
    return lock(t, link(t, 'Забыли PIN?') + sheet_over(t, 'Начать заново?', 'Данные на этом телефоне сотрутся, PIN снимется. Если вы в «Семье» или есть резервная копия — всё вернётся оттуда.',
                                                    P.pill_btn(t, 'Отмена', False) + P.pill_btn(t, 'Стереть', icon=N.glyph(P.TRASH, t['onp'], 20)), P.TRASH),
                'Б: PIN не обойти — только стереть данные с телефона и вернуть из «Семьи» или резервной копии. Строже, но вор с телефоном не получит записей.')


def entry_sheet(t, extra='', amount='', cap='', clip_row='', seg_btns=True):
    amt = amount or Q.empty_amount(t)
    body = extra + Q.seg2(t, seg_btns) + clip_row + A.strip(t) + amt + A.cats(t) + A.details(t) + A.keypad(t, 40)
    return U.phone(t, A.sheet(t, body) + (caption(t, cap, 14) if cap else ''), H)


def unsaved_plate(t):
    return ('<div style="margin:0 12px 8px;padding:10px 12px;border-radius:20px;background:%s;border:1px solid %s;display:flex;align-items:center;gap:10px">'
            '<span style="flex:1;font-size:13px;line-height:1.3"><b>Не сохранено</b><br><span style="%scolor:%s">3 500 ARS · Кафе</span> · 5 мин назад</span>'
            '<span style="padding:8px 14px;border-radius:999px;background:%s;color:%s;font-weight:600;font-size:14px">Продолжить</span>%s</div>' % (
                t['surf'], t['line'], MONO, t['ink'], t['prim'], t['onp'], N.glyph(P.X, t['muted'], 20)))


def draft_a(t, v):
    return entry_sheet(t, unsaved_plate(t), cap='А: «+» → вверху шторки плашка. «Продолжить» вернёт сумму и счёт, «✕» — забыть. Новая операция открывается пустой, как всегда.')


def draft_b(t, v):
    filled = Q.filled_amount(t, '3 500', 'ARS')
    line = ('<div style="margin:0 16px 8px;display:flex;justify-content:space-between;font-size:13px;color:%s"><span>Продолжили с 5 мин назад</span>'
            '<span style="color:%s;font-weight:600">Начать заново</span></div>' % (t['muted'], t['acc']))
    return entry_sheet(t, line, filled, cap='Б: шторка сразу открывается с несохранённым вводом, сверху тихая строка «Начать заново». Быстрее, но можно по ошибке дописать в старое.')


def clip_a(t, v):
    row = ('<div style="margin:0 12px 8px;height:40px;border-radius:999px;background:%s;display:flex;align-items:center;justify-content:center;gap:8px;font-size:14px">%s<span>Вставить из буфера</span></div>' % (
        t['bar'], N.glyph(CLIP, t['ink'], 20)))
    return entry_sheet(t, '', cap='А: строка над счетами, видна всегда, пока сумма пустая. Нажали — сумма и заметка из скопированного текста («Compra $12.500,00 COTO»).', clip_row=row, seg_btns=False)


def clip_b(t, v):
    btn = '<span style="width:40px;height:40px;border-radius:50%%;background:%s;display:flex;align-items:center;justify-content:center">%s</span>' % (t['surf'], N.glyph(CLIP, t['ink'], 20))
    field = ('<div style="margin-top:6px;height:52px;border-radius:16px;border:2px solid %s;background:%s;display:flex;align-items:center;justify-content:space-between;padding:0 8px 0 14px">'
             '<span style="%sfont-size:26px;color:%s">0</span><span style="display:flex;align-items:center;gap:6px">%s<span style="padding:8px 14px;border-radius:999px;background:%s;font-size:14px">ARS</span></span></div>' % (
                 A.edge(t), t['bar'], MONO, t['muted'], btn, t['surf']))
    return entry_sheet(t, '', A.card(t, 'СУММА', field, '+ Чаевые &nbsp;&nbsp; + Сумма покупки'),
                       cap='Б: значок-скрепка внутри поля суммы, рядом с валютой. Компактно, но новичок может не понять, что он делает.')


BOARDS = [('EwHoldNow', hold_now), ('EwHoldA', fan), ('EwHoldB', stack), ('EwPinNow', pin_now), ('EwPinA', pin_a), ('EwPinB', pin_b),
          ('EwDraftA', draft_a), ('EwDraftB', draft_b), ('EwClipA', clip_a), ('EwClipB', clip_b)]


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
