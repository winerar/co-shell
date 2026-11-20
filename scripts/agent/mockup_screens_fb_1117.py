"""Generated boards: backlog decisions of the poll 2026-11-17 (https://claude.ai/artifact/9V4aYUUrFHQYwcggHPKbE8).

The owner picked: #204 А (CSV export in «Ещё»), #338 А + «drag a category into another folder and back», #180 В (five new
account types), #440 А (about inside «Обновление»), #322 А (tips in the sheet), #471 А (the button under the row).
These boards draw the picked variants (static, one state) for the «ок» before code. Imported by mockup_screens_feedback.py (FEEDBACK + BOARDS).
"""
from mockup_bricks import *  # noqa: F401,F403
from mockup_parts import *  # noqa: F401,F403
from mockup_screens_fb_nov14 import key

FEEDBACK = [
    (1117, 'Решения опроса 2026-11-17 · бэклог',
     'Выбранные варианты: экспорт CSV (#204 А), ручной порядок и вложение перетаскиванием (#338 А), типы счетов (#180 В), '
     '«О приложении» в «Обновлении» (#440 А), чаевые в шторке (#322 А), строка прошлого импорта (#471 А).'),
]


def chip_on(text):
    return pill(text).replace('height: 22px', 'height: 36px').replace('padding: 0 8px', 'padding: 0 14px').replace('font-size: 11px', 'font-size: 13px')


def chips(items, on):
    cells = [chip_on(t) if t == on else chip(t) for t in items]
    return '<div style="display: flex; flex-wrap: wrap; gap: 8px;">%s</div>' % ''.join(cells)


# ---- #204 export ----------------------------------------------------------------------------------------------------

def export():
    periods = [('m', 'Месяц', 'Ноябрь 2026', '1 284'), ('y', 'Год', '2026', '9 410'), ('c', 'Свой', '1 сент. — 17 нояб. 2026', '2 655')]

    def body(k):
        _, name, value, count = next(p for p in periods if p[0] == k)
        return (chips([p[1] for p in periods], name)
                + '<div style="margin-top: 10px;">%s</div>' % group([row(value, 'Период', svg('calendar', LIME), chevron())])
                + label('Счета') + group([row('Все счета', '12 счетов, кроме архивных', '', switch(False)),
                                          row('Galicia, Mercado Pago, USDT · Binance', 'Выбрано: 3', '', chevron())])
                + '<div style="margin-top: 16px; text-align: center; font-size: 13px; color: %s;">Операций в файле: <b style="color: %s;">%s</b></div>' % (MUTED, INK, count))
    hint = note('Файл CSV: разделитель «;», суммы с запятой — откроется в Excel и Google Таблицах. Отправьте его через «Поделиться»: '
                'в почту, мессенджер или на диск.', 14)
    return (top_bar('Экспорт в CSV') + '<div style="padding: 0 16px;">%s%s</div>' % (body('m'), hint)
            + footer(button('Выгрузить и поделиться', 'primary', 'upload', 0)))


def export_more():
    """Where the export lives: «Ещё» → «Семья и данные» → a row after «Импорт из Wallet»."""
    def r(title, sub, icon, c, bg, new=False):
        return row(title + (' ' + pill('Новое') if new else ''), sub, tile_icon(icon, c, bg), chevron())
    data = group([r('Семья', 'Общий бюджет с близкими', 'user', LIME, LIMEBG), r('Резервная копия', 'Сохранить или восстановить данные', 'download', YELLOW, YELLOWBG),
                  r('Импорт из Wallet', 'Перенести операции из выгрузки CSV', 'download', CYAN, CYANBG),
                  r('Экспорт в CSV', 'Операции за период — в таблицу', 'upload', VIOLET, VIOLETBG, True)])
    return tab_screen('Ещё', label('Семья и данные', top=6) + data, 'more')


# ---- #338 manual order ----------------------------------------------------------------------------------------------

def handle():
    return svg('drag', MUTED, 22, 2.6)


def lifted(inner):
    """The row being dragged: no shadow (#477) — a lime frame and the raised surface."""
    return '<div style="margin: 6px -4px; border-radius: 24px; border: 2px solid %s; background: %s;">%s</div>' % (LIME, CARD, inner)


def sort_accounts():
    """One list, as the order is one for every grouping and every account picker (#338; code — AccountsOrder)."""
    names = ['Galicia', 'Mercado Pago', 'Santander']
    rows_ = [row(a[0], a[4], badge(a[1], a[2], a[3], 40, 12), handle()) for a in map(acc, names)]
    moving = row('USDT · Binance', 'Крипто', badge('BN', '#F0B90B', '#1A1400', 40, 12), handle())
    cash = [row('Наличные USD', 'Наличные', badge('$', '#9AA594', '#0B0F0A', 40, 12), handle()),
            row('Наличные ARS', 'Наличные', badge('$', '#9AA594', '#0B0F0A', 40, 12), handle())]
    body = note('Удерживайте строку и перетащите. Порядок общий для семьи.', 4) + group(rows_[:1]) + lifted(moving) + group(rows_[1:] + cash)
    return inner_screen('Порядок счетов', body, icon_button('check', 'Готово', LIME))


def sort_categories():
    def cat(name, icon, color, indent=False, extra=''):
        r = row(name, '', dot_icon(icon, color, 36), handle())
        return r.replace('padding: 8px 16px;', 'padding: 8px 16px 8px 52px;') if indent else r
    drop = ('<div style="margin: 6px 0; border-radius: 24px; border: 2px dashed %s; background: %s;">%s%s</div>'
            % (LIME, mix(LIME, 8), row('<b>Еда</b>', 'Отпустите — «Пекарня» станет подкатегорией', dot_icon('cart', '#8BC34A', 36), ''),
               cat('Продукты', 'cart', '#8BC34A', True) + cat('Кафе', 'cafe', '#FF9800', True)))
    moving = lifted(row('Пекарня', 'без папки', dot_icon('school', '#E07A4F', 36), handle()))
    body = (note('Удерживайте строку и перетащите. На середину другой категории — станет подкатегорией, из группы наверх — отдельной.', 4)
            + drop + moving
            + group([cat('Транспорт', 'taxi', '#FFEB3B'), cat('Такси', 'taxi', '#FFEB3B', True), cat('Бензин и парковка', 'bolt', '#FFB547', True)])
            + '<div style="margin-top: 10px;">%s</div>' % group([cat('Здоровье', 'pill', '#E91E63'), cat('Аптека', 'pill', '#E91E63', True)]))
    return inner_screen('Порядок категорий', body, icon_button('check', 'Готово', LIME))


# ---- #180 account types ---------------------------------------------------------------------------------------------

TYPES = [('cash', 'Наличные'), ('card', 'Карта'), ('credit', 'Кредитная карта'), ('save', 'Сберегательный'), ('dep', 'Вклад'),
         ('inv', 'Инвестиции'), ('loan', 'Кредит'), ('crypto', 'Крипто'), ('other', 'Другое')]


def account_types():
    def preview(k):
        neg = k in ('credit', 'loan')
        amt = '−45 300,00' if neg else '245 300,00'
        sub = {'credit': 'Долг вычитается из капитала', 'loan': 'Долг вычитается из капитала'}.get(k, 'Считается в капитале')
        return ('<div style="padding: 16px 18px; border-radius: 20px; background: %s; color: %s;"><div style="display: flex; font-size: 13px; font-weight: 600;">'
                '<span style="flex-grow: 1;">Galicia</span><span>%s</span></div><div style="margin-top: 26px; %s font-size: 30px; font-weight: 700;">%s '
                '<span style="font-size: 16px;">ARS</span></div><div style="margin-top: 4px; font-size: 12px;">%s</div></div>'
                % ('#F26E22', '#1A0A00', dict(TYPES)[k], MONO, amt, sub))
    names = [t for _, t in TYPES]
    body = (preview('credit') + field('Название', 'Galicia')
            + label('Тип') + chips(names, 'Кредитная карта')
            + note('Кредитная карта и кредит: баланс ниже нуля — это долг. Ставки и графики для вклада и кредита — после 1.0.', 14))
    return top_bar('Счёт', 'close') + '<div style="padding: 0 16px;">%s</div>' % body + footer(button('Сохранить', mt=0))


# ---- #440 about inside «Обновление» ---------------------------------------------------------------------------------

PRINCIPLES = [('lock', 'Данные на телефоне', 'Работает без интернета; синк — только внутри семьи'),
              ('user', 'Для двоих', 'Общие и личные счета, кто сколько потратил'),
              ('rate', 'Любые валюты', 'Курс дня для каждой операции'),
              ('spark', 'Без рекламы и подписок', 'Ничего не продаём и не показываем'),
              ('download', 'Свой файл', 'Резервная копия и экспорт в любой момент')]


def about_update():
    logo = ('<span style="width: 72px; height: 72px; border-radius: 22px; background: %s; display: flex; align-items: center; justify-content: center;">%s</span>'
            % (LIME, svg('wallet', ONLIME, 36, 2.2)))
    head = ('<div style="display: flex; align-items: center; gap: 14px; margin-top: 4px;">%s<div><div style="font-size: 22px; font-weight: 700;">Co-Shell</div>'
            '<div style="font-size: 13px; color: %s;">Версия 1.0 · сборка 712</div></div></div>'
            '<div style="margin-top: 12px; font-size: 15px; line-height: 1.45;">Семейный бюджет на двоих: общие траты, счета в разных валютах, всё — на ваших телефонах.</div>'
            % (logo, MUTED))
    princ = group([row(t, s, tile_icon(i, LIME, LIMEBG), '') for i, t, s in PRINCIPLES])
    upd = group([row('У вас последняя версия', 'Проверено только что', tile_icon('check', LIME, LIMEBG), icon_button('refresh', 'Проверить ещё раз'))])
    body = head + label('Принципы') + princ + label('Обновление') + upd
    return inner_screen('О приложении', body)


# ---- #322 tips in the entry sheet -----------------------------------------------------------------------------------

def tips():
    head = ('<div style="padding: 6px 20px 0;"><div style="display: flex; align-items: center; gap: 10px;">%s<div style="flex-grow: 1;"><b style="font-size: 17px;">Кафе</b>'
            '<div style="font-size: 12px; color: %s;">USDT · Binance · сегодня</div></div>%s</div>'
            '<div style="margin-top: 10px; %s font-size: 34px; font-weight: 700;">13,10 <span style="font-size: 18px; color: %s;">USDT</span></div>'
            '<div style="font-size: 12px; color: %s;">Сумма покупки: 13 300 ARS</div></div>'
            % (dot_icon('cafe', '#FF9800', 40), MUTED, icon_button('close', 'Закрыть', MUTED), MONO, MUTED, MUTED))
    opts = [('p10', '10 %', '1 330 ARS'), ('p15', '15 %', '1 995 ARS'), ('own', 'Своя сумма', '')]

    def block(k):
        cells = [chip_on(t) if key == k else chip(t) for key, t, _ in opts]
        amount_line = ('<div style="margin-top: 12px; display: flex; align-items: baseline; gap: 8px;"><span style="%s font-size: 24px; font-weight: 700;">%s</span>'
                       '<span style="font-size: 13px; color: %s;">≈ 1,31 USD по курсу дня</span></div>' % (MONO, dict((o[0], o[2]) for o in opts)[k] or '0 ARS', MUTED))
        return '<div style="display: flex; gap: 8px;">%s</div>%s' % (''.join(cells), amount_line)
    tip = card('<div style="display: flex; align-items: center;"><b style="flex-grow: 1; font-size: 15px;">Чаевые</b>'
               '<span style="font-size: 13px; color: %s;">отдельной операцией</span></div><div style="margin-top: 10px;">%s</div>'
               '<div style="margin: 12px -14px -14px;">%s</div>'
               % (MUTED, block('p10'),
                  group([row('Счёт чаевых', 'Наличные USD', badge('$', '#9AA594', '#0B0F0A', 36, 11), chevron()),
                         row('Категория чаевых', 'Чаевые', dot_icon('gift', '#FF8FA3', 36), chevron())])
                  .replace('border-radius: 24px', 'border-radius: 0 0 24px 24px')),
               pad='14px', extra='margin: 14px 16px 0;')
    inner = head + tip + '<div style="padding: 0 16px;">%s</div>' % button('Сохранить две операции', 'primary', 'check', 16)
    return scrim() + sheet(inner, 150)


def tips_own():
    """«Своя сумма»: the amount is typed on the keypad inside the sheet; «Готово» saves both operations."""
    ok = ('<span style="border-radius: 16px; background: %s; color: %s; font-size: 15px; font-weight: 700; display: flex; align-items: center; justify-content: center; gap: 6px;">%sГотово</span>'
          % (LIME, ONLIME, svg('check', ONLIME, 22, 2.2)))
    pad = ('<div style="display: grid; grid-template-columns: repeat(4, minmax(0, 1fr)); grid-auto-rows: 54px; gap: 6px; margin-top: 12px;">%s</div>'
           % (key('7') + key('8') + key('9') + key('⌫', KEY2, INK, 20) + key('4') + key('5') + key('6') + '<span></span>'
              + key('1') + key('2') + key('3') + ok.replace('border-radius: 16px;', 'border-radius: 16px; grid-row: span 2;', 1) + key(',') + key('0') + key('000', KEY, INK, 18)))
    chips_ = '<div style="display: flex; gap: 8px;">%s</div>' % (chip('10 %') + chip('15 %') + chip_on('Своя сумма'))
    rows = group([row('Счёт чаевых', 'Наличные ARS', badge('$', '#9AA594', '#0B0F0A', 36, 11), chevron()),
                  row('Категория чаевых', 'Чаевые', dot_icon('gift', '#FF8FA3', 36), chevron())])
    inner = ('<div style="padding: 6px 20px 0;"><b style="font-size: 22px;">Чаевые</b><div style="font-size: 13px; color: %s;">От суммы 13 300 ARS</div>'
             '<div style="margin-top: 12px;">%s</div><div style="margin-top: 8px; %s font-size: 30px; font-weight: 700; color: %s;">500 <span style="font-size: 16px; color: %s;">ARS</span></div>'
             '<div style="margin: 6px -4px 0;">%s</div>%s</div>' % (MUTED, chips_, MONO, PINK, MUTED, rows, pad))
    return scrim() + sheet(inner, 56)


def tips_first():
    """First «＋ Чаевые»: which category to put tips in (asked once, changed in «Ещё»)."""
    cats = group([row(n, s, dot_icon(i, c, 36), svg('check', LIME, 22, 2.4) if n == 'Как у покупки' else '')
                  for n, s, i, c in [('Как у покупки', 'категория каждой покупки', 'cafe', '#FF9800'), ('Чаевые', 'новая категория', 'gift', '#FF8FA3'),
                                     ('Рестораны и бары', 'Еда', 'cafe', '#FF7A59'), ('Прочее', '', 'more', '#9AA594')]])
    inner = ('<div style="padding: 6px 20px 0;"><b style="font-size: 22px;">Куда записывать чаевые?</b><div style="font-size: 13px; color: %s; margin-top: 4px;">'
             'Спросим один раз. Поменять — строка «Категория чаевых».</div><div style="margin-top: 14px;">%s</div></div>' % (MUTED, cats))
    return scrim() + sheet(inner, 300)


BOARDS = [  # name, title, fn, issue number
    ('Fb204Export', '#204 А: экспорт в CSV — период, счета, «Выгрузить»', export, 1117),
    ('Fb204More', '#204 А: «Экспорт в CSV» в «Ещё»', export_more, 1117),
    ('Fb338Acc', '#338 А: порядок счетов перетаскиванием', sort_accounts, 1117),
    ('Fb338Cat', '#338 А: категория — в папку и из папки перетаскиванием', sort_categories, 1117),
    ('Fb180Types', '#180 В: девять типов счёта, долг со знаком минус', account_types, 1117),
    ('Fb440About', '#440 А: «О приложении» вместе с обновлением', about_update, 1117),
    ('Fb322Tips', '#322 А: «Чаевые» в шторке — 10 % / 15 % / своя, счёт и категория', tips, 1117),
    ('Fb322Own', '#322 А: «Своя сумма» — клавиатура в шторке, «Готово» сохраняет обе операции', tips_own, 1117),
    ('Fb322First', '#322 А: первый раз — куда записывать чаевые', tips_first, 1117),
]
