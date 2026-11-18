"""Generated boards: the converter made easy to find and quick to use (follow-up of #319, owner request 2026-11-18).

Entry points: a tile on «Обзор», a row in «Ещё → Деньги», the compact card on «Курсы». The converter itself is a sheet
(CoShellModalSheet) with the amount keypad; three variants: А «Табло», Б «Отдаю / Получаю», В «Все сразу».
The amount starts empty (grey «0»): the first digit is the answer, nothing to erase. Static, one state — for the «ок» before code.
Imported by mockup_screens_feedback.py (FEEDBACK + BOARDS).
"""
from mockup_bricks import *  # noqa: F401,F403
from mockup_parts import *  # noqa: F401,F403
from mockup_screens_main import RECENT, balance_hero, keypad, recent_card, tile, tiles
from mockup_screens_fb_modals import sheet_head

FEEDBACK = [
    (319, 'Конвертер: заметный вход и быстрый ввод · 2026-11-18',
     'Три входа: плитка «Обзора», строка «Ещё → Деньги», карточка «Курсов». Конвертер — шторка с клавиатурой; сумма сначала пустая '
     '(первая цифра — уже ответ). Варианты шторки: А «Табло» · Б «Отдаю / Получаю» · В «Все сразу» (рекомендую).'),
]

# Totals currencies of the family; the first is the main one. (code, bare result for 100 USD, rate for «1 USD»)
CUR = [('USD', '100', '1'), ('ARS', '101 500', '1 015'), ('AMD', '38 700', '387'), ('RUB', '9 150', '91,5'), ('USDT', '100,10', '0,999'),
       ('EUR', '92,40', '0,924'), ('BRL', '566,20', '5,662'), ('GBP', '78,90', '0,789')]
EMPTY = '0'


def num(text, size=17, color=INK):
    return '<span style="%s font-size: %dpx; font-weight: 700; color: %s;">%s</span>' % (MONO, size, color, text)


def caret():
    return '<span style="width: 2px; height: 30px; background: %s; margin-left: 2px; align-self: center;"></span>' % LIME


def hint(text='Введите сумму'):
    return '<span style="font-size: 14px; color: %s;">%s</span>' % (MUTED, text)


# ---- entry points -------------------------------------------------------------------------------------------------------

def conv_tile(wide=True):
    """«Обзор» tile: the same plain tile as the others, the amount is not preset."""
    return ('<div style="margin-top: 10px; padding: 14px 16px; border-radius: 20px; background: %s; border: 1px solid %s; display: flex; align-items: center; gap: 12px;">'
            '%s<div style="flex-grow: 1;"><div style="font-size: 14px; font-weight: 600;">Конвертер</div>'
            '<div style="font-size: 12px; color: %s;">Сколько это в других валютах</div></div>%s</div>'
            % (CARD, LINE, tile_icon('rate', LIME, CHIP), MUTED, chevron()))


def home_entry():
    body = (balance_hero() + tiles(tile('Траты за ноябрь', '1 240,50', 'USD'), tile('Без категории', '14', 'операций')) + conv_tile()
            + recent_card(RECENT[:3]))
    return tab_screen('Обзор', body, 'home', icon_button('search', 'Поиск'))


def more_entry():
    def r(icon, title, sub, color=LIME):
        return row(title, sub, tile_icon(icon, color, CHIP), chevron(), min_h=52)
    money = group([r('wallet', 'Счета', '12 · общий баланс'), r('folder', 'Категории', '34 · 6 родителей'),
                   r('rate', 'Курсы валют', 'обновлены сегодня'), r('rate', 'Конвертер', 'сколько это в других валютах', CYAN),
                   r('budget', 'Бюджеты', '18 лимитов')])
    return tab_screen('Ещё', label('Деньги', top=4) + money, 'more')


def rates_entry():
    """«Курсы»: the card is one calm row now (no preset 100); a tap opens the same sheet."""
    card_row = ('<div style="margin: 4px 0 0; padding: 14px 16px; border-radius: 24px; background: %s; border: 1px solid %s; display: flex; align-items: center; gap: 12px;">'
                '%s<div style="flex-grow: 1;"><div style="font-size: 14px; font-weight: 600;">Конвертер</div>%s</div>%s</div>'
                % (CARD, LINE, tile_icon('rate', LIME, CHIP), hint('Нажмите, чтобы посчитать'), chevron()))
    rows = ''.join(row(c, '1 USD = %s %s' % (rt, c), badge(c[:2], CHIP, INK, 36, 18), num(rt, 16), min_h=56) for c, _, rt in CUR[1:6])
    return inner_screen('Курсы валют', card_row + label('Курсы к USD') + card(rows), icon_button('refresh', 'Обновить'))


# ---- the sheet: shared pieces ---------------------------------------------------------------------------------------------

def under_home():
    return home_entry()


def conv_sheet(body, top=70):
    """Sheet with the title, the variant's body and the keypad pinned to the bottom."""
    inner = (sheet_head('Конвертер', 'Курс на сегодня') + '<div style="padding: 8px 20px 0;">%s</div>' % body
             + '<div style="position: absolute; left: 12px; right: 12px; bottom: 14px;">%s</div>' % keypad('Готово'))
    return scrim(under_home()) + sheet(inner, top)


def cur_pill(code):
    return ('<span style="display: inline-flex; align-items: center; gap: 6px; height: 36px; padding: 0 12px 0 4px; border-radius: 999px; border: 1px solid %s; background: %s; font-size: 14px; font-weight: 700;">'
            '%s%s<span style="color: %s;">▾</span></span>' % (LINE2, BAR, badge(code[:2], CHIP, INK, 28, 14), code, MUTED))


# ---- А «Табло» ---------------------------------------------------------------------------------------------------------

def tablo(value, empty=False):
    big = ('<div style="display: flex; align-items: center; justify-content: center; gap: 10px; margin: 6px 0 12px;">%s%s%s</div>'
           % ('<span style="%s font-size: 44px; font-weight: 700; color: %s;">%s</span>' % (MONO, MUTED if empty else LIME, EMPTY if empty else value),
              caret() if empty else '', cur_pill('USD')))
    rows = ''.join(row(c, '1 USD = %s %s' % (rt, c), badge(c[:2], CHIP, INK, 36, 18), num('—' if empty else v, 17, MUTED if empty else INK), min_h=52)
                   for c, v, rt in CUR[1:5])
    return big + card(rows)


def sheet_a_empty():
    return conv_sheet(tablo('', True), 70)


def sheet_a_typed():
    return conv_sheet(tablo('100'), 70)


# ---- Б «Отдаю / Получаю» ---------------------------------------------------------------------------------------------------

def side(title, value, code, on, empty=False):
    border = '2px solid %s' % LIME if on else '1px solid %s' % LINE
    val = (num(EMPTY, 30, MUTED) + (caret() if on else '')) if empty else num(value, 30, LIME if on else INK)
    return ('<div style="padding: 12px 16px; border-radius: 24px; background: %s; border: %s;"><div style="font-size: 12px; color: %s;">%s</div>'
            '<div style="display: flex; align-items: center; gap: 8px; margin-top: 4px;">%s<span style="flex-grow: 1;"></span>%s</div></div>'
            % (CARD, border, MUTED, title, val, cur_pill(code)))


def swap():
    return ('<div style="display: flex; justify-content: center; margin: -10px 0; position: relative;"><span style="width: 40px; height: 40px; border-radius: 50%%; background: %s; border: 1px solid %s; '
            'display: flex; align-items: center; justify-content: center;">%s</span></div>' % (BAR, LINE2, svg('transfer', INK, 20, 1.8)))


def pair(a, b, empty=False):
    return ('<div style="margin-top: 6px;">%s%s%s</div>' % (side('Отдаю', a, 'USD', True, empty), swap(), side('Получаю', b, 'ARS', False, empty))
            + note('1 USD = 1 015 ARS · курс на сегодня', 10)
            + '<div style="margin-top: 8px; display: flex; gap: 6px; flex-wrap: wrap;">%s</div>'
            % ''.join(chip(('%s %s' % (v, c)) if not empty else c) for c, v, _ in CUR[2:5]))


def sheet_b_empty():
    return conv_sheet(pair('', '', True), 70)


def sheet_b_typed():
    return conv_sheet(pair('100', '101 500'), 70)


# ---- В «Все сразу» ------------------------------------------------------------------------------------------------------------

def all_row(code, value, on, empty, h=52):
    if empty:
        val = num(EMPTY, 18, MUTED) + (caret() if on else '')
    else:
        val = num(value, 18, LIME if on else INK)
    border = 'border: 2px solid %s;' % LIME if on else 'border: 1px solid transparent;'
    name = '<span style="font-size: 15px; font-weight: 700;">%s</span>' % code
    return ('<div style="display: flex; align-items: center; gap: 12px; min-height: %dpx; padding: 0 14px; border-radius: 18px; %s background: %s;">%s%s'
            '<span style="flex-grow: 1;"></span>%s</div>' % (h, border, CARD if on else 'transparent', badge(code[:2], CHIP, INK, 36, 18), name, val))


def all_list(n, active, empty, typed=None):
    rows = ''
    for c, v, _ in CUR[:n]:
        rows += all_row(c, typed if (c == 'USD' and typed) else v, c == active, empty, 48 if n > 5 else 52)
    return '<div style="display: flex; flex-direction: column; gap: 4px;">%s</div>' % rows


def sheet_c_empty():
    return conv_sheet(all_list(5, 'USD', True) + note('Нажмите на строку — считаем из этой валюты', 10), 70)


def sheet_c_typed():
    return conv_sheet(all_list(5, 'USD', False, '100') + note('Нажмите на строку — считаем из этой валюты', 10), 70)


def sheet_c_other():
    """Source switched to ARS: the typed number now lives in the ARS row, the others recalculate."""
    rows = ''
    vals = {'USD': '100', 'ARS': '101 500', 'AMD': '38 700', 'RUB': '9 150', 'USDT': '100,10'}
    for c in ('USD', 'ARS', 'AMD', 'RUB', 'USDT'):
        rows += all_row(c, vals[c], c == 'ARS', False)
    return conv_sheet('<div style="display: flex; flex-direction: column; gap: 4px;">%s</div>' % rows + note('Нажмите на строку — считаем из этой валюты', 10), 70)


def sheet_c_many():
    """Many totals currencies (8): rows 48 dp, the list stays above the keypad; more — the list scrolls."""
    return conv_sheet(all_list(8, 'ARS', False) + note('Ещё валюты — прокрутите список', 8), 40)


def sheet_c_norate():
    rows = (all_row('USD', '100', True, False) + all_row('ARS', '101 500', False, False)
            + '<div style="display: flex; align-items: center; gap: 12px; min-height: 52px; padding: 0 14px;">%s<span style="font-size: 15px; font-weight: 700;">AMD</span>'
              '<span style="flex-grow: 1;"></span><span style="font-size: 13px; color: %s;">нет курса · <span style="color: %s; font-weight: 600;">задать</span></span></div>' % (badge('AM', CHIP, INK, 36, 18), MUTED, INK2))
    return conv_sheet('<div style="display: flex; flex-direction: column; gap: 4px;">%s</div>' % rows, 70)


# ---- owner's decision 2026-11-18: two modes in one sheet (poll answer «В» + note) ------------------------------------------------
# «Пара» (Отдаю / Получаю) opens by default; a button in the header switches to «Все» and back; the last pair and the last mode are remembered.

def mode_head(mode):
    """Sheet header with the mode button: «Пара» shows the «Все» icon and vice versa; the label under the title says what is on."""
    icon, label_txt, to = ('list', 'Пара валют', 'Показать все валюты') if mode == 'pair' else ('transfer', 'Все валюты', 'Показать пару')
    return ('<div style="display: flex; align-items: flex-start; gap: 4px; padding: 8px 8px 0 20px;"><div style="flex-grow: 1; padding-top: 10px;">'
            '<h2 style="margin: 0; font-size: 22px; font-weight: 700;">Конвертер</h2>'
            '<div style="margin-top: 4px; font-size: 13px; color: %s;">%s · курс на сегодня</div></div>%s%s</div>'
            % (MUTED, label_txt, icon_button(icon, to, INK), icon_button('close', 'Закрыть', MUTED)))


def mode_sheet(mode, body, top=70):
    inner = (mode_head(mode) + '<div style="padding: 8px 20px 0;">%s</div>' % body
             + '<div style="position: absolute; left: 12px; right: 12px; bottom: 14px;">%s</div>' % keypad('Готово'))
    return scrim(under_home()) + sheet(inner, top)


def pair_body(a, b, empty=False):
    """The pair: «Отдаю» is the active side (tap the other side to type there); the swap button flips the currencies."""
    return ('<div style="margin-top: 6px;">%s%s%s</div>' % (side('Отдаю', a, 'USD', True, empty), swap(), side('Получаю', b, 'ARS', False, empty))
            + note('1 USD = 1 015 ARS · курс на сегодня', 10))


def sheet2_pair_empty():
    return mode_sheet('pair', pair_body('', '', True))


def sheet2_pair_typed():
    return mode_sheet('pair', pair_body('100', '101 500'))


def sheet2_all_typed():
    return mode_sheet('all', all_list(5, 'USD', False, '100') + note('Нажмите на строку — считаем из этой валюты', 10))


def sheet2_all_many():
    return mode_sheet('all', all_list(8, 'ARS', False) + note('Ещё валюты — прокрутите список', 8), 40)


BOARDS = [  # name, title, fn, issue number
    ('Conv2PairEmpty', 'Итог · «Пара» (по умолчанию): пусто', sheet2_pair_empty, 319),
    ('Conv2PairType', 'Итог · «Пара»: набрано 100 USD', sheet2_pair_typed, 319),
    ('Conv2AllType', 'Итог · «Все валюты» (кнопка в шапке)', sheet2_all_typed, 319),
    ('Conv2AllMany', 'Итог · «Все валюты»: 8 валют', sheet2_all_many, 319),
    ('Conv1Home', 'Вход 1 · «Обзор»: плитка «Конвертер»', home_entry, 319),
    ('Conv1More', 'Вход 2 · «Ещё → Деньги»: строка «Конвертер»', more_entry, 319),
    ('Conv1Rates', 'Вход 3 · «Курсы»: компактная карточка, без числа по умолчанию', rates_entry, 319),
    ('Conv1AEmpty', 'А «Табло»: пусто, курсор мигает', sheet_a_empty, 319),
    ('Conv1AType', 'А «Табло»: набрано 100 USD', sheet_a_typed, 319),
    ('Conv1BEmpty', 'Б «Отдаю / Получаю»: пусто', sheet_b_empty, 319),
    ('Conv1BType', 'Б «Отдаю / Получаю»: набрано', sheet_b_typed, 319),
    ('Conv1CEmpty', 'В «Все сразу»: пусто', sheet_c_empty, 319),
    ('Conv1CType', 'В «Все сразу»: набрано 100 USD', sheet_c_typed, 319),
    ('Conv1COther', 'В «Все сразу»: тап по ARS — считаем из песо', sheet_c_other, 319),
    ('Conv1CMany', 'В «Все сразу»: 8 валют итогов', sheet_c_many, 319),
    ('Conv1CNoRate', 'В «Все сразу»: у валюты нет курса', sheet_c_norate, 319),
]
