"""Generated boards: small conveniences of story #317 (#318–#326), variants for the owner's poll.

Imported by mockup_screens_feedback.py (FEEDBACK + BOARDS). Data: USDT card paying pesos, cash tips, Аня as the partner.
The entry sheet is the approved #290 layout (fb_290.frame/block), so new bits must still fit without scrolling.
"""
from mockup_bricks import *  # noqa: F401,F403
from mockup_screens_main import ACC, acc_chip, CATS, RECENT, balance_hero, keypad, recent_card, tile, tiles
from mockup_screens_fb_290 import all_cell, amount_block, block, cat_cell, detail, details_block, frame, grid4, head, refund_check, small_link

FEEDBACK = [
    (318, 'Задача #318 · долгое нажатие на сумму — она же в других валютах',
     'Курс дня операции (у баланса — сегодня), все валюты отчётов. Рекомендую А: пузырь у самой суммы, отпустил палец — исчез.'),
    (319, 'Задача #319 · конвертер на «Курсах»', 'Сумма — своей клавиатурой. Рекомендую А: конвертер прямо в карточке сверху экрана «Курсы».'),
    (320, 'Задача #320 · «Поделиться» текстом → ввод', 'Рекомендую А: сразу шторка ввода с суммой, исходный текст — в заметке, его можно поправить.'),
    (321, 'Задача #321 · сумма из буфера', 'Рекомендую А: без системной плашки Android «приложение получило доступ к буферу».'),
    (322, 'Задача #322 · чаевые с другого счёта', 'Отдельная операция той же категории и даты. Рекомендую А: «＋ Чаевые» в блоке суммы.'),
    (323, 'Задача #323 · черновик не теряется', 'Рекомендую А: плашка «Продолжить» — шторка не открывается сама с чужой суммой.'),
    (324, 'Задача #324 · экраном вниз — суммы скрыты', 'Рекомендую А: «•••» читается однозначно и не грузит телефон (размытие дороже).'),
    (325, 'Задача #325 · встряхнуть после сохранения — отменить', 'Рекомендую А: подсказка в снэкбаре, иначе жест никто не найдёт.'),
    (326, 'Задача #326 · крупная трата второго участника', 'Без сервера: при синке. Рекомендую А: одно уведомление на трату от порога.'),
]


def bubble(lines, top, left=120, arrow=36):
    rows = ''.join('<div style="display: flex; justify-content: space-between; gap: 18px; %s font-size: 15px;"><span>%s</span><span style="color: %s;">%s</span></div>'
                   % (MONO, a, MUTED, c) for a, c in lines)
    return ('<div style="position: absolute; left: %dpx; right: 16px; top: %dpx; padding: 12px 14px; border-radius: 16px; background: %s; '
            'box-shadow: 0 12px 32px rgba(0,0,0,0.5); border: 1px solid %s; display: flex; flex-direction: column; gap: 4px;">%s'
            '<div style="font-size: 11px; color: %s; margin-top: 4px;">по курсу 12 нояб. · ARS официальный</div>'
            '<span style="position: absolute; bottom: -7px; right: %dpx; width: 12px; height: 12px; background: %s; transform: rotate(45deg); '
            'border-right: 1px solid %s; border-bottom: 1px solid %s;"></span></div>' % (left, top, CHIP, LINE2, rows, MUTED, arrow, CHIP, LINE2, LINE2))


CONV = [('≈ 31,53', 'USD'), ('≈ 12 190', 'AMD'), ('≈ 2 980', 'RUB'), ('≈ 31,56', 'USDT')]


def ops_rows():
    rows = ''.join(tx(*r) for r in RECENT[:2]) + day('ВЧЕРА', '−6 500', 'ARS') + ''.join(tx(*r) for r in RECENT[2:])
    return '<div style="margin: 14px -16px 0;">%s%s</div>' % (day('СЕГОДНЯ', '−45 100', 'ARS'), rows)


def ops_screen(extra=''):
    return tab_screen('Операции', search('Поиск: заметка, сумма, категория') + ops_rows(), 'ops') + extra


def conv_a():
    hl = '<div style="position: absolute; left: 0; right: 0; top: 174px; height: 64px; background: rgba(184,255,46,0.07);"></div>'
    return ops_screen(hl + bubble(CONV, 64))


def conv_b():
    lines = ''.join(row('<span style="%s">%s</span>' % (MONO, a), '', '', '<span style="color: %s;">%s</span>' % (MUTED, c), min_h=48) for a, c in CONV)
    inner = ('<div style="padding: 8px 16px;"><div style="font-size: 13px; color: %s;">Продукты · Galicia · сегодня</div>'
             '<div style="%s font-size: 30px; font-weight: 700; color: %s; margin: 4px 0 8px;">−32 000 ARS</div>%s%s</div>'
             % (MUTED, MONO, PINK, card(lines), note('По курсу 12 нояб. · ARS официальный')))
    return ops_screen(scrim() + sheet(inner, 420))


RATES = [('USD', '100,00', '1 USD'), ('ARS', '101 500', '1 015,00'), ('AMD', '38 700', '387,00'), ('RUB', '9 150', '91,50'),
         ('USDT', '100,10', '0,999'), ('EUR', '92,40', '0,924'), ('BRL', '566,20', '5,662')]


def cur_row(code, value, rate, first=False):
    lead = badge(code[:2], CHIP, INK, 36, 18)
    val = '<span style="%s font-size: 17px; font-weight: 700; color: %s;">%s</span>' % (MONO, LIME if first else INK, value)
    return row(code, '1 USD = %s %s' % (rate, code) if not first else 'исходная · тап по строке — сделать исходной', lead, val, min_h=56)


def converter_a():
    hero = hero_card('<div style="font-size: 13px;">Сколько это</div><div style="display: flex; align-items: baseline; gap: 8px; margin-top: 4px;">'
                     '<span style="%s font-size: 34px; font-weight: 700; color: %s;">100</span><span style="%s font-size: 18px;">USD ▾</span>'
                     '<span style="flex-grow: 1;"></span><span style="font-size: 12px;">курс на сегодня</span></div>' % (MONO, LIME, MONO), extra='margin-top: 4px;')
    rows = ''.join(cur_row(c, v, r) for c, v, r in RATES[1:])
    return inner_screen('Курсы валют', hero + label('В валютах отчётов и курсы') + card(rows), icon_button('refresh', 'Обновить'))


def converter_b():
    top = ('<div style="display: flex; align-items: baseline; justify-content: center; gap: 8px; margin: 8px 0;">'
           '<span style="%s font-size: 40px; font-weight: 700; color: %s;">100</span><span style="%s font-size: 18px; color: %s;">USD ▾</span></div>' % (MONO, LIME, MONO, MUTED))
    rows = ''.join(cur_row(c, v, r) for c, v, r in RATES[1:5])
    body = top + card(rows) + '<div style="position: absolute; left: 12px; right: 12px; bottom: 14px;">%s</div>' % keypad('Готово')
    return inner_screen('Конвертер', body)


def shared_quote(text):
    return ('<div style="display: flex; gap: 8px; align-items: flex-start; padding: 8px 10px; border-radius: 12px; background: %s; font-size: 12px; color: %s;">%s'
            '<span>%s</span></div>' % (BAR, MUTED, svg('download', MUTED, 16), text))


QUOTE = 'Из «Поделиться»: Compra con tarjeta <b style="color: %s;">$12.500,00</b> en COTO SUC 115 — Galicia' % INK


def plain_details(note_text=''):
    cells = (detail('calendar', 'Дата', 'Сегодня, 13 нояб.') + detail('note', 'Заметка', note_text or '—', bool(note_text))
             + detail('spark', 'Теги', '—', False) + refund_check())
    return block('Детали', '<div style="display: grid; grid-template-columns: 1fr 1fr; gap: 6px;">%s</div>' % cells)


def entry_from(amount_html, extra_top='', extra_bottom='', cat='Продукты', det=None, acc='USDT-карта'):
    cats = [cat_cell(n, ic, col, n == cat) for n, ic, col in CATS[:7]] + [all_cell('Все')]
    cat_b = block('Категория · частые', grid4(cats), right=small_link(cat + ' ✓') if cat else '')
    det = det if det is not None else details_block()
    top = head().replace(acc_chip(ACC[3]), acc_chip(next(a for a in ACC if a[0] == acc)))
    return frame(top + extra_top + amount_html + cat_b + det + '<div style="flex-grow: 1;"></div>' + extra_bottom + keypad())


def simple_amount(value, cur='ARS', color=PINK, right=''):
    big = ('<div style="display: flex; align-items: baseline; justify-content: center; gap: 8px;"><span style="%s font-size: 40px; font-weight: 700; color: %s;">%s</span>'
           '<span style="%s font-size: 16px; color: %s;">%s</span></div>' % (MONO, color, value, MONO, MUTED, cur))
    return block('Сумма', big, right=right)


def share_a():
    return entry_from(simple_amount('12 500'), shared_quote(QUOTE), det=plain_details('COTO SUC 115'), acc='Galicia')


def share_b():
    body = ('<div style="font-size: 14px; color: %s;">Compra con tarjeta $12.500,00 en COTO SUC 115</div>'
            '<div style="margin-top: 14px;">%s%s%s</div>' % (INK2, row('Сумма', '', '', '<b style="%s">12 500 ARS</b>' % MONO, min_h=44),
                                                              row('Заметка', '', '', 'COTO SUC 115', min_h=44), row('Счёт', '', '', 'Galicia', min_h=44)))
    return ops_screen(scrim() + dialog('Нашли в тексте', '', [('Отмена', 'text'), ('Внести расход', 'primary')], body))


def paste_pill(text):
    return ('<div style="display: flex; justify-content: center;"><span style="height: 36px; padding: 0 14px; border-radius: 999px; background: %s; color: %s; '
            'font-size: 13px; font-weight: 600; display: inline-flex; align-items: center; gap: 6px;">%s%s</span></div>' % (LIMEPILL, LIME, svg('copy', LIME, 16), text))


def clip_a():
    return entry_from(simple_amount('0', color=MUTED2), extra_bottom=paste_pill('Вставить из буфера'), cat='', det=plain_details())


def clip_b():
    toast = ('<div style="position: absolute; left: 50%; bottom: 300px; transform: translateX(-50%); padding: 10px 16px; border-radius: 999px; background: #3A3F38; '
             'font-size: 12px; white-space: nowrap;">co-shell получил доступ к буферу обмена</div>')
    return entry_from(simple_amount('0', color=MUTED2), extra_bottom=paste_pill('Вставить 12 500'), cat='', det=plain_details()) + toast


def tip_line():
    return ('<div style="display: flex; align-items: center; gap: 8px; margin-top: 6px; padding-top: 8px; border-top: 1px dashed %s; font-size: 12px; color: %s;">'
            '<span>Чаевые 10 %%</span><span style="%s font-size: 15px; color: %s; font-weight: 700;">1 330 ARS</span><span style="flex-grow: 1;"></span>'
            '<span>с «Наличные ARS»</span>%s</div>' % (LINE2, MUTED, MONO, INK, svg('close', MUTED, 14)))


def tip_amount(with_tip):
    html = amount_block()
    link = small_link('＋ Чаевые')
    html = html.replace('<span style="flex-grow: 1;">Сумма · списано</span>', '<span style="flex-grow: 1;">Сумма · списано</span>' + link, 1)
    if with_tip:
        html = html[:-6] + tip_line() + html[-6:]
    return html


def tips_after():
    return entry_from(tip_amount(True), cat='Кафе')


def tips_sheet():
    pct = '<div style="display: flex; gap: 8px;">%s</div>' % (chip('5 %') + chip('10 %', True) + chip('15 %') + chip('Своя сумма'))
    accs = ('<div style="display: flex; gap: 8px; flex-wrap: wrap;">%s</div>'
            % (chip('Наличные ARS', True, dot='#C9D3C2') + chip('Mercado Pago', dot='#00B1EA') + chip('Galicia', dot='#F26E22') + chip('Наличные USD', dot='#C9D3C2')))
    inner = ('<div style="padding: 8px 16px;"><div style="display: flex; align-items: center; height: 48px;"><b style="flex-grow: 1; font-size: 20px;">Чаевые</b>'
             '<span style="color: %s; font-weight: 600;">Готово</span></div>'
             '<div style="%s font-size: 34px; font-weight: 700; color: %s; text-align: center; margin: 4px 0 10px;">1 330 <span style="font-size: 16px; color: %s;">ARS</span></div>'
             '%s%s%s%s%s</div>' % (LIME, MONO, PINK, MUTED, pct, label('С какого счёта', top=14), accs,
                                   label('Как сохранится', top=14), note('Отдельный расход «Кафе», сегодня, заметка «Чаевые». 10 % от чека 13 300 ARS. '
                                                                         'Счёт чаевых запомнится для следующего раза.', mt=4)))
    return entry_from(tip_amount(False), cat='Кафе') + scrim() + sheet(inner, 360)


def tips_b():
    return ops_screen(snackbar('Сохранено: Кафе −13,10 USDT', '＋ Чаевые'))


def draft_banner():
    return ('<div style="display: flex; align-items: center; gap: 10px; padding: 8px 8px 8px 12px; border-radius: 14px; background: %s; border: 1px solid %s;">'
            '%s<span style="display: flex; flex-direction: column; flex-grow: 1; min-width: 0;"><span style="font-size: 13px;">Не сохранено · 5 мин назад</span>'
            '<span style="%s font-size: 13px; color: %s;">3 500 ARS · Кафе</span></span>'
            '<span style="height: 34px; padding: 0 12px; border-radius: 999px; background: %s; color: %s; font-size: 13px; font-weight: 600; display: flex; align-items: center;">Продолжить</span>%s</div>'
            % (WARNBG, WARNLINE, svg('note', WARN, 18), MONO, INK2, LIME, ONLIME, svg('close', MUTED, 18)))


def draft_a():
    return entry_from(simple_amount('0', color=MUTED2), draft_banner(), cat='', det=plain_details())


def draft_b():
    return entry_from(simple_amount('3 500', right=small_link('Черновик · Очистить')), cat='Кафе', det=plain_details('с Аней'))


HIDDEN = '•••'


def home_hidden(style):
    if style == 'dots':
        hero = balance_hero(HIDDEN, 'USD', '≈ ••• ARS · ••• AMD')
        t = tiles(tile('Траты за ноябрь', HIDDEN, 'USD'), tile('Бюджет · 17 дней', HIDDEN, 'осталось'))
        rec = recent_card([(r[0], r[1], HIDDEN, r[3], r[4], r[5], r[6], '') for r in RECENT])
    else:
        hero = balance_hero().replace('%s font-size: 34px' % MONO, 'filter: blur(9px); %s font-size: 34px' % MONO)
        t = tiles(tile('Траты за ноябрь', '1 240,50', 'USD'), tile('Бюджет · 17 дней', '412 000', 'осталось')).replace('%s font-size: 18px' % MONO, 'filter: blur(7px); %s font-size: 18px' % MONO)
        rec = recent_card(RECENT).replace(MONO, 'filter: blur(6px); ' + MONO)
    pill_ = ('<div style="position: absolute; left: 50%%; bottom: 100px; transform: translateX(-50%%); height: 36px; padding: 0 14px; border-radius: 999px; '
             'background: %s; font-size: 13px; display: flex; align-items: center; gap: 6px; white-space: nowrap;">%sСуммы скрыты · дважды тап по балансу — показать</div>'
             % (CHIP, svg('lock', LIME, 16)))
    return tab_screen('Обзор', hero + t + rec, 'home') + pill_


def hidden_settings():
    rows = (row('Встряхнуть — новая операция', 'пока приложение открыто', tile_icon('bolt'), switch(True))
            + row('Экраном вниз — скрыть суммы', 'поднимите телефон — суммы вернутся', tile_icon('lock'), switch(True))
            + row('Встряхнуть после сохранения — отменить', 'пока виден снэкбар «Сохранено»', tile_icon('refresh'), switch(True)))
    return inner_screen('Внешний вид', label('Жесты') + card(rows) + note('Двойной тап по общему балансу на «Обзоре» прячет и показывает суммы вручную.'))


def undo_a():
    return ops_screen(snackbar('Сохранено · встряхните — отменить', 'Отменить'))


def undo_b():
    return ops_screen(snackbar('Сохранено: Кафе −13,10 USDT', 'Отменить'))


def notif(title, text, when='сейчас'):
    return ('<div style="margin-top: 10px; padding: 12px 14px; border-radius: 20px; background: #20271C;"><div style="display: flex; align-items: center; gap: 8px; font-size: 12px; color: %s;">'
            '<span style="width: 18px; height: 18px; border-radius: 50%%; background: %s;"></span>co-shell · %s</div>'
            '<div style="margin-top: 6px; font-size: 15px; font-weight: 600;">%s</div><div style="font-size: 14px; color: %s;">%s</div></div>' % (MUTED, LIME, when, title, INK2, text))


def lock_screen(items):
    clock = '<div style="text-align: center; margin: 40px 0 24px;"><div style="font-size: 64px; font-weight: 300;">21:47</div><div style="color: %s;">пятница, 13 ноября</div></div>' % MUTED
    return '<div style="padding: 0 16px;">%s%s</div>' % (clock, ''.join(items))


def partner_a():
    return lock_screen([notif('Аня: −80 000 ARS', 'Техника · Galicia · «наушники»'), notif('Аня: −52 300 ARS', 'Продукты · Mercado Pago', '2 ч назад')])


def partner_a_set():
    rows = (row('Траты другого участника', 'уведомлять о каждой от порога', tile_icon('bell') if 'bell' in ICON else tile_icon('user'), switch(True))
            + row('Порог', 'в основной валюте, по курсу дня', '', '<b style="%s">50 000 ARS</b>' % MONO))
    return inner_screen('Семья', label('Уведомления') + card(rows) + note('Без сервера: уведомление приходит при синхронизации — сразу, если приложение открыто, '
                                                                          'иначе фоновым синком (раз в час). Переводы, импорт и свои операции — не сообщаются.'))


def partner_b():
    return lock_screen([notif('Аня сегодня: 4 траты', '−142 300 ARS · больше всего — Техника', '21:00')])


BOARDS = [  # name, title, fn, issue number
    ('Fb318A', 'А: долгое нажатие — пузырь у суммы', conv_a, 318),
    ('Fb318B', 'Б: долгое нажатие — шторка снизу', conv_b, 318),
    ('Fb319A', 'А: конвертер в карточке сверху «Курсов»', converter_a, 319),
    ('Fb319B', 'Б: отдельный экран «Конвертер» с клавиатурой', converter_b, 319),
    ('Fb320A', 'А: «Поделиться» — сразу шторка ввода', share_a, 320),
    ('Fb320B', 'Б: «Поделиться» — сначала окно «Нашли в тексте»', share_b, 320),
    ('Fb321A', 'А: «Вставить из буфера» — читаем по нажатию', clip_a, 321),
    ('Fb321B', 'Б: число на пилюле — Android показывает плашку', clip_b, 321),
    ('Fb322A', 'А: «＋ Чаевые» в блоке суммы → шторка', tips_sheet, 322),
    ('Fb322AAfter', 'А: чаевые добавлены строкой под суммой', tips_after, 322),
    ('Fb322B', 'Б: чаевые — из снэкбара после сохранения', tips_b, 322),
    ('Fb323A', 'А: черновик — плашка «Продолжить»', draft_a, 323),
    ('Fb323B', 'Б: черновик подставляется сам', draft_b, 323),
    ('Fb324A', 'А: экраном вниз — «•••»', lambda: home_hidden('dots'), 324),
    ('Fb324B', 'Б: экраном вниз — размытие', lambda: home_hidden('blur'), 324),
    ('Fb324Set', 'Общее: выключатели жестов во «Внешнем виде»', hidden_settings, 324),
    ('Fb325A', 'А: снэкбар подсказывает жест', undo_a, 325),
    ('Fb325B', 'Б: снэкбар без подсказки', undo_b, 325),
    ('Fb326A', 'А: уведомление на каждую трату от порога', partner_a, 326),
    ('Fb326ASet', 'А: порог в «Семье»', partner_a_set, 326),
    ('Fb326B', 'Б: сводка раз в день вечером', partner_b, 326),
]
