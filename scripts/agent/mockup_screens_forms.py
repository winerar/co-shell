"""Editors (interactive): «Категории», «Категория | Папка», «Счёт: новый», «Цвет счёта»."""
import json

from mockup_parts import *  # noqa: F401,F403
from mockup_logic import go, flip, per, proto, tap, toggled, when

# Category palette: the first row follows the theme accents, the rest are fixed category colours.
SWATCHES = [LIME, CYAN, VIOLET, '#FF4FD8', YELLOW, '#FF8A5C', PINK, OK, '#FF7A6B', '#E07A4F', '#FF8FA3', '#FFC46B', '#D99A5B', '#E8D3A2', '#6BB8FF', '#7A8494']
ICONS = ['cart', 'cafe', 'taxi', 'pill', 'house', 'phone', 'film', 'gift', 'school', 'bus', 'wifi', 'bolt', 'spark', 'salary', 'note', 'folder', 'chart', 'budget']


def swatches(selected, cols=8, group_=None):
    cells = [swatch(c, c == selected, 30) for c in SWATCHES]
    if group_:
        cells = [go(group_, 'c%d' % i, h) for i, h in enumerate(cells)]
    return grid(cells, cols, 6)


# ---- categories list ------------------------------------------------------------------------------------------------

FOLDERS = [('food', 'Еда', 'cart', LIME, ['Продукты', 'Кафе', 'Пекарня', 'Доставка']), ('home', 'Дом', 'house', CYAN, ['Аренда', 'Коммуналка', 'Интернет']),
           ('transport', 'Транспорт', 'taxi', WARN, ['Такси', 'Метро', 'Бензин'])]
LOOSE = [('Здоровье', 'pill', PINK), ('Развлечения', 'film', VIOLET), ('Отпуск', 'spark', '#FF8A5C')]
INCOME = [('Зарплата', 'salary', CYAN), ('Подарки', 'gift', VIOLET), ('Кэшбэк', 'chart', LIME)]


def folder_card(key, title, icon, color, subs):
    def head(open_):
        return row('<b>%s</b>' % title, '%d категории' % len(subs), dot_icon(icon, color), svg('down' if not open_ else 'chev', MUTED, 20).replace('<svg', '<svg style="transform: rotate(%s);"' % ('-90deg' if open_ else '0deg')))
    kids = ('<div style="display: flex; flex-wrap: wrap; gap: 8px; padding: 0 14px 14px 66px;">%s%s</div>'
            % (''.join(chip(s, dot=color) for s in subs), chip('＋ В папку', dashed=True)))
    inner = toggled(key, head(True) + kids, head(False))
    return '<div style="margin-top: 10px;">%s</div>' % card(flip(key, inner))


def categories():
    exp = ''.join(folder_card(*f) for f in FOLDERS) + ''.join('<div style="margin-top: 10px;">%s</div>' % card(row('<b>%s</b>' % t, 'без папки', dot_icon(i, c))) for t, i, c in LOOSE)
    inc = ''.join('<div style="margin-top: 10px;">%s</div>' % card(row('<b>%s</b>' % t, 'без папки', dot_icon(i, c))) for t, i, c in INCOME)
    seg = per('k', ['exp', 'inc'], lambda k: segmented(['Расходы', 'Доходы'], 'Расходы' if k == 'exp' else 'Доходы', 'lime', ['go_k_exp', 'go_k_inc']))
    body = (seg + when('is_k_exp', exp) + when('is_k_inc', inc) + note('Показать архивные (2)', 14))
    return proto('Категории', inner_screen('Категории', body) + fab('Добавить'), k='exp', food=True)


# ---- category / folder editor ---------------------------------------------------------------------------------------

def big_icon(icon, color):
    return ('<span style="width: 60px; height: 60px; flex: none; border-radius: 50%%; background: %s; display: flex; align-items: center; justify-content: center;">%s</span>'
            % (color, svg(icon, ONLIME, 28, 1.9)))


def category_edit():
    color = '#E07A4F'

    def form(kind):
        name = field('Название', 'Пекарня' if kind == 'cat' else 'Еда').replace('border: 1px solid %s;' % LINE2, 'border: 1px solid %s;' % LIME, 1)
        head = '<div style="display: flex; align-items: center; gap: 12px; margin-top: 4px;">%s<div style="flex-grow: 1; margin-top: -12px;">%s</div></div>' % (
            big_icon('school' if kind == 'cat' else 'cart', color), name)
        extra = (grid([field('Тип', 'Расходы'), field('Папка', 'Еда', trail=svg('down', MUTED, 18))], 2) if kind == 'cat'
                 else note('В папке: Продукты, Кафе, Пекарня, Доставка — их можно перетащить в другую папку'))
        icons = grid([icon_cell(i, i == ('school' if kind == 'cat' else 'cart'), color) for i in ICONS], 6, 8)
        return head + extra + label('Иконка') + icons + label('Цвет') + swatches(color)
    seg = per('k', ['cat', 'folder'], lambda k: segmented(['Категория', 'Папка'], 'Категория' if k == 'cat' else 'Папка', 'lime', ['go_k_cat', 'go_k_folder']))
    body = seg + '<div style="margin-top: 14px;">%s</div>' % per('k', ['cat', 'folder'], form)
    return proto('Категория | Папка', top_bar('Категория', 'close', icon_button('archive', 'В архив')) + '<div style="padding: 0 16px;">%s</div>' % body
                 + footer(button('Сохранить', mt=0)), k='cat')


# ---- new account ------------------------------------------------------------------------------------------------------

def account_edit():
    def preview(c):
        col, ink = {'cyan': (CYAN, '#002A2E'), 'orange': ('#F26E22', '#1A0A00')}[c] if c in ('cyan', 'orange') else (SWATCHES[int(c[1:])], ONLIME)
        return ('<div style="padding: 16px 18px; border-radius: 20px; background: %s; color: %s;"><div style="display: flex; font-size: 13px; font-weight: 600;">'
                '<span style="flex-grow: 1;">Галисия · карта</span><span>Карта</span></div><div style="margin-top: 30px; %s font-size: 30px; font-weight: 700;">245 300,00 '
                '<span style="font-size: 16px;">ARS</span></div></div>' % (col, ink, MONO))
    colors = ['cyan', 'orange'] + ['s%d' % i for i in range(8)]
    hint = ('<div style="margin-top: 4px; display: flex; align-items: center; gap: 10px; padding: 10px 10px 10px 14px; border-radius: 16px; background: %s; border: 1px solid %s;">'
            '<span style="width: 20px; height: 20px; border-radius: 50%%; background: #F26E22;"></span><span style="flex-grow: 1; font-size: 13px;">Похоже на Галисию — взять её цвет</span>%s</div>'
            % (CARD, LINE, go('c', 'orange', pill('Взять'))))
    sw = per('c', colors, lambda c: grid([go('c', 's%d' % i, swatch(SWATCHES[i], c == 's%d' % i, 30)) for i in range(8)], 8, 6))
    types = '<div style="display: flex; flex-wrap: wrap; gap: 8px;">%s</div>' % ''.join(chip(t) if t != 'Карта' else pill('Карта').replace('height: 24px', 'height: 36px').replace('padding: 0 10px', 'padding: 0 14px').replace('font-size: 12px', 'font-size: 13px')
                                                                         for t in ['Наличные', 'Карта', 'Вклад', 'Крипто', 'Другое'])
    sw_row = flip('off', toggled('off', row('Учитывать в общем балансе', 'Выключите для долга или чужих денег', '', switch(False)),
                                 row('Учитывать в общем балансе', 'Выключите для долга или чужих денег', '', switch(True))))
    body = (per('c', colors, preview) + field('Название', 'Галисия · карта') + grid([field('Валюта', 'ARS <span style="font-size: 11px; color: %s;">заблок.</span>' % MUTED, mono=True),
                                                                                  field('Начальный баланс', '120 000,00', mono=True)], 2)
            + label('Тип') + types + label('Цвет') + when('is_c_cyan', hint) + '<div style="margin-top: 10px;">%s</div>' % sw + '<div style="margin: 8px -14px 0;">%s</div>' % sw_row)
    return proto('Счёт: новый', top_bar('Счёт', 'close', icon_button('archive', 'В архив')) + '<div style="padding: 0 16px;">%s</div>' % body
                 + footer(button('Сохранить', mt=0)), c='cyan')


# ---- account colour: banks + palette ----------------------------------------------------------------------------------

BANKS = [('Аргентина', [('Galicia', 'GA', '#F26E22'), ('Santander', 'SA', '#EC0000'), ('BBVA', 'BB', '#1973B8'), ('Nación', 'BN', '#005AA7'), ('Mercado Pago', 'MP', '#00B1EA'),
                        ('Ualá', 'UA', '#3E4FE0'), ('Naranja X', 'NX', '#FE5000'), ('Brubank', 'BR', '#6A2EE8'), ('Lemon', 'LE', '#00E36B')]),
         ('Армения', [('Ameria', 'AM', '#B01E2F'), ('Inecobank', 'IN', '#0096D6'), ('Ardshinbank', 'AR', '#004B93'), ('ACBA', 'AC', '#00843D'), ('IDBank', 'ID', '#E4002B'), ('Evocabank', 'EV', '#6D2C91')]),
         ('Россия', [('Сбер', 'СБ', '#21A038'), ('Т-Банк', 'Т', '#FFDD2D'), ('Альфа', 'А', '#EF3124'), ('ВТБ', 'ВТБ', '#0A2896'), ('Ozon Банк', 'OZ', '#005BFF'), ('Райффайзен', 'Р', '#FEE600')]),
         ('Крипто и кошельки', [('Binance', 'BN', '#F0B90B'), ('Bybit', 'BY', '#F7A600'), ('Tether', '₮', '#26A17B'), ('Wise', 'W', '#9FE870'), ('PayPal', 'PP', '#003087'),
                                ('Payoneer', 'PY', '#FF4800'), ('Revolut', 'R', '#0075EB')])]
PALETTE = [('FF5470', 'Красный'), ('FF4FD8', 'Розовый'), ('D36BFF', 'Пурпур'), ('9D7BFF', 'Фиолет'), ('6E8BFF', 'Индиго'), ('38B6FF', 'Синий'), ('2BE4FF', 'Циан'), ('00E5C0', 'Мята'),
           ('4DFF88', 'Зелёный'), ('A8FF3E', 'Лайм'), ('F5FF3D', 'Жёлтый'), ('FFD43B', 'Янтарь'), ('FFA53D', 'Оранжевый'), ('FF7A3D', 'Пламя'), ('FFB38A', 'Персик'), ('8FB3CC', 'Сталь'),
           ('B0B8C4', 'Серебро'), ('7A8494', 'Графит'), ('FF8A65', 'Коралл'), ('E07A5F', 'Терракота'), ('FF8FA3', 'Роза'), ('FFC46B', 'Мёд'), ('D9A066', 'Карамель'), ('F2D0A4', 'Песок')]
FRESH = [('C9B6FF', 'Лаванда'), ('9FD8FF', 'Небо'), ('9BF5D6', 'Морская пена'), ('F4F59A', 'Лимонный крем'), ('FFB3D1', 'Румянец'), ('E89BFF', 'Орхидея'), ('D6F5FF', 'Лёд'),
         ('DFFF00', 'Шартрез'), ('4D4DFF', 'Ультрамарин'), ('B5C23A', 'Олива'), ('C9B27C', 'Хаки'), ('3DDC97', 'Нефрит')]

COLORS_JS = r'''
    const s = this.state, color = s.color || '#' + '38B6FF', name = s.name || 'Синий', bg = __co('k_050805_n00');
    const ink = (c) => { const n = parseInt(c.slice(1), 16), l = (0.299 * (n >> 16 & 255) + 0.587 * (n >> 8 & 255) + 0.114 * (n & 255)) / 255; return l > 0.6 ? '#' + '0B0F0A' : '#FFFFFF'; };
    const B = %s, P = %s;
    return {
      banks: B.map((g) => ({ title: g[0], items: g[1].map((b) => ({ name: b[0], ini: b[1], color: '#' + b[2], ink: ink('#' + b[2]),
        cell: 'border: 2px solid ' + (color === '#' + b[2] ? '#' + b[2] : 'transparent') + ';', pick: () => this.setState({ color: '#' + b[2], name: 'Цвет банка ' + b[0] }) })) })),
      palette: P.map((p) => ({ color: '#' + p[0], isNew: p[2], ring: 'border: 2px solid ' + (color === '#' + p[0] ? __co('k_EEF5E6_n93') : 'transparent') + ';',
        pick: () => this.setState({ color: '#' + p[0], name: p[1] }) })),
      name, color, colorInk: ink(color), cardBg: __coMix(bg, color, 0.16), cardLine: __coMix(bg, color, 0.45),
      suggest: () => this.setState({ color: '#' + 'F26E22', name: 'Цвет банка Galicia' })
    };
'''


def colors():
    preview = ('<div style="display: flex; flex-direction: column; gap: 10px; padding: 16px; border-radius: 16px; background: {{cardBg}}; border: 1px solid {{cardLine}};">'
               '<div style="display: flex; align-items: center; gap: 12px;">%s<span><div style="font-size: 16px; font-weight: 600;">Galicia</div>'
               '<div style="font-size: 12px; color: %s;">{{name}}</div></span></div>%s</div>' % (badge('GA', '{{color}}', '{{colorInk}}', 44, 14), MUTED, amount('1 250 000', 'ARS', INK, 22)))
    hint = ('<div style="margin-top: 10px; display: flex; align-items: center; gap: 10px; padding: 10px 10px 10px 14px; border-radius: 16px; background: %s; border: 1px solid %s;">'
            '<span style="width: 20px; height: 20px; border-radius: 50%%; background: #F26E22;"></span><span style="flex-grow: 1; font-size: 13px;">Похоже на банк Galicia — взять его цвет</span>%s</div>'
            % (CARD, LINE, tap(pill('Взять'), 'suggest')))
    bank = tap('<span style="display: flex; flex-direction: column; align-items: center; gap: 4px; padding: 6px 2px; border-radius: 14px; min-width: 0; {{b.cell}}">%s'
               '<span style="font-size: 10px; color: %s; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; max-width: 100%%;">{{b.name}}</span></span>'
               % (badge('{{b.ini}}', '{{b.color}}', '{{b.ink}}', 34, 10), MUTED), 'b.pick')
    banks = ('<sc-for list="{{banks}}" as="g" hint-placeholder-count="4">%s<div style="display: grid; grid-template-columns: repeat(4, minmax(0, 1fr)); gap: 4px;">'
             '<sc-for list="{{g.items}}" as="b" hint-placeholder-count="6">%s</sc-for></div></sc-for>' % (label('{{g.title}}', True, 12), bank))
    dot = when('p.isNew', '<span style="position: absolute; top: 0; right: 0; width: 8px; height: 8px; border-radius: 50%%; background: %s; border: 2px solid %s;"></span>' % (INK, SHEET))
    pal = ('<div style="display: grid; grid-template-columns: repeat(6, minmax(0, 1fr)); gap: 8px; justify-items: center;"><sc-for list="{{palette}}" as="p" hint-placeholder-count="36">%s</sc-for></div>'
           % tap('<span style="position: relative; display: inline-flex; padding: 2px; border-radius: 50%%; {{p.ring}}"><span style="width: 36px; height: 36px; border-radius: 50%%; '
                 'background: {{p.color}};"></span>%s</span>' % dot, 'p.pick'))
    body = ('<div style="padding: 4px 16px 0;"><h2 style="margin: 4px 0 12px; font-size: 20px;">Цвет счёта</h2>' + preview + hint
            + label('Банки и приложения', True) + note('Оттенки примерные — сверим по логотипам перед кодом', 0) + banks
            + label('Палитра Вольт · 36 цветов', True) + note('Общая для счетов и категорий. Точкой отмечены 12 новых', 0) + '<div style="margin-top: 10px;">%s</div></div>' % pal)
    js = COLORS_JS % (json.dumps([[t.upper(), [[n, i, c[1:]] for n, i, c in items]] for t, items in BANKS], ensure_ascii=False),
                      json.dumps([[c, n, False] for c, n in PALETTE] + [[c, n, True] for c, n in FRESH], ensure_ascii=False))
    html = board('Цвет счёта', '<div style="position: absolute; inset: 0; background: %s;">%s</div>' % (BG, sheet(body, 8)), '__C__', 1240)
    return html.replace('  renderVals() { return {__C__}; }', '  renderVals() {' + js + '  }')


BOARDS = [('Categories', categories), ('CategoryEdit', category_edit), ('AccountEdit', account_edit), ('Colors', colors)]
