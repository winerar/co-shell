"""Feedback boards of 2026-11-12: update screen (#268 version and notes, #269 manual install) and fruit palettes (#271).

Fixed colours (palette swatches) are written as rgb(): mockup_theme.py repaints only #hex, so they stay as drawn.
"""
from mockup_bricks import *  # noqa: F401,F403
from mockup_parts import ring, top_bar, footer, pill

FEEDBACK = [
    (268, 'Отзывы #268 + #269 · экран обновления: какая версия стоит, что в ней, ручная установка',
     'Сейчас у всех сборок имя «0.1.0». Решение (2026-11-12): версия растёт с каждым релизом (0.12 → 0.13), рядом — номер сборки. '
     'На экране — «Сейчас 0.12 → новая 0.13» и «Что нового»; когда обновлений нет — «Ваша версия 0.13» и что в ней. «Установить вручную» — сразу под кнопкой, как только файл скачан (сейчас — через 15 с ожидания).'),
    (271, 'Отзыв #271 · темы-фрукты: сочные и нежные',
     'Выбрано 2026-11-12: 7 «сочных» (Вольт → Лайм, Мохито → Маракуйя (фиолетовая: Киви слишком похож на Лайм), Ультра → Черника, Лагуна → Фейхоа, '
     'Сакура → Питахайя, Манго, новый Лимон) и 7 новых «нежных»: Персик, Личи, Дыня, Голубика, Слива, Груша, Крыжовник. У каждой — тёмный и светлый вид.'),
]

NOTES = ['Плавная смена темы, когда телефон переключает светлую/тёмную', 'Поиск по операциям — пилюлей сверху', 'Приложение легче: 8,0 → 5,5 МБ']
OLD_NOTES = ['Журнал обновления уходит в отзыв', 'Кнопка «Установить вручную»']


def bullets(items):
    return '<ul style="margin: 8px 0 0; padding-left: 18px; font-size: 13px; line-height: 1.5;">%s</ul>' % ''.join('<li>%s</li>' % t for t in items)


def round_icon(icon, fill=LIME, ink=ONLIME):
    return ('<span style="width: 84px; height: 84px; border-radius: 50%%; background: %s; display: flex; align-items: center; justify-content: center;">%s</span>'
            % (fill, svg(icon, ink, 40, 2.3)))


def heading(title, sub):
    return ('<h2 style="margin: 18px 0 0; text-align: center; font-size: 22px;">%s</h2>'
            '<div style="text-align: center; margin-top: 4px; font-size: 13px; color: %s;">%s</div>' % (title, MUTED, sub))


def versions(now, new):
    col = ('<span style="flex: 1; display: flex; flex-direction: column; gap: 2px; padding: 12px 14px;">'
           '<span style="font-size: 12px; color: %s;">%s</span><span style="%s font-size: 20px; font-weight: 700; color: %s;">%s</span>'
           '<span style="font-size: 12px; color: %s;">%s</span></span>')
    arrow = '<span style="align-self: center; color: %s; font-size: 20px;">→</span>' % MUTED
    return card('<div style="display: flex;">%s%s%s</div>' % (col % (MUTED, 'Сейчас', MONO, INK, now[0], MUTED, now[1]), arrow,
                                                               col % (MUTED, 'Новая', MONO, LIME, new[0], MUTED, new[1])), extra='margin-top: 18px;')


def notes_card(title, when, items, extra=''):
    return card('<div style="display: flex; font-size: 13px;"><b style="flex-grow: 1; color: %s;">%s</b><span style="color: %s;">%s</span></div>%s%s'
                % (LIME, title, MUTED, when, bullets(items), extra), pad='14px', extra='margin-top: 12px;')


def manual_link(text='Не ставится? Установить вручную'):
    return ('<div style="margin-top: 6px; height: 44px; display: flex; align-items: center; justify-content: center; gap: 6px; font-size: 14px; '
            'font-weight: 600; color: %s;">%s%s</div>' % (LIME, svg('file', LIME, 18), text))


def update_available():
    body = ('<div style="padding: 8px 16px 0;">%s%s%s%s</div>'
            % (ring(1, round_icon('download')), heading('Вышла версия 0.13', 'сборка 384 · 5,5 МБ · 12 ноября'),
               versions(('0.12', 'сборка 380 · 10 нояб.'), ('0.13', 'сборка 384 · 12 нояб.')), notes_card('Что нового в 0.13', '3 пункта', NOTES)))
    return top_bar('Обновление') + body + footer(button('Обновить', 'primary', 'download', 0), manual_link())


def update_latest():
    older = ('<div style="margin-top: 12px; padding-top: 10px; border-top: 1px solid %s; display: flex; font-size: 13px; color: %s;">'
             '<span style="flex-grow: 1;">Прошлые версии: 0.12, 0.11, 0.10…</span>%s</div>' % (DIV, MUTED, chevron()))
    mine = card('<div style="display: flex; align-items: center; gap: 12px;">%s<span style="flex-grow: 1; display: flex; flex-direction: column; gap: 2px;">'
                '<span style="font-size: 12px; color: %s;">Ваша версия</span><span style="%s font-size: 20px; font-weight: 700;">0.13</span><span style="font-size: 12px; color: %s;">сборка 384</span></span>'
                '<span style="font-size: 12px; color: %s; text-align: right;">установлена<br>12 ноября</span></div>'
                % (tile_icon('check', ONLIME, LIME), MUTED, MONO, MUTED, MUTED), pad='14px', extra='margin-top: 18px;')
    body = ('<div style="padding: 8px 16px 0;">%s%s%s%s</div>'
            % (ring(0, round_icon('check', CHIP, LIME)), heading('У вас последняя версия', 'Проверили только что'), mine,
               notes_card('Что в этой версии', '', NOTES, older)))
    return top_bar('Обновление') + body + footer(button('Проверить снова', 'outline', 'refresh', 0))


def update_installing():
    hint = ('<div style="margin-top: 18px; display: flex; gap: 10px; padding: 12px 14px; border-radius: 14px; background: %s; font-size: 13px; line-height: 1.45; color: %s;">'
            '%s<span>Окно Android не появилось или выдало ошибку? «Установить вручную» откроет установщик Android — как для файла из браузера. '
            'Кнопка видна сразу, ждать 15 секунд не нужно.</span></div>' % (CHIP, INK2, svg('info', MUTED, 18)))
    body = ('<div style="padding: 8px 16px 0;">%s%s%s</div>'
            % (ring(0.72, '<span style="%s font-size: 15px; color: %s;">0.13</span>' % (MONO, MUTED)),
               heading('Устанавливаем', 'Подтвердите установку в окне Android. Данные сохранятся.'), hint))
    return top_bar('Обновление') + body + footer(button('Установить вручную', 'outline', 'file', 0))


# ---- #271 · fruit palettes -------------------------------------------------------------------------------------------
# name, primary, dark bg, light bg, income dark/light, expense dark/light (hex without #, drawn as rgb())
JUICY = [('Лайм', 'B8FF2E', '050805', 'F9FFE9', '00F0FF', '00708A', 'FF2E63', 'D1004B'),
         ('Маракуйя', 'A66BFF', '0E0A14', 'F8F4FF', '38D9FF', '00709E', 'FF5C7A', 'D6004A'),
         ('Манго', 'FF8A3D', '160C16', 'FFF5EA', 'B6FF3D', '3F7300', 'FF4D5E', 'C8102E'),
         ('Черника', '3D7BFF', '07090C', 'F4F7FF', '7CFF6B', '2E7A00', 'FF3B6B', 'C4004A'),
         ('Фейхоа', '2EE6C5', '04100F', 'F0FFFB', '7CF8FF', '006A78', 'FF4D8D', 'C2185B'),
         ('Питахайя', 'FF7AB8', '120A10', 'FFF4F8', '6BE4FF', '006A87', 'FF4D5E', 'C8102E'),
         ('Лимон', 'FFE14D', '0F0E06', 'FFFCE8', '38D9FF', '00709E', 'FF4D6D', 'C8102E')]
SOFT = [('Персик', 'FFB38A', '15100E', 'FFF7F2', '8FD9C4', '1F7A64', 'F28B9B', 'B3364D'),
        ('Личи', 'F4A7C0', '140E11', 'FFF6F9', '9FD8E8', '1D6E82', 'F2907E', 'B04030'),
        ('Дыня', 'EDD27E', '13120C', 'FFFBEF', '9CD8B0', '2A7445', 'F29A9A', 'B23A3A'),
        ('Голубика', 'A3BFFA', '0C0F15', 'F5F8FF', '9EE0C0', '1F7550', 'F5A3B5', 'B0385A'),
        ('Слива', 'C3A6F0', '110D16', 'FAF6FF', '9BD8E0', '1D6B78', 'F5A08F', 'B04330'),
        ('Груша', 'C8E39A', '0F130C', 'F8FCF1', '93D3E8', '1C6A85', 'F09AAF', 'AD3756'),
        ('Крыжовник', '9EE6CF', '0B1311', 'F2FCF8', 'A9C8FF', '2A55A8', 'F5A3A0', 'B23A36')]


def rgbh(h):
    return 'rgb(%d,%d,%d)' % tuple(int(h[i:i + 2], 16) for i in (0, 2, 4))


def swatch(p, selected=False, light=False):
    name, prim, bgd, bgl = p[:4]
    ring_ = 'box-shadow: 0 0 0 2px %s, 0 0 0 4px %s;' % (BG, LIME) if selected else ''
    return ('<span style="display: flex; flex-direction: column; align-items: center; gap: 6px; font-size: 12px; color: %s;">'
            '<span style="width: 56px; height: 56px; border-radius: 16px; background: %s; border: 1px solid %s; position: relative; %s">'
            '<span style="position: absolute; left: 10px; top: 10px; width: 24px; height: 24px; border-radius: 50%%; background: %s;"></span>'
            '<span style="position: absolute; right: 9px; bottom: 9px; width: 12px; height: 12px; border-radius: 50%%; background: %s;"></span></span>%s</span>'
            % (INK if selected else MUTED, rgbh(bgl if light else bgd), LINE2, ring_, rgbh(prim), rgbh(p[4 if not light else 5]), name))


def swatch_grid(items, selected=None):
    return ('<div style="display: grid; grid-template-columns: repeat(4, minmax(0, 1fr)); gap: 14px 6px; padding: 4px 0;">%s</div>'
            % ''.join(swatch(p, p[0] == selected) for p in items))


def look_fruit():
    modes = segmented(['Система', 'Тёмная', 'Светлая'], 'Тёмная')
    body = (label('Тема', top=4) + modes + label('Сочные · яркий неон · 7', top=16) + swatch_grid(JUICY, 'Лайм')
            + label('Нежные · пастель · 7', top=16) + swatch_grid(SOFT)
            + note('Сочные — нынешние палитры с именами фруктов (Вольт → Лайм…), Мохито → Маракуйя, новый Лимон; ваш выбор сохранится. Нежные — все новые.'))
    return inner_screen('Внешний вид', body)


def mini(p, light):
    name, prim, bgd, bgl, incd, incl, expd, expl = p
    bg, inc, exp_ = (bgl, incl, expl) if light else (bgd, incd, expd)
    ink = '#0B0F0A' if light else INK
    return ('<span style="flex: 1; min-width: 0; height: 40px; border-radius: 12px; background: %s; border: 1px solid %s; display: flex; align-items: center; '
            'gap: 6px; padding: 0 8px; box-sizing: border-box; color: %s;"><span style="height: 22px; padding: 0 8px; border-radius: 999px; background: %s; '
            'color: rgb(14,20,10); font-size: 11px; font-weight: 600; display: flex; align-items: center;">Ок</span>'
            '<span style="%s font-size: 11px; color: %s;">+1 200</span><span style="%s font-size: 11px; color: %s;">−450</span></span>'
            % (rgbh(bg), LINE2, ink, rgbh(prim), MONO, rgbh(inc), MONO, rgbh(exp_)))


def palette_rows(items):
    return ''.join('<div style="display: flex; align-items: center; gap: 8px; margin-top: 5px;"><span style="width: 78px; font-size: 13px;">%s</span>%s%s</div>'
                   % (p[0], mini(p, False), mini(p, True)) for p in items)


def palettes_all():
    head = ('<div style="display: flex; gap: 8px; margin: 0 0 2px 86px; font-size: 12px; color: %s;"><span style="flex: 1;">тёмная</span>'
            '<span style="flex: 1;">светлая</span></div>' % MUTED)
    body = (head + label('Сочные', top=6) + palette_rows(JUICY) + label('Нежные', top=14) + palette_rows(SOFT)
            + note('Кнопка, доход и расход на фоне каждой темы. Цвета нежных — черновик; подберу точно по контрасту после «ок».'))
    return inner_screen('14 тем · обе стороны', body)


BOARDS = [  # name, title, fn, issue number
    ('Fb268A', 'Есть новая: «Сейчас 0.12 → новая 0.13», что нового, ручная установка снизу (выбрано)', update_available, 268),
    ('Fb268B', 'Последняя версия: какая стоит и что в ней', update_latest, 268),
    ('Fb269A', 'Установка: «Установить вручную» сразу, без 15 с ожидания', update_installing, 268),
    ('Fb271A', 'Внешний вид: 7 сочных + 7 нежных (выбрано)', look_fruit, 271),
    ('Fb271Pal', 'Все 14 тем в тёмной и светлой', palettes_all, 271),
]
