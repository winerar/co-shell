"""«Компоненты» and «История решений» boards — the catalog is drawn by the same bricks the screens use.

Each card: live bricks, then «brick() → Compose name». New brick in mockup_bricks.py / mockup_parts.py → add it here.
"""
from mockup_parts import *  # noqa: F401,F403
from mockup_logic import per, proto
import mockup_screens_main as m
import mockup_theme

W, H = 1280, 1960


def frame(html, h, extra=''):
    """Room for bricks that are positioned on the screen (bar, FAB, snackbar, dialog)."""
    return '<div style="position: relative; height: %dpx; overflow: hidden; border-radius: 16px; background: %s; %s">%s</div>' % (h, BG, extra, html)


def section(title, demo, code, span=1):
    return ('<div style="grid-column: span %d; padding: 18px; border-radius: 20px; background: %s; border: 1px solid %s; display: flex; flex-direction: column; gap: 12px;">'
            '<div style="font-size: 17px; font-weight: 700;">%s</div>%s<div style="margin-top: auto; %s font-size: 11px; line-height: 1.6; color: %s;">%s</div></div>'
            % (span, CARD, LINE, title, demo, MONO, MUTED, code))


def stack(*items, gap=10):
    return '<div style="display: flex; flex-direction: column; gap: %dpx;">%s</div>' % (gap, ''.join(items))


def line(*items):
    return '<div style="display: flex; flex-wrap: wrap; align-items: center; gap: 8px;">%s</div>' % ''.join(items)


TOKENS = [('Фон', BG), ('Поверхность', CARD), ('Панель', BAR), ('Поле', FIELD), ('Линия', LINE2), ('Текст', INK), ('Вторичный', MUTED),
          ('Основной', LIME), ('Акцент', VIOLET), ('Доход', CYAN), ('Расход', '#FF2E63'), ('Тёплый', YELLOW), ('Внимание', WARN), ('Готово', OK)]


def token_key(hexv):
    return 'k_%s_%s' % (hexv[1:].upper(), mockup_theme.classify(hexv, False))


def tokens():
    cells = ''.join('<div style="display: flex; flex-direction: column; gap: 4px;"><span style="height: 44px; border-radius: 12px; background: %s; border: 1px solid %s;"></span>'
                    '<span style="font-size: 12px;">%s</span><span style="%s font-size: 10px; color: %s;">{{tok%d}}</span></div>' % (c, LINE, n, MONO, MUTED, i)
                    for i, (n, c) in enumerate(TOKENS))
    radii = line(*['<span style="display: flex; flex-direction: column; align-items: center; gap: 4px; font-size: 11px; color: %s;"><span style="width: %dpx; height: 44px; '
                   'border: 1px solid %s; border-radius: %s;"></span>%s</span>' % (MUTED, w, LIME, r, t)
                   for r, w, t in [('8px', 44, '8'), ('12px', 44, '12 поля'), ('16px', 52, '16 карточки'), ('28px 28px 0 0', 52, '28 шторки'), ('999px', 72, 'пилюля')]])
    return grid([cells], 1) .replace('repeat(1, minmax(0, 1fr))', 'repeat(7, minmax(0, 1fr))') + radii + note('Отступы 2 · 4 · 8 · 12 · 16 · 24 · 32', 0)


def components():
    tabs = ['home', 'ops', 'acc', 'more']
    bars = frame(per('tab', tabs, lambda t: nav(t)), 100) + frame(nav('home', True), 100)
    bars = bars.replace('<a href="#" aria-label="Обзор"', '<a href="#" onClick="{{go_tab_home}}" aria-label="Обзор"').replace(
        '<a href="#" aria-label="Операции"', '<a href="#" onClick="{{go_tab_ops}}" aria-label="Операции"').replace(
        '<a href="#" aria-label="Счета"', '<a href="#" onClick="{{go_tab_acc}}" aria-label="Счета"').replace('<a href="#" aria-label="Ещё"', '<a href="#" onClick="{{go_tab_more}}" aria-label="Ещё"')
    cards = [
        section('Нижняя панель', grid([bars], 1).replace('repeat(1, minmax(0, 1fr))', 'repeat(2, minmax(0, 1fr))'),
                'nav(active, dot_more) → CoShellBottomBar · CoShellTab — капсула 64 dp, пилюля 44 dp, «+» 52 dp (нажимайте вкладки) · '
                'tab_screen(title, body, active) / inner_screen(title, body) / top_bar(title, lead) → TabTopBar · ScreenTopBar', 3),
        section('Кнопки', stack(button('Сохранить', mt=0), button('Отмена', 'outline', None, 0), button('Удалить', 'danger', 'trash', 0), button('Синхронизировать', 'violet', 'refresh', 0),
                                line(icon_button('search', 'Поиск', INK, True), icon_button('back', 'Назад'), button('Все →', 'text', None, 0).replace('width: 100%', 'width: auto; padding: 0 12px')),
                                dashed('Добавить', 0), frame(fab('Добавить'), 90, 'background: transparent;')),
                'button(text, kind) → CoShellButton · CoShellOutlinedButton · CoShellTextButton · icon_button → CoShellIconButton · fab → CoShellExtendedFab · dashed'),
        section('Выбор', stack(line(chip('Все', True), chip('Расходы'), chip('Еда', dot=LIME), chip('Счёт ▾'), chip('＋ В папку', dashed=True)),
                               per('seg', ['a', 'b', 'c'], lambda v: segmented(['Расход', 'Доход', 'Перевод'], {'a': 'Расход', 'b': 'Доход', 'c': 'Перевод'}[v], 'lime', ['go_seg_a', 'go_seg_b', 'go_seg_c'])),
                               segmented(['По типу', 'По валюте', 'Чьи'], 'По типу'), line(switch(True), switch(False)),
                               line(pill('Основная'), pill('Идея', 'violet'), pill('Ошибка', 'pink'), pill('Внимание', 'warn'), pill('Архив', 'muted'))),
                'chip(text, on, icon, dot, dashed) → CoShellFilterChip · DateChip · segmented(options, selected, tone, taps) → SingleChoiceSegmentedButtonRow · switch → CoShellSwitch · CoShellSwitchRow · pill'),
        section('Поля', stack(field('Название', 'Галисия · карта'), field('Сумма', '0', error='Введите сумму больше нуля', mono=True), field('Код валюты', 'EUR', hint='Три буквы ISO', placeholder=True),
                              search('Поиск: заметка, сумма, категория'), textarea('Что случилось и где?', 0)),
                'field(label, value, hint, error, mono) → CoShellTextField · CoShellAmountField · search → CoShellSearchField · textarea'),
        section('Строки и карточки', stack(group([row('Валюты отчётов', 'Основная: USD', tile_icon('rate'), chevron()), row('Учитывать в общем балансе', 'для долга — выключить', '', switch(True))]),
                                           card(m.tx('Супермаркет', 'Galicia · Продукты', '−42 300', 'ARS', 'e', 'cart', LIME) + m.tx('Ameria → Galicia', 'Перевод', '500', 'USD', 't', 'transfer', VIOLET, '512 000 ARS')),
                                           group([acc_row(acc('Galicia'))]), group_head('Карты и банки', 6, '3 176')),
                'group(rows) / row(...) → CoShellRowGroup · CoShellListRow · tx → CoShellListRow + CategoryBadge · acc_row → CoShellListRow + AccountBadge · money · group_head → CoShellExpandable · label → CoShellSectionLabel · card → CoShellCard'),
        section('Плитки и герои', stack(m.balance_hero(), m.tiles(m.tile('Траты за ноябрь', '1 240,50', 'USD'), m.tile('Внимание', '2', 'нет курса VES', WARN, True)),
                                        grid([nav_tile('folder', 'Категории', '34 · 6 папок', LIME, LIMEBG), nav_tile('rate', 'Курсы', 'сегодня', CYAN, CYANBG)], 2),
                                        big_total('Всего в общем балансе', '9 640', 'USD')),
                'balance_hero · tile → CoShellTile · nav_tile → CoShellTile · big_total → RollingAmountText'),
        section('Уведомления', stack(message('Копия сохранена.', 'ok'), message('Не удалось загрузить курсы.', 'error'), message('Курсы ещё не загружались.', 'warn'),
                                     message('Сначала сохраним копию.', 'info', 'Перед стиранием'), frame(snackbar('Операция удалена', bottom=12) + toast('Сохранено', 76), 136)),
                'message(text, kind, title) → CoShellMessage · SyncBanner · snackbar → CoShellSnackbarHost · toast / pill → CoShellNoticePill · UpdatePill'),
        section('Диалоги и шторки', stack(frame(scrim() + dialog('Удалить счёт?', 'Счёт без операций будет удалён безвозвратно.', [('Отмена', 'text'), ('Удалить', 'danger')]), 250),
                                          frame(scrim() + sheet('<div style="padding: 14px 16px;">%s</div>' % segmented(['Расход', 'Доход', 'Перевод'], 'Расход', 'lime'), 60), 160)),
                'dialog(title, text, actions, body) → CoShellDialog · CoShellAlertDialog · sheet(inner, top) + scrim → CoShellSheet'),
        section('Клавиатура и сумма', stack(m.big_amount('4 500', 'ARS'), m.keypad(), m.cat_grid('Продукты')),
                'keypad → CoShellKeypad · big_amount · cat_grid · acc_chip — части шторки (transactions/edit/)'),
        section('Значки и прогресс', stack(line(badge('GA', '#F26E22', '#1A0A00'), dot_icon('cart', LIME), tile_icon('rate', CYAN, CYANBG), swatch(VIOLET, True), swatch('#FF8A5C', new=True)),
                                           grid([icon_cell('cart', True), icon_cell('cafe'), icon_cell('taxi'), icon_cell('house')], 4),
                                           line(ring(0.45, '<span style="%s font-size: 20px; font-weight: 700;">45 %%</span>' % MONO, LIME, 110, 8), ring(0.72, '<b>2</b>', YELLOW, 110, 8)),
                                           progress(0.62, 0), empty('wallet', 'Пустое состояние', 'Добавить').replace('padding: 96px 28px 0;', 'padding: 8px 28px 0;')),
                'badge → AccountBadge · dot_icon → CategoryBadge · tile_icon → IconBadge · swatch → CoShellPaletteSwatch · icon_cell · ring · progress → CoShellLinearProgress · empty · hero — пустые экраны'),
        section('Токены', tokens(), 'PaletteSpecs · Spacing · Radii — подписи показывают цвет текущей темы', 2),
    ]
    body = ('<div style="padding: 40px;"><h1 style="margin: 0; font-size: 34px;">Компоненты · {{coPal}}, {{coMode}}</h1>'
            '<p style="margin: 8px 0 24px; font-size: 15px; color: %s;">Строительные блоки макетов — те же функции, из которых собраны все доски '
            '(<code>scripts/agent/mockup_bricks.py</code>, <code>mockup_parts.py</code>). Справа от стрелки — имя в коде (<code>core:designsystem</code>).</p>'
            '<div style="display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); gap: 16px; align-items: start;">%s</div></div>' % (MUTED, ''.join(cards)))
    extra = ', '.join("tok%d: __co('%s')" % (i, token_key(c)) for i, (_, c) in enumerate(TOKENS))
    html = proto('Компоненты', body, extra, height=H, tab='home', seg='a')
    return html.replace('width: 390px; height: %dpx' % H, 'width: %dpx; height: %dpx' % (W, H)).replace('"width":390,"height":%d' % H, '"width":%d,"height":%d' % (W, H))


PAGES = [('https://claude.ai/artifact/WqocoPvC6g6mLRt31WvcCW', '9 ноября · рефакторинг', 'Новый облик co-shell', 'Пружины вместо длительностей, шторка без рывков, палитры A / Кислота / B / C, вёрстка «Обзора» плитками.'),
         ('https://claude.ai/artifact/DHcu66PAJq6rZwJL7uhhbo', '9 ноября · отзывы #62–#66', 'Выбор дизайна', 'Светлая тема Б, раскрытие экранов из нажатого, шторка из «+», плавные размеры, кнопки в цвет «+».'),
         ('https://claude.ai/artifact/VYVhJ7roxaTLXpaBXhRFYh', '9 ноября · #66', 'Иконка', 'Кошелёк-ракушка: тонкая линия на графите, защёлка из двух шариков. Выбран вариант В.')]


def history():
    cards = ''.join('<a href="%s" target="_blank" style="text-decoration: none; color: inherit;">%s</a>'
                    % (u, card('<div style="font-size: 12px; font-weight: 600; color: %s;">%s</div><div style="margin-top: 6px; font-size: 20px; font-weight: 700;">%s</div>'
                               '<div style="margin-top: 8px; font-size: 14px; line-height: 1.45; color: %s;">%s</div>' % (LIME, d, t, INK2, s), pad='18px', radius=20, extra='height: 100%; box-sizing: border-box;'))
                    for u, d, t, s in PAGES)
    body = ('<div style="padding: 40px;"><h1 style="margin: 0; font-size: 34px;">История решений</h1>'
            '<p style="margin: 8px 0 24px; font-size: 15px; color: %s;">Ранние страницы, на которых выбирали облик. Решённое уже перенесено в экраны и компоненты этого холста.</p>%s'
            '<div style="margin-top: 16px;">%s</div></div>'
            % (MUTED, grid([cards], 1).replace('repeat(1, minmax(0, 1fr)); gap: 8px;">', 'repeat(3, minmax(0, 1fr)); gap: 16px;">'),
               card('<div style="font-size: 14px; line-height: 1.5;">В репозитории: <b>docs/design/README.md</b> — оглавление холста и соответствие макетов коду; '
                    'генераторы досок — <b>scripts/agent/mockup_*.py</b>; правила дизайн-системы — <b>docs/agents/design-system.md</b>.</div>', pad='16px')))
    html = board('История решений', body, height=640)
    return html.replace('width: 390px; height: 640px', 'width: 1040px; height: 640px').replace('"width":390,"height":640', '"width":1040,"height":640')


BOARDS = [('Components', components), ('History', history)]
