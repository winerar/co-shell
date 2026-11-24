# Лендинг с загрузкой APK (GitHub Pages)

Страница на ru/en/es: кнопка «Скачать», версия, SHA-256, «нужен код приглашения», три шага установки. Собирает её `scripts/make_landing.py` из `site/index.template.html`
при каждом релизе (CI, шаг «Publish landing») и кладёт в ветку `gh-pages` публичного репозитория релизов `co-shell-releases`.
По умолчанию публикация **выключена**.

## Включить (один раз)
Общий порядок вместе с кодами — `closed-test.md`.
1. В репозитории `co-shell-releases`: **Settings → Pages → Build and deployment → Source: Deploy from a branch → Branch: `gh-pages` / `(root)` → Save.**
   (Ветка `gh-pages` появится после первой публикации — шаг 2 можно сделать раньше и вернуться.)
2. В `co-shell` → Settings → Secrets and variables → Actions → Variables → **New repository variable**: `LANDING` = `true`.
   Секрет `RELEASES_TOKEN` уже есть (самообновление): ему нужен доступ Contents: write к `co-shell-releases`.
3. Выпустить релиз. Адрес страницы: `https://<ваш-логин>.github.io/co-shell-releases/` (через минуту после публикации).
4. Собственный домен — по желанию: Settings → Pages → Custom domain.

## Что дальше
Политика и «удалить данные» (#652) уже на сайте: `/privacy/<язык>/`, `/delete/<язык>/` (`scripts/privacy_pages.py`; текст сверять с реальным трафиком приложения и формой «Безопасность данных»). Ссылка из «Ещё → Настройки» — после доски и «ок». QR-код для телефона и скриншоты установки Xiaomi/HyperOS — позже (#660).
Тексты страницы — словарь `TEXTS` в `scripts/make_landing.py` (все языки сразу; тест следит, что ключи одинаковы).
