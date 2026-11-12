"""Generated boards: variants for app feedback that needs a design first (GitHub issues, «Предложения» sheet).

FEEDBACK — one row per issue (number, title, sticky text); BOARDS — (name, title, fn, issue number).
Rows are placed by gen-mockups.py above the proposal variants. Picked (2026-11-11): 154А, 157А.
"""
from mockup_bricks import *  # noqa: F401,F403
from mockup_screens_main import tile, tiles
import mockup_screens_fb_cats as fb_cats
import mockup_screens_fb_search as fb_search
import mockup_screens_fb_nov12 as fb_nov12

FEEDBACK = [
    (154, 'Отзывы #154 + #155 · что где лежит: вкладки, плитки, «Ещё», настройки',
     'Частое — во вкладках (выбираете сами), среднее — плитки «Обзора» (тоже выбираете), редкое — в «Ещё» по разделам. '
     'Экран «Настройки» исчезает: его пункты разъезжаются по разделам. Версия и обновление — отдельной заметной карточкой сверху «Ещё». '
     '«Стереть все данные» — внизу «Резервной копии». Строки 52 dp вместо 60 — меньше прокрутки. Выбран А (2026-11-11).'),
    (157, 'Отзыв #157 · отправленные отзывы: что с ними дальше',
     'Сейчас список отправленных растёт, не открывается и не удаляется. Решение: у каждого отзыва статус задачи '
     '(получен → в работе → сделано в версии N / отложен). Статус пишет обратно в отзыв тот же робот, что делает из отзыва задачу '
     '(раз в час), — бесплатно, без своих серверов. Тап — карточка отзыва; удержание — убрать из списка. Выбран А (2026-11-11).'),
]

VERSION_CARD = ('<div style="margin-top: 4px; display: flex; align-items: center; gap: 12px; padding: 12px 12px 12px 14px; border-radius: 20px; '
                'background: %s; border: 1px solid %s;">%s<span style="flex-grow: 1; display: flex; flex-direction: column; gap: 2px;">'
                '<span style="font-size: 15px; font-weight: 600;">Co-Shell 1.43</span><span style="font-size: 12px; color: %s;">Есть 1.44 · что нового</span></span>'
                '<span style="height: 40px; padding: 0 16px; border-radius: 999px; background: %s; color: %s; font-size: 14px; font-weight: 600; '
                'display: flex; align-items: center;">Обновить</span></div>' % (LIMEPILL, LIME, tile_icon('download', ONLIME, LIME), INK2, LIME, ONLIME))


def r(title, sub, icon, color=LIME, bg=CHIP, trail=None):
    return row(title, sub, tile_icon(icon, color, bg), trail if trail is not None else chevron(), min_h=52)


# ---- #154 + #155 · navigation by frequency ---------------------------------------------------------------------

def more_list():
    money = group([r('Категории', '24 категории · 6 папок', 'folder'), r('Курсы валют', 'обновлены сегодня 09:12', 'rate', CYAN),
                   r('Валюты отчётов', 'основная USD · ещё ARS, AMD', 'rate', VIOLET)])
    data = group([r('Семья', 'Аня и Миша · синхронно', 'user', OK), r('Резервная копия', 'вчера 23:00 · авто', 'archive', CYAN),
                  r('Импорт из Wallet', 'последний — 3 нояб.', 'download', WARN)])
    app = group([r('Внешний вид', 'Вольт · тёмная', 'spark', VIOLET), r('Вкладки и плитки', 'Обзор · Операции · Счета', 'list'),
                 r('Обратная связь', '1 ответ: сделано в 1.44', 'mail', LIME, CHIP, '<span style="width: 8px; height: 8px; border-radius: 50%%; background: %s;"></span>' % LIME)])
    body = VERSION_CARD + label('Деньги', top=14) + money + label('Семья и данные', top=14) + data + label('Приложение', top=14) + app
    return tab_screen('Ещё', body, 'more')






def grip():
    dots = ''.join('<span style="width: 4px; height: 4px; border-radius: 50%%; background: %s;"></span>' % MUTED for _ in range(6))
    return '<span style="display: grid; grid-template-columns: repeat(2, 4px); gap: 3px; padding: 0 4px;">%s</span>' % dots


def tabs_and_tiles():
    tabs = group([r('Обзор', 'вкладка 1', 'home', ONLIME, LIME, grip()), r('Операции', 'вкладка 2', 'list', ONLIME, LIME, grip()),
                  r('Счета', 'вкладка 3', 'wallet', ONLIME, LIME, grip()), r('Ещё', 'всегда последняя', 'more', MUTED, CHIP, '')])
    spare = ''.join(chip(t, False, 'plus') for t in ['Курсы', 'Категории', 'Семья', 'Бюджеты'])
    tl = group([row('Траты за месяц', '', '', switch(True), min_h=48), row('Без категории', '', '', switch(True), min_h=48),
                row('Курсы: USD, USDT', '', '', switch(False), min_h=48), row('Бюджет месяца', 'появится с бюджетами', '', switch(False), min_h=48)])
    body = (label('Вкладки · держите ⠿ и тяните', top=4) + tabs
            + '<div style="margin-top: 10px; display: flex; flex-wrap: wrap; gap: 8px;">%s</div>' % spare
            + label('Плитки на «Обзоре» · до 4', top=16) + tl)
    return inner_screen('Вкладки и плитки', body)


def look():
    modes = segmented(['Система', 'Тёмная', 'Светлая'], 'Тёмная')
    sw = ''.join('<span style="display: flex; flex-direction: column; align-items: center; gap: 6px; font-size: 12px; color: %s;">'
                 '<span style="width: 52px; height: 52px; border-radius: 16px; background: %s; %s"></span>%s</span>'
                 % (INK if n == 'Вольт' else MUTED, c, 'box-shadow: 0 0 0 2px %s, 0 0 0 4px %s;' % (BG, LIME) if n == 'Вольт' else '', n)
                 for n, c in [('Вольт', '#' + 'B8FF2E'), ('Манго', '#' + 'FFB547'), ('Лагуна', '#' + '00F0FF'), ('Сирень', '#' + 'B266FF'), ('Роза', '#' + 'FF5C7F'), ('Мята', '#' + '5CFFB0')])
    pal = '<div style="display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); gap: 14px; padding: 6px 0;">%s</div>' % sw
    rest = group([r('Встряхнуть — новая операция', 'встряхните телефон в приложении', 'phone', LIME, CHIP, switch(True))])
    return inner_screen('Внешний вид', label('Тема', top=4) + modes + label('Палитра', top=16) + pal + label('Ввод', top=16) + rest)


def home_tiles_edit():
    x = '<span style="float: right; color: %s;">✕</span>' % MUTED
    body = (tiles(tile('Траты за ноябрь ' + x, '1 240,50', 'USD'), tile('Без категории ' + x, '14', 'операций'))
            + '<div style="margin-top: 10px; height: 92px; border-radius: 20px; border: 1.5px dashed %s; display: flex; align-items: center; justify-content: center; gap: 8px; color: %s; font-size: 14px;">%s Плитка</div>'
            % (LINE2, MUTED, svg('plus', MUTED, 18)) + button('Готово', 'primary', None, 20)
            + note('Удержание плитки на «Обзоре» — режим правки. То же — в «Ещё → Вкладки и плитки».'))
    return tab_screen('Обзор', body, 'home')


# ---- #157 · sent feedback with status -------------------------------------------------------------------------------

def status_chip(text, kind):
    fg, bg = {'done': (ONLIME, LIME), 'work': (VIOLETINK, VIOLETPILL), 'new': (INK2, CHIP), 'later': (MUTED, CHIP)}[kind]
    return ('<span style="flex: none; height: 24px; padding: 0 10px; border-radius: 999px; background: %s; color: %s; font-size: 12px; font-weight: 600; '
            'display: flex; align-items: center;">%s</span>' % (bg, fg, text))


SENT = [('Ошибка · сегодня', 'Список отправленных растёт, открыть нельзя', 'Получен', 'new'),
        ('Идея · вчера', 'Настройки по категориям, версия заметно', 'В работе', 'work'),
        ('Ошибка · 9 нояб.', 'Цвета категорий слишком похожи', 'Сделано · 1.44', 'done'),
        ('Идея · 9 нояб.', 'Шаблоны платежей', 'В работе', 'work'),
        ('Идея · 8 нояб.', 'Стереть старые операции до даты', 'Отложено', 'later')]


def feedback_status_list():
    rows = [row(t, s, '', status_chip(c, k), min_h=56) for s, t, c, k in SENT]
    body = (button('Написать отзыв', 'primary', 'plus', 4) + label('Мои отзывы · 5', top=18) + group(rows)
            + note('Статус — из задачи разработки, обновляется раз в час. Тап — подробно, удержание — убрать из списка.'))
    return inner_screen('Обратная связь', body)


def step(text, when, on, last=False):
    dot = '<span style="width: 12px; height: 12px; border-radius: 50%%; background: %s; flex: none; margin-top: 4px;"></span>' % (LIME if on else LINE2)
    line = '' if last else '<span style="position: absolute; left: 5px; top: 18px; bottom: -6px; width: 2px; background: %s;"></span>' % (LIME if on else LINE2)
    return ('<div style="position: relative; display: flex; gap: 12px; padding-bottom: 14px;">%s%s<span style="display: flex; flex-direction: column; gap: 2px;">'
            '<span style="font-size: 15px; color: %s;">%s</span><span style="font-size: 12px; color: %s;">%s</span></span></div>' % (line, dot, INK if on else MUTED, text, MUTED, when))


def feedback_detail():
    msg = card('<div style="display: flex; align-items: center; gap: 8px;">%s<span style="font-size: 12px; color: %s;">Ошибка · 9 нояб., 18:21</span></div>'
               '<div style="margin-top: 10px; font-size: 15px; line-height: 1.45;">Иконки категорий слишком похожих цветов. Все цвета должны быть хорошо отличимы. То же для счетов.</div>'
               % (status_chip('Сделано · 1.44', 'done'), MUTED), pad='14px')
    tl = card(step('Получен', '9 нояб., 18:21', True) + step('Стал задачей разработки', '9 нояб., 19:00', True)
              + step('В работе', '11 нояб.', True) + step('Сделано в версии 1.44', '12 нояб. · обновитесь, чтобы увидеть', True, True), pad='16px 14px 2px')
    body = msg + label('Что происходит', top=16) + tl + button('Убрать из списка', 'outline', 'trash', 16)
    return inner_screen('Отзыв', body)




BOARDS = [  # name, title, fn, issue number
    ('Fb154A', 'А · «Ещё»: разделы строками, версия сверху (выбрано)', more_list, 154),
    ('Fb154Tabs', 'А · Вкладки и плитки: выбираете сами', tabs_and_tiles, 154),
    ('Fb154Home', 'А · «Обзор»: правка плиток', home_tiles_edit, 154),
    ('Fb154Look', 'А · Внешний вид (бывшие «Настройки»)', look, 154),
    ('Fb157A', 'А · Мои отзывы со статусом (выбрано)', feedback_status_list, 157),
    ('Fb157A2', 'А · Отзыв подробно: путь до версии', feedback_detail, 157),
] + fb_nov12.BOARDS + fb_cats.BOARDS + fb_search.BOARDS
FEEDBACK = FEEDBACK + fb_nov12.FEEDBACK + fb_cats.FEEDBACK + fb_search.FEEDBACK
