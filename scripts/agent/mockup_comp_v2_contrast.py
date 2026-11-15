"""«Компоненты v2 · контраст» (#385, found by `NonTextContrastTest` #376): variants per failing state, А = now.

Look at it in the light theme too: the chosen neon segment on its track is 1.01 in light Лайм.
"""
from mockup_comp_v2_parts import *  # noqa: F401,F403
from mockup_comp_v2 import wide
from mockup_catalog import section

REC = ' <span style="color: %s;">· рекомендую</span>' % LIME


def tab_pill(frame=False, check=False):
    inner = svg('check' if check else 'list', ONLIME, 24, 1.9)
    st = 'box-shadow: inset 0 0 0 2px %s;' % INK if frame else ''
    tabs = svg('home', MUTED, 24, 1.7) + '<span style="width: 64px; height: 44px; border-radius: 999px; background: %s; display: flex; align-items: center; justify-content: center; %s">%s</span>' % (LIME, st, inner) + svg('wallet', MUTED, 24, 1.7)
    return '<div style="height: 64px; border-radius: 999px; background: %s; display: flex; align-items: center; justify-content: space-around;">%s</div>' % (BAR, tabs)


def seg_check(labels, sel, tone='choice'):
    return seg_v2(labels, sel, tone).replace('>%s</span></span>' % labels[sel], '>%s%s</span></span>' % (svg('check', ONLIME if tone == 'choice' else VIOLETINK, 16, 2.6).replace('<svg ', '<svg style="vertical-align: -3px; margin-right: 4px;" '), labels[sel]), 1)


def selected(v):
    if v == 'A':
        return col(seg_v2(['Расход', 'Доход', 'Перевод'], 0), seg_v2(['По типу', 'По валюте'], 0, 'view'), tab_pill(), day_grid(15, days=21))
    if v == 'B':
        return col(seg_v2(['Расход', 'Доход', 'Перевод'], 0, mark=True), seg_v2(['По типу', 'По валюте'], 0, 'view', mark=True), tab_pill(True),
                   day_grid(15, days=21).replace('font-weight: 700;', 'font-weight: 700; box-shadow: inset 0 0 0 2px %s;' % INK))
    return col(seg_check(['Расход', 'Доход', 'Перевод'], 0), seg_check(['По типу', 'По валюте'], 0, 'view'), tab_pill(check=True), day_grid(15, days=21))


def marks(frame):
    return flexrow(check_mark('off', frame), check_mark('on'), radio_mark(False, frame), radio_mark(True), chip('Еда').replace(LINE2, frame), chip('Счёт ▾').replace(LINE2, frame), gap=14)


def field_v(v):
    f = field('Название', 'Galicia · карта').replace('margin-top: 12px;', '', 1)
    if v == 'B':
        f = f.replace('border: 1px solid %s;' % LINE2, 'border: none; border-bottom: 2px solid %s; border-radius: 12px 12px 0 0;' % OUTLINE)
    elif v == 'C':
        f = f.replace('border: 1px solid %s;' % LINE2, 'border: 1px solid %s;' % OUTLINE)
    empty_ = f.replace('Galicia · карта', '<span style="color: %s;">Например, «Наличные»</span>' % MUTED2)
    return card(col(f, empty_), pad='12px')


def tags_v(v):
    ink = {'A': None, 'B': INK, 'C': None}[v]
    chips, pills = [], []
    for n, c in TAGS[:4]:
        k = ink or ('color-mix(in srgb, %s 55%%, %s)' % (c, INK) if v == 'C' else None)
        chips.append(tag_chip(n.split()[0], c, True, ink=k))
        pills.append(tag_pill(n.split()[0], c, k))
    return col(flexrow(*chips), flexrow(*pills))


def problem(title, why, variants, span=3):
    cells = ''.join('<div style="display: flex; flex-direction: column; gap: 10px;"><div style="font-size: 14px; font-weight: 600;">%s</div>%s</div>' % (t, d) for t, d in variants)
    return section(title, '<div style="display: grid; grid-template-columns: repeat(%d, minmax(0, 1fr)); gap: 20px;">%s</div>' % (len(variants), cells), why, span)


def contrast():
    cards = [
        problem('1 · Выбранное видно только оттенком', 'Сегмент Choice в светлой теме 1.01–3 (13 из 28), сегмент View 1.0–1.8 (28 из 28), пилюля вкладки 1.02 (14 из 28), выбранный день 1.18 (12 из 28). '
                'Нужно ≥ 3:1 (WCAG 1.4.11). Б — рамка цветом текста + жирный; В — галочка у выбранного.',
                [('А · сейчас', selected('A')), ('Б · рамка + жирный' + REC, selected('B')), ('В · галочка', selected('C'))]),
        problem('2 · Пустой флажок, радио, невыбранный чип', 'Рамка цвета линии — 1.3–1.95 в тёмных темах. Б — рамка цветом «outline» (≥ 3 во всех палитрах).',
                [('А · сейчас', marks(LINE2)), ('Б · outline' + REC, marks(OUTLINE))]),
        problem('3 · Граница поля', 'Заливка и рамка поля на карточке — 1.07–1.7: поле узнаётся только по подписи. Б — линия снизу, В — рамка outline.',
                [('А · сейчас', field_v('A')), ('Б · линия снизу', field_v('B')), ('В · рамка outline' + REC, field_v('C'))]),
        problem('4 · Подпись выбранного тега', 'Подпись цветом тега на его же заливке: 381 из 1008 сочетаний < 4.5 (минимум 3.54). Б — подпись цветом текста, цвет остаётся в точке и заливке; '
                'В — цвет тега, сдвинутый к цвету текста (темнее в светлой, светлее в тёмной).',
                [('А · сейчас', tags_v('A')), ('Б · подпись цветом текста' + REC, tags_v('B')), ('В · оттенок к тексту', tags_v('C'))]),
    ]
    return wide('Компоненты v2 · контраст', 'Что не дотягивает до WCAG в части из 14 палитр × 2 тем (#385, проверка #376). Смотрите и в светлой теме — там проблема 1 видна сразу. '
                'Выбранный вариант станет правилом компонента во всех палитрах.', cards, 1360, cols=3)


BOARDS = [('CompV2Contrast', contrast)]
