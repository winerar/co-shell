# Сборка и тесты

## Требования
- JDK 21 (байткод проекта — Java 17).
- Android SDK: `platforms;android-37.0`, `build-tools;36.0.0`. Путь — `ANDROID_HOME`
  или `sdk.dir` в `local.properties` (файл не коммитится).
- Gradle — через `./gradlew` (версия зафиксирована во wrapper). **Агент — только через `scripts/agent/agent.sh`**
  (`check`, `quick`, `test <модуль> [фильтр]`, `gradle <задачи>`): там зеркало Maven Central и автоповтор при 429 (#156).

## Установка SDK вручную (если setup-скрипт окружения не отработал)
Нужен доступ к `dl.google.com` (окружение **co-shell**).
```bash
mkdir -p /opt/android-sdk/cmdline-tools && cd /opt/android-sdk
curl -sSo /tmp/clt.zip https://dl.google.com/android/repository/commandlinetools-linux-9862592_latest.zip
unzip -q /tmp/clt.zip -d cmdline-tools && mv cmdline-tools/cmdline-tools cmdline-tools/latest
yes | cmdline-tools/latest/bin/sdkmanager --sdk_root=/opt/android-sdk --licenses >/dev/null
cmdline-tools/latest/bin/sdkmanager --sdk_root=/opt/android-sdk "platforms;android-37.0" "build-tools;36.0.0"
echo "sdk.dir=/opt/android-sdk" > <repo>/local.properties
```

## Команды
| Команда | Что делает |
|---|---|
| `./gradlew check` | lint всех Android-модулей + все unit-тесты |
| `./gradlew :core:model:jvmTest` | тесты модели (быстро) |
| `./gradlew :core:domain:jvmTest` | тесты use cases (на фейках) |
| `./gradlew :core:data:jvmTest` | тесты Room на JVM + перегенерация `core/data/schemas/` |
| `./gradlew assembleDebug` | debug APK → `app/build/outputs/apk/debug/` |
| `./gradlew :app:testDebugUnitTest` | unit-тесты приложения |

Перед коммитом обязательно: `./gradlew check assembleDebug`. Агенту — через `scripts/agent/check.sh` (или флоу `agent.sh ship`): то же самое,
но печатает только ошибки, упавшие тесты и итог (`Tests: N`, `OK`/`FAILED`) — экономит токены.

## Тесты
- Модель и домен — `kotlin.test` в `commonTest` (запускаются на JVM, позже и на iOS).
- Data — `core/data/src/jvmTest`, in-memory SQLite (подробности — `database.md`).
- ViewModel — JUnit + `kotlinx-coroutines-test` в `app/src/test`.
- **Смоук-тест каркаса** — `app/src/test/.../navigation/MainShellSmokeTest` (Robolectric + Compose test, ~8 с):
  `MainShell` (панель, NavHost, вкладки) с настоящими экранами вкладок на примерных данных — экран виден над
  панелью, все вкладки открываются, «+» срабатывает. Без Hilt/Room/сети (`application = Application::class`:
  `CoShellApp` открыл бы Room с нативным SQLite). Новая вкладка или поменял каркас — дополни тест.
- Правила Firestore и Realtime Database (`database.test.js`) и сквозной синк двух телефонов — в эмуляторе: `scripts/test-rules.sh` (правила) или
  `scripts/test-rules.sh --kotlin` (+ `FirestoreEmulatorTest`); нужны Node 20+ и Java, эмулятор скачивается
  сам. Поменял `firestore.rules`, `database.rules.json` или `core:data/remote` — прогони. В обычном `check` этот тест пропускается.
- Python-скрипты — `python3 -m unittest discover -s scripts -p 'test_*.py'` и то же для `-s scripts/ci` (шаг в CI).
- ViewModel с `flowOn(Dispatchers.Default)` в тесте: поток доезжает после `resetMain` и роняет **следующий** тест
  (`UncaughtExceptionsBeforeTest`). Делай контекст подменяемым — `internal var work = Dispatchers.Default`, `state by lazy`
  (`BudgetsViewModel`, `WalletImportViewModel`), в тесте `work = EmptyCoroutineContext`.
- **Скриншот-тесты** (#225) — Roborazzi на Robolectric (JVM, без эмулятора), `core/designsystem/.../screenshots/`:
  каталоги компонентов и токенов, нижняя панель, палитры; светлая и тёмная тема, `ru`, 411 dp, xhdpi. Эталоны —
  `core/designsystem/src/test/screenshots/*.png` в git; обычный `check` их **сравнивает** (`roborazzi.test.verify` в
  `gradle.properties`), расхождение = упавший тест + `build/outputs/roborazzi/*_compare.png` (в CI — артефакт `reports`).
  Намеренно поменял вид → `agent.sh shots` (перезапись), посмотри PNG, приложи к PR. Новый компонент → в каталог или
  отдельный `shot(...)`. Набор маленький: ~1,5 с на снимок.
- **Скриншоты экранов** (#226) — `app/.../screenshots/ScreensScreenshotTest.kt`: Обзор, Операции, Счета, Бюджеты на
  образцах из `*Previews.kt` (`internal val …PreviewSample`), обе темы; эталоны — `app/src/test/screenshots/`.
  Часы Compose стоят и проматываются на 3 с (каскад появления блоков доходит до конца). `agent.sh shots` пишет оба модуля.
- Имена тестов — предложение в обратных кавычках: `` fun `refuses to mix currencies`() ``.

## Версии
Все версии — в `gradle/libs.versions.toml`. AGP 9 встроенно компилирует Kotlin, поэтому
плагин `org.jetbrains.kotlin.android` **не** подключается; нужен только `kotlin.compose` для Compose.

## CI
Воркфлоу, что где проверяется и почему так быстро — [`ci.md`](ci.md).
`versionCode` = номер запуска CI на `main` (`BUILD_NUMBER`; локально и в PR — 1, `ci.md`); `versionName` = `0.N` по числу релизов (`VERSION_NAME`, там же). Debug подписан общим ключом
`app/debug.keystore` из репозитория (не секрет), пакет `app.coshell.debug`, название «co-shell debug».

## Обратная связь
- CI передаёт сборке переменные репозитория `FIREBASE_PROJECT_ID`, `FIREBASE_API_KEY` (Variables, не
  секреты) → `BuildConfig`; локально пусто → форма «не настроено». `feedback.yml` раз в 6 ч создаёт
  issue из Firestore `feedback` (секрет `FIREBASE_SERVICE_ACCOUNT`). Настройка — `docs/setup/feedback.md`.

## Самообновление (release)
- Включается, когда заданы секреты `RELEASE_KEYSTORE_BASE64`, `RELEASE_KEYSTORE_PASSWORD`,
  `RELEASE_KEY_ALIAS`, `RELEASE_KEY_PASSWORD` и `RELEASES_TOKEN` (fine-grained PAT, Contents: write
  только на `co-shell-releases`). Шаги владельца — `docs/setup/self-update.md`. Без них шаги release
  пропускаются, CI зелёный.
- CI: `assembleRelease` (ключ из секрета во временный файл,
  `UPDATE_BASE_URL=https://github.com/<owner>/co-shell-releases/releases/latest/download`) →
  `scripts/make-update-files.py` → `update-files/` → `gh release create` в **публичном** `co-shell-releases`.
- Firebase Hosting не подходит: на Spark запрещены исполняемые файлы, в т.ч. `.apk`.
- Локально release без ключа собирается неподписанным и с пустым `UPDATE_BASE_URL` (экран «не настроено»).
  Проверить R8/подпись: `keytool -genkeypair` во временный файл и те же переменные окружения.
- Текст «Что нового» — верхний раздел `docs/whats-new.md` (он публичен — без личных данных).
