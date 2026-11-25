"""Generated boards: #526 — scanning the QR of an Argentine receipt (owner polled 2026-11-19, all «рекомендую»).

Round camera button next to «Шаблоны» in the entry sheet, a quiet «Из QR чека» line, the receipt in pesos on a card in another currency,
the «Этот чек уже записан» window, the scanner (frame, flashlight, «Ввести вручную»), «Это не QR чека», no camera access.
Owner's note: the code holds a link, so no «читается без интернета» line on the scanner. Imported by mockup_screens_feedback.py.
"""
from mockup_bricks import *  # noqa: F401,F403
from mockup_screens_main import ACC, CATS, acc_chip, keypad
from mockup_screens_fb_290 import block, small_link, grid4, detail, refund_check, frame, cat_cell, all_cell

FEEDBACK = [
    (526, 'Сканирование QR чека: сумма и день подставляются в шторку · 2026-11-19',
     'Круглая камера рядом с «Шаблоны» (шторка не растёт), после скана — тихая строка «Из QR чека · продавец …». Счёт в другой валюте: сумма чека уходит в '
     '«Сумму покупки», списанное считается по курсу. Повтор чека — окно «Этот чек уже записан». Сканер: рамка, фонарик, «Ввести вручную» (туда же ведёт '
     'встряхивание). Камеру спрашиваем при первом открытии сканера.'),
]

QR = ('<svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="%s" stroke-width="1.7" stroke-linecap="round"><path d="M4 8V5a1 1 0 0 1 1-1h3M16 4h3a1 1 0 0 1 1 1v3M20 16v3a1 1 0 0 1-1 1h-3M8 20H5a1 1 0 0 1-1-1v-3"/>'
      '<rect x="8" y="8" width="3" height="3"/><rect x="13" y="8" width="3" height="3"/><rect x="8" y="13" width="3" height="3"/><path d="M13 13h3v3"/></svg>')
SELLER = '30-71234567-8'
# «Вставить из буфера» (#321): a round icon before the camera, a new operation that is not a transfer.
CLIP = ('<svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="%s" stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round">'
        '<path d="M9 4h6v3H9zM7 6H6v14h12V6h-1M9 12h6M9 16h4"/></svg>')


def round_btn(inner):
    return '<span style="width: 48px; height: 48px; border-radius: 50%%; background: %s; display: flex; align-items: center; justify-content: center; flex: none;">%s</span>' % (BAR, inner)


def seg():
    """Type of the operation (the pill the app has) + paste + the camera + the templates button."""
    pill = ('<span style="flex-grow: 1; height: 48px; border-radius: 999px; background: %s; display: flex; align-items: center; padding: 4px; gap: 4px;">'
            '<span style="flex: 1; height: 40px; border-radius: 999px; background: %s; color: %s; font-weight: 700; font-size: 14px; display: flex; align-items: center; justify-content: center;">%s</span>'
            '<span style="flex: 1; display: flex; justify-content: center;">%s</span><span style="flex: 1; display: flex; justify-content: center;">%s</span></span>'
            % (BAR, LIME, ONLIME, svg('out', ONLIME, 20), svg('in', MUTED, 20), svg('swaph', MUTED, 20)))
    return ('<div style="display: flex; align-items: center; gap: 8px;">%s%s%s%s</div>'
            % (pill, round_btn(CLIP % MUTED), round_btn(QR % LIME), round_btn(svg('list', MUTED, 22))))


def amount(value, cur, color=PINK, extra=''):
    big = ('<div style="display: flex; align-items: baseline; justify-content: center; gap: 8px;"><span style="%s font-size: 40px; font-weight: 700; color: %s;">%s</span>'
           '<span style="%s font-size: 16px; color: %s;">%s</span></div>' % (MONO, color, value, MONO, MUTED, cur))
    return block('Сумма', big + extra)


def source_line():
    return ('<div style="display: flex; align-items: center; gap: 6px; margin-top: 6px; font-size: 12px; color: %s;">%s<span>Из QR чека · продавец %s</span></div>'
            % (MUTED, QR % MUTED, SELLER))


def purchase_line(buy, rate):
    return ('<div style="display: flex; align-items: center; gap: 8px; margin-top: 6px; padding-top: 8px; border-top: 1px dashed %s; font-size: 12px; color: %s;">'
            '<span>Сумма покупки</span><span style="%s font-size: 15px; color: %s; font-weight: 700;">%s ARS</span><span style="flex-grow: 1;"></span><span>%s</span></div>'
            % (LINE2, MUTED, MONO, INK, buy, rate))


def sheet_body(amount_html, day='Сегодня, 19 нояб.', acc=3, cat=None):
    cats = [cat_cell(n, ic, col, n == cat) for n, ic, col in CATS[:7]] + [all_cell('Все')]
    cat_b = block('Категория · частые', grid4(cats), right=small_link(cat + ' ✓') if cat else '')
    det = block('Детали', '<div style="display: grid; grid-template-columns: 1fr 1fr; gap: 6px;">%s</div>'
                % (detail('calendar', 'Дата', day) + detail('note', 'Заметка', '—', False) + detail('spark', 'Теги', '—', False) + refund_check()))
    accs = acc_chip(ACC[acc])
    top = '<div style="display: flex; align-items: center;">%s</div>' % accs
    return frame(seg() + top + amount_html + cat_b + det + '<div style="flex-grow: 1;"></div>' + keypad())


def btn():
    return sheet_body(amount('0', 'USDT', MUTED2))


def filled():
    return sheet_body(amount('37 914', 'ARS', extra=source_line()), day='18 нояб.', acc=0)


def foreign():
    return sheet_body(amount('32,13', 'USDT', extra=purchase_line('37 914', '1 USDT = 1 180 ARS') + source_line()), day='18 нояб.')


def dup():
    d = dialog('Этот чек уже записан', 'В операциях есть такой же: Продукты, 18 нояб., 37 914 ARS.', [('Отмена', 'text'), ('Всё равно записать', 'primary')])
    return sheet_body(amount('37 914', 'ARS', extra=source_line()), day='18 нояб.', acc=0) + '<div style="position: absolute; inset: 0; background: rgba(0,0,0,0.5);"></div>' + d


# ---- scanner -----------------------------------------------------------------------------------------------------------

def camera(bottom, over=''):
    frame_svg = ('<div style="position: absolute; left: 55px; top: 250px; width: 280px; height: 280px;"><svg viewBox="0 0 280 280" width="280" height="280" fill="none" stroke="%s" stroke-width="5" stroke-linecap="round">'
                 '<path d="M4 60V24a20 20 0 0 1 20-20h36M220 4h36a20 20 0 0 1 20 20v36M276 220v36a20 20 0 0 1-20 20h-36M60 276H24a20 20 0 0 1-20-20v-36"/></svg></div>' % LIME)
    top = ('<div style="position: absolute; left: 32px; right: 32px; top: 44px; height: 56px; border-radius: 999px; background: %s; display: flex; align-items: center; gap: 8px; padding: 0 8px;">'
           '%s<span style="font-size: 22px; font-weight: 700;">Сканировать QR</span></div>' % (BAR, icon_button('back', 'Назад')))
    return ('<div style="position: absolute; inset: 0; background: linear-gradient(160deg, #2a2f2b, #0c0f0d 70%%);"></div>' + top + frame_svg + bottom + over)


def scan_bottom():
    hint = ('<div style="position: absolute; left: 16px; right: 16px; bottom: 124px; padding: 16px; border-radius: 24px; background: %s; border: 1px solid %s; '
            'text-align: center; font-size: 15px;">Наведите камеру на QR-код на чеке.</div>' % (CARD, LINE))
    row_ = ('<div style="position: absolute; left: 16px; right: 16px; bottom: 44px; display: flex; gap: 8px; align-items: center;">%s'
            '<span style="flex: 1; height: 48px; border-radius: 999px; background: %s; color: %s; font-weight: 700; font-size: 15px; display: flex; align-items: center; justify-content: center;">Ввести вручную</span></div>'
            % (round_btn(svg('bolt', INK, 22)), LIME, ONLIME))
    return hint + row_


def scan():
    return camera(scan_bottom())


def bad():
    pill = ('<div style="position: absolute; left: 24px; right: 24px; bottom: 196px; min-height: 48px; padding: 0 8px 0 6px; border-radius: 999px; background: %s; '
            'border: 1px solid %s; display: flex; align-items: center; gap: 8px;"><span style="width: 36px; height: 36px; border-radius: 50%%; background: %s; display: flex; align-items: center; justify-content: center;">%s</span>'
            '<span style="flex-grow: 1; font-size: 14px;">Это не QR чека. Попробуйте другой.</span><span style="color: %s;">✕</span></div>' % (CHIP, LIME, LIME, QR % ONLIME, MUTED))
    return camera(scan_bottom(), pill)


def nocam():
    d = dialog('Камера нужна, чтобы прочитать QR', 'Видео не сохраняется и никуда не уходит.', [('Ввести вручную', 'text'), ('Разрешить', 'primary')])
    return camera('', '<div style="position: absolute; inset: 0; background: rgba(0,0,0,0.5);"></div>' + d)


BOARDS = [  # name, title, fn, issue number
    ('Qr526Btn', '#526 · шторка ввода: круглая камера рядом с «Шаблоны»', btn, 526),
    ('Qr526Filled', '#526 · после скана: сумма, день и строка «Из QR чека»', filled, 526),
    ('Qr526Foreign', '#526 · счёт в другой валюте: «Сумма покупки» в песо', foreign, 526),
    ('Qr526Dup', '#526 · «Этот чек уже записан»', dup, 526),
    ('Qr526Scan', '#526 · сканер: рамка, фонарик, «Ввести вручную»', scan, 526),
    ('Qr526Bad', '#526 · сканер: «Это не QR чека»', bad, 526),
    ('Qr526NoCam', '#526 · нет доступа к камере', nocam, 526),
]
