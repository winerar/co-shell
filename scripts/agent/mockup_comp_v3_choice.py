"""«Компоненты v3» — existing choice components (#367, part 1 of 3), each as a function of the rule set v."""
from mockup_comp_v3_kit import *  # noqa: F401,F403


def seg(v, labels, chosen, view=False, disabled=False):
    """CoShellSegmented. View tone: А — violet wash instead of neon; Б — same tint, the frame tells it."""
    on = sel(v) if not view or v == 'B' else 'background: %s; color: %s; font-weight: 700; box-shadow: inset 0 0 0 2px %s;' % (VIOLETPILL, VIOLETINK, INK)
    cells = ''.join('<span style="height: 38px; border-radius: %s; display: flex; align-items: center; justify-content: center; font-size: 14px; min-width: 0; %s">'
                    '<span style="overflow: hidden; white-space: nowrap; text-overflow: ellipsis; padding: 0 4px;">%s</span></span>'
                    % ('999px' if v == 'A' else '9px', on if i == chosen else 'color: %s;' % INK2, l) for i, l in enumerate(labels))
    return ('<div style="width: 100%%; box-sizing: border-box; display: grid; grid-template-columns: repeat(%d, minmax(0, 1fr)); gap: 2px; border-radius: %s; %s %s">%s</div>'
            % (len(labels), r(v, 'ctl'), track(v), 'opacity: 0.5;' if disabled else '', cells))


def segments(v):
    return col(labeled(seg(v, ['Расход', 'Доход', 'Перевод'], 0), 'Choice — вид операции'), labeled(seg(v, ['По типу', 'По валюте', 'Чьи'], 1, True), 'View — как показать'),
               labeled(seg(v, ['Категория', 'Папка'], 0, disabled=True), 'заблокировано — 50 %'), labeled(seg(v, ['Ежедневно', 'Еженедельно', 'Ежемесячно', 'Никогда'], 2), 'длинные — «…»'))


def pill(v, text, on=False):
    off = 'color: %s; background: %s;' % (INK2, CHIP) if v == 'A' else idle(v)
    return ('<span style="height: 32px; padding: 0 13px; border-radius: %s; font-size: 13px; display: inline-flex; align-items: center; gap: 5px; %s">%s%s</span>'
            % (r(v, 'small'), sel(v) if on else off, svg('check', ink_on_sel(v), 14, 2.6) if on and v == 'B' else '', text))


def pills(v):
    return col(flexrow(pill(v, 'Карта', True), pill(v, 'Наличные'), pill(v, 'Кошелёк'), pill(v, 'Вклад'), pill(v, 'Долг')),
               cap('3–5 вариантов в карточке формы; выбранная — ' + ('неон + рамка' if v == 'A' else 'тон + рамка + ✓')))


def check(v, state):
    rad = '7px' if v == 'A' else '4px'
    if state == 'off':
        return '<span style="width: 22px; height: 22px; box-sizing: border-box; flex: none; border-radius: %s; border: 2px solid %s;"></span>' % (rad, OUTLINE)
    fill, ink = (LIME, ONLIME) if v == 'A' else (INK, CARD)
    inner = svg('check', ink, 16, 2.8) if state == 'on' else '<span style="width: 10px; height: 2.5px; background: %s;"></span>' % ink
    return '<span style="width: 22px; height: 22px; flex: none; border-radius: %s; background: %s; display: flex; align-items: center; justify-content: center;">%s</span>' % (rad, fill, inner)


def radio(v, on):
    if not on:
        return '<span style="width: 22px; height: 22px; box-sizing: border-box; flex: none; border-radius: 50%%; border: 2px solid %s;"></span>' % OUTLINE
    if v == 'A':
        return ('<span style="width: 22px; height: 22px; box-sizing: border-box; flex: none; border-radius: 50%%; border: 2px solid %s; background: %s; display: flex; '
                'align-items: center; justify-content: center;">%s</span>' % (INK, LIME, dot(ONLIME, 8)))
    return '<span style="width: 22px; height: 22px; box-sizing: border-box; flex: none; border-radius: 50%%; border: 7px solid %s;"></span>' % INK


def marks(v):
    return col(flexrow(labeled(check(v, 'off'), 'нет'), labeled(check(v, 'on'), 'да'), labeled(check(v, 'some'), 'часть'),
                       labeled(radio(v, False), 'не выбран'), labeled(radio(v, True), 'выбран'), gap=16),
               cap('пустые — рамка outline (#385); выбранные — ' + ('неон' if v == 'A' else 'цвет текста')))


def tile(v, inner):
    return ('<div style="display: flex; align-items: center; gap: 10px; min-height: 48px; padding: 0 12px; border-radius: %s; box-sizing: border-box; %s">%s</div>'
            % (r(v, 'tile') if v == 'B' else '18px', 'background: %s;' % KEY if v == 'A' else 'box-shadow: inset 0 0 0 1px %s;' % OUTLINE, inner))


def detail(v, icon, lbl, value, empty_=False):
    return tile(v, holder(v, icon, MUTED, 28) + '<span style="display: flex; flex-direction: column; min-width: 0;"><span style="font-size: 11px; color: %s;">%s</span>'
                '<span style="font-size: 14px; color: %s;">%s</span></span>' % (MUTED, lbl, MUTED if empty_ else INK, value))


def tiles(v):
    return col(tile(v, check(v, 'off') + two_lines('Это возврат', 'деньги вернули')), tile(v, check(v, 'on') + two_lines('Это возврат', 'деньги вернули')),
               '<div style="display: grid; grid-template-columns: 1fr 1fr; gap: 8px;">%s%s</div>' % (detail(v, 'calendar', 'Дата', 'Сегодня, 15 ноя'), detail(v, 'note', 'Заметка', 'Нет', True)))


def sw(v, on):
    if v == 'A':
        return switch(on)
    knob = 'right: 3px; background: %s;' % CARD if on else 'left: 3px; background: %s;' % OUTLINE
    return ('<span style="position: relative; flex: none; width: 48px; height: 28px; box-sizing: border-box; border-radius: 9px; %s"><span style="position: absolute; top: 3px; %s '
            'width: 22px; height: 22px; border-radius: 6px;"></span></span>' % ('background: %s;' % INK if on else 'box-shadow: inset 0 0 0 2px %s;' % OUTLINE, knob))


def switch_lines(v):
    return col(flexrow(two_lines('Учитывать в балансе', 'для долга — выключить'), sw(v, True), gap=12, wrap=False),
               flexrow(two_lines('Учитывать в балансе', 'для долга — выключить'), sw(v, False), gap=12, wrap=False),
               flexrow(two_lines('Очень длинное название настройки, которое не помещается', 'и длинная подпись'), sw(v, True), gap=12, wrap=False))


def expand(v, open_):
    if v == 'A':
        return '<span style="display: inline-flex; transform: rotate(%ddeg);">%s</span>' % (180 if open_ else 0, svg('down', INK2, 22))
    return ('<span style="width: 28px; height: 28px; border-radius: 8px; box-shadow: inset 0 0 0 1px %s; display: flex; align-items: center; justify-content: center;">%s</span>'
            % (OUTLINE, svg('M6 12h12' if open_ else 'plus', INK2, 18, 2)))


def expands(v):
    return flexrow(labeled(expand(v, False), 'закрыто'), labeled(expand(v, True), 'открыто'), cap('стрелка поворачивается пружиной' if v == 'A' else '«+» → «−», плитка'), gap=24)


def days(v):
    rad = '50%' if v == 'A' else '10px'
    cells = ''.join('<span style="height: 34px; border-radius: %s; display: flex; flex-direction: column; align-items: center; justify-content: center; font-size: 13px; %s">%d%s</span>'
                    % (rad, sel(v) if d == 10 else 'color: %s;' % INK, d, dot(ink_on_sel(v) if d == 10 else MUTED, 4) if d == 1 else '') for d in range(1, 29))
    return col('<div style="display: grid; grid-template-columns: repeat(7, minmax(0, 1fr)); gap: 2px;">%s</div>' % cells, cap('начало месяца: выбранный · по умолчанию — точка'))


def labels(v):
    def g(items, chosen, cols=4):
        return ('<div style="display: grid; grid-template-columns: repeat(%d, minmax(0, 1fr)); gap: 6px;">%s</div>'
                % (cols, ''.join('<span style="height: 34px; border-radius: %s; display: flex; align-items: center; justify-content: center; font-size: 13px; %s">%s</span>'
                                 % (r(v, 'ctl') if v == 'A' else '10px', sel(v) if i == chosen else ('background: %s; color: %s;' % (CHIP, INK) if v == 'A' else idle(v)), l)
                                 for i, l in enumerate(items))))
    return col(g(['янв', 'фев', 'мар', 'апр', 'май', 'июн', 'июл', 'авг', 'сен', 'окт', 'ноя', 'дек'], 10), g(['I кв', 'II кв', 'III кв', 'IV кв'], 3), g(['2024', '2025', '2026'], 2, 3))


def fab_bar(v):
    if v == 'A':
        f = flexrow(fab_round().replace('border-radius: 18px', 'border-radius: 50%'), fab_round('check').replace('border-radius: 18px', 'border-radius: 50%'), cap('круг 56, без текста'))
        bar = action_bar([('copy', 'Дублировать'), ('trash', 'Удалить'), ('close', '3')])
    else:
        f = flexrow(btn(v, 'Операция', icon='plus').replace('height: 48px', 'height: 56px').replace('border-radius: 14px', 'border-radius: 16px'), cap('плитка 56 с подписью'))
        cells = ''.join('<span style="flex: 1; display: flex; flex-direction: column; align-items: center; gap: 4px; font-size: 12px; color: %s;">%s%s</span>'
                        % (PINK if ic == 'trash' else INK, svg(ic, PINK if ic == 'trash' else INK, 22), t) for ic, t in [('copy', 'Дублировать'), ('trash', 'Удалить'), ('close', 'Снять')])
        bar = '<div style="display: flex; padding: 12px 8px; border-radius: 16px 16px 0 0; background: %s; border-top: 1px solid %s;">%s</div>' % (KEY, OUTLINE, cells)
    return col(f, bar, cap('панель выбора — ' + ('плавающая капсула' if v == 'A' else 'прижата к низу, иконка над подписью')))


CHOICE = [
    ('Сегменты', segments, 'CoShellSegmented(labels, selected, tone = Choice | View, enabled)'),
    ('Пилюли выбора', pills, 'CoShellChoicePill(text, selected)'),
    ('Отметки', marks, 'CoShellCheckMark(state) · CoShellRadioMark(selected)'),
    ('Плитки формы', tiles, 'CoShellCheckTile · CoShellDetailTile(icon, label, value, empty)'),
    ('Строка-переключатель', switch_lines, 'CoShellSwitchLine(title, subtitle, checked)'),
    ('Раскрытие', expands, 'CoShellExpandIcon(expanded)'),
    ('Сетка дней', days, 'CoShellDayGrid(selected, days, markedDay)'),
    ('Сетка подписей', labels, 'CoShellLabelGrid(labels, selected, columns)'),
    ('Кнопка «+» и панель действий', fab_bar, 'CoShellFab · CoShellActionBar(visible)'),
]
