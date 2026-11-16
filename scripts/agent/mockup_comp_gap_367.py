"""#367 proposals: look-alikes for the screens' hand-drawn bits that have no approved component yet.

Boards Gap1…Gap6, variants side by side (А «Капсула»-based, Б an alternative), «сейчас» = what the code draws today.
NOT an approved set: lives outside mockup_catalog*.py until the owner's «ок» (components-approved.md); then the winner
moves into a mockup_catalog_* board and the component into core:designsystem.
"""
from mockup_catalog_kit import *  # noqa: F401,F403
from mockup_catalog_style import u_chip, u_label, u_holder, u_card, u_field, u_bar, u_icon_btn, tagged, u_check
from mockup_bricks import board

W = 640
VIOLETC = '#B266FF'


def tag(letter, title, rec=False):
    return ('<div style="display: flex; align-items: center; gap: 8px; margin: 2px 0 8px;"><span style="width: 24px; height: 24px; border-radius: 50%%; background: %s; color: %s; '
            'font-size: 13px; font-weight: 700; display: flex; align-items: center; justify-content: center;">%s</span><b style="font-size: 14px;">%s</b>%s</div>'
            % (LIME if rec else CHIP, ONLIME if rec else INK, letter, title,
               '<span style="font-size: 11px; color: %s;">рекомендую</span>' % LIME if rec else ''))


def variant(letter, title, demo, note, rec=False):
    return ('<div style="padding: 14px 16px; border-radius: 24px; background: %s; border: 1px solid %s;">%s%s<div style="margin-top: 10px; font-size: 11.5px; line-height: 1.5; color: %s;">%s</div></div>'
            % (CARD, LINE, tag(letter, title, rec), demo, MUTED, note))


def now(demo, note):
    return ('<div style="padding: 14px 16px; border-radius: 24px; border: 1px dashed %s;">%s<div style="margin: 0 0 8px; font-size: 12px; color: %s;">Сейчас в коде</div>%s'
            '<div style="margin-top: 10px; font-size: 11.5px; line-height: 1.5; color: %s;">%s</div></div>' % (LINE2, '', MUTED, demo, MUTED, note))


def page(title, lead, *blocks):
    return ('<div style="padding: 32px 36px;"><h1 style="margin: 0; font-size: 26px;">%s</h1><p style="margin: 8px 0 20px; max-width: 560px; font-size: 14px; line-height: 1.5; color: %s;">%s</p>'
            '<div style="display: flex; flex-direction: column; gap: 14px;">%s</div></div>' % (title, MUTED, lead, ''.join(blocks)))


def rows_card(rows, r=24):
    inner = ''.join('<div style="%s">%s</div>' % ('border-top: 1px solid %s;' % LINE2 if i else '', x) for i, x in enumerate(rows))
    return '<div style="border-radius: %dpx; background: %s; border: 1px solid %s; overflow: hidden;">%s</div>' % (r, CARD, LINE, inner)


def line(lead, title, sub, trail='', sub_color=MUTED, pad='0 16px', h=64):
    t = ('<span style="flex-grow: 1; min-width: 0; display: flex; flex-direction: column;"><span style="font-size: 15px; %s">%s</span>'
         '<span style="font-size: 12px; color: %s;">%s</span></span>' % (MONO, title, sub_color, sub))
    return '<div style="display: flex; align-items: center; gap: 12px; min-height: %dpx; padding: %s;">%s%s%s</div>' % (h, pad, lead, t, trail)


def amount(text, bold=True):
    return '<span style="%s font-size: 17px; font-weight: %d;">%s</span>' % (MONO, 700 if bold else 500, text)


# ---- 1. Код валюты в строке курсов (CurrentRateRow) ----
RATES = [('AMD', '1 USD = 387,15 AMD', 'на 9 ноября', '38 715'), ('ARS', '1 USD = 1 450 ARS', 'вручную', '145 000'), ('RUB', 'RUB', 'Курса пока нет', '')]


def g1():
    def card_of(lead_fn, with_sub_label=False):
        rows = []
        for code, t, s, a in RATES:
            sub = s if code != 'RUB' else '<span style="color: %s;">%s</span>' % (WARN, s)
            rows.append(line(lead_fn(code), t, sub, amount(a) if a else ''))
        return rows_card(rows)
    cur = lambda c: ('<span style="width: 52px; height: 32px; flex: none; border-radius: 12px; background: %s; display: flex; align-items: center; justify-content: center; '
                     '%s font-size: 11px; font-weight: 700; color: %s;">%s</span>' % (CHIP, MONO, WARN, c))
    a = lambda c: u_label(c, WARN, lead='')
    b = lambda c: ('<span style="width: 40px; height: 40px; flex: none; border-radius: 50%%; background: %s; display: flex; align-items: center; justify-content: center; '
                   '%s font-size: 11px; font-weight: 700; color: %s;">%s</span>' % (mix(WARN, 18), MONO, INK, c))
    v = lambda c: '<span style="width: 0;"></span>'
    return page('1 · Код валюты в строке курсов', 'Слева в строке — код валюты в плитке. Её нет в утверждённом наборе: заменить чем-то из набора или нарисовать новую.',
                now(card_of(cur), 'Плитка 52×32, радиус 12, код янтарным. Нигде больше не встречается.'),
                variant('А', 'Метка 22: код в капсуле тоном 18 %', card_of(a), 'Существующая метка (Style2): без нового компонента, `CoShellStatusChip`-подобная. Код — цветом текста, янтарь — только тон подложки.', rec=True),
                variant('Б', 'Круглый значок-код 40', card_of(b), 'Как значок категории (Style3): круг 40, тон 18 %, код 11 жирным. Нужен новый компонент `CoShellCodeBadge` — ровнее с остальными строками списков.'),
                variant('В', 'Без значка', card_of(v), 'Код и так в первой строке («1 USD = … AMD»). Проще всего; но строка начинается с текста, а не с метки.'))


# ---- 2. Ручной курс (ManualRateRow) ----
def g2():
    def one(frame, bar, label):
        left = '<span style="width: 4px; align-self: stretch; border-radius: 2px; background: %s; margin: 0 4px 0 0;"></span>' % VIOLETC if bar else ''
        sub = u_label('вручную', VIOLETC) + '<span style="font-size: 12px; color: %s; margin-left: 8px;">с 5 ноября</span>' % MUTED if label else '<span style="font-size: 12px; color: %s;">вручную · с 5 ноября</span>' % MUTED
        inner = ('<div style="display: flex; align-items: center; gap: 10px; min-height: 64px; padding: 0 8px 0 16px;">%s<span style="flex-grow: 1;"><div style="%s font-size: 15px;">1 USD = 1 450 ARS</div>'
                 '<div style="margin-top: 3px; display: flex; align-items: center;">%s</div></span>%s</div>' % (left, MONO, sub, u_icon_btn('trash')))
        return '<div style="border-radius: 24px; background: %s; %s">%s</div>' % (mix(VIOLETC, 8), 'box-shadow: inset 0 0 0 1px %s;' % mix(VIOLETC, 55) if frame else 'border: 1px solid %s;' % LINE, inner)
    return page('2 · Ручной курс', 'Курс, который ввели руками, выделен фиолетовой полоской слева и рамкой. Полоски нет в наборе.',
                now(one(True, True, False), 'Полоса 4 dp слева + фиолетовая рамка + лёгкая заливка.'),
                variant('А', 'Метка «вручную» вместо полоски', one(True, False, True), 'Рамка и заливка остаются, полоса уходит; смысл — в метке (Style2, цвет в точке). Новых компонентов нет.', rec=True),
                variant('Б', 'Обычная карточка + метка', one(False, False, True), 'Совсем без фиолетовой рамки и заливки: ручной курс отличает только метка. Список ровный, но выделяется слабее.'),
                variant('В', 'Оставить полосу — новый компонент', one(True, True, False), 'Новый «маркер-полоса» `CoShellMarkerBar` 4 dp: добавить на доску. Решает и другие места, где нужно выделить строку.'))


# ---- 3. Пилюли режима: «Копия · сегодня», «⚡ Испанский ✕» ----
def g3():
    def pill_now(txt, icon=None, x=False):
        return ('<span style="height: 34px; padding: 0 14px; border-radius: 999px; background: %s; color: %s; font-size: 14px; display: inline-flex; align-items: center; gap: 6px;">%s%s%s</span>'
                % (mix(VIOLETC, 24), INK, svg(icon, INK, 18, 2) if icon else '', txt, svg('close', INK, 16, 2) if x else ''))
    def demo_now():
        return flexrow(pill_now('Копия · сегодня'), pill_now('Испанский', 'bolt', True), pill_now('Свет · 15 нояб.', 'refresh', True), gap=10)
    def demo_a():
        return flexrow(u_label('Копия · сегодня', VIOLETC), u_chip('Испанский ✕', False, svg('bolt', INK, 18, 2)), u_chip('Свет · 15 нояб. ✕', False, svg('refresh', INK, 18, 2)), gap=10)
    def demo_b():
        return flexrow(u_label('Копия · сегодня', VIOLETC), u_chip('Испанский ✕', True, svg('bolt', ONLIME, 18, 2)), u_chip('Свет · 15 нояб. ✕', True, svg('refresh', ONLIME, 18, 2)), gap=10)
    def demo_c():
        def m(txt, icon=None, x=False):
            return ('<span style="height: 32px; padding: 0 12px; border-radius: 999px; box-shadow: inset 0 0 0 1px %s; color: %s; font-size: 13px; display: inline-flex; align-items: center; gap: 6px;">%s%s%s</span>'
                    % (VIOLETC, INK, svg(icon, VIOLETC, 16, 2) if icon else dot(VIOLETC, 6), txt, svg('close', MUTED, 14, 2) if x else ''))
        return flexrow(m('Копия · сегодня'), m('Испанский', 'bolt', True), m('Свет · 15 нояб.', 'refresh', True), gap=10)
    return page('3 · Пилюли режима в редакторе операции', 'Когда операцию заполняют из шаблона или копируют, сверху — фиолетовая пилюля. Свой стиль, не похожий на метки и чипы.',
                now(demo_now(), 'Пилюля 34, тон 24 %, иконка и «✕» внутри; шаблон нажимается, «Копия» — просто подпись.'),
                variant('А', 'Копия — метка; шаблон — обычный чип 36', demo_a(), 'Только утверждённое: `CoShellStatusChip`-метка и чип выбора (не выбран). Фиолетовый остаётся в точке метки.', rec=True),
                variant('Б', 'Шаблон — выбранный чип (неон + рамка)', demo_b(), 'Показывает «включено»: шаблон применён, ✕ снимает. Заметнее, но неон конкурирует с главной кнопкой.'),
                variant('В', 'Контурная пилюля 32 — новый компонент', demo_c(), '`CoShellModePill`: контур фиолетовым, точка/иконка; «✕» серым. Остаётся «режим» как отдельная вещь, но по сетке (32, капсула).'))


# ---- 4. Выбор цвета и иконки ----
COLORS = ['#FF6B6B', '#FFB547', '#F7D154', '#91E668', '#4FD1A5', '#4FC3F7', '#6C8CFF', '#B266FF', '#FF7AB8', '#C9D3C2']


def swatch(color, size, chosen=False, ring=True):
    sel = 'box-shadow: 0 0 0 2px %s, 0 0 0 4px %s;' % (CARD, INK) if chosen else ''
    inner = svg('check', '#0B0F0A', int(size * 0.5), 2.6) if chosen else ''
    return '<span style="width: %dpx; height: %dpx; flex: none; border-radius: 50%%; background: %s; display: flex; align-items: center; justify-content: center; %s">%s</span>' % (size, size, color, sel, inner)


def g4():
    def strip_now():
        return flexrow(*[swatch(c, 32, i == 3) for i, c in enumerate(COLORS[:8])], '<span style="color: %s; font-size: 13px; font-weight: 600;">+16</span>' % LIME, gap=8, wrap=False)
    def strip_a():
        return flexrow(*[swatch(c, 40, i == 3) for i, c in enumerate(COLORS[:7])], u_chip('+17', False), gap=6, wrap=False)
    def grid_b():
        return ('<div style="display: grid; grid-template-columns: repeat(6, 1fr); gap: 10px; justify-items: center;">%s</div>'
                % ''.join(swatch(c, 44, i == 3) for i, c in enumerate(COLORS[:6] + COLORS[:6])))
    def icons(cell):
        names = ['cart', 'cafe', 'taxi', 'house', 'gift', 'pill']
        def one(n, on):
            if cell == 'A':
                return ('<span style="width: 44px; height: 44px; border-radius: 50%%; display: flex; align-items: center; justify-content: center; %s">%s</span>'
                        % ('background: %s; box-shadow: inset 0 0 0 2px %s;' % (LIME, INK) if on else 'background: %s;' % CHIP, svg(n, ONLIME if on else INK2, 22, 1.9)))
            return ('<span style="width: 44px; height: 44px; border-radius: 14px; display: flex; align-items: center; justify-content: center; %s">%s</span>'
                    % ('background: %s; box-shadow: inset 0 0 0 2px %s;' % (mix(LIME, 22), LIME) if on else 'box-shadow: inset 0 0 0 1px %s;' % LINE2, svg(n, INK if on else INK2, 22, 1.9)))
        return flexrow(*[one(n, i == 1) for i, n in enumerate(names)], gap=8, wrap=False)
    return page('4 · Выбор цвета и иконки', 'Ленты цветов и иконок в формах счёта, категории, шаблона. Сейчас свои круги с рамками. Новые компоненты `CoShellColorStrip` и `CoShellIconStrip` уже в плане — выберем вид.',
                now(strip_now(), 'Круги 32, выбранный — кольцо из двух рамок + галочка; «+16» текстом.'),
                variant('А', 'Лента: круги 40 в одну строку + «+17»', strip_a() + '<div style="height: 14px;"></div>' + icons('A'),
                        'Как остальные выборы (Style1): выбранный — рамка 2 dp цветом текста + галочка. «+17» — чип «Все». Иконки — круги 44 на дорожке, выбранная — неон + рамка.', rec=True),
                variant('Б', 'Сетка 6 в ряд, всё сразу', grid_b() + '<div style="height: 14px;"></div>' + icons('B'),
                        'Без ленты и «Все»: в форме открыта сетка (в шторке — 24 цвета в 4 ряда). Иконки — скруглённые плитки. Форма длиннее, зато без лишнего шага.'))


# ---- 5. Поле суммы в редакторе операции ----
def g5():
    def now_():
        return ('<div style="padding: 8px 4px;"><div style="display: flex; align-items: baseline; justify-content: flex-end; gap: 10px;"><span style="%s font-size: 40px; font-weight: 600; color: %s;">1 250,00</span>'
                '<span style="%s font-size: 18px; color: %s;">USD ▾</span></div><div style="height: 3px; border-radius: 2px; background: %s; margin-top: 6px;"></div></div>' % (MONO, '#FF6B6B', MONO, MUTED, '#FF6B6B'))
    def a():
        return ('<div style="min-height: 72px; padding: 8px 16px; box-sizing: border-box; border-radius: 16px; background: %s; box-shadow: inset 0 0 0 1px %s; display: flex; align-items: center; justify-content: space-between;">'
                '<span style="display: flex; flex-direction: column;"><span style="font-size: 12px; color: %s;">Расход</span><span style="%s font-size: 28px; font-weight: 600; color: %s;">1 250,00</span></span>'
                '%s</div>' % (FIELD, OUTLINE, MUTED, MONO, '#FF6B6B', u_chip('USD ▾', False)))
    def b():
        return ('<div style="padding: 8px 4px;"><div style="display: flex; align-items: center; justify-content: space-between;"><span style="%s font-size: 40px; font-weight: 600; color: %s;">1 250,00</span>%s</div>'
                '%s</div>' % (MONO, '#FF6B6B', u_chip('USD ▾', False), '<div style="margin-top: 10px;">%s</div>' % u_bar(1.0, '#FF6B6B', h=4)))
    return page('5 · Сумма в редакторе операции', 'Крупное число и линия под ним — рисуются прямо в экране. Утверждённое поле суммы (56, рамка) — мелкое для главного действия.',
                now(now_(), 'Число 40, цвет по типу (расход/доход), линия 3 dp под ним, валюта текстом.'),
                variant('А', 'Поле суммы 72: рамка, подпись, число 28, валюта — чип', a(), 'Один язык с остальными полями (рамка outline, радиус 16). Тип — в подписи, цвет — в числе. Число мельче, чем сейчас.', rec=True),
                variant('Б', 'Крупное число 40 + полоса 4 dp (как «полоса» набора)', b(), 'Число остаётся крупным; линия — компонент «полоса» (Style7), валюта — чип. Нужно только разрешение: полоса как подчёркивание.'))


# ---- 6. Выбор счёта в редакторе ----
ACC = [('Galicia · карта', '84 120 ARS'), ('Наличные', '1 250 USD'), ('Вклад', '300 000 RUB')]


def g6():
    def now_():
        t = lambda n, b, on: ('<span style="padding: 8px 14px; border-radius: 16px; background: %s; box-shadow: inset 0 0 0 %dpx %s; display: inline-flex; flex-direction: column;">'
                              '<span style="font-size: 14px;">%s</span><span style="%s font-size: 12px; color: %s;">%s</span></span>' % (CHIP if on else FIELD, 2 if on else 1, INK if on else LINE2, n, MONO, MUTED, b))
        return flexrow(*[t(n, b, i == 0) for i, (n, b) in enumerate(ACC)], gap=8)
    def a():
        return flexrow(*[u_chip(n, i == 0, u_holder('wallet', ONLIME if i == 0 else LIME, 24)) for i, (n, b) in enumerate(ACC)], gap=8)
    def b():
        t = lambda n, bal, on: ('<span style="height: 56px; padding: 0 16px 0 10px; border-radius: 999px; display: inline-flex; align-items: center; gap: 10px; %s">%s'
                                '<span style="display: flex; flex-direction: column;"><span style="font-size: 14px; %s">%s</span><span style="%s font-size: 12px; opacity: 0.75;">%s</span></span></span>'
                                % ('background: %s; color: %s; box-shadow: inset 0 0 0 2px %s;' % (LIME, ONLIME, INK) if on else 'background: %s; color: %s;' % (CHIP, INK2),
                                   u_holder('wallet', ONLIME if on else LIME, 32), 'font-weight: 700;' if on else '', n, MONO, bal))
        return flexrow(*[t(n, bal, i == 0) for i, (n, bal) in enumerate(ACC)], gap=8)
    return page('6 · Выбор счёта в редакторе операции', 'Плитки счетов с остатком — свои рамки и подложки. Нужно привести к набору: чипы выбора (36) или плитка побольше.',
                now(now_(), 'Плитка радиус 16, две строки (имя + остаток), рамка 2 dp у выбранной — не из набора.'),
                variant('А', 'Чип 36 с иконкой счёта: только имя', a(), 'Чистый набор: чип выбора (неон + рамка у выбранного). Остаток не виден — он в подписи экрана после выбора.', rec=True),
                variant('Б', 'Капсула 56: имя + остаток', b(), 'Остаток виден сразу. Новый компонент `CoShellAccountPill` (капсула 56, выбранная — неон + рамка), строится на чипе выбора.'))


FAMILIES = [('Gap1', 'Предложение · код валюты', g1), ('Gap2', 'Предложение · ручной курс', g2), ('Gap3', 'Предложение · пилюли режима', g3),
            ('Gap4', 'Предложение · цвет и иконка', g4), ('Gap5', 'Предложение · сумма', g5), ('Gap6', 'Предложение · выбор счёта', g6)]
HEIGHTS = {'Gap1': 1400, 'Gap2': 870, 'Gap3': 750, 'Gap4': 800, 'Gap5': 720, 'Gap6': 630}  # content + ~30, measured in Chromium


def make(name, title, fn):
    h = HEIGHTS[name]
    return board(title, fn(), height=h).replace('width: 390px; height: %dpx' % h, 'width: %dpx; height: %dpx' % (W, h)).replace(
        '"width":390,"height":%d' % h, '"width":%d,"height":%d' % (W, h))


BOARDS = [(n, (lambda n=n, t=t, f=f: make(n, t, f))) for n, t, f in FAMILIES]
