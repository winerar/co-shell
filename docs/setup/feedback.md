# Включить обратную связь из приложения

Как это работает: «Ещё → Обратная связь» → сообщение (текст + версия, модель телефона, Android,
id установки) пишется в Firestore, коллекция `feedback` → раз в 6 часов GitHub Action
`Feedback to issues` создаёт из новых сообщений issue с меткой `feedback` в `co-shell` →
агент в новой сессии читает issue и чинит пачкой. Токен GitHub в приложении не хранится.

Пока шаги ниже не сделаны, форма отвечает «В этой сборке отправка не настроена».

## 1. Создать базу Firestore (один раз)
1. console.firebase.google.com → проект co-shell → **Build → Firestore Database → Create database**.
2. Edition — Standard; режим — **Production**; регион — `southamerica-east1` (Сан-Паулу, ближе всего
   к Аргентине). Регион потом не меняется; эта же база будет для синхронизации (этап 4).

## 2. Правила доступа
Firestore → вкладка **Rules** → заменить всё содержимым файла `firebase/firestore.rules` → **Publish**.
Правила разрешают приложению только *добавлять* сообщения; читать и менять их из приложения нельзя.

## 3. Ключ Web API → переменная GitHub
1. Firebase → ⚙ **Project settings → General** → поле **Web API key**.
   Если там «No Web API key» — ниже **Add app → Android**, package name `app.coshell`, остальное
   пропустить (файл `google-services.json` не нужен); ключ появится.
2. Ключ не секретный (он есть в любом приложении на Firebase), но **не ограничивайте его
   «Android apps»** — приложение ходит в Firestore напрямую, и запрос будет отклонён. Ограничение
   по API (Cloud Firestore API) — можно.
3. GitHub → `co-shell` → **Settings → Secrets and variables → Actions → вкладка Variables** →
   **New repository variable**: `FIREBASE_API_KEY` = ключ. Там же должна быть `FIREBASE_PROJECT_ID`
   (уже есть).

## 4. Права сервисного аккаунта
Action читает Firestore ключом из секрета `FIREBASE_SERVICE_ACCOUNT`.
1. console.cloud.google.com → тот же проект → **IAM & Admin → IAM**.
2. Найти сервисный аккаунт из секрета (адрес `…@….iam.gserviceaccount.com`; поле `client_email`
   в JSON-ключе) → ✏ → **Add another role → Cloud Datastore User** → Save.

## 5. Проверить
1. Ключ попадает в приложение при сборке → нужен **релиз** (новая сборка из `main`).
2. В приложении: Ещё → Обратная связь → «Проверка» → Отправить → «Спасибо! Сообщение отправлено».
   В Firebase → Firestore → Data появится документ в `feedback`.
3. GitHub → **Actions → Feedback to issues → Run workflow** → через минуту в Issues — «Отзыв: Проверка».
   Дальше Action запускается сам каждые 6 часов.

## Если не работает
- В приложении «Не удалось отправить», а интернет есть → неверный ключ или правила не опубликованы
  (шаги 2–3).
- Action падает с `403` от Firestore → нет роли из шага 4; с `404` → не создана база (шаг 1).
