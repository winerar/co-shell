"""Boards (#598, owner 2026-11-25): «Знакомство» — onboarding stories on «Обзор» for a new install (what each screen is for and
in which order to create things), and «Показать подсказки заново» as an action instead of a row with «›». Poll boards — until «ок».

Help: А — the row turns into an action (↻ on the left, the title in the link colour, no «›»); Б — an outlined button under the switch.
Tour: card «Знакомство с приложением» on top of a new «Обзор» (the same look as «Что нового», #693 «Б»), six stories in the order of creation;
the library in «Помощь» (#598 «А»). Schemes — the same three-layer vectors as `story_art_693.py` (new ones live in TOUR_ART until «ок»).
Imported by mockup_screens_feedback.py (FEEDBACK + BOARDS).
"""
from mockup_plan_bricks import *  # noqa: F401,F403
from mockup_screens_main import balance_hero
from mockup_screens_notes import footer
import mockup_screens_fb_invite as inv
import mockup_screens_fb_setcards as sc
import story_art_693 as ART

FEEDBACK = [
    (598, 'Знакомство на «Обзоре» и кнопка «Показать подсказки заново» · опрос 2026-11-25',
     'Новому человеку — карточка «Знакомство с приложением» вверху «Обзора»: шесть историй, какие экраны для чего и что создавать по порядку '
     '(счёт → операция → категории → лимит → отчёты → семья и копия). «Показать подсказки заново» — действие, а не переход: без «›».'),
]

R, L = ART.rrect, ART.line
TOUR_ART = {
    'categories': [
        ('accent', 'fill', ART.circle(70, 50, 26)),
        ('ink', 'stroke', ART.circle(150, 50, 26) + ' ' + ART.circle(230, 50, 26) + ' ' + ART.circle(70, 130, 26) + ' ' + ART.circle(150, 130, 26)),
        ('ink', 'stroke', 'M230,116 v28 M216,130 h28', 3),
        ('ink', 'stroke', R(140, 40, 20, 18, 4) + ' ' + L((222, 58), (230, 40), (238, 58)) + ' ' + L((60, 122), (80, 138)) + ' ' + ART.circle(150, 130, 8)),
    ],
    'budgets': [
        ('ink', 'stroke', R(30, 30, 240, 20, 10) + ' ' + R(30, 80, 240, 20, 10) + ' ' + R(30, 130, 240, 20, 10)),
        ('accent', 'fill', R(30, 30, 150, 20, 10) + ' ' + R(30, 80, 90, 20, 10) + ' ' + R(30, 130, 200, 20, 10)),
        ('ink', 'stroke', L((210, 20), (210, 160)), 2.5),
    ],
    'family': [
        ('ink', 'stroke', ART.circle(80, 70, 22) + ' M44,140 a36,30 0 0 1 72,0'),
        ('accent', 'fill', ART.circle(160, 70, 22)),
        ('ink', 'stroke', 'M124,140 a36,30 0 0 1 72,0'),
        ('ink', 'stroke', 'M222,112 h44 a14,14 0 0 0 -2,-28 a20,20 0 0 0 -38,4 a12,12 0 0 0 -4,24z', 2.5),
        ('accent', 'stroke', L((244, 104), (244, 124)) + ' ' + L((236, 116), (244, 124), (252, 116)), 3),
    ],
}

STORIES = [
    ('accounts', 'Начните со счёта', 'Карта, наличные или копилка — где лежат деньги. Баланс считается сам: Ещё → Счета.'),
    ('fan', 'Записывайте траты', 'Нажмите «+» — расход, доход или перевод. Удерживайте «+»: QR чека, заметка, шаблон.'),
    ('categories', 'Категории', 'На что ушли деньги. Готовые уже есть, свои добавите в «Ещё → Категории».'),
    ('budgets', 'Лимиты на месяц', 'Сколько готовы тратить на категорию. Видно, сколько осталось. Это ориентир, а не запрет: Ещё → Бюджеты.'),
    ('recap', 'Отчёты и итоги', 'Куда уходят деньги — по категориям, месяцам и в календаре. В начале месяца — «Итоги».'),
    ('family', 'Вместе и в сохранности', 'Позовите близкого — записи соберутся вместе: Ещё → Семья. Резервная копия — в «Ещё → Резервная копия».'),
]


def art(key):
    saved = ART.STORIES
    ART.STORIES = {**saved, **TOUR_ART}
    try:
        return ART.svg(key, INK, LIME, ONLIME)
    finally:
        ART.STORIES = saved


def tour_card():
    words = ('<div style="flex: 1;"><div style="font-size: 12px; color: %s;">Шесть коротких историй</div>'
             '<div style="font-size: 16px; font-weight: 600;">Знакомство с приложением</div></div>' % MUTED)
    return card('<div style="display: flex; align-items: center; gap: 8px;">%s<span style="flex: none;">%s</span>%s</div>'
                % (words, button('Смотреть', 'primary', None, 0), icon_button('close', 'Не сейчас', INK)), pad='8px 4px 8px 16px', extra='margin-top: 8px;')


def steps_row():
    return card(row('Первые шаги', 'Начнём со счёта', '', chevron(), min_h=52), extra='margin-top: 12px;')


def home():
    body = tour_card() + balance_hero('0,00', 'USD', 'Счетов пока нет') + steps_row() + \
        empty('wallet', 'Счетов пока нет. Начните с того, где лежат деньги: карта, наличные, вклад.', 'Добавить счёт')
    return tab_screen('Обзор', body, 'home')


def bars(at):
    return '<div style="display: flex; gap: 4px;">%s</div>' % ''.join(
        '<div style="flex: 1; height: 8px; border-radius: 999px; background: %s; %s"></div>' % (LIME if i <= at else LINE2, 'box-shadow: inset 0 0 0 1px %s;' % LIME if i <= at else '')
        for i in range(len(STORIES)))


def story(at):
    key, title, text = STORIES[at]
    scheme = '<div style="display: flex; justify-content: center; padding: 32px 0 20px;">%s</div>' % art(key)
    inner = scheme + ('<div style="font-size: 13px; color: %s; margin-bottom: 6px;">Шаг %d из %d</div>' % (MUTED, at + 1, len(STORIES))) + \
        '<div style="font-size: 24px; font-weight: 600; line-height: 1.2;">%s</div><div style="margin-top: 12px; font-size: 16px; color: %s; line-height: 1.45;">%s</div>' % (title, MUTED, text)
    last = at == len(STORIES) - 1
    return inner_screen('Знакомство', bars(at) + story_card(inner, pad='16px', extra='margin-top: 12px; min-height: 560px;')) + \
        footer(button('Начать' if last else 'Дальше', 'primary', None, 0))


def reset_action():
    """А: the row is an action — ↻ in the link colour on the left, the title in the link colour, no «›»."""
    return row('Показать подсказки заново', 'Вернёт «Первые шаги» и советы', svg('refresh', CYAN, 22), '', title_color=CYAN, min_h=52)


def help_a():
    smart = sc.section(*sc.RU['smart'][0], group([sc.val(*sc.RU['smart'][1], beta=True), sc.val(*sc.RU['smart'][2], beta=True)]), '')
    rows = group([sc.sw(*sc.RU['help'][1], True), reset_action(), row('Знакомство с приложением', 'Шесть историй: что где и для чего', '', chevron(), min_h=52),
                  inv.a_row(), row('Политика конфиденциальности', 'Что хранится и как удалить данные', '', chevron(), min_h=52)])
    return inner_screen(sc.RU['title'], smart + sc.section(*sc.RU['help'][0], rows, ''))


def help_b():
    """Б: an outlined button under the switch, inside the card; the other rows stay rows with «›»."""
    smart = sc.section(*sc.RU['smart'][0], group([sc.val(*sc.RU['smart'][1], beta=True), sc.val(*sc.RU['smart'][2], beta=True)]), '')
    first = card(sc.sw(*sc.RU['help'][1], True) + '<div style="padding: 0 16px 14px;">%s</div>' % button('Показать подсказки заново', 'outline', 'refresh', 4))
    rest = group([row('Знакомство с приложением', 'Шесть историй: что где и для чего', '', chevron(), min_h=52), inv.a_row(),
                  row('Политика конфиденциальности', 'Что хранится и как удалить данные', '', chevron(), min_h=52)])
    return inner_screen(sc.RU['title'], smart + sc.section(*sc.RU['help'][0], first + '<div style="height: 8px;"></div>' + rest, ''))


BOARDS = [  # name, title, fn, issue number
    ('Help598A', 'А · «Показать подсказки заново» — действие: ↻ и цвет ссылки, без «›»', help_a, 598),
    ('Help598B', 'Б · кнопка-контур под переключателем', help_b, 598),
    ('Tour598Home', '«Обзор» новой установки: карточка «Знакомство с приложением»', home, 598),
] + [('Tour598S%d' % (i + 1), 'Знакомство %d из 6: %s' % (i + 1, STORIES[i][1]), (lambda i=i: story(i)), 598) for i in range(len(STORIES))]
