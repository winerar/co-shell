#!/usr/bin/env python3
"""Regenerate the «Воркфлоу» board (docs/design/mockups/Flows.dc.html) from the ROWS table below.

Usage: scripts/agent/gen-flows-board.py [out.html]. Then bump its `h` in canvas.json if the row count changed
(printed) and publish both files to the design canvas (docs/design/README.md).
"""
import os
import sys

# kind: p — step without a board, l — opens a board (href), g — lime («+» and its answer), n — no board yet (dashed)
ROWS = [
    ("Вкладки", "Панель внизу; на внутренних экранах уезжает. #112: листать свайпом?", False, [
        ("Обзор", "баланс, плитки, последние", "SyncA.dc.html", "l"),
        ("Операции", "поиск, фильтры", "Operations.dc.html", "l"),
        ("«+»", "новая операция", "Main.dc.html", "g"),
        ("Счета", "группы, итоги", "Accounts.dc.html", "l"),
        ("Ещё", "плитки разделов", "More.dc.html", "l"),
    ]),
    ("Новая операция", "Одна шторка, всё под большим пальцем; встряхнуть телефон — тоже", True, [
        ("«+» на панели", "или встряхнуть на вкладке", None, "g"),
        ("Шаг 1", "счёт: недавние, группы, поиск", "Main.dc.html", "l"),
        ("Шаг 2", "тип, сумма, категория", "Main.dc.html", "l"),
        ("Чек в др. валюте", "по желанию: ARS → списано USDT", "Main.dc.html", "l"),
        ("«Готово»", "на клавиатуре", None, "p"),
        ("«+» → ✓", "тёплая вспышка", None, "g"),
    ]),
    ("Удаление", "Ничего не теряется сразу. Отзыв #112: свайп убрать — ждёт макета", True, [
        ("Операции", "строка", "Operations.dc.html", "l"),
        ("Свайп влево", "удалить", None, "p"),
        ("Снэкбар", "«Отменить», полоска времени", None, "p"),
    ]),
    ("Дубль", "Повтор покупки. Отзыв #112: свайп убрать — ждёт макета", True, [
        ("Операции", "строка", "Operations.dc.html", "l"),
        ("Свайп вправо", "дублировать", None, "p"),
        ("Шторка", "с копией, дата — сегодня", "Main.dc.html", "l"),
        ("Сохранить", "", None, "p"),
    ]),
    ("Без категории", "Плитка «Внимание» ведёт к тому, что надо поправить", True, [
        ("Обзор", "плитка «Внимание»", "SyncA.dc.html", "l"),
        ("Операции", "фильтр «Без категории · Этот месяц»", "Operations.dc.html", "l"),
        ("Тап по строке", "шторка операции", "Main.dc.html", "l"),
        ("Категория", "сохранить", None, "p"),
        ("Чип ✕", "снять фильтр", None, "p"),
    ]),
    ("Курсы", "Нет курса — подсказка на «Обзоре»", True, [
        ("Обзор", "«Внимание»: нет курса", "SyncA.dc.html", "l"),
        ("Курсы валют", "загружены раз в день", "Rates.dc.html", "l"),
        ("Тап по валюте", "или «+ Добавить курс»", "Rates.dc.html", "l"),
        ("Ручной курс", "важнее загруженного", None, "p"),
    ]),
    ("Счёт", "Цвет — от банка или из палитры", True, [
        ("Счета", "«+» или тап по счёту", "Accounts.dc.html", "l"),
        ("Редактор счёта", "тип, валюта, баланс", "AccountEdit.dc.html", "l"),
        ("Цвет", "«Похоже на банк» → Взять", "Colors.dc.html", "l"),
        ("Сохранить", "", None, "p"),
    ]),
    ("Категории", "Один «+» для категорий и папок", True, [
        ("Ещё", "Категории", "More.dc.html", "l"),
        ("Категории", "Расходы | Доходы", "Categories.dc.html", "l"),
        ("«+ Добавить»", "одна кнопка", "Categories.dc.html", "l"),
        ("Категория | Папка", "переключатель в редакторе", "CategoryEdit.dc.html", "l"),
        ("Сохранить", "", None, "p"),
    ]),
    ("Обновление", "Без диалога при запуске", True, [
        ("Вышла версия", "проверка раз в 15 мин", None, "p"),
        ("Пилюля", "над панелью + точка на «Ещё»", "UpdatePill.dc.html", "l"),
        ("Экран обновления", "скачивание, проценты", "Update.dc.html", "l"),
        ("Установка APK", "системный диалог", None, "p"),
    ]),
    ("Семья", "Общий бюджет на двоих", True, [
        ("Ещё → Семья", "", "Family.dc.html", "l"),
        ("Вход Google", "", None, "p"),
        ("Создать семью", "", None, "p"),
        ("Пригласить", "по адресу Google", "Family.dc.html", "l"),
        ("Второй телефон", "«Присоединиться»", None, "p"),
        ("Синхронизация", "за 2–3 с; запасной опрос — раз в минуту", "Family.dc.html", "l"),
    ]),
    ("Ошибка синка", "Причина словами, не тревожим из-за минутного сбоя", True, [
        ("Синк не прошёл", "сеть/сервер — 2 мин, вход/доступ — сразу", None, "p"),
        ("Плашка", "над балансом «Обзора»", "SyncA.dc.html", "l"),
        ("Повторить", "или «Войти» → Семья", "SyncA.dc.html", "l"),
        ("Отзыв", "форма с кодом ошибки", "Feedback.dc.html", "l"),
    ]),
    ("Резервная копия", "Один файл со всем; восстановление добавляет записи", True, [
        ("Ещё → Резервная копия", "", "Backup.dc.html", "l"),
        ("Автокопия", "раз в неделю в выбранную папку", "Backup.dc.html", "l"),
        ("Сохранить копию", "вручную, в папку или на Диск", "Backup.dc.html", "l"),
        ("Восстановить", "записи добавятся к текущим", "Backup.dc.html", "l"),
    ]),
    ("Импорт из Wallet", "Мастер по шагам; можно отменить пакетом", True, [
        ("Ещё → Импорт", "выбрать файл выгрузки", None, "n"),
        ("Сводка", "сколько счетов и операций", None, "n"),
        ("Счета и категории", "сопоставить или создать", None, "n"),
        ("Переводы", "без пары — решить", None, "n"),
        ("Проверка", "копия перед записью", None, "n"),
        ("Готово", "«Отменить импорт»", None, "n"),
    ]),
    ("Стереть всё", "Чтобы загрузить Wallet на чистую", True, [
        ("Настройки → Данные", "Стереть все данные", "Wipe.dc.html", "l"),
        ("Копия", "в «Загрузки»", "Wipe.dc.html", "l"),
        ("Слово СТЕРЕТЬ", "подтверждение", "Wipe.dc.html", "l"),
        ("Стёрто у семьи", "второй телефон — при синке", "Wipe.dc.html", "l"),
        ("Импорт из Wallet", "следующий шаг", None, "n"),
    ]),
    ("Обратная связь", "Отзыв сразу становится задачей", True, [
        ("Ещё → Обратная связь", "", "Feedback.dc.html", "l"),
        ("Тип и текст", "до 500 знаков", "Feedback.dc.html", "l"),
        ("Отправить", "версия и экран — приложим", "Feedback.dc.html", "l"),
        ("Задача", "issue «feedback» за несколько часов", None, "p"),
    ]),
    ("Настройки", "Тема раскрывается кругом", True, [
        ("Ещё → Настройки", "", "Settings.dc.html", "l"),
        ("Тема", "светлая / тёмная / системная", "Settings.dc.html", "l"),
        ("Палитра", "6 карточек", "Settings.dc.html", "l"),
        ("Применено", "сразу, без перезапуска", None, "p"),
    ]),
]

TOP0, STEP, BAND_H, X0, DX, W, H = 184, 132, 112, 300, 256, 196, 76
height = TOP0 + STEP * len(ROWS) + 56
NODE = ("position: absolute; left: {x}px; top: {y}px; width: 196px; height: 76px; box-sizing: border-box; "
        "padding: 8px 12px; display: flex; align-items: center; justify-content: center; text-align: center; gap: 8px; "
        "background: {bg}; color: {ink}; border: {border}; border-radius: 16px; font-size: 15px; line-height: 20px; "
        "text-decoration: none;")


def node(x, y, title, sub, href, kind):
    lime = kind == "g"
    bg, ink, subc = ("#B8FF2E", "#0E1F00", "#2D4A00") if lime else ("#161D12", "#EEF5E6", "#9AA594")
    border = {"g": "none", "l": "1px solid #3A4A1E", "p": "1px solid #222B1E", "n": "1px dashed #8A7A3A"}[kind]
    if lime and href:
        border = "none"
    style = NODE.format(x=x, y=y, bg=bg, ink=ink, border=border)
    inner = f"<b>{title}</b>" + (f'<br><span style="font-size: 12px; color: {subc};">{sub}</span>' if sub else "")
    tag = "a" if href else "div"
    attr = f'href="{href}" ' if href else ""
    return f'<{tag} {attr}style="{style}"><span>{inner}</span></{tag}>'


arrow_id = 0


def arrow(x, y):
    global arrow_id
    arrow_id += 1
    return (f'<svg width="59" height="8" viewBox="0 0 59 8" preserveAspectRatio="none" style="position: absolute; '
            f'left: {x}px; top: {y}px; width: 59px; height: 8px; overflow: visible; fill: none; stroke: #7D8A76; '
            f'stroke-width: 2; stroke-linecap: round; stroke-linejoin: round"><defs><marker id="dc-arrow-head-filled-{arrow_id}" '
            f'orient="auto" markerWidth="5" markerHeight="5" refX="2" refY="2" overflow="visible"><path d="M0 0 L4 2 L0 4 Z" '
            f'fill="#7D8A76" stroke="none" style="stroke: none; fill: #7D8A76; fill: context-stroke"/></marker></defs>'
            f'<path d="M 0 4 L 55 4" marker-end="url(#dc-arrow-head-filled-{arrow_id})"></path></svg>')


bands, arrows, nodes, labels = [], [], [], []
for i, (title, sub, chain, steps) in enumerate(ROWS):
    top = TOP0 + STEP * i
    bg = "#0E140B" if i % 2 == 0 else "#0A0F08"
    bands.append(f'<div style="position: absolute; left: 32px; top: {top}px; width: 1776px; height: {BAND_H}px; border-radius: 20px; background: {bg};"></div>')
    labels.append(f'<div style="position: absolute; left: 64px; top: {top + 24}px; width: 196px; font-size: 18px; line-height: 24px; font-weight: 700; color: #EEF5E6;">{title}</div>')
    labels.append(f'<div style="position: absolute; left: 64px; top: {top + 52}px; width: 196px; font-size: 13px; line-height: 18px; color: #9AA594;">{sub}</div>')
    assert len(steps) <= 6, title
    for k, (t, s, href, kind) in enumerate(steps):
        x = X0 + DX * k
        nodes.append(node(x, top + 16, t, s, href, kind))
        if chain and k:
            arrows.append(arrow(x - 60, top + 50))

head = """<!doctype html>
<html lang="ru">
<head>
<meta charset="utf-8">
<title>Воркфлоу</title>
<script src="./support.js"></script>
</head>
<body>
<x-dc>
<helmet>
<link href="https://fonts.googleapis.com/css2?family=Onest:wght@400;500;600;700&amp;display=swap" rel="stylesheet">
<style>
body{margin:0}
a{color:#B8FF2E}a:hover{color:#D6FF7A}
</style>
</helmet>
"""
body = [f'<div style="position: relative; width: 1840px; height: {height}px; background: #050805; font-family: Onest, system-ui, sans-serif; color: #EEF5E6; overflow: hidden;">']
body += bands + arrows + nodes + labels
body.append('<div style="position: absolute; left: 64px; top: 56px; font-size: 34px; line-height: 40px; font-weight: 700; color: #EEF5E6;">Воркфлоу: как проходят главные действия</div>')
body.append('<div style="position: absolute; left: 64px; top: 108px; width: 1500px; font-size: 15px; line-height: 22px; color: #9AA594;">'
            'Карточки с зелёной рамкой открывают макет экрана (кнопка Play). Лайм — «+» и его ответ после сохранения. '
            'Пунктир — экран есть в приложении, но макета ещё нет (задача «интерфейс 1 в 1 к мокапам»). Сверено с кодом 10 ноября.</div>')
body.append("</div>")
tail = f"""</x-dc>
<script type="text/x-dc" data-dc-script data-props='{{"$preview":{{"width":1840,"height":{height}}}}}'>
class Component extends DCLogic {{
  renderVals() {{ return {{}}; }}
}}
</script>
</body>
</html>
"""
out = sys.argv[1] if len(sys.argv) > 1 else os.path.join(os.path.dirname(__file__), "../../docs/design/mockups/Flows.dc.html")
open(out, "w").write(head + "\n".join(body) + "\n" + tail)
print("height", height)
