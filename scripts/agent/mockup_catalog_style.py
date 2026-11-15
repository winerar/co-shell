"""«Единый стиль» (owner 2026-11-15, poll «Единый стиль»: А for all 8 families; buttons — one height 48, circles too).

Approved: one rule per family of look-alike components (choice, labels, icon holders, surfaces, rows, buttons, bars,
inputs) in the А «Капсула» language. Boards Style1…Style8 under «Компоненты»; the bricks u_* are the reference for
core:designsystem tokens (components.md §«Единый стиль»).
"""
from mockup_catalog_kit import *  # noqa: F401,F403
from mockup_catalog_bricks import avatar, status_chip
from mockup_bricks import board

W = 640

# ---- the unified rule set (А «Капсула», tightened) ----
CHIP_H, CELL_H = 36, 44                  # chips in a row · cells of grids, segments, days
SEL = 'background: %s; color: %s; font-weight: 700; box-shadow: inset 0 0 0 2px %s;' % (LIME, ONLIME, INK)
IDLE = 'background: %s; color: %s;' % (CHIP, INK2)
BTN_H = 48                                # every button, the icon button (circle) included — owner 2026-11-15
R_SHEET, R_CARD, R_FIELD = 28, 24, 16     # sheets & dialogs · every card-like surface · text fields; controls — capsule


def u_chip(text, on=False, lead=''):
    return ('<span style="height: %dpx; padding: 0 14px; border-radius: 999px; font-size: 14px; display: inline-flex; align-items: center; gap: 7px; white-space: nowrap; %s">%s%s</span>'
            % (CHIP_H, SEL if on else IDLE, lead, text))


def u_more(text):
    return ('<span style="height: %dpx; padding: 0 14px; border-radius: 999px; font-size: 14px; display: inline-flex; align-items: center; box-shadow: inset 0 0 0 1px %s; color: %s;">%s</span>'
            % (CHIP_H, OUTLINE, INK, text))


def u_cells(labels, chosen, cols, circle=False):
    cells = ''.join('<span style="height: %dpx; border-radius: 999px; display: flex; align-items: center; justify-content: center; font-size: 14px; %s%s">%s</span>'
                    % (CELL_H, 'width: %dpx; justify-self: center; ' % CELL_H if circle else '', SEL if i == chosen else ('color: %s;' % INK if circle else IDLE), l)
                    for i, l in enumerate(labels))
    return '<div style="display: grid; grid-template-columns: repeat(%d, minmax(0, 1fr)); gap: 6px;">%s</div>' % (cols, cells)


def u_seg(labels, chosen):
    cells = ''.join('<span style="height: %dpx; border-radius: 999px; display: flex; align-items: center; justify-content: center; font-size: 14px; %s">%s</span>'
                    % (CELL_H - 8, SEL if i == chosen else 'color: %s;' % INK2, l) for i, l in enumerate(labels))
    return ('<div style="display: grid; grid-template-columns: repeat(%d, minmax(0, 1fr)); gap: 2px; padding: 4px; border-radius: 999px; background: %s;">%s</div>'
            % (len(labels), CHIP, cells))


def u_label(text, color, lead=None):
    return ('<span style="height: 22px; padding: 0 8px; border-radius: 999px; background: %s; display: inline-flex; align-items: center; gap: 5px; font-size: 11px; '
            'font-weight: 600; color: %s; white-space: nowrap;">%s%s</span>' % (mix(color, 18), INK, lead if lead is not None else dot(color, 6), text))


def u_holder(icon, color, size=40):
    return ('<span style="width: %dpx; height: %dpx; flex: none; border-radius: 50%%; background: %s; display: flex; align-items: center; justify-content: center;">%s</span>'
            % (size, size, mix(color, 18), svg(icon, color, round(size * 0.55), 1.9)))


def u_btn(text, kind='primary', icon=None):
    st = {'primary': 'background: %s; color: %s;' % (LIME, ONLIME), 'outline': 'box-shadow: inset 0 0 0 1px %s; color: %s;' % (OUTLINE, INK),
          'danger': 'background: %s; color: %s;' % (PINKBG, PINK), 'tonal': 'background: %s; color: %s;' % (VIOLETPILL, VIOLETINK), 'text': 'color: %s;' % LIME}[kind]
    ic = svg(icon, ONLIME if kind == 'primary' else INK, 20, 2) if icon else ''
    return ('<span style="height: %dpx; padding: 0 20px; border-radius: 999px; font-size: 15px; font-weight: 600; display: inline-flex; align-items: center; justify-content: center; '
            'gap: 8px; white-space: nowrap; %s">%s%s</span>' % (BTN_H, st, ic, text))


def u_icon_btn(icon):
    return ('<span style="width: %dpx; height: %dpx; border-radius: 50%%; background: %s; display: inline-flex; align-items: center; justify-content: center;">%s</span>'
            % (BTN_H, BTN_H, CHIP, svg(icon, INK, 22)))


def u_bar(frac, color=LIME, mark=None, h=8):
    m = ('<span style="position: absolute; left: %d%%; top: -3px; bottom: -3px; width: 2px; background: %s; box-shadow: 0 0 0 2px %s;"></span>' % (round(mark * 100), INK, CARD)
         if mark is not None else '')
    return ('<div style="position: relative; width: 100%%; height: %dpx; border-radius: 999px; background: %s;"><div style="width: %d%%; height: 100%%; border-radius: 999px; '
            'background: %s;"></div>%s</div>' % (h, KEY2, round(frac * 100), color, m))


def u_row(icon, color, title, sub, trail, h=56):
    return ('<div style="display: flex; align-items: center; gap: 12px; min-height: %dpx; padding: 0 16px;">%s%s%s</div>'
            % (h, u_holder(icon, color, 40 if h >= 56 else 32), two_lines(title, sub).replace('font-size: 14px', 'font-size: 16px').replace('font-size: 11px', 'font-size: 12px'), trail))


def u_line(inner):
    """A line inside a form card (#290 keeps it 40 dp): a capsule on the card."""
    return '<div style="display: flex; align-items: center; gap: 10px; height: 40px; padding: 0 14px; border-radius: 999px; background: %s;">%s</div>' % (KEY, inner)


def u_card(inner, r=R_CARD, pad='14px 16px', bg=None):
    return '<div style="border-radius: %dpx; background: %s; border: 1px solid %s; padding: %s;">%s</div>' % (r, bg or CARD, LINE, pad, inner)


def u_field(lbl, value, err=False):
    return ('<div style="min-height: 56px; padding: 7px 16px; box-sizing: border-box; border-radius: %dpx; background: %s; box-shadow: inset 0 0 0 1px %s; display: flex; '
            'flex-direction: column; justify-content: center;"><span style="font-size: 12px; color: %s;">%s</span><span style="font-size: 16px;">%s</span></div>'
            % (R_FIELD, FIELD, PINK if err else OUTLINE, PINK if err else MUTED, lbl, value))


def u_check(on):
    if not on:
        return '<span style="width: 22px; height: 22px; box-sizing: border-box; flex: none; border-radius: 7px; border: 2px solid %s;"></span>' % OUTLINE
    return ('<span style="width: 22px; height: 22px; box-sizing: border-box; flex: none; border-radius: 7px; border: 2px solid %s; background: %s; display: flex; align-items: center; '
            'justify-content: center;">%s</span>' % (INK, LIME, svg('check', ONLIME, 14, 2.8)))


def u_radio(on):
    if not on:
        return '<span style="width: 22px; height: 22px; box-sizing: border-box; flex: none; border-radius: 50%%; border: 2px solid %s;"></span>' % OUTLINE
    return ('<span style="width: 22px; height: 22px; box-sizing: border-box; flex: none; border-radius: 50%%; border: 2px solid %s; background: %s; display: flex; align-items: center; '
            'justify-content: center;">%s</span>' % (INK, LIME, dot(ONLIME, 8)))


def tagged(demo, note):
    """A demo with its code name and numbers under it."""
    return col(demo, '<div style="%s font-size: 10.5px; line-height: 1.5; color: %s;">%s</div>' % (MONO, MUTED, note), gap=6)


def compare(uni, rule):
    """The approved rule of a family: demos, then the rule in words."""
    return ('<div style="display: flex; flex-direction: column; gap: 16px;">%s</div>'
            '<div style="margin-top: 20px; padding: 14px 16px; border-radius: 20px; background: %s; border: 1px solid %s; font-size: 13.5px; line-height: 1.6;">%s</div>'
            % (uni, CARD, LINE, rule))


def page(title, lead, body):
    return ('<div style="padding: 36px 40px;"><h1 style="margin: 0; font-size: 30px;">%s</h1><p style="margin: 8px 0 24px; max-width: 940px; font-size: 14.5px; line-height: 1.5; color: %s;">%s</p>%s</div>'
            % (title, MUTED, lead, body))


# ---- families ----
def choice():
    uni = col(tagged(flexrow(u_chip('Карта', True), u_chip('Наличные'), u_chip('Вклад')), 'пилюля 36 · ChoicePill = FilterChip'),
              tagged(flexrow(u_chip('Отпуск', True, dot(TAGS[0][1], 8)), u_chip('Работа', False, dot(TAGS[1][1], 8)), u_more('Все 34')), 'тег — та же пилюля, цвет тега — в точке'),
              tagged(u_cells(['1-го', '5-го', '10-го', '15-го'], 1, 4), 'ячейка сетки 44 — капсула'),
              tagged(u_seg(['По типу', 'По валюте', 'Чьи'], 1), 'сегменты 44 — дорожка + пилюля'),
              tagged(u_cells([str(d) for d in range(1, 15)], 11, 7, True), 'день 44 — круг'))
    rule = ('<b>Одно правило для всего, чем выбирают:</b> выбрано — неон + рамка 2 dp цветом текста + жирный; не выбрано — залитая дорожка без рамки. '
            'Две высоты: 36 — чипы в строку, 44 — ячейки сеток, сегменты, дни. Форма — капсула (день — круг). ')
    return page('1 · Выбор', 'ChoicePill, FilterChip, TagChip, LabelGrid, Segmented, DayGrid, пункт меню и день календаря — всё это «выбери одно/несколько».', compare(uni, rule))


def labels():
    uni = col(tagged(flexrow(u_label('Отпуск', TAGS[0][1]), u_label('Работа', TAGS[1][1]), u_label('+2', MUTED, '')), 'тег'),
              tagged(flexrow(u_label('45 %', PINK, '<b style="color: %s;">↑</b>' % PINK), u_label('33 %', OK, '<b style="color: %s;">↓</b>' % OK)), 'изменение: стрелка вместо точки, цифры моно'),
              tagged(flexrow(u_label('Отправлено', CYAN), u_label('В работе', WARN), u_label('Готово', OK)), 'статус'),
              tagged(flexrow(u_label('новое', LIME)), 'новое'))
    rule = ('<b>Метка (не нажимается):</b> капсула 22, отступ 8, 11 sp полужирный; фон — цвет 18 %; цвет — только в точке 6 dp или стрелке; '
            'подпись — цветом текста (#385).')
    return page('2 · Метки', 'TagPill, ChangePill, статус, «новое» — короткая подпись-признак в строке или на карточке.', compare(uni, rule))


def holders():
    tint16 = lambda i, c: u_holder(i, c).replace(mix(c, 18), mix(c, 16))
    uni = col(tagged(flexrow(u_holder('cart', LIME), u_holder('cafe', '#FF8A5C'), u_holder('folder', LIME, 32), u_holder('rate', CYAN, 32), u_holder('trash', PINK, 56), u_holder('refresh', LIME, 32)),
                     'категория 40 · раздел меню 32 · вопрос 56 · уведомление 32'),
              tagged(flexrow(badge('GA', '#F26E22', '#1A0A00'), avatar('М', '#B266FF', 40)), 'залиты цветом — только «кто»: счёт (квадрат — это карта банка) и человек (круг)'))
    rule = ('<b>Иконка в подложке:</b> всегда круг, фон — цвет 18 %, иконка — цвет, 55 % размера. Три размера: 32 (меню, уведомление, плотная строка), 40 (строка списка), 56 (вопрос, пустой экран). '
            'Залитые — только счёт (скруглённый квадрат: отличаем счёт от категории) и участник (круг).')
    return page('3 · Иконка в подложке', 'CategoryBadge, IconBadge, MenuIcon, иконка вопроса, кружок уведомления, значок счёта, аватар.', compare(uni, rule))


def surfaces():
    def box(r, t, bg=None):
        return ('<div style="height: 64px; border-radius: %dpx; background: %s; border: 1px solid %s; display: flex; align-items: center; justify-content: center; font-size: 12px; color: %s; text-align: center; padding: 0 6px;">%s</div>'
                % (r, bg or CARD, LINE, MUTED, t))
    grid2 = lambda *c: '<div style="display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); gap: 10px;">%s</div>' % ''.join(c)
    uni = col(grid2(box(R_CARD, 'Card · 24'), box(R_CARD, 'RowGroup · 24'), box(R_CARD, 'FormSection · 24'), box(R_CARD, 'Message · 24'), box(R_CARD, 'PeekCard · 24'), box(R_CARD, 'Tile · 24'),
                    box(R_CARD, 'ConfirmCard · 24'), box(R_SHEET, 'Dialog · 28'), box(R_SHEET, 'Sheet · 28')),
              tagged(flexrow(u_line(svg('calendar', MUTED, 18) + two_lines('Дата', 'сегодня')), '<span style="width: 150px; height: 40px; border-radius: 16px; box-shadow: inset 0 0 0 1px %s;"></span>' % OUTLINE),
                     'плитка формы — капсула 40 · поле 16'))
    rule = ('<b>Четыре ступени формы:</b> 28 — то, что выезжает поверх (шторка, диалог); 24 — всё, что карточка (карточка, группа строк, карточка формы, плитка «Обзора», сообщение, '
            'карточка-вопрос, пузырь, меню); 16 — поле ввода; капсула — всё, что нажимается или выбирается (кнопки, чипы, сегменты, поиск, плитки формы, пункты меню). ')
    return page('4 · Поверхности и радиусы', 'Карточки, группы строк, сообщения, пузырь, вопрос, диалог, шторка, поля, плитки формы.', compare(uni, rule))


def rows():
    uni_rows = ''.join('<div style="%s">%s</div>' % ('border-top: 1px solid %s;' % LINE2 if i else '', r) for i, r in enumerate([
        u_row('rate', CYAN, 'Валюты отчётов', 'Основная: USD', chevron()),
        u_row('folder', LIME, 'Категории', '24 категории', chevron(), 52),
        u_row('wallet', VIOLET, 'Учитывать в общем балансе', 'для долга — выключить', switch(True))]))
    uni = col(tagged(u_card(uni_rows, pad='0'), 'строка 56 (плотная 52) · иконка — круг 40 / 32 · отступ 16'),
              tagged(col(u_line(u_check(True) + two_lines('Дом', 'общая')), u_line(two_lines('Повторять', 'каждый месяц') + switch(True))), 'строка формы — капсула 40 (#290: шторка без прокрутки)'))
    rule = ('<b>Два вида строк:</b> строка списка — 56 (плотная 52), слева иконка-круг, текст 16 / 12, справа шеврон, значение или переключатель; '
            'строка формы — капсула 40 на карточке, текст 14 / 11. Переключатель, флажок и иконка стоят в одних и тех же местах. ')
    return page('5 · Строки', 'ListRow, MenuRow, SwitchRow; в карточке формы — CheckTile, DetailTile, SwitchLine.', compare(uni, rule))


def buttons():
    uni = col(tagged(flexrow(u_btn('Сохранить'), u_btn('Отмена', 'outline'), u_btn('Удалить', 'danger')), 'одна высота 48'),
              tagged(flexrow(u_btn('Синхр.', 'tonal', 'refresh'), u_btn('Все →', 'text'), u_icon_btn('search'), u_icon_btn('back')), 'текстовая — та же 48; кнопка-иконка — круг 48 на дорожке'))
    rule = ('<b>Кнопка:</b> капсула 48, текст 15 полужирный, иконка 20; оттенки: основная, контур, опасная, тональная, текстом — размер у всех один. '
            'Кнопка-иконка и круглые кнопки — круг 48 на залитой дорожке: высота у всех кнопок одна — стандартная зона касания 48.')
    return page('6 · Кнопки', 'CoShellButton, OutlinedButton, TextButton, IconButton, кнопки вопроса и шторки.', compare(uni, rule))


def scales():
    uni = col(tagged(u_bar(0.42, LIME, 0.5), 'шкала бюджета'), tagged(u_bar(0.7, '#FF8A5C'), 'строка-столбик'), tagged(u_bar(0.62), 'загрузка, прогресс'))
    rule = ('<b>Полоса:</b> 8 dp, капсула, дорожка — тон поверхности; отметка «сегодня» или «прошлый период» — черта 2 dp цветом текста с ореолом. ')
    return page('7 · Полосы', 'CoShellBullet, CoShellBarRow, CoShellLinearProgress.', compare(uni, rule))


def inputs():
    uni = col(tagged(u_field('Название', 'Galicia · карта'), 'поле 56 · радиус 16 · рамка outline ≥ 3:1'),
              tagged('<div style="height: 48px; border-radius: 999px; background: %s; box-shadow: inset 0 0 0 1px %s; display: flex; align-items: center; gap: 10px; padding: 0 16px; color: %s;">%s Поиск: заметка, сумма</div>'
                     % (FIELD, OUTLINE, MUTED, svg('search', MUTED, 20)), 'поиск — капсула 48 с той же рамкой'),
              tagged(flexrow(u_check(False), u_check(True), u_radio(False), u_radio(True), switch(True), gap=16), 'пусто — рамка outline; выбрано — неон + рамка цветом текста'))
    rule = ('<b>Ввод:</b> поле — 56, радиус 16, рамка outline (≥ 3:1, #385), подпись внутри; поиск — капсула 48 с той же рамкой и заливкой; '
            'флажок и радио — 22, пустые — рамка outline, выбранные — неон + рамка цветом текста, как всё выбранное.')
    return page('8 · Поля и отметки', 'CoShellTextField, CoShellAmountField, CoShellSearchField, CoShellCheckMark, CoShellRadioMark, CoShellSwitch.', compare(uni, rule))


FAMILIES = [('Style1', 'Единый стиль · выбор', choice), ('Style2', 'Единый стиль · метки', labels), ('Style3', 'Единый стиль · иконки', holders),
            ('Style4', 'Единый стиль · поверхности', surfaces), ('Style5', 'Единый стиль · строки', rows), ('Style6', 'Единый стиль · кнопки', buttons),
            ('Style7', 'Единый стиль · полосы', scales), ('Style8', 'Единый стиль · поля и отметки', inputs)]
HEIGHTS = {'Style1': 744, 'Style2': 504, 'Style3': 490, 'Style4': 646, 'Style5': 648, 'Style6': 490, 'Style7': 384, 'Style8': 540}  # content + ~20, measured in Chromium


def make(name, title, fn):
    h = HEIGHTS.get(name, 900)
    return board(title, fn(), height=h).replace('width: 390px; height: %dpx' % h, 'width: %dpx; height: %dpx' % (W, h)).replace(
        '"width":390,"height":%d' % h, '"width":%d,"height":%d' % (W, h))


BOARDS = [(n, (lambda n=n, t=t, f=f: make(n, t, f))) for n, t, f in FAMILIES]
