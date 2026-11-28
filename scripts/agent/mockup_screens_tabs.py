"""Main boards of the tabs (interactive): entry sheet «Main» (one step, #505), «Счета», «Ещё», sync banner, update pill."""
from mockup_parts import *  # noqa: F401,F403
from mockup_logic import go, flip, per, proto, toggled, when
import mockup_screens_main as m
import mockup_screens_fb_290 as p
import mockup_screens_entry as e

# ---- entry sheet: one step (#505, = `DetailsStep.kt`) ---------------------------------------------------------------
# Type + camera + templates → ribbon of account badges («Откуда»/«Куда» for a transfer) → «Сумма» → «Категория · частые»
# (not for a transfer) → «Детали» → keypad with «Готово». Tap a type; tap the keypad: «0» → «4 500» → saved (toast, empty again).

def entry_body(t, typed):
    color = dict((k, c) for k, _, c in e.TYPES)[t]
    accs = e.ribbon(0, 'Откуда') + e.ribbon(2, 'Куда') if t == 'tr' else e.ribbon(0)
    amount = e.amount_card('4 500' if typed else '0', 'ARS', color if typed else MUTED2, links='' if t == 'tr' else '+ Сумма покупки')
    cats = '' if t == 'tr' else e.categories('Продукты' if typed else None)
    keys = go('st', 'saved', m.keypad()) if typed else go('st', 'typed', m.keypad(done=False))
    return e.type_bar(t, taps=True) + accs + amount + cats + e.details(refund=t == 'exp') + '<div style="flex-grow: 1;"></div>' + keys


def entry_main():
    empty = per('t', [x[0] for x in e.TYPES], lambda t: entry_body(t, False))
    inner = (when('is_st_sum', empty) + when('is_st_saved', empty + toast('Сохранено: −4 500 ARS · Продукты', 90))
             + when('is_st_typed', per('t', [x[0] for x in e.TYPES], lambda t: entry_body(t, True))))
    return proto('Шторка ввода', p.frame(inner), st='sum', t='exp')


# ---- accounts ----------------------------------------------------------------------------------------------------

def accounts():
    # ⇅ — «Порядок» (#338). «Счета» opens from «Ещё»/«Обзор»: back arrow, no bottom bar; the add button is the round «+» (fab)
    acts = icon_button('swap', 'Изменить порядок') + icon_button('search', 'Поиск', INK, True)

    def by_type(_):
        out = ''
        for g, title, total in GROUPS:
            items = in_group(g)
            head = flip(g, toggled(g, group_head(title, len(items), total, False), group_head(title, len(items), total)))
            out += head + toggled(g, '', group([acc_row(a) for a in items]))
        return out

    def by_cur(_):
        out = ''
        for cur, total in [('ARS', '1 824'), ('AMD', '1 585'), ('USDT', '2 310'), ('USD', '2 500'), ('RUB', '190')]:
            items = [a for a in ACCOUNTS if a[6] == cur]
            out += group_head(cur, len(items), total) + group([acc_row(a) for a in items])
        return out

    def by_who(_):
        mine = [a for a in ACCOUNTS if 'Аня' not in a[4]]
        return group_head('Общие', len(mine), '8 580') + group([acc_row(a) for a in mine[:5]]) + group_head('Аня', 1, '1 060') + group([acc_row(acc('Ameria'))])

    views = [('type', 'По типу'), ('cur', 'По валюте'), ('who', 'Чьи')]
    seg = per('v', [k for k, _ in views], lambda v: segmented([t for _, t in views], dict(views)[v], 'violet', ['go_v_' + k for k, _ in views]))
    lists = per('v', ['type'], by_type) + per('v', ['cur'], by_cur) + per('v', ['who'], by_who)
    body = (big_total('Всего в общем балансе · 12 счетов', '9 640', 'USD') + '<div style="margin-top: 14px;">%s</div>' % seg
            + '<div style="height: 610px; overflow: hidden;">%s</div>' % lists)
    return proto('Счета', inner_screen('Счета', body, acts) + fab('Добавить счёт'), v='type')


# ---- «Ещё» -------------------------------------------------------------------------------------------------------

def more_body():
    """«Ещё» as in the app (poll 2026-11-28 «А»): two grids of icons and three rows of «Приложение» — `mockup_screens_fb_menu.py`."""
    import mockup_screens_fb_menu as menu
    return menu.body()


def more():
    return board('Ещё', tab_screen('Ещё', more_body(), 'more'))


# ---- «Обзор»: sync banner ------------------------------------------------------------------------------------------

REASONS = [('net', 'Нет интернета', '3 изменения сохранены на телефоне и уйдут сами, когда появится сеть.', 'Повторить'),
           ('auth', 'Вход в Google истёк', 'Войдите снова — 3 изменения ждут отправки, ничего не потеряно.', 'Войти'),
           ('access', 'Сервер не пускает в семью', 'Нет доступа к «Общему бюджету». Отправьте отзыв — в нём будет код ошибки PERMISSION_DENIED.', 'Отзыв'),
           ('server', 'Сервер не отвечает', 'Это не у вас. Попробуем снова через минуту; изменения ждут на телефоне.', 'Повторить'),
           ('quota', 'Дневной лимит исчерпан', 'Бесплатный лимит Firebase на сегодня закончился. Синхронизация продолжится в 04:00.', 'Понятно')]


def sync_banner(i):
    key, title, text, action = REASONS[i]
    nxt = REASONS[(i + 1) % len(REASONS)][0]
    btn = ('<span style="height: 36px; padding: 0 14px; border-radius: 999px; font-size: 13px; font-weight: 600; display: inline-flex; align-items: center; %s">%s</span>')
    acts = ('<div style="display: flex; justify-content: flex-end; gap: 8px; margin-top: 10px;">%s%s</div>'
            % (go('r', nxt, btn % ('border: 1px solid %s; color: %s;' % (WARNLINE, INK), 'Другая причина →')), go('r', 'ok', btn % ('background: %s; color: %s;' % (WARN, '#2A1E08'), action))))
    return ('<div style="padding: 12px 14px; border-radius: 24px; background: %s; border: 1px solid %s;"><div style="display: flex; align-items: center; gap: 8px; '
            'font-size: 14px; font-weight: 600; color: %s;">%s%s</div><div style="margin-top: 4px; font-size: 13px; line-height: 1.4;">%s</div>'
            '<div style="margin-top: 4px; font-size: 12px; color: %s;">Последняя синхронизация: сегодня 09:12</div>%s</div>'
            % (WARNBG, WARNLINE, WARN, svg('cloud', WARN, 18), title, text, MUTED, acts))


def home_sync():
    banner = per('r', [r[0] for r in REASONS], lambda k: sync_banner([r[0] for r in REASONS].index(k)))
    ok = when('is_r_ok', go('r', 'net', message('Синхронизировано только что', 'ok')))
    body = (banner + ok + m.balance_hero() + m.tiles(m.tile('Траты за ноябрь', '1 240,50', 'USD'), m.tile('Без категории', '14', 'операций'))
            + m.recent_card(m.RECENT[:3]))
    return proto('Обзор: ошибка синка', tab_screen('Обзор', body, 'home', m.SEARCH_BTN), r='net')


# ---- update pill over «Обзор» ------------------------------------------------------------------------------------

def update_pill():
    pill_ = ('<div style="position: absolute; left: 50%%; transform: translateX(-50%%); bottom: 96px; height: 44px; padding: 0 8px 0 6px; border-radius: 999px; '
             'background: %s; border: 1px solid %s; display: flex; align-items: center; gap: 8px; font-size: 14px; font-weight: 600; white-space: nowrap; '
             '">%s%s%s</div>'
             % (CHIP, LIMEPILL, go('p', 'screen', dot_icon('download', LIME, 32) + '<span>Версия 43 · Обновить</span>'), '', go('p', 'gone', svg('close', MUTED, 18))))
    home = tab_screen('Обзор', m.balance_hero() + m.tiles(m.tile('Траты за ноябрь', '1 240,50', 'USD'), m.tile('Без категории', '14', 'операций'))
                      + m.recent_card(m.RECENT[:3]), 'home', m.SEARCH_BTN, False)
    screen = ('<div style="position: absolute; inset: 0; background: %s;">%s</div>'
              % (BG, go('p', 'gone', top_bar('Обновление')) + '<div style="padding: 24px 16px;">'
                 + ring(0.45, '<span style="%s font-size: 30px; font-weight: 700;">45 %%</span><span style="font-size: 12px; color: %s;">3,6 из 8,1 МБ</span>' % (MONO, MUTED))
                 + '<h2 style="margin: 24px 0 0; text-align: center; font-size: 22px;">Скачиваем…</h2>' + note('Можно выйти — загрузка продолжится', 6).replace('margin: 6px 4px 0;', 'margin: 6px 4px 0; text-align: center;')
                 + '</div>'))
    return proto('Пилюля «есть обновление»', home + when('is_p_pill', pill_ + nav('home', True)) + when('is_p_gone', nav('home', True)) + when('is_p_screen', screen), p='pill')


BOARDS = [('Main', entry_main), ('Accounts', accounts), ('More', more), ('HomeSync', home_sync), ('UpdatePill', update_pill)]
