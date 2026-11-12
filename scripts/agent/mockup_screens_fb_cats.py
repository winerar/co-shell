"""Generated boards: variants for #181 (categories vs folders), #105 (colour distinctness), #173 (swipe conflicts).

Imported by mockup_screens_feedback.py (FEEDBACK + BOARDS). Long lists on purpose (owner 2026-11-11): 6 folders,
30 categories. Palette colours are written as rgb(): mockup_theme.py converts only raw hexes, so swatches stay true.
"""
import json
import os

from mockup_bricks import *  # noqa: F401,F403

FEEDBACK = [
    (181, 'Отзыв #181 · категории и папки различимы с первого взгляда',
     'Правило владельца: разные сущности в одном списке всегда различимы. Предлагаю В: папка — скруглённый квадрат '
     'со значком папки и числом, категория — круг. Форма работает везде, где они встречаются (шторка ввода, лимиты, отчёты), '
     'а список «Категорий» почти не меняется. А — папка карточкой с вложенными строками; Б — папки становятся заголовками групп.'),
    (105, 'Отзыв #105 · различимость цветов категорий и счетов',
     'Замер ΔE2000 (≥ 10 — различимы рядом): в тёмной теме 5 пар-близнецов, в светлой — 33 (тёмные оттенки пастели сливаются: '
     'blue/sky 2.6, amber/honey 3.6). Предлагаю Б: 24 цвета с разносом ≥ 10 в обеих темах, значок — заливка цветом и тёмный рисунок '
     'в обеих темах; новой категории сам подбирается цвет, самый далёкий от соседей. Старые 12 цветов в данных остаются, '
     'показываются ближайшим из 24. В — 16 основных крупно и «Ещё оттенки».'),
    (173, 'Задача #173 · свайп сегментов не спорит со свайпом вкладок',
     'На экранах с сегментами («Операции | Шаблоны», отчёты, «Расходы | Доходы») горизонтальный жест нужен двоим. '
     'Предлагаю А: жест сначала листает сегменты; с крайнего сегмента — дальше в соседнюю нижнюю вкладку. На внутренних экранах («Категории») с края — ничего. '
     'Б — проще, но медленнее: по содержимому всегда листаются нижние вкладки, сегменты — только тапом или по самой пилюле.'),
]

HERE = os.path.dirname(os.path.abspath(__file__))
_PAL = os.path.join(HERE, 'mockup_palette_105.json')  # computed by the ΔE audit (see FEEDBACK 105)

FOLDERS = [  # (name, icon, rgb, categories)
    ('Еда', 'cart', '168,255,62', ['Продукты', 'Кафе', 'Пекарня', 'Доставка', 'Алкоголь']),
    ('Дом', 'house', '43,228,255', ['Аренда', 'Коммуналка', 'Интернет', 'Ремонт']),
    ('Транспорт', 'taxi', '255,212,59', ['Такси', 'Автобус', 'Бензин']),
    ('Дети', 'school', '157,123,255', ['Школа', 'Испанский', 'Кружки', 'Игрушки']),
    ('Здоровье', 'pill', '255,84,112', ['Аптека', 'Врачи', 'Спорт']),
    ('Развлечения', 'film', '255,79,216', ['Кино', 'Подписки', 'Игры']),
]
LOOSE = [('Подарки', 'gift', '255,165,61'), ('Одежда', 'spark', '0,229,192'), ('Отпуск', 'cloud', '56,182,255'),
         ('Связь', 'phone', '110,139,255'), ('Красота', 'spark', '255,179,138'), ('Животные', 'house', '77,255,136')]


def rgbc(v):
    return 'rgb(%s)' % v


def circle(icon, col, size=36):
    return ('<span style="width: %dpx; height: %dpx; flex: none; border-radius: 50%%; background: %s; display: flex; align-items: center; '
            'justify-content: center;">%s</span>' % (size, size, rgbc(col), svg(icon, ONLIME, size // 2, 1.8)))


def square(icon, col, count=None, size=40):
    n = ('<span style="position: absolute; right: -5px; bottom: -5px; min-width: 18px; height: 18px; padding: 0 4px; box-sizing: border-box; '
         'border-radius: 999px; background: %s; border: 2px solid %s; color: %s; font-size: 10px; font-weight: 700; display: flex; '
         'align-items: center; justify-content: center;">%d</span>' % (INK, CARD, BG, count) if count else '')
    return ('<span style="position: relative; width: %dpx; height: %dpx; flex: none; border-radius: 12px; background: %s; display: flex; '
            'align-items: center; justify-content: center;">%s%s</span>' % (size, size, rgbc(col), svg('folder', ONLIME, size // 2, 1.9), n))


def cats_screen(body):
    return inner_screen('Категории', segmented(['Расходы', 'Доходы'], 'Расходы', 'lime') + '<div style="margin-top: 10px;">%s</div>' % body,
                        icon_button('search', 'Поиск'))


def cats_square():  # В — shape tells them apart, layout stays
    out = ''
    for i, (name, icon, col, cats) in enumerate(FOLDERS[:4]):
        open_ = i == 0
        inner = row('<b>%s</b>' % name, 'папка · %d категорий' % len(cats), square(icon, col, len(cats)), svg('down', MUTED, 18), min_h=60)
        if open_:
            chips = ''.join('<span style="display: inline-flex; align-items: center; gap: 6px; height: 32px; padding: 0 10px 0 4px; border-radius: 999px; '
                            'background: %s; font-size: 13px;">%s%s</span>' % (CHIP, circle(icon, col, 24), c) for c in cats)
            inner += '<div style="display: flex; flex-wrap: wrap; gap: 6px; padding: 0 14px 12px 66px;">%s</div>' % chips
        out += card(inner, extra='margin-bottom: 8px;' + (' border-color: %s;' % LINE2 if open_ else ''))
    out += label('Без папки · 6') + group([row(n, '', circle(ic, c), chevron(), min_h=52) for n, ic, c in LOOSE[:4]])
    return cats_screen(out)


def cats_nested():  # А — folder card with nested rows and a guide line
    out = ''
    for name, icon, col, cats in FOLDERS[:3]:
        head = row('<b>%s</b>' % name, '%d категорий · 41 200 ARS в ноябре' % len(cats), square(icon, col, None, 40), svg('more', MUTED, 18), min_h=60)
        kids = ''.join('<div style="display: flex; align-items: center; gap: 10px; min-height: 44px; padding: 0 14px 0 34px; position: relative;">'
                       '<span style="position: absolute; left: 33px; top: 0; bottom: 0; width: 2px; background: %s;"></span>'
                       '<span style="width: 14px; height: 2px; background: %s; flex: none;"></span>%s<span style="font-size: 14px; flex-grow: 1;">%s</span>%s</div>'
                       % (rgbc(col), rgbc(col), circle(icon, col, 26), c, chevron()) for c in cats)
        out += card(head + '<div style="padding-bottom: 6px;">%s</div>' % kids, extra='margin-bottom: 8px;')
    return cats_screen(out)


def cats_headers():  # Б — folders are group headers (К4) over row cards (К5)
    out = ''
    for name, icon, col, cats in FOLDERS[:4]:
        out += ('<div style="display: flex; align-items: center; gap: 8px; margin: 14px 4px 8px;">%s<span style="font-size: 13px; font-weight: 600; '
                'letter-spacing: 0.04em; text-transform: uppercase; color: %s; flex-grow: 1;">%s · %d</span>%s</div>'
                % (square(icon, col, None, 22), INK2, name, len(cats), svg('more', MUTED, 16)))
        out += group([row(c, '', circle(icon, col, 30), '', min_h=46) for c in cats])
    return cats_screen(out)


# ---- #105 · colour distinctness -------------------------------------------------------------------------------

def _pal():
    with open(_PAL, encoding='utf-8') as f:
        return json.load(f)


def _rgb(h):
    return '%d,%d,%d' % tuple(int(h[i:i + 2], 16) for i in (0, 2, 4))


def swatch_grid(colors, bg, flagged=(), size=34, cols=9):
    cells = ''
    for i, c in enumerate(colors):
        ring = 'box-shadow: 0 0 0 2px rgb(255,92,127);' if i in flagged else ''
        cells += '<span style="width: %dpx; height: %dpx; border-radius: 50%%; background: rgb(%s); %s"></span>' % (size, size, c, ring)
    return ('<div style="display: grid; grid-template-columns: repeat(%d, %dpx); gap: 6px; justify-content: center; padding: 12px; border-radius: 16px; '
            'background: %s;">%s</div>' % (cols, size, bg, cells))


def panel_title(text, sub):
    return ('<div style="margin: 14px 4px 8px; display: flex; justify-content: space-between; font-size: 13px;"><b>%s</b>'
            '<span style="color: %s;">%s</span></div>' % (text, MUTED, sub))


def colors_now():
    p = _pal()
    dark = [_rgb(s[0]) for s in p['sw']]
    light = [_rgb(s[1]) for s in p['sw']]
    flag_d = {i for a, b in p['close_dark'] for i in (a, b)}
    flag_l = {i for a, b in p['close_light'] for i in (a, b)}
    pairs = ''.join('<span style="display: inline-flex; align-items: center; gap: 4px; margin: 0 8px 6px 0; font-size: 12px; color: %s;">'
                    '<span style="width: 18px; height: 18px; border-radius: 50%%; background: rgb(%s);"></span>'
                    '<span style="width: 18px; height: 18px; border-radius: 50%%; background: rgb(%s);"></span>%s</span>'
                    % (INK2, _rgb(a), _rgb(b), d) for a, b, d in p['worst_light'])
    body = (panel_title('Тёмная тема · 36 цветов', '5 пар-близнецов') + swatch_grid(dark, '#10160D', flag_d)
            + panel_title('Светлая тема · те же 36', '33 пары-близнеца') + swatch_grid(light, 'rgb(250,253,245)', flag_l)
            + label('Самые похожие в светлой (ΔE2000)', top=14) + '<div style="padding: 0 4px;">%s</div>' % pairs
            + note('Красное кольцо — у цвета есть сосед с ΔE < 8: рядом в списке их не отличить.'))
    return inner_screen('Цвета: замер сейчас', body)


def badge_row(name, col_rgb, icon, light=False):
    ink = 'rgb(20,24,18)' if light else INK
    return ('<div style="display: flex; align-items: center; gap: 12px; min-height: 46px;"><span style="width: 32px; height: 32px; border-radius: 50%%; '
            'background: rgb(%s); display: flex; align-items: center; justify-content: center;">%s</span><span style="font-size: 14px; color: %s;">%s</span></div>'
            % (col_rgb, svg(icon, '#0E1F00', 16, 1.9).replace('#0E1F00', 'rgb(14,31,0)'), ink, name))


def colors_24():
    p = _pal()
    keep = p['keep']
    dark = [_rgb(p['sw'][i][0]) for i in keep]
    light = [_rgb(p['sw'][i][3]) for i in keep]
    names = ['Продукты', 'Кафе', 'Такси', 'Аренда', 'Аптека', 'Школа', 'Кино', 'Подарки', 'Связь', 'Отпуск']
    icons = ['cart', 'cafe', 'taxi', 'house', 'pill', 'school', 'film', 'gift', 'phone', 'cloud']
    picks = [0, 2, 9, 5, 1, 4, 3, 11, 6, 8]
    rows_l = ''.join(badge_row(n, light[k % len(light)], ic, True) for n, ic, k in zip(names, icons, picks))
    body = (panel_title('Тёмная · 24 цвета', 'мин. ΔE 10.1') + swatch_grid(dark, '#10160D', cols=8)
            + panel_title('Светлая · заливка −12 %', 'мин. ΔE 10.1') + swatch_grid(light, 'rgb(250,253,245)', cols=8)
            + label('Светлая тема: список категорий', top=14)
            + '<div style="padding: 4px 14px; border-radius: 16px; background: rgb(240,244,234); display: grid; grid-template-columns: 1fr 1fr; column-gap: 8px;">%s</div>'
            % rows_l)
    return inner_screen('Б · 24 различимых цвета', body)


def colors_16():
    p = _pal()
    main = [_rgb(p['sw'][i][0]) for i in p['keep'][:16]]
    rest = [_rgb(s[0]) for i, s in enumerate(p['sw']) if i not in p['keep'][:16]]
    big = ''.join('<span style="width: 52px; height: 52px; border-radius: 50%%; background: rgb(%s); %s"></span>'
                  % (c, 'box-shadow: 0 0 0 3px #050805, 0 0 0 5px #EEF5E6;' if i == 2 else '') for i, c in enumerate(main))
    body = (
            field('Название', 'Пекарня') + label('Цвет · основные', top=16)
            + '<div style="display: grid; grid-template-columns: repeat(4, 52px); gap: 16px 24px; justify-content: center;">%s</div>' % big
            + '<div style="margin: 16px 4px 8px; display: flex; align-items: center; gap: 6px; font-size: 13px; color: %s;">Ещё оттенки · %d %s</div>'
            % (MUTED, len(rest), svg('down', MUTED, 16)) + swatch_grid(rest, '#10160D', size=26, cols=10)
            + note('Основные 16 различимы в обеих темах; оттенки — для тех, кому мало, с предупреждением «похож на …».'))
    return inner_screen('В · 16 основных + оттенки', body)


# ---- #173 · swipe conflicts -----------------------------------------------------------------------------------

def arrow(text, col, top, dir_='→'):
    return ('<div style="position: absolute; left: 24px; right: 24px; top: %dpx; display: flex; align-items: center; gap: 10px; padding: 10px 14px; '
            'border-radius: 14px; background: rgba(5,8,5,0.85); border: 1px dashed %s;"><span style="font-size: 22px; color: %s;">%s</span>'
            '<span style="font-size: 13px; line-height: 1.4;">%s</span></div>' % (top, col, col, dir_, text))


def swipe_screen(title, notes):
    from mockup_screens_main import RECENT
    rows = [tx(*r) for r in RECENT * 2][:9]
    lst = '<div style="margin: 10px -16px 0;">%s%s</div>' % (day('СЕГОДНЯ', '−45 100', 'ARS'), ''.join(rows))
    return tab_screen('Операции · ' + title, segmented(['Операции', 'Шаблоны'], 'Операции') + lst, 'ops') + notes


def swipe_a():
    return swipe_screen('А', arrow('Свайп влево по списку: «Операции» → «Шаблоны». Пилюля едет за пальцем.', LIME, 210, '←')
                        + arrow('На «Шаблонах» (крайний) тот же свайп — уже вкладка «Счета». Вправо с «Операций» — «Обзор».', CYAN, 330, '←')
                        + arrow('Вертикальная прокрутка не включает листание: угол жеста > 30° — это прокрутка.', MUTED, 450, '↕'))


def swipe_b():
    return swipe_screen('Б', arrow('Свайп по списку — всегда нижние вкладки (как сейчас, 19В).', CYAN, 210, '←')
                        + arrow('Сегменты — только тап или свайп по самой пилюле.', LIME, 330, '⇆')
                        + arrow('Минус: до «Шаблонов» — всегда тянуться к верху экрана.', PINK, 450, '!'))


BOARDS = [  # name, title, fn, issue number
    ('Fb181V', 'В · папка — квадрат с числом, категория — круг (рекомендую)', cats_square, 181),
    ('Fb181A', 'А · папка карточкой, вложенные строки', cats_nested, 181),
    ('Fb181B', 'Б · папки — заголовки групп', cats_headers, 181),
    ('Fb105Now', 'Замер сейчас: пары-близнецы', colors_now, 105),
    ('Fb105B', 'Б · 24 различимых цвета (рекомендую)', colors_24, 105),
    ('Fb105V', 'В · 16 основных + оттенки', colors_16, 105),
    ('Fb173A', 'А · сначала сегменты, с края — вкладка (рекомендую)', swipe_a, 173),
    ('Fb173B', 'Б · по экрану — только вкладки', swipe_b, 173),
]
