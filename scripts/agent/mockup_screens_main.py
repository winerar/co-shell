"""Generated boards: tabs (Обзор, Операции, Счета), entry sheet states, categories, account editor states."""
from mockup_bricks import *  # noqa: F401,F403
from mockup_parts import fab

ACC = [  # name, ini, colour, ink, type, balance, cur
    ('Galicia', 'GA', '#F26E22', '#1A0A00', 'Карта', '1 250 000', 'ARS'),
    ('Mercado Pago', 'MP', '#00B1EA', '#002A3A', 'Кошелёк', '96 400', 'ARS'),
    ('Ameria', 'AM', '#B01E2F', '#FFFFFF', 'Карта', '410 000', 'AMD'),
    ('USDT-карта', 'UT', '#26A17B', '#FFFFFF', 'Крипто', '1 840', 'USDT'),
    ('Наличные', 'НЛ', '#C9D3C2', '#0B0F0A', 'Наличные', '320', 'USD'),
]
RECENT = [
    ('Продукты', 'Galicia · Coto', '−32 000', 'ARS', 'e', 'cart', LIME, ''),
    ('Кафе', 'USDT-карта · Аня', '−13,10', 'USDT', 'e', 'cafe', WARN, 'чек 13 300 ARS'),
    ('Зарплата', 'Ameria', '+1 200', 'USD', 'i', 'salary', CYAN, ''),
    ('Ameria → Galicia', 'Перевод', '500', 'USD', 't', 'transfer', '#C79BFF', '512 000 ARS'),
]


def balance_hero(total='12 480,00', cur='USD', sub='≈ 15 974 400 ARS · 4 836 000 AMD', extra=''):
    return hero_card('<div style="font-size: 13px;">Общий баланс</div>'
                     '<div style="margin-top: 6px; %s font-size: 34px; font-weight: 700; letter-spacing: -0.5px; color: %s;">%s <span style="font-size: 18px;">%s</span></div>'
                     '<div style="margin-top: 4px; %s font-size: 13px;">%s</div>%s' % (MONO, LIME, total, cur, MONO, sub, extra),
                     pad='18px 18px 16px', radius=16, extra='margin-top: 8px;')


def tile(title, value, sub, color=INK, warn=False):
    border = WARNLINE if warn else LINE
    return ('<div style="padding: 14px; border-radius: 20px; background: %s; border: 1px solid %s;"><div style="font-size: 12px; color: %s;">%s</div>'
            '<div style="margin-top: 6px; %s font-size: 18px; font-weight: 700; color: %s;">%s</div><div style="font-size: 12px; color: %s;">%s</div></div>'
            % (CARD, border, WARN if warn else MUTED, title, MONO, color, value, MUTED, sub))


def converter_card():
    """«Конвертер» on «Обзор» (#319, board `Conv1Home`): one menu row in a card that opens the converter sheet."""
    return ('<div style="margin-top: 10px; padding: 8px 14px; border-radius: 24px; background: %s; border: 1px solid %s; display: flex; align-items: center; gap: 12px; min-height: 52px;">'
            '%s<div style="flex-grow: 1;"><div style="font-size: 15px; font-weight: 600;">Конвертер</div>'
            '<div style="font-size: 12px; color: %s;">Сколько это в других валютах</div></div>%s</div>'
            % (CARD, LINE, tile_icon('transfer', LIME, CHIP), MUTED, chevron()))


def tiles(*t):
    return '<div style="margin-top: 10px; display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 10px;">%s</div>' % ''.join(t)


def recent_card(items):
    return label('Последние операции') + card(''.join(tx(*r) for r in items))


SEARCH_BTN = icon_button('search', 'Поиск')


def home():
    return tab_screen('Обзор', balance_hero() + tiles(tile('Траты за ноябрь', '1 240,50', 'USD'), tile('Без категории', '14', 'операций'))
                      + converter_card() + recent_card(RECENT), 'home', SEARCH_BTN)


def home_empty():
    body = balance_hero('0,00', 'USD', 'Счетов пока нет') + empty('wallet', 'Счетов пока нет. Начните с того, где лежат деньги: карта, наличные, вклад.', 'Добавить счёт')
    return tab_screen('Обзор', body, 'home')


def home_attention():
    extra = ('<div style="margin-top: 10px; display: flex; align-items: center; gap: 6px; font-size: 12px; color: %s;">%s'
             'Без учёта: VES — нет курса</div>' % (WARN, svg('alert', WARN, 16)))
    body = (balance_hero(extra=extra) + tiles(tile('Траты за ноябрь', '1 240,50', 'USD · без VES'), tile('Внимание', '2', 'нет курса VES · курсы от 7 нояб.', WARN, True))
            + converter_card() + recent_card(RECENT[:3]))
    return tab_screen('Обзор', body, 'home', SEARCH_BTN)


def ops_head(chips=None):
    """Filter chips under the «Операции» header; the search itself is a loupe in the header (#394 Б)."""
    # As in the app: «Тип · Категория · Период · Счёт»; the row scrolls sideways, the last chip peeks out (#464).
    chips = chips or [chip('Тип ▾'), chip('Категория ▾'), chip('Период ▾'), chip('Счёт ▾')]
    return '<div style="display: flex; gap: 8px; margin: 4px -16px 0; padding: 0 16px; overflow: hidden; white-space: nowrap;">%s</div>' % ''.join(chips)


def loupe():
    return icon_button('search', 'Найти операцию')


def ops_empty():
    return tab_screen('Операции', ops_head() + empty('list', 'Операций пока нет. Нажмите «+», чтобы записать первую.'), 'ops', loupe())


def ops_nothing():
    body = ('<div style="display: flex; gap: 8px; margin-top: 4px;">%s</div>' % (chip('Расходы ✕', True) + chip('Galicia ✕', True) + chip('Октябрь ▾'))
            + empty('search', 'Ничего не нашлось') + '<div style="text-align: center; margin-top: 4px;"><span style="color: %s; font-size: 15px; font-weight: 600;">Сбросить фильтры</span></div>' % LIME)
    return search_head('кофе 500') + '<div style="padding: 0 16px;">%s</div>' % body + nav('ops')


def ops_scrolled():
    """Header poll А: scrolled «Операции» — solid header + hairline, the day at the top and its net under the title;
    day titles scroll away with the list (they no longer stick). A long list: 8 days, 4 rows each."""
    names = [('Продукты', 'Galicia · Coto', 'cart', LIME), ('Кафе', 'USDT-карта', 'cafe', WARN), ('Такси', 'Наличные', 'taxi', YELLOW), ('Аптека', 'Galicia', 'pill', PINK)]
    days = ['10 НОЯБРЯ, ВТ', '9 НОЯБРЯ, ПН', '8 НОЯБРЯ, ВС', '7 НОЯБРЯ, СБ']
    rows = []
    for d, title in enumerate(days):
        if d:
            rows.append(day(title, '−%d 000' % (24 + d * 4), 'ARS'))
        for r, (t, sub, ic, col) in enumerate(names[(1 if d == 0 else 0):]):
            rows.append(tx(t, sub, '−%d 250' % (20 + d * 4 + r), 'ARS', 'e', ic, col, ''))
    return tab_screen('Операции', '<div style="margin: 0 -16px;">%s</div>' % ''.join(rows), 'ops', loupe(), sub='10 ноября, вт · −24 000,00 ARS')


def ops_list(selected=(), snack=False):
    rows = []
    for i, r in enumerate(RECENT + [('Такси', 'Наличные', '−6 500', 'ARS', 'e', 'taxi', YELLOW, '')]):
        h = tx(*r)
        if i in selected:
            h = h.replace(dot_icon(r[5], r[6]), '<span style="width: 40px; height: 40px; flex: none; border-radius: 50%%; background: %s; display: flex; align-items: center; justify-content: center;">%s</span>' % (LIME, svg('check', ONLIME, 20, 2.4)))
            h = '<div style="background: #16200F;">%s</div>' % h
        rows.append(h)
        if i == 1:
            rows.append(day('ВЧЕРА', '−6 500', 'ARS'))
    lst = '<div style="margin: 14px -16px 0;">%s%s</div>' % (day('СЕГОДНЯ', '−45 100', 'ARS'), ''.join(rows))
    if selected:
        top = select_head(len(selected))
        bar = ('<div style="position: absolute; left: 32px; right: 32px; bottom: 18px; height: 56px; border-radius: 999px; background: %s; display: flex; align-items: center; gap: 8px; padding: 0 8px; box-sizing: border-box;">'
               '<span style="flex: 1; height: 48px; border-radius: 999px; display: flex; align-items: center; justify-content: center; gap: 8px; color: %s; font-size: 15px;">%sДублировать</span>'
               '<span style="flex: 1; height: 48px; border-radius: 999px; background: %s; color: %s; display: flex; align-items: center; justify-content: center; gap: 8px; font-size: 15px; font-weight: 600;">%sУдалить · %d</span></div>'
               % (PANEL, '#5C6757', svg('copy', '#5C6757', 20), PINKBG, PINK, svg('trash', PINK, 20), len(selected)))
        return top + '<div style="padding: 0 16px;">%s%s</div>' % (ops_head(), lst) + bar
    body = ops_head([chip('Расходы ✕', True), chip('Счёт ▾'), chip('Ноябрь ▾')]) + lst
    return tab_screen('Операции', body, 'ops', loupe()) + (snackbar('Удалено операций: 3') if snack else '')


def accounts_empty():
    # «Счета» is an inner screen («Ещё → Счета», #140): back arrow, no bottom bar, the round «+».
    return inner_screen('Счета', empty('wallet', 'Счетов пока нет. Нажмите «+», чтобы добавить первый.', 'Добавить счёт')) + fab('Добавить счёт')


def accounts_search():
    rows = [row('<b>Ga</b>licia', 'Карта', badge('GA', '#F26E22', '#1A0A00'), amount('1 250 000', 'ARS'))]
    body = (search('', 'ga') + label('Найдено: 1') + group(rows) + note('Архивные счета тоже ищутся — помечены «в архиве».'))
    return inner_screen('Счета', body) + fab('Добавить счёт')


# ---- entry sheet ----------------------------------------------------------------------------------------------

def keypad(save='Готово', done=True):
    """CoShellKeypad; `done=False` — «Готово» dimmed while there is no amount (`doneEnabled`, #505)."""
    k = lambda t, bg=KEY, col=INK, sz=22: ('<span style="border-radius: 16px; background: %s; color: %s; font-size: %dpx; %s display: flex; align-items: center; justify-content: center;">%s</span>'
                                           % (bg, col, sz, MONO, t))
    # K2 (owner 2026-11-18, #291): right column ⌫ · + · − · «Готово» (one row, wide); every key ≥ 48 dp
    keys = (k('7') + k('8') + k('9') + k('⌫', KEY2, INK, 20) + k('4') + k('5') + k('6') + k('+', KEY2, LIME, 24) + k('1') + k('2') + k('3')
            + k('−', KEY2, LIME, 24) + k(',') + k('0') + k('000', KEY, INK, 18)
            + '<span style="border-radius: 18px; background: %s; color: %s; font-size: 15px; font-weight: 700; display: flex; align-items: center; justify-content: center; gap: 6px; opacity: %s;">%s%s</span>'
            % (LIME, ONLIME, '1' if done else '0.38', svg('check', ONLIME, 22, 2.2), save))
    return '<div style="display: grid; grid-template-columns: repeat(4, minmax(0, 1fr)); grid-auto-rows: 54px; gap: 6px;">%s</div>' % keys


def acc_chip(a, label_txt=''):
    lt = '<span style="font-size: 11px; color: %s;">%s</span>' % (MUTED, label_txt) if label_txt else ''
    return ('<span style="display: flex; align-items: center; gap: 8px; height: 48px; padding: 0 14px 0 6px; border-radius: 999px; border: 1px solid %s; background: %s;">%s'
            '<span style="display: flex; flex-direction: column;">%s<span style="font-size: 14px; font-weight: 600;">%s</span></span><span style="color: %s;">▾</span></span>'
            % (LINE2, BAR, badge(a[1], a[2], a[3], 34), lt, a[0], MUTED))


def big_amount(text, cur, color=PINK, size=48):
    return ('<div style="display: flex; align-items: baseline; justify-content: center; gap: 8px; min-height: 60px;"><span style="%s font-size: %dpx; font-weight: 700; color: %s;">%s</span>'
            '<span style="%s font-size: 18px; color: %s;">%s</span></div>' % (MONO, size, color, text, MONO, MUTED, cur))


CATS = [('Продукты', 'cart', LIME), ('Кафе', 'cafe', WARN), ('Такси', 'taxi', YELLOW), ('Аптека', 'pill', '#FF8FA3'),
        ('Дом', 'house', CYAN), ('Связь', 'phone', '#C79BFF'), ('Кино', 'film', '#5CFFB0'), ('Подарки', 'gift', '#FF8A5C')]


def cat_grid(selected=None, error=False):
    out = ''
    for n, ic, col in CATS:
        ring = 'padding: 3px; border-radius: 50%%; box-shadow: 0 0 0 2px %s;' % LIME if n == selected else 'padding: 3px;'
        out += ('<span style="display: flex; flex-direction: column; align-items: center; gap: 4px; padding: 4px 0;"><span style="%s">%s</span>'
                '<span style="font-size: 12px; color: %s;">%s</span></span>' % (ring, dot_icon(ic, col), INK if n == selected else INK2, n))
    err = '<div style="text-align: center; font-size: 12px; color: %s; margin-top: -4px;">Выберите категорию</div>' % PINK if error else ''
    return '<div style="display: grid; grid-template-columns: repeat(4, minmax(0, 1fr)); gap: 2px;">%s</div>%s' % (out, err)


def chips_row(*c):
    return '<div style="display: flex; gap: 8px; padding: 0 4px; flex-wrap: wrap;">%s</div>' % ''.join(c)


def entry(inner):
    return scrim() + sheet('<div style="position: absolute; left: 0; right: 0; bottom: 0; padding: 0 12px 14px; display: flex; flex-direction: column; gap: 12px;">%s</div>' % inner)


# ---- categories / accounts editors ----------------------------------------------------------------------------

def form_footer(primary='Сохранить'):
    return '<div style="position: absolute; left: 16px; right: 16px; bottom: 24px;">%s</div>' % button(primary, mt=0)


def category_blocked():
    under = inner_screen('Категория', field('Название', 'Продукты') + field('Тип', 'Расходы'))
    return under + scrim() + dialog('Категорию нельзя удалить', 'Она есть в операциях. Отправьте её в архив: история сохранится, а при вводе операций она больше не появится.',
                                    [('Отмена', 'text'), ('В архив', 'primary')])


def account_existing():
    acts = icon_button('archive', 'В архив') + icon_button('trash', 'Удалить', PINK)
    body = (card('<div style="display: flex; align-items: center; gap: 12px;">%s<span style="flex-grow: 1;"><div style="font-size: 16px; font-weight: 600;">Galicia</div>'
                 '<div style="font-size: 12px; color: %s;">Карта · ARS</div></span>%s</div>' % (badge('GA', '#F26E22', '#1A0A00', 44, 14), MUTED, amount('1 250 000', 'ARS', INK, 16)), pad='14px', extra='margin-top: 4px;')
            + field('Название', 'Galicia') + field('Валюта', 'ARS', hint='По счёту уже есть операции — валюту изменить нельзя', trail=svg('lock', MUTED, 18))
            + field('Начальный баланс', '−15 000,00', error='Слишком много знаков после запятой для этой валюты', mono=True)
            + '<div style="margin-top: 12px;">' + group([row('Учитывать в общем балансе', 'Выключите для долга или чужих денег', '', switch(True)),
                                                         ]) + '</div>'
            + note('«Только у меня» включается при создании счёта.'))
    return inner_screen('Счёт', body, acts) + form_footer()


def account_local():
    """«Только у меня» (#558, poll A): a new account in a family — the switch on, the note about transfers."""
    body = (field('Название', 'Моя копилка') + field('Валюта', 'ARS')
            + '<div style="margin-top: 12px;">' + group([row('Учитывать в общем балансе', 'Выключите для долга или чужих денег', '', switch(True)),
                                                         row('Только у меня', 'Семья не увидит счёт и его операции', '', switch(True))]) + '</div>'
            + note('Переводы между этим счётом и общими другие участники не увидят.'))
    return inner_screen('Счёт', body, '') + form_footer()


def account_blocked():
    return (inner_screen('Счёт', field('Название', 'Galicia') + field('Валюта', 'ARS')) + scrim()
            + dialog('Счёт нельзя удалить', 'По счёту есть операции. Отправьте его в архив: история и баланс сохранятся, а в списках выбора он больше не появится.',
                     [('Отмена', 'text'), ('В архив', 'primary')]))


def _entry():
    import mockup_screens_entry  # one-step entry sheet (#505); imported late: it builds on this module
    return mockup_screens_entry


BOARDS = [
    # name, title, html-function
    ('Home', '1 · Обзор', home), ('HomeEmpty', '1 · Обзор: первый запуск', home_empty), ('HomeAttention', '1 · Обзор: нет курса', home_attention),
    ('OpsEmpty', '2 · Операции: пусто', ops_empty), ('OpsNothing', '2 · Операции: ничего не нашлось', ops_nothing),
    ('OpsScrolled', '2 · Операции: прокручено, шапка «где я»', ops_scrolled),
    ('OpsSelect', '2 · Операции: выбор нескольких', lambda: ops_list((0, 1, 3))), ('OpsUndo', '2 · Операции: удалено, «Отменить»', lambda: ops_list(snack=True)),
    ('AccountsEmpty', '3 · Счета: пусто', accounts_empty), ('AccountsSearch', '3 · Счета: поиск', accounts_search),
    ('EntryTransfer', '5 · Шторка: перевод между валютами', lambda: _entry().transfer()), ('EntryReceipt', '5 · Шторка: чек в другой валюте', lambda: _entry().receipt()),
    ('EntryGuessHist', '5 · Шторка: категория по прошлым операциям', lambda: _entry().guess('hist')), ('EntryGuessAi', '5 · Шторка: категория предложена ИИ', lambda: _entry().guess('ai')),
    ('EntryEdit', '5 · Шторка: правка операции', lambda: _entry().edit()), ('EntryError', '5 · Шторка: не выбрана категория', lambda: _entry().error()),
    ('EntryNoAccounts', '5 · Шторка: нет счетов', lambda: _entry().no_accounts()),
    ('EntryAllAccounts', '5 · Шторка: «Все ›» — все счета, как на «Счетах»', lambda: _entry().all_accounts()),
    ('CategoryBlocked', '7 · Категория: удалить нельзя', category_blocked),
    ('AccountEditExisting', '9 · Счёт: правка, ошибки', account_existing), ('AccountEditLocal', '9 · Счёт: «Только у меня»', account_local),
    ('AccountBlocked', '9 · Счёт: удалить нельзя', account_blocked),
]
