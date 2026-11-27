"""Boards (#794, owner 2026-11-27): bulk edit of operations in the list's selection mode.

The bar of the selection mode grows one button: «Изменить». «Дублировать» becomes an icon (still only for one chosen row), «Удалить · N» stays.
«Изменить» opens a small sheet: «Категория» (only when every chosen row is the same type: all expenses or all incomes) and «Теги» (added to every chosen row).
One «Отменить» in the snackbar brings every row back. Imported by mockup_screens_feedback.py (FEEDBACK + BOARDS).
"""
from mockup_bricks import *  # noqa: F401,F403
from mockup_screens_main import ops_list
from mockup_screens_fb_tags import tag, tag_row

FEEDBACK = [
    (794, 'Массовая правка операций: «Изменить» в режиме выбора',
     'Удержали строку, отметили несколько — в панели «⧉ · Изменить · Удалить · N». «Изменить» открывает шторку: «Категория» (если все выбранные — одного типа, '
     'иначе строка серая с пояснением) и «Теги» (добавляются ко всем). Применили — снэкбар «Изменено: N · Отменить», одно нажатие возвращает всё. '
     'Счёт массово не меняем: у счетов своя валюта. Переводы и долги в выборе остаются, но «Категория» для них недоступна.'),
]

SHEET_TOP = 330
EXPENSES = (0, 1, 4)  # Продукты, Кафе, Такси — all expenses
MIXED = (0, 2, 4)  # an income among them


def bar(n):
    """The selection bar: copy (an icon, only for one row), «Изменить», «Удалить · N» (`SelectionActions`)."""
    dim = 1 if n == 1 else 0.38
    return ('<div style="position: absolute; left: 32px; right: 32px; bottom: 18px; height: 56px; border-radius: 999px; background: %s; display: flex; align-items: center; '
            'gap: 8px; padding: 0 8px; box-sizing: border-box;">'
            '<span aria-label="Дублировать" style="width: 48px; height: 48px; flex: none; border-radius: 50%%; display: flex; align-items: center; justify-content: center; '
            'opacity: %s;">%s</span>'
            '<span style="flex: 1; height: 48px; border-radius: 999px; display: flex; align-items: center; justify-content: center; gap: 8px; color: %s; font-size: 15px; '
            'font-weight: 600; box-shadow: inset 0 0 0 1px %s;">%sИзменить</span>'
            '<span style="flex: 1; height: 48px; border-radius: 999px; background: %s; color: %s; display: flex; align-items: center; justify-content: center; gap: 8px; '
            'font-size: 15px; font-weight: 600;">%sУдалить · %d</span></div>'
            % (PANEL, dim, svg('copy', INK, 22), INK, LINE2, svg('edit', INK, 20), PINKBG, PINK, svg('trash', PINK, 20), n))


def select_screen(selected):
    base = ops_list(selected)
    i = base.rfind('<div style="position: absolute; left: 32px; right: 32px; bottom: 18px')
    return base[:i] + bar(len(selected))


def many():
    """Three rows chosen: «Дублировать» is dim, «Изменить» is ready."""
    return select_screen(EXPENSES)


def one():
    """One row chosen: all three buttons work."""
    return select_screen((1,))


def dim_under(inner, selected=None):
    return select_screen(selected or EXPENSES) + '<div style="position: absolute; inset: 0; background: rgba(0,0,0,.55);"></div>' + inner


def option(icon, title, sub, enabled=True, trail=None):
    op = '' if enabled else 'opacity: 0.38;'
    return row('<span style="%s">%s</span>' % (op, title), '<span style="%s">%s</span>' % (op, sub) if sub else '',
               lead='<span style="%s">%s</span>' % (op, dot_icon(icon, LIME)), trail=trail if trail is not None else (chevron() if enabled else ''))


def sheet_head(title, sub):
    return ('<div style="display: flex; align-items: center; padding: 8px 20px 4px 20px;"><span style="flex-grow: 1;"><span style="display: block; font-size: 20px; '
            'font-weight: 700;">%s</span><span style="display: block; font-size: 13px; color: %s; margin-top: 2px;">%s</span></span>%s</div>'
            % (title, MUTED, sub, icon_button('close', 'Закрыть')))


def edit_sheet():
    body = sheet_head('Изменить', 'Выбрано операций: 3') + (
        '<div style="padding: 8px 16px 0;">%s</div>' % group([option('folder', 'Категория', 'Одна для всех выбранных'), option('tag', 'Теги', 'Добавятся ко всем выбранным')]))
    return dim_under(sheet(body, SHEET_TOP + 120))


def edit_sheet_mixed():
    body = sheet_head('Изменить', 'Выбрано операций: 3') + (
        '<div style="padding: 8px 16px 0;">%s</div>' % group([option('folder', 'Категория', 'Нужны только расходы или только доходы', enabled=False),
                                                              option('tag', 'Теги', 'Добавятся ко всем выбранным')]))
    return dim_under(sheet(body, SHEET_TOP + 120), MIXED)


def category_sheet():
    def cat(icon, color, name, indent=False, on=False):
        mark = svg('check', LIME, 20, 2.4) if on else ''
        return row(name, lead=('<span style="width: 20px;"></span>' if indent else '') + dot_icon(icon, color, 32 if indent else 40), trail=mark, min_h=52)
    lines = [cat('cart', LIME, 'Продукты', on=True), cat('cafe', WARN, 'Кафе и рестораны'), cat('taxi', YELLOW, 'Транспорт'),
             cat('bus', CYAN, 'Метро и автобус', indent=True), cat('taxi', YELLOW, 'Такси', indent=True), cat('house', PINK, 'Дом'), cat('gift', VIOLET, 'Подарки')]
    body = sheet_head('Категория', 'Для операций: 3') + '<div style="padding: 8px 16px 0;">%s</div>' % group(lines)
    return dim_under(sheet(body, 170))


def tags_sheet():
    tags = tag_row(tag('отпуск'), tag('подарок', False), tag('Аня', False)) + '<div style="height: 8px;"></div>' + tag_row(tag('Испанский', False), tag('ремонт', False))
    footer = ('<div style="position: absolute; left: 16px; right: 16px; bottom: 16px; height: 48px; border-radius: 999px; background: %s; color: %s; display: flex; '
              'align-items: center; justify-content: center; font-size: 15px; font-weight: 700;">Добавить к 3</div>' % (LIME, ONLIME))
    body = sheet_head('Теги', 'Добавятся ко всем выбранным') + '<div style="padding: 12px 16px 0;">%s</div>' % tags + footer
    return dim_under(sheet(body, SHEET_TOP + 40))


def done():
    """After «Добавить»: the list is back to normal, one snackbar with the undo for all rows."""
    return ops_list((), snack=False) + snackbar('Изменено операций: 3')


BOARDS = [  # name, title, fn, issue number
    ('Bulk794Many', 'Выбрано 3: панель «⧉ · Изменить · Удалить · 3»', many, 794),
    ('Bulk794One', 'Выбрана одна: «Дублировать» доступен', one, 794),
    ('Bulk794Sheet', 'Шторка «Изменить»: Категория и Теги', edit_sheet, 794),
    ('Bulk794Mixed', 'Шторка «Изменить»: расходы и доходы вместе — «Категория» недоступна', edit_sheet_mixed, 794),
    ('Bulk794Category', 'Выбор категории для всех выбранных', category_sheet, 794),
    ('Bulk794Tags', 'Теги: «Добавить к 3»', tags_sheet, 794),
    ('Bulk794Done', 'Готово: «Изменено операций: 3 · Отменить»', done, 794),
]
