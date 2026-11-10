# co-shell

Android-приложение для совместного учёта семейного бюджета.

**Название:** *co-shell* — игра слов: читается как «кошел(ёк)», а по-английски
*co-* — «совместный», *shell* — «оболочка/раковина», то есть «общий кошелёк»,
одна «скорлупа» для денег всей семьи.

## Статус
Этап 0 завершён: каркас проекта, модель денег, CI. См.:
- [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) — архитектура и стек
- [docs/ROADMAP.md](docs/ROADMAP.md) — план разработки
- [docs/design/](docs/design/README.md) — дизайн: макеты экранов, воркфлоу, компоненты ([холст](https://claude.ai/artifact/Kfegn8qM8QEnPKr7HRYpw5))
- [AGENTS.md](AGENTS.md) — правила для разработки (людям и ИИ-агентам)

## Сборка
```bash
./gradlew check assembleDebug
```
Нужны JDK 21 и Android SDK (подробности — [docs/agents/build-and-test.md](docs/agents/build-and-test.md)).
