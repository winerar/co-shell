"""«ИИ на телефоне» (#587, stage 2; poll 2026-11-21: card with a bar = «А», one «Включить» with two steps = «А», «Удалить модель» = «А»,
always ask before 2,5 GB = «А»). Boards `AiOff`, `AiStep1`, `AiStep2`, `AiOn`, `AiData`; the row in «Настройки» is drawn in `Set1Root`
(mockup_screens_fb_setcards.py). Imported by mockup_screens_feedback.py.
"""
from mockup_bricks import *  # noqa: F401,F403

TEXT = 'Необязательно. ИИ читает незнакомые чеки, разбирает заметки и голос и подсказывает категорию для нового получателя.'
FACTS = [('cloud', 'Размер', 'Дополнение 20 МБ + модель 2,5 ГБ'), ('phone', 'Память телефона', 'Нужно от 6 ГБ'),
         ('lock', 'Приватность', 'Всё считается на телефоне, без интернета')]
USE = [('note', 'Незнакомые чеки', '«Прочитать с ИИ»'), ('note', 'Быстрые заметки и голос', 'разбор сложных фраз'),
       ('spark', 'Категория для нового получателя', 'подсказка')]


def status_card(title, text, extra=''):
    """The one main block: sparkle disc, state, one sentence, then a bar or a label."""
    disc = ('<span style="width: 56px; height: 56px; border-radius: 50%%; background: %s; display: flex; align-items: center; justify-content: center;">%s</span>'
            % (LIMEPILL, svg('spark', LIME, 28, 1.8)))
    return hero_card(disc + '<div style="margin-top: 12px; font-size: 22px; font-weight: 700;">%s</div>'
                     '<div style="margin-top: 6px; font-size: 14px; line-height: 1.45; color: %s;">%s</div>%s' % (title, HERO_HINT, text, extra))


def label_pill(text, icon):
    """CoShellStatusChip: a dot in the tone and the words (the icon argument is kept for the callers' sake)."""
    return ('<span style="margin-top: 10px; display: inline-flex; align-items: center; gap: 6px; height: 22px; padding: 0 8px; border-radius: 999px; background: %s; font-size: 12px;">'
            '<span style="width: 6px; height: 6px; border-radius: 50%%; background: %s;"></span>%s</span>' % (LIMEPILL, LIME, text))


def bar(frac, done, total, left):
    """The bar and, under it, how much came and how much is left (like the update screen): «1 060 из 2 500 МБ · 42 %», «осталось 1 440 МБ»."""
    line = '<div style="margin-top: 4px; font-size: 13px; color: %s;">%s</div>'
    return (progress(frac, 14) + (line % (HERO_HINT, '%s из %s МБ · %d %%' % (done, total, round(frac * 100))))
            + (line % (HERO_HINT, 'осталось %s МБ' % left)))


def disc(n, state):
    """The step's icon (MenuIcon): done — check, now — download, waiting — grey cloud (no numbers: the card says «Шаг N из 2»)."""
    return {'done': tile_icon('check', LIME), 'now': tile_icon('download', LIME), 'wait': tile_icon('cloud', MUTED)}[state]


def steps(a, b):
    return group([row('Дополнение', a[0], disc(1, a[1]), min_h=56), row('Модель', b[0], disc(2, b[1]), min_h=56)])


def facts():
    return group([row(t, s, tile_icon(i, CYAN), min_h=56) for i, t, s in FACTS])


def footer(inner):
    return '<div style="position: absolute; left: 16px; right: 16px; bottom: 16px;">%s</div>' % inner


def screen(title_body, bottom=''):
    return inner_screen('ИИ на телефоне', '<div style="margin-top: 8px;">%s</div>' % title_body) + bottom


def off():
    body = status_card('Выключен', TEXT, label_pill('без него всё работает как раньше', 'check')) + '<div style="height: 10px;"></div>' + facts()
    return board('ИИ на телефоне: выключен', screen(body, footer(button('Включить · 2,5 ГБ', 'primary', 'cloud', 0))))


def step1():
    body = (status_card('Шаг 1 из 2', 'Android спросит, можно ли установить дополнение. Нажмите «Установить».', label_pill('ждём ваше «Установить»', 'info'))
            + '<div style="height: 10px;"></div>' + steps(('ждём установки', 'now'), ('потом, около 2,5 ГБ', 'wait')))
    return board('ИИ на телефоне: шаг 1, дополнение', screen(body, footer(button('Отменить', 'outline', None, 0))))


def step1_download():
    """Step 1 while the add-on's APK (20 МБ) downloads, before Android's «Установить»."""
    body = (status_card('Шаг 1 из 2', 'Android спросит, можно ли установить дополнение. Нажмите «Установить».', bar(.4, '8', '20', '12'))
            + '<div style="height: 10px;"></div>' + steps(('ждём установки', 'now'), ('потом, около 2,5 ГБ', 'wait')))
    return board('ИИ на телефоне: шаг 1, скачивание дополнения', screen(body, footer(button('Отменить', 'outline', None, 0))))


def step2():
    body = (status_card('Шаг 2 из 2 · скачиваю', 'Можно закрыть экран: загрузка продолжится.', bar(.42, '1 060', '2 500', '1 440'))
            + '<div style="height: 10px;"></div>' + steps(('установлено', 'done'), ('скачиваю…', 'now')))
    return board('ИИ на телефоне: шаг 2, модель', screen(body, footer(button('Отменить', 'outline', None, 0))))


def on():
    use = ('<div style="margin: 0 4px 6px; font-size: 12px; letter-spacing: 0.04em; color: %s;">ГДЕ РАБОТАЕТ</div>' % MUTED
           + group([row(t, s, tile_icon(i, CYAN), min_h=56) for i, t, s in USE]))
    body = status_card('Включён', 'Работает без интернета. Занимает 2,5 ГБ.', label_pill('готов', 'check')) + '<div style="height: 14px;"></div>' + use
    note_ = ('<p style="margin: 10px 8px 0; font-size: 13px; line-height: 1.45; color: %s;">Дополнение (20 МБ) останется. Убрать его можно в настройках телефона, в списке приложений.</p>' % MUTED)
    return board('ИИ на телефоне: включён', screen(body + note_, footer(button('Удалить модель', 'outline', 'trash', 0))))


def data():
    under = screen(status_card('Выключен', TEXT, label_pill('без него всё работает как раньше', 'check')) + '<div style="height: 10px;"></div>' + facts(),
                   footer(button('Включить · 2,5 ГБ', 'primary', 'cloud', 0)))
    dlg = dialog('Скачать 2,5 ГБ?', 'Лучше по Wi-Fi: мобильный интернет может быть платным. Удалить можно в любой момент.', [('Отмена', 'text'), ('Скачать', 'primary')])
    return board('ИИ на телефоне: вопрос про мобильный интернет', scrim(under) + dlg)


def weak():
    """Phone under 6 GB (poll 2026-11-21, «А»): the card says why, no button."""
    body = (status_card('Этому телефону не подойдёт', 'Для ИИ нужно от 6 ГБ памяти, здесь 4. Всё остальное работает как раньше: чеки по QR, подсказки категории по истории.')
            + '<div style="height: 10px;"></div>' + facts())
    return board('ИИ на телефоне: слабый телефон', screen(body))


BOARDS = [('AiWeak', weak), ('AiOff', off), ('AiStep1', step1), ('AiStep1Load', step1_download), ('AiStep2', step2), ('AiOn', on), ('AiData', data)]
FEEDBACK = []
