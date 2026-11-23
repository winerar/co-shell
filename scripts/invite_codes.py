#!/usr/bin/env python3
"""Invitation codes for the closed test (#666, #667; run by .github/workflows/invite.yml).

  invite_codes.py create <label> [--max-devices N]   a new code for one person (label = who it is for)
  invite_codes.py revoke <code>                      the code stops working: the app locks entry on its next check
  invite_codes.py list                               every code: label, state, how many phones

A code is 12 characters from a 31-letter alphabet without look-alikes (about 59 bits), shown as XXXX-XXXX-XXXX; the Firestore
document `access_codes/<12 characters>` holds `label`, `createdAt`, `revoked`, `maxDevices`, `devices` (installation ids the app
adds itself, firebase/firestore.rules). The result goes to the run's summary ($GITHUB_STEP_SUMMARY) — visible to the repository's
owner only. The app side normalises what a person types the same way: core/domain/.../access/AccessCode.kt.

Environment: FIREBASE_SERVICE_ACCOUNT (key JSON, role "Cloud Datastore User"), FIREBASE_PROJECT_ID (optional).
"""
import argparse
import datetime
import json
import os
import secrets
import sys
import urllib.error
import urllib.parse

import feedback_to_issues as fb

ALPHABET = "ABCDEFGHJKMNPQRSTUVWXYZ23456789"  # no 0/O, 1/I/L: people read codes aloud and retype them
LENGTH = 12
DEFAULT_DEVICES = 3
LABEL_LENGTH = 80


def new_code(rng=secrets):
    return "".join(rng.choice(ALPHABET) for _ in range(LENGTH))


def normalize(text):
    """What a person typed → the 12-character id (upper case, no spaces or dashes); `None` if it cannot be a code."""
    code = "".join(ch for ch in text.upper() if ch.isalnum())
    return code if len(code) == LENGTH and all(ch in ALPHABET for ch in code) else None


def display(code):
    return "-".join(code[i:i + 4] for i in range(0, LENGTH, 4))


def clean_label(label):
    return " ".join(label.split())[:LABEL_LENGTH]


def document_fields(label, max_devices, now):
    return {
        "label": {"stringValue": clean_label(label)},
        "createdAt": {"timestampValue": now.strftime("%Y-%m-%dT%H:%M:%SZ")},
        "revoked": {"booleanValue": False},
        "maxDevices": {"integerValue": str(max_devices)},
        "devices": {"arrayValue": {}},
    }


def base(project):
    return f"{fb.FIRESTORE}/projects/{project}/databases/(default)/documents"


def create(project, token, label, max_devices, now=None):
    code = new_code()
    now = now or datetime.datetime.now(datetime.timezone.utc)
    url = f"{base(project)}/access_codes/{code}?currentDocument.exists=false"
    fb.request("PATCH", url, token, {"fields": document_fields(label, max_devices, now)})
    return code


def revoke(project, token, code):
    mask = urllib.parse.urlencode([("updateMask.fieldPaths", "revoked"), ("currentDocument.exists", "true")])
    fb.request("PATCH", f"{base(project)}/access_codes/{code}?{mask}", token, {"fields": {"revoked": {"booleanValue": True}}})


def rows(project, token):
    out = []
    url = f"{base(project)}/access_codes?pageSize=300"
    page = fb.request("GET", url, token)
    for doc in (page or {}).get("documents", []):
        f = fb.decode_document(doc)
        out.append({"code": f["_id"], "label": f.get("label", ""), "revoked": bool(f.get("revoked")),
                    "devices": len(f.get("devices") or []), "max": int(f.get("maxDevices", 0))})
    return sorted(out, key=lambda r: r["label"].lower())


def summary_created(code, label, max_devices):
    return (f"### Код приглашения\n\n**{display(code)}**\n\nДля: {clean_label(label)} · телефонов: до {max_devices}\n\n"
            "Передайте код этому человеку лично (не в общем чате). Ссылка на скачивание — на лендинге.\n")


def summary_revoked(code):
    return f"### Код отозван\n\n{display(code)}: приложение закроет ввод при следующей проверке (не позже чем через 3 дня, если есть интернет).\n"


def summary_list(items):
    if not items:
        return "### Коды приглашения\n\nКодов пока нет.\n"
    lines = ["### Коды приглашения", "", "| Код | Для кого | Состояние | Телефонов |", "|---|---|---|---|"]
    for r in items:
        state = "отозван" if r["revoked"] else "действует"
        lines.append(f"| {display(r['code'])} | {r['label']} | {state} | {r['devices']} из {r['max']} |")
    return "\n".join(lines) + "\n"


def emit(text):
    print(text)
    path = os.environ.get("GITHUB_STEP_SUMMARY")
    if path:
        with open(path, "a", encoding="utf-8") as f:
            f.write(text + "\n")


def main(argv=None):
    parser = argparse.ArgumentParser(description=__doc__.split("\n")[0])
    sub = parser.add_subparsers(dest="command", required=True)
    c = sub.add_parser("create")
    c.add_argument("label")
    c.add_argument("--max-devices", type=int, default=DEFAULT_DEVICES)
    r = sub.add_parser("revoke")
    r.add_argument("code")
    sub.add_parser("list")
    args = parser.parse_args(argv)

    account = json.loads(os.environ["FIREBASE_SERVICE_ACCOUNT"])
    project = os.environ.get("FIREBASE_PROJECT_ID") or account["project_id"]
    token = fb.google_token(account)
    if args.command == "create":
        if not clean_label(args.label) or not 1 <= args.max_devices <= 10:
            sys.exit("Нужно имя и число телефонов от 1 до 10")
        code = create(project, token, args.label, args.max_devices)
        emit(summary_created(code, args.label, args.max_devices))
    elif args.command == "revoke":
        code = normalize(args.code)
        if code is None:
            sys.exit("Это не похоже на код: нужно 12 знаков, например K7QX-M2PD-9WTA")
        try:
            revoke(project, token, code)
        except urllib.error.HTTPError as e:
            sys.exit("Такого кода нет" if e.code == 404 else f"Не удалось отозвать: HTTP {e.code}")
        emit(summary_revoked(code))
    else:
        emit(summary_list(rows(project, token)))


if __name__ == "__main__":
    main()
