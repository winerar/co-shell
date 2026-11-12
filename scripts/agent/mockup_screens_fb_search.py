"""Generated boards: variants for #141 (search and filters on «Операции», stage 6в).

Imported by mockup_screens_feedback.py (FEEDBACK + BOARDS). Long lists on purpose (owner 2026-11-11): 30 categories
in 6 folders, 12 accounts. 4А (filter total under the chips) is already picked (#160) — every variant shows it.
"""
from mockup_bricks import *  # noqa: F401,F403
from mockup_screens_fb_cats import FOLDERS, LOOSE, circle, square

FEEDBACK = [
    (141, 'Задача #141 · поиск и фильтры операций (6в)',
     'Общее для всех вариантов: поиск ищет в заметке, получателе, метках, категории, счёте и сумме; сумма — «=4500», «>10000», «1000-5000» '
     '(в любой валюте, по сумме счёта и по чеку), под полем строка «как понял». Фильтры: период (готовые + свой), тип, счёт, категория/папка, кто внёс. '
     'Итог под фильтрами — 4А (уже выбран), итоги дней — по фильтру. Предлагаю А: у каждого чипа своя шторка с поиском — '
     'длинные списки (30 категорий, 12 счетов) не мешают, а чипы уже есть в приложении. Б — всё в одной шторке «Фильтры» (больше прокрутки); '
     'В — фильтры из поиска подсказками (быстро, но надо знать, что печатать).'),
]

OPS = [  # title, sub (account · payee · labels), amount, cur, kind, icon, colour, second line
    ('Кафе', 'Galicia · Café Martínez', '−8 400', 'ARS', 'e', 'cafe', WARN, ''),
    ('Продукты', 'USDT-карта · Coto · Аня', '−41,20', 'USDT', 'e', 'cart', LIME, 'чек 41 900 ARS'),
    ('Пекарня', 'Наличные · La Panera', '−3 200', 'ARS', 'e', 'cart', LIME, ''),
    ('Доставка', 'Mercado Pago · PedidosYa', '−12 600', 'ARS', 'e', 'cart', LIME, ''),
    ('Кафе', 'USDT-карта · Аня · #отпуск', '−13,10', 'USDT', 'e', 'cafe', WARN, 'чек 13 300 ARS'),
    ('Продукты', 'Galicia · Día', '−27 800', 'ARS', 'e', 'cart', LIME, ''),
]


def chips(*c):
    return '<div style="display: flex; gap: 8px; margin-top: 12px; overflow: hidden;">%s</div>' % ''.join(c)


def total(count, amt, cur, approx):
    return ('<div style="margin-top: 12px; display: flex; align-items: baseline; justify-content: space-between; padding: 12px 14px; border-radius: 16px; '
            'background: %s; border: 1px solid %s;"><span style="font-size: 13px; color: %s;">%s</span><span style="display: flex; flex-direction: column; '
            'align-items: flex-end;">%s<span style="%s font-size: 12px; color: %s;">%s</span></span></div>' % (CARD, LINE, MUTED, count, amount(amt, cur, PINK, 18), MONO, MUTED, approx))


def hint(text):
    return ('<div style="margin: 8px 6px 0; display: flex; align-items: center; gap: 6px; font-size: 12px; color: %s;">%s%s</div>'
            % (LIME, svg('spark', LIME, 14), text))


def ops_list(days):
    out = ''.join(day(t, s, 'ARS') + ''.join(tx(*OPS[i]) for i in idx) for t, s, idx in days)
    return '<div style="margin: 6px -16px 0;">%s</div>' % out


DAYS = [('28 ОКТЯБРЯ', '−50 400', (0, 1)), ('27 ОКТЯБРЯ', '−15 800', (2, 3)), ('25 ОКТЯБРЯ', '−41 100', (4, 5))]


def list_a():
    head = search('Поиск: заметка, получатель, сумма') + chips(chip('Расходы ✕', True), chip('Еда · 3 ✕', True), chip('Октябрь ✕', True), chip('Аня ✕', True), chip('Счёт ▾'))
    return tab_screen('Операции', head + total('23 операции', '−184 300', 'ARS', '≈ 182,40 USD · из них 54,30 USDT') + ops_list(DAYS), 'ops')


def check(state):  # 'on' | 'off' | 'some'
    if state == 'off':
        return '<span style="width: 22px; height: 22px; flex: none; border-radius: 7px; border: 2px solid %s; box-sizing: border-box;"></span>' % LINE2
    mark = svg('check', ONLIME, 16, 2.6) if state == 'on' else '<span style="width: 10px; height: 2px; background: %s;"></span>' % ONLIME
    return ('<span style="width: 22px; height: 22px; flex: none; border-radius: 7px; background: %s; display: flex; align-items: center; '
            'justify-content: center;">%s</span>' % (LIME, mark))


def sheet_head(title, action='Сбросить'):
    return ('<div style="display: flex; align-items: center; padding: 8px 12px 0 20px;"><h2 style="margin: 0; flex-grow: 1; font-size: 20px;">%s</h2>'
            '<span style="padding: 10px 8px; color: %s; font-size: 14px; font-weight: 600;">%s</span></div>' % (title, LIME, action))


def sheet_button(text):
    return ('<div style="position: absolute; left: 0; right: 0; bottom: 0; padding: 12px 16px 24px; background: %s; border-top: 1px solid %s;">'
            '%s</div>' % (SHEET, LINE, button(text, mt=0)))


def over_list(inner, top=120):
    return scrim(list_a()) + sheet(inner, top)


def cats_sheet_a():
    picked = {'Продукты', 'Пекарня', 'Доставка'}
    rows = []
    for name, icon, col, kids in FOLDERS:
        n = len([k for k in kids if k in picked])
        state = 'on' if n == len(kids) else ('some' if n else 'off')
        sub = '%d из %d' % (n, len(kids)) if n else '%d категорий' % len(kids)
        rows.append(row(name, sub, square(icon, col, len(kids), 36), check(state), min_h=56))
        if name == 'Еда':
            for k in kids:
                rows.append(row(k, '', '<span style="width: 20px;"></span>' + circle(icon, col, 30), check('on' if k in picked else 'off'), min_h=48))
    rows += [row(name, '', circle(icon, col, 36), check('off'), min_h=52) for name, icon, col in LOOSE]
    body = ('<div style="padding: 10px 16px 0;">%s<div style="margin-top: 10px; display: flex; gap: 8px;">%s</div>'
            '<div style="margin-top: 10px;">%s</div></div>' % (search('Поиск среди 30 категорий'), chip('Все расходы') + chip('Без категории'), group(rows)))
    return over_list(sheet_head('Категории') + body + sheet_button('Показать 23 операции'))


def period_sheet_a():
    presets = ['Этот месяц', 'Прошлый месяц', '3 месяца', 'Этот год', 'Прошлый год', 'Всё время']
    grid = '<div style="display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 8px;">%s</div>' % ''.join(
        '<span style="height: 44px; border-radius: 14px; display: flex; align-items: center; justify-content: center; font-size: 14px; %s">%s</span>'
        % ('background: %s; color: %s; font-weight: 600;' % (VIOLETPILL, VIOLETINK) if p == 'Прошлый месяц' else 'border: 1px solid %s;' % LINE2, p) for p in presets)
    step = ('<div style="margin-top: 14px; display: flex; align-items: center; justify-content: space-between; height: 52px; padding: 0 6px; border-radius: 16px; '
            'background: %s;">%s<span style="font-size: 16px; font-weight: 600;">Октябрь 2026</span>%s</div>' % (CHIP, icon_button('back', 'Раньше'), icon_button('chev', 'Позже')))
    own = (label('Свой период', False, 18) + '<div style="display: grid; grid-template-columns: 1fr 1fr; gap: 8px;">%s%s</div>'
           % (field('С', '1 окт. 2026'), field('По', '31 окт. 2026')))
    body = '<div style="padding: 12px 16px 0;">%s%s%s%s</div>' % (grid, step, own, note('Стрелки листают выбранный отрезок: месяц, квартал, год.'))
    return over_list(sheet_head('Период') + body + sheet_button('Показать 23 операции'), 300)


def amount_a():
    rows = [('Доставка', 'Mercado Pago · PedidosYa', '−4 500', 'ARS', 'e', 'cart', LIME, ''), ('Такси', 'Наличные', '−3 900', 'ARS', 'e', 'taxi', YELLOW, ''),
            ('Аптека', 'Galicia · Farmacity', '−4 750', 'ARS', 'e', 'pill', '#FF8FA3', ''), ('Кафе', 'USDT-карта · Аня', '−4,10', 'USDT', 'e', 'cafe', WARN, 'чек 4 200 ARS'),
            ('Связь', 'Ameria · Ucom', '−4 800', 'AMD', 'e', 'phone', '#6E8BFF', '')]
    tips = '<div style="display: flex; gap: 8px; margin-top: 10px;">%s</div>' % ''.join(
        '<span style="height: 30px; padding: 0 10px; border-radius: 8px; background: %s; %s font-size: 12px; color: %s; display: inline-flex; align-items: center;">%s</span>'
        % (CHIP, MONO, INK2, t) for t in ['=4500', '>10000', '<500', '3000-5000'])
    lst = '<div style="margin: 6px -16px 0;">%s%s%s</div>' % (day('СЕГОДНЯ', '−8 400', 'ARS'), tx(*rows[0]) + tx(*rows[1]), day('ВЧЕРА', '−4 750', 'ARS') + ''.join(tx(*r) for r in rows[2:]))
    head = search('', '3000-5000') + hint('Сумма от 3 000 до 5 000 — в любой валюте, по счёту и по чеку') + tips
    return tab_screen('Операции', head + total('38 операций', '−162 900', 'ARS', '≈ 161,20 USD · ещё 9 600 AMD') + lst, 'ops')


def accounts_sheet_a():
    accs = [('Galicia', 'GA', '#F26E22', '#1A0A00', 'ARS'), ('Mercado Pago', 'MP', '#00B1EA', '#002A3A', 'ARS'), ('Brubank', 'BB', '#6B3FD9', '#FFFFFF', 'ARS'),
            ('Ameria', 'AM', '#B01E2F', '#FFFFFF', 'AMD'), ('Ardshinbank', 'AR', '#0F6E3A', '#FFFFFF', 'AMD'), ('Тинькофф', 'ТБ', '#FFDD2D', '#1A1600', 'RUB'),
            ('Сбер', 'СБ', '#21A038', '#FFFFFF', 'RUB'), ('USDT-карта', 'UT', '#26A17B', '#FFFFFF', 'USDT'), ('Binance', 'BN', '#F3BA2F', '#1A1400', 'USDT'),
            ('Наличные USD', 'НЛ', '#C9D3C2', '#0B0F0A', 'USD'), ('Наличные ARS', 'НА', '#9AA594', '#0B0F0A', 'ARS'), ('Копилка', 'КП', '#FF8FA3', '#2B0F17', 'USD')]
    on = {'Galicia', 'USDT-карта'}
    rows = [row(n, cur, badge(i, c, ink, 32, 10), check('on' if n in on else 'off'), min_h=50) for n, i, c, ink, cur in accs]
    who = label('Кто внёс', False, 16) + segmented(['Все', 'Я', 'Аня'], 'Аня')
    body = '<div style="padding: 10px 16px 0;">%s%s%s</div>' % (who, label('Счета · 2 из 12', False, 16), group(rows))
    return over_list(sheet_head('Счёт и кто внёс') + body + sheet_button('Показать 23 операции'), 96)


def filters_sheet_b():
    def sec(title, inner):
        return label(title, False, 16) + inner
    wrap = lambda items: '<div style="display: flex; flex-wrap: wrap; gap: 8px;">%s</div>' % ''.join(items)  # noqa: E731
    body = (sec('Период', wrap([chip('Этот месяц'), chip('Прошлый месяц', True), chip('3 месяца'), chip('Год'), chip('Свой…', icon='calendar')]))
            + sec('Тип', segmented(['Все', 'Расходы', 'Доходы', 'Переводы'], 'Расходы'))
            + sec('Кто внёс', segmented(['Все', 'Я', 'Аня'], 'Аня'))
            + sec('Счета · 2 из 12', wrap([chip('Galicia', True), chip('USDT-карта', True), chip('Mercado Pago'), chip('Ameria'), chip('+ ещё 8')]))
            + sec('Категории · 3 из 30', wrap([chip('Еда · 3 из 5', True), chip('Дом'), chip('Транспорт'), chip('Дети'), chip('+ ещё 8')]))
            + sec('Сумма', '<div style="display: grid; grid-template-columns: 1fr 1fr; gap: 8px; margin-top: -12px;">%s%s</div>' % (field('От', '', placeholder=True), field('До', '', placeholder=True))))
    return scrim(list_b()) + sheet(sheet_head('Фильтры', 'Сбросить все') + '<div style="padding: 0 16px;">%s</div>' % body + sheet_button('Показать 23 операции'), 64)


def list_b():
    btn = ('<span style="height: 36px; padding: 0 14px; border-radius: 999px; background: %s; color: %s; font-size: 13px; font-weight: 600; display: inline-flex; '
           'align-items: center; gap: 6px; white-space: nowrap;">%sФильтры · 4</span>' % (LIME, ONLIME, svg('list', ONLIME, 16)))
    head = search('Поиск: заметка, получатель, сумма') + chips(btn, chip('Расходы ✕', True), chip('Еда · 3 ✕', True), chip('Октябрь ✕', True))
    return tab_screen('Операции', head + total('23 операции', '−184 300', 'ARS', '≈ 182,40 USD · из них 54,30 USDT') + ops_list(DAYS), 'ops')


def smart_v():
    def sug(kind, text, count, icon, col=LIME):
        return row(text, kind, tile_icon(icon, col), '<span style="%s font-size: 13px; color: %s;">%s</span>' % (MONO, MUTED, count), min_h=56)
    rows = [sug('Категория', 'Кафе', '42', 'cafe', WARN), sug('Получатель', 'Café Martínez', '12', 'user'), sug('Метка', '#кафе-с-друзьями', '3', 'note', VIOLET),
            sug('Заметка содержит', '«кафе»', '57', 'search', INK2), sug('Период', 'Октябрь 2026', '—', 'calendar', CYAN), sug('Сумма', '= 500 · в любой валюте', '8', 'rate', CYAN)]
    head = (search('', 'кафе окт 500') + chips(chip('Расходы ✕', True)) + label('Сделать фильтром', True, 16) + group(rows)
            + note('Тап — слово становится чипом. Остальное ищется по всем полям, как сейчас.'))
    return tab_screen('Операции', head, 'ops')


BOARDS = [  # name, title, fn, issue number
    ('Fb141A', 'А · чипы фильтров, у каждого своя шторка (рекомендую)', list_a, 141),
    ('Fb141ACats', 'А · шторка «Категории»: папки, 30 категорий, поиск', cats_sheet_a, 141),
    ('Fb141APeriod', 'А · шторка «Период»: готовые, стрелки, свой', period_sheet_a, 141),
    ('Fb141AAcc', 'А · шторка «Счёт и кто внёс»: 12 счетов', accounts_sheet_a, 141),
    ('Fb141AAmount', 'Все варианты · поиск по сумме «3000-5000»', amount_a, 141),
    ('Fb141B', 'Б · одна шторка «Фильтры» со всем сразу', filters_sheet_b, 141),
    ('Fb141V', 'В · фильтры из поиска подсказками', smart_v, 141),
]
