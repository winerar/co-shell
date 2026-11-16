"""«Компоненты v3» — the 10 new components (#367), each as a function of the rule set v ('A' | 'B')."""
from mockup_catalog_kit import *  # noqa: F401,F403


def icon_btn(v, icon, color=INK):
    return ('<span style="width: 40px; height: 40px; flex: none; border-radius: %s; display: flex; align-items: center; justify-content: center; %s">%s</span>'
            % (r(v, 'icon'), 'background: %s;' % CHIP if v == 'A' else 'box-shadow: inset 0 0 0 1px %s;' % OUTLINE, svg(icon, color, 20)))


def top_bar(v, kind, title, actions=(), scrolled=False):
    lead = {'tab': '', 'screen': icon_btn(v, 'back'), 'selection': icon_btn(v, 'close')}[kind]
    size = 28 if kind == 'tab' else 20
    bg = 'background: %s;' % BAR if scrolled else ''
    t = ('<span style="flex-grow: 1; min-width: 0; font-size: %dpx; font-weight: 700; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; %s">%s</span>'
         % (size, 'text-align: center;' if v == 'B' and kind == 'screen' else '', title))
    acts = ''.join(icon_btn(v, a) for a in actions) or ('<span style="width: 40px;"></span>' if v == 'B' and kind == 'screen' else '')
    rad = '16px' if v == 'A' else '0'
    line = '' if v == 'A' or not scrolled else 'border-bottom: 1px solid %s;' % OUTLINE
    return ('<div style="display: flex; align-items: center; gap: 8px; height: 60px; padding: 0 8px; border-radius: %s; %s%s">%s%s%s</div>'
            % (rad, bg, line, lead, t, acts))


def top_bars(v):
    return col(top_bar(v, 'tab', 'Операции', ['search', 'more']), top_bar(v, 'screen', 'Резервная копия', ['more']),
               top_bar(v, 'screen', 'Очень длинное название экрана', [], True), top_bar(v, 'selection', '3 выбрано', ['copy', 'trash'], True),
               cap('вкладка · внутренний · длинный · выбор; при прокрутке — ' + ('фон-капсула' if v == 'A' else 'фон и линия снизу')))


def menu(v):
    rows = []
    for icon, text, st in [('calendar', 'По дате', 'on'), ('chart', 'По сумме', ''), ('copy', 'Дублировать', ''), ('lock', 'Недоступно', 'off'), ('trash', 'Удалить', 'danger')]:
        c = PINK if st == 'danger' else INK
        on = st == 'on'
        if v == 'A':
            item = ('<div style="display: flex; align-items: center; gap: 12px; height: 44px; padding: 0 14px; margin: 0 6px; border-radius: 999px; %s%s">%s'
                    '<span style="flex-grow: 1; font-size: 15px; color: %s; %s">%s</span>%s</div>'
                    % ('background: %s;' % LIMEBG if on else '', ' opacity: 0.38;' if st == 'off' else '', svg(icon, PINK if st == 'danger' else MUTED, 20),
                       c, 'font-weight: 700;' if on else '', text, svg('check', INK, 18, 2.4) if on else ''))
        else:
            item = ('<div style="display: flex; align-items: center; gap: 12px; height: 44px; padding: 0 14px; %s%s">'
                    '<span style="width: 18px; flex: none;">%s</span><span style="flex-grow: 1; font-size: 15px; color: %s; %s">%s</span>%s</div>'
                    % ('border-top: 1px solid %s;' % LINE2 if st == 'danger' else '', ' opacity: 0.38;' if st == 'off' else '', svg('check', INK, 18, 2.4) if on else '',
                       c, 'font-weight: 700;' if on else '', text, svg(icon, PINK if st == 'danger' else MUTED, 18)))
        rows.append(item)
    return flexrow('<div style="width: 236px; padding: 6px 0; border-radius: %s; background: %s; box-shadow: inset 0 0 0 1px %s, 0 12px 32px rgba(0,0,0,0.4);">%s</div>'
                   % ('22px' if v == 'A' else '12px', KEY, LINE2, ''.join(rows)),
                   cap('выбранный — ' + ('тон + жирный + ✓ справа' if v == 'A' else '✓ слева + жирный') + '<br>опасный — красный' + ('' if v == 'A' else ', отделён линией') + '<br>недоступный — 38 %'),
                   align='flex-start')


def cal(v, sel_days=(15,), band=None, today=12):
    wd = ''.join('<span style="text-align: center; font-size: 11px; color: %s;">%s</span>' % (MUTED, d) for d in ['пн', 'вт', 'ср', 'чт', 'пт', 'сб', 'вс'])
    cells = ['<span></span>'] * 6
    rad = '50%' if v == 'A' else '10px'
    for d in range(1, 31):
        st = 'color: %s; border-radius: %s;' % (INK, rad)
        mid = band and band[0] < d < band[1]
        if mid:
            st = 'background: %s; color: %s; border-radius: 0;' % (VIOLETPILL if v == 'A' else KEY2, VIOLETINK if v == 'A' else INK)
        if d in sel_days:
            st = sel(v) + ' border-radius: %s;' % rad
        elif d == today and not mid:
            st += (' box-shadow: inset 0 0 0 1.5px %s;' % OUTLINE) if v == 'A' else ' text-decoration: underline 2px; text-underline-offset: 4px; font-weight: 700;'
        cells.append('<span style="height: 34px; display: flex; align-items: center; justify-content: center; font-size: 13px; %s">%d</span>' % (st, d))
    head = flexrow('<span style="flex-grow: 1; font-size: 15px; font-weight: 600;">Ноябрь 2026</span>', svg('back', MUTED, 18), svg('chev', MUTED, 18), gap=14, wrap=False)
    return ('<div style="padding: 12px; border-radius: %s; background: %s; display: flex; flex-direction: column; gap: 8px;">%s'
            '<div style="display: grid; grid-template-columns: repeat(7, minmax(0, 1fr)); row-gap: 2px;">%s%s</div></div>' % (r(v, 'card'), KEY, head, wd, ''.join(cells)))


def date_one(v):
    return col(cal(v), cap('выбранный — ' + ('неон + рамка' if v == 'A' else 'тон + рамка') + '; сегодня — ' + ('кольцо' if v == 'A' else 'подчёркнут')))


def date_range(v):
    return col(cal(v, (10, 21), (10, 21)), cap('концы — как выбранный день, между — ' + ('фиолетовая полоса' if v == 'A' else 'серая полоса')))


def divider_loading(v):
    rows = [('Курсы валют', 'сегодня'), ('Валюты отчётов', 'USD, ARS'), ('Резервная копия', '2 дня назад')]
    if v == 'A':
        lst = card(''.join(row(t, s, min_h=48).replace('{{div}}', 'border-top: 1px solid %s; margin-left: 14px; padding-left: 0;' % LINE2 if i else '') for i, (t, s) in enumerate(rows)), radius=24)
        load = flexrow(labeled(spinner(24), 'в строке'), labeled(spinner(44, LIME, 4), 'экран'), labeled(btn(v, '%s Синхронизация' % spinner(18, ONLIME, 2)), 'в кнопке'))
    else:
        lst = col(*[card(row(t, s, min_h=48).replace('{{div}}', ''), radius=12) for t, s in rows], gap=4)
        bar = lambda w: '<div style="width: %s; height: 4px; border-radius: 2px; background: %s; overflow: hidden;"><div style="width: 40%%; height: 100%%; background: %s;"></div></div>' % (w, KEY2, LIME)
        dots = '<span style="display: inline-flex; gap: 4px;">%s</span>' % ''.join(dot(INK2 if i else LIME, 7) for i in range(3))
        load = flexrow(labeled(dots, 'в строке'), labeled(bar('120px'), 'экран — полоса сверху'), labeled(btn(v, '%s Синхронизация' % dots.replace(LIME, ONLIME).replace(INK2, ONLIME)), 'в кнопке'))
    return col(lst, load, cap('разделитель — ' + ('линия с отступом' if v == 'A' else 'нет линий: просвет 4 dp между плитками')))


def ava(v, ini, color, size=40, you=False, online=None):
    rad = '50%' if v == 'A' else '%dpx' % (size * 0.3)
    ring = 'box-shadow: 0 0 0 2px %s, 0 0 0 4px %s;' % (CARD, INK) if you else ''
    d = ('<span style="position: absolute; right: -2px; bottom: -2px; width: %dpx; height: %dpx; border-radius: %s; background: %s; border: 2px solid %s;"></span>'
         % (size // 4 + 2, size // 4 + 2, '50%' if v == 'A' else '3px', online, CARD) if online else '')
    return ('<span style="position: relative; width: %dpx; height: %dpx; flex: none; border-radius: %s; background: %s; color: #0B0F0A; display: flex; align-items: center; '
            'justify-content: center; font-size: %dpx; font-weight: 700; %s">%s%s</span>' % (size, size, rad, color, int(size * 0.4), ring, ini, d))


def avatars(v):
    stack = '<span style="display: inline-flex;">%s</span>' % ''.join(
        '<span style="margin-left: %dpx; border-radius: %s; box-shadow: 0 0 0 2px %s;">%s</span>' % (-8 if i else 0, '50%' if v == 'A' else '10px', CARD, ava(v, ini, c, 32))
        for i, (ini, c) in enumerate(MEMBERS[:3]))
    return col(flexrow(labeled(ava(v, 'А', MEMBERS[0][1], 56, True), '56 · вы'), labeled(ava(v, 'М', MEMBERS[1][1], 40, online=OK), '40 · в сети'),
                       labeled(ava(v, 'Л', MEMBERS[2][1], 24), '24'), labeled(stack, 'семья'), gap=16),
               hero_card(flexrow(stack, two_lines('Семья · 2 участника', 'синхронизировано 2 мин назад', HERO_HINT), gap=12, wrap=False), radius=24 if v == 'A' else 16))


def status(v, text, tone):
    c = {'new': CYAN, 'work': WARN, 'done': OK, 'no': MUTED}[tone]
    if v == 'A':
        return ('<span style="height: 26px; padding: 0 12px 0 9px; border-radius: 999px; background: %s; color: %s; font-size: 12px; font-weight: 600; display: inline-flex; '
                'align-items: center; gap: 6px;">%s%s</span>' % (mix(c, 16), INK, dot(c, 7), text))
    return ('<span style="height: 26px; padding: 0 9px; border-radius: 8px; box-shadow: inset 0 0 0 1px %s; color: %s; font-size: 12px; font-weight: 600; display: inline-flex; '
            'align-items: center; gap: 6px;"><span style="width: 7px; height: 7px; border-radius: 2px; background: %s;"></span>%s</span>' % (OUTLINE, INK, c, text))


def dots_status(v):
    shape = lambda c: dot(c, 9) if v == 'A' else '<span style="display: inline-block; width: 9px; height: 9px; border-radius: 2px; background: %s;"></span>' % c
    return col(flexrow(labeled(shape(LIME), 'новое'), labeled(shape(OK), 'готово'), labeled(shape(WARN), 'внимание'), labeled(shape(PINK), 'ошибка'), gap=20),
               flexrow(status(v, 'Отправлено', 'new'), status(v, 'В работе', 'work'), status(v, 'Готово', 'done'), status(v, 'Отклонено', 'no')),
               cap('подпись — цветом текста, цвет — только в метке (#385)'))


def strips(v):
    """Poll #367 (2026-11-16): А — circles 40 (colours) / 44 (icons) in one row, chosen = ring in the ink + check / neon + ring; the rest — «+N» / «Все 52» outline pill."""
    more = lambda t: '<span style="height: 36px; padding: 0 14px; border-radius: 999px; box-shadow: inset 0 0 0 1px %s; display: inline-flex; align-items: center; font-size: 14px; flex: none;">%s</span>' % (OUTLINE, t)
    icons = ''.join('<span style="width: 44px; height: 44px; flex: none; border-radius: 50%%; display: flex; align-items: center; justify-content: center; %s">%s</span>'
                    % (sel(v) if i == 'cafe' else 'background: %s;' % CHIP, svg(i, ink_on_sel(v) if i == 'cafe' else INK2, 22)) for i in ['cart', 'cafe', 'taxi', 'house', 'gift'])
    colors = ''.join('<span style="width: 40px; height: 40px; flex: none; border-radius: 50%%; background: %s; display: flex; align-items: center; justify-content: center; %s">%s</span>'
                     % (c, 'box-shadow: 0 0 0 2px %s, 0 0 0 4px %s;' % (CARD, INK) if c == '#00B1EA' else '',
                        svg('check', '#0B0F0A', 20, 2.6) if c == '#00B1EA' else '') for c in ['#F26E22', '#00B1EA', '#EC0000', '#91E668', '#B266FF'])
    return col('<div style="display: flex; gap: 8px; align-items: center;">%s%s</div>' % (icons, more('Все 52')),
               '<div style="display: flex; gap: 8px; align-items: center;">%s%s</div>' % (colors, more('+19')),
               cap('CoShellIconStrip · CoShellColorStrip; ячейки — CoShellIconCell, CoShellColorDot, CoShellNoColorDot, банк — CoShellBankDot; сколько кругов — по ширине'))


def acc_card(v, name, ini, color, ink, kind, bal, cur):
    big = 22 if len(bal) < 11 else 17
    if v == 'A':
        return ('<div style="width: 100%%; height: 120px; box-sizing: border-box; padding: 14px 16px; border-radius: 24px; background: %s; color: %s; display: flex; flex-direction: column;">'
                '<div style="display: flex; justify-content: space-between; gap: 8px; font-size: 13px; font-weight: 600;"><span style="min-width: 0; overflow: hidden; white-space: nowrap; text-overflow: ellipsis;">%s</span>'
                '<span>%s</span></div><div style="margin-top: auto; font-size: 12px; opacity: 0.8;">%s</div><div style="%s font-size: %dpx; font-weight: 700; white-space: nowrap;">%s '
                '<span style="font-size: 12px;">%s</span></div></div>' % (color, ink, name, ini, kind, MONO, big, bal, cur))
    return ('<div style="width: 100%%; height: 120px; box-sizing: border-box; padding: 14px; border-radius: 24px; background: %s; border: 1px solid %s; display: flex; flex-direction: column; '
            'border-left: 6px solid %s;"><div style="display: flex; align-items: center; gap: 10px;">%s<span style="min-width: 0; display: flex; flex-direction: column;">'
            '<span style="font-size: 14px; font-weight: 600; white-space: nowrap; overflow: hidden; text-overflow: ellipsis;">%s</span><span style="font-size: 12px; color: %s;">%s</span></span></div>'
            '<div style="margin-top: auto; %s font-size: %dpx; font-weight: 700; white-space: nowrap;">%s <span style="font-size: 12px; color: %s;">%s</span></div></div>'
            % (CARD, LINE, color, badge(ini, color, ink, 32, 8), name, MUTED, kind, MONO, big, bal, MUTED, cur))


def acc_cards(v):
    return col(acc_card(v, 'Galicia', 'GA', '#F26E22', '#1A0A00', 'Карта', '1 250 000', 'ARS'),
               acc_card(v, 'Очень длинное имя счёта', 'ВК', '#FFD43B', '#1A1400', 'Вклад', '12 345 678,90', 'USDT'),
               cap('цвет банка — ' + ('вся карточка, чернила по контрасту' if v == 'A' else 'полоса слева и значок; фон — карточка темы')))


def buttons(v):
    return col(flexrow(btn(v, 'Сохранить'), btn(v, 'Отмена', 'outline'), btn(v, 'Удалить', 'danger')),
               flexrow(btn(v, 'Синхр.', 'tonal', 'refresh'), btn(v, 'Ещё', 'text'), btn(v, 'Нельзя', disabled=True)),
               cap('основная · контур · опасная · тональная · текстом · выключена 40 %'))


NEW = [
    ('Шапка экрана', top_bars, 'TopAppBar ×3 → CoShellTopBar(kind = Tab | Screen | Selection, scrolled)'),
    ('Выпадающее меню', menu, 'DropdownMenu → CoShellDropdownMenu · CoShellMenuItem(checked, destructive, enabled)'),
    ('Выбор даты', date_one, 'DatePicker → CoShellDatePicker (в шторке, «Готово»)'),
    ('Выбор периода', date_range, 'DateRangePicker → CoShellDateRangePicker'),
    ('Разделитель и загрузка', divider_loading, 'HorizontalDivider → CoShellDivider · CircularProgressIndicator → CoShellLoading(size)'),
    ('Аватар участника', avatars, 'CoShellAvatar(size, you, online) · CoShellAvatarStack'),
    ('Точка и статус', dots_status, 'CoShellDot(tone) · CoShellStatusChip(tone)'),
    ('Выбор иконки и цвета', strips, 'CoShellIconStrip · CoShellColorStrip'),
    ('Превью карточки счёта', acc_cards, 'CoShellAccountCard (цвет банка, чернила по контрасту)'),
    ('Кнопки: оттенки', buttons, 'CoShellButton(tone = Primary | Outline | Danger | Tonal | Text, enabled)'),
]
