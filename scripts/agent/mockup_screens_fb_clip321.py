"""Generated boards: #321 — an amount from the clipboard in the entry sheet (owner polled 2026-11-23, variant «А»).

A quiet «Вставить из буфера» row above the accounts while the amount is empty; the clipboard is read only on the tap (Android 12+
shows its own «получил доступ к буферу» notice at every read). After the tap: the amount, its currency and the rest of the text as the note;
no amount in the text — a short pill under the row. Imported by mockup_screens_feedback.py.
"""
from mockup_bricks import *  # noqa: F401,F403
from mockup_screens_main import ACC, CATS, acc_chip, keypad
from mockup_screens_fb_290 import block, small_link, grid4, detail, refund_check, frame, cat_cell, all_cell
from mockup_screens_fb_qr526 import seg, amount

FEEDBACK = [
    (321, 'Сумма из буфера обмена: строка «Вставить из буфера» в шторке ввода · 2026-11-23',
     'Строка над счетами, пока сумма пустая. Буфер читается только по нажатию: Android 12+ показывает своё уведомление при каждом чтении. '
     'После нажатия — сумма с валютой и остаток текста в «Заметку»; чек известного формата читается как «Поделиться». Нет суммы — плашка «В буфере нет суммы».'),
]

CLIP = ('<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="%s" stroke-width="1.7" stroke-linecap="round" stroke-linejoin="round">'
        '<path d="M9 4h6v3H9zM7 6H6v14h12V6h-1M9 12h6M9 16h4"/></svg>')


def paste_row():
    return ('<div style="height: 48px; border-radius: 999px; border: 1px solid %s; display: flex; align-items: center; justify-content: center; gap: 8px; font-size: 14px;">'
            '%s<span>Вставить из буфера</span></div>' % (LINE, CLIP % INK))


def body(amount_html, row='', note='—', note_on=False, cats_pick=None, over=''):
    cats = [cat_cell(n, ic, col, n == cats_pick) for n, ic, col in CATS[:7]] + [all_cell('Все')]
    cat_b = block('Категория · частые', grid4(cats))
    det = block('Детали', '<div style="display: grid; grid-template-columns: 1fr 1fr; gap: 6px;">%s</div>'
                % (detail('calendar', 'Дата', 'Сегодня') + detail('note', 'Заметка', note, note_on) + detail('spark', 'Теги', '—', False) + refund_check()))
    top = '<div style="display: flex; align-items: center;">%s</div>' % acc_chip(ACC[0])
    return frame(seg() + row + top + amount_html + cat_b + det + '<div style="flex-grow: 1;"></div>' + keypad()) + over


def empty():
    return body(amount('0', 'ARS', MUTED2), paste_row())


def filled():
    return body(amount('12 500', 'ARS'), note='Compra COTO', note_on=True)


def nothing():
    pill = ('<div style="margin-top: 4px; min-height: 48px; padding: 0 8px 0 6px; border-radius: 999px; background: %s; '
            'border: 1px solid %s; display: flex; align-items: center; gap: 8px;"><span style="width: 36px; height: 36px; border-radius: 50%%; background: %s; display: flex; align-items: center; justify-content: center;">%s</span>'
            '<span style="flex-grow: 1; font-size: 14px;">В буфере нет суммы. Скопируйте текст с ней.</span><span style="color: %s;">✕</span></div>' % (CHIP, LIME, LIME, CLIP % ONLIME, MUTED))
    return body(amount('0', 'ARS', MUTED2), paste_row() + pill)


BOARDS = [  # name, title, fn, issue number
    ('Clip321Row', '#321 · шторка ввода: строка «Вставить из буфера»', empty, 321),
    ('Clip321Filled', '#321 · после нажатия: сумма и заметка из текста', filled, 321),
    ('Clip321None', '#321 · в буфере нет суммы', nothing, 321),
]
