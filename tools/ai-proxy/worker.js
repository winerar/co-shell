// Cloudflare Worker: the only door from the app to the Claude API (#635).
// Checks the Firebase ID token, limits requests per person per day, adds a fixed guard and calls Claude Haiku.
// The Claude API key lives only in the Worker secrets (ANTHROPIC_API_KEY); the app never sees it.

const JWKS_URL = "https://www.googleapis.com/service_accounts/v1/jwk/securetoken@system.gserviceaccount.com";
const MODEL = "claude-haiku-5-5";
const MAX_SYSTEM = 4000;
const MAX_PROMPT = 12000;
const MAX_ANSWER_TOKENS = 1024;
const DEFAULT_DAILY_LIMIT = 100;
const GUARD = "You only parse short personal-finance notes, receipts and spoken phrases for a budget app, " +
  "and answer in the exact format the task asks for. Refuse anything else with an empty answer.";

const json = (body, status = 200) =>
  new Response(JSON.stringify(body), { status, headers: { "content-type": "application/json" } });

const b64urlBytes = (s) => Uint8Array.from(atob(s.replace(/-/g, "+").replace(/_/g, "/")), (c) => c.charCodeAt(0));
const b64urlJson = (s) => JSON.parse(new TextDecoder().decode(b64urlBytes(s)));

/** Returns the uid of a valid Firebase ID token for [projectId], or `null`. */
export async function verifyIdToken(token, { projectId, nowSec, fetchFn }) {
  try {
    const [h, p, s] = String(token).split(".");
    if (!h || !p || !s) return null;
    const header = b64urlJson(h);
    const payload = b64urlJson(p);
    if (header.alg !== "RS256" || !header.kid) return null;
    const keys = (await (await fetchFn(JWKS_URL)).json()).keys ?? [];
    const jwk = keys.find((k) => k.kid === header.kid);
    if (!jwk) return null;
    const key = await crypto.subtle.importKey("jwk", jwk, { name: "RSASSA-PKCS1-v1_5", hash: "SHA-256" }, false, ["verify"]);
    const ok = await crypto.subtle.verify("RSASSA-PKCS1-v1_5", key, b64urlBytes(s), new TextEncoder().encode(`${h}.${p}`));
    if (!ok) return null;
    if (payload.aud !== projectId || payload.iss !== `https://securetoken.google.com/${projectId}`) return null;
    if (!(payload.exp > nowSec) || payload.iat > nowSec + 60) return null;
    return typeof payload.sub === "string" && payload.sub ? payload.sub : null;
  } catch {
    return null;
  }
}

export async function handle(request, env, deps = {}) {
  const fetchFn = deps.fetch ?? fetch;
  const nowSec = deps.nowSec ?? Math.floor(Date.now() / 1000);
  if (request.method !== "POST") return json({ error: "method" }, 405);

  const bearer = (request.headers.get("authorization") ?? "").replace(/^Bearer /, "");
  const uid = await verifyIdToken(bearer, { projectId: env.FIREBASE_PROJECT_ID, nowSec, fetchFn });
  if (!uid) return json({ error: "auth" }, 401);

  let body;
  try { body = await request.json(); } catch { return json({ error: "body" }, 400); }
  const { system, prompt } = body ?? {};
  if (typeof system !== "string" || typeof prompt !== "string" || !prompt || system.length > MAX_SYSTEM || prompt.length > MAX_PROMPT) {
    return json({ error: "body" }, 400);
  }

  // Counter per person per day. KV is not atomic: a burst may overshoot slightly, the prepaid balance is the hard cap.
  const day = new Date(nowSec * 1000).toISOString().slice(0, 10);
  const counterKey = `u:${uid}:${day}`;
  const used = Number((await env.USAGE.get(counterKey)) ?? 0);
  if (used >= Number(env.DAILY_LIMIT ?? DEFAULT_DAILY_LIMIT)) return json({ error: "limit" }, 429);
  await env.USAGE.put(counterKey, String(used + 1), { expirationTtl: 2 * 24 * 3600 });

  const reply = await fetchFn("https://api.anthropic.com/v1/messages", {
    method: "POST",
    headers: { "x-api-key": env.ANTHROPIC_API_KEY, "anthropic-version": "2023-06-01", "content-type": "application/json" },
    body: JSON.stringify({
      model: MODEL,
      max_tokens: MAX_ANSWER_TOKENS,
      thinking: { type: "disabled" },
      output_config: { effort: "low" },
      system: `${GUARD}\n\n${system}`,
      messages: [{ role: "user", content: prompt }],
    }),
  });
  if (!reply.ok) return json({ error: "upstream", status: reply.status }, 502);
  const data = await reply.json();
  const text = (data.content ?? []).filter((b) => b.type === "text").map((b) => b.text).join("");
  return json({ text });
}

export default { fetch: (request, env) => handle(request, env) };
