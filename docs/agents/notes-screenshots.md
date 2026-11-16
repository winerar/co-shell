# Заметки: скриншоты, Robolectric, Roborazzi

Часть `notes.md` (тот же формат). Правила снимков — `build-and-test.md` §«Тесты».

- (11-10) Скриншот экрана для сверки с доской без эмулятора: временный тест Robolectric с
  `@GraphicsMode(GraphicsMode.Mode.NATIVE)`, `qualifiers = "w390dp-h844dp-xxhdpi"`, `MainShell` + экран на данных
  доски, `onRoot().captureToImage().asAndroidBitmap()` → PNG в scratchpad; тест не коммитить. Работает (~20 с). Месяцы по-русски — `@Config(qualifiers = "ru")`;
  постоянные эталоны — Roborazzi (`build-and-test.md` §«Тесты»).
- (11-12) Roborazzi `captureRoboImage(path) { … }` снимает раньше, чем доходит каскад `staggeredEntrance` (на «Обзоре» не было
  «Последних операций») — экраны снимать через `createAndroidComposeRule` + `mainClock.autoAdvance = false` + `advanceTimeBy`.
- (11-12) Roborazzi-витрина `components_*` обрезается высотой экрана Robolectric: новое внизу `ComponentsCatalog` в эталон не
  попадает — новому компоненту отдельный снимок (так `row_group_*` в `CatalogScreenshotTest`).
- (11-12) Редакторы (счёт, категория) без `Scaffold` — в Roborazzi без `Surface` тёмная тема снимается на белом; `ScreensScreenshotTest.shot` оборачивает в `Surface`.
- (11-12) `CoShellSheet` вне навигации — в `Dialog(properties = CoShellSheetDialogProperties)` (так шторки фильтров #141); тело шторки — отдельная `ColumnScope`-функция, иначе Roborazzi его не снимет.
- (11-12) Анимации появления на `Animatable` + `LaunchedEffect` (графики `charts/`, `CoShellMiniBars`) в Roborazzi `captureRoboImage` доходят до конца — эталон снят с полными столбцами.
- (11-14) Robolectric: `@Config(qualifiers = "en")` на методе **заменяет** классовые квалификаторы (снимок 320×470) — для языка пиши `"+en"`.
- (11-14) С `values-en` Robolectric без `ru` в `qualifiers` показывает английский — тестам с русским текстом нужен `ru-…` (так в `MainShellSmokeTest`).
- (11-16, #378) Стресс-данные для снимков — только через `MoneyFormat.RU` (группы — узкий неразрывный пробел, перед кодом — неразрывный): с обычными пробелами суммы рвутся на строки, которых в приложении нет. Набор — `ScreensStressScreenshotTest` + `*StressSamples.kt` в пакете экрана.
- (11-16, #431) «≈ » перед суммой — с неразрывным пробелом (`"≈" + MoneyFormat.NBSP`): иначе при `autoSize` + `maxLines = 1` строка переносится после «≈» и видно только его. В `Row` с длинным текстом и суммой у обоих `weight(1f)` (сумма — в `Box(CenterEnd)` с `autoSize`), иначе первый невзвешенный сжимает второго в столбик по букве.
