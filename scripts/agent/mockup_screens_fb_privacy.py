"""Poll boards: #145 — privacy: app lock (own PIN + fingerprint), hidden sums («eye» + phone face down), recents preview and screenshots.

Owner's answers 2026-11-20: own 4-digit PIN + fingerprint/face, «Забыли PIN?» through the phone's own lock (data stays);
hide sums with the «eye» and by laying the phone face down; recents preview and screenshots — by settings; relock: at once / 1 / 5 min.
Poll poll-privacy-145.json — all «А» (2026-11-20): keypad cells, «•••• USD», eye in the header; notes: theme colours, «блокировать»; imported by mockup_screens_feedback.py.
"""
from mockup_bricks import *  # noqa: F401,F403
from mockup_parts import *  # noqa: F401,F403
from mockup_screens_main import RECENT, tile, tiles, recent_card

FEEDBACK = [
    (145, 'Конфиденциальность: PIN, отпечаток, скрытие сумм · опрос 2026-11-20, все А',
     'Свой PIN из 4 цифр + отпечаток или лицо; «Забыли PIN?» — через блокировку телефона, данные не тронуты. Блокировать: сразу / 1 / 5 мин. '
     'Суммы скрывает «глаз» и переворот телефона экраном вниз. Превью в недавних и скриншоты — переключатели в «Ещё → Конфиденциальность».'),
]

EYE = 'M2 12s3.6-7 10-7 10 7 10 7-3.6 7-10 7S2 12 2 12zM12 15a3 3 0 1 0 0-6 3 3 0 0 0 0 6z'
EYE_OFF = 'M3 3l18 18M10.6 5.1A10.9 10.9 0 0 1 12 5c6.4 0 10 7 10 7a17.6 17.6 0 0 1-3.2 4.2M6.6 6.6A17.4 17.4 0 0 0 2 12s3.6 7 10 7a10 10 0 0 0 5.4-1.6M9.9 9.9a3 3 0 0 0 4.2 4.2'
FINGER = ('M7.5 4.8A9 9 0 0 1 21 12.5M3.5 9a9 9 0 0 1 2-3M3 13.5v-1M6.5 20a14 14 0 0 1-1.4-7.5 6.9 6.9 0 0 1 13.8 0V14M9.6 21a14 14 0 0 1-1.6-8.5 4 4 0 0 1 8 0'
          'c0 3 .4 5.4 1.2 7.5M12 12.5c0 3.7.7 6.4 2 8.5')
SHIELD = 'M12 3l8 3v6c0 4.5-3.4 8.2-8 9-4.6-.8-8-4.5-8-9V6zM9 12l2 2 4-4'
FLIP = 'M7 3h10v18H7zM11 18h2M3 9l2-2 2 2M5 7v6M21 15l-2 2-2-2M19 17v-6'
HIDDEN = '••••'


def dots(n, filled, color=LIME, error=False):
    """PIN dots (new component «CoShellPinDots»): empty ring / filled neon; error — coral rings."""
    c = PINK if error else color
    one = lambda on: ('<span style="width: 16px; height: 16px; border-radius: 50%%; box-sizing: border-box; %s"></span>'
                      % (('background: %s; box-shadow: 0 0 0 1px %s;' % (c, INK)) if on else 'border: 2px solid %s;' % (PINK if error else OUTLINE)))
    return '<div style="display: flex; gap: 18px; justify-content: center;">%s</div>' % ''.join(one(i < filled) for i in range(n))


def pin_keypad(finger=True, rounded=False):
    """Icons — thin like everywhere (~1.5 px line at 24, owner 2026-11-20). A — CoShellKeypad cells (16 radius, the amount keypad's look); B — round keys like the phone's own lock (new component)."""
    def k(t, bg=KEY, col=INK, sz=26):
        r = '50%' if rounded else '16px'
        size = 'width: 76px; height: 76px; justify-self: center;' if rounded else ''
        return ('<span style="border-radius: %s; background: %s; color: %s; font-size: %dpx; %s %s display: flex; align-items: center; justify-content: center;">%s</span>'
                % (r, bg, col, sz, MONO, size, t))
    fp = k(svg(FINGER, INK, 24, 1.5), KEY2) if finger else '<span></span>'
    keys = ''.join(k(str(d)) for d in range(1, 10)) + fp + k('0') + k(svg('M20 6H9l-6 6 6 6h11zM12 9l6 6M18 9l-6 6', INK, 24, 1.5), KEY2)
    rows = '86px' if rounded else '64px'
    return '<div style="display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); grid-auto-rows: %s; gap: %s;">%s</div>' % (rows, '10px' if rounded else '8px', keys)


def lock_screen(rounded=False, filled=2, title='Введите PIN', sub='', error=False, link='Забыли PIN?', finger=True):
    logo = ('<div style="width: 56px; height: 56px; border-radius: 50%%; background: %s; display: flex; align-items: center; justify-content: center; margin: 0 auto;">%s</div>'
            % (LIME, svg('lock', ONLIME, 24, 1.5)))  # IconBadge(ic_lock, primary, badgeLarge): the theme's neon (owner: «в цветах темы»)
    sub_html = '<div style="margin-top: 8px; font-size: 14px; color: %s; text-align: center; min-height: 20px;">%s</div>' % (PINK if error else MUTED, sub)
    head = ('<div style="padding-top: 120px;">%s<div style="margin-top: 20px; font-size: 22px; font-weight: 700; text-align: center;">%s</div>%s'
            '<div style="margin-top: 24px;">%s</div></div>' % (logo, title, sub_html, dots(4, filled, error=error)))
    link_html = ('<div style="text-align: center; margin-top: 16px; height: 48px; line-height: 48px; font-size: 15px; font-weight: 600; color: %s;">%s</div>' % (VIOLET, link)) if link else ''
    pad = '<div style="position: absolute; left: 24px; right: 24px; bottom: 32px;">%s%s</div>' % (pin_keypad(finger, rounded), link_html)
    return head + pad


def lock_a():
    return lock_screen()


def lock_b():
    return lock_screen(rounded=True)


def lock_error():
    return lock_screen(filled=4, sub='PIN не подошёл. Попробуйте ещё раз.', error=True)


def lock_wait():
    return lock_screen(filled=0, sub='Попыток много. Повторите через 30 с.', error=True)


def biometric():
    """The system BiometricPrompt over the lock screen (opens by itself; we only set its title and the «PIN» button)."""
    prompt = ('<div style="position: absolute; left: 0; right: 0; bottom: 0; padding: 28px 24px 24px; border-radius: 28px 28px 0 0; background: #2A2F2A; text-align: center;">'
              '<div style="font-size: 20px; font-weight: 600;">Вход в co-shell</div>'
              '<div style="margin-top: 6px; font-size: 14px; color: %s;">Приложите палец</div>'
              '<div style="margin: 28px auto 20px; width: 72px; height: 72px; border-radius: 50%%; border: 2px solid %s; display: flex; align-items: center; justify-content: center;">%s</div>'
              '<div style="text-align: left; height: 48px; line-height: 48px; font-size: 15px; color: %s; font-weight: 600;">Ввести PIN</div>'
              '<div style="font-size: 11px; color: %s;">Окно системы Android — как в банковских приложениях</div></div>'
              % (MUTED, CYAN, svg(FINGER, CYAN, 40, 1.0), CYAN, MUTED2))
    return lock_screen(filled=0) + '<div style="position: absolute; inset: 0; background: rgba(0,0,0,0.55);"></div>' + prompt


def setup(step):
    if step == 1:
        return lock_screen(filled=1, title='Придумайте PIN', sub='4 цифры. Его спросим при входе.', link='', finger=False)
    return lock_screen(filled=3, title='Повторите PIN', sub='', link='', finger=False)


def forgot():
    d = dialog('Забыли PIN?', 'Подтвердите, что это вы, блокировкой телефона — и придумайте новый PIN. Данные не тронуты.',
               [('Отмена', 'text'), ('Продолжить', 'primary')])
    return lock_screen(filled=0) + '<div style="position: absolute; inset: 0; background: rgba(0,0,0,0.5);"></div>' + d


# ---- hidden sums -------------------------------------------------------------------------------------------------

def hide(text, mode):
    """mode A — «•••• USD» (code stays), B — «••••» without code, C — blur."""
    if mode == 'C':
        return '<span style="filter: blur(7px);">%s</span>' % text
    return HIDDEN


def hero_home(mode, eye_in_card):
    cur = '' if mode == 'B' else ' <span style="font-size: 18px;">USD</span>'
    val = hide('12 480,00', mode) if mode != 'C' else '<span style="filter: blur(9px);">12 480,00</span>'
    sub = (HIDDEN if mode == 'B' else '≈ %s ARS · %s AMD' % (HIDDEN, HIDDEN)) if mode != 'C' else '<span style="filter: blur(6px);">≈ 15 974 400 ARS · 4 836 000 AMD</span>'
    eye = ('<span style="display: inline-flex; vertical-align: middle; margin-left: 4px;">%s</span>' % icon_button(EYE_OFF, 'Показать суммы', HERO_HINT)) if eye_in_card else ''
    flip = '<div style="position: absolute; top: 4px; right: 4px;">%s</div>' % icon_button('refresh', 'Перевернуть', HERO_HINT)
    inner = ('<div style="font-size: 13px; display: flex; align-items: center; min-height: 24px;">Общий баланс%s</div>'
             '<div style="margin-top: 2px; %s font-size: 34px; font-weight: 700; letter-spacing: 2px; color: %s;">%s%s</div>'
             '<div style="margin-top: 4px; %s font-size: 13px;">%s</div>'
             '<div style="margin-top: 10px; font-size: 14px; font-weight: 600;">Все счета ›</div>' % (eye, MONO, LIME, val, cur, MONO, sub))
    return '<div style="margin-top: 8px; position: relative;">%s%s</div>' % (hero_card(inner, pad='18px'), flip)


def hidden_recent(mode):
    """The recent rows: amounts hidden the same way, names and categories stay (they are not money)."""
    items = []
    for t in RECENT[:3]:
        title, sub, amt, cur, kind, icon, color = t[:7]
        if mode == 'C':
            a = '<span style="filter: blur(6px);">%s</span>' % amt
        else:
            a = HIDDEN
        items.append((title, sub, a, '' if mode == 'B' else cur, kind, icon, color, ''))
    return recent_card(items)


def home_hidden(mode='A', eye_in_card=False):
    actions = icon_button('search', 'Поиск', INK, True) + ('' if eye_in_card else icon_button(EYE_OFF, 'Показать суммы', INK, True))
    t1 = tile('Траты за ноябрь', hide('1 240,50', mode) if mode != 'C' else '<span style="filter: blur(6px);">1 240,50</span>', '' if mode == 'B' else 'USD')
    body = hero_home(mode, eye_in_card) + tiles(t1, tile('Без категории', '14', 'операций')) + hidden_recent(mode)
    return tab_screen('Обзор', body, 'home', actions)


def home_visible():
    actions = icon_button('search', 'Поиск', INK, True) + icon_button(EYE, 'Скрыть суммы', INK, True)
    from mockup_screens_fb_flip import total_front, hero
    body = hero(total_front()) + tiles(tile('Траты за ноябрь', '1 240,50', 'USD'), tile('Без категории', '14', 'операций')) + recent_card(RECENT[:3])
    return tab_screen('Обзор', body, 'home', actions)


def flipped_pill():
    """After laying the phone face down: a short notice (CoShellNoticePill) on the way back."""
    pill = ('<div style="position: absolute; left: 24px; right: 24px; bottom: 100px; min-height: 48px; padding: 0 8px 0 6px; border-radius: 999px; background: %s; '
            'border: 1px solid %s; display: flex; align-items: center; gap: 8px;"><span style="width: 36px; height: 36px; border-radius: 50%%; background: %s; display: flex; '
            'align-items: center; justify-content: center;">%s</span><span style="flex-grow: 1; font-size: 14px;">Суммы скрыты. Нажмите «глаз», чтобы показать.</span>'
            '<span style="color: %s;">✕</span></div>' % (CHIP, LINE2, LIME, svg(EYE_OFF, ONLIME, 20), MUTED))
    return home_hidden('A') + pill


# ---- settings: «Ещё → Конфиденциальность» --------------------------------------------------------------------------

def settings(on=True):
    def sw(title, sub, v, enabled=True):
        r = row(title, sub, '', switch(v))
        return r if enabled else r.replace('display: flex; align-items: center; gap: 12px;', 'display: flex; align-items: center; gap: 12px; opacity: 0.38;', 1)
    lock = group([sw('Вход по PIN', 'Спросим при каждом входе' if on else '', on),
                  sw('Отпечаток или лицо', 'Вместо PIN, если телефон умеет', on, on),
                  row('Сменить PIN', '', '', chevron()) if on else ''])
    relock = ('<div style="padding: 12px 16px;"><div style="font-size: 15px; margin-bottom: 8px;">Блокировать после выхода</div>%s'
              '<div style="margin-top: 6px; font-size: 12px; color: %s;">Успеете заглянуть в банк и вернуться без PIN</div></div>'
              % (segmented(['Сразу', '1 мин', '5 мин'], '1 мин'), MUTED))
    sums = group([sw('Скрывать суммы при запуске', 'Показать — «глаз» на «Обзоре»', False),
                  sw('Перевернуть — скрыть', 'Положили экраном вниз — суммы скрылись', True)])
    screen = group([sw('Прятать в недавних', 'Вместо экрана — пустая карточка', on),
                    sw('Запретить скриншоты', 'И запись экрана', False)])
    body = (label('Вход', top=8) + lock + ('<div style="margin-top: 8px;">%s</div>' % group([relock]) if on else '')
            + label('Суммы', top=14) + sums + label('Экран', top=14) + screen)
    return inner_screen('Конфиденциальность', body)


def settings_off():
    return settings(False)


def more_row():
    """Where it lives: «Ещё» → «Приложение» → «Конфиденциальность» (the «Ещё» of poll 2026-11-20 «Б», scrolled down)."""
    import mockup_screens_fb_menu as menu
    return tab_screen('Ещё', menu.new_rest(), 'more', sub='Приложение')


def recents():
    """System «Недавние»: with «Прятать в недавних» the card is blank (palette background + app icon), sums are not visible."""
    def card(inner, name, x, scale=1.0):
        return ('<div style="position: absolute; top: 170px; left: %dpx; width: 250px; transform: scale(%s); transform-origin: top center;">'
                '<div style="display: flex; align-items: center; gap: 8px; margin-bottom: 10px; color: #fff; font-size: 14px;">'
                '<span style="width: 28px; height: 28px; border-radius: 50%%; background: %s;"></span>%s</div>'
                '<div style="height: 500px; border-radius: 24px; overflow: hidden; position: relative;">%s</div></div>' % (x, scale, LIME if name == 'co-shell' else '#4285F4', name, inner))
    ours = ('<div style="position: absolute; inset: 0; background: %s; display: flex; align-items: center; justify-content: center;">'
            '<div style="width: 72px; height: 72px; border-radius: 22px; background: %s; display: flex; align-items: center; justify-content: center;">%s</div></div>' % (BG, CHIP, svg('lock', LIME, 34, 1.1)))
    other = '<div style="position: absolute; inset: 0; background: #E9EEF6;"></div>'
    return ('<div style="position: absolute; inset: 0; background: #1E1F22;"></div>' + card(other, 'Банк', -200, 0.92) + card(ours, 'co-shell', 70)
            + '<div style="position: absolute; bottom: 60px; left: 0; right: 0; text-align: center; color: #fff; font-size: 14px;">Очистить всё</div>')


BOARDS = [  # name, title, fn, issue number
    ('Priv145LockA', '#145 · вход: клавиши как у клавиатуры суммы', lock_a, 145),
    ('Priv145Bio', '#145 · отпечаток: окно системы поверх входа', biometric, 145),
    ('Priv145Error', '#145 · PIN не подошёл', lock_error, 145),
    ('Priv145Wait', '#145 · много попыток: пауза 30 с', lock_wait, 145),
    ('Priv145Set1', '#145 · создать PIN: придумайте', lambda: setup(1), 145),
    ('Priv145Set2', '#145 · создать PIN: повторите', lambda: setup(2), 145),
    ('Priv145Forgot', '#145 · «Забыли PIN?» → блокировка телефона', forgot, 145),
    ('Priv145Shown', '#145 · «Обзор»: суммы видны, «глаз» в шапке', home_visible, 145),
    ('Priv145HideA', '#145 · скрыто: «•••• USD», «глаз» в шапке', lambda: home_hidden('A'), 145),
    ('Priv145Flipped', '#145 · перевернули телефон: плашка «Суммы скрыты»', flipped_pill, 145),
    ('Priv145Settings', '#145 · Ещё → Конфиденциальность', settings, 145),
    ('Priv145SettingsOff', '#145 · Конфиденциальность: PIN выключен', settings_off, 145),
    ('Priv145More', '#145 · «Ещё»: строка «Конфиденциальность»', more_row, 145),
    ('Priv145Recents', '#145 · недавние приложения: пустая карточка', recents, 145),
]
