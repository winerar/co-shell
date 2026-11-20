"""Proposal boards: #509 — change the type of a saved operation (feedback: «выбран не тот тип, а сумма уже введена»).

Today the sheet of a saved operation (board `EntryEdit`) has no «Расход · Доход · Перевод»: only «Дублировать» and «Удалить».
Variants: А — the type switch in the top row next to the two icons; Б — a chip «Расход ▾» opens a small choice sheet;
В — the full type row on top, as for a new operation, icons in a row under it. Poll `docs/design/poll-509.json`.
Imported by mockup_screens_feedback.py; after the owner's «ок» the winner moves to `mockup_screens_entry.edit()`.
"""
from mockup_bricks import *  # noqa: F401,F403
from mockup_screens_fb_modals import sheet_head
import mockup_screens_entry as e

TYPES = e.TYPES

FEEDBACK = [
    (509, 'Сменить тип сохранённой операции · опрос 2026-11-20',
     'У сохранённой операции нет «Расход · Доход · Перевод»: ошиблись типом — только удалить и ввести заново. '
     'Варианты: А — переключатель в верхней строке рядом с «Дублировать» и «Удалить»; Б — метка «Расход ▾» открывает выбор; '
     'В — строка типа сверху, как у новой операции. Сумма и счёт остаются; категория — если подходит к новому типу.'),
]


def seg(cur, short=False):
    """CoShellSegmented without the camera and «Шаблоны»: those are for a new operation only."""
    def cell(k, name, _):
        on = k == cur
        style = ('background: %s; color: %s; font-weight: 700; box-shadow: inset 0 0 0 2px %s;' % (LIME, ONLIME, ONLIME) if on
                 else 'color: %s;' % MUTED)
        return ('<span style="flex: 1; height: 40px; border-radius: 999px; %s font-size: %s; display: flex; align-items: center; '
                'justify-content: center;">%s</span>' % (style, '13px' if short else '14px', name))
    return ('<span style="flex-grow: 1; min-width: 0; height: 48px; border-radius: 999px; background: %s; display: flex; align-items: center; '
            'padding: 4px; gap: 2px;">%s</span>' % (BAR, ''.join(cell(*x) for x in TYPES)))


def icons():
    return icon_button('copy', 'Дублировать', INK) + icon_button('trash', 'Удалить', PINK)


def row(*parts):
    return '<div style="display: flex; align-items: center; gap: 4px;">%s</div>' % ''.join(parts)


def body(cur='exp'):
    """The same cards as `EntryEdit`; a transfer gets «Куда» and loses the category."""
    if cur == 'tr':
        return [e.ribbon(0, 'Откуда') + e.ribbon(2, 'Куда'), e.amount_card('32 000', 'ARS', INK, links=''),
                e.details('10 нояб.', 'Coto, на неделю', refund=False)]
    color = PINK if cur == 'exp' else CYAN
    return [e.ribbon(0), e.amount_card('32 000', 'ARS', color), e.categories('Продукты' if cur == 'exp' else None),
            e.details('10 нояб.', 'Coto, на неделю', refund=cur == 'exp', save_as=True)]


def edit_a(cur='exp'):
    top, *cards = body(cur)
    return e.sheet(row(seg(cur, short=True), icons()) + top, *cards)


def chip(cur):
    name, color = {k: (n, c) for k, n, c in TYPES}[cur]
    return ('<span style="height: 40px; padding: 0 14px; border-radius: 999px; background: %s; border: 1px solid %s; display: inline-flex; '
            'align-items: center; gap: 6px; font-size: 14px; font-weight: 600; color: %s;">%s<span style="color: %s;">▾</span></span>'
            % (CHIP, LINE2, color, name, MUTED))


def edit_b(cur='exp'):
    top, *cards = body(cur)
    return e.sheet(row(chip(cur), '<span style="flex-grow: 1;"></span>', icons()) + top, *cards)


def edit_b_pick():
    lines = ''.join(
        '<div style="display: flex; align-items: center; height: 52px; border-bottom: 1px solid %s;"><span style="flex-grow: 1; font-size: 16px; %s">%s</span>%s</div>'
        % (LINE, 'font-weight: 700;' if k == 'exp' else '', n, '<span style="color: %s; font-size: 18px;">✓</span>' % LIME if k == 'exp' else '')
        for k, n, _ in TYPES)
    hint = ('<div style="margin-top: 12px; font-size: 13px; color: %s;">Сумма, счёт и дата останутся. Категорию выберете заново, если она не подходит.</div>' % MUTED)
    return e.sheet(row(chip('exp'), '<span style="flex-grow: 1;"></span>', icons()) + body()[0], *body()[1:]) + \
        scrim() + sheet(sheet_head('Тип операции') + '<div style="padding: 0 20px;">%s%s</div>' % (lines, hint), 520)


def edit_c(cur='exp'):
    top, *cards = body(cur)
    return e.sheet(row(seg(cur)) + row('<span style="flex-grow: 1;"></span>', icons()) + top, *cards)


BOARDS = [  # name, title, fn, issue number
    ('Fb509A', '#509 · А: тип в верхней строке рядом с «Дублировать» и «Удалить»', edit_a, 509),
    ('Fb509AIncome', '#509 · А: переключили на «Доход» — категория сброшена, сумма та же', lambda: edit_a('inc'), 509),
    ('Fb509ATransfer', '#509 · А: переключили на «Перевод» — появилась «Куда», категории нет', lambda: edit_a('tr'), 509),
    ('Fb509B', '#509 · Б: метка «Расход ▾» вместо переключателя', edit_b, 509),
    ('Fb509BPick', '#509 · Б: нажали метку — выбор типа', edit_b_pick, 509),
    ('Fb509C', '#509 · В: строка типа сверху, как у новой операции', edit_c, 509),
]
