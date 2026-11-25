"""Generated boards: #561 — receipts shared into the app and feedback with files (owner polled 2026-11-20, all «А»).

Shared PDF (Teleshka) → the sheet with the amount, the day, «Из чека · Teleshka» and the items in «Заметка»; an unknown format →
«Не удалось прочитать чек» (main «Ввести вручную», «Отправить образец» opens feedback «Формат чека» with the file attached);
feedback: «Файлы» card with rows and «+ Прикрепить», «Приложите чек · Выбрать» while «Формат чека» has no file.
Owner's note: a photo of a receipt is not read — only a photo of its QR, so texts say «фото QR-кода». Imported by mockup_screens_feedback.py.
"""
from mockup_bricks import *  # noqa: F401,F403
from mockup_parts import footer, textarea
from mockup_screens_fb_290 import block, small_link, grid4, detail, refund_check, frame, cat_cell, all_cell
from mockup_screens_fb_qr526 import seg, amount
from mockup_screens_main import ACC, CATS, acc_chip, keypad

FEEDBACK = [
    (561, 'Чеки из «Поделиться» и файлы в отзыве · 2026-11-20',
     'PDF, фото QR-кода, ссылка или текст через «Поделиться» → шторка с суммой и днём, строка «Из чека · Teleshka», список покупок — в «Заметке». '
     'Формат не знаем — окно «Не удалось прочитать чек»: «Ввести вручную» или «Отправить образец» (отзыв «Формат чека» с файлом). '
     'В отзыве — блок «Файлы» (до 3, 3 МБ), без файла для «Формата чека» — «Приложите чек».'),
]

DOC = '<svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="%s" stroke-width="1.6"><path d="M7 3h7l5 5v13H7zM14 3v5h5M10 13h6M10 17h6"/></svg>'


def source_line():
    return ('<div style="display: flex; align-items: center; gap: 6px; margin-top: 6px; font-size: 12px; color: %s;">%s<span>Из чека · Teleshka</span></div>'
            % (MUTED, DOC % MUTED))


def sheet(note='—', on=False, amount_html=None):
    cats = [cat_cell(n, ic, col, False) for n, ic, col in CATS[:7]] + [all_cell('Все')]
    det = block('Детали', '<div style="display: grid; grid-template-columns: 1fr 1fr; gap: 6px;">%s</div>'
                % (detail('calendar', 'Дата', '14 нояб.' if on else 'Сегодня, 20 нояб.') + detail('note', 'Заметка', note, on)
                   + detail('spark', 'Теги', '—', False) + refund_check()))
    top = '<div style="display: flex; align-items: center;">%s</div>' % acc_chip(ACC[0])
    return frame(seg() + top + (amount_html or amount('0', 'ARS', MUTED2)) + block('Категория · частые', grid4(cats)) + det
                 + '<div style="flex-grow: 1;"></div>' + keypad())


def shared():
    return sheet('Teleshka · Сырники…', True, amount('82 400', 'ARS', extra=source_line()))


def unread():
    d = dialog('Не удалось прочитать чек', 'Такой формат приложение пока не знает. Пришлите образец — научимся.',
               [('Отправить образец', 'text'), ('Ввести вручную', 'primary')])
    return sheet() + '<div style="position: absolute; inset: 0; background: rgba(0,0,0,0.5);"></div>' + d


KINDS = ['Ошибка', 'Идея', 'Дизайн', 'Формат чека', 'Другое']


def kinds(on='Формат чека'):
    return ('<div style="display: flex; gap: 8px; overflow: hidden;">%s</div>'
            % ''.join(chip(k, k == on).replace('height: 24px', 'height: 36px') for k in KINDS))


def file_row(name, sub, last=False):
    lead = ('<span style="width: 40px; height: 40px; border-radius: 50%%; background: %s; display: flex; align-items: center; justify-content: center;">%s</span>'
            % (BAR, DOC.replace('16', '20') % VIOLET))
    return row(name, sub, lead, icon_button('close', 'Убрать файл', MUTED))


def files_card(rows):
    head = ('<div style="display: flex; justify-content: space-between; align-items: center; padding: 12px 14px 4px; font-size: 11px; font-weight: 600; '
            'letter-spacing: 0.06em; color: %s;">ФАЙЛЫ%s</div>' % (MUTED, small_link('+ Прикрепить')))
    return '<div style="margin-top: 12px;">%s</div>' % card(head + ''.join(rows))


def hint(text):
    return '<div style="margin-top: 8px; font-size: 12px; color: %s;">%s</div>' % (MUTED, text)


HINT = 'До 3 файлов, всего 3 МБ: PDF, фото QR-кода, скриншот или текст. Файлы увидят только разработчики.'


def form():
    body = (kinds() + textarea('Какой это чек: магазин, бот или банк?', 36, 'Чек из бота Teleshka в Telegram, PDF')
            + files_card([file_row('teleshka-chek-4807.pdf', 'PDF · 17 КБ'), file_row('Screenshot_QR.png', 'Картинка · 412 КБ', True)]) + hint(HINT))
    return inner_screen('Обратная связь', body) + footer(button('Отправить', 'primary', 'upload', 0))


def kind_empty():
    empty = card(row('Приложите чек', 'PDF, фото QR-кода или текст со ссылкой — научим приложение его читать',
                     '<span style="width: 40px; height: 40px; border-radius: 50%%; background: %s; display: flex; align-items: center; justify-content: center;">%s</span>' % (BAR, svg('upload', VIOLET, 20)),
                     small_link('Выбрать')), extra='margin-top: 12px;')
    body = kinds() + textarea('Какой это чек: магазин, бот или банк?', 0) + empty + hint(HINT)
    return inner_screen('Обратная связь', body) + footer(button('Отправить', 'primary', 'upload', 0))


BOARDS = [  # name, title, fn, issue number
    ('Rc561Shared', '#561 · «Поделиться» PDF: сумма, день, «Из чека · Teleshka», покупки в заметке', shared, 561),
    ('Rc561Unread', '#561 · «Не удалось прочитать чек»', unread, 561),
    ('Rc561Form', '#561 · отзыв «Формат чека» с файлами', form, 561),
    ('Rc561KindEmpty', '#561 · «Формат чека» без файла: «Приложите чек»', kind_empty, 561),
]
