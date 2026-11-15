"""Generated boards: регулярный платёж в другой валюте, чем счёт (#415, часть #143) — variants for the owner's poll.

«Аренда · 650 USD» from Galicia (ARS): the template keeps 650 USD, the account is debited by the rate of the payment day
(as «чек в другой валюте» in the entry sheet); no rate — nothing posts by itself, the reminder opens the sheet with the
receipt. Owner 2026-11-15 (poll 2): editor Б — a checkbox «Платёж в другой валюте» that opens a second amount, like the
receipt block of the entry sheet (А, chips under the amount, dropped); no rate — В, the sheet asks what was debited (Г, own
rate in the template, dropped). Poll 3: Д — such a payment always asks, or Е — posts by itself at the rate + «Уточнить
сумму». Only the approved set (#367, А «Капсула», «Единый стиль»).

ROW — (key, row title, sticky text) for the «Экраны» page; BOARDS — (name, title, fn, key).
"""
from mockup_plan_bricks import *  # noqa: F401,F403
from mockup_catalog_parts import two_lines, form_section, amount_text, hint, col, radio_mark
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

def chips_line(*c):
    return '<div style="display: flex; gap: 8px; flex-wrap: wrap;">%s</div>' % ''.join(c)


def name_block():
    return form_section('НАЗВАНИЕ И СЧЁТ', '<div style="display: flex; align-items: center; gap: 10px;">%s%s%s</div>'
                        % (rule_icon('Аренда', 36), two_lines('Аренда', 'Расход · Аренда'), acc_chip(ACC[0])))


def details():
    return block('Детали', '<div style="display: grid; grid-template-columns: 1fr 1fr; gap: 6px;">%s%s%s%s</div>' % (
        detail('refresh', 'Повтор', 'Каждый месяц, 5-го'), detail('bolt', 'Как проводить', 'Проводить само'),
        detail('note', 'Заметка', 'Хозяйке, налом'), detail('calendar', 'Следующий', 'пт, 5 декабря')))


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


def sheet_no_rate(typed=False, ask=False):
    if ask:
        summ = receipt_sum('682 500', '1 USD = 1 050 ARS · курс 5 дек.')
        tip = message('Подставили по курсу. Банк списал иначе — исправьте по выписке.', 'warn')
    elif typed:
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
    ed = form_section('ПЛАТЁЖ В ДРУГОЙ ВАЛЮТЕ', '<div style="display: flex; flex-direction: column; gap: 8px;">%s%s%s</div>' % (
        amount_text('50 000', 'AMD', 28), chips_line(chip('AMD', True), chip('USD'), chip('Другая', dashed=True)),
        hint('Курса AMD → ARS пока нет · при проведении спросим, сколько списали')))
    return inner_screen('Где спросим сумму', col(
        label('Уведомление в день платежа — «само» не проводит', top=4),
        notif('Аренда · 650 USD — нет курса', 'Galicia · введите, сколько списали', ['Ввести сумму', 'Завтра']),
        label('«Скоро · 7 дней» на «Обзоре»', top=8), card(row_),
        label('Редактор шаблона — валюты без курса', top=8), ed,
        note('Курс появится позже — сумму не пересчитываем: вы ввели, сколько списали на самом деле.'), gap=8))


# ---- poll 3: always let the user enter the real debited amount (owner, 2026-11-15) ----
def how_rows(auto_off):
    rows = [('Напомнить', 'в день платежа — шторка, сумму списания вводите сами'),
            ('Проводить само', 'недоступно для платежа в другой валюте' if auto_off else 'по курсу дня · уведомим с «Уточнить сумму»')]
    return card(''.join(row(t, s_, '', radio_mark(i == 0), MUTED2 if (i and auto_off) else INK, 52).replace('{{div}}', 'border-top: 1px solid %s;' % DIV if i else '')
                        for i, (t, s_) in enumerate(rows)))


def ask_always():
    return inner_screen('Всегда спрашиваем', col(
        label('Уведомление в день платежа', top=4),
        notif('Сегодня платёж: Аренда · 650 USD', 'Galicia · ≈ 682 500 ARS по курсу', ['Ввести сумму', 'Завтра']),
        label('Шаблон с платежом в другой валюте — «Как проводить»', top=8), how_rows(True),
        note('Платёж в другой валюте всегда проводите вы: шторка открывается с суммой по курсу, её можно исправить. '
             'Курса нет — поле пустое. Платежи в валюте счёта — как раньше, «само» работает.'), gap=8))


def auto_then_fix():
    return inner_screen('Само + «Уточнить»', col(
        label('«Проводить само» — после проведения', top=4),
        notif('Проведено: Аренда · 682 500 ARS', 'по курсу 5 дек. · чек 650 USD', ['Уточнить сумму', 'Отменить']),
        label('Напоминание — кнопка без открытия приложения', top=8),
        notif('Сегодня платёж: Аренда · 650 USD', 'Galicia · ≈ 682 500 ARS по курсу', ['Провести по курсу', 'Ввести сумму']),
        label('Шаблон — «Как проводить»', top=8), how_rows(False),
        note('Проводим по курсу сразу, а «Уточнить сумму» открывает операцию, где списанное можно исправить. '
             'Курса нет — само не проводим и спрашиваем сумму.'), gap=8))


BOARDS = [  # name, title, fn, row key
    ('Rec415B', 'Выбрано (Б): галочка «Платёж в другой валюте»', variant_b, 'rec415'),
    ('Rec415NoRate', 'Выбрано (В) — нет курса: шторка спрашивает, сколько списали', sheet_no_rate, 'rec415'),
    ('Rec415NoRateTyped', 'Нет курса: сумма введена — курс из неё', lambda: sheet_no_rate(True), 'rec415'),
    ('Rec415NoRateWhere', 'Нет курса: уведомление, «Скоро», редактор', where_asked, 'rec415'),
    ('Rec415AskSheet', 'Шторка: сумма по курсу подставлена, её можно исправить', lambda: sheet_no_rate(ask=True), 'rec415'),
    ('Rec415AskAlways', 'Д: платёж в другой валюте всегда спрашивает сумму', ask_always, 'rec415'),
    ('Rec415AutoFix', 'Е: «само» по курсу + «Уточнить сумму»', auto_then_fix, 'rec415'),
]
