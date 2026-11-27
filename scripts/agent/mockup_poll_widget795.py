#!/usr/bin/env python3
"""Poll boards for #795, round 2: the look of the home-screen widget family and the quick-settings tile, all states.
Usage: python3 scripts/agent/mockup_poll_widget795.py <out-dir>  → <out-dir>/img/*.png (dark and «@light»), then
`agent.sh poll docs/design/poll-795-widgets.json --img <out-dir>/img`.
Standalone proposals (like mockup_poll_quick795.py): after the owner's «ок» the winners move to docs/design as `Wid795*` boards.
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from mockup_poll_f_common import *  # noqa: E402,F401,F403
import mockup_poll_f_common as C  # noqa: E402
import mockup_poll_quick795 as Q  # noqa: E402

B = C.B
mix = C.mix


# ---------- widget bricks ----------
def press(t):
    """Pressed shade of a filled button: a state-list drawable can change colour, not scale."""
    return mix(t['prim'], C.edge(t), .35)


def btn(t, icon, text, primary, pressed=False, muted=False, h=48):
    if muted:
        st = 'background:%s;color:%s;opacity:.55' % (t['wash'], t['muted'])
    elif primary:
        bg = press(t) if pressed else t['prim']
        st = 'background:%s;color:%s;box-shadow:inset 0 0 0 %dpx %s;font-weight:700' % (bg, t['onp'], 3 if pressed else 2, C.edge(t))
    else:
        st = 'background:%s;color:%s' % (mix(t['wash'], t['ink'], .12) if pressed else t['wash'], t['ink'])
    ink = t['muted'] if muted else (t['onp'] if primary else t['ink'])
    return ('<span style="flex:1;height:%dpx;border-radius:999px;display:flex;align-items:center;justify-content:center;gap:8px;font-size:15px;%s">%s%s</span>'
            % (h, st, B.svg(icon, ink, 20, 2), ('<span>%s</span>' % text) if text else ''))


def card_w(t, inner, h=76, extra='', w=None):
    return ('<div style="border-radius:28px;background:%s;border:1px solid %s;padding:14px 16px;box-sizing:border-box;height:%dpx;display:flex;align-items:center;%s%s">%s</div>'
            % (t['surf'], t['line'], h, ('width:%dpx;' % w) if w else '', extra, inner))


def hero_bg(t):
    return ('linear-gradient(135deg,%s,%s)' % (mix(t['page'], t['prim'], .22), mix(t['page'], t['acc'], .3))) if t['dark'] else \
        ('linear-gradient(135deg,%s,%s)' % (mix('#FFFFFF', t['prim'], .55), mix('#FFFFFF', t['acc'], .25)))


def row2(t, **kw):
    return '<div style="display:flex;gap:10px;width:100%%">%s%s</div>' % (btn(t, 'out', 'Расход', True, **{k: v for k, v in kw.items() if k != 'only'}),
                                                                           btn(t, 'in', 'Доход', False, muted=kw.get('muted', False)))


def tag(t, text):
    return '<div style="font-size:12px;color:%s;margin:0 4px 6px">%s</div>' % (t['muted'], text)


def stack(t, items, cap=None):
    """Several labelled widgets on one launcher screen."""
    body = ''.join(tag(t, lab) + w + '<div style="height:12px"></div>' for lab, w in items)
    return Q.launcher(t, body, cap or '') if cap else (
        '<div style="padding:56px 20px 0"><div style="font-size:44px;font-weight:300">9:41</div></div><div style="padding:20px 20px 0">%s</div>' % body)


# ---------- Q1: look ----------
def look_a(t):
    return Q.launcher(t, card_w(t, row2(t)), 'А «Капсулы»: плотная карточка цвета приложения, «Расход» — главная кнопка с неоновой заливкой, «Доход» — спокойная. Как карточки и кнопки в самом приложении.')


def look_b(t):
    mark = ('<span style="width:44px;height:44px;border-radius:14px;background:%s;display:flex;align-items:center;justify-content:center;flex:none">%s</span>'
            % (t['page'], B.svg('wallet', t['prim'] if t['dark'] else C.ink2(t), 24, 1.8)))
    inner = mark + '<div style="display:flex;gap:10px;flex:1;margin-left:12px">%s%s</div>' % (btn(t, 'out', 'Расход', True), btn(t, 'in', '', False))
    w = ('<div style="border-radius:28px;background:%s;border:1px solid %s;padding:14px 16px;box-sizing:border-box;height:76px;display:flex;align-items:center">%s</div>'
         % (hero_bg(t), t['line'], inner))
    return Q.launcher(t, w, 'Б «Градиент»: как главная карточка «Обзора» — градиент темы, слева значок приложения, «Расход» с подписью, «Доход» — значком. Заметнее на любых обоях.')


def look_c(t):
    def half(icon, text, main):
        bg = t['prim'] if main else t['wash']
        ink = t['onp'] if main else t['ink']
        ring = 'box-shadow:inset 0 0 0 2px %s;' % C.edge(t) if main else ''
        return ('<span style="flex:1;height:100%%;border-radius:22px;background:%s;color:%s;%sdisplay:flex;flex-direction:column;align-items:center;justify-content:center;gap:6px;font-size:14px;font-weight:%d">%s%s</span>'
                % (bg, ink, ring, 700 if main else 500, B.svg(icon, ink, 26, 2), text))
    inner = '<div style="display:flex;gap:10px;width:100%%;height:100%%">%s%s</div>' % (half('out', 'Расход', True), half('in', 'Доход', False))
    w = '<div style="border-radius:28px;background:%s;border:1px solid %s;padding:10px;box-sizing:border-box;height:104px">%s</div>' % (t['surf'], t['line'], inner)
    return Q.launcher(t, w, 'В «Две плитки»: выше (4×1,5), крупные цели нажатия со значком над словом — удобнее попадать большим пальцем, но занимает больше места.')


def themes(t):
    def pal(prim, onp, acc_d, acc_l, bg_d, bg_l):
        if t['dark']:
            page = mix(bg_d, prim, .06)
            return dict(t, page=page, surf=mix(page, prim, .07), wash=mix(page, acc_d, .26), acc=acc_d, prim=prim, onp=onp, ink=mix('#FFFFFF', page, .08),
                        muted=mix('#FFFFFF', page, .32), line=mix(page, prim, .45))
        ink = mix('#000000', bg_l, .1)
        return dict(t, page='#FFFFFF', surf=mix('#FFFFFF', prim, .12), wash=mix('#FFFFFF', acc_l, .18), acc=acc_l, prim=prim, onp=onp, ink=ink,
                    muted=mix('#000000', bg_l, .36), line=mix('#FFFFFF', ink, .42))
    items = [('Лайм', t), ('Гранат', pal('#FF3D6E', '#1F0009', '#2EE6BE', '#056E59', '#12060A', '#FFF4F6')),
             ('Василёк', pal('#5C8DFF', '#000B2A', '#FFCC4D', '#7A5600', '#060912', '#F4F7FF'))]
    return stack(t, [(n, card_w(p, row2(p))) for n, p in items], 'Виджет красится в выбранную в приложении тему — на любой из девяти палитр, без чёрного. Лайм, Гранат, Василёк — для примера.')


# ---------- Q2: states and flow ----------
def lock_a(t):
    inner = ('<div style="display:flex;flex-direction:column;gap:8px;width:100%%"><div style="display:flex;align-items:center;gap:8px;font-size:14px;color:%s">%s'
             '<span>Обновите приложение, чтобы записывать</span></div><div style="display:flex;gap:10px">%s%s</div></div>'
             % (t['muted'], B.svg('lock', t['muted'], 18, 1.9), btn(t, 'out', 'Расход', True, muted=True, h=40), btn(t, 'in', 'Доход', False, muted=True, h=40)))
    return Q.launcher(t, card_w(t, inner, h=104), 'А: кнопки приглушены, сверху строка с замком. Нажатие в любом месте открывает окно «Обновить» — человек сразу видит причину.')


def lock_b(t):
    return Q.launcher(t, card_w(t, row2(t)), 'Б: виджет выглядит как обычно; причину человек узнаёт после нажатия — открывается окно «Обновите приложение». Проще, но «сломанной» кнопка не выглядит.')


def flow(t):
    def mini(inner, label):
        return ('<div style="width:112px"><div style="width:112px;height:236px;border-radius:14px;overflow:hidden;position:relative;background:%s;border:1px solid %s">'
                '<div style="width:390px;height:820px;transform:scale(.287);transform-origin:0 0;position:relative">%s</div></div>'
                '<div style="font-size:12px;line-height:1.3;margin-top:6px;color:%s">%s</div></div>' % (t['page'], t['line'], inner, t['ink'], label))
    home = ('<div style="padding:56px 20px 0"><div style="font-size:44px;font-weight:300">9:41</div></div><div style="padding:60px 20px 0">%s</div>' % card_w(t, row2(t, pressed=True)))
    dots = ''.join('<span style="width:22px;height:22px;border-radius:50%%;background:%s"></span>' % (t['prim'] if i < 2 else t['wash']) for i in range(4))
    keys = ''.join('<span style="width:84px;height:84px;border-radius:50%%;background:%s;display:flex;align-items:center;justify-content:center;font-size:34px">%s</span>' % (t['wash'], k)
                   for k in '123456789')
    pin = ('<div style="padding:150px 0 0;text-align:center"><div style="font-size:30px;font-weight:600">Введите PIN</div><div style="display:flex;gap:16px;justify-content:center;margin:40px 0">%s</div>'
           '<div style="display:grid;grid-template-columns:repeat(3,84px);gap:20px;justify-content:center">%s</div></div>' % (dots, keys))
    entry = (C.topbar(t, 'Обзор', back=False) + C.hero(t, '<div style="height:160px"></div>', 'margin-top:80px')
             + C.sheet(t, 'Расход', '<div style="height:120px"></div>' + C.button(t, 'Готово'), 300))
    strip = ('<div style="display:flex;gap:12px;justify-content:center;align-items:flex-start;padding:0 8px">%s%s%s</div>'
             % (mini(home, '1. Нажали «Расход» на рабочем столе'), mini(pin, '2. Если включён PIN — вводите его'), mini(entry, '3. Открылась шторка ввода расхода')))
    cap = C.caption(t, 'Путь нажатия. Без PIN шаг 2 пропускается; ввод закрыт — вместо шага 3 окно «Обновите». Шторка выезжает как после «+» в приложении.', 0)
    return '<div style="padding:70px 0 0">%s</div>%s' % (strip, cap)


# ---------- Q3: sizes ----------
def sizes(t):
    def lab(s):
        return tag(t, s)
    w4 = card_w(t, row2(t))
    w3 = card_w(t, row2(t), w=262)
    round_btn = lambda icon, main: ('<span style="width:48px;height:48px;border-radius:50%%;background:%s;display:flex;align-items:center;justify-content:center;%s">%s</span>'  # noqa: E731
                                     % (t['prim'] if main else t['wash'], 'box-shadow:inset 0 0 0 2px %s' % C.edge(t) if main else '', B.svg(icon, t['onp'] if main else t['ink'], 24, 2)))
    w2 = card_w(t, '<div style="display:flex;gap:10px;width:100%%;justify-content:center">%s%s</div>' % (round_btn('out', True), round_btn('in', False)), w=170)
    big = card_w(t, '<div style="display:flex;gap:8px;width:100%%">%s%s</div>' % (btn(t, 'out', 'Расход', True, h=56).replace('font-size:15px', 'font-size:20px'),
                                                                                      btn(t, 'in', 'Доход', False, h=56).replace('font-size:15px', 'font-size:20px')), h=92)
    body = lab('4×1 — как задумано') + w4 + '<div style="height:10px"></div>' + lab('3×1 — тот же вид, уже') + w3 + '<div style="height:10px"></div>' + \
        lab('2×1 — только значки (подпись слышит TalkBack)') + w2 + '<div style="height:10px"></div>' + lab('Крупный шрифт ×1,3 — виджет подрастает, слова не режутся') + big
    return Q.launcher(t, body, 'Размеры: растягивается от 2×1 до 4×1; чем уже, тем меньше слов.')


# ---------- Q4–Q6: more widgets ----------
def chip(t, name, amount, main=False):
    bg, ink = (t['prim'], t['onp']) if main else (t['wash'], t['ink'])
    ring = 'box-shadow:inset 0 0 0 2px %s;' % C.edge(t) if main else ''
    return ('<span style="flex:1;min-width:0;height:48px;border-radius:999px;background:%s;color:%s;%sdisplay:flex;flex-direction:column;align-items:center;justify-content:center;font-size:13px;line-height:1.15">'
            '<b style="font-weight:600;white-space:nowrap">%s</b><span style="%sfont-size:11px;opacity:.8">%s</span></span>' % (bg, ink, ring, name, C.MONO, amount))


def tpl(t):
    one = card_w(t, '<div style="display:flex;gap:8px;width:100%%">%s%s%s%s</div>' % (chip(t, 'Кофе', '3 500', True), chip(t, 'Проезд', '700'), chip(t, 'Продукты', 'сумма?'),
                                                                                         '<span style="width:48px;height:48px;border-radius:50%%;background:%s;display:flex;align-items:center;justify-content:center;flex:none">%s</span>'
                                                                                         % (t['wash'], B.svg('plus', t['ink'], 22, 2))))
    grid = card_w(t, '<div style="display:grid;grid-template-columns:1fr 1fr;gap:8px;width:100%%">%s%s%s%s</div>' % (chip(t, 'Кофе', '3 500', True), chip(t, 'Проезд', '700'),
                                                                                                                       chip(t, 'Аренда', '650 USD'), chip(t, 'Все шаблоны ›', '')), h=140)
    empty = card_w(t, '<div style="display:flex;align-items:center;gap:12px;width:100%%"><span style="flex:1;font-size:14px;color:%s">Шаблонов пока нет</span>%s</div>'
                   % (t['muted'], btn(t, 'plus', 'Создать', False, h=44).replace('flex:1', 'flex:none;padding:0 18px')))
    return Q.launcher(t, tag(t, '4×1 — до трёх шаблонов') + one + '<div style="height:8px"></div>' + tag(t, '4×2 — до четырёх') + grid + '<div style="height:8px"></div>' + tag(t, 'Пока шаблонов нет') + empty,
                      'Виджет «Шаблоны»: частая трата — одно нажатие, шторка открывается уже заполненной (с суммой — если она в шаблоне). Порядок — как в приложении.')


def today(t):
    def full(hidden=False):
        amt = '•••• ARS' if hidden else '≈ 9 800 <span style="font-size:14px">ARS</span>'
        top = ('<div style="display:flex;justify-content:space-between;align-items:baseline"><span style="font-size:14px;color:%s">Сегодня можно</span></div>'
               '<div style="%sfont-size:28px;margin-top:2px">%s</div><div style="margin-top:8px">%s</div>' % (t['muted'], C.MONO, amt, C.meter(t, 0.25)))
        return ('<div style="border-radius:28px;background:%s;border:1px solid %s;padding:14px 16px;box-sizing:border-box">%s<div style="display:flex;gap:10px;margin-top:12px">%s%s</div></div>'
                % (t['surf'], t['line'], top, btn(t, 'out', 'Расход', True), btn(t, 'in', 'Доход', False)))
    empty = card_w(t, '<div style="font-size:14px;color:%s;flex:1">Задайте лимит — и здесь появится, сколько можно потратить сегодня</div>%s' % (
        t['muted'], btn(t, 'out', '', True).replace('flex:1', 'flex:none;width:48px;margin-left:12px')), h=84)
    return Q.launcher(t, tag(t, '4×2') + full() + '<div style="height:8px"></div>' + tag(t, 'Суммы скрыты («глаз» или PIN)') + full(True) + '<div style="height:8px"></div>' + tag(t, 'Лимитов ещё нет') + empty,
                      '«Сегодня можно» с «Обзора» плюс те же кнопки. Считается на телефоне, без сети; при скрытых суммах — «••••».')


def rnd(t):
    def r(icon, main, label):
        bg = t['prim'] if main else t['surf']
        ink = t['onp'] if main else t['ink']
        ring = 'inset 0 0 0 2px %s' % C.edge(t) if main else 'inset 0 0 0 1px %s' % t['line']
        return ('<div style="display:flex;flex-direction:column;align-items:center;gap:6px;font-size:12px;color:%s"><span style="width:72px;height:72px;border-radius:50%%;background:%s;box-shadow:%s;display:flex;align-items:center;justify-content:center">%s</span>%s</div>'
                % (t['muted'], bg, ring, B.svg(icon, ink, 32, 2.2), label))
    return Q.launcher(t, '<div style="display:flex;gap:28px;padding:8px 8px 0">%s%s%s</div>' % (r('plus', True, 'Расход'), r('in', False, 'Доход'), r('wallet', False, 'Обзор')),
                      'Круглые 1×1: «Расход» и «Доход» отдельными значками — можно поставить в углах рабочего стола. Третий — просто открыть приложение.')


# ---------- Q7: tile ----------
def qs_states(t):
    def tile(icon, label, sub='', ours=False, off=False):
        bg = t['prim'] if ours else mix(t['page'], t['ink'], .1)
        ink = t['onp'] if ours else t['ink']
        if off:
            bg, ink = mix(t['page'], t['ink'], .06), t['muted']
        s = '<span style="font-size:11px;opacity:.8">%s</span>' % sub if sub else ''
        return ('<div style="height:64px;border-radius:32px;background:%s;color:%s;display:flex;align-items:center;gap:10px;padding:0 16px;font-size:14px;%s">%s<span style="display:flex;flex-direction:column;line-height:1.15"><span>%s</span>%s</span></div>'
                % (bg, ink, ('box-shadow:inset 0 0 0 2px %s;font-weight:700' % C.edge(t)) if ours else '', B.svg(icon, ink, 22, 1.9), label, s))
    other = tile('wifi', 'Wi-Fi') + tile('bolt', 'Экономия')
    g = lambda *ts: '<div style="display:grid;grid-template-columns:1fr 1fr;gap:10px">%s</div>' % ''.join(ts)  # noqa: E731
    body = ('<div style="padding:56px 16px 0">' + tag(t, 'В шторке: готова') + g(tile('wifi', 'Wi-Fi'), tile('out', 'Расход', 'co-shell', True))
            + '<div style="height:14px"></div>' + tag(t, 'Ввод закрыт — нужно обновить') + g(tile('lock', 'Поворот'), tile('out', 'Расход', 'Обновите', off=True))
            + '<div style="height:14px"></div>' + tag(t, 'В списке «Изменить плитки»: «Расход» · co-shell')
            + '<div style="display:grid;grid-template-columns:1fr 1fr;gap:10px">%s%s</div></div>' % (tile('out', 'Расход', 'co-shell'), tile('bolt', 'Экономия')))
    return body + C.caption(t, 'Состояния плитки. «Готова» — цвет приложения; ввод закрыт — приглушена, подпись «Обновите», нажатие открывает окно. Плитку добавляет сам человек.', 0)


def qs_two(t):
    def tile(icon, label, ours=True):
        bg = t['prim'] if ours else t['wash']
        ink = t['onp'] if ours else t['ink']
        return ('<div style="height:64px;border-radius:32px;background:%s;color:%s;display:flex;align-items:center;gap:10px;padding:0 16px;font-size:14px;%s">%s<span>%s</span></div>'
                % (bg, ink, ('box-shadow:inset 0 0 0 2px %s;font-weight:700' % C.edge(t)) if ours else '', B.svg(icon, ink, 22, 1.9), label))
    body = ('<div style="padding:56px 16px 0"><div style="display:grid;grid-template-columns:1fr 1fr;gap:10px">%s%s%s%s</div></div>'
            % (tile('wifi', 'Wi-Fi', False), tile('bolt', 'Экономия', False), tile('out', 'Расход'), tile('in', 'Доход')))
    return body + C.caption(t, 'Две плитки: «Расход» и «Доход». Доход нужен реже — отдельная плитка занимает место; хватит одной и виджета.', 0)


# ---------- Q8: picker ----------
def picker(t):
    def entry(title, desc, thumb):
        return ('<div style="padding:12px 0;border-bottom:1px solid %s"><div style="font-size:16px;font-weight:600">%s</div><div style="font-size:13px;color:%s;margin:2px 0 10px">%s</div>%s</div>'
                % (t['line'], title, t['muted'], desc, thumb))
    small = lambda inner, w=330: '<div style="transform:scale(.9);transform-origin:0 0;width:%dpx">%s</div>' % (w, inner)  # noqa: E731
    body = ('<div style="padding:56px 20px 0"><div style="font-size:20px;font-weight:700;margin-bottom:4px">Виджеты</div><div style="font-size:13px;color:%s">co-shell</div>%s%s</div>'
            % (t['muted'], entry('Быстрая запись', 'Расход и доход в одно нажатие', small(card_w(t, row2(t), h=70))),
               entry('Шаблоны', 'Частые траты — одним нажатием', small(card_w(t, '<div style="display:flex;gap:8px;width:100%%">%s%s%s</div>' % (chip(t, 'Кофе', '3 500', True), chip(t, 'Проезд', '700'), chip(t, 'Аренда', '650 USD')), h=70)))))
    return body + C.caption(t, 'Так виджеты выглядят в списке виджетов телефона: название, одна строка и картинка. Названия — по словарю: «Расход», «Доход», «Шаблоны».', 0)


BOARDS = [('Wid795LookA', look_a, 390, 820), ('Wid795LookB', look_b, 390, 820), ('Wid795LookC', look_c, 390, 820), ('Wid795Themes', themes, 390, 820),
          ('Wid795LockA', lock_a, 390, 820), ('Wid795LockB', lock_b, 390, 820), ('Wid795Flow', flow, 390, 600), ('Wid795Sizes', sizes, 390, 820),
          ('Wid795Tpl', tpl, 390, 820), ('Wid795Today', today, 390, 820), ('Wid795Round', rnd, 390, 820),
          ('Qs795States', qs_states, 390, 820), ('Qs795Two', qs_two, 390, 820), ('Wid795Picker', picker, 390, 820)]

if __name__ == '__main__':
    C.run(BOARDS, sys.argv[1])
