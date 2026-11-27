"""Smart analytics without AI (#785; poll 2026-11-27 — all «А»): what the app now counts from the family's own history.

- `SmartReports` — «Отчёты»: «Необычное за ноябрь» (#787) and «Оплата в другой валюте» (#786); `SmartConversion` — its «Подробнее».
- `SmartInflation` — «Траты в ARS и в USD» and «Быстрее всего росли» (#788); `SmartRecap` — the story «Не как обычно» (#787).
- `SmartOps` — «Операции»: «Сделать регулярным?» (#789) and «похоже на дубль» (#791); `SmartDuplicate` — its sheet.
- `SmartLimit` — a new limit with «Обычно уходит ~X · Подставить» (#790); `SmartGoal` / `SmartWhatIf` — «Что если» of a goal (#792).
Imported by mockup_screens_forms.py. The poll boards lived in mockup_poll_smart785.py.
"""
from mockup_parts import *  # noqa: F401,F403
import mockup_screens_fb_modals as md
from mockup_catalog_bricks import status_chip
from mockup_screens_fb_tips import tip

CAFE, TAXI, FOOD, HOMEC = '#FFB86B', '#FFE14D', '#7FD6A0', '#6BB8FF'


def line(left, right, strong=False, mt=10):
    return ('<div style="display: flex; justify-content: space-between; align-items: baseline; gap: 12px; margin-top: %dpx; font-size: 15px; %s">'
            '<span>%s</span><span style="%s">%s</span></div>' % (mt, 'font-weight: 700;' if strong else '', left, MONO, right))


def small(text, mt=4):
    return '<div style="margin-top: %dpx; font-size: 13px; color: %s; line-height: 1.35;">%s</div>' % (mt, MUTED, text)


def head(title, right=''):
    return ('<div style="display: flex; justify-content: space-between; align-items: center; font-size: 16px; font-weight: 600;"><span>%s</span>'
            '<span style="font-size: 14px; font-weight: 700; color: %s;">%s</span></div>' % (title, VIOLET, right))


def bar_row(icon, color, name, value, frac, usual, caption):
    meter = ('<div style="position: relative; margin-top: 6px; height: 8px; border-radius: 999px; background: %s;"><div style="width: %d%%; height: 8px; border-radius: 999px; background: %s;"></div>'
             '<div style="position: absolute; left: %d%%; top: -3px; width: 2px; height: 14px; background: %s;"></div></div>' % (KEY, round(frac * 100), color, round(usual * 100), VIOLET))
    return ('<div style="display: flex; gap: 12px; align-items: center; padding: 8px 0;">%s<div style="flex: 1; min-width: 0;">'
            '<div style="display: flex; justify-content: space-between;"><span style="font-size: 15px;">%s</span><span style="%s font-size: 14px;">%s</span></div>%s%s</div></div>'
            % (dot_icon(icon, color, 36), name, MONO, value, meter, small(caption)))


def reports(body):
    period = ('<div style="margin: 4px 0 0; display: flex; justify-content: space-between; align-items: center; font-size: 16px; font-weight: 600;">'
              '<span style="color: %s;">‹</span><span>Ноябрь 2026 · USD</span><span style="color: %s;">›</span></div>' % (MUTED, MUTED))
    return tab_screen('Отчёты', period + body, 'chart')


def unusual():
    rows = bar_row('cafe', CAFE, 'Кафе', '182', 0.9, 0.45, 'примерно в 2 раза больше обычного') + bar_row('taxi', TAXI, 'Такси', '41', 0.2, 0.29, 'меньше обычного на 30 % — хорошо идёте')
    return card(head('Необычное за ноябрь') + rows + small('«Обычно» — в среднем за 3 прошлых месяца к тому же дню, по курсу дня операции.', 8), pad='16px', extra='margin-top: 12px;')


def conversion():
    body = (head('Оплата в другой валюте', 'Подробнее') + line('Покупки в ARS с карты USDT', '1 284 000 ARS') + line('Списано', '1 288,40 USDT') + line('По официальному курсу', '1 265,10 USDT')
            + '<div style="margin-top: 10px; border-top: 1px solid %s;"></div>' % LINE + line('Конвертация обошлась', '23,30 USD', True)
            + small('средний курс 1 USDT = 996,90 ARS · официальный 1 015 · хуже на 1,8 %') + label('По картам', accent=True, top=12)
            + line('USD · Galicia', '−3,9 %', mt=6) + line('USDT · Lemon', '−1,8 %', mt=6))
    return card(body, pad='16px', extra='margin-top: 12px;')


def smart_reports():
    return board('Отчёты: необычное и оплата в другой валюте', reports(unusual() + conversion()))


def smart_conversion():
    rows = (tx('26 ноября · Lemon', '48 500 ARS · 1 USDT = 985,77 ARS · хуже на 2,9 %', '49,20', 'USDT', 'out', 'cart', FOOD)
            + tx('24 ноября · Galicia', '120 000 ARS · 1 USD = 980,39 ARS · хуже на 3,9 %', '122,40', 'USD', 'out', 'cafe', CAFE))
    return board('Оплата в другой валюте: подробнее', md.modal_sheet(reports(conversion()), 'Оплата в другой валюте', rows, 'Курс каждой покупки против официального в тот день', top=360))


def chart():
    pts = lambda vs: ' '.join('%d,%d' % (i * 62, 120 - (v - 95) * 2.2) for i, v in enumerate(vs))  # noqa: E731
    return ('<svg width="310" height="130" style="display: block; margin-top: 12px;"><polyline points="%s" fill="none" stroke="%s" stroke-width="3"/>'
            '<polyline points="%s" fill="none" stroke="%s" stroke-width="3"/></svg>' % (pts([100, 106, 113, 121, 129, 138]), LIME, pts([100, 99, 102, 101, 103, 104]), VIOLET)
            + '<div style="display: flex; justify-content: space-between; font-size: 11px; color: %s;">%s</div>' % (MUTED, ''.join('<span>%s</span>' % m for m in ['июн', 'июл', 'авг', 'сен', 'окт', 'ноя']))
            + '<div style="display: flex; gap: 16px; margin-top: 8px; font-size: 13px; color: %s;"><span><b style="color: %s;">—</b> в ARS</span><span><b style="color: %s;">—</b> в USD</span></div>' % (MUTED, LIME, VIOLET))


def smart_inflation():
    infl = card(head('Траты в ARS и в USD') + '<div style="margin-top: 8px; font-size: 15px; line-height: 1.4;">Траты в ARS выросли на 38 %, в USD выросли на 4 %.</div>'
                + small('Почти весь рост — цены и курс, а не новые траты.') + chart() + small('Средний месяц второй половины против первой, по курсу дня операции.', 8), pad='16px', extra='margin-top: 12px;')
    growth = card(head('Быстрее всего росли · в USD') + line('Кафе', '+11 %') + line('Продукты', '+6 %') + line('Такси', '−8 %'), pad='16px', extra='margin-top: 12px;')
    return board('Отчёты: личная инфляция', reports(infl + growth))


def smart_recap():
    rows = bar_row('cafe', CAFE, 'Кафе', '182', 0.9, 0.45, 'примерно в 2 раза больше обычного') + bar_row('taxi', TAXI, 'Такси', '41', 0.2, 0.29, 'меньше обычного на 30 % — хорошо идёте')
    body = '<div style="font-size: 22px; font-weight: 700;">Не как обычно</div>' + rows + small('Сравнили с тремя прошлыми месяцами.', 12)
    return board('Итоги месяца: не как обычно', inner_screen('Итоги октября', story_card(body, extra='margin-top: 12px;')))


def ops(extra=''):
    dup = '<div style="margin: -6px 0 6px 52px;">%s</div>' % status_chip('похоже на дубль', 'work')
    rows = (label('Сегодня', accent=True) + tx('Продукты', 'Lemon · Аня', '18 300', 'ARS', 'out', 'cart', FOOD) + dup
            + tx('Продукты', 'Lemon', '18 300', 'ARS', 'out', 'cart', FOOD) + dup + tx('Кафе', 'Galicia', '2 450', 'ARS', 'out', 'cafe', CAFE))
    return tab_screen('Операции', extra + rows, 'list')


def smart_ops():
    offer = tip('Похоже на ежемесячный платёж: «Telecentro», ~25 000 ARS, около 5-го числа. Сделать регулярным? Приложение напомнит и покажет его в «Скоро».',
                'Сделать регулярным', 'Не сейчас')
    return board('Операции: регулярный платёж и дубль', ops('<div style="margin-top: 8px;">%s</div>' % offer))


def smart_duplicate():
    body = (small('Две одинаковые записи почти в одно время. Если это одна покупка, лишнюю можно удалить.', 0) + button('Удалить вторую')
            + button('Это разные', 'outline', mt=8) + button('Открыть операцию', 'text', mt=4))
    return board('Похоже на дубль', md.modal_sheet(ops(), 'Похоже на дубль', body, top=470))


def smart_limit():
    usual = ('<div style="display: flex; align-items: center; justify-content: space-between; gap: 8px; margin-top: 8px;"><span style="font-size: 13px; color: %s;">'
             'Обычно уходит ~420 000 ARS в месяц</span>%s</div>' % (MUTED, chip('Подставить')))
    body = (segmented(['Папка', 'Категория'], 1) + field('На что', 'Продукты') + field('В месяц', '0 ARS', mono=True, placeholder=True) + usual
            + note('Лимит — ориентир, а не запрет.', mt=14) + button('Сохранить', mt=20))
    return board('Новый лимит: обычно уходит', inner_screen('Новый лимит', body))


def goal(name, sub, saved, of, frac, whatif=''):
    return (row(name, sub, dot_icon('flag', HOMEC), '<div style="text-align: right;">%s<div style="font-size: 12px; color: %s;">из %s</div></div>' % (amount(saved, 'USD'), MUTED, of))
            + '<div style="padding: 0 16px 10px 68px;">%s</div>' % progress(frac, mt=0) + whatif)


def goals():
    what = ('<div style="padding: 0 16px 12px;">' + small('Если тратить на «Кафе» на 20 % меньше, цель будет на 3 месяца раньше.', 0)
            + '<div style="margin-top: 8px; font-size: 14px; font-weight: 700; color: %s;">Что если</div></div>' % VIOLET)
    return inner_screen('Цели', card(goal('Отпуск', 'к 1 июля 2027 · ≈ 160 USD в месяц', '1 240', '3 000', 0.41, what)
                                     + goal('Ноутбук', 'без срока · осталось 600 USD', '400', '1 000', 0.4), extra='margin-top: 12px;'))


def smart_goal():
    return board('Цели: что если', goals())


def smart_whatif():
    chips = '<div style="display: flex; gap: 8px; flex-wrap: wrap; margin-top: 8px;">%s%s%s</div>' % (chip('Кафе', True), chip('Такси'), chip('Развлечения'))
    body = (small('На какой категории сэкономить', 0) + chips + small('Насколько меньше тратить', 14) + segmented(['10 %', '20 %', '30 %'], 1)
            + line('Сэкономите в месяц', '≈ 90 USD', mt=14) + line('Сейчас', 'Октябрь 2027') + line('С экономией', 'Июль 2027', True)
            + small('Считаем по среднему за 3 месяца. Это прикидка: ничего не сохраняется и не меняется.', 12))
    return board('Что если', md.modal_sheet(goals(), 'Что если', body, 'Отпуск · 3 000 USD', top=330))


BOARDS = [('SmartReports', smart_reports), ('SmartConversion', smart_conversion), ('SmartInflation', smart_inflation), ('SmartRecap', smart_recap),
          ('SmartOps', smart_ops), ('SmartDuplicate', smart_duplicate), ('SmartLimit', smart_limit), ('SmartGoal', smart_goal), ('SmartWhatIf', smart_whatif)]
