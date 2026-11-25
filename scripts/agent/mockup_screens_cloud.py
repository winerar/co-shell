"""«Облачный разбор» (#635; poll 2026-11-23, all «А»): a screen next to «ИИ на телефоне» with one switch, a question before the first switch-on,
and what «Быстрые заметки» show while the cloud reads and when the daily limit is over. Boards `CloudOff`, `CloudOn`, `CloudConsent`, `CloudNotes`,
`CloudBusy`, `CloudLimit`; the row in «Настройки» is drawn in `Set1Root` (mockup_screens_fb_setcards.py). Imported by mockup_screens_forms.py.
"""
from mockup_bricks import *  # noqa: F401,F403
from mockup_catalog_bricks import status_chip
import mockup_screens_ai as ai
import mockup_screens_notes as notes

TEXT = ('Необязательно. Если телефон слабый или ИИ на нём не включён, строки заметок, нечитаемые чеки и голос прочитает облачный ИИ.')
SENT = [('note', 'Уходит', 'текст заметки или чека, названия категорий и счетов'),
        ('wallet', 'Не уходит', 'суммы и балансы, история, имена и данные семьи'),
        ('cloud', 'Нужен интернет', 'без сети всё работает как раньше')]
USE = [('note', 'Быстрые заметки и голос', 'строка, счёт и категория'), ('note', 'Незнакомые чеки', '«Прочитать с ИИ»'),
       ('spark', 'Категория для нового получателя', 'подсказка')]


def switch_card(on):
    return card('<div style="display: flex; align-items: center; gap: 12px; min-height: 48px; padding: 0 16px;"><span style="flex-grow: 1; font-size: 16px;">Облачный разбор</span>%s</div>' % switch(on))


def screen(body, bottom=''):
    title = 'Облачный разбор <span style="display: inline-flex; vertical-align: middle; margin-left: 4px;">%s</span>' % status_chip('бета', 'manual')
    return inner_screen(title, '<div style="margin-top: 8px; display: flex; flex-direction: column; gap: 10px;">%s</div>' % body) + bottom


def facts():
    return group([row(t, s, tile_icon(i, CYAN), min_h=56) for i, t, s in SENT])


def off_body():
    return ai.status_card('Выключен', TEXT) + switch_card(False) + facts()


def off():
    return board('Облачный разбор: выключен', screen(off_body()))


def on():
    use = ('<div style="margin: 0 4px 6px; font-size: 12px; letter-spacing: 0.04em; color: %s;">ГДЕ РАБОТАЕТ</div>' % MUTED
           + group([row(t, s, tile_icon(i, CYAN), min_h=56) for i, t, s in USE]))
    body = (ai.status_card('Включён', 'Работает, когда есть интернет. Осталось сегодня: 87 из 100.')
            + switch_card(True) + '<div>%s</div>' % use)
    return board('Облачный разбор: включён', screen(body))


def consent():
    dlg = dialog('Включить облачный разбор?', 'Текст заметки или чека и названия ваших категорий и счетов уйдут на сервер ИИ. Суммы, балансы и история '
                 'останутся на телефоне. Выключить можно в любой момент.', [('Не сейчас', 'text'), ('Включить', 'primary')])
    return board('Облачный разбор: вопрос перед включением', scrim(screen(off_body())) + dlg)


def no_login():
    """Not signed in to «Семья»: the card says what to do, the switch is dimmed and does nothing."""
    dim = '<div style="opacity: 0.38;">%s</div>' % switch_card(False)
    body = ai.status_card('Выключен', TEXT, ai.label_pill('нужен вход: Ещё → Семья', 'info')) + dim + facts()
    return board('Облачный разбор: нужен вход в «Семью»', screen(body))


def under_button(text):
    return ('<div style="position: absolute; left: 28px; right: 28px; bottom: 80px; display: flex; justify-content: center; font-size: 13px; color: %s;">%s</div>'
            % (MUTED, text))


def notes_screen(line, label, disabled=False):
    body = notes.note_card(notes.LINES, 'Каждая строка — одна трата. Можно добавить счёт и день.')
    return (inner_screen('Быстрые заметки', '<div style="margin-top: 8px;">%s</div>' % body) + under_button(line)
            + notes.footer(button(label, 'primary', None if disabled else 'spark', 0, disabled)))


def notes_cloud():
    return board('Быстрые заметки: строки прочитает облачный ИИ', notes_screen('Строки прочитает облачный ИИ', 'Обработать · 4'))


def notes_busy():
    return board('Быстрые заметки: читает облачный ИИ', notes_screen('Читает облачный ИИ…', 'Обрабатываю…', True))


def notes_limit():
    rows = [notes.draft(*d) for d in notes.DRAFTS]
    msg = message('Дневной лимит облачного разбора. Остальное разобрали правила и ИИ на телефоне, ничего не потеряно.', 'info')
    body = '<div style="margin-top: 8px;">%s</div>' % msg + notes.drafts_body(rows, '4 черновика · все готовы', 'Нажмите на строку, чтобы поправить.')
    return board('Черновики: дневной лимит облака', inner_screen('Черновики', body) + notes.footer(button('Записать · 4', 'primary', 'check', 0)))


BOARDS = [('CloudOff', off), ('CloudNoLogin', no_login), ('CloudOn', on), ('CloudConsent', consent), ('CloudNotes', notes_cloud), ('CloudBusy', notes_busy), ('CloudLimit', notes_limit)]
