"""Generated boards: tails of the header poll (#394, after «Живая строка» А was chosen 2026-11-15).

Chosen Б and Б (2026-11-15): «Операции» search folds into a loupe in the header; inner screens draw a hairline
under the header once scrolled, like tabs. The А boards are removed.
Imported by mockup_screens_feedback.py (FEEDBACK + BOARDS).
"""
from mockup_bricks import *  # noqa: F401,F403
from mockup_screens_main import day, loupe, tx
from mockup_screens_more import rate_rows

FEEDBACK = [
    (394, 'Задача #394 · шапки: поиск лупой? линия у внутренних экранов?',
     'Выбрано Б и Б (2026-11-15): поиск — лупа в шапке, поле раскрывается на месте заголовка; у внутренних экранов при прокрутке '
     'под шапкой линия, как у вкладок. Сделано в коде.'),
]

NAMES = [('Продукты', 'Galicia · Coto', 'cart', LIME), ('Кафе', 'USDT-карта', 'cafe', WARN), ('Такси', 'Наличные', 'taxi', YELLOW), ('Аптека', 'Galicia', 'pill', PINK)]
DAYS = ['СЕГОДНЯ', 'ВЧЕРА', '13 НОЯБРЯ, ПТ', '12 НОЯБРЯ, ЧТ', '11 НОЯБРЯ, СР']


def ops_rows(days=DAYS, only=None):
    out = []
    for d, title in enumerate(days):
        rows = [(t, s, ic, col, r) for r, (t, s, ic, col) in enumerate(NAMES) if only is None or t == only]
        if not rows:
            continue
        out.append(day(title, '−%d 000' % (12 + d * 5), 'ARS'))
        out += [tx(t, s, '−%d 250' % (3 + d * 4 + r), 'ARS', 'e', ic, col, '') for t, s, ic, col, r in rows]
    return '<div style="margin: 14px -16px 0;">%s</div>' % ''.join(out)


def chips_row():
    return '<div style="display: flex; gap: 8px; margin-top: 4px;">%s</div>' % ''.join([chip('Тип ▾'), chip('Категория ▾'), chip('Период ▾')])


def search_b():
    """Б: a loupe in the header; the chips stay — the list starts one line (≈ 60 px) higher."""
    return tab_screen('Операции', chips_row() + ops_rows(), 'ops', actions=loupe())


def search_b_open():
    """Б, the loupe tapped: the field opens in the header itself, the list filters as you type; ✕ closes it."""
    return search_head('кафе') + '<div style="padding: 0 16px;">%s%s</div>' % (chips_row(), ops_rows(only='Кафе')) + nav('ops')


RATES = [(c, r, 'на 15 нояб.') for c, r in [('ARS', '0,000977'), ('AMD', '0,00258'), ('RUB', '0,0123'), ('USDT', '1,0004'), ('EUR', '1,0820'), ('GEL', '0,3690'),
                                             ('TRY', '0,0291'), ('BRL', '0,1735'), ('CLP', '0,00106'), ('UYU', '0,0241'), ('KZT', '0,00201'), ('GBP', '1,2690'),
                                             ('CNY', '0,1381'), ('AED', '0,2723'), ('ILS', '0,2688'), ('PLN', '0,2501')]]


def rates_scrolled():
    """«Курсы валют» scrolled to the middle of 16 currencies: the hairline under the header (#394 Б)."""
    # The list continues under the header: the first visible row is cut, as on a phone mid-scroll.
    return inner_head('Курсы валют', icon_button('refresh', 'Обновить'), scrolled=True) + '<div style="padding: 0 16px; margin-top: -22px;">%s</div>' % rate_rows(RATES[4:])


BOARDS = [  # name, title, fn, issue
    ('Hdr394SearchB', '#394 · поиск Б: лупа в шапке', search_b, 394),
    ('Hdr394SearchBOpen', '#394 · поиск Б: лупа нажата — поле в шапке', search_b_open, 394),
    ('Hdr394LineB', '#394 · внутренний экран Б: линия при прокрутке', rates_scrolled, 394),
]
