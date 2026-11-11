"""Generated boards with logic: «Тема макетов» switch, «Настройки» (its theme pickers drive the canvas theme)."""
from mockup_bricks import *  # noqa: F401,F403

# Palette swatches are written without «#» so mockup_theme.py leaves them alone (they must not follow the theme).
PALETTES = [('Вольт', 'B8FF2E', 'B266FF', '00F0FF', 'кислотный лайм и циан'), ('Мохито', '91E668', 'A77BFF', '38D9FF', 'мягкий лайм и фиолет'),
            ('Манго', 'FF8A3D', 'FF3D7F', 'FFC93C', 'тёплый закат: манго и малина'), ('Ультра', '3D7BFF', 'FF2BD6', '91E668', 'электрик и маджента'),
            ('Лагуна', '2EE6C5', 'FF6B5C', 'FFD27A', 'бирюза и коралл'), ('Сакура', 'FF7AB8', '7CF2C4', 'B9A4FF', 'розовый и мята')]

SCRIPT = '''
  renderVals() {
    var s = {};
    try { s = JSON.parse(window.localStorage.getItem('coshell-mock-theme') || '{}') || {}; } catch (e) { s = {}; }
    var dark = s.dark !== false, pal = s.pal || 'Вольт', self = this;
    var save = function (o) {
      var n = { dark: dark, pal: pal };
      for (var k in o) n[k] = o[k];
      try { window.localStorage.setItem('coshell-mock-theme', JSON.stringify(n)); } catch (e) {}
      self.forceUpdate();
    };
    var P = %s;
    var seg = function (on) { return 'height: 40px; border: none; border-radius: 999px; font-size: 14px; ' + (on ? 'background: #B8FF2E; color: #0E1F00; font-weight: 600;' : 'background: transparent; color: #C9D3C2;'); };
    return {
      modes: [['Системная', null], ['Светлая', false], ['Тёмная', true]].map(function (m) {
        var on = m[1] === null ? false : m[1] === dark;
        return { label: m[0], style: seg(on), pick: function () { save({ dark: m[1] === null ? true : m[1] }); } };
      }),
      dark2: [['Тёмная', true], ['Светлая', false]].map(function (m) {
        return { label: m[0], style: seg(m[1] === dark), pick: function () { save({ dark: m[1] }); } };
      }),
      palettes: P.map(function (p) {
        return { name: p[0], hint: p[4], a: '#' + p[1], b: '#' + p[2], c: '#' + p[3],
          border: p[0] === pal ? '2px solid #B8FF2E' : '1px solid #222B1E', pick: function () { save({ pal: p[0] }); } };
      }),
      current: (dark ? 'Тёмная' : 'Светлая') + ' · ' + pal
    };
  }
''' % repr([list(p) for p in PALETTES]).replace("'", '"')

PALETTE_GRID = '''<div style="display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 8px;">
    <sc-for list="{{palettes}}" as="p" hint-placeholder-count="6">
      <button type="button" onClick="{{p.pick}}" style="padding: 12px; border-radius: 16px; background: #10160D; border: {{p.border}}; color: #EEF5E6; text-align: left; display: flex; flex-direction: column; gap: 8px; cursor: pointer;">
        <span style="display: flex; gap: 4px;"><span style="width: 26px; height: 26px; border-radius: 50%; background: {{p.a}};"></span><span style="width: 26px; height: 26px; border-radius: 50%; background: {{p.b}};"></span><span style="width: 26px; height: 26px; border-radius: 50%; background: {{p.c}};"></span></span>
        <span style="font-size: 15px; font-weight: 600;">{{p.name}}</span>
        <span style="font-size: 12px; color: #9AA594;">{{p.hint}}</span>
      </button>
    </sc-for>
  </div>'''


def with_logic(html, height=844):
    html = html.replace("renderVals() { return {}; }", SCRIPT.strip())
    return html.replace('"height":844', '"height":%d' % height).replace('height: 844px', 'height: %dpx' % height)


def theme_switch():
    body = ('<div style="padding: 20px 16px;"><h1 style="margin: 0; font-size: 22px; font-weight: 700;">Тема макетов</h1>'
            '<p style="margin: 6px 0 14px; font-size: 13px; line-height: 1.4; color: #9AA594;">Перекрашивает все доски холста — как «Настройки → Тема» в приложении. '
            'Сейчас: <b style="color: #EEF5E6;">{{current}}</b></p>'
            '<div style="display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); padding: 4px; border-radius: 999px; background: #1B2317; margin-bottom: 12px;">'
            '<sc-for list="{{dark2}}" as="m" hint-placeholder-count="2"><button type="button" onClick="{{m.pick}}" style="{{m.style}}">{{m.label}}</button></sc-for></div>'
            + PALETTE_GRID + '<p style="margin: 12px 0 0; font-size: 12px; line-height: 1.4; color: #7D8A76;">Одна доска в другой теме — Tweaks на ней: «Тема», «Палитра».</p></div>')
    return with_logic(board('Тема макетов', body), 560)


def settings():
    modes = ('<div style="display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); padding: 4px; border-radius: 999px; background: #1B2317;">'
             '<sc-for list="{{modes}}" as="m" hint-placeholder-count="3"><button type="button" onClick="{{m.pick}}" style="{{m.style}}">{{m.label}}</button></sc-for></div>')
    rest = group([row('Валюты отчётов', 'Основная: USD', tile_icon('rate'), chevron()), row('Встряхнуть — новая операция', 'Встряхните телефон, пока открыто приложение', tile_icon('spark'), switch(True)),
                  row('Обновление приложения', 'Версия 1.43 · последняя', tile_icon('download'), chevron())])
    danger = group([row('Стереть все данные', 'Чтобы загрузить Wallet на чистую. Сначала — копия', tile_icon('trash', PINK, PINKBG), chevron(), PINK)])
    body = label('Тема', top=4) + modes + label('Палитра', top=16) + PALETTE_GRID + label('Общее') + rest + label('Данные') + danger
    return with_logic(board('Настройки', inner_screen('Настройки', body)), 1060)


IDEAS = [
    ('Обзор', 'Бюджет месяца — кольцо', 'Плитка «Траты за ноябрь» показывает только сумму.', 'Кольцо «потрачено из плана» и «осталось в день»; план — одной цифрой в настройках.', 'M'),
    ('Обзор', 'Первый запуск — чек-лист', 'Пустой «Обзор» говорит «Счетов пока нет».', 'Три шага карточками: добавить счёт → импорт из Wallet → пригласить семью; исчезает, когда всё сделано.', 'S'),
    ('Шторка', 'Подсказка категории', 'Категории всегда в одном порядке.', 'Первые 4 — самые частые для этого счёта и времени дня; остальное как сейчас. Меньше тапов в 80 % записей.', 'M'),
    ('Операции', 'Итог фильтра в шапке', 'Чтобы узнать «сколько на кафе в октябре», надо считать в уме.', 'При активном фильтре — строка «12 операций · −84 300 ARS ≈ 82 USD» над списком; липкий заголовок дня.', 'S'),
    ('Счета', 'Доли валют в герое', 'Итог один, не видно, где лежат деньги.', 'Тонкая полоса долей ARS / USD / AMD / USDT под общим балансом, тап — разбивка.', 'S'),
    ('Синк', 'Тихий индикатор', 'Плашка есть только при ошибке — не видно, что всё хорошо.', 'Точка у заголовка «Обзора»: зелёная — синхронно, жёлтая — ждут отправки; тап — «Семья».', 'S'),
    ('Удаление', '«Отменить» вместо диалогов', 'Удаление счёта/категории без операций — диалог «Удалить?».', 'Удалять сразу со снэкбаром «Отменить», как операции; диалог — только когда удалить нельзя.', 'S'),
    ('Формы', 'Поле с заливкой (К6)', 'Подпись «садится» на рамку, формы выглядят по-разному.', 'Одно поле: заливка, рамка 1 dp, радиус 12, подпись внутри — как на всех новых досках.', 'M'),
    ('Списки', 'Строки в карточках (К4, К5)', 'Настройки, Семья, Курсы — строки на фоне, 4 вида заголовков групп.', 'CoShellRowGroup + CoShellSectionLabel: карточка с разделителями и один стиль заголовка.', 'M'),
    ('Курсы', 'Спарклайн курса', 'Видна только сегодняшняя цифра.', 'Мини-график за 30 дней в строке валюты и % за месяц — видно, когда менять деньги.', 'M'),
    ('Импорт', 'Пропускать пустые шаги', '7 шагов, даже когда решать нечего.', 'Шаги без вопросов (всё сопоставлено) проходятся сами; на «Проверке» — что было решено автоматически.', 'S'),
    ('Доступность', 'Крупный шрифт', 'Длинные суммы ARS/AMD не проверены при шрифте 130 %.', 'Доски-проверки с fontScale 1.3: сумма переносится под название, а не обрезается.', 'S'),
    ('Быстрый ввод', 'Плитка и виджет «+»', 'Записать трату — открыть приложение и нажать «+».', 'Плитка быстрых настроек и виджет 2×1 сразу открывают шторку на шаге 2 с последним счётом.', 'M'),
    ('Светлая тема', 'Довести контраст', 'Светлые доски теперь видны: неон на белом местами бледный.', 'Пройтись по доскам в светлой теме, поправить пастельные подписи (участники, плашки) до 4.5:1.', 'S'),
]


def ideas():
    cards = ''
    for i, (area, title, now, prop, size) in enumerate(IDEAS, 1):
        cards += ('<div style="padding: 18px; border-radius: 20px; background: #10160D; border: 1px solid #222B1E; display: flex; flex-direction: column; gap: 10px;">'
                  '<div style="display: flex; align-items: center; gap: 8px;"><span style="%s font-size: 13px; color: #0E1F00; background: #B8FF2E; border-radius: 999px; padding: 2px 9px; font-weight: 700;">%d</span>'
                  '<span style="font-size: 12px; font-weight: 600; color: #B8FF2E; letter-spacing: 0.04em; text-transform: uppercase;">%s</span><span style="flex-grow: 1;"></span>'
                  '<span style="font-size: 12px; color: #9AA594;">объём %s</span></div><div style="font-size: 18px; font-weight: 700;">%s</div>'
                  '<div style="font-size: 13px; line-height: 1.45; color: #9AA594;"><b style="color: #C9D3C2;">Сейчас:</b> %s</div>'
                  '<div style="font-size: 14px; line-height: 1.45;"><b style="color: #B8FF2E;">Предлагаю:</b> %s</div></div>' % (MONO, i, area, size, title, now, prop))
    body = ('<div style="padding: 40px;"><h1 style="margin: 0; font-size: 36px;">Предложения по улучшению</h1>'
            '<p style="margin: 8px 0 24px; font-size: 15px; color: #9AA594;">Объём: S — до дня, M — 2–3 дня. Ниже на этой вкладке — макеты вариантов для 3–12 (рекомендация — в названии доски). Выберите букву — переношу на «Экраны» и в код.</p>'
            '<div style="display: grid; grid-template-columns: repeat(3, minmax(0, 1fr)); gap: 16px;">%s</div></div>' % cards)
    html = board('Предложения', body)
    return html.replace('width: 390px; height: 844px', 'width: 1280px; height: 1500px').replace('"width":390,"height":844', '"width":1280,"height":1500')
