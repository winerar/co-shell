"""Main boards of the tabs (interactive): entry sheet «Main», «Счета», «Ещё», sync banner, update pill."""
from mockup_parts import *  # noqa: F401,F403
from mockup_logic import go, flip, per, proto, toggled, when
import mockup_screens_main as m

# ---- entry sheet: step 1 account → step 2 amount -----------------------------------------------------------------

TYPES = [('exp', 'Расход', 'Откуда списали?'), ('inc', 'Доход', 'Куда пришло?'), ('tr', 'Перевод', 'Откуда перевод?')]
RECENT_ACC = ['Galicia', 'Binance', 'Песо', 'Mercado Pago']


def type_seg(cur):
    return segmented([x[1] for x in TYPES], next(x[1] for x in TYPES if x[0] == cur), 'lime', ['go_t_' + x[0] for x in TYPES])


def step1(t):
    q = next(x[2] for x in TYPES if x[0] == t)
    head = ('<div style="display: flex; align-items: baseline; margin-top: 18px;"><h2 style="margin: 0; font-size: 19px; flex-grow: 1;">%s</h2>'
            '<span style="font-size: 12px; color: %s;">12 счетов</span></div>' % (q, MUTED))
    recent = label('Недавние', True, 14) + grid([go('st', 'sum', acc_recent(acc(n), n == 'Galicia')) for n in RECENT_ACC], 4, 4)
    groups = ''.join(label(title, True, 14) + grid([go('st', 'sum', acc_tile(a, a[0] == 'Galicia')) for a in in_group(g)])
                     for g, title, _ in GROUPS[:3])
    find = ('<div style="position: absolute; left: 0; right: 0; bottom: 0; padding: 10px 16px 14px; background: %s;">%s</div>' % (SHEET, search('Найти счёт')))
    return ('<div style="padding: 8px 16px 0; height: 100%%; box-sizing: border-box; overflow: hidden;">%s%s%s%s</div>%s'
            % (type_seg(t), head, recent, groups, find))


def step2(typed):
    a = acc('Galicia')
    head = ('<div style="display: flex; align-items: center; gap: 8px; padding: 0 4px;">%s<span style="flex-grow: 1;"></span>'
            '<span style="font-size: 13px; font-weight: 600; color: %s;">Расход</span></div>' % (go('st', 'acc', m.acc_chip(a)), PINK))
    amt = m.big_amount('4 500' if typed else '0', 'ARS', PINK if typed else MUTED2)
    keys = go('st', 'saved', m.keypad()) if typed else go('st', 'typed', m.keypad())
    return ('<div style="position: absolute; left: 0; right: 0; bottom: 0; padding: 0 12px 14px; display: flex; flex-direction: column; gap: 12px;">%s</div>'
            % (head + amt + m.cat_grid('Продукты' if typed else None) + m.chips_row(chip('Сегодня ▾'), chip('＋ Заметка'), chip('Чек в др. валюте')) + keys))


def entry_main():
    under = '<div style="padding: 14px 16px; font-size: 28px; font-weight: 700;">Обзор</div>'
    pick = per('t', [t[0] for t in TYPES], step1)
    inner = (when('is_st_acc', pick) + when('is_st_saved', pick + toast('Сохранено: −4 500 ARS · Продукты', 90))
             + when('is_st_sum', step2(False)) + when('is_st_typed', step2(True)))
    return proto('Шторка ввода', m.scrim(under) + m.sheet(inner), st='acc', t='exp')


# ---- accounts ----------------------------------------------------------------------------------------------------

def accounts():
    acts = icon_button('search', 'Поиск', INK, True) + ('<span style="width: 48px; height: 48px; border-radius: 50%%; background: %s; display: flex; '
                                                         'align-items: center; justify-content: center; margin-left: 6px;">%s</span>' % (LIME, svg('plus', ONLIME, 24, 2.2)))

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
    return proto('Счета', tab_screen('Счета', body, 'acc', acts), v='type')


# ---- «Ещё» -------------------------------------------------------------------------------------------------------

def more_body():
    tiles = grid([nav_tile('folder', 'Категории', '34 · 6 папок', LIME, LIMEBG), nav_tile('rate', 'Курсы валют', 'обновлены сегодня', CYAN, CYANBG),
                  nav_tile('user', 'Семья', 'синхронизировано · 2 чел.', VIOLET, VIOLETBG), nav_tile('download', 'Резервная копия', 'авто · 3 ноября', YELLOW, YELLOWBG),
                  nav_tile('mail', 'Обратная связь', 'написать о проблеме', PINK, PINKBG)], 2, 10)
    return tiles + '<div style="margin-top: 14px;">%s</div>' % row('Настройки', 'Тема · Вольт, валюты отчётов, встряхивание, обновление', svg('gear'), chevron(), min_h=56)


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
    return ('<div style="padding: 12px 14px; border-radius: 16px; background: %s; border: 1px solid %s;"><div style="display: flex; align-items: center; gap: 8px; '
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
             'box-shadow: 0 8px 24px rgba(0,0,0,0.35);">%s%s%s</div>'
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
