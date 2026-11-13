"""Generated boards: variants for #180 (more account types), #208 (refund — income in an expense category) and
#284 (past versions on the update screen).

Imported by mockup_screens_feedback.py (FEEDBACK + BOARDS). Long lists on purpose (owner 2026-11-11): 12+ accounts,
20 versions.
"""
from mockup_plan_bricks import *  # noqa: F401,F403
from mockup_parts import ACCOUNTS, acc_row, big_total, footer, grid, group_head, top_bar
from mockup_screens_main import ACC, CATS, acc_chip, big_amount, chips_row, entry, keypad

FEEDBACK = [
    (180, 'Отзыв #180 · больше типов счетов: сберегательный, кредитная карта…',
     'Было 5 типов (Наличные, Карта, Вклад, Крипто, Другое). Станет 10: + Сберегательный, Кредитная карта, Кредит, Инвестиции, Электронный кошелёк. '
     'Кредитная карта и кредит — «долги»: баланс со знаком минус, у карты — лимит и «доступно». Выбрать: А — сетка из 10 значков; '
     'Б — два раздела «Свои деньги» и «Долги» (долги по умолчанию не входят в общий баланс). Рекомендую Б: сразу видно, что кредитка — это долг.'),
    (208, 'Задача #208 · возврат: доход в категории расходов',
     'Выбрано в #207: возврат уменьшает траты категории в бюджетах и отчётах. Показ одинаковый: в списке — «возврат», сумма цветом дохода; '
     'в конверте бюджета — вычитается из потраченного. Выбрать, как ввести: А — в «Доходе» под доходными категориями раздел «Возврат в категорию»; '
     'Б — четвёртая вкладка «Возврат»; В — чип «↩ Возврат» в шторке расхода. Рекомендую В: возврат вводят, глядя на ту же категорию, что и покупку.'),
    (284, 'Задача #284 · экран обновления: прошлые версии',
     'Под «Что в этой версии» — строка «Прошлые версии» → экран со списком выпусков (последние 20). Выбрать: А — карточки, последняя раскрыта, '
     'остальные — тап раскрывает; Б — сплошная лента по месяцам, все пункты видны сразу. Рекомендую А: 20 версий × 3–6 пунктов — лента слишком длинная.'),
]

# ---- #180 · account types --------------------------------------------------------------------------------------------
CARD_ICON = 'M3 6h18v12H3zM3 10h18M7 15h4'
PIG = 'M5 11a7 6 0 0 1 13-2h2v4l-2 1v3h-3v-2H9v2H6v-3a6 6 0 0 1-1-3zM14 9h.01'
DEBT = 'M4 20h16M6 16V9M10 16V9M14 16V9M18 16V9M3 9l9-5 9 5'
INVEST = 'M4 18l5-6 4 3 7-9M15 6h5v5'
PHONEW = 'M7 3h10v18H7zM10 7h4M11 18h2'
OWN = [('Наличные', 'в кошельке', 'wallet'), ('Карта', 'дебетовая', CARD_ICON), ('Сберега&shy;тельный', 'процент на остаток', PIG), ('Вклад', 'до даты', 'lock'),
       ('Инвестиции', 'брокер, акции', INVEST), ('Крипто', 'биржа, кошелёк', 'bolt'), ('Эл. кошелёк', 'Mercado Pago, PayPal', PHONEW), ('Другое', '', 'more')]
DEBTS = [('Кредитная карта', 'лимит и долг', CARD_ICON), ('Кредит', 'ипотека, рассрочка', DEBT)]


def type_cell(name, icon, on=False):
    st = ('background: %s; border: 2px solid %s;' % (LIMEPILL, LIME)) if on else ('background: %s; border: 1px solid %s;' % (CARD, LINE))
    return ('<span style="display: flex; flex-direction: column; align-items: center; justify-content: center; gap: 6px; height: 76px; border-radius: 16px; %s '
            'font-size: 12px; color: %s; text-align: center; padding: 0 4px; hyphens: manual;">%s%s</span>' % (st, INK if on else INK2, svg(icon, LIME if on else MUTED, 24), name))


def editor_top(body, title='Счёт'):
    return top_bar(title, 'close') + '<div style="padding: 0 16px;">%s</div>' % body + footer(button('Сохранить', mt=0))


def types_a():
    cells = [type_cell(n, i, n == 'Кредитная карта') for n, _, i in OWN[:7] + DEBTS + OWN[7:]]
    body = (field('Название', 'Galicia Visa') + grid([field('Валюта', 'ARS', mono=True), field('Долг сейчас', '−186 400', mono=True)], 2)
            + label('Тип · 10') + grid(cells, 4, 8)
            + field('Кредитный лимит', '900 000', 'Покажем «доступно 713 600» и сколько занято', mono=True, trail=chip('ARS')))
    return editor_top(body)


def types_b():
    def tline(n, sub, icon, on=False):
        mark = svg('check', LIME, 20, 2.4) if on else ''
        return row(n, sub, '<span style="width: 36px; height: 36px; border-radius: 11px; background: %s; display: flex; align-items: center; justify-content: center;">%s</span>'
                   % (CHIP, svg(icon, LIME if on else MUTED, 20)), mark, min_h=52)
    body = (field('Название', 'Galicia Visa') + label('Свои деньги') + group([tline(n, s, i) for n, s, i in OWN if n != 'Другое'])
            + label('Долги · не входят в общий баланс') + group([tline(n, s, i, n == 'Кредитная карта') for n, s, i in DEBTS]))
    return top_bar('Тип счёта', 'back') + '<div style="padding: 0 16px;">%s</div>' % body


def credit_row(name, ini, col, ink, debt, limit, cur, frac):
    bar = ('<div style="margin-top: 6px; height: 4px; border-radius: 2px; background: %s; overflow: hidden;"><div style="width: %d%%; height: 100%%; background: %s;"></div></div>'
           % (LINE2, round(frac * 100), WARN if frac > 0.6 else LIME))
    sub = '<span style="font-size: 12px; color: %s;">свободно %s</span>%s' % (MUTED, limit[0], bar)
    return ('<div style="display: flex; align-items: center; gap: 12px; padding: 10px 14px; {{div}}">%s<span style="flex-grow: 1; min-width: 0; display: flex; '
            'flex-direction: column;"><span style="font-size: 15px;">%s</span>%s</span>%s</div>' % (badge(ini, col, ink, 40, 12), name, sub, amount(debt, cur, PINK)))


def accounts_types():
    own = [a for a in ACCOUNTS if a[8] in ('cards', 'cash')][:6]
    debts = group([credit_row('Galicia Visa', 'GA', '#F26E22', '#1A0A00', '−186 400', ('713 600', '900 000'), 'ARS', 0.21),
                   credit_row('Santander Amex', 'SA', '#EC0000', '#FFFFFF', '−540 000', ('160 000', '700 000'), 'ARS', 0.77),
                   credit_row('Т-Банк Платинум', 'Т', '#FFDD2D', '#1A1400', '−12 300', ('87 700', '100 000'), 'RUB', 0.12)])
    savings = group([row('Ameria Saving', 'Сберегательный · 7% годовых', badge('AM', '#B01E2F', '#FFFFFF', 40, 12), amount('1 200 000', 'AMD')),
                     row('IOL Inversiones', 'Инвестиции', badge('IO', '#1E88E5', '#FFFFFF', 40, 12), amount('2 400', 'USD'))])
    body = (big_total('Всего в общем балансе · 14 счетов', '9 640', 'USD') + '<div style="margin-top: 4px; font-size: 13px; color: %s;">долги отдельно: −760 USD</div>' % PINK
            + group_head('Карты и наличные', 6, '4 693') + group([acc_row(a) for a in own[:3]]) + group_head('Сбережения', 2, '5 520') + savings
            + group_head('Долги', 3, '−760') + debts)
    return tab_screen('Счета', body, 'acc')


# ---- #208 · refund ---------------------------------------------------------------------------------------------------
REFUND = 'M9 14L4 9l5-5M4 9h10a6 6 0 0 1 0 12h-3'


def head(kind_txt, color, a=ACC[0]):
    return ('<div style="display: flex; align-items: center; gap: 8px; padding: 0 4px;">%s<span style="flex-grow: 1;"></span>'
            '<span style="font-size: 13px; font-weight: 600; color: %s;">%s</span></div>' % (acc_chip(a), color, kind_txt))


def mini_cats(items, selected=None, cols=4):
    out = ''
    for n, ic, col in items:
        ring = 'padding: 3px; border-radius: 50%%; box-shadow: 0 0 0 2px %s;' % CYAN if n == selected else 'padding: 3px;'
        out += ('<span style="display: flex; flex-direction: column; align-items: center; gap: 4px;"><span style="%s">%s</span><span style="font-size: 12px; color: %s;">%s</span></span>'
                % (ring, dot_icon(ic, col, 36), INK if n == selected else INK2, n))
    return '<div style="display: grid; grid-template-columns: repeat(%d, minmax(0, 1fr)); gap: 4px;">%s</div>' % (cols, out)


def sec(text):
    return '<div style="font-size: 12px; font-weight: 600; color: %s; padding: 0 4px; margin-bottom: -6px;">%s</div>' % (MUTED, text)


INC = [('Зарплата', 'salary', CYAN), ('Подарки', 'gift', VIOLET), ('Кэшбэк', 'chart', LIME), ('Другое', 'more', MUTED)]


def refund_a():
    return entry(segmented(['Расход', 'Доход', 'Перевод'], 'Доход', 'lime') + head('Доход', CYAN) + big_amount('+4 500', 'ARS', CYAN, 44)
                 + sec('Доходы') + mini_cats(INC) + sec('↩ Возврат в категорию расходов') + mini_cats(CATS[:4], 'Кафе')
                 + chips_row(chip('Сегодня ▾'), chip('＋ Заметка')) + keypad())


def refund_b():
    hint = '<div style="font-size: 12px; color: %s; padding: 0 4px; margin-top: -6px;">Вернём в траты категории: в бюджете «Кафе» станет на 4 500 меньше</div>' % MUTED
    return entry(segmented(['Расход', 'Доход', 'Перевод', 'Возврат'], 'Возврат', 'lime') + head('Возврат', CYAN) + big_amount('+4 500', 'ARS', CYAN, 44)
                 + mini_cats(CATS, 'Кафе') + hint + chips_row(chip('Сегодня ▾'), chip('＋ Заметка')) + keypad())


def refund_c():
    on = ('<span style="height: 36px; padding: 0 14px; border-radius: 999px; background: #062224; border: 1px solid %s; color: %s; font-size: 13px; font-weight: 600; '
          'display: inline-flex; align-items: center; gap: 6px;">%sВозврат</span>' % (CYAN, CYAN, svg(REFUND, CYAN, 16)))
    hint = '<div style="font-size: 12px; color: %s; padding: 0 4px; margin-top: -6px;">Возврат: деньги пришли на счёт, траты «Кафе» уменьшатся</div>' % MUTED
    return entry(head('Расход → возврат', CYAN) + big_amount('+4 500', 'ARS', CYAN, 44) + mini_cats(CATS, 'Кафе') + hint
                 + chips_row(chip('Сегодня ▾'), on, chip('＋ Заметка')) + keypad())


def refund_show():
    from mockup_bricks import tx
    ret = ('<div style="display: flex; align-items: center; gap: 12px; min-height: 64px; padding: 0 16px; background: #071A1C;">%s'
           '<span style="display: flex; flex-direction: column; flex-grow: 1;"><span style="font-size: 16px;">Кафе</span><span style="font-size: 13px; color: %s;">'
           '<span style="color: %s;">↩ возврат</span> · Galicia</span></span>%s</div>' % (dot_icon('cafe', WARN), MUTED, CYAN, amount('+4 500', 'ARS', CYAN)))
    top = hero_card('<div style="display: flex; align-items: baseline; justify-content: space-between;"><span style="%s font-size: 26px; font-weight: 700; color: %s;">−10 500 '
                    '<span style="font-size: 14px;">ARS</span></span><span style="font-size: 12px; color: %s;">перерасход</span></div>' % (MONO, PINK, HERO_HINT)
                    + bullet(1.13, 'over', h=10, mt=12)
                    + caption('Потрачено 90 500 из 80 000 · 95 000 трат − 4 500 возврат', 'over'), pad='16px')
    ops = card(ret + tx('Кафе', 'Galicia · Café Martínez', '−12 000', 'ARS', 'e', 'cafe', WARN) + tx('Кафе', 'USDT-карта · Аня', '−13,10', 'USDT', 'e', 'cafe', WARN, 'чек 13 300 ARS')
               + tx('Кафе', 'Наличные', '−6 800', 'ARS', 'e', 'cafe', WARN))
    body = top + label('Операции за ноябрь · 14') + ops + note('В «Операциях» строка возврата такая же; «Траты за ноябрь» на «Обзоре» и отчёты тоже вычитают возврат.')
    return inner_screen('Кафе · ноябрь', body)


# ---- #284 · past versions --------------------------------------------------------------------------------------------
VERSIONS = [('0.13', '12 нояб.', ['Плавная смена темы', 'Поиск — пилюлей сверху', 'Приложение легче: 8,0 → 5,5 МБ']),
            ('0.12', '10 нояб.', ['Журнал обновления уходит в отзыв', 'Кнопка «Установить вручную»', 'Журнал сбоев']),
            ('0.11', '8 нояб.', ['Бюджеты: конверты и лимиты', 'Плитка бюджета на «Обзоре»', 'Проверка целостности', 'Причины ошибок «Семьи»']),
            ('0.10', '5 нояб.', ['Шаблоны операций', 'Импорт из Wallet: переводы']), ('0.9', '3 нояб.', ['Резервная копия по расписанию', 'Ручной курс']),
            ('0.8', '1 нояб.', ['Синхронизация на двоих']), ('0.7', '29 окт.', ['Счета по группам', 'Архив счетов', 'Цвета банков']),
            ('0.6', '27 окт.', ['Категории и папки']), ('0.5', '24 окт.', ['Курсы валют: ARS, AMD, USDT'])]


def ver_card(v, date, items, open_, mine=False):
    badge_ = ('<span style="height: 22px; padding: 0 8px; border-radius: 999px; background: %s; color: %s; font-size: 11px; font-weight: 600; display: inline-flex; '
              'align-items: center;">у вас</span>' % (LIMEPILL, LIME)) if mine else ''
    head_ = ('<div style="display: flex; align-items: center; gap: 8px;"><span style="%s font-size: 17px; font-weight: 700;">%s</span>%s<span style="flex-grow: 1;"></span>'
             '<span style="font-size: 12px; color: %s;">%s · %d</span>%s</div>' % (MONO, v, badge_, MUTED, date, len(items), svg('down' if open_ else 'chev', MUTED, 18)))
    body = ('<ul style="margin: 8px 0 0; padding-left: 18px; font-size: 13px; line-height: 1.5; color: %s;">%s</ul>' % (INK2, ''.join('<li>%s</li>' % t for t in items))
            if open_ else '<div style="margin-top: 4px; font-size: 13px; color: %s; white-space: nowrap; overflow: hidden; text-overflow: ellipsis;">%s</div>' % (MUTED, items[0] + (' · …' if len(items) > 1 else '')))
    return card(head_ + body, pad='12px 14px', extra='margin-top: 8px;')


def versions_a():
    body = (note('20 последних выпусков · тап раскрывает', 0) + ''.join(ver_card(v, d, it, i == 0, i == 0) for i, (v, d, it) in enumerate(VERSIONS)))
    return inner_screen('Прошлые версии', body)


def versions_b():
    def block(v, d, items, mine=False):
        dot = '<span style="position: absolute; left: -21px; top: 4px; width: 10px; height: 10px; border-radius: 50%%; background: %s;"></span>' % (LIME if mine else LINE2)
        return ('<div style="position: relative; padding: 0 0 12px;">%s<div style="display: flex; gap: 8px; align-items: baseline;"><span style="%s font-size: 15px; font-weight: 700; '
                'color: %s;">%s</span><span style="font-size: 12px; color: %s;">%s%s</span></div><div style="font-size: 13px; line-height: 1.5; color: %s;">%s</div></div>'
                % (dot, MONO, LIME if mine else INK, v, MUTED, d, ' · у вас' if mine else '', INK2, '<br>'.join('— ' + t for t in items)))
    lane = ''.join(block(v, d, it, i == 0) for i, (v, d, it) in enumerate(VERSIONS[:7]))
    body = (label('Ноябрь', True, 4) + '<div style="margin-left: 22px; padding-left: 0; border-left: 2px solid %s;"><div style="padding-left: 18px;">%s</div></div>' % (LINE2, lane))
    return inner_screen('Прошлые версии', body)


BOARDS = [  # name, title, fn, issue number
    ('Fb180A', 'А: сетка из 10 типов; у кредитки — лимит', types_a, 180),
    ('Fb180B', 'Б: «Свои деньги» и «Долги» (рекомендую)', types_b, 180),
    ('Fb180List', 'Общее: «Счета» — сбережения и долги с «доступно»', accounts_types, 180),
    ('Fb208A', 'А: в «Доходе» раздел «Возврат в категорию»', refund_a, 208),
    ('Fb208B', 'Б: четвёртая вкладка «Возврат»', refund_b, 208),
    ('Fb208C', 'В: чип «↩ Возврат» в шторке расхода (рекомендую)', refund_c, 208),
    ('Fb208Show', 'Общее: возврат в конверте бюджета и в списке', refund_show, 208),
    ('Fb284A', 'А: карточки версий, тап раскрывает (рекомендую)', versions_a, 284),
    ('Fb284B', 'Б: лента — все пункты сразу', versions_b, 284),
]
