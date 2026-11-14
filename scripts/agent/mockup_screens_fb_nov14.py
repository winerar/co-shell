"""Generated boards: feedback of 2026-11-13/14, part 1 — category tree (#330, #315), tips category (#328),
sorting (#338), the «−» key (#291). Variants for the owner's poll.

Imported by mockup_screens_feedback.py (FEEDBACK + BOARDS). Long lists on purpose (owner 2026-11-11):
6 parents / 30 categories, 12 accounts. Entry sheet = the approved #290 layout (fb_290 + fb_quick bricks).
"""
from mockup_bricks import *  # noqa: F401,F403
from mockup_parts import ACCOUNTS, GROUPS, in_group, money
from mockup_screens_fb_cats import FOLDERS, LOOSE, circle, square, cats_screen
from mockup_screens_fb_290 import block, small_link, page, name_line, detail, icon_row, color_row, seg_small, frame, head
from mockup_screens_fb_quick import entry_from, tip_amount, plain_details

FEEDBACK = [
    (330, 'Отзывы #315, #330 · «папка» — странное слово; категории деревом',
     'В данных папка уже категория с подкатегориями (parentId, один уровень), поэтому А — почти только слова и вид. Рекомендую А.'),
    (328, 'Отзыв #328 · категория чаевых при первом использовании', 'Дополняет выбранный А из #322. Рекомендую А: строка категории прямо в шторке чаевых.'),
    (338, 'Отзыв #338 · сортировка счетов и категорий', 'Рекомендую А: режим «Порядок» — перетаскивание + быстрые сортировки.'),
    (291, 'Отзыв #291 · клавиша «−» на клавиатуре суммы', 'Рекомендую А: «−» под «+» в правой колонке, «Готово» — на одну клавишу ниже.'),
]

SORT_ICON = 'M7 4v16M3 8l4-4 4 4M17 20V4M13 16l4 4 4-4'
HANDLE = 'M9 6h.01M15 6h.01M9 12h.01M15 12h.01M9 18h.01M15 18h.01'


def kid_rows(icon, col, cats, line=True):
    out = ''
    for i, c in enumerate(cats):
        tree = ('<span style="position: absolute; left: 33px; top: 0; bottom: %s; border-left: 2px solid %s;"></span>'
                '<span style="position: absolute; left: 33px; top: 50%%; width: 14px; border-top: 2px solid %s;"></span>'
                % ('50%' if i == len(cats) - 1 else '0', LINE2, LINE2)) if line else ''
        out += ('<div style="position: relative; display: flex; align-items: center; gap: 10px; height: 40px; padding-left: 54px;">%s%s'
                '<span style="font-size: 14px; flex-grow: 1;">%s</span><span style="%s font-size: 12px; color: %s; padding-right: 14px;">%s</span></div>'
                % (tree, circle(icon, col, 24), c, MONO, MUTED, '%d опер.' % (14 - i * 3)))
    return out


def parent_row(name, icon, col, n, open_=True):
    lead = '<span style="position: relative;">%s<span style="position: absolute; right: -4px; bottom: -4px; min-width: 18px; height: 18px; padding: 0 4px; box-sizing: border-box; border-radius: 999px; background: %s; border: 2px solid %s; color: %s; font-size: 10px; font-weight: 700; display: flex; align-items: center; justify-content: center;">%d</span></span>' % (circle(icon, col, 40), INK, CARD, BG, n)
    return row('<b>%s</b>' % name, '%d подкатегорий · можно выбрать саму «%s»' % (n, name), lead, svg('down' if open_ else 'chev', MUTED, 18), min_h=58)


def tree_a():
    out = ''
    for i, (name, icon, col, cats) in enumerate(FOLDERS[:4]):
        inner = parent_row(name, icon, col, len(cats), i < 2)
        if i < 2:
            inner += kid_rows(icon, col, cats[:4 if i == 0 else 3]) + '<div style="height: 6px;"></div>'
        out += card(inner, extra='margin-bottom: 8px;')
    out += group([row(n, '', circle(ic, c), chevron(), min_h=50) for n, ic, c in LOOSE[:3]])
    return cats_screen(out)


def tree_a_edit():
    parents = ''.join(chip(n, n == 'Еда', dot='rgb(%s)' % c) for n, _, c, _ in FOLDERS[:4]) + chip('Нет — верхний уровень')
    body = (name_line('Пекарня', 'school', '#E07A4F')
            + block('Тип', seg_small(['Расход', 'Доход'], 'Расход'))
            + block('Входит в', '<div style="display: flex; flex-wrap: wrap; gap: 6px;">%s</div>' % parents, right=small_link('Все 12'))
            + block('Иконка', icon_row()) + block('Цвет', color_row()))
    return page('Категория', body)


def rename_b():
    out = ''
    for i, (name, icon, col, cats) in enumerate(FOLDERS[:4]):
        inner = row('<b>%s</b>' % name, 'группа · %d категорий' % len(cats), square(icon, col, len(cats)), svg('down' if i == 0 else 'chev', MUTED, 18), min_h=60)
        if i == 0:
            inner += '<div style="display: flex; flex-wrap: wrap; gap: 6px; padding: 0 14px 12px 66px;">%s</div>' % ''.join(
                '<span style="display: inline-flex; align-items: center; gap: 6px; height: 32px; padding: 0 10px 0 4px; border-radius: 999px; background: %s; font-size: 13px;">%s%s</span>'
                % (CHIP, circle(icon, col, 24), c) for c in cats)
        out += card(inner, extra='margin-bottom: 8px;')
    out += label('Без группы · 6') + group([row(n, '', circle(ic, c), chevron(), min_h=52) for n, ic, c in LOOSE[:3]])
    return cats_screen(out + note('Только слово: «Папка» → «Группа». Квадрат с числом (#181 В) остаётся.'))


def deep_c():
    crumbs = ('<div style="display: flex; align-items: center; gap: 6px; font-size: 13px; color: %s; margin: 2px 4px 10px;">Все %s Дети %s <b style="color: %s;">Школа</b></div>'
              % (MUTED, svg('chev', MUTED, 14), svg('chev', MUTED, 14), INK))
    sub = [('Учебники', 3), ('Форма', 2), ('Поездки', 4), ('Обеды', 0), ('Взносы', 0), ('Канцелярия', 0)]
    rows = [row(n, '%d подкатегорий' % k if k else 'без подкатегорий', circle('school', '157,123,255', 36), chevron() if k else '', min_h=52) for n, k in sub]
    return cats_screen(crumbs + hero_card('<div style="font-size: 12px; color: %s;">Школа · 3-й уровень</div><div style="%s font-size: 24px; font-weight: 700; color: %s; margin-top: 4px;">−184 500 ARS</div>'
                                          '<div style="font-size: 12px; color: %s;">за ноябрь вместе с подкатегориями</div>' % (HERO_HINT, MONO, LIME, HERO_HINT), pad='12px 14px')
                       + label('Внутри · 6') + group(rows) + note('Любая глубина: Дети › Школа › Учебники › … Сложнее искать и выбирать в шторке.'))


# ---- #328 tips category ---------------------------------------------------------------------------------------------

def tips_sheet(cat_block):
    pct = '<div style="display: flex; gap: 8px;">%s</div>' % (chip('10 %', True) + chip('15 %') + chip('Своя сумма'))
    accs = '<div style="display: flex; gap: 8px; flex-wrap: wrap;">%s</div>' % (chip('Наличные ARS', True, dot='#C9D3C2') + chip('Mercado Pago', dot='#00B1EA') + chip('Galicia', dot='#F26E22'))
    inner = ('<div style="padding: 8px 16px;"><div style="display: flex; align-items: center; height: 44px;"><b style="flex-grow: 1; font-size: 20px;">Чаевые</b>'
             '<span style="color: %s; font-weight: 600;">Готово</span></div>'
             '<div style="%s font-size: 32px; font-weight: 700; color: %s; text-align: center; margin: 2px 0 8px;">1 330 <span style="font-size: 16px; color: %s;">ARS</span></div>'
             '%s%s%s%s</div>' % (LIME, MONO, PINK, MUTED, pct, label('С какого счёта', top=12), accs, cat_block))
    return entry_from(tip_amount(False), cat='Кафе') + scrim() + sheet(inner, 300)


def tips_a():
    cats = [('Чаевые', 'gift', '#FF8A5C'), ('Кафе', 'cafe', WARN), ('Рестораны', 'cafe', '#FFC46B'), ('Доставка', 'taxi', YELLOW)]
    cells = ''.join(chip(n, n == 'Чаевые', dot=c) for n, _, c in cats) + chip('Все 34', dashed=True)
    return tips_sheet(label('Категория чаевых · первый раз — выберите', top=12) + '<div style="display: flex; gap: 8px; flex-wrap: wrap;">%s</div>' % cells
                      + note('Две операции с одним временем: «Кафе» 13 300 ARS и «Чаевые» 1 330 ARS. В следующий раз категория подставится сама.', mt=10))


def tips_b():
    opts = (row('Как у чека', 'сейчас — «Кафе»', svg('copy'), '<span style="width: 20px; height: 20px; border-radius: 50%%; border: 2px solid %s; box-sizing: border-box;"></span>' % LINE2, min_h=52)
            + row('Отдельная «Чаевые»', 'создадим категорию', svg('gift', '#FF8A5C'), '<span style="width: 20px; height: 20px; border-radius: 50%%; border: 6px solid %s; box-sizing: border-box;"></span>' % LIME, min_h=52)
            + row('Выбрать другую…', '', svg('list'), chevron(), min_h=52))
    body = '<div style="margin: 0 -24px;">%s</div>' % opts
    return entry_from(tip_amount(False), cat='Кафе') + scrim() + dialog('Куда записывать чаевые?', 'Спросим один раз, поменять — в «Настройки → Ввод».',
                                                                       [('Отмена', 'text'), ('Дальше', 'primary')], body)


# ---- #338 sorting ---------------------------------------------------------------------------------------------------

def drag_row(a, lifted=False):
    lead = '<span style="display: flex; align-items: center; gap: 8px;">%s%s</span>' % (svg(HANDLE, MUTED2, 20, 2.6), badge(a[1], a[2], a[3], 34, 10))
    r = row(a[0], a[4], lead, money(a[5], a[6]), min_h=52)
    if lifted:
        r = ('<div style="position: relative; z-index: 2; margin: 0 -6px; border-radius: 14px; background: %s; box-shadow: 0 12px 28px rgba(0,0,0,0.55); '
             'border: 1px solid %s; transform: rotate(-1deg);">%s</div>' % (BAR, LIME, r.replace('{{div}}', '')))
    return r


def sort_chips(sel):
    return '<div style="display: flex; gap: 6px; flex-wrap: wrap; margin-bottom: 8px;">%s</div>' % ''.join(chip(t, t == sel) for t in ('Вручную', 'По балансу', 'По имени', 'Частые'))


def reorder_a():
    done = '<span style="height: 36px; padding: 0 14px; border-radius: 999px; background: %s; color: %s; font-weight: 600; font-size: 14px; display: flex; align-items: center;">Готово</span>' % (LIME, ONLIME)
    out = sort_chips('Вручную')
    for g, title, _ in GROUPS[:3]:
        accs = in_group(g)
        out += ('<div style="display: flex; align-items: center; gap: 8px; margin: 12px 4px 6px;">%s<span style="font-size: 13px; font-weight: 600; color: %s; '
                'text-transform: uppercase; letter-spacing: 0.04em;">%s</span><span style="font-size: 12px; color: %s;">%d</span></div>' % (svg(HANDLE, MUTED2, 18, 2.6), LIME, title, MUTED, len(accs)))
        out += group([drag_row(a, a[0] == 'Santander') for a in accs])
    top = ('<div style="display: flex; align-items: center; gap: 8px; height: 64px; padding: 0 16px;"><h1 style="margin: 0; font-size: 22px; font-weight: 700; flex-grow: 1;">Порядок счетов</h1>%s</div>' % done)
    return top + '<div style="padding: 0 16px;">%s%s</div>' % (out, note('Тянуть за ⠿ — счёт или целую группу. Порядок общий для семьи: так же видит Аня.'))


def reorder_a_cats():
    done = '<span style="height: 36px; padding: 0 14px; border-radius: 999px; background: %s; color: %s; font-weight: 600; font-size: 14px; display: flex; align-items: center;">Готово</span>' % (LIME, ONLIME)
    out = sort_chips('Частые')
    for i, (name, icon, col, cats) in enumerate(FOLDERS[:5]):
        lead = '<span style="display: flex; align-items: center; gap: 8px;">%s%s</span>' % (svg(HANDLE, MUTED2, 20, 2.6), circle(icon, col, 34))
        inner = row('<b>%s</b>' % name, '%d подкатегорий · %d опер. за месяц' % (len(cats), 60 - i * 9), lead, svg('down' if i == 0 else 'chev', MUTED, 18), min_h=54)
        if i == 0:
            inner += ''.join(row(c, '', '<span style="display: flex; align-items: center; gap: 8px; padding-left: 28px;">%s%s</span>' % (svg(HANDLE, MUTED2, 18, 2.6), circle(icon, col, 26)), '', min_h=42).replace('{{div}}', '') for c in cats)
        out += card(inner, extra='margin-bottom: 8px;')
    top = ('<div style="display: flex; align-items: center; gap: 8px; height: 64px; padding: 0 16px;"><h1 style="margin: 0; font-size: 22px; font-weight: 700; flex-grow: 1;">Порядок категорий</h1>%s</div>' % done)
    return top + '<div style="padding: 0 16px;">%s%s</div>' % (out, note('«Частые» — по операциям за 30 дней сверху, и в шторке ввода тоже. Тронули ⠿ — станет «Вручную».'))


def sort_b():
    under = tab_screen('Счета', group([row(a[0], a[4], badge(a[1], a[2], a[3], 36), money(a[5], a[6]), min_h=54) for a in ACCOUNTS[:9]]), 'acc', icon_button(SORT_ICON, 'Сортировка'))
    radio = lambda on: ('<span style="width: 20px; height: 20px; border-radius: 50%%; border: %s; box-sizing: border-box;"></span>' % ('6px solid %s' % LIME if on else '2px solid %s' % LINE2))
    opts = ''.join(row(t, s, '', radio(t == 'По балансу'), min_h=52) for t, s in (('По балансу', 'большие сверху, в основной валюте'), ('По имени', 'А → Я'),
                                                                                ('Часто используемые', 'по операциям за 30 дней'), ('По дате создания', 'новые снизу')))
    inner = '<div style="padding: 8px 16px;"><b style="display: block; font-size: 20px; height: 44px; line-height: 44px;">Сортировка счетов</b>%s%s</div>' % (opts, note('Без ручного порядка: только готовые правила.'))
    return under + scrim() + sheet(inner, 470)


# ---- #291 minus key -------------------------------------------------------------------------------------------------

def key(t, bg=KEY, col=INK, sz=22, extra=''):
    return ('<span style="border-radius: 16px; background: %s; color: %s; font-size: %dpx; %s display: flex; align-items: center; justify-content: center; %s">%s</span>'
            % (bg, col, sz, MONO, extra, t))


def expr_amount(neg=False):
    val, col = ('−500', PINK) if neg else ('30 500', PINK)
    expr = '32 000 − 1 500' if not neg else '1 000 − 1 500'
    big = ('<div style="text-align: center; %s font-size: 14px; color: %s;">%s</div><div style="display: flex; align-items: baseline; justify-content: center; gap: 8px;">'
           '<span style="%s font-size: 38px; font-weight: 700; color: %s;">%s</span><span style="%s font-size: 16px; color: %s;">ARS</span></div>' % (MONO, MUTED, expr, MONO, col, val, MONO, MUTED))
    return block('Сумма', big)


def keypad_a():
    ok = '<span style="border-radius: 16px; background: %s; color: %s; font-size: 15px; font-weight: 700; display: flex; align-items: center; justify-content: center; gap: 6px;">%sГотово</span>' % (LIME, ONLIME, svg('check', ONLIME, 22, 2.2))
    keys = (key('7') + key('8') + key('9') + key('⌫', KEY2, INK, 20) + key('4') + key('5') + key('6') + key('+', KEY2, LIME, 26) + key('1') + key('2') + key('3')
            + key('−', KEY2, LIME, 26) + key(',') + key('0') + key('000', KEY, INK, 18) + ok)
    return '<div style="display: grid; grid-template-columns: repeat(4, minmax(0, 1fr)); grid-auto-rows: 54px; gap: 6px;">%s</div>' % keys


def keypad_b():
    ops = ('<div style="display: grid; grid-template-columns: 1fr 1fr 2fr; gap: 6px; margin-bottom: 6px;">%s%s%s</div>'
           % (key('+', KEY2, LIME, 24, 'height: 40px;'), key('−', KEY2, LIME, 24, 'height: 40px;'), key('= 30 500', BAR, INK2, 15, 'height: 40px;')))
    k = (key('7') + key('8') + key('9') + key('⌫', KEY2, INK, 20) + key('4') + key('5') + key('6')
         + '<span style="grid-row: span 3; border-radius: 18px; background: %s; color: %s; font-size: 15px; font-weight: 700; display: flex; flex-direction: column; align-items: center; justify-content: center; gap: 4px;">%sГотово</span>' % (LIME, ONLIME, svg('check', ONLIME, 26, 2.2))
         + key('1') + key('2') + key('3') + key(',') + key('0') + key('000', KEY, INK, 18))
    return ops + '<div style="display: grid; grid-template-columns: repeat(4, minmax(0, 1fr)); grid-auto-rows: 50px; gap: 6px;">%s</div>' % k


def entry_keys(pad, neg=False):
    cats = block('Категория', '<div style="display: flex; gap: 6px;">%s</div>' % (chip('Продукты', True, dot=LIME) + chip('Кафе', dot=WARN) + chip('Все 34', dashed=True)))
    warn = message('Сумма меньше нуля — сохранить нельзя. Это доход? Смените тип вверху.', 'error') if neg else ''
    return frame(head() + expr_amount(neg) + cats + plain_details() + warn + '<div style="flex-grow: 1;"></div>' + pad)


BOARDS = [  # name, title, fn, issue number
    ('Fb330A', 'А: категории деревом — у любой категории подкатегории, слова «папка» нет', tree_a, 330),
    ('Fb330AEdit', 'А: редактор — «Входит в: Еда» вместо «Категория | Папка»', tree_a_edit, 330),
    ('Fb330B', 'Б: только новое слово — «Группа», вид #181 В остаётся', rename_b, 330),
    ('Fb330C', 'В: дерево любой глубины с «хлебными крошками»', deep_c, 330),
    ('Fb328A', 'А: категория чаевых — строкой в шторке, запоминается', tips_a, 328),
    ('Fb328B', 'Б: первый раз — окно «Куда записывать чаевые?»', tips_b, 328),
    ('Fb338A', 'А: режим «Порядок» — перетаскивание счетов и групп + быстрые сортировки', reorder_a, 338),
    ('Fb338ACats', 'А: порядок категорий — родители и подкатегории', reorder_a_cats, 338),
    ('Fb338B', 'Б: только готовые сортировки в шторке', sort_b, 338),
    ('Fb291A', 'А: «−» под «+», «Готово» — одна клавиша', lambda: entry_keys(keypad_a()), 291),
    ('Fb291B', 'Б: строка действий над цифрами: + − и итог', lambda: entry_keys(keypad_b()), 291),
    ('Fb291Neg', 'Общее: итог меньше нуля — красным, сохранить нельзя', lambda: entry_keys(keypad_a(), True), 291),
]
