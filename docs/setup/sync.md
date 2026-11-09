# Включить семью и синхронизацию (этап 4)

Как это работает: «Ещё → Семья и синхронизация» → вход через Google → первый участник создаёт
семью → приглашает второго по адресу Gmail → второй входит своим Google-аккаунтом и присоединяется.
Данные живут в Room на телефоне и копируются в Firestore (`families/main/records`); посторонний
Google-аккаунт видит «нет доступа» (правила в `firebase/firestore.rules`, issue #34).

Пока шаги 1–4 не сделаны, экран пишет «Синхронизация в этой сборке не настроена» — приложение
работает как раньше, только на телефоне. База Firestore и `FIREBASE_API_KEY` уже настроены
для обратной связи (`feedback.md`).

## 1. Включить вход через Google (Firebase)
1. console.firebase.google.com → проект → **Build → Authentication → Get started** (если ещё не).
2. Вкладка **Sign-in method → Add new provider → Google → Enable**, указать свой email
   поддержки → **Save**.
3. Там же раскрыть **Web SDK configuration** → скопировать **Web client ID**
   (вида `123…-abc….apps.googleusercontent.com`).
4. GitHub → `co-shell` → **Settings → Secrets and variables → Actions → Variables** →
   **New repository variable**: `GOOGLE_WEB_CLIENT_ID` = этот ID. Он не секретный.

## 2. Отпечатки ключей подписи (без них Google откажет во входе)
1. GitHub → **Actions → Signing fingerprints → Run workflow**. В итоге запуска (Summary) будут
   SHA-1 и SHA-256 для release (`app.coshell`) и debug (`app.coshell.debug`).
   Debug-отпечаток уже известен: SHA-1 `C4:07:C9:15:C6:2A:BE:6A:E8:1C:4B:B0:D5:98:DF:64:B2:9D:E2:29`.
2. Firebase → ⚙ **Project settings → General → Your apps**:
   - приложение Android `app.coshell` (если нет — **Add app → Android**, `google-services.json`
     скачивать не нужно) → **Add fingerprint** → release SHA-1, затем release SHA-256;
   - **Add app → Android** `app.coshell.debug` → debug SHA-1 (нужно только для debug-сборок).

## 3. Ограничения ключа Web API (если включали)
Google Cloud Console → **APIs & Services → Credentials** → ключ Web API: в «API restrictions»
должны быть разрешены **Cloud Firestore API**, **Identity Toolkit API** и **Token Service API**.
Без ограничений — тоже работает. «Application restrictions: Android apps» не ставить.

## 4. Обновить правила Firestore
Firestore → **Rules** → заменить всё содержимым `firebase/firestore.rules` → **Publish**.
Правила для отзывов в файле остались, добавились семья, приглашения и записи бюджета.
Проверка правил агентом — `scripts/test-rules.sh` (эмулятор).

## 5. Порядок на телефонах (после релиза)
1. **Владелец**: «Ещё → Семья и синхронизация» → **Войти через Google** → **Создать семью**.
   Данные этого телефона станут общими (отправятся в облако).
2. Там же → **Пригласить** → Gmail второго участника.
3. **Второй участник**: обновить приложение → «Ещё → Семья и синхронизация» → **Войти через Google**
   (тем самым адресом) → **Присоединиться к семье**. Если на телефоне были свои операции, приложение
   спросит: **Заменить** (рекомендуется; перед этим сохранится копия) или **Добавить в семью**.
4. Проверка: добавить операцию на одном телефоне — на другом она появится в течение 10–15 секунд, пока
   приложение открыто (в фоне — до часа).

## Если что-то не так
- «Не удалось войти через Google» — не добавлен отпечаток (шаг 2) или не тот `GOOGLE_WEB_CLIENT_ID`.
- «Семья уже создана» у владельца — семью успел создать кто-то другой: Firestore → удалить документ
  `families/main` и создать заново. Создать можно только одну семью — так задумано.
- «Нет доступа» у второго участника — приглашён другой адрес; пригласить тот, которым он входит.
