"""Generated boards: payment templates (#104), «Предложения» sheet. Variant А — templates in the entry sheet,
Б — a «Шаблоны» mode inside «Операции». Shared boards: entry filled from a template, «Сохранить как шаблон», editor.

ROW — (key, title, sticky text); BOARDS — (name, title, fn, key).
"""
from mockup_plan_bricks import *  # noqa: F401,F403
from mockup_screens_main import ACC, keypad, acc_chip, big_amount, chips_row, entry

ROW = ('tpl', 'Шаблоны платежей · отзыв #104',
       'Шаблон помнит тип, счёт, сумму (можно пустую), категорию и заметку; дата всегда «сегодня», всё можно поправить. '
       'А — шаблоны прямо в шторке «+»: одна полоса над счетами, доступны с любого экрана. '
       'Б — режим «Шаблоны» во вкладке «Операции», плитки покрупнее. Рекомендую А: шаблон нужен в момент ввода, лишний переход не нужен. '
       'Общие для обоих: шторка из шаблона, «Сохранить как шаблон» в правке операции, редактор шаблона.')

TPL = [('Испанский', 'Образование', '40 000', 'ARS', 'Galicia'), ('Аренда', 'Аренда', '650', 'USD', 'Наличные'),
       ('Интернет', 'Связь', '18 900', 'ARS', 'Galicia'), ('Кофе', 'Кафе', '', 'ARS', 'USDT-карта'),
       ('Продукты Coto', 'Продукты', '', 'ARS', 'Galicia'), ('Перевод маме', 'Подарки', '50 000', 'AMD', 'Ameria')]


def tpl_chip(name, cat, amt, cur):
    a = '<span style="%s font-size: 12px; color: %s;">%s</span>' % (MONO, MUTED, (amt + ' ' + cur) if amt else 'сумма при вводе')
    return ('<span style="flex: none; display: flex; align-items: center; gap: 8px; height: 52px; padding: 0 14px 0 6px; border-radius: 16px; '
            'background: %s; border: 1px solid %s;">%s<span style="display: flex; flex-direction: column;"><span style="font-size: 14px; font-weight: 600;">%s</span>%s</span></span>'
            % (BAR, LINE2, cat_icon(cat, 38), name, a))


def acc_line(a, sub):
    return row(a[0], sub, badge(a[1], a[2], a[3], 38), amount(a[5], a[6], INK, 14), min_h=56)


def tpl_in_sheet():
    strip = ('<div style="display: flex; gap: 8px; overflow: hidden; margin: 0 -16px; padding: 0 16px;">%s'
             '<span style="flex: none; width: 52px; height: 52px; border-radius: 16px; border: 1.5px dashed %s; display: flex; align-items: center; justify-content: center;">%s</span></div>'
             % (''.join(tpl_chip(n, c, a, cu) for n, c, a, cu, _ in TPL[:3]), LINE2, svg('plus', MUTED, 20)))
    accs = group([acc_line(ACC[0], 'недавний'), acc_line(ACC[3], 'недавний'), acc_line(ACC[2], ''), acc_line(ACC[4], '')])
    inner = (segmented(['Расход', 'Доход', 'Перевод'], 'Расход', 'lime')
             + '<div style="display: flex; align-items: center; margin: 16px 4px 8px;"><span style="flex-grow: 1; font-size: 13px; font-weight: 600; color: %s;">'
               'Шаблоны</span><span style="font-size: 13px; color: %s;">Все 6 ›</span></div>' % (MUTED, LIME)
             + strip + label('Или счёт', top=16) + accs + '<div style="margin-top: 12px;">%s</div>' % search('Найти счёт'))
    return sheet_page(inner, 150)


def tpl_card(name, cat, amt, cur, acc):
    a = amount(amt, cur, PINK, 15) if amt else '<span style="font-size: 13px; color: %s;">сумма при вводе</span>' % MUTED
    return ('<div style="padding: 12px; border-radius: 20px; background: %s; border: 1px solid %s; display: flex; flex-direction: column; gap: 8px;">%s'
            '<span style="display: flex; flex-direction: column; gap: 2px;"><span style="font-size: 15px; font-weight: 600;">%s</span>'
            '<span style="font-size: 12px; color: %s;">%s · %s</span></span>%s</div>' % (CARD, LINE, cat_icon(cat, 36), name, MUTED, cat, acc, a))


def tpl_in_ops():
    grid = ('<div style="margin-top: 14px; display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 10px;">%s'
            '<div style="border-radius: 20px; border: 1.5px dashed %s; display: flex; align-items: center; justify-content: center; gap: 8px; '
            'min-height: 120px; color: %s; font-size: 14px;">%s Шаблон</div></div>'
            % (''.join(tpl_card(*t) for t in TPL[:5]), LINE2, MUTED, svg('plus', MUTED, 18)))
    body = segmented(['Операции', 'Шаблоны'], 'Шаблоны') + grid + note('Тап — шторка с заполненными полями. Удержание — правка, порядок.')
    return tab_page('Операции', body, 'ops')


def tpl_entry():
    head = ('<div style="display: flex; align-items: center; gap: 8px; padding: 0 4px;">%s<span style="flex-grow: 1;"></span>'
            '<span style="height: 32px; padding: 0 12px; border-radius: 999px; background: %s; color: %s; font-size: 13px; font-weight: 600; '
            'display: flex; align-items: center; gap: 6px;">%s Испанский ✕</span></div>' % (acc_chip(ACC[0]), VIOLETPILL, VIOLETINK, svg('bolt', VIOLETINK, 16)))
    cats = ''.join('<span style="display: flex; flex-direction: column; align-items: center; gap: 4px; padding: 4px 0;"><span style="padding: 3px; %s">%s</span>'
                   '<span style="font-size: 12px; color: %s;">%s</span></span>'
                   % ('border-radius: 50%%; box-shadow: 0 0 0 2px %s;' % LIME if n == 'Образование' else '', cat_icon(n), INK if n == 'Образование' else INK2, n)
                   for n in ['Образование', 'Продукты', 'Кафе', 'Транспорт'])
    grid = '<div style="display: grid; grid-template-columns: repeat(4, minmax(0, 1fr)); gap: 2px;">%s</div>' % cats
    hint = '<div style="text-align: center; font-size: 12px; color: %s; margin-top: -8px;">Из шаблона · сумму можно поправить</div>' % MUTED
    return entry(head + big_amount('40 000', 'ARS') + hint + grid + chips_row(chip('Сегодня ▾', True), chip('Испанский, Марта', False, 'note')) + keypad('Сохранить'))


def tpl_save():
    from mockup_screens_main import entry_edit
    body = (field('Название', 'Продукты Coto') + '<div style="margin-top: 8px;">%s</div>'
            % group([row('Запомнить сумму', '32 000 ARS — или спрашивать каждый раз', '', switch(False), min_h=56)]))
    return entry_edit() + '<div style="position: absolute; inset: 0; background: rgba(0,0,0,0.55);"></div>' + dialog(
        'Сохранить как шаблон', 'Счёт Galicia, категория «Продукты», заметка — запомнятся. Дата — всегда сегодня.', [('Отмена', 'text'), ('Сохранить', 'primary')], body)


def tpl_edit():
    body = (segmented(['Расход', 'Доход', 'Перевод'], 'Расход', 'lime') + field('Название', 'Испанский')
            + field('Счёт', 'Galicia · ARS', trail=chevron()) + field('Сумма', '40 000 ARS', 'Пусто — спросим при вводе', mono=True)
            + field('Категория', 'Образование', trail=chevron()) + field('Заметка', 'Испанский, Марта')
            + button('Сохранить', mt=20) + button('Удалить шаблон', 'danger', 'trash'))
    return inner_screen('Шаблон', body)


BOARDS = [  # name, title, fn, row key
    ('TplA', 'А · Шаблоны в шторке «+» (рекомендую)', tpl_in_sheet, 'tpl'),
    ('TplB', 'Б · Шаблоны — режим в «Операциях»', tpl_in_ops, 'tpl'),
    ('TplEntry', 'Шторка из шаблона «Испанский»', tpl_entry, 'tpl'),
    ('TplSave', 'Правка операции → «Сохранить как шаблон»', tpl_save, 'tpl'),
    ('TplEdit', 'Редактор шаблона', tpl_edit, 'tpl'),
]
