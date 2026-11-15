"""«Компоненты v3» — existing rows, forms, questions and sheets (#367, part 2 of 3), each as a function of v."""
from mockup_catalog_kit import *  # noqa: F401,F403
from mockup_catalog_choice import seg, pill, radio


def menu_rows(v):
    items = [('folder', LIME, 'Категории', '24 категории · 6 папок', ''), ('rate', CYAN, 'Курсы валют', 'сегодня', ''),
             ('gift', VIOLET, 'Что нового', '', dot(LIME)), ('user', WARN, 'Очень длинное название раздела меню', 'и длинная подпись', '')]
    rows_ = [row(t, s, holder(v, ic, c, 36), (tr + ' ' if tr else '') + chevron(), min_h=56) for ic, c, t, s, tr in items]
    if v == 'A':
        body = card(''.join(x.replace('{{div}}', 'border-top: 1px solid %s;' % DIV if i else '') for i, x in enumerate(rows_)), radius=24)
    else:
        body = col(*[card(x.replace('{{div}}', ''), radius=12) for x in rows_], gap=4)
    return col(body, cap('значок — ' + ('круг с тоном' if v == 'A' else 'квадрат с тоном') + '; строки — ' + ('одной карточкой' if v == 'A' else 'отдельными плитками')))


def form_section(v, title, inner, link=''):
    lk = '<span style="font-size: 13px; font-weight: 600; color: %s;">%s</span>' % (INK if v == 'B' else LIME, link) if link else ''
    if v == 'A':
        head = ('<div style="display: flex; align-items: center;"><span style="flex-grow: 1; font-size: 11px; font-weight: 600; letter-spacing: 0.06em; color: %s;">%s</span>%s</div>'
                % (MUTED, title.upper(), lk))
        return box(v, head + '<div style="margin-top: 8px;">%s</div>' % inner, '12px 14px')
    head = ('<div style="display: flex; align-items: center; padding-bottom: 8px; border-bottom: 1px solid %s;"><span style="flex-grow: 1; font-size: 14px; font-weight: 700;">%s</span>%s</div>'
            % (LINE2, title, lk.replace('">', '; text-decoration: underline;">', 1) if lk else ''))
    return box(v, head + '<div style="margin-top: 10px;">%s</div>' % inner, '12px 14px')


def forms(v):
    return col(form_section(v, 'Сумма · списано', amount_text('4 500', 'ARS', 26, PINK), '+ Чек'),
               form_section(v, 'Категория · частые', flexrow(pill(v, 'Продукты', True), pill(v, 'Кафе'), pill(v, 'Такси')), 'Все'))


def hint(v, text):
    if v == 'A':
        return ('<div style="display: flex; gap: 10px; padding: 10px 14px; border-radius: 18px; background: %s; font-size: 12px; line-height: 1.4; color: %s;">%s%s</div>'
                % (mix(CYAN, 10), INK2, svg('info', CYAN, 18), text))
    return ('<div style="display: flex; gap: 10px; padding: 10px 12px; border-left: 3px solid %s; font-size: 12px; line-height: 1.4; color: %s;">%s</div>' % (OUTLINE, INK2, text))


def fields(v):
    return col(fld(v, 'Название', 'Galicia · карта'), fld(v, 'Название', 'Например, «Наличные»', True), fld(v, 'Код валюты', 'EU', error='Нужны три буквы ISO'),
               hint(v, 'Курс берём на дату операции. Его можно поправить вручную.'))


def confirm(v, title, text, ok, cancel, danger=False):
    icon_c = PINK if danger else LIME
    if v == 'A':
        ic = holder(v, 'trash' if danger else 'info', icon_c, 48)
        btns = '<div style="align-self: stretch; display: flex; gap: 8px; margin-top: 6px;">%s%s</div>' % (
            btn(v, cancel, 'outline', w='100%'), btn(v, ok, 'danger' if danger else 'primary', w='100%'))
        return ('<div style="padding: 20px; border-radius: 28px; background: %s; display: flex; flex-direction: column; align-items: center; gap: 10px; text-align: center;">%s'
                '<span style="font-size: 18px; font-weight: 700;">%s</span><span style="font-size: 13px; line-height: 1.4; color: %s;">%s</span>%s</div>' % (KEY, ic, title, INK2, text, btns))
    head = flexrow(holder(v, 'trash' if danger else 'info', icon_c, 32), '<span style="font-size: 18px; font-weight: 700;">%s</span>' % title, gap=10, wrap=False)
    btns = '<div style="display: flex; justify-content: flex-end; gap: 8px; margin-top: 6px;">%s%s</div>' % (btn(v, cancel, 'outline'), btn(v, ok, 'danger' if danger else 'primary'))
    return ('<div style="padding: 18px; border-radius: 16px; background: %s; border: 1px solid %s; display: flex; flex-direction: column; gap: 10px;">%s'
            '<span style="font-size: 13px; line-height: 1.4; color: %s;">%s</span>%s</div>' % (KEY, OUTLINE, head, INK2, text, btns))


def confirms(v):
    return col(confirm(v, 'Сохранить копию?', 'Перед стиранием сохраним всё в файл.', 'Сохранить', 'Не сейчас'),
               confirm(v, 'Удалить счёт?', 'Счёт без операций удалится навсегда.', 'Удалить', 'Отмена', True),
               cap('по центру, кнопки поровну' if v == 'A' else 'по левому краю, кнопки справа'))


def sheet(v, title, body, action=None, action_on=True):
    act = '<div style="padding: 10px 16px 16px; display: flex;">%s</div>' % btn(v, action, w='100%', disabled=not action_on) if action else ''
    if v == 'A':
        top = ('<div style="display: flex; justify-content: center; padding: 10px 0 4px;"><span style="width: 36px; height: 4px; border-radius: 2px; background: %s;"></span></div>'
               '<div style="display: flex; align-items: center; padding: 4px 16px 10px;"><span style="flex-grow: 1; font-size: 20px; font-weight: 700;">%s</span>%s</div>'
               % (OUTLINE, title, svg('close', MUTED, 22)))
        return '<div style="border-radius: 28px 28px 0 0; background: %s; overflow: hidden;">%s<div style="padding: 0 16px;">%s</div>%s</div>' % (SHEET, top, body, act)
    top = ('<div style="display: flex; align-items: center; gap: 8px; padding: 12px 12px 12px 16px; border-bottom: 1px solid %s;"><span style="flex-grow: 1; font-size: 18px; '
           'font-weight: 700;">%s</span><span style="width: 32px; height: 32px; border-radius: 8px; box-shadow: inset 0 0 0 1px %s; display: flex; align-items: center; '
           'justify-content: center;">%s</span></div>' % (LINE2, title, OUTLINE, svg('close', INK2, 18)))
    return '<div style="border-radius: 16px 16px 0 0; background: %s; overflow: hidden; border: 1px solid %s; border-bottom: none;">%s<div style="padding: 12px 16px 0;">%s</div>%s</div>' % (
        SHEET, LINE, top, body, act)


def sheets(v):
    pick = ''.join(row(c, n, '', radio(v, i == 0), min_h=48).replace('{{div}}', 'border-top: 1px solid %s;' % DIV if i else '') for i, (c, n) in enumerate([('USD', 'Доллар США'), ('ARS', 'Аргентинское песо')]))
    return col(sheet(v, 'Валюта', pick), sheet(v, 'Курс вручную', fld(v, '1 USD =', '1 482,10 ARS'), 'Сохранить', False),
               cap('ручка сверху, радиус 28' if v == 'A' else 'без ручки: заголовок с ✕ и линия, радиус 16'))


def peek(v):
    rows_ = ''.join('<div style="display: flex; justify-content: space-between; gap: 16px; font-size: 14px;"><span style="%s color: %s;">%s</span><span style="color: %s;">%s</span></div>'
                    % (MONO, INK if a else MUTED, a or 'нет курса', MUTED, c) for a, c in [('≈ 31,53', 'USD'), ('≈ 12 180', 'AMD'), (None, 'VES')])
    return ('<div style="width: 190px; padding: 12px 14px; border-radius: %s; background: %s; box-shadow: inset 0 0 0 1px %s, 0 12px 32px rgba(0,0,0,0.45); display: flex; '
            'flex-direction: column; gap: 6px;">%s<span style="font-size: 11px; color: %s;">по курсу на 12 ноября</span></div>' % ('20px' if v == 'A' else '10px', KEY, LINE2, rows_, MUTED))


def entry(v):
    return col(sheet(v, 'Новая операция', seg(v, ['Расход', 'Доход', 'Перевод'], 0), 'Сохранить'), flexrow(peek(v), cap('держите палец<br>на сумме'), align='flex-start'))


def reveal(v):
    if v == 'A':
        demo = ('<div style="position: relative; height: 110px; border-radius: 24px; background: %s; overflow: hidden;"><div style="position: absolute; right: -60px; top: -60px; '
                'width: 200px; height: 200px; border-radius: 50%%; background: #F9FFE9;"></div></div>' % BG)
        return col(demo, cap('новая тема раскрывается кругом из нажатой кнопки'))
    demo = ('<div style="position: relative; height: 110px; border-radius: 16px; background: %s; overflow: hidden;"><div style="position: absolute; left: 0; right: 0; top: 0; '
            'height: 55%%; background: #F9FFE9;"></div><div style="position: absolute; left: 0; right: 0; top: 55%%; height: 2px; background: %s;"></div></div>' % (BG, LIME))
    return col(demo, cap('новая тема наплывает сверху шторкой'))


ROWS = [
    ('Строка меню', menu_rows, 'CoShellMenuRow(icon, color, title, subtitle, trailing) · MenuIcon'),
    ('Карточка формы', forms, 'CoShellFormSection(title, trailing) · CoShellSectionLink'),
    ('Поле, ошибка, подсказка', fields, 'CoShellTextField · CoShellSection(title, error) · CoShellHint'),
    ('Карточка-вопрос', confirms, 'CoShellConfirmCard(title, text, confirmLabel, dismissLabel, destructive)'),
    ('Шторка', sheets, 'CoShellModalSheet(title, actionLabel, actionEnabled) · CoShellModalSheetLayout'),
    ('Шторка ввода и пузырь', entry, 'CoShellSheetLayout · CoShellPeekBubble(lines, footnote) · PeekCard — содержимое пузыря'),
    ('Смена темы', reveal, 'ThemeRevealHost — обёртка приложения'),
]
