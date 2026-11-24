"""Generated boards: #593 «Прочитать с ИИ» — a shared receipt no format knows (window of #561) read by the model (owner polled 2026-11-21, «А»).

With a model: «✦ Прочитать с ИИ» (main), «Отправить образец», «Ввести вручную»; the text says whether the receipt leaves the phone (cloud #635)
or not (AI on this phone #587). No model on: «Отправить образец» (main), «Включить ИИ», «Ввести вручную». While reading — «Читаю чек…» with a
running line and «Отменить». Read — the sheet as from any receipt with one line «✦ Прочитано ИИ · проверьте сумму и дату» + «бета».
Nothing printed found — the window again without «Прочитать с ИИ». Imported by mockup_screens_feedback.py.
"""
from mockup_bricks import *  # noqa: F401,F403
from mockup_catalog_bricks import status_chip
from mockup_screens_fb_561 import sheet, DOC
from mockup_screens_fb_qr526 import amount

FEEDBACK = [
    (593, '«Прочитать с ИИ» для незнакомого чека · 2026-11-24',
     'Окно «Не удалось прочитать чек» получает «✦ Прочитать с ИИ» (облако или ИИ на телефоне — текст окна говорит, уходит ли чек с телефона); '
     'без ИИ — «Включить ИИ». Пока читает — «Читаю чек…» и «Отменить». Сумма берётся, только если она напечатана в чеке; '
     'над суммой — «Прочитано ИИ · проверьте сумму и дату». Не нашёл итог — окно снова, без кнопки ИИ.'),
]

SCRIM = '<div style="position: absolute; inset: 0; background: rgba(0,0,0,0.5);"></div>'


def pill(label, kind, icon=None):
    st = ('background: %s; color: %s; font-weight: 700; box-shadow: inset 0 0 0 2px %s;' % (LIME, ONLIME, ONLIME) if kind == 'primary'
          else 'background: transparent; color: %s; box-shadow: inset 0 0 0 1px %s;' % (INK, OUTLINE))
    glyph = svg(icon, ONLIME if kind == 'primary' else INK, 18) if icon else ''
    return ('<div style="height: 48px; border-radius: 999px; display: flex; align-items: center; justify-content: center; gap: 8px; font-size: 15px; %s">%s%s</div>'
            % (st, glyph, label))


def window(icon, title, text, buttons, extra=''):
    """`CoShellAlertDialog` with stacked pills (a third action or a narrow screen) and, when reading, a running line under the text."""
    badge = ('<span style="width: 40px; height: 40px; border-radius: 16px; background: %s; display: flex; align-items: center; justify-content: center;">%s</span>'
             % (VIOLETPILL, icon))
    return ('<div role="dialog" aria-label="%s" style="position: absolute; left: 24px; right: 24px; top: 50%%; transform: translateY(-50%%); padding: 24px 20px 20px; '
            'border-radius: 28px; background: %s;">%s<h2 style="margin: 14px 0 0; font-size: 20px;">%s</h2>'
            '<p style="margin: 8px 0 0; font-size: 14px; line-height: 1.45; color: %s;">%s</p>%s'
            '<div style="margin-top: 20px; display: flex; flex-direction: column; gap: 10px;">%s</div></div>'
            % (title, CHIP, badge, title, INK2, text, extra, ''.join(buttons)))


def receipt_icon():
    return DOC.replace('16', '22') % VIOLET


def unread_with(text, buttons):
    return sheet() + SCRIM + window(receipt_icon(), 'Не удалось прочитать чек', text, buttons)


def on_cloud():
    return unread_with('Такого формата мы ещё не знаем. Облачный ИИ прочитает чек, а вы проверите поля. Текст чека уйдёт на сервер ИИ.',
                       [pill('Прочитать с ИИ', 'primary', 'spark'), pill('Отправить образец', 'outline'), pill('Ввести вручную', 'outline')])


def on_phone():
    return unread_with('Такого формата мы ещё не знаем. ИИ на телефоне прочитает чек сам, без интернета, а вы проверите поля.',
                       [pill('Прочитать с ИИ', 'primary', 'spark'), pill('Отправить образец', 'outline'), pill('Ввести вручную', 'outline')])


def off():
    return unread_with('Такого формата мы ещё не знаем. Отправьте образец — добавим формат. Или включите ИИ: Ещё → Настройки → Умные функции.',
                       [pill('Отправить образец', 'primary'), pill('Включить ИИ', 'outline'), pill('Ввести вручную', 'outline')])


def busy():
    bar = ('<div style="margin-top: 16px; height: 8px; border-radius: 4px; background: %s; overflow: hidden;"><div style="width: 55%%; height: 8px; '
           'border-radius: 4px; background: %s; box-shadow: 0 0 0 1px %s;"></div></div>' % (BAR, LIME, ONLIME))
    return sheet() + SCRIM + window(svg('spark', VIOLET, 22), 'Читаю чек…', 'Это займёт несколько секунд.', [pill('Отменить', 'primary')], bar)


def failed():
    return unread_with('ИИ не нашёл итог в чеке. Введите сумму вручную или отправьте образец — добавим формат.',
                       [pill('Ввести вручную', 'primary'), pill('Отправить образец', 'outline')])


def ai_line():
    return ('<div style="display: flex; align-items: center; gap: 6px; margin-top: 6px; font-size: 12px; color: %s;">%s<span>Прочитано ИИ · проверьте сумму и дату</span>%s</div>'
            % (MUTED, svg('spark', MUTED, 16), status_chip('бета', 'manual')))


def filled():
    return sheet('Mi Kiosco', True, amount('37 914,50', 'ARS', extra=ai_line()))


BOARDS = [  # name, title, fn, issue number
    ('Rc593Cloud', '#593 · незнакомый чек: «Прочитать с ИИ» (облако)', on_cloud, 593),
    ('Rc593Phone', '#593 · незнакомый чек: «Прочитать с ИИ» (ИИ на телефоне)', on_phone, 593),
    ('Rc593Off', '#593 · незнакомый чек, ИИ не включён: «Включить ИИ»', off, 593),
    ('Rc593Busy', '#593 · «Читаю чек…»', busy, 593),
    ('Rc593Filled', '#593 · прочитано ИИ: «проверьте сумму и дату»', filled, 593),
    ('Rc593Failed', '#593 · ИИ не нашёл итог', failed, 593),
]
