"""Generated boards: variants for proposals 3–12 of the «Предложения» sheet (mockup_special.IDEAS)."""
from mockup_bricks import *  # noqa: F401,F403
from mockup_screens_main import (ACC, CATS, RECENT, acc_chip, balance_hero, big_amount, chips_row, day, entry, keypad,
                                 ops_head, recent_card, tile, tiles)

DOT_TAG = '<span style="font-size: 10px; color: %s; letter-spacing: 0.04em;">ЧАСТО</span>' % LIME


# ---- 3 · category suggestion ------------------------------------------------------------------------------------

def cat_cell(n, ic, col, sel=False, tag=''):
    ring = 'padding: 3px; border-radius: 50%%; box-shadow: 0 0 0 2px %s;' % LIME if sel else 'padding: 3px;'
    return ('<span style="display: flex; flex-direction: column; align-items: center; gap: 3px; padding: 4px 0;"><span style="%s">%s</span>'
            '<span style="font-size: 12px; color: %s;">%s</span>%s</span>' % (ring, dot_icon(ic, col), INK if sel else INK2, n, tag))


def entry_head():
    return ('<div style="display: flex; align-items: center; gap: 8px; padding: 0 4px;">%s<span style="flex-grow: 1;"></span>'
            '<span style="font-size: 13px; font-weight: 600; color: %s;">Расход</span></div>' % (acc_chip(ACC[0]), PINK))


def idea3a():
    order = [CATS[0], CATS[1], CATS[4], CATS[2], CATS[3], CATS[5], CATS[6], CATS[7]]
    cells = ''.join(cat_cell(n, ic, c, False, DOT_TAG if i < 4 else '<span style="font-size: 10px;">&nbsp;</span>') for i, (n, ic, c) in enumerate(order))
    grid = '<div style="display: grid; grid-template-columns: repeat(4, minmax(0, 1fr)); gap: 0;">%s</div>' % cells
    hint = '<div style="font-size: 12px; color: %s; padding: 0 4px;">Первый ряд — частые для Galicia в это время дня; остальные — как всегда</div>' % MUTED
    return entry(entry_head() + big_amount('4 500', 'ARS') + hint + grid + chips_row(chip('Сегодня ▾'), chip('＋ Заметка')) + keypad())


def idea3b():
    sug = ''.join('<span style="display: inline-flex; align-items: center; gap: 6px; height: 40px; padding: 0 14px 0 6px; border-radius: 999px; background: %s; border: 1px solid %s; font-size: 14px;">%s%s</span>'
                  % (LIMEPILL if i == 0 else BAR, LIME if i == 0 else LINE2, dot_icon(ic, c, 28), n) for i, (n, ic, c) in enumerate([CATS[0], CATS[1], CATS[4]]))
    row_ = ('<div style="padding: 0 4px;"><div style="font-size: 12px; font-weight: 600; color: %s; letter-spacing: 0.04em; margin-bottom: 8px;">ОБЫЧНО ЗДЕСЬ</div>'
            '<div style="display: flex; gap: 8px;">%s</div></div>' % (LIME, sug))
    grid = '<div style="display: grid; grid-template-columns: repeat(4, minmax(0, 1fr)); gap: 0;">%s</div>' % ''.join(cat_cell(n, ic, c) for n, ic, c in CATS)
    return entry(entry_head() + big_amount('4 500', 'ARS') + row_ + grid + chips_row(chip('Сегодня ▾'), chip('＋ Заметка')) + keypad())


# ---- 4 · filter total ---------------------------------------------------------------------------------------------

CAFE = [('Кафе', 'Galicia · Café Martínez', '−8 400', 'ARS', 'e', 'cafe', WARN, ''), ('Кафе', 'USDT-карта · Аня', '−13,10', 'USDT', 'e', 'cafe', WARN, 'чек 13 300 ARS'),
        ('Кафе', 'Наличные', '−6 000', 'ARS', 'e', 'cafe', WARN, '')]


def cafe_list():
    return ('<div style="margin: 6px -16px 0;">%s%s%s%s</div>' % (day('28 ОКТЯБРЯ', '−8 400', 'ARS'), tx(*CAFE[0]), day('21 ОКТЯБРЯ', '−19 300', 'ARS'), tx(*CAFE[1]) + tx(*CAFE[2])))


def cafe_chips():
    return search('', 'кафе') + '<div style="display: flex; gap: 8px; margin-top: 12px;">%s</div>' % (chip('Расходы ✕', True) + chip('Счёт ▾') + chip('Октябрь ✕', True))


def idea4a():
    total = ('<div style="margin-top: 12px; display: flex; align-items: baseline; justify-content: space-between; padding: 12px 14px; border-radius: 16px; background: %s; border: 1px solid %s;">'
             '<span style="font-size: 13px; color: %s;">12 операций</span><span style="display: flex; flex-direction: column; align-items: flex-end;">%s'
             '<span style="%s font-size: 12px; color: %s;">≈ 82,10 USD</span></span></div>' % (CARD, LINE, MUTED, amount('−84 300', 'ARS', PINK, 18), MONO, MUTED))
    return tab_screen('Операции', cafe_chips() + total + cafe_list(), 'ops')


def idea4b():
    bar = ('<div style="position: absolute; left: 16px; right: 16px; bottom: 96px; padding: 12px 16px; border-radius: 20px; background: %s; border: 1px solid %s; box-shadow: 0 12px 32px rgba(0,0,0,0.45);">'
           '<div style="display: flex; justify-content: space-between; align-items: baseline;"><span style="font-size: 13px; color: %s;">Итог фильтра · 12 операций</span>%s</div>'
           '<div style="margin-top: 8px; display: flex; gap: 16px; font-size: 12px; color: %s;"><span>Расход %s</span><span>Доход %s</span><span>≈ 82,10 USD</span></div></div>'
           % (CHIP, LINE2, MUTED, amount('−84 300', 'ARS', PINK, 18), MUTED, amount('−84 300', '', PINK, 12), amount('0', '', CYAN, 12)))
    return tab_screen('Операции', cafe_chips() + cafe_list(), 'ops') + bar


# ---- 5 · currency shares on «Счета» -----------------------------------------------------------------------------

SHARES = [('USD', 46, LIME), ('ARS', 28, CYAN), ('USDT', 15, VIOLET), ('AMD', 11, YELLOW)]


def accounts_rows():
    rows = [row(a[0], a[4], badge(a[1], a[2], a[3]), amount(a[5], a[6])) for a in ACC[:4]]
    return label('Карты и банки', True, 16) + group(rows)


def acc_hero(extra):
    return card('<div style="font-size: 13px; color: %s;">Всего в общем балансе · 12 счетов</div><div style="margin-top: 4px; %s font-size: 34px; font-weight: 700;">9 640 '
                '<span style="font-size: 18px; color: %s;">USD</span></div>%s' % (MUTED, MONO, MUTED, extra), pad='16px', radius=24)


def idea5a():
    bar = '<div style="margin-top: 12px; display: flex; gap: 3px; height: 10px; border-radius: 5px; overflow: hidden;">%s</div>' % ''.join(
        '<span style="width: %d%%; background: %s;"></span>' % (p, c) for _, p, c in SHARES)
    legend = '<div style="margin-top: 10px; display: flex; flex-wrap: wrap; gap: 6px 14px; font-size: 12px; color: %s;">%s</div>' % (INK2, ''.join(
        '<span style="display: inline-flex; align-items: center; gap: 6px;"><span style="width: 8px; height: 8px; border-radius: 50%%; background: %s;"></span>%s <b style="%s">%d%%</b></span>' % (c, n, MONO, p) for n, p, c in SHARES))
    return tab_screen('Счета', acc_hero(bar + legend) + '<div style="margin-top: 12px;">%s</div>' % segmented(['По типу', 'По валюте', 'Чьи'], 'По типу') + accounts_rows(), 'acc', icon_button('search', 'Поиск'))


def idea5b():
    chips = '<div style="margin-top: 12px; display: grid; grid-template-columns: repeat(4, minmax(0, 1fr)); gap: 6px;">%s</div>' % ''.join(
        '<span style="padding: 8px; border-radius: 12px; background: %s; display: flex; flex-direction: column; gap: 2px;"><span style="font-size: 12px; color: %s;">%s</span>'
        '<span style="%s font-size: 15px; font-weight: 700;">%d%%</span><span style="height: 3px; border-radius: 2px; background: %s; width: %d%%;"></span></span>' % (CHIP, MUTED, n, MONO, p, c, max(20, p * 2)) for n, p, c in SHARES)
    return tab_screen('Счета', acc_hero(chips) + '<div style="margin-top: 12px;">%s</div>' % segmented(['По типу', 'По валюте', 'Чьи'], 'По типу') + accounts_rows(), 'acc', icon_button('search', 'Поиск'))


# ---- 6 · quiet sync indicator ------------------------------------------------------------------------------------

def home_with(actions, top_extra=''):
    return tab_screen('Обзор', top_extra + balance_hero() + tiles(tile('Траты за ноябрь', '1 240,50', 'USD'), tile('Без категории', '14', 'операций')) + recent_card(RECENT[:3]),
                      'home', actions)


def sync_dot(color, text):
    return ('<span style="display: inline-flex; align-items: center; gap: 6px; height: 32px; padding: 0 12px; border-radius: 999px; background: %s; font-size: 12px; color: %s;">'
            '<span style="width: 8px; height: 8px; border-radius: 50%%; background: %s;"></span>%s</span>' % (CHIP, INK2, color, text))


def idea6a():
    return home_with(sync_dot(OK, 'синхронно') + icon_button('search', 'Поиск'))


def idea6a2():
    return home_with(sync_dot(WARN, 'ждут: 3') + icon_button('search', 'Поиск'))


def idea6b():
    av = ('<span style="display: flex; margin-right: 4px;"><span style="width: 34px; height: 34px; border-radius: 50%%; background: %s; color: #1A0533; display: flex; align-items: center; justify-content: center; font-size: 13px; font-weight: 700; box-shadow: 0 0 0 2px %s, 0 0 0 4px %s;">М</span>'
          '<span style="width: 34px; height: 34px; margin-left: -6px; border-radius: 50%%; background: %s; color: #002A2E; display: flex; align-items: center; justify-content: center; font-size: 13px; font-weight: 700; box-shadow: 0 0 0 2px %s, 0 0 0 4px %s;">А</span></span>'
          % (VIOLET, BG, OK, CYAN, BG, OK))
    return home_with(av + icon_button('search', 'Поиск'))


# ---- 7 · undo instead of dialogs --------------------------------------------------------------------------------

def idea7a():
    rows = [row(a[0], a[4], badge(a[1], a[2], a[3]), amount(a[5], a[6])) for a in ACC[:3]]
    return tab_screen('Счета', acc_hero('') + label('Карты и банки', True, 16) + group(rows), 'acc') + snackbar('Счёт «Старая карта» удалён', bottom=96)


def idea7b():
    body = (field('Название', 'Старая карта') + field('Валюта', 'USD') + field('Начальный баланс', '0,00', mono=True)
            + '<div style="margin-top: 20px; display: flex; gap: 8px;"><span style="flex: 1;">%s</span><span style="flex: 1;">%s</span></div>' % (button('Отмена', 'outline', None, 0), button('Точно удалить', 'danger', 'trash', 0))
            + note('Нажали «Удалить» — кнопка спрашивает ещё раз прямо на месте, без окна поверх. Через 4 с вернётся обратно.', 10))
    return inner_screen('Счёт', body)


# ---- 8 · filled text field ------------------------------------------------------------------------------------------

def field_above(lbl, value, hint='', error='', mono=False):
    under = ('<div style="margin: 6px 4px 0; font-size: 12px; color: %s;">%s</div>' % (PINK, error) if error else
             '<div style="margin: 6px 4px 0; font-size: 12px; color: %s;">%s</div>' % (MUTED, hint) if hint else '')
    return ('<div style="margin-top: 14px;"><div style="margin: 0 4px 6px; font-size: 13px; font-weight: 600; color: %s;">%s</div>'
            '<div style="height: 52px; padding: 0 16px; box-sizing: border-box; border-radius: 14px; background: %s; display: flex; align-items: center; font-size: 16px; %s %s">%s</div>%s</div>'
            % (PINK if error else MUTED, lbl, CHIP, MONO if mono else '', 'box-shadow: inset 0 0 0 1px %s;' % PINK if error else '', value, under))


def account_form(f):
    types = '<div style="margin-top: 14px;">%s</div>' % segmented(['Карта', 'Наличные', 'Вклад', 'Крипто'], 'Карта')
    return inner_screen('Новый счёт', f('Название', 'Galicia') + types + f('Валюта', 'ARS', 'Код из 3–10 латинских букв или цифр, например ARS, USDT')
                        + f('Начальный баланс', '1 250 000,555', '', 'Слишком много знаков после запятой для этой валюты', True)) + \
        '<div style="position: absolute; left: 16px; right: 16px; bottom: 24px;">%s</div>' % button('Сохранить', mt=0)


def idea8a():
    return account_form(lambda l, v, h='', e='', m=False: field(l, v, h, e, m))


def idea8b():
    return account_form(field_above)


# ---- 9 · rows in cards ------------------------------------------------------------------------------------------------

SETTINGS_ROWS = [('Валюты отчётов', 'Основная: USD', 'rate'), ('Встряхнуть — новая операция', 'Пока открыто приложение', 'spark'), ('Обновление приложения', 'Версия 1.43 · последняя', 'download')]


def idea9a():
    rows = group([row(t, s, tile_icon(i), chevron()) for t, s, i in SETTINGS_ROWS])
    fam = group([row('Семья и синхронизация', 'Синхронизировано: 2 мин назад', tile_icon('user', VIOLET), chevron()), row('Резервная копия', 'Автокопия: вчера', tile_icon('download', CYAN), chevron())])
    return inner_screen('Настройки', label('Тема', top=4) + segmented(['Системная', 'Светлая', 'Тёмная'], 'Тёмная', 'lime') + label('Общее') + rows + label('Данные') + fam)


def idea9b():
    def one(t, s, i, col=LIME):
        return card(row(t, s, tile_icon(i, col), chevron()).replace('{{div}}', ''), extra='margin-top: 8px;')
    return inner_screen('Настройки', label('Тема', top=4) + segmented(['Системная', 'Светлая', 'Тёмная'], 'Тёмная', 'lime') + label('Общее')
                        + ''.join(one(*r) for r in SETTINGS_ROWS) + label('Данные') + one('Семья и синхронизация', 'Синхронизировано: 2 мин назад', 'user', VIOLET)
                        + one('Резервная копия', 'Автокопия: вчера', 'download', CYAN))


# ---- 10 · rate sparkline ------------------------------------------------------------------------------------------------

def spark(points, color=LIME, w=72, h=28):
    mx, mn = max(points), min(points)
    pts = ' '.join('%.1f,%.1f' % (i * w / (len(points) - 1), h - 2 - (p - mn) * (h - 4) / ((mx - mn) or 1)) for i, p in enumerate(points))
    return ('<svg width="%d" height="%d" viewBox="0 0 %d %d" fill="none" aria-hidden="true"><polyline points="%s" stroke="%s" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"></polyline></svg>'
            % (w, h, w, h, pts, color))


SERIES = {'ARS': ([1040, 1036, 1030, 1022, 1025, 1018, 1012, 1006, 1001, 996, 990, 984, 980, 977], '−6,1 %', PINK),
          'AMD': ([392, 391, 390, 391, 389, 388, 389, 388, 387, 388, 387, 386, 387, 387], '−1,3 %', PINK),
          'RUB': ([84, 83, 82, 82, 81, 81, 80, 81, 81, 82, 81, 81, 81, 81], '−3,4 %', PINK),
          'USDT': ([1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1], '0 %', MUTED)}
RATE_TXT = {'ARS': '0,000977', 'AMD': '0,00258', 'RUB': '0,0123', 'USDT': '1,0004'}


def idea10a():
    rows = [row('1 %s = %s USD' % (c, RATE_TXT[c]), 'на 11 нояб.', badge(c[:2], CHIP, INK, 36, 18),
                '<span style="display: flex; flex-direction: column; align-items: flex-end; gap: 2px;">%s<span style="%s font-size: 11px; color: %s;">%s за 30 дн.</span></span>'
                % (spark(s[0], s[2] if s[2] != MUTED else MUTED), MONO, s[2], s[1])) for c, s in SERIES.items()]
    return inner_screen('Курсы валют', note('Загружено: курсы на 11 нояб., источник fawazahmed0/currency-api', 0) + label('Курсы на сегодня') + group(rows))


def idea10b():
    pts = SERIES['ARS'][0]
    big = card('<div style="display: flex; justify-content: space-between; align-items: baseline;"><b>1 USD = 1 024 ARS</b><span style="%s font-size: 12px; color: %s;">+6,5 %% за 30 дн.</span></div>'
               '<div style="margin-top: 10px;">%s</div><div style="display: flex; justify-content: space-between; font-size: 11px; color: %s; margin-top: 4px;"><span>12 окт.</span><span>11 нояб.</span></div>'
               '<div style="margin-top: 8px; display: flex; gap: 8px;">%s</div>'
               % (MONO, WARN, spark([1 / p * 1e6 for p in pts], LIME, 324, 110), MUTED, chip('7 дн.') + chip('30 дн.', True) + chip('1 год')), pad='14px')
    rows = [row('1 %s = %s USD' % (c, RATE_TXT[c]), 'на 11 нояб.', badge(c[:2], CHIP, INK, 36, 18), chevron()) for c in ['AMD', 'RUB', 'USDT']]
    return inner_screen('Курсы валют', label('ARS', True, 4) + big + label('Остальные') + group(rows))


# ---- 11 · skip empty import steps ------------------------------------------------------------------------------------

def idea11a():
    steps = [('Файл', 'done'), ('Что в файле', 'done'), ('Счета', 'auto'), ('Категории', 'done'), ('Переводы без пары', 'auto'), ('Проверка', 'now')]
    out = ''
    for n, st in steps:
        mark = {'done': svg('check', LIME, 18, 2.2), 'auto': '<span style="font-size: 11px; color: %s;">авто</span>' % MUTED, 'now': '<span style="width: 10px; height: 10px; border-radius: 50%%; background: %s;"></span>' % LIME}[st]
        out += '<div style="display: flex; align-items: center; gap: 10px; height: 34px; font-size: 14px; color: %s;"><span style="width: 24px; display: flex; justify-content: center;">%s</span>%s</div>' % (INK if st != 'auto' else MUTED, mark, n)
    auto = group([row('Счета: все 9 нашлись у вас', 'Galicia, Ameria, USDT-карта… — той же валюты', svg('check', OK)), row('Переводов без пары нет', 'шаг пропущен', svg('check', OK))])
    return inner_screen('Импорт из Wallet', card(out, pad='10px 14px') + label('Решено автоматически') + auto + note('Открыть любой пропущенный шаг — тап по строке.')) + \
        '<div style="position: absolute; left: 16px; right: 16px; bottom: 24px;">%s</div>' % button('Импортировать 1 287 операций', mt=0)


def idea11b():
    def sec(title, sub, open_=False, inner=''):
        return card('<div style="display: flex; align-items: center; gap: 10px;"><span style="flex-grow: 1;"><div style="font-size: 15px; font-weight: 600;">%s</div>'
                    '<div style="font-size: 12px; color: %s;">%s</div></span>%s</div>%s' % (title, MUTED, sub, svg('down' if not open_ else 'chev', MUTED, 18), inner), pad='14px', extra='margin-top: 8px;')
    cats = ('<div style="margin-top: 10px;">%s</div>' % group([row('Restaurants', 'Расход · 96', '', chip('Новая')), row('Еда', 'Расход · 512', '', chip('→ Продукты', True))]))
    return inner_screen('Импорт из Wallet', '<div style="font-size: 13px; color: %s; margin: 0 4px;">Шаг 2 из 3 · <b style="color: %s;">Проверка</b></div>' % (MUTED, INK)
                        + sec('1 287 операций', 'с 3 янв. 2024 по 10 нояб. 2026 · 20 уже были') + sec('Счета · 9', 'все сопоставлены')
                        + sec('Категории · 27', '4 новые — проверьте', True, cats) + sec('Переводы без пары · 0', 'нечего решать')) + \
        '<div style="position: absolute; left: 16px; right: 16px; bottom: 24px;">%s</div>' % button('Импортировать', mt=0)


# ---- 12 · large font ----------------------------------------------------------------------------------------------------

def big_tx(title, sub, amt, cur, icon, color, wrap):
    a = '<span style="%s font-size: 20px; color: %s; white-space: nowrap;">%s <span style="font-size: 15px; color: %s;">%s</span></span>' % (MONO, PINK, amt, MUTED, cur)
    name = '<span style="font-size: 21px;">%s</span><span style="font-size: 17px; color: %s;">%s</span>' % (title, MUTED, sub)
    if wrap:
        return ('<div style="display: flex; gap: 12px; padding: 12px 16px;">%s<span style="display: flex; flex-direction: column; gap: 2px; flex-grow: 1; min-width: 0;">%s%s</span></div>'
                % (dot_icon(icon, color, 48), name, a))
    return ('<div style="display: flex; align-items: center; gap: 12px; min-height: 76px; padding: 0 16px;">%s<span style="display: flex; flex-direction: column; flex-grow: 1; min-width: 0;">%s</span>%s</div>'
            % (dot_icon(icon, color, 48), name, a))


def idea12(wrap):
    data = [('Продукты', 'Galicia · Coto', '−1 250 000', 'ARS', 'cart', LIME), ('Зарплата', 'Ameria', '+850 000', 'AMD', 'salary', CYAN), ('Кафе', 'USDT-карта · Аня', '−13,10', 'USDT', 'cafe', WARN)]
    if not wrap:
        data = [(t, s, a.replace('1 250 000', '1,25 млн').replace('850 000', '850 тыс.'), c, i, col) for t, s, a, c, i, col in data]
    lst = ''.join(big_tx(*d, wrap=wrap) for d in data)
    top = '<div style="display: flex; align-items: center; height: 72px; padding: 0 16px;"><h1 style="margin: 0; font-size: 36px; font-weight: 700;">Операции</h1></div>'
    head = '<div style="padding: 10px 20px 6px; font-size: 17px; font-weight: 600; color: %s; letter-spacing: 0.04em;">СЕГОДНЯ</div>' % LIME
    tag = note('Шрифт 130 %%: %s' % ('сумма уходит под название, ничего не обрезано' if wrap else 'крупные суммы сокращаются (1,25 млн), полная — в шторке'), 4)
    return top + '<div style="padding: 0 16px;">%s</div>' % tag + head + lst + nav('ops')


BOARDS = [  # name, title, fn, proposal number
    ('Idea3A', '3А · Частые — первым рядом сетки', idea3a, 3), ('Idea3B', '3Б · Строка «Обычно здесь» над сеткой (рекомендую)', idea3b, 3),
    ('Idea4A', '4А · Итог под фильтрами (рекомендую)', idea4a, 4), ('Idea4B', '4Б · Плавающая карточка итога', idea4b, 4),
    ('Idea5A', '5А · Полоса долей и легенда (рекомендую)', idea5a, 5), ('Idea5B', '5Б · Плитки валют с %', idea5b, 5),
    ('Idea6A', '6А · Точка «синхронно» (рекомендую)', idea6a, 6), ('Idea6A2', '6А · …и «ждут отправки»', idea6a2, 6), ('Idea6B', '6Б · Аватары семьи с кольцом', idea6b, 6),
    ('Idea7A', '7А · Удалено сразу, снэкбар «Отменить» (рекомендую)', idea7a, 7), ('Idea7B', '7Б · Подтверждение в самой кнопке', idea7b, 7),
    ('Idea8A', '8А · Заливка, подпись внутри (рекомендую)', idea8a, 8), ('Idea8B', '8Б · Подпись над полем', idea8b, 8),
    ('Idea9A', '9А · Группа строк в одной карточке (рекомендую)', idea9a, 9), ('Idea9B', '9Б · Каждая строка — своя карточка', idea9b, 9),
    ('Idea10A', '10А · Спарклайн в строке (рекомендую)', idea10a, 10), ('Idea10B', '10Б · Большой график выбранной валюты', idea10b, 10),
    ('Idea11A', '11А · Пустые шаги — «авто» (рекомендую)', idea11a, 11), ('Idea11B', '11Б · 3 шага, секции в «Проверке»', idea11b, 11),
    ('Idea12A', '12А · Сумма под названием (рекомендую)', lambda: idea12(True), 12), ('Idea12B', '12Б · Сокращать: 1,25 млн', lambda: idea12(False), 12),
]
