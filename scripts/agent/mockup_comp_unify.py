"""«Единый стиль» (owner 2026-11-15): components that look alike or serve one purpose → one rule per family.

A proposal, not the catalog (ApprovedComponentsTest reads only mockup_catalog*.py). Each board: «Сейчас» — the
families as the code draws them today (bricks of mockup_catalog_parts, drawn after core:designsystem) and «Единый» —
one rule for the family, in the approved А «Капсула» language. After the owner's «ок» the rule goes into the kit
(mockup_catalog_kit.py), the boards CompNew…CompData and core:designsystem tokens.
"""
from mockup_catalog_kit import *  # noqa: F401,F403
from mockup_catalog_bricks import avatar, status_chip
from mockup_bricks import board

W = 1040

# ---- the unified rule set (А «Капсула», tightened) ----
CHIP_H, CELL_H = 36, 44                  # chips in a row · cells of grids, segments, days
SEL = 'background: %s; color: %s; font-weight: 700; box-shadow: inset 0 0 0 2px %s;' % (LIME, ONLIME, INK)
IDLE = 'background: %s; color: %s;' % (CHIP, INK2)
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
    return ('<span style="height: 52px; padding: 0 20px; border-radius: 999px; font-size: 15px; font-weight: 600; display: inline-flex; align-items: center; justify-content: center; '
            'gap: 8px; white-space: nowrap; %s">%s%s</span>' % (st, ic, text))


def u_icon_btn(icon):
    return ('<span style="width: 40px; height: 40px; border-radius: 50%%; background: %s; display: inline-flex; align-items: center; justify-content: center;">%s</span>'
            % (CHIP, svg(icon, INK, 20)))


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


# ---- «Сейчас»: today's code (sizes and colour roles from core:designsystem) ----
def now_chip(text, h, r, st, fs=13):
    return ('<span style="height: %dpx; padding: 0 12px; border-radius: %s; font-size: %dpx; display: inline-flex; align-items: center; gap: 6px; white-space: nowrap; %s">%s</span>'
            % (h, r, fs, st, text))


VIOL = 'background: %s; color: %s; font-weight: 600;' % (VIOLETPILL, VIOLETINK)
LIMEW = 'background: %s; color: %s; font-weight: 600;' % (LIMEBG, INK)
NEON = 'background: %s; color: %s; font-weight: 600;' % (LIME, ONLIME)
HAIR = 'box-shadow: inset 0 0 0 1px %s; color: %s;' % (LINE2, INK2)


def tagged(demo, note):
    """A demo with its code name and numbers under it."""
    return col(demo, '<div style="%s font-size: 10.5px; line-height: 1.5; color: %s;">%s</div>' % (MONO, MUTED, note), gap=6)


def compare(now, uni, rule):
    head = '<div style="font-size: 12px; font-weight: 700; letter-spacing: 0.06em; color: %s;">%s</div>'
    return ('<div style="display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 24px;">'
            '<div style="display: flex; flex-direction: column; gap: 16px; min-width: 0;">%s%s</div>'
            '<div style="display: flex; flex-direction: column; gap: 16px; min-width: 0; padding-left: 24px; border-left: 1px dashed %s;">%s%s</div></div>'
            '<div style="margin-top: 20px; padding: 14px 16px; border-radius: 20px; background: %s; border: 1px solid %s; font-size: 13.5px; line-height: 1.6;">%s</div>'
            % (head % (MUTED, 'СЕЙЧАС · В КОДЕ'), now, LINE2, head % (LIME, 'ЕДИНЫЙ'), uni, CARD, LINE, rule))


def page(title, lead, body):
    return ('<div style="padding: 36px 40px;"><h1 style="margin: 0; font-size: 30px;">%s</h1><p style="margin: 8px 0 24px; max-width: 940px; font-size: 14.5px; line-height: 1.5; color: %s;">%s</p>%s</div>'
            % (title, MUTED, lead, body))


# ---- families ----
def choice():
    now = col(tagged(flexrow(now_chip('Карта', 30, '999px', VIOL), now_chip('Наличные', 30, '999px', HAIR)), 'CoShellChoicePill · 30 · выбран фиолетовым тоном, рамка 1'),
              tagged(flexrow(now_chip('Еда · 3', 32, '8px', VIOL), now_chip('Счёт', 32, '8px', HAIR)), 'CoShellFilterChip (Material) · 32 · радиус 8'),
              tagged(flexrow(tag_chip('Отпуск', TAGS[0][1], True, True), tag_chip('Работа', TAGS[1][1], False)), 'CoShellTagChip · 34 · выбран тоном тега'),
              tagged(label_grid(['1-го', '5-го', '10-го', '15-го'], 1), 'CoShellLabelGrid · 36–48 · радиус 12 · тон основного'),
              tagged(seg_v2(['По типу', 'По валюте', 'Чьи'], 1, 'view'), 'CoShellSegmented View · 40 · фиолетовый'),
              tagged(day_grid(12, days=14).replace('height: 36px; border-radius: 50%', 'height: 40px; width: 40px; justify-self: center; border-radius: 50%'), 'CoShellDayGrid · круг 40–48 · тон основного'))
    uni = col(tagged(flexrow(u_chip('Карта', True), u_chip('Наличные'), u_chip('Вклад')), 'пилюля 36 · ChoicePill = FilterChip'),
              tagged(flexrow(u_chip('Отпуск', True, dot(TAGS[0][1], 8)), u_chip('Работа', False, dot(TAGS[1][1], 8)), u_more('Все 34')), 'тег — та же пилюля, цвет тега — в точке'),
              tagged(u_cells(['1-го', '5-го', '10-го', '15-го'], 1, 4), 'ячейка сетки 44 — капсула'),
              tagged(u_seg(['По типу', 'По валюте', 'Чьи'], 1), 'сегменты 44 — дорожка + пилюля'),
              tagged(u_cells([str(d) for d in range(1, 15)], 11, 7, True), 'день 44 — круг'))
    rule = ('<b>Одно правило для всего, чем выбирают:</b> выбрано — неон + рамка 2 dp цветом текста + жирный; не выбрано — залитая дорожка без рамки. '
            'Две высоты: 36 — чипы в строку, 44 — ячейки сеток, сегменты, дни. Форма — капсула (день — круг). '
            'Сейчас: 4 разных «выбранных» (неон, тон основного, фиолетовый, тон тега), 5 высот (30–48), радиусы 8 / 12 / капсула.')
    return page('1 · Выбор', 'ChoicePill, FilterChip, TagChip, LabelGrid, Segmented, DayGrid, пункт меню и день календаря — всё это «выбери одно/несколько».', compare(now, uni, rule))


def labels():
    now = col(tagged(flexrow(tag_pill('Отпуск', TAGS[0][1]), tag_pill('Работа', TAGS[1][1])), 'CoShellTagPill · 22 · тон + рамка, подпись цветом тега'),
              tagged(flexrow(change_pill('↑45 %', PINK), change_pill('↓33 %', OK)), 'CoShellChangePill · 20 · подпись цветом'),
              tagged(flexrow(status_chip('Отправлено', 'new'), status_chip('Готово', 'done')), 'StatusChip (макет) · 24'),
              tagged(flexrow(now_chip('новое', 20, '6px', 'background: %s; color: %s; font-size: 10px; font-weight: 700;' % (LIME, ONLIME))), 'метка «новое» · 20 · радиус 6'))
    uni = col(tagged(flexrow(u_label('Отпуск', TAGS[0][1]), u_label('Работа', TAGS[1][1]), u_label('+2', MUTED, '')), 'тег'),
              tagged(flexrow(u_label('45 %', PINK, '<b style="color: %s;">↑</b>' % PINK), u_label('33 %', OK, '<b style="color: %s;">↓</b>' % OK)), 'изменение: стрелка вместо точки, цифры моно'),
              tagged(flexrow(u_label('Отправлено', CYAN), u_label('В работе', WARN), u_label('Готово', OK)), 'статус'),
              tagged(flexrow(u_label('новое', LIME)), 'новое'))
    rule = ('<b>Метка (не нажимается):</b> капсула 22, отступ 8, 11 sp полужирный; фон — цвет 18 %; цвет — только в точке 6 dp или стрелке; '
            'подпись — цветом текста (#385). Сейчас: 3 высоты (20/22/24), радиус 6 у «нового», подпись то цветом, то текстом, у тега ещё рамка.')
    return page('2 · Метки', 'TagPill, ChangePill, статус, «новое» — короткая подпись-признак в строке или на карточке.', compare(now, uni, rule))


def holders():
    tint16 = lambda i, c: u_holder(i, c).replace(mix(c, 18), mix(c, 16))
    now = col(tagged(flexrow(tint16('cart', LIME), tint16('cafe', '#FF8A5C'), '<span style="width: 12px;"></span>', dot_icon('cart', LIME, 40), dot_icon('cafe', '#FF8A5C', 40)),
                     'CategoryBadge / IconBadge в коде — круг 40, тон 16 % · на экранных досках — залит цветом (расходятся)'),
              tagged(flexrow(menu_icon('folder', LIME), menu_icon('rate', CYAN)), 'MenuIcon · квадрат 32, радиус 8, тон 20 %'),
              tagged(flexrow('<span style="width: 48px; height: 48px; border-radius: 16px; background: %s; display: flex; align-items: center; justify-content: center;">%s</span>' % (mix(PINK, 18), svg('trash', PINK, 24))),
                     'иконка вопроса (ConfirmCard/Dialog) · квадрат 48, радиус 16'),
              tagged(flexrow('<span style="width: 32px; height: 32px; border-radius: 50%%; background: %s; display: flex; align-items: center; justify-content: center;">%s</span>' % (LIME, svg('refresh', ONLIME, 18))),
                     'NoticePill · круг 32 залит основным'),
              tagged(flexrow(badge('GA', '#F26E22', '#1A0A00'), avatar('М', '#B266FF', 40)), 'AccountBadge — квадрат 30 % залит банком · CoShellAvatar — круг залит'))
    uni = col(tagged(flexrow(u_holder('cart', LIME), u_holder('cafe', '#FF8A5C'), u_holder('folder', LIME, 32), u_holder('rate', CYAN, 32), u_holder('trash', PINK, 56), u_holder('refresh', LIME, 32)),
                     'категория 40 · раздел меню 32 · вопрос 56 · уведомление 32'),
              tagged(flexrow(badge('GA', '#F26E22', '#1A0A00'), avatar('М', '#B266FF', 40)), 'залиты цветом — только «кто»: счёт (квадрат — это карта банка) и человек (круг)'))
    rule = ('<b>Иконка в подложке:</b> всегда круг, фон — цвет 18 %, иконка — цвет, 55 % размера. Три размера: 32 (меню, уведомление, плотная строка), 40 (строка списка), 56 (вопрос, пустой экран). '
            'Залитые — только счёт (скруглённый квадрат: отличаем счёт от категории) и участник (круг). Сейчас: круг / квадрат 8 / квадрат 16, размеры 32/40/48, тон 16 или 20 %.')
    return page('3 · Иконка в подложке', 'CategoryBadge, IconBadge, MenuIcon, иконка вопроса, кружок уведомления, значок счёта, аватар.', compare(now, uni, rule))


def surfaces():
    def box(r, t, bg=None):
        return ('<div style="height: 64px; border-radius: %dpx; background: %s; border: 1px solid %s; display: flex; align-items: center; justify-content: center; font-size: 12px; color: %s; text-align: center; padding: 0 6px;">%s</div>'
                % (r, bg or CARD, LINE, MUTED, t))
    grid2 = lambda *c: '<div style="display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); gap: 10px;">%s</div>' % ''.join(c)
    now = col(grid2(box(16, 'Card · 16'), box(16, 'RowGroup · 16'), box(16, 'FormSection · 16'), box(16, 'Message · 16'), box(16, 'PeekCard · 16'), box(16, 'Tile · 16'),
                    box(28, 'ConfirmCard · 28'), box(28, 'Dialog · 28'), box(28, 'Sheet · 28')),
              tagged(flexrow(tile(svg('calendar', MUTED, 18) + two_lines('Дата', 'сегодня'), 170), '<span style="width: 150px; height: 40px; border-radius: 12px; box-shadow: inset 0 0 0 1px %s;"></span>' % OUTLINE),
                     'плитка формы 40 · радиус 12 · поле 12'))
    uni = col(grid2(box(R_CARD, 'Card · 24'), box(R_CARD, 'RowGroup · 24'), box(R_CARD, 'FormSection · 24'), box(R_CARD, 'Message · 24'), box(R_CARD, 'PeekCard · 24'), box(R_CARD, 'Tile · 24'),
                    box(R_CARD, 'ConfirmCard · 24'), box(R_SHEET, 'Dialog · 28'), box(R_SHEET, 'Sheet · 28')),
              tagged(flexrow(u_line(svg('calendar', MUTED, 18) + two_lines('Дата', 'сегодня')), '<span style="width: 150px; height: 40px; border-radius: 16px; box-shadow: inset 0 0 0 1px %s;"></span>' % OUTLINE),
                     'плитка формы — капсула 40 · поле 16'))
    rule = ('<b>Четыре ступени формы:</b> 28 — то, что выезжает поверх (шторка, диалог); 24 — всё, что карточка (карточка, группа строк, карточка формы, плитка «Обзора», сообщение, '
            'карточка-вопрос, пузырь, меню); 16 — поле ввода; капсула — всё, что нажимается или выбирается (кнопки, чипы, сегменты, поиск, плитки формы, пункты меню). '
            'Сейчас: карточки 16, вопрос 28, поля 12, плитки формы 12 — на досках «Капсулы» уже 24/20/16.')
    return page('4 · Поверхности и радиусы', 'Карточки, группы строк, сообщения, пузырь, вопрос, диалог, шторка, поля, плитки формы.', compare(now, uni, rule))


def rows():
    now = group([row('Валюты отчётов', 'Основная: USD', dot_icon('rate', CYAN, 40), chevron()),
                 menu_row('folder', LIME, 'Категории', '24 категории'),
                 row('Учитывать в общем балансе', 'для долга — выключить', '', switch(True))])
    now = col(tagged(now, 'ListRow 56 · MenuRow 52 (иконка-квадрат 32) · SwitchRow 56 без иконки'),
              tagged(col(check_tile(True, 'Дом', 'общая'), switch_line('Повторять', 'каждый месяц', True)), 'CheckTile 40 · SwitchLine — без подложки'))
    uni_rows = ''.join('<div style="%s">%s</div>' % ('border-top: 1px solid %s;' % LINE2 if i else '', r) for i, r in enumerate([
        u_row('rate', CYAN, 'Валюты отчётов', 'Основная: USD', chevron()),
        u_row('folder', LIME, 'Категории', '24 категории', chevron(), 52),
        u_row('wallet', VIOLET, 'Учитывать в общем балансе', 'для долга — выключить', switch(True))]))
    uni = col(tagged(u_card(uni_rows, pad='0'), 'строка 56 (плотная 52) · иконка — круг 40 / 32 · отступ 16'),
              tagged(col(u_line(u_check(True) + two_lines('Дом', 'общая')), u_line(two_lines('Повторять', 'каждый месяц') + switch(True))), 'строка формы — капсула 40 (#290: шторка без прокрутки)'))
    rule = ('<b>Два вида строк:</b> строка списка — 56 (плотная 52), слева иконка-круг, текст 16 / 12, справа шеврон, значение или переключатель; '
            'строка формы — капсула 40 на карточке, текст 14 / 11. Переключатель, флажок и иконка стоят в одних и тех же местах. '
            'Сейчас: у MenuRow иконка-квадрат, у SwitchRow места под иконку нет, SwitchLine и CheckTile выглядят по-разному.')
    return page('5 · Строки', 'ListRow, MenuRow, SwitchRow; в карточке формы — CheckTile, DetailTile, SwitchLine.', compare(now, uni, rule))


def buttons():
    now = col(tagged(flexrow(now_chip('Сохранить', 40, '999px', NEON, 14), now_chip('Отмена', 40, '999px', HAIR, 14)), 'CoShellButton / OutlinedButton · Material 40'),
              tagged(flexrow(now_chip('Сохранить', 54, '999px', NEON, 15), now_chip('Не сейчас', 54, '999px', HAIR, 15)), 'кнопки вопроса и шторки · 54'),
              tagged(flexrow(now_chip('Все →', 40, '999px', 'color: %s; font-weight: 600;' % LIME, 14), icon_button('search', 'Поиск'), icon_button('back', 'Назад')), 'TextButton 40 · IconButton 48 без подложки'))
    uni = col(tagged(flexrow(u_btn('Сохранить'), u_btn('Отмена', 'outline'), u_btn('Удалить', 'danger')), 'одна высота 52'),
              tagged(flexrow(u_btn('Синхр.', 'tonal', 'refresh'), u_btn('Все →', 'text'), u_icon_btn('search'), u_icon_btn('back')), 'текстовая — та же 52; кнопка-иконка — круг 40 на дорожке (касание 48)'))
    rule = ('<b>Кнопка:</b> капсула 52, текст 15 полужирный, иконка 20; оттенки: основная, контур, опасная, тональная, текстом — размер у всех один. '
            'Кнопка-иконка — круг 40 на залитой дорожке, зона касания 48. Сейчас: 40 (Material), 54 в вопросах и шторках, иконки без подложки.')
    return page('6 · Кнопки', 'CoShellButton, OutlinedButton, TextButton, IconButton, кнопки вопроса и шторки.', compare(now, uni, rule))


def scales():
    now = col(tagged(bullet_v2(0.42, 'ok', 0.5), 'CoShellBullet · 8 · радиус 4'),
              tagged('<div style="height: 10px; border-radius: 5px; background: %s; width: 70%%;"></div>' % LIME, 'CoShellBarRow · 10'),
              tagged('<div style="height: 4px; border-radius: 2px; background: %s;"><div style="width: 62%%; height: 100%%; background: %s; border-radius: 2px;"></div></div>' % (KEY2, LIME),
                     'CoShellLinearProgress (Material) · 4'))
    uni = col(tagged(u_bar(0.42, LIME, 0.5), 'шкала бюджета'), tagged(u_bar(0.7, '#FF8A5C'), 'строка-столбик'), tagged(u_bar(0.62), 'загрузка, прогресс'))
    rule = ('<b>Полоса:</b> 8 dp, капсула, дорожка — тон поверхности; отметка «сегодня» или «прошлый период» — черта 2 dp цветом текста с ореолом. '
            'Сейчас: 8 / 10 / 4 dp, у прогресса нет отметки.')
    return page('7 · Полосы', 'CoShellBullet, CoShellBarRow, CoShellLinearProgress.', compare(now, uni, rule))


def inputs():
    now = col(tagged(field('Название', 'Galicia · карта'), 'CoShellTextField · радиус 12, рамка outlineVariant'),
              tagged(search('Поиск: заметка, сумма'), 'CoShellSearchField · капсула 48, без рамки'),
              tagged(flexrow(check_mark('off'), check_mark('on'), radio_mark(False), radio_mark(True), switch(True), gap=16), 'флажок 22 r7 · радио — точка без заливки · рамки 1,8–1,95:1 в тёмной'))
    uni = col(tagged(u_field('Название', 'Galicia · карта'), 'поле 56 · радиус 16 · рамка outline ≥ 3:1'),
              tagged('<div style="height: 48px; border-radius: 999px; background: %s; box-shadow: inset 0 0 0 1px %s; display: flex; align-items: center; gap: 10px; padding: 0 16px; color: %s;">%s Поиск: заметка, сумма</div>'
                     % (FIELD, OUTLINE, MUTED, svg('search', MUTED, 20)), 'поиск — капсула 48 с той же рамкой'),
              tagged(flexrow(u_check(False), u_check(True), u_radio(False), u_radio(True), switch(True), gap=16), 'пусто — рамка outline; выбрано — неон + рамка цветом текста'))
    rule = ('<b>Ввод:</b> поле — 56, радиус 16, рамка outline (≥ 3:1, #385), подпись внутри; поиск — капсула 48 с той же рамкой и заливкой; '
            'флажок и радио — 22, пустые — рамка outline, выбранные — неон + рамка цветом текста, как всё выбранное.')
    return page('8 · Поля и отметки', 'CoShellTextField, CoShellAmountField, CoShellSearchField, CoShellCheckMark, CoShellRadioMark, CoShellSwitch.', compare(now, uni, rule))


FAMILIES = [('Uni1', 'Единый стиль · выбор', choice), ('Uni2', 'Единый стиль · метки', labels), ('Uni3', 'Единый стиль · иконки', holders),
            ('Uni4', 'Единый стиль · поверхности', surfaces), ('Uni5', 'Единый стиль · строки', rows), ('Uni6', 'Единый стиль · кнопки', buttons),
            ('Uni7', 'Единый стиль · полосы', scales), ('Uni8', 'Единый стиль · поля и отметки', inputs)]
HEIGHTS = {'Uni1': 764, 'Uni2': 512, 'Uni3': 670, 'Uni4': 634, 'Uni5': 640, 'Uni6': 534, 'Uni7': 416, 'Uni8': 540}  # content + ~20, measured in Chromium


def make(name, title, fn):
    h = HEIGHTS.get(name, 900)
    return board(title, fn(), height=h).replace('width: 390px; height: %dpx' % h, 'width: %dpx; height: %dpx' % (W, h)).replace(
        '"width":390,"height":%d' % h, '"width":%d,"height":%d' % (W, h))


BOARDS = [(n, (lambda n=n, t=t, f=f: make(n, t, f))) for n, t, f in FAMILIES]
