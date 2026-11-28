"""«Помощь → Жесты» (#797) and the balance forecast on «Отчёты» (#770) — poll 2026-11-27, both «А».

- `Gest797` — the cheat sheet of every gesture, by screen; `Forecast770` — the card with the 60-day line;
  `Forecast770Low` — the same when the money runs out (the line goes under the zero line, words in the attention colour).
Imported by mockup_screens_forms.py. The poll boards lived in mockup_poll_gest797.py.
"""
from mockup_parts import *  # noqa: F401,F403

GESTURES = [
    ('«Обзор»', [('Нажмите на карточку', 'перевернуть: на обороте самое полезное за месяц'), ('Удерживайте карточку', 'переставить или скрыть')]),
    ('«+»', [('Нажмите', 'новая операция'), ('Удерживайте', 'веером: QR чека, заметка, шаблон')]),
    ('«Операции»', [('Нажмите на операцию', 'поправить'), ('Удерживайте операцию', 'выбрать несколько'), ('Удерживайте сумму', 'увидеть её в других валютах')]),
    ('«Отчёты»', [('Удерживайте карточку', 'поменять порядок или убрать лишнее')]),
    ('Истории', [('Нажмите слева', 'вернуться к прошлой истории'), ('Нажмите справа', 'дальше')]),
    ('Везде', [('Листайте вбок', 'сменить вкладку'), ('Встряхните телефон', 'отменить только что сохранённую операцию'),
               ('Удерживайте значок приложения', 'расход или доход сразу')]),
]


def gesture_row(title, text, first):
    line = '' if first else 'border-top: 1px solid %s;' % LINE
    return ('<div style="padding: 10px 16px; %s"><div style="font-size: 15px; font-weight: 600;">%s</div>'
            '<div style="font-size: 13px; color: %s;">%s</div></div>' % (line, title, MUTED, text))


def gestures():
    blocks = ''
    for head, items in GESTURES:
        blocks += label(head, True, 14) + card(''.join(gesture_row(a, b, i == 0) for i, (a, b) in enumerate(items)), extra='margin-top: 6px;')
    return board('Помощь → Жесты', inner_screen('Жесты', blocks))


def chart(below):
    """The balance: the last 14 days solid, 60 days ahead dashed; the dotted line is zero; the ring marks the lowest point."""
    zero, w = 96, 310
    past = [64, 62, 66, 60, 58, 61, 56, 54, 57, 52, 50, 53, 48, 46]
    fut = [46, 52, 56, 62, 68, 72, 78, 74, 70, 64, 58, 56, 52, 50, 48, 46, 44, 42, 40, 38, 36]
    if below:
        fut = [46, 54, 62, 72, 84, 92, 100, 108, 104, 98, 90, 84, 78, 72, 66, 60, 54, 50, 46, 42, 40]
    step = w / (len(past) + len(fut) - 1)
    pts = lambda arr, off: ' '.join('%.1f,%d' % ((off + i) * step, v) for i, v in enumerate(arr))  # noqa: E731
    low = max(range(len(fut)), key=lambda i: fut[i])
    mark = WARN if below else VIOLET
    return ('<svg width="%d" height="130" style="display: block; margin-top: 8px;"><line x1="0" y1="%d" x2="%d" y2="%d" stroke="%s" stroke-width="1" stroke-dasharray="2 4"/>'
            '<polyline points="%s" fill="none" stroke="%s" stroke-width="2.4" stroke-linecap="round" stroke-linejoin="round"/>'
            '<polyline points="%s" fill="none" stroke="%s" stroke-width="2.4" stroke-dasharray="5 5" stroke-linecap="round"/>'
            '<line x1="%.1f" y1="0" x2="%.1f" y2="130" stroke="%s" stroke-width="1" stroke-dasharray="1 3"/>'
            '<circle cx="%.1f" cy="%d" r="5" fill="%s"/></svg>'
            % (w, zero, w, zero, MUTED, pts(past, 0), LIME, pts([past[-1]] + fut, len(past) - 1), mark, len(past) * step, len(past) * step, MUTED,
               (len(past) + low) * step, fut[low], mark))


def forecast(below):
    verdict = ('<div style="font-size: 13px; color: %s; margin-top: 2px;">Не хватит с 3 декабря · дальше −40 USD</div>' % WARN if below else
               '<div style="font-size: 13px; color: %s; margin-top: 2px;">Самая низкая точка: 12 декабря, ≈ 80 USD</div>' % MUTED)
    axis = ('<div style="display: flex; justify-content: space-between; font-size: 11px; color: %s;"><span>14 ноября</span><span>сегодня</span><span>26 января</span></div>' % MUTED)
    body = ('<div style="font-size: 16px; font-weight: 600;">Прогноз остатка</div><div style="font-size: 12px; color: %s; margin-top: 6px;">сплошная — было, пунктир — ожидаем</div>' % MUTED
            + verdict + chart(below) + axis + '<div style="margin-top: 10px;">%s</div>' % segmented(['30 дней', '60 дней'], '30 дней'))
    period = ('<div style="margin: 4px 0 0; display: flex; justify-content: space-between; align-items: center; font-size: 16px; font-weight: 600;">'
              '<span style="color: %s;">‹</span><span>Ноябрь 2026 · USD</span><span style="color: %s;">›</span></div>' % (MUTED, MUTED))
    return board('Отчёты: прогноз остатка', tab_screen('Отчёты', period + card(body, pad='16px', extra='margin-top: 12px;'), 'chart'))


BOARDS = [('Gest797', gestures), ('Forecast770', lambda: forecast(False)), ('Forecast770Low', lambda: forecast(True))]
