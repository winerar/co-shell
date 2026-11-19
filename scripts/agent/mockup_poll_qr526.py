#!/usr/bin/env python3
"""Poll boards for #526: scanning the QR of an Argentine receipt (AFIP) from the entry sheet.

Standalone PNGs (like mockup_poll_accounts4.py, whose entry-sheet bricks are reused): `QrXxx.png` dark + `QrXxx@light.png`.
Usage: python3 scripts/agent/mockup_poll_qr526.py <out-dir>  → <out-dir>/img/*.png   then   poll-page.py <spec> <poll-dir> --img <out-dir>/img
Account badges are letters (as in the app today), not bank logos, so no SVG downloads are needed.
"""
import os
import subprocess
import sys

HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, HERE)
import mockup_nav_poll as N  # noqa: E402
import mockup_poll_accounts as P  # noqa: E402
import mockup_poll_accounts4 as A  # noqa: E402
import mockup_poll_update as U  # noqa: E402

_badge = P.badge
P.badge = lambda acc, size=40, style='A', t=None: _badge(acc, size, 'L', t)  # letters, like the app

MONO = A.MONO
QR = ('<svg width="%d" height="%d" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.6" stroke-linecap="round">'
      '<path d="M4 8V5a1 1 0 0 1 1-1h3M16 4h3a1 1 0 0 1 1 1v3M20 16v3a1 1 0 0 1-1 1h-3M8 20H5a1 1 0 0 1-1-1v-3"/>'
      '<rect x="8" y="8" width="3" height="3"/><rect x="13" y="8" width="3" height="3"/><rect x="8" y="13" width="3" height="3"/><path d="M13 13h3v3"/></svg>')


def qr(size=20):
    return QR % (size, size)


def round_btn(t, inner, size=44):
    return '<span style="width:%dpx;height:%dpx;flex:none;border-radius:50%%;background:%s;display:flex;align-items:center;justify-content:center;font-size:18px">%s</span>' % (size, size, t['bar'], inner)


def seg2(t, qr_btn=True):
    """The type switch with «Шаблоны» and (variant A) the QR button next to it."""
    items = ''.join('<span style="flex:1;text-align:center;height:36px;line-height:36px;border-radius:999px;font-size:14px;%s">%s</span>' % (
        ('background:%s;color:%s;font-weight:700;box-shadow:inset 0 0 0 2px %s' % (t['prim'], t['onp'], A.edge(t))) if i == 0 else '', n)
        for i, n in enumerate(['Расход', 'Доход', 'Перевод']))
    btns = round_btn(t, '☰') + (round_btn(t, qr(22)) if qr_btn else '')
    return '<div style="display:flex;gap:8px;align-items:center;margin:0 12px 8px"><div style="flex:1;padding:4px;border-radius:999px;background:%s;display:flex">%s</div>%s</div>' % (t['bar'], items, btns)


def scan_row(t):
    """Variant B: its own full-width row above the amount."""
    return ('<div style="margin:0 12px 8px;height:40px;border-radius:999px;background:%s;display:flex;align-items:center;justify-content:center;gap:8px;font-size:14px">%s<span>Сканировать QR чека</span></div>' % (t['bar'], qr(20)))


def empty_amount(t, qr_in_field=False):
    btn = '<span style="width:40px;height:40px;border-radius:50%%;background:%s;display:flex;align-items:center;justify-content:center">%s</span>' % (t['surf'], qr(20)) if qr_in_field else ''
    field = ('<div style="margin-top:6px;height:52px;border-radius:16px;border:2px solid %s;background:%s;display:flex;align-items:center;justify-content:space-between;padding:0 8px 0 14px">'
             '<span style="%sfont-size:26px;color:%s">0</span><span style="display:flex;align-items:center;gap:6px">%s<span style="padding:8px 14px;border-radius:999px;background:%s;font-size:14px">ARS</span></span></div>' % (
                 A.edge(t), t['bar'], MONO, t['muted'], btn, t['surf']))
    return A.card(t, 'СУММА', field, '+ Чаевые &nbsp;&nbsp; + Сумма покупки')


def line(t, left, right):
    return '<div style="margin:-2px 16px 8px;display:flex;justify-content:space-between;font-size:13px;color:%s"><span>%s</span><span style="%s">%s</span></div>' % (t['muted'], left, MONO, right)


def source_tag(t):
    """After a scan: where the numbers came from, one quiet line."""
    return ('<div style="margin:0 16px 8px;display:flex;align-items:center;gap:6px;font-size:13px;color:%s">%s<span>Из QR чека · продавец 30-68584975-1</span></div>' % (t['muted'], qr(16)))


def filled_amount(t, value, code, below=''):
    field = ('<div style="margin-top:6px;height:52px;border-radius:16px;border:2px solid %s;background:%s;display:flex;align-items:center;justify-content:space-between;padding:0 8px 0 14px">'
             '<span style="%sfont-size:26px;color:%s">%s</span><span style="padding:8px 14px;border-radius:999px;background:%s;font-size:14px">%s</span></div>%s' % (
                 A.edge(t), t['bar'], MONO, '#E0406A' if not t['dark'] else '#FF8FA8', value, t['surf'], code, below))
    return A.card(t, 'СУММА', field, '+ Чаевые &nbsp;&nbsp; <span style="font-weight:700">✓ Сумма покупки</span>' if below else '+ Чаевые &nbsp;&nbsp; + Сумма покупки')


def purchase_line(t):
    return ('<div style="margin-top:6px;padding:6px 12px;border-radius:16px;background:%s;display:flex;justify-content:space-between;align-items:center">'
            '<span style="font-size:13px;color:%s;white-space:nowrap">Сумма покупки · курс 1 180</span><span style="%sfont-size:15px;white-space:nowrap">37 914 ARS</span></div>' % (t['bar'], t['muted'], MONO))


def date_chips(t, date_filled=True):
    d = A.on_pill(t, '◷ 19 нояб.', 1) if date_filled else A.pill(t, 'Сегодня')
    rest = ''.join('<span style="flex:1;height:40px;border-radius:999px;display:flex;align-items:center;justify-content:center;font-size:13px;background:%s">%s</span>' % (t['bar'], x)
                   for x in ['Заметка', 'Теги', 'Возврат'])
    return '<div style="display:flex;gap:6px;margin:0 12px 8px">%s%s</div>' % (d.replace('flex:1 1 auto', 'flex:1 1 0'), rest)


def entry(t, head, amount, tag='', acc_line='', sel=1, note=''):
    return A.sheet(t, head + A.strip(t, sel) + acc_line + amount + tag + A.cats(t) + date_chips(t, bool(tag)) + A.keypad(t, 44))


def btn_a(t, v):
    return A.sheet(t, seg2(t) + A.strip(t) + empty_amount(t) + A.cats(t) + A.details(t) + A.keypad(t))


def btn_b(t, v):
    return A.sheet(t, seg2(t, False) + scan_row(t) + A.strip(t) + empty_amount(t) + A.cats(t) + A.details(t) + A.keypad(t, 40))


def btn_c(t, v):
    return A.sheet(t, seg2(t, False) + A.strip(t) + empty_amount(t, True) + A.cats(t) + A.details(t) + A.keypad(t))


def filled(t, v):
    return entry(t, seg2(t), filled_amount(t, '37 914', 'ARS'), source_tag(t), line(t, 'Galicia ARS', 'остаток 220 000 ARS'))


def foreign(t, v):
    return entry(t, seg2(t), filled_amount(t, '32,13', 'USDT', purchase_line(t)), source_tag(t), line(t, 'USDT-карта', 'остаток 410 USDT'), 3)


def dup_banner(t, v):
    box = ('<div style="margin:0 12px 8px;padding:10px 14px;border-radius:24px;background:%s;border:1.5px solid %s"><div style="font-size:15px;font-weight:700">Этот чек уже записан</div>'
           '<div style="font-size:13px;color:%s;margin-top:2px">Продукты · 19 нояб., 14:05 · 37 914 ARS</div><div style="display:flex;gap:8px;margin-top:8px">%s%s</div></div>' % (
               t['surf'], t['acc'] if t['dark'] else '#7A2BD6', t['muted'], A.pill(t, 'Закрыть'), A.on_pill(t, 'Записать ещё раз', 1)))
    return A.sheet(t, seg2(t) + box + A.strip(t, 1) + filled_amount(t, '37 914', 'ARS') + A.cats(t) + A.keypad(t, 40))


def dup_dialog(t, v):
    base = filled(t, v)
    card = ('<div style="position:absolute;left:28px;right:28px;top:300px;padding:20px;border-radius:28px;background:%s;border:1px solid %s">'
            '<div style="font-size:18px;font-weight:700">Этот чек уже записан</div><div style="font-size:14px;color:%s;margin-top:6px">В операциях есть такой же: Продукты, 19 нояб., 37 914 ARS.</div>'
            '<div style="display:flex;gap:8px;margin-top:16px"><span style="flex:1;height:48px;border-radius:999px;background:%s;display:flex;align-items:center;justify-content:center;font-size:14px">Отмена</span>'
            '<span style="flex:1.4;height:48px;border-radius:999px;background:%s;color:%s;font-weight:700;display:flex;align-items:center;justify-content:center;font-size:14px;box-shadow:inset 0 0 0 2px %s">Всё равно записать</span></div></div>' % (
                t['page'], t['line'], t['muted'], t['bar'], t['prim'], t['onp'], A.edge(t)))
    return _overlay(base, card)


def _overlay(page_html, extra):
    i = page_html.rfind('</div>')
    return page_html[:i] + '<div style="position:absolute;inset:0;background:rgba(0,0,0,.45)"></div>' + extra + page_html[i:]


def camera(t, bottom, snack=''):
    frame = ('<div style="position:absolute;left:55px;top:230px;width:280px;height:280px"><svg viewBox="0 0 280 280" width="280" height="280" fill="none" stroke="%s" stroke-width="5" stroke-linecap="round">'
             '<path d="M4 60V24a20 20 0 0 1 20-20h36M220 4h36a20 20 0 0 1 20 20v36M276 220v36a20 20 0 0 1-20 20h-36M60 276H24a20 20 0 0 1-20-20v-36"/></svg></div>' % t['prim'])
    top = ('<div style="position:absolute;left:16px;right:16px;top:44px;display:flex;align-items:center;gap:12px"><span style="width:48px;height:48px;border-radius:50%;background:rgba(255,255,255,.14);display:flex;align-items:center;justify-content:center;font-size:20px;color:#fff">✕</span>'
           '<span style="font-size:22px;font-weight:700;color:#fff">Сканировать QR</span></div>')
    body = ('<div style="position:absolute;inset:0;background:linear-gradient(160deg,#2a2f2b,#0c0f0d 70%)"></div>' + top + frame + bottom + snack)
    return U.phone(t, body, 844)


def scan_bottom(t):
    return ('<div style="position:absolute;left:24px;right:24px;top:540px;text-align:center;color:#E8EEE9;font-size:15px">Наведите камеру на QR-код на чеке.<br><span style="color:#9AA79D;font-size:13px">Он читается без интернета.</span></div>'
              '<div style="position:absolute;left:16px;right:16px;bottom:44px;display:flex;gap:8px;align-items:center"><span style="width:48px;height:48px;border-radius:50%%;background:rgba(255,255,255,.14);display:flex;align-items:center;justify-content:center;font-size:20px;color:#fff">🔦</span>'
              '<span style="flex:1;height:48px;border-radius:999px;background:%s;color:%s;font-weight:700;display:flex;align-items:center;justify-content:center;font-size:15px;box-shadow:inset 0 0 0 2px %s">Ввести вручную</span></div>' % (t['prim'], t['onp'], A.edge(t)))


def scan(t, v):
    return camera(t, scan_bottom(t))


def bad(t, v):
    snack = ('<div style="position:absolute;left:16px;right:16px;bottom:112px;padding:12px 16px;border-radius:999px;background:%s;color:%s;font-size:14px;display:flex;justify-content:space-between"><span>Это не QR чека. Попробуйте другой.</span><span style="font-weight:700;color:%s">OK</span></div>' % (
        t['page'], '#fff' if t['dark'] else '#111', t['acc'] if t['dark'] else '#7A2BD6'))
    return camera(t, scan_bottom(t), snack)


def nocam(t, v):
    card = ('<div style="position:absolute;left:20px;right:20px;top:250px;padding:22px;border-radius:28px;background:%s;border:1px solid %s;text-align:center">'
            '<div style="display:flex;justify-content:center;margin-bottom:10px"><span style="width:56px;height:56px;border-radius:50%%;background:%s;display:flex;align-items:center;justify-content:center">%s</span></div>'
            '<div style="font-size:18px;font-weight:700">Камера нужна, чтобы прочитать QR</div><div style="font-size:14px;color:%s;margin-top:6px">Видео не сохраняется и никуда не уходит.</div>'
            '<div style="display:flex;flex-direction:column;gap:8px;margin-top:16px"><span style="height:48px;border-radius:999px;background:%s;color:%s;font-weight:700;display:flex;align-items:center;justify-content:center;box-shadow:inset 0 0 0 2px %s">Разрешить</span>'
            '<span style="height:48px;border-radius:999px;background:%s;display:flex;align-items:center;justify-content:center">Ввести вручную</span></div></div>' % (
                t['page'], t['line'], t['bar'], qr(28), t['muted'], t['prim'], t['onp'], A.edge(t), t['bar']))
    return U.phone(t, '<div style="position:absolute;inset:0;background:rgba(0,0,0,.35)"></div>' + card, 844)


BOARDS = [('QrBtnA', btn_a), ('QrBtnB', btn_b), ('QrBtnC', btn_c), ('QrFilled', filled), ('QrForeign', foreign),
          ('QrDupA', dup_banner), ('QrDupB', dup_dialog), ('QrScan', scan), ('QrBad', bad), ('QrNoCam', nocam)]


def main():
    out = sys.argv[1]
    os.makedirs(os.path.join(out, 'html'), exist_ok=True)
    for name, fn in BOARDS:
        for suffix, dark in (('', True), ('@light', False)):
            t = U.theme(dark)
            open(os.path.join(out, 'html', name + suffix + '.html'), 'w', encoding='utf-8').write(N.page(fn(t, name), 390, 844, dict(page=t['page'])))
    js = os.path.join(out, 'render.js')
    src = open(os.path.join(HERE, 'mockup_poll_update.py'), encoding='utf-8').read()
    open(js, 'w').write(src[src.index("'''const") + 3:src.index("''')")].replace('\\\\d', '\\d'))
    subprocess.run(['node', js, os.path.abspath(out)], check=True, cwd=out)
    print('boards → %s/img' % out)


if __name__ == '__main__':
    main()
