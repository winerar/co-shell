"""Generated boards: tails of the header poll (#394, after «Живая строка» А was chosen 2026-11-15).

1. «Операции» search: keep the search field above the chips (#141, А) or fold it into a loupe in the header (Б).
2. Inner screens: a hairline under the header once scrolled, like tabs (Б), or as now (А).
Imported by mockup_screens_feedback.py (FEEDBACK + BOARDS).
"""
from mockup_bricks import *  # noqa: F401,F403
from mockup_screens_main import day, tx
from mockup_screens_more import rate_rows

FEEDBACK = [
    (394, 'Задача #394 · шапки: поиск лупой? линия у внутренних экранов?',
     'Слева — поиск в «Операциях»: А строка как сейчас, Б лупа в шапке (+ она же раскрытая). Справа — «Курсы» после прокрутки: '
     'А без линии, Б с линией, как у вкладок. Рекомендую Б и Б: список на одну строку длиннее, все шапки ведут себя одинаково.'),
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


def search_a():
    """А (now, #141): the search field under the title, chips under it."""
    body = search('Найти: заметка, тег, сумма') + '<div style="margin-top: 12px;">%s</div>' % chips_row() + ops_rows()
    return tab_screen('Операции', body, 'ops')


def search_b():
    """Б: a loupe in the header; the chips stay — the list starts one line (≈ 60 px) higher."""
    return tab_screen('Операции', chips_row() + ops_rows(), 'ops', actions=icon_button('search', 'Поиск'))


def search_b_open():
    """Б, the loupe tapped: the field opens in the header itself, the list filters as you type; ✕ closes it."""
    field_ = ('<div style="display: flex; align-items: center; gap: 4px; height: 64px; padding: 0 8px 0 16px;">'
              '<label style="flex-grow: 1; display: flex; align-items: center; gap: 10px; height: 48px; padding: 0 16px; border-radius: 999px; background: %s;">%s'
              '<span style="font-size: 15px;">кафе</span><span style="width: 2px; height: 20px; background: %s;"></span></label>%s</div>'
              % (FIELD, svg('search', MUTED, 20), LIME, icon_button('close', 'Закрыть поиск')))
    return field_ + '<div style="padding: 0 16px;">%s%s</div>' % (chips_row(), ops_rows(only='Кафе')) + nav('ops')


RATES = [(c, r, 'на 15 нояб.') for c, r in [('ARS', '0,000977'), ('AMD', '0,00258'), ('RUB', '0,0123'), ('USDT', '1,0004'), ('EUR', '1,0820'), ('GEL', '0,3690'),
                                             ('TRY', '0,0291'), ('BRL', '0,1735'), ('CLP', '0,00106'), ('UYU', '0,0241'), ('KZT', '0,00201'), ('GBP', '1,2690'),
                                             ('CNY', '0,1381'), ('AED', '0,2723'), ('ILS', '0,2688'), ('PLN', '0,2501')]]


def rates_scrolled(line):
    """«Курсы валют» scrolled to the middle of 16 currencies; [line] — the hairline under the header (Б)."""
    border = 'border-bottom: 1px solid %s; box-sizing: border-box;' % LINE if line else ''
    top = ('<div style="display: flex; align-items: center; gap: 4px; height: 64px; padding: 0 8px; background: %s; position: relative; z-index: 2; %s">%s'
           '<h1 style="margin: 0; font-size: 22px; font-weight: 700; flex-grow: 1;">Курсы валют</h1>%s</div>'
           % (BG, border, icon_button('back', 'Назад', INK), icon_button('refresh', 'Обновить')))
    # The list continues under the header: the first visible row is cut, as on a phone mid-scroll.
    return top + '<div style="padding: 0 16px; margin-top: -22px;">%s</div>' % rate_rows(RATES[4:])


BOARDS = [  # name, title, fn, issue
    ('Hdr394SearchA', '#394 · поиск А: строка над фильтрами (как сейчас)', search_a, 394),
    ('Hdr394SearchB', '#394 · поиск Б: лупа в шапке', search_b, 394),
    ('Hdr394SearchBOpen', '#394 · поиск Б: лупа нажата — поле в шапке', search_b_open, 394),
    ('Hdr394LineA', '#394 · внутренний экран А: без линии', lambda: rates_scrolled(False), 394),
    ('Hdr394LineB', '#394 · внутренний экран Б: линия при прокрутке', lambda: rates_scrolled(True), 394),
]
