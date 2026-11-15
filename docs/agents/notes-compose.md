# Заметки агента: Compose и Material 3

Грабли Compose, Material 3 и компилятора Compose, найденные в работе (вынесено из `notes.md`).
Формат тот же: `- (дата) факт — что из этого следует`.

- (11-12) Compose `lerp(Color, Color, t)` смешивает в **Oklab**, не в sRGB — подбирать цвета/контраст заранее скриптом с Oklab (как в #249), иначе числа не сойдутся с `ColorContrastTest`. (11-13, #271) Тёмный герой — узкое место новых палитр: яркий `primary` (Лимон) валит подсказку → темнее `backgroundDark`.
- (11-10) `Modifier.drawBehind` на `NavigationBar`/`Surface` рисует **под** их фоном — фон закрасит рисунок.
  Фон ставить своим `background()` перед `drawBehind`, а `containerColor = Transparent` (так пилюля вкладки).
- (11-09) `rememberGraphicsLayer().record{}` хранит ссылки на слои детей, а не их копию — снимок «старого» кадра
  делать сразу `toImageBitmap()` до смены (так `motion/ThemeReveal.kt`), иначе дети перерисуются уже новыми.
- (11-09) `MaterialTheme(motionScheme = …)` и интерфейс `MotionScheme` в material3 **1.4.0 закрыты** (`internal`),
  в `1.5.0-rc01` открыты — поэтому версия material3 закреплена в `libs.versions.toml`; при выходе стабильной 1.5 — перейти на неё.
- (11-09) Пересечение касаний в Compose: родительский `detectTapGestures` получает тап по ребёнку, если тот не
  поглотил событие (`Surface` без `onClick` не поглощает) — скрим шторки сделан **соседом** под ней, а не родителем.
- (11-09) `AnimatedVisibility` внутри колонки с `spacedBy`: зазор появляется/исчезает рывком вместе с ребёнком —
  отступ класть внутрь раскрывашки, а её — в колонку, где всегда что-то видно (так в форме операции).
- (11-09) Lint `UnusedContentLambdaTargetStateParameter`: в `AnimatedContent` рисовать из параметра лямбды, а не из
  внешнего состояния; ключ анимации — `contentKey` (иначе уходящий кадр показывает уже новое состояние).
- (11-09) M3 красит `primary` и как заливку, и как текст (`TextButton`, фокус поля, `Tab`, «сегодня» в `DatePicker`).
  При светлом неоновом `primary` текстовые места переопределять на `accentInk()`, иначе текст исчезает на белом.
- (11-14) Robolectric снимает `Popup` (второй корень `onAllNodes(isRoot())[1]`) **чёрным** — для эталона/сверки выносить содержимое попапа в отдельную функцию (так `PeekCard`). Долгое нажатие в UI-тесте: `performTouchInput { down(center) }` + `mainClock.advanceTimeBy(1000)` (`advanceEventTime` таймаут жеста не двигает).
- (11-15, #383) Отчёты компилятора Compose (`reportsDestination`) кэш сборки **не восстанавливает**: `compileDebugKotlin FROM-CACHE` — папки отчётов нет. Поэтому проверка — `agent.sh stability` с `--rerun`, не тест в `check`. Без `compose-stability.conf` нестабильны все `LocalDate`/`YearMonth` и классы `core:model` (модуль без компилятора Compose); рекурсивный класс (`children: List<Сам себя>`) — только `@Immutable`.
