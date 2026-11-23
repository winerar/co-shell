"""Proposal boards: «Ещё → Настройки» — list vs tiles vs group cards (owner's question 2026-11-20, poll-settings-cards.json).

А — today's list (board Set1Root, unchanged); Б — 2×3 tiles (`MoreTile`); В — one card per group with its main settings
inline and «Все ›» to the group screen. Stress boards: Spanish, ≈ 320 dp with font ×1,3 (CSS zoom over the body).
Picked 2026-11-20: В. Boards А/Б removed from the canvas (functions stay for the poll page history).
"""
from mockup_catalog_bricks import status_chip
from mockup_bricks import *  # noqa: F401,F403
from mockup_parts import *  # noqa: F401,F403

FEEDBACK = []

RU = {
    'title': 'Настройки', 'intro': 'Настройки этого телефона. Валюты итогов — общие для семьи.', 'all': 'Все',
    'groups': [
        ('Внешний вид', 'Язык · тема · палитра · плотность списков', 'spark', VIOLET),
        ('Экраны', 'Обзор · операции · счета', 'home', LIME),
        ('Деньги и даты', 'Валюты итогов · начало недели', 'rate', CYAN),
        ('Ввод операции', 'Встряхнуть · вибрация', 'plus', LIME),
        ('Уведомления', 'Платежи · время · траты партнёра', 'alert', YELLOW),
        ('Данные и копия', 'Копия · импорт · Wi-Fi · стереть', 'archive', CYAN),
    ],
    'theme': ('Тема', ['Система', 'Тёмная', 'Светлая']), 'palette': ('Палитра', 'Лайм'),
    'screens': ('Обзор, операции, отчёты, счета', 'что показывать и в каком порядке'),
    'money': [('Валюты итогов', 'USD · ARS · AMD'), ('Неделя начинается с', 'понедельника')],
    'input': [('Встряхнуть — сканер QR', 'когда приложение открыто'), ('Вибрация', 'касания и сохранение')],
    'notices': [('Напоминания о платежах', 'в день платежа'), ('Время напоминания', '9:00')],
    'data': [('Резервная копия', 'вчера 23:00 · раз в неделю'), ('Синхронизировать только по Wi-Fi', '')],
    'smart': [('Умные функции', 'spark', VIOLET), ('ИИ на телефоне', 'Выключен · необязательно'), ('Облачный разбор', 'Выключен · нужен интернет')],
    'help': [('Помощь', 'bulb', CYAN), ('Подсказки', 'Показываются'), ('Показать подсказки заново', 'Вернёт «Первые шаги» и советы')],
}
ES = {
    'title': 'Ajustes', 'intro': 'Ajustes de este teléfono. Las monedas de los totales son comunes a la familia.', 'all': 'Todo',
    'groups': [
        ('Apariencia', 'Idioma · tema · paleta · densidad de listas', 'spark', VIOLET),
        ('Pantallas', 'Resumen · movimientos · cuentas', 'home', LIME),
        ('Dinero y fechas', 'Monedas de los totales · inicio de semana', 'rate', CYAN),
        ('Introducir movimientos', 'Agitar · vibración', 'plus', LIME),
        ('Notificaciones', 'Pagos · hora', 'alert', YELLOW),
        ('Datos y copia', 'Copia · importar · Wi-Fi · borrar', 'archive', CYAN),
    ],
    'theme': ('Tema', ['Sistema', 'Oscuro', 'Claro']), 'palette': ('Paleta', 'Lima'),
    'screens': ('Resumen, movimientos, informes, cuentas', 'qué mostrar y en qué orden'),
    'money': [('Monedas de los totales', 'USD · ARS · AMD'), ('La semana empieza el', 'lunes')],
    'input': [('Agitar — escáner QR', 'con la app abierta'), ('Vibración', 'toques y guardado')],
    'notices': [('Recordatorios de pagos', 'el día del pago'), ('Hora del recordatorio', '9:00')],
    'data': [('Copia de seguridad', 'ayer 23:00 · cada semana'), ('Sincronizar solo por Wi-Fi', '')],
    'smart': [('Funciones inteligentes', 'spark', VIOLET), ('IA en el teléfono', 'Desactivada · opcional'), ('Análisis en la nube', 'Desactivado · necesita internet')],
    'help': [('Ayuda', 'bulb', CYAN), ('Consejos', 'Se muestran'), ('Mostrar los consejos de nuevo', 'Recupera «Primeros pasos» y los consejos')],
}

STRESS = 'zoom: 1.6;'  # ≈ 320 dp × font 1.3 on a 390 board: (390 − 32) / ((320 − 32) / 1.3) ≈ 1.6


def head(t):
    return note(t['intro'], 4)


# ---- А: the list (as today) ------------------------------------------------------------------------------------------

def list_body(t):
    rows = [row(n, s, tile_icon(i, c), chevron(), min_h=52) for n, s, i, c in t['groups']]
    return head(t) + label(t['title'], top=10) + group(rows)


# ---- Б: tiles 2×3 ----------------------------------------------------------------------------------------------------

def tile(name, sub, icon, color):
    """MoreTile: icon in a tinted circle, title one line, two lines about it; tiles of a row are of one height."""
    clamp = 'overflow: hidden; display: -webkit-box; -webkit-box-orient: vertical; -webkit-line-clamp: %d;'
    return ('<div style="padding: 16px; border-radius: 24px; background: %s; border: 1px solid %s; min-height: 132px; box-sizing: border-box; '
            'display: flex; flex-direction: column; gap: 4px; min-width: 0;">%s<div style="height: 8px;"></div>'
            '<div style="font-size: 16px; font-weight: 600; white-space: nowrap; overflow: hidden; text-overflow: ellipsis;">%s</div>'
            '<div style="font-size: 12px; line-height: 1.35; color: %s; min-height: 2.7em; %s">%s</div></div>'
            % (CARD, LINE, tile_icon(icon, color), name, MUTED, clamp % 2, sub))


def tiles_body(t):
    grid = ('<div style="margin-top: 10px; display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 10px;">%s</div>'
            % ''.join(tile(*g) for g in t['groups']))
    return head(t) + grid


# ---- В: group cards with settings inside -----------------------------------------------------------------------------

def section(name, icon, color, inner, all_):
    """CoShellFormSection: caps title (+ its icon), «Все ›» on the right, the group's main settings in the card."""
    title = ('<div style="display: flex; align-items: center; gap: 8px; margin: 18px 4px 8px;">%s'
             '<span style="flex-grow: 1; min-width: 0; font-size: 13px; font-weight: 600; letter-spacing: 0.04em; text-transform: uppercase; color: %s;">%s</span>'
             '%s</div>'
             % (dot_icon(icon, color, 32), MUTED, name,
                '<span style="flex: none; font-size: 14px; font-weight: 600; color: %s;">%s ›</span>' % (CYAN, all_) if all_ else ''))
    return title + inner


def sw(title, sub, on):
    return row(title, sub, '', switch(on), min_h=52)


def val(title, sub, beta=False):
    if beta:  # «бета» after the title (#643 «А», poll 2026-11-23)
        title += ' <span style="display: inline-flex; vertical-align: middle; margin-left: 4px;">%s</span>' % status_chip('бета', 'manual')
    return row(title, sub, '', chevron(), min_h=52)


def cards_sections(t):
    g = {n: (n, i, c) for n, _, i, c in t['groups']}
    names = [x[0] for x in t['groups']]
    theme, opts = t['theme']
    look = card('<div style="padding: 12px 16px 4px;"><div style="font-size: 15px; margin-bottom: 10px;">%s</div>%s</div>'
                % (theme, segmented(opts, opts[1])) + group([val(*t['palette'])]))
    return [
        section(*g[names[0]], look, t['all']),
        section(*g[names[3]], group([sw(*t['input'][0], True), sw(*t['input'][1], True)]), t['all']),
        section(*g[names[4]], group([sw(*t['notices'][0], True), val(*t['notices'][1])]), t['all']),
        section(*g[names[2]], group([val(*m) for m in t['money']]), t['all']),
        section(*g[names[1]], group([val(*t['screens'])]), t['all']),
        section(*g[names[5]], group([val(*t['data'][0]), sw(*t['data'][1], False)]), t['all']),
        section(*t['smart'][0], group([val(*t['smart'][1], beta=True), val(*t['smart'][2], beta=True)]), ''),
        section(*t['help'][0], group([sw(*t['help'][1], True), val(*t['help'][2])]), ''),
    ]


def cards_top(t):
    return head(t) + ''.join(cards_sections(t)[:3])


def cards_rest(t):
    return ''.join(cards_sections(t)[3:])


def stress(t, body):
    return '<div style="%s">%s</div>' % (STRESS, body)


BOARDS = []  # picked В: now boards Set1Root, Set1RootMore, Set1RootEs (mockup_screens_fb_set.py)
