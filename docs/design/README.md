# Дизайн Co-Shell — всё в одном месте

**Холст:** https://claude.ai/artifact/Kfegn8qM8QEnPKr7HRYpw5 — закреплён в боковой панели claude.ai
(«Co-Shell — дизайн»). Вкладки вверху холста: **Экраны · Воркфлоу · Компоненты · История решений**.
**Правило:** любое визуальное изменение сначала показываем здесь макетом, код — после «ок» владельца
(`docs/agents/workflow.md` §«Визуальные изменения»).

Макеты живые: кнопка Play на доске — нажимать вкладки, переключатели, шторку.

Холст — источник истины по виду. `mockups/` — копия его файлов в репозитории (читать без сети, видеть
историю правок в git). Поменял холст → скопируй файлы сюда тем же коммитом, что и код.

## Экраны → код (`app/src/main/kotlin/app/coshell/`)
| Доска (`mockups/`) | Экран | Код |
|---|---|---|
| `Main` — шторка ввода, 2 шага | Новая операция | `transactions/edit/`, `components/keypad/` |
| `Operations` | Операции | `transactions/list/` |
| `Accounts` | Счета (группы, итоги) | `accounts/list/` |
| `More` | Ещё | `more/` |
| `Feedback` | Обратная связь | `feedback/` |
| `Update`, `UpdatePill` | Обновление, пилюля над панелью | `update/`, `update/notice/` |
| `Colors` | Цвет счёта: банки + палитра | `category/BankColors`, `CategoryPalette` (designsystem) |
| `Categories`, `CategoryEdit` | Категории, редактор «Категория \| Папка» | `categories/list/`, `categories/edit/` |
| `AccountEdit` | Редактор счёта | `accounts/edit/` |
| `Rates` | Курсы валют | `rates/list/` |
| `ReportCurrencies` | Валюты отчётов | `currencies/` |
| `Family` | Семья и синхронизация | `family/` |
| `Backup` | Резервная копия | `backup/` |
| `Settings` | Настройки, 6 палитр | `settings/`, `theme/` |
| `SyncA`, `SyncB`, `SyncC` | Ошибка синка на «Обзоре», 3 варианта (#102) — **ждут выбора** | `home/` |
| `Wipe` | Стереть все данные (#107) — **ждёт «ок»** | `settings/` |
| `Flows` | Воркфлоу: главные действия по шагам | — (карта переходов, `navigation/`) |
| `Components` | Компоненты и токены | `core/designsystem/` (`components.md`) |
| `History` | Ссылки на ранние страницы выбора | — |

Нижняя панель (на досках `Operations`, `Accounts`, `More`, `UpdatePill`, `Components`) —
`CoShellBottomBar` + `navigation/MainBottomBar`: плавающая капсула 64 dp, вкладки без подписей,
пилюля 44 dp, «+» 52 dp (`Sizes.bottomBar*`).

## Ранние страницы (до холста)
- Новый облик (пружины, палитры, вёрстка): https://claude.ai/artifact/WqocoPvC6g6mLRt31WvcCW
- Выбор дизайна (#62–#66): https://claude.ai/artifact/DHcu66PAJq6rZwJL7uhhbo
- Иконка (#66): https://claude.ai/artifact/VYVhJ7roxaTLXpaBXhRFYh

## Правила для агента
- Новый экран или заметная переделка — сначала доска на этом же холсте (не новый артефакт), потом код.
- Не сделанное из макетов — в `docs/STATUS.md` («Не сделано из мокапов»).
- Правила токенов и компонентов — `docs/agents/design-system.md`, `components.md`, `motion.md`.
