"""Privacy policy and data-deletion pages (#652) for the landing site: privacy/<lang>/ and delete/<lang>/.

Plain static HTML, no scripts, no trackers. Texts must match what the app really does (docs/research/play-release-plan.md §2, §5):
data lives on the phone; the family cloud is Firestore; exchange rates and updates are the only other traffic.
"""
import html

LANGS = ["ru", "en", "es"]

TEXTS = {
    "ru": {
        "privacy_title": "Политика конфиденциальности — Co-Shell", "delete_title": "Удаление данных — Co-Shell",
        "back": "На главную", "privacy_link": "Конфиденциальность", "delete_link": "Удалить данные",
        "privacy": [
            ("h2", "Что хранится"),
            ("p", "Операции, счета, категории и бюджеты хранятся на вашем телефоне. Приложение работает без интернета и не требует учётной записи."),
            ("h2", "Облако семьи (необязательно)"),
            ("p", "Если вы включили «Семью», общие данные синхронизируются через Google Firestore: только участникам вашей семьи. Для входа используется ваша учётная запись Google; мы храним её идентификатор и имя участника."),
            ("h2", "Что ещё уходит в интернет"),
            ("ul", ["Курсы валют: приложение скачивает общедоступные курсы; ваши операции и суммы туда не передаются.",
                    "Отзыв: то, что вы написали сами, файлы, которые вы приложили (например, образец чека), версия приложения, модель телефона, случайный номер установки и статус синхронизации. Операции и суммы не прикладываются.",
                    "Код приглашения (закрытое тестирование): код и случайный номер установки сохраняются в нашей базе, чтобы считать телефоны на один код; приложение иногда проверяет, действует ли код.",
                    "Обновления: приложение проверяет, вышла ли новая версия, и скачивает её с GitHub."]),
            ("h2", "Что мы не делаем"),
            ("p", "Не показываем рекламу, не продаём и не передаём данные третьим лицам, не собираем аналитику о ваших тратах."),
            ("h2", "Безопасность"),
            ("p", "Обмен данными идёт только по HTTPS. Доступ к приложению можно закрыть PIN-кодом или отпечатком, суммы можно скрыть. Доступ к облачным данным семьи имеют только её участники."),
            ("h2", "Ваши права"),
            ("p", "Вы можете в любой момент удалить свои данные: как — на странице «Удалить данные»."),
        ],
        "delete": [
            ("h2", "Данные на телефоне"),
            ("p", "Удалите приложение — данные на телефоне исчезнут. Или нажмите «Стереть все данные» в разделе «Ещё → Резервная копия»."),
            ("h2", "Облако семьи"),
            ("ul", ["«Ещё → Семья → Выйти из семьи»: вы перестанете видеть общие данные, на телефоне они останутся.",
                    "«Ещё → Семья → Удалить семью» (для владельца семьи): общие данные удалятся из облака у всех участников."]),
            ("h2", "Не получается удалить"),
            ("p", "Напишите нам из приложения: «Ещё → Отзыв». Укажите, какие данные удалить; ответим и удалим в разумный срок."),
        ],
    },
    "en": {
        "privacy_title": "Privacy policy — Co-Shell", "delete_title": "Delete your data — Co-Shell",
        "back": "Home", "privacy_link": "Privacy", "delete_link": "Delete data",
        "privacy": [
            ("h2", "What is stored"),
            ("p", "Transactions, accounts, categories and budgets are stored on your phone. The app works offline and needs no account."),
            ("h2", "Family cloud (optional)"),
            ("p", "If you turn on “Family”, shared data syncs through Google Firestore, only for the members of your family. Sign-in uses your Google account; we store its identifier and your member name."),
            ("h2", "What else goes online"),
            ("ul", ["Exchange rates: the app downloads public rates; your transactions and amounts are not sent.",
                    "Feedback: what you wrote, files you attached yourself (for example, a receipt sample), the app version, the phone model, a random installation number and the sync status. Transactions and amounts are not attached.",
                    "Invitation code (closed test): the code and a random installation number are stored in our database to count phones per code; the app checks now and then whether the code is still valid.",
                    "Updates: the app checks whether a new version is out and downloads it from GitHub."]),
            ("h2", "What we do not do"),
            ("p", "We show no ads, do not sell or share your data with third parties, and collect no analytics about your spending."),
            ("h2", "Security"),
            ("p", "Data travels over HTTPS only. You can lock the app with a PIN or fingerprint and hide amounts. Only family members can reach the family cloud data."),
            ("h2", "Your rights"),
            ("p", "You can delete your data at any time: see the “Delete data” page."),
        ],
        "delete": [
            ("h2", "Data on your phone"),
            ("p", "Uninstall the app and the data on the phone is gone. Or tap “Erase all data” in “More → Backup”."),
            ("h2", "Family cloud"),
            ("ul", ["“More → Family → Leave family”: you stop seeing shared data; it stays on your phone.",
                    "“More → Family → Delete family” (family owner): shared data is removed from the cloud for every member."]),
            ("h2", "Cannot delete?"),
            ("p", "Write to us from the app: “More → Feedback”. Say which data to delete; we will reply and delete it within a reasonable time."),
        ],
    },
    "es": {
        "privacy_title": "Política de privacidad — Co-Shell", "delete_title": "Eliminar tus datos — Co-Shell",
        "back": "Inicio", "privacy_link": "Privacidad", "delete_link": "Eliminar datos",
        "privacy": [
            ("h2", "Qué se guarda"),
            ("p", "Los movimientos, cuentas, categorías y presupuestos se guardan en tu teléfono. La app funciona sin conexión y no pide una cuenta."),
            ("h2", "Nube de la familia (opcional)"),
            ("p", "Si activas «Familia», los datos compartidos se sincronizan con Google Firestore, solo para los miembros de tu familia. El acceso usa tu cuenta de Google; guardamos su identificador y tu nombre de miembro."),
            ("h2", "Qué más sale a internet"),
            ("ul", ["Tipos de cambio: la app descarga tipos públicos; tus movimientos e importes no se envían.",
                    "Comentarios: lo que escribes, los archivos que adjuntas tú (por ejemplo, un ticket de muestra), la versión de la app, el modelo del teléfono, un número de instalación aleatorio y el estado de la sincronización. No se adjuntan movimientos ni importes.",
                    "Código de invitación (prueba cerrada): el código y un número de instalación aleatorio se guardan en nuestra base de datos para contar los teléfonos por código; la app comprueba de vez en cuando si el código sigue valiendo.",
                    "Actualizaciones: la app comprueba si hay una versión nueva y la descarga de GitHub."]),
            ("h2", "Lo que no hacemos"),
            ("p", "No mostramos anuncios, no vendemos ni compartimos tus datos con terceros y no recopilamos analíticas de tus gastos."),
            ("h2", "Seguridad"),
            ("p", "Los datos viajan solo por HTTPS. Puedes bloquear la app con PIN o huella y ocultar los importes. Solo los miembros de la familia acceden a los datos de la nube."),
            ("h2", "Tus derechos"),
            ("p", "Puedes eliminar tus datos en cualquier momento: mira la página «Eliminar datos»."),
        ],
        "delete": [
            ("h2", "Datos en el teléfono"),
            ("p", "Desinstala la app y los datos del teléfono desaparecen. O toca «Borrar todos los datos» en «Más → Copia de seguridad»."),
            ("h2", "Nube de la familia"),
            ("ul", ["«Más → Familia → Salir de la familia»: dejas de ver los datos compartidos; siguen en tu teléfono.",
                    "«Más → Familia → Eliminar la familia» (quien la creó): los datos compartidos se borran de la nube para todos."]),
            ("h2", "¿No puedes eliminarlos?"),
            ("p", "Escríbenos desde la app: «Más → Opinión». Indica qué datos borrar; responderemos y los eliminaremos en un plazo razonable."),
        ],
    },
}

CSS = ("body{font:16px/1.5 system-ui,sans-serif;max-width:40rem;margin:0 auto;padding:1rem 16px 3rem;color:#1b1b1b;background:#fff}"
       "@media(prefers-color-scheme:dark){body{color:#eee;background:#111}a{color:#9cf}}"
       "h1{font-size:1.6rem}h2{font-size:1.15rem;margin-top:1.6rem}nav{font-size:.9rem;margin-bottom:1rem}")


def _body(blocks):
    out = []
    for kind, value in blocks:
        if kind == "ul":
            out.append("<ul>" + "".join(f"<li>{html.escape(item)}</li>" for item in value) + "</ul>")
        else:
            out.append(f"<{kind}>{html.escape(value)}</{kind}>")
    return "\n".join(out)


def render(kind, lang):
    """kind is `privacy` or `delete`; returns the full HTML of that page in `lang`."""
    t = TEXTS[lang]
    other = "delete" if kind == "privacy" else "privacy"
    title = t[f"{kind}_title"]
    nav = (f'<a href="../../{lang}/">{html.escape(t["back"])}</a> · '
           f'<a href="../../{other}/{lang}/">{html.escape(t[f"{other}_link"])}</a>')
    return (f'<!doctype html><html lang="{lang}"><head><meta charset="utf-8">'
            '<meta name="viewport" content="width=device-width, initial-scale=1">'
            '<meta name="robots" content="noindex, nofollow">'  # shared by link only, not in search (owner 2026-11-25)
            f"<title>{html.escape(title)}</title><style>{CSS}</style></head><body>"
            f"<nav>{nav}</nav><h1>{html.escape(title.split(' — ')[0])}</h1>\n{_body(t[kind])}</body></html>\n")


def write_all(out_dir):
    import os
    for kind in ("privacy", "delete"):
        for lang in LANGS:
            path = os.path.join(out_dir, kind, lang)
            os.makedirs(path, exist_ok=True)
            with open(os.path.join(path, "index.html"), "w", encoding="utf-8") as f:
                f.write(render(kind, lang))
