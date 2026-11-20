#!/usr/bin/env python3
"""Poll boards for #561: receipts shared into the app and feedback with files («Формат чека»).

Standalone PNGs like mockup_poll_qr526.py: `RcXxx.png` dark + `RcXxx@light.png`.
Usage: python3 scripts/agent/mockup_poll_receipts561.py <out-dir>  → <out-dir>/img/*.png   then   poll-page.py <spec> <poll-dir> --img <out-dir>/img
"""
import os
import subprocess
import sys

HERE = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, HERE)
import mockup_nav_poll as N  # noqa: E402
import mockup_poll_accounts4 as A  # noqa: E402
import mockup_poll_qr526 as Q  # noqa: E402
import mockup_poll_update as U  # noqa: E402

DOC = 'M7 3h7l5 5v13H7zM14 3v5h5M10 13h6M10 17h6'
CLIP = 'M8 12.5l6.4-6.4a3 3 0 0 1 4.2 4.2l-8.5 8.5a5 5 0 0 1-7-7L11 4'
IMG = 'M4 5h16v14H4zM4 16l5-5 4 4 3-3 4 4M15 9h.01'
X = 'M7 7l10 10M17 7L7 17'
KINDS = ['Ошибка', 'Идея', 'Дизайн', 'Формат чека', 'Другое']


def topbar(t, title):
    return ('<div style="margin:8px 32px;height:56px;border-radius:999px;background:%s;display:flex;align-items:center;padding:0 4px">'
            '<span style="width:48px;display:flex;justify-content:center">%s</span><b style="font-size:22px">%s</b></div>' % (t['bar'], N.glyph(N.ARROW, t['ink'], 22), title))


def chip(t, text, on=False):
    st = ('background:%s;color:%s;font-weight:700;box-shadow:inset 0 0 0 2px %s' % (t['prim'], t['onp'], A.edge(t))) if on else 'background:%s' % t['bar']
    return '<span style="flex:none;height:36px;padding:0 16px;border-radius:999px;display:flex;align-items:center;font-size:14px;%s">%s</span>' % (st, text)


def chips(t, on):
    return '<div style="display:flex;gap:8px;margin:12px 16px 0;overflow:hidden">%s</div>' % ''.join(chip(t, k, k == on) for k in KINDS)


def field(t, label, text, lines=5):
    body = text or '<span style="color:%s">&nbsp;</span>' % t['muted']
    return ('<div style="margin:12px 16px 0;padding:10px 14px;border-radius:16px;background:%s;border:1px solid %s;min-height:%dpx">'
            '<div style="font-size:12px;color:%s">%s</div><div style="font-size:15px;margin-top:4px;line-height:1.35">%s</div></div>'
            '<div style="margin:4px 30px 0;font-size:12px;color:%s">%d / 500</div>' % (t['surf'], t['line'], lines * 22, t['muted'], label, body, t['muted'], len(text)))


def file_icon(t, path):
    return '<span style="width:40px;height:40px;flex:none;border-radius:50%%;background:%s;display:flex;align-items:center;justify-content:center">%s</span>' % (
        U.mix(t['page'], t['acc'], .18), N.glyph(path, t['acc'] if t['dark'] else '#5B1FA8', 20, 1.6))


def file_row(t, name, size, path=DOC, last=False):
    return ('<div style="display:flex;align-items:center;gap:12px;padding:8px 12px;%s">%s<div style="flex:1;min-width:0"><div style="font-size:15px;white-space:nowrap;overflow:hidden;text-overflow:ellipsis">%s</div>'
            '<div style="font-size:12px;color:%s">%s</div></div><span style="width:48px;height:48px;display:flex;align-items:center;justify-content:center">%s</span></div>' % (
                '' if last else 'border-bottom:1px solid %s;' % t['bar'], file_icon(t, path), name, t['muted'], size, N.glyph(X, t['muted'], 20, 1.8)))


def files_card(t, rows, link=True):
    head = ('<div style="display:flex;justify-content:space-between;align-items:center;padding:12px 14px 4px;font-size:12px;letter-spacing:.04em;color:%s">ФАЙЛЫ'
            '<span style="font-size:13px;letter-spacing:0;color:%s">%s</span></div>' % (t['muted'], t['acc'] if t['dark'] else '#5B1FA8', '+ Прикрепить' if link else ''))
    return '<div style="margin:12px 16px 0;border-radius:24px;background:%s;border:1px solid %s">%s%s</div>' % (t['surf'], t['line'], head, ''.join(rows))


def note(t, text):
    return '<div style="margin:12px 16px 0;font-size:12px;color:%s;line-height:1.4">%s</div>' % (t['muted'], text)


def send(t, extra=''):
    return ('<div style="position:absolute;left:16px;right:16px;bottom:24px;display:flex;gap:8px">%s<div style="flex:1;height:48px;border-radius:999px;background:%s;color:%s;'
            'font-weight:600;display:flex;align-items:center;justify-content:center;box-shadow:inset 0 0 0 1px %s">Отправить</div></div>' % (extra, t['prim'], t['onp'], A.edge(t)))


PROMPT = 'Какой это чек: магазин, бот или банк?'
TEXT = 'Чек из бота Teleshka в Telegram, PDF'
TECH = 'Приложим: версия 1.0 · Pixel 8 · Android 16. Файлы увидят только разработчики, после разбора их удаляют.'


def form_a(t, v):
    body = topbar(t, 'Обратная связь') + chips(t, 'Формат чека') + field(t, PROMPT, TEXT, 4)
    body += files_card(t, [file_row(t, 'teleshka-chek-4807.pdf', 'PDF · 17 КБ'), file_row(t, 'Screenshot_2026-11-20.png', 'Картинка · 412 КБ', IMG, True)])
    body += note(t, 'До 3 файлов, всего 3 МБ. Подойдут PDF, фото чека или скриншот.') + note(t, TECH)
    return U.phone(t, body + send(t), 844)


def form_b(t, v):
    clip = '<span style="width:48px;height:48px;flex:none;border-radius:50%%;background:%s;display:flex;align-items:center;justify-content:center">%s</span>' % (t['bar'], N.glyph(CLIP, t['ink'], 22, 1.7))
    pills = ''.join('<span style="flex:none;height:36px;padding:0 6px 0 12px;border-radius:999px;background:%s;display:flex;align-items:center;gap:6px;font-size:13px">%s<span>%s</span>%s</span>' % (
        t['bar'], N.glyph(p, t['muted'], 16, 1.6), n, N.glyph(X, t['muted'], 16, 1.8)) for n, p in [('teleshka-chek-4807.pdf', DOC), ('Screenshot.png', IMG)])
    strip = '<div style="position:absolute;left:16px;right:16px;bottom:84px;display:flex;gap:8px;overflow:hidden">%s</div>' % pills
    body = topbar(t, 'Обратная связь') + chips(t, 'Формат чека') + field(t, PROMPT, TEXT, 5) + note(t, TECH)
    return U.phone(t, body + strip + send(t, clip), 844)


def kind_empty(t, v):
    body = topbar(t, 'Обратная связь') + chips(t, 'Формат чека') + field(t, PROMPT, '', 4)
    empty = ('<div style="margin:12px 16px 0;padding:14px;border-radius:24px;background:%s;border:1px solid %s;display:flex;gap:12px;align-items:center">%s'
             '<div style="flex:1"><div style="font-size:15px">Приложите чек</div><div style="font-size:12px;color:%s;margin-top:2px">PDF, фото или скриншот — научим приложение его читать</div></div>'
             '<span style="height:40px;padding:0 16px;border-radius:999px;background:%s;display:flex;align-items:center;font-size:14px">Выбрать</span></div>' % (
                 t['surf'], t['line'], file_icon(t, CLIP), t['muted'], t['bar']))
    return U.phone(t, body + empty + note(t, TECH) + send(t), 844)


def sheet_bg(t):
    """The entry sheet behind, dimmed."""
    sheet = A.sheet(t, A.seg(t) + A.strip(t) + Q.empty_amount(t) + A.cats(t) + A.details(t), 770)
    return sheet + '<div style="position:absolute;inset:0;background:rgba(0,0,0,.45)"></div>'


def unread_dialog(t, v):
    card = ('<div style="position:absolute;left:20px;right:20px;top:240px;padding:22px;border-radius:28px;background:%s;border:1px solid %s">'
            '<div style="display:flex;justify-content:center;margin-bottom:12px">%s</div>'
            '<div style="font-size:18px;font-weight:700;text-align:center">Не удалось прочитать чек</div>'
            '<div style="font-size:14px;color:%s;margin-top:6px;text-align:center;line-height:1.4">Такой формат приложение пока не знает. Отправьте образец — научим. Запись можно внести вручную.</div>'
            '<div style="display:flex;flex-direction:column;gap:8px;margin-top:16px">'
            '<span style="height:48px;border-radius:999px;background:%s;color:%s;font-weight:700;display:flex;align-items:center;justify-content:center;box-shadow:inset 0 0 0 1px %s">Ввести вручную</span>'
            '<span style="height:48px;border-radius:999px;background:%s;display:flex;align-items:center;justify-content:center">Отправить образец</span></div></div>' % (
                t['page'], t['line'], file_icon(t, DOC).replace('40px', '56px'), t['muted'], t['prim'], t['onp'], A.edge(t), t['bar']))
    return U.phone(t, sheet_bg(t) + card, 844)


def unread_line(t, v):
    line = ('<div style="margin:0 16px 8px;display:flex;align-items:center;gap:8px;font-size:13px;color:%s">%s<span style="flex:1">Чек не прочитался</span>'
            '<span style="color:%s">Отправить образец</span></div>' % (t['muted'], N.glyph(DOC, t['muted'], 18, 1.6), t['acc'] if t['dark'] else '#5B1FA8'))
    return U.phone(t, A.sheet(t, A.seg(t) + A.strip(t) + line + Q.empty_amount(t) + A.cats(t) + A.details(t), 770), 844)


def shared_ok(t, v):
    line = ('<div style="margin:-2px 16px 8px;display:flex;align-items:center;gap:6px;font-size:13px;color:%s">%s<span>Из чека · Teleshka</span></div>' % (t['muted'], N.glyph(DOC, t['muted'], 16, 1.6)))
    return U.phone(t, A.sheet(t, A.seg(t) + A.strip(t) + Q.filled_amount(t, '82 400', 'ARS') + line + A.cats(t) + A.details(t), 770), 844)


BOARDS = [('RcFormA', form_a), ('RcFormB', form_b), ('RcKindEmpty', kind_empty), ('RcUnreadA', unread_dialog), ('RcUnreadB', unread_line), ('RcShared', shared_ok)]


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
