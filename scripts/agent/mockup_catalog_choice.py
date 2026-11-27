"""«Компоненты v3» — existing choice components (#367, part 1 of 3), each as a function of the rule set v."""
from mockup_catalog_kit import *  # noqa: F401,F403
import mockup_screens_fb_qr526 as qr526  # noqa: E402


def seg(v, labels, chosen, disabled=False):
    """CoShellSegmented: 36 cells on a 44 track; chosen like everything — also «how a list is shown» («Единый стиль», Style1)."""
    on = sel(v)
    cells = ''.join('<span style="height: 36px; border-radius: %s; display: flex; align-items: center; justify-content: center; font-size: 14px; min-width: 0; %s">'
                    '<span style="overflow: hidden; white-space: nowrap; text-overflow: ellipsis; padding: 0 4px;">%s</span></span>'
                    % ('999px' if v == 'A' else '9px', on if i == chosen else 'color: %s;' % INK2, l) for i, l in enumerate(labels))
    return ('<div style="width: 100%%; box-sizing: border-box; display: grid; grid-template-columns: repeat(%d, minmax(0, 1fr)); gap: 2px; border-radius: %s; %s %s">%s</div>'
            % (len(labels), r(v, 'ctl'), track(v), 'opacity: 0.5;' if disabled else '', cells))


def segments(v):
    return col(labeled(seg(v, ['Расход', 'Доход', 'Перевод'], 0), 'что выбрать — вид операции'), labeled(seg(v, ['По типу', 'По валюте', 'Чьи'], 1), 'как показать — тоже неон'),
               labeled(seg(v, ['Категория', 'Папка'], 0, disabled=True), 'заблокировано — 50 %'), labeled(seg(v, ['Ежедневно', 'Еженедельно', 'Ежемесячно', 'Никогда'], 2), 'длинные — «…»'),
               labeled(seg(v, [svg('out', ONLIME, 20), svg('in', INK2, 20), svg('swaph', INK2, 20)], 0), 'слова не влезли — значки, подпись для TalkBack'))


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
        f = flexrow(fab_round().replace('border-radius: 18px', 'border-radius: 50%'), fab_round('check').replace('border-radius: 18px', 'border-radius: 50%'), cap('круг 48, без текста'))
        bar = action_bar([('copy', 'Дублировать'), ('trash', 'Удалить'), ('close', '3')])
    else:
        f = flexrow(btn(v, 'Операция', icon='plus').replace('height: 48px', 'height: 56px').replace('border-radius: 14px', 'border-radius: 16px'), cap('плитка 56 с подписью'))
        cells = ''.join('<span style="flex: 1; display: flex; flex-direction: column; align-items: center; gap: 4px; font-size: 12px; color: %s;">%s%s</span>'
                        % (PINK if ic == 'trash' else INK, svg(ic, PINK if ic == 'trash' else INK, 22), t) for ic, t in [('copy', 'Дублировать'), ('trash', 'Удалить'), ('close', 'Снять')])
        bar = '<div style="display: flex; padding: 12px 8px; border-radius: 28px 28px 0 0; background: %s; border-top: 1px solid %s;">%s</div>' % (KEY, OUTLINE, cells)
    return col(f, bar, cap('панель выбора — ' + ('плавающая капсула' if v == 'A' else 'прижата к низу, иконка над подписью')))


def account_ribbon(v):
    """CoShellAccountRibbon: one row of account badges (44), the chosen one in a theme-coloured ring, «Все ›» at the end."""
    accs = [('GA', '#F26E22', '#1A0A00'), ('MP', '#00B1EA', '#001A24'), ('Tn', '#26A17B', '#001A12'), ('Сб', '#21A038', '#001A06'), ('Wi', '#9FE870', '#0A1A00')]
    cells = ''.join('<span style="display: flex; border-radius: 14px; %s">%s</span>'
                    % ('box-shadow: 0 0 0 2px %s, 0 0 0 4px %s;' % (CARD, INK) if i == 0 else '', badge(a[0], a[1], a[2], 44, 12)) for i, a in enumerate(accs))
    more = '<span style="height: 44px; padding: 0 14px; border-radius: 999px; display: inline-flex; align-items: center; font-size: 14px; background: %s; color: %s;">Все ›</span>' % (CHIP, INK2)
    return col(flexrow(cells, more, gap=10, wrap=False), cap('последний использованный выбран; «Все ›» — сетка счетов; обводка выбранного — цвет темы, не чёрный/белый'))


def action_fan(v):
    """CoShellActionFan (#623, poll «А», board `EwHoldA`): hold «+» — round buttons open on an arc over it; lead the finger to one and let go."""
    def item(cx, cy, icon, label, on=False):
        circle = ('<span style="position: absolute; left: %dpx; top: %dpx; width: 56px; height: 56px; border-radius: 50%%; display: flex; align-items: center; justify-content: center; %s">%s</span>'
                  % (cx - 28, cy - 28, ('background: %s; box-shadow: inset 0 0 0 2px %s;' % (LIME, ONLIME)) if on else ('background: %s; border: 1px solid %s;' % (CHIP, OUTLINE)),
                     (qr526.QR % (ONLIME if on else INK)) if icon == 'qr' else svg(icon, ONLIME if on else INK, 24, 1.8)))
        cap_ = ('<span style="position: absolute; left: %dpx; top: %dpx; width: 88px; text-align: center; font-size: 12px;"><span style="padding: 2px 8px; border-radius: 999px; background: %s; color: %s;">%s</span></span>'
                % (cx - 44, cy + 32, CHIP, INK, label))
        return circle + cap_
    plus = ('<span style="position: absolute; left: 126px; top: 126px; width: 48px; height: 48px; border-radius: 50%%; background: %s; color: %s; display: flex; align-items: center; justify-content: center;">%s</span>'
            % (LIME, ONLIME, svg('plus', ONLIME, 24, 2.2)))
    arc = ''.join([item(73, 58, 'qr', 'QR чека'), item(150, 30, 'note', 'Заметка', True), item(227, 58, 'list', 'Шаблон')])
    return col('<div style="position: relative; width: 300px; height: 190px;">%s%s</div>' % (arc, plus),
               cap('удержали «+» — три кнопки веером; не отпуская, ведут палец к нужной и отпускают; мимо — закроется; короткое нажатие — новая операция'))


def counter(v):
    """CoShellCounter (#777, poll 2026-11-27 «А»): «− value +», round 48 buttons on the track; holding a button keeps stepping."""
    def one(value, sub, mono, dim_minus=False):
        btn = ('<span style="width: 48px; height: 48px; border-radius: 50%%; background: %s; display: inline-flex; align-items: center; justify-content: center; flex: none; %s">%s</span>')
        font = MONO + ' font-size: 22px;' if mono else 'font-size: 22px; font-weight: 600;'
        return ('<div style="display: flex; align-items: center; gap: 12px; width: 100%%;">%s<div style="flex-grow: 1; text-align: center;"><div style="%s">%s</div>'
                '<div style="font-size: 12px; color: %s;">%s</div></div>%s</div>' % (
                    btn % (CHIP, 'opacity: 0.38;' if dim_minus else '', svg('minus', INK, 22)), font, value, MUTED, sub, btn % (CHIP, '', svg('plus', INK, 22))))
    return col(labeled(one('7:30', 'шаг 15 минут', True), 'время — шаг 15 минут, через полночь по кругу'),
               labeled(one('В тот же день', 'до платежа', False, dim_minus=True), 'на краю — кнопка 38 %'),
               cap('нажатие — шаг, удержание — листает быстрее; шторка «Другое…» в «Уведомлениях»'))


CHOICE = [
    ('Веер над «+»', action_fan, 'CoShellActionFan(actions, enabled) { «+» } · FanAction(icon, label, onSelect)'),
    ('Лента счетов', account_ribbon, 'CoShellAccountRibbon(accounts, selectedId, onSelect, onAll)'),
    ('Сегменты', segments, 'CoShellSegmented(labels, selected, enabled, icons) · CoShellStepper(label, onEarlier, onLater) — «‹ Октябрь 2026 ›»'),
    ('Пилюли выбора', pills, 'CoShellChoicePill(text, selected)'),
    ('Счётчик', counter, 'CoShellCounter(value, onStep, minusLabel, plusLabel, caption, canMinus, canPlus, mono)'),
    ('Отметки', marks, 'CoShellCheckMark(state) · CoShellRadioMark(selected)'),
    ('Плитки формы', tiles, 'CoShellCheckTile · CoShellDetailTile(icon, label, value, empty)'),
    ('Строка-переключатель', switch_lines, 'CoShellSwitchLine(title, subtitle, checked)'),
    ('Раскрытие', expands, 'CoShellExpandIcon(expanded)'),
    ('Сетка дней', days, 'CoShellDayGrid(selected, days, markedDay)'),
    ('Сетка подписей', labels, 'CoShellLabelGrid(labels, selected, columns)'),
    ('Кнопка «+» и панель действий', fab_bar, 'CoShellFab · CoShellActionBar(visible)'),
]
