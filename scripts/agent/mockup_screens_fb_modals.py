"""Generated boards: one modal system for the whole app (owner 2026-11-14: «модалки выглядят очень плохо»).

Today: Material AlertDialog — plain title, stacked fields, a 31-line list in a dialog, text buttons at the corner.
Variants on the same three cases (manual rate, budget month start, delete confirmation):
А — everything is a bottom sheet; Б — refreshed centred card; В (hybrid) = А's sheets + Б's confirmation.
Imported by mockup_screens_feedback.py (FEEDBACK + BOARDS).
"""
from mockup_bricks import *  # noqa: F401,F403
from mockup_parts import pill, footer, grid
from mockup_screens_main import keypad

FEEDBACK = [
    (345, 'Отзыв #345 · Модальные окна — одна система вместо Material-диалогов',
     'Выбран В (опрос 2026-11-14): формы и выбор — шторка снизу (кнопка под пальцем, своя клавиатура для чисел), вопросы «Удалить?» — компактная карточка по центру.'),
]

RATES = [('USD', 'Доллар США', '1 482,10'), ('USDT', 'Tether', '1 479,55'), ('EUR', 'Евро', '1 590,02'), ('AMD', 'Армянский драм', '3,8240'),
         ('RUB', 'Российский рубль', '15,07'), ('AED', 'Дирхам ОАЭ', '403,57'), ('BRL', 'Бразильский реал', '256,40'), ('GEL', 'Грузинский лари', '546,12')]


def rates_bg():
    rows = [row(c, n, badge(c[:2], CHIP, INK, 36), amount(v, 'ARS', INK, 14)) for c, n, v in RATES]
    return inner_screen('Курсы валют', label('Валюты · 1 единица в ARS', top=8) + group(rows))


def budget_bg():
    rows = [row(n, 'потрачено %s из %s' % (s, t), dot_icon(ic, col, 36), amount(s, 'ARS', INK, 14)) for n, ic, col, s, t in
            [('Продукты', 'cart', LIME, '412 300', '600 000'), ('Кафе', 'cafe', WARN, '98 500', '120 000'), ('Такси', 'taxi', YELLOW, '41 000', '60 000'),
             ('Дом', 'house', CYAN, '310 000', '310 000'), ('Связь', 'phone', '#C79BFF', '22 000', '25 000'), ('Кино', 'film', OK, '0', '15 000')]]
    return inner_screen('Бюджеты', label('Ноябрь · 1–30 ноя', top=8) + group(rows))


def accounts_bg():
    rows = [row(n, k, badge(i, c, ink, 36), amount(v, cur, INK, 14)) for n, k, i, c, ink, v, cur in
            [('Galicia', 'Карта', 'GA', '#F26E22', '#1A0A00', '1 250 000', 'ARS'), ('Binance', 'Крипто', 'BN', '#F3BA2F', '#1A1400', '2 140,55', 'USDT'),
             ('Ameria', 'Карта', 'AM', '#2E6BFF', '#FFFFFF', '845 000', 'AMD'), ('Наличные', 'Кошелёк', 'НА', LIME, ONLIME, '310 000', 'ARS'),
             ('Тинькофф', 'Карта', 'ТИ', YELLOW, '#1A1400', '42 300', 'RUB'), ('Mercado Pago', 'Кошелёк', 'MP', '#00B1EA', '#00141A', '98 450', 'ARS')]]
    return tab_screen('Счета', group(rows), 'acc')


# ---- shared pieces -------------------------------------------------------------------------------------------

def sheet_head(title, sub=''):
    """Sheet header (CoShellModalSheet): title + optional subtitle, × on the right."""
    s = '<div style="margin-top: 4px; font-size: 13px; line-height: 1.4; color: %s;">%s</div>' % (MUTED, sub) if sub else ''
    return ('<div style="display: flex; align-items: flex-start; gap: 8px; padding: 8px 8px 0 20px;"><div style="flex-grow: 1; padding-top: 10px;">'
            '<h2 style="margin: 0; font-size: 22px; font-weight: 700;">%s</h2>%s</div>%s</div>' % (title, s, icon_button('close', 'Закрыть', MUTED)))


def modal_sheet(under, title, body, sub='', top=300):
    return scrim(under) + sheet(sheet_head(title, sub) + '<div style="padding: 0 20px;">%s</div>' % body, top)


def cur_pick(code, name):
    return ('<span style="display: flex; align-items: center; gap: 8px; height: 48px; padding: 0 12px 0 6px; border-radius: 999px; border: 1px solid %s; background: %s;">'
            '%s<span style="display: flex; flex-direction: column;"><span style="font-size: 15px; font-weight: 700;">%s</span>'
            '<span style="font-size: 11px; color: %s;">%s</span></span><span style="color: %s;">▾</span></span>'
            % (LINE2, BAR, badge(code[:2], CHIP, INK, 34, 17), code, MUTED, name, MUTED))


def rate_pair(value='1 482,1', caret=True):
    c = '<span style="width: 2px; height: 34px; background: %s; margin-left: 2px;"></span>' % LIME if caret else ''
    return ('<div style="margin-top: 16px; display: flex; align-items: center; gap: 10px;"><span style="%s font-size: 20px; color: %s;">1</span>%s'
            '<span style="%s font-size: 20px; color: %s;">=</span><span style="flex-grow: 1;"></span>%s</div>'
            '<div style="margin-top: 14px; display: flex; align-items: center; justify-content: flex-end; min-height: 52px;">'
            '<span style="%s font-size: 40px; font-weight: 700;">%s</span>%s<span style="%s font-size: 18px; color: %s; margin-left: 8px;">ARS</span></div>'
            '<div style="text-align: right; font-size: 12px; color: %s; %s">загружено сегодня: 1 482,10 · 1 ARS = 0,000675 USD</div>'
            % (MONO, MUTED, cur_pick('USD', 'Доллар'), MONO, MUTED, cur_pick('ARS', 'Песо'), MONO, value, c, MONO, MUTED, MUTED, MONO))


def date_seg():
    return segmented(['Сегодня', 'Вчера', '12 ноя ▾'], 'Сегодня')


def day_grid(selected=10, cell=40):
    cells = []
    for d in range(1, 32):
        on = d == selected
        st = 'background: %s; color: %s; font-weight: 700;' % (LIME, ONLIME) if on else 'color: %s;' % INK
        mark = '<span style="position: absolute; bottom: 4px; width: 4px; height: 4px; border-radius: 50%%; background: %s;"></span>' % VIOLET if d == 1 and not on else ''
        cells.append('<span style="position: relative; height: %dpx; border-radius: 999px; %s %s font-size: 15px; display: flex; align-items: center; justify-content: center;">%d%s</span>'
                     % (cell, st, MONO, d, mark))
    return '<div style="display: grid; grid-template-columns: repeat(7, minmax(0, 1fr)); gap: 4px; margin-top: 14px;">%s</div>' % ''.join(cells)


def period_line(text='Ноябрь: 10 ноя — 9 дек · Декабрь: 10 дек — 9 янв'):
    return ('<div style="margin-top: 14px; display: flex; gap: 10px; align-items: center; padding: 12px 14px; border-radius: 14px; background: %s;">%s'
            '<span style="font-size: 13px; line-height: 1.4;">%s</span></div>' % (CHIP, svg('calendar', LIME, 20), text))


def two_buttons(left, right, kind='primary'):
    return ('<div style="display: grid; grid-template-columns: 1fr 1fr; gap: 10px;">%s%s</div>'
            % (button(left, 'outline', mt=0), button(right, kind, mt=0)))


# ---- А: sheets everywhere --------------------------------------------------------------------------------------

def a_rate():
    body = (rate_pair() + label('Действует с', top=14) + date_seg()
            + '<div style="margin: 14px 0 12px; font-size: 12px; line-height: 1.4; color: %s;">Ручной курс важнее загруженного — до следующего ручного курса этой пары.</div>' % MUTED
            + keypad('Сохранить'))
    return modal_sheet(rates_bg(), 'Курс вручную', body, top=150)


def a_day():
    body = day_grid() + period_line() + '<div style="margin-top: 10px; font-size: 12px; color: %s;">● — по умолчанию (календарный месяц). В коротком месяце — с последнего дня.</div>' % MUTED
    return modal_sheet(budget_bg(), 'Начало месяца', body, 'С какого числа считать бюджет — удобно в день зарплаты', top=278) + footer(button('Готово', 'primary', mt=0))


def a_confirm():
    ic = ('<span style="width: 56px; height: 56px; border-radius: 18px; background: %s; display: flex; align-items: center; justify-content: center;">%s</span>'
          % (PINKBG, svg('trash', PINK, 28)))
    body = ('<div style="padding: 20px 20px 0;">%s<h2 style="margin: 16px 0 0; font-size: 22px;">Удалить счёт «Galicia»?</h2>'
            '<p style="margin: 8px 0 0; font-size: 14px; line-height: 1.45; color: %s;">148 операций уйдут в корзину вместе со счётом. Вернуть можно 30 дней.</p></div>'
            % (ic, INK2))
    return scrim(accounts_bg()) + sheet(body, 520) + footer(button('Удалить', 'danger', 'trash', 0), button('Отмена', 'outline', mt=10))


# ---- Б: refreshed centred card ---------------------------------------------------------------------------------

def card_dialog(under, icon, title, body, actions, top=None, color=LIME, bg=LIMEPILL):
    pos = 'top: %dpx;' % top if top is not None else 'top: 50%; transform: translateY(-50%);'
    ic = ('<span style="width: 48px; height: 48px; border-radius: 16px; background: %s; display: flex; align-items: center; justify-content: center;">%s</span>'
          % (bg, svg(icon, color, 24))) if icon else ''
    return scrim(under) + ('<div role="dialog" style="position: absolute; left: 20px; right: 20px; %s padding: 22px 20px 20px; border-radius: 28px; background: %s; '
                           'box-shadow: 0 16px 48px rgba(0,0,0,0.5);">%s<h2 style="margin: %dpx 0 0; font-size: 22px;">%s</h2>%s<div style="margin-top: 20px;">%s</div></div>'
                           % (pos, SHEET, ic, 14 if icon else 0, title, body, actions))


def b_rate():
    body = rate_pair(caret=True).replace('font-size: 40px', 'font-size: 32px') + '<div style="margin-top: 14px;">%s</div>' % date_seg()
    dlg = card_dialog(rates_bg(), 'rate', 'Курс вручную', body, two_buttons('Отмена', 'Сохранить'), top=40)
    kp = '<div style="position: absolute; left: 0; right: 0; bottom: 0; padding: 10px 12px 18px; background: %s; border-radius: 24px 24px 0 0;">%s</div>' % (SHEET, keypad('Готово'))
    return dlg + kp


def b_day():
    body = ('<p style="margin: 6px 0 0; font-size: 13px; color: %s;">С какого числа считать бюджет</p>' % MUTED + day_grid(cell=36)
            + period_line('Ноябрь: 10 ноя — 9 дек'))
    return card_dialog(budget_bg(), 'calendar', 'Начало месяца', body, two_buttons('Отмена', 'Готово'))


def b_confirm():
    body = '<p style="margin: 8px 0 0; font-size: 14px; line-height: 1.45; color: %s;">148 операций уйдут в корзину вместе со счётом. Вернуть можно 30 дней.</p>' % INK2
    return card_dialog(accounts_bg(), 'trash', 'Удалить счёт «Galicia»?', body, two_buttons('Отмена', 'Удалить', 'danger'), color=PINK, bg=PINKBG)


# ---- common: anatomy and where it changes ----------------------------------------------------------------------

def anatomy():
    def item(name, what):
        return ('<div style="padding: 12px 14px; border-radius: 14px; background: %s; margin-top: 8px;"><div style="%s font-size: 13px; color: %s;">%s</div>'
                '<div style="margin-top: 4px; font-size: 13px; line-height: 1.4; color: %s;">%s</div></div>' % (CARD, MONO, LIME, name, INK2, what))
    comps = (item('CoShellModalSheet', 'шторка: заголовок + подзаголовок + ×, тело, кнопка закреплена внизу во всю ширину; свайп вниз и тап мимо закрывают')
             + item('CoShellChoiceSheet', 'выбор одного: до 8 — строки с ✓, больше — поиск сверху; числа (1–31) — сетка 7 в ряд')
             + item('CoShellConfirm', 'вопрос: иконка в цветном квадрате, заголовок-вопрос, последствия одной фразой, две пилюли «Отмена · Удалить»')
             + item('NumberField + CoShellKeypad', 'любое число в модалке (курс, лимит) — своя клавиатура, не системная'))
    where = ('Курс вручную · Новая валюта · Начало месяца · Период бюджета · Выбор шаблона · Новый тег · Папки категорий · '
             'Семья: приглашение, выход · Резервная копия: восстановить · Импорт Wallet: 3 окна · Обновление · Сбой приложения · '
             'Все «Удалить?» (счёт, категория, операции, шаблон, бюджет) · Выбор даты')
    body = (label('4 компонента', top=4) + comps + label('Где поменяем — 21 файл, 30+ окон') +
            '<div style="font-size: 13px; line-height: 1.6; color: %s;">%s</div>' % (INK2, where)
            + note('Material-диалоги в app запретим тестом (как кнопки и чипы): только эти компоненты.'))
    return inner_screen('Модальные окна', body)


BOARDS = [  # name, title, fn, issue number
    ('FbModalARate', 'В: «Курс вручную» — шторка, пара «1 USD = … ARS», своя клавиатура', a_rate, 345),
    ('FbModalADay', 'В: «Начало месяца» — шторка, сетка 1–31 и период', a_day, 345),
    ('FbModalBConfirm', 'В: «Удалить?» — компактная карточка по центру (выбрано)', b_confirm, 345),
    ('FbModalAll', 'Общее: 4 компонента и где поменяем', anatomy, 345),
]
