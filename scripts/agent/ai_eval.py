#!/usr/bin/env python3
"""Accuracy/speed of small local models on the co-shell tasks (#587): category by payee, quick notes, voice phrase, unknown receipt.

Usage: python3 scripts/agent/ai_eval.py <model.gguf>... [--tasks category,notes,voice,receipt] [--limit N] [--out report.json]
Needs `pip install llama-cpp-python` (CPU wheel). Gold set — docs/samples/ai/eval.json (invented data). Speed here is a PC, not a phone:
use it to rank models by accuracy; real speed and memory are measured on the phone by the same cases (`docs/research/on-device-ai.md`).
"""
import argparse
import json
import os
import re
import resource
import sys
import time

ROOT = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
GOLD = os.path.join(ROOT, 'docs/samples/ai/eval.json')
TODAY = '2026-11-21'
SYS = 'You are a precise parser for a family budget app. Answer with one JSON object only, no comments.'


def schema(props, required=None):
    return {'type': 'object', 'properties': props, 'required': required or list(props)}


def minor(text):
    """'3500' / '6.99' / '3 500,50' → minor units (what the app's parser does), None if unreadable."""
    from decimal import Decimal, InvalidOperation
    t = str(text).replace(' ', '').replace('\u202f', '')
    if ',' in t and '.' in t:
        t = t.replace('.', '').replace(',', '.') if t.rfind(',') > t.rfind('.') else t.replace(',', '')
    elif re.fullmatch(r'\d{1,3}(\.\d{3})+', t):
        t = t.replace('.', '')  # Argentine thousands: 82.400 = 82400
    else:
        t = t.replace(',', '.')
    try:
        return int((Decimal(t) * 100).to_integral_value())
    except InvalidOperation:
        return None


def ask(llm, prompt, sch):
    t = time.time()
    r = llm.create_chat_completion(messages=[{'role': 'system', 'content': SYS}, {'role': 'user', 'content': '/no_think\n' + prompt}], temperature=0, max_tokens=200,
                                   response_format={'type': 'json_object', 'schema': sch})
    dt = time.time() - t
    text = r['choices'][0]['message']['content']
    try:
        out = json.loads(text)
    except ValueError:
        out = {}
    return out, dt, r['usage']['completion_tokens']


def task_category(llm, gold, limit):
    cats = gold['categories']
    ok = abstain = n = 0
    sch = schema({'category': {'type': 'string', 'enum': cats + ['unknown']}})
    for c in gold['payees'][:limit]:
        out, dt, tk = ask(llm, 'My categories: %s.\nThe payee of a purchase is "%s". Pick the one category it belongs to, or "unknown".' % (', '.join(cats), c['payee']), sch)
        got = out.get('category')
        ok += got == c['category']
        abstain += got == 'unknown'
        n += 1
        yield dt, tk
    yield ('score', 'category', ok, n, 'abstained %d' % abstain)


def task_notes(llm, gold, limit, label='notes'):
    sch = schema({'amount': {'type': 'string'}, 'title': {'type': 'string'}, 'day': {'type': 'integer'}})
    ok = amt = n = 0
    for c in gold[label][:limit]:
        text = c['line'] if label == 'notes' else c['phrase']
        out, dt, tk = ask(llm, 'Today is %s. Parse one spending note. amount = the amount as digits with a dot for decimals, no spaces, in the units written (3 500 -> 3500, 6,99 usd -> 6.99, \"к\"/k = thousand: 30к -> 30000, '
                          '"три пятьсот" -> 3500, "two thousand five hundred" -> 2500); title = short thing bought, same language; day = 0 today, -1 yesterday, -2 day before yesterday.\n'
                          'Example: "кофе 1 200 карта" -> {"amount": "1200", "title": "кофе", "day": 0}\nNote: "%s"' % (TODAY, text), sch)
        a = minor(out.get('amount', '')) == c['amount_minor']
        t = str(c['title']).split()[0].lower() in str(out.get('title', '')).lower()
        d = out.get('day', 0) == c.get('day', 0)
        amt += a
        ok += a and t and d
        n += 1
        yield dt, tk
    yield ('score', label, ok, n, 'amount only %d' % amt)


def task_voice(llm, gold, limit):
    yield from task_notes(llm, gold, limit, label='voice')


def task_receipt(llm, gold, limit):
    sch = schema({'total': {'type': 'string'}, 'currency': {'type': 'string'}, 'date': {'type': 'string'}, 'merchant': {'type': 'string'}})
    ok = n = 0
    for c in gold['receipts'][:limit]:
        text = c.get('text') or open(os.path.join(os.path.dirname(GOLD), c['file']), encoding='utf-8').read()
        out, dt, tk = ask(llm, 'Read this receipt. total = the final amount to pay, copied as written (digits, with its separators); currency = ISO code (a bare "$" in Argentina is ARS); '
                          'date = purchase date as YYYY-MM-DD; merchant = shop name.\n---\n%s' % text, sch)
        good = (minor(out.get('total', '')) == c['total'] and out.get('currency') == c['currency'] and out.get('date') == c['date']
                and c['merchant'].split()[0].lower() in str(out.get('merchant', '')).lower())
        ok += good
        n += 1
        yield dt, tk
    yield ('score', 'receipt', ok, n, '')


TASKS = {'category': task_category, 'notes': task_notes, 'voice': task_voice, 'receipt': task_receipt}


def run(path, tasks, limit):
    from llama_cpp import Llama
    t0 = time.time()
    llm = Llama(model_path=path, n_ctx=2048, n_threads=4, verbose=False)
    res = {'model': os.path.basename(path), 'size_mb': round(os.path.getsize(path) / 1e6), 'load_s': round(time.time() - t0, 1), 'tasks': {}}
    secs = toks = 0
    gold = json.load(open(GOLD, encoding='utf-8'))
    for name in tasks:
        for item in TASKS[name](llm, gold, limit):
            if item[0] == 'score':
                _, label, ok, n, extra = item
                res['tasks'][label] = {'ok': ok, 'of': n, 'pct': round(100 * ok / max(n, 1)), 'note': extra}
            else:
                secs += item[0]
                toks += item[1]
    res['tok_per_s'] = round(toks / secs, 1) if secs else 0
    res['peak_rss_mb'] = round(resource.getrusage(resource.RUSAGE_SELF).ru_maxrss / 1024)
    return res


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument('models', nargs='+')
    ap.add_argument('--tasks', default='category,notes,voice,receipt')
    ap.add_argument('--limit', type=int, default=1000)
    ap.add_argument('--out')
    a = ap.parse_args()
    report = [run(m, a.tasks.split(','), a.limit) for m in a.models]
    for r in report:
        print('%s (%d МБ, load %.1f s, %.1f tok/s, RSS %d МБ)' % (r['model'], r['size_mb'], r['load_s'], r['tok_per_s'], r['peak_rss_mb']))
        for k, v in r['tasks'].items():
            print('  %-9s %3d%%  %d/%d  %s' % (k, v['pct'], v['ok'], v['of'], v['note']))
    if a.out:
        json.dump(report, open(a.out, 'w'), ensure_ascii=False, indent=1)


if __name__ == '__main__':
    sys.exit(main())
