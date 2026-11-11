"""Generated boards: tabs (Обзор, Операции, Счета), entry sheet states, categories, account editor states."""
from mockup_bricks import *  # noqa: F401,F403

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
    return card('<div style="font-size: 13px; color: %s;">Общий баланс</div>'
                '<div style="margin-top: 6px; %s font-size: 34px; font-weight: 700; letter-spacing: -0.5px;">%s <span style="font-size: 18px; color: %s;">%s</span></div>'
                '<div style="margin-top: 4px; %s font-size: 13px; color: %s;">%s</div>%s' % (MUTED, MONO, total, MUTED, cur, MONO, MUTED, sub, extra),
                pad='18px 18px 16px', radius=24, extra='margin-top: 8px;')


def tile(title, value, sub, color=INK, warn=False):
    border = WARNLINE if warn else LINE
    return ('<div style="padding: 14px; border-radius: 20px; background: %s; border: 1px solid %s;"><div style="font-size: 12px; color: %s;">%s</div>'
            '<div style="margin-top: 6px; %s font-size: 18px; font-weight: 700; color: %s;">%s</div><div style="font-size: 12px; color: %s;">%s</div></div>'
            % (CARD, border, WARN if warn else MUTED, title, MONO, color, value, MUTED, sub))


def tiles(*t):
    return '<div style="margin-top: 10px; display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 10px;">%s</div>' % ''.join(t)


def recent_card(items):
    return label('Последние операции') + card(''.join(tx(*r) for r in items))


SEARCH_BTN = icon_button('search', 'Поиск')


def home():
    return tab_screen('Обзор', balance_hero() + tiles(tile('Траты за ноябрь', '1 240,50', 'USD'), tile('Без категории', '14', 'операций'))
                      + recent_card(RECENT), 'home', SEARCH_BTN)


def home_empty():
    body = balance_hero('0,00', 'USD', 'Счетов пока нет') + empty('wallet', 'Счетов пока нет. Начните с того, где лежат деньги: карта, наличные, вклад.', 'Добавить счёт')
    return tab_screen('Обзор', body, 'home')


def home_attention():
    extra = ('<div style="margin-top: 10px; display: flex; align-items: center; gap: 6px; font-size: 12px; color: %s;">%s'
             'Без учёта: VES — нет курса</div>' % (WARN, svg('alert', WARN, 16)))
    body = (balance_hero(extra=extra) + tiles(tile('Траты за ноябрь', '1 240,50', 'USD · без VES'), tile('Внимание', '2', 'нет курса VES · курсы от 7 нояб.', WARN, True))
            + recent_card(RECENT[:3]))
    return tab_screen('Обзор', body, 'home', SEARCH_BTN)


def ops_head(chips=None):
    chips = chips or [chip('Тип ▾'), chip('Счёт ▾'), chip('Ноябрь ▾')]
    return search('Поиск: заметка, сумма, категория') + '<div style="display: flex; gap: 8px; margin-top: 12px;">%s</div>' % ''.join(chips)


def ops_empty():
    return tab_screen('Операции', ops_head() + empty('list', 'Операций пока нет. Нажмите «+», чтобы записать первую.'), 'ops')


def ops_nothing():
    body = (search('', 'кофе 500') + '<div style="display: flex; gap: 8px; margin-top: 12px;">%s</div>' % (chip('Расходы ✕', True) + chip('Galicia ✕', True) + chip('Октябрь ▾'))
            + empty('search', 'Ничего не нашлось') + '<div style="text-align: center; margin-top: 4px;"><span style="color: %s; font-size: 15px; font-weight: 600;">Сбросить фильтры</span></div>' % LIME)
    return tab_screen('Операции', body, 'ops')


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
        top = ('<div style="display: flex; align-items: center; gap: 8px; height: 64px; padding: 0 8px;">%s<span style="font-size: 22px; font-weight: 700;">Выбрано %d</span></div>'
               % (icon_button('close', 'Отменить выбор'), len(selected)))
        bar = ('<div style="position: absolute; left: 16px; right: 16px; bottom: 18px; height: 64px; border-radius: 999px; background: %s; display: flex; align-items: center; gap: 8px; padding: 0 8px; box-sizing: border-box;">'
               '<span style="flex: 1; height: 48px; border-radius: 999px; display: flex; align-items: center; justify-content: center; gap: 8px; color: %s; font-size: 15px;">%sДублировать</span>'
               '<span style="flex: 1; height: 48px; border-radius: 999px; background: %s; color: %s; display: flex; align-items: center; justify-content: center; gap: 8px; font-size: 15px; font-weight: 600;">%sУдалить · %d</span></div>'
               % (BAR, '#5C6757', svg('copy', '#5C6757', 20), PINKBG, PINK, svg('trash', PINK, 20), len(selected)))
        return top + '<div style="padding: 0 16px;">%s%s</div>' % (ops_head(), lst) + bar
    body = ops_head([chip('Расходы ✕', True), chip('Счёт ▾'), chip('Ноябрь ▾')]) + lst
    return tab_screen('Операции', body, 'ops') + (snackbar('Удалено операций: 3') if snack else '')


def accounts_empty():
    return tab_screen('Счета', empty('wallet', 'Счетов пока нет. Нажмите «+», чтобы добавить первый.', 'Добавить счёт'), 'acc')


def accounts_search():
    rows = [row('<b>Ga</b>licia', 'Карта', badge('GA', '#F26E22', '#1A0A00'), amount('1 250 000', 'ARS'))]
    body = (search('', 'ga') + label('Найдено: 1') + group(rows) + note('Архивные счета тоже ищутся — помечены «в архиве».'))
    return tab_screen('Счета', body, 'acc')


# ---- entry sheet ----------------------------------------------------------------------------------------------

def keypad(save='Готово'):
    k = lambda t, bg=KEY, col=INK, sz=22: ('<span style="border-radius: 16px; background: %s; color: %s; font-size: %dpx; %s display: flex; align-items: center; justify-content: center;">%s</span>'
                                           % (bg, col, sz, MONO, t))
    keys = (k('7') + k('8') + k('9') + k('⌫', KEY2, INK, 20) + k('4') + k('5') + k('6') + k('+', KEY2, LIME, 24) + k('1') + k('2') + k('3')
            + '<span style="grid-row: span 2; border-radius: 18px; background: %s; color: %s; font-size: 15px; font-weight: 700; display: flex; flex-direction: column; align-items: center; justify-content: center; gap: 4px;">%s%s</span>'
            % (LIME, ONLIME, svg('check', ONLIME, 26, 2.2), save) + k(',') + k('0') + k('000', KEY, INK, 18))
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


def entry_transfer():
    a, b = ACC[2], ACC[0]
    two = ('<div style="display: grid; grid-template-columns: 1fr 1fr; gap: 8px;">'
           '<div style="padding: 10px 12px; border-radius: 16px; background: %s; border: 2px solid %s;"><div style="font-size: 12px; color: %s;">Списано</div>%s</div>'
           '<div style="padding: 10px 12px; border-radius: 16px; background: %s; border: 1px solid %s;"><div style="font-size: 12px; color: %s;">Зачислено</div>%s</div></div>'
           % (BAR, LIME, MUTED, amount('500', 'USD', INK, 22), BAR, LINE2, MUTED, amount('512 000', 'ARS', INK, 22)))
    hint = '<div style="font-size: 12px; color: %s; padding: 0 4px;">Курс обмена: 1 USD = 1 024 ARS — считается из двух сумм</div>' % MUTED
    head = ('<div style="display: flex; align-items: center; gap: 6px; padding: 0 4px;">%s<span style="color: %s;">→</span>%s</div>'
            % (acc_chip(('Ameria USD',) + a[1:], 'откуда'), MUTED, acc_chip(b, 'куда')))
    return entry(segmented(['Расход', 'Доход', 'Перевод'], 'Перевод', 'lime') + head + two + hint + chips_row(chip('Сегодня ▾'), chip('＋ Заметка')) + keypad())


def entry_receipt():
    rc = ('<div style="padding: 10px 14px; border-radius: 16px; background: %s; border: 1px solid %s;"><div style="display: flex; justify-content: space-between; font-size: 12px; color: %s;">'
          '<span>Чек в другой валюте</span><span>✕</span></div><div style="display: flex; justify-content: space-between; align-items: baseline; margin-top: 4px;">%s'
          '<span style="font-size: 12px; color: %s;">Курс покупки: 1 USDT = 1 015 ARS</span></div></div>' % (BAR, LINE2, MUTED, amount('13 300', 'ARS', INK, 20), MUTED))
    head = '<div style="display: flex; align-items: center; gap: 8px; padding: 0 4px;">%s<span style="flex-grow: 1;"></span><span style="font-size: 13px; font-weight: 600; color: %s;">Расход</span></div>' % (acc_chip(ACC[3]), PINK)
    return entry(head + big_amount('13,10', 'USDT') + '<div style="text-align: center; font-size: 12px; color: %s; margin-top: -8px;">Списано со счёта</div>' % MUTED
                 + rc + cat_grid('Кафе') + chips_row(chip('Сегодня ▾'), chip('＋ Заметка')) + keypad())


def entry_edit():
    head = ('<div style="display: flex; align-items: center; gap: 8px; padding: 0 4px;">%s<span style="flex-grow: 1;"></span>%s%s</div>'
            % (acc_chip(ACC[0]), icon_button('copy', 'Дублировать', INK, True), icon_button('trash', 'Удалить', PINK, True)))
    meta = '<div style="text-align: center; font-size: 12px; color: %s; margin-top: -8px;">Записала Аня · 10 нояб., 14:20</div>' % MUTED
    return entry(head + big_amount('32 000', 'ARS') + meta + cat_grid('Продукты') + chips_row(chip('10 нояб. ▾'), chip('Coto, на неделю', False, 'note')) + keypad('Сохранить'))


def entry_error():
    head = '<div style="display: flex; align-items: center; gap: 8px; padding: 0 4px;">%s<span style="flex-grow: 1;"></span><span style="font-size: 13px; font-weight: 600; color: %s;">Расход</span></div>' % (acc_chip(ACC[0]), PINK)
    return entry(head + big_amount('4 500', 'ARS') + cat_grid(None, True) + chips_row(chip('Вчера ▾', True), chip('＋ Заметка'), chip('Чек в др. валюте')) + keypad())


def entry_no_accounts():
    inner = ('<div style="padding: 24px 20px;">' + segmented(['Расход', 'Доход', 'Перевод'], 'Расход', 'lime')
             + empty('wallet', 'Чтобы записать операцию, сначала добавьте счёт.', 'Добавить счёт') + '</div>')
    return scrim() + sheet(inner, 380)


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
                                                         row('Общий счёт', 'Выключите для личного — он останется виден всей семье', '', switch(False))]) + '</div>')
    return inner_screen('Счёт', body, acts) + form_footer()


def account_blocked():
    return (inner_screen('Счёт', field('Название', 'Galicia') + field('Валюта', 'ARS')) + scrim()
            + dialog('Счёт нельзя удалить', 'По счёту есть операции. Отправьте его в архив: история и баланс сохранятся, а в списках выбора он больше не появится.',
                     [('Отмена', 'text'), ('В архив', 'primary')]))


def account_delete():
    return (inner_screen('Счёт', field('Название', 'Старая карта') + field('Валюта', 'USD')) + scrim()
            + dialog('Удалить счёт?', 'Счёт без операций будет удалён безвозвратно.', [('Отмена', 'text'), ('Удалить', 'danger')]))


BOARDS = [
    # name, title, html-function
    ('Home', '1 · Обзор', home), ('HomeEmpty', '1 · Обзор: первый запуск', home_empty), ('HomeAttention', '1 · Обзор: нет курса', home_attention),
    ('OpsEmpty', '2 · Операции: пусто', ops_empty), ('OpsNothing', '2 · Операции: ничего не нашлось', ops_nothing),
    ('OpsSelect', '2 · Операции: выбор нескольких', lambda: ops_list((0, 1, 3))), ('OpsUndo', '2 · Операции: удалено, «Отменить»', lambda: ops_list(snack=True)),
    ('AccountsEmpty', '3 · Счета: пусто', accounts_empty), ('AccountsSearch', '3 · Счета: поиск', accounts_search),
    ('EntryTransfer', '5 · Шторка: перевод между валютами', entry_transfer), ('EntryReceipt', '5 · Шторка: чек в другой валюте', entry_receipt),
    ('EntryEdit', '5 · Шторка: правка операции', entry_edit), ('EntryError', '5 · Шторка: не выбрана категория', entry_error),
    ('EntryNoAccounts', '5 · Шторка: нет счетов', entry_no_accounts),
    ('CategoryBlocked', '7 · Категория: удалить нельзя', category_blocked),
    ('AccountEditExisting', '9 · Счёт: правка, ошибки', account_existing), ('AccountDelete', '9 · Счёт: удалить?', account_delete),
    ('AccountBlocked', '9 · Счёт: удалить нельзя', account_blocked),
]
