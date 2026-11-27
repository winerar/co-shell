"""Generated boards: регулярные платежи (#143, ROADMAP этап 7а) — variants for the owner's poll.

Rule on top of a template (#104): every month on day N / every week on a weekday / once a year; the rule reminds
(«Провести» with the amount editable) or posts by itself. Q1 — where upcoming payments show (А «Скоро» on «Обзор»,
Б banner on the due day only, В «Запланировано» on top of «Операции»); Q2 — where the rule is set (А tile «Повтор» in the
template editor → sheet, Б the schedule open inside the editor). Common: the list (24 rules), «Провести», notifications,
edge cases. Owner 2026-11-15: А + А (Б and В dropped). Today — Sunday 15 November 2026. Only the approved set (#367, А «Капсула», «Единый стиль»).

ROW — (key, row title, sticky text) for the «Экраны» page; BOARDS — (name, title, fn, key).
"""
from mockup_plan_bricks import *  # noqa: F401,F403
from mockup_catalog_parts import two_lines, form_section, section_link, day_grid, label_grid, radio_mark, modal_sheet, amount_text, hint, flexrow, col
from mockup_catalog_parts import tile as line_tile
from mockup_catalog_new import status
from mockup_catalog_choice import seg
from mockup_parts import fab  # noqa: E402
from mockup_screens_main import balance_hero, tiles, tile, recent_card, RECENT, ACC, acc_chip, keypad
from mockup_screens_reports import REP_TABS
from mockup_screens_fb_290 import block, frame, head, small_link, detail, grid4

ROW = ('rec', 'Регулярные платежи',
       'Макет #143 — выбрано А + А (владелец 2026-11-15): «Скоро · 7 дней» на «Обзоре», повтор — плиткой в шаблоне. Правило поверх шаблона: каждый месяц N-го / каждую неделю / раз в год. По умолчанию напоминает: «Провести» '
       'открывает шторку с датой платежа, сумму можно поправить (свет, газ — каждый раз разные). «Проводить само» — только с суммой. '
       'Правило синхронизируется; напоминание приходит обоим, проведённое одним исчезает у второго; «само» проводит телефон автора правила.')

# name, category, amount ('' — ask when posting), cur, account, rule, next date, kind ('e' expense, 'i' income), auto
RULES = [
    ('Уборка', 'Дом', '30 000', 'ARS', 'Наличные', 'каждую пятницу', '13 ноя', 'e', False),
    ('Свет', 'Коммуналка', '', 'ARS', 'Galicia', 'каждый месяц, 15-го', 'сегодня', 'e', False),
    ('Испанский', 'Образование', '40 000', 'ARS', 'Galicia', 'каждый вторник', '17 ноя', 'e', False),
    ('Netflix', 'Подписки', '7 990', 'ARS', 'USDT-карта', 'каждый месяц, 18-го', '18 ноя', 'e', True),
    ('Перевод маме', 'Мама', '50 000', 'AMD', 'Ameria', 'каждый месяц, 20-го', '20 ноя', 'e', False),
    ('Газ', 'Коммуналка', '', 'ARS', 'Galicia', 'каждый месяц, 22-го', '22 ноя', 'e', False),
    ('Spotify', 'Подписки', '3 490', 'ARS', 'USDT-карта', 'каждый месяц, 24-го', '24 ноя', 'e', True),
    ('Вода', 'Коммуналка', '', 'ARS', 'Galicia', 'каждый месяц, 25-го', '25 ноя', 'e', False),
    ('Бассейн', 'Спорт', '45 000', 'ARS', 'Galicia', 'каждый месяц, 28-го', '28 ноя', 'e', False),
    ('Зарплата', 'Зарплата', '1 200', 'USD', 'Ameria', 'каждый месяц, 1-го', '1 дек', 'i', False),
    ('Аренда', 'Аренда', '650', 'USD', 'Наличные', 'каждый месяц, 5-го', '5 дек', 'e', False),
    ('Expensas', 'Дом', '180 000', 'ARS', 'Galicia', 'каждый месяц, 10-го', '10 дек', 'e', False),
    ('Интернет', 'Связь', '18 900', 'ARS', 'Galicia', 'каждый месяц, 10-го', '10 дек', 'e', True),
    ('Мобильная связь', 'Связь', '9 500', 'ARS', 'Mercado Pago', 'каждый месяц, 12-го', '12 дек', 'e', True),
    ('Налог ABL', 'Налоги', '12 000', 'ARS', 'Galicia', 'каждый месяц, 14-го', '14 дек', 'e', False),
    ('iCloud', 'Подписки', '2,99', 'USD', 'USDT-карта', 'каждый месяц, 16-го', '16 дек', 'e', True),
    ('Обслуживание карты', 'Банк', '2 500', 'ARS', 'Galicia', 'каждый месяц, 31-го', '31 дек', 'e', True),
    ('Фриланс', 'Зарплата', '', 'USD', 'USDT-карта', 'каждый месяц, 1-го', '1 дек', 'i', False),
    ('Ветеринар — корм', 'Питомцы', '28 000', 'ARS', 'Mercado Pago', 'каждый месяц, 3-го', '3 дек', 'e', False),
    ('Спортзал', 'Спорт', '35 000', 'ARS', 'Galicia', 'каждый месяц, 7-го', '7 дек', 'e', False),
    ('Страховка машины', 'Транспорт', '1 200', 'USD', 'Наличные', 'раз в год, 1 марта', '1 мар', 'e', False),
    ('Домен сайта', 'Подписки', '15', 'USD', 'USDT-карта', 'раз в год, 9 апреля', '9 апр', 'e', True),
    ('ДР Ани — подарок', 'Подарки', '', 'USD', 'Наличные', 'раз в год, 2 июня', '2 июн', 'e', False),
    ('Аванс', 'Зарплата', '600', 'USD', 'Ameria', 'каждый месяц, 15-го', '15 дек', 'i', False),
]
SOON = [RULES[0], RULES[1], RULES[2], RULES[3]]


def rule_icon(cat, size=40):
    return cat_icon(cat, size)


def amt(r, size=15):
    name, cat, a, cur, acc_, rule, nxt, kind, auto = r
    if not a:
        return '<span style="font-size: 12px; color: %s;">сумма при вводе</span>' % MUTED
    return amount(('+' if kind == 'i' else '−') + a, cur, CYAN if kind == 'i' else INK, size)


def when_label(nxt):
    if nxt == '13 ноя':
        return status('A', 'пропущен', 'work')
    if nxt == 'сегодня':
        return status('A', 'сегодня', 'new')
    return '<span style="font-size: 12px; color: %s;">%s</span>' % (MUTED, nxt)


def post_btn(text='Провести'):
    # Approved tonal button (CompNew «Кнопки: оттенки»): the grey capsule was not in the set.
    return ('<span style="height: 48px; padding: 0 16px; border-radius: 999px; background: %s; color: %s; font-size: 14px; font-weight: 600; display: inline-flex; '
            'align-items: center; flex: none;">%s</span>' % (VIOLETPILL, VIOLETINK, text))


def soon_row(r, div=True):
    name, cat, a, cur, acc_, rule, nxt, kind, auto = r
    date = {'13 ноя': 'пт, 13 ноя', '17 ноя': 'вт, 17 ноя', '18 ноя': 'ср, 18 ноя'}.get(nxt, nxt)
    when = when_label(nxt) if nxt in ('сегодня', '13 ноя') else '<span style="font-size: 12px; color: %s;">%s</span>' % (MUTED, date)
    # Code (2026-11-15): the amount sits next to the label/date — on the right it left no room for the name; wraps on a narrow phone.
    line2 = '<span style="display: flex; flex-wrap: wrap; align-items: center; gap: 2px 8px;">%s%s</span>' % (when, amt(r, 12).replace('сумма при вводе', 'при вводе'))
    left = ('<span style="flex-grow: 1; min-width: 0; display: flex; flex-direction: column; align-items: flex-start; gap: 3px;"><span style="font-size: 15px; '
            'white-space: nowrap; overflow: hidden; text-overflow: ellipsis; max-width: 100%%;">%s</span>%s</span>' % (name, line2))
    act = post_btn() if not auto else '<span style="font-size: 12px; color: %s; width: 64px; text-align: right;">само</span>' % MUTED
    return ('<div style="display: flex; align-items: center; gap: 10px; padding: 8px 10px 8px 14px; min-height: 64px; box-sizing: border-box; %s">%s%s%s</div>'
            % ('border-top: 1px solid %s;' % DIV if div else '', rule_icon(cat, 36), left, act))


# ---- Q1: where upcoming payments show ----
def home_a():
    soon = ('<div style="display: flex; align-items: center; margin: 18px 4px 8px;"><span style="flex-grow: 1; font-size: 13px; font-weight: 600; color: %s;">'
            'Скоро · 7 дней</span>%s</div>' % (MUTED, section_link('Все 24 ›')))
    body = (balance_hero() + soon + card(''.join(soon_row(r, i > 0) for i, r in enumerate(SOON)))
            + tiles(tile('Траты за ноябрь', '1 240,50', 'USD'), tile('До конца ноября', '≈ 410', 'USD · 9 платежей')) + recent_card(RECENT[:2]))
    return tab_page('Обзор', body, 'home', REP_TABS, icon_button('search', 'Поиск'))


# ---- Q2: where the rule is set ----
def ed_top(title):
    return ('<div style="display: flex; align-items: center; gap: 4px; height: 60px; padding: 0 8px;">%s<h1 style="margin: 0; font-size: 22px; flex-grow: 1;">%s</h1>%s</div>'
            % (icon_button('close', 'Закрыть'), title, icon_button('trash', 'Удалить шаблон', PINK)))


def ed_page(title, inner, action='Сохранить'):
    return (ed_top(title) + '<div style="position: absolute; left: 16px; right: 16px; top: 60px; bottom: 20px; display: flex; flex-direction: column; gap: 8px;">%s'
            '<div style="flex-grow: 1;"></div>%s</div>' % (inner, button(action, mt=0)))


def tpl_common():
    name = form_section('НАЗВАНИЕ И СЧЁТ', '<div style="display: flex; align-items: center; gap: 10px;">%s%s%s</div>'
                        % (rule_icon('Аренда', 36), two_lines('Аренда', 'Расход · Аренда'), acc_chip(ACC[4])))
    summ = form_section('СУММА', amount_text('650', 'USD', 28), section_link('Пусто — спросим'))
    return seg('A', ['Расход', 'Доход', 'Перевод'], 0) + name + summ


def edit_a():
    rep = '<div style="display: grid; grid-template-columns: 1fr 1fr; gap: 6px;">%s%s%s%s</div>' % (
        detail('refresh', 'Повтор', 'Каждый месяц, 5-го'), detail('bolt', 'Как проводить', 'Напомнить'),
        detail('note', 'Заметка', 'Хозяйке, налом'), detail('calendar', 'Следующий', 'пт, 5 декабря'))
    return ed_page('Шаблон', tpl_common() + block('Детали', rep) + note('Тап по «Повтор» — шторка с днём и частотой. Без повтора шаблон — как сейчас.', 4))


def repeat_body(compact=False):
    freq = seg('A', ['Нет', 'Месяц', 'Неделя', 'Год'], 1)
    days = day_grid(5, None, 31)
    how = ''.join(row(t, s, '', radio_mark(i == 0), min_h=52).replace('{{div}}', 'border-top: 1px solid %s;' % DIV if i else '')
                  for i, (t, s) in enumerate([('Напомнить', 'в день платежа — «Провести», сумму можно поправить'), ('Проводить само', 'только если сумма задана · уведомим с «Отменить»')]))
    end = hint('31-е в коротком месяце — последний день. Повтор с 5 декабря · без конца.')
    return freq, days, how, end


def repeat_sheet():
    freq, days, how, end = repeat_body()
    body = col(freq, label('День месяца', top=4) + days, label('Как проводить', top=4) + card(how), end, gap=8)
    return scrim(edit_a()) + '<div style="position: absolute; left: 0; right: 0; bottom: 0;">%s</div>' % modal_sheet('Повтор', body, 'Аренда · 650 USD', 'Готово')


# ---- common ----
def list_row(r, div=True):
    name, cat, a, cur, acc_, rule, nxt, kind, auto = r
    sub = '%s · %s%s' % (rule, acc_, ' · само' if auto else '')
    return ('<div style="display: flex; align-items: center; gap: 12px; padding: 6px 14px; min-height: 56px; box-sizing: border-box; %s">%s%s'
            '<span style="display: flex; flex-direction: column; align-items: flex-end; gap: 3px;">%s%s</span></div>'
            % ('border-top: 1px solid %s;' % DIV if div else '', rule_icon(cat, 32), two_lines(name, sub), amt(r, 14), when_label(nxt)))


def list_screen():
    hero = hero_card('<div style="font-size: 13px;">До конца ноября · 9 платежей</div><div style="margin-top: 4px; display: flex; align-items: baseline; gap: 10px;">%s'
                     '<span style="font-size: 12px; color: %s;">≈ USD, без «сумма при вводе» · 3</span></div>' % (amount_text('−410', 'USD', 28, LIME), HERO_HINT), extra='margin-top: 4px;')
    flt = '<div style="display: flex; gap: 8px; margin-top: 12px; overflow: hidden;">%s</div>' % ''.join(
        chip(t, t.startswith('Все')) for t in ['Все · 24', 'Расходы · 20', 'Доходы · 4', 'Само · 7'])
    body = (seg('A', ['До конца месяца', 'За год'], 0) + '<div style="height: 12px;"></div>' + search('Найти платёж') + hero + flt
            + label('Ноябрь · по дате', top=14) + card(''.join(list_row(r, i > 0) for i, r in enumerate(RULES[:9])))
            + label('Декабрь', top=14) + card(''.join(list_row(r, i > 0) for i, r in enumerate(RULES[9:20]))))
    return inner_screen('Регулярные платежи', body) + fab('Новый платёж')  # the one add button is the round «+» (#464)


# ---- #771 «Постоянные траты» (poll 2026-11-27 «В · А»): the card in «Отчёты» → «Регулярные» on «За год» -------------------------
FIXED = [('Аренда', 'Дом', '7 800', '650', 'каждый месяц, 1-го'), ('Спортзал', 'Спорт', '480', '40', 'каждый месяц, 5-го'),
         ('Страховка', 'Врачи', '360', '30', 'каждый год, 12 марта'), ('Интернет', 'Связь', '300', '25', 'каждый месяц, 10-го'),
         ('Телефон', 'Связь', '180', '15', 'каждый месяц, 20-го'), ('Кино и сериалы', 'Кино', '144', '12', 'каждый месяц, 3-го'),
         ('Музыка', 'Подписки', '72', '6', 'каждый месяц, 7-го')]


def fixed_row(r, div=True):
    name, cat, year, month, rule = r
    return ('<div style="display: flex; align-items: center; gap: 12px; padding: 6px 14px; min-height: 56px; box-sizing: border-box; %s">%s%s'
            '<span style="display: flex; flex-direction: column; align-items: flex-end; gap: 3px;">%s<span style="font-size: 12px; color: %s;">в год · %s в месяц</span></span></div>'
            % ('border-top: 1px solid %s;' % DIV if div else '', rule_icon(cat, 32), two_lines(name, rule[0].upper() + rule[1:]), amount('−' + year, 'USD', INK, 14), MUTED, month))


def fixed_total(color=INK):
    return ('<div style="display: flex; align-items: baseline; gap: 6px;">%s<span style="font-size: 14px; color: %s;">в год</span></div>'
            '<div style="font-size: 13px; color: %s; margin-top: 2px;">778 USD в месяц · 41 %% ваших трат</div>%s'
            % (amount_text('9 336', 'USD', 26, color), MUTED, MUTED, progress(0.41, 10)))


def fixed_reports():
    head = ('<div style="display: flex; justify-content: space-between; align-items: center; padding: 14px 14px 4px;"><span style="font-size: 15px; font-weight: 600;">Постоянные траты</span>'
            '<span style="font-size: 14px; font-weight: 600; color: %s;">Все 7 ›</span></div>' % VIOLET)
    c = card(head + '<div style="padding: 4px 14px 10px;">%s</div>' % fixed_total() + ''.join(fixed_row(r) for r in FIXED[:3]))
    return tab_screen('Отчёты', label('Ниже «Подушки»', top=6) + c, 'rep')


def fixed_year():
    hero = hero_card('<div style="font-size: 13px;">Расходы за год · 7 платежей</div><div style="margin-top: 4px;">%s</div>' % fixed_total(LIME), extra='margin-top: 12px;')
    body = seg('A', ['До конца месяца', 'За год'], 1) + hero + '<div style="height: 12px;"></div>' + card(''.join(fixed_row(r, i > 0) for i, r in enumerate(FIXED)))
    return inner_screen('Регулярные платежи', body) + fab('Новый платёж')


def cat_pick(name, on=False):
    ring = 'border-radius: 50%%; box-shadow: 0 0 0 2px %s;' % INK if on else ''
    return ('<span style="display: flex; flex-direction: column; align-items: center; gap: 3px; min-width: 0;"><span style="padding: 2px; %s">%s</span>'
            '<span style="font-size: 11px; color: %s; %s">%s</span></span>' % (ring, cat_icon(name, 34), INK if on else INK2, 'font-weight: 700;' if on else '', name))


def post_sheet():
    tag = ('<span style="height: 36px; padding: 0 12px; border-radius: 999px; background: %s; color: %s; font-size: 13px; font-weight: 600; display: flex; '
           'align-items: center; gap: 6px;">%s Свет · 15 ноя</span>' % (VIOLETPILL, VIOLETINK, svg('refresh', VIOLETINK, 16)))
    summ = block('Сумма · списано', '<div style="display: flex; align-items: baseline; justify-content: center; gap: 8px;"><span style="%s font-size: 40px; font-weight: 700; '
                 'color: %s;">48 200</span><span style="%s font-size: 16px; color: %s;">ARS</span></div><div style="text-align: center; font-size: 12px; color: %s;">'
                 'В прошлый раз 41 300 · в среднем 44 000</div>' % (MONO, PINK, MONO, MUTED, MUTED), right=small_link('Пропустить этот раз'))
    cat = block('Категория', grid4([cat_pick('Коммуналка', True)] + [cat_pick(n) for n in ('Дом', 'Аренда', 'Связь')]), right=small_link('Коммуналка ✓'))
    det = block('Детали', '<div style="display: grid; grid-template-columns: 1fr 1fr; gap: 6px;">%s%s</div>'
                % (detail('calendar', 'Дата', 'Сегодня, 15 нояб.'), detail('note', 'Заметка', 'Edenor, ноябрь')))
    return frame(head(extra=tag).replace(acc_chip(ACC[3]), acc_chip(ACC[0])) + summ + cat + det + '<div style="flex-grow: 1;"></div>' + keypad('Провести'))


def notif(title, text, actions, time='9:00'):
    acts = ''.join('<span style="font-size: 13px; font-weight: 600; color: %s;">%s</span>' % (LIME, a) for a in actions)
    return ('<div style="padding: 12px 14px; border-radius: 24px; background: %s; border: 1px solid %s;"><div style="display: flex; align-items: center; gap: 8px; '
            'font-size: 12px; color: %s;">%sCo-Shell · %s</div><div style="margin-top: 6px; font-size: 14px; font-weight: 600;">%s</div>'
            '<div style="font-size: 13px; color: %s;">%s</div><div style="display: flex; gap: 20px; margin-top: 10px;">%s</div></div>'
            % (KEY, LINE, MUTED, svg('wallet', LIME, 14), time, title, INK2, text, acts))


def notifications():
    body = col(label('Напомнить — в 9:00 в день платежа', top=6),
               notif('Сегодня платёж: Свет', 'Galicia · сумма при вводе', ['Провести', 'Завтра']),
               notif('Сегодня платёж: Аренда · 650 USD', 'Наличные · каждый месяц, 5-го', ['Провести 650 USD', 'Изменить сумму']),
               label('Проводить само — уведомление после', top=10),
               notif('Проведено: Netflix · 7 990 ARS', 'USDT-карта · Подписки', ['Отменить', 'Открыть']),
               label('Несколько в один день — одно уведомление', top=10),
               notif('3 платежа сегодня · ≈ 96 USD', 'Интернет, Expensas, Мобильная связь', ['Открыть']),
               note('«Провести 650 USD» проводит без открытия приложения; «Провести» без суммы и «Изменить сумму» открывают шторку. '
                    'Выключатель — «Ещё → Настройки → Напоминания о платежах».'), gap=8)
    return inner_screen('Уведомления', body)


def edge_cases():
    missed = card('<div style="display: flex; align-items: center; gap: 12px;">%s%s</div>'
                  '<div style="display: flex; gap: 8px; margin-top: 10px;">%s%s</div>'
                  % (rule_icon('Дом', 40), two_lines('Уборка — пропущено 2', 'пт 6 и пт 13 ноября · 30 000 ARS каждый'),
                     button('Провести оба', 'primary', None, 0).replace('width: 100%', 'flex: 1'), button('Пропустить', 'outline', None, 0).replace('width: 100%', 'flex: 1')),
                  pad='14px', extra='margin-top: 6px;')
    feb = card(list_row(('Обслуживание карты', 'Банк', '2 500', 'ARS', 'Galicia', 'каждый месяц, 31-го', '28 фев', 'e', True), False))
    other = card(list_row(('Аренда', 'Аренда', '650', 'USD', 'Galicia', 'каждый месяц, 5-го', '5 дек', 'e', False), False))
    gone = message('Счёт «Наличные» в архиве — платёж «Аренда» на паузе. Выберите другой счёт.', 'warn')
    both = message('Свет провела Аня — у вас напоминание исчезло.', 'ok')
    return inner_screen('Особые случаи', col(
        label('Не провели вовремя — копятся, не теряются', top=4), missed,
        label('31-е в коротком месяце — последний день', top=8), feb,
        label('Валюта платежа ≠ валюте счёта — пересчёт по курсу дня, как в шторке', top=8), other,
        label('Счёт в архиве', top=8), gone, label('На двоих', top=8), both,
        note('Пустой список: «Регулярных платежей пока нет. Отметьте «Повтор» в шаблоне: аренда, подписки, зарплата.»'), gap=6))


BOARDS = [  # name, title, fn, row key
    ('Fixed771Reports', '«Отчёты»: карточка «Постоянные траты» (#771)', fixed_reports, 'rec'),
    ('Fixed771Year', '«Регулярные» → «За год» (#771)', fixed_year, 'rec'),
    ('Rec143HomeA', 'Выбрано (А): «Скоро · 7 дней» на «Обзоре»', home_a, 'rec'),
    ('Rec143EditA', 'Выбрано (А): плитка «Повтор» в шаблоне', edit_a, 'rec'),
    ('Rec143RepeatA', 'Выбрано (А): шторка «Повтор»', repeat_sheet, 'rec'),
    ('Rec143List', '«Регулярные платежи» — 24 правила', list_screen, 'rec'),
    ('Rec143Post', '«Провести» — шторка с датой платежа', post_sheet, 'rec'),
    ('Rec143Notify', 'Уведомления', notifications, 'rec'),
    ('Rec143Edge', 'Пропущенные, 31-е, валюта, архив, на двоих', edge_cases, 'rec'),
]
