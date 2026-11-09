# Стиль кода

## Kotlin
- Официальный стиль Kotlin (`kotlin.code.style=official`), отступ 4 пробела, trailing commas.
- Код, идентификаторы и комментарии — на английском. Строки UI — только в `strings.xml` (русский).
- Неизменяемые данные: `val`, `data class`, `List` вместо `MutableList` в публичных API.
- Ошибки ввода — через sealed `Result`, а не исключения. Исключения — для нарушенных инвариантов
  (`require`, переполнение).
- Без `!!`. Без `GlobalScope`.

## Слои и поток данных (UDF)
```
Screen (stateless) --onAction--> ViewModel --> UseCase --> Repository --> Room / Remote
Screen <---- StateFlow<UiState> ---- ViewModel
```
- ViewModel отдаёт один `StateFlow<UiState>`; события от UI — методы или `onAction(Action)`.
- UiState содержит уже отформатированные строки — Compose не знает о `Money`-арифметике.
- Use case заводим только при наличии логики; простое чтение — сразу из репозитория.
- Репозиторий: интерфейс в `core:domain`, реализация в `core:data`.

## Compose
- `Screen` принимает state и лямбды, не ViewModel → его можно превьюить и тестировать.
- У каждого экрана есть `@Preview` в `CoShellTheme`.
- **В экранах — только компоненты и токены дизайн-системы** (`design-system.md`): кнопки, поля, строки,
  диалоги, карточки, шторки — `CoShell*`; отступы и размеры — `Spacing`/`Sizes`, анимации — `Motion`,
  цвета — `MaterialTheme` / `LocalAmountPalette` / `LocalStatusPalette`, текст — `typography`. Голый Material
  и числа `N.dp` ловят тесты `TactileRuleTest` и `TokensRuleTest`. Нужного компонента нет — добавь его в
  `core:designsystem`, а не рисуй в экране. Производительность Compose — `performance.md`.
- Суммы — стилем `AmountTextStyle` (табличные цифры). Крупная сумма — в одну строку с `autoSize`:
  неразрывные пробелы в `Money` иначе рвут строку посреди кода валюты («AR / S»).
- Edge-to-edge: внешний `Scaffold` (`CoShellNavHost`) поглощает только нижний отступ; строку
  статуса обрабатывает `Scaffold` экрана (через `padding` или `TopAppBar`).

## Иконки
- Только **Material Symbols Outlined, weight 300** (тонкие контурные), без заливки — и в выбранном состоянии тоже.
- Лежат в `core/designsystem/src/main/res/drawable/ic_<name>.xml`, в коде — `painterResource(DesignR.drawable.ic_<name>)`.
- Новая иконка: `npm pack @material-symbols/svg-300` в scratchpad, распаковать и выполнить
  `python3 scripts/material-symbol-to-vector.py <распаковка>/package/outlined core/designsystem/src/main/res/drawable <name>[:mirrored]`
  (`:mirrored` — для стрелок, зеркалятся в RTL). Имена — как на fonts.google.com/icons.
- Иконки категорий: в БД хранится **ключ** (`"food"`), не имя файла; новый — строка в
  `core/designsystem/.../category/CategoryIcons.kt`. Ключи не переименовывать (они в данных и бэкапах).

## DI (Hilt)
- `@HiltViewModel` + `@Inject constructor`. Модули — `app/.../di/` (позже в модулях data).
- Зависимости через интерфейсы там, где их надо подменять в тестах.
