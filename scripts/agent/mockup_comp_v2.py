"""«Компоненты v2» (#367): every component of the app in its states — proposal for the owner's poll.

Five wide boards on the «Компоненты» canvas page, under the approved board: new components (were Material or
home-made in screens), the 37 from `ApprovedComponentsTest.pending` in three groups, and contrast variants (#385).
After «ок» the approved cards move to mockup_catalog.py (the test reads that file) and `pending` shrinks.
"""
from mockup_comp_v2_parts import *  # noqa: F401,F403
from mockup_catalog import section
from mockup_bricks import board

W = 1280
PROPOSED = '<span style="color: %s;">новый</span> · ' % CYAN


def wide(title, intro, cards, h, cols=3):
    body = ('<div style="padding: 40px;"><h1 style="margin: 0; font-size: 34px;">%s</h1><p style="margin: 8px 0 24px; max-width: 1000px; font-size: 15px; line-height: 1.45; color: %s;">%s</p>'
            '<div style="display: grid; grid-template-columns: repeat(%d, minmax(0, 1fr)); gap: 16px; align-items: start;">%s</div></div>' % (title, MUTED, intro, cols, ''.join(cards)))
    return board(title, body, height=h).replace('width: 390px; height: %dpx' % h, 'width: %dpx; height: %dpx' % (W, h)).replace(
        '"width":390,"height":%d' % h, '"width":%d,"height":%d' % (W, h))


def new_components():
    cards = [
        section('Шапка экрана', col(top_bar_v2('tab', 'Операции', ['search', 'more']), top_bar_v2('screen', 'Резервная копия', ['more']),
                                    top_bar_v2('screen', 'Очень длинное название экрана, которое не влезает', [], True), top_bar_v2('selection', '3 выбрано', ['copy', 'trash'], True),
                                    cap('вкладка · внутренний · длинный заголовок — «…» · выбор; при прокрутке — фон панели')),
                PROPOSED + 'TopAppBar ×3 → CoShellTopBar(kind = Tab | Screen | Selection, scrolled)'),
        section('Выпадающее меню', flexrow(menu_v2([('calendar', 'По дате', 'on'), ('chart', 'По сумме', ''), ('copy', 'Дублировать', ''), ('lock', 'Недоступно', 'off'),
                                                    ('trash', 'Удалить', 'danger')]), cap('выбранный пункт — галочка<br>опасный — красный<br>недоступный — 38 %'), align='flex-start'),
                PROPOSED + 'DropdownMenu → CoShellDropdownMenu · CoShellMenuItem(checked, destructive, enabled)'),
        section('Выбор даты', col(calendar(sel=(15,), today=12), cap('сегодня — кольцо, выбранный — заливка; листается свайпом по месяцам')),
                PROPOSED + 'DatePicker → CoShellDatePicker (в CoShellModalSheet, кнопка «Готово»)'),
        section('Выбор периода', col(calendar(sel=(10, 21), band=(10, 21), today=12), cap('концы — заливка, между — полоса; второй тап — конец')),
                PROPOSED + 'DateRangePicker → CoShellDateRangePicker'),
        section('Разделитель и загрузка', col(card(row('Курсы валют', 'сегодня') + divider(14) + row('Валюты отчётов', 'USD, ARS') + divider(0) + row('Резервная копия', '2 дня назад')),
                                              flexrow(labeled(spinner(24), '24 dp — в строке'), labeled(spinner(48, LIME, 4), '48 dp — экран'),
                                                      labeled(button('Синхронизация…', 'outline', None, 0).replace('width: 100%', 'width: 200px').replace('>Синх', '>%sСинх' % spinner(18, INK, 2)), 'в кнопке'))),
                PROPOSED + 'HorizontalDivider → CoShellDivider(inset) · CircularProgressIndicator → CoShellLoading(size)'),
        section('Аватар участника', col(flexrow(labeled(avatar('А', MEMBERS[0][1], 56, True), '56 · вы'), labeled(avatar('М', MEMBERS[1][1], 40, dot=OK), '40 · в сети'),
                                                labeled(avatar('Л', MEMBERS[2][1], 24), '24 · в строке'), labeled(avatar_stack(3), 'семья'), gap=18),
                                        hero_card(flexrow(avatar_stack(2, 40), two_lines('Семья · 2 участника', 'синхронизировано 2 мин назад', HERO_HINT), gap=12, wrap=False))),
                PROPOSED + 'самодельный круг → CoShellAvatar(size, you, online) · CoShellAvatarStack · герой «Семьи» = CoShellCard(accent) + стопка'),
        section('Точка, статус, «новое»', col(flexrow(labeled(dot(LIME), 'новое'), labeled(dot(OK), 'готово'), labeled(dot(WARN), 'внимание'), labeled(dot(PINK), 'ошибка'), gap=22),
                                              flexrow(status_chip('Отправлено', 'new'), status_chip('В работе', 'work'), status_chip('Готово', 'done'), status_chip('Отклонено', 'no'))),
                PROPOSED + 'точки и плашки в экранах → CoShellDot(tone) · CoShellStatusChip(tone) (статус отзыва)'),
        section('Выбор иконки и цвета', col(icon_strip(['cart', 'cafe', 'taxi', 'house', 'gift', 'school'], 'taxi'),
                                            color_strip(['#F26E22', '#00B1EA', '#EC0000', '#91E668', '#B266FF', '#FFD43B'], '#00B1EA'),
                                            cap('выбранное — рамка; полоса листается, последний — «Все →»')),
                PROPOSED + 'полоски в редакторах → CoShellIconStrip · CoShellColorStrip'),
        section('Превью карточки счёта', flexrow(account_card('Galicia', 'GA', '#F26E22', '#1A0A00', 'Карта', '1 250 000', 'ARS'),
                                                 account_card('Очень длинное имя счёта', 'ВК', '#FFD43B', '#1A1400', 'Вклад', '12 345 678,90', 'USDT')),
                PROPOSED + 'самодельная плашка → CoShellAccountCard (цвет банка, чернила по контрасту)'),
        section('Оттенки кнопок и чипов', col(flexrow(*[button(t, k, None, 0).replace('width: 100%', 'width: auto; padding: 0 18px') for t, k in
                                                        [('Сохранить', 'primary'), ('Отмена', 'outline'), ('Удалить', 'danger'), ('Синхр.', 'violet')]]),
                                              flexrow(chip('Все', True), chip('Еда'), chip('Счёт ▾'), button('Нельзя', 'primary', None, 0, True).replace('width: 100%', 'width: auto; padding: 0 18px'))),
                'ButtonDefaults · FilterChipDefaults в экранах → параметр tone у CoShellButton / CoShellFilterChip (Primary, Outline, Danger, Violet; выключено — 40 %)'),
    ]
    return wide('Компоненты v2 · новые', 'Чего нет в наборе: экраны рисуют это Material-виджетами или сами (#367, 128 мест). Предлагаю 10 компонентов — после «ок» '
                'они появятся в <code>core:designsystem</code>, а экраны перейдут на них. Справа от стрелки — имя в коде.', cards, 1880)


def choice_components():
    cards = [
        section('Сегменты', col(labeled(seg_v2(['Расход', 'Доход', 'Перевод'], 0), 'Choice — вид операции, тема'), labeled(seg_v2(['По типу', 'По валюте', 'Чьи'], 1, 'view'), 'View — как показать список'),
                                labeled(seg_v2(['Категория', 'Папка'], 0, disabled=True), 'заблокировано — 50 %'), labeled(seg_v2(['Ежедневно', 'Еженедельно', 'Ежемесячно', 'Никогда'], 2), '4 длинных — «…»')),
                'CoShellSegmented(labels, selected, tone = Choice | View, enabled, swipe)'),
        section('Пилюли выбора', col(flexrow(choice_pill('Карта', True), choice_pill('Наличные'), choice_pill('Кошелёк'), choice_pill('Вклад'), choice_pill('Долг')), cap('для 3–5 вариантов в карточке формы; выбранная — заливка, остальные — контур')),
                'CoShellChoicePill(text, selected)'),
        section('Отметки', col(flexrow(labeled(check_mark('off'), 'нет'), labeled(check_mark('on'), 'да'), labeled(check_mark('some'), 'часть папки'),
                                       labeled(radio_mark(False), 'не выбран'), labeled(radio_mark(True), 'выбран'), gap=20), cap('рисуют только знак — нажимается вся строка')),
                'CoShellCheckMark(state) · CoShellRadioMark(selected)'),
        section('Плитки формы', col(flexrow(check_tile(False, 'Это возврат', 'деньги вернули'), check_tile(True, 'Это возврат', 'деньги вернули')),
                                    flexrow(detail_tile('calendar', 'Дата', 'Сегодня, 15 ноя'), detail_tile('note', 'Заметка', 'Без заметки', True))),
                'CoShellCheckTile(checked, title, subtitle) · CoShellDetailTile(icon, label, value, empty)'),
        section('Строка-переключатель', col(switch_line('Учитывать в балансе', 'для долга — выключить', True), switch_line('Учитывать в балансе', 'для долга — выключить', False),
                                            switch_line('Очень длинное название настройки, которое не помещается', 'и такая же длинная подпись под ним', True)),
                'CoShellSwitchLine(title, subtitle, checked) — тоньше CoShellSwitchRow'),
        section('Стрелка раскрытия', flexrow(labeled(expand_icon(False), 'закрыто'), labeled(expand_icon(True), 'открыто'), cap('поворот пружиной'), gap=24),
                'CoShellExpandIcon(expanded)'),
        section('Сетка дней', col(day_grid(10, 1), cap('начало месяца: выбранный — заливка, по умолчанию — точка')), 'CoShellDayGrid(selected, days, markedDay)'),
        section('Сетка подписей', col(label_grid(['янв', 'фев', 'мар', 'апр', 'май', 'июн', 'июл', 'авг', 'сен', 'окт', 'ноя', 'дек'], 10), label_grid(['I кв', 'II кв', 'III кв', 'IV кв'], 3),
                                      label_grid(['2024', '2025', '2026'], 2, 3)), 'CoShellLabelGrid(labels, selected, columns)'),
        section('Кнопка «+» и панель действий', col(flexrow(fab_round(), fab_round('check'), cap('FAB — круглый, без текста')),
                                                    action_bar([('copy', 'Дублировать'), ('trash', 'Удалить'), ('close', '3')])),
                'CoShellFab(onClick) · CoShellActionBar(visible) — капсула панели с действиями выбора'),
    ]
    return wide('Компоненты v2 · выбор', 'Есть в коде, нет на доске (часть 1 из 3): всё, чем выбирают. Нарисовано по коду <code>core:designsystem</code>, все состояния.', cards, 1280)


def rows_components():
    cards = [
        section('Строка меню', col(group([menu_row('folder', LIME, 'Категории', '24 категории · 6 папок'), menu_row('rate', CYAN, 'Курсы валют', 'сегодня'),
                                         menu_row('gift', VIOLET, 'Что нового', '', dot(LIME)), menu_row('user', WARN, 'Очень длинное название раздела меню', 'и длинная подпись')]),
                                   flexrow(menu_icon('folder', LIME), menu_icon('rate', CYAN), menu_icon('gift', VIOLET), menu_icon('alert', WARN), menu_icon('trash', PINK))),
                'CoShellMenuRow(icon, color, title, subtitle, trailing) · MenuIcon(icon, color) — квадрат = раздел, круг = категория'),
        section('Карточка формы', col(form_section('СУММА · СПИСАНО', amount_text('4 500', 'ARS', 26, PINK), section_link('+ Чек')),
                                      form_section('КАТЕГОРИЯ · ЧАСТЫЕ', flexrow(choice_pill('Продукты', True), choice_pill('Кафе'), choice_pill('Такси')), section_link('Все'))),
                'CoShellFormSection(title, trailing) · CoShellSectionLink(text)'),
        section('Блок с подписью и подсказка', col(section_v2('Название', field('', 'Galicia · карта').replace('margin-top: 12px;', '', 1)),
                                                   section_v2('Код валюты', field('', 'EU').replace('margin-top: 12px;', '', 1).replace(LINE2, PINK, 1), 'Нужны три буквы ISO'),
                                                   hint('Подсказка: курс берём на дату операции. Его можно поправить вручную.')),
                'CoShellSection(title, error) · CoShellHint(text)'),
        section('Карточка-вопрос', col(confirm_card('Сохранить копию?', 'Перед стиранием сохраним всё в файл.', 'Сохранить', 'Не сейчас'),
                                       confirm_card('Удалить счёт?', 'Счёт без операций удалится навсегда.', 'Удалить', 'Отмена', True)),
                'CoShellConfirmCard(title, text, confirmLabel, dismissLabel, destructive)'),
        section('Шторка', col(modal_sheet('Валюта', '%s' % group([row('USD', 'Доллар США', '', radio_mark(True), min_h=48), row('ARS', 'Аргентинское песо', '', radio_mark(False), min_h=48)]),
                                          'выбор закрывает шторку'),
                              modal_sheet('Курс вручную', field('1 USD =', '1 482,10 ARS', mono=True).replace('margin-top: 12px;', '', 1), action='Сохранить', action_on=False)),
                'CoShellModalSheet(title, subtitle, actionLabel, actionEnabled) · CoShellModalSheetLayout — то же без окна'),
        section('Шторка ввода и пузырь', col(modal_sheet('Новая операция', seg_v2(['Расход', 'Доход', 'Перевод'], 0), action='Сохранить'),
                                             flexrow(peek_card([('≈ 31,53', 'USD'), ('≈ 12 180', 'AMD'), (None, 'VES')], 'по курсу на 12 ноября'), cap('держите палец на сумме'))),
                'CoShellSheetLayout(title, actionLabel, titleAction) · CoShellPeekBubble(lines, footnote) · PeekCard — пузырь без окна'),
        section('Смена темы', flexrow('<div style="position: relative; width: 200px; height: 120px; border-radius: 16px; background: %s; overflow: hidden;">'
                                      '<div style="position: absolute; right: -60px; top: -60px; width: 200px; height: 200px; border-radius: 50%%; background: #F9FFE9;"></div></div>' % BG,
                                      cap('новая тема раскрывается кругом<br>из нажатой кнопки (motion.md)')),
                'ThemeRevealHost — обёртка приложения, видимой части нет'),
    ]
    return wide('Компоненты v2 · строки и шторки', 'Есть в коде, нет на доске (часть 2 из 3): строки, формы, вопросы, шторки.', cards, 1460)


def data_components():
    tag_row = flexrow(*[tag_chip(n, c, i in (0, 2), i == 0) for i, (n, c) in enumerate(TAGS)], tag_more_chip('Все 34'))
    cards = [
        section('Сумма', col(amount_text('1 240,50', 'USD', 34), amount_text('−42 300', 'ARS', 22, PINK), amount_text('+512 000', 'AMD', 22, CYAN),
                             '<div style="width: 220px; overflow: hidden;">%s</div>' % amount_text('12 345 678 901,99', 'USDT', 18), cap('длинная — уменьшается до minFontSize, не обрезается')),
                'AmountText(text, style, color, minFontSize)'),
        section('Изменение', flexrow(change_pill('↑ 45 %', PINK), change_pill('↓ 33 %', OK), change_pill('новое', CYAN), change_pill('0 %', MUTED)),
                'CoShellChangePill(text, color)'),
        section('Шкала бюджета', col(labeled(bullet_v2(0.4, 'ok', 0.5), 'в плане · отметка «сегодня»'), labeled(bullet_v2(0.7, 'ahead', 0.5), 'быстрее плана'),
                                     labeled(bullet_v2(1.2, 'over'), 'перерасход'), labeled(bullet_v2(0, 'idle', 0.5), 'ещё ничего')),
                'CoShellBullet(fraction, tone = Ok | Ahead | Over | Idle, mark)'),
        section('Мини-столбики', col(labeled(mini_bars([3, 5, 2, 8, 4, 0, 6, 9, 3, 5, 7, 2, 4, 6, 0, 0, 0, 0, 0, 0], 14, 13), 'прошедшие дни · сегодня ярче · впереди точки'),
                                     labeled(mini_bars([4, 7, 5] + [0] * 17, 3, 2, CYAN), 'начало месяца')), 'CoShellMiniBars(values, color, elapsed, highlight)'),
        section('Строка-столбик', col(bar_row('cart', LIME, 'Продукты', '290', '38 %', 0.8, 0.65, change_pill('↑ 45 %', PINK)), bar_row('cafe', '#FF8A5C', 'Кафе', '120', '16 %', 0.35, 0.4),
                                      bar_row('taxi', '#FFD43B', 'Такси ночью из аэропорта', '8', '1 %', 0.03)), 'CoShellBarRow(label, value, share, fraction, ghost, badge)'),
        section('Столбцы по месяцам', columns_v2([980, 1210, 860, 1350, 1100, 1240], ['июн', 'июл', 'авг', 'сен', 'окт', 'ноя'], 5, 1120),
                'CoShellColumns(columns, selected, average) — средняя пунктиром'),
        section('Кольцо', flexrow(donut([(0.38, LIME), (0.22, CYAN), (0.16, VIOLET), (0.12, '#FF8A5C'), (0.12, MUTED2)], amount_text('760', 'USD', 18)),
                                  donut([(1, LINE2)], cap('нет трат'))), 'CoShellDonut(slices, description, content)'),
        section('Теги — выбор', col(tag_row, cap('выбран — заливка цветом тега · ✕ — в шапке шторки · длинный — «…»')),
                'CoShellTagChip(name, color, selected, removeLabel) · CoShellTagMoreChip(label)', 2),
        section('Теги — в строке', col(flexrow(tag_pill('Отпуск', TAGS[0][1]), tag_pill('Ремонт кухни и ванной комнаты', TAGS[2][1]), tag_more(2)),
                                       flexrow(*[labeled('<span style="display: inline-block; width: 14px; height: 14px; border-radius: 50%%; background: %s;"></span>' % c, n.split()[0]) for n, c in TAGS], gap=14)),
                'CoShellTagPill(name, color) · CoShellTagMore(count) · CoShellTagDot(color)'),
    ]
    return wide('Компоненты v2 · данные и теги', 'Есть в коде, нет на доске (часть 3 из 3): суммы, графики, теги. Цвета тегов и категорий — из палитры, не из темы.', cards, 1160)


BOARDS = [('CompV2New', new_components), ('CompV2Choice', choice_components), ('CompV2Rows', rows_components), ('CompV2Data', data_components)]
