"""Generated boards: «Ещё → О приложении» (#440, poll 2026-11-23, variant А): the update screen grows an «about» block.

Icon, name, version, a short description and three principles on top; the update (ring state, versions, «Что нового»,
«Прошлые версии», «Установить вручную») stays below as it is on the `Update*` boards. «Ещё» → «Приложение» gets the row
«О приложении» instead of the quiet version line. Imported by mockup_screens_feedback.py (FEEDBACK + BOARDS).
"""
from mockup_bricks import *  # noqa: F401,F403
from mockup_parts import footer, top_bar
from mockup_screens_fb_nov12 import NOTES, manual_link, notes_card, versions
import mockup_screens_fb_menu as menu

FEEDBACK = [
    (440, 'Отзыв #440 · «Ещё → О приложении» · 2026-11-23',
     'Опрос «А» (правки владельца: «Общий бюджет семьи», не «на двоих»; больше цветов темы в значках): экран «Обновление» становится «О приложении». Сверху — иконка, название, версия, два предложения о том, что это, '
     'и три принципа; ниже всё, что было на экране обновления. В «Ещё → Приложение» строка «О приложении» с версией вместо тихой строки внизу.'),
]

SHIELD = 'M12 3l8 3v6c0 4.5-3.4 8.2-8 9-4.6-.8-8-4.5-8-9V6zM9 12l2 2 4-4'
PRINCIPLES = [('wallet', 'Один бюджет на всех', 'Участников сколько нужно, валют сколько нужно', LIME), ('rate', 'Работает без интернета', 'Записи сохраняются на телефоне и отправятся, когда появится сеть', CYAN),
              (SHIELD, 'Данные у вас', 'Резервную копию можно сохранить файлом, а всё стереть одним действием', VIOLET)]


def app_head(sub):
    icon = ('<span style="width: 84px; height: 84px; border-radius: 22px; background: #050805; display: flex; align-items: center; justify-content: center; '
            'border: 1px solid %s;">%s</span>' % (LINE, svg('wallet', '#B8FF2E', 44, 2.2)))
    return ('<div style="display: flex; flex-direction: column; align-items: center; padding: 8px 16px 0;">%s'
            '<h2 style="margin: 14px 0 0; font-size: 24px;">Co-Shell</h2><div style="margin-top: 4px; font-size: 13px; color: %s;">%s</div></div>' % (icon, MUTED, sub))


def about_card():
    text = 'Бюджет для всей семьи. Записывайте траты за пару нажатий и смотрите, куда уходят деньги.'
    rows = [row(t, s, tile_icon(i, c), '', min_h=56) for i, t, s, c in PRINCIPLES]
    return ('<div style="padding: 0 16px;">%s%s</div>' % (card('<div style="font-size: 14px; line-height: 1.45;">%s</div>' % text, pad='14px', extra='margin-top: 18px;'),
                                                           label('Принципы', top=14) + group(rows)))


def links():
    return group([row('Отправить отзыв', 'Ошибка или идея', tile_icon('mail', PINK), chevron(), min_h=56)])


def credits():
    """Authors (#761, poll 2026-11-26: «скромно и внизу», round 2 «Д»): two quiet centred lines after «Отправить отзыв»."""
    return ('<div style="margin-top: 20px; text-align: center; font-size: 12px; line-height: 1.6; color: %s;">'
            'Идеи и тестирование — Анастасия Карачевцева<br>Разработка — Александр Карачевцев · вместе с Claude</div>' % MUTED)


def older():
    return ('<div style="margin-top: 12px; padding-top: 10px; border-top: 1px solid %s; display: flex; font-size: 13px; color: %s;">'
            '<span style="flex-grow: 1;">Прошлые версии: 1.1, 1.0, 0.57…</span>%s</div>' % (DIV, MUTED, chevron()))


def about_latest():
    mine = card('<div style="display: flex; align-items: center; gap: 12px;">%s<span style="flex-grow: 1; display: flex; flex-direction: column; gap: 2px;">'
                '<span style="font-size: 12px; color: %s;">Ваша версия</span><span style="%s font-size: 20px; font-weight: 700;">1.2</span>'
                '<span style="font-size: 12px; color: %s;">сборка 512</span></span><span style="font-size: 12px; color: %s; text-align: right;">актуальная<br>проверили только что</span></div>'
                % (tile_icon('check', LIME), MUTED, MONO, MUTED, MUTED), pad='14px', extra='margin-top: 12px;')
    body = (app_head('Версия 1.2 · сборка 512') + about_card() + '<div style="padding: 0 16px;">%s%s%s</div>'
            % (mine, notes_card('Что в этой версии', '', NOTES, older()), '<div style="margin-top: 14px;">%s</div>' % links() + credits()))
    return top_bar('О приложении') + '<div style="height: 690px; overflow: hidden;">%s</div>' % body + footer(button('Проверить обновление', 'outline', 'refresh', 0))


def about_update():
    body = (app_head('Версия 1.2 · сборка 512') + '<div style="padding: 0 16px;">%s%s</div>'
            % (versions(('1.2', 'сборка 512'), ('1.3', 'сборка 520')), notes_card('Что нового в 1.3', '3 пункта', NOTES)) + about_card())
    return top_bar('О приложении') + '<div style="height: 640px; overflow: hidden;">%s</div>' % body + footer(button('Обновить', 'primary', 'download', 0), manual_link())


def more_app(update=False):
    info = row('О приложении', 'Вышла версия 1.3' if update else 'Версия 1.2 · актуальная', tile_icon('info', LIME),
               '<span style="flex: none; height: 22px; padding: 0 8px; border-radius: 999px; background: %s; color: %s; font-size: 11px; font-weight: 600; line-height: 22px;">Новое</span>'
               % (LIME, ONLIME) if update else chevron(), min_h=56)
    app = group([menu.r('Настройки', 'Тема, ввод, уведомления, экраны', 'gear', VIOLET), menu.r('Конфиденциальность', 'PIN, отпечаток, скрытие сумм', SHIELD, CYAN),
                 menu.r('Обратная связь', 'Ошибка или идея', 'mail', PINK), info])
    return tab_screen('Ещё', label('Семья и данные', top=6) + menu.new_data() + label('Приложение', top=14) + app, 'more')


BOARDS = [
    ('AboutLatest', '«О приложении»: версия актуальная', about_latest, 440),
    ('AboutUpdate', '«О приложении»: вышло обновление', about_update, 440),
    ('MoreAbout', '«Ещё»: строка «О приложении»', lambda: more_app(False), 440),
    ('MoreAboutUpdate', '«Ещё»: «О приложении», вышла версия', lambda: more_app(True), 440),
]
