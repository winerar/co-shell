"""Bricks shared by the templates / budgets / reports boards (mockup_screens_templates|budgets|reports.py).

Same «Вольт · тёмная» colours as mockup_bricks.py. Month on every board: ноябрь 2026, today = 11th (day 11 of 30),
so the pace mark sits at 37%.
"""
from mockup_bricks import *  # noqa: F401,F403

PACE = 11 / 30
STATE = {'ok': LIME, 'fast': WARN, 'over': PINK, 'idle': MUTED}

# Family categories used across the boards: name -> (icon, colour)
CAT = {'Дом': ('house', CYAN), 'Продукты': ('cart', LIME), 'Кафе': ('cafe', WARN), 'Транспорт': ('bus', YELLOW),
       'Образование': ('school', '#C79BFF'), 'Подарки': ('gift', '#FF8A5C'), 'Аптека': ('pill', '#FF8FA3'), 'Связь': ('wifi', '#5CFFB0'),
       'Аренда': ('house', CYAN), 'Коммуналка': ('bolt', CYAN), 'Зарплата': ('salary', CYAN)}


def cat_icon(name, size=40):
    ic, col = CAT[name]
    return dot_icon(ic, col, size)


def nav_tabs(active, tabs=(('home', 'home', 'Обзор'), ('ops', 'list', 'Операции'), ('budget', 'budget', 'Бюджеты'), ('more', 'more', 'Ещё'))):
    """Bottom bar with owner-chosen tabs (154А): 4 tabs around the «+»."""
    def tab(key, icon, lbl):
        inner = svg(icon, ONLIME if key == active else MUTED, 24, 1.7)
        if key == active:
            inner = ('<span style="width: 64px; height: 44px; border-radius: 999px; background: %s; display: flex; align-items: center; '
                     'justify-content: center;">%s</span>' % (LIME, inner))
        return '<a href="#" aria-label="%s" style="display: flex; justify-content: center; align-items: center; height: 64px;">%s</a>' % (lbl, inner)
    plus = ('<a href="#" aria-label="Новая операция" style="display: flex; justify-content: center;"><span style="width: 52px; height: 52px; '
            'border-radius: 50%%; background: %s; display: flex; align-items: center; justify-content: center;">%s</span></a>' % (LIME, svg('plus', ONLIME, 26, 2.2)))
    cells = [tab(*t) for t in tabs]
    return ('<nav aria-label="Навигация" style="position: absolute; left: 16px; right: 16px; bottom: 18px; height: 64px; border-radius: 999px; '
            'background: %s; display: grid; grid-template-columns: repeat(5, minmax(0, 1fr)); align-items: center; padding: 0 6px; box-sizing: border-box;">'
            '%s%s%s</nav>' % (BAR, ''.join(cells[:2]), plus, ''.join(cells[2:])))


def tab_page(title, body, active, tabs=None, actions=''):
    top = ('<div style="display: flex; align-items: center; gap: 4px; height: 64px; padding: 0 8px 0 16px;">'
           '<h1 style="margin: 0; font-size: 28px; font-weight: 700; flex-grow: 1;">%s</h1>%s</div>' % (title, actions))
    bar = nav_tabs(active, tabs) if tabs else nav_tabs(active)
    return top + '<div style="padding: 0 16px;">%s</div>' % body + bar


def month_switch(text='Ноябрь 2026', cur=''):
    c = chip(cur + ' ▾') if cur else ''
    return ('<div style="display: flex; align-items: center; gap: 4px; margin: 0 -4px;">%s<span style="flex-grow: 1; text-align: center; font-size: 16px; '
            'font-weight: 600;">%s</span>%s%s</div>' % (icon_button('back', 'Прошлый месяц', MUTED), text, icon_button('chev', 'Следующий месяц', MUTED), c))


def bullet(frac, state='ok', pace=PACE, h=8, mt=8):
    """Bullet graph: filled length = spent / limit, thin mark = where spending should be today."""
    fill = '<span style="position: absolute; left: 0; top: 0; bottom: 0; width: %d%%; border-radius: %dpx; background: %s;"></span>' % (
        round(min(frac, 1) * 100), h // 2, STATE[state])
    mark = ('<span style="position: absolute; left: %d%%; top: -4px; width: 2px; height: %dpx; border-radius: 1px; background: %s;"></span>'
            % (round(pace * 100), h + 8, INK) if pace is not None else '')
    return ('<div style="position: relative; margin-top: %dpx; height: %dpx; border-radius: %dpx; background: %s;">%s%s</div>'
            % (mt, h, h // 2, LINE2, fill, mark))


def caption(text, state='ok'):
    col = {'ok': MUTED, 'fast': WARN, 'over': PINK, 'idle': MUTED}[state]
    return '<div style="margin-top: 6px; font-size: 12px; color: %s;">%s</div>' % (col, text)


def env_row(name, spent, limit, cur, state, cap, sub='', trail=None, div=True):
    """One budget envelope: icon, name, spent / limit, bullet with pace mark, «осталось …» caption."""
    frac = float(spent.replace(' ', '').replace(',', '.')) / float(limit.replace(' ', '').replace(',', '.'))
    s = '<span style="font-size: 12px; color: %s;">%s</span>' % (MUTED, sub) if sub else ''
    t = trail if trail is not None else ''
    return ('<div style="display: flex; gap: 12px; padding: 12px 14px; %s">%s<div style="flex-grow: 1; min-width: 0;">'
            '<div style="display: flex; align-items: baseline; gap: 8px;"><span style="flex-grow: 1; display: flex; flex-direction: column;">'
            '<span style="font-size: 15px;">%s</span>%s</span><span style="%s font-size: 13px; color: %s; white-space: nowrap;">%s'
            '<span style="color: %s;"> / %s %s</span></span>%s</div>%s%s</div></div>'
            % ('border-top: 1px solid %s;' % DIV if div else '', cat_icon(name, 36), name, s, MONO, INK, spent, MUTED, limit, cur, t,
               bullet(frac, state), caption(cap, state)))


def sheet_page(inner, top=300, under=''):
    return scrim(under) + sheet('<div style="padding: 8px 16px 20px;">%s</div>' % inner, top)


def toggle_row(title, sub, on):
    return row(title, sub, '', switch(on), min_h=56)


MORE_CATS = {'Одежда': ('gift', '#FF8A5C'), 'Спорт': ('spark', OK), 'Кино': ('film', '#5CFFB0'), 'Такси': ('taxi', YELLOW), 'Бензин': ('bus', YELLOW),
             'Врачи': ('pill', '#FF8FA3'), 'Подписки': ('phone', '#C79BFF'), 'Питомцы': ('house', WARN), 'Ремонт': ('house', CYAN),
             'Красота': ('spark', '#FF8FA3'), 'Книги': ('school', '#C79BFF'), 'Налоги': ('file', MUTED), 'Банк': ('wallet', MUTED),
             'Игрушки': ('gift', LIME), 'Доставка': ('cart', WARN), 'Мама': ('user', '#FF8A5C')}
CAT.update(MORE_CATS)


def env_compact(name, spent, limit, cur, state, cap='', indent=False, div=True):
    """Dense envelope row for long lists: one line + thin bullet; caption only when something is wrong."""
    frac = float(spent.replace(' ', '')) / float(limit.replace(' ', ''))
    c = caption(cap, state) if cap else ''
    return ('<div style="display: flex; gap: 10px; align-items: center; padding: 8px 14px 8px %dpx; %s">%s<div style="flex-grow: 1; min-width: 0;">'
            '<div style="display: flex; align-items: baseline; gap: 8px;"><span style="flex-grow: 1; font-size: 14px;">%s</span>'
            '<span style="%s font-size: 12px; white-space: nowrap;">%s<span style="color: %s;"> / %s %s</span></span></div>%s%s</div></div>'
            % (34 if indent else 14, 'border-top: 1px solid %s;' % DIV if div else '', cat_icon(name, 28), name, MONO, spent, MUTED, limit, cur,
               bullet(frac, state, h=5, mt=6), c))


def folder_head(name, count, spent, limit, cur, state, open_=True, div=True):
    frac = float(spent.replace(' ', '')) / float(limit.replace(' ', ''))
    return ('<div style="display: flex; gap: 10px; align-items: center; padding: 10px 14px; %s background: %s;">%s<div style="flex-grow: 1;">'
            '<div style="display: flex; align-items: baseline; gap: 8px;"><span style="flex-grow: 1; font-size: 14px; font-weight: 600;">%s '
            '<span style="font-weight: 400; color: %s;">· %d</span></span><span style="%s font-size: 12px;">%s<span style="color: %s;"> / %s %s</span></span></div>%s</div></div>'
            % ('border-top: 1px solid %s;' % DIV if div else '', BAR, svg('down' if open_ else 'chev', MUTED, 18), name, MUTED, count,
               MONO, spent, MUTED, limit, cur, bullet(frac, state, h=5, mt=6)))
