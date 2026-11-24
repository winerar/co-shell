import test from "node:test";
import assert from "node:assert/strict";
import { handle } from "./worker.js";

const PROJECT = "demo-project";
const NOW = 1_800_000_000;
const enc = (o) => Buffer.from(JSON.stringify(o)).toString("base64url");

const pair = await crypto.subtle.generateKey(
  { name: "RSASSA-PKCS1-v1_5", modulusLength: 2048, publicExponent: new Uint8Array([1, 0, 1]), hash: "SHA-256" }, true, ["sign", "verify"]);
const jwk = { ...(await crypto.subtle.exportKey("jwk", pair.publicKey)), kid: "k1" };

async function token(over = {}, kid = "k1") {
  const head = enc({ alg: "RS256", kid });
  const payload = enc({ aud: PROJECT, iss: `https://securetoken.google.com/${PROJECT}`, sub: "uid1", iat: NOW - 10, exp: NOW + 3000, ...over });
  const sig = await crypto.subtle.sign("RSASSA-PKCS1-v1_5", pair.privateKey, new TextEncoder().encode(`${head}.${payload}`));
  return `${head}.${payload}.${Buffer.from(sig).toString("base64url")}`;
}

function setup({ limit = 2, upstream = { ok: true, body: { content: [{ type: "text", text: "[{\"i\":0}]" }] } } } = {}) {
  const store = new Map();
  const calls = [];
  const env = {
    FIREBASE_PROJECT_ID: PROJECT, DAILY_LIMIT: String(limit), ANTHROPIC_API_KEY: "sk-test",
    USAGE: { get: async (k) => store.get(k) ?? null, put: async (k, v) => void store.set(k, v) },
  };
  const fetch = async (url, init) => {
    calls.push({ url, init });
    if (url.includes("googleapis.com")) return { ok: true, json: async () => ({ keys: [jwk] }) };
    return { ok: upstream.ok, status: upstream.ok ? 200 : 500, json: async () => upstream.body };
  };
  return { env, fetch, calls };
}

const req = async (t, body = { system: "s", prompt: "p" }, method = "POST") =>
  new Request("https://w.example/", { method, headers: { authorization: `Bearer ${t}` }, body: method === "POST" ? JSON.stringify(body) : undefined });

test("valid token and body returns the model text", async () => {
  const { env, fetch, calls } = setup();
  const res = await handle(await req(await token()), env, { fetch, nowSec: NOW });
  assert.equal(res.status, 200);
  assert.deepEqual(await res.json(), { text: "[{\"i\":0}]", left: 1, limit: 2 });
  const sent = JSON.parse(calls.at(-1).init.body);
  assert.equal(sent.model, "claude-haiku-5-5");
  assert.match(sent.system, /^You only parse/);
  assert.equal(calls.at(-1).init.headers["x-api-key"], "sk-test");
});

test("bad signature, wrong audience, expired token and unknown key are refused", async () => {
  const { env, fetch } = setup();
  const good = await token();
  const forged = good.slice(0, -4) + "AAAA";
  for (const t of [forged, await token({ aud: "other" }), await token({ exp: NOW - 1 }), await token({}, "nope"), "junk", ""]) {
    const res = await handle(await req(t), env, { fetch, nowSec: NOW });
    assert.equal(res.status, 401);
  }
});

test("only POST and a sane body are accepted", async () => {
  const { env, fetch } = setup();
  const t = await token();
  assert.equal((await handle(await req(t, null, "GET"), env, { fetch, nowSec: NOW })).status, 405);
  assert.equal((await handle(await req(t, { system: "s" }), env, { fetch, nowSec: NOW })).status, 400);
  assert.equal((await handle(await req(t, { system: "s", prompt: "x".repeat(12001) }), env, { fetch, nowSec: NOW })).status, 400);
});

test("daily limit per person stops the third request", async () => {
  const { env, fetch } = setup({ limit: 2 });
  const t = await token();
  const codes = [];
  for (let i = 0; i < 3; i++) codes.push((await handle(await req(t), env, { fetch, nowSec: NOW })).status);
  assert.deepEqual(codes, [200, 200, 429]);
  const over = await handle(await req(t), env, { fetch, nowSec: NOW });
  assert.deepEqual(await over.json(), { error: "limit", left: 0, limit: 2 });
});

test("upstream failure becomes 502 without leaking details", async () => {
  const { env, fetch } = setup({ upstream: { ok: false, body: { error: "secret" } } });
  const res = await handle(await req(await token()), env, { fetch, nowSec: NOW });
  assert.equal(res.status, 502);
  assert.ok(!JSON.stringify(await res.json()).includes("secret"));
});
