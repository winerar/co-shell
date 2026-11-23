# Облачный разбор: развернуть Worker (владельцу, #635)

Нужны: бесплатный аккаунт Cloudflare, ключ Claude API (console.anthropic.com → API keys, баланс ~$5), Node 20+.

1. `cd tools/ai-proxy && npx wrangler login`
2. `npx wrangler kv namespace create USAGE` → вставьте `id` в `wrangler.toml` (`[[kv_namespaces]]`).
3. В `wrangler.toml` впишите `FIREBASE_PROJECT_ID` (тот же, что в переменных репозитория).
4. `npx wrangler secret put ANTHROPIC_API_KEY` → вставьте ключ (в файлы и чат ключ не попадает).
5. `npx wrangler deploy` → адрес вида `https://co-shell-ai.<имя>.workers.dev`.
6. GitHub → Settings → Secrets and variables → Actions → **Variables** → `AI_PROXY_URL` = этот адрес. Следующая сборка получит облако.
7. В консоли Claude поставьте лимит расходов не выше баланса.

Проверка: без переключателя в приложении ничего не уходит; кнопка появится после «ок» по макету.
