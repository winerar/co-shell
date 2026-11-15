"""Generated boards: регулярный платёж в другой валюте, чем счёт (#415, часть #143) — variants for the owner's poll.

«Аренда · 650 USD» from Galicia (ARS): the template keeps 650 USD, the account is debited by the rate of the payment day
(as «чек в другой валюте» in the entry sheet); no rate — nothing posts by itself, the reminder opens the sheet with the
receipt. Logic is in code already (#415); the question is only how the editor picks the currency of the amount:
А — the currency next to the amount, chips under it (account's first); Б — a checkbox «Платёж в другой валюте» that opens a
second amount, like the receipt block of the entry sheet. Only the approved set (#367, А «Капсула», «Единый стиль»).

ROW — (key, row title, sticky text) for the «Экраны» page; BOARDS — (name, title, fn, key).
"""
from mockup_plan_bricks import *  # noqa: F401,F403
from mockup_catalog_parts import two_lines, form_section, section_link, amount_text, hint
from mockup_catalog_choice import seg
from mockup_screens_main import ACC, acc_chip
from mockup_screens_fb_290 import block, detail, refund_check
from mockup_screens_recurring import ed_page, rule_icon

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


BOARDS = [  # name, title, fn, row key
    ('Rec415A', 'А (рекомендую): валюта у суммы', variant_a, 'rec415'),
    ('Rec415ADefault', 'А: обычный шаблон — без чипов', variant_a_default, 'rec415'),
    ('Rec415B', 'Б: галочка «Платёж в другой валюте»', variant_b, 'rec415'),
]
