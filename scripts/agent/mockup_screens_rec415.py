"""Generated boards: регулярный платёж в другой валюте, чем счёт (#415, часть #143) — variants for the owner's poll.

«Аренда · 650 USD» from Galicia (ARS): the template keeps 650 USD, the account is debited by the rate of the payment day
(as «чек в другой валюте» in the entry sheet); no rate — nothing posts by itself, the reminder opens the sheet with the
receipt. Logic is in code already (#415); the question is only how the editor picks the currency of the amount:
А — the currency next to the amount, chips under it (account's first); Б — a checkbox «Платёж в другой валюте» that opens a
second amount, like the receipt block of the entry sheet. Only the approved set (#367, А «Капсула», «Единый стиль»).

ROW — (key, row title, sticky text) for the «Экраны» page; BOARDS — (name, title, fn, key).
"""
from mockup_plan_bricks import *  # noqa: F401,F403
from mockup_catalog_parts import two_lines, form_section, section_link, amount_text, hint, col
from mockup_catalog_choice import seg
from mockup_screens_main import ACC, acc_chip
from mockup_screens_fb_290 import block, detail, refund_check
from mockup_screens_recurring import ed_page, rule_icon, post_btn, notif
from mockup_screens_main import keypad
from mockup_screens_fb_290 import frame, head, small_link
from mockup_catalog_new import status

ROW = ('rec415', 'Регулярные платежи: валюта платежа (#415)',
       'Аренда 650 USD со счёта в ARS: шаблон хранит 650 USD, со счёта списывается по курсу дня платежа (как «чек в другой валюте» '
       'в шторке). Нет курса — само не проводим, напоминание откроет шторку с чеком. Вопрос — как выбрать валюту суммы в редакторе.')

RATE_HINT = '≈ 682 500 ARS по курсу 15 нояб. · спишем с Galicia по курсу дня платежа'


def chips_line(*c):
    return '<div style="display: flex; gap: 8px; flex-wrap: wrap;">%s</div>' % ''.join(c)


def name_block():
    return form_section('НАЗВАНИЕ И СЧЁТ', '<div style="display: flex; align-items: center; gap: 10px;">%s%s%s</div>'
                        % (rule_icon('Аренда', 36), two_lines('Аренда', 'Расход · Аренда'), acc_chip(ACC[0])))


def details():
    return block('Детали', '<div style="display: grid; grid-template-columns: 1fr 1fr; gap: 6px;">%s%s%s%s</div>' % (
        detail('refresh', 'Повтор', 'Каждый месяц, 5-го'), detail('bolt', 'Как проводить', 'Проводить само'),
        detail('note', 'Заметка', 'Хозяйке, налом'), detail('calendar', 'Следующий', 'пт, 5 декабря')))


# ---- А: the currency next to the amount ----
def variant_a():
    amount_line = amount_text('650', 'USD', 28)
    choices = chips_line(chip('ARS · счёт'), chip('USD', True), chip('EUR'), chip('Другая', dashed=True))
    summ = form_section('СУММА', '<div style="display: flex; flex-direction: column; gap: 8px;">%s%s%s</div>'
                        % (amount_line, choices, hint(RATE_HINT)), section_link('Пусто — спросим'))
    return ed_page('Шаблон', seg('A', ['Расход', 'Доход', 'Перевод'], 0) + name_block() + summ + details()
                   + note('Валюта — чипами под суммой: первой — валюта счёта, дальше — недавние валюты чеков; «Другая» — список всех валют. '
                        'Выбрали не валюту счёта — под суммой курс и как спишем.', 4))


def variant_a_default():
    choices = chips_line(chip('ARS · счёт', True), chip('USD'), chip('EUR'), chip('Другая', dashed=True))
    summ = form_section('СУММА', '<div style="display: flex; flex-direction: column; gap: 8px;">%s%s</div>'
                        % (amount_text('180 000', 'ARS', 28), choices), section_link('Пусто — спросим'))
    return ed_page('Шаблон', seg('A', ['Расход', 'Доход', 'Перевод'], 0) + name_block().replace('Аренда', 'Expensas', 2) + summ
                   + note('Обычный шаблон: выбрана валюта счёта, подсказки о курсе нет. У перевода чипов нет — валюта счёта.', 4))


# ---- Б: a checkbox like the receipt block ----
def variant_b():
    main = form_section('СУММА · СПИСАНО', '<div style="display: flex; flex-direction: column; gap: 6px;">%s%s</div>'
                        % (amount_text('≈ 682 500', 'ARS', 24, MUTED), hint('по курсу дня платежа')))
    other = form_section('ПЛАТЁЖ В ДРУГОЙ ВАЛЮТЕ', '<div style="display: flex; flex-direction: column; gap: 8px;">%s%s%s</div>'
                         % (amount_text('650', 'USD', 28), chips_line(chip('USD', True), chip('EUR'), chip('Другая', dashed=True)),
                            hint('Курс 15 нояб.: 1 USD = 1 050 ARS')), svg('close', MUTED, 16))
    return ed_page('Шаблон', seg('A', ['Расход', 'Доход', 'Перевод'], 0) + name_block() + refund_check(True).replace('Это возврат', 'Платёж в другой валюте')
                   .replace('вернули на счёт', 'как чек в шторке') + other + main + details()
                   + note('Как «чек в другой валюте» в шторке: галочка открывает вторую сумму. Списано — только подсказка, её не вводят.', 4))


# ---- no rate on the payment day: the user types what was debited (owner's note to the poll, 2026-11-15) ----
NO_RATE = 'Курса USD → ARS на 5 дек. нет — введите, сколько списали'


def receipt_sum(debited, rate_line, empty=False):
    big = ('<div style="display: flex; align-items: baseline; justify-content: center; gap: 8px;"><span style="%s font-size: 40px; font-weight: 700; color: %s;">%s</span>'
           '<span style="%s font-size: 16px; color: %s;">ARS</span>%s</div>'
           % (MONO, MUTED2 if empty else PINK, debited, MONO, MUTED,
              '<span style="width: 2px; height: 34px; background: %s; align-self: center;"></span>' % LIME if empty else ''))
    receipt = ('<div style="display: flex; align-items: center; gap: 8px; margin-top: 6px; padding-top: 8px; border-top: 1px dashed %s; font-size: 12px; color: %s;">'
               '<span>Чек</span><span style="%s font-size: 15px; color: %s; font-weight: 700;">650 USD</span><span style="flex-grow: 1;"></span><span>%s</span></div>'
               % (LINE2, MUTED, MONO, INK, rate_line))
    return block('Сумма · списано', big + receipt, right=small_link('Пропустить этот раз'))


def rent_tag():
    return ('<span style="height: 36px; padding: 0 12px; border-radius: 999px; background: %s; color: %s; font-size: 13px; font-weight: 600; display: flex; '
            'align-items: center; gap: 6px;">%s Аренда</span>' % (VIOLETPILL, VIOLETINK, svg('refresh', VIOLETINK, 16)))


def sheet_no_rate(typed=False):
    if typed:
        summ = receipt_sum('682 500', '1 USD = 1 050 ARS · по вашей сумме')
        tip = hint('Курс посчитан из вашей суммы и сохранится в операции.')
    else:
        summ = receipt_sum('0', 'курса нет', empty=True)
        tip = message(NO_RATE, 'warn')
    det = block('Детали', '<div style="display: grid; grid-template-columns: 1fr 1fr; gap: 6px;">%s%s</div>'
                % (detail('calendar', 'Дата', 'пт, 5 декабря'), detail('note', 'Заметка', 'Хозяйке, налом')))
    return frame(head(extra=rent_tag()).replace(acc_chip(ACC[3]), acc_chip(ACC[0])) + summ + tip + det + '<div style="flex-grow: 1;"></div>' + keypad('Провести'))


def where_asked():
    row_ = ('<div style="display: flex; align-items: center; gap: 10px; padding: 8px 10px 8px 14px; min-height: 64px; box-sizing: border-box;">%s'
            '<span style="flex-grow: 1; min-width: 0; display: flex; flex-direction: column; align-items: flex-start; gap: 3px;"><span style="font-size: 15px;">Аренда</span>'
            '<span style="display: flex; flex-wrap: wrap; align-items: center; gap: 2px 8px;">%s%s</span></span>%s</div>'
            % (rule_icon('Аренда', 36), status('A', 'нет курса', 'work'), amount('−650', 'USD', INK, 12), post_btn('Ввести')))
    ed = form_section('СУММА', '<div style="display: flex; flex-direction: column; gap: 8px;">%s%s%s</div>' % (
        amount_text('50 000', 'AMD', 28), chips_line(chip('ARS · счёт'), chip('AMD', True), chip('Другая', dashed=True)),
        hint('Курса AMD → ARS пока нет · при проведении спросим, сколько списали')))
    return inner_screen('Где спросим сумму', col(
        label('Уведомление в день платежа — «само» не проводит', top=4),
        notif('Аренда · 650 USD — нет курса', 'Galicia · введите, сколько списали', ['Ввести сумму', 'Завтра']),
        label('«Скоро · 7 дней» на «Обзоре»', top=8), card(row_),
        label('Редактор шаблона — валюты без курса', top=8), ed,
        note('Курс появится позже — сумму не пересчитываем: вы ввели, сколько списали на самом деле.'), gap=8))


def own_rate():
    choices = chips_line(chip('ARS · счёт'), chip('USD', True), chip('EUR'), chip('Другая', dashed=True))
    summ = form_section('СУММА', '<div style="display: flex; flex-direction: column; gap: 8px;">%s%s</div>'
                        % (amount_text('650', 'USD', 28), choices), section_link('Пусто — спросим'))
    rate = form_section('КУРС', '<div style="display: flex; flex-direction: column; gap: 8px;">%s%s%s</div>' % (
        seg('A', ['По дню платежа', 'Свой'], 1), amount_text('1 USD = 1 050', 'ARS', 20),
        hint('Спишем 682 500 ARS каждый раз, даже без связи. Курс сам не меняется — поправляйте при росте цен.')))
    return ed_page('Шаблон', seg('A', ['Расход', 'Доход', 'Перевод'], 0) + name_block() + summ + rate + details())


BOARDS = [  # name, title, fn, row key
    ('Rec415A', 'А (рекомендую): валюта у суммы', variant_a, 'rec415'),
    ('Rec415ADefault', 'А: обычный шаблон — без чипов', variant_a_default, 'rec415'),
    ('Rec415B', 'Б: галочка «Платёж в другой валюте»', variant_b, 'rec415'),
    ('Rec415NoRate', 'Нет курса: шторка спрашивает, сколько списали', sheet_no_rate, 'rec415'),
    ('Rec415NoRateTyped', 'Нет курса: сумма введена — курс из неё', lambda: sheet_no_rate(True), 'rec415'),
    ('Rec415NoRateWhere', 'Нет курса: уведомление, «Скоро», редактор', where_asked, 'rec415'),
    ('Rec415OwnRate', 'Г: свой курс в шаблоне', own_rate, 'rec415'),
]
